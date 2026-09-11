/**
 * @fileoverview puttPanelManager.js — 推桿頁「左欄下半」的面板管理。
 *
 * 管的範圍（一個功能一支 js，比照 shortTableManager.js / cmpChartManager.js）：
 *   ① 界標列   ▶ 架桿 頂點 碰球 收桿          規劃文件 §2.3
 *   ② 數值面板 節奏比／總時長                  規劃文件 §2.4
 *   ③ 詳細數值面板（⇄ 切換 ＋ 六分頁）         規劃文件 §2.9
 *
 * ⛔ 不管右欄的卡片、綜合評價、一致性 —— 那些在 puttingIssuesManager.js。
 * 規劃文件：docs/expert-data-v8-putt-plan.md
 *
 * ═══ 這一輪（§6.1 第 2〜3 項）做到哪裡 ═══
 * ⭐ 切換、分頁、界標鈕的「行為」是真的、會動的。
 * ⚠️ 餵給它的資料是 PUTT_PANEL_DEV_DATA（這個檔案最下面），寫死的假資料。
 * ⛔ 真正的資料來源是 shot_video_swing 的 PuttingPhases / PuttingTempo，
 *    要等 Core 的 fixture 轉換程式（§6.2 第 11〜13 項）。
 * ⛔ fixture 沒到不要硬做 §3.3 那些分支 —— 沒有資料驗證，
 *    規劃階段已經在紙上寫錯過兩次（黑名單擋總時長、只停用對應那顆界標鈕）。
 */

class PuttPanelManager {

    /**
     * @param {Object} opts
     * @param {string} opts.marksId        界標列容器 id
     * @param {string} opts.valuePanelId   數值面板 id
     * @param {string} opts.detailPanelId  詳細數值面板 id
     * @param {string} opts.markHintId     界標提示那一行的 id（⚠️ 見 bindEvents()）
     * @param {Function} opts.onSeekFrame  點界標鈕時呼叫，參數是正面影片的幀號
     */
    constructor(opts) {
        this.marksEl = document.getElementById(opts.marksId);
        this.valueEl = document.getElementById(opts.valuePanelId);
        this.detailEl = document.getElementById(opts.detailPanelId);
        this.markHintEl = opts.markHintId ? document.getElementById(opts.markHintId) : null;
        this.onSeekFrame = opts.onSeekFrame || function () {};
        this.data = null;
        this.bindEvents();
    }

    bindEvents() {
        const self = this;

        if (this.marksEl) {
            this.marksEl.querySelectorAll('.step button[data-phase]').forEach(function (btn) {
                btn.addEventListener('click', function (e) {
                    const target = e.currentTarget;
                    // ⚠️ 不可信的那顆：**點得下去，但⛔ 不跳**。
                    //    ⛔ 外觀完全不變（user 2026-09-10 兩次裁示：
                    //    鈕消失＝像 bug、調暗＝屏蔽按鈕，兩個都不要）。
                    //    ⭐ 值是合法幀號，跳過去⛔ 不會出錯、只會安靜跳到錯的位置。
                    //
                    // ⚠️⚠️ 但「鈕看起來正常、點了卻沒反應」會被讀成什麼，
                    //      前一輪⛔ 完全沒驗過，⛔ 有可能又被讀成「頁面壞了」
                    //      （§1.3 第 9 點：空位就是這樣被讀成 bug 的）。
                    //      → user 2026-09-10 裁示：⭐ 維持不跳，⛔ 但要講一句為什麼。
                    //      ⚠️ 這一句是主畫面上除了綜合評價以外唯一的品質字眼，
                    //      ⛔ 它只講「這一顆抓不到」，⛔ 不要擴寫成整支影片的品質評語。
                    if (target.classList.contains('is-untrusted')) {
                        self.showMarkHint(target.dataset.phase);
                        return;
                    }
                    self.hideMarkHint();
                    self.selectMark(target);
                    const frame = parseInt(target.dataset.frontFrame, 10);
                    if (!isNaN(frame)) self.onSeekFrame(frame);
                });
            });
        }

        if (this.detailEl) {
            this.detailEl.querySelectorAll('.putt-detail-tab').forEach(function (btn) {
                btn.addEventListener('click', function (e) {
                    self.showTab(e.currentTarget.dataset.tabKey);
                });
            });
        }
    }

    /* =================================================================
     * ① 界標列（§2.3）
     * ================================================================= */

    /**
     * 填入四顆界標鈕的幀號。
     *
     * ⚠️⚠️ 四顆鈕**永遠都在、位置固定**，⛔ 絕不可以塌成連續幾顆 ——
     *      塌陷會讓「頂點抓不到」在畫面上被讀成「這一推沒有上桿頂點」
     *      或「這支影片沒拍到收桿」。畫面看起來合理，意思是錯的。
     * ⚠️ 不可信的那顆：**外觀完全不變**，只是點了⛔ 不跳
     *    （user 2026-09-10 兩次裁示：消失＝像 bug，調暗＝屏蔽按鈕，兩個都不要）。
     *    ⭐ .is-untrusted ⛔ 沒有 CSS，純粹是行為旗標。
     * ⭐ 少了哪幾顆、為什麼，在〔詳細數值〕的「狀態」分頁交代。
     * ⚠️ 起桿（onset）⛔ 不做成按鈕，值留在 data.onset 供卡片標「上桿段」時跳段用。
     *
     * ⚠️⚠️⚠️ 可不可信⛔ 一定要由 `trust` 明確指定，⛔ 絕對不可以用「值存不存在」判斷。
     *      Core 2026-09-10 明確提醒（putting_columns.md §2.2.2）：
     *      **界標找不到時，放進去的值仍然落在合法範圍內** ——
     *        address 找不到 → 0
     *        top 找不到     → 0 或推估值
     *        finish 找不到  → n−1
     *      所以那些值拿去 seek ⛔ 不會出錯、⛔ 不會拋例外，
     *      ⛔ **只會安靜地跳到錯的地方**，而畫面看起來完全正常。
     *      這正是這一頁一路在防的那一型錯誤。
     *      判法照 putting_columns.md §2.2.1 那張表（用 reason 判）。
     * ⭐ Core 批 1 之後 PuttingPhases 會追加具名鍵 `found: {address, top, finish}`，
     *    ⭐ 直接把它當 trust 傳進來就好，⛔ 不必再從 reason 反推。
     * ⚠️ `found` ⛔ 只有三個鍵：impact ⛔ 沒有對應布林（找不到就早退），
     *    ⛔ 不要當成四個。impact 的可信度要另外判（§2.6：Core 的分期品質分級
     *    ⛔ 不檢查碰球，這也是「▶ 看這一段」存在的理由）。
     *
     * @param {Object} phases 例：{address:88, top:231, impact:279, finish:324}
     * @param {Object} trust  例：{address:true, top:false, impact:true, finish:true}
     *                        ⚠️ 不給就一律視為⛔ 不可信（安全預設，寧可少一顆鈕）。
     */
    setMarks(phases, trust) {
        if (!this.marksEl) return;
        // 換一支影片就把上一句提示收掉，⛔ 不要讓它留在畫面上講別支的事
        this.hideMarkHint();
        const map = { A: 'address', T: 'top', I: 'impact', F: 'finish' };
        const self = this;
        // ⚠️ 安全預設：沒給 trust 就當全部不可信。
        //    ⛔ 不要改成「沒給就全部可信」—— 那會讓沒接好的資料靜靜跳到錯的幀。
        const isTrusted = function (key) {
            return !!(trust && trust[key] === true);
        };
        this.marksEl.querySelectorAll('.step button[data-phase]').forEach(function (btn) {
            const key = map[btn.dataset.phase];
            const frame = phases ? phases[key] : null;
            if (typeof frame === 'number' && isTrusted(key)) {
                btn.dataset.frontFrame = frame;
                btn.classList.remove('is-untrusted');
            } else {
                // ⚠️ 鈕的外觀**完全不變**：⛔ 不消失、⛔ 不調暗、⛔ 不屏蔽。
                //    .is-untrusted ⛔ 沒有對應的 CSS，⭐ 純粹是行為旗標。
                // ⛔⛔ 但一定要把 data-front-frame 拿掉 —— 值是合法幀號，
                //    留著就會被跳過去，⛔ 不會報錯、只會安靜跳到錯的位置。
                delete btn.dataset.frontFrame;
                btn.classList.add('is-untrusted');
            }
        });
        // 界標整組不可信（例：ex03 的 head_flicker）→ 四顆都變成不可用，
        // ⛔ 但都還在；標題與播放鈕不受影響，影片照樣能播
        const anyTrusted = !!phases && Object.keys(map).some(function (k) {
            return typeof phases[map[k]] === 'number' && isTrusted(map[k]);
        });
        this.marksEl.classList.toggle('marks-all-untrusted', !anyTrusted);
        if (anyTrusted) {
            const first = self.marksEl.querySelector('.step button[data-phase]:not(.is-untrusted)');
            if (first) self.selectMark(first);
        }
    }

    /**
     * 不可信的那顆被點下去時講一句話（user 2026-09-10 裁示）。
     *
     * ⚠️ 鈕的外觀仍然⛔ 完全不變 —— 這一行只在「真的被點了」之後才出現，
     *    ⛔ 不是常駐的品質標示，⛔ 也不是把鈕標成不可用。
     * ⭐ 為什麼要有它：不跳是對的（值是合法幀號，跳過去會安靜跳到錯的位置），
     *    ⛔ 但沉默會被讀成「頁面壞了」——§1.3 第 9 點已經栽過一次。
     * ⚠️ 鈕上⛔ 不放中文（§1.3 第 1 點），⛔ 但這一句要講是哪一顆，
     *    否則教練不知道剛剛按的是什麼。
     */
    showMarkHint(phase) {
        if (!this.markHintEl) return;
        const names = { A: '架桿', T: '頂點', I: '碰球', F: '收桿' };
        const name = names[phase] || '這個階段';
        // ⚠️⚠️ ⛔ 只講結果，⛔ 不解釋系統為什麼做不到（user 2026-09-10 裁示）。
        //      ⛔ 原本寫的是「沒有抓到…，為了不跳到錯的位置，這顆鈕不跳」——
        //      ⛔ 那是在跟教練講我們的內部限制。
        //      ⭐ 為什麼不能跳，收進〔詳細數值〕的「狀態」分頁（§0.2、§2.9）。
        this.markHintEl.textContent = '這一推無法跳到「' + name + '」';
        this.markHintEl.classList.remove('hidden-element');
    }

    hideMarkHint() {
        if (!this.markHintEl) return;
        this.markHintEl.textContent = '';
        this.markHintEl.classList.add('hidden-element');
    }

    // 選取樣式的切換方式與 v8 / v8-short 相同：兩個 class 互換
    selectMark(btn) {
        this.marksEl.querySelectorAll('.step button[data-phase]').forEach(function (b) {
            b.classList.remove('stepbutton_selected');
            b.classList.add('stepbutton');
        });
        btn.classList.remove('stepbutton');
        btn.classList.add('stepbutton_selected');
    }

    /* =================================================================
     * ② 數值面板（§2.4）
     * ================================================================= */

    /**
     * 填入節奏比與總時長。
     *
     * ⭐ 整塊語意一致：全部是秒與比值，⛔ 一個判定都不混進來。
     * ⛔ 不可信的格子⛔ 不出現（⛔ 不是顯示「—」）—— 這裡是整格 display:none。
     *    ⚠️ 跟界標列的「留空位」規則不同，⛔ 不要互相套用：
     *       數字格少一格不會製造錯誤印象，界標少一顆會。
     * ⚠️ 第二階段（onset 入庫後）這裡會多出上桿時間與下桿時間兩格；
     *    ⛔ 兩者都掛在「頂點」上，所以 top_missing 時只剩總時長，
     *    ⛔ 那時不要為了填版面放別的東西。
     *
     * @param {Object} values 例：{tempoRatio:'1.65 : 1', totalDuration:'3.93s'}
     *                        某一格算不出來就給 null。
     */
    setValues(values) {
        if (!this.valueEl) return;
        // ⚠️ class 名稱沿用 short 右欄的 .stats-table / .stat-row，
        //    ⛔ 不要改回卡片（.data-panel .card）—— 那是 short 的左下慣例。
        this.valueEl.querySelectorAll('.stat-row[data-value-key]').forEach(function (row) {
            const key = row.dataset.valueKey;
            const text = values ? values[key] : null;
            const valueEl = row.querySelector('.stat-value');
            // ⛔ 不可信的那一列整列不出現，⛔ 不是顯示「—」
            if (text === null || text === undefined || text === '') {
                row.classList.add('hidden-element');
                if (valueEl) valueEl.textContent = '';
            } else {
                row.classList.remove('hidden-element');
                if (valueEl) valueEl.textContent = text;
            }
        });
    }

    /* =================================================================
     * ③ 詳細數值面板（§2.9）
     * ================================================================= */

    /** 數值面板 ⇄ 詳細數值面板。抄自切桿頁的 toggleMotionPanel()。 */
    toggle() {
        const showingDetail = this.detailEl.style.display !== 'none';
        this.detailEl.style.display = showingDetail ? 'none' : '';
        this.valueEl.style.display = showingDetail ? '' : 'none';
    }

    /**
     * 餵詳細數值的內容。
     *
     * ⛔ 一次只出一類（⛔ 不要 23 列一次倒出來）。
     * ⚠️ 分頁鈕本身要能反映狀態（該類算不出來 → .is-empty），
     *    否則點進去才發現是空的。
     *
     * @param {Object} groups key 是分頁 key，值是
     *        {title, verdict, metrics:[{name, value, note, kind}]}
     *        kind: 'decides' | 'reference' | 'na'
     */
    setDetail(groups) {
        this.detailGroups = groups || {};
        const self = this;
        this.detailEl.querySelectorAll('.putt-detail-tab').forEach(function (btn) {
            const g = self.detailGroups[btn.dataset.tabKey];
            const empty = !g || !g.metrics || g.metrics.length === 0;
            btn.classList.toggle('is-empty', empty && btn.dataset.tabKey !== 'status');
        });
        const active = this.detailEl.querySelector('.putt-detail-tab.is-active')
            || this.detailEl.querySelector('.putt-detail-tab');
        if (active) this.showTab(active.dataset.tabKey);
    }

    showTab(key) {
        const body = this.detailEl.querySelector('.putt-detail-body');
        if (!body) return;

        this.detailEl.querySelectorAll('.putt-detail-tab').forEach(function (b) {
            b.classList.toggle('is-active', b.dataset.tabKey === key);
        });

        const group = (this.detailGroups || {})[key];
        if (!group) {
            body.innerHTML = '';
            return;
        }

        let html = '<div class="putt-detail-group-title">'
            + '<span>' + this.esc(group.title) + '</span>'
            + '<span class="verdict">' + this.esc(group.verdict || '') + '</span>'
            + '</div>';

        (group.metrics || []).forEach(function (m) {
            // ⭐ 「參考，不判定」（decides:false）很重要：ex02 的肩線傾斜是 14.25°，
            //    看起來很大，但系統只算不判 —— 那是教練、動作是對的。
            //    ⛔ 這種數字放主畫面會被誤讀成問題。
            // ⚠️ metrics 每一列各有自己的 applicable，⛔ 類別層一個旗標蓋不住。
            const cls = (m.kind === 'reference') ? ' is-reference'
                : (m.kind === 'na') ? ' is-na' : '';
            html += '<div class="putt-metric-row' + cls + '">'
                + '<span class="metric-name">' + this.esc(m.name) + '</span>'
                + '<span class="metric-value">' + this.esc(m.value) + '</span>'
                + '<span class="metric-note">' + this.esc(m.note || '') + '</span>'
                + '</div>';
        }, this);

        if (group.note) {
            html += '<div class="putt-status-note">' + this.esc(group.note) + '</div>';
        }
        body.innerHTML = html;
        body.scrollTop = 0;
    }

    esc(s) {
        if (s === null || s === undefined) return '';
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }
}


/* =====================================================================
 * ⛔ 以下是這一輪的開發用假資料，接上真資料後整段刪掉。
 *
 * 出處：六支範例的 ex01（多項偵測到），數字照規劃文件 §3.4：
 *     phases [88, 231, 279, 324]、onset 152、tempo_ratio 1.646、總時長 3.93s
 * 六支範例 JSON 在 page/js/dev-data/putting/（⛔ 不在版控，見 .gitignore）。
 * ===================================================================== */
const PUTT_PANEL_DEV_DATA = {

    phases: { address: 88, top: 231, impact: 279, finish: 324 },

    // ⚠️⚠️ 每一顆界標可不可信，⛔ 一定要明確給 —— 見 setMarks() 的說明。
    //    ⛔ 絕對不可以用「值存不存在」判斷：界標找不到時放的值仍在合法範圍內，
    //    拿去 seek 不會出錯，只會安靜跳到錯的地方。
    // ⭐ Core 批 1 之後直接把 PuttingPhases 的 found 傳進來（只有三個鍵，
    //    impact 要另外判）。這裡是假資料，四顆都當可信。
    trust: { address: true, top: true, impact: true, finish: true },

    // ⚠️ 起桿⛔ 不做成按鈕，但值要留著 —— 卡片標「上桿段」時靠它決定跳到哪
    onset: 152,

    // ⚠️ fps ⛔ 不可寫死：六支範例分別是 60 / 29.97 / 25。
    //    fps 推導是 §6.2 第 11 項，等 Core 的 fixture 才做。
    fps: 60,

    // ⛔ 側面的幀號完全不可拿正面的來套（實測同一次推擊偏移是 35/36/22/60，不是常數）。
    //    沒有側面那一列的 PuttingPhases → 側面就⛔ 不跳、也⛔ 不標示。
    sidePhases: null,

    // ⚠️ 單位寫在 jsp 的 .unit 那一行（上桿 : 下桿／秒），
    //    ⛔ 這裡只放數字，⛔ 不要再把單位黏進來
    values: {
        tempoRatio: '1.65 : 1',
        totalDuration: '3.93',
        // ⚠️ 球速：來源是 shot_data.BallSpeed（E6 碰球瞬間量到的），
        //    ⛔ 不是模擬器滾出來的結果。⚠️ 這裡是假資料。
        //    ⛔ 算不出來就給 null → setValues() 會讓整列不出現（⛔ 不是顯示「—」）。
        ballSpeed: '4.6',
    },

    detail: {
        status: {
            title: '狀態',
            verdict: '',
            metrics: [
                { name: '判定狀態',           value: 'evaluated',            note: '', kind: 'reference' },
                { name: '視角',               value: '正面',                 note: '', kind: 'reference' },
                { name: '界標品質',           value: 'OK',                   note: '', kind: 'reference' },
                { name: 'reason 代碼',        value: '（無）',               note: '', kind: 'reference' },
                { name: 'threshold_profile',  value: 'putting_issue_th_v1',  note: '', kind: 'reference' },
            ],
            note: '這個分頁還要放「為什麼某一格沒出現」的說明，例如哪幾顆界標不可信、為什麼。',
        },
        stance: {
            title: '站姿',
            verdict: '判定：正常',
            metrics: [
                { name: '兩腳張開寬度（相對肩寬）', value: '0.6812', note: '門檻 0.4〜0.8', kind: 'decides' },
                { name: '兩腳踝間距（相對肩寬）',   value: '0.8807', note: '參考，不判定', kind: 'reference' },
                { name: '兩腳外緣寬度（相對肩寬）', value: '1.2114', note: '參考，不判定', kind: 'reference' },
                { name: '鞋子寬度佔比',             value: '0.2651', note: '量測品質',     kind: 'reference' },
            ],
        },
        triangle: {
            title: '三角形變動',
            verdict: '判定：有風險',
            metrics: [
                { name: '球桿偏離手臂方向－上桿段', value: '1.75°',  note: '參考，不判定',  kind: 'reference' },
                { name: '球桿偏離手臂方向－下桿段', value: '10.15°', note: '門檻 10.0',     kind: 'decides' },
                { name: '球桿偏離手臂方向－送桿段', value: '4.23°',  note: '門檻 5.0',      kind: 'decides' },
                { name: '手肘彎曲程度（相對架桿時）', value: '7.82°', note: '門檻 20.0',    kind: 'decides' },
                // ⚠️ 這兩列示範「metrics 各有自己的 applicable」，類別是有問題，
                //    底下仍可能有某一列算不出來
                { name: '雙手相對雙肩的左右偏移',   value: '本次未取得', note: 'not_computed', kind: 'na' },
                { name: '收桿時肩膀寬度變化',       value: '本次未取得', note: 'not_computed', kind: 'na' },
            ],
        },
        // ⚠️ 這一類整類算不出來 → 分頁鈕會自動變成 .is-empty，
        //    ⛔ 不要讓人點進去才發現是空的
        ball_position: {
            title: '球位',
            verdict: '本次無法判定',
            metrics: [],
        },
        body_sway: {
            title: '身體位移',
            verdict: '判定：有風險',
            metrics: [
                { name: '下桿左右移動量',       value: '0.2225', note: '門檻 0.0533',  kind: 'decides' },
                { name: '上桿左右移動量',       value: '0.0844', note: '參考，不判定', kind: 'reference' },
                { name: '大腿傾斜角度',         value: '3.07°',  note: '參考，不判定', kind: 'reference' },
                { name: '移動量／雜訊倍數',     value: '75.1',   note: '量測品質',     kind: 'reference' },
                { name: '骨盆寬度（量測基準）', value: '本次未取得', note: 'not_computed', kind: 'na' },
            ],
        },
        swing_angle: {
            title: '身體傾斜',
            verdict: '判定：正常',
            metrics: [
                { name: '上半身傾斜角度（相對架桿時）', value: '1.04°', note: '門檻 3.0',     kind: 'decides' },
                // ⭐ 這一列就是那個例子：看起來很大，但系統只算不判。
                //    ⛔ 這種數字放主畫面會被誤讀成問題。
                { name: '肩膀連線的傾斜角度',           value: '8.52°', note: '參考，不判定', kind: 'reference' },
                { name: '架桿前的角度抖動',             value: '0.10°', note: '量測品質',     kind: 'reference' },
                { name: '上半身中線長度（相對肩寬）',   value: '2.22',  note: '參考，不判定', kind: 'reference' },
                { name: '頭部相對軀幹的角度變化倍數',   value: '本次未取得', note: 'not_computed', kind: 'na' },
            ],
        },
    },
};
