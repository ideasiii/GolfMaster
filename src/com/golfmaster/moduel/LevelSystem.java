package com.golfmaster.moduel;

import org.json.JSONObject;

import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData.WrgData;
import com.golfmaster.moduel.DeviceData.WrgExpert;

public class LevelSystem extends DeviceData {
	public LevelSystem() {

	}

	@Override
	protected void finalize() throws Throwable {

	}

	@Override
	public String toString() {
		return super.toString();
	}

	// 中文
	public String expertAnalysis(float BallSpeed, float ClubAnglePath, float ClubAngleFace, float TotalDistFt,
			float CarryDistFt, float LaunchAngle, float SmashFactor, float BackSpin, float SideSpin,
			float ClubHeadSpeed, float LaunchDirection, float DistToPinFt, String ClubType) {
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
		wrgData.ClubType = ClubType; // 球桿種類

		strResult = formatExpertJSON(wrgExpert, jsonExpert);
//		Logs.log(Logs.RUN_LOG, strResult);
		return strResult;
	}

	private String formatExpertJSON(WrgExpert wrgExpert, JSONObject jsonExpert) {
		jsonExpert.put("expert_suggestion", wrgExpert.expert_suggestion);
		jsonExpert.put("expert_cause", wrgExpert.expert_cause);
		jsonExpert.put("expert_trajectory", wrgExpert.expert_trajectory);
		jsonExpert.put("expert_p_system", wrgExpert.expert_p_system);
		return jsonExpert.toString();
	}

	private boolean clubValid(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		if (wrgData.ClubAngleFace >= 15 || wrgData.ClubAngleFace <= -15) {
			// 球桿面和高爾夫球接觸中心點的水平球桿面方向
			wrgExpert.expert_suggestion = "球桿面和高爾夫球接觸中心點的水平球桿面方向超出正常範圍";
			wrgExpert.expert_cause = SHOT_FAIL_BODY;
			wrgExpert.expert_trajectory = SHOT_FAIL;
			bResult = true;
		}

		if (wrgData.ClubAnglePath >= 15 || wrgData.ClubAnglePath <= -15) {
			// 高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)技術定義：FACE
			// ANGLE 和 CLUB PATH 定義的角度差（FACE ANGLE 減去 CLUB PATH）。
			wrgExpert.expert_suggestion = "高爾夫擊球的預期曲率超出正常範圍";
			wrgExpert.expert_cause = SHOT_FAIL_BODY;
			wrgExpert.expert_trajectory = SHOT_FAIL;
			bResult = true;
		}

		return bResult;
	}

	private boolean getCarryDistFt(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bValid = false;

		return bValid;
	}

	private String getTrajectory(WrgData wrgData) {
		String strTrajectory = null;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):
		if (wrgData.ClubAngleFace < FACE_SQUARE_MIN) {
			if (fPathFace > 0) {
				strTrajectory = PULL_HOOK;
			}
			if (fPathFace == 0) {
				strTrajectory = PULL;
			}
			if (fPathFace < 0) {
				strTrajectory = PULL_SLICE;
			}
		}

		// 2.桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX) {
			if (fPathFace > 0) {
				strTrajectory = DRAW;
			}
			if (fPathFace == 0) {
				strTrajectory = STRAIGHT;
			}
			if (fPathFace < 0) {
				strTrajectory = FADE;
			}
		}

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX) {
			if (fPathFace > 0) {
				strTrajectory = PUSH_HOOK;
			}
			if (fPathFace == 0) {
				strTrajectory = PUSH;
			}
			if (fPathFace < 0) {
				strTrajectory = PUSH_SLICE;
			}
		}

		return strTrajectory;
	}

	private boolean getHitFail(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bValid = false;

		return bValid;
	}

	private boolean pSystemCheck(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bValid = false;

		wrgExpert.expert_trajectory = getTrajectory(wrgData); // 取得彈道

		return bValid;
	}
}
