<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page import="com.golfmaster.moduel.DeviceData"%>
<%@ page import="com.golfmaster.moduel.PSystem"%>
<%@ page import="com.golfmaster.moduel.PSystemJP"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>
<%!PSystem pSystem = new PSystem();%>
<%!PSystemJP pSystemJP = new PSystemJP();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");
Long exID = result.getLong("id");

Object temp[] = shotVideo.processAnalyz(shot_data_id);
int[] sideFrames = (int[]) temp[0];
int[] frontFrames = (int[]) temp[1];
String frontVideoName = (String) temp[2];
String sideVideoName = (String) temp[3];
int aEffect = (int) temp[4];
int tEffect = (int) temp[5];
int iEffect = (int) temp[6];
int fEffect = (int) temp[7];
String sideSwingPlane = (String) temp[8]; // 側面 SwingPlane 資料
String frontSwingPlane = (String) temp[9]; // 正面 SwingPlane 資料

float[][] shotResult = shotData.processPlayerReq(shot_data_id);

String psystem = "";
String trajectory = "";
String cause = "";
String suggestion = "";
String img = "";
String talkhead = "";

float backSwingTime = 0;
float downSwingTime = 0;
float tempo = 0;
float BallSpeed;
float BackSpin;
float SideSpin;
float LaunchAngle;
float Angle;

if (result != null && result.getString("expert_p_system") != null) {
	psystem = result.getString("expert_p_system");
}
if (result != null && result.getString("expert_trajectory") != null) {
	trajectory = result.getString("expert_trajectory");
}
if (result != null && result.getString("expert_cause") != null) {
	cause = result.getString("expert_cause");
}
if (result != null && result.getString("expert_suggestion") != null) {
	suggestion = result.getString("expert_suggestion");
}
//if(result != null && result.getString("video_url") != null){
//	talkhead = result.getString("video_url");
//}




%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="../../page/css/GM.css" rel="stylesheet" type="text/css">
<title>Expert</title>
<!-- <script src="https://cdn.jsdelivr.net/npm/chart.js"></script> -->
<script
	src="
https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.5.1/chart.js">
</script>
<style>
</style>

</head>
<body>
	<div class="c_m">
		<div class="header">
			<img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
			<!-- <Input Type="Button" Value="重新整理" onClick="window.location.reload();">  -->
		</div>
		<div class="row2 ">
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->

						<div class="row2" style="background-color: #000000">
							<div style="position: relative;" class="image_v">
								<video id="myvideo" controls muted>
									<source src="../../video/analyzVideo_front/<%=frontVideoName%>"
										type="video/mp4" alt="Image 1" />
									<!--<source src="../../page/video/<%=frontVideoName%>"
										type="video/mp4" alt="Image 1" />-->
								</video>
								<canvas id="overlayCanvas"></canvas>
							</div>
							<div style="position: relative;" class="image_v">
								<video id="myvideo1" controls muted>
									<source src="../../video/analyzVideo_side/<%=sideVideoName%>"
										type="video/mp4" alt="Image 1" />
									<!--<source src="../../page/video/<%=sideVideoName%>" type="video/mp4"
										alt="Image 1" />-->
								</video>
								<canvas id="overlayCanvas1"></canvas>
							</div>
						</div>
						<!-- <input type="range" id="videoSlider" min="0" value="0" step="0.01"
							style="width: 100%;">  -->
					</div>
				</div>
			</div>
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>能力提升分析</h1>-->
						<div class="aanalysisSection">
							<p class="strikeeff" id="ballscore"></p>
								<div class="row2" style="background-color: #000000">
									<div style="position: relative;" class="image_v">
										<div class="rida_p">
											<%
											if (result.getBoolean("result")) {
												if (trajectory.equals(pSystem.DRAW) || trajectory.equals(pSystem.STRAIGHT) || trajectory.equals(pSystem.FADE)
													|| trajectory.equals(pSystemJP.DRAW) || trajectory.equals(pSystemJP.STRAIGHT)
													|| trajectory.equals(pSystemJP.FADE)) {
															out.print("<img src='../../page/gif/" + "Straight" + ".gif' class='analysis-gif' />");
												} else if (trajectory.equals(pSystem.PUSH_SLICE) || trajectory.equals(pSystemJP.PUSH_SLICE)) {
															out.print("<img src='../../page/gif/" + "Pushs" + ".gif' class='analysis-gif' />");
												} else if (trajectory.equals(pSystem.PULL_HOOK) || trajectory.equals(pSystemJP.PULL_HOOK)) {
															out.print("<img src='../../page/gif/" + "Pullh" + ".gif' class='analysis-gif' />");
												} else if (trajectory.equals(pSystem.PULL) || trajectory.equals(pSystemJP.PULL)
													|| trajectory.equals(pSystem.PULL_SLICE) || trajectory.equals(pSystemJP.PULL_SLICE)) {
															out.print("<img src='../../page/gif/" + "Pull" + ".gif' class='analysis-gif' />");
												} else if (trajectory.equals(pSystem.PUSH) || trajectory.equals(pSystemJP.PUSH)
													|| trajectory.equals(pSystem.PUSH_HOOK) || trajectory.equals(pSystemJP.PUSH_HOOK)) {
															out.print("<img src='../../page/gif/" + "Push" + ".gif' class='analysis-gif' />");
												}
											} else {
														out.print("");
												}
											%>	
										</div>
									</div>
								<div style="position: relative;" class="image_v">
									<div style="margin-top: 20px;">
										<canvas id="radarChart"></canvas>
									</div>
								</div>
							</div>
							<!-- 
							<div class="aanalysis">
								<p class="strikeeff" id="smachfactDisplay"></p>
								<p id="ballSpeedDisplay"></p>
								<p id="clubSpeedDisplay"></p>
								<p id="distanceDisplay"></p>
								<p id="launchAngleDisplay"></p>
								<p id="backSpinDisplay"></p>
							</div>
							 -->
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="row2 stretch">
			<div class="column2">
				<div class="custom-div2">
					<div class="steps">
						<div class="step">
							<span class="step_title">Swing</span><br> <span
								class="step_4">4 Steps</span>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=frontFrames[0]%>,<%=sideFrames[0]%>)">A</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=frontFrames[1]%>,<%=sideFrames[1]%>)">T</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=frontFrames[2]%>,<%=sideFrames[2]%>)">I</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=frontFrames[3]%>,<%=sideFrames[3]%>)">F</button>
						</div>
						<div class="step">
							<div id="player-container">
								<div id="play-pause" class="play">Play</div>
							</div>
						</div>
					</div>
				</div>
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->
						<div class="psystemSection">
							<div class="box">
								<img src="../../page/img/A/A<%=aEffect%>.png">
							</div>
							<div class="box">
								<img src="../../page/img/T/T<%=tEffect%>.png">
							</div>
							<div class="box">
								<img src="../../page/img/I/I<%=iEffect%>.png">
							</div>
							<div class="box">
								<img src="../../page/img/F/F<%=fEffect%>.png">
							</div>
						</div>

					</div>
				</div>
			</div>
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>能力提升分析</h1>-->
						<div class="f_content">
							<div class="column2">
								<div class="suggestion">
									<div class="vertical-image" style="width: 100%">
										<img src="../../page/img/pic_coach.png" alt="Image">
										<!-- <video style="width: 200px; height: 200px;" controls>
											<source src="<%=talkhead%>" type="video/mp4">
										</video>  -->
										<div class="inner-text">
											<p class="title"><%="軌跡：" + trajectory%></p>
											<!-- <p class="s_content"><%="p-system：" + psystem%></p>  -->
											<p class="s_content"><%="原因：" + cause%></p>
											<p class="s_content"><%="建議：" + suggestion%></p>
										</div>
										
										
										
									</div>
								</div>
							</div>

						</div>
					</div>
				</div>
			</div>
		</div>

	</div>
	<script>
		//var videoSlider = document.getElementById('videoSlider');
		var sideSwingPlaneData = <%=sideSwingPlane%>;
    	var frontSwingPlaneData = <%=frontSwingPlane%>;
    	var initialSideFrame = <%=sideFrames[0]%>; // 側面影片的第一步幀數
    	var initialFrontFrame = <%=frontFrames[0]%>; // 正面影片的第一步幀數
		var frameRate = 60;
		var controlBtn = document.getElementById('play-pause');
		// 設定影片和對應的畫布
		var video = document.getElementById('myvideo');
		var video1 = document.getElementById('myvideo1');
		var canvas = document.getElementById('overlayCanvas');
		var canvas1 = document.getElementById('overlayCanvas1');
		var ctx = canvas.getContext('2d');
		var ctx1 = canvas1.getContext('2d');
		const videoContainer = document.getElementById('videoContainer');
		const videoContainer1 = document.getElementById('videoContainer1');
		var dragging = false;
		var requestId = null;

		// 根據影片實際顯示大小調整畫布
		function resizeCanvas(videoElement, canvasElement) {
		    const videoDisplayWidth = videoElement.clientWidth;
		    const videoDisplayHeight = videoElement.clientHeight;

	    	canvasElement.style.width = `${videoDisplayWidth}px`;
	    	canvasElement.style.height = `${videoDisplayHeight}px`;
	    	canvasElement.width = videoDisplayWidth;
	    	canvasElement.height = videoDisplayHeight;
	    	//console.log("Canvas resized to match video dimensions: " + canvasElement.width + "x" + canvasElement.height);
	        // 更新畫布大小後，重新繪製輔助線
	        const ctx = canvasElement.getContext('2d');
	        const isSideView = (videoElement.id === "myvideo1");
	        clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, isSideView ? sideSwingPlaneData : frontSwingPlaneData, isSideView);
		}

		// // 繪製邊框
		// function drawBoundingBoxForVideo(bbox, videoElement, canvasElement, ctx, color = 'white') {
		//     const videoWidth = videoElement.videoWidth;
		//     const videoHeight = videoElement.videoHeight;
		//     const videoDisplayWidth = canvasElement.width;
		//     const videoDisplayHeight = canvasElement.height;

		//     // 計算縮放比例，並保持等比例縮放
		//     const scale = Math.min(videoDisplayWidth / videoWidth, videoDisplayHeight / videoHeight);
		//     const offsetX = (videoDisplayWidth - videoWidth * scale) / 2;
		//     const offsetY = (videoDisplayHeight - videoHeight * scale) / 2;

		//     const startX = bbox[0] * videoWidth * scale + offsetX;
		//     const startY = bbox[1] * videoHeight * scale + offsetY;
		//     const width = (bbox[2] - bbox[0]) * videoWidth * scale;
		//     const height = (bbox[3] - bbox[1]) * videoHeight * scale;

		//     ctx.strokeStyle = color;
		//     ctx.lineWidth = 4;
		//     ctx.strokeRect(startX, startY, width, height);

		//    //console.log("BoundingBox drawn: startX = " + startX + ", startY = " + startY + ", width = " + width + ", height = " + height);
		// }

		// 繪製輔助線
		// function drawLineForVideo(line, videoElement, canvasElement, ctx, color = 'orange') {
		//     const videoWidth = videoElement.videoWidth;
		//     const videoHeight = videoElement.videoHeight;
		//     const videoDisplayWidth = canvasElement.width;
		//     const videoDisplayHeight = canvasElement.height;

		//     // 計算縮放比例，並保持等比例縮放
		//     const scale = Math.min(videoDisplayWidth / videoWidth, videoDisplayHeight / videoHeight);
		//     const offsetX = (videoDisplayWidth - videoWidth * scale) / 2;
		//     const offsetY = (videoDisplayHeight - videoHeight * scale) / 2;

		//     const startX = line.pt1[0] * videoWidth * scale + offsetX;
		//     const startY = line.pt1[1] * videoHeight * scale + offsetY;
		//     const endX = line.pt2[0] * videoWidth * scale + offsetX;
		//     const endY = line.pt2[1] * videoHeight * scale + offsetY;

		//     ctx.beginPath();
		//     ctx.moveTo(startX, startY);
		//     ctx.lineTo(endX, endY);
		//     ctx.strokeStyle = color;
		//     ctx.lineWidth = 4;
		//     ctx.stroke();

		//    //console.log("Line drawn: startX = " + startX + ", startY = " + startY + ", endX = " + endX + ", endY = " + endY);
		// }

		// 繪製頭部：side的情況是矩形，front是橢圓
		// function drawHeadForVideo(head, videoElement, canvasElement, ctx, color = 'blue', side = true) {
		//     const videoWidth = videoElement.videoWidth;
		//     const videoHeight = videoElement.videoHeight;
		//     const videoDisplayWidth = canvasElement.width;
		//     const videoDisplayHeight = canvasElement.height;

		//     // 計算縮放比例和偏移量
		//     const scale = Math.min(videoDisplayWidth / videoWidth, videoDisplayHeight / videoHeight);
		//     const offsetX = (videoDisplayWidth - videoWidth * scale) / 2;
		//     const offsetY = (videoDisplayHeight - videoHeight * scale) / 2;

		//     // 計算頭部中心點位置
		//     const ptX = head.pt[0] * videoWidth * scale + offsetX;
		//     const ptY = head.pt[1] * videoHeight * scale + offsetY;

		//     if (side) {
		//         // 側視圖：繪製90度的夾角
		//         const lineLength = head.h_length * videoWidth * scale; // 使用頭部的水平長度作為線段長度

		//         // 繪製水平線（從頭部中心點向右延伸）
		//         ctx.beginPath();
		//         ctx.moveTo(ptX, ptY);  // 從中心點開始
		//         ctx.lineTo(ptX - lineLength, ptY);  // 向左延伸
		//         ctx.strokeStyle = color;
		//         ctx.lineWidth = 4;
		//         ctx.stroke();

		//         // 繪製垂直線（從頭部中心點向下延伸）
		//         ctx.beginPath();
		//         ctx.moveTo(ptX, ptY);  // 從中心點開始
		//         ctx.lineTo(ptX, ptY + lineLength);  // 向下延伸
		//         ctx.strokeStyle = color;
		//         ctx.lineWidth = 4;
		//         ctx.stroke();

		//         //console.log("Head drawn as lines for side view.");
		//     } else {
		//         // 正面圖，繪製橢圓
		//         const h_length = head.h_length * videoWidth * scale;
		//         const v_length = head.v_length * videoHeight * scale;

		//         ctx.beginPath();
		//         ctx.ellipse(ptX, ptY, h_length , v_length , 0, 0, 2 * Math.PI);
		//         ctx.strokeStyle = color;
		//         ctx.lineWidth = 4;
		//         ctx.stroke();
		//         //console.log("Head drawn as ellipse for front view.");
		//     }
		// }

		// ===========================
		// 繪製邊框 (修正版，適用於非等比例縮放到 canvas 的情況)
		function drawBoundingBoxForVideo(bbox, videoElement, canvasElement, ctx, color = 'white') {
			// 取得 canvas 的實際顯示尺寸
			const canvasWidth = canvasElement.width;
			const canvasHeight = canvasElement.height;

			// 假設影片內容已經被拉伸或壓縮以填滿整個 canvas 區域 (310x460)
			// 正規化後的標定框座標 [0, 1] 直接對應到 canvas 的像素座標

			// 計算標定框在 canvas 上的起始點座標
			const startX = bbox[0] * canvasWidth;
			const startY = bbox[1] * canvasHeight;

			// 計算標定框在 canvas 上的寬度和高度
			// 寬度 = (x2 - x1) * canvas 寬度
			// 高度 = (y2 - y1) * canvas 高度
			const width = (bbox[2] - bbox[0]) * canvasWidth;
			const height = (bbox[3] - bbox[1]) * canvasHeight;

			// 設定繪製樣式
			ctx.strokeStyle = color;
			ctx.lineWidth = 4; // 可以根據需要調整線條粗細

			// 繪製矩形
			ctx.strokeRect(startX, startY, width, height);

			// 註解掉 console.log，避免在正式環境中輸出過多資訊
			// console.log("BoundingBox drawn: startX = " + startX + ", startY = " + startY + ", width = " + width + ", height = " + height);
		}

		// 繪製輔助線 (修正版，適用於非等比例縮放到 canvas 的情況)
		function drawLineForVideo(line, videoElement, canvasElement, ctx, color = 'orange') {
			// 取得 canvas 的實際顯示尺寸
			const canvasWidth = canvasElement.width;
			const canvasHeight = canvasElement.height;

			// 假設影片內容已經被拉伸或壓縮以填滿整個 canvas 區域 (310x460)
			// 正規化後的線段端點座標 [0, 1] 直接對應到 canvas 的像素座標

			// 計算線段第一個端點在 canvas 上的座標
			const startX = line.pt1[0] * canvasWidth;
			const startY = line.pt1[1] * canvasHeight;

			// 計算線段第二個端點在 canvas 上的座標
			const endX = line.pt2[0] * canvasWidth;
			const endY = line.pt2[1] * canvasHeight;

			// 繪製線段
			ctx.beginPath();
			ctx.moveTo(startX, startY); // 從第一個端點開始
			ctx.lineTo(endX, endY);   // 連接到第二個端點
			ctx.strokeStyle = color;
			ctx.lineWidth = 4; // 可以根據需要調整線條粗細
			ctx.stroke();

			// 註解掉 console.log
			// console.log("Line drawn: startX = " + startX + ", startY = " + startY + ", endX = " + endX + ", endY = " + endY);
		}

		// 繪製頭部：side的情況是線條組，front是橢圓 (修正版，適用於非等比例縮放到 canvas 的情況)
		function drawHeadForVideo(head, videoElement, canvasElement, ctx, color = 'blue', side = true) {
			// 取得 canvas 的實際顯示尺寸
			const canvasWidth = canvasElement.width;
			const canvasHeight = canvasElement.height;

			// 假設影片內容已經被拉伸或壓縮以填滿整個 canvas 區域 (310x460)
			// 正規化後的頭部中心點座標 [0, 1] 以及長度直接對應到 canvas 的像素值

			// 計算頭部中心點在 canvas 上的位置
			const ptX = head.pt[0] * canvasWidth;
			const ptY = head.pt[1] * canvasHeight;

			if (side) {
				// 側視圖：繪製表示方向的線條組
				// 假設 head.h_length 和 head.v_length 是正規化後的水平和垂直長度概念
				const horizontalLineLength = head.h_length * canvasWidth; // 根據 canvas 寬度縮放正規化水平長度
				// 原程式碼兩條線都用了 head.h_length，但如果是非等比例，垂直長度應根據 canvas 高度縮放
				// 如果 head.h_length 也代表垂直方向的比例，則垂直線段長度應為 head.h_length * canvasHeight
				// 這裡暫時沿用原程式碼只使用 h_length 的邏輯，但將其垂直方向的縮放改為 canvasHeight
				const verticalLineLength = head.h_length * canvasHeight; // 根據 canvas 高度縮放正規化水平長度 (用於垂直線段)


				// 繪製水平線（從頭部中心點向左延伸，與原始程式碼方向一致）
				ctx.beginPath();
				ctx.moveTo(ptX, ptY);  // 從中心點開始
				ctx.lineTo(ptX - horizontalLineLength, ptY);  // 向左延伸，長度根據 canvas 寬度計算
				ctx.strokeStyle = color;
				ctx.lineWidth = 4;
				ctx.stroke();

				// 繪製垂直線（從頭部中心點向下延伸）
				ctx.beginPath();
				ctx.moveTo(ptX, ptY);  // 從中心點開始
				ctx.lineTo(ptX, ptY + verticalLineLength);  // 向下延伸，長度根據 canvas 高度計算
				ctx.strokeStyle = color;
				ctx.lineWidth = 4;
				ctx.stroke();

				// console.log("Head drawn as lines for side view.");
			} else {
				// 正面圖，繪製橢圓
				// 橢圓的水平半徑根據 canvas 寬度縮放正規化水平長度
				const h_radius = head.h_length * canvasWidth;
				// 橢圓的垂直半徑根據 canvas 高度縮放正規化垂直長度
				const v_radius = head.v_length * canvasHeight;

				ctx.beginPath();
				// ctx.ellipse(x, y, radiusX, radiusY, rotation, startAngle, endAngle);
				ctx.ellipse(ptX, ptY, h_radius, v_radius, 0, 0, 2 * Math.PI);
				ctx.strokeStyle = color;
				ctx.lineWidth = 4;
				ctx.stroke();
				// console.log("Head drawn as ellipse for front view.");
			}
		}
		// ===========================

		// 清除畫布並重新繪製輔助線，根據影片不同比例進行調整
		function clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, swingPlaneData, isSideView = true) {
		    ctx.clearRect(0, 0, canvasElement.width, canvasElement.height); // 清除畫布
		    drawBoundingBoxForVideo(swingPlaneData.data.bbox, videoElement, canvasElement, ctx, 'white');
		    if (swingPlaneData.data.club) {
		        drawLineForVideo(swingPlaneData.data.club, videoElement, canvasElement, ctx, 'orange');
		    }
		    if (swingPlaneData.data.shoulder) {
		        drawLineForVideo(swingPlaneData.data.shoulder, videoElement, canvasElement, ctx, 'orange');
		    }
		    if (swingPlaneData.data.left_leg) {
		        drawLineForVideo(swingPlaneData.data.left_leg, videoElement, canvasElement, ctx, 'blue');
		    }
		    if (swingPlaneData.data.right_leg) {
		        drawLineForVideo(swingPlaneData.data.right_leg, videoElement, canvasElement, ctx, 'blue');
		    }
		    if (swingPlaneData.data.head) {
		        drawHeadForVideo(swingPlaneData.data.head, videoElement, canvasElement, ctx, 'blue', isSideView);
		    } else {
		        console.log('Head data is missing or incomplete.');
		    }
		    
		    
		}
		// 確保影片元數據加載完成後再進行相關操作
		function setupVideoEvents(videoElement, canvasElement, ctx, swingPlaneData, initialFrame, isSideView) {
			videoElement.addEventListener('loadedmetadata', function () {
				//videoSlider.max = videoElement.duration;
				resizeCanvas(videoElement, canvasElement);
				videoElement.currentTime = initialFrame / frameRate;
				videoElement.pause();
				clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, swingPlaneData, isSideView);
			});
		}

		// 初始化影片事件
		setupVideoEvents(video, canvas, ctx, frontSwingPlaneData, initialFrontFrame, false);
		setupVideoEvents(video1, canvas1, ctx1, sideSwingPlaneData, initialSideFrame, true);
		
		// 當滑動條被拖動時，暫停影片並頻繁更新影片時間
//		videoSlider.addEventListener('input', function () {
//		    dragging = true;
//		    video.pause();
//		    video1.pause();
//		    
//		    if (requestId) {
//		        cancelAnimationFrame(requestId);
//		    }
//
//		    requestId = requestAnimationFrame(function updateSlider() {
//		        video.currentTime = videoSlider.value; 
//		        video1.currentTime = videoSlider.value;
//		        requestId = requestAnimationFrame(updateSlider); // 不斷請求下一幀更新
//		    });
//		});

		// 當用戶結束拖曳後，停止更新並恢復播放
//		videoSlider.addEventListener('change', function () {
//		    dragging = false;

		    // 停止 requestAnimationFrame 更新
//		    if (requestId) {
//		        cancelAnimationFrame(requestId);
//		        requestId = null;
//		    }

//		    video.currentTime = videoSlider.value;
//		    video1.currentTime = videoSlider.value;

//		    video.play();
//		    video1.play();
//		});

		// 同步影片時間更新滑動條
//		function syncSliderWithVideo(videoElement) {
//		    videoElement.addEventListener('timeupdate', function () {
//		        if (!dragging) {
//		            videoSlider.value = videoElement.currentTime;
//		        }
//		    });
//		}

		//syncSliderWithVideo(video);
		//syncSliderWithVideo(video1);
		
		// 當視窗大小改變時，調整每個影片的畫布大小
		window.addEventListener('resize', function () {
		    resizeCanvas(video, canvas);
		    resizeCanvas(video1, canvas1);
		 // 確保畫布大小調整後重新繪製
		    clearAndDrawOverlayForVideo(video, canvas, ctx, frontSwingPlaneData, false);
		    clearAndDrawOverlayForVideo(video1, canvas1, ctx1, sideSwingPlaneData, true);
		});

		
		function toggleFullScreen(videoElement, canvasElement, ctx, swingPlaneData, isSideView) {
		    if (!document.fullscreenElement) {
		        videoElement.requestFullscreen().catch(err => {
		            console.error(`Error attempting to enable full-screen mode: ${err.message} (${err.name})`);
		        });
		        videoElement.style.objectFit = 'contain';  // 確保全螢幕時保持影片比例
		        // 畫布調整後立即重新繪製輔助線
		        setTimeout(() => {
		            resizeCanvas(videoElement, canvasElement);
		            clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, swingPlaneData, isSideView);
		        }, 100);  // 可微調延遲時間
		    } else {
		        document.exitFullscreen();
		        videoElement.style.objectFit = '';  // 退出全螢幕後重置樣式
		    }
		    
		    // 當全螢幕狀態改變時重新調整畫布
		    resizeCanvas(videoElement, canvasElement);
		    clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, swingPlaneData, isSideView);
		}

		// 添加全螢幕狀態變更的事件監聽器，分別處理 video 和 video1
		document.addEventListener("fullscreenchange", () => {
		    if (document.fullscreenElement === video) {
		        // 如果 video 進入或退出全螢幕
		        resizeCanvas(video, canvas);
		        clearAndDrawOverlayForVideo(video, canvas, ctx, frontSwingPlaneData, false);
		    } else if (document.fullscreenElement === video1) {
		        // 如果 video1 進入或退出全螢幕
		        resizeCanvas(video1, canvas1);
		        clearAndDrawOverlayForVideo(video1, canvas1, ctx1, sideSwingPlaneData, true);
		    } else {
		        // 當退出全螢幕狀態時，恢復輔助線原始比例
		        resizeCanvas(video, canvas);
		        resizeCanvas(video1, canvas1);
		        clearAndDrawOverlayForVideo(video, canvas, ctx, frontSwingPlaneData, false);
		        clearAndDrawOverlayForVideo(video1, canvas1, ctx1, sideSwingPlaneData, true);
		    }
		});

		// 為每個影片增加點擊全螢幕的事件
		video.addEventListener('dblclick', function() {
		    toggleFullScreen(video, canvas, ctx, frontSwingPlaneData, false);
		});

		video1.addEventListener('dblclick', function() {
		    toggleFullScreen(video1, canvas1, ctx1, sideSwingPlaneData, true);
		});
		
        function goToFrame(frameNumber,frameNumber1) {
    		// 計算目標帧對應的時間（秒）
    		var time = frameNumber / frameRate;
    		var time1 = frameNumber1 / frameRate;
    		// 確保影片已載入完成後再設置 currentTime
    	    if (video.readyState >= 2) {
    	        video.pause(); // 暫停影片，確保時間設置後不立即播放
    	        video.currentTime = time;
    	    } else {
    	        video.addEventListener('loadedmetadata', function () {
    	            video.pause();
    	            video.currentTime = time;
    	        });
    	    }

    	    if (video1.readyState >= 2) {
    	        video1.pause(); // 暫停影片，確保時間設置後不立即播放
    	        video1.currentTime = time1;
    	    } else {
    	        video1.addEventListener('loadedmetadata', function () {
    	            video1.pause();
    	            video1.currentTime = time1;
    	        });
    	    }
    	    
    		controlBtn.className = 'play'; // 將按鈕設為播放狀態
    	    controlBtn.innerText = 'Play';
    	}
     	
        function playPause() {
			if (video.paused && video1.pause) {
                video.play();
                video1.play();
                controlBtn.className = 'pause';
                controlBtn.innerText = 'Pause';
            } else {
                video.pause();
                video1.pause();
                controlBtn.className = 'play';
                controlBtn.innerText = 'Play';
            }
		}
		
		controlBtn.addEventListener("click", playPause);
		function handleVideoEnd() {
		    if (video.ended && video1.ended) {
		        // 兩個視頻都已結束，重置按鈕以允許重新播放
		        controlBtn.className = 'play';
		        controlBtn.innerText = '播放';
		    } else {
		        // 只有一個視頻結束，檢查另一個視頻的狀態
		        if (video.ended && !video1.paused) {
		            // 第一個視頻結束，第二個正在播放，設置按鈕為暫停
		            controlBtn.className = 'pause';
		            controlBtn.innerText = '暫停';
		        } else if (video1.ended && !video.paused) {
		            // 第二個視頻結束，第一個正在播放，設置按鈕為暫停
		            controlBtn.className = 'pause';
		            controlBtn.innerText = '暫停';
		        }
		    }
		}
		video.addEventListener("ended", handleVideoEnd);
		video1.addEventListener("ended", handleVideoEnd);
		// 將JSP變量轉換為JavaScript變量
	    var greatLevelTopBS = <%=expertData.GreatLevelTopBS%>;
	    var greatLevelLowBS = <%=expertData.GreatLevelLowBS%>;
	    var goodLevelLowBS = <%=expertData.GoodLevelLowBS%>;
	    var normalLevelLowBS = <%=expertData.NormalLevelLowBS%>;
	    var badLevelLowBS = <%=expertData.BadLevelLowBS%>;
	    var worseLevelLowBS = <%=expertData.WorseLevelLowBS%>;
	    
	    var greatLevelTopCS = <%=expertData.GreatLevelTopCS%>;
	    var greatLevelLowCS = <%=expertData.GreatLevelLowCS%>;
	    var goodLevelLowCS = <%=expertData.GoodLevelLowCS%>;
	    var normalLevelLowCS = <%=expertData.NormalLevelLowCS%>;
	    var badLevelLowCS = <%=expertData.BadLevelLowCS%>;
	    var worseLevelLowCS = <%=expertData.WorseLevelLowCS%>;

	    var greatLevelTopDist = <%=expertData.GreatLevelTopDist%>;
	    var greatLevelLowDist = <%=expertData.GreatLevelLowDist%>;
	    var goodLevelLowDist = <%=expertData.GoodLevelLowDist%>;
	    var normalLevelLowDist = <%=expertData.NormalLevelLowDist%>;
	    var badLevelLowDist = <%=expertData.BadLevelLowDist%>;
	    var worseLevelLowDist = <%=expertData.WorseLevelLowDist%>;

	    var greatLevelTopLA = <%=expertData.GreatLevelTopLA%>;
	    var greatLevelLowLA = <%=expertData.GreatLevelLowLA%>;
	    var goodLevelLowLA = <%=expertData.GoodLevelLowLA%>;
	    var normalLevelLowLA = <%=expertData.NormalLevelLowLA%>;
	    var badLevelLowLA = <%=expertData.BadLevelLowLA%>;
	    var worseLevelLowLA = <%=expertData.WorseLevelLowLA%>;

	    var greatLevelTopBsp = <%=expertData.GreatLevelTopBsp%>;
	    var greatLevelLowBsp = <%=expertData.GreatLevelLowBsp%>;
	    var goodLevelLowBsp = <%=expertData.GoodLevelLowBsp%>;
	    var normalLevelLowBsp = <%=expertData.NormalLevelLowBsp%>;
	    var badLevelLowBsp = <%=expertData.BadLevelLowBsp%>;
	    var worseLevelLowBsp = <%=expertData.WorseLevelLowBsp%>;

		function getBallSpeedLevel(ballSpeed){
			if(ballSpeed>greatLevelTopBS){
				return greatLevelTopBS;
			}else if (ballSpeed <= greatLevelTopBS && ballSpeed>greatLevelLowBS){
				return greatLevelTopBS;
			}else if (ballSpeed<=greatLevelLowBS && ballSpeed>goodLevelLowBS){
				return greatLevelLowBS;
			}else if(ballSpeed<=goodLevelLowBS && ballSpeed>normalLevelLowBS){
				return goodLevelLowBS;
			}else if(ballSpeed<=normalLevelLowBS && ballSpeed>badLevelLowBS){
				return normalLevelLowBS;
			}else if(ballSpeed<=badLevelLowBS && ballSpeed>worseLevelLowBS){
				return badLevelLowBS;
			}else {
				return worseLevelLowBS;
			}
		}
		
		function getClubSpeedLevel(clubSpeed){
			if(clubSpeed>greatLevelTopCS ){
				return greatLevelTopCS ;
			}else if (clubSpeed<=greatLevelTopCS  && clubSpeed>greatLevelLowCS ){
				return greatLevelTopCS ;
			}else if (clubSpeed<=greatLevelLowCS  && clubSpeed>goodLevelLowCS ){
				return greatLevelLowCS ;
			}else if(clubSpeed<=goodLevelLowCS  && clubSpeed>normalLevelLowCS){
				return goodLevelLowCS ;
			}else if(clubSpeed<=normalLevelLowCS && clubSpeed>badLevelLowCS){
				return normalLevelLowCS;
			}else if(clubSpeed<=badLevelLowCS && clubSpeed>worseLevelLowCS ){
				return badLevelLowCS;
			}else {
				return worseLevelLowCS ;
			}
		}
		
		function getDistanceLevel(distance){
			if(distance>greatLevelTopDist){
				return greatLevelTopDist;
			}else if (distance<=greatLevelTopDist && distance>greatLevelLowDist ){
				return greatLevelTopDist;
			}else if (distance<=greatLevelLowDist  && distance>goodLevelLowDist ){
				return greatLevelLowDist ;
			}else if(distance<=goodLevelLowDist  && distance>normalLevelLowDist ){
				return goodLevelLowDist ;
			}else if(distance<=normalLevelLowDist  && distance>badLevelLowDist ){
				return normalLevelLowDist ;
			}else if(distance<=badLevelLowDist  && distance>worseLevelLowDist ){
				return badLevelLowDist ;
			}else {
				return worseLevelLowDist ;
			}
		}
		
		function getLaunchAngleLevel(launchAngle){
			if(launchAngle>greatLevelTopLA ){
				return greatLevelTopLA ;
			}else if (launchAngle<=greatLevelTopLA  && launchAngle>greatLevelLowLA ){
				return greatLevelTopLA ;
			}else if (launchAngle<=greatLevelLowLA  && launchAngle>goodLevelLowLA ){
				return greatLevelLowLA ;
			}else if(launchAngle<=goodLevelLowLA  && launchAngle>normalLevelLowLA ){
				return goodLevelLowLA ;
			}else if(launchAngle<=normalLevelLowLA  && launchAngle>badLevelLowLA ){
				return normalLevelLowLA ;
			}else if(launchAngle<=badLevelLowLA  && launchAngle>worseLevelLowLA ){
				return badLevelLowLA ;
			}else {
				return worseLevelLowLA ;
			}
		}
		
		function getBackSpinLevel(backSpin){
			if(backSpin>greatLevelTopBsp){
				return greatLevelTopBsp;
			}else if (backSpin<=greatLevelTopBsp && backSpin>greatLevelLowBsp){
				return greatLevelTopBsp;
			}else if (backSpin<=greatLevelLowBsp && backSpin>goodLevelLowBsp){
				return greatLevelLowBsp;
			}else if(backSpin<=goodLevelLowBsp && backSpin>normalLevelLowBsp ){
				return goodLevelLowBsp;
			}else if(backSpin<=normalLevelLowBsp  && backSpin>badLevelLowBsp){
				return normalLevelLowBsp ;
			}else if(backSpin<=badLevelLowBsp && backSpin>worseLevelLowBsp){
				return badLevelLowBsp;
			}else {
				return worseLevelLowBsp;
			}
		}

		var ballSpeed = <%=shotResult[0][0]%>;
		var avgBS = <%=shotResult[0][1]%>;
		var ballSpeedLevel = getBallSpeedLevel(ballSpeed);
		var clubSpeed = <%=shotResult[1][0]%>;
		var avgCS = <%=shotResult[1][1]%>;
		var clubSpeedLevel = getClubSpeedLevel(clubSpeed);
	    var distance = <%=shotResult[2][0]%>;
	    var launchDirection = <%=shotResult[5][0]%>		
		var avgDist = <%=shotResult[2][1]%>;
		var distanceLevel = getDistanceLevel(distance);
		var launchAngle = <%=shotResult[3][0]%>;
		var avgLA = <%=shotResult[3][1]%>;
		var launchAngleLevel = getLaunchAngleLevel(launchAngle);
		var backSpin = <%=shotResult[4][0]%>;
		var avgBsp = <%=shotResult[4][1]%>;
		var backSpinLevel = getBackSpinLevel(backSpin);	        
		var smachfact = Math.round((ballSpeed/clubSpeed)*100)/100;
		
		// 計算公式
		function calculateBallScore(distance, direction) {
		    if (distance > 176) {
		        distance = 176;
		    }

		    // direction 取絕對值
		    direction = Math.abs(direction);

		    // 當 direction 大於 16 時，direction 等於 16
		    if (direction > 16) {
		        direction = 16;
		    }
		    const score = ((distance / 176 + ((16 - direction) / 16) * 0.5) / 1.5) * 100;
		    return Math.ceil(Math.min(score, 100)); // 確保不超過100，並限制小數位數
		}
		//document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed + " -> " + ballSpeedLevel + "mph";
	    document.addEventListener("DOMContentLoaded", function() {
        	//document.getElementById("smachfactDisplay").innerText = "擊球效率:"+smachfact;
	        //document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed  + "mph";
	        //document.getElementById("clubSpeedDisplay").innerText = "桿頭速度: " + clubSpeed  + "mph";
	        //document.getElementById("distanceDisplay").innerText = "飛行距離: " + distance +  "yard";
	        //document.getElementById("launchAngleDisplay").innerText = "發射角度: " + launchAngle  + "degree";
	        //document.getElementById("backSpinDisplay").innerText = "後旋: " + backSpin  + "rpm";
	        document.getElementById("ballscore").innerText = "彈道分數: " + calculateBallScore(distance,launchDirection) ;
	    });
	    
	    // 數據範圍，您可以用後端服務的實際範圍來替換這些值
	    var ranges = {
	        'BackSpin': [[1, worseLevelLowBsp], [worseLevelLowBsp, badLevelLowBsp], [badLevelLowBsp, normalLevelLowBsp], [normalLevelLowBsp, goodLevelLowBsp], [goodLevelLowBsp,greatLevelLowBsp] ,[greatLevelLowBsp,greatLevelTopBsp],[greatLevelTopBsp,greatLevelTopBsp+backSpin]],
	        'ClubSpeed': [[1, worseLevelLowCS], [worseLevelLowCS, badLevelLowCS], [badLevelLowCS, normalLevelLowCS], [normalLevelLowCS, goodLevelLowCS], [goodLevelLowCS,greatLevelLowCS] ,[greatLevelLowCS,greatLevelTopCS],[greatLevelTopCS,greatLevelTopCS+clubSpeed]],
	        'Distance': [[1, worseLevelLowDist], [worseLevelLowDist, badLevelLowDist], [badLevelLowDist, normalLevelLowDist], [normalLevelLowDist, goodLevelLowDist], [goodLevelLowDist,greatLevelLowDist] ,[greatLevelLowDist,greatLevelTopDist],[greatLevelTopDist,greatLevelTopDist+distance]],
	        'BallSpeed': [[1, worseLevelLowBS], [worseLevelLowBS, badLevelLowBS], [badLevelLowBS, normalLevelLowBS], [normalLevelLowBS, goodLevelLowBS], [goodLevelLowBS,greatLevelLowBS] ,[greatLevelLowBS,greatLevelTopBS],[greatLevelTopBS,greatLevelTopBS+ballSpeed]],
	        'LaunchAngle': [[1, worseLevelLowLA], [worseLevelLowLA, badLevelLowLA], [badLevelLowLA, normalLevelLowLA], [normalLevelLowLA, goodLevelLowLA], [goodLevelLowLA,greatLevelLowLA] ,[greatLevelLowLA,greatLevelTopLA],[greatLevelTopLA,greatLevelTopLA+launchAngle]],
	        
	    };

	    // 將數據值映射到雷達圖的層級
	    function getLevel(value, range) {
	        return range.findIndex(r => value >= r[0] && value <= r[1]) + 1;
	    }

	    var radarData = {
	        labels: ['後旋', '桿頭速度', '距離' , '球速' , '發射角度'],
	        datasets: [{
	            label: '本次擊球',
	            data: [getLevel(backSpin, ranges.BackSpin), 
	            	getLevel(clubSpeed, ranges.ClubSpeed), 
	            	getLevel(distance, ranges.Distance), 
	            	getLevel(ballSpeed, ranges.BallSpeed), 
	            	getLevel(launchAngle, ranges.LaunchAngle)],
	            backgroundColor: 'rgba(135, 206, 250, 0.2)',
	            borderColor: 'rgba(135, 206, 250, 1)',
	            pointBackgroundColor: 'rgba(135, 206, 250, 1)',
	            pointBorderColor: '#fff',
	            pointHoverBackgroundColor: '#fff',
	            pointHoverBorderColor: 'rgba(135, 206, 250, 1)'
	        },
	        {
	            label: '擊球歷程',
	            data: [
	            	getLevel(avgBsp, ranges.BackSpin), 
	            	getLevel(avgCS, ranges.ClubSpeed), 
	            	getLevel(avgDist, ranges.Distance), 
	            	getLevel(avgBS, ranges.BallSpeed), 
	            	getLevel(avgLA, ranges.LaunchAngle)
	            ],
	            backgroundColor: 'rgba(255, 193, 7, 0.2)',
	            borderColor: 'rgba(255, 193, 7, 1)',
	            pointBackgroundColor: 'rgba(255, 193, 7, 1)',
	            pointBorderColor: '#fff',
	            pointHoverBackgroundColor: '#fff',
	            pointHoverBorderColor: 'rgba(255, 193, 7, 1)'
	        }
	    ]
	    };
	    var hiddenRangeDataset = {
	    	    label: '',
	    	    data: [1, 2, 3, 4, 5, 6, 7], // 涵蓋1到7的數據範圍
	    	    borderColor: 'rgba(0, 0, 0, 0)', // 完全透明
	    	    backgroundColor: 'rgba(0, 0, 0, 0)' // 完全透明
	    };

	    	radarData.datasets.push(hiddenRangeDataset);
	    var myRadarChart = new Chart(document.getElementById('radarChart'), {
	        type: 'radar',
	        data: radarData,
	        options: {
	            scales: {
	            	r: {
	                ticks: {
	                	display: false,
	                	backdropColor: 'transparent', // 去除背景色
	                	beginAtZero: false, // 不從0開始
	                    min: 1,  // 最小值設定為1
	                    max: 7,  // 最大值設定為7
	                    stepSize: 1,  // 步長為1
	                },
	                angleLines: {
	                    display: true
	                },
	                grid: {
	                	color: 'white'
	                },
	                pointLabels: {
	                    font: {
	                        size: 16, // 字體大小
	                        family: "'Arial', sans-serif", // 字體類型
	                        weight: 'bold' // 字體粗體
	                    	},
	                    color: '#FFFFFF' // 字體顏色
	                	},
	            	},
	            },
	            plugins: {
	                legend: {
	                    labels: {
	                        color: 'white', // 設置圖例文字顏色
	                        font: {
	                            size: 16, // 設置圖例文字大小
	                            family: "'Arial', sans-serif",
	                            weight: 'bold'
	                        }
	                    }
	                }
	            },
	        }
	    });
	</script>
</body>
</html>
