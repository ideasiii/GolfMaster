package com.golfmaster.service;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
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
			
			String analysisResult = system.expertAnalysis(
					info.shotData.BallSpeed, 
					info.shotData.ClubAnglePath,
					info.shotData.ClubAngleFace, 
					0.0f, 
					info.shotData.CarryDistFt, // Duplicate value to match existing
					info.shotData.LaunchAngle, 
					0.0f, // SmashFactor is not needed
					0.0f, // BackSpin is not needed
					0.0f, // SideSpin is not needed
					info.shotData.ClubHeadSpeed, 
					info.shotData.LaunchDirection, 
					0.0f // DistToPinFt is not needed
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

			jsonResponse.put("success", true);
			jsonResponse.put("result", analysisJson);
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
}
