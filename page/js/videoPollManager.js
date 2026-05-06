/**
 * VideoPollManager — 輪詢分析狀態並決定何時 reload / AJAX 補上單邊資料。
 *
 * Python 端寫入順序：
 *   raw_shotVideo_X     ← 廠商送進來
 *   analyze_shotVideo_X ← 轉檔完成（影片可播）
 *   id_analyzeVideo_X   ← 影像分析完成（PSystem/SwingPlane/TPI/PoseImpact 寫入 SVS）
 *
 * VideoStatusServlet 一次回傳 front/side 各自的：
 *   { expected, ready (=轉檔完成), url, analysisReady (=分析完成) }
 *   外加整體 shouldReload。
 *
 * 兩種使用方式：
 *  A) start({...}) — v8.jsp 用：等所有 expected 邊都 analysisReady → reload；
 *     如果兩邊都 expected，先 ready 那邊先做 AJAX 補上覆蓋線（左上角陸續顯示），
 *     第二邊 ready 才整頁 reload（左下角 TPI/教練比較才有完整資料）。
 *  B) watch(videoEl, camera) — 舊頁面（v8-short.jsp 等）用：等轉檔完成換 src。
 */
class VideoPollManager {
    /**
     * @param {Object} options
     * @param {string} options.statusUrl    - VideoStatus servlet 路徑
     * @param {string} [options.analysisUrl] - AnalysisData servlet 路徑（start 模式才需要）
     * @param {string} options.shotDataId
     * @param {number} [options.interval=3000]
     * @param {number} [options.maxAttempts=40]
     * @param {number} [options.initialDelay=3000]
     */
    constructor(options = {}) {
        this.statusUrl = options.statusUrl || 'VideoStatus';
        this.analysisUrl = options.analysisUrl || 'AnalysisData';
        this.shotDataId = options.shotDataId || '';
        this.interval = options.interval || 3000;
        this.maxAttempts = options.maxAttempts || 40;
        this.initialDelay = options.initialDelay || 3000;

        this._reloadFired = false;
        this._frontReady = false;
        this._sideReady = false;
        this._frontExpected = false;
        this._sideExpected = false;
        this._expectedCount = 0;
        this._onAnalysisUpdate = null;
        this._onVideoReady = null;
        this._frontVideoSwapped = false;
        this._sideVideoSwapped = false;
        this._attempts = 0;
    }

    // ============================================================
    // A) start 模式：完整分析輪詢（v8.jsp）
    // ============================================================

    /**
     * @param {Object} opts
     * @param {boolean} opts.frontExpected - 廠商會送 front 影片
     * @param {boolean} opts.sideExpected  - 廠商會送 side 影片
     * @param {boolean} opts.frontReady    - JSP 載入時 front 已 analysisReady
     * @param {boolean} opts.sideReady     - JSP 載入時 side 已 analysisReady
     * @param {Function} [opts.onAnalysisUpdate] - (camera, sideData) => void
     *        camera = 'front' | 'side'
     *        sideData = { frames: [a,t,i,f], swingPlane: {data: {...}} }
     * @param {Function} [opts.onVideoReady] - (camera, url) => void
     *        當該邊轉檔完成、analyze_shotVideo_X 寫入時觸發；和分析就緒分開，
     *        只觸發一次（之後 poll 不再呼叫）。前端可用來換 <source> src。
     */
    start(opts = {}) {
        if (!this.shotDataId) return;

        this._frontExpected = !!opts.frontExpected;
        this._sideExpected = !!opts.sideExpected;
        this._frontReady = !!opts.frontReady;
        this._sideReady = !!opts.sideReady;
        this._expectedCount = (this._frontExpected ? 1 : 0) + (this._sideExpected ? 1 : 0);
        this._onAnalysisUpdate = typeof opts.onAnalysisUpdate === 'function' ? opts.onAnalysisUpdate : null;
        this._onVideoReady = typeof opts.onVideoReady === 'function' ? opts.onVideoReady : null;

        if (this._expectedCount === 0) {
            console.log('[VideoPoll] no expected video, polling skipped');
            return;
        }
        if ((!this._frontExpected || this._frontReady) && (!this._sideExpected || this._sideReady)) {
            console.log('[VideoPoll] all expected sides already ready at load time, polling skipped');
            return;
        }

        var self = this;
        console.log('[VideoPoll] start: frontExpected=' + this._frontExpected
            + ' sideExpected=' + this._sideExpected
            + ' frontReady=' + this._frontReady
            + ' sideReady=' + this._sideReady);
        setTimeout(function () { self._poll(); }, this.initialDelay);
    }

    _poll() {
        if (this._reloadFired) return;
        if (this._attempts >= this.maxAttempts) {
            console.warn('[VideoPoll] timeout after ' + this.maxAttempts + ' attempts');
            // timeout fallback：至少有一邊 expected 已 ready，仍 reload 拿可用結果
            var anyReady = (this._frontExpected && this._frontReady)
                        || (this._sideExpected && this._sideReady);
            if (anyReady) {
                console.log('[VideoPoll] timeout but at least one side ready, reload');
                this._fireReload();
            }
            return;
        }
        this._attempts++;

        var self = this;
        var url = this.statusUrl + '?shotDataId=' + encodeURIComponent(this.shotDataId);
        fetch(url)
            .then(function (res) { return res.json(); })
            .then(function (data) { self._handleStatus(data); })
            .catch(function (err) { console.warn('[VideoPoll] status fetch error', err); })
            .then(function () {
                if (!self._reloadFired) {
                    setTimeout(function () { self._poll(); }, self.interval);
                }
            });
    }

    _handleStatus(data) {
        if (!data) return;
        var front = data.front || {};
        var side = data.side || {};

        // 轉檔完成（與分析就緒分開判斷）→ 通知前端換 <source> src，只觸發一次
        if (this._onVideoReady) {
            if (this._frontExpected && !this._frontVideoSwapped && front.ready && front.url) {
                this._frontVideoSwapped = true;
                this._onVideoReady('front', front.url);
            }
            if (this._sideExpected && !this._sideVideoSwapped && side.ready && side.url) {
                this._sideVideoSwapped = true;
                this._onVideoReady('side', side.url);
            }
        }

        var frontNewlyReady = this._frontExpected && !this._frontReady && !!front.analysisReady;
        var sideNewlyReady = this._sideExpected && !this._sideReady && !!side.analysisReady;

        if (frontNewlyReady) this._frontReady = true;
        if (sideNewlyReady) this._sideReady = true;

        // 雙邊情境才做單邊 AJAX 補上覆蓋線；單邊情境直接等 shouldReload
        if (this._expectedCount === 2 && this._onAnalysisUpdate) {
            if (frontNewlyReady) this._fetchAndApply('front');
            if (sideNewlyReady) this._fetchAndApply('side');
        }

        if (data.shouldReload && !this._reloadFired) {
            this._fireReload();
        }
    }

    _fetchAndApply(camera) {
        var self = this;
        var url = this.analysisUrl + '?shotDataId=' + encodeURIComponent(this.shotDataId);
        fetch(url)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var sideData = data ? data[camera] : null;
                if (sideData && self._onAnalysisUpdate) {
                    self._onAnalysisUpdate(camera, sideData);
                    console.log('[VideoPoll] applied analysis update for ' + camera);
                }
            })
            .catch(function (err) {
                console.warn('[VideoPoll] analysis fetch error', err);
            });
    }

    _fireReload() {
        if (this._reloadFired) return;
        this._reloadFired = true;
        console.log('[VideoPoll] reloading page to apply analysis');
        location.reload();
    }

    // ============================================================
    // B) watch 模式：舊版 src 替換（v8-short.jsp 等）
    // ============================================================

    /**
     * 監視一個 <video>，等 servlet 回傳 ready=true（轉檔完成）→ 換 src。
     * 不處理分析就緒。
     */
    watch(videoEl, camera) {
        if (!videoEl || !this.shotDataId) return;
        var self = this;
        console.log('[VideoPoll watch] schedule ' + camera + ' in ' + this.initialDelay + 'ms');
        setTimeout(function () { self._watchPoll(videoEl, camera, 0); }, this.initialDelay);
    }

    _watchPoll(videoEl, camera, attempt) {
        if (attempt >= this.maxAttempts) {
            console.warn('[VideoPoll watch] timeout: ' + camera);
            return;
        }
        var self = this;
        var url = this.statusUrl + '?shotDataId=' + encodeURIComponent(this.shotDataId);
        fetch(url)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var info = data ? data[camera] : null;
                if (info && info.ready && info.url) {
                    var sourceEl = videoEl.querySelector('source');
                    if (sourceEl && sourceEl.getAttribute('src') !== info.url) {
                        sourceEl.setAttribute('src', info.url);
                        videoEl.load();
                        console.log('[VideoPoll watch] swapped ' + camera + ' to ' + info.url);
                    }
                } else {
                    setTimeout(function () { self._watchPoll(videoEl, camera, attempt + 1); }, self.interval);
                }
            })
            .catch(function () {
                setTimeout(function () { self._watchPoll(videoEl, camera, attempt + 1); }, self.interval);
            });
    }
}