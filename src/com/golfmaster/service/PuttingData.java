/*
 * 推桿頁的影像分析與判定結果查詢。
 *
 * 查這一推的 shot_video_swing 推桿列（界標、節奏兩欄原始字串），
 * 以及同一列在 putting_issue 的判定結果，組回判定物件。
 *
 * ⛔ 這一層只查詢與搬移，⛔ 不做任何判斷：
 *    可信度、幀率、要不要顯示、判定算不算完成，全部在頁面的 JS。
 *
 * ═══ ⛔ 三個地雷 ═══
 * 1. ⛔ 不可以用 ClubType 篩推桿。那是輸入（球桿種類），AnalysisMode 才是結果。
 * 2. ⛔ putting_issue 一定要 LEFT JOIN。還沒判定時那一列不存在，
 *    INNER JOIN 會讓整支影片的界標也跟著查不到。
 * 3. ⛔ shot_video_swing.id 與 putting_issue.SwingId 是 BIGINT，⛔ 不可以用 int 讀。
 */
package com.golfmaster.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class PuttingData {

	public static final String SCHEMA_VERSION = "putting_issues/1.1";

	/** issues 陣列的固定順序：站姿、三角形、球位、位移、傾斜。 */
	private static final String[] ISSUE_COLUMNS = { "Stance", "Triangle", "BallPosition", "BodySway", "SwingAngle" };

	/** putting_issue 一列的原始欄位。欄位是 SQL NULL 時就是 null。 */
	public static class IssueRow {
		public String issueStatus;
		public String issueReason;
		public String detectedIssues;
		public String thresholdProfile;
		public String overall;
		/** 依 ISSUE_COLUMNS 的順序 */
		public String[] issueColumns = new String[ISSUE_COLUMNS.length];
	}

	/**
	 * @param shot_data_id 這一推的 shot_data.id（⛔ 不是網址的 ?expert=）
	 * @return {
	 *     PuttingPhases:     字串或 null（SQL NULL 或查不到推桿列）,
	 *     PuttingTempo:      字串或 null,
	 *     view:              挑中那一列的 CamPos（front 優先），查不到是 null,
	 *     SidePuttingPhases: 側面那一列的同一欄，沒有側面列時是 null,
	 *     SidePuttingTempo:  同上,
	 *     issues:            組好的判定物件；putting_issue 沒有這一列時是 null
	 *   }
	 *   ⚠️ 沒有推桿列時每個鍵都是 null，⛔ 不拋例外 —— 拋了整頁連影片都不見。
	 *
	 * ⚠️ 側面兩欄只給影片跳幀用：側面影片有自己的界標與秒數，
	 *    ⛔ 正面的幀號拿到側面完全對不上（同一推實測差 7〜240 幀）。
	 * ⛔ 判定仍然只看挑中的那一列（front 優先），⛔ 側面那兩欄⛔ 不參與任何判定。
	 */
	public JSONObject processPutting(Long shot_data_id) {
		JSONObject result = new JSONObject();
		result.put("PuttingPhases", JSONObject.NULL);
		result.put("PuttingTempo", JSONObject.NULL);
		result.put("view", JSONObject.NULL);
		result.put("SidePuttingPhases", JSONObject.NULL);
		result.put("SidePuttingTempo", JSONObject.NULL);
		result.put("issues", JSONObject.NULL);
		if (shot_data_id == null) {
			return result;
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// ⚠️ 同一次推擊正面與側面各一列：判定挑 front，沒有才用 side（讀取端規則）。
		//    同一個視角重跑過就取最新那一列。
		// ⚠️ 兩列都要讀回來（側面那一列的界標給側面影片用），所以⛔ 沒有 LIMIT：
		//    排序讓 front 在前、同視角新的在前 → 第一列就是判定要用的那一列。
		String strSQL = "SELECT SVS.id AS SwingId, SVS.CamPos, SVS.PuttingPhases, SVS.PuttingTempo, "
				+ "PI.Id AS IssueId, PI.IssueStatus, PI.IssueReason, PI.DetectedIssues, "
				+ "PI.ThresholdProfile, PI.Overall, "
				+ "PI.Stance, PI.Triangle, PI.BallPosition, PI.BodySway, PI.SwingAngle "
				+ "FROM golf_master.shot_video AS SV "
				+ "JOIN golf_master.shot_video_swing AS SVS "
				+ "ON SVS.ShotVideoId = SV.id AND SVS.AnalysisMode = 'putting' "
				+ "LEFT JOIN golf_master.putting_issue AS PI ON PI.SwingId = SVS.id "
				+ "WHERE SV.shot_data_id = ? "
				+ "ORDER BY CASE WHEN SVS.CamPos = 'front' THEN 0 ELSE 1 END, SVS.id DESC";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setLong(1, shot_data_id);
			rs = pstmt.executeQuery();
			boolean primaryTaken = false;
			boolean sideTaken = false;
			while (rs.next()) {
				String camPos = rs.getString("CamPos");
				String phases = rs.getString("PuttingPhases");
				String tempo = rs.getString("PuttingTempo");

				// 第一列就是判定要用的那一列（排序保證 front 優先、同視角取新的）
				if (!primaryTaken) {
					primaryTaken = true;
					result.put("view", camPos == null ? JSONObject.NULL : camPos);
					result.put("PuttingPhases", phases == null ? JSONObject.NULL : phases);
					result.put("PuttingTempo", tempo == null ? JSONObject.NULL : tempo);

					rs.getLong("IssueId");
					if (!rs.wasNull()) {
						IssueRow row = new IssueRow();
						row.issueStatus = rs.getString("IssueStatus");
						row.issueReason = rs.getString("IssueReason");
						row.detectedIssues = rs.getString("DetectedIssues");
						row.thresholdProfile = rs.getString("ThresholdProfile");
						row.overall = rs.getString("Overall");
						for (int i = 0; i < ISSUE_COLUMNS.length; i++) {
							row.issueColumns[i] = rs.getString(ISSUE_COLUMNS[i]);
						}
						result.put("issues", assemble(camPos, phases, row));
					}
				}

				// ⚠️ 只有正面列時這兩個鍵仍然是 null → 側面影片就不跳（安全預設）。
				//    只有側面列時第一列就是它，所以兩邊指到同一列，⭐ 那也是對的。
				if (!sideTaken && "side".equalsIgnoreCase(camPos)) {
					sideTaken = true;
					result.put("SidePuttingPhases", phases == null ? JSONObject.NULL : phases);
					result.put("SidePuttingTempo", tempo == null ? JSONObject.NULL : tempo);
				}

				if (primaryTaken && sideTaken) {
					break;
				}
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "PuttingData.processPutting failed: " + e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		return result;
	}

	/**
	 * 把 putting_issue 一列組回判定物件（putting_issues_schema.md §1 的形狀）。
	 *
	 * ⛔ 沒有判斷邏輯，每一個鍵都是直接搬：
	 *   view         ← shot_video_swing.CamPos（⛔ 不在 putting_issue 表）
	 *   phase_status ← 同一列 PuttingPhases 的 status 鍵（⛔ 不在 putting_issue 表）
	 *   detected     ← DetectedIssues；⚠️ NULL（沒判過）與 []（判過都正常）要分開，⛔ 不可以混成同一個
	 *   issues       ← 五欄依固定順序；某一欄是 NULL 或不是合法 JSON 就跳過那一欄
	 *                  （not_computed 的列本來就是空陣列）
	 *
	 * 不連資料庫，驗收直接拿範例檔的內容餵進來比對。
	 */
	public static JSONObject assemble(String camPos, String puttingPhases, IssueRow row) {
		JSONObject obj = new JSONObject();
		obj.put("schema_version", SCHEMA_VERSION);
		obj.put("status", orNull(row.issueStatus));
		obj.put("reason", orNull(row.issueReason));
		obj.put("view", orNull(camPos));
		obj.put("phase_status", phaseStatusOf(puttingPhases));
		obj.put("threshold_profile", orNull(row.thresholdProfile));
		obj.put("overall", orNull(row.overall));
		obj.put("detected", parseArray(row.detectedIssues, "DetectedIssues"));

		JSONArray issues = new JSONArray();
		for (int i = 0; i < ISSUE_COLUMNS.length; i++) {
			Object parsed = parseObject(row.issueColumns[i], ISSUE_COLUMNS[i]);
			if (parsed != JSONObject.NULL) {
				issues.put(parsed);
			}
		}
		obj.put("issues", issues);
		return obj;
	}

	private static Object orNull(String value) {
		return value == null ? JSONObject.NULL : value;
	}

	private static Object phaseStatusOf(String puttingPhases) {
		Object parsed = parseObject(puttingPhases, "PuttingPhases");
		if (parsed == JSONObject.NULL) {
			return JSONObject.NULL;
		}
		JSONObject phases = (JSONObject) parsed;
		return phases.has("status") ? phases.get("status") : JSONObject.NULL;
	}

	private static Object parseObject(String text, String column) {
		if (text == null) {
			return JSONObject.NULL;
		}
		try {
			return new JSONObject(text);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "PuttingData: " + column + " is not a JSON object: " + e.toString());
			return JSONObject.NULL;
		}
	}

	private static Object parseArray(String text, String column) {
		if (text == null) {
			return JSONObject.NULL;
		}
		try {
			return new JSONArray(text);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "PuttingData: " + column + " is not a JSON array: " + e.toString());
			return JSONObject.NULL;
		}
	}
}
