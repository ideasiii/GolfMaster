package com.golfmaster.moduel.analysis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.golfmaster.common.DBUtil;
import com.golfmaster.common.Logs;
import com.golfmaster.moduel.DeviceData;

public class DrawTrajectory extends AnalysisRunner {
	public DrawTrajectory(DeviceData.WrgData wrgData) {
		super(wrgData);
	}

	@Override
	public void run() {
		System.out.println("DrawTrajectory analysis start");
		if (!getDrawTrajectory(m_wrgData, m_wrgExpert)) {
			setWeight(0);
		}
		System.out.println("DrawTrajectory analysis finish");
	}

	private boolean getDrawTrajectory(WrgData wrgData, WrgExpert wrgExpert) {
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX) {
			bResult = true;
			setWeight(2);
			if (fPathFace > 0) {
				wrgExpert.expert_trajectory = DRAW;
			}

			else {
				bResult = false;
			}
		}

		return bResult;
	}
}
