/**
 * @fileoverview puttingIssuesManager.js — 推桿頁「右欄」的卡片管理。
 *
 * 管的範圍（一個功能一支 js）：
 *   ① 最近 N 推的一致性（標題與圖說）        規劃文件 §2.10
 *   ② 綜合評價                              規劃文件 §2.7
 *   ③ 推桿風險（三態 ＋ 分頁切換 ＋ 跳段）      規劃文件 §2.5、§2.6
 *
 * ⛔ 不管左欄的界標列與數值面板 —— 那些在 puttPanelManager.js。
 * ⚠️ 檔名照 feedback_impl_spec.md §5.3 的命名，⛔ 不要改成別的。
 * 規劃文件：docs/expert-data-v8-putt-plan.md
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
 * ⚠️⚠️ 這一輪（§6.1 第 2〜3 項）只把版面撐出來。
 *
 * 已經做的：把「已經整理好的一份清單」畫出來，讓三種樣子都看得到
 *           （有問題／正常／不適用，含一類兩個子型與「▶ 看這一段」）。
 * ⛔ 還沒做（下一輪 §6.1 工項 4〜8）：
 *     · 四組分派與組內排序          → classifyAndSort()   目前是 TODO
 *     · 三態判斷                    → decideState()       目前是 TODO
 *     · 預設選哪一個（prominence）  → pickDefaultOpen()    目前是 TODO
 *     · 「▶ 看這一段」的可跳判斷    → resolveSegment()     目前是 TODO
 *   ⛔ 不要因為版面看起來會動了就以為那幾條規則已經寫好。
 * ═══════════════════════════════════════════════════════════════
 */

class PuttingIssuesManager {

    /**
     * @param {Object} opts
     * @param {string} opts.tabsId          標籤列容器 id
     * @param {string} opts.panelId         內容容器 id（一次只畫一類）
     * @param {string} opts.overallId       綜合評價容器 id
     * @param {string} opts.consistencyId   一致性區塊 id
     * @param {Function} opts.onSeekSegment 「▶ 看這一段」按下去時呼叫，參數 (起幀, 迄幀)
     */
    constructor(opts) {
        this.tabsEl = document.getElementById(opts.tabsId);
        this.panelEl = document.getElementById(opts.panelId);
        this.overallEl = document.getElementById(opts.overallId);
        this.consistencyEl = document.getElementById(opts.consistencyId);
        this.onSeekSegment = opts.onSeekSegment || function () {};
        this.items = [];
    }

    /* =================================================================
     * 文案查詢
     *
     * ⭐ 這是「換來源只換這一個函式」的接縫，三個階段都走它：
     *   ① 現在        寫死在這個檔案下面的 PUTT_ISSUES_DEV_DATA.tips
     *   ② 下一輪      改讀 putting_issue_seed.json 的 18 條（§6.1 第 6 項）
     *   ③ 建表之後    改由 PuttingIssueTip.java 查資料庫 putting_issue_tip，
     *                 含 Coach 查不到時退回 default（§3.2「查詢 B」、§6.3 第 15 項）
     *                 ⚠️ 18 條讀進記憶體快取，⛔ 不要每張卡片查一次資料庫。
     * ⚠️ R1：正式機的 putting_issue_tip 表還沒有，18 條文案目前只在測試機。
     *
     * @param {string} tipId 例：'triangle.T3.downswing.mild'
     * @returns {string} 找不到就回空字串，⛔ 不要回「查無文案」那種字。
     */
    lookupTip(tipId) {
        const tips = (this.data && this.data.tips) || {};
        return tips[tipId] || '';
    }

    /* =================================================================
     * 對外入口
     * ================================================================= */

    /**
     * @param {Object} data 已經整理好的一份頁面資料。
     * ⚠️ 這一輪傳進來的是寫死的假資料 PUTT_ISSUES_DEV_DATA。
     * ⛔ 下一輪要改成傳 Core 的 issues 陣列，並在這裡先跑 classifyAndSort()。
     */
    render(data) {
        this.data = data;
        this.renderConsistency(data.consistency);
        this.renderOverall(data.overall);
        this.renderIssues(data);
    }

    /* =================================================================
     * ① 最近 N 推的一致性（§2.10）
     *
     * ⭐ 圖上⛔ 不出現任何絕對距離數字 —— 模擬器估的推桿距離不準，
     *    但「彼此散得多開」可信（系統性誤差對每一球都一樣，會互相抵消）。
     *    這張圖回答「這個人穩不穩」，⛔ 不是「這一推準不準」。
     * ⛔ 不做雷達圖（推桿只有 3 個站得住的維度）、⛔ 不做曲線球。
     * ⛔ 後旋／側旋／擊球效率／飛行距離⛔ 不顯示，對推桿無意義。
     * ⚠️ 撈幾推是參數，⛔ 不寫死（切桿現在是寫死 10）。
     * ⚠️ 落點圖本身排在第一階段之後（§6.4），現在只填標題與圖說。
     * ================================================================= */
    renderConsistency(consistency) {
        if (!this.consistencyEl || !consistency) return;
        const titleEl = this.consistencyEl.querySelector('.box-title');
        const legendEl = this.consistencyEl.querySelector('.putt-consistency-legend');
        const noteEl = this.consistencyEl.querySelector('.putt-consistency-note');

        // ⚠️ 標題講這張圖回答什麼（穩不穩），⛔ 不要寫成「最近 N 推」——
        //    那只講了資料範圍，沒講這張圖在回答什麼。推幾推放在下面那行說明裡。
        if (titleEl) titleEl.textContent = consistency.title;

        // ⚠️ 圖說只留一行、字要少。⛔ 不要再列四個記號 ——
        //    ⊕ 在正中央、舊球本來就比較淡，⛔ 不必逐一解釋。
        if (legendEl) {
            legendEl.innerHTML =
                '<span class="mk mk-new">●</span> 最新一推'
                + '<span class="mk mk-avg">⊕</span> 平均'
                + '<span class="mk"></span>' + this.esc(consistency.note);
        }
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
     * ================================================================= */

    /**
     * ⛔ TODO（下一輪 §6.1 工項 7）：Core 的 overall 還拿不到時的**暫代**計算。
     *
     * ⚠️⚠️ 這三條規則是 Core 2026-09-03 定案的（putting_issues_schema.md §5.3），
     *      ⛔ 不是頁面自己發明的尺度。Core 2026-09-10 已確認仍是現行版本，
     *      並同意頁面暫代，⛔ 但附四個條件（以下每一條都要守）：
     *
     *   ① ⛔ 規則只能寫在這一個函式裡，⛔ 不可以散進 render。
     *      Core 的 overall 一到要能一次整段刪掉。
     *   ② ⛔ 三條規則要**按順序**套：先看有沒有 moderate/severe，再看有沒有 detected。
     *      ⛔ 順序顛倒會把「有 severe 但也有 mild」誤判成 at_risk。
     *   ③ ⛔ grade 一律讀**類別層**的 issues[].grade，⛔ 不是 metrics[].grade。
     *      metric 層那個是給數值面板與除錯用的，兩層可能不同（schema 有明寫）。
     *   ④ ⛔ 算出 null 那格⛔ 絕對不可以 fallback 成「動作穩定」。
     *      ⚠️ 這條最容易在改版時被人「順手補預設值」弄壞。
     *
     * 規則（⛔ 一條都不要改、⛔ 不加權、⛔ 不加總）：
     *   五類全部 applicable = false                     → null（⛔ 不顯示評價）
     *   有任何一類 grade ∈ {moderate, severe}           → 'marked'
     *   有任何一類 detected = true（mild 或 grade:null）→ 'at_risk'
     *   其餘（至少一類 applicable、沒有任何 detected）  → 'stable'
     *
     * ⭐ 驗收：六支範例應該算出
     *   ex01 at_risk／ex02 stable／ex03 at_risk／ex04 null／ex05 marked／ex06 stable
     *   （schema §5.3 就列了這六個答案，⛔ 算不出同樣六個就是寫錯了。）
     *
     * @returns {string|null} 'stable' | 'at_risk' | 'marked' | null
     */
    computeOverallTemp(issues) {
        throw new Error('computeOverallTemp: 尚未實作（§6.1 工項 7，暫代）');
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
     * ⛔ TODO（下一輪 §6.1 工項 4）：四組分派與組內排序。
     *
     *   第 1 組  grade ∈ {severe, moderate}
     *   第 2 組  detected = true 的其餘（mild 與 grade:null 並列）
     *   第 3 組  detected = false 且 applicable = true（正常）
     *   第 4 組  applicable = false（不適用）
     *   組內按五類固定順序：站姿 → 三角形變動 → 球位 → 身體位移 → 身體傾斜。
     *
     * ⛔ 不用類別權重、⛔ 不用「離門檻多遠」跨類別排。
     * ⚠️ ball_position 與 body_sway 的 grade 永遠是 null（只有一條線，沒有輕重之分），
     *    所以它們永遠在第 2 組。⛔ 這是刻意的，不是漏填。
     * ⚠️ 已知的刻意結果：超標 4.17 倍的身體位移會排在超標 1.5% 的三角形變動之後。
     *    ⛔ 不要「修正」它 —— 跨類別的嚴重度不可比
     *    （度／百分點／骨盆寬倍數沒有共同尺度）。要改的話得為身體位移訂第二條線，
     *    那是 Core 的工項。
     * ⛔ 卡片數不寫死：跑 issues 陣列，之後加第六類會自動長出來。
     */
    classifyAndSort(issues) {
        throw new Error('classifyAndSort: 尚未實作（§6.1 工項 4）');
    }

    /**
     * ⛔ TODO（下一輪 §6.1 工項 4）：三態，每張卡片固定三步判斷。
     *
     *   ① applicable = false                   → 【不適用】顯示白話原因
     *   ② applicable = true, detected = false   → 【正常】一行灰字
     *   ③ applicable = true, detected = true    → 【有問題】跑 subtypes
     *
     * ⚠️ metrics 的每一列也各有自己的 applicable —— 類別是②或③時，
     *    底下仍可能有某一列算不出來。⛔ 類別層一個旗標蓋不住。
     */
    decideState(issue) {
        throw new Error('decideState: 尚未實作（§6.1 工項 4）');
    }

    /**
     * ⛔ TODO（下一輪 §6.1 工項 5）：預設展開哪一張。
     *
     *   展開候選 = 第 1 組全部 ＋ 第 2 組裡 prominence == 'clear' 的
     *   從候選中取排序最前的一張展開，其餘收合（⛔ 但都留在畫面上、都可點開）
     *   沒有候選 → 回 null（⚠️ 標籤列版面的退路寫在 renderIssues()，見下）
     *
     * ⚠️⚠️ 「沒有候選」在標籤列版面要有退路（Core 2026-09-10，§4.8.4）：
     *    舊版五張卡片都在畫面上，一張都不展開沒問題；
     *    新版一次只顯示一類，一個都不選 → ⛔ 內容區整塊空白。
     *    → ⭐ 退路是 renderIssues() 那一端寫 pickDefaultOpen(...) ?? 0，
     *      ⛔ 不要改這個函式的回傳值 —— 「候選」的語意要留給 schema 引用。
     *
     * ⭐ 白話：打開最上面那張「值得講」的卡片。
     * ⚠️ 第 1 組的 prominence 是 null（Core 只在 grade ∈ {null, mild} 時給值），
     *    所以⛔ 不會出現「severe 但 borderline 被收起來」的情況，由資料保證。
     * ⛔ prominence ⛔ 不可顯示成文字，它只用來決定展開哪一張。
     * ⛔ 頁面⛔ 不定「多近算貼近」—— prominence 由 Core 算。
     *
     * ⚠️⚠️ 到 2026-09-09 為止，六支範例還是 9/3 版，⛔ 沒有 prominence 欄。
     *    → 下一輪先做「第 1 組全部展開」那半，prominence 那半等 Core 重產範例。
     */
    pickDefaultOpen(sortedIssues) {
        throw new Error('pickDefaultOpen: 尚未實作（§6.1 工項 5）');
    }

    /**
     * ⛔ TODO（下一輪 §6.1 工項 8）：「▶ 看這一段」能不能跳、跳去哪。
     *
     *   下桿段 = [頂點, 碰球]、送桿段 = [碰球, 收桿]、上桿段 = [起桿, 頂點]
     *   ⛔ 兩端都可信才准跳；任一端不可信 → 提示不出現
     *      （卡片照樣可展開看文案）。
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
     */
    resolveSegment(subtype, phases) {
        throw new Error('resolveSegment: 尚未實作（§6.1 工項 8）');
    }

    /**
     * 把一份「已經整理好的」清單畫成標籤 ＋ 一次一張的內容。
     *
     * ⭐ 一次只顯示一類。⚠️ 五類仍然全部在標籤列上、全部可點 ——
     *    符合 §2.5「其餘收合，⛔ 但都留在畫面上、都可點開」。
     *    標籤上的 ●／○／⊘ 讓三態一眼看得到，⛔ 不必先點進去才知道。
     * ⛔ 標籤數不寫死：跑 items 陣列，之後加第六類會自動長出來。
     *
     * ⚠️ 這一輪傳進來的 items 已經是排好序、標好狀態的假資料，
     *    ⛔ 這裡沒有跑 classifyAndSort() / decideState() / pickDefaultOpen()。
     *    下一輪要讓 classifyAndSort() 產出的就是這個形狀的陣列。
     */
    renderIssues(data) {
        if (!this.tabsEl || !this.panelEl) return;
        const items = data.items || [];
        this.items = items;

        // 5 項全部不適用 → 併成一行，⛔ 不要列五個都寫同一個原因的標籤
        const allNa = items.length > 0 && items.every(function (it) { return it.state === 'na'; });
        if (allNa) {
            this.tabsEl.innerHTML = '';
            this.tabsEl.classList.add('hidden-element');
            this.panelEl.innerHTML = '<div class="putt-na-all">' + this.esc(data.naAllText) + '</div>';
            return;
        }
        this.tabsEl.classList.remove('hidden-element');

        const dot = { issue: '●', normal: '○', na: '⊘' };
        let tabs = '';
        items.forEach(function (it, idx) {
            tabs += '<button type="button" class="putt-issue-tab state-' + it.state + '"'
                + ' data-index="' + idx + '">'
                // ⚠️ 標籤用短名（跟詳細數值面板那六個分頁同一套稱呼），
                //    內容區裡仍然顯示完整名稱。⛔ 標籤太長會擠成兩行、吃掉內容高度。
                + '<span class="tab-dot">' + dot[it.state] + '</span>'
                + this.esc(it.short || it.title) + '</button>';
        }, this);
        // ⚠️ ●／○／⊘ 的說明跟標籤一起產生，⛔ 不要寫在 jsp 的靜態標記裡
        //    —— 那會被這一行的 innerHTML 蓋掉。⛔ 也不要拿掉，三個記號不會自己解釋自己。
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

        // ⚠️ 預設選第一個。正式規則是 pickDefaultOpen()（§6.1 工項 5），
        //    ⛔ 這一輪沒有實作 —— 假資料本身已經排好序，第一個剛好就是答案。
        // ⚠️⚠️ 下一輪換成 pickDefaultOpen() 時，⛔ 不可以直接用它的回傳值：
        //    它「沒有候選」時回 null，而這個版面一次只顯示一類 →
        //    一個都不選會讓內容區整塊空白（§4.8.4）。
        //    ⭐ 寫成 const i = this.pickDefaultOpen(items); this.showItem(i == null ? 0 : i);
        //    ⛔ 退路寫在這一端，⛔ 不要改 pickDefaultOpen() 的回傳值。
        if (items.length > 0) this.showItem(0);
    }

    showItem(index) {
        const it = this.items[index];
        if (!it) return;

        this.tabsEl.querySelectorAll('.putt-issue-tab').forEach(function (b) {
            b.classList.toggle('is-active', parseInt(b.dataset.index, 10) === index);
        });

        let html = '';
        if (it.state === 'issue') {
            html = this.buildIssueBody(it);
        } else if (it.state === 'normal') {
            // ⭐ 正常：一行就夠，每條文案只有 10〜21 字，⛔ 不做展開鈕。
            // ⚠️ note 是 ex06 那種「只有一段取不到畫面」的情況：另外兩段有量到
            //    → 仍然判為正常，只在後面註明。⛔ 把它講成「無法判定」，
            //    就是把一個有結論的項目講成沒結論。
            html = '<div class="putt-oneline is-normal">'
                + '<span class="state-mark">○</span>'
                + this.esc(it.title) + '　' + this.esc(it.text)
                + (it.note ? '<span class="note">' + this.esc(it.note) + '</span>' : '')
                + '</div>';
        } else {
            // ⛔ 不適用⛔ 不可以講成「正常」，也⛔ 不可以併進正常那一類。
            html = '<div class="putt-oneline is-na">'
                + '<span class="state-mark">⊘</span>'
                + this.esc(it.title) + '　' + this.esc(it.reason)
                + '</div>';
        }

        this.panelEl.innerHTML = html;
        this.panelEl.scrollTop = 0;

        const self = this;
        this.panelEl.querySelectorAll('.putt-card-seek').forEach(function (btn) {
            btn.addEventListener('click', function () {
                self.onSeekSegment(
                    parseInt(btn.dataset.seekFrom, 10),
                    parseInt(btn.dataset.seekTo, 10)
                );
            });
        });
    }

    /**
     * 有風險那一類的內容。
     *
     * ⭐⭐ 結構與 class 完全比照 GM08_short.css 的建議區（.p_de）：
     *     .p_de_title_container / .p_de_title  類別名稱 ＋ 3px 藍底線
     *     .p_de_content ＋ .p_de_label ＋ .p_de_posture / .p_de_re  「標籤：內容」兩欄
     *     ⛔ 不要在推桿頁另立一套 —— 三個分頁切換時建議區的長相要一致。
     */
    buildIssueBody(it) {
        const self = this;
        // 一類兩個子型 → ⛔ 不拆成兩類，同一類底下依序列出
        const subs = it.subtypes || [];

        // ⛔ 兩端都可信才准跳；任一端不可信 → 這顆鈕⛔ 不出現（文案照樣看得到）。
        //    ⚠️ 可跳判斷是 resolveSegment()（§6.1 工項 8），⛔ 這一輪還沒實作。
        //    ⚠️⚠️ Core 提醒（2026-09-10）：界標找不到時放的值**落在合法範圍內**
        //         （address 缺是 0、top 缺是 0 或推估值、finish 缺是 n−1），
        //         ⛔ 拿去 seek 不會出錯，只會安靜跳到錯的地方。
        //         → 一定要先判可不可信，⛔ 不可以用「值存不存在」當判斷。
        const canSeek = typeof it.seekFrom === 'number' && typeof it.seekTo === 'number';

        let html = '<div class="p_de_title_container">'
            + '<span class="p_de_title">' + this.esc(it.title)
            + (canSeek
                ? '<button class="putt-card-seek" type="button" data-seek-from="' + it.seekFrom
                    + '" data-seek-to="' + it.seekTo + '">' + this.esc(it.seekLabel) + '</button>'
                : '')
            + '</span></div>';

        subs.forEach(function (sub) {
            html += '<p class="p_de_content">'
                + '<span class="p_de_label">狀況</span>'
                + '<span class="p_de_posture">' + self.esc(sub.title) + '</span>'
                + '</p>'
                + '<p class="p_de_content">'
                + '<span class="p_de_label">說明</span>'
                + '<span class="p_de_re">' + self.esc(self.lookupTip(sub.tipId)) + '</span>'
                + '</p>';
        });
        return html;
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
 * 出處：六支範例的 ex01（多項偵測到）。
 * ⚠️ 球位那一類原本是「有問題」，這裡刻意改成「不適用」，
 *    好讓卡片區同時看得到三種樣子（有問題／正常／不適用）。
 * ⚠️ 這份清單是「已經排好序、已經標好狀態」的結果，
 *    ⛔ 不是原始的 issues 陣列 —— 下一輪要換成跑真的 issues 並自己分派排序。
 * 六支範例 JSON 在 page/js/dev-data/putting/（⛔ 不在版控，見 .gitignore）。
 * ===================================================================== */
const PUTT_ISSUES_DEV_DATA = {

    consistency: {
        // ⚠️ 撈幾推是參數，⛔ 不寫死（切桿現在是寫死 10）
        recentCount: 10,
        // ⚠️ 標題講「回答什麼」，⛔ 不是講資料範圍
        title: '推桿穩定度',
        // ⛔ 這句⛔ 不可以講成「準不準」—— 模擬器估的推桿距離不準，
        //    但「彼此散得多開」可信，所以這張圖只能回答「穩不穩」。
        // ⚠️ ⛔ 不要寫回「這 10 推」：撈幾推是參數，句子裡寫死數字會跟參數脫節。
        //    需要帶數字時用 {N}，renderConsistency() 會換成 recentCount。
        note: '越集中越穩定',
    },

    overall: {
        code: 'at_risk',
        reason: '',
        // ⚠️ 「影片品質不足，結果僅供參考」這一行 user 指示先不要顯示（2026-09-09）。
        //    ⛔ 程式路徑與樣式都留著（renderOverall 會在 quality 為空時自動隱藏），
        //    ⛔ 不要把 .putt-overall-quality 或那段程式刪掉 ——
        //    §2.7 定案：這裡是主畫面上唯一保留品質標示的地方。
        //    接真資料時由 Core 說品質不足才填這個欄位。
        quality: '',
    },

    /* ---- 五類的順序與狀態（⚠️ 已經排好序、標好狀態）--------------------
     * 排序：第 1 組 severe/moderate → 第 2 組 其餘 detected → 第 3 組 正常
     *      → 第 4 組 不適用。組內按五類固定順序：
     *      站姿 → 三角形變動 → 球位 → 身體位移 → 身體傾斜。
     * ⛔ 不用類別權重、⛔ 不用「離門檻多遠」跨類別排（§2.5、§4.1）。
     * ⚠️ 這裡沒有第 1 組（ex01 沒有 severe/moderate），
     *    三角形變動與身體位移同在第 2 組，組內順序讓三角形變動在前。
     * ⭐ 下一輪 classifyAndSort() 要產出的就是這個形狀的陣列。
     * state: 'issue'（有問題）／'normal'（正常）／'na'（不適用）
     */
    items: [
        {
            // 樣子 ①：有問題 ＋ 一類兩個子型（⛔ 不拆成兩類）
            key: 'triangle',
            title: '三角形變動',
            short: '三角形',
            state: 'issue',
            subtypes: [
                { title: '送桿段：球桿脫離手臂帶動的方向', tipId: 'triangle.T3.followthrough.mild' },
                { title: '手肘過度彎曲', tipId: 'triangle.T2' },
            ],
            // ⛔ 送桿段兩端是 [碰球, 收桿]，這裡刻意不給 seek，
            //    用來示範「任一端不可信 → 提示不出現，內容照樣看得到」
            seekFrom: null,
            seekTo: null,
        },
        {
            // 樣子 ②：有問題 ＋「▶ 看這一段」
            key: 'body_sway',
            title: '身體位移',
            short: '位移',
            state: 'issue',
            subtypes: [
                { title: '下半身左右移動（主要在下桿）', tipId: 'body_sway.W0.downswing' },
            ],
            seekLabel: '▶ 看這一段',
            seekFrom: 231,   // 頂點
            seekTo: 279,     // 碰球
        },
        // 樣子 ③：正常
        { key: 'stance', title: '站姿', short: '站姿', state: 'normal', text: '正常', note: '' },
        // ⚠️ ex06 實際發生在三角形變動，這裡掛在身體傾斜只是為了同時展示兩種樣子
        { key: 'swing_angle', title: '身體傾斜', short: '傾斜', state: 'normal', text: '正常', note: '（送桿段未取得畫面）' },
        // 樣子 ④：不適用。⛔ 不可以講成「正常」。
        { key: 'ball_position', title: '球位', short: '球位', state: 'na', reason: '這次無法判定：畫面中找不到球' },
    ],

    // 5 項全部不適用時才用這一句（例：ex04 側面拍攝）
    naAllText: '5 個檢查項目本次都未判定',

    /* ---- 文案（暫時寫死）------------------------------------------------
     * ⚠️ 下一輪改讀 putting_issue_seed.json 的 18 條（§6.1 第 6 項）；
     *    建表之後再改由 PuttingIssueTip.java 查資料庫（§6.3 第 15 項）。
     * ⛔ 兩次都只換 lookupTip()，⛔ 渲染那段不要動。
     * ⚠️ 這裡的三條是照範例的 tip_id 湊出來的示意文字，
     *    ⛔ 不是 putting_issue_seed.json 的正式文案。
     */
    tips: {
        'triangle.T3.followthrough.mild':
            '打完球往前送的時候，球桿沒有跟著手臂走，變成手腕自己在動。',
        'triangle.T2':
            '推的過程中手肘比架桿時彎了不少，三角形被破壞掉。',
        'body_sway.W0.downswing':
            '把球桿揮下去打到球的過程中，下半身有明顯的左右位移，會讓推擊的方向不容易固定。',
    },
};
