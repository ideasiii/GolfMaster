<%@ page import="org.json.JSONArray"%>
<%@ page import="org.json.JSONObject"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.text.ParseException"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.Calendar"%>
<%@ page import="org.apache.commons.lang3.StringUtils"%>

<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page import="com.golfmaster.moduel.PSystem"%>
<%@ page import="com.golfmaster.moduel.PSystemJP"%>
<%@ page import="com.golfmaster.common.Logs"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>
<%!PSystem pSystem = new PSystem();%>
<%!PSystemJP pSystemJP = new PSystemJP();%>

<%
request.setCharacterEncoding("UTF-8");

JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");

Object[] temp = shotVideo.processAnalyz(shot_data_id);
int[] sideFrames = (int[]) temp[0];
int[] frontFrames = (int[]) temp[1];
String frontVideoPath = (String) temp[2];
String sideVideoPath = (String) temp[3];
int aEffect = (int) temp[4];
int tEffect = (int) temp[5];
int iEffect = (int) temp[6];
int fEffect = (int) temp[7];
String sideSwingPlane = (String) temp[8];
String frontSwingPlane = (String) temp[9];
int[] combinedTpiSwingTable = (int[]) temp[10];
String tpiAdvicesJson = (String) temp[11];
boolean frontAnalyzReady = (boolean) temp[12];
boolean sideAnalyzReady = (boolean) temp[13];
boolean frontExpected = (boolean) temp[14];
boolean sideExpected = (boolean) temp[15];

float[][] shotResult = shotData.processPlayerReq(shot_data_id);
String shortGameResult = shotData.processShortGameData(shot_data_id, 10);
JSONObject meta = shotData.processShotMeta(shot_data_id);

String trajectory = result.optString("expert_trajectory", "");
String cause = result.optString("expert_cause", "");
String suggestion = result.optString("expert_suggestion", "");

String dbPlayer = meta.optString("player", "Guest");
String dbClubType = meta.optString("ClubType", "");
String dbDateRaw = meta.optString("Date", "");

// 處理使用者輸入（暱稱、場地）
String paramNickname = request.getParameter("nickname");
String paramVenue = request.getParameter("venue");
String nickname = (paramNickname != null && !paramNickname.trim().isEmpty()) ? paramNickname.trim() : dbPlayer;
String venue = (paramVenue != null && !paramVenue.trim().isEmpty()) ? paramVenue.trim() : "室內高爾夫模擬器";

// 中文化日期：2026 年 5 月 6 日 下午 3:24
String shotTimeChinese = "";
String recordIdDate = "";
if (!dbDateRaw.isEmpty()) {
	try {
		SimpleDateFormat[] candidateFormats = new SimpleDateFormat[] {
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
		};
		Date parsed = null;
		for (SimpleDateFormat fmt : candidateFormats) {
			try {
				parsed = fmt.parse(dbDateRaw);
				break;
			} catch (ParseException ignored) {}
		}
		if (parsed != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(parsed);
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			String ampm = (hour < 12) ? "上午" : "下午";
			int hour12 = (hour % 12 == 0) ? 12 : (hour % 12);
			shotTimeChinese = String.format(
				"%d 年 %d 月 %d 日 %s %d:%02d",
				cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
				ampm, hour12, cal.get(Calendar.MINUTE)
			);
			recordIdDate = new SimpleDateFormat("yyyyMMdd").format(parsed);
		}
	} catch (Exception e) {
		Logs.log(Logs.EXCEPTION_LOG, "PDF date parse: " + e.toString());
	}
}
// 沒有有效的擊球時間（DB 缺資料或格式異常）→ 顯示「—」，紀錄編號省略日期段
// 不能用「今天」當 fallback，那會誤導為 PDF 生成日 = 擊球日
if (shotTimeChinese.isEmpty()) {
	shotTimeChinese = "—";
}
String recordId;
if (recordIdDate.isEmpty()) {
	recordId = "GM-" + shot_data_id;
} else {
	recordId = "GM-" + recordIdDate + "-" + shot_data_id;
}

// 紀錄下載 (Q2-a：iframe 載入 = 視為下載開始)
String clientIp = request.getHeader("X-Forwarded-For");
if (clientIp == null || clientIp.isEmpty()) clientIp = request.getRemoteAddr();
String userAgent = request.getHeader("User-Agent");
if (userAgent == null) userAgent = "";
Logs.log(Logs.RUN_LOG, "PDF_DOWNLOAD," + shot_data_id + "," + clientIp + "," + userAgent.replace(',', ';'));

// 是否為 iframe 嵌入模式（v8.jsp 主視窗開啟時帶 embed=1，省略內嵌 lib 與自動下載）
boolean isEmbed = "1".equals(request.getParameter("embed"));
%>

<!DOCTYPE html>
<html lang="zh-TW">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>GolfMaster 紀念紀錄 — <%= recordId %></title>
	<link href="../../page/css/GM08_pdf.css" rel="stylesheet" type="text/css">
	<script src="../../page/js/chart_3_5_1.min.js"></script>
	<script src="../../page/js/radarChart.js"></script>
	<script src="../../page/js/swingVideo.js"></script>
	<script src="../../page/js/shortTableManager.js"></script>
<% if (!isEmbed) { %>
	<script src="../../page/js/lib/html2canvas.min.js"></script>
	<script src="../../page/js/lib/jspdf.umd.min.js"></script>
<% } %>
</head>
<body>
<div class="pdf-doc">

	<%-- ============== 第一頁 ============== --%>
	<section class="pdf-page" id="pdf-page-1">
		<header class="pdf-header">
			<div class="pdf-header-left">
				<div class="pdf-header-title">擊球紀念紀錄</div>
				<div class="pdf-header-subtitle">GolfMaster Memorial Shot Record</div>
			</div>
			<div class="pdf-header-right">
				<div><%= shotTimeChinese %></div>
				<div class="record-id"><%= recordId %></div>
			</div>
		</header>

		<div class="pdf-meta-bar">
			<div class="meta-item">
				<span class="meta-label">暱稱</span>
				<span class="meta-value"><%= StringUtils.defaultIfEmpty(nickname, "—") %></span>
			</div>
			<div class="meta-item">
				<span class="meta-label">球桿</span>
				<span class="meta-value"><%= StringUtils.defaultIfEmpty(dbClubType, "—") %></span>
			</div>
			<div class="meta-item">
				<span class="meta-label">場地</span>
				<span class="meta-value"><%= venue %></span>
			</div>
			<div class="meta-item">
				<span class="meta-label">擊球時間</span>
				<span class="meta-value"><%= shotTimeChinese %></span>
			</div>
		</div>

		<div class="pdf-main">
			<div class="pdf-cards">
				<div class="pdf-card highlight">
					<div class="pdf-card-label">距離</div>
					<div id="pdf-val-distance" class="pdf-card-value">--</div>
					<div class="pdf-card-unit">yards</div>
				</div>
				<div class="pdf-card-row">
					<div class="pdf-card">
						<div class="pdf-card-label">球速</div>
						<div id="pdf-val-ballSpeed" class="pdf-card-value">--</div>
						<div class="pdf-card-unit">mph</div>
					</div>
					<div class="pdf-card">
						<div class="pdf-card-label">桿頭速度</div>
						<div id="pdf-val-clubSpeed" class="pdf-card-value">--</div>
						<div class="pdf-card-unit">mph</div>
					</div>
				</div>
				<div class="pdf-card-row">
					<div class="pdf-card">
						<div class="pdf-card-label">後旋</div>
						<div id="pdf-val-backSpin" class="pdf-card-value">--</div>
						<div class="pdf-card-unit">rpm</div>
					</div>
					<div class="pdf-card">
						<div class="pdf-card-label">發射角度</div>
						<div id="pdf-val-launchAngle" class="pdf-card-value">--</div>
						<div class="pdf-card-unit">°</div>
					</div>
				</div>
			</div>

			<div class="pdf-analysis">
				<div class="pdf-score-row">
					<div class="pdf-score-box">
						<div class="score-label">彈道分數</div>
						<div id="pdf-ballscore" class="score-value">--</div>
					</div>
					<div class="pdf-trajectory" id="pdf-trajectory-container">
						<%-- GIF 由 JS 注入 --%>
					</div>
				</div>
				<div class="pdf-radar">
					<canvas id="pdf-radarChart"></canvas>
				</div>
			</div>
		</div>

		<%-- 教練建議 --%>
		<div class="pdf-advice">
			<div class="advice-title">教練建議</div>
			<div class="advice-line"><span class="label">軌跡：</span><%= StringUtils.defaultIfEmpty(trajectory, "—") %></div>
			<div class="advice-line"><span class="label">原因：</span><%= StringUtils.defaultIfEmpty(cause, "—") %></div>
			<div class="advice-line"><span class="label">建議：</span><%= StringUtils.defaultIfEmpty(suggestion, "—") %></div>
		</div>

		<footer class="pdf-footer">
			<div class="privacy">隱私提醒：本紀念 PDF 含個人肖像與擊球紀錄，請妥善保管。未經本人同意，請勿公開散佈或上傳至公開平台。</div>
			<div class="copyright">
				<span>&copy; 2026 GolfMaster 系統</span>
				<span>紀錄編號 <%= recordId %></span>
			</div>
		</footer>
	</section>

	<%-- ============== 第二頁 ============== --%>
	<section class="pdf-page" id="pdf-page-2">
		<header class="pdf-header">
			<div class="pdf-header-left">
				<div class="pdf-header-title">詳細分析</div>
				<div class="pdf-header-subtitle">Detailed Analysis</div>
			</div>
			<div class="pdf-header-right">
				<div><%= StringUtils.defaultIfEmpty(nickname, "—") %></div>
				<div class="record-id"><%= recordId %></div>
			</div>
		</header>

		<%-- A. 使用者影像 --%>
		<div class="pdf-section" id="pdf-section-frames">
			<div class="pdf-section-title">使用者影像（A／T／I／F 四相）</div>
			<div id="pdf-frames-banner"></div>
			<div id="pdf-frames-body" class="pdf-frames">
				<div class="pdf-section-empty">影像處理中...</div>
			</div>
		</div>

		<%-- B. TPI 問題特徵 --%>
		<div class="pdf-section" id="pdf-section-tpi">
			<div class="pdf-section-title">TPI 揮桿特徵分析</div>
			<div id="pdf-tpi-body" class="pdf-tpi-grid"></div>
		</div>

		<%-- C. 擊球穩定性 --%>
		<div class="pdf-section" id="pdf-section-stability">
			<div class="pdf-section-title">擊球穩定性</div>
			<div id="pdf-stability-body">
				<div class="pdf-stability">
					<div class="pdf-stability-chart" id="pdf-shotDispersionMap"></div>
					<div class="pdf-stability-table" id="pdf-shotStatsTable"></div>
				</div>
			</div>
		</div>

		<footer class="pdf-footer">
			<div class="privacy">隱私提醒：本紀念 PDF 含個人肖像與擊球紀錄，請妥善保管。未經本人同意，請勿公開散佈或上傳至公開平台。</div>
			<div class="copyright">
				<span>&copy; 2026 GolfMaster 系統</span>
				<span>紀錄編號 <%= recordId %></span>
			</div>
		</footer>
	</section>

</div>

<%-- 兩部影片 — 隱藏，只是用來抓 frame 截圖 --%>
<div style="position:absolute; left:-9999px; top:-9999px; width:1px; height:1px; overflow:hidden;">
	<div id="pdfVideoContainerFront" style="position:relative;">
		<video id="pdfVideoFront" muted preload="auto" crossorigin="anonymous">
			<source src="<%= frontVideoPath %>" type="video/mp4" />
		</video>
		<canvas id="pdfOverlayCanvasFront"></canvas>
	</div>
	<div id="pdfVideoContainerSide" style="position:relative;">
		<video id="pdfVideoSide" muted preload="auto" crossorigin="anonymous">
			<source src="<%= sideVideoPath %>" type="video/mp4" />
		</video>
		<canvas id="pdfOverlayCanvasSide"></canvas>
	</div>
</div>

<script>
// ============== JSP -> JS 變數 ==============
const PDF_DATA = {
	shotDataId: '<%= shot_data_id %>',
	recordId: '<%= recordId %>',
	nickname: <%= toJsString(nickname) %>,
	venue: <%= toJsString(venue) %>,
	clubType: <%= toJsString(dbClubType) %>,
	shotTimeChinese: <%= toJsString(shotTimeChinese) %>,
	trajectory: <%= toJsString(trajectory) %>,
	frontFrames: <%= new JSONArray(frontFrames).toString() %>,
	sideFrames: <%= new JSONArray(sideFrames).toString() %>,
	frontExpected: <%= frontExpected %>,
	sideExpected: <%= sideExpected %>,
	frontAnalyzReady: <%= frontAnalyzReady %>,
	sideAnalyzReady: <%= sideAnalyzReady %>,
	frontSwingPlane: <%= StringUtils.defaultIfEmpty(frontSwingPlane, "null") %>,
	sideSwingPlane: <%= StringUtils.defaultIfEmpty(sideSwingPlane, "null") %>,
	tpiAdvicesJson: <%= toJsString(StringUtils.defaultIfEmpty(tpiAdvicesJson, "")) %>,
	combinedTpiSwingTable: <%= new JSONArray(combinedTpiSwingTable).toString() %>,
	shortGameResult: <%= toJsString(StringUtils.defaultIfEmpty(shortGameResult, "")) %>,
	shotResult: <%= new JSONArray(shotResult).toString() %>,
	aEffect: <%= aEffect %>,
	tEffect: <%= tEffect %>,
	iEffect: <%= iEffect %>,
	fEffect: <%= fEffect %>,
	isEmbed: <%= isEmbed %>,
	expertLevels: {
		greatLevelTopBS: <%= expertData.GreatLevelTopBS %>, greatLevelLowBS: <%= expertData.GreatLevelLowBS %>,
		goodLevelLowBS: <%= expertData.GoodLevelLowBS %>, normalLevelLowBS: <%= expertData.NormalLevelLowBS %>,
		badLevelLowBS: <%= expertData.BadLevelLowBS %>, worseLevelLowBS: <%= expertData.WorseLevelLowBS %>,
		greatLevelTopCS: <%= expertData.GreatLevelTopCS %>, greatLevelLowCS: <%= expertData.GreatLevelLowCS %>,
		goodLevelLowCS: <%= expertData.GoodLevelLowCS %>, normalLevelLowCS: <%= expertData.NormalLevelLowCS %>,
		badLevelLowCS: <%= expertData.BadLevelLowCS %>, worseLevelLowCS: <%= expertData.WorseLevelLowCS %>,
		greatLevelTopDist: <%= expertData.GreatLevelTopDist %>, greatLevelLowDist: <%= expertData.GreatLevelLowDist %>,
		goodLevelLowDist: <%= expertData.GoodLevelLowDist %>, normalLevelLowDist: <%= expertData.NormalLevelLowDist %>,
		badLevelLowDist: <%= expertData.BadLevelLowDist %>, worseLevelLowDist: <%= expertData.WorseLevelLowDist %>,
		greatLevelTopLA: <%= expertData.GreatLevelTopLA %>, greatLevelLowLA: <%= expertData.GreatLevelLowLA %>,
		goodLevelLowLA: <%= expertData.GoodLevelLowLA %>, normalLevelLowLA: <%= expertData.NormalLevelLowLA %>,
		badLevelLowLA: <%= expertData.BadLevelLowLA %>, worseLevelLowLA: <%= expertData.WorseLevelLowLA %>,
		greatLevelTopBsp: <%= expertData.GreatLevelTopBsp %>, greatLevelLowBsp: <%= expertData.GreatLevelLowBsp %>,
		goodLevelLowBsp: <%= expertData.GoodLevelLowBsp %>, normalLevelLowBsp: <%= expertData.NormalLevelLowBsp %>,
		badLevelLowBsp: <%= expertData.BadLevelLowBsp %>, worseLevelLowBsp: <%= expertData.WorseLevelLowBsp %>
	}
};

const userShotData = {
	'ballSpeed': PDF_DATA.shotResult[0][0],
	'avgBS': PDF_DATA.shotResult[0][1],
	'clubSpeed': PDF_DATA.shotResult[1][0],
	'avgCS': PDF_DATA.shotResult[1][1],
	'distance': PDF_DATA.shotResult[2][0],
	'avgDist': PDF_DATA.shotResult[2][1],
	'launchAngle': PDF_DATA.shotResult[3][0],
	'avgLA': PDF_DATA.shotResult[3][1],
	'backSpin': PDF_DATA.shotResult[4][0],
	'avgBsp': PDF_DATA.shotResult[4][1],
	'launchDirection': PDF_DATA.shotResult[5][0]
};

// ============== 共用工具 ==============
function fmtNum(v) { return (typeof v === 'number' && isFinite(v)) ? v.toFixed(1) : '--'; }

function calcBallScore(distance, direction) {
	const baseDistance = 176;
	distance = Math.min(distance || 0, baseDistance);
	const dirAbs = Math.min(Math.abs(direction || 0), 16);
	const score = ((distance / baseDistance + ((16 - dirAbs) / 16) * 0.5) / 1.5) * 100;
	return Math.ceil(Math.min(score, 100));
}

// 動畫 GIF 在 html2canvas 截圖時通常只能取到第一幀（多半是空白），
// 因此 PDF 模式下優先用 page/img/ 內的靜態 PNG/JPG。
// 沒對應到的軌跡 → 回 null，讓畫面顯示文字 fallback。
function resolveTrajectoryImage(traj) {
	if (!traj) return null;
	const STRAIGHT = ['Draw 右曲球','Straight 直球','Fade 左曲球','右曲球（ドロー）','直球（ストレート）','左曲球（フェード）'];
	const PULL_HOOK = ['Pull Hook 左曲球','左曲球（プルフック）'];
	if (STRAIGHT.includes(traj))  return '../../page/img/path_straight_5.png';
	if (PULL_HOOK.includes(traj)) return '../../page/img/platform_pullhook_5.jpg';
	// TODO: Push / Push Slice / Pull / Push Hook 等其他軌跡類型，加靜態圖後可在此擴充
	return null;
}

// ============== 第一頁渲染 ==============
function renderPage1() {
	document.getElementById('pdf-val-distance').innerText = fmtNum(userShotData.distance);
	document.getElementById('pdf-val-ballSpeed').innerText = fmtNum(userShotData.ballSpeed);
	document.getElementById('pdf-val-clubSpeed').innerText = fmtNum(userShotData.clubSpeed);
	document.getElementById('pdf-val-backSpin').innerText = fmtNum(userShotData.backSpin);
	document.getElementById('pdf-val-launchAngle').innerText = fmtNum(userShotData.launchAngle);

	document.getElementById('pdf-ballscore').innerText = calcBallScore(userShotData.distance, userShotData.launchDirection);

	const trajImg = resolveTrajectoryImage(PDF_DATA.trajectory);
	const trajContainer = document.getElementById('pdf-trajectory-container');
	if (trajImg) {
		trajContainer.innerHTML = '<img src="' + trajImg + '" alt="軌跡">';
	} else if (PDF_DATA.trajectory) {
		trajContainer.innerHTML = '<div style="color:#1a3a6e;font-size:14px;font-weight:700;text-align:center;">'
			+ '<div style="font-size:11px;color:#888;margin-bottom:4px;">軌跡類型</div>'
			+ PDF_DATA.trajectory + '</div>';
	} else {
		trajContainer.innerHTML = '<span style="color:#888;font-size:12px;">無軌跡資料</span>';
	}

	const radarCanvas = document.getElementById('pdf-radarChart');
	if (radarCanvas && typeof initializeRadarChart === 'function') {
		try { initializeRadarChart(radarCanvas, userShotData, PDF_DATA.expertLevels, { pdfMode: true }); }
		catch (e) { console.warn('radar render error:', e); }
	}
}

// ============== 第二頁 — TPI ==============
function renderTpi() {
	const body = document.getElementById('pdf-tpi-body');
	body.innerHTML = '';

	let advices = null;
	try {
		const raw = PDF_DATA.tpiAdvicesJson;
		if (raw && raw !== 'null' && raw.trim() !== '') {
			advices = JSON.parse(raw);
		}
	} catch (e) { console.warn('TPI parse error:', e); }

	const phases = [
		{ key: 'A', title: 'A 準備', effect: PDF_DATA.aEffect },
		{ key: 'T', title: 'T 上桿', effect: PDF_DATA.tEffect },
		{ key: 'I', title: 'I 下桿', effect: PDF_DATA.iEffect },
		{ key: 'F', title: 'F 收桿', effect: PDF_DATA.fEffect }
	];

	let totalItems = 0;
	phases.forEach(ph => {
		const items = (advices && Array.isArray(advices[ph.key])) ? advices[ph.key] : [];
		totalItems += items.length;
		const phaseDiv = document.createElement('div');
		phaseDiv.className = 'pdf-tpi-phase';
		const titleHtml = '<div class="pdf-tpi-phase-title">' + ph.title + '</div>';
		if (items.length === 0) {
			const okMsg = (parseInt(ph.effect, 10) >= 6)
				? '動作與 TPI 標準吻合，協調性與穩定性表現出色。'
				: '動作大致良好，與教練略有不同。';
			phaseDiv.innerHTML = titleHtml + '<div class="pdf-tpi-empty-msg">' + okMsg + '</div>';
		} else {
			let inner = titleHtml;
			items.forEach(it => {
				inner += '<div class="pdf-tpi-item">'
					+ '<div class="tpi-title">' + (it.title || '') + '</div>'
					+ '<div class="tpi-posture"><b>特徵：</b>' + (it.posture || '') + '</div>'
					+ '<div class="tpi-suggestion"><b>建議：</b>' + (it.suggestion || '') + '</div>'
					+ '</div>';
			});
			phaseDiv.innerHTML = inner;
		}
		body.appendChild(phaseDiv);
	});

	return { totalItems: totalItems };
}

// ============== 第二頁 — 擊球穩定性 ==============
function renderStability() {
	const container = document.getElementById('pdf-stability-body');
	let parsed = null;
	try {
		const raw = PDF_DATA.shortGameResult;
		if (raw && raw.trim() !== '' && raw.trim() !== 'null') {
			parsed = JSON.parse(raw);
		}
	} catch (e) { console.warn('shortGame parse error:', e); }

	if (!parsed || parsed.status !== 'success') {
		container.innerHTML = '<div class="pdf-section-empty">擊球資料不足，需要更多歷史擊球紀錄才能繪製穩定性分佈。</div>';
		return { ok: false };
	}

	// 建一個臨時容器，模擬 shortTableManager 的 DOM 預期
	container.innerHTML =
		'<div class="pdf-stability">' +
			'<div class="pdf-stability-chart" id="pdf-shotAnalysisBox">' +
				'<div id="shotDispersionMap" style="width:100%;height:240px;"></div>' +
				'<div id="shotStatsTable" style="display:none;"></div>' +
			'</div>' +
			'<div class="pdf-stability-table" id="pdf-stability-summary"></div>' +
		'</div>';

	try {
		const mgr = new ShortTableManager('pdf-shotAnalysisBox', { pdfMode: true });
		mgr.updateTable(PDF_DATA.shortGameResult);
	} catch (e) {
		console.warn('stability render error:', e);
		container.innerHTML = '<div class="pdf-section-empty">擊球穩定性渲染失敗。</div>';
		return { ok: false };
	}

	// 簡單統計表格（用 parsed.data 內容）
	try {
		const d = parsed.data || parsed;
		const summaryEl = document.getElementById('pdf-stability-summary');
		const rows = [
			['平均擊球距離', (d.avg_total_yd != null ? d.avg_total_yd.toFixed(1) + ' y' : '—')],
			['前後分散', (d.stdev_carry_yd != null ? d.stdev_carry_yd.toFixed(1) + ' y' : '—')],
			['左右分散', (d.stdev_horizontal_yd != null ? d.stdev_horizontal_yd.toFixed(1) + ' y' : '—')],
			['樣本數', (d.sample_count != null ? d.sample_count : '—')]
		];
		summaryEl.innerHTML = '<table>'
			+ '<thead><tr><th colspan="2">統計摘要</th></tr></thead>'
			+ '<tbody>' + rows.map(r => '<tr><td>' + r[0] + '</td><td>' + r[1] + '</td></tr>').join('') + '</tbody>'
			+ '</table>';
	} catch (e) { /* ignore summary failure */ }

	return { ok: true };
}

// ============== 影片截圖 — 4 幀 x 兩部影片 ==============
const FRAME_LABELS = ['A', 'T', 'I', 'F'];
const FRAME_RATE = 60;

function waitForLoadedMetadata(video) {
	return new Promise((resolve, reject) => {
		if (video.readyState >= 1) return resolve();
		const onLoaded = () => { cleanup(); resolve(); };
		const onErr = (e) => { cleanup(); reject(e); };
		const cleanup = () => {
			video.removeEventListener('loadedmetadata', onLoaded);
			video.removeEventListener('error', onErr);
		};
		video.addEventListener('loadedmetadata', onLoaded);
		video.addEventListener('error', onErr);
		setTimeout(() => { cleanup(); reject(new Error('loadedmetadata timeout')); }, 15000);
	});
}

function seekVideo(video, timeSec) {
	return new Promise((resolve, reject) => {
		const onSeeked = () => { cleanup(); resolve(); };
		const onErr = (e) => { cleanup(); reject(e); };
		const cleanup = () => {
			video.removeEventListener('seeked', onSeeked);
			video.removeEventListener('error', onErr);
		};
		video.addEventListener('seeked', onSeeked);
		video.addEventListener('error', onErr);
		try { video.currentTime = timeSec; }
		catch (e) { cleanup(); reject(e); return; }
		setTimeout(() => { cleanup(); reject(new Error('seek timeout')); }, 8000);
	});
}

function drawOverlayOnly(video, canvas, ctx, swingPlaneData, isSideView) {
	if (!swingPlaneData || !swingPlaneData.data) return;
	const d = swingPlaneData.data;
	if (d.bbox)      drawBoundingBoxForVideo(d.bbox, video, canvas, ctx, 'rgba(255,255,255,1)');
	if (d.club)      drawLineForVideo(d.club, video, canvas, ctx, 'rgba(255,153,51,1)');
	if (d.shoulder)  drawLineForVideo(d.shoulder, video, canvas, ctx, 'rgba(255,153,51,1)');
	if (d.left_leg)  drawLineForVideo(d.left_leg, video, canvas, ctx, 'rgba(255,153,255,1)');
	if (d.right_leg) drawLineForVideo(d.right_leg, video, canvas, ctx, 'rgba(255,153,255,1)');
	if (d.head)      drawHeadForVideo(d.head, video, canvas, ctx, 'rgba(255,153,255,1)', isSideView);
}

async function captureFrames(video, frames, swingPlaneData, isSideView) {
	await waitForLoadedMetadata(video);
	const w = video.videoWidth || 720;
	const h = video.videoHeight || 1280;
	const out = [];
	for (let i = 0; i < frames.length; i++) {
		const fn = frames[i];
		try {
			await seekVideo(video, fn / FRAME_RATE);
			// 留個 frame buffer，避免某些瀏覽器 seek 完還沒解碼好
			await new Promise(r => requestAnimationFrame(() => r()));
			const cv = document.createElement('canvas');
			cv.width = w;
			cv.height = h;
			const c = cv.getContext('2d');
			c.drawImage(video, 0, 0, w, h);
			drawOverlayOnly(video, cv, c, swingPlaneData, isSideView);
			out.push(cv.toDataURL('image/jpeg', 0.85));
		} catch (e) {
			console.warn('frame capture error @ idx', i, e);
			out.push(null);
		}
	}
	return out;
}

function buildFrameRowHtml(label, dataUrls) {
	let html = '<div class="pdf-frames-row-label">' + label + '</div>';
	html += '<div class="pdf-frames-row">';
	for (let i = 0; i < 4; i++) {
		const src = dataUrls[i];
		const cellInner = src
			? '<img src="' + src + '" alt="' + FRAME_LABELS[i] + '">'
			: '<span style="color:#888;font-size:10px;">無資料</span>';
		html += '<div class="pdf-frame-cell">'
			+ '<span class="frame-label">' + FRAME_LABELS[i] + '</span>'
			+ cellInner + '</div>';
	}
	html += '</div>';
	return html;
}

async function renderVideoFrames() {
	const body = document.getElementById('pdf-frames-body');
	const banner = document.getElementById('pdf-frames-banner');
	const hasFront = PDF_DATA.frontExpected && PDF_DATA.frontAnalyzReady;
	const hasSide  = PDF_DATA.sideExpected  && PDF_DATA.sideAnalyzReady;

	if (!hasFront && !hasSide) {
		body.innerHTML = '<div class="pdf-section-empty">本次擊球未提供影片資料</div>';
		return { hasAny: false };
	}
	if (hasFront !== hasSide) {
		const missing = !hasFront ? '正面' : '側面';
		banner.innerHTML = '<div class="pdf-section-banner">⚠ ' + missing + '影片缺失，僅顯示另一邊分析結果</div>';
	}

	body.innerHTML = '<div class="pdf-section-empty">擷取影像中...</div>';

	const tasks = [];
	if (hasFront) {
		const v = document.getElementById('pdfVideoFront');
		tasks.push(captureFrames(v, PDF_DATA.frontFrames, PDF_DATA.frontSwingPlane, false)
			.then(arr => ({ side: 'front', frames: arr })));
	}
	if (hasSide) {
		const v = document.getElementById('pdfVideoSide');
		tasks.push(captureFrames(v, PDF_DATA.sideFrames, PDF_DATA.sideSwingPlane, true)
			.then(arr => ({ side: 'side', frames: arr })));
	}

	let results;
	try {
		results = await Promise.all(tasks);
	} catch (e) {
		console.warn('captureFrames overall error:', e);
		body.innerHTML = '<div class="pdf-section-empty">影像擷取失敗</div>';
		return { hasAny: false };
	}

	const front = results.find(r => r.side === 'front');
	const side  = results.find(r => r.side === 'side');

	let html = '';
	if (front) html += buildFrameRowHtml('正面（Front）', front.frames);
	if (side)  html += buildFrameRowHtml('側面（Side）', side.frames);
	body.innerHTML = html;

	const anyFrame = (front && front.frames.some(Boolean)) || (side && side.frames.some(Boolean));
	return { hasAny: anyFrame };
}

// ============== 主流程 ==============
async function runRender() {
	renderPage1();
	const tpiResult = renderTpi();
	const stabilityResult = renderStability();
	const framesResult = await renderVideoFrames();

	// Edge case: 第二頁三塊全部空 → 標記移除
	const page2HasContent = framesResult.hasAny || tpiResult.totalItems > 0 || stabilityResult.ok;
	if (!page2HasContent) {
		document.getElementById('pdf-page-2').classList.add('is-hidden');
	}

	const payload = {
		type: 'pdf-ready',
		recordId: PDF_DATA.recordId,
		nickname: PDF_DATA.nickname,
		hasPage2: page2HasContent
	};

	// iframe 模式：通知父視窗
	if (PDF_DATA.isEmbed && window.parent !== window) {
		try { window.parent.postMessage(payload, '*'); } catch (e) { console.warn(e); }
		return;
	}

	// standalone 模式：自己跑 html2canvas + jsPDF（Phase D 後段實作）
	console.log('Standalone mode pdf-ready:', payload);
}

document.addEventListener('DOMContentLoaded', runRender);
</script>

</body>
</html>

<%!
private static String toJsString(String s) {
	if (s == null) return "''";
	StringBuilder sb = new StringBuilder("'");
	for (int i = 0; i < s.length(); i++) {
		char c = s.charAt(i);
		switch (c) {
			case '\'': sb.append("\\'"); break;
			case '\\': sb.append("\\\\"); break;
			case '\n': sb.append("\\n"); break;
			case '\r': sb.append("\\r"); break;
			case '<':  sb.append("\\u003c"); break;
			case '>':  sb.append("\\u003e"); break;
			default:   sb.append(c);
		}
	}
	sb.append("'");
	return sb.toString();
}
%>