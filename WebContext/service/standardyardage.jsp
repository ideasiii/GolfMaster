<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.StandardYardageData"%>

<%!StandardYardageData standardYardageData = new StandardYardageData();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(standardYardageData.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>

