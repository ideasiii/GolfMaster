package com.golfmaster.tool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class TestClubStrategy {

	private final static String PART_3 = "3";
	private final static String PART_4 = "4";
	private final static String PART_5 = "5";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double distc = 167;
		String part = "3";
		String player = "Y.H.Meng";
		// 選桿(User本次帶來的桿組){"Driver":true, "1Wood":true, ....}
		String[] clubTypes = { "1Wood", "", "", "7Iron" };

		try {
			// 比對數據集初始化
			List<Double> tmps = new ArrayList<Double>();

			// 根據player取出對應的碼數本
			JSONArray datas = getDatas(player, clubTypes);

			// 球桿建議文字初始化
			StringBuilder result = new StringBuilder();

			// 球桿建議推算
			// 3桿洞
			if (PART_3.equals(part)) {
				// 擊球數據集重新整理
				for (int i = 0; i < datas.length(); i++) {
					tmps.add(datas.getJSONObject(i).getDouble("avg"));
				}

				tmps.add(distc);

				// 比對數據集降冪排序
				Collections.sort(tmps, Collections.reverseOrder());

				// 找出近似距離的數值
				int idx = tmps.indexOf(distc);
				double value = tmps.get((idx + 1));

				// 產出3桿洞的球桿建議
				for (int i = 0; i < datas.length(); i++) {
					JSONObject jobj = datas.getJSONObject(i);
					if (jobj.getDouble("avg") == value) {
						result.append(jobj.getString("ClubType") + "-");
					}
				}
			}
			// 4桿洞
			if (PART_4.equals(part)) {
				// pw跟driver數值初始化
				double pwValue = 0.0;
				double driverValue = 0.0;

				// 擊球數據集重新整理
				for (int i = 0; i < datas.length(); i++) {
					JSONObject jobj = datas.getJSONObject(i);
					if (!jobj.has("1Wood")) {
						tmps.add(jobj.getDouble("avg"));
					}
				}

				// 從碼數碼錶中取出pw跟driver數值
				for (int i = 0; i < datas.length(); i++) {
					JSONObject jobj = datas.getJSONObject(i);
					// driver數值
					if ("1Wood".endsWith(jobj.getString("ClubType"))) {
						driverValue = jobj.getDouble("avg");
					}
					// pw數值
					if ("PitchingWedge".endsWith(jobj.getString("ClubType"))) {
						pwValue = jobj.getDouble("avg");
					}
				}

				// 距離與pw跟driver數值運算
				double resValue = distc - pwValue - driverValue;

				tmps.add(resValue);
				// 比對數據集降冪排序
				Collections.sort(tmps, Collections.reverseOrder());

				// 找出第二桿近似的數值 & 第二桿名稱初始化
				int idx = tmps.indexOf(resValue);
				double value = tmps.get((idx + 1));
				String club = null;

				// 找出第二桿名稱
				ArrayList<String> list = new ArrayList<String>(Arrays.asList(clubTypes));
				for (int i = 0; i < datas.length(); i++) {
					JSONObject jobj = datas.getJSONObject(i);
					if (!jobj.has("1Wood")) {
						if (jobj.getDouble("avg") == value) {
							club = jobj.getString("ClubType");
						}
					}
				}

				// 產出4桿洞的球桿建議
				result.append("Driver-" + (club == null ? "PW" : club) + "-");
			}
			// 5桿洞
			if (PART_5.equals(part)) {
				// pw跟driver數值初始化
				double pwValue = 0.0;
				double driverValue = 0.0;

				// 從碼數碼錶中取出pw跟driver數值
				for (int i = 0; i < datas.length(); i++) {
					JSONObject jobj = datas.getJSONObject(i);
					// driver數值
					if ("1Wood".endsWith(jobj.getString("ClubType"))) {
						driverValue = jobj.getDouble("avg");
					}
					// pw數值
					if ("PitchingWedge".endsWith(jobj.getString("ClubType"))) {
						pwValue = jobj.getDouble("avg");
					}
				}

				// 設置第一桿為Driver，因為五桿洞第一桿一定是Driver
				result.append("Driver-");

				// 距離與pw跟driver數值運算
				double resValue = distc - pwValue - driverValue;

				while ((resValue > 0)) {
					// 整理擊球數據集
					tmps.clear();
					for (int i = 0; i < datas.length(); i++) {
						JSONObject jobj = datas.getJSONObject(i);
						if (!jobj.has("1Wood")) {
							tmps.add(jobj.getDouble("avg"));
						}
					}

					tmps.add(resValue);

					// 比對數據集降冪排序
					Collections.sort(tmps, Collections.reverseOrder());

					// 找出下一桿近似的數值 & 名稱初始化
					int idx = tmps.indexOf(resValue);
					double value = tmps.get((idx + 1));
					String club = null;

					// 找出下一桿名稱
					for (int i = 0; i < datas.length(); i++) {
						JSONObject jobj = datas.getJSONObject(i);
						if (!jobj.has("1Wood")) {
							if (jobj.getDouble("avg") == value) {
								club = jobj.getString("ClubType");
							}
						}
					}

					// 組合五桿洞球桿建議
					result.append((club == null ? "PW" : club) + "-");

					resValue = resValue - value;
				}
			}

			// 最後兩桿是用PW-Putter
			result.append("PW-Putter");

			// 輸出總運算結果
			System.out.println(result.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void clubRecommendV1(double distc) {
		// 比對數據集初始化
		List<Double> tmps = new ArrayList<Double>();

		// 擊球數據集重新整理
		Map<String, String> datas = getDatas();
		for (String key : datas.keySet()) {
			double va = Double.parseDouble(datas.get(key)) / 3.0;

			BigDecimal bigDecimal = new BigDecimal(va);
			va = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

			datas.put(key, String.valueOf(va));

			tmps.add(va);
		}

		// 比對數據集降冪排序
		Collections.sort(tmps, Collections.reverseOrder());

		// 球桿推薦推算
		double max = tmps.get(0);
		double min = 0.00;

//		if(distc == max) {

//		}else {
		if (distc != max) {
			double res = distc - max;
			if (res > 0) {
				int index = 0;

				tmps.add(res);
				// 再次降冪排序
				Collections.sort(tmps, Collections.reverseOrder());
				for (int i = 0; i < tmps.size(); i++) {
					if (tmps.get(i) == res) {
						index = i;
						break;
					}
				}

				min = tmps.get((index + 1));
			} else {
				int index = 0;

				tmps.add(distc);
				tmps.add(Math.abs(res));
				// 再次降冪排序
				Collections.sort(tmps, Collections.reverseOrder());

				System.out.println(tmps.toString());

				for (int i = 0; i < tmps.size(); i++) {
					if (tmps.get(i) == distc) {
						max = tmps.get((i + 1));
						break;
					}
				}

				for (int i = 0; i < tmps.size(); i++) {
					if (tmps.get(i) == Math.abs(res)) {
						index = i;
						break;
					}
				}

				min = tmps.get((index + 1));
			}
		}

		// 取出運算結果
		for (String key : datas.keySet()) {
			if (Double.parseDouble(datas.get(key)) == max) {
				System.out.println(key);
			}
			if (Double.parseDouble(datas.get(key)) == min) {
				System.out.println(key);
			}
		}
	}

	private static Map<String, String> getDatas() {
		Map<String, String> datas = new HashMap<String, String>() {
			{
				put("SandWedge", "49");
				put("SandWedge", "24.8");
				put("7Iron", "13.4");
				put("7Iron", "94");
				put("Putter", "0.3");
				put("1Wood", "444.1");
				put("3Wood", "365.8");
				put("7Iron", "226.2");
				put("SandWedge", "28.3");
				put("Putter", "0.3");
			}
		};

		return datas;
	}

	private static JSONArray getDatas(String player, String[] clubTypes) throws Exception {
		JSONArray array = new JSONArray();

		URL url = new URL("http://61.216.149.161/GolfMaster/service/yardage.jsp?player=" + player);

		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
		httpConn.setUseCaches(false);
		httpConn.setRequestMethod("GET");

		int res = httpConn.getResponseCode();
		StringBuffer sb = new StringBuffer();

		if (res == HttpURLConnection.HTTP_OK) {
			Reader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
			for (int c = in.read(); c != -1; c = in.read()) {
				sb.append((char) c);
			}
		}

		JSONObject result = new JSONObject(sb.toString());
		if (result.getBoolean("success")) {
			JSONArray tmps = result.getJSONArray("result");

			List<String> list = new ArrayList<String>(Arrays.asList(clubTypes));

			for (int i = 0; i < tmps.length(); i++) {
				JSONObject jobj = tmps.getJSONObject(i);
				if (list.contains(jobj.getString("ClubType"))) {
					jobj.put("avg", (jobj.getDouble("avg") > 0 ? (jobj.getDouble("avg") - 10) : 0));
					array.put(jobj);
				}

			}
		}

		return array;
	}

}
