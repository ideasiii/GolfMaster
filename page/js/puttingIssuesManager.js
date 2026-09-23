/**
 * @fileoverview puttingIssuesManager.js — 推桿頁的卡片管理。
 *
 * 管的範圍（一個功能一支 js）：
 *   ① 綜合評價                              規劃文件 §2.7
 *   ② 推桿風險（三態 ＋ 分頁切換 ＋ 跳段）      規劃文件 §2.5、§2.6
 *
 * ⛔ 不管的：界標列與數值面板（puttPanelManager.js）、推桿穩定度圖（puttConsistencyManager.js）、
 *    擊球數據卡片 ⇄ 回饋切換（puttShotDataManager.js）、影片控制（puttVideoManager.js）。
 * ⚠️ 檔名照 feedback_impl_spec.md §5.3 的命名，⛔ 不要改成別的。
 * 規劃文件：docs/expert-data-v8-putt-plan.md
 * 上一輪紀錄：docs/expert-data-v8-putt-session-04-08.md
 *
 * ═══ ⚠️ 呈現方式跟規劃圖不同（user 2026-09-09 裁示）═══
 * 規劃 §2.5 畫的是「五張卡片疊著、一張展開」。實作改成
 * **標籤列 ＋ 一次只顯示一類**：
 *   · 五類全部在標籤列上、全部可點 → 仍然符合「都留在畫面上、都可點開」
 *   · 標籤上的 ●（有問題）／○（正常）／⊘（不適用）讓三態一眼看得到
 *   · 標籤用短名（跟詳細數值面板同一套），內容區顯示完整名稱
 * ⭐ 理由：整頁高度鎖在 710px（跟另外三頁一致），疊起來的清單會捲，
 *    而 user 明確說「滾輪不是好顯示方式」。
 * ⛔ 排序規則⛔ 沒有改，仍然是 §2.5 的四組分派 ＋ 五類固定順序。
 *
 * ═══════════════════════════════════════════════════════════════
 * ⭐ 這一輪（§6.1 工項 4〜8）做完的：
 *     · 四組分派與組內排序          → classifyAndSort()
 *     · 三態判斷                    → decideState()
 *     · 預設選哪一個                → pickDefaultOpen()
 *     · 「▶ 看這一段」的可跳判斷    → resolveSegment()
 *     · 綜合評價的暫代計算          → computeOverallTemp()
 *     · 文案改讀 putting_issue_seed.json 的 18 條 → lookupTip()
 *
 * ⚠️ render() 現在吃的是 **Core 原始的 issues 陣列**，⛔ 不再是排好序的清單。
 * ⚠️ 仍然走不到的一半：pickDefaultOpen() 的 prominence 那半 ——
 *    六支範例還是 9/3 版、⛔ 沒有 prominence 欄。程式寫好了但條件不會成立。
 *    ⛔ 不要為了測它自己編一個 prominence 值進去（§4.2：頁面⛔ 不定「多近算貼近」）。
 * ═══════════════════════════════════════════════════════════════
 */

/* =====================================================================
 * 五類的固定順序與顯示名稱
 *
 * ⚠️ 編號 1〜5 就是 §2.5 的五類固定順序，⛔ 不要動。
 * ⚠️ 顯示名稱用 user 2026-09-09 給定的那一套（球員聽得懂的字），
 *    ⛔ 不要用 Core JSON 裡 issues[].title 的舊名
 *    （那一欄寫的還是「上半身穩定度／身體晃動／身體傾斜角度」）。
 * ⚠️ key 與程式內部的變數名⛔ 都不改，⛔ 只改對使用者顯示的字。
 * ⛔ 卡片數⛔ 不寫死：這張表只決定「已知的五類排哪裡、叫什麼」，
 *    不在表內的類別（例如之後加的第六類）會自動排在五類之後、
 *    用 Core 給的 title，⛔ 不必改這個檔。
 * ===================================================================== */
const PUTT_CLASS_ORDER = ['stance', 'triangle', 'ball_position', 'body_sway', 'swing_angle'];

const PUTT_CLASS_LABELS = {
    stance:        { title: '站姿',       short: '站姿' },
    triangle:      { title: '三角形變動', short: '三角形' },
    ball_position: { title: '球位',       short: '球位' },
    body_sway:     { title: '身體位移',   short: '位移' },
    swing_angle:   { title: '身體傾斜',   short: '傾斜' },
};

/* ⚠️ 跳段的三段對應（§2.6）。⛔ 收桿之後沒有段，⛔ 不要自己加第四段。 */
const PUTT_SEGMENTS = {
    backswing:     { from: 'onset',  to: 'top',    label: '上桿段' },
    downswing:     { from: 'top',    to: 'impact', label: '下桿段' },
    followthrough: { from: 'impact', to: 'finish', label: '送桿段' },
};

const PUTT_SEEK_LABEL = '▶ 看這一段';

/* ⚠️ metric 的欄位名尾巴 → 講給人聽的段名。
 *    只用在「正常但某一段沒量到」那一行的註記（ex06）。 */
const PUTT_METRIC_SEGMENT_LABELS = {
    backswing: '上桿段',
    downswing: '下桿段',
    followthrough: '送桿段',
    finish: '收桿',
};

/* =====================================================================
 * `na` 代碼 → 白話原因（⭐ 共 11 個，⛔ 不是 5 個）
 *
 * ⭐ 權威來源是 Core 產生器的 `NA_TEXT`（`make_issue_examples.py`），
 *    ⚠️ 產生器會對帳，⛔ 對不上就失敗 —— 所以這 11 句是查證過的。
 *    ⛔ 不要自己另外編一套措辭：同一個代碼在卡片上與在數值面板上
 *    講不同的話，教練會以為是兩件事（Core 2026-09-10）。
 *
 * ⚠️⚠️ **類別層的 `issues[].na_text` 本來就帶著這些中文**，⛔ 那一層直接讀就好。
 *      ⛔ 但 **`metrics[]` 這一層⛔ 沒有 `na_text`、只有 `na` 代碼** ——
 *      這張表就是為那一層存在的。
 *
 * ⚠️⚠️ **`landmark_missing` 一定會出現在真資料上**（關節點看不到很常見）。
 *      ⛔ 絕對不可以把它落進一句籠統的「未取得畫面」——
 *      它的原因是**關節點看不到**，⛔ 不是沒取到畫面，⛔ 講錯就是編一個原因出來。
 *
 * ⚠️ 有兩句刻意跟產生器的原句不同，從產生器重新同步時⛔ 不要蓋回去：
 *    · not_computed 是空字串：還沒接上的欄位⛔ 不解釋，那一列也⛔ 不列。
 *    · club_not_at_address 只講結果：原句會被讀成「球員架桿時沒拿桿」。
 * ===================================================================== */
const PUTT_NA_TEXTS = {
    '':                        '',
    view_not_supported:        '非正面拍攝，本期只支援正面',
    not_computed:              '',
    ball_not_detected:         '畫面中找不到球',
    ball_detected:             '有偵測到球，不需要用桿頭當下界',
    head_bound_inconclusive:   '只能從桿頭推出球的位置範圍，而那個範圍跨過判定線',
    club_not_at_address:       '無法定位球的位置',
    club_not_detected:         '畫面中找不到球桿，無法用桿頭推出範圍',
    direction_unknown:         '推不出目標方向',
    segment_unavailable:       '這一段在這支影片上取不到足夠的畫面',
    landmark_missing:          '判定所需的關節點在畫面中看不到',
    ball_not_in_place:         '球未到擊球位置',
};


/* 沒有判定結果時回饋那一面的一行字。⛔ 只講狀態，⛔ 不接原因（原因在〔詳細數值〕）。 */
const PUTT_NOT_ANALYZED_TEXT = '尚未分析';

/**
 * 這一推算不算「判定完成」：putting_issue 有這一列，而且狀態是
 * evaluated（已判定）或 not_applicable（整支不適用，例如側面）。
 * 查不到、或 not_computed（判定模組沒跑）都不算。
 *
 * @param {Object} header {status, …}；沒有判定結果時是 null
 */
function puttJudgementComplete(header) {
    return !!header && (header.status === 'evaluated' || header.status === 'not_applicable');
}

/**
 * 把後端組好的判定物件（PuttingData.processPutting() 的 issues）套進 render() 要的資料。
 * ⛔ 沒有判斷邏輯，只搬欄位；判定物件是 null（沒有判定結果）時原樣回傳 base。
 * ⚠️ 表頭只給〔詳細數值〕的「狀態」分頁；view 來自 shot_video_swing 那一列。
 *
 * @param {Object} base    render() 的資料骨架（tips 等已經放好）
 * @param {Object} judged  判定物件，或 null
 * @returns {Object} 新的物件，⛔ 不改 base
 */
function applyPuttJudgement(base, judged) {
    if (!judged) return base;
    return Object.assign({}, base, {
        issues: judged.issues || [],
        overall: judged.overall,
        header: {
            status: judged.status,
            reason: judged.reason,
            view: judged.view,
            threshold_profile: judged.threshold_profile,
        },
    });
}


class PuttingIssuesManager {

    /**
     * @param {Object} opts
     * @param {string} opts.tabsId          標籤列容器 id
     * @param {string} opts.panelId         內容容器 id（一次只畫一類）
     * @param {string} opts.overallId       綜合評價容器 id
     * @param {Function} opts.onSeekSegment 「▶ 看這一段」按下去時呼叫，
     *                   參數 (起幀, 迄幀, 起點界標鍵名, 迄點界標鍵名)。
     *                   ⚠️ 幀號是判定那一列的；側面影片要靠鍵名查自己那一支的幀號。
     */
    constructor(opts) {
        this.tabsEl = document.getElementById(opts.tabsId);
        this.panelEl = document.getElementById(opts.panelId);
        this.overallEl = document.getElementById(opts.overallId);
        this.onSeekSegment = opts.onSeekSegment || function () {};
        this.items = [];
    }

    /* =================================================================
     * 文案查詢
     *
     * ⭐ 這是「換來源只換這一個函式」的接縫，三個階段都走它：
     *   ① 前一輪      寫死的三條示意文字（已刪）
     *   ② 上一輪      putting_issue_seed.json 的 18 條，
     *                 整份放在這個檔案最下面的 PUTT_ISSUE_TIPS
     *   ③ ⭐ 現在      PuttingIssueTip.java 查資料庫 putting_issue_tip，
     *                 含 Coach 查不到時退回 default（§3.2「查詢 B」、§6.3 第 15 項）
     *                 ⭐ 18 條讀進記憶體快取，⛔ 不是每張卡片查一次資料庫。
     *                 ⭐ jsp 把查好的整包放進 data.tips，⛔ 這支 .js 裡沒有任何 Java。
     *
     * ⚠️⚠️ R1：**正式機的 putting_issue_tip 表還沒有**，上線前要建表灌資料。
     *    ⭐ 測試機 2026-09-11 實測有 18 筆，Coach 全部是 'default'（⛔ 沒有任何教練專屬文案）。
     *
     * ⚠️ 只取 tip_text。seed 裡的 caveat（9 條有）⛔ 這一輪不顯示 ——
     *    「放在畫面上是加分還是扣分」是 18 條文案審閱的工項（§5.2），還沒審。
     *
     * @param {string} tipId 例：'triangle.T3.downswing.mild'
     * @returns {string} 找不到就回空字串，⛔ 不要回「查無文案」那種字。
     */
    lookupTip(tipId) {
        if (!tipId) return '';

        // ⭐ 有 tips 這個物件＝走的是資料庫那條路（jsp 從 PuttingIssueTip.java 取下來的）。
        //    → ⛔ 一律以它為準，查不到就是**沒有那段文字**（規劃 §3.2「查詢 B」：
        //      退 default → 還是查不到 → ⛔ 卡片仍然要出現，只是沒有字）。
        // ⛔⛔ 這裡⛔ 絕對不可以「查不到就偷偷退回下面那 18 條」——
        //    那會讓「資料庫沒建表／沒灌資料」在畫面上看起來**完全正常**，
        //    ⛔ 不報錯、⛔ 沒有人會發現，而正式機到現在都還沒有那張表（R1）。
        //    ⚠️ 這跟這一頁一路在防的是同一型錯誤：錯的東西安靜地看起來很合理。
        const tips = this.data && this.data.tips;
        if (tips) return tips[tipId] || '';

        // ⚠️ 完全沒有 tips 物件＝單機那條路（page/js/dev-data/verify/*.js），
        //    那時才用這個檔案最下面的 18 條。⛔ 接上資料庫的頁面走不到這一行。
        return PUTT_ISSUE_TIPS[tipId] || '';
    }

    /* =================================================================
     * 對外入口
     * ================================================================= */

    /**
     * @param {Object} data 一份頁面資料：
     *   {
     *     issues:      Core 的 issues 陣列（⚠️ 原始的，⛔ 不是排好序的）
     *     overall:     Core 的 overall（⚠️ 還沒交 → 給 null，這裡會走暫代）
     *     quality:     影片品質不足時的那一行字（⚠️ 現在一律空字串）
     *     phases:      {address, top, impact, finish, onset, trust:{...}} 跳段用
     *     tips:        （選用）已經查好的文案
     *     header:      （選用）{status, reason, view, threshold_profile}，只給「狀態」分頁用
     *   }
     */
    render(data) {
        this.data = data || {};
        const issues = this.data.issues || [];

        this.renderOverall(this.resolveOverall(issues));

        // ⭐ 分派與排序在這裡跑一次，⛔ 不要散進 render 的其他地方。
        this.renderIssues(this.classifyAndSort(issues));
    }

    /* =================================================================
     * ② 綜合評價（§2.7）
     *
     * ⭐ 值直接讀 Core 的 overall 欄，⛔ 頁面不實作規則、⛔ 不自己算。
     * ⛔ overall = null → 整塊不顯示評價（⛔ 絕不可顯示成「穩定」）。
     * ⛔ 不顯示任何分數或百分比 —— 沒有進球結果的資料，分數的意義無法辯護。
     * ⚠️ 這張卡是主畫面上唯一保留品質標示的地方，⛔ 其他卡片上全部不放。
     * ⚠️ Core 的綜合回饋文字之後補進這張卡，版面不動；
     *    目前沒有那段文字 → ⛔ 不留空框、⛔ 不寫「尚未提供」。
     * ⚠️ 整塊目前掛著 hidden-element（PM 2026-09-10：風險講太過了）。
     *    ⛔ 隱藏⛔ 不等於不做 —— 拿掉那個 class 的那一刻就要能算對六支。
     * ================================================================= */

    /**
     * Core 的 overall 有就用它，沒有才走暫代。
     * ⛔ 這裡⛔ 不可以出現任何門檻或計數規則 —— 規則只能在 computeOverallTemp()。
     */
    resolveOverall(issues) {
        const given = this.data.overall;
        // ⭐ Core 的 overall 一到，走的就是這一條，暫代那一段可以整段刪掉。
        const code = (given && typeof given.code !== 'undefined')
            ? given.code
            : this.computeOverallTemp(issues);

        return {
            code: code,
            // ⚠️ 整支未判定時右欄一定要有話說（§3.3.6，這是「不確定就不出現」的唯一例外）。
            //    白話原因就用五類共同的 na_text，⛔ 不要自己另外寫一句。
            reason: (given && given.reason) || (code === null ? this.commonNaText(issues) : ''),
            // ⚠️ 「影片品質不足，結果僅供參考」user 指示先不顯示（2026-09-09）。
            //    ⛔ 程式路徑與樣式都留著，接真資料時由 Core 說品質不足才填這個欄位。
            quality: (given && given.quality) || this.data.quality || '',
        };
    }

    /**
     * Core 的 overall 還拿不到時的**暫代**計算。
     *
     * ⚠️⚠️ 這三條規則是 Core 2026-09-03 定案的（putting_issues_schema.md §5.3），
     *      ⛔ 不是頁面自己發明的尺度。Core 2026-09-10 已確認仍是現行版本，
     *      並同意頁面暫代，⛔ 但附四個條件（以下每一條都要守）：
     *
     *   ① ⛔ 規則只能寫在這一個函式裡，⛔ 不可以散進 render。
     *      Core 的 overall 一到要能一次整段刪掉。
     *      ⭐ 已守：唯一的呼叫點是 resolveOverall()，而它只做「有沒有給」的分岔。
     *   ② ⛔ 三條規則要**按順序**套：先看有沒有 moderate/severe，再看有沒有 detected。
     *      ⛔ 順序顛倒會把「有 severe 但也有 mild」誤判成 at_risk。
     *   ③ ⛔ grade 一律讀**類別層**的 issues[].grade，⛔ 不是 metrics[].grade。
     *      metric 層那個是給數值面板與除錯用的，兩層可能不同（schema 有明寫）。
     *      ⭐ 已守：這個函式從頭到尾⛔ 沒有碰過 metrics。
     *   ④ ⛔ 算出 null 那格⛔ 絕對不可以 fallback 成「動作穩定」。
     *      ⚠️ 這條最容易在改版時被人「順手補預設值」弄壞。
     *      ⭐ 已守：null 是第一條就早退，⛔ 後面三條碰不到它。
     *
     * 規則（⛔ 一條都不要改、⛔ 不加第四條、⛔ 不加權、⛔ 不加總）：
     *   五類全部 applicable = false                     → null（⛔ 不顯示評價）
     *   有任何一類 grade ∈ {moderate, severe}           → 'marked'
     *   有任何一類 detected = true（mild 或 grade:null）→ 'at_risk'
     *   其餘（至少一類 applicable、沒有任何 detected）  → 'stable'
     *
     * ⭐ 這個規則只宣稱「最嚴重的那一類有多嚴重」，⛔ 不宣稱各類等權。
     * ⚠️ 原本的版本是四條，對 detected=true 但 grade=null 的情況一條都套不上
     *    （ex03 就落在那個洞裡）；第三條「其餘有 detected 的情況」就是為了補它。
     *
     * ⭐ 驗收：六支範例應該算出
     *   ex01 at_risk／ex02 stable／ex03 at_risk／ex04 null／ex05 marked／ex06 stable
     *   （schema §5.3 就列了這六個答案，⛔ 算不出同樣六個就是寫錯了。）
     *
     * @returns {string|null} 'stable' | 'at_risk' | 'marked' | null
     */
    computeOverallTemp(issues) {
        const list = issues || [];

        // 第 0 條：五類全部不適用 → null。
        // ⛔ 這一條一定要在最前面早退，⛔ 絕對不可以在後面補一個 'stable' 的預設值。
        const anyApplicable = list.some(function (it) { return it.applicable === true; });
        if (!anyApplicable) return null;

        // 第 1 條：先看 moderate / severe。⛔ 不可以跟第 2 條對調。
        const anyMarked = list.some(function (it) {
            return it.grade === 'moderate' || it.grade === 'severe';
        });
        if (anyMarked) return 'marked';

        // 第 2 條：其餘有 detected 的（mild 或 grade:null）。
        const anyDetected = list.some(function (it) { return it.detected === true; });
        if (anyDetected) return 'at_risk';

        // 第 3 條：至少一類 applicable、沒有任何 detected。
        return 'stable';
    }

    /** ⛔ 只做中文對應，⛔ 這裡不可以出現任何門檻或計數規則。 */
    overallText(code) {
        return { stable: '動作穩定', at_risk: '有風險', marked: '問題明顯' }[code] || null;
    }

    renderOverall(overall) {
        if (!this.overallEl) return;
        const valueEl = this.overallEl.querySelector('.putt-overall-value');
        const reasonEl = this.overallEl.querySelector('.putt-overall-reason');
        const qualityEl = this.overallEl.querySelector('.putt-overall-quality');
        if (!valueEl) return;

        const text = overall ? this.overallText(overall.code) : null;
        valueEl.className = 'putt-overall-value';

        if (text) {
            valueEl.textContent = text;
            valueEl.classList.add('is-' + overall.code.replace('_', '-'));
        } else {
            // overall = null。整支未判定時（例：ex04 側面拍攝）講白話原因，
            // ⛔ 絕不可以退回顯示「穩定」。
            valueEl.textContent = (overall && overall.noneText) || '本次未判定';
            valueEl.classList.add('is-none');
        }

        if (reasonEl) {
            reasonEl.textContent = (overall && overall.reason) || '';
            reasonEl.classList.toggle('hidden-element', !(overall && overall.reason));
        }
        if (qualityEl) {
            qualityEl.textContent = (overall && overall.quality) || '';
            qualityEl.classList.toggle('hidden-element', !(overall && overall.quality));
        }
    }

    /* =================================================================
     * ③ 推桿風險（§2.5、§2.6）
     *
     * ⚠️ 對外的標題是「推桿風險」，⛔ 不是「推桿問題」（PM 2026-09-09 指正）：
     *    這五類的判定會隨推桿距離、握桿方式、姿勢而不同。
     *    ⭐ 與規劃 §0.2 同一件事：這一頁提供的是風險，⛔ 不是精密量測值。
     * ⚠️ 內部的狀態名 state:'issue' 與變數名維持不動，⛔ 只改對使用者顯示的字。
     * ================================================================= */

    /**
     * 三態，每張卡片固定三步判斷。
     *
     *   ① applicable = false                   → 【不適用】顯示白話原因
     *   ② applicable = true, detected = false   → 【正常】一行
     *   ③ applicable = true, detected = true    → 【有問題】跑 subtypes
     *
     * ⚠️ metrics 的每一列也各有自己的 applicable —— 類別是②或③時，
     *    底下仍可能有某一列算不出來。⛔ 類別層一個旗標蓋不住。
     *    ⭐ 那一層在 segmentNote() 處理（正常但某一段沒量到 → 仍判正常、只註記）。
     *
     * ⚠️ applicable ⛔ 不是 true 就一律當「不適用」（⛔ 不是「不等於 false 就當可用」）：
     *    欄位缺漏時⛔ 寧可說「沒判」也⛔ 不要說「正常」。
     */
    decideState(issue) {
        if (!issue || issue.applicable !== true) return 'na';
        if (issue.detected !== true) return 'normal';
        return 'issue';
    }

    /**
     * 四組分派與組內排序。
     *
     *   第 1 組  grade ∈ {severe, moderate}
     *   第 2 組  detected = true 的其餘（mild 與 grade:null 並列）
     *   第 3 組  detected = false 且 applicable = true（正常）
     *   第 4 組  applicable = false（不適用）
     *   組內按五類固定順序：站姿 → 三角形變動 → 球位 → 身體位移 → 身體傾斜。
     *
     * ⛔ 不用類別權重、⛔ 不用「離門檻多遠」跨類別排（§4.1）。
     * ⚠️ ball_position 與 body_sway 的 grade 永遠是 null（只有一條線，沒有輕重之分），
     *    所以它們永遠在第 2 組。⛔ 這是刻意的，不是漏填。
     * ⚠️ 已知的刻意結果：超標 4.17 倍的身體位移會排在超標 1.5% 的三角形變動之後。
     *    ⛔ 不要「修正」它 —— 跨類別的嚴重度不可比
     *    （度／百分點／骨盆寬倍數沒有共同尺度）。要改的話得為身體位移訂第二條線，
     *    那是 Core 的工項。
     * ⛔ 卡片數不寫死：跑 issues 陣列，之後加第六類會自動長出來
     *    （不在 PUTT_CLASS_ORDER 裡的排在五類之後，維持 Core 給的先後）。
     *
     * @returns {Array} 畫得出來的清單，每一筆帶 group 與 prominence（給 pickDefaultOpen 用）
     */
    classifyAndSort(issues) {
        const self = this;
        const list = issues || [];

        const decorated = list.map(function (issue, inputIndex) {
            const state = self.decideState(issue);
            return {
                item: self.buildItem(issue, state),
                // ⚠️ 第 1 組只看類別層的 grade，⛔ 不是 metrics[].grade。
                group: (state === 'issue')
                    ? ((issue.grade === 'severe' || issue.grade === 'moderate') ? 1 : 2)
                    : (state === 'normal' ? 3 : 4),
                order: self.classOrderIndex(issue),
                inputIndex: inputIndex,
            };
        });

        decorated.sort(function (a, b) {
            if (a.group !== b.group) return a.group - b.group;
            if (a.order !== b.order) return a.order - b.order;
            // ⚠️ 同一類不會出現兩次；這一條只是讓排序穩定（未知類別照 Core 給的先後）。
            return a.inputIndex - b.inputIndex;
        });

        return decorated.map(function (d) {
            d.item.group = d.group;
            return d.item;
        });
    }

    /**
     * 五類固定順序裡的位置。
     * ⛔ 不在表內的（之後加的第六類）一律排在五類之後，⛔ 不要因此丟掉它。
     */
    classOrderIndex(issue) {
        const idx = PUTT_CLASS_ORDER.indexOf(issue && issue['class']);
        return idx === -1 ? PUTT_CLASS_ORDER.length : idx;
    }

    /** 把一筆 Core 的 issue 變成畫得出來的一筆。 */
    buildItem(issue, state) {
        const self = this;
        const key = issue['class'];
        const label = PUTT_CLASS_LABELS[key] || { title: issue.title, short: issue.title };

        const item = {
            key: key,
            // ⚠️ 顯示名稱用 PUTT_CLASS_LABELS，⛔ 不是 Core JSON 的 issues[].title（舊名）。
            title: label.title,
            short: label.short,
            state: state,
            // ⚠️ prominence 只給 pickDefaultOpen() 用，⛔ 絕不可顯示成文字（§4.5）：
            //    ex05 的傾斜離「正常區上限」其實是 +223%，只是離 severe 的入口近，
            //    畫面若寫「接近判定線」會被讀成那是輕微的。
            prominence: issue.prominence || null,
        };

        if (state === 'issue') {
            // 一類兩個子型 → ⛔ 不拆成兩類，同一類底下依序列出
            item.subtypes = (issue.subtypes || []).map(function (sub) {
                const seg = self.resolveSegment(sub, self.data && self.data.phases);
                return {
                    title: sub.title,
                    tipId: sub.tip_id,
                    // ⛔ 兩端都可信才會有值；任一端不可信 → null → 那一列⛔ 不出現跳段鈕
                    seekFrom: seg ? seg.from : null,
                    seekTo: seg ? seg.to : null,
                    // 界標鍵名：側面影片要跟著播這一段時靠它查自己的幀號
                    seekFromKey: seg ? seg.fromKey : null,
                    seekToKey: seg ? seg.toKey : null,
                    seekLabel: PUTT_SEEK_LABEL,
                };
            });
        } else if (state === 'normal') {
            // ⭐ 正常：一行就夠，每條文案只有 10〜21 字，⛔ 不做展開鈕。
            item.text = self.lookupTip(key + '.normal');
            // ⚠️ ex06 那種「只有一段取不到畫面」：另外兩段有量到 → 仍然判為正常，
            //    ⛔ 把它拉出來當「無法判定」，就是把一個有結論的項目講成沒結論。
            item.note = self.segmentNote(issue);
        } else {
            // ⛔ 不適用⛔ 不可以講成「正常」，也⛔ 不可以併進正常那一類。
            // ⚠️ 兩個都留著：naText 是 Core 給的原句（放「說明」欄），
            //    reason 是併好的整句（⛔ 不要刪，schema／除錯與之後改版都可能要引用）。
            item.naText = self.naText(issue);
            item.reason = self.naReason(issue);
        }
        return item;
    }

    /**
     * 「正常，但底下某一列算不出來」的註記（§2.5：metrics 各有自己的 applicable）。
     *
     * ⭐ 只看 decides:true 的那幾列 —— 它們才是本來要參與判定的。
     *    ⭐ Core 2026-09-10 確認這個分界跟原意一致：schema §3.1 對 decides:false
     *    的定義就是「照算、照輸出、⛔ 不判定」，所以
     *    decides === true && applicable === false ＝「本來要參與判定，但這一支量不到」。
     *    ⚠️ decides:false 的（hands_vs_shoulders_shift 等）六支裡每一支都是 not_computed，
     *    ⛔ 那是「只算不判」的參考欄位，每張卡都註記一次只會變成雜訊。
     *
     * ⚠️ 兩個⛔ 不要以為壞掉的預期行為（Core 2026-09-10）：
     *    · axis_shaft_angle_deg.backswing（上桿段）在**每一支**都是 decides:false
     *      —— ⛔ 上桿段訂不出線（池子裡最高的四名全是教練片，是素材缺口）。
     *      ⭐ 所以它永遠不會被註記，⭐ 那是預期的。
     *    · hands_vs_shoulders_shift 現在是 decides:false，⛔ 但它是「訂得出、
     *      只是現在沒素材訂」。將來補了素材它會變成 decides:true ——
     *      ⭐ 這裡吃的是 payload、⛔ 沒有寫死，那天到了會自動跟上，⛔ 不必改碼。
     *
     * ⚠️⚠️ 措辭一律走 PUTT_NA_TEXTS（Core 產生器那 11 句），⛔ 不要自己另外編一套：
     *      同一個代碼在卡片上與在數值面板上講不同的話，教練會以為是兩件事。
     *      ⛔ 尤其⛔ 不可以把所有代碼都講成「未取得畫面」——
     *      landmark_missing 的原因是關節點看不到，⛔ 不是沒取到畫面。
     */
    segmentNote(issue) {
        const self = this;
        const parts = [];
        (issue.metrics || []).forEach(function (m) {
            if (m.decides !== true || m.applicable === true) return;
            // ⛔ 還沒接上的欄位不列、不解釋
            if (m.na === 'not_computed') return;
            // 講「是哪一項」：有段名就用段名，否則用 Core 給的 metric 標題
            const tail = String(m.key || '').split('.').pop();
            const who = PUTT_METRIC_SEGMENT_LABELS[tail] || m.title || '部分項目';
            const why = self.naTextForCode(m.na);
            // ⚠️ 代碼不在那 11 個裡面（Core 之後新增的）→ ⛔ 絕不可以編一個原因，
            //    只講「這一項沒有納入判定」—— 那是從 decides/applicable 直接推得的事實。
            parts.push(why ? who + '：' + why : who + '本次沒有納入判定');
        });
        return parts.length ? '（' + parts.join('；') + '）' : '';
    }

    /* =================================================================
     * 〔詳細數值〕面板的內容
     *
     * ⭐ 畫面由 puttPanelManager 畫，這裡只把 issues 整理成它吃的形狀。
     * ⚠️ 要在 render() 之後呼叫：用的是 render() 排好、算好的 this.items，
     *    主畫面拿掉的原因（item.naText、item.note）就從那裡來，⛔ 不要另外再算一次。
     * ================================================================= */

    /**
     * @returns {{groups:Object, classes:Array, tips:{source:string, count:number}}}
     *   groups  分頁 key → {title, verdict, empty, metrics:[{name, value, note, kind}], note}
     *   classes 每一類的狀態摘要，給「狀態」分頁用（五類固定順序）
     *   tips    文案從哪裡來（db／file）、載到幾條
     */
    buildDetailSummary() {
        const self = this;
        const raw = {};
        ((this.data && this.data.issues) || []).forEach(function (issue) {
            if (issue && issue['class']) raw[issue['class']] = issue;
        });

        const ordered = this.items.slice().sort(function (a, b) {
            return self.classOrderIndex({ 'class': a.key }) - self.classOrderIndex({ 'class': b.key });
        });

        const groups = {};
        const classes = [];
        ordered.forEach(function (it) {
            const issue = raw[it.key] || {};
            // 不適用講原因；正常但某一段沒量到講那一段
            const why = (it.state === 'na') ? (it.naText || '') : (it.note || '');
            groups[it.key] = {
                title: it.title,
                verdict: { issue: '判定：有風險', normal: '判定：正常', na: '無法判定' }[it.state],
                // ⚠️ 整類不適用時分頁鈕要看得出來 —— ⛔ 不看底下還有沒有參考數值
                //    （例：球位判不出來，桿頭邊緣位置仍然有值）
                empty: it.state === 'na',
                metrics: self.buildDetailMetrics(issue.metrics),
                note: why,
            };
            classes.push({
                key: it.key,
                short: it.short,
                state: it.state,
                confidence: issue.confidence || '',
                flags: Array.isArray(issue.flags) ? issue.flags : [],
                na: issue.na || '',
                why: why,
            });
        });

        const tips = this.data && this.data.tips;
        return {
            groups: groups,
            classes: classes,
            tips: tips
                ? { source: 'db', count: Object.keys(tips).length }
                : { source: 'file', count: Object.keys(PUTT_ISSUE_TIPS).length },
        };
    }

    /**
     * metrics[] → 詳細數值的列。
     *
     * ⛔ 門檻⛔ 不寫死，一律照 bands 原樣列出。
     *    ⛔ 也⛔ 不從 bands 推「正常區間」或「觸發的是哪一條」——
     *    那要知道刻度的極性（站姿與球位是雙側，其餘單側），bands 本身不帶這個資訊。
     * ⭐ decides:false ＝ 照算、照輸出、⛔ 不判定 → 標「參考，不判定」。
     *    看起來很大的數字（例：肩線傾斜）標了才不會被讀成問題。
     * ⚠️ metrics 每一列各有自己的 applicable → 算不出來的那一列講原因。
     * ⛔ na 是 not_computed（還沒接上的欄位）整列不列。
     */
    buildDetailMetrics(metrics) {
        const self = this;
        const rows = [];
        (metrics || []).forEach(function (m) {
            if (!m || m.na === 'not_computed') return;
            const name = m.title || m.key || '';
            if (m.applicable !== true) {
                // ⚠️ 代碼不在表裡 → ⛔ 不編原因，只講沒有納入判定
                const why = self.naTextForCode(m.na);
                rows.push({ name: name, value: '', note: why || '本次沒有納入判定', kind: 'na' });
            } else if (m.decides !== true) {
                rows.push({ name: name, value: self.formatMetricValue(m), note: '參考，不判定', kind: 'reference' });
            } else {
                const bands = (Array.isArray(m.bands) && m.bands.length) ? '門檻 ' + m.bands.join(' / ') : '';
                rows.push({ name: name, value: self.formatMetricValue(m), note: bands, kind: 'decides' });
            }
        });
        return rows;
    }

    /** 值 ＋ 單位。⛔ 不另外 round，照 Core 給的位數。 */
    formatMetricValue(m) {
        if (typeof m.value !== 'number' || !isFinite(m.value)) return '';
        const suffix = (m.unit === 'deg') ? '°' : (m.unit === 'pct') ? '%' : '';
        return String(m.value) + suffix;
    }

    /** na 代碼 → 白話原因。代碼不在表裡回空字串。 */
    naTextForCode(code) {
        return Object.prototype.hasOwnProperty.call(PUTT_NA_TEXTS, code) ? PUTT_NA_TEXTS[code] : '';
    }

    /**
     * 不適用那一行的白話原因。
     * ⭐ 代碼在 PUTT_NA_TEXTS 裡就用那一句 —— metrics 層沒有 na_text、只能查表，
     *    ⛔ 兩層要講同一句話，教練才不會以為是兩件事。
     * ⚠️ 代碼不在表裡（Core 之後新增的）才用類別層 Core 給的 na_text。
     */
    naText(issue) {
        if (!issue) return '';
        if (Object.prototype.hasOwnProperty.call(PUTT_NA_TEXTS, issue.na)) {
            return PUTT_NA_TEXTS[issue.na];
        }
        return issue.na_text || '';
    }

    naReason(issue) {
        const text = this.naText(issue);
        return text ? '無法判定：' + text : '無法判定';
    }

    /** 五類共同的 na_text（只有全部一樣時才算數）。 */
    commonNaText(issues) {
        const texts = (issues || []).map(function (it) { return it.na_text || ''; });
        if (!texts.length || !texts[0]) return '';
        const same = texts.every(function (t) { return t === texts[0]; });
        return same ? texts[0] : '';
    }

    /**
     * 預設選中哪一個。
     *
     *   展開候選 = 第 1 組全部 ＋ 第 2 組裡 prominence == 'clear' 的
     *   從候選中取排序最前的一張，其餘收合（⛔ 但都留在畫面上、都可點開）
     *   沒有候選 → 回 null
     *
     * ⚠️⚠️ 「沒有候選」在標籤列版面要有退路（Core 2026-09-10，§4.8.4）：
     *    舊版五張卡片都在畫面上，一張都不展開沒問題；
     *    新版一次只顯示一類，一個都不選 → ⛔ 內容區整塊空白。
     *    → ⭐ 退路寫在 renderIssues() 那一端（pickDefaultOpen(...) ?? 0），
     *      ⛔ 不要改這個函式的回傳值 —— 「候選」的語意要留給 schema 引用。
     *
     * ⭐ 白話：打開最上面那張「值得講」的卡片。
     * ⚠️ 第 1 組的 prominence 是 null（Core 只在 grade ∈ {null, mild} 時給值），
     *    所以⛔ 不會出現「severe 但 borderline 被收起來」的情況，由資料保證。
     * ⛔ prominence ⛔ 不可顯示成文字，它只用來決定選哪一個。
     * ⛔ 頁面⛔ 不定「多近算貼近」—— prominence 由 Core 算（§4.2）。
     *
     * ⚠️⚠️ 六支範例還是 9/3 版、⛔ 沒有 prominence 欄 →
     *    下面第 2 組那個條件現在**永遠不成立**，⛔ 這是預期的。
     *    ⛔ 不要為了測它自己編一個 prominence 值進去。
     *
     * @param {Array} sortedIssues classifyAndSort() 的結果（⚠️ 一定要是排好序的）
     * @returns {number|null} 要選中的索引，沒有候選就是 null
     */
    pickDefaultOpen(sortedIssues) {
        const list = sortedIssues || [];
        for (let i = 0; i < list.length; i++) {
            const it = list[i];
            if (it.group === 1) return i;
            if (it.group === 2 && it.prominence === 'clear') return i;
        }
        return null;
    }

    /**
     * 「▶ 看這一段」能不能跳、跳去哪。
     *
     *   下桿段 = [頂點, 碰球]、送桿段 = [碰球, 收桿]、上桿段 = [起桿, 頂點]
     *   ⛔ 兩端都可信才准跳；任一端不可信 → 提示不出現
     *      （卡片照樣可展開看文案）。
     *
     * ⚠️⚠️⚠️ 可不可信⛔ 一定要看 phases.trust，⛔ 絕對不可以用「值存不存在」判斷。
     *      Core 2026-09-10 明確提醒（putting_columns.md §2.2.2）：
     *      **界標找不到時，放進去的值仍然落在合法範圍內** ——
     *        address 找不到 → 0、top 找不到 → 0 或推估值、finish 找不到 → n−1。
     *      那些值拿去 seek ⛔ 不會出錯、⛔ 不會拋例外，
     *      ⛔ **只會安靜地跳到錯的地方**，而畫面看起來完全正常。
     * ⚠️ onset 是唯一有哨兵的（缺值 −1，§3.3.5），⛔ 但也一樣要過 trust 那一關。
     *    ⛔ onset = −1 時⛔ 不要拿架桿代替 —— 架桿到起桿之間是瞄準停頓，
     *    實務上可能好幾秒，硬算會得到「上桿時間 5.3 秒」。
     * ⚠️ impact ⛔ 沒有對應的 found 布林（找不到就走 impact_missing 早退），
     *    所以它的可信度要由呼叫端在 trust.impact 明確給（§4.2）。
     *
     * ⭐⭐ 這⛔ 不是裝飾，⛔ 不可以被砍成加分項。
     *    它是「碰球界標找錯」這個缺口唯一的現場檢查手段：
     *    Core 的分期品質分級只檢查架桿／頂點／收桿，⛔ 不檢查碰球，
     *    而碰球的規則是「全片速度最快的那一幀」、⛔ 沒有範圍限制 ——
     *    影片裡有試揮或收桿後走開撿球就會選錯。
     *    實測抓錯的案例裡有一半是品質良好、會直接發布的
     *    （phase_status = OK、confidence = high、prominence = clear，
     *     每個欄位都說可信，而窗是錯的）。
     *    教練點下去看到的不是下桿，當場就會發現。
     *    ⛔ 沒有這個互動，那個超標 4 倍的數字沒有任何辦法被質疑。
     *
     * @param {Object} subtype Core 的 subtypes[] 一筆（⚠️ segment 可能是 null）
     * @param {Object} phases  {address, top, impact, finish, onset, trust:{...}}
     * @returns {{from:number, to:number, fromKey:string, toKey:string}|null} 不能跳就是 null
     *
     * ⚠️ 幀號與界標鍵名兩個都要回：幀號是判定那一列（正面）的，
     *    側面影片要靠鍵名去查自己那一支的幀號 —— ⛔ 兩支的幀號不可互換。
     */
    resolveSegment(subtype, phases) {
        // ⚠️ segment 是 null 的子型（球位 B2、手肘 T2、傾斜 R0）本來就沒有對應的一段，
        //    ⛔ 不要硬塞一段給它 —— 那會叫教練去看一段跟這條結論無關的影片。
        const segName = subtype && subtype.segment;
        const seg = segName ? PUTT_SEGMENTS[segName] : null;
        if (!seg || !phases) return null;

        // ⚠️⚠️ 安全預設：沒給 trust 就一律當⛔ 不可信，寧可少一顆鈕。
        //    ⛔ 不要改成「沒給就全部可信」—— 那會讓沒接好的資料靜靜跳到錯的幀。
        const trust = phases.trust;
        const trusted = function (key) {
            return !!(trust && trust[key] === true);
        };
        if (!trusted(seg.from) || !trusted(seg.to)) return null;

        const from = phases[seg.from];
        const to = phases[seg.to];
        if (typeof from !== 'number' || typeof to !== 'number') return null;
        // ⚠️ onset 的哨兵是 −1。⛔ 這一條⛔ 不可以拿來取代上面的 trust 判斷 ——
        //    另外三顆界標缺值時填的是合法幀號，⛔ 沒有哨兵可以檢查。
        if (from < 0 || to < 0) return null;
        // ⚠️ 收不出一段區間（起迄同一幀或反過來）就⛔ 不跳：那不是一段，看不出東西。
        if (to <= from) return null;

        return { from: from, to: to, fromKey: seg.from, toKey: seg.to };
    }

    /**
     * 把排好序的清單畫成標籤 ＋ 一次一張的內容。
     *
     * ⭐ 一次只顯示一類。⚠️ 五類仍然全部在標籤列上、全部可點 ——
     *    符合 §2.5「其餘收合，⛔ 但都留在畫面上、都可點開」。
     *    標籤上的 ●／○／⊘ 讓三態一眼看得到，⛔ 不必先點進去才知道。
     * ⛔ 標籤數不寫死：跑 items 陣列，之後加第六類會自動長出來。
     */
    renderIssues(items) {
        if (!this.tabsEl || !this.panelEl) return;
        this.items = items || [];

        // 沒有判定結果（查不到、或判定模組沒跑）→ 一行，⛔ 不接原因
        if (this.items.length === 0) {
            this.tabsEl.innerHTML = '';
            this.tabsEl.classList.add('hidden-element');
            this.panelEl.innerHTML = '<div class="putt-na-all">' + PUTT_NOT_ANALYZED_TEXT + '</div>';
            return;
        }

        // 5 項全部不適用 → 併成一行，⛔ 不要列五個都寫同一個原因的標籤
        const allNa = this.items.length > 0 && this.items.every(function (it) {
            return it.state === 'na';
        });
        if (allNa) {
            this.tabsEl.innerHTML = '';
            this.tabsEl.classList.add('hidden-element');
            this.panelEl.innerHTML = '<div class="putt-na-all">'
                + this.esc(this.naAllText()) + '</div>';
            return;
        }
        this.tabsEl.classList.remove('hidden-element');

        const dot = { issue: '●', normal: '○', na: '⊘' };
        let tabs = '';
        this.items.forEach(function (it, idx) {
            tabs += '<button type="button" class="putt-issue-tab state-' + it.state + '"'
                + ' data-index="' + idx + '">'
                // ⚠️ 標籤用短名（跟詳細數值面板那六個分頁同一套稱呼），
                //    內容區裡仍然顯示完整名稱。⛔ 標籤太長會擠成兩行、吃掉內容高度。
                + '<span class="tab-dot">' + dot[it.state] + '</span>'
                + this.esc(it.short || it.title) + '</button>';
        }, this);
        // ⚠️ ●／○／⊘ 的說明跟標籤一起產生，⛔ 不要寫在 jsp 的靜態標記裡
        //    —— 那會被這一行的 innerHTML 蓋掉。⛔ 也不要拿掉，三個記號不會自己解釋自己。
        // ⚠️ 記號⛔ 停在三態（user 2026-09-10 拍板，§4.8.4）：
        //    ⛔ 不要在標籤上並排顯示 severe／moderate／mild ——
        //    那會請教練跨類別比嚴重度，而度／百分點／骨盆寬倍數⛔ 沒有共同尺度。
        tabs += '<span class="putt-issues-legend">'
            + '<span class="d-issue">●</span> 有風險'
            + '<span class="d-normal">○</span> 正常'
            + '<span class="d-na">⊘</span> 未判定'
            + '</span>';
        this.tabsEl.innerHTML = tabs;

        const self = this;
        this.tabsEl.querySelectorAll('.putt-issue-tab').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                self.showItem(parseInt(e.currentTarget.dataset.index, 10));
            });
        });

        // ⚠️⚠️ 退路寫在這一端（§4.8.4）：pickDefaultOpen() 沒有候選時回 null，
        //    而這個版面一次只顯示一類 → 一個都不選會讓內容區整塊空白。
        //    ⭐ 那時改選排序後的第一個，⛔ 不可以真的一個都不選。
        //    ⛔ 不要把這個退路搬進 pickDefaultOpen()：那個函式的回傳值是
        //       「候選」的語意，schema 那邊要引用。
        if (this.items.length > 0) {
            const picked = this.pickDefaultOpen(this.items);
            this.showItem(picked === null ? 0 : picked);
        }
    }

    /**
     * 五項全不適用時的那一句。
     * ⛔ 卡片數不寫死 → 這句裡的數字也⛔ 不寫死。
     * ⚠️ 原因要講（§3.3.6：整支未判定時右欄一定要有話說，
     *    那時整個右欄是空的，不講話教練會以為頁面壞了）。
     */
    naAllText() {
        // ⚠️⚠️ ⛔ 不接技術原因（user 2026-09-10 裁示）——
        //      原本會接成「…都未判定：非正面拍攝，本期只支援正面」。
        //      ⭐ §3.3.6 要的只是「右欄不可以整片空白」，這一句就夠了；
        //      ⛔ 原因收進〔詳細數值〕的「狀態」分頁（§0.2）。
        return this.items.length + ' 個檢查項目本次都未判定';
    }

    showItem(index) {
        const it = this.items[index];
        if (!it) return;

        this.tabsEl.querySelectorAll('.putt-issue-tab').forEach(function (b) {
            b.classList.toggle('is-active', parseInt(b.dataset.index, 10) === index);
        });

        this.panelEl.innerHTML = this.buildBody(it);
        this.panelEl.scrollTop = 0;

        const self = this;
        this.panelEl.querySelectorAll('.putt-card-seek').forEach(function (btn) {
            btn.addEventListener('click', function () {
                self.onSeekSegment(
                    parseInt(btn.dataset.seekFrom, 10),
                    parseInt(btn.dataset.seekTo, 10),
                    btn.dataset.seekFromKey || null,
                    btn.dataset.seekToKey || null
                );
            });
        });
    }

    /**
     * 一類的內容。⭐ **三態共用同一套殼**（user 2026-09-10 裁示）。
     *
     * ⭐⭐ 結構與 class 完全比照 GM08_short.css 的建議區（.p_de）：
     *     .p_de_title_container / .p_de_title  記號 ＋ 類別名稱 ＋ 3px 藍底線
     *     .p_de_content ＋ .p_de_label ＋ .p_de_posture / .p_de_re  「標籤：內容」兩欄
     *     ⛔ 不要在推桿頁另立一套 —— 三個分頁切換時建議區的長相要一致。
     *
     * ⚠️⚠️ **為什麼三態要長一樣**（user 2026-09-10 看了實際畫面之後裁示）：
     *      原本只有「有風險」有藍色標題列與「狀況／說明」兩欄，
     *      「正常」與「不適用」只有一行黑字 → 版面像兩個不同的頁面。
     *      ⭐ 改成同一套殼之後，換標籤時只有**內容**在變，⛔ 版面不會整個換掉。
     *
     * ⛔⛔ **這⛔ 不是「把不適用講成正常」**（§2.5 明文禁止）——
     *      三態的區別由**記號與文字**承擔，⛔ 不是由「有沒有版面」承擔：
     *        ● 有風險 → 狀況是子型名稱
     *        ○ 正常   → 狀況寫「正常」
     *        ⊘ 未判定 → 狀況寫「無法判定」
     *      ⛔ 而且三者仍然分屬不同組、⛔ 排序沒有變。
     *
     * ⛔⛔ 也⛔ 不是拿東西填版面（§0.3）：
     *      正常那一欄放的是該類**本來就有**的正常文案（10〜21 字），
     *      不適用那一欄放的是 **Core 給的 na_text**，⛔ 兩者都不是新編的。
     */
    buildBody(it) {
        const self = this;
        const mark = { issue: '●', normal: '○', na: '⊘' }[it.state] || '';

        // ⚠️ 記號放在標題列，三態同一個位置 —— 標籤列上那個記號是「哪一類是什麼狀態」，
        //    這一個是「你現在看的這一類是什麼狀態」。⛔ 兩個都要，⛔ 不要拿掉任何一個。
        let html = '<div class="p_de_title_container">'
            + '<span class="p_de_title">'
            + '<span class="state-mark state-' + it.state + '">' + mark + '</span>'
            + this.esc(it.title) + '</span></div>';

        if (it.state === 'issue') {
            // 一類兩個子型 → ⛔ 不拆成兩類，同一類底下依序列出
            (it.subtypes || []).forEach(function (sub) {
                html += self.buildRow(sub.title, self.lookupTip(sub.tipId), '', sub);
            });
        } else if (it.state === 'normal') {
            // ⭐ 正常就是正常。
            // ⚠️⚠️ it.note（ex06 那種「只有一段取不到畫面」）⛔ **不放主畫面**
            //      （user 2026-09-10 裁示）：這一類已經判為正常，
            //      在後面補一句「某一段沒取到」等於自己扣自己一句，
            //      ⛔ 而且那是技術性的品質標示 —— §0.2 明寫那種東西
            //      **全部收進〔詳細數值〕面板**。
            //      ⭐ 值仍然算好、掛在 item.note 上，⛔ 不要刪 ——
            //      工項 13 要把它放進〔詳細數值〕的「狀態」分頁。
            html += this.buildRow('正常', it.text, '', null);
        } else {
            // ⛔ 不適用⛔ 不可以講成「正常」，也⛔ 不可以併進正常那一類。
            // ⚠️⚠️ 主畫面**只講「無法判定」**，⛔ 不接技術原因（user 2026-09-10 裁示）。
            //      ⛔ 原本接的是 Core 的 na_text，例如「只能從桿頭推出球的位置範圍，
            //      而那個範圍跨過判定線」「這一項尚未接上，之後會補」——
            //      ⛔ 那是在跟教練逐格解釋我們哪裡沒做到。
            //      ⭐ 原因收進〔詳細數值〕的「狀態」分頁（§0.2、§2.9 本來就這樣定）。
            //      ⭐ it.naText 仍然帶著，⛔ 不要刪 —— 工項 13 要用。
            html += this.buildRow('無法判定', '', '', null);
        }
        return html;
    }

    /**
     * 「狀況／說明」兩欄的一組。
     *
     * ⚠️⚠️ 「▶ 看這一段」掛在**每一個子型的「狀況」那一列**，⛔ 不是類別標題列。
     *      理由：一類兩個子型時兩條可能落在不同段（ex05 的三角形變動是
     *      「送桿段 T3」＋「手肘 T2」，後者⛔ 根本沒有對應的段），
     *      ⛔ 標題列只放得下一顆鈕 → 教練無從知道它要帶你去看哪一段。
     *      ⭐ 規劃 §2.6 的圖本來就是把鈕畫在子型文字旁邊：
     *         「● 身體左右晃動  下半身左右移動（主要在下桿）  ▶ 看這一段」
     *
     * @param {string} status  「狀況」欄的字（結論）
     * @param {string} explain 「說明」欄的字（內文）
     * @param {string} note    附註，⚠️ 接在說明後面、字級小一點（⛔ 可以是空字串）
     * @param {Object} sub     子型，⚠️ 只有它會帶跳段；⛔ 沒有就傳 null
     */
    buildRow(status, explain, note, sub) {
        // ⚠️ 沒有說明就⛔ 不出那一列（⛔ 不是出一列空的）——
        //    「無法判定」那一格就是這條路：主畫面只講結論，原因在〔詳細數值〕。
        // ⛔ 兩端都可信才准跳；任一端不可信 → 這顆鈕⛔ 不出現（文案照樣看得到）。
        //    ⚠️⚠️ 可跳判斷是 resolveSegment()，⛔ 絕對不可以在這裡用
        //         「值存不存在」重新判一次 —— 界標找不到時填的值落在合法範圍內。
        const canSeek = sub && typeof sub.seekFrom === 'number' && typeof sub.seekTo === 'number';
        return '<p class="p_de_content">'
            + '<span class="p_de_label">狀況</span>'
            + '<span class="p_de_posture">' + this.esc(status)
            + (canSeek
                ? '<button class="putt-card-seek" type="button" data-seek-from="' + sub.seekFrom
                    + '" data-seek-to="' + sub.seekTo + '"'
                    + ' data-seek-from-key="' + this.esc(sub.seekFromKey || '')
                    + '" data-seek-to-key="' + this.esc(sub.seekToKey || '') + '">'
                    + this.esc(sub.seekLabel || PUTT_SEEK_LABEL) + '</button>'
                : '')
            + '</span>'
            + '</p>'
            + (explain
                ? '<p class="p_de_content">'
                    + '<span class="p_de_label">說明</span>'
                    + '<span class="p_de_re">' + this.esc(explain)
                    + (note ? '<span class="note">' + this.esc(note) + '</span>' : '')
                    + '</span>'
                    + '</p>'
                : '');
    }

    esc(s) {
        if (s === null || s === undefined) return '';
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }
}


/* =====================================================================
 * 文案：putting_issue_seed.json 的 18 條（13 條錯誤 ＋ 5 條正常）
 *
 * ⭐ 出處 golf_anl_ws/.../working_spec/feedback/putting_issue_seed.json，
 *    ⚠️ 那一份是權威來源，⛔ 改文案要回去改它再同步過來，⛔ 不要只改這裡。
 * ⚠️ 只抄 tip_text。seed 裡還有 caveat（9 條有）與 source，
 *    ⛔ caveat 這一輪不顯示 —— 要不要放在畫面上是 18 條文案審閱的工項（§5.2）。
 * ⚠️ swing_angle 的 moderate 與 severe **共用同一句**（swing_angle.R0.marked），
 *    ⛔ 不是漏抄（§3.2「查詢 B」）。
 * ⚠️ 手肘那條是 triangle.T2，⛔ 不是 T4。
 * ⛔ 建表之後（工項 15）整段刪掉，改由 PuttingIssueTip.java 查
 *    putting_issue_tip，含 Coach 查不到時退回 default。⛔ 只換 lookupTip()。
 * ===================================================================== */
const PUTT_ISSUE_TIPS = {
    'stance.S1':
        '架桿時兩腳靠得比多數高水準球員近一些。可以試著把雙腳稍微打開一點，回到自然站立時的寬度，站起來會比較穩。',
    'stance.S2':
        '架桿時兩腳站得比多數高水準球員開一些。可以試著把雙腳稍微收近一點，回到自然站立時的寬度，比較容易用身體帶動而不是用手。',
    'triangle.T3.followthrough.mild':
        '球打出去之後，球桿比多數高水準球員更早離開手臂帶動的方向。可以試著讓手臂、雙手和球桿在送桿時一起停，而不是讓桿頭自己往前跑。',
    'triangle.T3.followthrough.severe':
        '球打出去之後，球桿明顯不再跟著手臂走。可以試著在架桿時記住手腕的角度，並在整個推擊過程中——包含球打出去之後——維持它。',
    'triangle.T3.downswing.mild':
        '往下推向球的那一段，球桿比多數高水準球員更早離開手臂帶動的方向。可以試著讓手臂和球桿一起下來，而不是用手去「甩」球桿。',
    'triangle.T3.downswing.severe':
        '往下推向球的那一段，球桿明顯不再跟著手臂走。可以試著在架桿時記住手腕的角度，讓手臂帶著球桿一起下來，整段維持同一個形狀。',
    'triangle.T2':
        '推擊過程中手肘比架桿時彎了不少，比多數高水準球員明顯。可以試著在架桿時把兩隻手臂的形狀記住，整段維持它。',
    'ball_position.B2':
        '把球往目標側挪一點，大約落在站姿中央再往前一點的位置，讓桿頭在觸球時已經開始往上走。',
    'ball_position.B1':
        '球放得太靠前會讓你必須往前搆，把球收回到接近站姿中央再稍前的位置。',
    'body_sway.W0.downswing':
        '把球桿揮下去打到球的過程中，你的下半身有往旁邊移動。試著讓兩腳和骨盆固定住，只用肩膀帶動雙臂把桿送出去。',
    'body_sway.W0.backswing':
        '把球桿往後拉的時候，你的下半身就開始往旁邊移動了。試著讓下半身固定住，只讓上半身轉動。',
    'swing_angle.R0.mild':
        '從起桿到擊球的過程中，你的上半身相對於架桿時稍微歪了一點。試著把頭和身體固定住，只讓肩膀帶動雙臂。',
    'swing_angle.R0.marked':
        '從起桿到擊球的過程中，你的上半身明顯離開了架桿時的位置。試著讓下半身和頭固定住，只用肩膀帶動雙臂把桿送出去。',
    'stance.normal':
        '站姿落在常規範圍內。',
    'triangle.normal':
        '從架桿到收桿，肩膀與雙臂的形狀維持得不錯。',
    'ball_position.normal':
        '球位落在常規範圍內。',
    'body_sway.normal':
        '整個推擊過程中你的下半身很穩定。',
    'swing_angle.normal':
        '推擊過程中你的身體維持得很穩定。',
};


/* =====================================================================
 * 沒有判定結果時的底稿（⛔ 不是示範資料）。
 *
 * jsp 以它為底，有判定結果時用 applyPuttJudgement() 蓋上去。
 * ⛔ 不可以放示範卡片或示範數字：放了就會讓沒有判定結果的每一推都顯示同一組內容。
 * ===================================================================== */
const PUTT_ISSUES_EMPTY_DATA = {

    // 沒有判定結果 → null。⛔ 絕不可以填成 stable
    overall: null,

    // ⚠️ 「影片品質不足，結果僅供參考」user 指示先不顯示（2026-09-09）。
    //    ⛔ 程式路徑與樣式都留著，接真資料時由 Core 說品質不足才填。
    quality: '',

    // ⚠️ 沒有判定結果。頁面上的真資料由 jsp 從 PuttingData 取；
    //    沒有判定結果時就是這一份 → 顯示「尚未分析」。
    //    ⛔ 不可以放示範卡片：放了就會讓沒有判定結果的每一推都顯示同一組風險。
    issues: [],
};
