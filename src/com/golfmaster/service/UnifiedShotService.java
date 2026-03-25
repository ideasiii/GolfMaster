package com.golfmaster.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.UploadMode;
import com.golfmaster.moduel.ShotDataManager;

/*
 * 整合型擊球數據與影片上傳 API
 * * 【時間參數說明】：
 * 1. 廠商輸入之 Date：格式為 Unix Timestamp (例如 1769654857000)。
 * - 程式內部對應：unixDateStr
 * - 資料庫最終對應：Date_str (String/Bigint 欄位)
 * 2. 資料庫實際採用之 Date：格式為 yyyy-MM-dd HH:mm:ss (由 unixDateStr 轉換而來)。
 * - 資料庫最終對應：Date (Timestamp 欄位)
 * * 【身分參數說明】：
 * - idx: 模擬器標記之數據 ID (若廠商未提供，則採預設值)。
 * - LID: 模擬器所在之場域 ID (由我方規範，廠商提供)。
 * - Player: 使用者暱稱。
 */
@WebServlet("/service/UnifiedShotService")
@MultipartConfig(
    location = "/opt/golfmaster/video/temp",
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 50,
    maxRequestSize = 1024 * 1024 * 160
)
public class UnifiedShotService extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();
        ShotDataManager manager = new ShotDataManager();

        try {
            /* --- 獲取原始數據 --- */
            String shotDataJson = request.getParameter("shotData");
            Part frontVideo = request.getPart("shotVideo_front");
            Part sideVideo = request.getPart("shotVideo_side");
            Part headVideo = request.getPart("headVideo");

            /* --- 判定模式 --- */
            UploadMode mode = UploadMode.determine(shotDataJson, frontVideo, sideVideo, headVideo);

            /* --- 建立身分基準物件 --- */
            ShotDataManager.ShotMetadata meta = manager.new ShotMetadata();

            /* --- 參數提取 (不論模式，嘗試從所有可能來源抓取必要欄位) --- */
            // 嘗試從 JSON 解析
            String jsonIdx = null, jsonLid = null, jsonPlayer = null, jsonDate = null;
            if (shotDataJson != null && !shotDataJson.trim().isEmpty()) {
                try {
                    JSONObject json = new JSONObject(shotDataJson);
                    JSONObject dataObj = json.has("shotData") ? json.getJSONObject("shotData") : json;
                    jsonIdx = dataObj.optString("idx", null);
                    jsonLid = dataObj.optString("LID", null);
                    jsonPlayer = dataObj.optString("Player", null);
                    jsonDate = dataObj.optString("Date", null);
                } catch (Exception e) {
                    Logs.log(Logs.EXCEPTION_LOG, "JSON 解析失敗，將依賴 Form 欄位: " + e.getMessage());
                }
            }

            // 統一填充：優先級 JSON > Form Parameter
            meta.idx = fetchValue(jsonIdx, request.getParameter("idx"));
            meta.lid = fetchValue(jsonLid, request.getParameter("LID"));
            meta.player = fetchValue(jsonPlayer, request.getParameter("Player"));
            meta.unixDateStr = fetchValue(jsonDate, request.getParameter("Date"));

            /* --- 核心非空驗證 (最重要的關卡) --- */
            // 確保所有關鍵項目「必須有值」，不論值內容是什麼
            if (isAnyEmpty(meta.idx, meta.lid, meta.player, meta.unixDateStr)) {
                jsonResponse = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER, 
                    "缺少必要參數：請確認 idx, LID, Player, Date 均已正確傳送");
                out.print(jsonResponse.toString());
                return;
            }

            /* --- 記錄日誌並進入商務處理 --- */
            Logs.log(Logs.RUN_LOG, String.format("[UnifiedShotService] Mode: %s | User: %s | idx: %s", 
                    mode.name(), meta.player, meta.idx));

            if (mode == UploadMode.INVALID) {
                jsonResponse = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER, "無效的請求：未偵測到擊球數據或影片檔案");
            } else {
                jsonResponse = manager.processIntegratedUpload(meta, shotDataJson, frontVideo, sideVideo, headVideo, mode);
            }

        } catch (Exception e) {
            Logs.log(Logs.EXCEPTION_LOG, "UnifiedShotService 系統異常: " + e.toString());
            jsonResponse = ApiResponse.error(ApiResponse.STATUS_INTERNAL_ERROR, e.getMessage());
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }

    /**
     * 輔助方法：取值順序優化
     */
    private String fetchValue(String primary, String secondary) {
        if (primary != null && !primary.trim().isEmpty()) return primary.trim();
        if (secondary != null && !secondary.trim().isEmpty()) return secondary.trim();
        return ""; // 若都沒抓到，先回傳空字串供後續 isAnyEmpty 檢查
    }

    /**
     * 輔助方法：檢查是否有任何一個欄位為空
     */
    private boolean isAnyEmpty(String... fields) {
        for (String f : fields) {
            if (f == null || f.trim().isEmpty()) return true;
        }
        return false;
    }
}

