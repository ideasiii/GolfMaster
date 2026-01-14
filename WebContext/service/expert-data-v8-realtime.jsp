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
Long shot_data_id = result.getLong("shotdata_id");
// Long shot_data_id = 128069L; // test
Long exID = result.getLong("id"); // Unused, but kept for context

Object temp[] = shotVideo.processAnalyz(shot_data_id);
int[] sideFrames = (int[]) temp[0];
int[] frontFrames = (int[]) temp[1];
// String frontVideoName = (String) temp[2];
// String sideVideoName = (String) temp[3];
String frontVideoPath = (String) temp[2];
String sideVideoPath = (String) temp[3];
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
	<link href="../../page/css/GM08_realtime.css" rel="stylesheet" type="text/css">
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
	<script src="../../page/js/headerNavManager.js"></script>
	<style>
	</style>
</head>
<body>
	<div class="c_m">
		<div class="header">
			<img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
			<div class="navigation-buttons hidden-element">
				<button class="nav-button" id="nav-swing" data-page="expert-data-v8.jsp" data-current="true">
					<img src="../../page/img/swing_icon.png" alt="揮桿分析">
				</button>
				<button class="nav-button" id="nav-chip" data-page="expert-data-v8-short.jsp">
					<img src="../../page/img/chip_icon.png" alt="切桿分析">
				</button>
				<button class="nav-button temporarily-disabled" id="nav-putt" data-page="expert-data-v8-putt.jsp">
					<img src="../../page/img/putt_icon.png" alt="推桿分析">
				</button>
			</div>
		</div>

		<div class="main-layout">
			<div class="left-panel">
				<div class="data-container">
					<div class="data-row">
						<div class="card highlight">
							<div class="title">距離</div>
							<div class="unit">yards</div>
							<div id="val-distance" class="number">--</div>
						</div>
					</div>

					<div class="data-row">
						<div class="card">
							<div class="title">球速</div>
							<div class="unit">mph</div>
							<div id="val-ballSpeed" class="number">--</div>
						</div>
						<div class="card">
							<div class="title">桿頭速度</div>
							<div class="unit">mph</div>
							<div id="val-clubSpeed" class="number">--</div>
						</div>
					</div>

					<div class="data-row">
						<div class="card">
							<div class="title">倒旋</div>
							<div class="unit">rpm</div>
							<div id="val-backSpin" class="number">--</div>
						</div>
						<div class="card">
							<div class="title">發射角度</div>
							<div class="unit">°</div>
							<div id="val-launchAngle" class="number">--</div>
						</div>
					</div>
				</div>
			</div>

			<div class="right-panel">
				<div class="analysis-section-wrapper">
					<div class="analysis-container">

						<div class="analysis-left-col">
							<div class="analysis-header">
								<p class="strike-off">
									彈道分數: <span class="score" id="ballscore"></span>
								</p>
							</div>

							<div class="trajectory-visual">
								<div class="trajectory-scaling-box" id="trajectory-gif-container">
								</div>
							</div>
						</div>

						<div class="analysis-right-col">
							<div class="radar-chart-container">
								<canvas id="radarChart"></canvas>
							</div>
						</div>

					</div>
				</div>

				<div class="advice-section-wrapper">
					<div class="f_content">
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

	<script>
		// --- 1. JSP 數據導出至 JS 變數 ---
		const tpiAdvicesData = '<%= StringUtils.defaultIfEmpty(tpiAdvicesJson, "null") %>';
		const combinedTpiSwingTable = <%= new JSONArray(combinedTpiSwingTable).toString() %>;
		const shotResultData = <%= new JSONArray(shotResult).toString() %>;
		const currentTrajectory = '<%= trajectory %>';
    	const hasAnalysisResult = <%= result.optBoolean("result", false) %>;

		// 定義軌跡與 GIF 檔案的對應關係 (包含中日文與英文判斷)
		const P_SYS = {
			STRAIGHT_GROUP: [
				'<%= pSystem.DRAW %>', '<%= pSystem.STRAIGHT %>', '<%= pSystem.FADE %>',
				'<%= pSystemJP.DRAW %>', '<%= pSystemJP.STRAIGHT %>', '<%= pSystemJP.FADE %>'
			],
			PUSH_SLICE: ['<%= pSystem.PUSH_SLICE %>', '<%= pSystemJP.PUSH_SLICE %>'],
			PULL_HOOK: ['<%= pSystem.PULL_HOOK %>', '<%= pSystemJP.PULL_HOOK %>'],
			PULL_GROUP: [
				'<%= pSystem.PULL %>', '<%= pSystem.PULL_SLICE %>',
				'<%= pSystemJP.PULL %>', '<%= pSystemJP.PULL_SLICE %>'
			],
			PUSH_GROUP: [
				'<%= pSystem.PUSH %>', '<%= pSystem.PUSH_HOOK %>',
				'<%= pSystemJP.PUSH %>', '<%= pSystemJP.PUSH_HOOK %>'
			]
		};

		// 專家級數據臨界值 (用於雷達圖)
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

		// --- 2. 擊球數據處理 ---
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

		// --- 3. 核心邏輯函式 ---

		// 計算彈道分數 (由距離與偏誤角度決定)
		function calculateBallScore(distance, direction) {
			// PGA 男子 7 Iron 平均距離基準 = 176
			const baseDistance = 176;
			distance = Math.min(distance, baseDistance);

			// 方向偏誤取絕對值，最大懲罰臨界值為 16 度
			const directionTemp = Math.min(Math.abs(direction), 16);

			const score = ((distance / baseDistance + ((16 - directionTemp) / 16) * 0.5) / 1.5) * 100;
			return Math.ceil(Math.min(score, 100));
		}

		// --- 2. 核心函式：取得 GIF 名稱 ---
		function resolveGifName(trajectory) {
			if (!trajectory) return "";

			if (P_SYS.STRAIGHT_GROUP.includes(trajectory)) return "Straight_2";
			if (P_SYS.PUSH_SLICE.includes(trajectory)) return "Pushs_2";
			if (P_SYS.PULL_HOOK.includes(trajectory)) return "Pullh_2";
			if (P_SYS.PULL_GROUP.includes(trajectory)) return "Pull_2";
			if (P_SYS.PUSH_GROUP.includes(trajectory)) return "Push_2";

			return "";
		}

		// --- 4. 初始化 ---
		function init() {
			console.log("初始化數據面板...");

			// --- 填入左側擊球數據 ---
			const dataMapping = {
				'val-distance': userShotData.distance,
				'val-ballSpeed': userShotData.ballSpeed,
				'val-clubSpeed': userShotData.clubSpeed,
				'val-backSpin': userShotData.backSpin,
				'val-launchAngle': userShotData.launchAngle
			};

			// 迴圈遍歷並填入數值
			for (const [id, value] of Object.entries(dataMapping)) {
				const el = document.getElementById(id);
				if (el) {
					// 如果數值是數字，則取小數點後一位；否則顯示 --
					el.innerText = (typeof value === 'number') ? value.toFixed(1) : '--';
				}
			}

			// --- 更新右側彈道分數 ---
			const ballScoreElement = document.getElementById("ballscore");
			if (ballScoreElement) {
				ballScoreElement.innerText = calculateBallScore(
					userShotData.distance,
					userShotData.launchDirection
				);
			}

			// --- 處理右側彈道 GIF 顯示 (原本的邏輯) ---
			const container = document.getElementById('trajectory-gif-container');
			if (hasAnalysisResult && container) {
				const gifName = resolveGifName(currentTrajectory);
				// console.log(currentTrajectory);
				// console.log(gifName);
				if (gifName) {
					// 插入圖片並套用我們設定好的 class
					container.innerHTML = `<img src="../../page/gif/`+ gifName + `.gif" class="trajectory-img" alt="彈道軌跡">`;
				}
			}

			// --- 初始化雷達圖 ---
			const radarCanvas = document.getElementById('radarChart');
			if (radarCanvas) {
				initializeRadarChart(
					radarCanvas,
					userShotData,
					expertDataLevels
				);
			}
			// 注意：原本關於影片播放 (toggleFullScreen, goToFrame, playPause)
			// 以及步驟按鈕 (handleButtonClick) 的程式碼已根據需求刪除。
		}

		// 當文件載入完成時執行初始化
		document.addEventListener("DOMContentLoaded", init);
	</script>
</body>
</html>
