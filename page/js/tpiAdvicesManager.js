/**
 * @fileoverview 此檔案負責根據 TPI (Titleist Performance Institute) 揮桿分析數據，
 * 動態生成並顯示揮桿特徵建議表格。它包含一個輪播功能，用於在多個特徵之間切換，
 * 並帶有簡單的滑入/滑出動畫效果。
 */

class TpiAdvicesManager {
    /**
     * @param {HTMLElement} tableContainerElement - 用於顯示表格的容器元素。
     * @param {number} [maxDisplayItems=1] - 表格中一次最多顯示的項目數量。 // 最少顯示1
     * @param {object} [timingOptions={}] - 控制動畫時間的選項。
     * @param {number} [timingOptions.animationDuration=500] - 動畫持續時間。 // 延遲時間與 CSS 過渡時間必須一致
     * @param {number} [timingOptions.carouselInterval=5000] - 輪播間隔時間。
     * @param {number} [timingOptions.initialDelay=500] - 初始顯示延遲時間。
     */
    constructor(tableContainerElement, maxDisplayItems = 1, timingOptions = {}) {
        if (!tableContainerElement) {
            console.warn("TpiAdvicesManager: tableContainerElement is undefined or null.");
            return;
        }

        // 將傳入的參數設定為 Class 的屬性
        this.tableContainerElement = tableContainerElement;
        this.maxDisplayItems = maxDisplayItems || 1; // 透過邏輯 OR 運算子，確保 maxDisplayItems 至少為 1
        this.timingOptions = {
            animationDuration: 500,
            carouselInterval: 5000,
            initialDelay: 500,
            ...timingOptions // 使用展開運算符來合併外部傳入的選項，覆蓋預設值
        };

        // 宣告內部狀態變數
        this.displayIndex = 0;
        this.intervalId = null;
        this.currentData = [];

        // 預先取得表格元素，避免重複查詢 DOM
        this.tableElement = this.tableContainerElement.querySelector('table');
        if (!this.tableElement) {
            console.error("TpiAdvicesManager: 找不到 table 元素。請確保容器內有 <table> 標籤。");
        }

        this.currentPhase = 'A'; // 預設為 'A'
        this.currentEffectValue = 6; // 預設為 '正常動作' (6)
        this.golfAdviceResult = '';
    }

    /**
     * 輔助方法：根據 Effect 值判斷教練比對是否有差異。
     * Effect 值 6 代表「正常動作」，小於 6 代表與教練有差異。
     * @param {string|number} effectValue - 當前階段的 aEffect, tEffect, iEffect, 或 fEffect 值。
     * @returns {boolean} - true 表示有差異圖 (Effect < 6)，false 表示正常動作 (Effect = 6)。
     */
    hasCoachComparisonDifference(effectValue) {
        // 將值轉換為數字，並檢查是否小於 6
        return parseInt(effectValue, 10) < 6;
    }

    getPhaseTitle(phase) {
        switch (phase) {
            case 'A':
                return '準備';
            case 'T':
                return '上桿';
            case 'I':
                return '下桿';
            case 'F':
                return '收桿';
            default:
                return '';
        }
    }

    /**
     * 根據揮桿階段和數據更新並渲染表格。
     * @param {Array<number>} combinedTpiSwingTable - 包含 0 或 1 的陣列。
     * @param {string} phase - 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @param {object} tpiMapping - 映射物件。
     * @param {Array<object>} tpiAdvices - 建議數據源。
     */
    // updateTable(combinedTpiSwingTable, phase, tpiMapping, tpiAdvices) {
    //     if (!this.tableElement) return;

    //     // 清除舊的計時器
    //     clearInterval(this.intervalId);
    //     this.displayIndex = 0;

    //     // 篩選數據
    //     if (!combinedTpiSwingTable || combinedTpiSwingTable.length === 0) {
    //         console.warn("combinedTpiSwingTable is null or empty.");
    //         this.renderTable([]); // 傳入空陣列以顯示沒有數據的訊息
    //         return;
    //     }

    //     const phaseIndices = tpiMapping[phase] || [];
    //     const filteredIndices = combinedTpiSwingTable
    //         .map((value, index) => (value === 1 && phaseIndices.includes(index)) ? index : -1)
    //         .filter(index => index !== -1);
    //     this.currentData = filteredIndices.map(index => tpiAdvices[index]);

    //     // 啟動初始渲染
    //     setTimeout(() => {
    //         this.renderTable(this.currentData);
    //         this.tableElement.classList.remove('slide-out');
    //         this.tableElement.classList.add('slide-in');

    //         setTimeout(() => {
    //             this.tableElement.classList.remove('slide-in');
    //         }, this.timingOptions.animationDuration); // 延遲時間與 CSS 過渡時間必須一致
    //     }, this.timingOptions.initialDelay); // 延遲 500ms 觸發新內容渲染

    //     // 啟動輪播
    //     if (this.currentData.length > this.maxDisplayItems) {
    //         this.intervalId = setInterval(() => {
    //             this.tableElement.classList.add('slide-out');

    //             setTimeout(() => {
    //                 this.displayIndex += this.maxDisplayItems;
    //                 if (this.displayIndex >= this.currentData.length) {
    //                     this.displayIndex = 0;
    //                 }
    //                 this.renderTable(this.currentData);
    //                 this.tableElement.classList.remove('slide-out');
    //                 this.tableElement.classList.add('slide-in');

    //                 setTimeout(() => {
    //                     this.tableElement.classList.remove('slide-in');
    //                 }, this.timingOptions.animationDuration);
    //             }, this.timingOptions.animationDuration);
    //         }, this.timingOptions.carouselInterval);
    //     }
    // }

    /**
     * 根據後端提供的篩選後 JSON 數據、當前階段和 LLM 綜合建議更新並渲染表格。
     *
     * @param {string} phase - 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @param {string} allFilteredAdvicesJson - 後端 (Java) 輸出的 JSON 字串，包含所有階段篩選後的 TPI 建議。
     * @param {string|number} effectValue - 當前階段的 aEffect, tEffect, iEffect, 或 fEffect 值。
     * @param {string} golfAdviceResultJson - 後端 (JSP) 輸出的 LLM 綜合建議 JSON 字串。
     */
    updateTable(phase, allFilteredAdvicesJson, effectValue, golfAdviceResultJson="") {
        if (!this.tableElement) return;

        // 1. 清除舊的計時器並重設狀態
        clearInterval(this.intervalId);
        this.displayIndex = 0;
        this.currentPhase = phase;
        this.currentEffectValue = effectValue;

        let allAdvices;
        let golfAdvice = null; // 初始化 LLM 建議數據

        // 處理 TPI Advices JSON
        try {
            allAdvices = JSON.parse(allFilteredAdvicesJson);
        } catch (error) {
            console.error("TpiAdvicesManager: 無法解析 allFilteredAdvicesJson", error);
            allAdvices = {}; // 設置為空物件以避免後續錯誤
        }

        // 🚨 關鍵修改點 1: 呼叫新的解析函式
        if (typeof golfAdviceResultJson === 'string' && golfAdviceResultJson.trim().length > 0) {
            golfAdvice = this._parseGolfAdviceResult(golfAdviceResultJson);
        }

        // --- 特殊處理：階段 'F' 顯示 LLM 綜合建議 ---
        // 🚨 關鍵修改點 2: golfAdvice 現在可能是 null 或一個包含 cause/suggestion 的錯誤物件
        if (phase === 'F' && golfAdvice) {
            console.log(`TpiAdvicesManager: 階段 ${phase} 顯示 LLM 綜合建議或錯誤。`);
            this.currentData = [golfAdvice]; // 使用 LLM 建議數據或錯誤物件
        } else {
            // 處理 A, T, I 階段的 TPI 建議
            this.currentData = allAdvices[phase] || [];
        }

        // 2. 判斷是否有 TPI 或 LLM 數據需要渲染
        const hasData = this.currentData.length > 0;

        // 3. 啟動初始渲染與動畫
        this.tableElement.classList.add('slide-out');

        setTimeout(() => {
            this.renderTable(this.currentData);
            this.tableElement.classList.remove('slide-out');
            this.tableElement.classList.add('slide-in');

            setTimeout(() => {
                this.tableElement.classList.remove('slide-in');
            }, this.timingOptions.animationDuration);
        }, this.timingOptions.initialDelay);

        // 4. 啟動輪播 (只有在有 TPI 數據且數量大於 maxDisplayItems 時才輪播)
        if (phase !== 'F' && hasData && this.currentData.length > this.maxDisplayItems) {
            this.intervalId = setInterval(() => {
                this.tableElement.classList.add('slide-out');

                setTimeout(() => {
                    this.displayIndex += this.maxDisplayItems;
                    if (this.displayIndex >= this.currentData.length) {
                        this.displayIndex = 0;
                    }
                    this.renderTable(this.currentData);
                    this.tableElement.classList.remove('slide-out');
                    this.tableElement.classList.add('slide-in');

                    setTimeout(() => {
                        this.tableElement.classList.remove('slide-in');
                    }, this.timingOptions.animationDuration);
                }, this.timingOptions.animationDuration);
            }, this.timingOptions.carouselInterval);
        }
    }

    /**
     * 渲染表格內容。
     * @param {Array<object>} data - 要顯示的揮桿特徵數據 (TPI Advices) 或單一 LLM Advice 物件陣列。
     */
    renderTable(data) {
        this.tableElement.innerHTML = '';

        if (data.length > 0) {
            const item = data[this.displayIndex];

            if (this.currentPhase === 'F' && data.length === 1 && (item.cause || item.suggestion)) {
                 // --- 渲染 LLM 綜合建議 (F 階段) ---
                 this.tableElement.innerHTML =
                     '<div class="p_de_title_container">' +
                     '<div class="p_de_title">' + "綜合建議" + '</div>' +
                     '</div>' +
                     '<p class="p_de_content">' +
                     '<span class="p_de_label">擊球成因</span>' +
                     // item.cause 現在可能是 Worker 提供的成因，或錯誤訊息 'API 呼叫失敗'
                     '<span class="p_de_posture">' + (item.cause || '未提供成因') + '</span>' +
                     '</p>' +
                     '<p class="p_de_content">' +
                     '<span class="p_de_label">擊球建議</span>' +
                     // item.suggestion 現在可能是 Worker 提供的建議，或錯誤訊息
                     '<span class="p_de_re">' + (item.suggestion || '未提供建議') + '</span>' +
                     '</p>';
            } else {
                 // --- 渲染 TPI 建議 (A, T, I 階段) ---
                 const dataToShow = data.slice(this.displayIndex, this.displayIndex + this.maxDisplayItems);

                 dataToShow.forEach(tpiItem => {
                     const row = this.tableElement.insertRow();
                     const cell = row.insertCell();

                     if (this.maxDisplayItems === 1) {
                         cell.colSpan = 2;
                     }

                     cell.innerHTML =
                         '<div class="p_de_title_container">' +
                         '<div class="p_de_title">' + tpiItem.title + '</div>' +
                         '</div>' +
                         '<p class="p_de_content">' +
                         '<span class="p_de_label">揮桿特徵</span>' +
                         '<span class="p_de_posture">' + tpiItem.posture + '</span>' +
                         '</p>' +
                         '<p class="p_de_content">' +
                         '<span class="p_de_label">動作建議</span>' +
                         '<span class="p_de_re">' + tpiItem.suggestion + '</span>' +
                         '</p>';
                 });
            }
        } else {
            // **沒有 TPI 或 LLM 錯誤時/未啟用時的訊息邏輯更新**
            let message = '';
            const phaseTitle = this.getPhaseTitle(this.currentPhase); // 取得當前階段名稱 ('準備', '上桿', '下桿', '收桿')

            // 🚨 關鍵修改點：將 F 階段的處理邏輯與 A/T/I 階段合併，共同判斷 TPI/比對預設訊息
            if (this.hasCoachComparisonDifference(this.currentEffectValue)) {
                 // 有差異圖 (Effect < 6)
                 message = `您的${phaseTitle}動作大致良好，但與教練比對略有不同。 建議可參考左側圖像標示的紅色部位。`;
            } else {
                 // 無差異圖 (Effect = 6)
                 message = `您的${phaseTitle}動作與TPI標準吻合。動作協調性與穩定性都表現出色`;
            }

            // 渲染無數據或預設訊息
            this.tableElement.innerHTML =
                '<div class="p_de_title_container">' +
                '<div class="p_de_title">' + phaseTitle + '</div>' +
                '</div>' +
                '<p class="p_de_content">' +
                '<p class="p_de_content">' +
                '<span class="p_de_label">動作建議</span>' +
                '<span class="p_de_re">' + message + '</span>' +
                '</p>';
        }
    }

    /**
     * 輔助函式：解析 LLM 建議的 JSON 響應。
     * ...
     * @returns {object | null} - 成功時返回包含 cause/suggestion 的建議物件，失敗或無數據時返回 null。
     */
    _parseGolfAdviceResult(golfAdviceResultJson) {
        if (typeof golfAdviceResultJson !== 'string' || golfAdviceResultJson.trim().length === 0) {
            return null;
        }

        try {
            const parsedObj = JSON.parse(golfAdviceResultJson);

            // 1. 處理 Java Client 錯誤響應結構: { "success": false, "result": "..." }
            if (parsedObj.success === false) {
                console.error("LLM API Call Failed (from Java Client):", parsedObj.result);
                // 🚨 修改：API 呼叫失敗，直接返回 null，讓頁面顯示 TPI 預設訊息
                return null;
            }

            // 2. 處理 Worker 成功響應結構: { "task_id": "...", "status": "completed", "data": {...} }
            if (parsedObj.status === 'completed' && parsedObj.data && typeof parsedObj.data === 'object') {
                return parsedObj.data; // 返回純淨的建議數據物件 { cause: ..., suggestion: ... }
            }

            // 3. 處理 Worker 返回但 status 非 completed 的情況
            if (parsedObj.status && parsedObj.status !== 'completed') {
                console.warn("LLM Worker Status Not Completed:", parsedObj.status);
                // 🚨 修改：Worker 處理失敗，直接返回 null，讓頁面顯示 TPI 預設訊息
                return null;
            }

            // 4. 未知結構/無 data 欄位
            console.warn("LLM Result JSON format unknown or missing data:", parsedObj);
            return null;

        } catch (error) {
            // 處理 JSON.parse 失敗的情況
            console.error("TpiAdvicesManager: 最終 LLM JSON 解析失敗:", error, "原始響應:", golfAdviceResultJson);
            // 🚨 修改：解析失敗，直接返回 null
            return null;
        }
    }
}

