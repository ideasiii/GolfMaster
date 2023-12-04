package com.golfmaster.tool;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Random;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;

import javax.servlet.http.HttpServletRequest;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;

public class RandomData {

	public int insertD(HttpServletRequest req) {
		Connection conn = null;
		Statement stmt = null;
		int stmtRs = -1;
		String x = req.getParameter("num");
		int numInserts = Integer.parseInt(x);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			Random random = new Random();

			// 初始日期時間
			LocalDate startDate = LocalDate.of(2023, 9, 1);
			LocalDate endDate = LocalDate.now();
			LocalTime startTime = LocalTime.of(9, 0); // 起始時間為早上 9 點
			LocalTime endTime = LocalTime.of(18, 0); // 結束時間為下午 6 點
			LocalDateTime currentDateTime = LocalDateTime.of(startDate, startTime);

			for (int i = 0; i < numInserts; i++) {
				// 生成隨機秒數在 30 到 90 之間
				int randomSeconds = random.nextInt(600) + 300; // 30 到 90 之間的隨機秒數

				// 計算下一筆日期時間
				currentDateTime = currentDateTime.plusSeconds(randomSeconds);

				 // 如果超過結束時間，則加一天
	            if (currentDateTime.toLocalTime().isAfter(endTime)) {
	                currentDateTime = currentDateTime.plusDays(1).with(startTime);
	            }
	            
	            // 確保日期時間在工作日 (非六日)
	            while (currentDateTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
	                   currentDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
	                // 如果是六日，則加一天
	                currentDateTime = currentDateTime.plusDays(1).with(startTime);
	            }

				double maxBall = 120.99;
				double minBall = 0.00;
				double maxBack = 9000.00;
				double minBack = -9000.00;
				double maxSide = 9000.00;
				double minSide = -9000.00;
				double maxLauch = 50.00;
				double minLauch = 0.00;
				double maxAngle = 40.00;
				double minAngle = -30.00;

				double randomBall = (Math.random() * (maxBall - minBall + 0.01)) + minBall;
				double randomBack = (Math.random() * (maxBack - minBack + 0.01)) + minBack;
				double randomSide = (Math.random() * (maxSide - minSide + 0.01)) + minSide;
				double randomLauch = (Math.random() * (maxLauch - minLauch + 0.01)) + minLauch;
				double randomAngle = (Math.random() * (maxAngle - minAngle + 0.01)) + minAngle;

				// 創建 SQL 語句
				String strSQL = String.format(
						"INSERT INTO raw_data.IRIT (Day, BallSpeed, BackSpin, SideSpin, LaunchAngle, Angle) VALUES ('%s',%.2f,%.2f,%.2f,%.2f,%.2f)",
						currentDateTime, randomBall, randomBack, randomSide, randomLauch, randomAngle);

				// 執行插入操作
				stmtRs = stmt.executeUpdate(strSQL);
				System.out.println(i);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(null, stmt, conn);
		}

		return numInserts;
	}
	
	public int insertBodyData(HttpServletRequest req) {
		Connection conn = null;
		Statement stmt = null;
		int stmtRs = -1;
		String x = req.getParameter("num");
		int numInserts = Integer.parseInt(x);
		try {
			conn = DBUtil.getConnGolfMaster();
			stmt = conn.createStatement();
			Random random = new Random();

			// 初始日期時間
			LocalDate startDate = LocalDate.of(2023, 8, 2);
			LocalDate endDate = LocalDate.now();
			LocalTime startTime = LocalTime.of(12, 0); // 起始時間為早上 9 點
			LocalTime endTime = LocalTime.of(14, 0); // 結束時間為下午 6 點
			LocalDateTime currentDateTime = LocalDateTime.of(startDate, startTime);

			for (int i = 0; i < numInserts; i++) {
				// 生成隨機秒數在 30 到 90 之間
				int randomSeconds = random.nextInt(600) + 300; // 30 到 90 之間的隨機秒數

				// 計算下一筆日期時間
				currentDateTime = currentDateTime.plusSeconds(randomSeconds);

				 // 如果超過結束時間，則加一天
	            if (currentDateTime.toLocalTime().isAfter(endTime)) {
	                currentDateTime = currentDateTime.plusDays(1).with(startTime);
	            }
	            
	            // 確保日期時間在工作日 (非六日)
	            while (currentDateTime.getDayOfWeek() == DayOfWeek.SATURDAY ||
	                   currentDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
	                // 如果是六日，則加一天
	                currentDateTime = currentDateTime.plusDays(1).with(startTime);
	            }

				double BackSwingMax = 3.00;
				double BackSwingMin = 1.20;
				double DownSwingMax = 1.00;
				double DownSwingMin = 0.40;

				double randomBall = (Math.random() * (BackSwingMax - BackSwingMin + 0.01)) + BackSwingMin;
				double randomBack = (Math.random() * (DownSwingMax - DownSwingMin + 0.01)) + DownSwingMin;
				double tempo = randomBall / randomBack;

				// 創建 SQL 語句
				String strSQL = String.format(
						"INSERT INTO raw_data.JM (Day, BackSwing, DownSwing, Tempo) VALUES ('%s',%.2f,%.2f,%.2f)",
						currentDateTime, randomBall, randomBack ,tempo);

				// 執行插入操作
				stmtRs = stmt.executeUpdate(strSQL);
				System.out.println(i);
			}
		} catch (Exception e) {
			Logs.log(Logs.EXCEPTION_LOG, e.toString());
			e.printStackTrace();
		} finally {
			DBUtil.close(null, stmt, conn);
		}

		return numInserts;
	}

}
