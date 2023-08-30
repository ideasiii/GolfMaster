/*
 * 資料庫帳號相關API
 * 參數: 依不同呼叫的函式而不同
 * http://localhost/GolfMaster/service/check-player.jsp?player=Guest.1
 * http://61.216.149.161/GolfMaster/service/check-account.jsp?player=Guest.1
 * 
 */
package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;


public class AccountManager extends Service
{

	/*
	 * API參數
	 */
	private class ParamData 
	{
		//private String account;
		//private String password;
		private String player;
	}
	
	@Override
	public String processRequest(HttpServletRequest request) 
	{
		Logs.log(Logs.RUN_LOG, printParam(request));
		return null;
	}
	
	/**
	 * 查詢shot_data是否有此player
	 * @param player
	 * @return json response
	 */
	public String checkPlayer(HttpServletRequest request)
	{
		JSONObject jsonResponse = new JSONObject();
		initResponse(jsonResponse);
		Logs.log(Logs.RUN_LOG, printParam(request));
		
		AccountManager.ParamData paramData = new AccountManager.ParamData();
		if(requestAndTrimParams(request, paramData, jsonResponse))
		{
			queryPlayer(paramData, jsonResponse);
		}
		
		return jsonResponse.toString();
	}
	
	private boolean requestAndTrimParams(HttpServletRequest request, ParamData paramData, JSONObject jsonResponse)
	{
		boolean bResult = false;
		
		paramData.player = StringUtils.trimToEmpty(request.getParameter("player"));
		if (StringUtils.isEmpty(paramData.player)) 
		{
			errorResponse(jsonResponse, ERROR_PARAMETER, errorMessage.get(ERROR_PARAMETER));
		}
		else
		{
			System.out.println("player=" + paramData.player);
			bResult = true;
		}
		return bResult;
	}
	
	private int queryPlayer(ParamData paramData, JSONObject jsonResponse)
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		
		try
		{
			conn = DBUtil.getConnGolfMaster();
			
			if(null == conn)
			{
				errorResponse(jsonResponse, ERROR_PARAMETER, errorMessage.get(ERROR_PARAMETER));
				return 0;
			}
			stmt = conn.createStatement();
			strSQL = String.format("SELECT count(1) as player FROM golf_master.shot_data WHERE Player = '%s'", paramData.player);
			if(null != strSQL)
			{
				Logs.log(Logs.RUN_LOG, "SQL : " + strSQL);
				rs = stmt.executeQuery(strSQL);
				
				JSONObject jsonRecord = new JSONObject();
				if (rs.next()) 
				{
					jsonRecord.put("player", rs.getInt("player"));
				}
				else
				{
					jsonRecord.put("player", 0);
				}
				
				jsonResponse.getJSONArray("result").put(jsonRecord);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return 0;
	}
	
}
