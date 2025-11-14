package com.golfmaster.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.golfmaster.common.Logs;


/**
 * 呼叫大語言模型 (LLM) 以生成高爾夫綜合建議的 API 客戶端。
 * <p>
 * 此類別負責組合來自不同來源的擊球數據、動作分析數據和規則建議，
 * 並將其發送到指定的 LLM API 端點。它處理 API 的請求、響應和錯誤，
 * 為使用者提供一個統一的建議生成介面。
 * </p>
 */
public class CallLlmAdviseAPI {
    private static final String LLM_ADVISE_API_URL =
        "http://127.0.0.1/VLM/service/llm_golf_advice";

    // API 請求參數
    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_ANAL_DATA = "anal_data";

    // API 連線設定
    private static final int CONNECT_TIMEOUT_MS = 5000; // 5 s
    private static final int LOAD_TIMEOUT_MS = 70000; // 70 s

    // 球桿類型定義
    private static final List<String> SHORT_GAME_CLUB_TYPES =
        new ArrayList<>(Arrays.asList("SandWedge", "GapWedge"));
    private static final String PUTTING_CLUB_TYPE = "Putting";

    /**
     * 獲取 LLM 的綜合建議。
     * <p>
     * 此方法組合所有輸入數據，呼叫外部 LLM API，並返回其生成的建議。
     * 它處理了數據準備、API 連接、請求發送、響應讀取和錯誤處理的完整流程。
     *
     * @param clientId      客戶端唯一識別碼。
     * @param currShotData  當前擊球數據的 JSON 字串。
     * @param analShotData  歷史分析數據的 JSON 字串 (例如短桿分析)。
     * @param motionPayload 動作分析數據的 JSON 字串 (例如 TPI 建議)。
     * @param ruleAdvice    規則型專家建議的 JSON 字串。
     * @return 一個 JSON 字串，其中包含 LLM 的建議或錯誤訊息。
     */
    public String getLlmAdvise(
        String clientId,
        String currShotData,
        String analShotData,
        String motionPayload,
        String ruleAdvice
    ) {
        String inputJsonString;

        // ===================================
        // A. 數據準備階段
        // ===================================
        try {
            inputJsonString = genApiInput(
                currShotData, analShotData, motionPayload, ruleAdvice);

            if (inputJsonString == null || inputJsonString.isEmpty()) {
                String logMsg = "API input data generation failed.";
                String userMsg = "資料準備階段失敗: 輸入資料為空。";
                return createErrorResponse(logMsg, userMsg, null);
            }
        } catch (Exception e) {
            String logMsg = "getLlmAdvise Exception in genApiInput: " + e.getMessage();
            String userMsg = "資料準備階段失敗: " + e.getClass().getSimpleName();
            return createErrorResponse(logMsg, userMsg, e);
        }

        // ===================================
        // B. API 呼叫階段
        // ===================================
        HttpURLConnection conn = null;
        try {
            URL url = new URL(LLM_ADVISE_API_URL);
            conn = (HttpURLConnection) url.openConnection();
            setupHttpConnection(conn);

            sendPostRequest(conn, clientId, inputJsonString);

            return handleHttpResponse(conn);
        } catch (SocketTimeoutException e) {
            String logMsg = "API Request Timeout: " + e.getMessage();
            String userMsg = "API 請求超時，請稍後重試。";
            return createErrorResponse(logMsg, userMsg, e);
        } catch (Exception e) {
            String logMsg = "API Request General Exception: " + e.getMessage();
            String userMsg = "API 請求發生未知錯誤: " + e.getClass().getSimpleName();
            return createErrorResponse(logMsg, userMsg, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 組合所有輸入數據，生成用於呼叫 LLM API 的 JSON 字串。
     *
     * @param currShotData  當前擊球數據。
     * @param analShotData  歷史分析數據。
     * @param motionPayload 動作分析數據。
     * @param ruleAdvice    規則型專家建議。
     * @return 組合後的 JSON 字串。
     */
    private String genApiInput(
        String currShotData,
        String analShotData,
        String motionPayload,
        String ruleAdvice
    ) {
        String adviceType = getAdviceType(currShotData);
        String cleanAnalShotData = getCleanAnalShotData(adviceType, analShotData);
        String cleanRuleAdvice = getCleanRuleAdvice(adviceType, ruleAdvice);

        JSONObject currShotDataJson = new JSONObject(currShotData);
        JSONObject motionPayloadJson = new JSONObject(motionPayload);

        System.out.println("adviceType: " + adviceType);
        System.out.println("currShotDataJson: " + currShotDataJson);
        System.out.println("analShotDataJson: " + cleanAnalShotData);
        System.out.println("motionPayloadJson: " + motionPayloadJson);
        System.out.println("ruleAdviceJson: " + cleanRuleAdvice);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("adviceType", adviceType);
        jsonObject.put("currShotData", currShotDataJson.toString());
        jsonObject.put("analShotData", cleanAnalShotData);
        jsonObject.put("motion", motionPayloadJson.toString());
        jsonObject.put("ruleAdvice", cleanRuleAdvice);

        return jsonObject.toString();
    }

    /**
     * 根據當前擊球數據中的球桿類型，判斷建議的類型。
     *
     * @param currShotData 當前擊球數據的 JSON 字串。
     * @return "ShortGame", "Putting", 或 "LongGame"。
     */
    private String getAdviceType(String currShotData) {
        try {
            JSONObject currShotDataJson = new JSONObject(currShotData);
            String clubType = currShotDataJson.getString("ClubType");

            if (SHORT_GAME_CLUB_TYPES.contains(clubType)) {
                return "ShortGame";
            } else if (PUTTING_CLUB_TYPE.equals(clubType)) {
                return "Putting";
            } else {
                return "LongGame";
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logs.log(Logs.EXCEPTION_LOG, "getAdviceType Exception: " + e.getMessage());
            return "LongGame"; // 預設為長桿
        }
    }

    /**
     * 清理並提取擊球分析數據中的必要欄位。
     * <p>
     *
     * @param adviceType   建議類型 ("ShortGame", "Putting", "LongGame")。
     * @param analShotData 原始的短桿分析數據 JSON 字串。
     * @return 清理後的 JSON 字串，或空字串。
     */
    private String getCleanAnalShotData(String adviceType, String analShotData) {
        if (analShotData == null || analShotData.isEmpty()) {
            return "";
        }

        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject analShotDataJson = new JSONObject(analShotData);

            if ("Putting".equals(adviceType)) {
                return "";
            }

            jsonObject.put("club_type", analShotDataJson.getString("club_type"));
            jsonObject.put("total_shots", analShotDataJson.getInt("total_shots"));
            jsonObject.put("analyzed_shots", analShotDataJson.getInt("analyzed_shots"));
            jsonObject.put("avg_carry_dist_yd", analShotDataJson.getDouble("avg_carry_dist_yd"));
            jsonObject.put("avg_horizontal_deviation_yd", analShotDataJson.getDouble("avg_horizontal_deviation_yd"));
            jsonObject.put("avg_launch_direction_deg", analShotDataJson.getDouble("avg_launch_direction_deg"));
            jsonObject.put("stdev_carry_yd", analShotDataJson.getDouble("stdev_carry_yd"));
            jsonObject.put("stdev_horizontal_yd", analShotDataJson.getDouble("stdev_horizontal_yd"));
            jsonObject.put("avg_carry_ratio", analShotDataJson.getDouble("avg_carry_ratio"));
            jsonObject.put("avg_roll_ratio", analShotDataJson.getDouble("avg_roll_ratio"));
            jsonObject.put("max_deviation_yd", analShotDataJson.getDouble("max_deviation_yd"));
            jsonObject.put("covariance_xy", analShotDataJson.getDouble("covariance_xy"));
            // jsonObject.put("landing_consistency_percent", analShotDataJson.getDouble("landing_consistency_percent"));

            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            String logMsg = "getCleanAnalShotData Exception: " + e.getMessage();
            Logs.log(Logs.EXCEPTION_LOG, logMsg);
            return "";
        }
    }

    /**
     * 清理並提取規則型專家建議中的必要欄位。
     * <p>
     * FUTURE: 資料庫將新增不同類型的回饋規則，資料庫名稱不同但都會對應欄位(expert_cause, expert_suggestion)
     * 需要修改 ExpertData
     *
     * @param adviceType 建議類型 ("ShortGame", "Putting", "LongGame")。
     * @param ruleAdvice 原始的規則建議 JSON 字串。
     * @return 清理後的 JSON 字串，或空字串。
     */
    private String getCleanRuleAdvice(String adviceType, String ruleAdvice) {
        if (ruleAdvice == null ||
            ruleAdvice.isEmpty()) {
            return "";
        }

        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject ruleAdviceJson = new JSONObject(ruleAdvice);

            if ("LongGame".equals(adviceType)) {
                jsonObject.put("expert_trajectory", ruleAdviceJson.getString("expert_trajectory"));
            }

            jsonObject.put("expert_cause", ruleAdviceJson.getString("expert_cause"));
            jsonObject.put("expert_suggestion", ruleAdviceJson.getString("expert_suggestion"));
            // jsonObject.put("expert_p_system", ruleAdviceJson.getString("expert_p_system");

            return jsonObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
            String logMsg = "getCleanRuleAdvice Exception: " + e.getMessage();
            Logs.log(Logs.EXCEPTION_LOG, logMsg);
            return "";
        }
    }

    // ===================================
    // 輔助函式
    // ===================================

    /**
     * 統一處理錯誤日誌記錄和錯誤 JSON 響應的建構。
     *
     * @param logMessage  要記錄到日誌的詳細錯誤訊息。
     * @param userMessage 要返回給前端的簡化錯誤訊息。
     * @param e           可選的異常對象，用於打印堆疊追蹤。
     * @return 一個包含錯誤訊息的 JSON 字串。
     */
    private String createErrorResponse(String logMessage, String userMessage, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        Logs.log(Logs.EXCEPTION_LOG, logMessage);

        JSONObject errJson = new JSONObject();
        errJson.put("success", false);
        errJson.put("result", userMessage);
        return errJson.toString();
    }

    /**
     * 統一處理成功的 JSON 響應建構。
     * @param rawResponse 從 API 獲取的原始響應字串。
     * @return 一個包含成功狀態和結果的 JSON 字串。
     */
    private String createSuccessResponse(String rawResponse) {
        JSONObject finalResJson = new JSONObject();
        finalResJson.put("success", true);
        finalResJson.put("result", rawResponse);
        return finalResJson.toString();
    }

    /**
     * 設定 HTTP 連線的共通屬性 (POST, Timeout 等)。
     * @param conn HttpURLConnection 物件。
     * @throws java.net.ProtocolException 如果請求方法無法設定。
     */
    private void setupHttpConnection(HttpURLConnection conn)
        throws java.net.ProtocolException {
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",
            "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(LOAD_TIMEOUT_MS);
    }

    /**
     * 發送 POST 請求到指定的連線。
     * @param conn            HttpURLConnection 物件。
     * @param clientId        客戶端 ID。
     * @param inputJsonString 要發送的數據 JSON 字串。
     * @throws java.io.IOException 如果發生 I/O 錯誤。
     */
    private void sendPostRequest(HttpURLConnection conn, String clientId,
        String inputJsonString) throws java.io.IOException {
        String encodedClientId = URLEncoder.encode(
            clientId, StandardCharsets.UTF_8.name());
        String encodedAnalData = URLEncoder.encode(
            inputJsonString, StandardCharsets.UTF_8.name());

        String postData = PARAM_CLIENT_ID + "=" + encodedClientId + "&" +
                          PARAM_ANAL_DATA + "=" + encodedAnalData;
        byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

        conn.setRequestProperty(
            "Content-Length", String.valueOf(postDataBytes.length));

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postDataBytes);
            wr.flush();
        }
    }

    /**
     * 處理 HTTP 響應，根據狀態碼返回成功或失敗的 JSON 字串。
     * @param conn HttpURLConnection 物件。
     * @return 包含 API 結果的 JSON 字串。
     * @throws java.io.IOException 如果讀取響應時發生 I/O 錯誤。
     */
    private String handleHttpResponse(HttpURLConnection conn)
        throws java.io.IOException {
        int responseCode = conn.getResponseCode();
        System.out.println("Response Code : " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            String rawResponse = readStream(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            System.out.println("rawResponse: " + rawResponse);
            return createSuccessResponse(rawResponse);
        } else {
            String errorContent = readStream(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            String logMsg = "API 呼叫失敗，HTTP 錯誤碼: " + responseCode +
                            ", 錯誤內容: " + errorContent;
            String userMsg = "API 請求失敗: " + responseCode;
            return createErrorResponse(logMsg, userMsg, null);
        }
    }

    /**
     * 從指定的 Reader 中讀取所有內容並返回一個字串。
     * @param reader InputStreamReader 物件。
     * @return 讀取到的完整字串。
     */
    private String readStream(InputStreamReader reader) throws java.io.IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(reader)) {
            String errorLine;
            while ((errorLine = in.readLine()) != null) {
                response.append(errorLine);
            }
        }
        return response.toString();
    }
}
