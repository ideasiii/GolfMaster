<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>
<%@ page import="com.golfmaster.service.GolfMasterLogin"%>

<%! GolfMasterLogin golfMasterLogin = new GolfMasterLogin();%>
<%
request.setCharacterEncoding("UTF-8");
response.setCharacterEncoding("UTF-8");
golfMasterLogin.getMemberData(request, response);
System.gc();
Runtime.getRuntime().gc();
%>