<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.RawDataReceive"%>

<%!RawDataReceive rawDataReceive = new RawDataReceive();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(rawDataReceive.insertSpaceCapuleRawData(request));
System.gc();
Runtime.getRuntime().gc();

%>

