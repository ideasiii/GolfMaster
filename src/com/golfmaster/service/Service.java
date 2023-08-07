package com.golfmaster.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;



public abstract class Service 
{
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

}
