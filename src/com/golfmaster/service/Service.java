package com.golfmaster.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;



public abstract class Service 
{
	public final static int ERROR_PARAMETER = -1;
	public final static int ERROR_PARAMETER_DATE_FORMAT = -2;
	public final static int ERROR_PARAMETER_DB_CONNECT = -3;
	public final static HashMap<Integer, String> errorMessage  = new HashMap<Integer, String>() 
	{
		private static final long serialVersionUID = 1L;
	{
	    put(ERROR_PARAMETER, "參數不正確");
	    put(ERROR_PARAMETER_DATE_FORMAT, "日期格式不正確");
	    put(ERROR_PARAMETER_DB_CONNECT, "資料庫連結失敗");
	}};
	
	
	abstract public String processRequest(HttpServletRequest request);
	
	protected void initResponse(JSONObject jsonResponse)
	{
		if(null == jsonResponse)
			jsonResponse = new JSONObject();
		
		jsonResponse.put("success", true);
		jsonResponse.put("result", new JSONArray());
		jsonResponse.put("code",0);
		jsonResponse.put("message", "");
	}
	
	protected String printParam(HttpServletRequest request) 
	{
		String strRequest = " =========== Request Parameter ============";
		Enumeration<?> in = request.getParameterNames();
		while (in.hasMoreElements()) 
		{
			String paramName = in.nextElement().toString();
			String pValue = request.getParameter(paramName);
			strRequest = strRequest + "\n" + paramName + " : " + pValue;
		}
		return strRequest;
	}
	
	protected boolean isValidDate(String dateStr) 
	{
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try 
        {
            sdf.parse(dateStr);
        } 
        catch (ParseException e) 
        {
            return false;
        }
        return true;
    }
	
	protected void errorResponse(JSONObject jsonResponse, int nCode, String strMessage)
	{
		jsonResponse.put("success", false);
		jsonResponse.put("code", nCode);
		jsonResponse.put("message", strMessage);
	}

}
