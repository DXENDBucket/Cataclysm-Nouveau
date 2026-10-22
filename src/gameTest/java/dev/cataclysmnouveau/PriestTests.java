package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.Deepling.Deepling_Priest_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_priest") @PrefixGameTestTemplate(false)
public final class PriestTests {
    @GameTest(template="empty",batch="priest_0",timeoutTicks=100)
    public static void nativeGazeTimingVisibilityDamageAndArsBonus(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        var walls=new ArrayList<BlockPos>();
        var sounds=new ArrayList<net.neoforged.neoforge.event.PlayLevelSoundEvent>();
        java.util.function.Consumer<net.neoforged.neoforge.event.PlayLevelSoundEvent> observer=sounds::add;
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.PlayLevelSoundEvent.class,observer);
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(3);
            var frame=new SpellPriest(h.getLevel(),CastContext.detached(a.p,block(a.center),null),0);
            frame.setPos(a.center);var light=frame.getEyePosition();frame.discard();
            var victim=a.target(a.center.add(0,0,3),false);face(victim,light);
            var ally=a.target(a.center.add(0,0,3),true);face(ally,light);
            var away=a.target(a.center.add(0,0,-3),false);face(away,away.getEyePosition().add(0,0,-1));
            var hidden=a.target(a.center.add(4,0,0),false);face(hidden,light);
            for(int y=0;y<=4;y++)for(int z=-1;z<=1;z++){
                var p=a.base.offset(2,y,z);walls.add(p);h.getLevel().setBlockAndUpdate(p,Blocks.STONE.defaultBlockState());
            }
            a.cast(GlyphCatalog.PRIEST_GLARE,block(a.center),true);near(h,mana(a),9910,"One eighty-mana fee and ten for amplification");
            step(18);near(h,victim.getHealth(),500,"Native windup ends before tick nineteen");
            var lights=sounds.stream().filter(e->e.getSound()!=null && e.getSound().value()==com.github.L_Ender.cataclysm.init.ModSounds.DEEPLING_LIGHT.get()).toList();
            h.assertTrue(lights.size()==1 && lights.getFirst() instanceof net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition p && p.getPosition().distanceToSqr(a.center)<.001,"Native tick18 light sound uses anchor position once");
            step(1);
            near(h,500-victim.getHealth(),7.8,"Native four times .7 plus bounded2 plus Ars3 once");
            h.assertTrue(victim.getLastDamageSource().getEntity()==a.p,"Real caster owns indirect magic");
            h.assertTrue(victim.hasEffect(MobEffects.BLINDNESS) && victim.getEffect(MobEffects.BLINDNESS).getDuration()==80,"Native four-second blindness follows successful hurt");
            near(h,ally.getHealth(),500,"Core friends are excluded");near(h,away.getHealth(),500,"Looking away defeats native gaze");near(h,hidden.getHealth(),500,"Opaque wall defeats native line of sight");
            step(1);near(h,500-victim.getHealth(),7.8,"Native damage immunity is respected");
            step(26);victim.invulnerableTime=0;step(1);near(h,500-victim.getHealth(),7.8,"No glare damage from tick47 onward");
            step(10);h.assertTrue(frames.getLast().closed() && frames.getLast().age()==57,"Full native57tick animation closes");
            near(h,mana(a),9910,"Repeated gaze checks belong to the original fee");h.succeed();
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);walls.forEach(p->h.getLevel().setBlockAndUpdate(p,Blocks.AIR.defaultBlockState()));}
    }
    @GameTest(template="empty",batch="priest_1",timeoutTicks=100)
    public static void allyFollowsActualExecutorAndCoexistsWithBody(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);var victim=a.target(a.center.add(0,0,3),false);
            var frame=new SpellPriest(h.getLevel(),CastContext.detached(a.p,new EntityHitResult(host),null).withExecutor(host),0);
            frame.setPos(a.center);face(victim,frame.getEyePosition());frame.discard();
            a.cast(GlyphCatalog.BERSERKER_SPIN,new EntityHitResult(host),false);
            a.cast(GlyphCatalog.PRIEST_GLARE,new EntityHitResult(host),false);
            h.assertTrue(BerserkerCasting.isActing(host),"Area effect does not replace allied body action");
            step(19);near(h,500-victim.getHealth(),2.8,"Ally uses native gaze damage");
            h.assertTrue(victim.getLastDamageSource().getDirectEntity()==host && victim.getLastDamageSource().getEntity()==a.p,"Emitter and damage owner remain separate");
            host.setPos(a.center.add(12,0,0));victim.invulnerableTime=0;step(1);near(h,500-victim.getHealth(),2.8,"Glare follows moved ally instead of remaining at original point");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);
            h.assertTrue(!BerserkerCasting.isActing(host),"Revoked authority closes existing body and parallel glare");
            near(h,mana(a),9740,"Parallel effects charge their own fee exactly once");h.succeed();
        }
    }
    @GameTest(template="empty",batch="priest_2",timeoutTicks=100)
    public static void rejectedTargetsCreativeAndRemovedOwner(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.PRIEST_GLARE,new EntityHitResult(enemy),false);
            near(h,mana(a),10000,"Hostile executor fails without charging or caster fallback");h.assertTrue(frames.isEmpty(),"Rejected target creates no phantom");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.PRIEST_GLARE,block(a.center),false);h.assertTrue(frames.isEmpty(),"Unpaid effect never begins");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.PRIEST_GLARE,new EntityHitResult(a.p),false);
            h.assertTrue(!frames.isEmpty() && !frames.getLast().closed(),"Creative empty-mana effect begins");near(h,mana(a),0,"Creative fee bypass");
            a.p.discard();step(1);h.assertTrue(frames.getLast().closed(),"Owner loss sends immediate visual close");h.succeed();
        }
    }
    @GameTest(template="empty",batch="priest_3",timeoutTicks=100)
    public static void nativePriestAndVisualProtocol(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var priest=new Deepling_Priest_Entity(ModEntities.DEEPLING_PRIEST.get(),h.getLevel());priest.setNoAi(true);priest.setNoGravity(true);priest.setPos(a.center);
            priest.setAnimation(Deepling_Priest_Entity.DEEPLING_BLIND);priest.setAnimationTick(19);
            var victim=a.target(a.center.add(0,0,3),false);face(victim,priest.getEyePosition());priest.tick();
            near(h,500-victim.getHealth(),2.8,"Unbound native priest keeps native damage");
            h.assertTrue(victim.getLastDamageSource().getEntity()==priest,"Unbound native source remains priest");priest.discard();
        }
        NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);
    }
    private static void face(LivingEntity observer,Vec3 point){
        var d=point.subtract(observer.getEyePosition());observer.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));
        observer.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));
        // LivingEntity.getViewYRot(1) reads the head, not the body yaw. These no-AI fixtures never rotate it themselves.
        observer.yHeadRot=observer.yHeadRotO=observer.yRotO=observer.getYRot();observer.xRotO=observer.getXRot();
    }
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int count){for(int i=0;i<count;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double actual,double expected,String text){h.assertTrue(Math.abs(actual-expected)<.003,text+": "+actual+" != "+expected);}
}
