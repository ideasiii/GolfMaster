/*
 * 個人碼數表API
 * 參數: player(必填)
 * http://localhost/GolfMaster/service/yardage.jsp?player=Guest.1&type=month&start_date=2023-01-01&end_date=2023-08-02
 *
 */

package com.golfmaster.service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.YardageBook;


public class YardageBookData extends Service
{
	
	public String processRequest(HttpServletRequest request)
	{
		JSONObject jsonResponse = new JSONObject();
		initResponse(jsonResponse);
		Logs.log(Logs.RUN_LOG, printParam(request));
		
		try {
			Map<String, Object> reqData = this.getRequestMap(request);
			if(this.checkParams(reqData, jsonResponse)) {
				YardageBook ydb = new YardageBook();
				YardageBook.ParamData paramData = ydb.new ParamData(reqData.get("player").toString(), (reqData.get("start_date") == null ? "":reqData.get("start_date").toString()), (reqData.get("end_date") == null ? "":reqData.get("end_date").toString()), 100.0, 650.0);
				List<YardageBook.YardageData> list = ydb.getYardageBook(paramData);
				
				if(!list.isEmpty()) {
					JSONArray array = new JSONArray();
					for(YardageBook.YardageData data : list) {
						JSONObject jobj = new JSONObject();
						jobj.put("ClubType", data.getClubType());
						jobj.put("max", data.getCarryDistFtMax());
						jobj.put("avg", data.getCarryDistFtAvg());
						jobj.put("min", data.getCarryDistFtMin());
						
						array.put(jobj);
					}
					
					jsonResponse.put("result", array);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			errorResponse(jsonResponse, -10, "processing failed");
		}
		
		return jsonResponse.toString();
	}
	
	private boolean checkParams(Map<String, Object> reqData, JSONObject jsonResponse)
	{
		boolean bResult = false;
		
		String player = StringUtils.trimToEmpty((reqData.get("player") == null ? "":reqData.get("player").toString()));
		String start_date = StringUtils.trimToEmpty((reqData.get("start_date") == null ? "":reqData.get("start_date").toString()));
		String end_date = StringUtils.trimToEmpty((reqData.get("end_date") == null ? "":reqData.get("end_date").toString()));
		
		if (StringUtils.isEmpty(player)) 
		{
			errorResponse(jsonResponse, ERROR_PARAMETER, errorMessage.get(ERROR_PARAMETER));
		}
		else
		{
			if(!start_date.isEmpty()) {
				if(!isValidDate(start_date)) {
					errorResponse(jsonResponse, ERROR_PARAMETER_DATE_FORMAT, errorMessage.get(ERROR_PARAMETER_DATE_FORMAT));
				}
			}
			
			if(!end_date.isEmpty()) {
				if(!isValidDate(end_date)) {
					errorResponse(jsonResponse, ERROR_PARAMETER_DATE_FORMAT, errorMessage.get(ERROR_PARAMETER_DATE_FORMAT));
				}
			}
		}
		
		if(jsonResponse.getBoolean("success")) {
			bResult = true;
		}
		
		return bResult;
	}
	
	private Map<String, Object> getRequestMap(HttpServletRequest request) throws Exception
	{
		Map<String, Object> requestMap = new HashMap<String, Object>();
		Enumeration enumeration = request.getParameterNames();
		String name = null;
		String[] values = null;
		
		while (enumeration.hasMoreElements()) {
			name = String.valueOf(enumeration.nextElement());
			values = request.getParameterValues(name);
			if(request.getMethod().equals("GET")){
				for(int i=0;i<values.length;i++){
					values[i] = new String(values[i].getBytes("ISO-8859-1"), "UTF-8");
				}
			}
			if (values.length == 1) {
				requestMap.put(name, values[0]);
			} else {
				requestMap.put(name, values);
			}
		}
		
		return requestMap;
	}
}
