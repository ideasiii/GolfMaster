<%@ page language="java" contentType="application/json; charset=UTF-8"
	pageEncoding="UTF-8" session="false" trimDirectiveWhitespaces="true"%>

<%@ page import="com.golfmaster.service.CourseStrategy"%>

<%!CourseStrategy courseStrategy = new CourseStrategy();%>
<%
request.setCharacterEncoding("UTF-8");
out.print(courseStrategy.processStrategy(request));
System.gc();
Runtime.getRuntime().gc();
%>

