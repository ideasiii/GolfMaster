package com.golfmaster.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

/**
 * 檢查轉檔影片是否就緒、且影像分析是否完成。
 *
 * 三個欄位的角色：
 *   raw_shotVideo_X      → 廠商會送這部影片（expected）
 *   analyze_shotVideo_X  → 轉檔完成 + 對應的 mp4 URL（影片本體可用）
 *   id_analyzeVideo_X    → 影像分析完成（PSystem / SwingPlane / TPI / PoseImpact 已寫入 SVS）
 *
 * GET /service/VideoStatus?shotDataId=12345
 * 回傳 JSON:
 * {
 *   "front": {
 *     "expected": true,           // 廠商會送這部
 *     "ready": true,              // (legacy) 轉檔完成 — 給舊的 watch() 用
 *     "url": "../downloads/...",  // 轉檔完成的影片相對路徑
 *     "analysisReady": false      // 影像分析完成
 *   },
 *   "side":  { ... 同上 ... },
 *   "shouldReload": false         // 所有 expected 邊都已 analysisReady，且至少有一邊 expected
 * }
 */
@WebServlet("/service/VideoStatus")
public class VideoStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/** 影片基礎路徑，與 ShotVideo 中 VIDEO_BASE_URL 邏輯一致 */
	private final String videoBaseUrl;

	public VideoStatusServlet() {
		String baseUrl = Config.getParameter("videoBaseUrl");
		videoBaseUrl = (baseUrl != null && !baseUrl.isEmpty())
			? (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
			: "/downloads/video/";
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store");

		String shotDataId = request.getParameter("shotDataId");
		JSONObject json = new JSONObject();

		if (shotDataId == null || shotDataId.trim().isEmpty()) {
			json.put("error", "missing shotDataId parameter");
			writeResponse(response, json);
			return;
		}

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		String rawFront = "", rawSide = "";
		String analyzeFront = "", analyzeSide = "";
		String idAnalyzeFront = "", idAnalyzeSide = "";

		try {
			conn = DBUtil.getConnGolfMaster();
			ps = conn.prepareStatement(
				"SELECT raw_shotVideo_front, raw_shotVideo_side, "
				+ "analyze_shotVideo_front, analyze_shotVideo_side, "
				+ "id_analyzeVideo_front, id_analyzeVideo_side "
				+ "FROM golf_master.shot_video "
				+ "WHERE shot_data_id = ? LIMIT 1"
			);
			ps.setString(1, shotDataId);
			rs = ps.executeQuery();

			if (rs.next()) {
				rawFront = nz(rs.getString("raw_shotVideo_front"));
				rawSide = nz(rs.getString("raw_shotVideo_side"));
				analyzeFront = nz(rs.getString("analyze_shotVideo_front"));
				analyzeSide = nz(rs.getString("analyze_shotVideo_side"));
				idAnalyzeFront = nz(rs.getString("id_analyzeVideo_front"));
				idAnalyzeSide = nz(rs.getString("id_analyzeVideo_side"));
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "VideoStatusServlet error: " + e.getMessage());
			json.put("error", "db query failed");
			writeResponse(response, json);
			return;
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignored) {}
			try { if (ps != null) ps.close(); } catch (Exception ignored) {}
			try { if (conn != null) conn.close(); } catch (Exception ignored) {}
		}

		boolean frontExpected = !rawFront.isEmpty();
		boolean sideExpected = !rawSide.isEmpty();
		boolean frontVideoReady = !analyzeFront.isEmpty();
		boolean sideVideoReady = !analyzeSide.isEmpty();
		boolean frontAnalysisReady = !idAnalyzeFront.isEmpty();
		boolean sideAnalysisReady = !idAnalyzeSide.isEmpty();

		JSONObject frontStatus = new JSONObject();
		frontStatus.put("expected", frontExpected);
		frontStatus.put("ready", frontVideoReady);
		frontStatus.put("url", frontVideoReady ? toFrontendPath(analyzeFront) : "");
		frontStatus.put("analysisReady", frontAnalysisReady);

		JSONObject sideStatus = new JSONObject();
		sideStatus.put("expected", sideExpected);
		sideStatus.put("ready", sideVideoReady);
		sideStatus.put("url", sideVideoReady ? toFrontendPath(analyzeSide) : "");
		sideStatus.put("analysisReady", sideAnalysisReady);

		// 至少一邊 expected，且每個 expected 邊都 analysisReady → 可以 reload 拿完整結果
		boolean shouldReload = (frontExpected || sideExpected)
			&& (!frontExpected || frontAnalysisReady)
			&& (!sideExpected || sideAnalysisReady);

		json.put("front", frontStatus);
		json.put("side", sideStatus);
		json.put("shouldReload", shouldReload);

		writeResponse(response, json);
	}

	private static String nz(String s) {
		return s == null ? "" : s;
	}

	/**
	 * 將 DB 中的完整 URL 轉為前端可用的相對路徑。
	 * 例：http://127.0.0.1:8080/downloads/video/analyzVideo_front/xxx.mp4
	 *   → ../downloads/video/analyzVideo_front/xxx.mp4
	 */
	private String toFrontendPath(String dbUrl) {
		int idx = dbUrl.indexOf("/video/");
		if (idx >= 0) {
			return videoBaseUrl + dbUrl.substring(idx + "/video/".length());
		}
		return dbUrl;
	}

	private void writeResponse(HttpServletResponse response, JSONObject json) throws IOException {
		PrintWriter out = response.getWriter();
		out.print(json.toString());
		out.flush();
	}
}