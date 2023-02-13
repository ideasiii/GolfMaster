package com.golfmaster.moduel.analysis;

import com.golfmaster.moduel.DeviceData;

public class Trajectory extends AnalysisRunner
{

	public Trajectory(DeviceData.WrgData wrgData)
	{
		super(wrgData);
	}
	
	@Override
	public void run()
	{
		System.out.println("Trajectory analysis start");
		if(!getTrajectory(m_wrgData, m_wrgExpert))
		{
			setWeight(0);
		}
		System.out.println("Trajectory analysis finish");
	}

	private boolean getTrajectory(WrgData wrgData, WrgExpert wrgExpert)
	{
		boolean bResult = false;
		float fPathFace = wrgData.ClubAnglePath - wrgData.ClubAngleFace;

		// 1.桿面(clubFace)是關的(Closed):
		if (wrgData.ClubAngleFace < FACE_SQUARE_MIN)
		{
			bResult = true;
			setWeight(2);
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = PULL_HOOK;
			}
			else if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = PULL;
			}
			else if (fPathFace < 0)
			{
				wrgExpert.expert_trajectory = PULL_SLICE_TRAJECTORY;
			}
			else
			{
				bResult = false;
			}
		}

		// 2.桿面(clubFace)是正的(Square):
		if (wrgData.ClubAngleFace >= FACE_SQUARE_MIN && wrgData.ClubAngleFace <= FACE_SQUARE_MAX)
		{
			bResult = true;
			setWeight(2);
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = DRAW;
			}
			else if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = STRAIGHT;
			}
			else if (fPathFace < 0)
			{
				wrgExpert.expert_trajectory = STRAIGHT_SLICE_TRAJECTORY;
			}
			else
			{
				bResult = false;
			}
		}

		// 3.桿面(clubFace)是開的(Open):
		if (wrgData.ClubAngleFace > FACE_SQUARE_MAX)
		{
			bResult = true;
			setWeight(2);
			if (fPathFace > 0)
			{
				wrgExpert.expert_trajectory = PUSH_HOOK;
			}
			else if (fPathFace == 0)
			{
				wrgExpert.expert_trajectory = PUSH;
			}
			else if (fPathFace < 0)
			{
				wrgExpert.expert_trajectory = PUSH_SLICE_TRAJECTORY;
			}
			else
			{
				bResult = false;
			}
		}

		return bResult;
	}
}
