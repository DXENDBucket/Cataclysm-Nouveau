package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_steering") @PrefixGameTestTemplate(false)
public final class BeneficialSteeringTests {
 @GameTest(template="empty",batch="steering_0",timeoutTicks=100) public static void thunder_combo(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="steering_1",timeoutTicks=100) public static void anchor_spin(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.ANCHOR_SPIN);}
 @GameTest(template="empty",batch="steering_2",timeoutTicks=100) public static void anchor_leap(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.ANCHOR_LEAP);}
 @GameTest(template="empty",batch="steering_3",timeoutTicks=100) public static void harbinger_charge(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.HARBINGER_CHARGE);}
 @GameTest(template="empty",batch="steering_4",timeoutTicks=100) public static void gravity_punch(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GRAVITY_PUNCH);}
 @GameTest(template="empty",batch="steering_5",timeoutTicks=100) public static void guardian_uppercut(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GUARDIAN_UPPERCUT);}
 @GameTest(template="empty",batch="steering_6",timeoutTicks=100) public static void guardian_blink(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GUARDIAN_BLINK);}
 @GameTest(template="empty",batch="steering_7",timeoutTicks=100) public static void guardian_air_strike(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GUARDIAN_AIR_STRIKE);}
 @GameTest(template="empty",batch="steering_8",timeoutTicks=100) public static void guardian_rocket_punch(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GUARDIAN_ROCKET_PUNCH);}
 @GameTest(template="empty",batch="steering_9",timeoutTicks=100) public static void shoulder_check(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.SHOULDER_CHECK);}
 @GameTest(template="empty",batch="steering_10",timeoutTicks=100) public static void ignis_air_smash(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_AIR_SMASH);}
 @GameTest(template="empty",batch="steering_11",timeoutTicks=100) public static void ignis_sword_combo(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_SWORD_COMBO);}
 @GameTest(template="empty",batch="steering_12",timeoutTicks=100) public static void ignis_earth_shudders(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_EARTH_SHUDDERS);}
 @GameTest(template="empty",batch="steering_13",timeoutTicks=100) public static void ignis_reinforced_smash(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_REINFORCED_SMASH);}
 @GameTest(template="empty",batch="steering_14",timeoutTicks=100) public static void ignis_ultimate(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_ULTIMATE);}
 @GameTest(template="empty",batch="steering_reg_0",timeoutTicks=160) public static void beneficialHostileHitsRefundAndRetainExistingAction(GameTestHelper h){NativeAbilityTests.beneficialHostileHitsRefundAndRetainExistingAction(h);}
 @GameTest(template="empty",batch="steering_reg_1",timeoutTicks=160) public static void sharedSteeringLocksDamageAndVisualAtStrike(GameTestHelper h){NativeAbilityTests.sharedSteeringLocksDamageAndVisualAtStrike(h);}
 @GameTest(template="empty",batch="steering_reg_2",timeoutTicks=160) public static void invalidBodyReplacementRetainsPreviousActionAndItsPhysicalState(GameTestHelper h){NativeAbilityTests.invalidBodyReplacementRetainsPreviousActionAndItsPhysicalState(h);}
 @GameTest(template="empty",batch="steering_reg_3",timeoutTicks=160) public static void positionActionsCoexistAndInvalidOriginKeepsLiveBody(GameTestHelper h){NativeAbilityTests.positionActionsCoexistAndInvalidOriginKeepsLiveBody(h);}
 @GameTest(template="empty",batch="steering_reg_4",timeoutTicks=160) public static void guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(GameTestHelper h){NativeAbilityTests.guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(h);}
 @GameTest(template="empty",batch="steering_reg_5",timeoutTicks=160) public static void thunderComboNativeStrikesSpearsAlliesAndPayment(GameTestHelper h){NativeAbilityTests.thunderComboNativeStrikesSpearsAlliesAndPayment(h);}
 @GameTest(template="empty",batch="steering_reg_6",timeoutTicks=160) public static void shoulderUsesNativeCollisionBoneFractureAndObstacleStop(GameTestHelper h){NativeAbilityTests.shoulderUsesNativeCollisionBoneFractureAndObstacleStop(h);}
 @GameTest(template="empty",batch="steering_pos_0",timeoutTicks=100) public static void position_thunder_combo(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="steering_pos_1",timeoutTicks=100) public static void position_guardian_rocket_punch(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.GUARDIAN_ROCKET_PUNCH);}
 @GameTest(template="empty",batch="steering_pos_2",timeoutTicks=100) public static void position_harbinger_charge(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.HARBINGER_CHARGE);}
}
