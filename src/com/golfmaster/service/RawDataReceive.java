package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.Logs;

public class RawDataReceive {

	private class jmexParamData {
		String k = null;
	}

	private class spaceCapuleParamData {

	}

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = null;
		String strResponse = "{\"success\": true,\"result\": []}";

		printParam(request);
		jmexParamData jmasParamData = new jmexParamData();
		spaceCapuleParamData sCapuleParamData = new spaceCapuleParamData();

		Logs.log(Logs.RUN_LOG, "Response : " + strResponse);
		return strResponse;
	}

	public int insertJmexRawData(jmexParamData jparamData, JSONObject jsonObject) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();
		jsonObject.put("result", jarrProjects);
//		if(jparamData. .equals(null) || jparamData. .isEmpty()) {
//			return -1;
//		}

		strSQL = "INSERT INTO raw_data.JMEX () VALUES ();";
		return 0;
	}

	public int insertSpaceCapuleRawData(spaceCapuleParamData scparamData,JSONObject jsonObject) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();
		jsonObject.put("result", jarrProjects);
//		if(scparamData. .equals(null) || scparamData. .isEmpty()) {
//			return -1;
//		}

		strSQL ="INSERT INTO raw_data.SpaceCapule () VALUES ();";
		return 0;
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
