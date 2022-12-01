package com.golfmaster.service;

//http://localhost:8080/GolfMaster/service/golfmasterlogin.jsp?account=1234&password=12356&nickname=joke&dexterity=1
import java.io.IOException;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GolfMasterLogin {

	public class gmParam {
		private int id;
		private String name;
		private String nickname;
		private String birth;
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
		private int id;
		private String account;
		private String password;
		private int member_id;
	}

	public void getMemberData(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		gmParam paramMember = new gmParam();

//		paramMember.name = req.getParameter("name");
		paramMember.nickname = req.getParameter("nickname");
//		paramMember.birth = req.getParameter("birth");
//		paramMember.gender = Integer.parseInt(req.getParameter("gender"));
//		paramMember.tee = Integer.parseInt(req.getParameter("tee"));
//		paramMember.phone = req.getParameter("phone");
//		paramMember.address = req.getParameter("address");
//		paramMember.seniority = Integer.parseInt(req.getParameter("seniority"));
//		paramMember.recent = Date.valueOf(req.getParameter("recent"));
//		paramMember.average = req.getParameter("average");
//		paramMember.score = Integer.parseInt(req.getParameter("score"));
		paramMember.dexterity = Integer.parseInt(req.getParameter("dexterity"));

		gaParam paramAccount = new gaParam();

		paramAccount.account = req.getParameter("account");
		paramAccount.password = req.getParameter("password");


		if (req.getParameter("wrong") == "true") {
			System.out.println("E6資料有誤");
		}else {
			resp.sendRedirect("logine6.jsp" + "?" + "account=" + paramAccount.account + "&password="
					+ paramAccount.password + "&nickname=" + paramMember.nickname + "&dexterity="
					+ String.valueOf(paramMember.dexterity));
			
		}
	}
}
