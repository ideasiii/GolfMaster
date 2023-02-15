package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class StraightTrajectory extends AnalysisRunner {

	public StraightTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("StraightTrajectory analysis start");
		if (!getStraightTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("StraightTrajectory analysis finish");
	}

	private boolean getStraightTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX) {
			bResult = true;
			setWeight(2);

			if (fPathFace == 0) {
				wrgExpert.expert_trajectory = STRAIGHT;
			}

			else {
				bResult = false;
			}
		}

		return bResult;
	}

}
