<%@page import="com.golfmaster.service.ShotVideo"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.ShotData"%>

<%!ShotVideo shotVideo = new ShotVideo();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(shotVideo.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>

