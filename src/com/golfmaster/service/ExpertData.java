/*
 * 顯示專家系統建議API
 * 參數: expert(必填)
 * http://localhost/GolfMaster/service/expert-data.jsp?expert=1
 * http://18.181.37.98/GolfMaster/service/expert-data.jsp?expert=1
 * http://localhost:8080/GolfMaster/service/expert-data.jsp?expert=1
 * http://61.216.149.161/GolfMaster/service/expert-data-v2.jsp?expert=11349
 */
package com.golfmaster.service;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;

public class ExpertData extends DeviceData{
	
	@SuppressWarnings("unused")
	private class Expert {
		
		public Long shot_data_id;
		public String expert_trajectory;
		public String expert_p_system;
		public String expert_suggestion;
		public String expert_cause;
		public String expert_update_time;
	}
	
	
	
	public JSONObject processRequest(HttpServletRequest request) {
		JSONObject expert = null;
//		String strResponse = "{\"success\": true,\"result\": []}";
		
		try {
			printParam(request);
			
			String expertID = request.getParameter("expert");
			if(expertID != null) {
				expert = this.queryExpertData(Long.parseLong(expertID));
				
//				String imgFile = "";
				boolean result = false;
				String psystem = expert.getString("expert_p_system");
				if(psystem != null && !psystem.isEmpty()) {
					if(psystem.toUpperCase().indexOf("P") >= 0) {
						String vf = "";
						String p = psystem.substring(0, 2);
						if("P1".equals(p) || "P2".equals(p) || "P3".equals(p) || "P4".equals(p)) {
//							vf = "P1_P4.mp4";
							vf = "P1_P10v5.mp4";
						}
						if("P5".equals(p) || "P6".equals(p) || "P7".equals(p) ) {
//							vf = "P5_P7.mp4";
							vf = "P1_P10v5.mp4";
						}
						if("P8".equals(p) || "P9".equals(p) || "P10".equals(p)) {
//							vf = "P8_P10.mp4";
							vf = "P1_P10v5.mp4";
						}
						
						result = true;
						expert.put("video", vf);
					}else {
						if(psystem.contains("擊球失敗")) {
//							imgFile = "warning.png";
							expert.put("img_name", "warning.png");
						}
					}
				}
				
//				expert.put("img_name", imgFile);
				expert.put("result", result);
			}
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
				jsonExpert.put("shotdata_id", rs.getLong("shot_data_id"));
				
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
