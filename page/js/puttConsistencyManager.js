/**
 * @fileoverview puttConsistencyManager.js — 推桿頁右欄上方的「推桿穩定度」圖。
 *
 * 這張圖回答「這個人穩不穩」，⛔ 不是「這一推準不準」。
 * ⚠️ 目前畫的是固定的示意圖（還沒接真資料）。
 *
 * ═══ 真的接資料時的算法（⛔ 不要改成「離洞多遠」）═══
 * 兩軸都必須是**碰球瞬間量到的值**，⛔ 不可以用滾動後的結果：
 *   橫軸 ＝ shot_data.LaunchDirection 相對「自己的平均」（出球偏左／偏右）
 *   縱軸 ＝ shot_data.BallSpeed       相對「自己的平均」（推太強／推太弱）
 * ⛔ 縱軸⛔ 不是距離 —— TotalDistFt 是模擬器用草皮摩擦係數滾出來的。
 * ⛔ 不可以用 ShortGameData 的 landing_points：那是球飛行模型幾何反推的，對推桿不成立。
 *   · 原點 ⊕ ＝ 這幾推的平均，⛔ 不是洞、⛔ 不是目標
 *   · 虛線圈 ＝ 散布範圍（一個標準差）
 * ⚠️ 力道軸要依 DistToPinFt 分層（2 呎和 30 呎的球速本來就差很多），基準是
 *    「自己在同一個距離帶的平均」；⛔ 樣本不足時不畫，⛔ 絕不可退回全域平均。
 * ⚠️ 不同 LID 的資料⛔ 不要直接倒在一起算標準差（量測標準不同）。
 * ⭐ 要畫時用 Chart.js（page/js/chart_4_4_0.umd.min.js），⛔ 不要延用下面的手寫 SVG。
 *
 * ═══ 顏色（比照 shortTableManager.js 的落點圖）═══
 *   最新這一推 ＝ 亮綠 rgba(0,255,132,1)，半徑放大 ＋ 白框
 *   之前幾推   ＝ 亮黃 rgb(255,206,86)，越新 alpha 越高（0.35〜0.90）
 *   平均點與散布圈 ＝ 亮藍 rgb(54,162,235)
 * ⚠️ alpha ⛔ 不要調回切桿頁的 0.20 —— 這一頁投在投影機上，最舊那幾顆會看不見。
 * ⚠️ 圖說的記號顏色寫在 GM08_putt.css 的 .putt-consistency-legend .mk-*，兩邊要一致。
 * ⛔ 圖上⛔ 不出現任何絕對距離數字；⛔ 不做雷達圖、⛔ 不做曲線球。
 */

const PUTT_CONSISTENCY_TEXT = {
    // ⚠️ 標題講這張圖回答什麼（穩不穩），⛔ 不要寫成「最近 N 推」
    title: '推桿穩定度',
    // ⛔ 不可以講成「準不準」
    note: '越集中越穩定',
};

/* ⚠️ 示意圖：投影機會把低對比吃掉，軸線與文字⛔ 不可以用暗灰、字級⛔ 不要再往下調。 */
const PUTT_CONSISTENCY_PLACEHOLDER_SVG =
    '<svg viewBox="0 0 280 160" width="100%" height="100%" role="img" aria-label="推桿落點散布示意">'
    + '<line x1="140" y1="16" x2="140" y2="144" stroke="#8f979b" stroke-width="1.4"/>'
    + '<line x1="50" y1="80" x2="230" y2="80" stroke="#8f979b" stroke-width="1.4"/>'
    + '<text x="140" y="12" fill="#ffffff" font-size="15" text-anchor="middle">推太強</text>'
    + '<text x="140" y="157" fill="#ffffff" font-size="15" text-anchor="middle">推太弱</text>'
    + '<text x="46" y="85" fill="#ffffff" font-size="15" text-anchor="end">偏左</text>'
    + '<text x="234" y="85" fill="#ffffff" font-size="15" text-anchor="start">偏右</text>'
    + '<circle cx="137" cy="80" r="28" fill="none" stroke="rgba(54, 162, 235, 0.85)" stroke-width="1.4" stroke-dasharray="4 4"/>'
    + '<circle cx="122" cy="64" r="3.4" fill="rgba(255, 206, 86, 0.90)"/>'
    + '<circle cx="151" cy="72" r="3.4" fill="rgba(255, 206, 86, 0.83)"/>'
    + '<circle cx="134" cy="95" r="3.4" fill="rgba(255, 206, 86, 0.76)"/>'
    + '<circle cx="160" cy="88" r="3.4" fill="rgba(255, 206, 86, 0.69)"/>'
    + '<circle cx="118" cy="90" r="3.4" fill="rgba(255, 206, 86, 0.62)"/>'
    + '<circle cx="128" cy="78" r="3.4" fill="rgba(255, 206, 86, 0.56)"/>'
    + '<circle cx="156" cy="103" r="3.4" fill="rgba(255, 206, 86, 0.49)"/>'
    + '<circle cx="112" cy="72" r="3.4" fill="rgba(255, 206, 86, 0.42)"/>'
    + '<circle cx="143" cy="83" r="3.4" fill="rgba(255, 206, 86, 0.35)"/>'
    + '<circle cx="147" cy="55" r="5.2" fill="rgba(0, 255, 132, 1)" stroke="rgba(255,255,255,0.8)" stroke-width="1.6"/>'
    + '<circle cx="137" cy="80" r="8" fill="none" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>'
    + '<line x1="129" y1="80" x2="145" y2="80" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>'
    + '<line x1="137" y1="72" x2="137" y2="88" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>'
    + '</svg>';


class PuttConsistencyManager {

    /**
     * @param {Object} opts
     * @param {string} opts.consistencyId 穩定度區塊的容器 id（內含 .box-title、.putt-consistency-map、.putt-consistency-legend）
     */
    constructor(opts) {
        this.el = document.getElementById(opts.consistencyId);
    }

    render() {
        if (!this.el) return;
        const titleEl = this.el.querySelector('.box-title');
        const mapEl = this.el.querySelector('.putt-consistency-map');
        const legendEl = this.el.querySelector('.putt-consistency-legend');

        if (titleEl) titleEl.textContent = PUTT_CONSISTENCY_TEXT.title;
        if (mapEl) mapEl.innerHTML = PUTT_CONSISTENCY_PLACEHOLDER_SVG;
        // ⚠️ 圖說只留一行、字要少。⛔ 不要再列四個記號
        if (legendEl) {
            legendEl.innerHTML =
                '<span class="mk mk-new">●</span> 最新一推'
                + '<span class="mk mk-avg">⊕</span> 平均'
                + '<span class="mk"></span>' + PUTT_CONSISTENCY_TEXT.note;
        }
    }
}
