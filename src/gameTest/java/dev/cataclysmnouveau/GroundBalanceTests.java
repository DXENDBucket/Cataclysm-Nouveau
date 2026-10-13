package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_ground_balance") @PrefixGameTestTemplate(false)
public final class GroundBalanceTests {
 @GameTest(template="empty",batch="ground_balance_0",timeoutTicks=100) public static void groundNativeDamage(GameTestHelper h){NativeAbilityTests.nativeGroundEffectsDamageFireAlliesAndCleanup(h);}
 @GameTest(template="empty",batch="ground_balance_1",timeoutTicks=100) public static void groundRefundAndOwner(GameTestHelper h){NativeAbilityTests.groundFailureBudgetsBlockPlacementAndDamageOwner(h);}
 @GameTest(template="empty",batch="ground_balance_2",timeoutTicks=100) public static void halberdRageDamage(GameTestHelper h){NativeAbilityTests.maledictusHalberdNative(h);}
}
