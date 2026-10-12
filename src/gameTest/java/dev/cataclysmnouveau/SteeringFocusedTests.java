package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_steering_focused") @PrefixGameTestTemplate(false)
public final class SteeringFocusedTests {
 @GameTest(template="empty",batch="focus_0",timeoutTicks=100) public static void combo(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="focus_1",timeoutTicks=100) public static void charge(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.HARBINGER_CHARGE);}
 @GameTest(template="empty",batch="focus_2",timeoutTicks=100) public static void blink(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.GUARDIAN_BLINK);}
 @GameTest(template="empty",batch="focus_3",timeoutTicks=100) public static void ignis(GameTestHelper h){NativeAbilityTests.sharedBodySteeringUsesSelfAndAllyIdentically(h,Ability.IGNIS_SWORD_COMBO);}
 @GameTest(template="empty",batch="focus_4",timeoutTicks=100) public static void gravity(GameTestHelper h){NativeAbilityTests.sharedSteeringLocksDamageAndVisualAtStrike(h);}
 @GameTest(template="empty",batch="focus_5",timeoutTicks=100) public static void permission(GameTestHelper h){NativeAbilityTests.beneficialHostileHitsRefundAndRetainExistingAction(h);}
}
