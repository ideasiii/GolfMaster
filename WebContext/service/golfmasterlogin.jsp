<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>
<%@ page import="com.golfmaster.service.GolfMasterRegister"%>

<%!GolfMasterRegister golfMasterLogin = new GolfMasterRegister();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(golfMasterLogin.doLogin(request));

System.gc();
Runtime.getRuntime().gc();
%>