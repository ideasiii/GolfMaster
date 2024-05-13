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

Object temp[] = shotVideo.processAnalyz(shot_data_id);
int[] sideFrames = (int[]) temp[0];
int[] frontFrames = (int[]) temp[1];
String frontVideoName = (String) temp[2];
String sideVideoName = (String) temp[3];

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
https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.5.1/chart.js"></script>
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
							<video id="myvideo" class="image_v" controls muted>
								<source src="../../video/analyzVideo_front/<%=frontVideoName%>"
									type="video/mp4" alt="Image 1" />
								<!--<source src="../../page/video/<%=frontVideoName%>" type="video/mp4"
									alt="Image 1" /> -->
							</video>
							<video id="myvideo1" class="image_v" controls muted>
								<source src="../../video/analyzVideo_side/<%=sideVideoName%>"
									type="video/mp4" alt="Image 1" />
								<!--<source src="../../page/video/<%=sideVideoName%>" type="video/mp4"
									alt="Image 1" />-->
							</video>
						</div>
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
				<div class="content" style="text-align: center;">
					<div class="row">
						<!--<h1>個人影像</h1>-->
						<div class="psystemSection">
							<div class="box">
								<img src="../../page/img/GP_F_01.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_04.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_07.jpg">
							</div>
							<div class="box">
								<img src="../../page/img/GP_F_09.jpg">
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
											<p class="title"><%="彈道：" + trajectory%></p>
											<p class="s_content"><%="p-system：" + psystem%></p>
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
		var video = document.getElementById('myvideo');
		var video1 = document.getElementById('myvideo1');
		var controlBtn = document.getElementById('play-pause');
	
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
        	document.getElementById("smachfactDisplay").innerText = "擊球效率:"+smachfact+"mph";
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
