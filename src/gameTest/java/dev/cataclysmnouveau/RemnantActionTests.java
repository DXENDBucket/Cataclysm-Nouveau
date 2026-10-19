package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.init.ModEffect;
import com.github.L_Ender.cataclysm.entity.effect.Cm_Falling_Block_Entity;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_remnant_actions") @PrefixGameTestTemplate(false)
public final class RemnantActionTests {
    @GameTest(template="empty",batch="remnant_action_0",timeoutTicks=100)
    public static void tailNativeTimingAllyAimDamageSourceStunAndOneFee(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);host.setYRot(90);a.p.setYRot(0);
            var enemy=a.target(a.center.add(-3,0,0),false);var friend=a.target(enemy.position(),true);
            var behind=a.target(a.center.add(0,0,3),false);
            a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,new EntityHitResult(host),true);
            near(h,mana(a),9810,"One augmented tail quote");step(24);near(h,enemy.getHealth(),500,"Native twenty-five tick strike retained");step(1);
            near(h,500-enemy.getHealth(),60,"Native twenty-five plus capped five percent and bounded augment ten");
            near(h,friend.getHealth(),500,"Ally protected");near(h,behind.getHealth(),500,"Tail uses executor heading rather than payer");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p && enemy.getEffect(ModEffect.EFFECTSTUN).getDuration()==100,"Real source and native five second stun");
            step(33);h.assertTrue(!RemnantActions.isActing(host) && !ExecutionSessions.isOccupied(host,ExecutionClaim.ENTITY_ACTION),"Native recovery releases actor");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_1",timeoutTicks=100)
    public static void chargeNativePreparationMotionDamageAndNoTerrainBreaking(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);host.setOnGround(true);host.setYRot(0);
            var enemy=a.target(a.center.add(0,0,1),false);enemy.setOnGround(true);var friend=a.target(enemy.position(),true);
            var rock=BlockPos.containing(a.center.add(1,0,0));h.getLevel().setBlockAndUpdate(rock,Blocks.STONE.defaultBlockState());
            a.cast(GlyphCatalog.REMNANT_CHARGE,new EntityHitResult(host),true);near(h,mana(a),9690,"One augmented charge quote");
            step(69);near(h,enemy.getHealth(),500,"Native seventy-tick preparation retained");near(h,host.getDeltaMovement().horizontalDistance(),0,"No early forward impulse");
            step(1);step(4);h.assertTrue(host.getDeltaMovement().z>1 && Math.abs(host.getDeltaMovement().x)<.001,"Native state-goal drives forward charge");
            near(h,500-enemy.getHealth(),110,"Native fifty plus capped ten percent then bounded ten augment");near(h,friend.getHealth(),500,"Native collision respects allies");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p && h.getLevel().getBlockState(rock).is(Blocks.STONE),"Real source and opt-in non-destructive charge");
            host.horizontalCollision=true;step(1);h.assertTrue(!RemnantActions.isActing(host),"Physical wall collision ends charge");near(h,mana(a),9690,"Contact never charges a second time");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_2",timeoutTicks=100)
    public static void detachedChargeMovesItsPhantomAndStopsAtRealWall(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            a.p.setYRot(0);a.p.setXRot(0);var original=a.p.position();
            for(int x=-1;x<=1;x++)for(int y=1;y<=3;y++)h.getLevel().setBlockAndUpdate(a.base.offset(x,y,10),Blocks.STONE.defaultBlockState());
            a.cast(GlyphCatalog.REMNANT_CHARGE,block(a.center),false);step(100);
            h.assertTrue(frames.stream().anyMatch(f->f.animation()==11 && f.position().z>a.center.z+2) && frames.getLast().closed(),"Position invocation really moves and ends at collision");
            h.assertTrue(frames.stream().allMatch(BodyPhantomPayload::positionAnchored),"No payer fallback in visual anchors");
            near(h,a.p.position().distanceTo(original),0,"Detached motion never moves payer");h.assertTrue(h.getLevel().getBlockState(a.base.offset(0,1,10)).is(Blocks.STONE),"Wall remains intact");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_3",timeoutTicks=100)
    public static void halfHealthStompUsesRootPhaseAndCompletesSecondWave(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            force(a);a.p.setHealth(5);a.p.setYRot(0);a.p.setXRot(0);
            var host=a.target(a.center,true);host.setYRot(0);
            var enemy=a.target(a.center.add(1.4,0,3),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.REMNANT_STOMP,new EntityHitResult(host),true);
            h.assertTrue(!ExecutionSessions.isOccupied(host,ExecutionClaim.ENTITY_ACTION),"Stomp stays an independent field");
            step(27);near(h,enemy.getHealth(),500,"First stomp at tick28");step(1);h.assertTrue(enemy.getHealth()<500,"First native stomp hits");
            step(21);enemy.setHealth(500);enemy.invulnerableTime=0;step(1);
            near(h,500-enemy.getHealth(),45,"Second native stomp retains damage and one augmentation");near(h,friend.getHealth(),500,"Both waves protect allies");
            step(16);int before=fragments(a).size();step(2);h.assertTrue(fragments(a).size()>before,"Final second-wave ground fragments at tick68 are not truncated at native goal timeout66");
            near(h,mana(a),9750,"Both stomps share original quote");h.assertTrue(host.getHealth()==500,"Full-health ally still receives half-health payer double stomp");
            var children=fragments(a);a.p.discard();step(1);h.assertTrue(children.stream().allMatch(Entity::isRemoved),"Root loss cleans every fragment");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_4",timeoutTicks=100)
    public static void paidReplacementAuthorityAndUnfundedRejection(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);host.setOnGround(true);var enemy=a.target(a.center.add(0,0,2),false);
            a.cast(GlyphCatalog.REMNANT_CHARGE,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Hostile executor rejected and refunded");
            a.cast(GlyphCatalog.REMNANT_CHARGE,new EntityHitResult(host),false);h.assertTrue(RemnantActions.isActing(host),"Charge owns real ally action");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,new EntityHitResult(host),false);
            h.assertTrue(RemnantActions.isActing(host),"Unfunded replacement leaves old charge intact");
            CapabilityRegistry.getMana(a.p).setMana(10000);a.cast(GlyphCatalog.MALEDICTUS_UPPERCUT,new EntityHitResult(host),false);
            h.assertTrue(!RemnantActions.isActing(host) && MaledictusCasting.isActing(host),"Another Boss action interrupts charge through shared claim");
            a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,new EntityHitResult(host),false);
            h.assertTrue(RemnantActions.isActing(host) && !MaledictusCasting.isActing(host),"Remnant action can replace other Boss action");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);
            h.assertTrue(!RemnantActions.isActing(host) && !ExecutionSessions.isOccupied(host,ExecutionClaim.ENTITY_ACTION),"Authority loss releases AI and claim");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_5",timeoutTicks=100)
    public static void creativeEmptyManaAndDetachedTailCleanup(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            a.p.setYRot(0);a.p.setXRot(0);a.p.setGameMode(GameType.CREATIVE);CapabilityRegistry.getMana(a.p).setMana(0);
            var enemy=a.target(a.center.add(0,0,3),false);a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,block(a.center),false);step(25);
            near(h,500-enemy.getHealth(),50,"Position tail uses native damage even at creative empty mana");near(h,mana(a),0,"Creative pays no mana");
            h.assertTrue(frames.stream().allMatch(f->f.positionAnchored() && f.animation()==16),"Tail visual uses native position and animation");
            a.p.discard();step(1);h.assertTrue(frames.getLast().closed(),"Detached action still obeys root lifetime");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_action_6",timeoutTicks=100)
    public static void completeVisualProtocol(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="remnant_action_7",timeoutTicks=100)
    public static void fullHealthStompRegression(GameTestHelper h){RemnantTests.stompNativeTimingDamageCapSourceAndGroundBlocks(h);}
    @GameTest(template="empty",batch="remnant_action_8",timeoutTicks=100)
    public static void tripleQuakeDamageRegression(GameTestHelper h){RemnantSecondTests.nativeThreeSlamsCountsDamageOwnershipAndChildAmplification(h);}
    @GameTest(template="empty",batch="remnant_action_9",timeoutTicks=100)
    public static void offensiveStompVisualKeepsSelectedPointAndPoweredState(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            force(a);a.p.setHealth(5);var enemy=a.target(a.center,false);
            a.cast(GlyphCatalog.REMNANT_STOMP,new EntityHitResult(enemy),false);step(1);
            h.assertTrue(!frames.isEmpty() && frames.stream().allMatch(f->f.positionAnchored() && f.animation()==13 && f.flags()==1 && f.position().distanceTo(a.center)<.01),"Enemy hit locates an offensive double stomp without borrowing payer body or visual anchor");
            step(83);h.assertTrue(frames.getLast().closed(),"Full double stomp naturally closes its position phantom");near(h,mana(a),9760,"Natural double stomp still pays once");h.succeed();
        }
    }
    private static void force(NativeAbilityTests.Arena a){for(int x=(a.base.getX()-32)>>4;x<=(a.base.getX()+32)>>4;x++)for(int z=(a.base.getZ()-32)>>4;z<=(a.base.getZ()+32)>>4;z++)if(!a.h.getLevel().getForcedChunks().contains(ChunkPos.asLong(x,z))){a.h.getLevel().setChunkForced(x,z,true);a.forced.add(new ChunkPos(x,z));}}
    private static List<Cm_Falling_Block_Entity> fragments(NativeAbilityTests.Arena a){return a.h.getLevel().getEntitiesOfClass(Cm_Falling_Block_Entity.class,new AABB(a.center,a.center).inflate(32));}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double x,double y,String message){h.assertTrue(Math.abs(x-y)<.002,message+": "+x+" != "+y);}
}
