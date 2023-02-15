package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class PushTrajectory extends AnalysisRunner {
	public PushTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("PushTrajectory analysis start");
		if (!getPushTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("PushTrajectory analysis finish");
	}

	private boolean getPushTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX) {
			bResult = true;
			setWeight(2);
			if (fPathFace == 0) {
				wrgExpert.expert_trajectory = PUSH;
			} else {
				bResult = false;
			}
		}

		return bResult;
	}
}
