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
    }

    /**
     * 根據揮桿階段和數據更新並渲染表格。
     * @param {Array<number>} combinedTpiSwingTable - 包含 0 或 1 的陣列。
     * @param {string} phase - 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @param {object} tpiMapping - 映射物件。
     * @param {Array<object>} tpiAdvices - 建議數據源。
     */
    updateTable(combinedTpiSwingTable, phase, tpiMapping, tpiAdvices) {
        if (!this.tableElement) return;

        // 清除舊的計時器
        clearInterval(this.intervalId);
        this.displayIndex = 0;

        // 篩選數據
        if (!combinedTpiSwingTable || combinedTpiSwingTable.length === 0) {
            console.warn("combinedTpiSwingTable is null or empty.");
            this.renderTable([]); // 傳入空陣列以顯示沒有數據的訊息
            return;
        }

        const phaseIndices = tpiMapping[phase] || [];
        const filteredIndices = combinedTpiSwingTable
            .map((value, index) => (value === 1 && phaseIndices.includes(index)) ? index : -1)
            .filter(index => index !== -1);
        this.currentData = filteredIndices.map(index => tpiAdvices[index]);

        // 啟動初始渲染
        setTimeout(() => {
            this.renderTable(this.currentData);
            this.tableElement.classList.remove('slide-out');
            this.tableElement.classList.add('slide-in');

            setTimeout(() => {
                this.tableElement.classList.remove('slide-in');
            }, this.timingOptions.animationDuration); // 延遲時間與 CSS 過渡時間必須一致
        }, this.timingOptions.initialDelay); // 延遲 500ms 觸發新內容渲染

        // 啟動輪播
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
     * @param {Array<object>} data - 要顯示的揮桿特徵數據。
     */
    renderTable(data) {
        this.tableElement.innerHTML = ''; // 清空舊內容

        if (data.length > 0) {
            const dataToShow = data.slice(this.displayIndex, this.displayIndex + this.maxDisplayItems);

            dataToShow.forEach(item => {
                const row = this.tableElement.insertRow();
                const cell = row.insertCell();

                if (this.maxItemsToDisplay === 1) {
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
                    '<span class="p_de_label">原因</span>' +
                    '<span class="p_de_re">' + item.reason + '</span>' +
                    '</p>';
            });
        } else {
            this.tableElement.innerHTML = '<tr><td>沒有明顯錯誤。</td></tr>';
        }
    }
}
