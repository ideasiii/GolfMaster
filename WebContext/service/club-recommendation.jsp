<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.ClubRecommendation"%>

<%!ClubRecommendation clubRecommendation = new ClubRecommendation();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(clubRecommendation.processRequest(request));
System.gc();
Runtime.getRuntime().gc();
%>