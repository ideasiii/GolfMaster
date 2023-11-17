/*
 * 串接工研院之即時擊球數據API
 * 參數: shotData(必填)
 * http://localhost/GolfMaster/service/irit-shotdata.jsp
 * http://61.216.149.161/GolfMaster/service/irit-shotdata.jsp
 * http://localhost:8080/GolfMaster/service/irit-shotdata.jsp
 */
package com.golfmaster.service;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
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

public class IritShotData extends DeviceData{
	
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
			
			String redirectUrl = basePath + "service/expert-data-v" + Config.getParameter("expert") + ".jsp";
			
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
					
					long expertId = this.saveExpertData(expert);
					
					redirectUrl = redirectUrl + "?expert=" + expertId;
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
				String [] keys = {"BallSpeed", "LaunchAngle", "BackSpin", "SideSpin", "LaunchDirection"};
				for(int i = 0;i<keys.length;++i) 
				{
					if(!main.has(keys[i])) 
					{
						result = ApiResponse.error(ApiResponse.STATUS_MISSING_PARAMETER);
						break;
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
			info.idx = "00001";
			info.LID = "100";
			info.Player = "irit";
			info.Date = String.valueOf(new Date().getTime());
			info.Token = Base64.getEncoder().encodeToString((info.Date + info.Player).getBytes("UTF-8"));
			
			JSONObject shotJsonData = new JSONObject(jsonStr);
			shotJsonData.put("ClubType", "7Iron");
			
			shotJsonData = this.getStandarShotData(shotJsonData);
			
			Field [] fields = shotData.getClass().getDeclaredFields();
			
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
			throw e;
		}
		
		return info;
	}
	
	private JSONObject getStandarShotData(JSONObject shotJsonData) throws Exception{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sql = new StringBuffer();
			sql.append("select ClubType, round(avg(ClubAnglePath),4) as 'ClubAnglePath_avg', round(avg(ClubAngleFace),4) as 'ClubAngleFace_avg', round(avg(TotalDistFt),4) as 'TotalDistFt_avg',");
			sql.append(" round(avg(CarryDistFt),4) as 'CarryDistFt_avg', round(avg(SmashFactor),4) as 'SmashFactor_avg', round(avg(ClubHeadSpeed),4) as 'ClubHeadSpeed_avg', round(avg(DistToPinFt),4) as 'DistToPinFt_avg'");
			sql.append(" from golf_master.shot_data where player = 'Guest.1' and ClubType = '" + shotJsonData.getString("ClubType") + "' ");
			
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			
			rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				shotJsonData.put("ClubAnglePath", rs.getDouble("ClubAnglePath_avg"));
				shotJsonData.put("ClubAngleFace", rs.getDouble("ClubAngleFace_avg"));
				shotJsonData.put("TotalDistFt", rs.getDouble("TotalDistFt_avg"));
				shotJsonData.put("CarryDistFt", rs.getDouble("CarryDistFt_avg"));
				shotJsonData.put("SmashFactor", rs.getDouble("SmashFactor_avg"));
				shotJsonData.put("ClubHeadSpeed", rs.getDouble("ClubHeadSpeed_avg"));
				shotJsonData.put("DistToPinFt", rs.getDouble("DistToPinFt_avg"));
			}
		}catch(Exception e) {
			throw e;
		}finally {
			DBUtil.close(rs, stmt, conn);
		}
		
		return shotJsonData;
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
