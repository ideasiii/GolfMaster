<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page import="org.json.JSONObject"%>
<%@ page import="com.golfmaster.service.ExpertData"%>

<%!ExpertData expertData = new ExpertData();%>
<%
request.setCharacterEncoding("UTF-8");
JSONObject result = expertData.processRequest(request);

String psystem = result.getString("expert_p_system");
String trajectory = result.getString("expert_trajectory");
String cause = result.getString("expert_cause");
String suggestion = result.getString("expert_suggestion");
%>

<!DOCTYPE html>
<html>
 <head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Expert Data</title>
  
  <style>
   .main{
   display: block;
    flex: 1;
    flex-basis: auto;
    overflow: auto;
    box-sizing: border-box;
    padding: 0;
    height: 100%;
    margin-left: 0 
   }
   .p2Box {
     width: 98%;
     min-height: 930px;
     display: flex;
     flex-direction: column;
     background-color: #F7F7F7;
   }
   .logoImage {
     position: absolute;
     margin-left: 3.8%;
     height: 32px;
     margin-top: 24px;
     margin-bottom: 24px;
   }
   
.p2Box__content {
    width: 100%;
    display: flex;
    margin: 170px 0%;
    flex-direction: row;
}
   .p2Box__content-left {
     width: 50%;
     display: flex;
     justify-content: flex-end;
   }
   .p2Box__content-right {
    width: 50%;
    display: flex;
    flex-direction: column;
    margin-left: 0%;
}
.p2Box__content-right-upper {
    width: 100%;
    height: 70%;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
}
.p2Box__content-right-lower {
    width: 100%;
    height: 50%;
    padding-top: 8%;
}
.expert_p_system {
    font-style: normal;
    font-weight: 500;
    font-size: 30px;
    line-height: 35px;
    letter-spacing: 0.02em;
    color: #2D2D2D;
}
.expert_cause {
    margin-top: 3%;
    font-style: normal;
    font-weight: 500;
    font-size: 25px;
    line-height: 160%;
    letter-spacing: 0.1em;
    color: rgba(45, 45, 45, 0.55);
}
  </style>
 </head>
 <body class="p2Box" style="background-image:url('../page/img/background.png');background-size:100%;;background-position:center center">
 
	 
	  <div>
	    <img src="../page/img/logo.756e00c6.png" alt="logo" class="logoImage">
	  </div>
	  <div class="p2Box__content">
	   <div class="p2Box__content-left"><img src="../page/img/problem2.091e3551.png" alt="問題2"></div>
	   <div class="p2Box__content-right">
	    <div class="p2Box__content-right-upper">
	     <div class="expert_p_system"> <%="P-System:" + psystem%> </div>
	     <div class="expert_cause"> <%="彈道:" + trajectory%> </div>
	     <div class="expert_cause"> <%="原因:" + cause%> </div>
	     <div class="expert_cause"> <%="建議:" + suggestion%> </div>
	    </div>
	    <div class="p2Box__content-right-lower"></div>
	   </div>
	  </div>
	  <%--
	  <p><%="P-System:" + psystem%></p>
	  
	  <p><%="彈道:" + trajectory%></p>
	  <p><%="原因:" + cause%></p>
	  <p><%="建議:" + suggestion%></p>
	  --%>
	 

 </body>
</html>

<%
System.gc();
Runtime.getRuntime().gc();
%>