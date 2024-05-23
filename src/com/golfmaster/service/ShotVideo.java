package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
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
		private String type;
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

	public Object[] processAnalyz(Long shot_data_id) {
		JSONObject jso = new JSONObject();
		String tempSDI = Long.toString(shot_data_id);
		String response = queryShotVideoAnalyz(tempSDI, jso);
		Object[] framesData = extractFrames(response);
		// 檢查是否有有效數據，如果無效則返回預設值
		if (framesData == null || ((int[]) framesData[0]).length == 0) {
			Logs.log(Logs.RUN_LOG, "Null framesData ");
			int[] defaultSideArray = { 257, 351, 402, 498 };
			int[] defaultFrontArray = { 239, 339, 388, 481 };
			Random ran = new Random();
			int aPos = ran.nextInt(6);
			int tPos = ran.nextInt(6);
			int iPos = ran.nextInt(6);
			int fPos = ran.nextInt(6);
			String defaultFrontVideoName = "Guest.1_shotVideo_front_161424_202404101614.mp4";
			String defaultSideVideoName = "Guest.1_shotVideo_side_161424_202404101614.mp4";
			return new Object[] { defaultSideArray, defaultFrontArray, defaultFrontVideoName, defaultSideVideoName,
					aPos, tPos, iPos, fPos };
		}
//		Object[] fD = new Object[framesData.length+1];
//		for (int idx = 0; idx < framesData.length; idx++) {
//			fD[idx] = framesData[idx];
//		}
//		fD[fD.length - 1] = true; 
		Logs.log(Logs.RUN_LOG, "Got framesData ");
		return framesData;
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
			strSQL = String.format(
					"SELECT * FROM golf_master.shot_video WHERE Player = '%s' AND id = '%s' ORDER BY Date Desc",
					paramData.player, paramData.video_id);
		}
		Logs.log(Logs.RUN_LOG, "queryShotVideo strSQL: " + strSQL);
		Logs.log(Logs.RUN_LOG,
				"queryShotVideo paramData.player: " + paramData.player + " paramData.video_id:" + paramData.video_id);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
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
				jsonProject.put("type", paramData.type);
//				jsonProject.put("analyze_shotVideo_front", rs.getString("analyze_shotVideo_front"));
//				jsonProject.put("analyze_shotVideo_side", rs.getString("analyze_shotVideo_side"));
				jsonProject.put("ClubType", rs.getString("ClubType"));
				jarrProjects.put(jsonProject);
				Logs.log(Logs.RUN_LOG, jsonProject.toString());
			}
			jsonResponse.put("success", true);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResponse.put("success", false);
			jsonResponse.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		jsonResponse.put("result", jarrProjects);
		return jarrProjects.length();
	}

	private String queryShotVideoAnalyz(String shot_data_id, JSONObject jsonResponse) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = null;
		JSONArray jarrProjects = new JSONArray();
		jsonResponse.put("result", jarrProjects);

		if (shot_data_id != null && !shot_data_id.isEmpty()) {
			strSQL = String.format(
					"SELECT SVS.PlayerPSystem AS PlayerPSystem, SVS.CamPos AS CamPos,SV.analyze_shotVideo_front ,SV.analyze_shotVideo_side,SVS.PoseImpact AS PoseImpact FROM golf_master.shot_video AS SV, golf_master.shot_video_swing AS SVS WHERE SV.shot_data_id = '%s' AND SVS.ShotVideoId = SV.id",
					shot_data_id);
		}
		Logs.log(Logs.RUN_LOG, "queryShotVideoAnalyz strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);

			boolean hasResults = false;
			while (rs.next()) {
				hasResults = true; // 確認至少有一行數據
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("CamPos", rs.getString("CamPos"));
				jsonProject.put("PlayerPSystem", rs.getString("PlayerPSystem"));
				jsonProject.put("analyze_shotVideo_front", rs.getString("analyze_shotVideo_front"));
				jsonProject.put("analyze_shotVideo_side", rs.getString("analyze_shotVideo_side"));
				jsonProject.put("PoseImpact", rs.getString("PoseImpact"));
				jarrProjects.put(jsonProject);
			}

			if (!hasResults) {
				Logs.log(Logs.RUN_LOG, "No data found for shot_data_id: " + shot_data_id);
				jsonResponse.put("success", false);
				jsonResponse.put("message", "No data found");
			} else {
				jsonResponse.put("success", true);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResponse.put("success", false);
			jsonResponse.put("message", e.getMessage());
		} finally {
			DBUtil.close(rs, stmt, conn);
		}

		jsonResponse.put("result", jarrProjects);
		return jsonResponse.toString();
	}

	private Object[] extractFrames(String jsonResponse) {
		ArrayList<Integer> sideFrames = new ArrayList<>();
		ArrayList<Integer> frontFrames = new ArrayList<>();
		String sideVideoName = "";
		String frontVideoName = "";
		// 修改變量以存儲最大值的索引
		int maxAIndex = -1, maxTIndex = -1, maxIIndex = -1, maxFIndex = -1;
		try {
			JSONObject responseObj = new JSONObject(jsonResponse);
			JSONArray results = responseObj.getJSONArray("result");
			if (results.length() == 0)
				return null; // 如果沒有數據，直接返回null

			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				String camPos = result.optString("CamPos");
				String playerPSystemStr = result.optString("PlayerPSystem");
				JSONObject playerPSystemObj = new JSONObject(playerPSystemStr);
				JSONArray data = playerPSystemObj.getJSONArray("data");

				if ("side".equals(camPos)) {
					for (int j = 0; j < data.length(); j++) {
						sideFrames.add(data.getInt(j));
					}
					sideVideoName = extractFileName(result.getString("analyze_shotVideo_side"));
				} else if ("front".equals(camPos)) {
					for (int j = 0; j < data.length(); j++) {
						frontFrames.add(data.getInt(j));
					}
					frontVideoName = extractFileName(result.getString("analyze_shotVideo_front"));
				}

				// 處理PoseImpact數據
				if (result.has("PoseImpact")) {
					JSONObject poseImpact = new JSONObject(result.getString("PoseImpact"));
					JSONObject dataObj = poseImpact.getJSONObject("data");
					
					maxAIndex = getMaxIndex(dataObj.getJSONArray("A"));
					maxTIndex = getMaxIndex(dataObj.getJSONArray("T"));
					maxIIndex = getMaxIndex(dataObj.getJSONArray("I"));
					maxFIndex = getMaxIndex(dataObj.getJSONArray("F"));
					if(maxAIndex<0)
						maxAIndex = 6;
					if(maxTIndex<0)
						maxTIndex = 6;
					if(maxIIndex<0)
						maxIIndex = 6;
					if(maxFIndex<0)
						maxFIndex = 6;

					Logs.log(Logs.RUN_LOG, "A:" + Integer.toString(maxAIndex) + "T:" + Integer.toString(maxTIndex) + "I"
							+ Integer.toString(maxIIndex) + "F" + Integer.toString(maxFIndex));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null; // 在發生錯誤時返回null
		}

		int[] sideArray = sideFrames.stream().mapToInt(i -> i).toArray();
		int[] frontArray = frontFrames.stream().mapToInt(i -> i).toArray();
		return new Object[] { sideArray, frontArray, frontVideoName, sideVideoName, maxAIndex, maxTIndex, maxIIndex,
				maxFIndex };
	}

	private String extractFileName(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	// 更新方法以計算數組中最大值的索引
	private int getMaxIndex(JSONArray array) throws JSONException {
		double max = Double.MIN_VALUE;
		int index = -1;
		for (int i = 0; i < array.length(); i++) {
			double value = array.getDouble(i);
			if (value > max) {
				max = value;
				index = i;
			}
		}
		return index;
	}

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData) {
		try {
			paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
			paramData.video_id = StringUtils.trimToEmpty(request.getParameter("video_id"));
			paramData.raw_shotVideo_front = StringUtils.trimToEmpty(request.getParameter("raw_shotVideo_front"));
			paramData.raw_shotVideo_side = StringUtils.trimToEmpty(request.getParameter("raw_shotVideo_side"));
			paramData.type = StringUtils.trimToEmpty(request.getParameter("type"));

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
//{"result":[{"PlayerPSystem":"{\"data\": [259, 361, 402, 498]}","analyze_shotVideo_side":"http://125.227.141.7:49147/GolfVisionAnalytics/service/download_video/143/Guest.1_shotVideo_side_161424_202404101614.mp4","analyze_shotVideo_front":"http://125.227.141.7:49147/GolfVisionAnalytics/service/download_video/143/Guest.1_shotVideo_front_161424_202404101614.mp4","shot_data_id":"117340"}],"success":true}