package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_vertical") @PrefixGameTestTemplate(false)
public final class SteeringVerticalTests {
 @GameTest(template="empty",batch="vertical_view",timeoutTicks=100) public static void verticalView(GameTestHelper h){NativeAbilityTests.verticalViewUsesActualActorYaw(h);}
}
