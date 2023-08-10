package com.golfmaster.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Config 
{
	//======== Web Driver =============================
	 static public int HEADLESS = 1;
	 
	 /**
	  * 讀取context.xml參數
	  * @param element : Environment name
	  * @return : Environment value
	  */
	 public static String getParameter(String element)
	 {
		 String strValue = null;
		try 
		{
			InitialContext context;
			context = new InitialContext();
			Context xmlNode = (Context) context.lookup("java:comp/env");
			strValue = (String) xmlNode.lookup(element);
		} 
		catch (NamingException e) 
		{
			e.printStackTrace();
		}
		return strValue;
	 }
}
