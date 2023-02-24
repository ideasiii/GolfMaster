package com.golfmaster.service;

import java.net.UnknownHostException;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.golfmaster.common.Logs;

public class JWebDriver {
	private WebDriver wdriver;

	final ChromeOptions chromeOptions = new ChromeOptions();

	public JWebDriver() {
		wdriver = null;
	}

	public void CreateWebDriver() throws UnknownHostException {
		System.setProperty("webdriver.chrome.silentOutput", "true");
		System.setProperty("webdriver.chrome.driver", "/opt/web_driver/chromedriver");
//		System.setProperty("webdriver.chrome.driver",
//				"C:\\Users\\P22361\\eclipse-workspace\\GolfMaster\\src\\com\\golfmaster\\driver\\test\\chromedriver.exe");


		if (1 == Config.HEADLESS) {
			chromeOptions.setHeadless(true);
		} else {
			chromeOptions.setHeadless(false);
		}
		chromeOptions.addArguments("--incognito");
		chromeOptions.addArguments("--disable-gpu", "--blink-settings=imagesEnabled=false");
		chromeOptions.addArguments("--disable-infobars");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-dev-shm-usage");
		chromeOptions.addArguments("--disable-extensions");
		
		wdriver = new ChromeDriver(chromeOptions);
	}

	// 載入頁面
	public void LoadUrl(String strUrl) {
		try {
			if (null != wdriver) {
				wdriver.get(strUrl);
				Logs.log(Logs.RUN_LOG, "Load URL : " + strUrl);
			} else {
				Logs.log(Logs.RUN_LOG, "Web Driver is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
	}
	
	public void closeDriver() {
		try {
			if(null != wdriver) {				
				wdriver.close();
				wdriver.quit();
				wdriver.quit();
				wdriver = null;
				Logs.log(Logs.RUN_LOG, "Web Driver is quit");
			}
		} catch(Exception e) {
			e.printStackTrace();
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
		}
	}

	public WebDriver getWdriver() {
		return wdriver;
	}
}
