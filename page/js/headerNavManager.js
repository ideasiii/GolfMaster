/**
 * @fileoverview 此檔案負責管理頁首的導覽按鈕。
 * 它會動態判斷當前頁面，並禁用對應的導覽按鈕。
 * 對於其他導覽按鈕，它會添加點擊事件監聽器，以便在切換頁面時保留原始的 URL 查詢參數。
 */

/**
 * 當 DOM 內容完全加載後，初始化頁首導覽按鈕的功能。
 *
 * 此事件監聽器會執行以下操作：
 * 1. 查找所有具有 'nav-button' 類別的按鈕。
 * 2. 獲取當前頁面的 URL、檔案名稱和查詢參數。
 * 3. 遍歷所有導覽按鈕：
 *    - 如果按鈕對應的頁面是當前頁面，則禁用該按鈕並添加 'current-page' 樣式。
 *    - 否則，為按鈕添加點擊事件，點擊後會導向目標頁面，並附加原始的 URL 參數。
 *
 * @listens DOMContentLoaded
 * @returns {void}
 */
document.addEventListener('DOMContentLoaded', function() {
    const navButtons = document.querySelectorAll('.nav-button');

    // 獲取當前頁面的 URL 參數和檔案名稱
    const currentUrl = new URL(window.location.href);
    // 取得路徑的最後一部分作為當前檔案名
    const currentPath = currentUrl.pathname.split('/').pop();
    // 獲取所有參數 (例如: ?user=xxx&id=yyy)
    const urlParams = currentUrl.search;

    // 設置按鈕事件監聽和禁用狀態
    navButtons.forEach(button => {
        const targetPage = button.getAttribute('data-page');

        // 檢查目標頁面是否是當前頁面
        if (targetPage === currentPath) {
            // 禁用當前頁面按鈕
            button.disabled = true;
            button.classList.add('current-page'); // 可選：用於額外的樣式標記
        } else {
            // 添加點擊事件
            button.addEventListener('click', function() {
                // 建構新的 URL： [目標頁面] + [原始參數]
                const newUrl = targetPage + urlParams;

                // 執行頁面跳轉
                window.location.href = newUrl;
            });
        }
    });
});
