package dev.cataclysmnouveau;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("cataclysm_nouveau_maledictus_spin") @PrefixGameTestTemplate(false)
public final class MaledictusSpinTests {
    @GameTest(template="empty",batch="spin_0",timeoutTicks=100)
    public static void allyAndNativeTiming(GameTestHelper h){NativeAbilityTests.maledictusSpinNativeAllyAndSteering(h);}
    @GameTest(template="empty",batch="spin_1",timeoutTicks=100)
    public static void positionAndFailure(GameTestHelper h){NativeAbilityTests.maledictusSpinPositionAndFailure(h);}
    @GameTest(template="empty",batch="spin_2",timeoutTicks=100)
    public static void shield(GameTestHelper h){NativeAbilityTests.maledictusSpinSecondStrikeBreaksShield(h);}
    @GameTest(template="empty",batch="spin_3",timeoutTicks=100)
    public static void phantomProtocol(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
}
