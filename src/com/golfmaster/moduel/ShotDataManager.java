package com.golfmaster.moduel;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import javax.servlet.http.Part;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.moduel.PSystem;
import com.golfmaster.moduel.PSystemJP;


public class ShotDataManager {

    // 定義基礎儲存路徑
    private static final String BASE_SAVE_PATH = "/opt/golfmaster/video/";
    // 定義基礎訪問 URL (請根據實際伺服器配置修改)
    private static final String BASE_URL = "http://127.0.0.1/downloads/video/";

    /**
     * 核心處理入口：由 Service 呼叫並傳入校正後的 meta
     */
    public JSONObject processIntegratedUpload(
        ShotMetadata meta, String shotDataJson, Part fPart, Part sPart, Part hPart, UploadMode mode
    ) {
        JSONObject response = new JSONObject();
        try {
            switch (mode) {
                case FULL_UPLOAD:
                    response = handleFullUpload(meta, shotDataJson, fPart, sPart, hPart);
                    break;
                case DATA_ONLY:
                    response = handleDataOnly(meta, shotDataJson);
                    break;
                case VIDEO_ONLY:
                    response = handleVideoOnly(meta, fPart, sPart, hPart);
                    break;
                default:
                    response = ApiResponse.error(ApiResponse.STATUS_INVALID_PARAMETER, "未知模式");
            }
        } catch (Exception e) {
            Logs.log(Logs.EXCEPTION_LOG, "ShotDataManager 處理崩潰: " + e.toString());
            response = ApiResponse.error(ApiResponse.STATUS_INTERNAL_ERROR, e.getMessage());
        }
        return response;
    }

    // --- [模式 A]：數據 + 影像 ---
    private JSONObject handleFullUpload(ShotMetadata meta, String jsonData, Part p1, Part p2, Part p3) throws Exception {
        Logs.log(Logs.RUN_LOG, "[DB Mode] Full Upload Processing - User: " + meta.player);

        // 解析與分析
        BasicInfo info = parseShotData(jsonData);
        String pSystemName = (meta.player.toLowerCase().endsWith(".jp")) ? "PSystemJP" : "PSystem";
        String analysisResult = runAnalysis(info, meta.player);

        // 寫入 shot_data
        long shotDataId = saveShotData(info, meta);

        // 寫入 expert
        long expertId = 0;
        if (shotDataId > 0 && analysisResult != null) {
            expertId = saveExpertData(shotDataId, analysisResult, pSystemName);
        }

        // 儲存影片檔案
        Map<String, String> urls = saveAllFiles(meta, p1, p2, p3);

        // 寫入 shot_video
        long videoId = insertShotVideo(meta, shotDataId, urls);

        // 雙向更新 (shot_data -> shot_video_id)
        if (shotDataId > 0 && videoId > 0) {
            updateShotDataVideoLink(shotDataId, videoId);
        }

        // 構建回應 (暫時先回傳基本成功訊息與 ID)
        JSONObject res = ApiResponse.successTemplate();
        res.put("shot_data_id", shotDataId);
        res.put("expert_id", expertId);
        res.put("video_id", videoId);
        res.put("player", meta.player);
        return res;
    }

    // --- [模式 B]：僅數據 ---
    private JSONObject handleDataOnly(ShotMetadata meta, String jsonData) throws Exception {
        Logs.log(Logs.RUN_LOG, "[TEST] 執行 純數據 模式 - 玩家: " + meta.player);
        BasicInfo info = parseShotData(jsonData);

        /* [SQL 暫不執行]
        long shotDataId = saveShotData(info, meta);
        */

        JSONObject res = ApiResponse.successTemplate();
        res.put("mode", "DATA_ONLY");
        res.put("player", meta.player);
        return res;
    }

    // --- [模式 C]：僅影像 ---
    private JSONObject handleVideoOnly(ShotMetadata meta, Part p1, Part p2, Part p3) throws Exception {
        Logs.log(Logs.RUN_LOG, "[TEST] 執行 純影像 模式 - 玩家: " + meta.player);

        // 使用 meta 確保影像檔名正確
        Map<String, String> urls = saveAllFiles(meta, p1, p2, p3);

        /* [SQL 暫不執行]
        long videoId = insertShotVideo(meta, 0, urls);
        */

        JSONObject res = ApiResponse.successTemplate();
        res.put("mode", "VIDEO_ONLY");
        res.put("saved_files", urls);
        return res;
    }


    // --- 內部私有輔助方法：檔案處理 ---

    private Map<String, String> saveAllFiles(ShotMetadata meta, Part f, Part s, Part h) throws Exception {
        Map<String, String> urls = new HashMap<>();
        urls.put("raw_shotVideo_front", saveFile(f, "shotVideo_front", meta));
        urls.put("raw_shotVideo_side", saveFile(s, "shotVideo_side", meta));
        urls.put("raw_headVideo", saveFile(h, "headVideo", meta));
        return urls;
    }

    private String saveFile(Part part, String folder, ShotMetadata meta) throws Exception {
        if (part == null || part.getSize() == 0) return "";

        /* 取得副檔名 (若無則預設 .mp4) */
        String originalName = part.getSubmittedFileName();
        String extension = ""; // ".mp4"
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot > 0) extension = originalName.substring(lastDot);

        /* 核心命名規則：Player_Folder_UnixTimestamp.ext */
        String fileName = meta.player + "_" + folder + "_" + meta.unixDateStr + extension;
        String fullPath = BASE_SAVE_PATH + folder + "/" + fileName;

        /* 目錄建立與權限防呆 */
        File dir = new File(BASE_SAVE_PATH + folder);
        if (!dir.exists()) dir.mkdirs();

        /* 執行寫入 */
        part.write(fullPath);

        /* 權限設定：允許所有人讀取 (解決 root 執行 Tomcat 導致其他帳號無法讀取的問題) */
        File savedFile = new File(fullPath);
        if (savedFile.exists()) {
            savedFile.setReadable(true, false);
        }

        return BASE_URL + folder + "/" + fileName;
    }

    // --- 內部私有輔助方法：數據解析 ---
    private BasicInfo parseShotData(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) return new BasicInfo();

        JSONObject root = new JSONObject(json);
        JSONObject obj = root.has("shotData") ? root.getJSONObject("shotData") : root;

        BasicInfo info = new BasicInfo();
        info.idx = obj.optString("idx");
        info.LID = obj.optString("LID");
        info.Player = obj.optString("Player");
        info.Date = obj.optString("Date");

        if (obj.has("ShotData")) {
            JSONObject sdJson = obj.getJSONObject("ShotData");
            info.shotData = new ShotData();
            for (Field f : info.shotData.getClass().getDeclaredFields()) {
                String fieldName = f.getName();
                if (sdJson.has(fieldName)) {
                    if (f.getType() == float.class) {
                        f.set(info.shotData, (float) sdJson.optDouble(fieldName, 0.0));
                    } else {
                        f.set(info.shotData, sdJson.optString(fieldName));
                    }
                }
            }
        }
        return info;
    }

    private String runAnalysis(BasicInfo info, String player) {
        return "{}";

        // String result;

        // if(0 < info.Player.lastIndexOf(".jp")) {
        //     result = new PSystemJP().expertAnalysis(
        //         info.shotData.BallSpeed,
        //         info.shotData.ClubAnglePath,
        //         info.shotData.ClubAngleFace,
        //         info.shotData.TotalDistFt,
        //         info.shotData.CarryDistFt,
        //         info.shotData.LaunchAngle,
        //         info.shotData.SmashFactor,
        //         info.shotData.BackSpin,
        //         info.shotData.SideSpin,
        //         info.shotData.ClubHeadSpeed,
        //         info.shotData.LaunchDirection,
        //         info.shotData.DistToPinFt);
        // } else {
        //     result = new PSystem().expertAnalysis(
        //         info.shotData.BallSpeed,
        //         info.shotData.ClubAnglePath,
        //         info.shotData.ClubAngleFace,
        //         info.shotData.TotalDistFt,
        //         info.shotData.CarryDistFt,
        //         info.shotData.LaunchAngle,
        //         info.shotData.SmashFactor,
        //         info.shotData.BackSpin,
        //         info.shotData.SideSpin,
        //         info.shotData.ClubHeadSpeed,
        //         info.shotData.LaunchDirection,
        //         info.shotData.DistToPinFt);
        // }

        // return result;
    }

    // --- 輔助計算方法 ---
    private double calculateBallScore(float distance, float direction) {
        float d = Math.min(distance, 176f);
        float dir = Math.min(Math.abs(direction), 16f);
        double score = ((d / 176.0 + ((16.0 - dir) / 16.0) * 0.5) / 1.5) * 100.0;
        return Math.ceil(Math.min(score, 100.0));
    }


    // --- 資料庫操作方法 ---

    /**
     * 插入影片紀錄 (shot_video)
     */
    private long insertShotVideo(ShotMetadata meta, long shotDataId, Map<String, String> urls) throws Exception {
        long id = 0;
        long unixTime = Long.parseLong(meta.unixDateStr);
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(unixTime));

        String sql = "INSERT INTO shot_video (idx, LID, Player, Date, Date_str, shot_data_id, " +
                     "raw_shotVideo_front, raw_shotVideo_side, raw_headVideo) VALUES (?,?,?,?,?,?,?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnGolfMaster();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, meta.idx);
            ps.setString(2, meta.lid);
            ps.setString(3, meta.player);
            ps.setString(4, formattedDate);
            ps.setLong(5, unixTime);
            ps.setObject(6, shotDataId > 0 ? shotDataId : null);
            ps.setString(7, urls.get("raw_shotVideo_front"));
            ps.setString(8, urls.get("raw_shotVideo_side"));
            ps.setString(9, urls.get("raw_headVideo"));

            if (ps.executeUpdate() == 1) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) id = rs.getLong(1);
            }
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return id;
    }

    private void updateShotDataVideoLink(long dataId, long videoId) throws Exception {
        String sql = "UPDATE shot_data SET shot_video_id = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnGolfMaster();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, videoId);
            ps.setLong(2, dataId);
            ps.executeUpdate();
        } finally {
            DBUtil.close(null, ps, conn);
        }
    }

    /**
     * 儲存擊球物理數據 (shot_data)
     */
    private long saveShotData(BasicInfo info, ShotMetadata meta) throws Exception {
        long id = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // 1. 準備寫入 Map (Key 為資料庫欄位名)
            Map<String, Object> datas = new HashMap<String, Object>();

            // 2. 填入身分資訊 (來自校正後的 meta)
            datas.put("idx", meta.idx);
            datas.put("LID", meta.lid);
            datas.put("Player", meta.player);
            datas.put("Date_str", meta.unixDateStr); // 存入原始毫秒字串

            // 3. 處理時間轉換 (毫秒字串 -> Timestamp)
            long unixTime = Long.parseLong(meta.unixDateStr);
            datas.put("Date", new Timestamp(unixTime)); // 存入 yyyy-MM-dd HH:mm:ss 格式

            // 4. 填入物理參數 (來自 info.shotData)
            if (info.shotData != null) {
                Field[] fields = info.shotData.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if ("this$0".equals(f.getName())) continue;
                    datas.put(f.getName(), f.get(info.shotData));
                }
            }

            // 5. 動態建構 SQL
            StringBuilder sql = new StringBuilder("INSERT INTO shot_data (");
            StringBuilder placeholders = new StringBuilder("VALUES (");
            int cnt = 0;
            for (String key : datas.keySet()) {
                sql.append(key).append(cnt < datas.size() - 1 ? ", " : "");
                placeholders.append("?").append(cnt < datas.size() - 1 ? ", " : "");
                cnt++;
            }
            sql.append(") ").append(placeholders).append(");");

            // 6. 執行寫入
            conn = DBUtil.getConnGolfMaster();
            ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
            int paramIdx = 1;
            for (String key : datas.keySet()) {
                ps.setObject(paramIdx++, datas.get(key));
            }

            if (ps.executeUpdate() == 1) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) id = rs.getLong(1);
            }
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return id;
    }

    /**
     * 儲存專家分析建議 (expert)
     */
    private long saveExpertData(long shotDataId, String analysisResult, String pSystemName) throws Exception {
        long id = 0;
        JSONObject analysis = new JSONObject(analysisResult);

        // 建立 Expert 物件並填充資料 (這裡不再使用反射，因為欄位與分析結果 key 通常需要手動對映)
        String sql = "INSERT INTO expert (shot_data_id, expert_trajectory, expert_p_system, expert_suggestion, expert_cause, expert_update_time) " +
                     "VALUES (?, ?, ?, ?, ?, current_timestamp())";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnGolfMaster();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, shotDataId);
            ps.setString(2, analysis.optString("expert_trajectory", "Straight"));
            ps.setString(3, pSystemName); // 紀錄是 PSystem 還是 PSystemJP
            ps.setString(4, analysis.optString("expert_suggestion", ""));
            ps.setString(5, analysis.optString("expert_cause", ""));

            if (ps.executeUpdate() == 1) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) id = rs.getLong(1);
            }
        } finally {
            DBUtil.close(rs, ps, conn);
        }
        return id;
    }

    // --- 內部資料結構 ---
    // 核心身分資料物件，確保命名基準
    public class ShotMetadata {
        public String idx = "";
        public String lid = "";
        public String player = "";
        public String unixDateStr = "";
    }

    public class BasicInfo {
        public String idx;
        public String LID;
        public String Player;
        public String Date;
        public ShotData shotData;
    }

    public class ShotData {
        public float BallSpeed;
        public float ClubAnglePath;
        public float ClubAngleFace;
        public float TotalDistFt;
        public float CarryDistFt;
        public float LaunchAngle;
        public float SmashFactor;
        public float BackSpin;
        public float SideSpin;
        public float ClubHeadSpeed;
        public float LaunchDirection;
        public float DistToPinFt;
        public String ClubType;
    }

    public class Expert {
        public long shot_data_id;
        public String expert_trajectory;
        public String expert_note;
        public String expert_p_system;
        public String expert_suggestion;
        public String expert_cause;
        public String expert_err_msg;
    }
}
