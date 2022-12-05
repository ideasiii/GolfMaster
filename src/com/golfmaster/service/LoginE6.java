/*
 * http://localhost:8080/GolfMaster/service/logintest.jsp
 * http://localhost:8080/GolfMaster/service/logintest.jsp?account=uwk51642@nezid.com&password=as45770437&nickname=Sam&dexterity=1
 * */

package com.golfmaster.service;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.golfmaster.common.Logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

public class LoginE6 {

	public int E6Web(String email, String password, String displayName, int dexterity,HttpServletRequest req)
			throws InterruptedException, IOException {
		printParam(req);
		
		System.out.println("E6 Crawl Run");
//		伺服器175.41.245.90為linux
//		System.setProperty("webdriver.chrome.driver", "/opt/web_driver/chromedriver");
//		本機windows
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\P22361\\eclipse-workspace\\GolfMaster\\src\\com\\golfmaster\\driver\\chromedriver.exe");

		final ChromeOptions chromeOpt = new ChromeOptions();

//		給server用的屬性
		chromeOpt.addArguments("--no-sandbox");
		chromeOpt.addArguments("--headless");
		chromeOpt.addArguments("--disable-dev-shm-usage");

		WebDriver driverE6 = new ChromeDriver(chromeOpt);
		driverE6.get("https://portal.e6golf.com/signup");

		// 信箱欄
		WebElement emailBar = driverE6.findElement(By.id("input-27"));
		emailBar.sendKeys(email);
		Logs.log(Logs.RUN_LOG, "E6mail: " + email);

		// 確認信箱欄
		WebElement confirmEmailBar = driverE6.findElement(By.id("input-30"));
		confirmEmailBar.sendKeys(email);
		Logs.log(Logs.RUN_LOG, "confirmEmail: " + email);

		// 密碼欄
		WebElement passwordBar = driverE6.findElement(By.id("input-33"));
		passwordBar.sendKeys(password);
		Logs.log(Logs.RUN_LOG, "E6password: " + password);

		// 確認密碼欄
		WebElement confirmPasswordBar = driverE6.findElement(By.id("input-37"));
		confirmPasswordBar.sendKeys(password);
		Logs.log(Logs.RUN_LOG, "confirmPassword: " + password);

		// 暱稱欄
		WebElement displayNameBar = driverE6.findElement(By.id("input-41"));
		displayNameBar.sendKeys(displayName);
		Logs.log(Logs.RUN_LOG, "E6displayName: " + displayName);

		// 列表國家
		WebElement countryScroll = driverE6.findElement(By.id("input-44"));
		countryScroll.sendKeys("Taiwan");

		// 列表國家台灣選項
		WebElement countrySR = driverE6.findElement(By.id("list-44"));
		countrySR.click();
		// 列表慣用手
		WebElement dexterityScroll = driverE6.findElement(By.id("input-49"));
		dexterityScroll.click();
		if (dexterity == 1 && dexterity > 0) {
			// 列表右手選項
			WebElement dexterityRight = driverE6.findElement(By.id("list-item-151-0"));
			dexterityRight.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Right");
		} else if (dexterity == 2 && dexterity > 0) {
			// 列表左手選項
			WebElement dexterityLeft = driverE6.findElement(By.id("list-item-70-1"));
			dexterityLeft.click();
			Logs.log(Logs.RUN_LOG, "Dexterity: Left");
		}
		// 同意合約框
		WebElement checkbox = driverE6
				.findElement(By.cssSelector("div [class='v-input--selection-controls__ripple']"));
		checkbox.click();

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
		// 記錄錯誤的bar
		for (Object message : showWrongMessage) {
			if (message.equals(showWrongMessage.indexOf(message))) {
				System.out.println(message.toString());

			}
			Logs.log(Logs.RUN_LOG, "錯誤項目:" + message.toString());
		}
		if (showWrongMessage.size() > 0) {
			System.out.println("有錯誤");
			return 1;
		} else {

			// 註冊按鈕
			WebElement signupBtn = driverE6
					.findElement(By.cssSelector("#app > div > div.signup > div > div > div > div.layout.wrap > div > form > button > span"));
			signupBtn.click();

		}

		Thread.sleep(2000);
		// 顯示信箱已被註冊
//			WebElement emailExists = driverE6.findElement(By.xpath("//*[@id=\"app\"]/div/div[2]/div"));
		WebElement emailExists = driverE6
				.findElement(By.cssSelector("div [ class='v-snack__wrapper v-sheet theme--dark elevation-16 error']"));
		String x = null;
		if (emailExists.getAttribute("style") != x) {
			System.out.println("已經被註冊");
			Logs.log(Logs.RUN_LOG, "emailExists: " + "true");
			return 2;
		}

		driverE6.quit();
		return 0;
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
