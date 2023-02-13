package com.golfmaster.moduel;

import org.json.JSONObject;

import com.golfmaster.common.Logs;
import com.golfmaster.moduel.analysis.SliceTrajectory;
import com.golfmaster.moduel.analysis.Trajectory;

public class AnalysisNetwork extends DeviceData
{
	private Trajectory trajectory = null;
	private SliceTrajectory sliceTrajectory = null;
	
	public AnalysisNetwork()
	{
		
	}

	@Override
	protected void finalize() throws Throwable
	{
		super.finalize();
	}
	
	public String expertAnalysis(float BallSpeed, float ClubAnglePath, float ClubAngleFace, float TotalDistFt, float CarryDistFt,
			float LaunchAngle, float SmashFactor, float BackSpin, float SideSpin, float ClubHeadSpeed, float LaunchDirection,
			float DistToPinFt) throws InterruptedException
	{
		String strResult;
		JSONObject jsonExpert = new JSONObject();
		jsonExpert.put("success", true);

		DeviceData.WrgData wrgData = new DeviceData.WrgData();
		WrgExpert wrgExpert = new WrgExpert();

		wrgData.BallSpeed = BallSpeed; // 球速
		wrgData.ClubAnglePath = ClubAnglePath; // 桿面路徑
		wrgData.ClubAngleFace = ClubAngleFace; // 桿面角度
		wrgData.TotalDistFt = TotalDistFt; // 置球點到擊球後停止滾動的距離(Ft)
		wrgData.CarryDistFt = CarryDistFt; // 置球點到擊球落點的距離(ft)。
		wrgData.DistToPinFt = DistToPinFt; // 擊球後與目標的距離(ft)。
		wrgData.LaunchAngle = LaunchAngle; // 發射角度
		wrgData.LaunchDirection = LaunchDirection; // 發射方向
		wrgData.SmashFactor = SmashFactor; // 擊球係數
		wrgData.BackSpin = BackSpin; // 後旋
		wrgData.SideSpin = SideSpin; // 側旋
		wrgData.ClubHeadSpeed = ClubHeadSpeed; // 桿頭速度
		
		//======== 分析網路開始 Sample ===============
		trajectory = new Trajectory(wrgData);
		sliceTrajectory = new SliceTrajectory(wrgData);
		
		Thread thrTrajectory = new Thread( trajectory );
		Thread theSliceTrajectory = new Thread( sliceTrajectory );
		
		thrTrajectory.start();
		theSliceTrajectory.start();
		
		thrTrajectory.join();
		theSliceTrajectory.join();
		
		System.out.println("Trajectory weight=" + trajectory.getWeight() + " suggest=" + trajectory.getWrgExpert().expert_trajectory);
		System.out.println("SliceTrajectory weight=" + sliceTrajectory.getWeight() + " suggest=" + sliceTrajectory.getWrgExpert().expert_suggestion);
		
		System.out.println("Analysis Network Finish");
		
	
		strResult = formatExpertJSON(wrgExpert, jsonExpert);
		Logs.log(Logs.RUN_LOG, strResult);
		return strResult;
	}
	
	private String formatExpertJSON(WrgExpert wrgExpert, JSONObject jsonExpert)
	{
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_cause", wrgExpert.expert_cause);
		jsonExpert.put("expert_trajectory", wrgExpert.expert_trajectory);
		jsonExpert.put("expert_p_system", wrgExpert.expert_p_system);
		return jsonExpert.toString();
	}

}
