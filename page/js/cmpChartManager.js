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

        // 預先取得圖片元素，使用 ID 讓存取更明確
        this.imageElements = {
            'A': document.getElementById('img-A'),
            'T': document.getElementById('img-T'),
            'I': document.getElementById('img-I'),
            'F': document.getElementById('img-F')
        };

        // 宣告內部狀態變數

        // 預先取得表格元素，避免重複查詢 DOM
        // this.imageElements = this.chartContainerElement.querySelectorAll('img');
        // if (!this.imageElements) {
        //     console.error("CmpChartManager: 找不到 image 元素。請確保容器內有 <image> 標籤。");
        // }

        // 取得表格元素 (用於顯示 TPI 建議的表格)
        if (!this.imageElements['A']) {
             console.error("CmpChartManager: 找不到必要的 DOM 元素 (img-A)。");
        }

        // 初始化時儲存原始圖片路徑
        Object.values(this.imageElements).forEach(img => {
            if (img && !img.dataset.originalSrc) {
                img.dataset.originalSrc = img.src;
            }
        });
    }

    /**
     * 根據給定的揮桿階段顯示對應的圖片。
     * @param {string} phase - 揮桿階段 ('A', 'T', 'I', 'F')。
     */
    updateChartImage(phase) {
        // 使用 Object.values() 來遍歷圖片元素
        Object.values(this.imageElements).forEach(img => {
            if (img && img.dataset.phase === phase) {
                // 如果圖片的 data-phase 與當前階段相符，則顯示它
                img.style.display = 'block';
            } else if (img) {
                // 否則隱藏其他圖片
                img.style.display = 'none';
            }
        });
    }

    /**
     * 根據揮桿階段和 TPI 數據更新圖片和渲染建議表格。
     * @param {string} phase - 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @param {string} tpiAdvicesJson - TPI 建議數據的 JSON 字串。
     */
    updateChartTpiImage(phase, tpiAdvicesJson) {
        if (!this.imageElements[phase] || !tpiAdvicesJson) return;

        // 1. 解析 TPI 建議數據
        let tpiData;
        try {
            // 注意：您提供的字串外面是雙引號，裡面也像 JSON，因此要用 JSON.parse 兩次
            // 實際應用中，確保 tpiAdvices 是標準 JSON 字串即可
            tpiData = JSON.parse(tpiAdvicesJson);
        } catch (e) {
            console.error("解析 TPI 建議 JSON 失敗:", e);
            return;
        }

        // 取得當前階段的 TPI 錯誤列表
        const currentPhaseErrors = tpiData[phase] || [];
        const imageElement = this.imageElements[phase];

        // 取得圖片元素的原始 src (從 HTML 模板中取得，用於無 TPI 錯誤時顯示)
        // 由於原始 src 是在 JSP/EJS 模板中設定的，我們需要一個方式記住它。
        // 在這裡，我們假設原始的 HTML 結構已經正確設定了初始 src。
        // 我們將原始 src 儲存在 data 屬性中。
        if (!imageElement.dataset.originalSrc) {
             imageElement.dataset.originalSrc = imageElement.src;
        }

        // 2. 決定要顯示的圖片路徑
        if (currentPhaseErrors.length > 0) {
            // 有 TPI 錯誤，嘗試載入 TPI 圖片
            const firstError = currentPhaseErrors[0];
            const tpiName = firstError.name; // 例如: "s_posture"
            const tpiImagePath = `../../page/img/tpi/${tpiName}.png`;

            // **實作圖片載入失敗時回退的核心邏輯**
            const testImg = new Image();
            testImg.onload = () => {
                // 圖片載入成功，設定給實際的 DOM 元素
                imageElement.src = tpiImagePath;
            };
            testImg.onerror = () => {
                // 圖片載入失敗，切換回原始圖片
                console.warn(`TPI 圖片未找到: ${tpiImagePath}。切換回原始圖片。`);
                imageElement.src = imageElement.dataset.originalSrc;
            };

            // 啟動載入程序
            testImg.src = tpiImagePath;

        } else {
            // 沒有 TPI 錯誤，直接顯示原始的教練比對圖
            imageElement.src = imageElement.dataset.originalSrc;
        }
    }
}

