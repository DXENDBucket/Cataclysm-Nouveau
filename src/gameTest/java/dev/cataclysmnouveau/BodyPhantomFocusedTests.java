package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_phantom_focused") @PrefixGameTestTemplate(false)
public final class BodyPhantomFocusedTests {
 @GameTest(template="empty",batch="phantom_combo",timeoutTicks=100) public static void thunderCombo(GameTestHelper h){NativeAbilityTests.bodyVisualLifecycle(h,Ability.THUNDER_COMBO);}
 @GameTest(template="empty",batch="phantom_protocol",timeoutTicks=100) public static void protocol(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
 @GameTest(template="empty",batch="phantom_close",timeoutTicks=100) public static void closeFallback(GameTestHelper h){NativeAbilityTests.bodyPhantomCloseSurvivesClearedAnimationAndRemovedOwner(h);}
}
