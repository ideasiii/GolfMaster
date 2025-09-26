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
     * 根據後端提供的篩選後 JSON 數據和當前階段更新並渲染表格。
     *
     * @param {string} phase - 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @param {string} allFilteredAdvicesJson - 後端 (Java) 輸出的 JSON 字串，包含所有階段篩選後的建議。
     * @param {string|number} effectValue - 當前階段的 aEffect, tEffect, iEffect, 或 fEffect 值。
     */
    updateTable(phase, allFilteredAdvicesJson, effectValue) {
        if (!this.tableElement) return;

        // 1. 清除舊的計時器並重設狀態
        clearInterval(this.intervalId);
        this.displayIndex = 0;
        // 儲存當前 phase
        this.currentPhase = phase;
        // 儲存當前 Effect 值
        this.currentEffectValue = effectValue;


        let allAdvices;
        try {
            // 2. 解析 JSON 字串
            allAdvices = JSON.parse(allFilteredAdvicesJson);
        } catch (error) {
            console.error("TpiAdvicesManager: 無法解析 allFilteredAdvicesJson", error);
            this.renderTable([]); // 傳入空陣列，會顯示「沒有明顯錯誤」
            return;
        }

        if (allAdvices === null || allAdvices === undefined) {
            console.log("TpiAdvicesManager: 後端傳回空資料 (null)。");
            this.renderTable([]);
            return;
        }

        // 3. 取得當前階段的篩選後數據
        this.currentData = allAdvices[phase] || [];

        // 檢查是否有 TPI 數據
        if (this.currentData.length === 0) {
            console.log(`TpiAdvicesManager: 階段 ${phase} 沒有篩選後的建議。`);
            // 沒有 TPI 錯誤，但表格的渲染要交由 renderTable 判斷是否有教練比對差異
            this.renderTable([]); 
            return;
        }

        // 4. 有 TPI 錯誤，啟動初始渲染與動畫（與先前邏輯相同）
        this.tableElement.classList.add('slide-out'); 

        setTimeout(() => {
            this.renderTable(this.currentData);
            this.tableElement.classList.remove('slide-out');
            this.tableElement.classList.add('slide-in');

            setTimeout(() => {
                this.tableElement.classList.remove('slide-in');
            }, this.timingOptions.animationDuration);
        }, this.timingOptions.initialDelay);

        // 5. 啟動輪播
        if (this.currentData.length > this.maxDisplayItems) {
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
     * **核心更新：** 在沒有 TPI 錯誤時，根據 `this.currentEffectValue` 顯示不同訊息。
     * @param {Array<object>} data - 要顯示的揮桿特徵數據。
     */
    renderTable(data) {
        this.tableElement.innerHTML = ''; 

        if (data.length > 0) {
            // ... (有 TPI 錯誤時的渲染邏輯保持不變)
            const dataToShow = data.slice(this.displayIndex, this.displayIndex + this.maxDisplayItems);

            dataToShow.forEach(item => {
                const row = this.tableElement.insertRow();
                const cell = row.insertCell();

                // 修正：maxItemsToDisplay 應為 this.maxDisplayItems
                if (this.maxDisplayItems === 1) { 
                    cell.colSpan = 2;
                }

                cell.innerHTML =
                    '<div class="p_de_title_container">' +
                    '<div class="p_de_title">' + item.title + '</div>' +
                    '</div>' +
                    '<p class="p_de_content">' +
                    '<span class="p_de_label">揮桿特徵</span>' +
                    '<span class="p_de_posture">' + item.posture + '</span>' +
                    '</p>' +
                    '<p class="p_de_content">' +
                    '<span class="p_de_label">動作建議</span>' +
                    '<span class="p_de_re">' + item.suggestion + '</span>' +
                    '</p>';
            });
        } else {
            // **沒有 TPI 錯誤時的訊息邏輯更新**
            let message = '';
            if (this.hasCoachComparisonDifference(this.currentEffectValue)) {
                // Effect < 6 (例如 0~5) 表示與教練比對有差異
                message = '您的揮桿動作無明顯TPI特徵，但與標準比對結果顯示有需要注意的部位，請參考影像圖。';
            } else {
                // Effect = 6 表示與教練動作無異
                message = '恭喜！您的揮桿動作無明顯TPI特徵，與標準動作比較也無明顯差異。';
            }
            // this.tableElement.innerHTML = `<tr><td class="p_de_re">${message}</td></tr>`;

            this.tableElement.innerHTML =
                '<div class="p_de_title_container">' +
                '<div class="p_de_title">' + this.getPhaseTitle(this.currentPhase) + '</div>' +
                '</div>' +
                '<p class="p_de_content">' +
                '<p class="p_de_content">' +
                '<span class="p_de_label">動作建議</span>' +
                '<span class="p_de_re">' + message + '</span>' +
                '</p>';
        }
    }
}

