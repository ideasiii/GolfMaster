/*
 * 即時擊球數據API
 * 參數: shotData(必填)
 * http://localhost/GolfMaster/service/realtime-shotdata.jsp
 * http://18.181.37.98/GolfMaster/service/realtime-shotdata.jsp
 * http://localhost:8080/GolfMaster/service/realtime-shotdata.jsp
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
import com.golfmaster.moduel.PSystemJP;

public class RealTimeShotDataV2 extends DeviceData{
	
	@SuppressWarnings("unused")
	private class BasicInfo 
	{	
		public String idx;
		public String LID;
		public String Player;
		public String Date;
		public String Token;
		public ShotData shotData;
	}
	
	@SuppressWarnings("unused")
	private class ShotData 
	{
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
	
	@SuppressWarnings("unused")
	private class Expert 
	{
		public Long shot_data_id;
		public String expert_trajectory;
		public String expert_p_system;
		public String expert_suggestion;
		public String expert_cause;
		public String expert_update_time;
	}
	
	@SuppressWarnings("unused")
	private class ShotVideo
	{
		public Long shot_data_id;
	}
	
	public String processRequest(HttpServletRequest request) 
	{
		JSONObject jsonResponse = null;
		
		try 
		{
			printParam(request);
			
			String port = "";
			if(request.getServerPort() != 80) 
			{
				port = ":" + String.valueOf(request.getServerPort());
			}	
			String basePath = request.getScheme() + "://" + request.getServerName() + port + request.getContextPath() + "/";
			// String redirectUrl = basePath + "service/expert-data.jsp";//original
			// String redirectUrl = basePath + "service/expert-data-v2.jsp";//for demo 1.0version
			// String redirectUrl = basePath + "service/expert-data-v3.jsp";//for demo 2.0version

			// for all version
			// String redirectUrl = basePath + "service/expert-data-v" + Config.getParameter("expert") + ".jsp";
			// only for v8
			String redirectUrl = basePath + "service/expert-data-v" + Config.getParameter("expert") + "-short" + ".jsp";

			String shotData = request.getParameter("shotData");
			jsonResponse = this.checkParam(shotData);
			if (null != jsonResponse) 
			{
				return jsonResponse.toString();
			}
			
			BasicInfo info = this.getShotData(shotData);
			String result;
			if(0 < info.Player.lastIndexOf(".jp"))
			{
				result = new PSystemJP().expertAnalysis(info.shotData.BallSpeed, info.shotData.ClubAnglePath, info.shotData.ClubAngleFace, info.shotData.TotalDistFt, 
						info.shotData.CarryDistFt, info.shotData.LaunchAngle, info.shotData.SmashFactor, info.shotData.BackSpin, info.shotData.SideSpin, 
						info.shotData.ClubHeadSpeed, info.shotData.LaunchDirection, info.shotData.DistToPinFt);
			}
			else
			{
			    result = new PSystem().expertAnalysis(info.shotData.BallSpeed, info.shotData.ClubAnglePath, info.shotData.ClubAngleFace, info.shotData.TotalDistFt, 
					info.shotData.CarryDistFt, info.shotData.LaunchAngle, info.shotData.SmashFactor, info.shotData.BackSpin, info.shotData.SideSpin, 
					info.shotData.ClubHeadSpeed, info.shotData.LaunchDirection, info.shotData.DistToPinFt);
			}
			
			Expert expert = new Expert();
			if(result != null && !result.isEmpty()) {
				long shotDataId = this.saveShotData(info);
				if(shotDataId > 0) {
					JSONObject jsonExpert = new JSONObject(result);
					JSONObject jsonShotVideo = new JSONObject(result);
					
					ShotVideo shotVideo = new ShotVideo();
					expert.shot_data_id = shotDataId;
					shotVideo.shot_data_id = shotDataId;
					
					Field [] fields = expert.getClass().getDeclaredFields();
					Field [] fields2 = shotVideo.getClass().getDeclaredFields();
					for(int i=0;i<fields.length;i++) {
						if("this$0".equals(fields[i].getName())) {
							continue;
						}
						if(jsonExpert.has(fields[i].getName())){
							fields[i].set(expert, jsonExpert.get(fields[i].getName()));
						}
					}
					
					for(int i=0;i<fields2.length;i++) {
						if("this$0".equals(fields2[i].getName())) {
							continue;
						}
						if(jsonExpert.has(fields2[i].getName())){
							fields2[i].set(shotVideo, jsonShotVideo.get(fields2[i].getName()));
						}
					}
					
					long expertId = this.saveExpertData(expert);
					redirectUrl = redirectUrl + "?expert=" + expertId;
				}
			}
			
			if(jsonResponse == null) {
				jsonResponse = new JSONObject();
			}
			jsonResponse.put("success", true);
			jsonResponse.put("result", redirectUrl);
			jsonResponse.put("ball_score", calculateBallScore(info.shotData.TotalDistFt, info.shotData.LaunchDirection));
			jsonResponse.put("expert_suggestion", expert.expert_suggestion);
			jsonResponse.put("expert_cause", expert.expert_cause);
			jsonResponse.put("expert_trajectory", expert.expert_trajectory);
			
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
	
	private JSONObject checkParam(String shotData) throws Exception
	{
		JSONObject result = null;
		try 
		{
			if(shotData == null || StringUtils.trimToEmpty(shotData).isEmpty()) 
			{
				result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
			}
			else 
			{
				JSONObject main = new JSONObject(shotData);
				String [] keys = {"idx", "LID", "Player", "Date", "Token", "ShotData"};
				for(int i = 0;i<keys.length;++i) 
				{
					if(!main.has(keys[i])) 
					{
						result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
						break;
					}
				}
				if(result == null) 
				{
					for(String key : main.keySet()) 
					{
						if(main.get(key) == null || main.get(key).toString().isEmpty()) 
						{
							result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
							break;
						}
					}
					
					JSONObject sd = main.getJSONObject("ShotData");
					ShotData shd = new ShotData();
					Field [] fields = shd.getClass().getDeclaredFields();
					
					for(int i=0;i<fields.length;++i) 
					{
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
	
	private long saveExpertData(Expert expert) throws Exception{
		long id = 0;
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
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
			
			Logs.log(Logs.RUN_LOG, "strSQL: " + sql.toString());
			
			conn = DBUtil.getConnGolfMaster();
			ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
			
			cnt = 1;
			for(String key:datas.keySet()) {
				if(!"expert_update_time".equals(key)) {
					ps.setObject(cnt, datas.get(key));
					cnt++;
				}
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
	

	private double calculateBallScore(float distance, float direction) 
	{
	    if (distance > 176) {
	        distance = 176;
	    }

	    // direction 取絕對值
	    direction = Math.abs(direction);

	    // 當 direction 大於 16 時，direction 等於 16
	    if (direction > 16) {
	        direction = 16;
	    }
	    double score = ((distance / 176 + ((16 - direction) / 16) * 0.5) / 1.5) * 100;
	    return Math.ceil(Math.min(score, 100)); // 確保不超過100，並限制小數位數
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
