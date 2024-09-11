<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.ShotVideo"%>
<%@ page import="com.golfmaster.moduel.DeviceData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!ShotVideo shotVideo = new ShotVideo();%>

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
<link href="../page/css/GM.css" rel="stylesheet" type="text/css">
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
			<img src="../page/img/logo_1.png" alt="Your Logo" class="logo">
			<!-- <Input Type="Button" Value="重新整理" onClick="window.location.reload();">  -->
		</div>
		<div class="row2 ">
			<div class="column2">
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->

						<div class="row2" style="background-color: #000000">
							<div style="position: relative;">
								<video id="myvideo" class="image_v" controls muted>
									<!--<source src="../video/analyzVideo_front/<%=frontVideoName%>"
									type="video/mp4" alt="Image 1" />-->
									<source src="../page/video/<%=frontVideoName%>"
										type="video/mp4" alt="Image 1" />
								</video>
								<canvas id="overlayCanvas"></canvas>
							</div>
							<div style="position: relative;">
								<video id="myvideo1" class="image_v" controls muted>
									<!--<source src="../video/analyzVideo_side/<%=sideVideoName%>"
									type="video/mp4" alt="Image 1" />-->
									<source src="../page/video/<%=sideVideoName%>" type="video/mp4"
										alt="Image 1" />
								</video>
								<canvas id="overlayCanvas1"></canvas>
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
							<!-- <div class="legend-container"></div>  -->
							<div class="rida_p">
								<canvas id="radarChart"></canvas>
							</div>
							<div class="aanalysis">
								<p class="strikeeff" id="smachfactDisplay"></p>
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
				<div class="custom-div2">
					<div class="steps">
						<div class="step">
							<span class="step_title">Swing</span><br> <span
								class="step_4">4 Steps</span>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=sideFrames[0]%>,<%=frontFrames[0]%>)">A</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=sideFrames[1]%>,<%=frontFrames[1]%>)">T</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=sideFrames[2]%>,<%=frontFrames[2]%>)">I</button>
						</div>
						<div class="step">
							<button class="stepbutton"
								onclick="goToFrame(<%=sideFrames[3]%>,<%=frontFrames[3]%>)">F</button>
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
								<img src="../page/img/A/A<%=aEffect%>.png">
							</div>
							<div class="box">
								<img src="../page/img/T/T<%=tEffect%>.png">
							</div>
							<div class="box">
								<img src="../page/img/I/I<%=iEffect%>.png">
							</div>
							<div class="box">
								<img src="../page/img/F/F<%=fEffect%>.png">
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
										<img src="../page/img/pic_coach.png" alt="Image">
										<!-- <video style="width: 200px; height: 200px;" controls>
											<source src="<%=talkhead%>" type="video/mp4">
										</video>  -->
										<div class="inner-text">
											<p class="title"><%="彈道：" + trajectory%></p>
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
		var frameRate = 60;
		var controlBtn = document.getElementById('play-pause');
		// 設定影片和對應的畫布
		var video = document.getElementById('myvideo');
		var video1 = document.getElementById('myvideo1');
		var canvas = document.getElementById('overlayCanvas');
		var canvas1 = document.getElementById('overlayCanvas1');
		var ctx = canvas.getContext('2d');
		var ctx1 = canvas1.getContext('2d');

		// 接收的輔助線數據
		const responseData = {
		    "success": true,
		    "bbox": [0.2518528386166221, 0.29106055365668404, 0.6435657802381014, 0.7766441062644676],
		    "head": { "pt": [0.5592105263157895, 0.3333333333333333], "h_length": 0.13486842105263158, "v_length": 0.07592592592592592 },
		    "club": { "pt1": [0.25164473684210525, 0.27685185185185185], "pt2": [0.7220394736842105, 0.75] },
		    "shoulder": { "pt1": [0.4555921052631579, 0.29074074074074074], "pt2": [0.7220394736842105, 0.75] },
		    "left_leg": { "pt1": [0.0, 0.0], "pt2": [0.0, 0.0] },
		    "right_leg": { "pt1": [0.0, 0.0], "pt2": [0.0, 0.0] }
		};

		// 根據影片實際顯示大小調整畫布
		function resizeCanvas(videoElement, canvasElement) {
			canvasElement.style.width = videoElement.clientWidth + 'px';
		    canvasElement.style.height = videoElement.clientHeight + 'px';
		    canvasElement.width = videoElement.clientWidth;
		    canvasElement.height = videoElement.clientHeight;
		    console.log("Canvas resized to match video dimensions: " + canvasElement.width + "x" + canvasElement.height);
		}

		// 繪製邊框
		function drawBoundingBoxForVideo(bbox, videoElement, canvasElement, ctx, color = 'green') {
		    const videoDisplayWidth = videoElement.clientWidth;
		    const videoDisplayHeight = videoElement.clientHeight;

		    const startX = bbox[0] * videoDisplayWidth;
		    const startY = bbox[1] * videoDisplayHeight;
		    const width = (bbox[2] - bbox[0]) * videoDisplayWidth;
		    const height = (bbox[3] - bbox[1]) * videoDisplayHeight;

		    ctx.strokeStyle = color;
		    ctx.lineWidth = 4; // 增加線條寬度以便更清晰顯示
		    ctx.strokeRect(startX, startY, width, height);

		    console.log("BoundingBox drawn: startX = " + startX + ", startY = " + startY + ", width = " + width + ", height = " + height);
		}

		// 繪製輔助線
		function drawLineForVideo(line, videoElement, canvasElement, ctx, color = 'red') {
		    const videoDisplayWidth = videoElement.clientWidth;
		    const videoDisplayHeight = videoElement.clientHeight;

		    const [startX, startY] = [line.pt1[0] * videoDisplayWidth, line.pt1[1] * videoDisplayHeight];
		    const [endX, endY] = [line.pt2[0] * videoDisplayWidth, line.pt2[1] * videoDisplayHeight];

		    ctx.beginPath();
		    ctx.moveTo(startX, startY);
		    ctx.lineTo(endX, endY);
		    ctx.strokeStyle = color;
		    ctx.lineWidth = 4; // 增加線條寬度以便更清晰顯示
		    ctx.stroke();

		    console.log("Line drawn: startX = " + startX + ", startY = " + startY + ", endX = " + endX + ", endY = " + endY);
		}

		// 清除畫布並重新繪製輔助線，根據影片不同比例進行調整
		function clearAndDrawOverlayForVideo(videoElement, canvasElement, ctx, responseData) {
		    ctx.clearRect(0, 0, canvasElement.width, canvasElement.height); // 清除畫布

		    // 繪製 Bounding Box
		    drawBoundingBoxForVideo(responseData.bbox, videoElement, canvasElement, ctx, 'white');

		    // 繪製 Club 線
		    drawLineForVideo(responseData.club, videoElement, canvasElement, ctx, 'orange');

		    // 繪製 Shoulder 線
		    drawLineForVideo(responseData.shoulder, videoElement, canvasElement, ctx, 'orange');
		}

		// 當影片元數據載入完成時，設置畫布大小
		video.addEventListener('loadedmetadata', function () {
		    resizeCanvas(video, canvas);
		    console.log("Left video canvas resized.");
		});
		video1.addEventListener('loadedmetadata', function () {
		    resizeCanvas(video1, canvas1);
		    console.log("Right video canvas resized.");
		});

		// 當影片播放時，為每個影片繪製輔助線
		video.addEventListener('play', function () {
		    clearAndDrawOverlayForVideo(video, canvas, ctx, responseData);
		    console.log("Left video playing and overlay drawn.");
		});

		video1.addEventListener('play', function () {
		    clearAndDrawOverlayForVideo(video1, canvas1, ctx1, responseData);
		    console.log("Right video playing and overlay drawn.");
		});

		// 當視窗大小改變時，調整每個影片的畫布大小
		window.addEventListener('resize', function () {
		    resizeCanvas(video, canvas);
		    resizeCanvas(video1, canvas1);
		 // 確保畫布大小調整後重新繪製
		    clearAndDrawOverlayForVideo(video, canvas, ctx, responseData);
		    clearAndDrawOverlayForVideo(video1, canvas1, ctx1, responseData);
		});

        function goToFrame(frameNumber,frameNumber1) {
    		// 計算目標帧對應的時間（秒）
    		var time = frameNumber / frameRate;
    		var time1 = frameNumber1 / frameRate;
    		video.currentTime = time;
    		video1.currentTime = time1;
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
		var avgDist = <%=shotResult[2][1]%>;
		var distanceLevel = getDistanceLevel(distance);
		var launchAngle = <%=shotResult[3][0]%>;
		var avgLA = <%=shotResult[3][1]%>;
		var launchAngleLevel = getLaunchAngleLevel(launchAngle);
		var backSpin = <%=shotResult[4][0]%>;
		var avgBsp = <%=shotResult[4][1]%>;
		var backSpinLevel = getBackSpinLevel(backSpin);	        
		var smachfact = Math.round((ballSpeed/clubSpeed)*100)/100;
		//document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed + " -> " + ballSpeedLevel + "mph";
	    document.addEventListener("DOMContentLoaded", function() {
        	document.getElementById("smachfactDisplay").innerText = "擊球效率:"+smachfact;
	        document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed  + "mph";
	        document.getElementById("clubSpeedDisplay").innerText = "桿頭速度: " + clubSpeed  + "mph";
	        document.getElementById("distanceDisplay").innerText = "飛行距離: " + distance +  "yard";
	        document.getElementById("launchAngleDisplay").innerText = "發射角度: " + launchAngle  + "degree";
	        document.getElementById("backSpinDisplay").innerText = "後旋: " + backSpin  + "rpm";
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
	            label: '最新一球',
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
	            label: '擊球記錄',
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
