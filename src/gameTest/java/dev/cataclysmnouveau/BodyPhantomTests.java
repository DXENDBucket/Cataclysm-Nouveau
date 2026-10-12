package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_phantoms") @PrefixGameTestTemplate(false)
public final class BodyPhantomTests {
 @GameTest(template="empty",batch="body_visual_0",timeoutTicks=100) public static void thunder_combo(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="body_visual_1",timeoutTicks=100) public static void anchor_spin(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.ANCHOR_SPIN);}
 @GameTest(template="empty",batch="body_visual_2",timeoutTicks=100) public static void anchor_leap(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.ANCHOR_LEAP);}
 @GameTest(template="empty",batch="body_visual_3",timeoutTicks=100) public static void harbinger_charge(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.HARBINGER_CHARGE);}
 @GameTest(template="empty",batch="body_visual_4",timeoutTicks=100) public static void gravity_punch(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.GRAVITY_PUNCH);}
 @GameTest(template="empty",batch="body_visual_5",timeoutTicks=100) public static void guardian_uppercut(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.GUARDIAN_UPPERCUT);}
 @GameTest(template="empty",batch="body_visual_6",timeoutTicks=100) public static void guardian_blink(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.GUARDIAN_BLINK);}
 @GameTest(template="empty",batch="body_visual_7",timeoutTicks=100) public static void guardian_air_strike(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.GUARDIAN_AIR_STRIKE);}
 @GameTest(template="empty",batch="body_visual_8",timeoutTicks=100) public static void guardian_rocket_punch(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.GUARDIAN_ROCKET_PUNCH);}
 @GameTest(template="empty",batch="body_visual_9",timeoutTicks=100) public static void shoulder_check(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.SHOULDER_CHECK);}
 @GameTest(template="empty",batch="body_regression_0",timeoutTicks=100) public static void bodyPhantomProtocolTracksNativeStatesAndActualExecutor(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
 @GameTest(template="empty",batch="body_regression_1",timeoutTicks=100) public static void ignisPhantomPayloadKeepsRolesDimensionPoseAndStages(GameTestHelper h){NativeAbilityTests.ignisPhantomPayloadKeepsRolesDimensionPoseAndStages(h);}
 @GameTest(template="empty",batch="body_regression_2",timeoutTicks=100) public static void ignisSwordDanceVisualPacketsFollowBothPhasesAndCancellation(GameTestHelper h){NativeAbilityTests.ignisSwordDanceVisualPacketsFollowBothPhasesAndCancellation(h);}
 @GameTest(template="empty",batch="body_regression_3",timeoutTicks=100) public static void physicalActionsReplaceAcrossSeriesKeepOtherActorsAndRestoreGravity(GameTestHelper h){NativeAbilityTests.physicalActionsReplaceAcrossSeriesKeepOtherActorsAndRestoreGravity(h);}
 @GameTest(template="empty",batch="body_regression_4",timeoutTicks=100) public static void guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(GameTestHelper h){NativeAbilityTests.guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(h);}
 @GameTest(template="empty",batch="body_regression_5",timeoutTicks=100) public static void harbingerChargeMovesAllyHitsEnemyAndLeavesTerrain(GameTestHelper h){NativeAbilityTests.harbingerChargeMovesAllyHitsEnemyAndLeavesTerrain(h);}
 @GameTest(template="empty",batch="body_close_fallback",timeoutTicks=100) public static void closeFallback(GameTestHelper h){NativeAbilityTests.bodyPhantomCloseSurvivesClearedAnimationAndRemovedOwner(h);}
}
