<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.moduel.DeviceData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>

<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
Long shot_data_id = result.getLong("shotdata_id");

float[][] shotResult = shotData.processPlayerReq(shot_data_id);

String psystem = "";
String trajectory = "";
String cause = "";
String suggestion = "";
String img = "";

float backSwingTime = 0;
float downSwingTime = 0;
float tempo = 0;
float BallSpeed;
float BackSpin;
float SideSpin;
float LaunchAngle;
float Angle;
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="../../page/css/GM.css" rel="stylesheet" type="text/css">
<title>RWD網頁</title>
<style>
</style>

</head>
<body>
	<div class="c_m">
		<div class="header">
			<img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
		</div>
		<div class="row2 ">
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<audio id="track">
							<source src="https://cldup.com/qR72ozoaiQ.mp3" type="audio/mpeg" />
						</audio>
						<!--<h1>個人影像</h1>-->

						<div style="background-color: #000000">
							<img src="../../page/img/image2.png" alt="Image 1"
								class="image_v">
						</div>
						<div class="custom-div2">
							<div class="steps">
								<div class="step">
									<span class="step_title">Swing</span><br> <span
										class="step_4">4 Steps</span>
								</div>
								<div class="step">
									<div class="stepbutton_selected">A</div>
								</div>
								<div class="step">
									<div class="stepbutton">T</div>
								</div>
								<div class="step">
									<div class="stepbutton">I</div>
								</div>
								<div class="step">
									<div class="stepbutton">F</div>
								</div>
								<div class="step">
									<div id="player-container">
										<div id="play-pause" class="play">Play</div>
									</div>
								</div>
							</div>
						</div>

					</div>
				</div>
			</div>
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>能力提升分析</h1>-->
						<div class="aanalysisSection">
							<div class="pic_d" style="position: absolute">
								<img src="../../page/img/Asset 42.png">
							</div>
							<div class="rida_p">
								<img src="../../page/img/pic_r.png" class="image_r">
							</div>
							<div class="aanalysis">
								<p class="strikeeff">擊球效率:1.27 mph</p>
								<p id="ballSpeedDisplay"></p>
								<p id="clubSpeedDisplay"></p>
								<p id="distanceDisplay"></p>
								<p id="launchAngleDisplay"></p>
								<p id="backSpinDisplay"></p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="row2 stretch">
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->
						<div class="psystemSection">
							<div class="box">
								<img src="../../page/img/GP_F_02.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_04.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_05.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_06.jpg">
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
										<div class="inner-text">
											<p class="title">彈道：Fade 小右曲球</p>
											<p class="s_content">建議：此擊球它看起來不錯，但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球。</p>
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
		var track = document.getElementById('track');

		var controlBtn = document.getElementById('play-pause');

		function playPause() {
			if (track.paused) {
				track.play();
				//controlBtn.textContent = "Pause";
				controlBtn.className = "pause";
			} else {
				track.pause();
				//controlBtn.textContent = "Play";
				controlBtn.className = "play";
			}
		}

		controlBtn.addEventListener("click", playPause);
		track.addEventListener("ended", function() {
			controlBtn.className = "play";
		});
	    // 將JSP變量轉換為JavaScript變量
	    var greatLevelTopBS[0] = <%=expertData.GreatLevelTopBS%>;
	    var greatLevelLowBS[0] = <%=expertData.GreatLevelLowBS%>;
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
	    var BS = [];
	    var CS = [];
	    var Dist = [];
	    var LA = [];
	    var Bsp = [];
	    <%for (int i = 0; i < shotResult[0].length; i++) {%>
	    	BS.push(<%=shotResult[0][i]%>);
	    	CS.push(<%=shotResult[1][i]%>);
	    	Dist.push(<%=shotResult[2][i]%>);
	    	LA.push(<%=shotResult[3][i]%>);
	    	Bsp.push(<%=shotResult[4][i]%>);
	    <%}%>
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

	    // 在頁面加載時執行
	    document.addEventListener("DOMContentLoaded", function() {
	        // 從後端取得球速
	        var ballSpeed = <%=shotResult[0][0]%>;
	        var ballSpeedLevel = getBallSpeedLevel(ballSpeed);
	        var clubSpeed = <%=shotResult[1][0]%>;
	        var clubSpeedLevel = getClubSpeedLevel(clubSpeed);
	        var distance = <%=shotResult[2][0]%>;
	        var distanceLevel = getDistanceLevel(distance);
	        var launchAngle = <%=shotResult[3][0]%>;
	        var launchAngleLevel = getLaunchAngleLevel(launchAngle);
	        var backSpin = <%=shotResult[4][0]%>;
	        var backSpinLevel = getBackSpinLevel(backSpin);
	        document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed + " mph -> " + ballSpeedLevel;
	        document.getElementById("clubSpeedDisplay").innerText = "桿頭速度: " + ballSpeed + " mph -> " + clubSpeedLevel;
	        document.getElementById("distanceDisplay").innerText = "距離: " + ballSpeed + " yard -> " + distanceLevel;
	        document.getElementById("launchAngleDisplay").innerText = "發射角度: " + ballSpeed + " degree -> " + launchAngleLevel;
	        document.getElementById("backSpinDisplay").innerText = "後旋: " + ballSpeed + " rpm -> " + backSpinLevel;
	    });
	</script>
</body>
</html>
