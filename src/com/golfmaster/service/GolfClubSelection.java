package com.golfmaster.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class GolfClubSelection {
    // 定義不使用的球桿類型
    private static final String EXCLUDED_CLUB = "4Hybrid";
    
    // 球桿資料類型
    private static final String TYPE_WOOD = "Wood";
    private static final String TYPE_IRON = "Iron";
    private static final String TYPE_WEDGE = "Wedge";
    private static final String TYPE_PUTTER = "Putter";

    // 貪婪選擇球桿
    public String selectClubsForHole(double distance, JSONArray clubArray, int par, String[] availableClubTypes) {
        List<JSONObject> clubs = convertJsonArrayToList(clubArray);
        // 過濾和排序球桿列表
        List<JSONObject> filteredClubs = filterAndSortClubs(clubs, availableClubTypes);
        
        List<String> selectedClubs = new ArrayList<>();
        double remainingDistance = distance;

        for (JSONObject club : filteredClubs) {
            if (fitsParRequirements(club.getString("ClubType"), par) && remainingDistance > 0) {
                selectedClubs.add(club.getString("ClubType"));
                System.out.print("remainingDistance:"+remainingDistance);
                remainingDistance -= club.getDouble("avg");
                System.out.print("-"+club.getString("ClubType")+":"+club.getDouble("avg"));
                System.out.println();
                if (remainingDistance <= 0) break;
            }
        }

        // 總是添加Putter
        if (!selectedClubs.contains(TYPE_PUTTER)) {
            selectedClubs.add(TYPE_PUTTER);
        }

        // 格式化輸出結果前排序
        Collections.sort(selectedClubs, new ClubComparator());

        return formatStrategyOutput(selectedClubs);
    }

    // 過濾和排序球桿列表
    private List<JSONObject> filterAndSortClubs(List<JSONObject> clubs, String[] availableClubTypes) {
        List<JSONObject> filtered = new ArrayList<>();
        List<String> availableTypes = Arrays.asList(availableClubTypes);
        for (JSONObject club : clubs) {
            String clubType = club.getString("ClubType");
            if (availableTypes.contains(clubType) && !clubType.equals(EXCLUDED_CLUB)) {
                filtered.add(club);
            }
        }
        // 按距離降序排序
        Collections.sort(filtered, (a, b) -> b.getInt("avg") - a.getInt("avg"));
        return filtered;
    }

    // 球桿排序比較器
    class ClubComparator implements Comparator<String> {
        public int compare(String a, String b) {
            int typeOrderA = getTypeOrder(a);
            int typeOrderB = getTypeOrder(b);
            if (typeOrderA != typeOrderB) {
                return typeOrderA - typeOrderB;
            } else {
                // 如果同類型，則按號碼升序排序
                return extractNumber(a) - extractNumber(b);
            }
        }

        private int getTypeOrder(String clubType) {
            if (clubType.contains(TYPE_WOOD)) return 1;
            if (clubType.contains(TYPE_IRON)) return 2;
            if (clubType.contains(TYPE_WEDGE)) return 3;
            if (clubType.equals(TYPE_PUTTER)) return 4;
            return 5; // Unknown types
        }

        private int extractNumber(String club) {
            String number = club.replaceAll("\\D+", "");
            return number.isEmpty() ? 0 : Integer.parseInt(number);
        }
    }

    // 將JSONArray轉換為List<JSONObject>以便操作
    private List<JSONObject> convertJsonArrayToList(JSONArray jsonArray) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getJSONObject(i));
        }
        return list;
    }

    // 判斷是否符合特定標準桿要求的球桿類型
    private boolean fitsParRequirements(String type, int par) {
        switch (par) {
            case 3:
                return !type.contains(TYPE_WOOD); // 三桿洞避免使用Wood
            case 4:
            case 5:
                return true; // 四桿洞和五桿洞可以使用任何球桿
            default:
                return false;
        }
    }

    // 格式化策略輸出
    private String formatStrategyOutput(List<String> clubs) {
        return String.join("-", clubs);
    }

//    public static void main(String[] args) {
//        GolfClubSelection selector = new GolfClubSelection();
//
//        // 測試數據
//        JSONArray testClubs = new JSONArray()
//            .put(new JSONObject().put("ClubType", "1Wood").put("Distance", 230))
//            .put(new JSONObject().put("ClubType", "3Wood").put("Distance", 210))
//            .put(new JSONObject().put("ClubType", "5Iron").put("Distance", 160))
//            .put(new JSONObject().put("ClubType", "7Iron").put("Distance", 140))
//            .put(new JSONObject().put("ClubType", "9Iron").put("Distance", 120))
//            .put(new JSONObject().put("ClubType", "4Hybrid").put("Distance", 180)); // 不應被選擇
//
//        String[] availableTypes = {"1Wood", "3Wood", "5Iron", "7Iron", "9Iron", "Putter"};
//        
//        // 測試案例
//        String strategy = selector.selectClubsForHole(450, testClubs, 5, availableTypes);
//        System.out.println("Selected Clubs Strategy: " + strategy);
//    }
}

