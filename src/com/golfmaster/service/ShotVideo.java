package com.golfmaster.service;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.service.CallMotionApi;
import com.golfmaster.service.TpiDataService;


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

	// 影片資料夾基礎路徑
	private static final String VIDEO_BASE_URL = "../../video/";
	private static final String FRONT_FOLDER = "analyzVideo_front/";
	private static final String SIDE_FOLDER = "analyzVideo_side/";

	// 預設影片名稱（通常是教練或範例影片）
	public final String defaultFrontVideoName = "Player0_shotVideo_front_160230_202405151602.mp4";
	public final String defaultSideVideoName = "Player0_shotVideo_side_160230_202405151602.mp4";

	// 完整的預設影片路徑
	public final String defaultFrontVideoPath = VIDEO_BASE_URL + FRONT_FOLDER + defaultFrontVideoName;
	public final String defaultSideVideoPath = VIDEO_BASE_URL + SIDE_FOLDER + defaultSideVideoName;

	// 預設數據
	public final int[] defaultSideArray = { 157, 281, 345, 407 };
	public final int[] defaultFrontArray = { 160, 305, 353, 413 };
	public final String emptySwingPlane = "{\"data\": {\"success\": true, \"bbox\": [0.0, 0.0, 0.0, 0.0], \"head\": {\"pt\": [0.0, 0.0], \"h_length\": 0.0, \"v_length\": 0.0, \"h_pt\": [0.0, 0.0], \"v_pt\": [0.0, 0.0]}, \"club\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"shoulder\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"left_leg\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"right_leg\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}}}";
	public final String defaultSideSwingPlane = "{\"data\": {\"success\": true, \"bbox\": [0.2338863172029194, 0.31861487494574653, 0.6572389100727282, 0.8508367467809607], \"head\": {\"pt\": [0.6572389100727282, 0.31861487494574653], \"h_length\": 0.1572389100727282, \"v_length\": 0.09434808801721643, \"h_pt\": [0.5, 0.31861487494574653], \"v_pt\": [0.6572389100727282, 0.412962962962963]}, \"club\": {\"pt1\": [0.23355263157894737, 0.3990740740740741], \"pt2\": [0.7483552631578947, 0.7981481481481482]}, \"shoulder\": {\"pt1\": [0.43914473684210525, 0.31851851851851853], \"pt2\": [0.7483552631578947, 0.7981481481481482]}, \"left_leg\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"right_leg\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}}}";
	public final String defaultFrontSwingPlane = "{\"data\": {\"success\": true, \"bbox\": [0.25553424976490163, 0.2374898910522461, 0.6378592597113715, 0.8286705017089844], \"head\": {\"pt\": [0.44166666666666665, 0.3098958333333333], \"h_length\": 0.10740740740740741, \"v_length\": 0.06041666666666667, \"h_pt\": [0.0, 0.0], \"v_pt\": [0.0, 0.0]}, \"club\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"shoulder\": {\"pt1\": [0.0, 0.0], \"pt2\": [0.0, 0.0]}, \"left_leg\": {\"pt1\": [0.5527777777777778, 0.5125], \"pt2\": [0.575, 0.6364583333333333]}, \"right_leg\": {\"pt1\": [0.34444444444444444, 0.5125], \"pt2\": [0.3111111111111111, 0.6354166666666666]}}}";
	public final int[] defaultSideTpiSwingTable = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public final int[] defaultFrontTpiSwingTable = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public final int[] defaultTpiSwingTable = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	public final String defaultAdvicesJson = "{\"A\":[], \"T\":[], \"I\":[], \"F\":[]}";


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


		// 檢查 framesData 是否有值，如果無效則返回預設值
		if (framesData == null || ((int[]) framesData[0]).length == 0) {
			Logs.log(Logs.RUN_LOG, "Null framesData ");
			Random ran = new Random();
			// int aPos = ran.nextInt(6);
			// int tPos = ran.nextInt(6);
			// int iPos = ran.nextInt(6);
			// int fPos = ran.nextInt(6);
			int aPos = 6;
			int tPos = 6;
			int iPos = 6;
			int fPos = 6;
			// return new Object[] { defaultSideArray, defaultFrontArray, defaultFrontVideoName, defaultSideVideoName,
			// 		aPos, tPos, iPos, fPos, defaultSideSwingPlane, defaultFrontSwingPlane,
			// 		defaultTpiSwingTable, defaultAdvicesJson
			// 	};
			return new Object[] { defaultSideArray, defaultFrontArray, defaultFrontVideoPath, defaultSideVideoPath,
					aPos, tPos, iPos, fPos, defaultSideSwingPlane, defaultFrontSwingPlane,
					defaultTpiSwingTable, defaultAdvicesJson
				};
		}

		// 確保 framesData 有效，並且所有必須的參數都不為 null
		int[] sideFrames = framesData[0] != null ? (int[]) framesData[0] : defaultSideArray;
		int[] frontFrames = framesData[1] != null ? (int[]) framesData[1] : defaultFrontArray;
		// String frontVideoName = framesData[2] != null ? (String) framesData[2] : defaultFrontVideoName;
		// String sideVideoName = framesData[3] != null ? (String) framesData[3] : defaultSideVideoName;
		String frontVideoPath = framesData[2] != null ? (String) framesData[2] : defaultFrontVideoPath;
		String sideVideoPath = framesData[3] != null ? (String) framesData[3] : defaultSideVideoPath;
		int aEffect = framesData[4] != null ? (int) framesData[4] : 0;
		int tEffect = framesData[5] != null ? (int) framesData[5] : 0;
		int iEffect = framesData[6] != null ? (int) framesData[6] : 0;
		int fEffect = framesData[7] != null ? (int) framesData[7] : 0;
		// int aEffect = 1;  // 設定 aEffect 為 1
		// int tEffect = 2;  // 設定 tEffect 為 2
		// int iEffect = 2;  // 設定 iEffect 為 2
		// int fEffect = 0;  // 設定 fEffect 為 0
		String sideSwingPlane = framesData.length > 8 && framesData[8] != null ? (String) framesData[8]
				: emptySwingPlane;
		String frontSwingPlane = framesData.length > 9 && framesData[9] != null ? (String) framesData[9]
				: emptySwingPlane;
		int[] sideTpiSwingTable = framesData.length > 10 && framesData[10] != null ? (int[]) framesData[10]
				: defaultTpiSwingTable;
		int[] frontTpiSwingTable = framesData.length > 11 && framesData[11] != null ? (int[]) framesData[11]
				: defaultTpiSwingTable;

		// 將前後台的 TPI 數據合併
		int[] combinedTpiSwingTable = new int[sideTpiSwingTable.length];
		for (int i = 0; i < sideTpiSwingTable.length; i++) {
			combinedTpiSwingTable[i] = sideTpiSwingTable[i] | frontTpiSwingTable[i];
		}
		// for test
		// int[] combinedTpiSwingTable = {1, 1, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0};
		// int[] combinedTpiSwingTable = {1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0};


		// // 使用 TpiDataService 篩選所有階段的數據
		System.out.println("DEBUG-PRE: Ready to initialize TpiDataService.");

		Map<String, List<Map<String, String>>> allFilteredAdvices = new HashMap<>();
		try{
			// 這行會導致類別載入
			TpiDataService tpiDataService = new TpiDataService();
			allFilteredAdvices = tpiDataService.getAllFilteredAdvices(combinedTpiSwingTable);
		} catch (Exception e) {
			// 雖然理論上抓不到 Error，但抓 Exception 是對的。
			System.out.println("processAnalyz caught an exception: " + e.getMessage());
		}

		// // // 將 Map<String, List> 轉換為 JSON 字串
		Gson gson = new Gson();
		String allFilteredAdvicesJson = gson.toJson(allFilteredAdvices);
		if (allFilteredAdvices.isEmpty()) {
            allFilteredAdvicesJson = defaultAdvicesJson;
        } else {
            allFilteredAdvicesJson = gson.toJson(allFilteredAdvices);
        }

		// DEBUG
		System.out.println(
			"DEBUG framesData :"
			+ Arrays.toString(sideFrames)
			+ Arrays.toString(frontFrames)
			+ frontVideoPath + sideVideoPath
			+ aEffect + tEffect + iEffect + fEffect
			+ sideSwingPlane + frontSwingPlane
			+ Arrays.toString(sideTpiSwingTable)
			+ Arrays.toString(frontTpiSwingTable)
			+ Arrays.toString(combinedTpiSwingTable)
			+ allFilteredAdvicesJson
		);
		// 確保返回的 Object[] 中沒有 null 值
		// return new Object[] { sideFrames, frontFrames, frontVideoName, sideVideoName, aEffect, tEffect, iEffect,
		// 		fEffect, sideSwingPlane, frontSwingPlane, combinedTpiSwingTable, allFilteredAdvicesJson};
		return new Object[] { sideFrames, frontFrames, frontVideoPath, sideVideoPath, aEffect, tEffect, iEffect,
				fEffect, sideSwingPlane, frontSwingPlane, combinedTpiSwingTable, allFilteredAdvicesJson};
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
				"SELECT SVS.CamPos AS CamPos, "
					+ "SVS.PlayerPSystem AS PlayerPSystem, "
					+ "SVS.PlayerScore AS PlayerScore, "
					+ "SVS.PoseImpact AS PoseImpact, "
					+ "SVS.SwingPlane AS SwingPlane, "
					+ "SVS.TpiSwingTable As TpiSwingTable, "
					+ "SV.analyze_shotVideo_front, SV.analyze_shotVideo_side "
					// + "FROM golf_master.shot_video AS SV, golf_master.shot_video_swing AS SVS "
					// + "WHERE SV.shot_data_id = '%s' AND SVS.ShotVideoId = SV.id",
					+ "FROM golf_master.shot_video AS SV "
					+ "LEFT JOIN golf_master.shot_video_swing AS SVS ON SVS.ShotVideoId = SV.id "
					+ "WHERE SV.shot_data_id = '%s'",
				shot_data_id
			);
		}
		Logs.log(Logs.RUN_LOG, "queryShotVideoAnalyz strSQL: " + strSQL);
		System.out.println("queryShotVideoAnalyz strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);

			boolean hasResults = false;
			while (rs.next()) {
				hasResults = true; // 確認至少有一行數據
				JSONObject jsonProject = new JSONObject();
				// jsonProject.put("CamPos", rs.getString("CamPos"));
				// jsonProject.put("PlayerPSystem", rs.getString("PlayerPSystem"));
				// jsonProject.put("analyze_shotVideo_front", rs.getString("analyze_shotVideo_front"));
				// jsonProject.put("analyze_shotVideo_side", rs.getString("analyze_shotVideo_side"));
				// jsonProject.put("PoseImpact", rs.getString("PoseImpact"));
				// jsonProject.put("SwingPlane", rs.getString("SwingPlane"));
				// jsonProject.put("TpiSwingTable", rs.getString("TpiSwingTable") != null ? rs.getString("TpiSwingTable") : "");

				// 檢查並處理可能為 null 的欄位
				jsonProject.put("CamPos", rs.getString("CamPos") != null ? rs.getString("CamPos") : "");
				jsonProject.put("PlayerPSystem", rs.getString("PlayerPSystem") != null ? rs.getString("PlayerPSystem") : "");
				jsonProject.put("analyze_shotVideo_front", rs.getString("analyze_shotVideo_front") != null ? rs.getString("analyze_shotVideo_front") : "");
				jsonProject.put("analyze_shotVideo_side", rs.getString("analyze_shotVideo_side") != null ? rs.getString("analyze_shotVideo_side") : "");
				jsonProject.put("PlayerScore", rs.getString("PlayerScore") != null ? rs.getString("PlayerScore") : "");
				jsonProject.put("PoseImpact", rs.getString("PoseImpact") != null ? rs.getString("PoseImpact") : "");
				jsonProject.put("SwingPlane", rs.getString("SwingPlane") != null ? rs.getString("SwingPlane") : "");
				jsonProject.put("TpiSwingTable", rs.getString("TpiSwingTable") != null ? rs.getString("TpiSwingTable") : "");

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

	// private Object[] extractFrames(String jsonResponse) {
	// 	ArrayList<Integer> sideFrames = new ArrayList<>();
	// 	ArrayList<Integer> frontFrames = new ArrayList<>();
	// 	// String sideVideoName = "";
	// 	// String frontVideoName = "";
	// 	String sideVideoDbData = "";
    //     String frontVideoDbData = "";
	// 	String sideSwingPlane = ""; // 新增側面的 SwingPlane 變數
	// 	String frontSwingPlane = ""; // 新增正面的 SwingPlane 變數
	// 	ArrayList<Integer> sideTpiTable = new ArrayList<>();
	// 	ArrayList<Integer> frontTpiTable = new ArrayList<>();

	// 	// parameters
	// 	int defaultTpiTableSize = 16;
	// 	double simScoreThreshold = 0.70; // 相似度分數, 最大值為1
	// 	int postImpactMaxIndex = 6; // 關節部位個數[shoulder, elbow, wrist, hip, knee, ankle]

	// 	// 修改變量以存儲最大值的索引
	// 	int maxAIndex = -1, maxTIndex = -1, maxIIndex = -1, maxFIndex = -1;
	// 	try {
	// 		JSONObject responseObj = new JSONObject(jsonResponse);
	// 		JSONArray results = responseObj.getJSONArray("result");

	// 		// 檢查 results 陣列是否存在且非空
	// 		if (results == null || results.length() == 0) {
	// 			return null;
	// 		}

	// 		for (int i = 0; i < results.length(); i++) {
	// 			JSONObject result = results.getJSONObject(i);
	// 			String camPos = result.optString("CamPos", "");

	// 			System.out.println("result = " + result);

	// 			// 處理 PlayerPSystem
	// 			String playerPSystemStr = result.optString("PlayerPSystem", "{}");
	// 			if (!playerPSystemStr.isEmpty()) { // 添加檢查，避免空字串
	// 				JSONObject playerPSystemObj = new JSONObject(playerPSystemStr);
	// 				JSONArray data = playerPSystemObj.optJSONArray("data");
	// 				if (data != null) {
	// 					for (int j = 0; j < data.length(); j++) {
	// 						if ("side".equals(camPos)) {
	// 							sideFrames.add(data.optInt(j));
	// 						} else if ("front".equals(camPos)) {
	// 							frontFrames.add(data.optInt(j));
	// 						}
	// 					}
	// 				}
	// 			}

	// 			// 處理 TpiSwingTable
	// 			String tpiTableStr = result.optString("TpiSwingTable", "{}");
	// 			if (!tpiTableStr.isEmpty()) { // 添加檢查，避免空字串
	// 				JSONObject tpiTableObj = new JSONObject(tpiTableStr);
	// 				JSONArray tpiTableData = tpiTableObj.optJSONArray("data");
	// 				if (tpiTableData != null) {
	// 					for (int j = 0; j < tpiTableData.length(); j++) {
	// 						// System.out.println(
	// 						// 	"side = "
	// 						// 	+ camPos
	// 						// 	+ " tpiTableData "
	// 						// 	+ j
	// 						// 	+ " = "
	// 						// 	+ tpiTableData.optInt(j)
	// 						// );
	// 						if ("side".equals(camPos)) {
	// 							sideTpiTable.add(tpiTableData.optInt(j));
	// 						} else if ("front".equals(camPos)) {
	// 							frontTpiTable.add(tpiTableData.optInt(j));
	// 						}
	// 					}
	// 				}
	// 			}

	// 			if ("side".equals(camPos)) {
	// 				// sideVideoName = extractFileName(result.getString("analyze_shotVideo_side"));
	// 				sideVideoDbData = result.optString("analyze_shotVideo_side", "");
	// 				sideSwingPlane = result.optString("SwingPlane", null); // 使用 optString 獲取 SwingPlane，允許為 null
	// 			} else if ("front".equals(camPos)) {
	// 				// frontVideoName = extractFileName(result.getString("analyze_shotVideo_front"));
	// 				frontVideoDbData = result.optString("analyze_shotVideo_front", "");
	// 				frontSwingPlane = result.optString("SwingPlane", null); // 使用 optString 獲取 SwingPlane，允許為 null
	// 			}

	// 			// 處理PoseImpact數據
	// 			int[] maxIndexes = processCmpPoseImpact(result, simScoreThreshold, postImpactMaxIndex);
	// 			maxAIndex = maxIndexes[0];
	// 			maxTIndex = maxIndexes[1];
	// 			maxIIndex = maxIndexes[2];
	// 			maxFIndex = maxIndexes[3];
	// 		}
	// 	} catch (Exception e) {
	// 		e.printStackTrace();
	// 		return null; // 在發生錯誤時返回null
	// 	}


	// 	// --- 優化預設影片選擇邏輯 ---
	// 	// 1. 轉換資料庫數據為最終的本地相對路徑
	// 	String sideVideoPath = getFullPath(sideVideoDbData, defaultSideVideoPath, SIDE_FOLDER);
	// 	String frontVideoPath = getFullPath(frontVideoDbData, defaultFrontVideoPath, FRONT_FOLDER);
    //     // 2. 檢查路徑是否為預設路徑 (代表資料庫沒有影片)
	// 	boolean isSideVideoPresent = !sideVideoPath.equals(defaultSideVideoPath);
    //     boolean isFrontVideoPresent = !frontVideoPath.equals(defaultFrontVideoPath);

	// 	if (!isSideVideoPresent && !isFrontVideoPresent) {
    //         // 情況 1: 前後都沒有影片資料
    //         Logs.log(Logs.RUN_LOG, "Side and Front data missing. Using default values for both.");
    //         sideFrames = Arrays.stream(defaultSideArray).boxed().collect(Collectors.toCollection(ArrayList::new));
    //         frontFrames = Arrays.stream(defaultFrontArray).boxed().collect(Collectors.toCollection(ArrayList::new));
    //         // sideVideoName = defaultSideVideoName;
    //         // frontVideoName = defaultFrontVideoName;
    //         sideSwingPlane = emptySwingPlane;
    //         frontSwingPlane = emptySwingPlane;
    //     } else if (isSideVideoPresent && !isFrontVideoPresent) {
    //         // 情況 2: 只有側面有影片資料，正面採用側面預設值
    //         Logs.log(Logs.RUN_LOG, "Only Side video present. Using Side video for Front default.");
    //         frontFrames = Arrays.stream(defaultSideArray).boxed().collect(Collectors.toCollection(ArrayList::new)); // 預設的 Side 影格資料
    //         // frontVideoName = defaultSideVideoName; // 使用側面預設路徑
	// 		frontVideoPath = defaultSideVideoPath; // 使用側面預設路徑
    //         frontSwingPlane = emptySwingPlane; // 正面分析無資料，給空
    //     } else if (!isSideVideoPresent && isFrontVideoPresent) {
    //         // 情況 3: 只有正面有影片資料，側面採用正面預設值
    //         Logs.log(Logs.RUN_LOG, "Only Front video present. Using Front video for Side default.");
    //         sideFrames = Arrays.stream(defaultFrontArray).boxed().collect(Collectors.toCollection(ArrayList::new)); // 預設的 Front 影格資料
    //         // sideVideoName = defaultFrontVideoName; // 使用正面預設路徑
	// 		sideVideoPath = defaultFrontVideoPath; // 使用正面預設路徑
    //         sideSwingPlane = emptySwingPlane; // 側面分析無資料，給空
    //     }
    //     // 情況 4: 前後都有影片資料 (isSideVideoPresent && isFrontVideoPresent) - 無需修改，保持提取結果

	// 	// 補充 framesData (影格)
    //     if (sideFrames.isEmpty()) {
    //         Logs.log(Logs.RUN_LOG, "Side frames data missing. Using default side frames.");
    //         sideFrames = Arrays.stream(defaultSideArray).boxed().collect(Collectors.toCollection(ArrayList::new));
    //     }
    //     if (frontFrames.isEmpty()) {
    //         Logs.log(Logs.RUN_LOG, "Front frames data missing. Using default front frames.");
    //         frontFrames = Arrays.stream(defaultFrontArray).boxed().collect(Collectors.toCollection(ArrayList::new));
    //     }

	//     // 補充缺少的資料
	//     // if (sideFrames.isEmpty()) {
	//     //     Logs.log(Logs.RUN_LOG, "Side data missing. Using default side values.");
	//     //     sideFrames = new ArrayList<>(Arrays.asList(157, 281, 345, 407));
	//     //     sideVideoName = defaultSideVideoName;
	//     //     sideSwingPlane = emptySwingPlane;
	//     // }
	//     // if (frontFrames.isEmpty()) {
	//     //     Logs.log(Logs.RUN_LOG, "Front data missing. Using default front values.");
	//     //     frontFrames = new ArrayList<>(Arrays.asList(160, 305, 353, 413));
	//     //     frontVideoName = defaultFrontVideoName;
	//     //     frontSwingPlane = emptySwingPlane;
	//     // }

	// 	// 補充 TpiTable
	// 	if (sideTpiTable.isEmpty()) {
	// 		for (int j = 0; j < defaultTpiTableSize; j++) {
	// 			sideTpiTable.add(0);
	// 		}
	// 	}

	// 	if (frontTpiTable.isEmpty()) {
	// 		for (int j = 0; j < defaultTpiTableSize; j++) {
	// 			frontTpiTable.add(0);
	// 		}
	// 	}

	// 	// 補充 SwingPlane
    //     if (sideSwingPlane == null) {
    //         sideSwingPlane = emptySwingPlane;
    //     }
    //     if (frontSwingPlane == null) {
    //         frontSwingPlane = emptySwingPlane;
    //     }

    //     // 補充 PoseImpact 索引
	// 	if (maxAIndex < 0)
	// 		maxAIndex = postImpactMaxIndex;
	// 	if (maxTIndex < 0)
	// 		maxTIndex = postImpactMaxIndex;
	// 	if (maxIIndex < 0)
	// 		maxIIndex = postImpactMaxIndex;
	// 	if (maxFIndex < 0)
	// 		maxFIndex = postImpactMaxIndex;

	// 	int[] sideArray = sideFrames.stream().mapToInt(i -> i).toArray();
	// 	int[] frontArray = frontFrames.stream().mapToInt(i -> i).toArray();
	// 	int[] sideTpi = sideTpiTable.stream().mapToInt(i -> i).toArray();
	// 	int[] frontTpi = frontTpiTable.stream().mapToInt(i -> i).toArray();

	// 	// System.out.println(
	// 	// 	"sideTpi = "
	// 	// 	+ Arrays.toString(sideTpi)
	// 	// 	+ " frontTpi = "
	// 	// 	+ Arrays.toString(frontTpi)
	// 	// );

	// 	// return new Object[] {
	// 	// 	sideArray, frontArray, frontVideoName, sideVideoName,
	// 	// 	maxAIndex, maxTIndex, maxIIndex, maxFIndex,
	// 	// 	sideSwingPlane, frontSwingPlane, sideTpi, frontTpi
	// 	// };
	// 	return new Object[] {
	// 		sideArray, frontArray, frontVideoPath, sideVideoPath,
	// 		maxAIndex, maxTIndex, maxIIndex, maxFIndex,
	// 		sideSwingPlane, frontSwingPlane, sideTpi, frontTpi
	// 	};
	// }

	private Object[] extractFrames(String jsonResponse) {
		ArrayList<Integer> sideFrames = new ArrayList<>();
		ArrayList<Integer> frontFrames = new ArrayList<>();
		String sideVideoDbData = "";
		String frontVideoDbData = "";
		String sideSwingPlane = null;
		String frontSwingPlane = null;
		ArrayList<Integer> sideTpiTable = new ArrayList<>();
		ArrayList<Integer> frontTpiTable = new ArrayList<>();

		// parameters
		int defaultTpiTableSize = 16;
		double simScoreThreshold = 0.70;
		int postImpactMaxIndex = 6;

		int maxAIndex = -1, maxTIndex = -1, maxIIndex = -1, maxFIndex = -1;
		boolean videoPathExtracted = false; // 旗標：確保影片路徑只提取一次

		try {
			JSONObject responseObj = new JSONObject(jsonResponse);

			// **【修正點 A】**：如果 DB 查詢結果 JSON 標記為不成功，直接返回 null
			if (!responseObj.optBoolean("success", true)) {
				Logs.log(Logs.RUN_LOG, "extractFrames received unsuccessful JSON (success=false): " + jsonResponse);
				return null;
			}

			JSONArray results = responseObj.getJSONArray("result");

			// **【修正點 B】**：如果結果集為空（shot_data_id 找不到對應的 SV 紀錄）
			if (results == null || results.length() == 0) {
				Logs.log(Logs.RUN_LOG, "Query returned no results at all (SV data missing).");
				return null;
			}

			// 處理 LEFT JOIN 可能返回的多筆 (CamPos) 或單筆 (CamPos=NULL) 數據
			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				String camPos = result.optString("CamPos", "");

				// 處理影片路徑 (SV 表欄位) - 這些欄位在 LEFT JOIN 時會存在且非 NULL
				// 由於這些欄位在所有結果行中都是重複的，只需提取一次
				if (!videoPathExtracted) {
					sideVideoDbData = result.optString("analyze_shotVideo_side", "");
					frontVideoDbData = result.optString("analyze_shotVideo_front", "");
					videoPathExtracted = true;
				}

				// 以下區塊只在 SVS 有數據時執行 (即 CamPos 非空)
				if (StringUtils.isNotEmpty(camPos)) {

					// 處理 PlayerPSystem (Frames)
					String playerPSystemStr = result.optString("PlayerPSystem", "{}");
					if (!playerPSystemStr.isEmpty() && !"{}".equals(playerPSystemStr)) {
						JSONObject playerPSystemObj = new JSONObject(playerPSystemStr);
						JSONArray data = playerPSystemObj.optJSONArray("data");
						if (data != null && data.length() > 0) {
							if ("side".equals(camPos)) {
								sideFrames = new ArrayList<>();
								for (int j = 0; j < data.length(); j++) sideFrames.add(data.optInt(j));
							} else if ("front".equals(camPos)) {
								frontFrames = new ArrayList<>();
								for (int j = 0; j < data.length(); j++) frontFrames.add(data.optInt(j));
							}
						}
					}

					// 處理 TpiSwingTable
					String tpiTableStr = result.optString("TpiSwingTable", "{}");
					if (!tpiTableStr.isEmpty() && !"{}".equals(tpiTableStr)) {
						JSONObject tpiTableObj = new JSONObject(tpiTableStr);
						JSONArray tpiTableData = tpiTableObj.optJSONArray("data");
						if (tpiTableData != null && tpiTableData.length() > 0) {
							if ("side".equals(camPos)) {
								sideTpiTable = new ArrayList<>();
								for (int j = 0; j < tpiTableData.length(); j++) sideTpiTable.add(tpiTableData.optInt(j));
							} else if ("front".equals(camPos)) {
								frontTpiTable = new ArrayList<>();
								for (int j = 0; j < tpiTableData.length(); j++) frontTpiTable.add(tpiTableData.optInt(j));
							}
						}
					}

					// 處理 SwingPlane
					if ("side".equals(camPos)) {
						sideSwingPlane = result.optString("SwingPlane", null);
					} else if ("front".equals(camPos)) {
						frontSwingPlane = result.optString("SwingPlane", null);
					}

					// 處理 PoseImpact 數據
					int[] maxIndexes = processCmpPoseImpact(result, simScoreThreshold, postImpactMaxIndex);
					if (maxAIndex < 0) maxAIndex = maxIndexes[0];
					if (maxTIndex < 0) maxTIndex = maxIndexes[1];
					if (maxIIndex < 0) maxIIndex = maxIndexes[2];
					if (maxFIndex < 0) maxFIndex = maxIndexes[3];
				}
			}

			// 理論上只要 DB 中有 shot_video 紀錄，videoPathExtracted 就會是 true
			if (!videoPathExtracted) {
				Logs.log(Logs.RUN_LOG, "Fatal error: SV path extraction failed unexpectedly.");
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			Logs.log(Logs.EXCEPTION_LOG, "Error in extractFrames processing JSON: " + e.getMessage());
			return null;
		}


		// --- 步驟 1-2: 處理影片路徑和判斷 DB 數據是否存在 ---
		String sideVideoPath = getFullPath(sideVideoDbData, defaultSideVideoPath, SIDE_FOLDER);
		String frontVideoPath = getFullPath(frontVideoDbData, defaultFrontVideoPath, FRONT_FOLDER);

		boolean isSideDbPresent = !sideVideoDbData.isEmpty();
		boolean isFrontDbPresent = !frontVideoDbData.isEmpty();


		// --- 步驟 3: 處理使用者影片分析數據缺失回退 ---

		// 側面 (Side) 邏輯：
		if (isSideDbPresent) {
			// 保持不變：有使用者影片，分析失敗則使用 default Frames + empty SwingPlane
			if (sideFrames.isEmpty()) {
				Logs.log(Logs.RUN_LOG, "User Side DB path present, but side frames missing (Analysis Failed). Using default side frames.");
				sideFrames = Arrays.stream(defaultSideArray).boxed().collect(Collectors.toCollection(ArrayList::new));
				sideSwingPlane = emptySwingPlane;
			} else if (sideSwingPlane == null || sideSwingPlane.isEmpty() || sideSwingPlane.equals("null")) {
				sideSwingPlane = emptySwingPlane;
			}
			if (sideTpiTable.isEmpty()) {
				sideTpiTable = Arrays.stream(defaultTpiSwingTable).boxed().collect(Collectors.toCollection(ArrayList::new));
			}
		} else {
			// 情況 3: 無使用者影片
			Logs.log(Logs.RUN_LOG, "No User Side video path. Using default Side coach data for Frames/TPI, but using empty SwingPlane.");
			sideFrames = Arrays.stream(defaultSideArray).boxed().collect(Collectors.toCollection(ArrayList::new));
			sideTpiTable = Arrays.stream(defaultFrontTpiSwingTable).boxed().collect(Collectors.toCollection(ArrayList::new));

			// **【修正點 1】**：無使用者影片時，Frame/TPI 使用教練預設，但 SwingPlane 使用 empty
			sideSwingPlane = emptySwingPlane;
		}

		// 正面 (Front) 邏輯：
		if (isFrontDbPresent) {
			// 保持不變：有使用者影片，分析失敗則使用 default Frames + empty SwingPlane
			if (frontFrames.isEmpty()) {
				Logs.log(Logs.RUN_LOG, "User Front DB path present, but front frames missing (Analysis Failed). Using default front frames.");
				frontFrames = Arrays.stream(defaultFrontArray).boxed().collect(Collectors.toCollection(ArrayList::new));
				frontSwingPlane = emptySwingPlane;
			} else if (frontSwingPlane == null || frontSwingPlane.isEmpty() || frontSwingPlane.equals("null")) {
				frontSwingPlane = emptySwingPlane;
			}
			if (frontTpiTable.isEmpty()) {
				frontTpiTable = Arrays.stream(defaultTpiSwingTable).boxed().collect(Collectors.toCollection(ArrayList::new));
			}
		} else {
			// 情況 3: 無使用者影片
			Logs.log(Logs.RUN_LOG, "No User Front video path. Using default Front coach data for Frames/TPI, but using empty SwingPlane.");
			frontFrames = Arrays.stream(defaultFrontArray).boxed().collect(Collectors.toCollection(ArrayList::new));
			frontTpiTable = Arrays.stream(defaultFrontTpiSwingTable).boxed().collect(Collectors.toCollection(ArrayList::new));

			// **【修正點 2】**：無使用者影片時，Frame/TPI 使用教練預設，但 SwingPlane 使用 empty
			frontSwingPlane = emptySwingPlane;
		}


		// --- 步驟 4: 處理單邊缺失的影片路徑替代邏輯 (影片路徑的備援) ---

		// 情境 B: 只有側面使用者影片 (正面使用側面教練影片)
		if (isSideDbPresent && !isFrontDbPresent) {
			Logs.log(Logs.RUN_LOG, "Only Side video present. Front uses Side coach video path.");
			frontVideoPath = defaultSideVideoPath; // **** 替代路徑 (用側面的教練影片) ****
		}

		// 情境 A: 只有正面使用者影片 (側面使用正面教練影片)
		if (isFrontDbPresent && !isSideDbPresent) {
			Logs.log(Logs.RUN_LOG, "Only Front video present. Side uses Front coach video path.");
			sideVideoPath = defaultFrontVideoPath; // **** 替代路徑 (用正面的教練影片) ****
		}


		// --- 步驟 5: 補充 PoseImpact 索引 ---
		// 如果 SVS 數據缺失 (CamPos 為空)，maxA/T/I/FIndex 將為 -1。
		if (maxAIndex < 0) maxAIndex = postImpactMaxIndex;
		if (maxTIndex < 0) maxTIndex = postImpactMaxIndex;
		if (maxIIndex < 0) maxIIndex = postImpactMaxIndex;
		if (maxFIndex < 0) maxFIndex = postImpactMaxIndex;


		// --- 步驟 6: 最終轉換並返回 ---
		int[] sideArray = sideFrames.stream().mapToInt(i -> i).toArray();
		int[] frontArray = frontFrames.stream().mapToInt(i -> i).toArray();
		int[] sideTpi = sideTpiTable.stream().mapToInt(i -> i).toArray();
		int[] frontTpi = frontTpiTable.stream().mapToInt(i -> i).toArray();

		return new Object[] {
			sideArray, frontArray, frontVideoPath, sideVideoPath,
			maxAIndex, maxTIndex, maxIIndex, maxFIndex,
			sideSwingPlane, frontSwingPlane, sideTpi, frontTpi
		};
	}

	// --- ---

	/**
	 * 根據 PlayerScore 判斷是否需要從 PoseImpact 中尋找最大值索引。
	 *
	 * @param result 單個 shotVideo 分析的 JSONObject。
	 * @param scoreThreshold 分數門檻值。
	 * @param maxIndex 關節部位分數的個數[shoulder, elbow, wrist, hip, knee, ankle]
	 * @return 包含四個最大值索引的 int 陣列。
	 */
	private int[] processCmpPoseImpact(
		JSONObject result,
		double scoreThreshold,
		int maxIndex
	) {
		int maxAIndex = maxIndex;
		int maxTIndex = maxIndex;
		int maxIIndex = maxIndex;
		int maxFIndex = maxIndex;

		String playerScoreStr = result.optString("PlayerScore", "{}");
		String poseImpactStr = result.optString("PoseImpact", "{}");

		if (!playerScoreStr.isEmpty() && !poseImpactStr.isEmpty()) {
			try {
				JSONObject playerScore = new JSONObject(playerScoreStr);
				JSONObject poseImpact = new JSONObject(poseImpactStr);

				JSONObject scoreData = playerScore.optJSONObject("data");
				JSONObject impactData = poseImpact.optJSONObject("data");

				// System.out.println("scoreData = " + scoreData);
				// System.out.println("impactData = " + impactData);

				if (scoreData != null && impactData != null) {
					if (scoreData.optDouble("A", 1.0) < scoreThreshold) {
						JSONArray aArray = impactData.optJSONArray("A");
						maxAIndex = getMaxIndex(aArray);
					}

					if (scoreData.optDouble("T", 1.0) < scoreThreshold) {
						JSONArray tArray = impactData.optJSONArray("T");
						maxTIndex = getMaxIndex(tArray);
					}

					if (scoreData.optDouble("I", 1.0) < scoreThreshold) {
						JSONArray iArray = impactData.optJSONArray("I");
						maxIIndex = getMaxIndex(iArray);
					}

					if (scoreData.optDouble("F", 1.0) < scoreThreshold) {
						JSONArray fArray = impactData.optJSONArray("F");
						maxFIndex = getMaxIndex(fArray);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logs.log(Logs.EXCEPTION_LOG, "Error processing PlayerScore and PoseImpact: " + e.getMessage());
				System.out.println("Error processing PlayerScore and PoseImpact: " + e.getMessage());
			}
		}

		Logs.log(Logs.RUN_LOG, "A:" + maxAIndex + " T:" + maxTIndex + " I:" + maxIIndex + " F:" + maxFIndex);
		System.out.println("A:" + maxAIndex + " T:" + maxTIndex + " I:" + maxIIndex + " F:" + maxFIndex);

		return new int[]{maxAIndex, maxTIndex, maxIIndex, maxFIndex};
	}

	// --- ---

	private String extractFileName(String url) {
		if (url == null || url.isEmpty()) return "";
		return url.substring(url.lastIndexOf('/') + 1);
	}

	/**
     * 根據資料庫儲存的檔名/URL，返回 JSP 可用的完整的本地相對路徑。
     * 即使資料庫存的是完整的下載 URL，也會將其轉換為本地相對路徑。
     */
    private String getFullPath(String dbPathOrName, String defaultPath, String folderName) {
        if (dbPathOrName == null || dbPathOrName.isEmpty()) {
            return defaultPath; // 資料庫無資料時，使用預設路徑
        }

        // 從資料庫數據中提取檔名 (適用於資料庫存儲完整的下載 URL 或僅檔名)
        String fileName = extractFileName(dbPathOrName);

        if (fileName.isEmpty()) {
            // 如果提取不到檔名，返回預設路徑
            return defaultPath;
        } else {
            // 使用提取到的檔名，拼接成 JSP 可用的本地相對路徑
            return VIDEO_BASE_URL + folderName + fileName;
        }
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
//{"data": {"success": true, "bbox": [0.2518528386166221, 0.29106055365668404, 0.6435657802381014, 0.7766441062644676], "head": {"pt": [0.5592105263157895, 0.3333333333333333], "h_length": 0.13486842105263158, "v_length": 0.07592592592592592}, "club": {"pt1": [0.25164473684210525, 0.27685185185185185], "pt2": [0.7220394736842105, 0.75]}, "shoulder": {"pt1": [0.4555921052631579, 0.29074074074074074], "pt2": [0.7220394736842105, 0.75]}, "left_leg": {"pt1": [0.0, 0.0], "pt2": [0.0, 0.0]}, "right_leg": {"pt1": [0.0, 0.0], "pt2": [0.0, 0.0]}}}
//{"result":[{"PlayerPSystem":"{\"data\": [259, 361, 402, 498]}","analyze_shotVideo_side":"http://125.227.141.7:49147/GolfVisionAnalytics/service/download_video/143/Guest.1_shotVideo_side_161424_202404101614.mp4","analyze_shotVideo_front":"http://125.227.141.7:49147/GolfVisionAnalytics/service/download_video/143/Guest.1_shotVideo_front_161424_202404101614.mp4","shot_data_id":"117340"}],"success":true}