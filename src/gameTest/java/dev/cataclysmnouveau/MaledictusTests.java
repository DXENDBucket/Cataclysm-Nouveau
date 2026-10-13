package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_maledictus") @PrefixGameTestTemplate(false)
public final class MaledictusTests {
 @GameTest(template="empty",batch="maledictus_0",timeoutTicks=250) public static void rageState(GameTestHelper h){NativeAbilityTests.maledictusRageState(h);}
 @GameTest(template="empty",batch="maledictus_1",timeoutTicks=100) public static void ragePersistence(GameTestHelper h){NativeAbilityTests.maledictusRageSaveAndCompleteDecay(h);}
 @GameTest(template="empty",batch="maledictus_2",timeoutTicks=100) public static void nativeArrow(GameTestHelper h){NativeAbilityTests.maledictusArrowNativeLaunch(h);}
 @GameTest(template="empty",batch="maledictus_3",timeoutTicks=100) public static void repeatArrow(GameTestHelper h){NativeAbilityTests.maledictusArrowRepeating(h);}
 @GameTest(template="empty",batch="maledictus_4",timeoutTicks=100) public static void nativeHalberd(GameTestHelper h){NativeAbilityTests.maledictusHalberdNative(h);}
 @GameTest(template="empty",batch="maledictus_5",timeoutTicks=150) public static void halberdExpiry(GameTestHelper h){NativeAbilityTests.maledictusHalberdRefundAndExpiry(h);}
}
