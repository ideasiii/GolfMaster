package com.golfmaster.persona;

import com.golfmaster.service.ShotData;

public class Persona {
	public static final float MaxDistance = 384.81f; // 128.27*3ft 越大越好
	public static final float MinDistance = 233.67f; // 77.89*3ft
	public static final float MaxBallSpeed = 94.71f; // mph 越大越好
	public static final float MinBallSpeed = 73.05f; // mph
	public static final float MaxClubSpeed = 75.01f; // mph 越大越好
	public static final float MinClubSpeed = 59.47f; // mph
	public static final float MaxLauchAngle = 22.29f; // deg  越大越好
	public static final float MinLauchAngle = 12.55f; // deg
	public static final float MaxLaunchDirection = 8.69f; // deg 越小越好
	public static final float MinLaunchDirection = 1.48f; // deg
	public static final float MaxClubAngleFace = 11.16f; // deg 越小越好
	public static final float MinClubAngleFace = 1.98f; // deg
	public static final float MaxClubAnglePath = 6.43f; // deg 越小越好
	public static final float MinClubAnglePath = 1.44f; // deg
	public static final float MaxBackSpin = 6632.2f; // rpm 越大越好
	public static final float MinBackSpin = 3276.0f; // rpm
	public static final float MaxSideSpin = 1810.2f; // rpm 越小越好
	public static final float MinSideSpin = 384.0f; // rpm

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
	
	public int getPlayerTrueLeve(String Player) {
		int playerLevel = 0;
		
		return playerLevel;
	}
	
	public int getTest() {
		return 0;
		
	}
}
