package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;

public class CourseStrategy extends DeviceData {
	public JSONObject processStrategy(HttpServletRequest request) {
		JSONObject jsobjParam = new JSONObject();
		try {
			printParam(request);
			String player = request.getParameter("player");
			String gender = request.getParameter("gender");
			int sleeppingTime = Integer.parseInt(request.getParameter("sleeppingTime"));
			String course = request.getParameter("course");
			String courseType = request.getParameter("courseType");
			String holeNumber = request.getParameter("holeNumber");
			String seniority = request.getParameter("seniority");
			if (course != null) {
				if (courseType != null) {
					if (holeNumber != null) {
						int coT = Integer.parseInt(courseType);
						int hN = Integer.parseInt(holeNumber);
						int g = Integer.parseInt(gender);
						JSONObject result = getCourseDistance(coT, hN, g);
						String par = result.getString("par");
						String distance = result.getString("distance");
						jsobjParam.put("player", player);
						jsobjParam.put("distance", distance);
						jsobjParam.put("par", par);
						jsobjParam.put("seniority", seniority);
						jsobjParam.put("sleeppingTime", sleeppingTime);
					} else {
						jsobjParam.put("holeNumber", false);
					}
				} else {
					jsobjParam.put("courseType", false);
				}
			} else {
				jsobjParam.put("course", false);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsobjParam.toString());
		return jsobjParam;
	}

	public JSONObject getCourseDistance(int courseID, int holeNumber, int gender) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();
		JSONObject jsonProject = null;
		JSONObject jsonResp = new JSONObject();
		String sex = null;
		if (gender == 0) {
			sex = "red";
		} else if (gender == 1) {
			sex = "white";
		}

		strSQL = String.format("SELECT course_name," + sex + "_tee_" + holeNumber + ",par_" + holeNumber
				+ " FROM golf_master.course_data WHERE id=%d", courseID);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("distance", rs.getString(sex + "_tee_" + holeNumber));
				jsonProject.put("par", rs.getString("par_" + holeNumber));

				jarrProj.put(jsonProject);
			}
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		Logs.log(Logs.RUN_LOG, jsonProject.toString());
		return jsonProject;
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
