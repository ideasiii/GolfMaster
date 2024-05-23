package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class CourseScoreCard {

	private class ParamData {
		private String originalStrategy;
		private String currentStrategy;
		private String player;
		private String courseName;
		private String holeNum;
		private String par;
		private String nowDate;
	}

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = null;
		String strResponse = "{\"success\": true,\"result\": []}";

		printParam(request);
		CourseScoreCard.ParamData paramData = new CourseScoreCard.ParamData();
		jsonResponse = requestAndTrimParams(request, paramData);
		if (null != jsonResponse)
			return jsonResponse.toString();

		jsonResponse = new JSONObject();
		saveCourseScore(paramData, jsonResponse);
		strResponse = jsonResponse.toString();
		Logs.log(Logs.RUN_LOG, "Response : " + strResponse);
		return strResponse;
	}

	private int saveCourseScore(ParamData paramData, JSONObject jsonResponse) {
		Connection conn = null;
		Statement stmt = null;
		int stmtRs = -1;
		String strSQL = null;
		JSONArray jarrProjects = new JSONArray();
		jsonResponse.put("result", jarrProjects);

		if (paramData.player != null && !paramData.player.isEmpty()) {
			strSQL = String.format(
					"INSERT IGNORE INTO golf_course.course_score "
							+ "(Player, Recommend_Strategy, Current_Strategy, Date, Course_Name, Hole_Number, Par)"
							+ " Values('%s','%s','%s','%s','%s','%s','%s')",
					paramData.player, paramData.originalStrategy, paramData.currentStrategy, paramData.nowDate,
					paramData.courseName, paramData.holeNum, paramData.par);
		}
		Logs.log(Logs.RUN_LOG, "saveCourseScore strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL);
			JSONObject jsonProject = new JSONObject();
			System.out.println("stmtRs:"+stmtRs);
			if (stmtRs == 1) {
				jsonProject.put("INSERT SCORE", true);
				jarrProjects.put(jsonProject);
			} else {
				jsonProject.put("INSERT SCORE", false);
				jarrProjects.put(jsonProject);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(null, stmt, conn);
		jsonResponse.put("result", jarrProjects);
		return stmtRs;
	}

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData) {
		try {
	        paramData.originalStrategy = StringUtils.trimToEmpty(request.getParameter("originalStrategy"));
	        paramData.currentStrategy = StringUtils.trimToEmpty(request.getParameter("currentStrategy"));
	        paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
	        paramData.courseName = StringUtils.trimToEmpty(request.getParameter("courseName"));
	        paramData.holeNum = StringUtils.trimToEmpty(request.getParameter("holeNum"));
	        paramData.par = StringUtils.trimToEmpty(request.getParameter("par"));
	        paramData.nowDate = StringUtils.trimToEmpty(request.getParameter("nowDate"));

			if (StringUtils.isEmpty(paramData.player)) {
				return ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			return ApiResponse.unknownError();
		}

		return null;
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
