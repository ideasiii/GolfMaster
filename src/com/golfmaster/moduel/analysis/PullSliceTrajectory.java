package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class PullSliceTrajectory extends AnalysisRunner {
	public PullSliceTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("PullSliceTrajectory analysis start");
		if (!getPullSliceTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("PullSliceTrajectory analysis finish");
	}

	private boolean getPullSliceTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):

		if (fPathFace < 0) {
			wrgExpert.expert_trajectory = PULL_SLICE;
		} else {
			bResult = false;
		}

		return bResult;
	}
}
