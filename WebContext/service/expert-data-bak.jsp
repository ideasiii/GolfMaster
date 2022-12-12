<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="org.json.JSONObject"%>
<%@ page import="com.golfmaster.service.ExpertData"%>

<%!ExpertData expertData = new ExpertData();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);
String psystem = result.getString("expert_p_system");
String trajectory = result.getString("expert_trajectory");
String cause = result.getString("expert_cause");
String suggestion = result.getString("expert_suggestion");
%>



<%="P-System:" + psystem%><br/>
<%="彈道:" + trajectory%><br/>
<%="原因:" + cause%><br/>
<%="建議:" + suggestion%><br/>


<%
System.gc();
Runtime.getRuntime().gc();
%>

