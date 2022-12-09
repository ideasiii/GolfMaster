package com.golfmaster.moduel;

public abstract class DeviceData
{
	public static final String	SHOT_FAIL		= "擊球失敗,無法判定";
	public static final String	SHOT_FAIL_BODY	= "擊球姿勢嚴重錯誤";

	// P-System
	public static final String	P2_3	= "P2~3 上桿";
	public static final String	P4		= "P4 頂點/轉換";
	public static final String	P5_6	= "P5~6 下桿";
	public static final String	P1		= "P1 Address 擊球準備";
	public static final String	P2		= "P2 Takeaway 起桿";
	public static final String	P3		= "P3 Backswing 上桿";
	// public static final String P4 = "P4 Top of Swing 上桿頂點";
	public static final String	P5	= "P5 Downswing 下桿";
	public static final String	P6	= "P6 Downswing 下桿";
	public static final String	P7	= "P7 Impact 擊球)";
	public static final String	P8	= "P8 Release 送桿";
	public static final String	P9	= "P9 Release 送桿";
	public static final String	P10	= "P10 Finish 收桿";

	public static final float	FACE_SQUARE_MIN		= -3;
	public static final float	FACE_SQUARE_MAX		= 3;
	public static final float	CARRY_DIST_FT_MIN	= 40 * 3;	// 40碼(yd)
	// public static final float CARRY_DIST_FT_MIN = 0; // 李老師建議先改成0
	public static final float LAUNCH_ANGLE_MIN = 6;

	// slice(外彎)與hook(內彎)
	// 1.Pull Hook, 2.Pull, 3.Pull Slice
	public static final String	PULL			= "左飛球";		// 5.Pull(左飛球)
	public static final String	PULL_TRAJECTORY	= "左飛球";		// 左曲球
	public static final String	PULL_CAUSE		= "上桿時，角度過於陡峭";	// 左曲球
	public static final String	PULL_SUGGEST	= "上桿時，肩膀往右轉動";	// 左曲球

	public static final String	PULL_HOOK				= "左曲球";		// 左曲球
	public static final String	PULL_HOOK_TRAJECTORY	= "左曲球";		// 左曲球
	public static final String	PULL_HOOK_CAUSE			= "上桿時，角度過於陡峭";	// 左曲球
	public static final String	PULL_HOOK_SUGGEST		= "上桿時，肩膀往右轉動";	// 左曲球

	public static final String	PUSH			= "右飛球";
	public static final String	PUSH_TRAJECTORY	= "右飛球";
	public static final String	PUSH_CAUSE		= "上桿時，角度過於平緩，手腕過度內收，未保持適當角度";
	public static final String	PUSH_SUGGEST	= "上桿時，左手腕維持固定，腰部減少轉動";

	public static final String	PUSH_HOOK				= "右曲球";
	public static final String	PUSH_HOOK_TRAJECTORY	= "右曲球";
	public static final String	PUSH_HOOK_CAUSE			= "右曲球";
	public static final String	PUSH_HOOK_SUGGEST		= "右曲球";

	// 4.Draw, 5.Straight, 6.Fade
	public static final String	DRAW			= "小左曲球";										// Draw(小左曲球）
	public static final String	DRAW_TRAJECTORY	= "一個輕微朝向左邊彎曲的擊球";
	public static final String	DRAW_CAUSE		= "技術純熟球員所刻意擊出的球路";
	public static final String	DRAW_SUGGEST	= "技術純熟球員所刻意擊出的球路,但一個過度的Draw通常會形成為一個大左曲球(左勾球)";

	public static final String	FADE			= "小右曲球";										// Fade(小右曲球)
	public static final String	FADE_TRAJECTORY	= "一個輕微朝向右邊彎曲的擊球";
	public static final String	FADE_CAUSE		= "技術純熟球員所刻意擊出的球路";
	public static final String	FADE_SUGGEST	= "技術純熟球員所刻意擊出的球路,但一個過度的Fade通常會形成為一個大右曲球(右弧球)";

	public static final String	STRAIGHT			= "直飛球";
	public static final String	STRAIGHT_TRAJECTORY	= "桿面垂直由內而內的揮桿路徑,球順著目標線筆直飛出";
	public static final String	STRAIGHT_CAUSE		= "良好的平衡";
	public static final String	STRAIGHT_SUGGEST	= "抓地力與你的身體相匹配而且正確瞄準,流暢的節奏,一個良好的擊球";

	// SLICE
	public static final String	STRAIGHT_SLICE_TRAJECTORY	= "STRAIGHT SLICE，球先往目標直直去，尾勁往右邊飛,相對危險度比較低，還有可能留在球道上，偶而會打到OB線附近";
	public static final String	STRAIGHT_SLICE_CAUSE		= "球桿方向朝目標方向右邊,幾乎直球飛出漸往右邊偏去";
	public static final String	STRAIGHT_SLICE_SUGGEST		= "雖然軌道正確，但桿面是有點打開的情形下觸球，如果可以改善這個問題，距離跟方向都會更上一層樓";

	public static final String	PUSH_SLICE_TRAJECTORY	= "球先往右邊飛出,再更往右偏,最容易打出OB的高危險球路,這種SLICE常會連續發生";											// 6.Push
																																				// //
																																				// Slice(右曲球)
	public static final String	PUSH_SLICE_CAUSE		= "桿頭路徑INSIDE IN,桿面方向朝向桿頭路徑更右方,結果球先往右邊飛出去再向右偏,上桿時，角度過於平緩，手腕過度內收，未保持適當角度，肩或腰旋轉過度";
	public static final String	PUSH_SLICE_SUGGEST		= "上桿時，左手腕維持固定，腰部減少轉動,正常站位就會打出INSIDE OUT的話，桿面在觸球時容易也是開的,不想往右邊去，稍微站關閉一點，想打一個左曲球路矯正回來";

	public static final String	PULL_SLICE_TRAJECTORY	= "球先往左邊飛出,然後再往右旋回到前方目標線,危險度比較低但是想要的距離出不來";
	public static final String	PULL_SLICE_CAUSE		= "桿頭路徑OUTSIDE IN,桿面方向是對往目標的左邊,結果球先往左邊飛再往右偏";
	public static final String	PULL_SLICE_SUGGEST		= "OUTSIDE IN的軌道,桿面稍往左關閉,即使有桿頭數度的人也會因為距離出不來,需要再改善";

	// 7.Push Hook, 8.Push, 9.Push Slice

	// TODO: 其他問題球:
	public static final String	THE_TOP				= "The Top";			// 1.The
																			// Top(剃頭球.切滾球)
	public static final String	DUFF				= "Duff";				// 2.Duff
																			// (先擊到球後方之草〈地〉，再擊到球)
	public static final String	SHANK				= "Shank";				// 3.Shank
																			// (棒擊球)
	public static final String	LACK_OF_STRENGTH	= "Lack of Strength";	// 8.Lack
																			// of
																			// Strength
																			// (力道不足)

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
		public String	expert_suggestion;
		public String	expert_cause;
		public String	expert_p_system;
		public String	expert_trajectory;
	}
}
