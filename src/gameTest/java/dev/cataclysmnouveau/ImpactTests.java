package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_impacts") @PrefixGameTestTemplate(false)
public final class ImpactTests {
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void rejectedContacts(GameTestHelper h){NativeAbilityTests.rejectedProjectileContactsKeepNativeAftermath(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void nativeBlocks(GameTestHelper h){NativeAbilityTests.nativeFlightPaymentContinuationAndCreative(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void anchor(GameTestHelper h){NativeAbilityTests.anchorRetainsReturnAndSingleContinuation(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void wave(GameTestHelper h){NativeAbilityTests.repeatingAnchorAndWavePayWithoutAmbientDamage(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void harbinger(GameTestHelper h){NativeAbilityTests.harbingerFlightFeesContinuationAndArsMotion(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void spear(GameTestHelper h){NativeAbilityTests.scyllaSpearCarrierPaymentContinuationsAndEmptyMana(h);}
 @GameTest(template="empty",templateNamespace="cataclysm_nouveau_impacts",timeoutTicks=100) public static void lightningField(GameTestHelper h){NativeAbilityTests.electricSparkNativeLandingAndRepeatedPayment(h);}
}
