<%--
  expert-data-v8-putt.jsp — 推桿教練端頁面。

  規劃文件：docs/expert-data-v8-putt-plan.md（§2 版面、§3 實作清單）
  ⛔ 檔名是 -putt.jsp，⛔ 不是 -putting.jsp（既有導覽鈕已經指這個名字）。

  ═══ 這一頁只放什麼 ═══
  ⭐ 版面骨架 ＋ 不會隨每一推改變的固定標籤 ＋ 後端的值 ＋ 把值交給各模組的接線。
  ⛔ 會隨每一推改變的文字與數值、以及任何判斷邏輯⛔ 一律不寫在這裡，由各功能模組負責：
       page/js/puttPanelManager.js        界標列、右欄數值面板、詳細數值面板（含界標推導）
       page/js/puttingIssuesManager.js    推桿風險回饋、綜合評價
       page/js/puttShotDataManager.js     影片下方：擊球數據卡片 ⇄ 回饋切換
       page/js/puttConsistencyManager.js  推桿穩定度圖
       page/js/puttVideoManager.js        影片跳幀、跳段、播放控制、影片輪詢換片
  後端：PuttingData（界標兩欄 ＋ 判定結果）、PuttingShotData（擊球數據）、PuttingIssueTip（建議文字）。
--%>
<%@ page import="org.json.JSONObject"%>

<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page import="com.golfmaster.service.PuttingShotData"%>
<%@ page import="com.golfmaster.service.PuttingIssueTip"%>
<%@ page import="com.golfmaster.service.PuttingData"%>
<%@ page import="com.golfmaster.service.PuttingDemoVideo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%-- Java Parameters --%>
<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>
<%!PuttingShotData puttingShotData = new PuttingShotData();%>
<%!PuttingIssueTip puttingIssueTip = new PuttingIssueTip();%>
<%!PuttingData puttingData = new PuttingData();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");

/* ── 最新一推輪詢的設定（交給 latestShotPollManager.js）────────────────
 * lid：網址的 LID，沒帶就不輪詢
 * follow：'0' 只顯示提示，其餘自動換到最新一推
 * sec：幾秒問一次，沒帶用預設值
 * ──────────────────────────────────────────────────────────────── */
JSONObject latestPollCfg = new JSONObject();
/* 網址同時帶 expert 與 LID 時，ExpertData 只認 expert、LID 會被忽略；
   這時若還輪詢，偵測到新一桿也只會重載成同一桿 —— 所以有 expert 就不輪詢。 */
latestPollCfg.put("lid", (request.getParameter("expert") != null || request.getParameter("LID") == null)
		? "" : request.getParameter("LID"));
latestPollCfg.put("follow", request.getParameter("follow") == null ? "1" : request.getParameter("follow"));
latestPollCfg.put("sec", request.getParameter("sec") == null ? "" : request.getParameter("sec"));
latestPollCfg.put("currentShotId", shot_data_id == null ? "" : String.valueOf(shot_data_id));

// ⭐ 影片路徑與分析完成旗標沿用切桿頁的來源（ShotVideo.processAnalyz）。
// ⛔ 這裡取得的 A/T/I/F 是「揮桿」的分期，⛔ 不是推桿的界標，所以本頁不取用。
//    推桿界標要讀 shot_video_swing.PuttingPhases，等 Core 的 fixture 到了才接（§6.2 第 11 項）。
// ⛔ 也⛔ 不在 ShotVideo.java 上加推桿的 join —— 揮桿與切桿都在用它，
//    推桿之後自己查自己的（PuttingData.java，§6.3 第 14 項）。
Object temp[] = shotVideo.processAnalyz(shot_data_id);
String frontVideoPath = (String) temp[2];
String sideVideoPath = (String) temp[3];
boolean frontAnalyzReady = (boolean) temp[12]; // 正面影像分析是否完成 (id_analyzeVideo_front)
boolean sideAnalyzReady = (boolean) temp[13];  // 側面影像分析是否完成 (id_analyzeVideo_side)
boolean frontExpected = (boolean) temp[14];    // 廠商會送 front 影片 (raw_shotVideo_front)
boolean sideExpected = (boolean) temp[15];     // 廠商會送 side 影片 (raw_shotVideo_side)

/* 推桿頁要播哪兩支影片：ShotVideo 退回的是三頁共用的示範片，
 * 這裡換成推桿專用的那一支（判斷全部在 PuttingDemoVideo，⛔ 不寫在這裡）。
 * frontDemoView／sideDemoView：那一格播的是哪一個視角的示範片（""＝不是示範片）。 */
JSONObject puttVideos = PuttingDemoVideo.resolveVideos(frontVideoPath, sideVideoPath);
frontVideoPath = puttVideos.getString("frontPath");
sideVideoPath = puttVideos.getString("sidePath");
String frontDemoView = puttVideos.getString("frontDemoView");
String sideDemoView = puttVideos.getString("sideDemoView");

// shot_data（E6 模擬器量到的）：右欄的球速、〔詳細數值〕的球速原因、影片下方的擊球數據卡片。
// ⛔ 球速算不出來或不可信時那個鍵不存在（⛔ 不是填 "—"、⛔ 不是填 0）→ JS 讓整列不出現。
JSONObject puttValues = puttingShotData.processPuttValues(shot_data_id);

// 推桿穩定度圖：同一個 Player、同一支球桿的最近幾推
int puttConsistencyMaxRecords = 10;         // 撈幾推
boolean puttConsistencySameLidOnly = false; // true：只撈同一個 LID
JSONObject puttConsistency = puttingShotData.processPuttConsistency(
		shot_data_id, puttConsistencyMaxRecords, puttConsistencySameLidOnly);

// ⭐ 建議文字改從資料庫查（工項 15）—— putting_issue_tip，18 條一次撈完讀進記憶體快取，
//    ⛔ 不是一張卡片查一次資料庫。
// ⚠️ 這一頁目前**沒有教練身分**這個概念（整個專案都沒有）→ 傳 null，只會拿到 default 那一組。
//    ⭐ PuttingIssueTip 的介面已經吃教練代號了，之後有了⛔ 不必改這一行以外的東西。
// ⚠️⚠️ R1：正式機還沒有這張表 → 那時這裡會是空的，⭐ 卡片照樣出現、只是沒有那段文字
//    （規劃 §3.2）。⛔ 絕不可以讓它偷偷退回 js 檔裡那 18 條 —— 那樣「沒灌資料」會看起來完全正常。
JSONObject puttTips = puttingIssueTip.tipsFor(null);

// 推桿列的界標兩欄原始字串 ＋ 判定結果（putting_issue 沒有這一列時 issues 是 null）
// ⛔ 這裡不做任何判斷，可信度與要不要顯示全部在 JS。
JSONObject puttAnalysis = puttingData.processPutting(shot_data_id);
%>

<%-- HTML --%>
<!DOCTYPE html>
<html lang="zh-Hant">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="../../page/css/GM08_putt.css" rel="stylesheet" type="text/css">
	<link href="../../page/css/GM08_pdf_modal.css" rel="stylesheet" type="text/css">
	<title>Expert - Putting</title>
	<%-- inner js --%>
	<script src="../../page/js/videoPollManager.js"></script>
	<script src="../../page/js/swingVideo.js"></script>
	<script src="../../page/js/headerNavManager.js"></script>
	<script src="../../page/js/chart_4_4_0.umd.min.js"></script>
	<script src="../../page/js/puttPanelManager.js"></script>
	<script src="../../page/js/puttingIssuesManager.js"></script>
	<script src="../../page/js/puttShotDataManager.js"></script>
	<script src="../../page/js/puttConsistencyManager.js"></script>
	<script src="../../page/js/puttVideoManager.js"></script>
	<script src="../../page/js/latestShotPollManager.js"></script>
	<script src="../../page/js/lib/html2canvas.min.js"></script>
	<script src="../../page/js/lib/jspdf.umd.min.js"></script>
	<script src="../../page/js/lib/qrcode.min.js"></script>
</head>
<body>
	<div class="c_m">
		<div class="header">
			<img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
			<div class="navigation-buttons">
				<button class="nav-button" id="nav-swing" data-page="expert-data-v8.jsp">
					<img src="../../page/img/swing_icon.png" alt="揮桿分析">
				</button>
				<button class="nav-button" id="nav-chip" data-page="expert-data-v8-short.jsp">
					<img src="../../page/img/chip_icon.png" alt="切桿分析">
				</button>
				<%--
				  ⚠️ 本頁自己這顆⛔ 不加 temporarily-disabled：headerNavManager.js 會比對
				     data-page 與當前檔名，自動把它設成 current-page（最亮、不可點）。
				--%>
				<button class="nav-button" id="nav-putt" data-page="expert-data-v8-putt.jsp">
					<img src="../../page/img/putt_icon.png" alt="推桿分析">
				</button>
				<button class="pdf-download-btn" id="btn-download-pdf" type="button" title="下載紀念 PDF">
					下載 PDF
				</button>
			</div>
		</div>

		<div class="main-layout-container">
			<%-- ══════════ 左欄：這一推怎麼動的 ══════════ --%>
			<div class="main-left-column">

				<%--
				  影片區（§2.8）
				  正面與側面並排，兩支都播 —— ⭐ 側面照播，有些教練會用到，
				  只是本期沒有側面的偵測模組。
				  ⛔ 側面不寫「不支援」「不適用」那種像壞掉的字。
				--%>
				<div class="video-section-top">
					<div id="videoContainer" style="position: relative;" class="image_v">
						<video id="myvideo" controls muted>
							<source src="<%=frontVideoPath%>" type="video/mp4" />
						</video>
						<canvas id="overlayCanvas"></canvas>
					</div>
					<div id="videoContainer1" style="position: relative;" class="image_v">
						<video id="myvideo1" controls muted>
							<source src="<%=sideVideoPath%>" type="video/mp4" />
						</video>
						<canvas id="overlayCanvas1"></canvas>
					</div>
				</div>

				<%--
				  界標列（§2.3）— 幀號由 puttPanelManager.setMarks() 填
				  ⭐ 結構與樣式完全比照 expert-data-v8.jsp / expert-data-v8-short.jsp 的階段列，
				     class 名稱也沿用。⛔ 鈕上不放中文：A=架桿、T=頂點、I=碰球、F=收桿。
				  ⚠️⚠️ 四顆鈕**永遠都在、位置固定**，⛔ 絕不可以塌成連續幾顆 ——
				       塌陷會讓「頂點抓不到」被讀成「這一推沒有上桿頂點」，
				       畫面看起來合理但意思是錯的。
				  ⚠️ 不可信的那顆：**外觀完全不變**，只是點了⛔ 不跳
				     （user 2026-09-10 兩次裁示：消失＝像 bug、調暗＝屏蔽按鈕，都不要）。
				  ⚠️⚠️ ⛔ 但它絕對不可以真的跳過去：界標找不到時填的值是**合法幀號**，
				       ⛔ seek 不會拋例外、只會安靜跳到錯的位置。
				  ⛔ 這六個 .step 格子是版面本體，⛔ 不可以由 JS 增刪。
				  ⚠️ 起桿（onset）⛔ 不做成按鈕，值留在 manager 裡供跳段用。
				--%>
				<div class="steps-bar-top">
					<div class="steps" id="puttMarks">
						<div class="step">
							<span class="step_title">Putt</span><br> <span class="step_4">4 Steps</span>
						</div>
						<div class="step">
							<button class="stepbutton" type="button" data-phase="A">A</button>
						</div>
						<div class="step">
							<button class="stepbutton" type="button" data-phase="T">T</button>
						</div>
						<div class="step">
							<button class="stepbutton" type="button" data-phase="I">I</button>
						</div>
						<div class="step">
							<button class="stepbutton" type="button" data-phase="F">F</button>
						</div>
						<div class="step">
							<div id="player-container">
								<div id="play-pause" class="play">Play</div>
							</div>
						</div>
					</div>
					<%--
					  界標提示（user 2026-09-10 裁示）
					  ⚠️ 不可信的那顆被**點下去**時才出現一行字，⛔ 不是常駐的品質標示，
					     ⛔ 也不是把鈕標成不可用 —— 鈕的外觀仍然完全不變。
					  ⭐ 為什麼要有它：不跳是對的（值是合法幀號，跳過去只會安靜跳到錯的位置），
					     ⛔ 但沉默會被讀成「頁面壞了」——§1.3 第 9 點已經栽過一次。
					  ⛔ 這一行的字由 puttPanelManager 產生，⛔ 不要寫在這裡。
					--%>
					<div class="putt-mark-hint hidden-element" id="puttMarkHint"></div>
				</div>

				<%--
				  回饋面板：綜合評價 ＋ 推桿風險。
				  ⭐ 白底、藍標題、深灰內文 —— 配色比照 v8 / v8-short 的建議區。
				  ⛔ 不要改回黑底配灰字：對比不足，而這一塊正是教練要讀的東西。

				  ⚠️⚠️ 這一塊放在左欄、影片正下方（2026-09-09 user 裁示，⛔ 與規劃 §2.1 的圖不同）：
				       · 左欄有 720px 寬，20px 的文案一行約 34 字；擠在 500px 的右欄只有 22 字，
				         行數多一倍、⛔ 一定會捲。
				       · 卡片就在影片正下方，「▶ 看這一段」點下去影片就在上面 ——
				         ⭐ §2.6 那個核心互動反而更順。
				  ⛔ 不要搬回右欄。
				--%>
				<%--
				  ⭐ 這一塊比照 expert-data-v8.jsp / -short.jsp：擊球數據卡片 ⇄ 回饋。
				     預設是卡片（data-mode）；判定完成時由 JS 切成回饋。
				  ⚠️ 卡片的值與切換由 puttShotDataManager.js 負責，沒有值顯示「--」。
				  ⛔ 球速⛔ 不放卡片：它在右欄數值面板，同一個數字⛔ 不可以出現兩次。
				--%>
				<div class="putt-feedback data-mode" id="puttFeedback">
					<button class="motion-toggle-btn" id="puttFeedbackToggle" type="button" title="切換顯示">&#x21C4;</button>

					<div class="putt-shot-cards" id="puttShotCards">
						<div class="card">
							<div class="title">推桿距離</div>
							<div class="unit">ft</div>
							<div class="number" data-card-key="distToPinFt">--</div>
						</div>
						<div class="card">
							<div class="title">出球方向</div>
							<div class="unit">°</div>
							<div class="number" data-card-key="launchDirection">--</div>
						</div>
						<div class="card">
							<div class="title">發射角度</div>
							<div class="unit">°</div>
							<div class="number" data-card-key="launchAngle">--</div>
						</div>
						<div class="card">
							<div class="title">擊球效率</div>
							<div class="unit"></div>
							<div class="number" data-card-key="smashFactor">--</div>
						</div>
					</div>

					<%--
					  推桿風險（§2.5）— 標籤與內容都由 puttingIssuesManager 填
					  ⚠️ 標題是「推桿風險」⛔ 不是「推桿問題」（PM 2026-09-09 指正）：
					     這五類的判定會隨推桿距離、握桿方式、姿勢而不同，
					     講「問題」等於宣稱一個這一頁撐不住的確定性。
					     ⭐ 這跟規劃 §0.2 是同一件事 ——「這一頁提供的是風險，
					     ⛔ 不是精密量測值」。⛔ 不要改回「問題」。
					  ⭐ 一次只顯示一類，用標籤切換。標籤上用 ●（有問題）／○（正常）／
					     ⊘（不適用）標三態 —— ⚠️ 五類仍然全部在畫面上、全部可點，
					     符合 §2.5「其餘收合，但都留在畫面上、都可點開」。
					  ⛔ 標籤數不寫死：跑 issues 陣列，之後加第六類會自動長出來。
					  ⛔ 這裡⛔ 不要寫任何一個標籤或一張卡片的 HTML。
					--%>
					<div class="putt-issues">
						<%-- ⚠️ ●／○／⊘ 三個記號要有說明，⛔ 不要讓教練自己猜。
						     說明放在標籤列右端，⛔ 不另外佔一行高度。
						     ⚠️ 這裡⛔ 沒有「推桿風險」標題 —— 內容區每一類自己會出
						     .p_de_title（跟 short 的建議區一樣），⛔ 不要再加一層標題。 --%>
						<%-- ⚠️ 標籤與右端的 ●／○／⊘ 說明都由 puttingIssuesManager 一起產生。
						     ⛔ 不要在這裡寫靜態的說明 —— 會被 innerHTML 蓋掉。 --%>
						<div class="putt-issue-tabs" id="puttIssueTabs"></div>
						<div class="putt-issue-panel" id="puttIssuePanel"></div>
					</div>

				</div>
			</div>

			<%-- ══════════ 右欄：哪裡要改 ══════════ --%>
			<%-- ⭐ 左欄回答「這一推怎麼動的」，右欄回答「哪裡要改」。
			     ⛔ 兩邊不對同一件事各給一個答案。 --%>
			<div class="main-right-column">
				<div class="analysis-box">

					<%--
					  綜合評價（§2.7）
					  ⚠️⚠️ 放在右欄總覽區最上方（2026-09-10）：
					       右欄 ＝ 總覽（這幾推穩不穩 ＋ 這一推的總評），
					       左欄 ＝ 這一推的細節（哪一類、怎麼改）。
					       ⛔ 不要搬回左欄 —— 它那一列會把回饋文字擠到要捲。
					  ⭐ 值直接讀 Core 的 overall 欄，⛔ 頁面不自己算規則。
					  ⛔ overall = null → 不顯示評價（⛔ 絕不可顯示成「穩定」）。
					  ⛔ 不顯示任何分數或百分比。
					  ⚠️ 這裡是主畫面上唯一保留品質標示的地方，⛔ 其他地方全部不放。
					  ⚠️ Core 的綜合回饋文字之後補進這一塊，版面不動；
					     目前沒有那段文字 → ⛔ 不留空框、⛔ 不寫「尚未提供」。
					--%>
					<%--
					  ⚠️⚠️ 2026-09-10 PM 指示：**先隱藏這一塊**。
					       理由：「風險」這個講法太重 —— 綜合評價是整支影片唯一的總判，
					       在沒有進洞結果、overall 也還沒交的情況下，那個詞宣稱得太滿。
					  ⛔ 只加 hidden-element，⛔ 不要把這一塊或 renderOverall() 刪掉：
					     §2.7 定案這裡是主畫面上**唯一**保留品質標示的地方，
					     Core 的 overall 一到就要開回來，拿掉 hidden-element 一行就好。
					  ⚠️ 顏色已經改成深色底用的（原本從白底左欄搬過來時是深灰，黑底上看不見）。
					--%>
					<div class="putt-overall hidden-element" id="puttOverall">
						<div class="putt-overall-label">綜合評價</div>
						<div class="putt-overall-value"></div>
						<div class="putt-overall-reason hidden-element"></div>
						<div class="putt-overall-quality hidden-element"></div>
					</div>

					<%--
					  最近 N 推的一致性（§2.10）
					  ⭐ 圖上⛔ 不出現任何絕對距離數字 —— 模擬器估的推桿距離不準，
					     但「彼此散得多開」可信。這張圖回答「這個人穩不穩」，
					     ⛔ 不是「這一推準不準」。
					  ⛔ 不做雷達圖、⛔ 不做曲線球、⛔ 不顯示後旋／側旋／擊球效率／飛行距離。
					  ⚠️ 標題與圖說由 manager 填（撈幾推是參數，⛔ 不寫死）。
					  ⭐ 圖由 puttConsistencyManager.js 用 Chart.js 畫：同一個 Player、同一支球桿的最近幾推。
					--%>
					<div class="putt-consistency" id="puttConsistency">
						<p class="box-title"></p>
						<div class="putt-consistency-map">
							<%-- 內容由 puttConsistencyManager.js 畫 --%>
						</div>
						<div class="putt-consistency-legend"></div>
					</div>

						<%--
						  數值面板 ⇄ 詳細數值面板（§2.4、§2.9）
						  ⚠️ 這個插槽高度固定；詳細數值那塊內部捲動，⛔ 不要把版面撐開。
						--%>
						<div class="putt-panel-slot">

							<%--
							  數值列（§2.4）
							  ⭐ 結構與 class 完全比照 GM08_short.css 的右欄統計表
							     （切桿頁的 .stats-table / .stat-row）：標籤靠左、數值靠右。
							     ⚠️ 卡片（.data-panel .card）是 short 的**左下**慣例；
							     數值搬到右欄之後就該跟右欄的慣例走。⛔ 不要改回卡片：
							     右欄只有 500px 寬，第二階段變四格時卡片會擠爆。
							  ⭐ 單位寫在標籤的括號裡，跟 short 一樣（例：擊球距離 (yds)）。
							  ⭐ 整塊語意一致，全部是秒與比值，⛔ 一個判定都不混進來。
							  ⚠️ 第二階段（onset 入庫後）這裡會多出上桿時間與下桿時間兩列，
							     ⛔ 版面不用再調，多兩列就是多兩列。
							  ⛔ 不可信的那一列⛔ 不出現（⛔ 不是顯示「—」）。
							--%>
							<div class="putt-value-panel" id="puttValuePanel">
								<button class="motion-toggle-btn" id="puttPanelToggle" type="button" title="切換顯示">&#x21C4;</button>
								<div class="stats-table">
									<div class="stat-row" data-value-key="tempoRatio">
										<span class="stat-label">節奏比 (上桿:下桿)</span>
										<span class="stat-value"></span>
									</div>
									<div class="stat-row" data-value-key="totalDuration">
										<span class="stat-label">總時長 (秒)</span>
										<span class="stat-value"></span>
									</div>
									<%--
									  球速（user 2026-09-10 指定）
									  ⭐ 來源是 shot_data.BallSpeed —— E6 模擬器**碰球瞬間量到**的值，
									     ⛔ 不是模擬器滾出來的結果，所以站得住（§4.7）。
									  ⚠️ 這一列的資料⛔ 不歸 Core 管，⛔ 不要問他 ——
									     shot_data 是 E6 送進資料庫的，跟影像分析是兩條線。
									  ⛔ 已經評估後**不放**的：
									     · 揮桿路徑 ClubAnglePath —— ⚠️ 實測 10052 筆推桿有 9911 筆是 0，
									       E6 根本沒送這一欄。
									     · 面路差（桿面角 − 揮桿路徑）—— ⛔ 減數是空的，
									       算出來會**恰好等於桿面角**卻被標成「面路差」，
									       ⛔ 不報錯、⛔ 看起來完全正常。這正是 §0.3 擋的那一型。
									     · 桿面角 ClubAngleFace —— ⚠️ 在 LID 017/018（E6）有值，
									       ⛔ 但⛔ 不是每一台模擬器都給（user 2026-09-10）。
									       ⛔ 只驗過兩台就上，會在別的場館變成一排 0。
									  ⭐ 這一列**已經接上真資料**（工項 12b，2026-09-11）：
									     PuttingShotData.processPuttValues()，⛔ 沒有改 ShortGameData.java。
									     ⭐ 球桿名稱⛔ 沒有寫死 —— 從這一推自己那一列取
									     （017/018 存 'Putter'、1000 存 'P'，寫死其中一個另一台就撈不到）。
									  ⭐ 節奏比與總時長兩列由 derivePuttValues() 從分析結果推導，算不出來或不可信時整列不出現。
									--%>
									<div class="stat-row" data-value-key="ballSpeed">
										<span class="stat-label">球速 (mph)</span>
										<span class="stat-value"></span>
									</div>
								</div>
							</div>

							<%-- 詳細數值面板：⛔ 一次只出一類，⛔ 不要 23 列一次倒出來。
							     ⚠️ 分頁鈕本身要能反映狀態（.is-empty），否則點進去才發現是空的。 --%>
							<div class="putt-detail-panel" id="puttDetailPanel" style="display:none;">
								<button class="motion-toggle-btn" id="puttDetailToggle" type="button" title="切換顯示">&#x21C4;</button>
								<div class="putt-detail-head">
									<span class="putt-detail-caption">詳細數值</span>
									<%--
									  ⚠️⚠️ 分頁順序＝**擊球階段順序**，⛔ 不要改成右欄那種依風險排。
									       左右兩欄刻意用不同的排法：
									         · 右欄（推桿風險）依「哪裡要改」排 —— 有問題的在前
									         · 左欄（詳細數值）依「動作怎麼走」排 —— 架桿的在前
									       ⛔ 把左欄也改成依風險排，教練就沒有辦法照動作順序讀完一遍。

									  各類實際涵蓋的階段（看 metrics 的欄位名就知道）：
									    站姿    架桿那一幀（stance_gap_ratio 等，全部在架桿量）
									    球位    架桿那一幀（ball_position_pct）
									    位移    上桿、下桿（hip_lateral_shift.backswing / .downswing）
									    三角形  上桿、下桿、送桿、收桿（axis_shaft_angle_deg.* ／
									            shoulder_width_ratio.finish）—— ⚠️ 跨最多段
									    傾斜    ⛔ 沒有分段，整段相對架桿在看（midline_tilt_deg）→ 放最後
									  ⚠️「狀態」不是五類之一，永遠排第一個。
									--%>
									<div class="putt-detail-tabs">
										<button class="putt-detail-tab is-active" type="button" data-tab-key="status">狀態</button>
										<button class="putt-detail-tab" type="button" data-tab-key="stance">站姿</button>
										<button class="putt-detail-tab" type="button" data-tab-key="ball_position">球位</button>
										<button class="putt-detail-tab" type="button" data-tab-key="body_sway">位移</button>
										<button class="putt-detail-tab" type="button" data-tab-key="triangle">三角形</button>
										<button class="putt-detail-tab" type="button" data-tab-key="swing_angle">傾斜</button>
									</div>
								</div>
								<div class="putt-detail-body"></div>
							</div>
						</div>

					</div>

				</div>
			</div>
		</div>

	<script>
		// ===== 後端的值（由 org.json 產生，字串裡的斜線與引號已經跳脫）=====
		// ⛔ 這個區塊的註解⛔ 不可以把 JSP 運算式的符號原樣寫出來 —— Jasper 照樣會把它當成空的運算式去編譯，整頁 500。
		// ⚠️ frontAnalyzReady／sideAnalyzReady 這兩個名字 pdfDownloadManager.js 會直接讀，⛔ 不要改名、⛔ 不要搬進函式
		const frontAnalyzReady = <%= frontAnalyzReady %>;
		const sideAnalyzReady = <%= sideAnalyzReady %>;
		// 球速（沒有這個鍵＝不顯示）、球速不顯示的原因、擊球數據卡片
		const puttValuesData = <%= puttValues.toString() %>;
		// 界標兩欄的原始字串（正面與側面各一份）、判定物件（沒有判定結果是 null）
		const puttAnalysisData = <%= puttAnalysis.toString() %>;
		// 播的是不是這一推自己的影片；false＝退回示範影片，那一支的幀號跟這一推對不上
		const puttFrontIsOwnVideo = <%= puttVideos.getBoolean("frontIsOwn") %>;
		const puttSideIsOwnVideo = <%= puttVideos.getBoolean("sideIsOwn") %>;
		// 那一格換成推桿示範片時，放的是哪一個視角（''＝不是示範片）。
		// ⚠️ 單邊缺影片時頂上來的是**另一邊那支**示範片 → 格子與視角可能不一樣。
		const puttFrontDemoView = '<%= frontDemoView %>';
		const puttSideDemoView = '<%= sideDemoView %>';
		// ⚠️ 這兩個值本身就是 JSON，直接當物件輸出，⛔ 不要再用引號包成字串
		const puttDemoPhases = {
			front: <%= PuttingDemoVideo.demoPhases("front") %>,
			side: <%= PuttingDemoVideo.demoPhases("side") %>,
		};
		// 穩定度圖的最近幾推（新的在前）
		const puttConsistencyData = <%= puttConsistency.toString() %>;

		// ===== 各功能模組 =====
		const videoPoller = new VideoPollManager({
			statusUrl: 'VideoStatus',
			shotDataId: '<%= shot_data_id %>',
			interval: 3000,       // 每 3 秒檢查一次
			maxAttempts: 40,      // 最多 40 次（約 2 分鐘）
			initialDelay: 3000,   // 頁面載入後 3 秒開始檢查
		});

		/* ── 最新一推輪詢 ────────────────────────────────────────────
		 * 同一個廠商（LID）有新的一推時換過去。網址沒帶 LID 就不會輪詢。
		 * ─────────────────────────────────────────────────────────── */
		const latestShotPoll = new LatestShotPollManager(<%= latestPollCfg.toString() %>);

		const puttVideo = new PuttVideoManager({
			frontVideoId: 'myvideo', sideVideoId: 'myvideo1',
			frontCanvasId: 'overlayCanvas', sideCanvasId: 'overlayCanvas1',
			frontContainerId: 'videoContainer', sideContainerId: 'videoContainer1',
			playButtonId: 'play-pause',
		});

		const puttPanelManager = new PuttPanelManager({
			marksId: 'puttMarks',
			valuePanelId: 'puttValuePanel',
			detailPanelId: 'puttDetailPanel',
			markHintId: 'puttMarkHint',
			onSeekFrame: function (frame, key) { puttVideo.goToFrame(frame, key); },
		});

		const puttIssuesManager = new PuttingIssuesManager({
			tabsId: 'puttIssueTabs',
			panelId: 'puttIssuePanel',
			overallId: 'puttOverall',
			onSeekSegment: function (from, to, fromKey, toKey) {
				puttVideo.playSegment(from, to, fromKey, toKey);
			},
		});

		const puttShotData = new PuttShotDataManager({
			feedbackId: 'puttFeedback',
			cardsId: 'puttShotCards',
			toggleId: 'puttFeedbackToggle',
		});

		const puttConsistency = new PuttConsistencyManager({ consistencyId: 'puttConsistency' });

		// ===== 初始化：只負責把值依序交給各模組 =====
		function init() {
			// 沒帶 LID 時這一行不會做任何事
			latestShotPoll.start();

			puttVideo.bindControls();
			puttVideo.startPolling(videoPoller, {
				frontExpected: <%= frontExpected %>,
				sideExpected: <%= sideExpected %>,
				frontReady: frontAnalyzReady,
				sideReady: sideAnalyzReady,
			});

			puttConsistency.render(puttConsistencyData);

			// 數值面板 ⇄ 詳細數值面板；推導完成前先全部收起來
			puttPanelManager.setValues(null);
			puttPanelManager.setDetail(null);
			document.getElementById('puttPanelToggle')
				.addEventListener('click', function () { puttPanelManager.toggle(); });
			document.getElementById('puttDetailToggle')
				.addEventListener('click', function () { puttPanelManager.toggle(); });

			// 影片下方先顯示擊球數據卡片，判定結果畫完才決定要不要切成回饋
			puttShotData.setCards(puttValuesData.shotCards);
			puttShotData.showDefault(false);

			const phases = derivePuttPhases(puttAnalysisData.PuttingPhases, puttAnalysisData.PuttingTempo);
			// 側面那一列的界標：只給側面影片跳幀用，⛔ 不參與判定、⛔ 不進數值與狀態分頁
			const sidePhases = derivePuttPhases(
				puttAnalysisData.SidePuttingPhases, puttAnalysisData.SidePuttingTempo);
			const values = Object.assign({}, derivePuttValues(phases), {
				ballSpeed: puttValuesData.ballSpeed || null,
			});
			puttPanelManager.setValues(values);

			const issuesBase = Object.assign({}, PUTT_ISSUES_EMPTY_DATA, { tips: <%= puttTips.toString() %> });
			const issuesData = applyPuttJudgement(issuesBase, puttAnalysisData.issues);
			// ⚠️⚠️ 界標列、跳段、狀態分頁⛔ 一定吃同一份 derived（含收桿停在片尾的旗標）
			const derived = applyPuttFinishFlag(phases, issuesData.issues);
			// 哪一支影片配哪一份界標、鈕要跟著誰，全部由 derivePuttVideoSources() 決定
			const sources = derivePuttVideoSources({
				derived: derived,
				demo: puttDemoPhases,
				frontDemoView: puttFrontDemoView,
				sideDemoView: puttSideDemoView,
				frontIsOwnVideo: puttFrontIsOwnVideo,
				judgedView: puttAnalysisData.view,
			});
			puttVideo.setCameraLandmarks('front', sources.front, true);
			puttVideo.setCameraLandmarks('side', sources.side, true);
			puttPanelManager.setMarks(sources.marks.phases, sources.marks.trust);

			issuesData.phases = Object.assign({ onset: derived.onset, trust: derived.trust }, derived.phases);
			puttIssuesManager.render(issuesData);
			puttShotData.showDefault(puttJudgementComplete(issuesData.header));

			const summary = puttIssuesManager.buildDetailSummary();
			puttPanelManager.setDetail(Object.assign({
				status: buildPuttStatusGroup({
					header: issuesData.header || null,
					derived: derived,
					values: values,
					ballSpeedReason: puttValuesData.ballSpeedReason || '',
					marksFromDemo: sources.marksFromDemo,
					tips: summary.tips,
					classes: summary.classes,
				}),
			}, summary.groups));
		}

		document.addEventListener('DOMContentLoaded', init);
	</script>

	<%-- 紀念 PDF 下載 Modal (HTML / PDF_CONTEXT / pdfDownloadManager.js) --%>
	<% request.setAttribute("__pdfPageMode", "putt"); %>
	<%@ include file="pdf-modal-fragment.jspf" %>

</body>
</html>
