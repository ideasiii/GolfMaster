<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>
<%@ page import="com.golfmaster.service.LoginE6"%>

<%!LoginE6 gmData = new LoginE6();%>

<%
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");

gmData.E6Web(request,response);


System.gc();
Runtime.getRuntime().gc();
%>
