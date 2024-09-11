package com.golfmaster.tool;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class OpenAITTSExample {

    private static final String API_KEY = "OpenAI key";  // 替換為你的OpenAI API金鑰
    private static final String API_URL = "https://api.openai.com/v1/audio/speech"; // 替換為實際的TTS API端點

    public static void main(String[] args) {
        try {
            // 建立URL對象
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // 設置請求屬性
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // 構建請求的JSON主體
            String jsonInputString = "{\"model\": \"tts-1\", \"input\": \"下桿角度過於陡峭，手腕過度彎曲，過度由內而外的路徑\", \"voice\": \"alloy\"}";

            // 發送請求
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 讀取回應
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 讀取音頻數據並播放
                try (InputStream in = new BufferedInputStream(conn.getInputStream())) {
                    Player player = new Player(in);
                    System.out.println("Playing audio...");
                    player.play();  // 播放音頻
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Request failed, HTTP error code: " + responseCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
