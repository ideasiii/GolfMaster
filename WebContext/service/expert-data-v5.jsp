<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.RawDataReceive"%>

<%!ExpertData expertData = new ExpertData();%>
<%!ShotData shotData = new ShotData();%>
<%!RawDataReceive rawDataReceive = new RawDataReceive();%>

<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
JSONObject rdResult = rawDataReceive.getJmexRawDataReq(request);
JSONObject iritResult = rawDataReceive.getIRITRawDataReq(request);
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

if (rdResult != null && rdResult.getString("backSwingTime") != null) {
	backSwingTime = Float.parseFloat(rdResult.getString("backSwingTime"));
}
if (rdResult != null && rdResult.getString("downSwingTime") != null) {
	downSwingTime = Float.parseFloat(rdResult.getString("downSwingTime"));
}
if (rdResult != null && rdResult.getString("tempo") != null) {
	tempo = Float.parseFloat(rdResult.getString("tempo"));
}
if (iritResult != null && iritResult.getString("BallSpeed") != null) {
	BallSpeed = Float.parseFloat(iritResult.getString("BallSpeed"));
}
if (iritResult != null && iritResult.getString("tempo") != null) {
	tempo = Float.parseFloat(iritResult.getString("tempo"));
}
if (iritResult != null && iritResult.getString("tempo") != null) {
	tempo = Float.parseFloat(iritResult.getString("tempo"));
}
if (iritResult != null && iritResult.getString("tempo") != null) {
	tempo = Float.parseFloat(iritResult.getString("tempo"));
}
if (iritResult != null && iritResult.getString("tempo") != null) {
	tempo = Float.parseFloat(iritResult.getString("tempo"));
}

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
	height: 30%;
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
}

.p2Box__content-right-mid {
	width: 100%;
	height: 40%;
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
}

.p2Box__content-right-lower {
	width: 100%;
	height: 30%;
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

.expert_cause1 {
	margin-top: 3%;
	font-style: normal;
	font-weight: 500;
	font-size: 16px;
	line-height: 160%;
	letter-spacing: 0.1em;
	color: rgba(45, 45, 45, 0.55);
}

.canvas {
	width: 500px;
	height: 250px;
}

.content {
	display: none;
}

.expert_cause:hover .content {
	display: block;
	position: absolute;
	top: 500px;
	left: 200px;
	padding: 5px;
	border-radius: 5px;
}

.highlight {
	font-style: normal;
	font-weight: 500;
	font-size: 25px;
	line-height: 160%;
	letter-spacing: 0.1em;
	color: rgba(45, 45, 45, 0.55);
}

.highlight:hover {
	display: block;
	position: absolute;
	top: 500px;
	left: 200px;
	padding: 5px;
	border-radius: 5px;
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
				<%="節奏 Tempo"%>
				<div><%="上桿時間 下桿時間 節奏"%></div>
				<div><%=backSwingTime%>
					<%=downSwingTime%>
					<%=tempo%></div>
			</div>
			<div class="p2Box__content-right-mid">
				<div>
					<%

					%>
				</div>
			</div>
			<div class="p2Box__content-right-lower">
				<div class="expert_cause"><%="彈道:" + trajectory%></div>
				<div class="expert_cause1"><%="建議:" + suggestion%></div>
			</div>
		</div>
	</div>

</body>
<script>
	
</script>
</html>

<%
System.gc();
Runtime.getRuntime().gc();
%>