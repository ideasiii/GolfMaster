package com.golfmaster.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.moduel.PSystem;

public class RealTimeShotsBenManchine extends DeviceData {

	private class BasicInfo {
		public String idx;
		public String LID;
		public String Date;
		public ShotData shotData;
	}

	private class ShotData {
		public float BallSpeed;
		public float ClubAnglePath;
		public float ClubAngleFace;
		public float CarryDistFt;
		public float LaunchAngle;
		public float ClubHeadSpeed;
		public float LaunchDirection;
		public float SideSpin;
		public float BackSpin;
		public String ClubType;
	}

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = new JSONObject();
		try {
			String shotData = request.getParameter("shotData");
			if (shotData == null || shotData.isEmpty()) {
				jsonResponse.put("success", false);
				jsonResponse.put("message", "Missing or invalid shotData parameter");
				return jsonResponse.toString();
			}

			BasicInfo info = getShotData(shotData);
			if (info == null || info.shotData == null) {
				jsonResponse.put("success", false);
				jsonResponse.put("message", "Invalid shotData structure");
				return jsonResponse.toString();
			}

			PSystem system = new PSystem();

			String analysisResult = system.expertAnalysis(info.shotData.BallSpeed, info.shotData.ClubAnglePath,
					info.shotData.ClubAngleFace, 0.0f, info.shotData.CarryDistFt, // Duplicate value to match existing
					info.shotData.LaunchAngle, 0.0f, // SmashFactor is not needed
					info.shotData.BackSpin, // BackSpin is not needed
					info.shotData.SideSpin, // SideSpin is not needed
					info.shotData.ClubHeadSpeed, info.shotData.LaunchDirection, 0.0f // DistToPinFt is not needed
			);

			JSONObject analysisJson = new JSONObject(analysisResult);
			analysisJson.put("BallSpeed", info.shotData.BallSpeed);
			analysisJson.put("ClubAnglePath", info.shotData.ClubAnglePath);
			analysisJson.put("ClubAngleFace", info.shotData.ClubAngleFace);
			analysisJson.put("LaunchAngle", info.shotData.LaunchAngle);
			analysisJson.put("LaunchDirection", info.shotData.LaunchDirection);
			analysisJson.put("ClubType", info.shotData.ClubType);
			analysisJson.put("CarryDistFt", info.shotData.CarryDistFt);
			analysisJson.put("ClubHeadSpeed", info.shotData.ClubHeadSpeed);
			// 添加 expert_update_time
			analysisJson.put("expert_update_time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			// 移除不必要的欄位 success
			analysisJson.remove("success");

            // 發送結果至 /analyze API
            String apiResponse = callAnalyzeAPI(analysisJson.toString());
            jsonResponse.put("analyze_response", apiResponse);
			
			jsonResponse.put("success", true);
			jsonResponse.put("result", analysisJson);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			jsonResponse.put("success", false);
			jsonResponse.put("message", "Error processing request");
		}
		return jsonResponse.toString();
	}
	
	public String processDataRequest(HttpServletRequest request) {
		JSONObject jsonResponse = new JSONObject();
		try {
			String shotData = request.getParameter("shot_data_id");
			if (shotData == null || shotData.isEmpty()) {
				jsonResponse.put("success", false);
				jsonResponse.put("message", "Missing or invalid shotData parameter");
				return jsonResponse.toString();
			}

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			jsonResponse.put("success", false);
			jsonResponse.put("message", "Error processing request");
		}
		return jsonResponse.toString();
	}
	private BasicInfo getShotData(String jsonStr) throws Exception {
		BasicInfo info = new BasicInfo();
		ShotData shotData = new ShotData();
		try {
			JSONObject infoJsonData = new JSONObject(jsonStr);
			info.idx = infoJsonData.optString("idx", null);
			info.LID = infoJsonData.optString("LID", null);
			info.Date = infoJsonData.optString("Date", null);

			JSONObject shotJsonData = infoJsonData.getJSONObject("ShotData");
			shotData.BallSpeed = shotJsonData.optFloat("BallSpeed", Float.NaN);
			shotData.ClubAnglePath = shotJsonData.optFloat("ClubAnglePath", Float.NaN);
			shotData.ClubAngleFace = shotJsonData.optFloat("ClubAngleFace", Float.NaN);
			shotData.LaunchAngle = shotJsonData.optFloat("LaunchAngle", Float.NaN);
			shotData.LaunchDirection = shotJsonData.optFloat("LaunchDirection", Float.NaN);
			shotData.ClubType = shotJsonData.optString("ClubType", null);
			shotData.CarryDistFt = shotJsonData.optFloat("CarryDistFt", Float.NaN);
			shotData.ClubHeadSpeed = shotJsonData.optFloat("ClubHeadSpeed", Float.NaN);

			info.shotData = shotData;
		} catch (Exception e) {
			throw e;
		}
		return info;
	}
	private String queryShotDataAndExpert(String shot_data_id, JSONObject jsonResponse) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = null;
		JSONArray jarrProjects = new JSONArray();
		jsonResponse.put("result", jarrProjects);

		if (shot_data_id != null && !shot_data_id.isEmpty()) {
			strSQL = String.format(
					"SELECT SVS.PlayerPSystem AS PlayerPSystem, SVS.CamPos AS CamPos, SVS.SwingPlane AS SwingPlane, "
							+ "SV.analyze_shotVideo_front, SV.analyze_shotVideo_side, SVS.PoseImpact AS PoseImpact "
							+ "FROM golf_master.shot_video AS SV, golf_master.shot_video_swing AS SVS "
							+ "WHERE SV.shot_data_id = '%s' AND SVS.ShotVideoId = SV.id",
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
				jsonProject.put("SwingPlane", rs.getString("SwingPlane"));
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

    private String callAnalyzeAPI(String analysisResult) throws Exception {
        URL url = new URL("http://127.0.0.1:5000/analyze");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("text", analysisResult);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(payload.toString().getBytes("UTF-8"));
        }

        InputStream is = connection.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
