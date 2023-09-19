package com.golfmaster.moduel;

public abstract class DeviceDataJP 
{
	public static final String SHOT_FAIL = "シュートは失敗し判定不能";
	public static final String SHOT_FAIL_BODY = "打撃姿勢の重大なミス";

	// P-System
	public static final String p2_9 = "P2～3 バックスイング、P5～6 ダウンスイング、P7 インパクトショット";
	public static final String P2_3 = "P2~3 バックスイング";	
	public static final String P4 = "P4 頂点/変換";
	public static final String P5_6 = "P5~6 下極";
	public static final String P1 = "P1 アドレスバッティング準備";
	public static final String P2 = "P2 テイクアウェイ";
	public static final String P3 = "P3 バックスイング";
	// public static final String P4 = "P4 Top of Swing";
	public static final String P5 = "P5 ダウンスイング";
	public static final String P6 = "P6 ダウンスイング";
	public static final String P7 = "P7 インパクトバッティング";
	public static final String P8 = "P8 送信ロッドをリリース";
	public static final String P9 = "P9 送信ロッドをリリース";
	public static final String P10 = "P10 終了";

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
	public static final String PULL = "レフトフライボールをプル"; // 5.プル(レフトフライボール)
	public static final String PULL_TRAJECTORY = "ボールの方向は、ボールを左に真っすぐ引くことです (P7 がボールに触れたとき、クラブフェースの角度と軌道は両方とも左で垂直なので、ボールの軌道は左に真っ直ぐです) ";
	public static final String PULL_CAUSE = "通常、外側からのダウンスイングが原因か、軸のズレが大きすぎるため、右手で補正する必要があるか、P3と左ボディで重心が左に移動していないことが原因ですダウンスイングをガイドします。";
	public static final String PULL_SUGGEST = "バックスイング中に軸を維持し、胸椎を P2 から P4 まで完全に回転させ、ダウンスイング中に重心を左に移動し、左体をリードし、右肩と右の外旋を維持します肘を P5 から P6 に上げ、その時点で左手首を P5.5 フレックスに保ち、ボールをパスする手首の角度を維持し、P10 に回転してクラブを閉じます。";

	public static final String PULL_HOOK = "左フックをプル"; // フック
	public static final String PULL_HOOK_TRAJECTORY = "ボールをターゲットの左側に残し、その後左にフックするフックもフックと呼ばれます（ボールに触れたときのP7、クラブフェースの角度と軌道は両方とも左を向いており、クラブフェースの角度がスイング軌道よりも大きいため、ボールは左に飛んでさらにフックします)。";
	public static final String PULL_HOOK_CAUSE = "通常、右手が手首を押し下げる速度が速すぎるため、スイング軌道が外側から内側になり、手首が反転してしまうことが原因です。";
	public static final String PULL_HOOK_SUGGEST = "バックスイング中は軸を維持します。胸椎 P2 と P4 は完全に回転します。ダウンスイング中、重心は左に移動します。体の左半身が先行します。右肩と右肩が動きます。肘は P5 から P6 までの外旋を維持します。左手首は P5.5 フレックスで、手首の角度を維持してボールをパスし、P10 まで回転して終了します。";

	public static final String PUSH = "ライトフライをプッシュ";
	public static final String PUSH_TRAJECTORY = "ボールを右にまっすぐプッシュします (P7 がボールに触れたとき、クラブフェースの角度と軌道は右に垂直になるため、ボールの軌道は右に真っ直ぐになります)";
	public static final String PUSH_CAUSE = "バックスイング中の P2 の位置が内側になりすぎ、ダウンスイング中のスイング軌道が内側から外側になり、クラブフェースの角度がスイング軌道に対して垂直になっています。";
	public static final String PUSH_SUGGEST = "バックスイング中は軸を維持し、P2 中にクラブヘッドを内側に動かさず、クラブフェースを開かないようにし、胸椎を P2 から P4 まで完全に回転させ、重心を左に移動させます。ダウンスイング、左半身でリード、P5からP6へ右へ 肩と右肘の外旋を維持し、P5.5で左手首を曲げ、ボールをパスする手首の角度を維持し、P10へ回転終わる。";

	public static final String PUSH_HOOK = "(プッシュ)フックフック";
	public static final String PUSH_HOOK_TRAJECTORY = "ターゲットラインの右側にボールを残し、次に左にフックするフック (ボールがボールに当たるとき、P7、クラブフェースは内側から外側に開きますが、クラブフェースの角度はスイング軌道より小さい）";
	public static final String PUSH_HOOK_CAUSE = "バックスイングでインサイドへ、そしてインサイドから手首の返しでボールを左回転させます。スイング軌道をアウトサイドからインサイドに修正するために、この種のボールは意図的にスイングすることがあります。";
	public static final String PUSH_HOOK_SUGGEST = "バックスイング中、軸を維持します。P2 では、シャフトは足の平行線と平行になり、クラブフェイスの角度を維持します。P2 から P4 までの胸椎は完全に回転します。ダウンスイング中、軸の中心はP3で左に重力がかかり、左半身がリードすると同時に、P5からP6まで右肩と右肘の外旋を維持し、P5.5で左手首を屈曲し、手首の角度をボールをパスして、P10 に曲がって終了します。";

	// 4.Draw, 5.Straight, 6.Fade	
	public static final String DRAW = "描画フック"; // 描画(マイナーフック)
	public static final String DRAW_TRAJECTORY = "ターゲットの右に飛び、左にフックしてターゲットに戻る小さなフック (P7) ボールに触れたときのクラブフェースの角度は直角で、スイング軌道はボールよりも大きくなります。クラブフェースの角度。ボールはまっすぐです。飛び出した後、左に曲がってターゲットに戻ります）";
	public static final String DRAW_CAUSE = "手首の角度は適切に維持され、ボールにコンタクトするときにクラブフェースはスクエアになり、スイング軌道はインサイドからアウトサイドになります。";
	public static final String DRAW_SUGGEST = "ここはプロ選手がプレイするのが好きなゴルフコースです。ボールにアドレスするとき、クラブフェースはターゲットを向き、スタンスはターゲットの右を向き、バックスイング中も軸は維持され、P2ダウンスイングでは胸椎からP4までを完全に回転させ、重心を完全に回転させ、左へ、左半身でリードし、P5からP6まで右肩と右肘の外旋を維持し、右肘を曲げます。左手首を P5.5 に置き、手首の角度を維持してボールをパスし、P10 に回転してクラブを閉じます。";

	public static final String FADE = "フェードフェード"; // フェード(フェード)
	public static final String FADE_TRAJECTORY = "ボールはターゲットの左側から飛び出し、その後カーブしてターゲットに戻ってきます (P7) ボールに触れたときのクラブのフェース角度は直角で、スイング軌道はクラブよりも大きくなりますフェースアングルはアウトサイドからインサイドへ。ボールはまっすぐ飛んで、右にカーブしてターゲットに戻ってくる）";
	public static final String FADE_CAUSE = "手首の角度は適切に維持され、クラブフェースはボールに真正面からコンタクトし、スイング軌道はアウトサイドからインサイドになります。";
	public static final String FADE_SUGGEST = "ここは、プロ プレーヤーに最も人気のあるゴルフ コースです。手と体のつながりを維持する必要があります。ボールにアドレスするとき、クラブ フェースはターゲットに向き、スタンスはターゲットの左側を向きます。目標を達成し、バックスイング中も軸を維持 P2 ～ P4 胸椎を完全に回転させ、ダウンスイングでは重心を左に置き、左半身がリード 右肩と右肘は外側を維持P5 から P6 への回転。左手首は P5.5 で曲がり、ボールをパスするために手首の角度を維持し、クラブを閉じるために P10 に回転します。";

	public static final String STRAIGHT = "ストレート";
	public static final String STRAIGHT_TRAJECTORY = "ボールをリリースする方向にターゲットに向かって飛んでいくストレートボール (P7 がボールに触れたとき、クラブフェースの角度と軌道は直角で垂直であるため、ボールの軌道はターゲットに真っ直ぐに飛びます) ";
	public static final String STRAIGHT_CAUSE = "P1 から P10 までのアクションの位置と時間がうまく調整されており、リズムがスムーズです。";
	public static final String STRAIGHT_SUGGEST = "このスイングを練習して繰り返します。";

	// SLICE
	public static final String STRAIGHT_SLICE_TRAJECTORY = "STRAIGHT SLICE、ボールは最初にターゲットに向かって真っすぐに進み、テールフォースで右に飛びます。相対的なリスクは比較的低く、フェアウェイに留まる可能性があり、場合によってはフェアウェイ近くにヒットする可能性があります。 OB ライン";
	public static final String STRAIGHT_SLICE_CAUSE = "クラブの方向は目標方向の右方向で、ボールはほぼ真っすぐに飛び、徐々に右に移動します。";
	public static final String STRAIGHT_SLICE_SUGGEST = "軌道は正しいが、ボールに触れたときにクラブフェースが少し開いています。この問題を改善できれば、飛距離と方向性が改善されます。";

	public static final String PUSH_SLICE = "スライスを押してボールを右に引っ張ります";
	public static final String PUSH_SLICE_TRAJECTORY = "ターゲットの右に行ってから右にフェードする巨大なフック (P7) ボールに触れたときのクラブフェースが広すぎ、スイング軌道はインサイドからアウトサイドですが、その幅は狭くなります。クラブフェースの角度よりも大きくなります。右に飛んで、また右に曲がります)"; // 6.プッシュ
	public static final String PUSH_SLICE_CAUSE = "バックスイングが急すぎるか、ダウンスイングが突然インサイドアウトから過度になりすぎるか、バックスイングがインサイドすぎてインサイドアウトに押されます。";
	public static final String PUSH_SLICE_SUGGEST = "バックスイング中、軸を維持します。P2 では、シャフトは足の平行線と平行になり、クラブフェースの角度を維持します。P2 から P4 までの胸椎は完全に回転します。ダウンスイング中、軸の中心はP3で左に重力がかかり、左半身がリードすると同時に、P5からP6まで右肩と右肘の外旋を維持し、P5.5で左手首を屈曲し、手首の角度をボールをパスして、P10 に曲がって終了します。";

	public static final String PULL_SLICE = "プルスライス";
	public static final String PULL_SLICE_TRAJECTORY = "ボールをターゲットの左側に残し、その後右にカーブするフック (P7 がボールに触れたときにクラブフェースの角度は閉じますが、クラブフェースの角度はスイング軌道よりも小さい) ";
	public static final String PULL_SLICE_CAUSE = "通常、バックスイング中に P2 が内側に入りすぎて、腕と体が近づきすぎてスタックしてしまうことが原因です。代わりに、ダウンスイングが外側から行われたときに右手がそれを補わなければなりません。または軸がオフセットしすぎています。最も一般的な問題は手羽先です。";
	public static final String PULL_SLICE_SUGGEST = "バックスイング中は軸を維持します。P2 では、シャフトは足の平行線と平行になり、クラブフェイスの角度を維持します。P2 から P4 までの胸椎は完全に回転します。ダウンスイング中、軸の中心はP3で左に重力がかかり、左半身がリードすると同時に、P5からP6まで右肩と右肘の外旋を維持し、P5.5で左手首を屈曲し、手首の角度をボールをパスして、P10 に曲がって終了します。";

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
	public static final String THE_TOP = "バッティングエラーによりショットまたはゴロになりました";
	public static final String DUFF = "ボールを打つ際のミスにより、最初にボールの後ろの芝生に当たり、その後ボールを打ちました。";
	public static final String SHANK = "バッティングエラーによる四球";
	public static final String LACK_OF_STRENGTH = "体力不足";

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
