<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.IritShotData"%>

<%!IritShotData shotData = new IritShotData();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(shotData.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>

