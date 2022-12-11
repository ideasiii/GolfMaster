/*
 * 即時擊球數據API
 * 參數: shotData(必填)
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
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;
import com.golfmaster.moduel.PSystem;

public class RealTimeShotData extends DeviceData{
	
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
		
		public ShotData shotData;
	}
	
	private class ShotData {
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
		@SuppressWarnings("unused")
		public String ClubType;
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
						if("this$0".equals(fields[i].getName())) {
							continue;
						}
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
					if(!main.has(keys[i])) {
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
					ShotData shd = new ShotData();
					Field [] fields = shd.getClass().getDeclaredFields();
					
					for(int i=0;i<fields.length;i++) {
						String fieldName = fields[i].getName();
						if("this$0".equals(fieldName)) {
							continue;
						}
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
		ShotData shotData = new ShotData();
		try {
			JSONObject infoJsonData = new JSONObject(jsonStr);
			Field [] fields = info.getClass().getDeclaredFields();
			
			for(int i=0;i<fields.length;i++) {
				String fieldName = fields[i].getName();
				if("this$0".equals(fieldName)) {
					continue;
				}
				if(!"shotData".equals(fieldName)) {
					fields[i].set(info, infoJsonData.getString(fieldName));
				}
				
			}
			
			JSONObject shotJsonData = infoJsonData.getJSONObject("ShotData");
			
			fields = shotData.getClass().getDeclaredFields();
			
			for(int i=0;i<fields.length;i++) {
				String fieldName = fields[i].getName();
				if("this$0".equals(fieldName)) {
					continue;
				}
				if("ClubType".equals(fieldName)) {
					fields[i].set(shotData, shotJsonData.getString(fieldName));
				}else {
					fields[i].set(shotData, shotJsonData.getFloat(fieldName));
				}
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
				if("this$0".equals(fields[i].getName())) {
					continue;
				}
				if(!"shotData".equals(fields[i].getName())) {
					datas.put(fields[i].getName(), fields[i].get(info));
					if("Date".equals(fields[i].getName())) {
						datas.put("Date_str", datas.get(fields[i].getName()));
						datas.put(fields[i].getName(), new Timestamp(Long.parseLong(datas.get(fields[i].getName()).toString())));
					}
				}
			}
			fields = info.shotData.getClass().getDeclaredFields();
			for(int i=0;i<fields.length;i++) {
				if("this$0".equals(fields[i].getName())) {
					continue;
				}
				datas.put(fields[i].getName(), fields[i].get(info.shotData));
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
			
			Logs.log(Logs.RUN_LOG, "strSQL: " + sql.toString());
			
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
				if("this$0".equals(fields[i].getName())) {
					continue;
				}
				datas.put(fields[i].getName(), fields[i].get(expert));
			}
			
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO expert (");
			
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
				cnt++;
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
