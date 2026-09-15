/**
 * @fileoverview puttShotDataManager.js — 推桿頁影片下方那一塊：擊球數據卡片 ⇄ 回饋。
 *
 * ⭐ 比照 expert-data-v8.jsp / -short.jsp：預設顯示擊球數據卡片，⇄ 切換。
 *    推桿頁多一條：這一推判定完成時，預設改成顯示回饋（推桿風險）。
 * ⚠️ 回饋那一面的內容由 puttingIssuesManager.js 畫，這支只管卡片與切換。
 *
 * 卡片來源：PuttingShotData.processPuttValues() 的 shotCards（模擬器量到的原始值）。
 * ⚠️ 沒有值顯示「--」（跟另外兩頁一樣），⛔ 卡片不收起來。
 * ⚠️ 0 是合法值（出球方向、發射角度），⛔ 不可以當成沒有值。
 * ⛔ 球速⛔ 不放卡片：它在右欄數值面板，同一個數字⛔ 不可以在畫面上出現兩次。
 */

/* 卡片的小數位數。⛔ 值⛔ 不另外換算，只在顯示時取位數。 */
const PUTT_SHOT_CARD_DIGITS = {
    distToPinFt: 1,
    launchDirection: 1,
    launchAngle: 1,
    smashFactor: 2,
};


class PuttShotDataManager {

    /**
     * @param {Object} opts
     * @param {string} opts.feedbackId 整塊的容器 id（切換靠它身上的 data-mode class）
     * @param {string} opts.cardsId    卡片容器 id，裡面每一格數字標 data-card-key
     * @param {string} opts.toggleId   ⇄ 鈕的 id
     */
    constructor(opts) {
        this.feedbackEl = document.getElementById(opts.feedbackId);
        this.cardsEl = document.getElementById(opts.cardsId);
        const toggleEl = opts.toggleId ? document.getElementById(opts.toggleId) : null;
        const self = this;
        if (toggleEl) {
            toggleEl.addEventListener('click', function () { self.toggle(); });
        }
    }

    /**
     * @param {Object} cards {distToPinFt, launchDirection, launchAngle, smashFactor}，
     *                       沒有值的那一格是 null；查不到這一推時整包可以是 null
     */
    setCards(cards) {
        if (!this.cardsEl) return;
        const c = cards || {};
        this.cardsEl.querySelectorAll('[data-card-key]').forEach(function (el) {
            const key = el.dataset.cardKey;
            const v = c[key];
            const digits = PUTT_SHOT_CARD_DIGITS[key];
            el.textContent = (typeof v === 'number' && isFinite(v))
                ? v.toFixed(digits === undefined ? 1 : digits)
                : '--';
        });
    }

    /**
     * 判定完成 → 預設顯示回饋；否則顯示卡片。
     * @param {boolean} judgementComplete puttingIssuesManager.js 的 puttJudgementComplete() 結果
     */
    showDefault(judgementComplete) {
        this.setMode(judgementComplete ? 'feedback' : 'data');
    }

    /** @param {string} mode 'feedback'（回饋）或 'data'（擊球數據卡片） */
    setMode(mode) {
        if (!this.feedbackEl) return;
        this.feedbackEl.classList.toggle('data-mode', mode !== 'feedback');
    }

    toggle() {
        if (!this.feedbackEl) return;
        this.setMode(this.feedbackEl.classList.contains('data-mode') ? 'feedback' : 'data');
    }
}
