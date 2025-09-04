/*

*/

class CmpChartManager {
    constructor(chartContainerElement) {
        if (!chartContainerElement) {
            console.warn("CmpChartManager: chartContainerElement is undefined or null.");
            return;
        }

        // 將傳入的參數設定為 Class 的屬性
        this.chartContainerElement = chartContainerElement;

        // 宣告內部狀態變數

        // 預先取得表格元素，避免重複查詢 DOM
        this.imageElements = this.chartContainerElement.querySelectorAll('img');
        if (!this.imageElements) {
            console.error("CmpChartManager: 找不到 image 元素。請確保容器內有 <image> 標籤。");
        }
    }

    /**
     * 根據給定的揮桿階段顯示對應的圖片。
     * @param {string} phase - 揮桿階段 ('A', 'T', 'I', 'F')。
     */
    updateChartImage(phase) {
        if (!this.imageElements) return;

        this.imageElements.forEach(img => {
            if (img.dataset.phase === phase) {
                // 如果圖片的 data-phase 與當前階段相符，則顯示它
                img.style.display = 'block';
            } else {
                // 否則隱藏其他圖片
                img.style.display = 'none';
            }
        });
    }
}