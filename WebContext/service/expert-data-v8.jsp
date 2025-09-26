<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>

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
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
// Long shot_data_id = result.getLong("shotdata_id");
Long shot_data_id = 128069L; // test
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

// String psystem = "";
// String trajectory = "";
// String cause = "";
// String suggestion = "";
// String img = "";
// String talkhead = "";

// float backSwingTime = 0;
// float downSwingTime = 0;
// float tempo = 0;
// float BallSpeed;
// float BackSpin;
// float SideSpin;
// float LaunchAngle;
// float Angle;

String psystem = result.optString("expert_p_system", "");
String trajectory = result.optString("expert_trajectory", "");
String cause = result.optString("expert_cause", "");
String suggestion = result.optString("expert_suggestion", "");

//if(result != null && result.getString("video_url") != null){
//	talkhead = result.getString("video_url");
//}

%>

<%-- HTML --%>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="../../page/css/GM08_3.css" rel="stylesheet" type="text/css">
	<title>Expert</title>
	<!-- <script src="https://cdn.jsdelivr.net/npm/chart.js"></script> -->
	<%-- outer js --%>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.5.1/chart.js">
	</script>
	<%-- inner js --%>
	<script src="../../page/js/radarChart.js"></script>
	<script src="../../page/js/swingVideo.js"></script>
	<script src="../../page/js/tpiAdvicesManager.js"></script>
	<script src="../../page/js/cmpChartManager.js"></script>
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
			<div class="column2 video-column">
				<div class="content">
                    <div class="row">
                        <div class="row2" style="background-color: #000000">
                            <div id="videoContainer" style="position: relative;" class="image_v">
                                <video id="myvideo" controls muted>
                                    <source src="../../video/analyzVideo_front/<%=frontVideoName%>"
                                        type="video/mp4" alt="Image 1" />
                                </video>
                                <canvas id="overlayCanvas"></canvas>
                            </div>
                            <div id="videoContainer1" style="position: relative;" class="image_v">
                                <video id="myvideo1" controls muted>
                                    <source src="../../video/analyzVideo_side/<%=sideVideoName%>"
                                        type="video/mp4" alt="Image 1" />
                                </video>
                                <canvas id="overlayCanvas1"></canvas>

								<div id="player-controls1">
									<%-- <button id="video-play-pause1" class="play" type="button"></button> --%>
									<%-- <input type="range" id="progress-bar1" value="0"> --%>
									<%-- <span id="time-display1">00:00 / 00:00</span> --%>
									<%-- <button id="fullscreen-btn1" type="button">全螢幕</button> --%>
								</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="column2 analysis-column">
                <div class="content" style="text-align: center;">
                    <div class="row">
                        <div class="aanalysisSection">
                            <div class="row2" style="background-color: #000000">
                                <div style="position: relative;" class="image_v_trajectory">
									<p class="strikeeff"> 彈道分數:
										<span class="score" id="ballscore"></span>
									</p>
                                    <div class="crop-left">
										<%
											if (result.getBoolean("result")) {
												String gifName = "";
												if (trajectory.equals(pSystem.DRAW) || trajectory.equals(pSystem.STRAIGHT) || trajectory.equals(pSystem.FADE)
														|| trajectory.equals(pSystemJP.DRAW) || trajectory.equals(pSystemJP.STRAIGHT)
														|| trajectory.equals(pSystemJP.FADE)) {
													gifName = "Straight_2";
												} else if (trajectory.equals(pSystem.PUSH_SLICE) || trajectory.equals(pSystemJP.PUSH_SLICE)) {
													gifName = "Pushs_2";
												} else if (trajectory.equals(pSystem.PULL_HOOK) || trajectory.equals(pSystemJP.PULL_HOOK)) {
													gifName = "Pullh_2";
												} else if (trajectory.equals(pSystem.PULL) || trajectory.equals(pSystem.PULL_SLICE)
														|| trajectory.equals(pSystemJP.PULL) || trajectory.equals(pSystemJP.PULL_SLICE)) {
													gifName = "Pull_2";
												} else if (trajectory.equals(pSystem.PUSH) || trajectory.equals(pSystem.PUSH_HOOK)
														|| trajectory.equals(pSystemJP.PUSH) || trajectory.equals(pSystemJP.PUSH_HOOK)) {
													gifName = "Push_2";
												}
												if (!gifName.isEmpty()) {
													out.print("<img src='../../page/gif/" + gifName + ".gif' class='crop-img' />");
												}
											}
										%>
                                    </div>
                                </div>
                                <div>
                                    <div>
                                        <canvas id="radarChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

		<div class="row2 stretch">
			<div class="column2 motion-column">
				<div class="custom-div2">
					<div class="steps">
						<div class="step">
							<span class="step_title">Swing</span><br> <span
								class="step_4">4 Steps</span>
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
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->
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
			<div class="column2 advice-column">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>能力提升分析</h1>-->
						<div class="f_content">
							<div class="column2">
								<div class="suggestion">
									<div class="vertical-image" style="width: 100%">
										<img src="../../page/img/aicoach.png" alt="Image">
										<div class="inner-text">
											<p class="title"><%="軌跡：" + trajectory%></p>
											<p class="s_content"><span class="f_c1">原因： </span><%= cause%> </p>
											<p class="s_content"><span class="f_c1">建議： </span><%= suggestion%> </p>
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
		// --- JSP Data to JS Variables ---
		const sideSwingPlaneData = <%= StringUtils.defaultIfEmpty(sideSwingPlane, "null") %>;
		const frontSwingPlaneData = <%= StringUtils.defaultIfEmpty(frontSwingPlane, "null") %>;
		const initialSideFrame = <%= sideFrames[0] %>; // 側面影片的第一步幀數
		const initialFrontFrame = <%= frontFrames[0] %>; // 正面影片的第一步幀數
		const combinedTpiSwingTable = <%= new JSONArray(combinedTpiSwingTable).toString() %>;
		const tpiAdvicesData = '<%= StringUtils.defaultIfEmpty(tpiAdvicesJson, "null") %>';

		console.log(tpiAdvicesData);

		// 假設這是你的 combinedTpiSwingTable 陣列，這裡用一個固定值來模擬
		// const combinedTpiSwingTable = [1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0];

		const shotResultData = <%= new JSONArray(shotResult).toString() %>;

		const expertDataLevels = {
			greatLevelTopBS: <%= expertData.GreatLevelTopBS %>,
			greatLevelLowBS: <%= expertData.GreatLevelLowBS %>,
			goodLevelLowBS: <%= expertData.GoodLevelLowBS %>,
			normalLevelLowBS: <%= expertData.NormalLevelLowBS %>,
			badLevelLowBS: <%= expertData.BadLevelLowBS %>,
			worseLevelLowBS: <%= expertData.WorseLevelLowBS %>,

			greatLevelTopCS: <%= expertData.GreatLevelTopCS %>,
			greatLevelLowCS: <%= expertData.GreatLevelLowCS %>,
			goodLevelLowCS: <%= expertData.GoodLevelLowCS %>,
			normalLevelLowCS: <%= expertData.NormalLevelLowCS %>,
			badLevelLowCS: <%= expertData.BadLevelLowCS %>,
			worseLevelLowCS: <%= expertData.WorseLevelLowCS %>,

			greatLevelTopDist: <%= expertData.GreatLevelTopDist %>,
			greatLevelLowDist: <%= expertData.GreatLevelLowDist %>,
			goodLevelLowDist: <%= expertData.GoodLevelLowDist %>,
			normalLevelLowDist: <%= expertData.NormalLevelLowDist %>,
			badLevelLowDist: <%= expertData.BadLevelLowDist %>,
			worseLevelLowDist: <%= expertData.WorseLevelLowDist %>,

			greatLevelTopLA: <%= expertData.GreatLevelTopLA %>,
			greatLevelLowLA: <%= expertData.GreatLevelLowLA %>,
			goodLevelLowLA: <%= expertData.GoodLevelLowLA %>,
			normalLevelLowLA: <%= expertData.NormalLevelLowLA %>,
			badLevelLowLA: <%= expertData.BadLevelLowLA %>,
			worseLevelLowLA: <%= expertData.WorseLevelLowLA %>,

			greatLevelTopBsp: <%= expertData.GreatLevelTopBsp %>,
			greatLevelLowBsp: <%= expertData.GreatLevelLowBsp %>,
			goodLevelLowBsp: <%= expertData.GoodLevelLowBsp %>,
			normalLevelLowBsp: <%= expertData.NormalLevelLowBsp %>,
			badLevelLowBsp: <%= expertData.BadLevelLowBsp %>,
			worseLevelLowBsp: <%= expertData.WorseLevelLowBsp %>
		};

		// console.log(shotResultData);
		// console.log(expertDataLevels);
		// console.log(sideSwingPlaneData);
		// console.log(frontSwingPlaneData);
		console.log(combinedTpiSwingTable);

		// --- Shot Data ---
		const userShotData = {
			'ballSpeed': shotResultData[0][0],
			'avgBS': shotResultData[0][1],
			'clubSpeed': shotResultData[1][0],
			'avgCS': shotResultData[1][1],
			'distance': shotResultData[2][0],
			'avgDist': shotResultData[2][1],
			'launchAngle': shotResultData[3][0],
			'avgLA': shotResultData[3][1],
			'backSpin': shotResultData[4][0],
			'avgBsp': shotResultData[4][1],
			'launchDirection': shotResultData[5][0],
			'smachfact': Math.round((shotResultData[0][0] / shotResultData[1][0]) * 100) / 100,
		};

		// const ballSpeedLevel = getBallSpeedLevel(ballSpeed);
		// const clubSpeedLevel = getClubSpeedLevel(clubSpeed);
		// const distanceLevel = getDistanceLevel(distance);
		// const launchAngleLevel = getLaunchAngleLevel(launchAngle);
		// const backSpinLevel = getBackSpinLevel(backSpin);

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
		// var dragging = false;
		// var requestId = null;

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

		// --- ball score Functions ---
		function calculateBallScore(distance, direction) {
			// PGA 男子 7 Iron 平均距離 = 176
			distance = Math.min(distance, 176);

			// direction 取絕對值
			// 當 direction 大於 16 時，direction 等於 16
			directionTemp = Math.min(Math.abs(direction), 16);

			const score = ((distance / 176 + ((16 - directionTemp) / 16) * 0.5) / 1.5) * 100;
			return Math.ceil(Math.min(score, 100)); // 確保不超過100，並限制小數位數
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
			);
		}

		// --- Initialization ---
		function init() {
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
				console.log("videoContainer");
				video.addEventListener('dblclick', () => toggleFullScreen(videoContainer));
			}
			if (videoContainer1) {
				console.log("videoContainer1");
				video1.addEventListener('dblclick', () => toggleFullScreen(videoContainer1));
			}
			controlBtn.addEventListener("click", playPause);
			video.addEventListener("ended", handleVideoEnd);
			video1.addEventListener("ended", handleVideoEnd);

			// 初始化影片事件
			setupVideoEvents(video, canvas, frontSwingPlaneData, initialFrontFrame, frameRate, false);
			setupVideoEvents(video1, canvas1, sideSwingPlaneData, initialSideFrame, frameRate, true);

			// 初始化擊球分數
			document.getElementById("ballscore").innerText = calculateBallScore(
				distance = userShotData.distance,
				launchDirection = userShotData.launchDirection,
			);

			// 初始化radar chart
			initializeRadarChart(
				document.getElementById('radarChart'),
				userShotData,
				expertDataLevels,
			);

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
				);
			}

			// 初始化兩個播放器
			// setupPlayerControls(video, playPauseBtn, progressBar, timeDisplay, volumeBar, fullscreenBtn, videoContainer);
			// setupPlayerControls(video1, playPauseBtn1, progressBar1, timeDisplay1, fullscreenBtn1, videoContainer1);
		}

		document.addEventListener("DOMContentLoaded", init);
	</script>
	<script>
		// TODO

		//document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed + " -> " + ballSpeedLevel + "mph";
		// document.addEventListener("DOMContentLoaded", function() {
		// 	//document.getElementById("smachfactDisplay").innerText = "擊球效率:"+smachfact;
		// 	//document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed  + "mph";
		// 	//document.getElementById("clubSpeedDisplay").innerText = "桿頭速度: " + clubSpeed  + "mph";
		// 	//document.getElementById("distanceDisplay").innerText = "飛行距離: " + distance +  "yard";
		// 	//document.getElementById("launchAngleDisplay").innerText = "發射角度: " + launchAngle  + "degree";
		// 	//document.getElementById("backSpinDisplay").innerText = "後旋: " + backSpin  + "rpm";
		// 	// document.getElementById("ballscore").innerText = "彈道分數: " + calculateBallScore(distance,launchDirection) ;
		// 	document.getElementById("ballscore").innerText = calculateBallScore(distance,launchDirection);
		// });

		// --- Video Control bar Functions ---
		// function setupPlayerControls(vid, playBtn, progress, timeDisp, fullBtn, container) {
		// 	if (!vid) return;

		// 	// 播放/暫停按鈕
		// 	playBtn.addEventListener('click', () => {
		// 		if (vid.paused || vid.ended) {
		// 			vid.play();
		// 			playBtn.className = 'pause'; // 修改按鈕樣式
		// 		} else {
		// 			vid.pause();
		// 			playBtn.className = 'play'; // 修改按鈕樣式
		// 		}
		// 	});

		// 	// 影片時間與進度條同步
		// 	vid.addEventListener('timeupdate', () => {
		// 		const value = (100 / vid.duration) * vid.currentTime;
		// 		progress.value = value;

		// 		// 內嵌時間格式化邏輯，避免函式衝突
		// 		const minutes = Math.floor(vid.currentTime / 60);
		// 		const seconds = Math.floor(vid.currentTime % 60);
		// 		const formattedTime = minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0');

		// 		const totalMinutes = Math.floor(vid.duration / 60);
		// 		const totalSeconds = Math.floor(vid.duration % 60);
		// 		const formattedDuration = totalMinutes.toString().padStart(2, '0') + ":" + totalSeconds.toString().padStart(2, '0');

		// 		timeDisp.textContent = formattedTime + "/" + formattedDuration;
		// 	});

		// 	// 拖曳進度條
		// 	progress.addEventListener('input', () => {
		// 		const time = (progress.value / 100) * vid.duration;
		// 		vid.currentTime = time;
		// 	});

		// 	// 全螢幕按鈕
		// 	fullBtn.addEventListener('click', () => {
		// 		if (container.requestFullscreen) {
		// 			container.requestFullscreen();
		// 		} else if (container.mozRequestFullScreen) { /* Firefox */
		// 			container.mozRequestFullScreen();
		// 		} else if (container.webkitRequestFullscreen) { /* Chrome, Safari & Opera */
		// 			container.webkitRequestFullscreen();
		// 		} else if (container.msRequestFullscreen) { /* IE/Edge */
		// 			container.msRequestFullscreen();
		// 		}
		// 	});
		// }
	</script>
	<script>
		// backup
		// function getBallSpeedLevel(ballSpeed){
		// 	if(ballSpeed>greatLevelTopBS){
		// 		return greatLevelTopBS;
		// 	}else if (ballSpeed <= greatLevelTopBS && ballSpeed>greatLevelLowBS){
		// 		return greatLevelTopBS;
		// 	}else if (ballSpeed<=greatLevelLowBS && ballSpeed>goodLevelLowBS){
		// 		return greatLevelLowBS;
		// 	}else if(ballSpeed<=goodLevelLowBS && ballSpeed>normalLevelLowBS){
		// 		return goodLevelLowBS;
		// 	}else if(ballSpeed<=normalLevelLowBS && ballSpeed>badLevelLowBS){
		// 		return normalLevelLowBS;
		// 	}else if(ballSpeed<=badLevelLowBS && ballSpeed>worseLevelLowBS){
		// 		return badLevelLowBS;
		// 	}else {
		// 		return worseLevelLowBS;
		// 	}
		// }

		// function getClubSpeedLevel(clubSpeed){
		// 	if(clubSpeed>greatLevelTopCS ){
		// 		return greatLevelTopCS ;
		// 	}else if (clubSpeed<=greatLevelTopCS  && clubSpeed>greatLevelLowCS ){
		// 		return greatLevelTopCS ;
		// 	}else if (clubSpeed<=greatLevelLowCS  && clubSpeed>goodLevelLowCS ){
		// 		return greatLevelLowCS ;
		// 	}else if(clubSpeed<=goodLevelLowCS  && clubSpeed>normalLevelLowCS){
		// 		return goodLevelLowCS ;
		// 	}else if(clubSpeed<=normalLevelLowCS && clubSpeed>badLevelLowCS){
		// 		return normalLevelLowCS;
		// 	}else if(clubSpeed<=badLevelLowCS && clubSpeed>worseLevelLowCS ){
		// 		return badLevelLowCS;
		// 	}else {
		// 		return worseLevelLowCS ;
		// 	}
		// }

		// function getDistanceLevel(distance){
		// 	if(distance>greatLevelTopDist){
		// 		return greatLevelTopDist;
		// 	}else if (distance<=greatLevelTopDist && distance>greatLevelLowDist ){
		// 		return greatLevelTopDist;
		// 	}else if (distance<=greatLevelLowDist  && distance>goodLevelLowDist ){
		// 		return greatLevelLowDist ;
		// 	}else if(distance<=goodLevelLowDist  && distance>normalLevelLowDist ){
		// 		return goodLevelLowDist ;
		// 	}else if(distance<=normalLevelLowDist  && distance>badLevelLowDist ){
		// 		return normalLevelLowDist ;
		// 	}else if(distance<=badLevelLowDist  && distance>worseLevelLowDist ){
		// 		return badLevelLowDist ;
		// 	}else {
		// 		return worseLevelLowDist ;
		// 	}
		// }

		// function getLaunchAngleLevel(launchAngle){
		// 	if(launchAngle>greatLevelTopLA ){
		// 		return greatLevelTopLA ;
		// 	}else if (launchAngle<=greatLevelTopLA  && launchAngle>greatLevelLowLA ){
		// 		return greatLevelTopLA ;
		// 	}else if (launchAngle<=greatLevelLowLA  && launchAngle>goodLevelLowLA ){
		// 		return greatLevelLowLA ;
		// 	}else if(launchAngle<=goodLevelLowLA  && launchAngle>normalLevelLowLA ){
		// 		return goodLevelLowLA ;
		// 	}else if(launchAngle<=normalLevelLowLA  && launchAngle>badLevelLowLA ){
		// 		return normalLevelLowLA ;
		// 	}else if(launchAngle<=badLevelLowLA  && launchAngle>worseLevelLowLA ){
		// 		return badLevelLowLA ;
		// 	}else {
		// 		return worseLevelLowLA ;
		// 	}
		// }

		// function getBackSpinLevel(backSpin){
		// 	if(backSpin>greatLevelTopBsp){
		// 		return greatLevelTopBsp;
		// 	}else if (backSpin<=greatLevelTopBsp && backSpin>greatLevelLowBsp){
		// 		return greatLevelTopBsp;
		// 	}else if (backSpin<=greatLevelLowBsp && backSpin>goodLevelLowBsp){
		// 		return greatLevelLowBsp;
		// 	}else if(backSpin<=goodLevelLowBsp && backSpin>normalLevelLowBsp ){
		// 		return goodLevelLowBsp;
		// 	}else if(backSpin<=normalLevelLowBsp  && backSpin>badLevelLowBsp){
		// 		return normalLevelLowBsp ;
		// 	}else if(backSpin<=badLevelLowBsp && backSpin>worseLevelLowBsp){
		// 		return badLevelLowBsp;
		// 	}else {
		// 		return worseLevelLowBsp;
		// 	}
		// }
	</script>
</body>
</html>
