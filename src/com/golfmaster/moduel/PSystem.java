/**
 * P System專家分析模組
 * 1公尺等於1.09361碼，你可以直接將公尺數乘以 1.09361 就可以得到碼數
 * 1碼相等於3英尺
 */
package com.golfmaster.moduel;

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

		if (!pSystemCheck(wrgData, wrgExpert))
		{
			if (!clubValid(wrgData, wrgExpert))
			{
				if (!launchAngleValid(wrgData, wrgExpert))
				{

				}
			}
		}

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

	private boolean clubValid(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bResult = false;
		if (wrgData.ClubAngleFace > 15 || wrgData.ClubAngleFace < -15)
		{
			// 球桿面和高爾夫球接觸中心點的水平球桿面方向
			wrgExpert.expert_suggestion = "球桿面和高爾夫球接觸中心點的水平球桿面方向超出正常範圍";
			wrgExpert.expert_cause = SHOT_FAIL_BODY;
			wrgExpert.expert_p_system = SHOT_FAIL;
			wrgExpert.expert_trajectory = SHOT_FAIL;
			bResult = true;
		}

		if (wrgData.ClubAnglePath > 15 || wrgData.ClubAnglePath < -15)
		{
			// 高爾夫擊球的預期曲率(旋轉軸)的關鍵因素。假設中心接觸，球應該朝向面角彎曲並遠離球桿路徑(+5、0、-5)技術定義：FACE
			// ANGLE 和 CLUB PATH 定義的角度差（FACE ANGLE 減去 CLUB PATH）。
			wrgExpert.expert_suggestion = "高爾夫擊球的預期曲率超出正常範圍";
			wrgExpert.expert_cause = SHOT_FAIL_BODY;
			wrgExpert.expert_p_system = SHOT_FAIL;
			wrgExpert.expert_trajectory = SHOT_FAIL;
			bResult = true;
		}

		return bResult;
	}

	private boolean getCarryDistFt(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		if (wrgData.CarryDistFt >= CARRY_DIST_FT_MIN)
		{
			bValid = true;
			if (wrgData.BallSpeed >= 150)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離達到職業級水準 ";
			}
			else if (wrgData.BallSpeed >= 140 && wrgData.BallSpeed < 150)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離達到教練水準 ";
			}
			else if (wrgData.BallSpeed >= 130 && wrgData.BallSpeed < 140)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離達到專業水準 ";
			}
			else if (wrgData.BallSpeed >= 120 && wrgData.BallSpeed < 130)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離達到專業水準 ";
			}
			else if (wrgData.BallSpeed >= 100 && wrgData.BallSpeed < 120)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離達到標準水準 ";
			}
			else if (wrgData.BallSpeed >= 80 && wrgData.BallSpeed < 100)
			{
				wrgExpert.expert_suggestion = "置球點到擊球落點的距離一般水準 ";
			}
			else
			{
				bValid = false;
			}
		}
		return bValid;
	}

	private boolean launchAngleValid(WrgData wrgData, WrgExpert wrgExpert)
	{
		// 隨著球速的降低，最佳發射角度必須增加，後旋也必須增加
		boolean bResult = false;

		if (wrgData.LaunchAngle < LAUNCH_ANGLE_MIN)
		{
			wrgExpert.expert_suggestion = "擊球發射角度超出正常範圍";
			wrgExpert.expert_cause = SHOT_FAIL_BODY;
			wrgExpert.expert_p_system = SHOT_FAIL;
			wrgExpert.expert_trajectory = SHOT_FAIL;
			bResult = true;
		}
		return bResult;
	}

	private String getTrajectory(WrgData wrgData)
	{
		String strTrajectory = null;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):
		if (wrgData.ClubAngleFace < FACE_SQUARE_MIN)
		{
			if (fPathFace > 0)
			{
				strTrajectory = PULL_HOOK;
			}
			if (fPathFace == 0)
			{
				strTrajectory = PULL;
			}
			if (fPathFace < 0)
			{
				strTrajectory = PULL_SLICE;
			}
		}

		// 2.桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX)
		{
			if (fPathFace > 0)
			{
				strTrajectory = DRAW;
			}
			if (fPathFace == 0)
			{
				strTrajectory = STRAIGHT;
			}
			if (fPathFace < 0)
			{
				strTrajectory = FADE;
			}
		}

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX)
		{
			if (fPathFace > 0)
			{
				strTrajectory = PUSH_HOOK;
			}
			if (fPathFace == 0)
			{
				strTrajectory = PUSH;
			}
			if (fPathFace < 0)
			{
				strTrajectory = PUSH_SLICE;
			}
		}

		return strTrajectory;
	}

	private boolean getSliceTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bIsSlice = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):
		if (wrgData.ClubAngleFace < FACE_SQUARE_MIN)
		{
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = PULL_HOOK;
			}
			if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = PULL;
			}
			if (fPathFace < 0)
			{
//				wrgExpert.expert_trajectory = PULL_SLICE_TRAJECTORY;
				wrgExpert.expert_trajectory = PULL_SLICE;
				wrgExpert.expert_cause = PULL_SLICE_CAUSE;
				wrgExpert.expert_suggestion = PULL_SLICE_SUGGEST;
//				wrgExpert.expert_p_system = P2_3;
				wrgExpert.expert_p_system = P2;
				bIsSlice = true;
			}
		}

		// 2.桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX)
		{
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = DRAW;
			}
			if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = STRAIGHT;
			}
			if (fPathFace < 0)
			{
//				wrgExpert.expert_trajectory = STRAIGHT_SLICE_TRAJECTORY;
				wrgExpert.expert_trajectory = FADE;
//				wrgExpert.expert_cause = STRAIGHT_SLICE_CAUSE;
				wrgExpert.expert_cause = FADE_CAUSE;
//				wrgExpert.expert_suggestion = STRAIGHT_SLICE_SUGGEST;
				wrgExpert.expert_suggestion = FADE_SUGGEST;
				wrgExpert.expert_p_system = P2_3;
				bIsSlice = true;
			}
		}

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX)
		{
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = PUSH_HOOK;
			}
			if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = PUSH;
			}
			if (fPathFace < 0)
			{
//				wrgExpert.expert_trajectory = PUSH_SLICE_TRAJECTORY;
				wrgExpert.expert_trajectory = PUSH_SLICE;
				wrgExpert.expert_cause = PUSH_SLICE_CAUSE;
				wrgExpert.expert_suggestion = PUSH_SLICE_SUGGEST;
				wrgExpert.expert_p_system = P2_3;
				bIsSlice = true;
			}
		}
		return bIsSlice;
	}

	private boolean getDrawTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		// 常為技術純熟球員所刻意擊出的球路，一個過度的Draw通常會形成為一個大左曲球(左勾球)
		boolean bValid = false;

//		wrgExpert.expert_trajectory = DRAW_TRAJECTORY;
		wrgExpert.expert_trajectory = DRAW;
		wrgExpert.expert_cause = DRAW_CAUSE;
//		wrgExpert.expert_p_system = P2_3;
		wrgExpert.expert_p_system = P7;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "下桿時，膝關節伸直，腰部伸展，聳肩、抬頭，上臂用力導致手肘彎曲，身體重心移到腳跟";
			wrgExpert.expert_p_system = P5_6;
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + DRAW_SUGGEST;

		return bValid;
	}

	private boolean getStraightTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = STRAIGHT_TRAJECTORY;
		wrgExpert.expert_trajectory = STRAIGHT;
		wrgExpert.expert_cause = STRAIGHT_CAUSE;
		wrgExpert.expert_p_system = P2_3;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "優秀的高爾夫球手往往非常擅長在球落地之前保持收尾，此擊球它看起來不錯但置球點到擊球落點的距離不佳。 如果你的後腳沒有轉動，你肯定有時間在你完成並將它帶到下一個擊球時注意到並糾正它。 我堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + STRAIGHT_SUGGEST;

		return bValid;
	}

	private boolean getFadeTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = FADE_TRAJECTORY;
		wrgExpert.expert_trajectory = FADE;
		wrgExpert.expert_cause = FADE_CAUSE;
//		wrgExpert.expert_p_system = P2_3;
		wrgExpert.expert_p_system = P7;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "此擊球它看起來不錯但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + FADE_SUGGEST;

		return bValid;
	}

	private boolean getPushHookTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = PUSH_HOOK_TRAJECTORY;
		wrgExpert.expert_trajectory = PUSH_HOOK;
		wrgExpert.expert_cause = PUSH_HOOK_CAUSE;
		wrgExpert.expert_p_system = P2_3;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "此擊球它看起來不錯但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + PUSH_HOOK_SUGGEST;

		return bValid;
	}

	private boolean getPushTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = PUSH_TRAJECTORY;
		wrgExpert.expert_trajectory = PUSH;
		wrgExpert.expert_cause = PUSH_CAUSE;
//		wrgExpert.expert_p_system = P2_3;
		wrgExpert.expert_p_system = P2;

		// 要限定至少要打多遠 CARRY_DIST_FT_MIN置球點到擊球落點的距離(ft)。
		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "此擊球它看起來不錯但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + PUSH_SUGGEST;
		return bValid;
	}

	private boolean getPullTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = PULL_TRAJECTORY;
		wrgExpert.expert_trajectory = PULL;
		wrgExpert.expert_cause = PULL_CAUSE;
//		wrgExpert.expert_p_system = P2_3;
		wrgExpert.expert_p_system = P3;

		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "此擊球它看起來不錯但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + PULL_SUGGEST;
		return bValid;
	}

	private boolean getPullHookTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

//		wrgExpert.expert_trajectory = PULL_HOOK_TRAJECTORY;
		wrgExpert.expert_trajectory = PULL_HOOK;
		wrgExpert.expert_cause = PULL_HOOK_CAUSE;
//		wrgExpert.expert_p_system = P2_3;
		wrgExpert.expert_p_system = P7;

		bValid = true;
		if (!getCarryDistFt(wrgData, wrgExpert))
		{
			wrgExpert.expert_suggestion = "此擊球它看起來不錯但置球點到擊球落點的距離不佳。堅信你的揮桿要保持良好的平衡，如果你能保持收杆，那是你能得到更好的擊球";
		}
		else
			wrgExpert.expert_suggestion = wrgExpert.expert_suggestion + PULL_HOOK_SUGGEST;
		return bValid;
	}

	private boolean getHitFail(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

		wrgExpert.expert_p_system = p2_9;
		wrgExpert.expert_suggestion = SHOT_FAIL_BODY;
		wrgExpert.expert_cause = "擊球角度過小 ";

		int nYt = Math.round(wrgData.CarryDistFt / 10);

		if (0 < nYt)
			bValid = true;

		if (4 <= wrgData.LaunchAngle && 70 <= wrgData.CarryDistFt)
		{
			if (60 <= wrgData.BallSpeed)
			{
				wrgExpert.expert_suggestion += "如果球不是落於沙坑,建議通過身體的旋轉，揮動手臂下杆或者下杆時肩膀太過主動，這些動作都無法形成強大的力量";
				wrgExpert.expert_cause = "下桿時，膝關節伸直，腰部伸展，聳肩、抬頭，上臂用力導致手肘彎曲，身體重心移到腳跟";
			}
			else
			{
				wrgExpert.expert_suggestion += "如果球不是落於沙坑,建議通過身體的旋轉，而不是依靠手臂的力量。身體旋轉地速度越快，擊球力量就會越大";
				wrgExpert.expert_cause = "下桿時，膝關節伸直，腰部伸展，聳肩、抬頭，上臂用力導致手肘彎曲，身體重心移到腳跟";
			}
		}
		else
		{
			switch (nYt)
			{
				case 0:
					wrgExpert.expert_suggestion = SHOT_FAIL;
					wrgExpert.expert_cause = SHOT_FAIL;
					wrgExpert.expert_p_system = SHOT_FAIL;
					wrgExpert.expert_trajectory = THE_TOP;
					break;
				case 1:
					wrgExpert.expert_cause += "下桿時，膝關節伸直，腰部伸展，聳肩、抬頭";
					wrgExpert.expert_suggestion += " 擊球時左腕保持平直以及桿身向前傾斜";
					break;
				case 2:
					wrgExpert.expert_cause += "下桿時，膝關節伸直，上臂用力導致手肘彎曲，身體重心移到腳跟";
					wrgExpert.expert_suggestion += " 擊球時左腕保持平直以及桿身向前傾斜";
					break;
				case 3:
				case 4:
				case 5:
					wrgExpert.expert_cause += "下桿時，球桿的釋放及球桿的加速度都太早發生";
					wrgExpert.expert_suggestion += " 上桿過程中,不要把身體重心太過右移,以不超過右腳內側為佳";
					break;
				case 6:
				case 7:
				case 8:
					wrgExpert.expert_cause += "下桿時，球桿的釋放及球桿的加速度都太早發生";
					wrgExpert.expert_suggestion += " 上桿過程中,不要把身體重心太過右移,以不超過右腳內側為佳";
					break;
				case 9:
					wrgExpert.expert_cause += "下桿時，球桿的釋放及球桿的加速度都太早發生";
					wrgExpert.expert_suggestion += " 木桿應該是延著球中線平行掃過去，而鐵桿應該是往球的中心點向下擊下去";
					break;
				case 10:
				case 11:
					wrgExpert.expert_cause += "下桿時，膝關節伸直，上臂用力導致手肘彎曲，身體重心移到腳跟";
					wrgExpert.expert_suggestion += " 木桿應該是延著球中線平行掃過去，而鐵桿應該是往球的中心點向下擊下去";
					break;
				default:
					bValid = false;
					break;
			}
		}

		return bValid;
	}

	private boolean pSystemCheck(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bValid = false;

		wrgExpert.expert_trajectory = getTrajectory(wrgData); // 取得彈道

		if (wrgData.CarryDistFt < CARRY_DIST_FT_MIN && wrgData.LaunchAngle < LAUNCH_ANGLE_MIN)
		{
			bValid = getHitFail(wrgData, wrgExpert);
		}
		else
		{
			if (PULL_SLICE.equals(wrgExpert.expert_trajectory) || STRAIGHT_SLICE_TRAJECTORY.equals(wrgExpert.expert_trajectory)
					|| PUSH_SLICE.equals(wrgExpert.expert_trajectory))
			{
				bValid = getSliceTrajectory(wrgData, wrgExpert);
			}
			else if (DRAW.equals(wrgExpert.expert_trajectory))
			{
				bValid = getDrawTrajectory(wrgData, wrgExpert);
			}
			else if (STRAIGHT.equals(wrgExpert.expert_trajectory))
			{
				bValid = getStraightTrajectory(wrgData, wrgExpert);
			}
			else if (FADE.equals(wrgExpert.expert_trajectory))
			{
				bValid = getFadeTrajectory(wrgData, wrgExpert);
			}
			else if (PUSH_HOOK.equals(wrgExpert.expert_trajectory))
			{
				bValid = getPushHookTrajectory(wrgData, wrgExpert);
			}
			else if (PUSH.equals(wrgExpert.expert_trajectory))
			{
				bValid = getPushTrajectory(wrgData, wrgExpert);
			}
			else if (PULL.equals(wrgExpert.expert_trajectory))
			{
				bValid = getPullTrajectory(wrgData, wrgExpert);
			}
			else if (PULL_HOOK.equals(wrgExpert.expert_trajectory))
			{
				bValid = getPullHookTrajectory(wrgData, wrgExpert);
			}
			else
			{
				bValid = false;
			}
		}

		return bValid;
	}
}

/*
 * 
 * 
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
