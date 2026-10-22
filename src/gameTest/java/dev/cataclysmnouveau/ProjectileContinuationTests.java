package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_continuation") @PrefixGameTestTemplate(false)
public final class ProjectileContinuationTests {
 @GameTest(template="empty",batch="continuation_0",timeoutTicks=100) public static void quake(GameTestHelper h){RemnantTests.nativeQuakeGroundedContactsSuccessfulDamageAndRepeatedContinuation(h);}
 @GameTest(template="empty",batch="continuation_1",timeoutTicks=100) public static void axe(GameTestHelper h){AptrgangrTests.piercingBladeNativeMotionDamageFadeAndRepeatedContinuation(h);}
 @GameTest(template="empty",batch="continuation_2",timeoutTicks=100) public static void wave(GameTestHelper h){NativeAbilityTests.nativeWaveContactsAlliesRepeatedContinuationAndLifetime(h);}
 @GameTest(template="empty",batch="continuation_3",timeoutTicks=100) public static void spears(GameTestHelper h){NativeAbilityTests.scyllaSpearCarrierPaymentContinuationsAndEmptyMana(h);}
 @GameTest(template="empty",batch="continuation_4",timeoutTicks=100) public static void spark(GameTestHelper h){NativeAbilityTests.electricSparkNativeLandingAndRepeatedPayment(h);}
}
