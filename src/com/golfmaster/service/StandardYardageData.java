package com.golfmaster.service;

/*
 * 通用球桿距離API
 * 參數: PeopleType(必填)
 * http://localhost:8080/GolfMaster/service/standardyardage.jsp?PeopleType=Women
 * http://localhost:8080/GolfMaster/service/standardyardage.jsp?PeopleType=Men
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class StandardYardageData {

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = new JSONObject();
		String result = null;
		String playerData = null;
		try {
			printParam(request);
//			String PeopleType = request.getParameter("PeopleType");
			String playerName = request.getParameter("Player");
			String Gender = request.getParameter("gender");
			String courseName = request.getParameter("courseName");
			String courseHole = request.getParameter("courseHole");
//			result = querryStandardYardage(PeopleType);
			int holeNum = Integer.parseInt(courseHole);
			result = getCourseAndPlayer(Gender, courseName, holeNum);
			playerData = querryStandardYardage(Gender, playerName);
			jsonResponse.put("success", result);
			jsonResponse.put("code", 0);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			jsonResponse.put("code", -1);
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsonResponse);
		return result;
	}

	public String querryStandardYardage(String gender, String player) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();
		JSONObject jsResp = new JSONObject();
		jsResp.put("result", jarrProjects);
		strSQL = String.format("SELECT * FROM golf_master.yardage_book WHERE gender = '%s';", gender);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("PeopleType", rs.getString("PeopleType"));
				jsonObject.put("ClubType", rs.getString("ClubType"));
				jsonObject.put("BeginnerDistance", rs.getString("BeginnerDistance"));
				jsonObject.put("AverageDistance", rs.getString("AverageDistance"));
				jsonObject.put("GoodDistance", rs.getString("GoodDistance"));

				jarrProjects.put(jsonObject);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return jsResp.toString();
	}

	public String getCourseAndPlayer(String gender, String courseName, int courseHole) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();
		JSONObject jsResp = new JSONObject();
		jsResp.put("result", jarrProjects);
		String teeType = null;
		if (!gender.isEmpty() && gender == "man") {
			teeType = "white";
		} else if (!gender.isEmpty() && gender == "women") {
			teeType = "red";
		}
		strSQL = String.format("SELECT * FROM golf_master.course_data WHERE course_name = '%s' AND '%s'_tee_'%d';",
				courseName, teeType, courseHole);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("courseName", rs.getString("course_name"));
				jsonObject.put("par", rs.getString("courseHole"));
				jsonObject.put("gender", rs.getString("gender"));
				jsonObject.put("distance", "");

				jarrProjects.put(jsonObject);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return jsResp.toString();
	}

	private void printParam(HttpServletRequest request) {
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements()) {
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		Logs.log(Logs.RUN_LOG, strRequest);
	}
}
