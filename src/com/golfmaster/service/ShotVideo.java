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
import com.golfmaster.service.CallMotionApi;

public class ShotVideo {
	private class ParamData {
		private String video_id;
		private String player;
		private String maxLimit;
		private String raw_shotVideo_front;
		private String raw_shotVideo_side;
		private boolean callMotionApi = false;
	}

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = null;
		CallMotionApi callMotionApi = new CallMotionApi();
		String strResponse = "{\"success\": true,\"result\": []}";

		printParam(request);
		ShotVideo.ParamData paramData = new ShotVideo.ParamData();
		jsonResponse = requestAndTrimParams(request, paramData);
		if (null != jsonResponse)
			return jsonResponse.toString();

		jsonResponse = new JSONObject();
		if (0 < queryShotVideo(paramData, jsonResponse))
			strResponse = jsonResponse.toString();
		try {
			callMotionApi.requestApi(jsonResponse);
		} catch (Exception e) {
			e.printStackTrace();
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + strResponse);
		return strResponse;
	}

	private int queryShotVideo(ParamData paramData, JSONObject jsonResponse) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = null;
		String strExpert;
		JSONArray jarrProjects = new JSONArray();

		jsonResponse.put("result", jarrProjects);
		// 無日期就取最近的10筆
		if (paramData.video_id != null && !paramData.video_id.isEmpty()) {
			strSQL = String.format("SELECT * FROM shot_video WHERE Player = '%s' AND id = '%s' ORDER BY Date Desc", paramData.player,
					paramData.video_id);
		} 
//		else {
//			strSQL = String.format("SELECT * FROM shot_video WHERE Player = '%s' order by Date DESC LIMIT 1",
//					paramData.player);
//		}
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			Logs.log(Logs.RUN_LOG, "conn: " + conn.toString());
			stmt = conn.createStatement();
			Logs.log(Logs.RUN_LOG, "conn stmt: " + stmt.toString());
			rs = stmt.executeQuery(strSQL);
			Logs.log(Logs.RUN_LOG, "rs: " + rs.toString());
			while (rs.next()) {
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("id", rs.getInt("id"));
				jsonProject.put("idx", rs.getString("idx"));
				jsonProject.put("LID", rs.getString("LID")); // 每個不同模擬器配合場域的odd
				jsonProject.put("Player", rs.getString("Player"));
				jsonProject.put("Date", rs.getString("Date"));
				jsonProject.put("Date_str", rs.getString("Date_str"));
				jsonProject.put("shot_data_id", rs.getString("shot_data_id"));
				jsonProject.put("raw_shotVideo_front", rs.getString("raw_shotVideo_front"));
				jsonProject.put("raw_shotVideo_side", rs.getString("raw_shotVideo_side"));
				jsonProject.put("raw_headVideo", rs.getString("raw_headVideo"));
				jsonProject.put("analyze_shotVideo_front", rs.getString("analyze_shotVideo_front"));
				jsonProject.put("analyze_shotVideo_side", rs.getString("analyze_shotVideo_side"));
				jsonProject.put("id_analyzeVideo", rs.getString("id_analyzeVideo"));
				jsonProject.put("ClubType", rs.getString("ClubType"));
				jarrProjects.put(jsonProject);
			}
			jsonResponse.put("success", true);
		}
		catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResponse.put("success", false);
			jsonResponse.put("message", e.getMessage());
		}
		Logs.log(Logs.RUN_LOG, "DB Closing");
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, "DB Close");
		jsonResponse.put("result", jarrProjects);
		return jarrProjects.length();
	}

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData) {
		try {
			paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
			paramData.video_id = StringUtils.trimToEmpty(request.getParameter("video_id"));
			paramData.raw_shotVideo_front = StringUtils.trimToEmpty(request.getParameter("raw_shotVideo_front"));
			paramData.raw_shotVideo_side = StringUtils.trimToEmpty(request.getParameter("raw_shotVideo_side"));

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
