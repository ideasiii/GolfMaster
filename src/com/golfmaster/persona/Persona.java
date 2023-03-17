package com.golfmaster.persona;

public class Persona {
	public static final float MaxDistance = 384.81f; // 128.27*3ft
	public static final float MinDistance = 233.67f; // 77.89*3ft
	public static final float MaxBallSpeed = 94.71f; // mph
	public static final float MinBallSpeed = 73.05f; // mph
	public static final float MaxClubSpeed = 75.01f; // mph
	public static final float MinClubSpeed = 59.47f; // mph
	public static final float MaxLauchAngle = 22.29f; // 度
	public static final float MinLauchAngle = 12.55f; // 度
	public static final float MaxLaunchDirection = 0.0f; // 度
	public static final float MinLaunchDirection = 0.0f; // 度
	public static final float MaxClubAngleFace = 0.0f; // 度
	public static final float MinClubAngleFace = 0.0f; // 度
	public static final float MaxClubAnglePath = 0.0f; // 度
	public static final float MinClubAnglePath = 0.0f; // 度
	public static final float MaxBackSpin = 0.0f; // rpm
	public static final float MinBackSpin = 0.0f; // rpm
	public static final float MaxSideSpin = 0.0f; // rpm
	public static final float MinSideSpin = 0.0f; // rpm

	public int getDistancelevel(float distance) {
		int tempLevel = 0;
		if (distance > MaxDistance) {
			tempLevel = 1;
		} else if (distance >= MinDistance && distance <= MaxDistance) {
			tempLevel = 2;
		} else if (distance < MinDistance) {
			tempLevel = 3;
		}
		return tempLevel;
	}
}
