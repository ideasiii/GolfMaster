/**
 * @fileoverview 此檔案包含在影片上繪製揮桿分析輔助線（如揮桿平面、身體線條）的相關函式。
 * 主要功能是處理影片與 Canvas 尺寸不匹配時的座標轉換，確保輔助線能正確疊加在影片畫面上。
 */

// --- 常數定義 ---
const bodyLineColor = 'rgba(255, 153, 255, 1)';
const bboxColor = 'rgba(255, 255, 255, 1)';
const swingPlaneColor = 'rgba(255, 153, 51, 1)';


/**
 * 設定影片相關的事件監聽器。
 * 當影片元數據加載完成後，會調整 Canvas 大小並跳轉到指定的初始影格。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {object} swingPlaneData - 包含揮桿平面數據的物件。
 * @param {number} initialFrame - 影片的初始影格。
 * @param {number} frameRate - 影片的影格率。
 * @param {boolean} isSideView - 是否為側視圖。
 */
function setupVideoEvents(
    videoElement,
    canvasElement,
    swingPlaneData,
    initialFrame,
    frameRate,
    isSideView,
) {
    videoElement.addEventListener('loadedmetadata', function () {
        //videoSlider.max = videoElement.duration;
        resizeCanvas(videoElement, canvasElement, swingPlaneData, isSideView);
        videoElement.currentTime = initialFrame / frameRate;
        videoElement.pause();
        // clearAndDrawOverlayForVideo(videoElement, canvasElement, context, swingPlaneData, isSideView);
    });
}


/**
 * 根據影片實際顯示大小調整畫布尺寸，並重新繪製疊加層。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {object} swingPlaneData - 包含揮桿平面數據的物件。
 * @param {boolean} [isSideView=false] - 是否為側視圖。
 */
function resizeCanvas(videoElement, canvasElement, swingPlaneData, isSideView = false) {
    const videoDisplayWidth = videoElement.clientWidth;
    const videoDisplayHeight = videoElement.clientHeight;

    canvasElement.style.width = `${videoDisplayWidth}px`;
    canvasElement.style.height = `${videoDisplayHeight}px`;
    canvasElement.width = videoDisplayWidth;
    canvasElement.height = videoDisplayHeight;
    // console.log("Canvas resized to match video dimensions: " + canvasElement.width + "x" + canvasElement.height);

    // 更新畫布大小後，重新繪製輔助線
    const context = canvasElement.getContext('2d');
    // const isSideView = (videoElement.id === "myvideo1");
    clearAndDrawOverlayForVideo(
        videoElement,
        canvasElement,
        context,
        // isSideView ? sideSwingPlaneData : frontSwingPlaneData,
        swingPlaneData,
        isSideView,
    );
}


/**
 * 計算影片在 Canvas 中實際繪製的尺寸和偏移量，以處理長寬比不匹配的問題 (letterboxing)。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @returns {{drawWidth: number, drawHeight: number, offsetX: number, offsetY: number}|null} 包含繪製尺寸和偏移量的物件，如果無法計算則返回 null。
 */
function getVideoDrawParams(videoElement, canvasElement) {
    const canvasWidth = canvasElement.width;
    const canvasHeight = canvasElement.height;

    const videoWidth = videoElement.videoWidth;
    const videoHeight = videoElement.videoHeight;

    if (videoWidth === 0 || videoHeight === 0 || canvasWidth === 0 || canvasHeight === 0) {
        console.warn("Video or canvas dimensions are zero.")
        return null;
    }

    const videoAspectRatio = videoWidth / videoHeight;
    const canvasAspectRatio = canvasWidth / canvasHeight;

    let drawWidth, drawHeight, offsetX, offsetY;

    if (videoAspectRatio > canvasAspectRatio) {
        // 影片較寬，左右兩側可能會留白
        drawWidth = canvasWidth;
        drawHeight = canvasWidth / videoAspectRatio;
        offsetX = 0;
        offsetY = (canvasHeight - drawHeight) / 2;
    } else {
        // 影片較高，上下兩側可能會留白
        drawHeight = canvasHeight;
        drawWidth = canvasHeight * videoAspectRatio;
        offsetX = (canvasWidth - drawWidth) / 2;
        offsetY = 0;
    }

    return { drawWidth, drawHeight, offsetX, offsetY };
}


/**
 * 在影片上繪製一個邊界框 (Bounding Box)。
 * @param {Array<number>} bbox - 包含 [x_min, y_min, x_max, y_max] 的邊界框陣列，座標為相對於影片的比例。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {CanvasRenderingContext2D} ctx - Canvas 的 2D 繪圖環境。
 * @param {string} [color='white'] - 邊界框顏色。
 */
function drawBoundingBoxForVideo(
    bbox,
    videoElement,
    canvasElement,
    ctx,
    color = 'white',
) {
    const params = getVideoDrawParams(videoElement, canvasElement);
    if (!params) return;
    const { drawWidth, drawHeight, offsetX, offsetY } = params;

    // 計算標定框在影片實際繪製區域的起始點座標、寬度和高度
    const startX = offsetX + bbox[0] * drawWidth;
    const startY = offsetY + bbox[1] * drawHeight;
    const width = (bbox[2] - bbox[0]) * drawWidth;
    const height = (bbox[3] - bbox[1]) * drawHeight;

    // 設定繪製樣式
    ctx.strokeStyle = color;
    ctx.lineWidth = 4;

    // 繪製矩形
    ctx.strokeRect(startX, startY, width, height);
}


/**
 * 在影片上繪製一條線。
 * @param {object} line - 包含 pt1 和 pt2 兩個點的線段物件，座標為相對於影片的比例。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {CanvasRenderingContext2D} ctx - Canvas 的 2D 繪圖環境。
 * @param {string} [color='orange'] - 線條顏色。
 */
function drawLineForVideo(
    line,
    videoElement,
    canvasElement,
    ctx,
    color = 'orange',
) {
    const params = getVideoDrawParams(videoElement, canvasElement);
    if (!params) return;
    const { drawWidth, drawHeight, offsetX, offsetY } = params;

    // 計算線段端點在影片實際繪製區域的座標
    const startX = offsetX + line.pt1[0] * drawWidth;
    const startY = offsetY + line.pt1[1] * drawHeight;
    const endX = offsetX + line.pt2[0] * drawWidth;
    const endY = offsetY + line.pt2[1] * drawHeight;

    // 繪製線段
    ctx.beginPath();
    ctx.moveTo(startX, startY);
    ctx.lineTo(endX, endY);
    ctx.strokeStyle = color;
    ctx.lineWidth = 4;
    ctx.stroke();
}


/**
 * 在影片上繪製頭部標記（正面為橢圓，側面為十字線）。
 * @param {object} head - 包含頭部中心點 (pt) 和半徑 (h_length, v_length) 的物件，座標為相對於影片的比例。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {CanvasRenderingContext2D} ctx - Canvas 的 2D 繪圖環境。
 * @param {string} [color='blue'] - 標記顏色。
 * @param {boolean} [isSideView=false] - 是否為側視圖。
 */
function drawHeadForVideo(
    head,
    videoElement,
    canvasElement,
    ctx,
    color = 'blue',
    isSideView = false,
) {
    const params = getVideoDrawParams(videoElement, canvasElement);
    if (!params) return;
    const { drawWidth, drawHeight, offsetX, offsetY } = params;

    // 計算頭部中心點在影片實際繪製區域的座標
    const ptX = offsetX + head.pt[0] * drawWidth;
    const ptY = offsetY + head.pt[1] * drawHeight;

    if (isSideView) {
        // 側視圖：繪製表示方向的線條組
        const lineLength = head.h_length * drawWidth; // 使用頭部的水平長度作為線段長度

        // 繪製水平線
        ctx.beginPath();
        ctx.moveTo(ptX, ptY);
        ctx.lineTo(ptX - lineLength, ptY);
        ctx.strokeStyle = color;
        ctx.lineWidth = 4;
        ctx.stroke();

        // 繪製垂直線
        ctx.beginPath();
        ctx.moveTo(ptX, ptY);
        ctx.lineTo(ptX, ptY + lineLength);
        ctx.strokeStyle = color;
        ctx.lineWidth = 4;
        ctx.stroke();
    } else {
        // 正面圖，繪製橢圓
        const h_radius = head.h_length * drawWidth;
        const v_radius = head.v_length * drawHeight;

        ctx.beginPath();
        ctx.ellipse(ptX, ptY, h_radius, v_radius, 0, 0, 2 * Math.PI);
        ctx.strokeStyle = color;
        ctx.lineWidth = 4;
        ctx.stroke();
    }
}


/**
 * 清除畫布並根據當前影片畫面重新繪製所有輔助線。
 * @param {HTMLVideoElement} videoElement - 影片元素。
 * @param {HTMLCanvasElement} canvasElement - 畫布元素。
 * @param {CanvasRenderingContext2D} context - Canvas 的 2D 繪圖環境。
_ * @param {object} swingPlaneData - 包含揮桿平面數據的物件。
 * @param {boolean} [isSideView=true] - 是否為側視圖。
 */
function clearAndDrawOverlayForVideo(
    videoElement,
    canvasElement,
    context,
    swingPlaneData,
    isSideView = true,
) {
    context.clearRect(0, 0, canvasElement.width, canvasElement.height); // 清除畫布

    if (!swingPlaneData || !swingPlaneData.data) return;

    if (swingPlaneData.data.bbox) {
        drawBoundingBoxForVideo(swingPlaneData.data.bbox, videoElement, canvasElement, context, bboxColor);
    }
    if (swingPlaneData.data.club) {
        drawLineForVideo(swingPlaneData.data.club, videoElement, canvasElement, context, swingPlaneColor);
    }
    if (swingPlaneData.data.shoulder) {
        drawLineForVideo(swingPlaneData.data.shoulder, videoElement, canvasElement, context, swingPlaneColor);
    }
    if (swingPlaneData.data.left_leg) {
        drawLineForVideo(swingPlaneData.data.left_leg, videoElement, canvasElement, context, bodyLineColor);
    }
    if (swingPlaneData.data.right_leg) {
        drawLineForVideo(swingPlaneData.data.right_leg, videoElement, canvasElement, context, bodyLineColor);
    }
    if (swingPlaneData.data.head) {
        drawHeadForVideo(swingPlaneData.data.head, videoElement, canvasElement, context, bodyLineColor, isSideView);
    }
}
