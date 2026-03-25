package com.golfmaster.moduel;

import javax.servlet.http.Part;

public enum UploadMode {
    DATA_ONLY,      // 僅數據
    VIDEO_ONLY,     // 僅影像（包含單個或多個影片）
    FULL_UPLOAD,    // 數據 + 影像
    INVALID;        // 無效請求

    /**
     * 根據傳入參數判斷當前模式
     */
    public static UploadMode determine(String shotData, Part p1, Part p2, Part p3) {
        boolean hasData = (shotData != null && !shotData.trim().isEmpty());
        boolean hasVideo = isPartValid(p1) || isPartValid(p2) || isPartValid(p3);

        if (hasData && hasVideo) return FULL_UPLOAD;
        if (hasData) return DATA_ONLY;
        if (hasVideo) return VIDEO_ONLY;
        return INVALID;
    }

    private static boolean isPartValid(Part part) {
        return part != null && part.getSize() > 0;
    }
}
