<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.moduel.PSystem"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!PSystem pSystem = new PSystem();%>

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
float BallSpeed = 0;
float BackSpin = 0;
float SideSpin = 0;
float LaunchAngle = 0;
float Angle = 0;
%>
<!DOCTYPE html>
<html>
<meta charset="UTF-8">
<title>Expert Data</title>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<style>
.container {
	display: flex;
	align-items: flex-start;
}

.text-side {
	display: flex;
	flex-direction: column;
	justify-content: space-around;
	padding-left: 20px; /* Adjust the spacing as needed */
}

.text-side p {
	font-size: 20px;
	color: #00A9BC;
	font-weight: bold;
	padding: 5px;
}

.chart-side {
	width: 500px;
	height: 400px;
}
</style>
</head>
<body>
	<div class="container">
		<div class="chart-side">
			<canvas id="radarChart"></canvas>
		</div>
		<div class="text-side">
			<p id="ballSpeedDisplay"></p>
			<p id="clubSpeedDisplay"></p>
			<p id="distanceDisplay"></p>
			<p id="launchAngleDisplay"></p>
			<p id="backSpinDisplay"></p>
		</div>
	</div>

</body>
<script>
    // 在頁面加載時執行
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
	
    document.addEventListener("DOMContentLoaded", function() {
        document.getElementById("ballSpeedDisplay").innerText = "球速: " + ballSpeed + " mph -> " + ballSpeedLevel + "mph";
        document.getElementById("clubSpeedDisplay").innerText = "桿頭速度: " + clubSpeed + " mph -> " + clubSpeedLevel + "mph";
        document.getElementById("distanceDisplay").innerText = "距離: " + distance + " yard -> " + distanceLevel + "yard";
        document.getElementById("launchAngleDisplay").innerText = "發射角度: " + launchAngle + " degree -> " + launchAngleLevel + "degree";
        document.getElementById("backSpinDisplay").innerText = "後旋: " + backSpin + " rpm -> " + backSpinLevel + "rpm";
    });
    
 // 隨機生成後旋、桿頭速度和距離的數據和範圍
    //var backSpin = Math.random() * 100;
    //var clubSpeed = Math.random() * 100;
    //var distance = Math.random() * 100;
    //var ballSpeed = Math.random() * 100;
    //var launchAngle = Math.random() * 100;

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
                	beginAtZero: false, // 不從0開始
                    min: 1,  // 最小值設定為1
                    max: 7,  // 最大值設定為7
                    stepSize: 1,  // 步長為1
                },
                angleLines: {
                    display: true
                },
                pointLabels: {
                    font: {
                        size: 16, // 字體大小
                        family: "'Arial', sans-serif", // 字體類型
                        weight: 'bold' // 字體粗體
                    	},
                    color: '#000000' // 字體顏色
                	},
            	},
            },
            plugins: {
                legend: {
                    labels: {
                        color: 'red', // 設置圖例文字顏色
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
</html>

<%
System.gc();
Runtime.getRuntime().gc();
%>