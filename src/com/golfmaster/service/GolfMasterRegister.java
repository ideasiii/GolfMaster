package com.golfmaster.service;

//http://localhost:8080/GolfMaster/service/golfmasterregister.jsp?account=1234&password=12356&nickname=joke&dexterity=1
//http://localhost:8080/GolfMaster/service/golfmasterregister.jsp?account=iak47273@cdfaq.com&password=test412345&nickname=SamOO&dexterity=1&name=阿仲&birth=1995-01-19&gender=1&tee=1&phone=0983123123&address=新北市板橋區&seniority=1&recent=2022-10-11&average=一個禮拜&score=20
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class GolfMasterRegister {

	public class gmParam {
		private int id;
		private String name;
		private String nickname;
		private Date birth;
		private int gender;
		private int tee;
		private String phone;
		private String address;
		private int seniority;
		private Date recent;
		private String average;
		private int score;
		private int dexterity;
	}

	public class gaParam {
		private String account;
		private String password;
		private int member_id;
	}

	public String getMemberData(HttpServletRequest req) throws ServletException, IOException, InterruptedException {
		printParam(req);

		gmParam paramM = new gmParam();
		gaParam paramA = new gaParam();
//		註冊部分
		paramM.name = req.getParameter("name");
		paramM.birth = Date.valueOf(req.getParameter("birth"));
		paramM.gender = Integer.parseInt(req.getParameter("gender"));
		paramM.tee = Integer.parseInt(req.getParameter("tee"));
		paramM.phone = req.getParameter("phone");
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
		String errorType = "{\"success\": true,\"result\": []}";
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
			if (-1 == queryGolfMasterMember(paramM, jsobj)) {
				jsobj.put("success", false);
				jsobj.put("code", -1);
				jsobj.put("message", "member新增失敗");
				errorType = jsobj.toString();
			} else if (0 == queryGolfMasterMember(paramM, jsobj)) {
				jsobj.put("success", false);
				jsobj.put("code", 1);
				jsobj.put("message", "member資料重複");
				errorType = jsobj.toString();
			} else {
				if (-1 == queryGolfMasterAccount(paramA, jsobj, paramM)) {
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

	public int queryGolfMasterMember(gmParam paramM, JSONObject jsonResp) {

		int stmtRs = -1;
		Connection conn = null;
		Statement stmt = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		jsonResp.put("result", jarrProj);
		// ignore可以讓uq問題不報錯
		strSQL = String.format(
				"insert ignore into golf_master.member ("
						+ "name,nickname,birth,gender,tee,phone,address,seniority,recent,average,score,dexterity)"
						+ "values('%s','%s','%s','%d','%d','%s','%s','%d','%s','%s','%d','%d')",
				paramM.name, paramM.nickname, paramM.birth, paramM.gender, paramM.tee, paramM.phone, paramM.address,
				paramM.seniority, paramM.recent, paramM.average, paramM.score, paramM.dexterity);
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
					jsonProject.put("insertResult", stmtRs);
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
		jsonResp.put("result", jarrProj);

		return stmtRs;
	}

	public int queryGolfMasterAccount(gaParam paramA, JSONObject jsonResp, gmParam paramM) {
		int stmtRs = -1;

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL;
		JSONArray jarrProj = new JSONArray();

		jsonResp.put("result", jarrProj);

		strSQL = String.format("insert into golf_master.account(account,password,member_id)values('%s','%s','%d')",
				paramA.account, paramA.password, paramM.id);
		Logs.log(Logs.RUN_LOG, strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			stmtRs = stmt.executeUpdate(strSQL);

			JSONObject jsonProject = new JSONObject();
			jsonProject.put("insertResult", stmtRs);
			jarrProj.put(jsonProject);
			jsonResp.put("success", true);

		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
			jsonResp.put("success", false);
			jsonResp.put("message", e.getMessage());
		}
		DBUtil.close(rs, stmt, conn);
		jsonResp.put("result", jarrProj);

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
