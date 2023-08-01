<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.StatisticsData"%>

<%!StatisticsData statisticsData = new StatisticsData();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(statisticsData.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>