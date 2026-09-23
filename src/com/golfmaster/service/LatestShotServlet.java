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
 * 回報某個廠商（LID）目前最新一筆擊球的 id。
 *
 * GET /service/LatestShot?LID=1000
 *   {"latestId": 588101}    該 LID 的最大 id
 *   {"latestId": null}      該 LID 沒有任何紀錄
 *   {"error": "..."}        沒帶 LID 或查詢失敗
 *
 * ⚠️ 只回一個 id，⛔ 不回那一推的內容 —— 呼叫端要的只是「變了沒有」。
 * ⚠️ 用 MAX(id)，⛔ 不要改成 ORDER BY Date：id 是自增主鍵，同一秒進來兩筆也不會挑錯。
 * ⚠️ LID ⛔ 一定要篩，否則別台模擬器打球也會被算成「有新的一推」。
 */
@WebServlet("/service/LatestShot")
public class LatestShotServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("application/json;charset=UTF-8");
		// ⛔ 不可以被快取：呼叫端是定期輪詢，快取住就永遠拿到第一次那個值
		response.setHeader("Cache-Control", "no-store");

		JSONObject out = new JSONObject();
		String lid = request.getParameter("LID");
		if (lid == null || lid.trim().isEmpty()) {
			out.put("error", "missing LID");
			write(response, out);
			return;
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement("SELECT MAX(id) AS latestId FROM golf_master.shot_data WHERE LID = ?");
			pstmt.setString(1, lid.trim());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				long latest = rs.getLong("latestId");
				// ⚠️ 沒有資料時 getLong 回 0，⛔ 不是 null → 一定要問 wasNull()
				out.put("latestId", rs.wasNull() ? JSONObject.NULL : Long.valueOf(latest));
			} else {
				out.put("latestId", JSONObject.NULL);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, "LatestShotServlet failed: " + e.toString());
			out.put("error", "query failed");
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		write(response, out);
	}

	private void write(HttpServletResponse response, JSONObject out) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write(out.toString());
		writer.flush();
	}
}
