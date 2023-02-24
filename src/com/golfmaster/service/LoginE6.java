/*
 * http://localhost:8080/GolfMaster/service/logintest.jsp
 * http://localhost:8080/GolfMaster/service/logintest.jsp?account=uwk51642@nezid.com&password=as45770437&nickname=Sam&dexterity=1
 * */

package com.golfmaster.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.service.GolfMasterRegister.gmParam;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class LoginE6 {
	private JWebDriver jWebDriver;

	public LoginE6() {
		jWebDriver = new JWebDriver();
		System.out.println("creat jWebDriver id:"+jWebDriver.getClass());
	}
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		jWebDriver.closeDriver();
//		JWebDriver.ClearChrome();
	}

	public int E6Web(String email, String password, String displayName, int dexterity, HttpServletRequest req)
			throws InterruptedException, IOException {
		printParam(req);

		System.out.println("E6 Crawl Run");
//		伺服器175.41.245.90為linux
		CreateWebDriver();
//		本機windows
//		System.setProperty("webdriver.chrome.driver",
//				"C:\\Users\\P22361\\eclipse-workspace\\GolfMaster\\src\\com\\golfmaster\\driver\\chromedriver.exe");

		jWebDriver.LoadUrl("https://portal.e6golf.com/signup");
		String startUrl = jWebDriver.getWdriver().getCurrentUrl();

		// 信箱欄
		WebElement emailBar = jWebDriver.getWdriver().findElement(By.cssSelector("#input-28"));
		emailBar.sendKeys(email);
		Logs.log(Logs.RUN_LOG, "E6mail: " + email);

		// 確認信箱欄
		WebElement confirmEmailBar = jWebDriver.getWdriver().findElement(By.cssSelector("#input-31"));
		confirmEmailBar.sendKeys(email);
		Logs.log(Logs.RUN_LOG, "confirmEmail: " + email);

		// 密碼欄
		WebElement passwordBar = jWebDriver.getWdriver().findElement(By.cssSelector("#input-34"));
		passwordBar.sendKeys(password);
		Logs.log(Logs.RUN_LOG, "E6password: " + password);

		// 確認密碼欄
		WebElement confirmPasswordBar = jWebDriver.getWdriver().findElement(By.cssSelector("#input-38"));
		confirmPasswordBar.sendKeys(password);
		Logs.log(Logs.RUN_LOG, "confirmPassword: " + password);

		// 暱稱欄
		WebElement displayNameBar = jWebDriver.getWdriver().findElement(By.cssSelector("#input-42"));
		displayNameBar.sendKeys(displayName);
		Logs.log(Logs.RUN_LOG, "E6displayName: " + displayName);

		// 列表國家
		WebElement countryScroll = jWebDriver.getWdriver().findElement(By.cssSelector("#input-45"));
		countryScroll.sendKeys("Taiwan");

		// 列表國家台灣選項
		WebElement countrySR = jWebDriver.getWdriver().findElement(By.cssSelector("#list-45"));
		countrySR.click();
		// 列表慣用手
		WebElement dexterityScroll = jWebDriver.getWdriver().findElement(By.cssSelector("#input-50"));
		dexterityScroll.click();
		if (dexterity == 1 && dexterity > 0) {
			// 列表右手選項
			WebElement dexterityRight = jWebDriver.getWdriver().findElement(By.cssSelector("#list-item-152-0"));
			dexterityRight.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Right");
		} else if (dexterity == 2 && dexterity > 0) {
			// 列表左手選項
			WebElement dexterityLeft = jWebDriver.getWdriver().findElement(By.cssSelector("#list-item-152-1"));
			dexterityLeft.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Left");
		}
		// 同意合約框
		WebElement checkbox = jWebDriver.getWdriver().findElement(By.cssSelector("div [class='v-input--selection-controls__ripple']"));
		checkbox.click();

		String regex = "[a-zA-Z0-9\\s\\d]+";
		if (displayName.matches(regex) == false) {
			closeChrome();
			
			return 1;
		}

		if (checkDisplayName(displayName) > 0) {
			closeChrome();
			return 1;
		}

		List<WebElement> wrongMessage = jWebDriver.getWdriver().findElements(By.cssSelector("div [class='v-messages__wrapper']"));
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
		// 記錄錯誤的bar
		for (Object message : showWrongMessage) {
			if (message.equals(showWrongMessage.indexOf(message))) {
				System.out.println(message.toString());

			}
			Logs.log(Logs.RUN_LOG, "錯誤項目:" + message.toString());
		}
		if (showWrongMessage.size() > 0) {
			System.out.println("有錯誤");
			closeChrome();
			return 1;
		} else {

			// 註冊按鈕
			WebElement signupBtn = jWebDriver.getWdriver().findElement(By.cssSelector(
					"#app > div > div.signup > div > div > div > div.layout.wrap > div > form > button > span"));
			signupBtn.click();

		}
		// 等待註冊結果
		Thread.sleep(5000);
		// 顯示信箱已被註冊
		if (jWebDriver.getWdriver().getCurrentUrl().compareTo(startUrl) == 0) {
			if (password.length() <= 8) {
				System.out.println("密碼過短");
				Logs.log(Logs.RUN_LOG, "passwordTooShort: " + "false");
				closeChrome();
				return 1;
			}
			System.out.println("已經被註冊");
			Logs.log(Logs.RUN_LOG, "emailExists: " + "true");
			closeChrome();
			return 2;
		}
		closeChrome();
		return 0;
	}

	public int checkDisplayName(String displayName) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		int result = 0;
		String strSQL;
		JSONArray jarrProjects = new JSONArray();

		strSQL = String.format("select * from golf_master.member where nickname = '%s'", displayName);
		Logs.log(Logs.RUN_LOG, strSQL);

		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(strSQL);
			while (rs.next()) {
				JSONObject jsonProject = new JSONObject();
				jsonProject.put("nickname", rs.getString("nickname"));
				jsonProject.put("birth", rs.getString("birth"));
				jsonProject.put("gender", rs.getString("gender"));
				jsonProject.put("tee", rs.getString("tee"));
				jsonProject.put("address", rs.getString("address"));
				jsonProject.put("seniority", rs.getString("seniority"));
				jsonProject.put("recent", rs.getString("recent"));
				jsonProject.put("average", rs.getString("average"));
				jsonProject.put("score", rs.getString("score"));
				jsonProject.put("dexterity", rs.getString("dexterity"));
				jsonProject.put("same_nickname", rs.getString("nickname").equals(displayName));

				if (jsonProject.get("same_nickname").toString() == "true")
					result = 1;

				jarrProjects.put(jsonProject);
			}

			Logs.log(Logs.RUN_LOG, jarrProjects.toString());
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		}
		DBUtil.close(rs, stmt, conn);
		return result;
	}

	private void printParam(HttpServletRequest req) {
		String strReq = "---Request Parameter---";
		Logs.log(Logs.RUN_LOG, strReq);
	}

	public JWebDriver CreateWebDriver() {
		jWebDriver.closeDriver();
		try {
			jWebDriver.CreateWebDriver();
			System.out.println("creat jWebDriver id:"+jWebDriver.getClass());
		} catch (UnknownHostException e) {
			Logs.log(Logs.EXCEPTION_LOG, e.getMessage());
			e.printStackTrace();
		}
		return jWebDriver;
	}
	
	public JWebDriver closeChrome() {
		System.out.println("close jWebDriver id:"+jWebDriver.getClass());
		try {
			jWebDriver.closeDriver();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return jWebDriver;
	}


}
