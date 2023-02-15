package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class PushHookTrajectory extends AnalysisRunner {
	public PushHookTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("PushHookTrajectory analysis start");
		if (!getPushHookTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("PushHookTrajectory analysis finish");
	}

	private boolean getPushHookTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX) {
			bResult = true;
			setWeight(2);
			if (fPathFace > 0) {
				wrgExpert.expert_trajectory = PUSH_HOOK;
			} else {
				bResult = false;
			}
		}

		return bResult;
	}
}
