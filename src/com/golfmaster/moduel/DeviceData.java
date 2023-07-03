package com.golfmaster.moduel;

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
	public static final String PULL_CAUSE = "通常是因為在下桿時由外側下桿、或是軸心偏移太多導致右手必須補償，或是沒有在P3時重心往左由左半身引導下桿所致。";
	public static final String PULL_SUGGEST = "上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String PULL_HOOK = "Pull Hook 左曲球"; // 左曲球
	public static final String PULL_HOOK_TRAJECTORY = "出球方向往目標左側之後再更向左曲之左拉曲球或稱為大左曲球(P7觸球時桿面角度和軌跡都朝左，且桿面角度大於揮桿軌跡，所以出球路徑往左飛後再更左曲)";
	public static final String PULL_HOOK_CAUSE = "通常是因為右手出力手腕往下壓太快導致揮桿路徑由外往內，且翻轉了手腕";
	public static final String PULL_HOOK_SUGGEST = "上桿時維持軸心，P2主P4胸椎充分旋轉下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String PUSH = "Push 右飛球";
	public static final String PUSH_TRAJECTORY = "出球方向直直往右之右推球(P7觸球時桿面角度和軌跡都往右而且垂直，所以出球路徑筆直往右)";
	public static final String PUSH_CAUSE = "上桿時P2位置過於內側，且下桿時揮桿路徑由內向外，桿面角度與揮桿路徑垂直所致。";
	public static final String PUSH_SUGGEST = "上桿時維持軸心，P2時桿頭不要往內側，桿面不要翻開，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String PUSH_HOOK = "(Push)Hook 右曲球";
	public static final String PUSH_HOOK_TRAJECTORY = "出球方向往目標線右側之後再往左曲之左曲球(P7 觸球時桿面打開軌跡由內向外，但桿面角度小於揮桿路徑)";
	public static final String PUSH_HOOK_CAUSE = "上桿往內側，然後再由內側透過手腕的翻轉使球往左旋。有時為了修正由外往內的揮桿路徑會刻意打出這種球來調整感覺，但做過頭的話很難控球，因為球落地會滾動太多，不容易控制距離。";
	public static final String PUSH_HOOK_SUGGEST = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	// 4.Draw, 5.Straight, 6.Fade
	public static final String DRAW = "Draw 小左曲球"; // Draw(小左曲球）
	public static final String DRAW_TRAJECTORY = "出球方向往目標右側飛出再向左曲回到目標之小左曲球(P7觸球時桿面角度方正，揮桿路徑大於桿面角度。球直直的飛出去之後往左曲回到目標)";
	public static final String DRAW_CAUSE = "手腕角度保持得很好，觸球時桿面方正，且揮桿軌跡由內向外所致。";
	public static final String DRAW_SUGGEST = "這是職業選手喜歡打出的球路，瞄球時桿面朝向目標，站姿朝向目標右方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String FADE = "Fade 小右曲球"; // Fade(小右曲球)
	public static final String FADE_TRAJECTORY = "出球方向由目標左側飛出再向右曲回到目標之小右曲球 (P7觸球時桿面角度方正，揮桿路徑由外向內大於桿面角度。球直直的飛出去之後往右曲回到目標)";
	public static final String FADE_CAUSE = "手腕角度保持得很好，桿面觸球方正，且揮桿軌跡由外向內所致。";
	public static final String FADE_SUGGEST = "這是職業選手最喜歡打出的球路，需要保持手和身體的連結，瞄球時桿面朝向目標，站姿朝向目標左方，上桿時維持軸心，P2至P4胸椎充分旋轉，下桿時重心往左，左半身帶領，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String STRAIGHT = "Straight 直飛球";
	public static final String STRAIGHT_TRAJECTORY = "出球方向朝目標飛去之直球(P7觸球時桿面角度和軌跡都方正而且垂直，所以出球路徑往目標筆直飛去)";
	public static final String STRAIGHT_CAUSE = "P1至P10動作的位置和時間都配合得很好，且節奏流暢。";
	public static final String STRAIGHT_SUGGEST = "多練習且重複此揮桿動作。";

	// SLICE
	public static final String STRAIGHT_SLICE_TRAJECTORY = "STRAIGHT SLICE，球先往目標直直去，尾勁往右邊飛,相對危險度比較低，還有可能留在球道上，偶而會打到OB線附近";
	public static final String STRAIGHT_SLICE_CAUSE = "球桿方向朝目標方向右邊,幾乎直球飛出漸往右邊偏去";
	public static final String STRAIGHT_SLICE_SUGGEST = "雖然軌道正確，但桿面是有點打開的情形下觸球，如果可以改善這個問題，距離跟方向都會更上一層樓";

	public static final String PUSH_SLICE = "Push Slice 左拉右曲";
	public static final String PUSH_SLICE_TRAJECTORY = "出球方向往目標右側之後再更往右曲之大右曲球(P7觸球時桿面過開，且揮桿軌跡由內往外但少於桿面角度，球會往右飛，然後再更向右曲)"; // 6.Push
	public static final String PUSH_SLICE_CAUSE = "上桿過度陡峭，下桿時突然過度由內往外打，或是上桿太內側且由內往外推所致。";
	public static final String PUSH_SLICE_SUGGEST = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	public static final String PULL_SLICE = "Pull Slice 左拉左曲";
	public static final String PULL_SLICE_TRAJECTORY = "出球方向往目標左側之後再向右曲之右曲球(P7觸球時桿面角度關閉，但桿面角度小於揮桿軌跡)";
	public static final String PULL_SLICE_CAUSE = "通常是因為上桿時P2過於內側，手臂和身體過於靠近卡住之後，反而在下桿時由外側下桿、或是軸心偏移太多導致右手必須補償所致，最容易出現雞翅膀的問題。";
	public static final String PULL_SLICE_SUGGEST = "上桿時維持軸心，P2時桿身平行於雙腳之平行線，保持桿面角度，P2至P4胸椎充分旋轉，下桿時於P3重心往左，左半身帶領的同時，P5至P6右肩和右手肘維持外旋，左手腕在P5.5時屈曲，維持手腕角度過球，轉身至P10收桿。";

	// 不同LEVEL的原因
	public static final String PULL_TRAJECTORY_LEVEL1 = "";
	public static final String PULL_HOOK_TRAJECTORY_LEVEL1 = "";
	public static final String PULL_SLICE_TRAJECTORY_LEVEL1 = "";
	public static final String PUSH_TRAJECTORY_LEVEL1 = "";
	public static final String PUSH_HOOK_TRAJECTORY_LEVEL1 = "";
	public static final String PUSH_SLICE_TRAJECTORY_LEVEL1 = "";
	public static final String DRAW_TRAJECTORY_LEVEL1 = "";
	public static final String STRAIGHT_TRAJECTORY_LEVEL1 = "";
	public static final String FADE_TRAJECTORY_LEVEL1 = "";

	public static final String PULL_TRAJECTORY_LEVEL2 = "";
	public static final String PULL_HOOK_TRAJECTORY_LEVEL2 = "";
	public static final String PULL_SLICE_TRAJECTORY_LEVEL2 = "";
	public static final String PUSH_TRAJECTORY_LEVEL2 = "";
	public static final String PUSH_HOOK_TRAJECTORY_LEVEL2 = "";
	public static final String PUSH_SLICE_TRAJECTORY_LEVEL2 = "";
	public static final String DRAW_TRAJECTORY_LEVEL2 = "";
	public static final String STRAIGHT_TRAJECTORY_LEVEL2 = "";
	public static final String FADE_TRAJECTORY_LEVEL2 = "";

	public static final String PULL_TRAJECTORY_LEVEL3 = "";
	public static final String PULL_HOOK_TRAJECTORY_LEVEL3 = "";
	public static final String PULL_SLICE_TRAJECTORY_LEVEL3 = "";
	public static final String PUSH_TRAJECTORY_LEVEL3 = "";
	public static final String PUSH_HOOK_TRAJECTORY_LEVEL3 = "";
	public static final String PUSH_SLICE_TRAJECTORY_LEVEL3 = "";
	public static final String DRAW_TRAJECTORY_LEVEL3 = "";
	public static final String STRAIGHT_TRAJECTORY_LEVEL3 = "";
	public static final String FADE_TRAJECTORY_LEVEL3 = "";

	// 不同LEVEL建議
	public static final String PULL_SUGGEST_LEVEL1 = "";
	public static final String PULL_HOOK_SUGGEST_LEVEL1 = "";
	public static final String PULL_SLICE_SUGGEST_LEVEL1 = "";
	public static final String PUSH_SUGGEST_LEVEL1 = "";
	public static final String PUSH_HOOK_SUGGEST_LEVEL1 = "";
	public static final String PUSH_SLICE_SUGGEST_LEVEL1 = "";
	public static final String DRAW_SUGGEST_LEVEL1 = "";
	public static final String STRAIGHT_SUGGEST_LEVEL1 = "";
	public static final String FADE_SUGGEST_LEVEL1 = "";

	public static final String PULL_SUGGEST_LEVEL2 = "";
	public static final String PULL_HOOK_SUGGEST_LEVEL2 = "";
	public static final String PULL_SLICE_SUGGEST_LEVEL2 = "";
	public static final String PUSH_SUGGEST_LEVEL2 = "";
	public static final String PUSH_HOOK_SUGGEST_LEVEL2 = "";
	public static final String PUSH_SLICE_SUGGEST_LEVEL2 = "";
	public static final String DRAW_SUGGEST_LEVEL2 = "";
	public static final String STRAIGHT_SUGGEST_LEVEL2 = "";
	public static final String FADE_SUGGEST_LEVEL2 = "";

	public static final String PULL_SUGGEST_LEVEL3 = "";
	public static final String PULL_HOOK_SUGGEST_LEVEL3 = "";
	public static final String PULL_SLICE_SUGGEST_LEVEL3 = "";
	public static final String PUSH_SUGGEST_LEVEL3 = "";
	public static final String PUSH_HOOK_SUGGEST_LEVEL3 = "";
	public static final String PUSH_SLICE_SUGGEST_LEVEL3 = "";
	public static final String DRAW_SUGGEST_LEVEL3 = "";
	public static final String STRAIGHT_SUGGEST_LEVEL3 = "";
	public static final String FADE_SUGGEST_LEVEL3 = "";

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
}
