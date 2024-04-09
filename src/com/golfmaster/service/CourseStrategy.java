package com.golfmaster.service;

/*
 * 擊球策略API
 * 參數: course,courseType,holeNumber(必填)
 * http://localhost:8080/GolfMaster/service/coursestrategy.jsp?player=guest&gender=0&course=1&courseType=1&holeNumber=14
 * http://61.216.149.161/GolfMaster/service/coursestrategy.jsp?player=guest&gender=0&course=1&courseType=1&holeNumber=14
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.moduel.YardageBook;
import com.golfmaster.moduel.YardageBook.YardageData;

public class CourseStrategy extends DeviceData {
	private final static String PART_3 = "3";
	private final static String PART_4 = "4";
	private final static String PART_5 = "5";
	private Random mutilSelect = new Random();

	public JSONObject processStrategy(HttpServletRequest request) {
		JSONObject jsobjParam = new JSONObject();
		try {
			printParam(request);
			String player = request.getParameter("player");
			String tee = request.getParameter("tee");
			String course = request.getParameter("course");
			String holeNumber = request.getParameter("holeNumber");
			String clubs = request.getParameter("clubs");
			if (course != null && !course.isEmpty()) {
				int coT = Integer.parseInt(course);
				if (holeNumber != null && !holeNumber.isEmpty()) {
					int hN = Integer.parseInt(holeNumber);
					int g = Integer.parseInt(tee);
					JSONObject result = getCourseDistance(coT, hN, g);
					System.out.println(result.toString());
					if (result.get("success").equals(false)) {
						jsobjParam.put("code", -1);
						jsobjParam.put("distance", false);
					} else if (result.get("success").equals(true)) {
						JSONArray clubArray = getDatas(player);
						JSONArray standClubArray = getStandDatas(g);
						String[] wedge = { "PitchingWedge", "LobWedge", "GapWedge", "ApproachWedge" };
						double distc = result.getDouble("distance");
						List<String> woodTemp = new ArrayList<>();
						List<String> ironTemp = new ArrayList<>();
						List<String> wedgeTemp = new ArrayList<>();
						String[] clubTypes = clubs.split(",");
						String par = result.getString("par");
						StringBuilder resultWord = new StringBuilder();
						// 打法建議
//						StringBuilder strikeMethod = new StringBuilder();
						if (clubArray.length() == 0) {

							processStandardClubData(standClubArray, distc, woodTemp, ironTemp, wedgeTemp);

							suggestClubs(par, clubTypes, woodTemp, ironTemp, resultWord);

							resultWord.append(wedgeTemp.get(mutilSelect.nextInt(wedgeTemp.size()))).append("-Putter");
							String rw = resultWord.toString();
//							String[] clubSelected = rw.split("-");
//							if (clubSelected.length == 4 && coT == 1 && hN == 1) {
//								strikeMethod.append("球道屬於直線球道，第一桿推薦使用" + clubSelected[0] + "，球道中避免左右沙坑，第二桿選擇"
//										+ clubSelected[1] + "朝果嶺方向擊球，若尚未on上果嶺則使用" + clubSelected[2] + "，最後使用"
//										+ clubSelected[3] + "完成球洞。");
//							}
							jsobjParam.put("code", 0);
							jsobjParam.put("strategy", rw);
							jsobjParam.put("Standard", true);
						} else {
							// 使用動態規劃來計算策略
							int p = Integer.parseInt(par);
							String strategy = calculateStrategy(distc, clubArray, p, clubTypes);
							System.out.println(strategy);
							jsobjParam.put("code", 0);
							jsobjParam.put("strategy", strategy);
							jsobjParam.put("Standard", false);
						}
					}
				} else {
					jsobjParam.put("code", -1);
					jsobjParam.put("holeNumber", false);
				}
			} else {
				jsobjParam.put("code", -1);
				jsobjParam.put("course", false);
			}
		} catch (

		Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		Logs.log(Logs.RUN_LOG, "Response : " + jsobjParam.toString());
		return jsobjParam;
	}

	public JSONObject getCourseDistance(int courseID, int holeNumber, int gender) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();
		JSONObject jsonProject = null;
		JSONObject jsonResp = new JSONObject();
		String sex = null;
		if (gender == 0) {
			sex = "red";
		} else if (gender == 1) {
			sex = "white";
		} else {
			sex = "red";
		}

		strSQL = String.format("SELECT course_name," + sex + "_tee_" + holeNumber + ",par_" + holeNumber
				+ " FROM golf_course.course_dis WHERE id=%d;", courseID);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			if (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("distance", rs.getString(sex + "_tee_" + holeNumber));
				jsonProject.put("par", rs.getString("par_" + holeNumber));
				jsonProject.put("course_name", rs.getString("course_name"));
				jsonProject.put("success", true);
			} else {
				jsonProject = new JSONObject();
				jsonProject.put("success", false);
			}

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		Logs.log(Logs.RUN_LOG, jsonProject.toString());
		return jsonProject;
	}

	private JSONArray getDatas(String player) throws Exception {
		JSONArray array = new JSONArray();
		JSONObject jsonProject = null;
		JSONObject jsonResp = new JSONObject();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;

		strSQL = String.format(
				"select ClubType, min(CarryDistFt) as 'min', round(avg(CarryDistFt)/3.0,4) as 'avg', max(CarryDistFt) as 'max'"
						+ " from golf_master.shot_data where player = '%s' group by ClubType order by 1;",
				player);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();

			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {

				jsonProject = new JSONObject();
				jsonProject.put("ClubType", rs.getString("ClubType"));
				jsonProject.put("avg", rs.getDouble("avg"));

				array.put(jsonProject);

			}
			if (array.length() == 0) {
				array = new JSONArray();
			}
			jsonResp.put("success", true);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return array;

	}

	private JSONArray getStandDatas(int tee) throws Exception {
		JSONArray array = new JSONArray();
		JSONObject jsonProject = null;
		JSONObject jsonResp = new JSONObject();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		String gender;
		if (tee == 1) {
			gender = "Men";
		} else if (tee == 0) {
			gender = "Women";
		} else {
			gender = "Women";
		}

		strSQL = String.format(
				"SELECT ClubType, Distance FROM golf_master.yardage_book WHERE Player = 'Standard' AND Gender = '%s';",
				gender);
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();

			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				jsonProject = new JSONObject();
				jsonProject.put("ClubType", rs.getString("ClubType"));
				jsonProject.put("Distance", rs.getInt("Distance"));

				array.put(jsonProject);

			}
			jsonResp.put("success", true);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return array;

	}

	// 處理木 鐵桿
	private void processStandardClubData(JSONArray standClubArray, double distc, List<String> woodTemp,
			List<String> ironTemp, List<String> wedgeTemp) {
		for (int i = 0; i < standClubArray.length(); i++) {
			JSONObject jobtemp = standClubArray.getJSONObject(i);
			String clubType = jobtemp.getString("ClubType");

			if (clubType.endsWith("Wood")) {
				woodTemp.add(clubType);
			} else if (clubType.endsWith("Iron")) {
				ironTemp.add(clubType);
			} else if (clubType.endsWith("Wedge")) {
				wedgeTemp.add(clubType);
			}
		}
	}

	// 距離判斷
	private boolean distanceInRange(double clubDistance, double targetDistance) {
		return targetDistance - clubDistance >= 10 && targetDistance - clubDistance <= 50;
	}

	private void suggestClubs(String par, String[] clubTypes, List<String> woodTemp, List<String> ironTemp,
			StringBuilder resultWord) {
		switch (par) {
		case PART_3:
			suggestClubFromList(ironTemp, clubTypes, resultWord);
			break;
		case PART_4:
			suggestClubFromList(woodTemp, clubTypes, resultWord);
			suggestClubFromList(ironTemp, clubTypes, resultWord);
			break;
		case PART_5:
			resultWord.append("1Wood-");
			suggestClubFromList(woodTemp, clubTypes, resultWord);
			suggestClubFromList(ironTemp, clubTypes, resultWord);
			break;
		}
	}

	private void suggestClubFromList(List<String> clubList, String[] clubTypes, StringBuilder resultWord) {
		List<String> temp = new ArrayList<>();
		for (String value : clubTypes) {
			if (clubList.contains(value)) {
				temp.add(value);
			}
		}
		if (!temp.isEmpty()) {
			resultWord.append(temp.get(mutilSelect.nextInt(temp.size()))).append("-");
		}
	}

	private String calculateStrategy(double distance, JSONArray clubArray, int par, String[] clubTypes) {
	    Map<String, List<String>> clubCategories = categorizeClubs(clubArray, clubTypes);
	    List<String> strategy = new ArrayList<>();

	    // 根據par值添加球桿
	    if (par >= 3) {
	        // 添加至少一個iron和一個wedge
	        strategy.add(selectRandomClub(clubCategories.get("Iron")));
	        strategy.add(selectRandomClub(clubCategories.get("Wedge")));
	    }
	    if (par >= 4) {
	        // 添加至少一個wood
	        strategy.add(0, selectRandomClub(clubCategories.get("Wood")));
	    }
	    if (par == 5) {
	        // 對於par 5，再添加一個wood或iron
	        strategy.add(1, selectRandomClub(
	                mutilSelect.nextBoolean() ? clubCategories.get("Wood") : clubCategories.get("Iron")));
	    }
	    // 總是在最後添加putter
	    strategy.add("Putter");

	    // 按照wood, iron, wedge, putter的順序排列
	    strategy.sort((a, b) -> {
	        int orderA = getOrder(a);
	        int orderB = getOrder(b);
	        return Integer.compare(orderA, orderB);
	    });

	    return String.join("-", strategy);
	}

	private int getOrder(String clubType) {
	    if (clubType.contains("Wood"))
	        return 1;
	    if (clubType.contains("Iron"))
	        return 2;
	    if (clubType.contains("Wedge"))
	        return 3;
	    if (clubType.equals("Putter"))
	        return 4;
	    return 5; // 未知類型的球桿
	}

	private Map<String, List<String>> categorizeClubs(JSONArray clubArray, String[] clubTypes) {
	    Map<String, List<String>> clubCategories = new HashMap<>();
	    clubCategories.put("Wood", new ArrayList<>());
	    clubCategories.put("Iron", new ArrayList<>());
	    clubCategories.put("Wedge", new ArrayList<>());
	    List<String> availableClubs = Arrays.asList(clubTypes);

	    for (int i = 0; i < clubArray.length(); i++) {
	        JSONObject club = clubArray.getJSONObject(i);
	        String clubType = club.getString("ClubType");
	        if (availableClubs.contains(clubType)) {
	            if (clubType.contains("Wood")) {
	                clubCategories.get("Wood").add(clubType);
	            } else if (clubType.contains("Iron")) {
	                clubCategories.get("Iron").add(clubType);
	            } else if (clubType.contains("Wedge")) {
	                clubCategories.get("Wedge").add(clubType);
	            }
	        }
	    }
	    // 對每個類別內的球桿進行排序，特別注意Iron類別的排序
	    for (Map.Entry<String, List<String>> entry : clubCategories.entrySet()) {
	        if (entry.getKey().equals("Iron")) {
	            entry.getValue().sort((a, b) -> extractNumber(a) - extractNumber(b));
	        } else {
	            entry.getValue().sort(Comparator.naturalOrder());
	        }
	    }
	    return clubCategories;
	}
	
	private int extractNumber(String club) {
	    String number = club.replaceAll("\\D+", "");
	    return number.isEmpty() ? 0 : Integer.parseInt(number);
	}

	private String selectRandomClub(List<String> clubs) {
		if (clubs.isEmpty())
			return "UnknownClub";
		return clubs.get(mutilSelect.nextInt(clubs.size()));
	}

	private void printParam(HttpServletRequest request) {
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements()) {
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		Logs.log(Logs.RUN_LOG, strRequest);
	}
}
