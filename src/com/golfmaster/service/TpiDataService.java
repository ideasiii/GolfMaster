/**
 * BUG
 * TPI (Titleist Performance Institute) 數據服務類。
 * <p>
 * 此類別提供與TPI揮桿特徵相關的建議數據。它包含了16種常見揮桿特徵的定義、
 * 原因和修正建議，並提供方法根據輸入的揮桿數據篩選出對應的建議。
 * </p>
 */

package com.golfmaster.service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


public class TpiDataService {

    // 定義 tpiAdvices 數據結構
    private static final List<Map<String, String>> TPI_ADVICES = new ArrayList<>();
    private static final Map<String, List<Integer>> TPI_MAPPING = new HashMap<>();

    static {
        // 初始化TPI建議列表，包含16種揮桿特徵的詳細資訊。

        // S-Posture (0)
        TPI_ADVICES.add(createAdvice("s_posture", "A", "準備", "S形姿勢",
                "骨盆前傾或下交叉綜合症造成",
                // "收緊核心，保持脊椎中立，避免過度彎曲下背部。"
                "站姿時，試著輕輕收緊腹部和屁股，讓下背部的凹陷減少，脊椎回到最自然的狀態。"
        ));
        // C-Posture (1)
        TPI_ADVICES.add(createAdvice("c_posture", "A", "準備", "C形姿勢",
                "胸椎伸展受限或上交叉綜合症",
                // "擴展胸椎，改善肩胛骨穩定性，並確認球桿長度是否合適。"
                "讓你的胸口稍微往前頂一點，感覺肩胛骨夾緊，讓上背部能更直挺。"
        ));
        // Loss of Posture (2)
        TPI_ADVICES.add(createAdvice("loss_of_posture", "I", "揮桿過程", "喪失體姿",
                "揮桿中原始設定角度變化",
                // "訓練核心力量與髖部、肩部柔韌性，保持脊椎角度穩定。"
                "強化核心力量、髖部及肩部的柔韌性，確保在整個揮桿過程中，脊椎的彎曲和前傾角度能保持一致。"
        ));
        // Flat Shoulder Plane (3)
        TPI_ADVICES.add(createAdvice("flat_shoulder_plane", "T", "上桿",
                "平坦肩部平面", "肩部轉動平面過於水平",
                // "練習軀幹與骨盆分離，並改善脊椎與肩部活動度。"
                "上桿時，感覺你的左肩（領先肩）要向下沉到球底下，讓肩膀的轉動更垂直。"
        ));

        // Flying Elbow (4)
        TPI_ADVICES.add(createAdvice("flying_elbow", "T", "上桿", "飛肘",
                "後側手肘離開身體後側",
                // "訓練肩關節和胸椎活動度，保持肩部穩定並同步揮桿。"
                "保持雙手和身體的連接感。想像用胸口帶動手臂上桿，而不是單獨甩手，讓右肘保持在身前。"
        ));
        // Early Extension (5)
        TPI_ADVICES.add(createAdvice("early_extension", "I", "下桿", "提前伸展",
                "下半身過早向球移動",
                // "增加髖部和下半身柔韌性，用身體旋轉而不是推動來啟動下桿。"
                "下桿時，感覺膝蓋和屁股保持原有的彎曲，用屁股的旋轉來帶動，而不是把身體推向前方。"
        ));
        // Over the Top (6)
        TPI_ADVICES.add(createAdvice("over_the_top", "I", "下桿", "由上而下",
                "下桿時過度使用上半身",
                // "啟動下半身，讓球桿從內側路徑下桿。"
                "下桿時，感覺腳和下半身先轉動，讓球桿從身體內側（右肩下方）進入。"
        ));
        // Sway (7)
        TPI_ADVICES.add(createAdvice("sway", "T", "上桿", "身體搖擺",
                "上桿時下半身橫向移動",
                // "加強髖部內旋和臀大肌力量，保持重心穩定在上半身。"
                "專注於讓髖部在後腳內側穩定旋轉，而非橫向滑動。"
        ));

        // Slide (8)
        TPI_ADVICES.add(createAdvice("slide", "I", "下桿", "身體側移",
                "下桿時下半身過度橫向移動",
                // "訓練臀大肌力量，讓下半身穩定，以旋轉而不是側移來帶動下桿。"
                "訓練用屁股的旋轉來推動下桿，而不是讓髖部向目標方向平移。"
        ));
        // Late Buckle (9)
        TPI_ADVICES.add(createAdvice("late_buckle", "I", "擊球後", "遲滯彎曲",
                "擊球後膝蓋突然彎曲下沉",
                // "強化髖部活動度和核心穩定性，讓身體能支撐下桿的巨大力量。"
                "強化核心和腿部力量，讓你在擊球後能有力地站直並支撐住身體釋放的衝擊力。"
        ));
        // Reverse Spine Angle (10)
        TPI_ADVICES.add(createAdvice("reverse_spine_angle", "T", "上桿", "反向脊柱角度",
                "上桿時上半身過度向後傾斜",
                // "訓練核心力量與軀幹分離，保持脊椎在正確的軸心上旋轉。"
                "上桿時，想像你的頭部和脊椎像一根柱子，身體只是繞著它旋轉，不要往目標方向傾倒。"
        ));
        // Forward Lunge (11)
        TPI_ADVICES.add(createAdvice("forward_lunge", "I", "下桿", "前向弓步",
                "下桿時上半身過度前移",
                // "提升下半身力量與爆發力，讓體重轉移正確。"
                "練習用下半身（腳與髖部）的轉動來移動重心，避免上半身急著過度前傾。"
        ));

        // Hanging Back (12)
        TPI_ADVICES.add(createAdvice("hanging_back", "I", "擊球", "向後滯留",
                "下桿時缺乏體重轉移",
                // "訓練核心與腿部力量，確保在下桿時將體重完整轉移到前腳。"
                "擊球時，確保你的重心和力量都壓在前腳上，感覺身體是穩穩地站在前腳上完成擊球。"
        ));
        // Casting (13)
        TPI_ADVICES.add(createAdvice("casting", "I", "下桿", "拋擲",
                "下桿時過早釋放手腕角度",
                // "強化下半身啟動，延遲手腕釋放，以增加桿頭速度。"
                "下桿時，感覺手腕角度要維持住，直到身體轉動帶領球桿接近球時，再讓它釋放。"
        ));
        // Scooping (14)
        TPI_ADVICES.add(createAdvice("scooping", "I", "擊球", "撈球",
                "擊球時試圖將球撈起",
                // "強化下半身力量與髖部活動度，專注於向下擊球以獲得穩定力量。"
                "強化下半身力量，讓身體帶動球桿向下且紮實地擊中球，而不是試圖用手腕將球從地上舀起來。"
        ));
        // Chicken Wing (15)
        TPI_ADVICES.add(createAdvice("chicken_wing", "I", "擊球", "雞翅膀",
                "擊球後領先手臂過度彎曲",
                // "增強下半身力量，讓身體轉動帶動手臂，保持手臂伸直以增加寬度。"
                "增強下半身的力量，讓身體的旋轉力量帶動手臂伸向目標，保持手臂寬度。"
        ));

        // 將揮桿階段 (A, T, I, F) 映射到 TPI_ADVICES 列表中的索引。
        // A 階段:主要檢查起始站姿問題，先處理結構缺陷
        // S-Posture (0) > C-Posture (1)
        TPI_MAPPING.put("A", Arrays.asList(0, 1));

        // T 階段: 影響揮桿路徑穩定的動作
        // Reverse Spine Angle (10) > Sway (7) > Flat Shoulder Plane (3) > Flying Elbow (4)
        // - Reverse Spine Angle 是 TPI 最強調的高風險特徵之一（直接與下背痛相關）。
        // - Sway 會阻礙下半身穩定與旋轉，嚴重影響動力鏈。
        // - Flat Shoulder Plane 與 Flying Elbow 影響更多在路徑與擊球穩定性，不會立刻帶來高受傷風險，
        TPI_MAPPING.put("T", Arrays.asList(10, 7, 3, 4));

        // I 階段 (下桿/擊球)：依據錯誤嚴重性分層排序
        // 排序邏輯：
        // 1. 核心結構錯誤（最嚴重，會直接破壞下桿穩定性）
        //    - Early Extension (5)：骨盆提前推向球，核心結構崩壞。
        //    - Loss of Posture (2)：脊椎角度消失，影響全身協調。
        //    - Over the Top (6)：揮桿路徑錯誤，常因核心沒帶動而強行用上半身主導。
        //      → 雖然表面看只是偏左/偏右，但因它代表「上半身接管、下半身無效化」，
        //         所以嚴重程度被歸類為核心結構問題，排序比下半身錯誤更前。
        //
        // 2. 下半身轉移/穩定錯誤（影響力量傳遞與擊球準確度）
        //    - Forward Lunge (11)：重心過度前移。
        //    - Hanging Back (12)：重心停留後側，無法完全轉移。
        //    - Slide (8)：骨盆橫向滑動，阻礙旋轉。
        //    - Late Buckle (9)：擊球後下肢支撐失敗。
        //      → 排序 11 > 12 > 8 > 9，依據對擊球瞬間的直接影響程度排列。
        //
        // 3. 手部/釋放錯誤（屬於揮桿表現缺陷，多為結果而非根因）
        //    - Casting (13)：提前釋放手腕角度。
        //    - Scooping (14)：擊球撈球，影響壓縮球。
        //    - Chicken Winging (15)：手臂過度彎曲，影響延伸。
        //      → 排序 13 > 14 > 15，依據對桿頭速度與球質影響程度排列。
        //
        TPI_MAPPING.put("I", Arrays.asList(5, 2, 6, 11, 12, 8, 9, 13, 14, 15));

        // F 階段: 保持不變 (空)
        TPI_MAPPING.put("F", new ArrayList<>());
    }

    /**
     * 創建一個包含TPI建議的Map對象。
     *
     * @param name       特徵的英文名稱 (e.g., "s_posture")
     * @param phase      揮桿階段 (A, T, I, F)
     * @param title      階段標題 (e.g., "準備")
     * @param posture    姿勢/特徵名稱 (e.g., "S形姿勢")
     * @param reason     可能原因
     * @param suggestion 修正建議
     * @return 一個包含所有建議資訊的Map
     */
    private static Map<String, String> createAdvice(
        String name,
        String phase,
        String title,
        String posture,
        String reason,
        String suggestion
    ) {
        Map<String, String> advice = new HashMap<>();
        advice.put("name", name);
        advice.put("phase", phase);
        advice.put("title", title);
        advice.put("posture", posture);
        advice.put("reason", reason);
        advice.put("suggestion", suggestion);
        return advice;
    }

    /**
     * 根據指定的揮桿階段和TPI揮桿特徵表，篩選出相關的建議。
     *
     * @param combinedTpiSwingTable 一個整數陣列，代表16個TPI特徵的檢測結果
     *                              (1表示存在該特徵，0表示不存在)。
     * @param phase                 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @return 一個包含篩選後建議的Map列表。如果沒有符合的建議，則返回空列表。
     */
    public List<Map<String, String>> getFilteredAdvices(
            int[] combinedTpiSwingTable,
            String phase
    ) {
        List<Map<String, String>> filteredAdvices = new ArrayList<>();
        List<Integer> phaseIndices = TPI_MAPPING.getOrDefault(phase, new ArrayList<>());

        if (combinedTpiSwingTable == null || combinedTpiSwingTable.length == 0) {
            return filteredAdvices; // 返回空列表
        }

        for (int index : phaseIndices) {
            if (index < combinedTpiSwingTable.length &&
                    combinedTpiSwingTable[index] == 1) {
                if (index < TPI_ADVICES.size()) {
                    filteredAdvices.add(TPI_ADVICES.get(index));
                }
            }
        }
        return filteredAdvices;
    }

    /**
     * 根據指定的揮桿階段和TPI揮桿特徵表，篩選出【優先級最高】的一個建議。
     *
     * 邏輯：遍歷 TPI_MAPPING 中【排好序】的索引，找到第一個在 combinedTpiSwingTable 中為 1 的特徵。
     * * @param combinedTpiSwingTable 一個整數陣列 (1表示存在該特徵，0表示不存在)。
     * @param phase                 要篩選的揮桿階段 ('A', 'T', 'I', 'F')。
     * @return 一個包含篩選後建議的列表 (最多只包含一個建議)。如果沒有符合的建議，則返回空列表。
     */
    public List<Map<String, String>> getTopFilteredAdvice(
            int[] combinedTpiSwingTable,
            String phase
    ) {
        List<Map<String, String>> topAdviceList = new ArrayList<>();
        // 取得該階段【已排序】的索引列表
        List<Integer> phaseIndices = TPI_MAPPING.getOrDefault(phase, new ArrayList<>());

        if (combinedTpiSwingTable == null || combinedTpiSwingTable.length == 0) {
            return topAdviceList; // 返回空列表
        }

        // 根據優先級（TPI_MAPPING 的順序）遍歷索引
        for (int index : phaseIndices) {
            // 檢查索引是否在 combinedTpiSwingTable 的範圍內
            if (index < combinedTpiSwingTable.length &&
                        combinedTpiSwingTable[index] == 1) {

                // 找到第一個為 1 的特徵，這就是優先級最高的特徵
                if (index < TPI_ADVICES.size()) {
                    topAdviceList.add(TPI_ADVICES.get(index));
                    // 【關鍵】只返回一個建議，然後立即退出迴圈
                    break;
                }
            }
        }
        return topAdviceList;
    }

    /**
     * 根據TPI揮桿特徵表，篩選出所有階段的建議。
     * @param combinedTpiSwingTable 一個整數陣列，代表16個TPI特徵的檢測結果。
     * @return 一個包含所有階段建議的Map。鍵是階段名稱，值是建議列表。
     */
    public Map<String, List<Map<String, String>>> getAllFilteredAdvices(
        int[] combinedTpiSwingTable
    ) {
        Map<String, List<Map<String, String>>> allFilteredAdvices = new HashMap<>();

        // for (String phase : TPI_MAPPING.keySet()) {
        //     List<Map<String, String>> filteredList = getFilteredAdvices(combinedTpiSwingTable, phase);
        //     allFilteredAdvices.put(phase, filteredList);
        // }

        for (String phase : TPI_MAPPING.keySet()) {
            // 使用新的 getTopFilteredAdvice 方法
            List<Map<String, String>> filteredList = getTopFilteredAdvice(combinedTpiSwingTable, phase);
            allFilteredAdvices.put(phase, filteredList);
        }

        return allFilteredAdvices;
    }
}
