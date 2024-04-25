package com.golfmaster.moduel;

import java.util.HashMap;
import java.util.Map;

public abstract class DeviceData {
	public static final String SHOT_FAIL = "擊球失敗,無法判定";
	public static final String SHOT_FAIL_BODY = "擊球姿勢嚴重錯誤";

	// P-System
	public static final String p2_9 = "P2~3 上桿 , P5~6 下桿 , P7 Impact 擊球";
	public static final String P2_3 = "P2~3 上桿";
	public static final String P4 = "P4 頂點/轉換";
	public static final String P5_6 = "P5~6 下桿";
	public static final String P1 = "P1 Address 擊球準備";
	public static final String P2 = "P2 Takeaway 起桿";
	public static final String P3 = "P3 Backswing 上桿";
	// public static final String P4 = "P4 Top of Swing 上桿頂點";
	public static final String P5 = "P5 Downswing 下桿";
	public static final String P6 = "P6 Downswing 下桿";
	public static final String P7 = "P7 Impact 擊球";
	public static final String P8 = "P8 Release 送桿";
	public static final String P9 = "P9 Release 送桿";
	public static final String P10 = "P10 Finish 收桿";


	public static final float FACE_SQUARE_MIN = -3;
	public static final float FACE_SQUARE_MAX = 3;
	public static final float CARRY_DIST_FT_MIN = 40 * 3; // 40碼(yd)
	// public static final float CARRY_DIST_FT_MIN = 0; // 李老師建議先改成0
	public static final float LAUNCH_ANGLE_MIN = 6;

	// 球桿種類
	public static final String Wood1 = "1Wood";
	public static final String Wood2 = "2Wood";
	public static final String Wood3 = "3Wood";
	public static final String Wood4 = "4Wood";
	public static final String Wood5 = "5Wood";
	public static final String Iron1 = "1Iron";
	public static final String Iron2 = "2Iron";
	public static final String Iron3 = "3Iron";
	public static final String Iron4 = "4Iron";
	public static final String Iron5 = "5Iron";
	public static final String Iron6 = "6Iron";
	public static final String Iron7 = "7Iron";
	public static final String Iron8 = "8Iron";
	public static final String Iron9 = "9Iron";
	public static final String Hybrid4 = "4Hybrid";
	public static final String SandWedge = "SandWedge";
	public static final String GapWedge = "GapWedge";
	public static final String PitchingWedge = "PitchingWedge";
	public static final String Putter = "Putter";

	// slice(外彎)與hook(內彎)
	// 1.Pull Hook, 2.Pull, 3.Pull Slice
	public static final String PULL = "Pull 左飛球"; // 5.Pull(左飛球)
	public static final String PULL_TRAJECTORY = "出球方向直直往左之左拉球(P7觸球時桿面角度和軌跡都往左而且垂直，所以出球路徑筆直往左)";
	public static final String PULL_CAUSE_P2_3 = "上桿(P2~3)時，角度過於陡峭";
	public static final String PULL_SUGGEST_P2_3 = "上桿(P2~3)時，肩膀往右轉動";
	public static final String PULL_CAUSE_P4 = "桿頭頂點過高";
	public static final String PULL_SUGGEST_P4 = "肩膀往右轉動";
	public static final String PULL_CAUSE_P5_6 = "下桿角度過於陡峭，左手腕過度外展，肩關節伸展抬起";
	public static final String PULL_SUGGEST_P5_6 = "下桿時，左手臂打直、左手腕維持固定、肩膀自然旋轉";
	public static final String PULL_CAUSE_P7 = "桿面關閉，擊球點位於球的外側";
	public static final String PULL_SUGGEST_P7 = "擊球時，左手腕維持固定，注意擊球點位置";
	
	public static final String PULL_HOOK = "Pull Hook 左拉左曲球"; // 左曲球
	public static final String PULL_HOOK_TRAJECTORY = "出球方向往目標左側之後再更向左曲之左拉曲球或稱為大左曲球(P7觸球時桿面角度和軌跡都朝左，且桿面角度大於揮桿軌跡，所以出球路徑往左飛後再更左曲)";
	public static final String PULL_HOOK_CAUSE_P2_3 = "上桿(P2~3)時，角度過於陡峭";
	public static final String PULL_HOOK_SUGGEST_P2_3 = "上桿(P2~3)時，肩膀往右轉動";
	public static final String PULL_HOOK_CAUSE_P4 = "桿頭頂點過高";
	public static final String PULL_HOOK_SUGGEST_P4 = "肩膀往右轉動";
	public static final String PULL_HOOK_CAUSE_P5_6 = "下桿角度過於陡峭，手腕過度彎曲，過度由內而外的路徑";
	public static final String PULL_HOOK_SUGGEST_P5_6 = "下桿時，左手臂打直、左手腕維持固定";
	public static final String PULL_HOOK_CAUSE_P7 = "桿面關閉，擊球點位於球的外側，手腕繼續彎曲未保持向前";
	public static final String PULL_HOOK_SUGGEST_P7 = "擊球時，左手腕應恢復成未彎曲的狀態，及注意擊球點位置";
	
	public static final String PULL_SLICE = "Pull Slice 左拉右曲球";
	public static final String PULL_SLICE_TRAJECTORY = "出球方向往目標左側之後再向右曲之右曲球(P7觸球時桿面角度關閉，但桿面角度小於揮桿軌跡)";
//	public static final String PULL_SLICE_CAUSE = "通常是因為上桿時P2過於內側，手臂和身體過於靠近卡住之後，反而在下桿時由外側下桿、或是軸心偏移太多導致右手必須補償所致，最容易出現雞翅膀的問題。";
//	public static final String PULL_SLICE_SUGGEST = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";
	public static final String PULL_SLICE_CAUSE_P2_3 = "上桿時P2過於內側，手臂和身體過於靠近";
	public static final String PULL_SLICE_SUGGEST_P2_3 = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度";
	public static final String PULL_SLICE_CAUSE_P4 = "擺動路徑過於內向";
	public static final String PULL_SLICE_SUGGEST_P4 = "胸椎充分旋轉，重心往左";
	public static final String PULL_SLICE_CAUSE_P5_6 = "下桿時由外側下桿";
	public static final String PULL_SLICE_SUGGEST_P5_6 = "P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲";
	public static final String PULL_SLICE_CAUSE_P7 = "觸球時桿面角度關閉，但桿面角度小於揮桿軌跡";
	public static final String PULL_SLICE_SUGGEST_P7 = "維持手腕角度過球轉身至收尾收桿";
	
	public static final String PUSH = "Push 右飛球";
	public static final String PUSH_TRAJECTORY = "出球方向直直往右之右推球(P7觸球時桿面角度和軌跡都往右而且垂直，所以出球路徑筆直往右)";
	public static final String PUSH_CAUSE_P2_3 = "上桿(P2~3)時，角度過於平緩，手腕過度內收，未保持適當角度";
	public static final String PUSH_SUGGEST_P2_3 = "上桿(P2~3)時，角度過於平緩，手腕過度內收，未保持適當角度";
	public static final String PUSH_CAUSE_P4 = "桿頭頂點過低，手腕過度彎曲";
	public static final String PUSH_SUGGEST_P4 = "左手腕維持固定，腰部減少轉動";
	public static final String PUSH_CAUSE_P5_6 = "下桿角度過於平緩，手腕未回復成預備站姿的角度，手用力而非肩膀轉動";
	public static final String PUSH_SUGGEST_P5_6 = "下桿時，手腕及肩膀往左轉動";
	public static final String PUSH_CAUSE_P7 = "桿面開放，擊球點位於球的內側";
	public static final String PUSH_SUGGEST_P7 = "擊球時，左手腕維持固定，注意擊球點位置";
	
	public static final String PUSH_SLICE = "Push Slice 右拉右曲球";
	public static final String PUSH_SLICE_TRAJECTORY = "出球方向往目標右側之後再更往右曲之大右曲球(P7觸球時桿面過開，且揮桿軌跡由內往外但少於桿面角度，球會往右飛，然後再更向右曲)"; // 6.Push
	public static final String PUSH_SLICE_CAUSE_P2_3 = "上桿(P2~3)時，角度過於平緩，手腕過度內收，未保持適當角度，肩或腰旋轉過度";
	public static final String PUSH_SLICE_SUGGEST_P2_3 = "上桿(P2~3)時，左手腕維持固定，腰部減少轉動";
	public static final String PUSH_SLICE_CAUSE_P4 = "桿頭頂點過低，腰部或手腕過度彎曲";
	public static final String PUSH_SLICE_SUGGEST_P4 = "左手腕維持固定，腰部減少轉動";
	public static final String PUSH_SLICE_CAUSE_P5_6 = "下桿角度過於平緩，左手腕過度外展，肩關節伸展抬起，腰部向前旋轉過度，身體重心太早向前移動";
	public static final String PUSH_SLICE_SUGGEST_P5_6 = "下桿時，手腕及肩膀往左轉動，腰部減少旋轉";
	public static final String PUSH_SLICE_CAUSE_P7 = "桿面開放，擊球點位於球的內側";
	public static final String PUSH_SLICE_SUGGEST_P7 = "擊球時，左手腕維持固定，注意擊球點位置";
	
	public static final String PUSH_HOOK = "(Push)Hook 右拉左曲球";
	public static final String PUSH_HOOK_TRAJECTORY = "出球方向往目標線右側之後再往左曲之左曲球(P7 觸球時桿面打開軌跡由內向外，但桿面角度小於揮桿路徑)";
//	public static final String PUSH_HOOK_CAUSE = "上桿往內側，然後再由內側透過手腕的翻轉使球往左旋。有時為了修正由外往內的揮桿路徑會刻意打出這種球來調整感覺，但做過頭的話很難控球，因為球落地會滾動太多，不容易控制距離。";
//	public static final String PUSH_HOOK_SUGGEST = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";
	public static final String PUSH_HOOK_CAUSE_P2_3 = "上桿往內側，然後再由內側透過手腕的翻轉使球往左旋";
	public static final String PUSH_HOOK_SUGGEST_P2_3 = "上桿時維持軸心，保持桿面角度";
	public static final String PUSH_HOOK_CAUSE_P4 = "桿頭在頂點位置過於內側";
	public static final String PUSH_HOOK_SUGGEST_P4 = "胸椎充分旋轉，保持桿頭方正";
	public static final String PUSH_HOOK_CAUSE_P5_6 = "開始下揮時，身體未能正確同步旋轉，桿面過於閉合";
	public static final String PUSH_HOOK_SUGGEST_P5_6 = "下桿時，從臀部開始動作，保持下半身與上半身的協調";
	public static final String PUSH_HOOK_CAUSE_P7 = "桿面打開軌跡由內向外，桿面角度小於揮桿路徑";
	public static final String PUSH_HOOK_SUGGEST_P7 = "注重在接近擊球點時，保持下半身的穩定與桿面的正確閉合度";

	// 4.Draw, 5.Straight, 6.Fade
	public static final String DRAW = "Draw 小左曲球"; // Draw(小左曲球）
	public static final String DRAW_TRAJECTORY = "出球方向往目標右側飛出再向左曲回到目標之小左曲球(P7觸球時桿面角度方正，揮桿路徑大於桿面角度。球直直的飛出去之後往左曲回到目標)";
//	public static final String DRAW_CAUSE = "手腕角度保持得很好，觸球時桿面方正，且揮桿軌跡由內向外所致。";
//	public static final String DRAW_SUGGEST = "這是職業選手喜歡打出的球路，瞄球時桿面朝向目標，站姿朝向目標右方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";
	public static final String DRAW_CAUSE_P2_3 = "上桿時，手腕角度保持得很好";
	public static final String DRAW_SUGGEST_P2_3 = "Nice Shot!";
	public static final String DRAW_CAUSE_P4 = "觸球時桿面方正";
	public static final String DRAW_SUGGEST_P4 = "漂亮的一擊！";
	public static final String DRAW_CAUSE_P5_6 = "揮桿軌跡由內向外所致";
	public static final String DRAW_SUGGEST_P5_6 = "你很優秀!繼續保持";

	public static final String FADE = "Fade 小右曲球"; // Fade(小右曲球)
	public static final String FADE_TRAJECTORY = "出球方向由目標左側飛出再向右曲回到目標之小右曲球 (P7觸球時桿面角度方正，揮桿路徑由外向內大於桿面角度。球直直的飛出去之後往右曲回到目標)";
//	public static final String FADE_CAUSE = "手腕角度保持得很好，桿面觸球方正，且揮桿軌跡由外向內所致。";
//	public static final String FADE_SUGGEST = "這是職業選手最喜歡打出的球路，需要保持手和身體的連結，瞄球時桿面朝向目標，站姿朝向目標左方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";
	public static final String FADE_CAUSE_P2_3 = "上桿時，胸椎保持良好";
	public static final String FADE_SUGGEST_P2_3 = "好球!";
	public static final String FADE_CAUSE_P4 = "觸球時桿面角度方正";
	public static final String FADE_SUGGEST_P4 = "優秀的觸球！";
	public static final String FADE_CAUSE_P5_6 = "揮桿軌跡由外向內所致";
	public static final String FADE_SUGGEST_P5_6 = "Excellent!";

	public static final String STRAIGHT = "Straight 直飛球";
	public static final String STRAIGHT_TRAJECTORY = "出球方向朝目標飛去之直球(P7觸球時桿面角度和軌跡都方正而且垂直，所以出球路徑往目標筆直飛去)";
//	public static final String STRAIGHT_CAUSE = "P1至P10動作的位置和時間都配合得很好，且節奏流暢。";
//	public static final String STRAIGHT_SUGGEST = "多練習且重複此揮桿動作。";
	public static final String STRAIGHT_CAUSE_P2_3 = "上桿時，動作協調良好";
	public static final String STRAIGHT_SUGGEST_P2_3 = "直得像箭一樣！";
	public static final String STRAIGHT_CAUSE_P4 = "節奏十分流暢";
	public static final String STRAIGHT_SUGGEST_P4 = "那是一個完美的擊發！";
	public static final String STRAIGHT_CAUSE_P5_6 = "P1至P10動作的位置和時間都配合得很好";
	public static final String STRAIGHT_SUGGEST_P5_6 = "絕妙的一球!";
	


	public static final float LONG_PUTT = 10; // 長推桿距離30英尺,約為10碼
	public static final float SHORT_PUTT = 2; // 短推桿距離6英尺,約2碼

	public static final float Putt_FACE_SQUARE_MIN = 2; // 桿面傾角
	public static final float Putt_FACE_SQUARE_MAX = 4;

	// TODO: 其他問題球:
	public static final String THE_TOP = "擊球失誤導致擊出剃頭球或切滾球";
	public static final String DUFF = "擊球失誤導致先擊到球後方之草地，再擊到球";
	public static final String SHANK = "擊球失誤導致棒擊球";
	public static final String LACK_OF_STRENGTH = "力道不足的擊球";
	

	public class jmexParamData {
		public int id;
		public float pelvisSpeed; // 骨盆速度 度/秒
		public float trunkSpeed; // 軀幹速度 度/秒
		public float forearmSpeed; // 上臂速度 度/秒
		public float handSpeed; // 手部速度 度/秒
		public float trunkForwardBendAddress; // 軀幹前傾瞄球位置 度
		public float trunkForwardBendTop; // 軀幹前傾頂點位置 度
		public float trunkForwardBendImpact; // 軀幹前傾擊球位置 度
		public float trunkSideBendAddress; // 軀幹側傾瞄球位置 度
		public float trunkSideBendTop; // 軀幹側傾頂點位置 度
		public float trunkSideBendImpact; // 軀幹側傾擊球位置 度
		public float trunkRotationAddress; // 軀幹轉動瞄球位置 度
		public float trunkRotationTop; // 軀幹轉動頂點位置 度
		public float trunkRotationImpact; // 軀幹轉動擊球位置 度

		public float pelvisForwardBendAddress; // 骨盆前傾瞄球位置 度
		public float pelvisForwardBendTop; // 骨盆前傾頂點位置 度
		public float pelvisForwardBendImpact; // 骨盆前傾擊球位置 度
		public float pelvisSideBendAddress; // 骨盆側傾瞄球位置 度
		public float pelvisSideBendTop; // 骨盆側傾頂點位置 度
		public float pelvisSideBendImpact; // 骨盆側傾擊球位置 度
		public float pelvisRotationAddress; // 骨盆轉動瞄球位置 度
		public float pelvisRotationTop; // 骨盆轉動頂點位置 度
		public float pelvisRotationImpact; // 骨盆轉動擊球位置 度
		public int TSPelvis; // TS = Transition Sequencing 1~4
		public int TSTrunk;
		public int TSForearm;
		public int TSHand;
		public int PSSPelvis; // PPS = Peak Speed Sequencing 1~4
		public int PSSTrunk;
		public int PSSForearm;
		public int PSSHand;
		public float backSwingTime; // 上桿時間 秒
		public float downSwingTime; // 下桿時間 秒
		public float tempo = backSwingTime / downSwingTime;// 節奏 倍
		public String addressS_Posture;
		public String addressC_Posture;
		public String topX_Factor;
		public String topReverseSpine;
		public String topFlatShoulder;
		public String impactHipTurn;
	}

	public class ITRIData {
		public float BallSpeed;
		public float BackSpin;
		public float SideSpin;
		public float LaunchAngle;
		public float Angle;
	}

	public class spaceCapuleParamData {

	}

	public class WrgData {
		public float BallSpeed;
		public float ClubAnglePath;
		public float ClubAngleFace;
		public float TotalDistFt;
		public float CarryDistFt;
		public float LaunchAngle;
		public float SmashFactor;
		public float BackSpin;
		public float SideSpin;
		public float ClubHeadSpeed;
		public float LaunchDirection;
		public float DistToPinFt;
		public String ClubType;
	}

	public class WrgExpert {
		public String expert_suggestion;
		public String expert_cause;
		public String expert_p_system;
		public String expert_trajectory;
	}

	public static final String Par3_FirstStrike = "";

	public static final String AllStatusGood = "各項數據表象良好，請繼續保持";
	public static final String BallSpeedExpl = "球速為擊球時初始速度";
	public static final String ClubHeadSpeedExpl = "桿頭速度為球桿頭在擊球瞬間的移動速度";
	public static final String BackSpinExpl = "後旋為高爾夫球在飛行中繞自身軸心的旋轉";
	public static final String LauchAngleExpl = "發射角度為高爾夫球離開球桿面時與地面的夾角";
	public static final String DistanceExpl = "距離為高爾夫球從擊球點到停止滾動的直線距離";
	
	public static final String LackBallSpeed1 = "力量不足";
	public static final String LackBallSpeed2 = "擊球位置不佳";
	public static final String LackClubHeadSpeed1 = "擊球節奏不佳";
	public static final String LackClubHeadSpeed2 = "擊球節奏不佳";
	public static final String LackDistance1 = "";
	public static final String LackDistance2 = "";
	public static final String LackBackSpin1 = "";
	public static final String LackBackSpin2 = "";
	public static final String LackLaunchAngle1 = "";
	public static final String LackLaunchAngle2 = "";
	public static final String AllStatusFail = "各項數據表現異常顯示擊球失誤，需要教練輔助和多加練習。";

	// 距離
	public final double GreatLevelTopDist = 139.10;
	public final double GreatLevelLowDist = 128.27;
	public final double GoodLevelLowDist = 114.69;
	public final double NormalLevelLowDist = 98.94;
	public final double BadLevelLowDist = 77.89;
	public final double WorseLevelLowDist = 60.15;
	// 桿頭速度
	public final double GreatLevelTopCS = 79.07;
	public final double GreatLevelLowCS = 75.01;
	public final double GoodLevelLowCS = 70.79;
	public final double NormalLevelLowCS = 66.33;
	public final double BadLevelLowCS = 59.47;
	public final double WorseLevelLowCS = 54.01;
	// 球速
	public final double GreatLevelTopBS = 100.15;
	public final double GreatLevelLowBS = 94.71;
	public final double GoodLevelLowBS = 89.06;
	public final double NormalLevelLowBS = 81.22;
	public final double BadLevelLowBS = 73.05;
	public final double WorseLevelLowBS = 63.14;
	// 發射角
	public final double GreatLevelTopLA = 25.30;
	public final double GreatLevelLowLA = 22.29;
	public final double GoodLevelLowLA = 19.52;
	public final double NormalLevelLowLA = 16.61;
	public final double BadLevelLowLA = 12.55;
	public final double WorseLevelLowLA = 5.09;
	// 後旋
	public final double GreatLevelTopBsp = 7570.40;
	public final double GreatLevelLowBsp = 6632.20;
	public final double GoodLevelLowBsp = 5633.40;
	public final double NormalLevelLowBsp = 4659.00;
	public final double BadLevelLowBsp = 3276.00;
	public final double WorseLevelLowBsp = 1194.08;
}

