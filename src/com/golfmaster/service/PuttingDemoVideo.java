/*
 * 推桿頁的示範影片。
 *
 * 這一推沒有影片時 ShotVideo 會退回「三頁共用」的示範影片（現在那一支是揮桿示範）。
 * 這支類別只做一件事：把那幾格換成推桿專用的示範影片，並交出那支影片自己的界標。
 *
 * ⛔ 不動 ShotVideo（揮桿與切桿都在用它）。
 * ⛔ 這裡不查資料庫、⛔ 不碰判定結果 —— 那是 PuttingData 的事。
 *
 * 換示範影片時要一起換的兩處：
 *   1. context.xml 的 defaultPuttFrontVideo / defaultPuttSideVideo（檔名）
 *   2. 下面那幾個界標常數（幀號）
 * ⛔ 只換其中一邊，界標就會跳到錯的地方，⛔ 而且不會報錯。
 */
package com.golfmaster.service;

import org.json.JSONArray;
import org.json.JSONObject;

public class PuttingDemoVideo {

	/*
	 * ── 推桿示範影片的界標 ────────────────────────────────────────────
	 * 這一推沒有自己的影片時畫面上播的那一支（檔名在 context.xml 的
	 * defaultPuttFrontVideo / defaultPuttSideVideo）。
	 * ⚠️ 位置比照揮桿與切桿：檔名在 context.xml、幀號在 Java
	 *    （那兩頁是 ShotVideo 的 defaultFrontArray / defaultSideArray）。
	 *
	 * ⚠️⚠️ 這一組是**人工標註**的，⛔ 不是演算法輸出 —— 所以四顆都當可信。
	 *      ⛔ 演算法輸出⛔ 絕對不可以照這樣填 found：
	 *      界標找不到時填進去的值仍然落在合法範圍內，跳過去不會報錯、
	 *      只會安靜停在錯的地方。
	 * ⚠️ 正面與側面是兩支不同的影片，幀號⛔ 不可互換
	 *    （這一支兩面的碰球差 22 幀、架桿差 40 幀、收桿差 6 幀，⛔ 不是固定差）。
	 * ⚠️ 影片做過水平翻轉與裁掉下緣，⛔ 兩者都不動時間軸 → 幀號照用。
	 * ⚠️ 換示範影片時這幾個數字要跟著換，⛔ 不換就會跳到錯的地方。
	 * ─────────────────────────────────────────────────────────────── */
	private static final double DEMO_FPS = 60.0;
	/** 架桿、頂點、碰球、收桿 */
	private static final int[] DEMO_FRONT_DATA = { 135, 271, 332, 389 };
	private static final int DEMO_FRONT_ONSET = 165;
	private static final int[] DEMO_SIDE_DATA = { 175, 305, 354, 395 };
	private static final int DEMO_SIDE_ONSET = 199;

	/**
	 * 推桿頁要播哪兩支影片。
	 *
	 * ShotVideo 已經處理過「這一推沒有影片就退回示範影片」，⛔ 但它退回的是三頁共用的
	 * 那一支（現在是揮桿示範）。這裡把它換成推桿專用的示範影片
	 * （context.xml 的 defaultPuttFrontVideo / defaultPuttSideVideo）。
	 *
	 * ⚠️⚠️ 換成**哪一個視角**，照 ShotVideo 挑中的那一支走，⛔ 不是照格子走：
	 *   ShotVideo 在單邊缺影片時，是拿**另一邊那支**示範片頂上 ——
	 *   只有正面影片時，側面那一格放的是「示範片的正面」。
	 *   ⭐ 那是刻意的：同一個視角才比得了（使用者的正面 對 示範的正面）。
	 *   ⛔ 換成示範片的側面，就變成「使用者正面 對 示範側面」，⛔ 兩者不可比。
	 *
	 * ⚠️ 預設路徑的組法必須跟 ShotVideo 一致（同樣三個參數、同樣的預設值）——
	 *    ⛔ 那邊改了這裡不會自動跟上。⛔ 不動 ShotVideo（揮桿與切桿都在用它）。
	 * ⚠️ 沒設推桿專用的鍵就整段不換，沿用原本那一支（那時沒有界標可用）。
	 *
	 * @param frontPath / sidePath ShotVideo.processAnalyz() 給的兩個路徑
	 * @return { frontPath, sidePath, frontDemoView, sideDemoView }
	 *         兩個 view 是 "front"／"side"／""（""＝那一格不是推桿示範片）
	 */
	public static JSONObject resolveVideos(String frontPath, String sidePath) {
		String base = folder(Config.getParameter("videoBaseUrl"), "/downloads/video/");
		String frontFolder = folder(Config.getParameter("videoFrontFolder"), "analyzVideo_front/");
		String sideFolder = folder(Config.getParameter("videoSideFolder"), "analyzVideo_side/");

		String sharedFront = base + frontFolder
				+ value(Config.getParameter("defaultFrontVideo"), "Player0_shotVideo_front_160230_202405151602.mp4");
		String sharedSide = base + sideFolder
				+ value(Config.getParameter("defaultSideVideo"), "Player0_shotVideo_side_160230_202405151602.mp4");

		String puttFront = value(Config.getParameter("defaultPuttFrontVideo"), "");
		String puttSide = value(Config.getParameter("defaultPuttSideVideo"), "");
		String puttFrontPath = puttFront.isEmpty() ? "" : base + frontFolder + puttFront;
		String puttSidePath = puttSide.isEmpty() ? "" : base + sideFolder + puttSide;

		JSONObject out = new JSONObject();
		out.put("frontPath", frontPath);
		out.put("sidePath", sidePath);
		out.put("frontDemoView", "");
		out.put("sideDemoView", "");
		// ⚠️ 播的是不是這一推自己的影片：⛔ 兩支示範片都要比對 ——
		//    單邊缺影片時頂上來的是「另一邊」那一支。
		out.put("frontIsOwn", !sharedFront.equals(frontPath) && !sharedSide.equals(frontPath));
		out.put("sideIsOwn", !sharedSide.equals(sidePath) && !sharedFront.equals(sidePath));

		if (sharedFront.equals(frontPath) && !puttFrontPath.isEmpty()) {
			out.put("frontPath", puttFrontPath);
			out.put("frontDemoView", "front");
		} else if (sharedSide.equals(frontPath) && !puttSidePath.isEmpty()) {
			out.put("frontPath", puttSidePath);
			out.put("frontDemoView", "side");
		}

		if (sharedSide.equals(sidePath) && !puttSidePath.isEmpty()) {
			out.put("sidePath", puttSidePath);
			out.put("sideDemoView", "side");
		} else if (sharedFront.equals(sidePath) && !puttFrontPath.isEmpty()) {
			out.put("sidePath", puttFrontPath);
			out.put("sideDemoView", "front");
		}
		return out;
	}

	/** context.xml 的值，沒設或空字串就用 fallback。 */
	private static String value(String v, String fallback) {
		return (v == null || v.isEmpty()) ? fallback : v;
	}

	/** 資料夾參數一律補成以斜線結尾（ShotVideo 也是這樣接的）。 */
	private static String folder(String v, String fallback) {
		String s = value(v, fallback);
		return s.endsWith("/") ? s : s + "/";
	}

	/**
	 * 示範影片那一支的界標，組成跟 shot_video_swing.PuttingPhases 同一個形狀。
	 *
	 * ⭐ 形狀一樣，頁面那一側就走**完全同一條推導路徑**（derivePuttPhases），
	 *    ⛔ 不必為示範影片另外寫一套。
	 *
	 * @param camPos "front" 或 "side"
	 */
	public static String demoPhases(String camPos) {
		boolean front = !"side".equalsIgnoreCase(camPos);
		int[] data = front ? DEMO_FRONT_DATA : DEMO_SIDE_DATA;
		int onset = front ? DEMO_FRONT_ONSET : DEMO_SIDE_ONSET;

		JSONObject phases = new JSONObject();
		phases.put("data", new JSONArray(data));
		phases.put("status", "OK");
		phases.put("reason", "");
		phases.put("onset", onset);
		phases.put("fps", DEMO_FPS);
		phases.put("total_duration_sec", sec(data[3] - data[0]));

		// ⚠️ 人工標註 → 四顆都定位得到。⛔ 演算法輸出不可以這樣填（見欄位上方的說明）。
		JSONObject found = new JSONObject();
		found.put("address", true);
		found.put("top", true);
		found.put("impact", true);
		found.put("finish", true);
		phases.put("found", found);

		JSONObject seconds = new JSONObject();
		seconds.put("address", sec(data[0]));
		seconds.put("top", sec(data[1]));
		seconds.put("impact", sec(data[2]));
		seconds.put("finish", sec(data[3]));
		seconds.put("onset", sec(onset));
		phases.put("seconds", seconds);

		return phases.toString();
	}

	/**
	 * 幀號 → 秒，取到小數第三位。
	 * ⚠️ 1 毫秒遠小於一幀（60fps 是 16.7 毫秒），⛔ 跳到哪一幀不受影響。
	 * ⛔ 幀號本身與 fps ⛔ 不可以 round。
	 */
	private static double sec(int frame) {
		return Math.round(frame / DEMO_FPS * 1000.0) / 1000.0;
	}

}
