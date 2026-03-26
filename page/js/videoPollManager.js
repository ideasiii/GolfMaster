/**
 * VideoPollManager — 輪詢 DB 確認轉檔影片是否完成。
 *
 * Python 轉檔流程：轉檔成功 → 才寫入 analyze URL 到 DB。
 * 因此 DB 有值 = 轉檔完成、檔案可用。
 *
 * 使用方式（JSP 端）：
 *   const poller = new VideoPollManager({
 *       statusUrl: 'VideoStatus',   // Servlet 路徑
 *       shotDataId: '12345',        // shot_data_id
 *       interval: 3000,             // 輪詢間隔（毫秒）
 *       maxAttempts: 40,            // 最大輪詢次數
 *       initialDelay: 3000,         // 首次檢查前的等待時間（毫秒）
 *   });
 *   // 僅對尚未就緒的影片呼叫 watch
 *   if (!frontAnalyzReady) poller.watch(videoEl, 'front');
 *   if (!sideAnalyzReady)  poller.watch(videoEl1, 'side');
 */
class VideoPollManager {
    /**
     * @param {Object} options
     * @param {string} options.statusUrl    - VideoStatusServlet 的相對路徑
     * @param {string} options.shotDataId   - shot_data_id（用於查詢 DB）
     * @param {number} options.interval     - 輪詢間隔（毫秒），預設 3000
     * @param {number} options.maxAttempts  - 最大輪詢次數，預設 40
     * @param {number} options.initialDelay - 首次檢查前等待（毫秒），預設 3000
     */
    constructor(options = {}) {
        this.statusUrl = options.statusUrl || 'VideoStatus';
        this.shotDataId = options.shotDataId || '';
        this.interval = options.interval || 3000;
        this.maxAttempts = options.maxAttempts || 40;
        this.initialDelay = options.initialDelay || 3000;
    }

    /**
     * 執行輪詢，直到該 camera 的影片就緒或達到上限。
     *
     * @param {HTMLVideoElement} videoEl - <video> DOM 元素
     * @param {string} camera           - 'front' 或 'side'
     * @param {number} attempt          - 目前第幾次嘗試
     */
    _poll(videoEl, camera, attempt) {
        if (attempt >= this.maxAttempts) {
            console.warn('[VideoPoll] 已達輪詢上限 (' + this.maxAttempts + ')，停止檢查: ' + camera);
            return;
        }

        var self = this;
        var url = this.statusUrl + '?shotDataId=' + encodeURIComponent(this.shotDataId);

        fetch(url)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                var info = data[camera];
                if (info && info.ready && info.url) {
                    // DB 已有 analyze URL → 轉檔完成，重新載入影片
                    var sourceEl = videoEl.querySelector('source');
                    if (sourceEl) {
                        sourceEl.setAttribute('src', info.url);
                    }
                    videoEl.load();
                    console.log('[VideoPoll] 轉檔完成，重新載入 ' + camera + ': ' + info.url);
                } else {
                    setTimeout(function () {
                        self._poll(videoEl, camera, attempt + 1);
                    }, self.interval);
                }
            })
            .catch(function () {
                setTimeout(function () {
                    self._poll(videoEl, camera, attempt + 1);
                }, self.interval);
            });
    }

    /**
     * 監控一個 <video> 元素，在 initialDelay 後開始輪詢 DB。
     * 呼叫前應先由 JSP 判斷該影片是否需要輪詢（analyze URL 尚未就緒時才呼叫）。
     *
     * @param {HTMLVideoElement} videoEl - <video> DOM 元素
     * @param {string} camera           - 'front' 或 'side'
     */
    watch(videoEl, camera) {
        if (!videoEl || !this.shotDataId) return;

        var self = this;
        console.log('[VideoPoll] 排程輪詢 ' + camera + '，將在 ' + self.initialDelay + 'ms 後開始');

        setTimeout(function () {
            self._poll(videoEl, camera, 0);
        }, self.initialDelay);
    }
}