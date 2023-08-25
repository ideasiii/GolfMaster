<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.YardageBookData"%>

<%!YardageBookData yardageBookData = new YardageBookData();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(yardageBookData.processRequest(request));
System.gc();
Runtime.getRuntime().gc();

%>