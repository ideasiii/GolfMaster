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
import java.util.Enumeration;
import java.util.List;
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
						String[] clubTypes = clubs.split(",");
						String par = result.getString("par");
						if (clubArray.length() == 0) {

							processStandardClubData(standClubArray, distc, woodTemp, ironTemp);

							StringBuilder resultWord = new StringBuilder();
							suggestClubs(par, clubTypes, woodTemp, ironTemp, resultWord);

							resultWord.append(wedge[mutilSelect.nextInt(wedge.length)]).append("-Putter");
							String rw = resultWord.toString();

							System.out.println(rw);
							jsobjParam.put("code", 0);
							jsobjParam.put("strategy", rw);
							jsobjParam.put("Standard", true);
						} else {
							// 比對數據集初始化
							List<Double> tmps = new ArrayList<Double>();
							// 球桿建議文字初始化
							StringBuilder resultWord = new StringBuilder();

							// 球桿建議推算
							// 3桿洞
							if (PART_3.equals(par)) {
								// 擊球數據集重新整理
								for (int i = 0; i < clubArray.length(); i++) {
									tmps.add(clubArray.getJSONObject(i).getDouble("avg"));
								}

								tmps.add(distc);

								// 比對數據集降冪排序
								Collections.sort(tmps, Collections.reverseOrder());

								// 找出近似距離的數值
								int idx = tmps.indexOf(distc);
								double value = tmps.get((idx + 1));

								// 產出3桿洞的球桿建議
								for (int i = 0; i < clubArray.length(); i++) {
									JSONObject jobj = clubArray.getJSONObject(i);
									if (jobj.getDouble("avg") == value) {
										resultWord.append(jobj.getString("ClubType") + "-");
									}
								}
							}
							// 4桿洞
							if (PART_4.equals(par)) {
								// pw跟driver數值初始化
								double pwValue = 0.0;
								double driverValue = 0.0;

								// 擊球數據集重新整理
								for (int i = 0; i < clubArray.length(); i++) {
									JSONObject jobj = clubArray.getJSONObject(i);
									if (!jobj.has("1Wood")) {
										tmps.add(jobj.getDouble("avg"));
									}
								}

								// 從碼數碼錶中取出pw跟driver數值
								for (int i = 0; i < clubArray.length(); i++) {
									JSONObject jobj = clubArray.getJSONObject(i);
									// driver數值
									if ("1Wood".endsWith(jobj.getString("ClubType"))) {
										driverValue = jobj.getDouble("avg");
									}
									// pw數值
									if ("PitchingWedge".endsWith(jobj.getString("ClubType"))) {
										pwValue = jobj.getDouble("avg");
									}
								}

								// 距離與pw跟driver數值運算
								double resValue = distc - pwValue - driverValue;

								tmps.add(resValue);
								System.out.println(tmps.toString());
								// 比對數據集降冪排序
								Collections.sort(tmps, Collections.reverseOrder());
								System.out.println("-----------------------");
								System.out.println(tmps.toString());
								// 找出第二桿近似的數值 & 第二桿名稱初始化
								int idx = tmps.indexOf(resValue);
								double value = tmps.get((idx + 1));
								String club = null;

								// 找出第二桿名稱
								ArrayList<String> list = new ArrayList<String>(Arrays.asList(clubTypes));
								for (int i = 0; i < clubArray.length(); i++) {
									JSONObject jobj = clubArray.getJSONObject(i);
									if (!jobj.has("1Wood")) {
										if (jobj.getDouble("avg") == value) {
											club = jobj.getString("ClubType");
										}
									}
								}

								// 產出4桿洞的球桿建議
								resultWord.append("1Wood-" + (club == null ? "PitchingWedge" : club) + "-");
							}
							// 5桿洞
							if (PART_5.equals(par)) {
								// pw跟driver數值初始化
								double pwValue = 0.0;
								double driverValue = 0.0;

								// 從碼數碼錶中取出pw跟driver數值
								for (int i = 0; i < clubArray.length(); i++) {
									JSONObject jobj = clubArray.getJSONObject(i);
									// driver數值
									if ("1Wood".endsWith(jobj.getString("ClubType"))) {
										driverValue = jobj.getDouble("avg");
									}
									// pw數值
									if ("PitchingWedge".endsWith(jobj.getString("ClubType"))) {
										pwValue = jobj.getDouble("avg");
									}
								}

								// 設置第一桿為Driver，因為五桿洞第一桿一定是Driver
								resultWord.append("1Wood-");

								// 距離與pw跟driver數值運算
								double resValue = distc - pwValue - driverValue;

								// 設定固定範圍比較可能達到推薦5桿
								while ((resValue > 60)) {
									System.out.println("resValue:" + resValue);
									// 整理擊球數據集
									tmps.clear();
									for (int i = 0; i < clubArray.length(); i++) {
										JSONObject jobj = clubArray.getJSONObject(i);
										if (!jobj.has("1Wood")) {
											tmps.add(jobj.getDouble("avg"));
										}
									}

									tmps.add(resValue);
									System.out.println(tmps.toString());
									// 比對數據集降冪排序
									Collections.sort(tmps, Collections.reverseOrder());
									System.out.println("-----------------------");
									System.out.println(tmps.toString());
									// 找出下一桿近似的數值 & 名稱初始化
									int idx = tmps.indexOf(resValue);
									double value = tmps.get((idx + 2));
									String club = null;

									// 找出下一桿名稱
									for (int i = 0; i < clubArray.length(); i++) {
										JSONObject jobj = clubArray.getJSONObject(i);
										if (!jobj.has("1Wood")) {
											if (jobj.getDouble("avg") == value) {
												club = jobj.getString("ClubType");
											}
										}
									}

									// 組合五桿洞球桿建議
									resultWord.append((club == null ? "PitchingWedge" : club) + "-");
									resValue = resValue - value;
								}
							}
							// 最後兩桿是用PW-Putter
							resultWord.append(wedge[mutilSelect.nextInt(wedge.length)] + "-Putter");
							String rw = resultWord.toString();

							// 輸出總運算結果
							System.out.println(resultWord.toString());
							jsobjParam.put("code", 0);
							jsobjParam.put("strategy", rw);
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
		} catch (Exception e) {
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
			List<String> ironTemp) {
		for (int i = 0; i < standClubArray.length(); i++) {
			JSONObject jobtemp = standClubArray.getJSONObject(i);
			String clubType = jobtemp.getString("ClubType");
			double distance = jobtemp.getDouble("Distance");

			if (clubType.endsWith("Wood")) {
				woodTemp.add(clubType);
			} else if (clubType.endsWith("Iron") && distanceInRange(distance, distc)) {
				ironTemp.add(clubType);
			}
		}
	}

	// 距離判斷
	private boolean distanceInRange(double clubDistance, double targetDistance) {
		return clubDistance - targetDistance >= 10 && clubDistance - targetDistance <= 50;
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
			if (PART_5.equals(par)) {
				resultWord.append("1Wood-");
			}
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
