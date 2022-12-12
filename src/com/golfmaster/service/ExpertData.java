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

public class ExpertData extends DeviceData{
	
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
	
	
	
	public JSONObject processRequest(HttpServletRequest request) {
		JSONObject expert = null;
//		String strResponse = "{\"success\": true,\"result\": []}";
		
		try {
			printParam(request);
			
			String expertID = request.getParameter("expert");
			expert = this.queryExpertData(Long.parseLong(expertID));
		}catch(Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			
			
		}
		
		Logs.log(Logs.RUN_LOG, "Response : " + (expert != null ? expert:null));
		
		return (expert != null ? expert:null);
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

	private JSONObject queryExpertData(long ExpertId){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONObject jsonExpert = null;		
		
		strSQL = "SELECT * FROM expert WHERE id = " + ExpertId;
		
		Logs.log(Logs.RUN_LOG, "strSQL: " + strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()){
				jsonExpert = new JSONObject();
				
				jsonExpert.put("expert_trajectory", rs.getString("expert_trajectory"));
				jsonExpert.put("expert_note", rs.getString("expert_note"));
				jsonExpert.put("expert_p_system", rs.getString("expert_p_system"));
				jsonExpert.put("expert_suggestion", rs.getString("expert_suggestion"));
				jsonExpert.put("expert_cause", rs.getString("expert_cause"));
				
			}
			
		} catch (Exception e){
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
		
		DBUtil.close(rs, stmt, conn);
		
		return jsonExpert;
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
