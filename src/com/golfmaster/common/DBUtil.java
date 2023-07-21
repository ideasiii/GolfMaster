package com.golfmaster.common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.dbcp2.BasicDataSource;

import com.golfmaster.tool.AesEncryptor;

public class DBUtil
{
	private static BasicDataSource	dbGolfMaster		= null;
	private static BasicDataSource	dbGolfController	= null;
	static
	{
		try
		{
			Logs.log(Logs.RUN_LOG, "datasource init...");
			Context initialContext = new InitialContext();
			dbGolfMaster = (BasicDataSource) initialContext.lookup("java:comp/env/jdbc/golfmaster");
			dbGolfController = (BasicDataSource) initialContext.lookup("java:comp/env/jdbc/golfcontroller");

			dbGolfMaster.setUsername(AesEncryptor.decrypt(dbGolfMaster.getUsername()));
			dbGolfMaster.setPassword(AesEncryptor.decrypt(dbGolfMaster.getPassword()));
			dbGolfController.setUsername(AesEncryptor.decrypt(dbGolfController.getUsername()));
			dbGolfController.setPassword(AesEncryptor.decrypt(dbGolfController.getPassword()));
		}
		catch (NamingException e)
		{
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
	}

	public static Connection getConnGolfMaster() throws Exception
	{
		return dbGolfMaster.getConnection();
	}

	public static Connection getConnGolfController() throws Exception
	{
		return dbGolfController.getConnection();
	}

	public static int closeResultSet(ResultSet rs)
	{
		try
		{
			rs.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Common.ERR_EXCEPTION;
		}
		return Common.ERR_SUCCESS;
	}

	public static int closePreparedStatement(PreparedStatement ps)
	{
		try
		{
			ps.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Common.ERR_EXCEPTION;
		}
		return Common.ERR_SUCCESS;
	}

	public static int closeStatement(Statement stme)
	{
		try
		{
			stme.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Common.ERR_EXCEPTION;
		}
		return Common.ERR_SUCCESS;
	}

	public static void close(ResultSet rs, PreparedStatement ps, Connection conn)
	{
		if (rs != null)
		{
			DBUtil.closeResultSet(rs);
		}
		if (ps != null)
		{
			DBUtil.closePreparedStatement(ps);
		}
		if (conn != null)
		{
			DBUtil.closeConn(conn);
		}
	}

	public static void close(ResultSet rs, Statement stmt, Connection conn)
	{
		if (rs != null)
		{
			DBUtil.closeResultSet(rs);
		}
		if (stmt != null)
		{
			DBUtil.closeStatement(stmt);
		}
		if (conn != null)
		{
			DBUtil.closeConn(conn);
		}
	}

	public static int closeConn(Connection conn)
	{
		try
		{
			conn.close();
			conn = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Common.ERR_EXCEPTION;
		}
		return Common.ERR_SUCCESS;
	}

}
