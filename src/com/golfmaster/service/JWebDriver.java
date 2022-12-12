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

		if (1 == Config.HEADLESS) {
			chromeOptions.setHeadless(true);
		} else {
			chromeOptions.setHeadless(false);
		}
		chromeOptions.addArguments("--disable-infobars");
		chromeOptions.addArguments("--incognito");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-dev-shm-usage");
		wdriver = new ChromeDriver(chromeOptions);
	}

	// 載入頁面
	public void LoadUrl(String strUrl) {
		try {
			if (null != wdriver) {
				System.out.println("Load URL : " + strUrl);
				wdriver.get(strUrl);
			} else {
				Logs.log(Logs.RUN_LOG, "Web Driver is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WebDriver getWdriver() {
		return wdriver;
	}
}
