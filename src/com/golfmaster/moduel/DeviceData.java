package com.golfmaster.moduel;

public abstract class DeviceData
{

	public class WrgData
	{
		public float	BallSpeed;
		public float	ClubAnglePath;
		public float	ClubAngleFace;
		public float	TotalDistFt;
		public float	CarryDistFt;
		public float	LaunchAngle;
		public float	SmashFactor;
		public float	BackSpin;
		public float	SideSpin;
		public float	ClubHeadSpeed;
		public float	LaunchDirection;
		public float	DistToPinFt;
	}
	
	public class WrgExpert
	{
		public String expert_suggestion;
		public String expert_cause;
		public String expert_p_system;
		public String expert_trajectory;
	}
}
