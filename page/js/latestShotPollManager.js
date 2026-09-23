/**
 * @fileoverview latestShotPollManager.js — 定期確認同一個廠商（LID）有沒有新的一推。
 *
 * 每隔幾秒問 /service/LatestShot?LID=xxx，拿到的是 shot_data 的最大 id。
 * 比頁面載入時那一推新 → follow 為 true 重新載入目前網址，false 則顯示一行可點的提示。
 *
 * ⭐ 重新載入⛔ 不需要知道新那一推的 id：網址帶著 LID，
 *    伺服器端本來就會查「該 LID 最新的一推」。
 *
 * ⛔ 沒帶 LID 就⛔ 不輪詢 —— LID 是啟用開關。
 *    ⛔ 不要改成「沒帶就用這一推的 LID」：那會讓所有只帶 expert 的既有網址突然自己跳頁。
 * ⛔ 查詢失敗、逾時、回傳看不懂 → 安靜略過等下一輪，⛔ 不可以擋住頁面、⛔ 不在畫面上喊錯。
 */

class LatestShotPollManager {
    /**
     * @param {Object} cfg
     * @param {string} cfg.lid                 網址的 LID；空字串＝⛔ 不輪詢
     * @param {string|number} cfg.currentShotId 這一頁正在看的 shot_data id
     * @param {string} [cfg.follow]            '0' 只顯示提示；其餘（含沒帶）＝自動重新載入
     * @param {string} [cfg.sec]               幾秒問一次，預設 15，夾在 5〜600
     * @param {string} [cfg.endpoint]          預設 'LatestShot'（相對於 /service/）
     */
    constructor(cfg) {
        cfg = cfg || {};
        this.lid = (cfg.lid === null || cfg.lid === undefined) ? '' : String(cfg.lid).trim();
        this.endpoint = cfg.endpoint || 'LatestShot';
        this.baseline = LatestShotPollManager.toId(cfg.currentShotId);
        // ⚠️ 只有明確寫 '0' 才是「只提示」，其餘一律自動
        this.follow = String(cfg.follow === undefined || cfg.follow === null ? '1' : cfg.follow) !== '0';
        this.intervalMs = LatestShotPollManager.toInterval(cfg.sec);
        this.timeoutMs = 5000;
        this.timer = null;
        this.noticeEl = null;
    }

    static toId(value) {
        const n = parseInt(value, 10);
        return isNaN(n) ? null : n;
    }

    /** ⚠️ 下限 5 秒（再短就是在打資料庫）、上限 10 分鐘；看不懂就回預設的 15 秒 */
    static toInterval(sec) {
        const n = parseInt(sec, 10);
        const safe = isNaN(n) ? 15 : Math.min(Math.max(n, 5), 600);
        return safe * 1000;
    }

    actedKey() {
        return 'latestShotActed:' + this.lid;
    }

    /** ⚠️ sessionStorage 在無痕視窗或關掉儲存時會丟例外，讀寫都要包起來 */
    readActed() {
        try {
            return LatestShotPollManager.toId(window.sessionStorage.getItem(this.actedKey()));
        } catch (e) {
            return null;
        }
    }

    writeActed(id) {
        try {
            window.sessionStorage.setItem(this.actedKey(), String(id));
        } catch (e) {
            /* ⛔ 寫不進去也不能擋住重載 */
        }
    }

    start() {
        if (!this.lid) return;
        if (this.timer) return;
        const self = this;
        this.timer = setInterval(function () { self.check(); }, this.intervalMs);
    }

    stop() {
        if (this.timer) clearInterval(this.timer);
        this.timer = null;
    }

    check() {
        const self = this;
        // ⚠️ 帶一個時間戳，避免中間有快取把回應留住
        const url = this.endpoint + '?LID=' + encodeURIComponent(this.lid) + '&_=' + Date.now();
        // ⚠️ 逾時就放棄這一輪，⛔ 不要讓請求無限期掛著
        const controller = (typeof AbortController === 'function') ? new AbortController() : null;
        const timer = controller ? setTimeout(function () { controller.abort(); }, this.timeoutMs) : null;

        fetch(url, controller ? { signal: controller.signal } : undefined)
            .then(function (resp) { return resp.ok ? resp.json() : null; })
            .then(function (data) {
                if (timer) clearTimeout(timer);
                if (!data) return;
                const latest = LatestShotPollManager.toId(data.latestId);
                if (latest === null) return;
                self.onLatest(latest);
            })
            .catch(function () {
                if (timer) clearTimeout(timer);
            });
    }

    onLatest(value) {
        // ⚠️ 自己再驗一次：NaN 跟任何數字比都是 false，會一路走到重載
        const latest = LatestShotPollManager.toId(value);
        if (latest === null) return;
        if (this.baseline !== null && latest <= this.baseline) return;
        const acted = this.readActed();
        // ⚠️⚠️ 同一個 id 只處理一次。最新那一推若還沒有 expert 表的列，
        //      重載之後畫面仍是舊那一推（LID 查詢是 INNER JOIN expert），
        //      ⛔ 少了這道防線會每一輪重載一次。
        if (acted !== null && latest <= acted) return;
        this.writeActed(latest);
        if (this.follow) {
            window.location.reload();
        } else {
            this.showNotice();
        }
    }

    /**
     * follow=0 時的提示：放在頁首按鈕列（.navigation-buttons）的最右邊，
     * 跟三個分頁鈕同一排；那一列是 flex 橫排，margin-left:auto 會把它推到最右。
     * ⚠️ 找不到那一列時退回右下角固定位置，⛔ 不要讓提示因為版面不同就不見。
     * ⚠️ 三個頁面的樣式表各自獨立，所以用行內樣式，⛔ 不必為了這一塊改三份 CSS。
     * ⚠️ 字級⛔ 不要再調小（投影機情境）。
     */
    showNotice() {
        if (this.noticeEl) return;
        const el = document.createElement('button');
        el.type = 'button';
        // ⚠️ 三頁共用這一句，所以講「一桿」⛔ 不講「一推」
        el.textContent = '有新的一桿，點此查看';
        // ⚠️ 接在 .header 這一層，⛔ 不是 .navigation-buttons：
        //    按鈕列是靠左排、寬度只有內容那麼寬，接進去會落在畫面中間。
        //    .header 才是撐滿整列的那一層，margin-left:auto 才推得到最右邊。
        const host = (typeof document.querySelector === 'function')
            ? document.querySelector('.header')
            : null;
        if (host) {
            el.className = 'latest-shot-notice';
            host.appendChild(el);
        } else {
            el.className = 'latest-shot-notice is-floating';
            document.body.appendChild(el);
        }
        el.addEventListener('click', function () { window.location.reload(); });
        this.noticeEl = el;
        // 提示已經在畫面上，再問也沒有意義；點下去會重載
        this.stop();
    }
}
