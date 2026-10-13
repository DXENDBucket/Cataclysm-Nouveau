package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_maledictus_formations") @PrefixGameTestTemplate(false)
public final class MaledictusFormationTests {
 @GameTest(template="empty",batch="maledictus_formation_0",timeoutTicks=100) public static void spiral(GameTestHelper h){NativeAbilityTests.maledictusSpiralNativeAndOneFee(h);}
 @GameTest(template="empty",batch="maledictus_formation_1",timeoutTicks=100) public static void healthRings(GameTestHelper h){NativeAbilityTests.maledictusRingHealthPhases(h);}
 @GameTest(template="empty",batch="maledictus_formation_2",timeoutTicks=100) public static void partialGround(GameTestHelper h){NativeAbilityTests.maledictusPartialRingAndRefund(h);}
 @GameTest(template="empty",batch="maledictus_formation_3",timeoutTicks=100) public static void allyCleave(GameTestHelper h){NativeAbilityTests.maledictusCleaveExecution(h,false);}
 @GameTest(template="empty",batch="maledictus_formation_4",timeoutTicks=100) public static void positionCleave(GameTestHelper h){NativeAbilityTests.maledictusCleaveExecution(h,true);}
 @GameTest(template="empty",batch="maledictus_formation_5",timeoutTicks=100) public static void soulBurst(GameTestHelper h){NativeAbilityTests.maledictusSoulNativeDamageVisualsAndClear(h);}
 @GameTest(template="empty",batch="maledictus_formation_6",timeoutTicks=100) public static void parallelSoul(GameTestHelper h){NativeAbilityTests.maledictusInvalidActorAndParallelSoul(h);}
 @GameTest(template="empty",batch="maledictus_formation_7",timeoutTicks=100) public static void phantomProtocol(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
}
