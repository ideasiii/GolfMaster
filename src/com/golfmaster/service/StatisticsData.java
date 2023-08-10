/*
 * 計算擊球數據及的各項統計API
 * 參數: player(必填),type(必填)
 * http://localhost/GolfMaster/service/statistics.jsp?player=Guest.1&type=month&start_date=2023-01-01&end_date=2023-08-02
 * http://61.216.149.161/GolfMaster/service/expert-data-v2.jsp?expert=11349
 */

package com.golfmaster.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;



public class StatisticsData extends Service
{	
	private class ShotData
	{
		private String fieldName;
		private double mix;
		private double max;
		public ShotData(final String strField, final double dMix, final double dMax)
		{
			fieldName = strField;
			mix = dMix;
			max = dMax;
		}
	}
	
	ArrayList<StatisticsData.ShotData> listShotField = new ArrayList<StatisticsData.ShotData>(
			Arrays.asList(new ShotData("BallSpeed", 10, 350),
            		new ShotData("TotalDistFt", 10, 350),
            		new ShotData("LaunchDirection", -90, 90)));
	private void printListShot()
	{
		for(ShotData shotData : listShotField)
		{
			System.out.println(shotData.fieldName + " " + shotData.mix + " " + shotData.max);
		}
	}
	
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
		printListShot();
		
		JSONObject jsonResponse = new JSONObject();
		initResponse(jsonResponse);
		Logs.log(Logs.RUN_LOG, printParam(request));
		
		StatisticsData.ParamData paramData = new StatisticsData.ParamData();
		if(requestAndTrimParams(request, paramData, jsonResponse))
		{
			queryShotData(paramData, jsonResponse);
		}
			
		return jsonResponse.toString();
	}
	
	private int queryShotData(ParamData paramData, JSONObject jsonResponse)
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
			strSQL = getStatisticsSQL(paramData, "BallSpeed", 300, 10);
			if(null != strSQL)
			{
				System.out.println("SQL : " + strSQL);
				rs = stmt.executeQuery(strSQL);
				while (rs.next()) 
				{
					JSONObject jsonRecord = new JSONObject();
					jsonRecord.put("year", rs.getInt("year"));
					if(0 == paramData.type.compareTo("week"))
						jsonRecord.put("week", rs.getInt("week"));
					if(0 == paramData.type.compareTo("month"))
						jsonRecord.put("month", rs.getInt("month"));
					jsonRecord.put("mix", rs.getDouble("mix"));
					jsonRecord.put("avg", rs.getDouble("avg"));
					jsonRecord.put("max", rs.getDouble("max"));
					jsonResponse.getJSONArray("result").put(jsonRecord);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return 0;
	}
	
	private String getStatisticsSQL(ParamData paramData, String shotField, float maxValue, float minValue)
	{
		String strSQL = null;
		
		if(0 == paramData.type.compareTo("week"))
		{
			strSQL = String.format("select year(Date) as 'year',week(Date) as 'week', min(%s) as 'mix', "
					+ "round(avg(%s),4) as 'avg', max(%s) as 'max' from golf_master.shot_data "
					+ "where %s > %f and %s < %f and Date >= '%s' and Date <= '%s' and Player = '%s' "
					+ "group by year(Date),week(Date) order by 1", shotField, shotField,shotField,
					shotField,minValue,shotField,maxValue,paramData.start_date,paramData.end_date, paramData.player);
		}
		
		if(0 == paramData.type.compareTo("month"))
		{
			strSQL = String.format("select year(Date) as 'year',month(Date) as 'month', min(%s) as 'mix', "
					+ "round(avg(%s),4) as 'avg', max(%s) as 'max' from golf_master.shot_data "
					+ "where %s > %f and %s < %f and Date >= '%s' and Date <= '%s' and Player = '%s' "
					+ "group by year(Date),month(Date) order by 1", shotField, shotField,shotField,
					shotField,minValue,shotField,maxValue,paramData.start_date,paramData.end_date, paramData.player);
		}
		
		return strSQL;
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
			errorResponse(jsonResponse, ERROR_PARAMETER, errorMessage.get(ERROR_PARAMETER));
		}
		else
		{
			System.out.println("type=" + paramData.type);
			if( 0 != paramData.type.compareTo("month") && 0 != paramData.type.compareTo("week"))
			{
				errorResponse(jsonResponse, ERROR_PARAMETER, errorMessage.get(ERROR_PARAMETER) + " type錯誤");
			}
			else
			{
				if(StringUtils.isEmpty(paramData.start_date) || StringUtils.isEmpty(paramData.end_date))
				{
					// 取目前年
					Calendar calendar = Calendar.getInstance();
					paramData.start_date = String.format("%d-01-01",calendar.get(Calendar.YEAR));
					paramData.end_date = String.format("%d-12-31",calendar.get(Calendar.YEAR));
				}
				
				if(!isValidDate(paramData.start_date) || !isValidDate(paramData.end_date))
				{
					errorResponse(jsonResponse, ERROR_PARAMETER_DATE_FORMAT, errorMessage.get(ERROR_PARAMETER_DATE_FORMAT));
				}
				else
					bResult = true;
			}
		}
		
		return bResult;
	}

	
}
