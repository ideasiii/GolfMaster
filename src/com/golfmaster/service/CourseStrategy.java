package com.golfmaster.service;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;

public class CourseStrategy extends DeviceData {
	public JSONObject processStrategy(HttpServletRequest request) {
		JSONObject jsobjParam = new JSONObject();
		try {
			printParam(request);
			String player = request.getParameter("player");
			String distance = request.getParameter("distance");
			String age = request.getParameter("age");
			String tee = request.getParameter("tee");
			if (distance != null || !distance.isEmpty()) {
				jsobjParam.put("player", player);
				jsobjParam.put("distance", distance);
				jsobjParam.put("age", age);
				jsobjParam.put("tee", tee);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsobjParam.toString());
		return jsobjParam;
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
