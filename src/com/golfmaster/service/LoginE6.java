/*
 * http://localhost:8080/GolfMaster/service/logintest.jsp
 * http://localhost:8080/GolfMaster/service/logintest.jsp?account=uwk51642@nezid.com&password=as45770437&nickname=Sam&dexterity=1
 * */

package com.golfmaster.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.golfmaster.common.Logs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LoginE6 {

	public class gaParam {
		private int id;
		private String account;
		private String password;
		private int member_id;
	}

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

	public void E6Web(HttpServletRequest req, HttpServletResponse resp) throws InterruptedException, IOException {
		printParam(req);

		gaParam paramA = new gaParam();
		gmParam paramM = new gmParam();
		paramA.account = req.getParameter("account");
		paramA.password = req.getParameter("password");
		paramM.nickname = req.getParameter("nickname");
		paramM.dexterity = Integer.parseInt(req.getParameter("dexterity"));
		System.out.println("E6 Crawl Run");
//		伺服器175.41.245.90為linux
//		System.setProperty("webdriver.chrome.driver", "/opt/web_driver/chromedriver");
//		本機windows
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\P22361\\eclipse-workspace\\GolfMaster\\src\\com\\golfmaster\\driver\\chromedriver.exe");

		final ChromeOptions chromeOpt = new ChromeOptions();

//		給server用的屬性
//		chromeOpt.addArguments("--no-sandbox");
//		chromeOpt.addArguments("--headless");
//		chromeOpt.addArguments("--disable-dev-shm-usage");

		WebDriver driverE6 = new ChromeDriver(chromeOpt);
		driverE6.get("https://portal.e6golf.com/signup");

		// 信箱欄
		WebElement emailBar = driverE6.findElement(By.id("input-27"));
		emailBar.sendKeys(paramA.account);
		Logs.log(Logs.RUN_LOG, "E6mail: " + paramA.account);

		// 確認信箱欄
		WebElement confirmEmailBar = driverE6.findElement(By.id("input-30"));
		confirmEmailBar.sendKeys(paramA.account);
		Logs.log(Logs.RUN_LOG, "confirmEmail: " + paramA.account);

		// 密碼欄
		WebElement passwordBar = driverE6.findElement(By.id("input-33"));
		passwordBar.sendKeys(paramA.password);
		Logs.log(Logs.RUN_LOG, "E6password: " + paramA.password);

		// 確認密碼欄
		WebElement confirmPasswordBar = driverE6.findElement(By.id("input-37"));
		confirmPasswordBar.sendKeys(paramA.password);
		Logs.log(Logs.RUN_LOG, "confirmPassword: " + paramA.password);

		// 暱稱欄
		WebElement displayNameBar = driverE6.findElement(By.id("input-41"));
		displayNameBar.sendKeys(paramM.nickname);
		Logs.log(Logs.RUN_LOG, "E6displayName: " + paramM.nickname);

		// 列表國家
		WebElement countryScroll = driverE6.findElement(By.id("input-44"));
		countryScroll.sendKeys("Taiwan");

		// 列表國家台灣選項
		WebElement countrySR = driverE6.findElement(By.id("list-44"));
		countrySR.click();
		// 列表慣用手
		WebElement dexterityScroll = driverE6.findElement(By.id("input-49"));
		dexterityScroll.click();
		if (paramM.dexterity == 1 && paramM.dexterity > 0) {
			// 列表右手選項
			WebElement dexterityRight = driverE6.findElement(By.id("list-item-151-0"));
			dexterityRight.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Right");
		} else if (paramM.dexterity == 2 && paramM.dexterity > 0) {
			// 列表左手選項
			WebElement dexterityLeft = driverE6.findElement(By.id("list-item-70-1"));
			dexterityLeft.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Left");
		}
		// 同意合約框
		WebElement checkbox = driverE6
				.findElement(By.xpath("//*[@id=\"app\"]/div/div[1]/div/div/div/div[2]/div/form/div[8]/div/div[1]/div"));
		checkbox.click();

		System.out.println(driverE6.findElements(By.cssSelector("div [class='v-messages__wrapper']")).size());
		List<WebElement> wrongMessage = driverE6.findElements(By.cssSelector("div [class='v-messages__wrapper']"));
		String noWord = "";

		ArrayList<Object> showWrongMessage = new ArrayList<>();
		int j = 0;
		for (int i = 0; i < wrongMessage.size(); i++) {
			System.out.println(wrongMessage.get(i).getText().toString() + i);
			if (wrongMessage.get(i).getText().toString().length() != noWord.length()) {
				showWrongMessage.add(j, wrongMessage.get(i).getText().toString());
				j++;
			}
		}
		//記錄錯誤的bar
		for (Object message : showWrongMessage) {
			if (message.equals(showWrongMessage.indexOf(message))) {
				System.out.println(message.toString());

			}
			Logs.log(Logs.RUN_LOG, "錯誤項目:" + message.toString());
		}
		if (showWrongMessage.size() > 0) {
			System.out.println("有錯誤");
//			resp.sendRedirect("golfmasterlogin.jsp" + "?wrong=true");
		} else {

			// 註冊按鈕
			WebElement signupBtn = driverE6
					.findElement(By.xpath("//*[@id=\"app\"]/div/div[1]/div/div/div/div[2]/div/form/button"));
			signupBtn.click();
		}
		//顯示信箱已被註冊
		WebElement emailExists = driverE6.findElement(By.xpath("//*[@id=\"app\"]/div/div[2]/div"));
		if (emailExists.getAttribute("display") != null) {

			System.out.println("已經被註冊");
			Logs.log(Logs.RUN_LOG, "emailExists: " + "true");
		}

		driverE6.quit();
	}

	public void queryGolfMaster(HttpServletRequest req) {
		gaParam paramA = new gaParam();
		gmParam paramM = new gmParam();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String strSQL1;
		String strSQL2;

		strSQL1 = String.format(
				"insert into golf_master.member (\r\n"
						+ "name,nickname,birth,gender,tee,phone,address,seniority,recent,average,score,dexterity)\r\n"
						+ "values('%s','%s','%s','%d','%d','%s','%s','%d','%s','%s','%d','%d')",
				paramM.name, paramM.nickname, paramM.birth, paramM.gender, paramM.tee, paramM.phone, paramM.address,
				paramM.seniority, paramM.recent, paramM.average, paramM.score, paramM.dexterity);

		strSQL2 = String.format("insert into golf_master.account(account,password,member_id)values('%s','%s','%d')",
				paramA.account,paramA.password,paramA.member_id);
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
