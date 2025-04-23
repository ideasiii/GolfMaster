package com.golfmaster.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Logs
{
	public static final String	SQL_LOG			= "sql_log.txt";
	public static final String	RUN_LOG			= "run_log.txt";
	public static final String	DEBUG			= "debug_log.txt";
	public static final String	EXCEPTION_LOG	= "exception_log.txt";
//	伺服器路徑
	public static final String	PATH_LOG		= "/logs/golfmaster/";
//	本機路徑
//	public static final String	PATH_LOG ="D:\\logs\\golfmaster";
	public static void log(String strPath, String strMsg)
	{
		Throwable throwable = new Throwable();
		String strClassPath = throwable.getStackTrace()[1].getClassName();
		String strLogPath = strClassPath + "_" + strPath;
		ThreadLog tlog = new ThreadLog(strLogPath, strMsg);
		tlog.start();
		// String strPathLog = PATH_LOG + GetDate() + "_" + strLogPath;
		// String strLogText = GetNow() + " : " + strMsg;
		// logger(strPathLog, strLogText);
	}

	public static void error(String strMsg)
	{
		Throwable throwable = new Throwable();
		String strFileName = throwable.getStackTrace()[1].getFileName();
		String strClassPath = throwable.getStackTrace()[1].getClassName();
		String strClassName = extractSimpleClassName(strClassPath);
		String strMethod = throwable.getStackTrace()[1].getMethodName();
		int nLine = throwable.getStackTrace()[1].getLineNumber();

		String strPath = PATH_LOG + GetDate() + "_" + "error.txt";
		String strDate = "====" + GetNow() + "====" + strFileName + ":" + strClassName + ":" + strMethod + ":" + nLine;

		try (FileWriter fw = new FileWriter(strPath, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw))
		{
			out.println(strDate);
			out.println(strMsg);
			System.out.println(strMsg);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String extractSimpleClassName(String fullClassName)
	{
		if ((null == fullClassName) || ("".equals(fullClassName)))
		{
			return "";
		}
		int lastDot = fullClassName.lastIndexOf('.');
		if (0 > lastDot)
			return fullClassName;
		return fullClassName.substring(++lastDot);
	}

	public static String GetDate()
	{
		String strDate;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd");
		strDate = timeformat.format(cal.getTime());
		return strDate;
	}

	public static String GetNow()
	{
		String strDate;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		strDate = timeformat.format(cal.getTime());
		return strDate;
	}

	public static class ThreadLog extends Thread
	{
		private String	m_strPath;
		private String	m_strMsg;

		public ThreadLog(String strPath, String strMsg) {
			this.m_strPath = strPath;
			this.m_strMsg = strMsg;
		}

		public void run()
		{

			String strPathLog = PATH_LOG + GetDate() + "_" + this.m_strPath;
			String strLogText = GetNow() + " : " + this.m_strMsg;
			logger(strPathLog, strLogText);
		}
	}

	synchronized public static void logger(String strPath, String strMsg) // synchronized
																			// ?
	{
		try (FileWriter fw = new FileWriter(strPath, true);
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(strPath, true), StandardCharsets.UTF_8));
				PrintWriter out = new PrintWriter(bw))
		{
			out.println(strMsg);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
