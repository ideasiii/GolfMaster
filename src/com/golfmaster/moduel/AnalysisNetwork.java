package com.golfmaster.moduel;

import org.json.JSONObject;

import com.golfmaster.common.Logs;
import com.golfmaster.moduel.analysis.DrawTrajectory;
import com.golfmaster.moduel.analysis.FadeTrajectory;
import com.golfmaster.moduel.analysis.PullHookTrajectory;
import com.golfmaster.moduel.analysis.PullSliceTrajectory;
import com.golfmaster.moduel.analysis.PullTrajectory;
import com.golfmaster.moduel.analysis.PushHookTrajectory;
import com.golfmaster.moduel.analysis.PushSliceTrajectory;
import com.golfmaster.moduel.analysis.PushTrajectory;
import com.golfmaster.moduel.analysis.SliceTrajectory;
import com.golfmaster.moduel.analysis.StraightTrajectory;
import com.golfmaster.moduel.analysis.Trajectory;

public class AnalysisNetwork extends DeviceData {
	private Trajectory trajectory = null;
	private SliceTrajectory sliceTrajectory = null;
	private DrawTrajectory drawTrajectory = null;
	private StraightTrajectory straightTrajectory = null;
	private FadeTrajectory fadeTrajectory = null;
	private PullTrajectory pullTrajectory = null;
	private PullHookTrajectory pullHookTrajectory = null;
	private PullSliceTrajectory pullSliceTrajectory = null;
	private PushTrajectory pushTrajectory = null;
	private PushHookTrajectory pushHookTrajectory = null;
	private PushSliceTrajectory pushSliceTrajectory = null;

	public AnalysisNetwork() {

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	public String expertAnalysis(float BallSpeed, float ClubAnglePath, float ClubAngleFace, float TotalDistFt,
			float CarryDistFt, float LaunchAngle, float SmashFactor, float BackSpin, float SideSpin,
			float ClubHeadSpeed, float LaunchDirection, float DistToPinFt) throws InterruptedException {
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

		// ======== 分析網路開始 Sample ===============
//		trajectory = new Trajectory(wrgData);
//		sliceTrajectory = new SliceTrajectory(wrgData);
		drawTrajectory = new DrawTrajectory(wrgData);
		straightTrajectory = new StraightTrajectory(wrgData);
		fadeTrajectory = new FadeTrajectory(wrgData);
		pullTrajectory = new PullTrajectory(wrgData);
		pullHookTrajectory = new PullHookTrajectory(wrgData);
		pullSliceTrajectory = new PullSliceTrajectory(wrgData);
		pushTrajectory = new PushTrajectory(wrgData);
		pushHookTrajectory = new PushHookTrajectory(wrgData);
		pushSliceTrajectory = new PushSliceTrajectory(wrgData);

//		Thread thrTrajectory = new Thread( trajectory );
//		Thread theSliceTrajectory = new Thread( sliceTrajectory );
		Thread theDrawTrajectory = new Thread(drawTrajectory);
		Thread theStraightTrajectory = new Thread(straightTrajectory);
		Thread theFadeTrajectory = new Thread(fadeTrajectory);
		Thread thePullTrajectory = new Thread(pullTrajectory);
		Thread thePullHookTrajectory = new Thread(pullHookTrajectory);
		Thread thePullSliceTrajectory = new Thread(pullSliceTrajectory);
		Thread thePushTrajectory = new Thread(pushTrajectory);
		Thread thePushHookTrajectory = new Thread(pushHookTrajectory);
		Thread thePushSliceTrajectory = new Thread(pushSliceTrajectory);

//		thrTrajectory.start();
//		theSliceTrajectory.start();
		theDrawTrajectory.start();
		theStraightTrajectory.start();
		theFadeTrajectory.start();
		thePullHookTrajectory.start();
		thePullSliceTrajectory.start();
		thePullTrajectory.start();
		thePushHookTrajectory.start();
		thePushSliceTrajectory.start();
		thePushTrajectory.start();

//		thrTrajectory.join();
//		theSliceTrajectory.join();
		theDrawTrajectory.start();
		theStraightTrajectory.start();
		theFadeTrajectory.start();
		thePullHookTrajectory.start();
		thePullSliceTrajectory.start();
		thePullTrajectory.start();
		thePushHookTrajectory.start();
		thePushSliceTrajectory.start();
		thePushTrajectory.start();

//		System.out.println("Trajectory weight=" + trajectory.getWeight() + " suggest="
//				+ trajectory.getWrgExpert().expert_trajectory);
//		System.out.println("SliceTrajectory weight=" + sliceTrajectory.getWeight() + " suggest="
//				+ sliceTrajectory.getWrgExpert().expert_suggestion);
		System.out.println("DrawTrajectory weight=" + drawTrajectory.getWeight() + " suggest="
				+ drawTrajectory.getWrgExpert().expert_trajectory);

		System.out.println("Analysis Network Finish");

		strResult = formatExpertJSON(wrgExpert, jsonExpert);
		Logs.log(Logs.RUN_LOG, strResult);
		return strResult;
	}

	private String formatExpertJSON(WrgExpert wrgExpert, JSONObject jsonExpert) {
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_cause", wrgExpert.expert_cause);
		jsonExpert.put("expert_trajectory", wrgExpert.expert_trajectory);
		jsonExpert.put("expert_p_system", wrgExpert.expert_p_system);
		return jsonExpert.toString();
	}

}
