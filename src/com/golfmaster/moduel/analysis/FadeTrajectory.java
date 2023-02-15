package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class FadeTrajectory extends AnalysisRunner {
	public FadeTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("FadeTrajectory analysis start");
		if (!getFadeTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("FadeTrajectory analysis finish");
	}

	private boolean getFadeTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 2.桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX) {
			bResult = true;
			setWeight(2);

			if (fPathFace < 0) {
				wrgExpert.expert_trajectory = FADE;
			} else {
				bResult = false;
			}
		}

		return bResult;
	}
}
