/*
 * 擊球數據API
 * 參數: player(必填),start_date,end_date (無日期則抓最近10筆)
 * http://localhost/GolfMaster/service/shot-data.jsp?player=Guest.1
 * http://175.41.245.90/GolfMaster/service/shot-data.jsp?player=Guest.1
 * http://localhost:8080/GolfMaster/service/shot-data.jsp?player=Guest.1
 * http://localhost:80/GolfMaster/service/shot-data.jsp?player=Guest.1
 * http://61.216.149.161/GolfMaster/service/shot-data.jsp?player=Guest.1
 */
package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.PSystem;

public class ShotData {
	private class ParamData {
		private String player;
		private String start_date;
		private String end_date;
	}

	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = null;
		String strResponse = "{\"success\": true,\"result\": []}";

		printParam(request);
		ShotData.ParamData paramData = new ShotData.ParamData();
		jsonResponse = requestAndTrimParams(request, paramData);
		if (null != jsonResponse)
			return jsonResponse.toString();

		jsonResponse = new JSONObject();
		if (0 < queryShotData(paramData, jsonResponse))
			strResponse = jsonResponse.toString();

		Logs.log(Logs.RUN_LOG, "Response : " + strResponse);
		return strResponse;
	}

	public JSONObject processIRITData(Long shot_data_id) {
		String player = queryPlayer(shot_data_id);
		JSONObject jsonObject = null;
		if (player != null || !player.isEmpty()) {
			jsonObject = queryIRITData(player);
		}
		return jsonObject;
	}

	public float[][] processPlayerReq(Long shot_data_id) {
		String player = queryPlayer(shot_data_id);
		float[][] playerBallSpeed = null;
		if (player != null || !player.isEmpty()) {
			playerBallSpeed = queryBallSpeed(player);
		}

		return playerBallSpeed;
	}

	private String queryPlayer(Long shot_data_id) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		String player = null;

		strSQL = String.format("SELECT * FROM golf_master.shot_data WHERE id = '%d'", shot_data_id);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				player = rs.getString("Player");

			}
			System.out.println(player);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return player;
	}

	private float[][] queryBallSpeed(String player) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		// BallSpeed and ClubHeadSpeed
		float[][] BSAndCHS = new float[5][10];
		// 取最近的10筆
		strSQL = String.format("SELECT * FROM golf_master.shot_data WHERE Player = '%s' order by Date DESC LIMIT 10",
				player);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			int i = 0;
			while (rs.next()) {
				BSAndCHS[0][i] = rs.getFloat("BallSpeed");
				BSAndCHS[1][i] = rs.getFloat("ClubHeadSpeed");
				BSAndCHS[2][i] = rs.getFloat("TotalDistFt");
				BSAndCHS[3][i] = rs.getFloat("LaunchAngle");
				BSAndCHS[4][i] = rs.getFloat("BackSpin");
				i++;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return BSAndCHS;
	}

	private JSONObject queryIRITData(String player) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONObject jsonProject = null;
		strSQL = String.format(
				"SELECT BallSpeed,LaunchAngle,BackSpin,LaunchDirection,SideSpin FROM golf_master.shot_data WHERE Player = '%s' order by Date DESC LIMIT 1;",
				player);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("BallSpeed", rs.getFloat("BallSpeed"));
				jsonProject.put("LaunchAngle", rs.getFloat("LaunchAngle"));
				jsonProject.put("BackSpin", rs.getFloat("BackSpin"));
				jsonProject.put("LaunchDirection", rs.getFloat("LaunchDirection"));
				jsonProject.put("SideSpin", rs.getFloat("SideSpin"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return jsonProject;
	}

	private int queryShotData(ParamData paramData, JSONObject jsonResponse) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		String strExpert;
		JSONArray jarrProjects = new JSONArray();
		PSystem psystem = new PSystem();

		jsonResponse.put("result", jarrProjects);
		// 無日期就取最近的10筆
		if (StringUtils.isEmpty(paramData.start_date) || StringUtils.isEmpty(paramData.end_date)) {
			strSQL = String.format("SELECT * FROM shot_data WHERE Player = '%s' order by Date DESC LIMIT 10",
					paramData.player);
		} else {
			strSQL = String.format(
					"SELECT * FROM shot_data WHERE Player = '%s' AND Date >= '%s 00:00:00' AND Date <= '%s 23:59:59' order by Date DESC",
					paramData.player, paramData.start_date, paramData.end_date);
		}
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("id", rs.getInt("id"));
				jsonProject.put("LID", rs.getString("LID")); // 每個不同模擬器配合場域的odd
				jsonProject.put("Player", rs.getString("Player"));
				jsonProject.put("Date", rs.getString("Date"));
				// jsonProject.put("shotVideo_front", rs.getString("shotVideo_front")); //正面影片
				// jsonProject.put("shotVideo_side", rs.getString("shotVideo_side")); //側面影片
				jsonProject.put("BallSpeed", rs.getDouble("BallSpeed")); // 高爾夫球在撞擊後立即的速度，球速是由球桿速度和衝擊力決定的。*技術定義：球速係指高爾夫球重心與桿面分離後的速度\n單位:
																			// mph
				jsonProject.put("LaunchAngle", rs.getDouble("LaunchAngle")); // 隨著球速的降低，最佳發射角度必須增加，後旋也必須增加。單位: degree
				jsonProject.put("LaunchDirection", rs.getDouble("LaunchDirection")); // 發射方向是球相對於目標線開始的初始方向。正發射方向表示球從目標右側開始，負發射方向......單位:
																						// degree
				jsonProject.put("ClubHeadSpeed", rs.getDouble("ClubHeadSpeed")); // 從身體設置，到上桿頂點身體旋轉，讓手臂和身體將保持連接，保持在正確的揮桿平面上，並使用大肌肉來創造擊球過程。(握壓/Swing/釋放/節奏)，單位:
																					// mph
				jsonProject.put("ClubAngleFace", rs.getDouble("ClubAngleFace")); // 高爾夫球手將此稱為具有“開放”或“封閉”桿面。*技術定義：在高爾夫球最大壓縮時，球桿面和高爾夫球接觸中心點的水平球桿面方向，單位:
																					// degree
				jsonProject.put("ClubAnglePath", rs.getDouble("ClubAnglePath")); // 高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)\\\\n技術定義：FACE
																					// ANGLE 和 CLUB PATH 定義的角度差（FACE
																					// ANGLE 減去 CLUB PATH）。單位: degree'
				jsonProject.put("BackSpin", rs.getInt("BackSpin")); // 高爾夫球迴旋是高爾夫球的反向旋轉
				jsonProject.put("SideSpin", rs.getInt("SideSpin")); // 高爾夫球側旋是橫向發生的旋轉
				jsonProject.put("SmashFactor", rs.getDouble("SmashFactor")); // 球桿速度除以球速
				jsonProject.put("ClubType", rs.getString("ClubType")); // 幾號球桿、材質
				jsonProject.put("DistToPinFt", rs.getDouble("DistToPinFt")); // 擊球後與目標的距離，單位: ft 呎
				jsonProject.put("CarryDistFt", rs.getDouble("CarryDistFt")); // 置球點到擊球落點的距離，單位: ft 呎
				jsonProject.put("TotalDistFt", rs.getDouble("TotalDistFt")); // 置球點到擊球後停止滾動的距離，單位: ft 呎
				// jsonProject.put("expert_note", rs.getString("expert_note"));
				// jsonProject.put("expert_trajectory", rs.getString("expert_trajectory"));
				// jsonProject.put("expert_p_system", rs.getString("expert_p-system"));
				// jsonProject.put("expert_suggestion", rs.getString("expert_suggestion"));
				// jsonProject.put("expert_cause", rs.getString("expert_cause"));
				strExpert = psystem.expertAnalysis((float) rs.getDouble("BallSpeed"),
						(float) rs.getDouble("ClubAnglePath"), (float) rs.getDouble("ClubAngleFace"),
						(float) rs.getDouble("TotalDistFt"), (float) rs.getDouble("CarryDistFt"),
						(float) rs.getDouble("LaunchAngle"), (float) rs.getDouble("SmashFactor"),
						(float) rs.getInt("BackSpin"), (float) rs.getInt("SideSpin"),
						(float) rs.getDouble("ClubHeadSpeed"), (float) rs.getDouble("LaunchDirection"),
						(float) rs.getDouble("DistToPinFt"));

				// 分析網路測試 ==============================
				// AnalysisNetwork analysisNetWork = new AnalysisNetwork();
				// analysisNetWork.expertAnalysis((float)rs.getDouble("BallSpeed"),
				// (float)rs.getDouble("ClubAnglePath"), (float)rs.getDouble("ClubAngleFace"),
				// (float)rs.getDouble("TotalDistFt"), (float)rs.getDouble("CarryDistFt"),
				// (float)rs.getDouble("LaunchAngle"), (float)rs.getDouble("SmashFactor"),
				// (float)rs.getInt("BackSpin"), (float)rs.getInt("SideSpin"),
				// (float)rs.getDouble("ClubHeadSpeed"), (float)rs.getDouble("LaunchDirection"),
				// (float)rs.getDouble("DistToPinFt"));
				// ===========================================

				JSONObject jsonExpert = new JSONObject(strExpert);
				jsonProject.put("expert_suggestion", jsonExpert.getString("expert_suggestion"));
				jsonProject.put("expert_cause", jsonExpert.getString("expert_cause"));
				jsonProject.put("expert_trajectory", jsonExpert.getString("expert_trajectory"));
				jsonProject.put("expert_p_system", jsonExpert.getString("expert_p_system"));

				jarrProjects.put(jsonProject);
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

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData) {
		try {
			paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
			paramData.start_date = StringUtils.trimToEmpty(request.getParameter("start_date"));
			paramData.end_date = StringUtils.trimToEmpty(request.getParameter("end_date"));

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
