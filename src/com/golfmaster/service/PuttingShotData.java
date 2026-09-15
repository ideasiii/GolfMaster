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

import org.json.JSONArray;
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
		public Float launchAngle;
		public Float smashFactor;
		public Float distToPinFt;
		public Float totalDistFt;
	}

	/**
	 * 這一推在頁面上要用的擊球數據。
	 *
	 * ballSpeed：右欄數值面板那一列。⭐ 不可信時**那個鍵不存在**（⛔ 不是填 "—"、⛔ 不是填 0）
	 *   → 頁面端 setValues() 會讓整列不出現；改放 ballSpeedReason（代碼見 ballSpeedReason()），
	 *   ⛔ 只給〔詳細數值〕的「狀態」分頁，⛔ 主畫面不顯示。
	 *
	 * shotCards：影片下方的擊球數據卡片（推桿距離、出球方向、發射角度、擊球效率）。
	 *   ⭐ 原始量測值照搬，欄位是 NULL 或查不到這一推就是 null，頁面顯示「--」。
	 *   ⚠️ 出球方向與發射角度的 0 是合法值（桿面正、球貼地出去），⛔ 不可以當成沒有值。
	 *   ⚠️ 球速⛔ 不在卡片裡：同一個數字⛔ 不可以在畫面上出現兩次。
	 *
	 * ⛔ 評估後**不放**的，⛔ 不要「補回來」：
	 *   · 揮桿路徑 ClubAnglePath —— 10052 筆推桿有 9911 筆是 0，E6 根本沒送這一欄。
	 *   · 面路差（桿面角 − 揮桿路徑）—— ⛔ 減數整欄是空的，相減會得到一個
	 *     **恰好等於桿面角**的數字卻被標成「面路差」，⛔ 不報錯、⛔ 畫面完全正常。
	 *   · 桿面角 ClubAngleFace —— LID 1000 那 100 筆推桿整欄是 0。
	 *   · 桿頭速度 ClubHeadSpeed —— LID 1000 那 100 筆推桿整欄是 120。
	 *   · 總距離、飛行距離 —— 推桿的滾動是模擬器依草皮假設算的，⛔ 不是量到的。
	 *
	 * @param shot_data_id 這一推的 shot_data.id（⛔ 不是網址的 ?expert=）
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

		JSONObject cards = new JSONObject();
		cards.put("distToPinFt", numberOrNull(shot == null ? null : shot.distToPinFt));
		cards.put("launchDirection", numberOrNull(shot == null ? null : shot.launchDirection));
		cards.put("launchAngle", numberOrNull(shot == null ? null : shot.launchAngle));
		// ⚠️ LID 1000 目前每一筆都是 1.33（桿頭速度固定 120 時的值），照模擬器給的顯示
		cards.put("smashFactor", numberOrNull(shot == null ? null : shot.smashFactor));
		values.put("shotCards", cards);
		return values;
	}

	/**
	 * 推桿穩定度圖：同一個 Player、同一支球桿的最近幾推（含這一推），新的在前。
	 *
	 * 回傳 {"currentId": 這一推的 id, "shots": [{"id", "ballSpeed", "launchDirection"}, …]}。
	 * 查不到這一推時 shots 是空陣列。
	 * ⚠️ 球速不可信（沒送、哨兵值）或出球方向是 NULL 的推⛔ 不放進來，所以筆數可能少於 maxRecords。
	 * ⚠️ 不分推的距離：距離不同的推混在一起時，散得開也可能只是距離不同。
	 *
	 * @param shot_data_id 這一推的 shot_data.id
	 * @param maxRecords   最多撈幾推
	 * @param sameLidOnly  true：只撈同一個 LID（各廠商量測標準可能不同）；false：不限 LID
	 */
	public JSONObject processPuttConsistency(Long shot_data_id, int maxRecords, boolean sameLidOnly) {
		JSONObject result = new JSONObject();
		JSONArray list = new JSONArray();
		PuttShot shot = (shot_data_id == null) ? null : queryThisPutt(shot_data_id);
		result.put("currentId", shot == null ? JSONObject.NULL : Long.valueOf(shot.id));

		if (shot != null) {
			List<PuttShot> shots = queryRecentPutts(shot.player, shot.clubType,
					sameLidOnly ? shot.lid : null, shot.id, maxRecords);
			for (PuttShot s : shots) {
				JSONObject o = new JSONObject();
				o.put("id", s.id);
				o.put("ballSpeed", numberOrNull(s.ballSpeed));
				o.put("launchDirection", numberOrNull(s.launchDirection));
				list.put(o);
			}
		}
		result.put("shots", list);
		return result;
	}

	private static Object numberOrNull(Float value) {
		// ⚠️ 用 Float 的字串轉 Double：直接轉會把 8.2 變成 8.199999809265137
		return value == null ? JSONObject.NULL : Double.valueOf(value.toString());
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
				+ "BallSpeed, ClubHeadSpeed, LaunchDirection, LaunchAngle, SmashFactor, DistToPinFt, TotalDistFt "
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
	 * 撈同一個球員、同一支球桿的最近幾推（id 不大於 maxId，新的在前）。
	 *
	 * ⛔ 撈幾推是**參數**，⛔ 不寫死。
	 * ⛔ ShotData.queryShortGameData() 有一行 `if (maxRecords < 10) maxRecords = 10;`
	 *    ——⛔ 推桿版⛔ 不照抄那一行：呼叫端要幾推就是幾推，⛔ 不要偷偷改大。
	 * ⚠️ 球速不可信（NULL、0、哨兵值）或出球方向是 NULL 的推在 SQL 就排除，
	 *    判斷跟 ballSpeedReason() 相同，⛔ 兩邊要一起改。
	 *
	 * @param lid ⚠️ null 或空字串＝不限 LID。LID 是我方發給**廠商**的 ID（⛔ 不是場館、⛔ 不是機器），
	 *            工研院（1000）與 E6 合作的幾家量測標準可能不同，要分開時傳這一推的 LID。
	 */
	public List<PuttShot> queryRecentPutts(String player, String clubType, String lid,
			long maxId, int maxRecords) {
		List<PuttShot> shots = new ArrayList<>();
		boolean filterLid = lid != null && !lid.isEmpty();

		// ⛔ 沒有球桿名稱就不要撈：⛔ 不可以退成「撈全部球桿」，那會把切桿與木桿混進來
		if (player == null || player.isEmpty()
				|| clubType == null || clubType.isEmpty()
				|| maxRecords <= 0) {
			return shots;
		}

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String strSQL = "SELECT id, Player, ClubType, LID, Date, "
				+ "BallSpeed, ClubHeadSpeed, LaunchDirection, LaunchAngle, SmashFactor, DistToPinFt, TotalDistFt "
				+ "FROM golf_master.shot_data "
				+ "WHERE Player = ? "
				+ "AND ClubType = ? "   // ⭐ 由 queryThisPutt() 取得，⛔ 不寫死
				+ (filterLid ? "AND LID = ? " : "")
				+ "AND id <= ? "
				+ "AND BallSpeed > 0 "
				+ "AND LaunchDirection IS NOT NULL "
				+ "AND NOT (BallSpeed >= ? AND ClubHeadSpeed IS NOT NULL AND ClubHeadSpeed = ?) "
				+ "ORDER BY id DESC "
				+ "LIMIT ?";

		try {
			conn = DBUtil.getConnGolfMaster();
			pstmt = conn.prepareStatement(strSQL);
			int i = 1;
			pstmt.setString(i++, player);
			pstmt.setString(i++, clubType);
			if (filterLid) {
				pstmt.setString(i++, lid);
			}
			pstmt.setLong(i++, maxId);
			pstmt.setFloat(i++, E6_BALL_SPEED_SENTINEL);
			pstmt.setFloat(i++, E6_CLUB_HEAD_SPEED_SENTINEL);
			pstmt.setInt(i++, maxRecords);
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
		shot.launchAngle = nullableFloat(rs, "LaunchAngle");
		shot.smashFactor = nullableFloat(rs, "SmashFactor");
		shot.distToPinFt = nullableFloat(rs, "DistToPinFt");
		shot.totalDistFt = nullableFloat(rs, "TotalDistFt");
		return shot;
	}

	private static Float nullableFloat(ResultSet rs, String column) throws Exception {
		float value = rs.getFloat(column);
		return rs.wasNull() ? null : Float.valueOf(value);
	}
}
