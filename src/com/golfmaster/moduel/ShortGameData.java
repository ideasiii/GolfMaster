package com.golfmaster.moduel;

import java.lang.Math;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

/**
 * 短桿數據分析模組。
 * <p>
 * 此類別負責處理短桿擊球數據，包括數據清理、統計分析，
 * 並計算出如平均落點、分佈、標準差、飛行與滾動佔比等關鍵指標。
 * </p>
 */
public class ShortGameData {
    /** 英尺 (ft) 轉換為碼 (yard) 的換算係數 */
    public final double FT_TO_YD = 3.0;
    /** 每 1000 RPM 側旋所造成的側向偏移距離 (英尺) */
    public final double SIDE_SPIN_FT_PER_1000_RPM = 15.0;

    // ----------------------------------------------------
    // 主要分析函式
    // ----------------------------------------------------
    /**
     * 對短桿擊球數據列表進行完整分析。
     *
     * @param rawShotList 原始的擊球數據列表。
     * @param clubType 球桿類型，用於決定分析的容忍度。
     * @return 一個包含所有分析結果的 JSONObject。格式如下：
     *         <pre>
     *         {
     *           "status": "success",                         // 執行狀態 (success, error, warning)
     *           "message": "...",                            // 狀態說明訊息 (當 status 非 success 時出現)
     *           "club_type": "SandWedge",                             // 球桿種類
     *           "total_shots": 10,                           // 原始總球數
     *           "analyzed_shots": 8,                         // 清理後用於分析的球數
     *           "avg_carry_dist_yd": 55.3,                   // 平均飛行距離 (碼)
     *           "avg_horizontal_deviation_yd": -1.5,         // 平均水平落點偏差 (碼, 負值為左)
     *           "avg_launch_direction_deg": -1.2,            // 平均水平發射方向 (度, 負值為左)
     *           "stdev_carry_yd": 2.5,                       // 飛行距離標準差 (碼)
     *           "stdev_horizontal_yd": 3.1,                  // 水平偏差標準差 (碼)
     *           "avg_carry_ratio": 85.0,                     // 平均飛行佔比 (%)
     *           "avg_roll_ratio": 15.0,                      // 平均滾動佔比 (%)
     *           "landing_consistency_percent": 75.0,         // 落點一致率 (%)
     *           "max_deviation_yd": 6.8,                     // 最大偏差值 (碼)
     *           "covariance_xy": ,                           // 座標XY協方差
     *           "landing_points": {                          // 所有擊球的落點座標 (碼)
     *             "x_coords_yd": [ -2.1, 1.5, ... ],         // 水平座標列表 (X軸, 負為左)
     *             "y_coords_yd": [ 54.8, 55.6, ... ]         // 飛行距離座標列表 (Y軸)
     *           }
     *         }
     *         </pre>
     */
    public JSONObject processAnalyz(
        List<ShortGameShotData> rawShotList,
        String clubType
    ) {
        JSONObject jsb = new JSONObject();

        if (rawShotList == null || rawShotList.isEmpty()) {
            jsb.put("status", "error");
            jsb.put("message", "No data to analyze.");
            return jsb;
        }

        // 步驟 1: 數據清理 (刪除極值)
        // System.out.println("rawShotList [0]: " + rawShotList.get(0).getDate());

        List<ShortGameShotData> cleanList = cleanOutliers(rawShotList);

        jsb.put("total_shots", rawShotList.size());
        jsb.put("analyzed_shots", cleanList.size());
        jsb.put("club_type", clubType);

        if (cleanList.size() < 2) {
            jsb.put("status", "warning");
            jsb.put("message", "Insufficient clean data for statistical analysis.");
            return jsb;
        }

        // 步驟 2: 數據轉換 (單次高效能計算所有衍生數據，如落點座標、飛行佔比等)
        List<ProcessedShotData> processedList =
            calculateAllPointsAndRatios(cleanList);

        // 從 processedList 中提取出用於統計的數據列表 (Java Stream 再次迭代，但比原始方案的重複計算效率高)
        List<Double> xCoordsYd = processedList.stream()
            .map(d -> d.x_coord_yd)
            .collect(Collectors.toList());
        List<Double> yCoordsYd = processedList.stream()
            .map(d -> d.y_coord_yd)
            .collect(Collectors.toList());

        // 步驟 3: 核心統計計算 (專注於聚合和輸出)
        // 1A. 平均落點距離 (Average Carry Distance) -> 單位：碼 (Yd)
        double avgCarryDistYd = yCoordsYd.stream().mapToDouble(d -> d).average().orElse(0.0);
        jsb.put("avg_carry_dist_yd", roundDecimal(avgCarryDistYd, 2));

        // 計算平均 X 座標
        double avgXCoordYd = xCoordsYd.stream()
            .mapToDouble(d -> d)
            .average().orElse(0.0);
        jsb.put("avg_horizontal_deviation_yd", roundDecimal(avgXCoordYd, 2));

        // 1B. 平均水平傾向 (Average Launch Direction) -> 單位：角度 (Degree)
        double avgLaunchDirection = processedList.stream()
            .mapToDouble(d -> d.launch_direction_deg)
            .average().orElse(0.0);
        jsb.put("avg_launch_direction_deg", roundDecimal(avgLaunchDirection, 2));

        // 2. 落點分佈 (Landing Points) -> 單位：碼 (Yd)
        //    用於在前端繪製散佈圖。
        //    X 軸代表水平偏差 (左/右)，Y 軸代表飛行距離 (遠/近)。
        JSONObject landingPoints = new JSONObject();
        landingPoints.put("x_coords_yd", xCoordsYd); // 水平座標列表
        landingPoints.put("y_coords_yd", yCoordsYd); // 飛行距離列表
        jsb.put("landing_points", landingPoints);

        // 3. 垂直標準差
        double carryStDevYd = calculateStDevFromList(yCoordsYd);
        jsb.put("stdev_carry_yd", roundDecimal(carryStDevYd, 2));

        // 4. 水平標準差 (基於包含側旋修正的 X 座標)
        double horizontalStDevYd = calculateStDevFromList(xCoordsYd);
        jsb.put("stdev_horizontal_yd", roundDecimal(horizontalStDevYd, 2));

        // 5. 飛行占比
        double avgCarryRatio = processedList.stream()
            .mapToDouble(d -> d.carry_ratio)
            .average().orElse(0.0);
        double avgRollRatio = 100.0 - avgCarryRatio;
        jsb.put("avg_carry_ratio", roundDecimal(avgCarryRatio, 2));
        jsb.put("avg_roll_ratio",  roundDecimal(avgRollRatio, 2));

        // 6. 落點一致率
        double toleranceYd = getToleranceByClubType(clubType);
        double consistency = calculateLandingConsistency(
            xCoordsYd,
            yCoordsYd,
            avgXCoordYd,
            avgCarryDistYd,
            toleranceYd
        );
        jsb.put("landing_consistency_percent", roundDecimal(consistency, 2));

        // 7. 最大偏差值
        double maxDeviation = calculateMaxDeviation(
            xCoordsYd,
            yCoordsYd,
            avgXCoordYd,
            avgCarryDistYd
        );
        jsb.put("max_deviation_yd", roundDecimal(maxDeviation, 2));

        // 計算協方差 (Covariance XY)
        double covarianceXY = calculateCovariance(
            xCoordsYd,
            yCoordsYd,
            avgXCoordYd,
            avgCarryDistYd
        );
        jsb.put("covariance_xy", roundDecimal(covarianceXY, 2));

        jsb.put("status", "success");
        return jsb;
    }

    // ----------------------------------------------------
    // 輔助函式：數據清理
    // ----------------------------------------------------

    /**
     * [步驟 1] 數據清理：移除數據列表中的極端值。
     *
     * @param rawList 原始擊球數據列表。
     * @return 清理極端值後的數據列表。
     */
    private List<ShortGameShotData> cleanOutliers(List<ShortGameShotData> rawList) {
        // return cleanOutliersByPercentile(rawList); // 將主調用指向百分比方法
        return cleanOutliersByIqr(rawList); // 將主調用指向 IQR 方法
    }

    /**
     * [篩選方法一] 使用百分位數法移除極端值。
     *
     * @param rawList 原始擊球數據列表。
     * @return 移除極端值後的數據列表。
     */
    private List<ShortGameShotData> cleanOutliersByPercentile(List<ShortGameShotData> rawList) {
        // 這裡我們進行一個簡單的篩選：移除 CarryDistFt 在最小 5% 和最大 5% 的數據。
        // 對於實際應用，建議使用基於標準差或 IQR (Interquartile Range) 的方法。

        if (rawList.size() < 5) {
            return rawList; // 數據太少，不進行極值處理
        }

        // 取得 CarryDistFt 數據並排序
        List<Double> carries = rawList.stream()
            .map(ShortGameShotData::getCarryDistFt)
            .sorted()
            .collect(Collectors.toList());

        int size = carries.size();
        // 假設我們移除最高和最低 5% 的數據
        int lowerBoundIndex = (int) (size * 0.05);
        int upperBoundIndex = (int) (size * 0.95);

        // 取得篩選範圍
        double minCarry = carries.get(lowerBoundIndex);
        double maxCarry = carries.get(upperBoundIndex);

        // 根據範圍篩選數據
        return rawList.stream()
            .filter(data -> data.getCarryDistFt() >= minCarry &&
                data.getCarryDistFt() <= maxCarry)
            .collect(Collectors.toList());
    }

    /**
     * [篩選方法二] 使用四分位距 (IQR) 法則移除極端值。
     * 極端值定義為 Q1 - 1.5*IQR 以下，或 Q3 + 1.5*IQR 以上的數據。
     *
     * @param rawList 原始擊球數據列表。
     * @return 移除極端值後的數據列表。
     */
    private List<ShortGameShotData> cleanOutliersByIqr(List<ShortGameShotData> rawList) {
        // 建議數據量少於 5 個時不進行 IQR 處理
        if (rawList.size() < 5) {
            return rawList;
        }

        // 1. 取得 CarryDistFt 數據並排序
        List<Double> carries = rawList.stream()
            .map(ShortGameShotData::getCarryDistFt)
            .sorted()
            .collect(Collectors.toList());

        // 2. 計算 Q1 (25th Percentile) 和 Q3 (75th Percentile)
        double q1 = getQuantile(carries, 0.25);
        double q3 = getQuantile(carries, 0.75);

        // 3. 計算 IQR 和極值邊界
        double iqr = q3 - q1;
        // 採用 1.5 倍 IQR 作為閥值
        double lowerBound = q1 - 1.5 * iqr;
        double upperBound = q3 + 1.5 * iqr;

        System.out.println("q1: " + q1);
        System.out.println("q3: " + q3);
        System.out.println("iqr: " + iqr);
        System.out.println("lowerBound: " + lowerBound);
        System.out.println("upperBound: " + upperBound);

        // 依照物理限制
        double adjustedLowerBound = (lowerBound < 0) ? 0.0 : lowerBound;

        // 4. 根據邊界篩選數據
        return rawList.stream()
            .filter(data -> {
                double carry = data.getCarryDistFt();
                return carry >= adjustedLowerBound && carry <= upperBound;
            })
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------
    // 輔助函式：核心函式
    // ----------------------------------------------------

    /**
     * [步驟 2-1] 數據轉換函式 (單次迭代，高效率)。
     * 將原始擊球數據列表轉換為包含所有衍生座標和比率的列表。
     *
     * E6模擬器標準(default)：(+) SideSpin -> Left (負X軸)；(-) SideSpin -> Right (正X軸)
     *   LID = 017, 018, 2000
     * 工研院模擬器相反：(+) SideSpin -> Right (正X軸)；(-) SideSpin -> Left (負X軸)
     *   LID = 1000
     *
     * @param cleanList 清理後的原始數據列表。
     * @return ProcessedShotData 列表。
     */
    private List<ProcessedShotData> calculateAllPointsAndRatios(List<ShortGameShotData> cleanList) {
        return cleanList.stream().map(data -> {
            double carryDistFt = data.getCarryDistFt();
            double totalDistFt = data.getTotalDistFt();

            // A. Y 座標 (Carry 距離)
            double yCoordYd = carryDistFt / FT_TO_YD;

            // B. X 座標 (水平偏差)
            double directionRad = Math.toRadians(data.getLaunchDirection());
            double sideDeviationFt = carryDistFt * Math.tan(directionRad);

            // 側旋修正：根據側旋值計算額外的側向偏移
            double rawSideSpinEffectFt = (data.getSideSpin() / 1000.0) * SIDE_SPIN_FT_PER_1000_RPM;

            // **修正點：反轉側旋修正方向**
            // E6模擬器標準：(+) SideSpin -> Left (負X軸)；(-) SideSpin -> Right (正X軸)
            double sideSpinEffectFt = -rawSideSpinEffectFt;

            // 總側向偏差
            double totalSideDeviationFt = sideDeviationFt + sideSpinEffectFt;
            double xCoordYd = totalSideDeviationFt / FT_TO_YD;

            // C. 飛行占比
            double carryRatio;
            if (totalDistFt > 0) {
                carryRatio = (carryDistFt / totalDistFt) * 100.0;
            } else {
                carryRatio = 100.0;
            }

            return new ProcessedShotData(
                xCoordYd, yCoordYd, carryRatio, data.getLaunchDirection());
        }).collect(Collectors.toList());
    }

    /**
     * 計算落點一致率。
     * 此指標計算有多少比例的球落在以平均落點為中心、指定容忍度為半徑的圓形範圍內。
     *
     * @param xCoords 最終 X 座標列表 (單位: 碼)。
     * @param yCoords 最終 Y 座標列表 (單位: 碼)。
     * @param avgX 平均 X 座標 (單位: 碼)。
     * @param avgY 平均 Y 座標 (單位: 碼)。
     * @param toleranceYd 容忍度半徑 (單位: 碼)。
     * @return 一致率 (百分比 %)。
     */
    private double calculateLandingConsistency(
        List<Double> xCoords,
        List<Double> yCoords,
        double avgX, double avgY,
        double toleranceYd
    ) {
        if (xCoords.isEmpty()) {
            return 0.0;
        }

        long withinRange = 0;
        for (int i = 0; i < xCoords.size(); i++) {
            double dx = xCoords.get(i) - avgX;
            double dy = yCoords.get(i) - avgY;
            // 歐氏距離 (Distance)
            double dist = Math.sqrt(dx * dx + dy * dy); 

            if (dist <= toleranceYd) {
                withinRange++;
            }
        }

        double oriValue = ((double) withinRange / xCoords.size()) * 100.0;

        return Math.round(oriValue * 100.0) / 100.0; // 百分比
    }

    /**
     * 計算最大偏差值。
     * 此指標計算所有落點中，距離平均落點最遠的距離。
     *
     * @param xCoords 最終 X 座標列表 (單位: 碼)。
     * @param yCoords 最終 Y 座標列表 (單位: 碼)。
     * @param avgX 平均 X 座標 (單位: 碼)。
     * @param avgY 平均 Y 座標 (單位: 碼)。
     * @return 最大偏差距離 (單位: 碼)。
     */
    private double calculateMaxDeviation(
        List<Double> xCoords,
        List<Double> yCoords,
        double avgX,
        double avgY
    ) {
        if (xCoords.isEmpty()) {
            return 0.0;
        }

        double maxDist = 0.0;
        for (int i = 0; i < xCoords.size(); i++) {
            double dx = xCoords.get(i) - avgX;
            double dy = yCoords.get(i) - avgY;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > maxDist) {
                maxDist = dist;
            }
        }

        return maxDist;
    }

    /**
     * 計算水平 (X) 和垂直 (Y) 座標的樣本協方差 (Covariance, 分母為 N-1)。
     * 協方差用於確定橢圓的旋轉角度。
     *
     * @param xCoords 最終 X 座標列表 (單位: 碼)。
     * @param yCoords 最終 Y 座標列表 (單位: 碼)。
     * @param avgX 平均 X 座標 (單位: 碼)。
     * @param avgY 平均 Y 座標 (單位: 碼)。
     * @return 樣本協方差 (單位: 碼的平方)。
     */
    private double calculateCovariance(
        List<Double> xCoords,
        List<Double> yCoords,
        double avgX,
        double avgY
    ) {
        int n = xCoords.size();
        if (n < 2) {
            return 0.0;
        }

        double sumOfProducts = 0.0;
        for (int i = 0; i < n; i++) {
            // (x_i - avg_x) * (y_i - avg_y)
            double xDev = xCoords.get(i) - avgX;
            double yDev = yCoords.get(i) - avgY;
            sumOfProducts += xDev * yDev;
        }

        // 樣本協方差 (Sample Covariance)
        return sumOfProducts / (n - 1);
    }

    // ----------------------------------------------------
    // 輔助工具
    // ----------------------------------------------------

    /**
     * 計算已排序列表中指定分位數的值 (例如 Q1=0.25, Q3=0.75)。
     * 使用最近排名法 (Nearest Rank Method) 進行近似計算。
     *
     * @param sortedValues 已經排序的數據列表 (List<Double>)。
     * @param quantile 想要計算的分位數 (例如 0.25 代表 Q1)。
     * @return 對應分位數的值。
     */
    private double getQuantile(List<Double> sortedValues, double quantile) {
        if (sortedValues == null || sortedValues.isEmpty()) {
            return 0.0;
        }
        int size = sortedValues.size();
        // 計算分位數在列表中的近似索引
        double indexDouble = (size - 1) * quantile;
        int indexFloor = (int) Math.floor(indexDouble);
        int indexCeil = (int) Math.ceil(indexDouble);

        if (indexFloor == indexCeil) {
            return sortedValues.get(indexFloor);
        }

        // 線性內插 (Linear Interpolation)
        double valueFloor = sortedValues.get(indexFloor);
        double valueCeil = sortedValues.get(indexCeil);

        return valueFloor + (valueCeil - valueFloor) * (indexDouble - indexFloor);
    }

    /**
     * [通用標準差計算函式] 計算數值列表的樣本標準差 (分母為 N-1)。
     *
     * @param list 數值列表。
     * @return 樣本標準差。
     */
    private double calculateStDevFromList(List<Double> list) {
        int n = list.size();
        if (n < 2) {
            return 0.0;
        }

        double mean = list.stream().mapToDouble(d -> d).average().orElse(0.0);
        double variance = list.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .sum() / (n - 1); // 樣本方差 (Sample Variance)

        return Math.sqrt(variance);
    }

    /**
     *
     * @param value 原始數值
     * @param places 小數點第幾位
     * @return 樣本標準差。
     */
    private double roundDecimal(double value, int places) {
        if (places < 0) {
            return value;
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    /**
     * 根據球桿類型取得動態的落點一致率容許範圍。
     *
     * @param clubType 球桿類型 (例如 "SandWedge", "9Iron")。
     * @return 容許範圍半徑 (單位: 碼)。
     */
    private double getToleranceByClubType(String clubType) {
        if (clubType == null) {
            return 5.0; // 預設值
        }

        // 將球桿類型標準化為大寫，以便進行穩定的比較
        String type = clubType.toUpperCase();

        // 採用階梯式定義：距離越短，要求越嚴格 (容許範圍越小)
        switch (type) {
            case "SANDWEDGE":
            case "S":
                // 建議用於短切或 50 碼內的全揮桿
                return 3.0;
            case "GAPWEDGE":
            case "GW":
            case "A": // 有些模擬器稱 A-Wedge
                // 建議用於 70-100 碼
                return 3.5;
            case "PITCHINGWEDGE":
            case "PW":
                // 建議用於 100-120 碼
                return 4.0;
            case "9IRON":
            case "9I":
            case "I9":
                // 建議用於 120-140 碼
                return 4.5;
            default:
                // 針對未定義或長揮桿的球桿類型提供一個合理的預設值
                return 5.0;
        }
    }


    // ----------------------------------------------------
    // 數據結構
    // ----------------------------------------------------

    /*
	 * 原始擊球數據的資料結構。
	 */
	public static class ShortGameShotData {
        // 核心識別符
        private long id;
        private String player;
		private String clubType;

        // 關鍵數據
		private double carryDistFt;
		private double totalDistFt;
		private double launchDirection;

        // 輔助數據
		private double clubHeadSpeed;
		private double backSpin;
        private double sideSpin;
		private double smashFactor;

        // 參考
        private String date;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getPlayer() {
            return player;
        }

        public void setPlayer(String player) {
            this.player = player;
        }

        public String getClubType() {
            return clubType;
        }

        public void setClubType(String clubType) {
            this.clubType = clubType;
        }

        public double getCarryDistFt() {
            return carryDistFt;
        }

        public void setCarryDistFt(double carryDistFt) {
            this.carryDistFt = carryDistFt;
        }

        public double getTotalDistFt() {
            return totalDistFt;
        }

        public void setTotalDistFt(double totalDistFt) {
            this.totalDistFt = totalDistFt;
        }

        public double getLaunchDirection() {
            return launchDirection;
        }

        public void setLaunchDirection(double launchDirection) {
            this.launchDirection = launchDirection;
        }

        public double getClubHeadSpeed() {
            return clubHeadSpeed;
        }
        public void setClubHeadSpeed(double clubHeadSpeed) {
            this.clubHeadSpeed = clubHeadSpeed;
        }

        public double getBackSpin() {
            return backSpin;
        }

        public void setBackSpin(double backSpin) {
            this.backSpin = backSpin;
        }

        public double getSideSpin() {
            return sideSpin;
        }

        public void setSideSpin(double sideSpin) {
            this.sideSpin = sideSpin;
        }

        public double getSmashFactor() {
            return smashFactor;
        }

        public void setSmashFactor(double smashFactor) {
            this.smashFactor = smashFactor;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
	}

    /*
	 * 經過處理後的短桿數據資料結構，用於內部計算。
	 */
    private static class ProcessedShotData {
        public final double x_coord_yd; // 最終 X 座標 (碼) - 用於落點圖和水平 StDev
        public final double y_coord_yd; // Carry 距離 (碼) - 用於落點圖和垂直 StDev
        public final double carry_ratio; // 飛行占比 (%)
        public final double launch_direction_deg; // 發射方向 (度)

        public ProcessedShotData(
            double x_coord_yd,
            double y_coord_yd,
            double carry_ratio,
            double launch_direction_deg
        ) {
            this.x_coord_yd = x_coord_yd;
            this.y_coord_yd = y_coord_yd;
            this.carry_ratio = carry_ratio;
            this.launch_direction_deg = launch_direction_deg;
        }
    }
}
