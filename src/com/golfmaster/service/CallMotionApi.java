package com.golfmaster.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class CallMotionApi {
	private static final String URL_STRING = "http://localhost:8080/GolfMaster/service/shot-data.jsp";

	public void requestApi(JSONObject jsonObj) throws IOException {
		URL url = new URL(URL_STRING);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

		try {
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setDoOutput(true); // 啟用輸出流
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

			// 從 jsonObj 中提取 Player 屬性
			JSONArray results = jsonObj.optJSONArray("result");
			String playerValue = "";
			if (results != null && results.length() > 0) {
				JSONObject firstResult = results.optJSONObject(0);
				if (firstResult != null) {
					playerValue = firstResult.optString("Player", "");
				}
			}

			// 構造表單數據
			String formParams = "player=" + URLEncoder.encode(playerValue, StandardCharsets.UTF_8.name());

			// 發送 POST 數據
			try (OutputStream os = httpURLConnection.getOutputStream()) {
				byte[] input = formParams.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			// 處理響應
			int responseCode = httpURLConnection.getResponseCode();
			StringBuilder response = new StringBuilder();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader br = new BufferedReader(
						new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
					String line;
					while ((line = br.readLine()) != null) {
						response.append(line.trim());
					}
				}
			}

			// 打印響應信息
			System.out.println("響應代碼: " + responseCode);
			System.out.println("響應內容: " + response.toString());

		} catch (IOException e) {
			e.printStackTrace();
			throw e; // 在記錄錯誤後重新拋出異常
		} finally {
			httpURLConnection.disconnect();
		}
	}
}
