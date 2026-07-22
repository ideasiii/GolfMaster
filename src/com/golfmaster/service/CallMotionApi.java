package com.golfmaster.service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import com.golfmaster.common.Logs;
public class CallMotionApi {
	// Fallback (context.xml 讀取失敗時使用)
	private static final String DEFAULT_API_BASE_URL = "http://172.16.78.11:49147";
	private static final String DEFAULT_MOTION_API_PATH = "/GolfVisionAnalytics/service/anl_video";
	// 外部 API 逾時設定（毫秒）。未設定時 HttpURLConnection 預設為 0 = 無限等待，
	// AI server 一旦無回應會讓 Tomcat 執行緒永久卡住。
	private static final int CONNECT_TIMEOUT_MS = 5000;
	private static final int READ_TIMEOUT_MS = 120000;
	/**
	 * 從 context.xml 讀取外部 API 完整 URL，失敗則使用預設值。
	 */
	private static String resolveMotionApiUrl() {
		String base = Config.getParameter("apiBaseUrl");
		if (base == null || base.isEmpty()) {
			base = DEFAULT_API_BASE_URL;
		}
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		String path = Config.getParameter("motionApiPath");
		if (path == null || path.isEmpty()) {
			path = DEFAULT_MOTION_API_PATH;
		}
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		return base + path;
	}
	public void requestApi(JSONObject jsonObj) throws IOException {
		URL url = new URL(resolveMotionApiUrl());
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		try {
			httpURLConnection.setUseCaches(false);
			httpURLConnection.setDoOutput(true); // 啟用輸出流
			httpURLConnection.setRequestMethod("POST");
			httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			// 逾時設定務必在 getOutputStream() 之前，連線建立後再設定無效
			httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
			httpURLConnection.setReadTimeout(READ_TIMEOUT_MS);
			// 準備發送的表單數據
			String formData = "data=" + URLEncoder.encode(jsonObj.toString(), StandardCharsets.UTF_8.name());
			// 發送 POST 數據
			try (OutputStream os = httpURLConnection.getOutputStream()) {
				byte[] input = formData.getBytes(StandardCharsets.UTF_8);
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
			Logs.log(Logs.RUN_LOG, "響應代碼: " + responseCode + "響應內容: " + response.toString());
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			httpURLConnection.disconnect();
		}
	}
}
