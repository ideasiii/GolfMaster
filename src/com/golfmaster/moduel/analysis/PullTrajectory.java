package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class PullTrajectory extends AnalysisRunner {
	public PullTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("PullTrajectory analysis start");
		if (!getPullTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("PullTrajectory analysis finish");
	}

	private boolean getPullTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):
		if (wrgData.ClubAngleFace < FACE_SQUARE_MIN) {
			bResult = true;
			setWeight(2);

			if (fPathFace == 0) {
				wrgExpert.expert_trajectory = PULL;
			}

			else {
				bResult = false;
			}
		}

		return bResult;
	}
}
