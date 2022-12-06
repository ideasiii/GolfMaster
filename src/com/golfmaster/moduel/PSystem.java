/**
 * P System專家分析模組
 */
package com.golfmaster.moduel;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.golfmaster.common.Logs;

public class PSystem extends DeviceData
{
	public PSystem() {
		
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}

	@Override
	public String toString()
	{
		return super.toString();
	}

	public String expertAnalysis(float BallSpeed, float ClubAnglePath, float ClubAngleFace, float TotalDistFt, float CarryDistFt,
			float LaunchAngle, float SmashFactor, float BackSpin, float SideSpin, float ClubHeadSpeed, float LaunchDirection,
			float DistToPinFt)
	{
		JSONObject jsonExpert = new JSONObject();
		jsonExpert.put("success", true);

		DeviceData.WrgData wrgData = new DeviceData.WrgData();
		DeviceData.WrgExpert wrgExpert = new DeviceData.WrgExpert();
		
		wrgData.BallSpeed = BallSpeed;
		wrgData.ClubAnglePath = ClubAnglePath;
		wrgData.ClubAngleFace = ClubAngleFace;
		wrgData.TotalDistFt = TotalDistFt;
		wrgData.CarryDistFt = CarryDistFt;
		wrgData.LaunchAngle = LaunchAngle;
		wrgData.SmashFactor = SmashFactor;
		wrgData.BackSpin = BackSpin;
		wrgData.SideSpin = SideSpin;
		wrgData.ClubHeadSpeed = ClubHeadSpeed;
		wrgData.LaunchDirection = LaunchDirection;
		wrgData.DistToPinFt = DistToPinFt;
		
		if(clubValid(wrgData,wrgExpert))
		{
			
		}
		
		return formatExpertJSON(wrgExpert, jsonExpert);
	}

	private String formatExpertJSON(DeviceData.WrgExpert wrgExpert, JSONObject jsonExpert)
	{
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		return jsonExpert.toString();
	}
	
	private boolean clubValid(DeviceData.WrgData wrgData, DeviceData.WrgExpert wrgExpert)
	{
		boolean bResult = true;
		if (wrgData.ClubAngleFace > 15 || wrgData.ClubAngleFace < -15)
		{
			// 球桿面和高爾夫球接觸中心點的水平球桿面方向
			wrgExpert.expert_suggestion = "球桿面和高爾夫球接觸中心點的水平球桿面方向超出正常範圍";
			wrgExpert.expert_cause = "擊球姿勢嚴重錯誤";
			wrgExpert.expert_p_system = "擊球失敗,無法判定";
			wrgExpert.expert_trajectory = "擊球失敗,無法判定";
			bResult = false;
		}

		if (wrgData.ClubAnglePath > 15 || wrgData.ClubAnglePath < -15)
		{
			// 高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)技術定義：FACE ANGLE 和 CLUB PATH 定義的角度差（FACE ANGLE 減去 CLUB PATH）。
			wrgExpert.expert_suggestion = "高爾夫擊球的預期曲率超出正常範圍";
			wrgExpert.expert_cause = "擊球姿勢嚴重錯誤";
			wrgExpert.expert_p_system = "擊球失敗,無法判定";
			wrgExpert.expert_trajectory = "擊球失敗,無法判定";
			bResult = false;
		}
		
		return bResult;
	}
	
	

}

/*
 * 
 * 
 * jsonProject.put("id", rs.getInt("id")); jsonProject.put("LID",
 * rs.getString("LID")); //每個不同模擬器配合場域的odd jsonProject.put("Player",
 * rs.getString("Player")); jsonProject.put("Date", rs.getString("Date"));
 * jsonProject.put("shotVideo_front", rs.getString("shotVideo_front")); //正面影片
 * jsonProject.put("shotVideo_side", rs.getString("shotVideo_side")); //側面影片
 * jsonProject.put("BallSpeed", rs.getDouble("BallSpeed"));
 * //高爾夫球在撞擊後立即的速度，球速是由球桿速度和衝擊力決定的。*技術定義：球速係指高爾夫球重心與桿面分離後的速度\n單位: mph
 * jsonProject.put("LaunchAngle", rs.getDouble("LaunchAngle"));
 * //隨著球速的降低，最佳發射角度必須增加，後旋也必須增加。單位: degree jsonProject.put("LaunchDirection",
 * rs.getDouble("LaunchDirection"));
 * //發射方向是球相對於目標線開始的初始方向。正發射方向表示球從目標右側開始，負發射方向......單位: degree
 * jsonProject.put("ClubHeadSpeed", rs.getDouble("ClubHeadSpeed"));
 * //從身體設置，到上桿頂點身體旋轉，讓手臂和身體將保持連接，保持在正確的揮桿平面上，並使用大肌肉來創造擊球過程。(握壓/Swing/釋放/節奏)，單位:
 * mph jsonProject.put("ClubAngleFace", rs.getDouble("ClubAngleFace"));
 * //高爾夫球手將此稱為具有“開放”或“封閉”桿面。*技術定義：在高爾夫球最大壓縮時，球桿面和高爾夫球接觸中心點的水平球桿面方向，單位: degree
 * jsonProject.put("ClubAnglePath", rs.getDouble("ClubAnglePath"));
 * //高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)\\\\n技術定義：FACE ANGLE 和
 * CLUB PATH 定義的角度差（FACE ANGLE 減去 CLUB PATH）。單位: degree'
 * jsonProject.put("BackSpin", rs.getInt("BackSpin")); //高爾夫球迴旋是高爾夫球的反向旋轉
 * jsonProject.put("SideSpin", rs.getInt("SideSpin")); //高爾夫球側旋是橫向發生的旋轉
 * jsonProject.put("SmashFactor", rs.getDouble("SmashFactor")); //球桿速度除以球速
 * jsonProject.put("ClubType", rs.getString("ClubType")); //幾號球桿、材質
 * jsonProject.put("DistToPinFt", rs.getDouble("DistToPinFt")); //擊球後與目標的距離，單位:
 * ft 呎 jsonProject.put("CarryDistFt", rs.getDouble("CarryDistFt"));
 * //置球點到擊球落點的距離，單位: ft 呎 jsonProject.put("TotalDistFt",
 * rs.getDouble("TotalDistFt")); //置球點到擊球後停止滾動的距離，單位: ft 呎
 */
