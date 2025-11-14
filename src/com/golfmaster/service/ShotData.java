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
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.LevelSystem;
import com.golfmaster.moduel.PSystem;
import com.golfmaster.moduel.ShortGameData;


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
		JSONObject jsb = queryPlayer(shot_data_id);
		String player = jsb.getString("player");
		JSONObject jsonObject = null;
		if (player != null || !player.isEmpty()) {
			jsonObject = queryIRITData(player);
		}
		return jsonObject;
	}

	public float[][] processPlayerReq(Long shot_data_id) {
		JSONObject jsb = queryPlayer(shot_data_id);
		String player = jsb.getString("player");
		String ClubType = jsb.getString("ClubType");
		float[][] playerBallSpeed = null;
		if (player != null || !player.isEmpty()) {
			playerBallSpeed = queryBallSpeed(player, shot_data_id, ClubType);
		}

		return playerBallSpeed;
	}

	public float[][] processPlayerReqOld(Long shot_data_id) {
		JSONObject jsb = queryPlayer(shot_data_id);
		String player = jsb.getString("player");
		float[][] playerBallSpeed = null;
		if (player != null || !player.isEmpty()) {
			playerBallSpeed = queryBallSpeedLastTen(player);
		}

		return playerBallSpeed;
	}

	public String processCurrShotData(Long shot_data_id) {
		JSONObject jsonResponse = null;
		String strResponse = "";

		JSONObject jsb = queryCurrShotData(shot_data_id);

		if (jsb != null) {
			strResponse = jsb.toString();
		}

		System.out.println(
			"CurrShotData strResponse: " + strResponse
		);

		return strResponse;
	}

	public String processShortGameData(Long shot_data_id) {
		JSONObject jsonResponse = null;
		String strResponse = "";

		JSONObject jsb = queryPlayer(shot_data_id);
		int maxRecords = 100;

		String player = jsb.getString("player");
		String clubType = jsb.getString("ClubType");
		String endDate = jsb.getString("Date");
		// test
		// String player = "Guest.1";
		// String clubType = "7Iron";
		// String clubType = "SandWedge";
		// String clubType = "GapWedge";
		// String endDate = "2025-09-30 23:27:14";
		// String endDate = "2025-10-02 10:32:36";

		if (player != null || !player.isEmpty()) {
			ShortGameData shortGameData = new ShortGameData();
			List<ShortGameData.ShortGameShotData> shotDataList = new ArrayList<>();

			shotDataList = queryShortGameData(player, clubType, endDate, maxRecords);
			jsonResponse = shortGameData.processAnalyz(shotDataList, clubType);

			// System.out.println(
			// 	"ShortGameData Player: " + player
			// );

			// System.out.println(
			// 	"ShortGameData ClubType: " + clubType
			// );

			// System.out.println(
			// 	"ShortGameData shotDataList.size(): " + shotDataList.size()
			// );

			// System.out.println(
			// 	"ShortGameData.processAnalyz jsonResponse: " + jsonResponse.toString()
			// );

			if (jsonResponse != null) {
				strResponse = jsonResponse.toString();
			}
		}

		return strResponse;
	}

	private JSONObject queryPlayer(Long shot_data_id) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		String player = null;
		JSONObject jsb = new JSONObject();
		strSQL = String.format("SELECT * FROM golf_master.shot_data WHERE id = '%d'", shot_data_id);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsb.put("player", rs.getString("Player"));
				jsb.put("ClubType", rs.getString("ClubType"));
				jsb.put("Date", rs.getString("Date"));
			}
			System.out.println(player);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return jsb;
	}

	private float[][] queryBallSpeed(String player, Long shot_data_id, String ClubType) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		// BallSpeed and ClubHeadSpeed
		float[][] BSAndCHS = new float[6][10];
		// 取最近的10筆
		strSQL = String.format(
				"(SELECT 'Last' AS RecordType, BallSpeed, ClubHeadSpeed, round((CarryDistFt/3), 2) AS CarryDistFt, LaunchAngle, BackSpin , LaunchDirection "
						+ " FROM golf_master.shot_data " + " WHERE Player = '%s' " + " AND id = '%d' " + " LIMIT 1) "
						+ "UNION ALL "
						+ "(SELECT 'Average' AS RecordType, AVG(BallSpeed), AVG(ClubHeadSpeed), AVG(round((CarryDistFt/3), 2)), AVG(LaunchAngle), AVG(BackSpin), AVG(LaunchDirection) "
						+ " FROM golf_master.shot_data " + " WHERE Player = '%s' " + "AND ClubType = '%s')",
				player, shot_data_id, player, ClubType);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				String recordType = rs.getString("RecordType");
				int colIndex = recordType.equals("Last") ? 0 : 1;

				BSAndCHS[0][colIndex] = rs.getFloat("BallSpeed");
				BSAndCHS[1][colIndex] = rs.getFloat("ClubHeadSpeed");
				BSAndCHS[2][colIndex] = rs.getFloat("CarryDistFt");
				BSAndCHS[3][colIndex] = rs.getFloat("LaunchAngle");
				BSAndCHS[4][colIndex] = rs.getFloat("BackSpin");
				BSAndCHS[5][colIndex] = rs.getFloat("LaunchDirection");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return BSAndCHS;
	}

	private float[][] queryBallSpeedLastTen(String player) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		// BallSpeed and ClubHeadSpeed
		float[][] BSAndCHS = new float[5][10];
		// 取最近的10筆
		strSQL = String.format("SELECT * FROM golf_master.shot_data WHERE Player = '%s' ORDER BY Date DESC limit 10",
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
		String strLevel;
		JSONArray jarrProjects = new JSONArray();
		PSystem psystem = new PSystem();
		LevelSystem levelSystem = new LevelSystem();
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
				strExpert = psystem.expertAnalysis((float) rs.getDouble("BallSpeed"),
						(float) rs.getDouble("ClubAnglePath"), (float) rs.getDouble("ClubAngleFace"),
						(float) rs.getDouble("TotalDistFt"), (float) rs.getDouble("CarryDistFt"),
						(float) rs.getDouble("LaunchAngle"), (float) rs.getDouble("SmashFactor"),
						(float) rs.getInt("BackSpin"), (float) rs.getInt("SideSpin"),
						(float) rs.getDouble("ClubHeadSpeed"), (float) rs.getDouble("LaunchDirection"),
						(float) rs.getDouble("DistToPinFt"));

				strLevel = levelSystem.expertAnalysis((float) rs.getDouble("BallSpeed"),
						(float) rs.getDouble("ClubAnglePath"), (float) rs.getDouble("ClubAngleFace"),
						(float) rs.getDouble("TotalDistFt"), (float) rs.getDouble("CarryDistFt"),
						(float) rs.getDouble("LaunchAngle"), (float) rs.getDouble("SmashFactor"),
						(float) rs.getInt("BackSpin"), (float) rs.getInt("SideSpin"),
						(float) rs.getDouble("ClubHeadSpeed"), (float) rs.getDouble("LaunchDirection"),
						(float) rs.getDouble("DistToPinFt"), (String) rs.getString("ClubType"));
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

	private JSONObject queryCurrShotData(Long shot_data_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		JSONObject jsonProject = null;

		// query command
        String strSQL = "SELECT * FROM golf_master.shot_data WHERE id = ?";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL); // 準備 PreparedStatement
			pstmt.setLong(1, shot_data_id); // 設置參數，取代 SQL 語句中的

			rs = pstmt.executeQuery(); // 執行查詢 (不帶參數)
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("id", rs.getInt("id"));
				jsonProject.put("BallSpeed", rs.getDouble("BallSpeed")); // 單位: mph
				jsonProject.put("LaunchAngle", rs.getDouble("LaunchAngle")); // 單位: degree
				jsonProject.put("LaunchDirection", rs.getDouble("LaunchDirection")); // 單位: degree
				jsonProject.put("ClubHeadSpeed", rs.getDouble("ClubHeadSpeed")); // 單位: mph
				jsonProject.put("ClubAngleFace", rs.getDouble("ClubAngleFace")); // 單位: degree
				jsonProject.put("ClubAnglePath", rs.getDouble("ClubAnglePath")); // 單位: degree
				jsonProject.put("BackSpin", rs.getInt("BackSpin")); // 單位: rpm
				jsonProject.put("SideSpin", rs.getInt("SideSpin")); // 單位: rpm
				jsonProject.put("SmashFactor", rs.getDouble("SmashFactor")); // 球桿速度除以球速
				jsonProject.put("ClubType", rs.getString("ClubType")); // 幾號球桿、材質
				jsonProject.put("DistToPinFt", rs.getDouble("DistToPinFt")); // 單位: ft 呎
				jsonProject.put("CarryDistFt", rs.getDouble("CarryDistFt")); // 單位: ft 呎
				jsonProject.put("TotalDistFt", rs.getDouble("TotalDistFt")); // 單位: ft 呎
			}

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		return jsonProject;
	}

	private List<ShortGameData.ShortGameShotData> queryShortGameData(
		String player,
		String clubType,
		String endDate,
		int maxRecords
	) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		List<ShortGameData.ShortGameShotData> shotList = new ArrayList<>();

		// query command
        String strSQL = "SELECT "
            + "t1.id, t1.Date, "
            + "t1.Player, t1.ClubType, "
            + "t1.CarryDistFt, t1.TotalDistFt, t1.LaunchDirection, "
            + "t1.ClubHeadSpeed, t1.BackSpin, t1.SideSpin, t1.SmashFactor "
            + "FROM golf_master.shot_data t1 "
            + "WHERE "
            + "t1.Player = ? " // 參數 1: Player
            + "AND t1.ClubType = ? " // 參數 2: ClubType
            + "AND Date <= ? " // 參數 3: Date 限制 (在指定時間點之前)
			// + "AND t1.CarryDistFt <= %d "
			// + "AND t1.ClubHeadSpeed BETWEEN %d AND %d "
			// + "AND t1.SmashFactor BETWEEN %f AND %f "
            // 你可以根據需要啟用或停用下面兩行（極值過濾）
            // + "AND t1.CarryDistFt > 0 "
            // + "AND ABS(t1.LaunchDirection) < 45 "
            + "ORDER BY "
            + "t1.id DESC "
            + "LIMIT ?"; // 參數 4: maxRecords

		// player, clubName, maxCarryDistFt, minClubSpeed, maxClubSpeed, minSmashFactor, maxSmashFactor, maxRecords

		try {
			conn = DBUtil.getConnGolfMaster();
			// stmt = conn.createStatement();
			pstmt = conn.prepareStatement(strSQL); // 準備 PreparedStatement

			// 設置參數，取代 SQL 語句中的 ?
            pstmt.setString(1, player);
            pstmt.setString(2, clubType);
			pstmt.setString(3, endDate);
            pstmt.setInt(4, maxRecords); // 設置 LIMIT 的最大筆數

            rs = pstmt.executeQuery(); // 執行查詢 (不帶參數)

			// 迭代結果集，將每筆資料封裝成 ShortGameShotData 物件並加入 List
			while (rs.next()) {
                ShortGameData.ShortGameShotData shot = new ShortGameData.ShortGameShotData();

                shot.setId(rs.getLong("id"));
                shot.setPlayer(rs.getString("Player"));
                shot.setClubType(rs.getString("ClubType"));

                // 注意：使用 getDouble() 來取得數值型數據
                shot.setCarryDistFt(rs.getDouble("CarryDistFt"));
                shot.setTotalDistFt(rs.getDouble("TotalDistFt"));
                shot.setLaunchDirection(rs.getDouble("LaunchDirection"));

                shot.setClubHeadSpeed(rs.getDouble("ClubHeadSpeed"));
                shot.setBackSpin(rs.getDouble("BackSpin"));
				shot.setSideSpin(rs.getDouble("SideSpin"));
                shot.setSmashFactor(rs.getDouble("SmashFactor"));

                shot.setDate(rs.getString("Date"));

                shotList.add(shot); // 將物件加入列表
            }

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		return shotList;
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
