package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_coexist") @PrefixGameTestTemplate(false)
public final class AnchorCoexistenceTests {
    @GameTest(template="empty",timeoutTicks=100) public static void actualIronBreathAndSword(GameTestHelper h){NativeAbilityTests.actualIronChannelAndSwordShareActor(h);}
}
