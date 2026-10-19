package dev.cataclysmnouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("cataclysm_nouveau_leviathan_ownership") @PrefixGameTestTemplate(false)
public final class LeviathanOwnerTests {
    @GameTest(template="empty",batch="lv_owner_0",timeoutTicks=100)
    public static void nativeSynchronizedCaster(GameTestHelper h){LeviathanVolleyTests.nativeTripleTimesPerRayBillingAndFixedDirection(h);}
    @GameTest(template="empty",batch="lv_owner_1",timeoutTicks=100)
    public static void allyOwnerAndEmitter(GameTestHelper h){LeviathanVolleyTests.allyReaimLockAndExtendedRaysKeepSeparateDirections(h);}
    @GameTest(template="empty",batch="lv_sound_0",timeoutTicks=100)
    public static void invocationSoundsUseRealPositions(GameTestHelper h) {
        var sounds=new java.util.ArrayList<net.neoforged.neoforge.event.PlayLevelSoundEvent>();
        java.util.function.Consumer<net.neoforged.neoforge.event.PlayLevelSoundEvent> observer=event->{sounds.add(event);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        try(var a=new NativeAbilityTests.Arena(h)) {
            var point=a.center.add(0,3,0);var hit=new net.minecraft.world.phys.BlockHitResult(point,net.minecraft.core.Direction.UP,net.minecraft.core.BlockPos.containing(point),false);
            a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,hit,false);a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,hit,false);
            for(int i=0;i<20;i++)dev.arsconflux.api.execution.ExecutionSessions.tick();
            var ids=java.util.List.of(com.github.L_Ender.cataclysm.init.ModSounds.ABYSS_BLAST_ONLY_CHARGE.get(),com.github.L_Ender.cataclysm.init.ModSounds.LEVIATHAN_STUN_ROAR.get());
            for(var id:ids) {
                var matching=sounds.stream().filter(e->e.getSound()!=null && e.getSound().value()==id).toList();
                h.assertTrue(matching.size()==1 && matching.getFirst() instanceof net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition,"One positional native sound, no unresolved frame ID or duplicates: "+id);
                var position=(net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition)matching.getFirst();
                h.assertTrue(position.getPosition().distanceToSqr(point)<.001,"Sound follows the actual execution anchor");
            }
            h.succeed();
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);}
    }
}
