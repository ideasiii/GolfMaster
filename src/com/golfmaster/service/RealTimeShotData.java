/*
 * 擊球數據API
 * 參數: player(必填),start_date,end_date (無日期則抓最近10筆)
 * http://localhost/GolfMaster/service/shot-data.jsp?player=louisju
 * http://18.181.37.98/GolfMaster/service/shot-data.jsp?player=louisju
 * http://localhost:8080/GolfMaster/service/shot-data.jsp?player=louisju
 */
package com.golfmaster.service;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.moduel.PSystem;

public class RealTimeShotData extends DeviceData{
	
	private class ParamData {
		private String	player;
		private String	start_date;
		private String	end_date;
	}
	
	private class BasicInfo {
		@SuppressWarnings("unused")
		public String idx;
		
		@SuppressWarnings("unused")
		public String LID;
		
		@SuppressWarnings("unused")
		public String Player;
		
		@SuppressWarnings("unused")
		public String Date;
		
		@SuppressWarnings("unused")
		public String Token;
		
		public DeviceData.WrgData shotData;
	}
	
	private class Expert {
		@SuppressWarnings("unused")
		public Long shot_data_id;
		
		@SuppressWarnings("unused")
		public String expert_trajectory;
		
		@SuppressWarnings("unused")
		public String expert_p_system;
		
		@SuppressWarnings("unused")
		public String expert_suggestion;
		
		@SuppressWarnings("unused")
		public String expert_cause;
		
		@SuppressWarnings("unused")
		public String expert_update_time;
	}
	
	
	
	public String processRequest(HttpServletRequest request) {
		JSONObject jsonResponse = null;
//		String strResponse = "{\"success\": true,\"result\": []}";
		
		try {
			printParam(request);
			
			String redirectUrl = "";
			String shotData = request.getParameter("shotData");
			jsonResponse = this.checkParam(shotData);
			if (null != jsonResponse) {
				return jsonResponse.toString();
			}
			
			BasicInfo info = this.getShotData(shotData);
			String result = new PSystem().expertAnalysis(info.shotData.BallSpeed, info.shotData.ClubAnglePath, info.shotData.ClubAngleFace, info.shotData.TotalDistFt, 
					info.shotData.CarryDistFt, info.shotData.LaunchAngle, info.shotData.SmashFactor, info.shotData.BackSpin, info.shotData.SideSpin, 
					info.shotData.ClubHeadSpeed, info.shotData.LaunchDirection, info.shotData.DistToPinFt);
			if(result != null && !result.isEmpty()) {
				long shotDataId = this.saveShotData(info);
				if(shotDataId > 0) {
					JSONObject jsonExpert = new JSONObject(result);
					Expert expert = new Expert();
					expert.shot_data_id = shotDataId;
					
					Field [] fields = expert.getClass().getDeclaredFields();
					for(int i=0;i<fields.length;i++) {
						if(jsonExpert.has(fields[i].getName())){
							fields[i].set(expert, jsonExpert.get(fields[i].getName()));
						}
					}
					
					this.saveExpertData(expert);
				}
			}
			
			if(jsonResponse == null) {
				jsonResponse = new JSONObject();
			}
			jsonResponse.put("success", true);
			jsonResponse.put("result", redirectUrl);
		}catch(Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			
			if(jsonResponse == null) {
				jsonResponse = new JSONObject();
			}
			jsonResponse.put("success", false);
			jsonResponse.put("message", e.getMessage());
		}
		
		Logs.log(Logs.RUN_LOG, "Response : " + jsonResponse.toString());
		
		return jsonResponse.toString();
	}
	
	private JSONObject checkParam(String shotData) throws Exception{
		JSONObject result = null;
		try {
			if(shotData == null || StringUtils.trimToEmpty(shotData).isEmpty()) {
				result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			}else {
//				boolean hasKey = true;
				JSONObject main = new JSONObject(shotData);
				String [] keys = {"idx", "LID", "Player", "Date", "Token", "ShotData"};
				for(int i = 0;i<keys.length;i++) {
					if(main.has(keys[i])) {
//						hasKey = false;
						result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
						break;
					}
				}
				if(result == null) {
					for(String key : main.keySet()) {
						if(main.get(key) == null || main.get(key).toString().isEmpty()) {
							result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
							break;
						}
					}
					
					JSONObject sd = main.getJSONObject("ShotData");
					DeviceData.WrgData wrgData = new DeviceData.WrgData();
					Field [] fields = wrgData.getClass().getDeclaredFields();
					
					for(int i=0;i<fields.length;i++) {
						String fieldName = fields[i].getName();
						if(!sd.has(fieldName)) {
							result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
							break;
						}else {
							if(sd.get(fieldName) == null || sd.get(fieldName).toString().isEmpty()) {
								result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
								break;
							}
						}
					}
				}
				/*
				if(!hasKey) {
					return ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
				}else {
					for(String key : main.keySet()) {
						if(main.get(key) == null || main.get(key).toString().isEmpty()) {
							
						}
					}
				}
				*/
			}
		}catch(Exception e) {
			throw e;
		}
		
		return result;
	}
	
	private BasicInfo getShotData(String jsonStr) throws Exception{
		BasicInfo info = new BasicInfo();
		DeviceData.WrgData shotData = new DeviceData.WrgData();
		try {
			JSONObject infoJsonData = new JSONObject(jsonStr);
			Field [] fields = info.getClass().getDeclaredFields();
			
			for(int i=0;i<fields.length;i++) {
				String fieldName = fields[i].getName();
				if(!"shotData".equals(fieldName)) {
					fields[i].set(info, infoJsonData.getString(fieldName));
				}
				
			}
			
			JSONObject shotJsonData = infoJsonData.getJSONObject("ShotData");
			
			fields = shotData.getClass().getDeclaredFields();
			
			for(int i=0;i<fields.length;i++) {
				String fieldName = fields[i].getName();
				fields[i].set(shotData, shotJsonData.getFloat(fieldName));
			}
			
			info.shotData = shotData;
		}catch(Exception e) {
//			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			throw e;
		}
		
		return info;
	}
	
	private long saveShotData(BasicInfo info) throws Exception{
		long id = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			Map<String, Object> datas = new HashMap<String, Object>();
			
			Field [] fields = info.getClass().getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				if(!"shotData".equals(fields[i].getName())) {
					datas.put(fields[i].getName(), fields[i].get(info));
				}
			}
			fields = info.shotData.getClass().getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				datas.put(fields[i].getName(), fields[i].get(info.shotData));
				if("Date".equals(fields[i].getName())) {
					datas.put(fields[i].getName(), new Timestamp(Long.parseLong(datas.get(fields[i].getName()).toString())));
					datas.put("Date_str", datas.get(fields[i].getName()));
				}
			}
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO shot_data (");
			int cnt = 0;
			for(String key:datas.keySet()) {
				sql.append(key + (cnt < (datas.size() - 1)?", ":""));
				cnt++;
			}
			sql.append(") VALUES (");
			for(int i=0;i<datas.size();i++) {
				sql.append("?" + (i < (datas.size() - 1)?", ":""));
			}
			sql.append(");");
			
			conn = DBUtil.getConnGolfMaster();
			ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
			cnt = 1;
			for(String key:datas.keySet()) {
				ps.setObject(cnt, datas.get(key));
				cnt++;
			}
			
			int updateResult = ps.executeUpdate();
			if(updateResult == 1) {
				rs = ps.getGeneratedKeys();
				while (rs.next()) {
					id = rs.getLong(1);
				}
			}
		}catch(Exception e) {
			throw e;
		}finally {
			DBUtil.closePreparedStatement(ps);
			DBUtil.closeResultSet(rs);
			DBUtil.closeConn(conn);
		}
		
		return id;
	}
	
	private void saveExpertData(Expert expert) throws Exception{
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			Map<String, Object> datas = new HashMap<String, Object>();
			
			Field [] fields = expert.getClass().getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				datas.put(fields[i].getName(), fields[i].get(expert));
			}
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO shot_data (");
			
			int cnt = 0;
			for(String key:datas.keySet()) {
				sql.append(key + (cnt < (datas.size() - 1)?", ":""));
				cnt++;
			}
			sql.append(") VALUES (");
			cnt = 0;
			for(String key:datas.keySet()) {
				String value = "?";
				if("expert_update_time".equals(key)) {
					value = "current_timestamp()";
				}
				sql.append(value + (cnt < (datas.size() - 1)?", ":""));
			}
			sql.append(");");
			
			conn = DBUtil.getConnGolfMaster();
			ps = conn.prepareStatement(sql.toString());
			
			cnt = 1;
			for(String key:datas.keySet()) {
				if(!"expert_update_time".equals(key)) {
					ps.setObject(cnt, datas.get(key));
					cnt++;
				}
			}
			
			ps.executeUpdate();
			
		}catch(Exception e) {
			throw e;
		}finally {
			DBUtil.closePreparedStatement(ps);
			DBUtil.closeConn(conn);
		}
	}

	private int queryShotData(ParamData paramData, JSONObject jsonResponse)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();

		jsonResponse.put("result", jarrProjects);
		// 無日期就取最近的10筆
		if (StringUtils.isEmpty(paramData.start_date) || StringUtils.isEmpty(paramData.end_date))
		{
			strSQL = String.format("SELECT * FROM golf_2022.ShotData WHERE Player = '%s' order by Date DESC LIMIT 10", paramData.player);
		}
		else
		{
			strSQL = String.format(
					"SELECT * FROM golf_2022.ShotData WHERE Player = '%s' AND AND Date >= '%s 00:00:00' AND Date <= '%s 23:59:59' order by Date DESC",
					paramData.player, paramData.start_date, paramData.end_date);
		}
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try
		{
			conn = DBUtil.getConnGolfController();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next())
			{
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("id", rs.getInt("id"));
				jsonProject.put("LID", rs.getString("LID")); //每個不同模擬器配合場域的odd
				jsonProject.put("Player", rs.getString("Player"));
				jsonProject.put("Date", rs.getString("Date"));
				jsonProject.put("shotVideo_front", rs.getString("shotVideo_front")); //正面影片
				jsonProject.put("shotVideo_side", rs.getString("shotVideo_side")); //側面影片
				jsonProject.put("BallSpeed", rs.getDouble("BallSpeed")); //高爾夫球在撞擊後立即的速度，球速是由球桿速度和衝擊力決定的。*技術定義：球速係指高爾夫球重心與桿面分離後的速度\n單位: mph
				jsonProject.put("LaunchAngle", rs.getDouble("LaunchAngle")); //隨著球速的降低，最佳發射角度必須增加，後旋也必須增加。單位: degree
				jsonProject.put("LaunchDirection", rs.getDouble("LaunchDirection")); //發射方向是球相對於目標線開始的初始方向。正發射方向表示球從目標右側開始，負發射方向......單位: degree
				jsonProject.put("ClubHeadSpeed", rs.getDouble("ClubHeadSpeed")); //從身體設置，到上桿頂點身體旋轉，讓手臂和身體將保持連接，保持在正確的揮桿平面上，並使用大肌肉來創造擊球過程。(握壓/Swing/釋放/節奏)，單位: mph
				jsonProject.put("ClubAngleFace", rs.getDouble("ClubAngleFace")); //高爾夫球手將此稱為具有“開放”或“封閉”桿面。*技術定義：在高爾夫球最大壓縮時，球桿面和高爾夫球接觸中心點的水平球桿面方向，單位: degree
				jsonProject.put("ClubAnglePath", rs.getDouble("ClubAnglePath")); //高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)\\\\n技術定義：FACE ANGLE 和 CLUB PATH 定義的角度差（FACE ANGLE 減去 CLUB PATH）。單位: degree'
				jsonProject.put("BackSpin", rs.getInt("BackSpin")); //高爾夫球迴旋是高爾夫球的反向旋轉
				jsonProject.put("SideSpin", rs.getInt("SideSpin")); //高爾夫球側旋是橫向發生的旋轉
				jsonProject.put("SmashFactor", rs.getDouble("SmashFactor")); //球桿速度除以球速
				jsonProject.put("ClubType", rs.getString("ClubType")); //幾號球桿、材質
				jsonProject.put("DistToPinFt", rs.getDouble("DistToPinFt")); //擊球後與目標的距離，單位: ft 呎
				jsonProject.put("CarryDistFt", rs.getDouble("CarryDistFt")); //置球點到擊球落點的距離，單位: ft 呎
				jsonProject.put("TotalDistFt", rs.getDouble("TotalDistFt")); //置球點到擊球後停止滾動的距離，單位: ft 呎
				jsonProject.put("expert_trajectory", rs.getString("expert_trajectory"));
				jsonProject.put("expert_note", rs.getString("expert_note"));
				jsonProject.put("expert_p-system", rs.getString("expert_p-system"));
				jsonProject.put("expert_suggestion", rs.getString("expert_suggestion"));
				jsonProject.put("expert_cause", rs.getString("expert_cause"));
				
				jarrProjects.put(jsonProject);
			}
			jsonResponse.put("success", true);
		}
		catch (Exception e)
		{
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResponse.put("success", false);
			jsonResponse.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		jsonResponse.put("result", jarrProjects);
		return jarrProjects.length();
	}

	private JSONObject requestAndTrimParams(HttpServletRequest request, ParamData paramData)
	{
		try
		{
			paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
			paramData.start_date = StringUtils.trimToEmpty(request.getParameter("start_date"));
			paramData.end_date = StringUtils.trimToEmpty(request.getParameter("end_date"));

			if (StringUtils.isEmpty(paramData.player))
			{
				return ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			}
		}
		catch (Exception e)
		{
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			return ApiResponse.unknownError();
		}

		return null;
	}

	private void printParam(HttpServletRequest request)
	{
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements())
		{
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		Logs.log(Logs.RUN_LOG, strRequest);
	}
}
