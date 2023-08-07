/*
 * 計算擊球數據及的各項統計API
 * 參數: (必填)
 * http://localhost/GolfMaster/service/statistics.jsp?player=Guest.1&type=month&start_date=2023-01-01&end_date=2023-08-02
 * http://61.216.149.161/GolfMaster/service/expert-data-v2.jsp?expert=11349
 */

package com.golfmaster.service;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.golfmaster.common.ApiResponse;
import com.golfmaster.common.Logs;

public class StatisticsData extends Service
{
	private class ParamData 
	{
		private String player;
		private String type;
		private String start_date;
		private String end_date;
	}
	
	public StatisticsData()
	{
		
	}
	
	protected void finalize()
	{
		
	}
	
	public String processRequest(HttpServletRequest request)
	{
		JSONObject jsonResponse = new JSONObject();
		initResponse(jsonResponse);
		Logs.log(Logs.RUN_LOG, printParam(request));
		
		StatisticsData.ParamData paramData = new StatisticsData.ParamData();
		if(requestAndTrimParams(request, paramData, jsonResponse))
		{
			
		}
		
		return jsonResponse.toString();
	}
	
	private boolean requestAndTrimParams(HttpServletRequest request, ParamData paramData, JSONObject jsonResponse)
	{
		boolean bResult = false;
		
		paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
		paramData.type = StringUtils.trimToEmpty(request.getParameter("type"));
		paramData.start_date = StringUtils.trimToEmpty(request.getParameter("start_date"));
		paramData.end_date = StringUtils.trimToEmpty(request.getParameter("end_date"));
		
		if (StringUtils.isEmpty(paramData.player) || StringUtils.isEmpty(paramData.type)) 
		{
			jsonResponse.put("success", false);
			jsonResponse.put("code", -1);
			jsonResponse.put("message", "參數不正確");
		}
		else
		{
			System.out.println("type=" + paramData.type);
			if( 0 != paramData.type.compareTo("month") && 0 != paramData.type.compareTo("week"))
			{
				jsonResponse.put("success", false);
				jsonResponse.put("code", -1);
				jsonResponse.put("message", "type參數不正確");
			}
			else
			{
				if(StringUtils.isEmpty(paramData.start_date) || StringUtils.isEmpty(paramData.end_date))
				{
					// 取當年
					Calendar calendar = Calendar.getInstance();
					paramData.start_date = String.format("%d-01-01",calendar.get(Calendar.YEAR));
					paramData.end_date = String.format("%d-12-31",calendar.get(Calendar.YEAR));
				}
				
				if(!isValidDate(paramData.start_date) || !isValidDate(paramData.end_date))
				{
					jsonResponse.put("success", false);
					jsonResponse.put("code", -2);
					jsonResponse.put("message", "日期格式不正確");
				}
				else
					bResult = true;
			}
		}
		
		return bResult;
	}

}
