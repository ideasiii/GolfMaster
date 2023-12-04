<%@page import="java.util.regex.Matcher"%>
<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ page import="org.json.JSONObject"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.golfmaster.service.ExpertData2"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page import="com.golfmaster.service.RawDataReceive"%>
<%@ page import="com.golfmaster.moduel.PSystem"%>
<%@ page import="com.golfmaster.moduel.PSystemJP"%>

<%!ExpertData2 expertData = new ExpertData2();%>
<%!ShotData shotData = new ShotData();%>
<%!RawDataReceive rawDataReceive = new RawDataReceive();%>
<%!PSystem pSystem = new PSystem();%>
<%!PSystemJP pSystemJP = new PSystemJP();%>

<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
//JSONObject rdResult = rawDataReceive.getJmexRawDataReq(request);
//JSONObject iritResult = rawDataReceive.getIRITRawDataReq(request);
Long shot_data_id = result.getLong("shotdata_id");

float[][] shotResult = shotData.processPlayerReq(shot_data_id);
JSONObject iritData = shotData.processIRITData(shot_data_id);

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

//if (rdResult != null && rdResult.getString("backSwingTime") != null) {
//	backSwingTime = Float.parseFloat(rdResult.getString("backSwingTime"));
//}
//if (rdResult != null && rdResult.getString("downSwingTime") != null) {
//	downSwingTime = Float.parseFloat(rdResult.getString("downSwingTime"));
//}
//if (rdResult != null && rdResult.getString("tempo") != null) {
//	tempo = Float.parseFloat(rdResult.getString("tempo"));
//}
//if (iritResult != null && iritResult.getString("BallSpeed") != null) {
//	BallSpeed = Float.parseFloat(iritResult.getString("BallSpeed"));
//}
//if (iritResult != null && iritResult.getString("BackSpin") != null) {
//	BackSpin = Float.parseFloat(iritResult.getString("BackSpin"));
//}
//if (iritResult != null && iritResult.getString("SideSpin") != null) {
//	SideSpin = Float.parseFloat(iritResult.getString("SideSpin"));
//}
//if (iritResult != null && iritResult.getString("LaunchAngle") != null) {
//	LaunchAngle = Float.parseFloat(iritResult.getString("LaunchAngle"));
//}
//if (iritResult != null && iritResult.getString("Angle") != null) {
//	Angle = Float.parseFloat(iritResult.getString("Angle"));
//}
%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Expert Data</title>
<head>
<script>
	setInterval('window.location.reload();', 10000);
</script>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link href="../../page/css/GM01.css" rel="stylesheet" type="text/css">
<style></style>

</head>


<div class="header">
	<img src="../../page/img/logo_1.png" alt="Your Logo" class="logo">
</div>
<div class="content">
	<div class="row">
		<div class="custom-div">
			<video width=500px; autoplay loop muted class="images">
				<source src="../../page/video/P1_P10v5.mp4" type="video/mp4">
			</video>
		</div>
		<!-- <div class="custom-div2">
			<div class="steps">
				<div class="step">
					<span class="step_title">Swing</span><br> <span class="step_4">4
						Steps</span>
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
			</div>
		</div> -->
		<div class="custom-div">
			<%
			if (result.getBoolean("result")) {
				if (iritData.getFloat("LaunchDirection") <= 3 && iritData.getFloat("LaunchDirection") >= -3) {
					out.print("<img src='../../page/gif/Straight.gif' class='image' alt='Image 3'>");
				} else if (iritData.getFloat("LaunchDirection") > 3 && iritData.getFloat("LaunchDirection") < 15) {
					out.print("<img src='../../page/gif/Push.gif' class='image' alt='Image 3'>");
				} else if (iritData.getFloat("LaunchDirection") > 15) {
					out.print("<img src='../../page/gif/Pushs.gif' class='image' alt='Image 3'>");
				} else if (iritData.getFloat("LaunchDirection") < -3 && iritData.getFloat("LaunchDirection") > -15) {
					out.print("<img src='../../page/gif/Pull.gif' class='image' alt='Image 3'>");
				} else if (iritData.getFloat("LaunchDirection") <= -15) {
					out.print("<img src='../../page/gif/Pullh.gif' class='image' alt='Image 3'>");
				}
			} else {
				out.print("");
			}
			%>

		</div>
	</div>
	<div class="row">
		<div class="vertical-div" style="display: flex; align-items: center;">
			<div class="cards">
				<div class="card">
					<div class="title">球速</div>
					<div class="unit">mph</div>
					<div class="number"><%=iritData.getFloat("BallSpeed")%></div>
				</div>
				<div class="card">
					<div class="title">倒旋</div>
					<div class="unit">rpm</div>
					<div class="number"><%=iritData.getFloat("BackSpin")%></div>
				</div>
				<div class="card">
					<div class="title">側旋</div>
					<div class="unit">rpm</div>
					<div class="number"><%=iritData.getFloat("SideSpin")%></div>
				</div>
				<div class="card">
					<div class="title">仰角</div>
					<div class="unit">°</div>
					<div class="number"><%=iritData.getFloat("LaunchAngle")%></div>
				</div>
				<div class="card">
					<div class="title">水平角度</div>
					<div class="unit">°</div>
					<div class="number"><%=iritData.getFloat("LaunchDirection")%></div>
				</div>
			</div>
		</div>
		<div class="vertical-div"
			style="display: flex; align-items: flex-end;">
			<div class="suggestion">
				<div class="vertical-image" style="width: 100%">
					<img src="../../page/img/pic_coach.png" alt="Image">
					<div class="inner-text">
						<p class="title">
							<%
							if (result.getBoolean("result")) {
								if (iritData.getFloat("LaunchDirection") <= 3 && iritData.getFloat("LaunchDirection") >= -3) {
									out.print("彈道:Straight 直飛球");
								} else if (iritData.getFloat("LaunchDirection") > 3 && iritData.getFloat("LaunchDirection") <= 5) {
									out.print("彈道:Fade 小右曲球");
								} else if (iritData.getFloat("LaunchDirection") >= -5 && iritData.getFloat("LaunchDirection") < -3) {
									out.print("彈道:Draw 小左曲球");
								} else if (iritData.getFloat("LaunchDirection") > 5 && iritData.getFloat("LaunchDirection") <= 15) {
									out.print("彈道:Push 右飛球");
								} else if (iritData.getFloat("LaunchDirection") > 15) {
									out.print("彈道:Push Slice 右拉右曲球");
								} else if (iritData.getFloat("LaunchDirection") >= -15 && iritData.getFloat("LaunchDirection") < -5) {
									out.print("彈道:Pull 左飛球");
								} else if (iritData.getFloat("LaunchDirection") < -15) {
									out.print("彈道:Pull Hook 左拉左曲球");
								}
							} else {
								out.print("");
							}
							%>
						</p>
						<p class="s_content">
							<%
							if (result.getBoolean("result")) {
								if (iritData.getFloat("LaunchDirection") <= 3 && iritData.getFloat("LaunchDirection") >= -3) {
									out.print("建議:P1至P10動作的位置和時間都配合得很好，且節奏流暢。");
								} else if (iritData.getFloat("LaunchDirection") > 3 && iritData.getFloat("LaunchDirection") <= 5) {
									out.print(
									"建議:這是職業選手最喜歡打出的球路，需要保持手和身體的連結，瞄球時桿面朝向目標，站姿朝向目標左方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								} else if (iritData.getFloat("LaunchDirection") >= -5 && iritData.getFloat("LaunchDirection") < -3) {
									out.print(
									"建議:這是職業選手喜歡打出的球路，瞄球時桿面朝向目標，站姿朝向目標右方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								} else if (iritData.getFloat("LaunchDirection") > 5 && iritData.getFloat("LaunchDirection") <= 15) {
									out.print(
									"建議:上桿時維持軸心，P2時桿頭不要往內側，桿面不要翻開，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								} else if (iritData.getFloat("LaunchDirection") > 15) {
									out.print(
									"建議:上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								} else if (iritData.getFloat("LaunchDirection") >= -15 && iritData.getFloat("LaunchDirection") < -5) {
									out.print("建議:上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								} else if (iritData.getFloat("LaunchDirection") < -15) {
									out.print("建議:上桿時維持軸心，P2主P4胸椎充分旋轉下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。");
								}
							} else {
								out.print("");
							}
							%>
						</p>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>



</body>

</html>

<%
System.gc();
Runtime.getRuntime().gc();
%>