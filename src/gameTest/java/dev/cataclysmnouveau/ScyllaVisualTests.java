package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_scylla_visuals") @PrefixGameTestTemplate(false)
public final class ScyllaVisualTests {
 @GameTest(template="empty",batch="scylla_visual_0",timeoutTicks=100) public static void positionSpin(GameTestHelper h){NativeAbilityTests.scyllaVisualPacketsUseActorOrFixedOrigin(h,true,true);}
 @GameTest(template="empty",batch="scylla_visual_1",timeoutTicks=100) public static void actorSpin(GameTestHelper h){NativeAbilityTests.scyllaVisualPacketsUseActorOrFixedOrigin(h,false,true);}
 @GameTest(template="empty",batch="scylla_visual_2",timeoutTicks=100) public static void positionCombo(GameTestHelper h){NativeAbilityTests.scyllaVisualPacketsUseActorOrFixedOrigin(h,true,false);}
 @GameTest(template="empty",batch="scylla_visual_3",timeoutTicks=100) public static void fixedNativeDamage(GameTestHelper h){NativeAbilityTests.positionBodyKeepsPoseDamageSourcePaymentAndCasterIndependent(h,Ability.ANCHOR_SPIN);}
 @GameTest(template="empty",batch="scylla_visual_4",timeoutTicks=100) public static void actorNativeDamage(GameTestHelper h){NativeAbilityTests.anchorWhirlNativeDamageFriendlyPushAndOwnerCleanup(h);}
}
