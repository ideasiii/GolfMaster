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

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

/**
 * 取得指定 shot_data_id 的影像分析結果（PSystem frames + SwingPlane）。
 * 給前端在「單邊分析剛完成、整頁尚未 reload」時，做單邊 AJAX 補上左上角影片覆蓋層用。
 *
 * GET /service/AnalysisData?shotDataId=12345
 * 回傳 JSON:
 * {
 *   "front": { "frames": [a,t,i,f], "swingPlane": {...} } 或 null,
 *   "side":  { "frames": [a,t,i,f], "swingPlane": {...} } 或 null
 * }
 */
@WebServlet("/service/AnalysisData")
public class AnalysisDataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache, no-store");

		String shotDataId = request.getParameter("shotDataId");
		JSONObject json = new JSONObject();
		json.put("front", JSONObject.NULL);
		json.put("side", JSONObject.NULL);

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
				"SELECT SVS.CamPos AS CamPos, "
				+ "SVS.PlayerPSystem AS PlayerPSystem, "
				+ "SVS.SwingPlane AS SwingPlane "
				+ "FROM golf_master.shot_video AS SV "
				+ "LEFT JOIN golf_master.shot_video_swing AS SVS ON SVS.ShotVideoId = SV.id "
				+ "WHERE SV.shot_data_id = ?"
			);
			ps.setString(1, shotDataId);
			rs = ps.executeQuery();

			while (rs.next()) {
				String camPos = rs.getString("CamPos");
				if (camPos == null || camPos.isEmpty()) continue;

				JSONObject sideObj = new JSONObject();

				String pSystemStr = rs.getString("PlayerPSystem");
				if (pSystemStr != null && !pSystemStr.isEmpty()) {
					try {
						JSONArray frames = new JSONObject(pSystemStr).optJSONArray("data");
						if (frames != null) sideObj.put("frames", frames);
					} catch (Exception ignore) {}
				}

				String swingPlaneStr = rs.getString("SwingPlane");
				if (swingPlaneStr != null && !swingPlaneStr.isEmpty()) {
					try {
						sideObj.put("swingPlane", new JSONObject(swingPlaneStr));
					} catch (Exception ignore) {}
				}

				if ("front".equals(camPos)) {
					json.put("front", sideObj);
				} else if ("side".equals(camPos)) {
					json.put("side", sideObj);
				}
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "AnalysisDataServlet error: " + e.getMessage());
			json.put("error", "db query failed");
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ignored) {}
			try { if (ps != null) ps.close(); } catch (Exception ignored) {}
			try { if (conn != null) conn.close(); } catch (Exception ignored) {}
		}

		writeResponse(response, json);
	}

	private void writeResponse(HttpServletResponse response, JSONObject json) throws IOException {
		PrintWriter out = response.getWriter();
		out.print(json.toString());
		out.flush();
	}
}