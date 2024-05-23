<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@page import="com.golfmaster.service.CourseScoreCard"%>

<%!CourseScoreCard courseScoreCard = new CourseScoreCard();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(courseScoreCard.processRequest(request));
System.gc();
Runtime.getRuntime().gc();
%>

