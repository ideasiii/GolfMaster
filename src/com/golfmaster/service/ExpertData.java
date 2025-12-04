/*
 * 顯示專家系統建議API
 * 參數: expert(必填)
 * http://localhost/GolfMaster/service/expert-data.jsp?expert=1
 * http://18.181.37.98/GolfMaster/service/expert-data.jsp?expert=1
 * http://localhost:8080/GolfMaster/service/expert-data.jsp?expert=1
 * http://61.216.149.161/GolfMaster/service/expert-data-v2.jsp?expert=11349
 */
package com.golfmaster.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
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
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.tool.FacialSpeakTest;

public class ExpertData extends DeviceData {

	@SuppressWarnings("unused")
	private class Expert {

		public Long shot_data_id;
		public String expert_trajectory;
		public String expert_p_system;
		public String expert_suggestion;
		public String expert_cause;
		public String expert_update_time;
	}

	public JSONObject processRequest(HttpServletRequest request) {
		JSONObject expert = null;
		FacialSpeakTest fst = new FacialSpeakTest();
//		String strResponse = "{\"success\": true,\"result\": []}";

		try {
			printParam(request);

			String expertID = request.getParameter("expert");
			String tempID = request.getParameter("temp");
			String fieldID = request.getParameter("LID");
			if (expertID != null && tempID == null) {
				expert = this.queryExpertData(Long.parseLong(expertID), tempID);
//				if (expert != null) {
//	                String expertTrajectory = expert.getString("expert_trajectory");
//	                String expertPSystem = expert.getString("expert_p_system");
//	                String expertSuggestion = expert.getString("expert_suggestion");
//	                String expertCause = expert.getString("expert_cause");
//
//	                // 調用 FacialSpeakTest 的 generateVideoUrl 方法
//	                String videoUrl = fst.generateVideoUrl(expertTrajectory, expertPSystem, expertSuggestion, expertCause);
//	                expert.put("video_url", videoUrl);
//	                if (videoUrl != null && !videoUrl.isEmpty()) {
//	                    expert.put("video_url", videoUrl);
//	                } else {
//	                    expert.put("video_url", "生成視頻失敗");
//	                }
//	            }
//				String imgFile = "";
				boolean result = false;
				String psystem = expert.getString("expert_p_system");
				if (psystem != null && !psystem.isEmpty()) {
					if (psystem.toUpperCase().indexOf("P") >= 0) {
						String vf = "";
						String p = psystem.substring(0, 2);
						if ("P1".equals(p) || "P2".equals(p) || "P3".equals(p) || "P4".equals(p)) {
//							vf = "P1_P4.mp4";
							vf = "P1_P10v5.mp4";
						}
						if ("P5".equals(p) || "P6".equals(p) || "P7".equals(p)) {
//							vf = "P5_P7.mp4";
							vf = "P1_P10v5.mp4";
						}
						if ("P8".equals(p) || "P9".equals(p) || "P10".equals(p)) {
//							vf = "P8_P10.mp4";
							vf = "P1_P10v5.mp4";
						}

						result = true;
						expert.put("video", vf);
					} else {
						if (psystem.contains("擊球失敗")) {
//							imgFile = "warning.png";
							expert.put("img_name", "warning.png");
						}
					}
				}

//				expert.put("img_name", imgFile);
				expert.put("result", result);
			} else if (expertID != null && tempID != null) {
				boolean result = false;

				expert = this.queryExpertData(Long.parseLong(expertID), tempID);
				String psystem = expert.getString("expert_p_system");
				if (psystem != null && !psystem.isEmpty()) {
					if (psystem.toUpperCase().indexOf("P") >= 0) {
						String vf = "";
						String p = psystem.substring(0, 2);
						if ("P1".equals(p) || "P2".equals(p) || "P3".equals(p) || "P4".equals(p)) {
							vf = "P1_P10v5.mp4";
						}
						if ("P5".equals(p) || "P6".equals(p) || "P7".equals(p)) {
							vf = "P1_P10v5.mp4";
						}
						if ("P8".equals(p) || "P9".equals(p) || "P10".equals(p)) {
							vf = "P1_P10v5.mp4";
						}

						result = true;
						expert.put("video", vf);
					} else {
						if (psystem.contains("擊球失敗")) {
							expert.put("img_name", "warning.png");
						}
					}
				}
				expert.put("result", result);
			} else if (expertID == null && tempID == null && fieldID != null) {
				expert = this.queryExpertDataByLID(fieldID);

				boolean result = false;
				String psystem = expert.getString("expert_p_system");
				if (psystem != null && !psystem.isEmpty()) {
					if (psystem.toUpperCase().indexOf("P") >= 0) {
						String vf = "";
						String p = psystem.substring(0, 2);
						if ("P1".equals(p) || "P2".equals(p) || "P3".equals(p) || "P4".equals(p)) {
							vf = "P1_P10v5.mp4";
						}
						if ("P5".equals(p) || "P6".equals(p) || "P7".equals(p)) {
							vf = "P1_P10v5.mp4";
						}
						if ("P8".equals(p) || "P9".equals(p) || "P10".equals(p)) {
							vf = "P1_P10v5.mp4";
						}

						result = true;
						expert.put("video", vf);
					} else {
						if (psystem.contains("擊球失敗")) {
							expert.put("img_name", "warning.png");
						}
					}
				}
				expert.put("result", result);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}

		Logs.log(Logs.RUN_LOG, "Response : " + (expert != null ? expert : null));

		return (expert != null ? expert : null);
	}

	private String getGPTSummary(JSONObject expert) {
		String summary = "無法獲取摘要"; // 預設值

		try {
			if (expert == null) {
				throw new IllegalArgumentException("The 'expert' JSONObject cannot be null.");
			}

			String psystem = expert.optString("expert_p_system", expert.getString("psystem"));
			String trajectory = expert.optString("expert_trajectory", expert.getString("trajectory"));
			String cause = expert.optString("expert_cause", expert.getString("cause"));
			String suggestion = expert.optString("expert_suggestion", expert.getString("suggestion"));

			// 構建要傳遞給GPT API的文本
			String text = "彈道: " + trajectory + ", 原因: " + cause + ", 建議: " + suggestion;

			// 呼叫GPT API
			String endpoint = "https://api.openai.com/v1/chat/completions";
			URL url = new URL(endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer my key"); // 使用你的API金鑰
			connection.setDoOutput(true);

			// 構建請求體
			JSONObject jsonBody = new JSONObject();
			jsonBody.put("model", "gpt-4");
			JSONArray messages = new JSONArray();
			JSONObject message = new JSONObject();
			message.put("role", "user");
			message.put("content", text);
			messages.put(message);
			jsonBody.put("messages", messages);
			jsonBody.put("max_tokens", 50);
			jsonBody.put("temperature", 0.7);

			// 發送請求
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = jsonBody.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}

			// 獲取回應
			try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
				StringBuilder response = new StringBuilder();
				String responseLine;
				while ((responseLine = br.readLine()) != null) {
					response.append(responseLine.trim());
				}

				JSONObject responseObject = new JSONObject(response.toString());
				summary = responseObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message")
						.getString("content");
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}

		return summary;
	}

	private JSONObject checkParam(String shotData) throws Exception {
		JSONObject result = null;
		try {
			if (shotData == null || StringUtils.trimToEmpty(shotData).isEmpty()) {
				result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			} else {
//				boolean hasKey = true;
				JSONObject main = new JSONObject(shotData);
				String[] keys = { "idx", "LID", "Player", "Date", "Token", "ShotData" };
				for (int i = 0; i < keys.length; i++) {
					if (main.has(keys[i])) {
//						hasKey = false;
						result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
						break;
					}
				}
				if (result == null) {
					for (String key : main.keySet()) {
						if (main.get(key) == null || main.get(key).toString().isEmpty()) {
							result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
							break;
						}
					}

					JSONObject sd = main.getJSONObject("ShotData");
					DeviceData.WrgData wrgData = new DeviceData.WrgData();
					Field[] fields = wrgData.getClass().getDeclaredFields();

					for (int i = 0; i < fields.length; i++) {
						String fieldName = fields[i].getName();
						if (!sd.has(fieldName)) {
							result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
							break;
						} else {
							if (sd.get(fieldName) == null || sd.get(fieldName).toString().isEmpty()) {
								result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
								break;
							}
						}
					}
				}
				/*
				 * if(!hasKey) { return ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
				 * }else { for(String key : main.keySet()) { if(main.get(key) == null ||
				 * main.get(key).toString().isEmpty()) {
				 * 
				 * } } }
				 */
			}
		} catch (Exception e) {
			throw e;
		}

		return result;
	}

	private JSONObject queryExpertData(long ExpertId, String temp) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONObject jsonExpert = null;

		strSQL = "SELECT * FROM expert WHERE id = " + ExpertId;

		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			if (temp == null) {
				while (rs.next()) {
					jsonExpert = new JSONObject();

					jsonExpert.put("expert_trajectory", rs.getString("expert_trajectory"));
					jsonExpert.put("expert_note", rs.getString("expert_note"));
					jsonExpert.put("expert_p_system", rs.getString("expert_p_system"));
					jsonExpert.put("expert_suggestion", rs.getString("expert_suggestion"));
					jsonExpert.put("expert_cause", rs.getString("expert_cause"));
					jsonExpert.put("shotdata_id", rs.getLong("shot_data_id"));
					jsonExpert.put("id", rs.getLong("id"));
				}
			} else if (temp != null) {
				while (rs.next()) {
					jsonExpert = new JSONObject();

					jsonExpert.put("expert_trajectory", "Pull Hook 左拉左曲球");

					jsonExpert.put("expert_p_system", "P5~6 下桿");
					jsonExpert.put("expert_suggestion", "下桿至Impact階段，左手臂打直、左手腕維持固定");
					jsonExpert.put("expert_cause", "Impact階段，手腕與教練差異最大，下桿角度過於陡峭，手腕過度彎曲，過度由內而外的路徑");
					jsonExpert.put("shotdata_id", rs.getLong("shot_data_id"));
					jsonExpert.put("id", rs.getLong("id"));
				}
			}

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}

		DBUtil.close(rs, stmt, conn);

		return jsonExpert;
	}

	private JSONObject queryExpertDataByLID(String shotData_LID) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL = null;
		JSONObject jsonExpert = null;

		// 1. 檢查輸入的 LID 是否有效
		if (shotData_LID != null && !shotData_LID.trim().isEmpty()) {
			// 2. 構建 SQL 查詢語句
			// 變數名稱 shotData_LID 更清晰地表達它來自 shot_data
			strSQL = String.format("SELECT e.* "
								+ "FROM expert AS e "
								+ "INNER JOIN shot_data AS s ON e.shot_data_id = s.id "
								+ "WHERE s.LID = '%s' " // 用單引號包住 LID，因為它是字元型別
								+ "ORDER BY s.Date DESC "
								+ "LIMIT 1", shotData_LID);
		} else {
			// 如果 LID 無效，直接返回 null 或拋出異常
			return null;
		}

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);

			while (rs.next()) {
				jsonExpert = new JSONObject();

				jsonExpert.put("expert_trajectory", rs.getString("expert_trajectory"));
				jsonExpert.put("expert_note", rs.getString("expert_note"));
				jsonExpert.put("expert_p_system", rs.getString("expert_p_system"));
				jsonExpert.put("expert_suggestion", rs.getString("expert_suggestion"));
				jsonExpert.put("expert_cause", rs.getString("expert_cause"));
				jsonExpert.put("shotdata_id", rs.getLong("shot_data_id"));
				jsonExpert.put("id", rs.getLong("id"));
			}

		} catch(Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		} finally {
			DBUtil.close(rs, stmt, conn);
		}

		return jsonExpert;
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
