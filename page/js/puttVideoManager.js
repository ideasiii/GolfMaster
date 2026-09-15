/**
 * @fileoverview puttVideoManager.js — 推桿頁的影片控制：
 *   界標跳幀、「▶ 看這一段」播放區間、播放／暫停、全螢幕、影片輪詢換片。
 *
 * ⛔ 兩支影片的幀號完全不可互換（同一次推擊偏移 35/36/22/60，⛔ 不是常數）：
 *    界標與跳段只作用在正面影片，側面⛔ 不跟著跳、也⛔ 不標示。
 * ⚠️ 幀號換秒一律用 setFrameRate() 給的幀率（由界標推導，⛔ 不是寫死的 60）。
 *    幀率不是正數時⛔ 一律不跳 —— 那時界標本來就全部不可信，這裡是第二道防線。
 * ⛔ 這支不判斷界標可不可信：可信度由 puttPanelManager（界標鈕）與
 *    puttingIssuesManager（跳段鈕）決定，不可信的根本不會呼叫到這裡。
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

    /** @param {number} frame 正面影片的幀號 */
    goToFrame(frame) {
        const time = this.timeOfFrame(frame);
        if (time === null) return;
        this.seek(this.frontVideo, time);
        this.setPlaying(false);
    }

    /**
     * ⭐⭐「▶ 看這一段」——⛔ 這不是裝飾，是「碰球界標找錯」唯一的現場檢查手段。
     *     跳到區間起點播到終點；教練看到的不是下桿，當場就會發現界標抓錯。
     */
    playSegment(startFrame, endFrame) {
        const startTime = this.timeOfFrame(startFrame);
        const endTime = this.timeOfFrame(endFrame);
        if (startTime === null || endTime === null) return;
        const self = this;
        const video = this.frontVideo;
        this.goToFrame(startFrame);
        const stopAtEnd = function () {
            if (video.currentTime >= endTime) {
                video.pause();
                video.removeEventListener('timeupdate', stopAtEnd);
                self.setPlaying(false);
            }
        };
        video.addEventListener('timeupdate', stopAtEnd);
        video.play();
        this.setPlaying(true);
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
