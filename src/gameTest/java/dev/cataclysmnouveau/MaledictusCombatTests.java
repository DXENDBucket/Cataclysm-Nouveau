package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_maledictus_combat") @PrefixGameTestTemplate(false)
public final class MaledictusCombatTests {
 @GameTest(template="empty",batch="maledictus_combat_0",timeoutTicks=100) public static void smash(GameTestHelper h){NativeAbilityTests.maledictusAerialSmashNativePhases(h);}
 @GameTest(template="empty",batch="maledictus_combat_1",timeoutTicks=100) public static void volley(GameTestHelper h){NativeAbilityTests.maledictusAerialVolleyNativeChildren(h);}
 @GameTest(template="empty",batch="maledictus_combat_2",timeoutTicks=100) public static void rush(GameTestHelper h){NativeAbilityTests.maledictusRushHealthChain(h);}
 @GameTest(template="empty",batch="maledictus_combat_3",timeoutTicks=100) public static void wave(GameTestHelper h){NativeAbilityTests.maledictusGroundWaveAndNoCombo(h);}
 @GameTest(template="empty",batch="maledictus_combat_4",timeoutTicks=100) public static void permissions(GameTestHelper h){NativeAbilityTests.maledictusCombatPermissionAndRecipes(h);}
 @GameTest(template="empty",batch="maledictus_combat_5",timeoutTicks=100) public static void uppercut(GameTestHelper h){NativeAbilityTests.maledictusUppercutAndCombo(h,false);}
 @GameTest(template="empty",batch="maledictus_combat_6",timeoutTicks=100) public static void combo(GameTestHelper h){NativeAbilityTests.maledictusUppercutAndCombo(h,true);}
}
