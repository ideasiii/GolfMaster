<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>

<%@ page import="com.golfmaster.service.CallLlmAdviseAPI"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page import="com.golfmaster.moduel.DeviceData"%>
<%@ page import="com.golfmaster.moduel.PSystem"%>
<%@ page import="com.golfmaster.moduel.PSystemJP"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%-- Java Parameters --%>
<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>
<%!PSystem pSystem = new PSystem();%>
<%!PSystemJP pSystemJP = new PSystemJP();%>
<%!CallLlmAdviseAPI callLlmAdviseAPI = new CallLlmAdviseAPI();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");
// Long shot_data_id = 128069L; // test (expert = 128002)
Long exID = result.getLong("id"); // Unused, but kept for context

Object temp[] = shotVideo.processAnalyz(shot_data_id);
int[] sideFrames = (int[]) temp[0];
int[] frontFrames = (int[]) temp[1];
String frontVideoName = (String) temp[2];
String sideVideoName = (String) temp[3];
int aEffect = (int) temp[4]; // Address
int tEffect = (int) temp[5]; // Top
int iEffect = (int) temp[6]; // Impact
int fEffect = (int) temp[7]; // Finish
String sideSwingPlane = (String) temp[8]; // 側面 SwingPlane 資料
String frontSwingPlane = (String) temp[9]; // 正面 SwingPlane 資料
int[] combinedTpiSwingTable = (int[]) temp[10]; // SwingTable 資料
String tpiAdvicesJson = (String) temp[11]; // allFilteredAdvicesJson 資料

float[][] shotResult = shotData.processPlayerReq(shot_data_id);
String currShotDataResult = shotData.processCurrShotData(shot_data_id);
String shortGameResult = shotData.processShortGameData(shot_data_id, 10);

String psystem = result.optString("expert_p_system", "");
String trajectory = result.optString("expert_trajectory", "");
String cause = result.optString("expert_cause", "");
String suggestion = result.optString("expert_suggestion", "");

String useLLM = request.getParameter("LLM");
String adviceResult = "";
if (useLLM != null && useLLM.equals("true")) {
	adviceResult = callLlmAdviseAPI.getLlmAdvise(
		shot_data_id.toString(),
		currShotDataResult,
		shortGameResult,
		tpiAdvicesJson,
		result.toString()
	);
}

%>

<%-- HTML --%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="../../page/css/GM08_short.css" rel="stylesheet" type="text/css">
	<title>Expert</title>
	<!-- <script src="https://cdn.jsdelivr.net/npm/chart.js"></script> -->
	<%-- outer js --%>
	<%-- <script
		src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.5.1/chart.js">
	</script> --%>
	<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script> 
	<script
		src="https://cdn.jsdelivr.net/npm/chartjs-plugin-annotation@3.0.1/dist/chartjs-plugin-annotation.min.js">
	</script>
	<%-- inner js --%>
	<script src="../../page/js/swingVideo.js"></script>
	<script src="../../page/js/tpiAdvicesManager.js"></script>
	<script src="../../page/js/cmpChartManager.js"></script>
	<script src="../../page/js/shortTableManager.js"></script>
	<script src="../../page/js/headerNavManager.js"></script>
	<style>
	</style>
</head>
<body>
	<div class="c_m">
        <div class="header">
            <img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
			<div class="navigation-buttons">
				<button class="nav-button" id="nav-swing" data-page="expert-data-v8.jsp" data-current="true">
					<img src="../../page/img/swing_icon.png" alt="揮桿分析">
				</button>
				<button class="nav-button" id="nav-chip" data-page="expert-data-v8-short.jsp">
					<img src="../../page/img/chip_icon.png" alt="切桿分析">
				</button>
				<%-- 尚未實作 --%>
				<button class="nav-button temporarily-disabled" id="nav-putt" data-page="expert-data-v8-putt.jsp">
					<img src="../../page/img/putt_icon.png" alt="推桿分析">
				</button>
			</div>
        </div>

        <div class="main-layout-container">
            <div class="main-left-column">
                <div class="video-section-top">
                    <div id="videoContainer" style="position: relative;" class="image_v">
                        <video id="myvideo" controls muted>
                            <source src="../../video/analyzVideo_front/<%=frontVideoName%>" type="video/mp4" alt="Image 1" />
                        </video>
                        <canvas id="overlayCanvas"></canvas>
                    </div>
                    <div id="videoContainer1" style="position: relative;" class="image_v">
                        <video id="myvideo1" controls muted>
                            <source src="../../video/analyzVideo_side/<%=sideVideoName%>" type="video/mp4" alt="Image 1" />
                        </video>
                        <canvas id="overlayCanvas1"></canvas>
                    </div>
                </div>

				<div class="motion-advice-section-bottom">
                    <div class="steps-bar-top">
                        <div class="steps">
                            <div class="step">
                                <span class="step_title">Swing</span><br> <span class="step_4">4 Steps</span>
                            </div>
                            <div class="step">
                                <button class="stepbutton" data-phase="A" data-front-frame="<%=frontFrames[0]%>" data-side-frame="<%=sideFrames[0]%>">A</button>
                            </div>
                            <div class="step">
                                <button class="stepbutton" data-phase="T" data-front-frame="<%=frontFrames[1]%>" data-side-frame="<%=sideFrames[1]%>">T</button>
                            </div>
                            <div class="step">
                                <button class="stepbutton" data-phase="I" data-front-frame="<%=frontFrames[2]%>" data-side-frame="<%=sideFrames[2]%>">I</button>
                            </div>
                            <div class="step">
                                <button class="stepbutton" data-phase="F" data-front-frame="<%=frontFrames[3]%>" data-side-frame="<%=sideFrames[3]%>">F</button>
                            </div>
                            <div class="step">
                                <div id="player-container">
                                    <div id="play-pause" class="play">Play</div>
                                </div>
                            </div>
                        </div>
                    </div>

					<div class="chart-and-advice-bottom">
						<div class="psystemSection">
							<div id="compare-chart" class="box">
								<img id="img-A" class="chart-img" data-phase="A" data-effect="<%=aEffect%>" src="../../page/img/A/A<%=aEffect%>.png">
								<img id="img-T" class="chart-img" data-phase="T" data-effect="<%=tEffect%>" src="../../page/img/T/T<%=tEffect%>.png">
								<img id="img-I" class="chart-img" data-phase="I" data-effect="<%=iEffect%>" src="../../page/img/I/I<%=iEffect%>.png">
								<img id="img-F" class="chart-img" data-phase="F" data-effect="<%=fEffect%>" src="../../page/img/F/F<%=fEffect%>.png">
							</div>
							<div id="tpi-advices" class="p_de">
								<table>
								</table>
							</div>
						</div>
					</div>
				</div>
            </div>

            <div class="main-right-column">
                <div class="analysis-box" id="shotAnalysisBox">
                    <p class="box-title">擊球穩定性</p>
                    <div class="shot-dispersion-map" id="shotDispersionMap">
                        <%-- <img src="../../page/img/dispersion_map.png" alt="Shot Dispersion Map" class="dispersion-img"> --%>
                    </div>

					<div class="target-control" id="targetControl">
                        <label for="targetDistanceInput">目標距離 (yards):</label>
                        <input type="number" id="targetDistanceInput" value="50" min="1" step="1">
                    </div>

					<div class="stats-table" id="shotStatsTable">
                    </div>
                </div>
            </div>
        </div>
    </div>

	<script>
		// --- JSP Data to JS Variables ---
		const sideSwingPlaneData = <%= StringUtils.defaultIfEmpty(sideSwingPlane, "null") %>;
		const frontSwingPlaneData = <%= StringUtils.defaultIfEmpty(frontSwingPlane, "null") %>;
		const initialSideFrame = <%= sideFrames[0] %>; // 側面影片的第一步幀數
		const initialFrontFrame = <%= frontFrames[0] %>; // 正面影片的第一步幀數
		const combinedTpiSwingTable = <%= new JSONArray(combinedTpiSwingTable).toString() %>;
		const tpiAdvicesData = '<%= StringUtils.defaultIfEmpty(tpiAdvicesJson, "null") %>';
		const shortGameResultData = '<%= shortGameResult %>';
		const golfAdviceResult = '<%= adviceResult %>';

		// console.log(sideSwingPlaneData);
		// console.log(frontSwingPlaneData);
		console.log(tpiAdvicesData);
		// console.log(shortGameResultData);
		console.log(golfAdviceResult);

		// --- Global DOM & App State Variables ---
		const frameRate = 60;

		const controlBtn = document.getElementById('play-pause');
		const videoContainer = document.getElementById('videoContainer');
		const video = document.getElementById('myvideo');
		const canvas = document.getElementById('overlayCanvas');
		const ctx = canvas.getContext('2d');

		const videoContainer1 = document.getElementById('videoContainer1');
		const video1 = document.getElementById('myvideo1');
		const canvas1 = document.getElementById('overlayCanvas1');
		const ctx1 = canvas1.getContext('2d');
		const playPauseBtn1 = document.getElementById('play-pause1');
		const progressBar1 = document.getElementById('progress-bar1');
		const timeDisplay1 = document.getElementById('time-display1');
		const fullscreenBtn1 = document.getElementById('fullscreen-btn1');

		// App State Variables ---
		const cmpChartManager = new CmpChartManager(
			chartContainerElement = document.getElementById('compare-chart'),
		);
		const tpiManager = new TpiAdvicesManager(
			tableContainerElement = document.getElementById('tpi-advices'),
			maxDisplayItems = 1,
			timingOptions = {
				animationDuration: 500,
				carouselInterval: 5000,
				initialDelay: 500,
			},
		);

		const shortTableManager = new ShortTableManager(
			"shotAnalysisBox"
		);


		// --- Video Control Functions ---
		function toggleFullScreen(containerElement) {
			console.log("toggleFullScreen");
			if (!document.fullscreenElement) {
				// 進入全螢幕
				if (containerElement.requestFullscreen) {
					containerElement.requestFullscreen();
				} else if (containerElement.webkitRequestFullscreen) { // Safari
					containerElement.webkitRequestFullscreen();
				} else if (containerElement.msRequestFullscreen) { // IE11
					containerElement.msRequestFullscreen();
				}
			} else {
				// 退出全螢幕
				if (document.exitFullscreen) {
					document.exitFullscreen();
				}
			}
		}

		function handleFullScreenChange() {
			// 使用 setTimeout 給瀏覽器一點時間來更新 DOM 元素的尺寸
			setTimeout(() => {
				// 無論是進入還是退出全螢幕，都重新調整兩個畫布的大小
				// 這樣可以確保畫布尺寸始終與影片的顯示尺寸保持一致
				resizeCanvas(video, canvas, frontSwingPlaneData, isSideView=false);
				resizeCanvas(video1, canvas1, sideSwingPlaneData, isSideView=true);
			}, 150); // 150毫秒的延遲，可以根據實際情況微調
		}

		// --- swing bar Functions ---
		// 按鈕樣式控制函式
		function selectButton(selectedBtn) {
			// 移除所有按鈕的選取樣式
			const buttons = document.querySelectorAll('.steps button');
			buttons.forEach(btn => {
				btn.classList.remove('stepbutton_selected');
				btn.classList.add('stepbutton');
			});

			// 為被點擊的按鈕添加選取樣式
			selectedBtn.classList.remove('stepbutton');
			selectedBtn.classList.add('stepbutton_selected');
		}

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
			if (video.paused && video1.paused) {
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

		function handleVideoEnd() {
			console.log("handleVideoEnd");
			if (video.ended && video1.ended) {
				// 兩個視頻都已結束，重置按鈕以允許重新播放
				controlBtn.className = 'play';
				controlBtn.innerText = 'Play';
			} else {
				// 只有一個視頻結束，檢查另一個視頻的狀態
				if (video.ended && !video1.paused) {
					// 第一個視頻結束，第二個正在播放，設置按鈕為暫停
					controlBtn.className = 'pause';
					controlBtn.innerText = 'Pause';
				} else if (video1.ended && !video.paused) {
					// 第二個視頻結束，第一個正在播放，設置按鈕為暫停
					controlBtn.className = 'pause';
					controlBtn.innerText = 'Pause';
				}
			}
		}

		// 處理按鈕點擊的總函式
		function handleButtonClick(event) {
			// 獲取按鈕上的 data-屬性
			const phase = event.target.dataset.phase;
			const effectValue = document.getElementById('img-'+ phase).dataset.effect;
			const frontFrame = parseInt(event.target.dataset.frontFrame);
			const sideFrame = parseInt(event.target.dataset.sideFrame);

			// 處理按鈕樣式
			selectButton(event.target);

			// 處理影片跳轉
			goToFrame(frontFrame, sideFrame);

			// 根據點擊的階段更新圖片顯示
			cmpChartManager.updateChartTpiImage(phase, tpiAdvicesData);
			cmpChartManager.updateChartImage(phase);

			// 處理 TPI 建議顯示
			tpiManager.updateTable(
				phase,
				tpiAdvicesData,
				effectValue,
				golfAdviceResult,
			);
		}


		// --- Initialization ---
		function init() {
			// console.log("init");

			// Setup event listeners
			window.addEventListener('resize', () => {
				resizeCanvas(video, canvas, frontSwingPlaneData, isSideView=false);
				resizeCanvas(video1, canvas1, sideSwingPlaneData, isSideView=true);
			});

			// 為所有步驟按鈕添加事件監聽器
			const stepButtons = document.querySelectorAll('.steps button');
			stepButtons.forEach(button => {
				button.addEventListener('click', handleButtonClick);
			});

			// 監聽全螢幕改變事件
			document.addEventListener("fullscreenchange", handleFullScreenChange);
			document.addEventListener("webkitfullscreenchange", handleFullScreenChange);
			document.addEventListener("mozfullscreenchange", handleFullScreenChange);
			document.addEventListener("MSFullscreenChange", handleFullScreenChange);

			// 為每個影片增加事件
			if (videoContainer) {
				// console.log("videoContainer");
				video.addEventListener('dblclick', () => toggleFullScreen(videoContainer));
			}
			if (videoContainer1) {
				// console.log("videoContainer1");
				video1.addEventListener('dblclick', () => toggleFullScreen(videoContainer1));
			}
			controlBtn.addEventListener("click", playPause);
			video.addEventListener("ended", handleVideoEnd);
			video1.addEventListener("ended", handleVideoEnd);

			// // 初始化影片事件
			setupVideoEvents(video, canvas, frontSwingPlaneData, initialFrontFrame, frameRate, false);
			setupVideoEvents(video1, canvas1, sideSwingPlaneData, initialSideFrame, frameRate, true);

			// 首次載入時顯示第一組數據
			// 預設載入時選取 'A' 按鈕並顯示內容
			const firstButton = document.querySelector('.steps button[data-phase="A"]');
			if (firstButton) {
				firstButton.classList.remove('stepbutton');
				firstButton.classList.add('stepbutton_selected');

				// 更新與教練比較圖
				cmpChartManager.updateChartTpiImage(firstButton.dataset.phase, tpiAdvicesData);
				cmpChartManager.updateChartImage(firstButton.dataset.phase);

				// 處理 TPI 建議顯示
				const effectValue = document.getElementById('img-'+ firstButton.dataset.phase).dataset.effect;
				tpiManager.updateTable(
					firstButton.dataset.phase,
					tpiAdvicesData,
					effectValue,
					golfAdviceResult,
				);
			}

			// 初始化擊球穩定性表
			if (typeof shortGameResultData === 'string' && shortGameResultData.trim().length > 0) {
				// 更新表格
				shortTableManager.updateTable(shortGameResultData);
			} else {
				shortTableManager.clearTable();
			}

			// 初始化兩個播放器
			// setupPlayerControls(video, playPauseBtn, progressBar, timeDisplay, volumeBar, fullscreenBtn, videoContainer);
			// setupPlayerControls(video1, playPauseBtn1, progressBar1, timeDisplay1, fullscreenBtn1, videoContainer1);
		}

		document.addEventListener("DOMContentLoaded", init);
	</script>
</body>
</html>
