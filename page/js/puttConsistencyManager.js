/**
 * @fileoverview puttConsistencyManager.js — 推桿頁右欄上方的「推桿穩定度」圖。
 *
 * 這張圖回答「這個人穩不穩」，⛔ 不是「這一推準不準」。
 * 資料：PuttingShotData.processPuttConsistency() —— 同一個 Player、同一支球桿的最近幾推（含這一推）。
 *
 * ═══ 算法 ═══
 * 兩軸都是**碰球瞬間量到的值**，⛔ 不可以用滾動後的結果：
 *   橫軸 ＝ LaunchDirection − 這幾推的平均（出球偏左／出球偏右，正值偏右）
 *   縱軸 ＝ BallSpeed       − 這幾推的平均（力道偏強／力道偏弱）
 * ⛔ 縱軸⛔ 不是距離 —— TotalDistFt 是模擬器用草皮摩擦係數滾出來的。
 * ⛔ 不可以用 ShortGameData 的 landing_points：那是球飛行模型幾何反推的，對推桿不成立。
 *   · 原點 ⊕ ＝ 這幾推的平均，⛔ 不是洞、⛔ 不是目標
 *   · 虛線橢圓 ＝ 兩軸各一個樣本標準差（兩軸單位不同，所以是橢圓）；少於 2 推畫不出來就不畫
 * ⚠️ 不分推的距離：距離不同的推混在一起時，散得開也可能只是距離不同。
 *
 * ═══ 顏色（比照 shortTableManager.js 的落點圖）═══
 *   最新這一推 ＝ 亮綠 rgba(0,255,132,1)，半徑放大 ＋ 白框
 *   之前幾推   ＝ 亮黃 rgb(255,206,86)，越新 alpha 越高（0.35〜0.90）
 *   平均點與散布圈 ＝ 亮藍 rgb(54,162,235)
 * ⚠️ alpha ⛔ 不要調回切桿頁的 0.20 —— 這一頁投在投影機上，最舊那幾顆會看不見。
 * ⚠️ 圖說的記號顏色寫在 GM08_putt.css 的 .putt-consistency-legend .mk-*，兩邊要一致。
 * ⛔ 圖上⛔ 不出現任何數字（刻度、tooltip 都關掉）；⛔ 不做雷達圖、⛔ 不做曲線球。
 */

const PUTT_CONSISTENCY_TEXT = {
    // ⚠️ 標題講這張圖回答什麼（穩不穩），⛔ 不要寫成「最近 N 推」
    title: '推桿穩定度',
    // ⛔ 不可以講成「準不準」
    note: '越集中越穩定',
    // ⛔ 不可以用「太強／太弱」：原點是這幾推的自己平均，⛔ 不是正確值，
    //    寫成「太」等於宣稱一個圖上沒有依據的標準（每個人都必然有一半的點落在那一側）。
    // ⛔ 橫軸講的是出球方向（量測值），⛔ 不是球最後停在哪（那是模擬器滾出來的）。
    up: '力道偏強', down: '力道偏弱', left: '出球偏左', right: '出球偏右',
};

const PUTT_CONSISTENCY_COLORS = {
    current: 'rgba(0, 255, 132, 1)',
    previous: function (alpha) { return 'rgba(255, 206, 86, ' + alpha + ')'; },
    average: 'rgba(54, 162, 235, 1)',
    spread: 'rgba(54, 162, 235, 0.85)',
    axis: '#8f979b',
    text: '#ffffff',
};

/**
 * 把後端的最近幾推換成圖上的點。⛔ 不碰 DOM，驗收程式直接測它。
 *
 * @param {Object} data {currentId, shots:[{id, ballSpeed, launchDirection}]}，新的在前；可以是 null
 * @returns {{n:number, points:Array, sdX:(number|null), sdY:(number|null), rangeX:number, rangeY:number}}
 *   points: [{x, y, current, alpha}]，x／y 是相對平均的偏移
 */
function buildPuttConsistencyPoints(data) {
    const num = function (v) { return typeof v === 'number' && isFinite(v); };
    const shots = ((data && data.shots) || []).filter(function (s) {
        return s && num(s.ballSpeed) && num(s.launchDirection);
    });
    const n = shots.length;
    const empty = { n: 0, points: [], sdX: null, sdY: null, rangeX: 1, rangeY: 1 };
    if (n === 0) return empty;

    const mean = function (key) {
        return shots.reduce(function (a, s) { return a + s[key]; }, 0) / n;
    };
    const sd = function (key, m) {
        if (n < 2) return null;
        const ss = shots.reduce(function (a, s) { return a + Math.pow(s[key] - m, 2); }, 0);
        return Math.sqrt(ss / (n - 1));
    };
    const mDir = mean('launchDirection');
    const mSpeed = mean('ballSpeed');
    const currentId = data.currentId;

    const points = shots.map(function (s, i) {
        // 新的在前：i = 0 最新。之前幾推越舊越淡（0.90 → 0.35）
        const older = n > 1 ? i / (n - 1) : 0;
        return {
            x: s.launchDirection - mDir,
            y: s.ballSpeed - mSpeed,
            current: currentId !== null && currentId !== undefined && s.id === currentId,
            alpha: Number((0.90 - 0.55 * older).toFixed(2)),
        };
    });

    const sdX = sd('launchDirection', mDir);
    const sdY = sd('ballSpeed', mSpeed);
    // 兩軸以原點置中；範圍取「最遠的點」與「散布圈」較大者再留邊，全部重疊時給 1 避免除以 0
    const reach = function (key, s) {
        const far = points.reduce(function (a, p) { return Math.max(a, Math.abs(p[key])); }, 0);
        const r = Math.max(far, s || 0) * 1.25;
        return r > 0 ? r : 1;
    };
    return { n: n, points: points, sdX: sdX, sdY: sdY, rangeX: reach('x', sdX), rangeY: reach('y', sdY) };
}


class PuttConsistencyManager {

    /**
     * @param {Object} opts
     * @param {string} opts.consistencyId 穩定度區塊的容器 id（內含 .box-title、.putt-consistency-map、.putt-consistency-legend）
     */
    constructor(opts) {
        this.el = document.getElementById(opts.consistencyId);
        this.chart = null;
    }

    /** @param {Object} data processPuttConsistency() 的回傳值；沒有就給 null */
    render(data) {
        if (!this.el) return;
        // 診斷用：圖是拿「這一桿的球桿」撈同球員的最近幾筆。
        // 最新一桿不是推桿時（LID 模式下會發生），畫的就是那支球桿的散布 ——
        // 畫面上看不出來，所以在這裡留一行給工程師對。
        if (data) {
            console.log('[推桿穩定度] 球桿=' + (data.clubType === null || data.clubType === undefined ? '(不明)' : data.clubType)
                + '　球員=' + (data.player === null || data.player === undefined ? '(不明)' : data.player)
                + '　LID=' + (data.lid === null || data.lid === undefined ? '(不明)' : data.lid)
                + '　這一桿 id=' + data.currentId
                + '　取樣 ' + ((data.shots || []).length) + ' 筆');
        }
        const titleEl = this.el.querySelector('.box-title');
        const mapEl = this.el.querySelector('.putt-consistency-map');
        const legendEl = this.el.querySelector('.putt-consistency-legend');

        if (titleEl) titleEl.textContent = PUTT_CONSISTENCY_TEXT.title;
        // ⚠️ 圖說只留一行、字要少。⛔ 不要再列四個記號
        if (legendEl) {
            legendEl.innerHTML =
                '<span class="mk mk-new">●</span> 最新一推'
                + '<span class="mk mk-avg">⊕</span> 平均'
                + '<span class="mk"></span>' + PUTT_CONSISTENCY_TEXT.note;
        }
        if (!mapEl || typeof Chart === 'undefined') return;

        mapEl.innerHTML = '<div class="putt-consistency-canvas"><canvas></canvas></div>';
        if (this.chart) this.chart.destroy();
        this.chart = new Chart(mapEl.querySelector('canvas'), this.buildConfig(buildPuttConsistencyPoints(data)));
    }

    buildConfig(built) {
        const C = PUTT_CONSISTENCY_COLORS;
        const datasets = [];

        if (built.sdX > 0 && built.sdY > 0) {
            const ring = [];
            for (let i = 0; i <= 64; i++) {
                const t = (i / 64) * Math.PI * 2;
                ring.push({ x: built.sdX * Math.cos(t), y: built.sdY * Math.sin(t) });
            }
            datasets.push({
                type: 'line', data: ring, borderColor: C.spread, borderWidth: 1.6, borderDash: [5, 5],
                pointRadius: 0, fill: false, tension: 0, order: 50,
            });
        }

        const previous = built.points.filter(function (p) { return !p.current; });
        datasets.push({
            type: 'scatter', data: previous,
            backgroundColor: previous.map(function (p) { return C.previous(p.alpha); }),
            pointRadius: 5, pointBorderWidth: 0, order: 40,
        });

        if (built.n > 0) {
            // ⊕ ＝ 空心圓 ＋ 十字
            datasets.push({
                type: 'scatter', data: [{ x: 0, y: 0 }], pointStyle: 'circle', pointRadius: 9,
                backgroundColor: 'rgba(0,0,0,0)', borderColor: C.average, pointBorderWidth: 2.6, order: 20,
            });
            datasets.push({
                type: 'scatter', data: [{ x: 0, y: 0 }], pointStyle: 'cross', pointRadius: 9,
                borderColor: C.average, pointBorderWidth: 2.6, order: 19,
            });
        }

        const current = built.points.filter(function (p) { return p.current; });
        datasets.push({
            type: 'scatter', data: current, backgroundColor: C.current,
            pointRadius: 7, pointBorderColor: 'rgba(255,255,255,0.8)', pointBorderWidth: 2, order: 1,
        });

        const T = PUTT_CONSISTENCY_TEXT;
        // ⚠️ 投影機情境：字級⛔ 不要往下調（舊示意圖放大後約 24px）。
        //    軸標畫在圖表區外的留白裡 → 留白不夠時字會被切掉。
        //    ⛔ 不要寫死留白：中文字寬約等於字級，改文案時字數一變就會再撐出去。
        const AXIS_FONT_PX = 22;
        const AXIS_GAP_PX = 6;
        const sidePad = Math.max(T.left.length, T.right.length) * AXIS_FONT_PX + AXIS_GAP_PX + 4;
        const vertPad = AXIS_FONT_PX + AXIS_GAP_PX + 2;
        const axesPlugin = {
            id: 'puttConsistencyAxes',
            beforeDatasetsDraw: function (chart) {
                const ctx = chart.ctx;
                const a = chart.chartArea;
                const x0 = chart.scales.x.getPixelForValue(0);
                const y0 = chart.scales.y.getPixelForValue(0);
                ctx.save();
                ctx.strokeStyle = C.axis;
                ctx.lineWidth = 1.4;
                ctx.beginPath();
                ctx.moveTo(x0, a.top); ctx.lineTo(x0, a.bottom);
                ctx.moveTo(a.left, y0); ctx.lineTo(a.right, y0);
                ctx.stroke();
                ctx.fillStyle = C.text;
                ctx.font = AXIS_FONT_PX + 'px sans-serif';
                ctx.textAlign = 'center';
                ctx.textBaseline = 'bottom';
                ctx.fillText(T.up, x0, a.top - 4);
                ctx.textBaseline = 'top';
                ctx.fillText(T.down, x0, a.bottom + 4);
                ctx.textBaseline = 'middle';
                ctx.textAlign = 'right';
                ctx.fillText(T.left, a.left - AXIS_GAP_PX, y0);
                ctx.textAlign = 'left';
                ctx.fillText(T.right, a.right + AXIS_GAP_PX, y0);
                ctx.restore();
            },
        };

        return {
            type: 'scatter',
            data: { datasets: datasets },
            options: {
                // 關掉動畫：截圖（PDF）時點要在最終位置
                animation: false,
                responsive: true,
                maintainAspectRatio: false,
                layout: { padding: { top: vertPad, bottom: vertPad, left: sidePad, right: sidePad } },
                plugins: { legend: { display: false }, tooltip: { enabled: false } },
                events: [],
                scales: {
                    x: { type: 'linear', min: -built.rangeX, max: built.rangeX,
                        ticks: { display: false }, grid: { display: false }, border: { display: false } },
                    y: { type: 'linear', min: -built.rangeY, max: built.rangeY,
                        ticks: { display: false }, grid: { display: false }, border: { display: false } },
                },
            },
            plugins: [axesPlugin],
        };
    }
}
