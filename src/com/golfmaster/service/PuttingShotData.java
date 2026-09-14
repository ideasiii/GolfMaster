/*
 * 推桿專用的 shot_data 查詢（工項 12b）。
 *
 * ═══ 這支存在的理由 ═══
 * shot_data 是 E6 模擬器送進資料庫的擊球數據，跟 Core 的影像分析是兩條線
 * ——⛔ 不要為了這些欄位去問 Core，⛔ 也不需要等他的 fixture。
 *
 * ⛔ 不重用 ShortGameData / ShotData.processShortGameData：
 *    ShortGameData.processAnalyz() 的「落點」⛔ 不是量到的落點，是用**球飛行模型**
 *    幾何反推的（carryDistFt、tan(方向) × 飛行距離、側旋修正常數）。
 *    推桿的球⛔ 不會飛，那三行每一行都不成立。
 *    ⭐ 推桿版只讀 shot_data 的**原始量測值**，⛔ 不做任何幾何換算、⛔ 不套任何模型。
 *
 * ═══ ⛔ 球桿名稱絕對不可以寫死 ═══
 * 同樣是推桿，LID 017/018（馭風）存的是 'Putter'、LID 1000（工研院）存的是 'P'。
 * 力揮（LID 2000）目前⛔ 沒有推桿資料，它會存什麼字⛔ 現在無從得知。
 * → ⭐ ClubType 一律從「這一推自己那一列」取（queryThisPutt），再拿它去撈同一個球員的其他推。
 *   這樣在哪一台就自動用那一台的字，⛔ 完全不必知道是 'Putter' 還是 'P'。
 * ⚠️ 寫死字串的後果是⛔ 不會報錯的：`WHERE ClubType LIKE '%Putter%'` 在工研院那台
 *    一筆都撈不到，畫面只會變成「這個球員沒有推桿紀錄」。
 *
 * ⚠️ 但「這一推是不是推桿」⛔ 不可以靠 ClubType 判斷 —— 那是**輸入**，
 *    AnalysisMode 才是**結果**（規劃 §3.2 地雷 1）。
 *    ⭐ 這支只負責「撈同一支球桿的紀錄」，⛔ 不負責判斷推桿與否。
 *
 * ═══ ⛔ 一條不可跨越的線 ═══
 * 算不出來、不可信 → 整格不出現，⛔ 絕不可拿沒有依據的數字填版面。
 * 錯的數字⛔ 不會報錯，只會安靜地看起來很合理。
 */
package com.golfmaster.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class PuttingShotData {

	/**
	 * ⚠️ E6 的球速上限哨兵值。
	 *
	 * 2026-09-11 實測 golf_master.shot_data 全表：BallSpeed 恰好等於 160.0 的列，
	 * ClubHeadSpeed 幾乎一律同時是 120.0，而且**沒有任何一筆超過 160**
	 * （推桿 12857 筆裡有 12 筆是這個組合，其中一筆是 0.4 呎的推配 160 mph）。
	 * 每一種球桿都有這個組合 → ⛔ 那是 E6 沒量到時填的固定值，⛔ 不是量到的。
	 *
	 * ⚠️ 這⛔ 不是「推桿的球速不可能這麼快」那種自訂的合理性門檻
	 *    （那種門檻這一頁⛔ 一律不自己訂）——
	 * ⭐ 判準是「整欄的最大值就停在這個數、而且固定成對出現」，跟 §12b 用
	 *    「整欄是不是同一個值」判斷 ClubAnglePath 沒被送是同一種欄位層級的證據。
	 */
	private static final float E6_BALL_SPEED_SENTINEL = 160.0f;
	private static final float E6_CLUB_HEAD_SPEED_SENTINEL = 120.0f;

	/** 這一推（或最近幾推）在 shot_data 上的原始量測值。⛔ 全部是量到的，⛔ 沒有任何換算。 */
	public static class PuttShot {
		public long id;
		public String player;
		public String clubType;
		public String lid;
		public String date;

		/** ⚠️ 以下都用 Float（⛔ 不是 float）：欄位是 NULL 時要分得出來，⛔ 不可以變成 0.0。 */
		public Float ballSpeed;
		public Float clubHeadSpeed;
		public Float launchDirection;
		public Float distToPinFt;
		public Float totalDistFt;
	}

	/**
	 * 右欄數值面板要的東西（工項 12b 目前只有球速一列）。
	 *
	 * ⛔ 這一輪**只放球速**。桿面角與面路差已評估後排除，⛔ 不要「補回來」：
	 *   · 揮桿路徑 ClubAnglePath —— 10052 筆推桿有 9911 筆是 0，E6 根本沒送這一欄。
	 *   · 面路差（桿面角 − 揮桿路徑）—— ⛔ 減數整欄是空的，相減會得到一個
	 *     **恰好等於桿面角**的數字卻被標成「面路差」，⛔ 不報錯、⛔ 畫面完全正常。
	 *   · 桿面角 ClubAngleFace —— 在 017/018 有值，⛔ 但⛔ 不是每一台機器都給
	 *     （LID 1000 那 100 筆推桿整欄是 0），只驗過一台就上會在別台變成一排 0。
	 * ⚠️ 版面上限實測是 4 列，現在用掉 3 列（節奏比、總時長、球速）。
	 *
	 * @param shot_data_id 網址 ?expert= 帶進來的擊球 ID
	 * @return JSONObject；⭐ 某一格算不出來或不可信時**那個鍵就不存在**
	 *         （⛔ 不是填 "—"、⛔ 不是填 0）→ 頁面端 setValues() 會讓整列不出現。
	 *         球速不出現時改放 ballSpeedReason（代碼見 ballSpeedReason()），
	 *         ⛔ 只給〔詳細數值〕的「狀態」分頁，⛔ 主畫面不顯示。
	 */
	public JSONObject processPuttValues(Long shot_data_id) {
		JSONObject values = new JSONObject();
		PuttShot shot = (shot_data_id == null) ? null : queryThisPutt(shot_data_id);

		String reason = ballSpeedReason(shot);
		if (reason.isEmpty()) {
			// ⚠️ 單位（mph）寫在 jsp 的標籤括號裡，⛔ 這裡只回數字
			values.put("ballSpeed", String.format("%.1f", shot.ballSpeed));
		} else {
			values.put("ballSpeedReason", reason);
		}
		return values;
	}

	/**
	 * 球速這一格為什麼不可信。可信時回空字串。
	 *
	 *   not_found   查不到這一推
	 *   not_sent    欄位是 NULL，或 E6 沒送（存成 0）
	 *   sentinel    E6 的上限哨兵值（160 配 120），⛔ 不是量到的
	 *
	 * ⚠️⚠️ 那幾個數值欄位在 Java 裡宣告成 float、預設 0.0f
	 *      → **E6 沒送的欄位會存成 0，⛔ 不是 NULL** → ⛔ 只檢查 null 是不夠的。
	 * ⚠️ 但「0 是不是合法值」⛔ 各欄不同：桿面角與出球方向的 0 是合法的（桿面正、球直直出去），
	 *    ⛔ 球速的 0 ⛔ 不是 —— 球沒有動就不存在這一推。
	 *    ⭐ 實測 12857 筆推桿⛔ 沒有任何一筆球速是 0 或 NULL，所以 0 只會是沒送。
	 * ⚠️ 哨兵值那種列⛔ 不會報錯：0.4 呎的推配 160 mph 照樣渲染得出來。
	 */
	private static String ballSpeedReason(PuttShot shot) {
		if (shot == null) {
			return "not_found";
		}
		Float speed = shot.ballSpeed;
		if (speed == null || speed <= 0f) {
			return "not_sent";
		}
		if (speed >= E6_BALL_SPEED_SENTINEL
				&& shot.clubHeadSpeed != null
				&& shot.clubHeadSpeed == E6_CLUB_HEAD_SPEED_SENTINEL) {
			return "sentinel";
		}
		return "";
	}

	/**
	 * 讀「這一推自己那一列」。
	 * ⭐ ClubType 與 LID 從這裡取，⛔ 呼叫端不需要（也不可以）知道那台機器用什麼字。
	 */
	private PuttShot queryThisPutt(Long shot_data_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PuttShot shot = null;

		// ⚠️ id 是 BIGINT，⛔ Java 這邊不可宣告成 Integer
		String strSQL = "SELECT id, Player, ClubType, LID, Date, "
				+ "BallSpeed, ClubHeadSpeed, LaunchDirection, DistToPinFt, TotalDistFt "
				+ "FROM golf_master.shot_data "
				+ "WHERE id = ?";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setLong(1, shot_data_id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				shot = readRow(rs);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		return shot;
	}

	/**
	 * 撈同一個球員、同一支球桿、同一個 LID 的最近幾推。
	 *
	 * ⭐ 給推桿穩定度圖（規劃 §2.10）用的底，⚠️ 這一輪⛔ 還沒有畫面在吃它。
	 * ⛔ 撈幾推是**參數**，⛔ 不寫死。
	 * ⛔ ShotData.queryShortGameData() 有一行 `if (maxRecords < 10) maxRecords = 10;`
	 *    ——⛔ 推桿版⛔ 不照抄那一行：呼叫端要幾推就是幾推，⛔ 不要偷偷改大。
	 *
	 * ⚠️ 篩 LID 的理由：LID 是我方發給**廠商**的 ID（⛔ 不是場館、⛔ 不是機器）。
	 *    與 E6 合作的幾家標準相同，工研院（1000）是另一套（側旋正負號相反）。
	 *    ⭐ 讀「這一推的單一數值」完全不受影響，⛔ 但把不同 LID 的資料倒在一起算離散度
	 *    ⛔ 就不行 —— 那是穩定度圖真的要畫時的事，這裡先把來源限定在同一個 LID。
	 */
	public List<PuttShot> queryRecentPutts(String player, String clubType, String lid,
			String endDate, int maxRecords) {
		List<PuttShot> shots = new ArrayList<>();

		// ⛔ 沒有球桿名稱就不要撈：⛔ 不可以退成「撈全部球桿」，那會把切桿與木桿混進來
		if (player == null || player.isEmpty()
				|| clubType == null || clubType.isEmpty()
				|| lid == null || lid.isEmpty()
				|| maxRecords <= 0) {
			return shots;
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String strSQL = "SELECT id, Player, ClubType, LID, Date, "
				+ "BallSpeed, ClubHeadSpeed, LaunchDirection, DistToPinFt, TotalDistFt "
				+ "FROM golf_master.shot_data "
				+ "WHERE Player = ? "
				+ "AND ClubType = ? "   // ⭐ 由 queryThisPutt() 取得，⛔ 不寫死
				+ "AND LID = ? "
				+ "AND Date <= ? "
				+ "ORDER BY id DESC "
				+ "LIMIT ?";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, player);
			pstmt.setString(2, clubType);
			pstmt.setString(3, lid);
			pstmt.setString(4, endDate);
			pstmt.setInt(5, maxRecords);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				shots.add(readRow(rs));
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(rs, pstmt, conn);
		}
		return shots;
	}

	/** ⚠️ 數值欄位一律走 wasNull()，⛔ 不可以讓 NULL 靜靜變成 0.0。 */
	private static PuttShot readRow(ResultSet rs) throws Exception {
		PuttShot shot = new PuttShot();
		shot.id = rs.getLong("id");
		shot.player = rs.getString("Player");
		shot.clubType = rs.getString("ClubType");
		shot.lid = rs.getString("LID");
		shot.date = rs.getString("Date");
		shot.ballSpeed = nullableFloat(rs, "BallSpeed");
		shot.clubHeadSpeed = nullableFloat(rs, "ClubHeadSpeed");
		shot.launchDirection = nullableFloat(rs, "LaunchDirection");
		shot.distToPinFt = nullableFloat(rs, "DistToPinFt");
		shot.totalDistFt = nullableFloat(rs, "TotalDistFt");
		return shot;
	}

	private static Float nullableFloat(ResultSet rs, String column) throws Exception {
		float value = rs.getFloat(column);
		return rs.wasNull() ? null : Float.valueOf(value);
	}
}
