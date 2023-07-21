<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>

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
/*
if(result != null && result.getString("img_name") != null){
	img = result.getString("img_name");
}
*/
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Expert Data</title>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.6.0/Chart.js"></script>
<style>
.main {
	display: block;
	flex: 1;
	flex-basis: auto;
	overflow: auto;
	box-sizing: border-box;
	padding: 0;
	height: 100%;
	margin-left: 0
}

.p2Box {
	width: 98%;
	/*min-height: 710px;*/
	max-height: 850px;
	display: flex;
	flex-direction: column;
	background-color: #F7F7F7;
}

.logoImage {
	position: absolute;
	margin-left: 3.8%;
	height: 32px;
	margin-top: 24px;
	margin-bottom: 24px;
}

.p2Box__content {
	width: 100%;
	display: flex;
	/*margin: 170px 0%;*/
	margin: 110px 0%;
	flex-direction: row;
}

.p2Box__content-left-upper {
	width: 40%;
	display: flex;
	margin-left: 50px;
	position: absolute;
	button: 0px;
	/*     justify-content: flex-end;*/
}

.p2Box__content-left-lower {
	width: 40%;
	display: flex;
	margin-left: 50px;
	position: absolute;
	top: 500px;
	/*     justify-content: flex-end;*/
}

.p2Box__content-left {
	width: 50%;
	display: flex;
	justify-content: center;
	/*     justify-content: flex-end;*/
}

.p2Box__content-right {
	width: 50%;
	display: flex;
	flex-direction: column;
	margin-left: 0%;
}

.p2Box__content-right-upper {
	width: 100%;
	height: 50%;
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
}

.p2Box__content-right-lower {
	width: 100%;
	height: 70%;
	padding-top: 0%;
}

.expert_p_system {
	font-style: normal;
	font-weight: 500;
	font-size: 30px;
	line-height: 35px;
	letter-spacing: 0.02em;
	color: #2D2D2D;
}

.expert_cause {
	margin-top: 3%;
	font-style: normal;
	font-weight: 500;
	font-size: 25px;
	line-height: 160%;
	letter-spacing: 0.1em;
	color: rgba(45, 45, 45, 0.55);
}

.canvas {
	width: 500px;
	height: 250px;
}

.highlight {
	font-style: normal;
	font-weight: 500;
	font-size: 25px;
	line-height: 160%;
	letter-spacing: 0.1em;
	color: red;
}

.highlight:hover .image-container { 
	display: block;
	position: absolute;
	top: 350px; 
	left: 650px;
	padding: 5px;
	border-radius: 5px;
	background-color: white;
}

.highlight:hover .image-container img { 
	max-width: 100%;
	max-height: 100%;
}

.image-container {
	display: none; 
}
</style>

</head>
<body class="p2Box"
	style="background-image: url('../page/img/background.png'); background-size: cover; background-position: center center; background-repeat: no-repeat">


	<div>
		<img src="../page/img/logo.756e00c6.png" alt="logo" class="logoImage">
	</div>
	<div class="p2Box__content">
		<div class="p2Box__content-left">
			<div class="p2Box__content-left-upper">
				<!--<img src="../page/img/problem2.091e3551.png" >-->
				<%
				if (result.getBoolean("result")) {
					String video = result.getString("video");
					out.print("<video style='width:500px;height:400px' autoplay loop muted><source src='../page/video/" + video
					+ "' type='video/mp4'></video>");
				} else {
					if (result != null && result.getString("img_name") != null) {
						img = result.getString("img_name");
					}
					if (img != null && !img.isEmpty()) {
						out.print("<img src='../page/img/" + img + "' style='width:50%' />");
					}
				}
				%>
			</div>
			<div class="p2Box__content-left-lower">
				<%
				if (result.getBoolean("result")) {
					out.print("<img src='../page/gif/" + trajectory + ".gif' style='width: 500px; height: 336px' />");
				} else {
					out.print("");
				}
				%>
			</div>
		</div>
		<div class="p2Box__content-right">
			<div class="p2Box__content-right-upper">
				<canvas id="canvas" style='height: 240px'></canvas>
				<div class="expert_cause">
					<%="彈道:" + trajectory%>
				</div>
			</div>
			<div class="p2Box__content-right-lower">

				<div class="expert_cause">
					<div id="textContainer1">
						原因:<span id="causeText"><%=cause%></span>
					</div>
				</div>
				<div class="expert_cause">
					<div id="textContainer">
						建議:<span id="suggestionText"><%=suggestion%></span>
					</div>
				</div>
			</div>
		</div>
	</div>

</body>
<script>
	var ballspeed = [];
	var clubheadspeed = [];
<%for (int i = 0; i < shotResult[0].length; i++) {%>
	ballspeed.push(
<%=shotResult[0][i]%>
	);
	clubheadspeed.push(
<%=shotResult[1][i]%>
	);
<%}%>
	var lineChartData = {
		labels : [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ], //顯示區間名稱
		datasets : [ {
			label : '球速', // tootip 出現的名稱
			lineTension : 0, // 曲線的彎度，設0 表示直線
			backgroundColor : "#ea464d",
			borderColor : "#ea464d",
			borderWidth : 5,
			data : ballspeed,
			fill : false, // 是否填滿色彩
		}, {
			label : '桿頭速度', // tootip 出現的名稱
			lineTension : 0, // 曲線的彎度，設0 表示直線
			backgroundColor : "#29b288",
			borderColor : "#29b288",
			borderWidth : 5,
			data : clubheadspeed,
			fill : false, // 是否填滿色彩
		}, ]
	};
	function drawLineCanvas(ctx, data) {
		window.myLine = new Chart(ctx, { //先建立一個 chart
			type : 'line', // 型態
			data : data,
			options : {

				responsive : true,
				legend : { //是否要顯示圖示
					display : true,
					align : 'center'
				},
				tooltips : { //是否要顯示 tooltip
					enabled : true
				},
				scales : { //是否要顯示 x、y 軸
					xAxes : [ {
						scaleLabel : {
							display : true,
							labelString : "次數",
							fontSize : 16
						}
					} ],
					yAxes : [ {
						scaleLabel : {
							display : true,
							labelString : "速度(mph)",
							fontSize : 16
						}
					} ]
				},
			}
		});
	};
	window.onload = function() {
		var ctx = document.getElementById("canvas").getContext("2d");
		drawLineCanvas(ctx, lineChartData);
		
		const textContainer = document.getElementById('textContainer');
		const textContainer1 = document.getElementById('textContainer1');
	    const suggestionContent = document.getElementById('suggestionText').textContent;
	    const causeContent = document.getElementById('causeText').textContent;
	    const wordImageMap = {
	        'P2': '../page/img/frontP2.png',
	        'P3': '../page/img/frontP3.png',
	        'P4': '../page/img/frontP4.png',
	        'P5': '../page/img/frontP5.png',
	        'P5.5': '../page/img/frontP5.png',
	        'P6': '../page/img/frontP6.png',
	        'P7': '../page/img/frontP7.png',
	        'P8': '../page/img/frontP8.png',
	        'P9': '../page/img/frontP9.png',
	        'P10': '../page/img/frontP10.png'
	    };

	    let updatedContent = suggestionContent;
	    Object.keys(wordImageMap).forEach(word => {
	        const regex = new RegExp(word, 'gi');
	        updatedContent = updatedContent.replace(regex, '<span class="highlight" data-word="' + word + '">' + word + '<div class="image-container"><img src="' + wordImageMap[word] + '"></div></span>');
	    });
	    textContainer.innerHTML = updatedContent;
	    
	    let updatedContent1 = causeContent;
	    Object.keys(wordImageMap).forEach(word => {
	        const regex = new RegExp(word, 'gi');
	        updatedContent1 = updatedContent1.replace(regex, '<span class="highlight" data-word="' + word + '">' + word + '<div class="image-container"><img src="' + wordImageMap[word] + '"></div></span>');
	    });
	    textContainer1.innerHTML = updatedContent1;
	};
	
</script>
</html>

<%
System.gc();
Runtime.getRuntime().gc();
%>