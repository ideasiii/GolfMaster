package com.golfmaster.service;

//http://localhost:8080/GolfMaster/service/golfmasterregister.jsp?account=1234&password=12356&nickname=joke&dexterity=1
//http://localhost:8080/GolfMaster/service/golfmasterregister.jsp?account=iak47273@cdfaq.com&password=test412345&nickname=SamOO&dexterity=1&name=阿仲&birth=1995-01-19&gender=1&tee=1&phone=0983123123&address=新北市板橋區&seniority=1&recent=2022-10-11&average=一個禮拜&score=20
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class GolfMasterRegister {

	public class gmParam {
		private int id;
		private String nickname;
		private int birth;
		private int gender;
		private int tee;
		private String address;
		private int seniority;
		private Date recent;
		private String average;
		private int score;
		private int dexterity;
	}

	public class gaParam {
		private int id;
		private String account;
		private String password;
		private int member_id;
	}

	// 註冊會員
	public String getMemberData(HttpServletRequest req)
			throws ServletException, IOException, InterruptedException {
		printParam(req);

		gmParam paramM = new gmParam();
		gaParam paramA = new gaParam();
//		註冊部分
		paramM.birth = Integer.parseInt(req.getParameter("birth"));
		paramM.gender = Integer.parseInt(req.getParameter("gender"));
		paramM.tee = Integer.parseInt(req.getParameter("tee"));
		paramM.address = req.getParameter("address");
		paramM.seniority = Integer.parseInt(req.getParameter("seniority"));
		paramM.recent = Date.valueOf(req.getParameter("recent"));
		paramM.average = req.getParameter("average");
		paramM.score = Integer.parseInt(req.getParameter("score"));
//		E6部分
		paramM.nickname = req.getParameter("nickname");
		paramM.dexterity = Integer.parseInt(req.getParameter("dexterity"));
		paramA.account = req.getParameter("account");
		paramA.password = req.getParameter("password");

		LoginE6 loginE6 = new LoginE6();
		JSONObject jsobj = new JSONObject();
		String errorType = "{\"code\":0,\"success\": true,\"message\": []}";
		int getResult = loginE6.E6Web(paramA.account, paramA.password, paramM.nickname, paramM.dexterity, req);
		switch (getResult) {

		case 1:
			jsobj.put("success", false);
			jsobj.put("code", -1);
			jsobj.put("message", "資料有誤");
			errorType = jsobj.toString();
			break;
		case 2:
			jsobj.put("success", false);
			jsobj.put("code", 1);
			jsobj.put("message", "信箱或暱稱已被註冊");
			errorType = jsobj.toString();
			break;
		case 0:
			int tempM = queryGolfMasterMember(paramM, jsobj);
			if (-1 == tempM) {
				jsobj.put("success", false);
				jsobj.put("code", -1);
				jsobj.put("message", "member新增失敗");
				errorType = jsobj.toString();
			} else if (0 == tempM) {
				jsobj.put("success", false);
				jsobj.put("code", 1);
				jsobj.put("message", "member資料重複");
				errorType = jsobj.toString();
			} else {
				int tempA = queryGolfMasterAccount(paramA, jsobj, paramM);
				if (-1 == tempA) {
					jsobj.put("success", false);
					jsobj.put("code", -1);
					jsobj.put("message", "account新增失敗");
					errorType = jsobj.toString();
				} else {
					jsobj.put("success", true);
					jsobj.put("code", 0);
					jsobj.put("message", "註冊完成");
					errorType = jsobj.toString();
				}

			}
			break;
		}
		Logs.log(Logs.RUN_LOG, errorType);
		return errorType;
	}

	// 會員登入
	public String doLogin(HttpServletRequest req) {
		printParam(req);

		gaParam paramA = new gaParam();
		paramA.account = req.getParameter("account");
		paramA.password = req.getParameter("password");

		JSONObject jsobj = new JSONObject();
		String errorType = "{\"code\":0,\"success\": true,\"message\": []}";
		if (0 < getAccountData(paramA, jsobj)) {
			insertLoginLog(paramA, 0);
			jsobj.put("success", true);
			jsobj.put("code", 0);
			jsobj.put("message", "登入成功");
			errorType = jsobj.toString();
		} else {
			insertLoginLog(paramA, -1);
			jsobj.put("success", false);
			jsobj.put("code", -1);
			jsobj.put("message", "帳號密碼錯誤");
			errorType = jsobj.toString();
		}
		Logs.log(Logs.RUN_LOG, errorType);
		return errorType;
	}

	// 輸入memeber table
	public int queryGolfMasterMember(gmParam paramM, JSONObject jsonResp) {

		int stmtRs = -1;
		Connection conn = null;
		Statement stmt = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		// ignore可以讓uq問題不報錯
		strSQL = String.format(
				"insert ignore into golf_master.member ("
						+ "nickname,birth,gender,tee,address,seniority,recent,average,score,dexterity)"
						+ "values('%s','%d','%d','%d','%s','%d','%s','%s','%d','%d')",
				paramM.nickname, paramM.birth, paramM.gender, paramM.tee, paramM.address, paramM.seniority,
				paramM.recent, paramM.average, paramM.score, paramM.dexterity);
		Logs.log(Logs.RUN_LOG, strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL, Statement.RETURN_GENERATED_KEYS);
			if (1 == stmtRs) {
				ResultSet rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					paramM.id = rs.getInt(1);
					JSONObject jsonProject = new JSONObject();
					jarrProj.put(jsonProject);
					jsonResp.put("success", true);
				}
				rs.close();
			}

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(null, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return stmtRs;
	}

	// 輸入account table
	public int queryGolfMasterAccount(gaParam paramA, JSONObject jsonResp, gmParam paramM) {
		int stmtRs = -1;

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		strSQL = String.format("insert into golf_master.account(account,password,member_id)values('%s','%s','%d')",
				paramA.account, paramA.password, paramM.id);
		Logs.log(Logs.RUN_LOG, strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL);

			JSONObject jsonProject = new JSONObject();
			jarrProj.put(jsonProject);
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return stmtRs;
	}

	// 確認登入帳號密碼
	public int getAccountData(gaParam paramA, JSONObject jsonResp) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();

		strSQL = String.format("select * from golf_master.account where account = '%s' and password = '%s'",
				paramA.account, paramA.password);
		Logs.log(Logs.RUN_LOG, strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("account", rs.getString("account"));
				jsonProject.put("password", rs.getString("password"));

				jarrProjects.put(jsonProject);
			}
			jsonResp.put("success", true);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		Logs.log(Logs.RUN_LOG, jsonResp.toString());
		return jarrProjects.length();
	}

	public int insertLoginLog(gaParam paramA, int enter) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime dateTime = LocalDateTime.now();
		String todayTime = dateTime.format(formatter).toString();

		Connection conn = null;
		Statement stmt = null;
		int stmtRs = -1;
		String strSQL;
		strSQL = String.format(
				"insert into golf_master.login_log(account,password,day,result)values('%s','%s','%s','%d')",
				paramA.account, paramA.password, todayTime, enter);
		Logs.log(Logs.RUN_LOG, strSQL);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL);
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(null, stmt, conn);
		return stmtRs;
	}

	private void printParam(HttpServletRequest req) {
		String strReq = "---Request Parameter---";
		Enumeration<?> in = req.getParameterNames();
		while (in.hasMoreElements()) {
			String paramName = in.nextElement().toString();
			String pValue = req.getParameter(paramName);
			strReq = strReq + "\n" + paramName + ":" + pValue;
		}
		Logs.log(Logs.RUN_LOG, strReq);
	}
}
