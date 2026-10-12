package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_positions") @PrefixGameTestTemplate(false)
public final class PositionBodyTests {
 @GameTest(template="empty",batch="position_body_0",timeoutTicks=100) public static void thunder_combo(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="position_body_1",timeoutTicks=100) public static void anchor_spin(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.ANCHOR_SPIN);}
 @GameTest(template="empty",batch="position_body_2",timeoutTicks=100) public static void anchor_leap(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.ANCHOR_LEAP);}
 @GameTest(template="empty",batch="position_body_3",timeoutTicks=100) public static void harbinger_charge(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.HARBINGER_CHARGE);}
 @GameTest(template="empty",batch="position_body_4",timeoutTicks=100) public static void gravity_punch(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GRAVITY_PUNCH);}
 @GameTest(template="empty",batch="position_body_5",timeoutTicks=100) public static void guardian_uppercut(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GUARDIAN_UPPERCUT);}
 @GameTest(template="empty",batch="position_body_6",timeoutTicks=100) public static void guardian_blink(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GUARDIAN_BLINK);}
 @GameTest(template="empty",batch="position_body_7",timeoutTicks=100) public static void guardian_air_strike(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GUARDIAN_AIR_STRIKE);}
 @GameTest(template="empty",batch="position_body_8",timeoutTicks=100) public static void guardian_rocket_punch(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GUARDIAN_ROCKET_PUNCH);}
 @GameTest(template="empty",batch="position_body_9",timeoutTicks=100) public static void shoulder_check(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.SHOULDER_CHECK);}
 @GameTest(template="empty",batch="position_reg_0",timeoutTicks=100) public static void positionActionsCoexistAndInvalidOriginKeepsLiveBody(GameTestHelper h){NativeAbilityTests.positionActionsCoexistAndInvalidOriginKeepsLiveBody(h);}
 @GameTest(template="empty",batch="position_reg_1",timeoutTicks=100) public static void bodyPhantomProtocolTracksNativeStatesAndActualExecutor(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
 @GameTest(template="empty",batch="position_reg_2",timeoutTicks=100) public static void bodyPhantomCloseSurvivesClearedAnimationAndRemovedOwner(GameTestHelper h){NativeAbilityTests.bodyPhantomCloseSurvivesClearedAnimationAndRemovedOwner(h);}
 @GameTest(template="empty",batch="position_reg_3",timeoutTicks=100) public static void physicalActionsReplaceAcrossSeriesKeepOtherActorsAndRestoreGravity(GameTestHelper h){NativeAbilityTests.physicalActionsReplaceAcrossSeriesKeepOtherActorsAndRestoreGravity(h);}
 @GameTest(template="empty",batch="position_reg_4",timeoutTicks=100) public static void guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(GameTestHelper h){NativeAbilityTests.guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(h);}
 @GameTest(template="empty",batch="position_reg_5",timeoutTicks=100) public static void guardianBlinkMovesRealActorAndUsesNativeHit(GameTestHelper h){NativeAbilityTests.guardianBlinkMovesRealActorAndUsesNativeHit(h);}
 @GameTest(template="empty",batch="position_reg_6",timeoutTicks=100) public static void ignisPositionSwordDanceKeepsGeometryOwnershipAndIndependentActor(GameTestHelper h){NativeAbilityTests.ignisPositionSwordDanceKeepsGeometryOwnershipAndIndependentActor(h);}
}
