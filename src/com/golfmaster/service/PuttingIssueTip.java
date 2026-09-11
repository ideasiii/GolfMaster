/*
 * 推桿文案查詢（工項 15）。
 *
 * ═══ 這支取代了什麼 ═══
 * 在這之前，18 條文案是**整份抄在 puttingIssuesManager.js 最下面**的 PUTT_ISSUE_TIPS。
 * ⭐ 這支上線之後，接上資料庫的那條路徑改由 putting_issue_tip 表供應，
 *    ⛔ 頁面端只換 lookupTip() 一個接縫，⛔ 渲染那一段一行都不用動。
 *
 * ═══ ⚠️ 權威來源 ═══
 * 文案的權威來源是 golf_anl_ws/.../working_spec/feedback/putting_issue_seed.json（18 條）。
 * ⛔ 改文案要回去改那一份再同步進資料庫，⛔ 不要只改資料庫、⛔ 也不要只改 js。
 *
 * ═══ ⚠️ R1：正式機還沒有這張表 ═══
 * putting_issue_tip 目前**只有測試機建了**（2026-09-11 實測：18 筆，Coach 全部是 'default'）。
 * ⛔ 正式機上線前要建表灌資料。
 * ⭐ 查不到時的行為是規劃 §3.2「查詢 B」明訂的：
 *    退 default → 還是查不到 → ⛔ **卡片仍然要出現**，只是沒有那段文字。
 *    ⛔ 不要回「查無文案」那種字，⛔ 也不要讓卡片消失。
 */
package com.golfmaster.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class PuttingIssueTip {

	/** 查不到教練專屬文案時墊底的那一個值，⛔ 對應 putting_issue_tip.Coach 的預設列。 */
	private static final String DEFAULT_COACH = "default";

	/**
	 * ⚠️ 18 條讀進記憶體快取，⛔ 不要一張卡片查一次資料庫（規劃 §3.2「查詢 B」）。
	 * ⚠️ 快取有存活時間：文案審閱階段有人會直接改資料庫，
	 *    ⛔ 永久快取會讓改完的文案要重啟 Tomcat 才看得到。
	 */
	private static final long CACHE_TTL_MS = 5 * 60 * 1000L;

	private static final Map<String, CachedTips> CACHE = new HashMap<>();

	private static class CachedTips {
		Map<String, String> tips;
		long loadedAt;
	}

	/**
	 * 取某位教練看得到的全部文案。
	 *
	 * ⭐ 一次撈完這位教練 ＋ default 的所有列，⛔ 不是一個 tip_id 查一次。
	 * ⭐ 教練專屬的蓋掉 default 的（＝規劃 §3.2 那句「排序讓專屬排前面、default 墊底，取第一列」，
	 *    ⛔ 用一次查詢 ＋ 覆蓋做到，語意相同）。
	 * ⛔ 只寫 `Coach = 該教練` 是錯的 —— 沒有專屬文案的教練會查到空的、畫面整片沒字。
	 *
	 * @param coach 教練代號；⚠️ 這一頁目前**沒有教練身分**這個概念，傳 null 即可 →
	 *              只會拿到 default 那 18 條。⭐ 介面先留著，之後有了不必改這支。
	 * @return tipId → tipText 的 JSONObject；⛔ 查不到就是空的（⛔ 不是 null）。
	 */
	public JSONObject tipsFor(String coach) {
		Map<String, String> tips = load(coach);
		JSONObject json = new JSONObject();
		for (Map.Entry<String, String> e : tips.entrySet()) {
			json.put(e.getKey(), e.getValue());
		}
		return json;
	}

	/**
	 * 這位教練實際載到幾條。
	 * ⭐ 給〔詳細數值〕的「狀態」分頁用（工項 13）——
	 *    ⚠️ 0 條代表**表是空的或查不到**，⛔ 那是要讓人查得到的事實，
	 *    ⛔ 但⛔ 不可以寫到主畫面上（user 裁示：主畫面不跟使用者解釋系統做不到什麼）。
	 */
	public int tipCount(String coach) {
		return load(coach).size();
	}

	/** ⚠️ 改完資料庫想立刻看到效果時呼叫，⛔ 平常不需要。 */
	public static void clearCache() {
		synchronized (CACHE) {
			CACHE.clear();
		}
	}

	private Map<String, String> load(String coach) {
		String key = (coach == null || coach.trim().isEmpty()) ? DEFAULT_COACH : coach.trim();

		synchronized (CACHE) {
			CachedTips cached = CACHE.get(key);
			if (cached != null && (System.currentTimeMillis() - cached.loadedAt) < CACHE_TTL_MS) {
				return cached.tips;
			}
		}

		Map<String, String> tips = query(key);

		synchronized (CACHE) {
			CachedTips entry = new CachedTips();
			entry.tips = tips;
			entry.loadedAt = System.currentTimeMillis();
			CACHE.put(key, entry);
		}
		return tips;
	}

	private Map<String, String> query(String coach) {
		Map<String, String> tips = new HashMap<>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// ⭐ 一次撈這位教練 ＋ default 的全部列。
		// ⚠️ ORDER BY 讓 default 先出來、專屬的後出來 →
		//    後面的 put() 會蓋掉前面的，⭐ 結果就是「有專屬用專屬、沒有就用 default」。
		//    ⛔ 順序反過來就變成 default 蓋掉專屬，⛔ 而且⛔ 不會報錯。
		String strSQL = "SELECT TipId, Coach, TipText "
				+ "FROM golf_master.putting_issue_tip "
				+ "WHERE Coach = ? OR Coach = ? "
				+ "ORDER BY CASE WHEN Coach = ? THEN 0 ELSE 1 END";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, coach);
			pstmt.setString(2, DEFAULT_COACH);
			pstmt.setString(3, DEFAULT_COACH);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String tipId = rs.getString("TipId");
				String tipText = rs.getString("TipText");
				// ⛔ 空字串的文案⛔ 不放進來：放進去會蓋掉 default 那一條，
				//    畫面上變成「有這張卡但沒有字」，⛔ 而且看不出是被蓋掉的
				if (tipId != null && tipText != null && !tipText.trim().isEmpty()) {
					tips.put(tipId, tipText);
				}
			}
		} catch (Exception e) {
			// ⛔ 查不到⛔ 不是錯誤畫面：卡片照樣出現，只是沒有那段文字（規劃 §3.2）。
			//    ⚠️ 但要留下紀錄 —— 正式機還沒建表（R1），⛔ 不要讓它安靜地沒字。
			Logs.log(Logs.EXCEPTION_LOG, "PuttingIssueTip.query failed: " + e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}

		Logs.log(Logs.RUN_LOG, "PuttingIssueTip loaded " + tips.size() + " tips for coach=" + coach);
		return tips;
	}
}
