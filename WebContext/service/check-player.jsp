<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.AccountManager"%>

<%!AccountManager accountManager = new AccountManager();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(accountManager.checkPlayer(request));
System.gc();
Runtime.getRuntime().gc();

%>