/*
 * 計算擊球數據及的各項統計API
 * 參數: (必填)
 * http://localhost/GolfMaster/service/expert-data.jsp?expert=1
 * http://61.216.149.161/GolfMaster/service/expert-data-v2.jsp?expert=11349
 */

package com.golfmaster.service;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
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
		
		ParamData paramData = new ParamData();
		
		return jsonResponse.toString();
	}
	
	

}
