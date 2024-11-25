<%@page import="com.golfmaster.service.RealTimeShotsBenManchine"%>
<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%!RealTimeShotsBenManchine shotData = new RealTimeShotsBenManchine();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(shotData.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>

