package com.golfmaster.moduel.analysis;


public class SliceTrajectory extends AnalysisRunner
{

	public SliceTrajectory(WrgData wrgData)
	{
		super(wrgData);
		
	}

	@Override
	public void run()
	{
		System.out.println("Slice Trajectory analysis start");
		if(!getSliceTrajectory(m_wrgData, m_wrgExpert))
		{
			setWeight(0);
		}
		System.out.println("Slice Trajectory analysis finish");
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
				wrgExpert.expert_trajectory = PULL_SLICE_TRAJECTORY;
				wrgExpert.expert_cause = PULL_SLICE_CAUSE;
				wrgExpert.expert_suggestion = PULL_SLICE_SUGGEST;
				wrgExpert.expert_p_system = P2_3;
				bIsSlice = true;
				setWeight(1);
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
				wrgExpert.expert_trajectory = STRAIGHT_SLICE_TRAJECTORY;
				wrgExpert.expert_cause = STRAIGHT_SLICE_CAUSE;
				wrgExpert.expert_suggestion = STRAIGHT_SLICE_SUGGEST;
				wrgExpert.expert_p_system = P2_3;
				bIsSlice = true;
				setWeight(3);
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
				wrgExpert.expert_trajectory = PUSH_SLICE_TRAJECTORY;
				wrgExpert.expert_cause = PUSH_SLICE_CAUSE;
				wrgExpert.expert_suggestion = PUSH_SLICE_SUGGEST;
				wrgExpert.expert_p_system = P2_3;
				bIsSlice = true;
				setWeight(1);
			}
		}
		return bIsSlice;
	}

	
}
