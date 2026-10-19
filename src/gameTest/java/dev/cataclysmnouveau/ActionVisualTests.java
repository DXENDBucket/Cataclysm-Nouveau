package dev.cataclysmnouveau;

import dev.cataclysmnouveau.casting.*;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import static dev.cataclysmnouveau.casting.VisualSampleClock.Step.*;

/** Server checks validate visual timing/protocol, never claim a rendered appearance check. */
@GameTestHolder("cataclysm_nouveau_action_visuals") @PrefixGameTestTemplate(false)
public final class ActionVisualTests {
    @GameTest(template="empty",batch="visual_clock",timeoutTicks=100)
    public static void renderRateAndPacketGapsDoNotMultiplyParticles(GameTestHelper h) {
        var clock=new VisualSampleClock();int samples=0;
        for(int tick=0;tick<20;tick++)for(int frame=0;frame<120;frame++) {
            var step=clock.sample(tick,1,tick);
            if(step!=SKIP)samples++;
            if(frame==0)h.assertTrue(step==(tick==0?RESET:ADVANCE),"Continuous native clock");
        }
        h.assertTrue(samples==20,"120 renders per tick still emit only one visual sample");
        for(int tick=20;tick<30;tick++)h.assertTrue(clock.sample(tick,1,19)==SKIP,"Stale packet age cannot repeat trail emission");
        h.assertTrue(clock.sample(30,1,40)==RESET,"A packet gap primes the blade instead of bridging an unsampled distance");
        h.assertTrue(clock.sample(31,2,0)==RESET,"Native phase transition re-primes the blade");
        h.assertTrue(clock.sample(40,2,1)==RESET,"Returning to a previously culled view cannot bridge the missing world ticks");h.succeed();
    }
    @GameTest(template="empty",batch="visual_windows",timeoutTicks=100)
    public static void nativeMaledictusTrailWindows(GameTestHelper h) {
        h.assertTrue(!BodyPhantomAnimations.maledictusAfterimage(11,23) && BodyPhantomAnimations.maledictusAfterimage(11,24)
            && BodyPhantomAnimations.maledictusAfterimage(12,33) && !BodyPhantomAnimations.maledictusAfterimage(12,34),"First rush exact native window");
        h.assertTrue(!BodyPhantomAnimations.maledictusAfterimage(15,15) && BodyPhantomAnimations.maledictusAfterimage(16,25)
            && !BodyPhantomAnimations.maledictusAfterimage(16,26) && BodyPhantomAnimations.maledictusAfterimage(17,24)
            && !BodyPhantomAnimations.maledictusAfterimage(17,25),"Later rushes retain their different endings");
        h.assertTrue(BodyPhantomAnimations.maledictusAfterimage(3,50) && BodyPhantomAnimations.maledictusAfterimage(24,60)
            && !BodyPhantomAnimations.maledictusAfterimage(18,20) && !BodyPhantomAnimations.maledictusAfterimage(19,24)
            && !BodyPhantomAnimations.maledictusAfterimage(24,-1),"Aerial trails, no invented native spin/combo afterimages");h.succeed();
    }
    @GameTest(template="empty",batch="visual_protocol",timeoutTicks=100)
    public static void nativePosesAndAllyAnchors(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="visual_ignis",timeoutTicks=100)
    public static void swordDancePhasesAndClose(GameTestHelper h){NativeAbilityTests.ignisSwordDanceVisualPacketsFollowBothPhasesAndCancellation(h);}
    @GameTest(template="empty",batch="visual_mine_fee",timeoutTicks=100)
    public static void minefieldFee(GameTestHelper h){LeviathanSecondTests.minefieldNativeCountDelayAndGroupedFee(h);}
    @GameTest(template="empty",batch="visual_array_fee",timeoutTicks=100)
    public static void bombardmentFee(GameTestHelper h){LeviathanSecondTests.bombardmentNativePhasesLiveTargetAndChildCleanup(h);}
}
