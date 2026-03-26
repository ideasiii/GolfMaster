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
 * 檢查轉檔後的分析影片是否已寫入資料庫（表示轉檔完成）。
 *
 * Python 端轉檔流程：轉檔成功 → 才寫入 analyze_shotVideo_front / analyze_shotVideo_side URL 到 DB。
 * 因此 DB 有值 = 轉檔完成、檔案可用。
 *
 * GET /service/VideoStatus?shotDataId=12345
 * 回傳 JSON:
 * {
 *   "front": { "ready": true, "url": "../downloads/video/analyzVideo_front/xxx.mp4" },
 *   "side":  { "ready": false, "url": "" }
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

		try {
			conn = DBUtil.getConnGolfMaster();
			ps = conn.prepareStatement(
				"SELECT analyze_shotVideo_front, analyze_shotVideo_side "
				+ "FROM golf_master.shot_video "
				+ "WHERE shot_data_id = ? LIMIT 1"
			);
			ps.setString(1, shotDataId);
			rs = ps.executeQuery();

			String frontDbUrl = "";
			String sideDbUrl = "";

			if (rs.next()) {
				String f = rs.getString("analyze_shotVideo_front");
				String s = rs.getString("analyze_shotVideo_side");
				frontDbUrl = (f != null) ? f : "";
				sideDbUrl = (s != null) ? s : "";
			}

			json.put("front", buildStatus(frontDbUrl));
			json.put("side", buildStatus(sideDbUrl));

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "VideoStatusServlet error: " + e.getMessage());
			json.put("error", "db query failed");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignored) {}
			try { if (ps != null) ps.close(); } catch (Exception ignored) {}
			try { if (conn != null) conn.close(); } catch (Exception ignored) {}
		}

		writeResponse(response, json);
	}

	/**
	 * 根據 DB 中的 analyze URL 產生狀態物件。
	 * DB 有值 = 轉檔完成（Python 端轉檔成功才寫入）。
	 * 回傳的 url 會轉成前端可用的相對路徑。
	 */
	private JSONObject buildStatus(String dbUrl) {
		JSONObject status = new JSONObject();
		boolean ready = !dbUrl.isEmpty();
		status.put("ready", ready);
		status.put("url", ready ? toFrontendPath(dbUrl) : "");
		return status;
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