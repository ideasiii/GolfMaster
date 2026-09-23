/**
 * @fileoverview puttVideoManager.js — 推桿頁的影片控制：
 *   界標跳幀、「▶ 看這一段」播放區間、播放／暫停、全螢幕、影片輪詢換片。
 *
 * ⛔⛔ 兩支影片的幀號完全不可互換（同一推實測偏移 7〜240 幀，⛔ 不是常數，
 *    ⛔ 同一推內各界標的偏移也各不相同）→ **每一支影片只用它自己那一列的界標**。
 *    ⭐ 所以這裡是以「界標代碼」(address/top/impact/finish/onset) 跳，⛔ 不是以幀號跳：
 *    鈕說要看頂點，兩支影片各自去查自己的頂點在第幾幀。
 * ⚠️ 側面跟不跟得動，看側面自己那一列可不可信 —— 側面不可信就⛔ 不跳，
 *    ⛔ 也⛔ 不標示（規劃 §2.8：界標鈕以正面為準，側面只在它自己也可信時跟著跳）。
 * ⚠️ 界標一定要是**畫面上那一支影片自己的**：影片退回示範片、而界標是這一推的，
 *    幀號完全對不上 → setCameraLandmarks() 第三個參數傳 false，那一支⛔ 不跳。
 *    ⭐ 示範影片帶了自己的界標時就傳 true，它跟一般的推一樣可以跳。
 * ⚠️ 幀號換秒優先用 Core 給的秒數，其次才是「幀號 ÷ 幀率」（幀率由界標推導，
 *    ⛔ 不是寫死的 60）。幀率不是正數又沒有秒數時⛔ 一律不跳。
 * ⛔ 這支不判斷界標可不可信，只照傳進來的 trust 走：
 *    可信度由 puttPanelManager（界標鈕）與 puttingIssuesManager（跳段鈕）決定。
 *
 * 依賴：swingVideo.js（setupVideoEvents、resizeCanvas）、videoPollManager.js。
 */

class PuttVideoManager {

    /**
     * @param {Object} opts
     * @param {string} opts.frontVideoId / sideVideoId         兩支 video 的 id
     * @param {string} opts.frontCanvasId / sideCanvasId       疊在影片上的 canvas id
     * @param {string} opts.frontContainerId / sideContainerId 全螢幕用的外框 id
     * @param {string} opts.playButtonId                       Play / Pause 鈕的 id
     */
    constructor(opts) {
        this.frontVideo = document.getElementById(opts.frontVideoId);
        this.sideVideo = document.getElementById(opts.sideVideoId);
        this.frontCanvas = document.getElementById(opts.frontCanvasId);
        this.sideCanvas = document.getElementById(opts.sideCanvasId);
        this.frontContainer = document.getElementById(opts.frontContainerId);
        this.sideContainer = document.getElementById(opts.sideContainerId);
        this.playButton = document.getElementById(opts.playButtonId);
        this.frameRate = null;
        // 每一支影片自己的界標：{front: {marks, fps}, side: {...}}
        // ⚠️ 沒有這個鍵 = 呼叫端還沒交代這一支（舊頁面只交代正面）→ 走最下面的舊路徑。
        this.cameras = {};
    }

    /**
     * 交代某一支影片自己的界標。
     *
     * @param {string} camera 'front' 或 'side'
     * @param {Object|null} derived derivePuttPhases() 的回傳值；
     *                      null＝這一支沒有自己的分析列（⛔ 那就一顆都不跳）
     * @param {boolean} landmarksMatchVideo 這一份界標是不是**畫面上這一支影片自己的**。
     *                      ⛔ 影片退回示範片、而界標是這一推的 → 一定要傳 false：
     *                      那些幀號跟畫面上這支毫無關係，跳過去⛔ 不會報錯，
     *                      只會安靜停在錯的地方。
     *                      ⭐ 示範影片帶了自己的界標（人工標註那一份）時就是 true，
     *                      ⭐ 那時它跟一般的推一樣可以跳 ——
     *                      ⛔ 判準是「界標是不是這支影片的」，⛔ 不是「是不是示範片」。
     */
    setCameraLandmarks(camera, derived, landmarksMatchVideo) {
        const marks = {};
        const fps = (derived && derived.fps > 0) ? derived.fps : null;
        if (derived && landmarksMatchVideo !== false) {
            const frames = Object.assign({ onset: derived.onset }, derived.phases);
            const seconds = derived.seconds || {};
            const trust = derived.trust || {};
            Object.keys(frames).forEach(function (key) {
                const frame = frames[key];
                if (typeof frame !== 'number' || isNaN(frame) || trust[key] !== true) return;
                // Core 給的秒數優先；沒有才用幀號 ÷ 幀率
                const sec = (typeof seconds[key] === 'number' && isFinite(seconds[key]))
                    ? seconds[key]
                    : (fps > 0 ? frame / fps : null);
                if (sec === null || !isFinite(sec) || sec < 0) return;
                marks[key] = sec;
            });
        }
        this.cameras[camera] = { marks: marks, fps: fps };
    }

    /** 這一支影片在那個界標的秒數；不可信、沒有值、或那一支沒交代過都回 null。 */
    cameraTime(camera, key) {
        const cam = this.cameras[camera];
        if (!cam || !key) return null;
        const sec = cam.marks[key];
        return (typeof sec === 'number') ? sec : null;
    }

    /** @param {number|null} fps derivePuttPhases() 的 fps；⛔ 不要 round */
    setFrameRate(fps) {
        this.frameRate = fps;
    }

    /**
     * 界標幀號 → Core 給的秒數。有對到秒數的幀號直接用那個秒數，
     * 其餘退回「幀號 ÷ 幀率」。
     * @param {Object} frames  {address, top, impact, finish, onset} 幀號
     * @param {Object} seconds {address, top, impact, finish, onset} 秒數，沒有的是 null；舊資料整包是 null
     */
    setLandmarkSeconds(frames, seconds) {
        this.landmarkSeconds = [];
        if (!frames || !seconds) return;
        const list = this.landmarkSeconds;
        Object.keys(frames).forEach(function (key) {
            if (typeof frames[key] === 'number' && typeof seconds[key] === 'number') {
                list.push({ frame: frames[key], sec: seconds[key] });
            }
        });
    }

    /** 幀號換秒；換不出來回 null。 */
    timeOfFrame(frame) {
        if (typeof frame !== 'number' || isNaN(frame)) return null;
        if (!(this.frameRate > 0)) return null;
        const hit = (this.landmarkSeconds || []).filter(function (m) { return m.frame === frame; })[0];
        return hit ? hit.sec : frame / this.frameRate;
    }

    /* =================================================================
     * 跳幀與跳段
     * ================================================================= */

    /**
     * 兩支影片各自跳到同一個界標。
     *
     * @param {number} frame 界標鈕上的幀號（⚠️ 那是判定用的那一列的幀號）
     * @param {string} [key] 界標代碼 address/top/impact/finish
     *
     * ⛔ 側面⛔ 絕不可以拿 frame 去跳 —— 它是另一支影片的幀號。
     *    側面只查自己那一份界標，查不到或不可信就不動（⛔ 也不標示）。
     */
    goToFrame(frame, key) {
        const frontTime = this.cameras.front
            ? this.cameraTime('front', key)
            : this.timeOfFrame(frame);   // 舊頁面只交代正面時走這條
        const sideTime = this.cameraTime('side', key);

        let moved = false;
        if (frontTime !== null && this.frontVideo) { this.seek(this.frontVideo, frontTime); moved = true; }
        if (sideTime !== null && this.sideVideo) { this.seek(this.sideVideo, sideTime); moved = true; }
        if (moved) this.setPlaying(false);
    }

    /**
     * ⭐⭐「▶ 看這一段」——⛔ 這不是裝飾，是「碰球界標找錯」唯一的現場檢查手段。
     *     跳到區間起點播到終點；教練看到的不是下桿，當場就會發現界標抓錯。
     *
     * @param {number} startFrame / endFrame 判定那一列的起迄幀號
     * @param {string} [startKey] / [endKey] 起迄的界標代碼（onset/top/impact/finish）
     *
     * ⚠️ 側面要跟著播，**兩端都要在側面自己那一份裡可信**；
     *    ⛔ 只有一端對得上就⛔ 不播側面 —— 半段區間看不出東西，反而像壞掉。
     */
    playSegment(startFrame, endFrame, startKey, endKey) {
        const frontStart = this.cameras.front
            ? this.cameraTime('front', startKey)
            : this.timeOfFrame(startFrame);
        const frontEnd = this.cameras.front
            ? this.cameraTime('front', endKey)
            : this.timeOfFrame(endFrame);
        const sideStart = this.cameraTime('side', startKey);
        const sideEnd = this.cameraTime('side', endKey);

        const frontOk = this.frontVideo && frontStart !== null && frontEnd !== null && frontEnd > frontStart;
        const sideOk = this.sideVideo && sideStart !== null && sideEnd !== null && sideEnd > sideStart;
        if (!frontOk && !sideOk) return;

        if (frontOk) this.playRange(this.frontVideo, frontStart, frontEnd);
        if (sideOk) this.playRange(this.sideVideo, sideStart, sideEnd);
        this.setPlaying(true);
    }

    /** 一支影片跳到 startTime 播到 endTime 就停。 */
    playRange(video, startTime, endTime) {
        const self = this;
        this.seek(video, startTime);
        const stopAtEnd = function () {
            if (video.currentTime >= endTime) {
                video.pause();
                video.removeEventListener('timeupdate', stopAtEnd);
                // 兩支都停了才把鈕切回 Play
                if (self.allPaused()) self.setPlaying(false);
            }
        };
        video.addEventListener('timeupdate', stopAtEnd);
        video.play();
    }

    /** 畫面上的影片是不是都停了（⚠️ 缺哪一支就不算它）。 */
    allPaused() {
        return [this.frontVideo, this.sideVideo]
            .filter(function (v) { return !!v; })
            .every(function (v) { return v.paused; });
    }

    seek(videoEl, time) {
        if (videoEl.readyState >= 2) {
            videoEl.pause();
            videoEl.currentTime = time;
        } else {
            videoEl.addEventListener('loadedmetadata', function () {
                videoEl.pause();
                videoEl.currentTime = time;
            });
        }
    }

    /* =================================================================
     * 播放控制（同 expert-data-v8-short.jsp）
     * ================================================================= */

    bindControls() {
        const self = this;
        const front = this.frontVideo;
        const side = this.sideVideo;

        window.addEventListener('resize', function () { self.resizeCanvases(); });

        const onFullScreenChange = function () {
            // 給瀏覽器一點時間更新 DOM 尺寸，再重算兩個畫布
            setTimeout(function () { self.resizeCanvases(); }, 150);
        };
        ['fullscreenchange', 'webkitfullscreenchange', 'mozfullscreenchange', 'MSFullscreenChange']
            .forEach(function (name) { document.addEventListener(name, onFullScreenChange); });

        if (this.frontContainer) {
            front.addEventListener('dblclick', function () { self.toggleFullScreen(self.frontContainer); });
        }
        if (this.sideContainer) {
            side.addEventListener('dblclick', function () { self.toggleFullScreen(self.sideContainer); });
        }

        this.playButton.addEventListener('click', function () { self.playPause(); });
        front.addEventListener('ended', function () { self.handleEnded(); });
        side.addEventListener('ended', function () { self.handleEnded(); });

        // ⛔ 推桿頁不畫揮桿平面覆蓋線 → swingPlaneData 傳 null（swingVideo.js 有防呆）
        // ⚠️ 兩支都從第 0 幀開始。第 5 個參數是除數，起始幀是 0 所以用不到；
        //    ⛔ 不可以填 60 —— 真的拿去換算會偏掉。
        setupVideoEvents(front, this.frontCanvas, null, 0, 1, false);
        setupVideoEvents(side, this.sideCanvas, null, 0, 1, true);
    }

    resizeCanvases() {
        resizeCanvas(this.frontVideo, this.frontCanvas, null, false);
        resizeCanvas(this.sideVideo, this.sideCanvas, null, true);
    }

    toggleFullScreen(containerElement) {
        if (!document.fullscreenElement) {
            if (containerElement.requestFullscreen) {
                containerElement.requestFullscreen();
            } else if (containerElement.webkitRequestFullscreen) { // Safari
                containerElement.webkitRequestFullscreen();
            } else if (containerElement.msRequestFullscreen) { // IE11
                containerElement.msRequestFullscreen();
            }
        } else if (document.exitFullscreen) {
            document.exitFullscreen();
        }
    }

    playPause() {
        if (this.frontVideo.paused && this.sideVideo.paused) {
            this.frontVideo.play();
            this.sideVideo.play();
            this.setPlaying(true);
        } else {
            this.frontVideo.pause();
            this.sideVideo.pause();
            this.setPlaying(false);
        }
    }

    handleEnded() {
        const front = this.frontVideo;
        const side = this.sideVideo;
        if (front.ended && side.ended) {
            this.setPlaying(false);
        } else if ((front.ended && !side.paused) || (side.ended && !front.paused)) {
            this.setPlaying(true);
        }
    }

    setPlaying(playing) {
        this.playButton.className = playing ? 'pause' : 'play';
        this.playButton.innerText = playing ? 'Pause' : 'Play';
    }

    /* =================================================================
     * 影片輪詢：轉檔完成就先換 src（不等分析），讓使用者更早看到自己的影片
     * ================================================================= */

    /**
     * @param {VideoPollManager} poller
     * @param {Object} state {frontExpected, sideExpected, frontReady, sideReady}
     * ⛔ 推桿頁沒有 SwingPlane 覆蓋線要補，所以⛔ 不傳 onAnalysisUpdate。
     */
    startPolling(poller, state) {
        const self = this;
        poller.start({
            frontExpected: state.frontExpected,
            sideExpected: state.sideExpected,
            frontReady: state.frontReady,
            sideReady: state.sideReady,
            onVideoReady: function (camera, url) {
                const videoEl = (camera === 'front') ? self.frontVideo : self.sideVideo;
                const sourceEl = videoEl.querySelector('source');
                if (!sourceEl || sourceEl.getAttribute('src') === url) return;
                sourceEl.setAttribute('src', url);
                videoEl.load();
                console.log('[onVideoReady] swapped ' + camera + ' to ' + url);
            },
        });
    }
}
