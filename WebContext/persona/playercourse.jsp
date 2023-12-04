<%@page import="com.golfmaster.persona.Persona"%>
<%@ page import="com.golfmaster.service.ShotData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	
<%@ page import="org.json.JSONObject"%>

<%!Persona persona = new  Persona();%>
<%!ShotData shotData = new ShotData();%>

<%
request.setCharacterEncoding("UTF-8");
//out.print(shotData.processRequest(request));
String player = request.getParameter("Player");
//out.println(shotData.playerDistanceAVG(player));
//out.print(shotData.playerHistory(player));
System.gc();
Runtime.getRuntime().gc();

%>
