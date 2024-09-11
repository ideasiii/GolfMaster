package com.golfmaster.tool;

import okhttp3.*;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class FacialSpeakTest {
	private final OkHttpClient client = new OkHttpClient();
//    private final String apiKey = "D-ID_Key";
    private final String apiKey ="Hey_Gen_Key";
//	  D-ID版本
//    public String generateVideoUrl(String expertTrajectory, String expertPSystem, String expertSuggestion, String expertCause) throws IOException {
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("application/json");
//        String inputText = "彈道: " + expertTrajectory + ", 系統: " + expertPSystem + ", 建議: " + expertSuggestion + ", 原因: " + expertCause;
//        RequestBody body = RequestBody.create(mediaType, "{\"script\":{\"type\":\"text\",\"input\":\""+ inputText +"\",\"provider\":{\"type\":\"microsoft\",\"voice_id\":\"zh-TW-YunJheNeural\"}},\"source_url\":\"https://images.pexels.com/photos/262391/pexels-photo-262391.jpeg\"}");
//        System.out.println("Request Body: " + body);
//        Request postRequest = new Request.Builder()
//            .url("https://api.d-id.com/talks")
//            .post(body)
//            .addHeader("accept", "application/json")
//            .addHeader("content-type", "application/json")
//            .addHeader("Authorization", "Basic amFuZXMyMjc1MzExNEBnbWFpbC5jb20:0Krqr7BK2hCPJe48lGSlE")
//            .build();
//
//        String resultUrl = null;
//        String talkId = null;  // 在這裡宣告 talkId 變數
//
//        try (Response postResponse = client.newCall(postRequest).execute()) {
//            System.out.println("POST Response Code: " + postResponse.code());
//            String responseBody = postResponse.body().string();
//            System.out.println("POST Response Body: " + responseBody);
//
//            if (!postResponse.isSuccessful()) {
//                throw new IOException("Unexpected code " + postResponse);
//            }
//
//            talkId = extractTalkId(responseBody);  // 提取 talkId
//            System.out.println("Extracted Talk ID: " + talkId);
//
//            if (talkId != null) {
//                resultUrl = getResultUrl(client, talkId);
//            }
//        }
//
//        System.out.println("Final Result URL: " + resultUrl);
//        return resultUrl;
//    }
	//HeyGen版本
	public String generateVideoUrl(String expertTrajectory, String expertPSystem, String expertSuggestion, String expertCause) throws IOException, InterruptedException {
		String videoId = postVideoRequest(expertTrajectory, expertPSystem, expertSuggestion, expertCause);
        if (videoId != null) {
            return getVideoUrl(videoId);
        }
        throw new IOException("Failed to obtain video ID from initial request.");
    }
	
	private String postVideoRequest(String trajectory, String pSystem, String suggestion, String cause) throws IOException {
	    String json = buildJsonRequestBody(trajectory, pSystem, suggestion, cause);
	    Request request = new Request.Builder()
	            .url("https://api.heygen.com/v2/video/generate")
	            .post(RequestBody.create(MediaType.get("application/json; charset=utf-8"), json))
	            .addHeader("X-Api-Key", apiKey)
	            .build();

	    try (Response response = client.newCall(request).execute()) {
	        String responseBody = response.body().string();  // 儲存 Response Body
	        System.out.println("Response from video generate request: " + responseBody);
	        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
	        JSONObject jsonObject = new JSONObject(responseBody);  // 使用儲存的 Response Body
	        System.out.println("post request video_id:" + jsonObject.getJSONObject("data").getString("video_id"));
	        return jsonObject.getJSONObject("data").getString("video_id");
	    } catch (JSONException e) {
	        System.err.println("JSON parsing error: " + e.getMessage());
	        e.printStackTrace();
	        throw new IOException("Failed to parse JSON response.");
	    } catch (Exception e) {
	        System.err.println("Unexpected error: " + e.getMessage());
	        e.printStackTrace();
	        throw new IOException("An unexpected error occurred.");
	    }
	}

	//D-ID版本
//    private String extractTalkId(String responseBody) {
//    	String talkId = null;
//        try {
//            JSONObject jsonResponse = new JSONObject(responseBody);
//            if (jsonResponse.has("id")) {
//                talkId = jsonResponse.getString("id");
//                System.out.println("Talk ID extracted: " + talkId);
//            }
//        } catch (Exception e) {
//            System.out.println("Failed to extract Talk ID: " + e.getMessage());
//        }
//        return talkId;
//    }

    private String getResultUrl(OkHttpClient client, String talkId) throws IOException {
    	String resultUrl = null;
        String status = null;

        do {
            Request getRequest = new Request.Builder()
                .url("https://api.d-id.com/talks/" + talkId)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Basic API_KEY")
                .build();

            try (Response response = client.newCall(getRequest).execute()) {
                System.out.println("GET Response Code: " + response.code());
                String responseBody = response.body().string();
                System.out.println("GET Response Body: " + responseBody);
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                JSONObject jsonResponse = new JSONObject(responseBody);
                status = jsonResponse.getString("status");
                System.out.println("Status: " + status);

                if (status.equals("done")) {
                    resultUrl = jsonResponse.getString("result_url");
                    System.out.println("Result URL extracted: " + resultUrl);
                } else {
                    // 暫停一段時間再重試
                    Thread.sleep(5000); // 5秒
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (status != null && !status.equals("done"));

        return resultUrl;
    }
    
    private String getVideoUrl(String videoId) throws IOException {
        String resultUrl = null;
        String status = null;
        int attempt = 0;
        int maxAttempts = 60;  // 設置最大嘗試次數

        do {
            attempt++;
            System.out.println("Attempt #" + attempt + " to fetch video_url for video_id: " + videoId);

            Request getRequest = new Request.Builder()
                .url("https://api.heygen.com/v1/video_status.get?video_id=" + videoId)
                .get()
                .addHeader("X-Api-Key", apiKey)
                .build();

            try (Response response = client.newCall(getRequest).execute()) {
                System.out.println("GET Response Code: " + response.code());

                if (!response.isSuccessful()) {
                    System.out.println("Unexpected response code: " + response.code());
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                System.out.println("GET Response Body: " + responseBody);

                JSONObject jsonResponse = new JSONObject(responseBody);
                status = jsonResponse.getJSONObject("data").getString("status");
                System.out.println("Status: " + status);

                if ("completed".equals(status)) {
                    resultUrl = jsonResponse.getJSONObject("data").getString("video_url");
                    System.out.println("Video URL extracted: " + resultUrl);
                    break;  // 找到video_url後跳出循環
                } else if ("error".equals(status)) {
                    System.out.println("Error in processing: " + jsonResponse.getJSONObject("data").getString("message"));
                    throw new IOException("Video processing failed.");
                } else {
                    System.out.println("Video not ready yet, retrying...");
                    Thread.sleep(10000); // 等待10秒後再次嘗試
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (attempt < maxAttempts && (status == null || !"completed".equals(status)));

        if (resultUrl == null) {
            throw new IOException("Failed to obtain video URL after " + maxAttempts + " attempts.");
        }

        return resultUrl;
    }
    
    private String buildJsonRequestBody(String trajectory, String pSystem, String suggestion, String cause) {
    	return "{"
    	        + "\"test\": false,"  // 根據需要設定是否為測試模式
    	        + "\"caption\": false,"  // 根據需要添加字幕
    	        + "\"dimension\": {\"width\": 1920, \"height\": 1080},"  // 指定視頻分辨率
    	        + "\"video_inputs\": [{"
    	        + "    \"character\": {"
    	        + "        \"type\": \"avatar\","
    	        + "        \"avatar_id\": \"Kristin_public_3_20240108\","
    	        + "        \"avatar_style\": \"normal\""
    	        + "    },"
    	        + "    \"voice\": {"
    	        + "        \"type\": \"text\","
    	        + "        \"input_text\": \"Details: 彈道: " + trajectory + ", P-System: " + pSystem + ", 建議: " + suggestion + ", 原因: " + cause + "\","
    	        + "        \"voice_id\": \"de6ad44022104ac0872392d1139e9364\""
    	        + "    }"
    	        + "}]"
    	        + "}";
    }
}
