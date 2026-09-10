<%--
  expert-data-v8-putt.jsp — 推桿教練端頁面。

  規劃文件：docs/expert-data-v8-putt-plan.md（§2 版面、§3 實作清單）
  ⛔ 檔名是 -putt.jsp，⛔ 不是 -putting.jsp（既有導覽鈕已經指這個名字）。

  ═══ 這一頁只放什麼 ═══
  ⭐ 版面骨架 ＋ 標題 ＋ 不會隨每一推改變的固定標籤
     （節奏比、總時長、界標名、分頁名、綜合評價、推桿風險…）。
  ⛔ 會隨每一推改變的文字與數值⛔ 一律不寫在這裡，由兩支 manager 填：
       page/js/puttPanelManager.js     左欄下半：界標列、數值面板、詳細數值六分頁
       page/js/puttingIssuesManager.js 右欄：一致性、綜合評價、推桿風險
     ⚠️ 檔名照 feedback_impl_spec.md §5.3 的命名，⛔ 不要改成別的。

  ═══ 這一輪（§6.1 第 2 項）做到哪裡 ═══
  ⭐ 頁首、影片區、影片輪詢、PDF modal 抄自 expert-data-v8-short.jsp，是真的、會動的。
  ⚠️ 界標列、數值面板、卡片的內容全部來自兩支 manager 裡的假資料，只為了撐出版面。
  ⛔ 卡片邏輯（四組分派、排序、三態、展開規則、跳段判斷）這一輪⛔ 沒有寫，
     在 puttingIssuesManager.js 裡是標了 TODO 的空函式 —— ⛔ 不要因為畫面會動就以為做好了。
  ⛔ 這一輪也不接 PuttingData.java（還沒有這個類別）、
     ⛔ 不解除另外三個頁面 nav-putt 的 temporarily-disabled（那是 §6.1 第 10 項，一定放最後）。
--%>
<%@ page import="org.json.JSONObject"%>

<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%-- Java Parameters --%>
<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");

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
	<script src="../../page/js/puttPanelManager.js"></script>
	<script src="../../page/js/puttingIssuesManager.js"></script>
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
				  ⛔ 另外三個頁面（v8 / v8-short / v8-realtime）的 nav-putt 停用⛔ 不要在這一輪解除，
				     那是最後一個 session 的工項 —— ⛔ 半成品不開放給使用者點進來。
				     停用只是一個 CSS class，⛔ 沒有 JS 邏輯要動。
				--%>
				<button class="nav-button" id="nav-putt" data-page="expert-data-v8-putt.jsp">
					<img src="../../page/img/putt_icon.png" alt="推桿分析">
				</button>
				<button class="pdf-download-btn" id="btn-download-pdf" type="button" title="下載紀念 PDF">
					下載 PDF
				</button>
			</div>
			<%-- ⛔ DEV ONLY：接真資料後刪掉 --%>
			<div class="dev-banner">版面草稿：界標列與右欄為假資料</div>
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
				<div class="putt-feedback">

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
					  ⚠️ 標題的「N」與圖說由 manager 填（撈幾推是參數，⛔ 不寫死）。
					  ⚠️ 落點圖本身排在第一階段之後；下面的 SVG 是版面佔位用的靜態示意圖。
					--%>
					<div class="putt-consistency" id="puttConsistency">
						<p class="box-title"></p>
						<div class="putt-consistency-map">
							<%--
							  ⚠️ 這一頁通常投在模擬器的投影機上 —— 投影會把低對比整個吃掉。
							     ⛔ 軸線與文字不可以用暗灰，字級也不要再往下調。

							  ⭐ 計算方式（真的接資料時照這個做，⛔ 不要改成「離洞多遠」）：
							     ⚠️⚠️ 兩軸都必須是**碰球瞬間量到的值**，⛔ 不可以用滾動後的結果：
							       橫軸 ＝ shot_data.LaunchDirection 相對「自己的平均」（出球偏左／偏右）
							       縱軸 ＝ shot_data.BallSpeed       相對「自己的平均」（推太強／推太弱）
							     ⛔ 縱軸⛔ 不是距離 —— TotalDistFt 是模擬器用草皮摩擦係數滾出來的。
							     ⛔ 也⛔ 不可以用 ShortGameData 的 landing_points：
							       那是球飛行模型幾何反推的（carry × tan(方向) ＋ 側旋常數），對推桿三行全不成立。
							     · 原點 ⊕ ＝ 這 N 推的**平均**，⛔ 不是洞、⛔ 不是目標
							     · 每個點 ＝ 該推相對那個平均的偏移
							     · 虛線圈 ＝ 散布範圍（一個標準差）
							     ⚠️ 力道軸要**依 DistToPinFt 分層**（2 呎和 30 呎的球速本來就差很多），
							       基準是「自己在同一個距離帶的平均」，⛔ 不是全域平均。
							       ⛔ 樣本不足時標「資料不足、不畫」，⛔ 絕不可退回全域平均。
							  ⭐ 顏色比照 shortTableManager.js（切桿頁的落點圖）：
							     · 最新這一推 ＝ 亮綠 rgba(0,255,132,1)，半徑放大 ＋ 白框
							     · 之前幾推   ＝ 亮黃 rgb(255,206,86)，越新 alpha 越高
							     · 平均點與散布圈 ＝ 亮藍 rgb(54,162,235)
							  ⚠️ alpha 這裡用 0.35〜0.90；切桿頁原本是 0.20〜0.60。
							     ⛔ 不要調回 0.20 —— 這一頁投在投影機上，最舊那幾顆會看不見。
							     · 軸標「偏長／偏短／偏左／偏右」都是**相對自己的平均**
							  ⛔ 軸標⛔ 不可以寫「推太長／推太短」—— 那是在說這一推不準，
							     而這張圖回答的是「這個人穩不穩」，⛔ 不是「這一推準不準」。
							  ⛔ 圖上⛔ 不出現任何絕對距離數字（模擬器估的推桿距離不準）。
							--%>
							<svg viewBox="0 0 280 160" width="100%" height="100%" role="img" aria-label="推桿落點散布示意">
								<line x1="140" y1="16" x2="140" y2="144" stroke="#8f979b" stroke-width="1.4"/>
								<line x1="50" y1="80" x2="230" y2="80" stroke="#8f979b" stroke-width="1.4"/>
								<text x="140" y="12" fill="#ffffff" font-size="15" text-anchor="middle">推太強</text>
								<text x="140" y="157" fill="#ffffff" font-size="15" text-anchor="middle">推太弱</text>
								<text x="46" y="85" fill="#ffffff" font-size="15" text-anchor="end">偏左</text>
								<text x="234" y="85" fill="#ffffff" font-size="15" text-anchor="start">偏右</text>
								<circle cx="137" cy="80" r="28" fill="none" stroke="rgba(54, 162, 235, 0.85)" stroke-width="1.4" stroke-dasharray="4 4"/>
								<circle cx="122" cy="64" r="3.4" fill="rgba(255, 206, 86, 0.90)"/>
								<circle cx="151" cy="72" r="3.4" fill="rgba(255, 206, 86, 0.83)"/>
								<circle cx="134" cy="95" r="3.4" fill="rgba(255, 206, 86, 0.76)"/>
								<circle cx="160" cy="88" r="3.4" fill="rgba(255, 206, 86, 0.69)"/>
								<circle cx="118" cy="90" r="3.4" fill="rgba(255, 206, 86, 0.62)"/>
								<circle cx="128" cy="78" r="3.4" fill="rgba(255, 206, 86, 0.56)"/>
								<circle cx="156" cy="103" r="3.4" fill="rgba(255, 206, 86, 0.49)"/>
								<circle cx="112" cy="72" r="3.4" fill="rgba(255, 206, 86, 0.42)"/>
								<circle cx="143" cy="83" r="3.4" fill="rgba(255, 206, 86, 0.35)"/>
								<circle cx="147" cy="55" r="5.2" fill="rgba(0, 255, 132, 1)" stroke="rgba(255,255,255,0.8)" stroke-width="1.6"/>
								<circle cx="137" cy="80" r="8" fill="none" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>
								<line x1="129" y1="80" x2="145" y2="80" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>
								<line x1="137" y1="72" x2="137" y2="88" stroke="rgba(54, 162, 235, 1)" stroke-width="2.6"/>
							</svg>
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
		// ===== JSP Data to JS Variables =====
		const frontVideoPathData = '<%= frontVideoPath %>';
		const sideVideoPathData = '<%= sideVideoPath %>';

		// 影像分析是否已在頁面載入時完成（id_analyzeVideo_X 有值）
		const frontAnalyzReady = <%= frontAnalyzReady %>;
		const sideAnalyzReady = <%= sideAnalyzReady %>;
		// 廠商會送哪部影片（raw_shotVideo_X 有值）— 不送的那邊永遠不會 ready，不需追蹤
		const frontExpected = <%= frontExpected %>;
		const sideExpected = <%= sideExpected %>;

		// 影片輪詢（等待轉檔完成）— 抄自 expert-data-v8-short.jsp，參數可在此調整
		const videoPoller = new VideoPollManager({
			statusUrl: 'VideoStatus',
			shotDataId: '<%= shot_data_id %>',
			interval: 3000,       // 每 3 秒檢查一次
			maxAttempts: 40,      // 最多 40 次（約 2 分鐘）
			initialDelay: 3000,   // 頁面載入後 3 秒開始檢查
		});

		// ===== Global DOM =====
		const controlBtn = document.getElementById('play-pause');
		const videoContainer = document.getElementById('videoContainer');
		const video = document.getElementById('myvideo');
		const canvas = document.getElementById('overlayCanvas');

		const videoContainer1 = document.getElementById('videoContainer1');
		const video1 = document.getElementById('myvideo1');
		const canvas1 = document.getElementById('overlayCanvas1');

		// ⚠️ 這一輪的資料來源是兩支 manager 裡的假資料。
		//    ⛔ 之後要換成 PuttingData.java 組出來的物件（§6.3 第 14 項），
		//    ⭐ 但保留「吃 JSON」那條路徑（用網址參數切換，像現有的 ?LLM=true），
		//       判定模組第一次接真資料出事時可以重現。
		const puttPanelData = PUTT_PANEL_DEV_DATA;
		const puttIssuesData = PUTT_ISSUES_DEV_DATA;

		// ===== 影片跳幀 =====
		// ⛔ 兩支影片的幀號完全不可互換（實測同一次推擊偏移是 35/36/22/60，⛔ 不是常數）。
		//    每支影片一定用它自己那一列的 PuttingPhases。
		//    側面只在它自己也可信時才跟著跳；⛔ 不可信就不跳，也⛔ 不標示。
		function goToPuttFrame(frontFrame) {
			if (typeof frontFrame !== 'number' || isNaN(frontFrame)) return;
			seekVideo(video, frontFrame / puttPanelData.fps);

			const sidePhases = puttPanelData.sidePhases;
			if (sidePhases && typeof sidePhases.frameFor === 'function') {
				// 之後接上側面那一列的 PuttingPhases 時走這裡
				seekVideo(video1, sidePhases.frameFor(frontFrame) / puttPanelData.fps);
			}

			controlBtn.className = 'play';
			controlBtn.innerText = 'Play';
		}

		function seekVideo(videoEl, time) {
			if (videoEl.readyState >= 2) {
				videoEl.pause();
				videoEl.currentTime = time;
			} else {
				videoEl.addEventListener('loadedmetadata', function () {
					videoEl.pause();
					videoEl.currentTime = time;
				});
			}
		}

		// ⭐⭐「▶ 看這一段」——⛔ 這不是裝飾，是「碰球界標找錯」唯一的現場檢查手段（§2.6）。
		//     跳到區間起點播到終點；教練看到的不是下桿，當場就會發現界標抓錯。
		function playPuttSegment(startFrame, endFrame) {
			goToPuttFrame(startFrame);
			const endTime = endFrame / puttPanelData.fps;
			const stopAtEnd = function () {
				if (video.currentTime >= endTime) {
					video.pause();
					video.removeEventListener('timeupdate', stopAtEnd);
					controlBtn.className = 'play';
					controlBtn.innerText = 'Play';
				}
			};
			video.addEventListener('timeupdate', stopAtEnd);
			video.play();
			controlBtn.className = 'pause';
			controlBtn.innerText = 'Pause';
		}

		// ===== Managers =====
		const puttPanelManager = new PuttPanelManager({
			marksId: 'puttMarks',
			valuePanelId: 'puttValuePanel',
			detailPanelId: 'puttDetailPanel',
			onSeekFrame: goToPuttFrame,
		});

		const puttIssuesManager = new PuttingIssuesManager({
			tabsId: 'puttIssueTabs',
			panelId: 'puttIssuePanel',
			overallId: 'puttOverall',
			consistencyId: 'puttConsistency',
			onSeekSegment: playPuttSegment,
		});

		// ===== 影片控制（抄自 expert-data-v8-short.jsp）=====
		function toggleFullScreen(containerElement) {
			if (!document.fullscreenElement) {
				if (containerElement.requestFullscreen) {
					containerElement.requestFullscreen();
				} else if (containerElement.webkitRequestFullscreen) { // Safari
					containerElement.webkitRequestFullscreen();
				} else if (containerElement.msRequestFullscreen) { // IE11
					containerElement.msRequestFullscreen();
				}
			} else if (document.exitFullscreen) {
				document.exitFullscreen();
			}
		}

		function handleFullScreenChange() {
			// 給瀏覽器一點時間更新 DOM 尺寸，再重算兩個畫布
			setTimeout(function () {
				resizeCanvas(video, canvas, null, false);
				resizeCanvas(video1, canvas1, null, true);
			}, 150);
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
			if (video.ended && video1.ended) {
				controlBtn.className = 'play';
				controlBtn.innerText = 'Play';
			} else if ((video.ended && !video1.paused) || (video1.ended && !video.paused)) {
				controlBtn.className = 'pause';
				controlBtn.innerText = 'Pause';
			}
		}

		// ===== Initialization =====
		function init() {
			window.addEventListener('resize', function () {
				resizeCanvas(video, canvas, null, false);
				resizeCanvas(video1, canvas1, null, true);
			});

			document.addEventListener('fullscreenchange', handleFullScreenChange);
			document.addEventListener('webkitfullscreenchange', handleFullScreenChange);
			document.addEventListener('mozfullscreenchange', handleFullScreenChange);
			document.addEventListener('MSFullscreenChange', handleFullScreenChange);

			if (videoContainer) {
				video.addEventListener('dblclick', function () { toggleFullScreen(videoContainer); });
			}
			if (videoContainer1) {
				video1.addEventListener('dblclick', function () { toggleFullScreen(videoContainer1); });
			}
			controlBtn.addEventListener('click', playPause);
			video.addEventListener('ended', handleVideoEnd);
			video1.addEventListener('ended', handleVideoEnd);

			// ⛔ 推桿頁不畫揮桿平面覆蓋線 → swingPlaneData 傳 null（swingVideo.js 有防呆）
			// ⚠️ 兩支都從第 0 幀開始。
			//    ⛔ 這一輪⛔ 不要用 puttPanelData.phases.address 當起始幀 ——
			//    那是 ex01 的假資料（88 幀 ÷ 60fps ≈ 1.47 秒），套到真影片上
			//    會變成一載入就跳到影片中間，看起來像壞掉。
			//    ⭐ 等 Core 的 fixture 有真的 PuttingPhases 之後，
			//    再把正面那一支改成跳到自己那一列的架桿幀（§6.2 第 11 項）。
			setupVideoEvents(video, canvas, null, 0, puttPanelData.fps, false);
			setupVideoEvents(video1, canvas1, null, 0, puttPanelData.fps, true);

			// 影片輪詢：轉檔完成就先換 src（不等分析），讓使用者更早看到自己的影片。
			// ⛔ 推桿頁沒有 SwingPlane 覆蓋線要補，所以⛔ 不傳 onAnalysisUpdate。
			videoPoller.start({
				frontExpected: frontExpected,
				sideExpected: sideExpected,
				frontReady: frontAnalyzReady,
				sideReady: sideAnalyzReady,
				onVideoReady: function (camera, url) {
					const videoEl = (camera === 'front') ? video : video1;
					const sourceEl = videoEl.querySelector('source');
					if (!sourceEl || sourceEl.getAttribute('src') === url) return;
					sourceEl.setAttribute('src', url);
					videoEl.load();
					console.log('[onVideoReady] swapped ' + camera + ' to ' + url);
				},
			});

			// 左欄下半
			// ⚠️ 第二個參數是「每一顆可不可信」，⛔ 一定要給 ——
			//    界標找不到時放的值仍在合法範圍內，⛔ 不可以用值存不存在判斷。
			puttPanelManager.setMarks(puttPanelData.phases, puttPanelData.trust);
			puttPanelManager.setValues(puttPanelData.values);
			puttPanelManager.setDetail(puttPanelData.detail);

			// 數值面板 ⇄ 詳細數值面板
			document.getElementById('puttPanelToggle')
				.addEventListener('click', function () { puttPanelManager.toggle(); });
			document.getElementById('puttDetailToggle')
				.addEventListener('click', function () { puttPanelManager.toggle(); });

			// 右欄
			puttIssuesManager.render(puttIssuesData);
		}

		document.addEventListener('DOMContentLoaded', init);
	</script>

	<%-- 紀念 PDF 下載 Modal (HTML / PDF_CONTEXT / pdfDownloadManager.js) --%>
	<% request.setAttribute("__pdfPageMode", "putt"); %>
	<%@ include file="pdf-modal-fragment.jspf" %>

</body>
</html>
