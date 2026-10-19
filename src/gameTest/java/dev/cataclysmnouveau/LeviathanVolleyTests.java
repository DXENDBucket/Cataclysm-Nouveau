package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_leviathan_volleys") @PrefixGameTestTemplate(false)
public final class LeviathanVolleyTests {
    @GameTest(template="empty",batch="lv_volley_0",timeoutTicks=100)
    public static void nativeTwelveOrbsTrackingDamageAndOneFee(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            var point=a.center.add(0,3,0);var enemy=a.target(point.add(0,0,6),false);var friend=a.target(enemy.position(),true);
            a.p.setYRot(0);a.p.setXRot(0);a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,block(point),true);
            near(h,mana(a),9690,"One group fee plus one augmentation fee");step(19);h.assertTrue(orbs(a).isEmpty(),"Short preparation precedes native burst");
            step(1);var children=orbs(a);h.assertTrue(children.size()==12,"Native three-layer twelve-orb formation");
            h.assertTrue(children.stream().filter(o->o.yPower<-.001).count()==3 && children.stream().filter(o->Math.abs(o.yPower)<.001).count()==6
                && children.stream().filter(o->o.yPower>.001).count()==3,"Preserve native lower/middle/upper directions");
            h.assertTrue(children.stream().allMatch(o->o.getOwner()==a.p && !o.shouldBeSaved() && ((ManagedProjectile)o).confluxCataclysm$sourceAbility()==Ability.ABYSS_ORB_ARRAY
                && ((ManagedProjectile)o).confluxCataclysm$impact()==null),"Actual owner, transient children, no copied Ars continuation");
            var orb=children.getFirst();for(int i=0;i<40;i++)orb.tick();h.assertTrue(orb.getTracking(),"Native forty-tick delayed homing remains");
            orb.setPos(enemy.position());((ProjectileHitAccess)orb).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(ModEffect.EFFECTABYSSAL_FEAR) && enemy.getLastDamageSource().getEntity()==a.p,"Native damage, fear and root credit");
            near(h,friend.getHealth(),500,"Direct and explosion damage protect allies");near(h,mana(a),9690,"No child fee on hit");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_1",timeoutTicks=100)
    public static void denseBurstUsesCasterHealthAndActualAllyEyes(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            a.p.setHealth(a.p.getMaxHealth()*.5F);var host=a.target(a.center.add(0,2,0),true);
            a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,new EntityHitResult(host),false);host.setPos(host.position().add(2,1,0));step(20);
            var children=orbs(a);h.assertTrue(children.size()==24,"Half-health logical caster selects native twenty-four burst despite full-health ally");
            h.assertTrue(children.stream().allMatch(o->o.position().distanceToSqr(host.getEyePosition())<.001 && o.getOwner()==a.p),"Emit from moving ally eyes, preserve payer owner");
            h.assertTrue(children.stream().map(o->Math.round(o.yPower*10000)).distinct().count()==5,"Native five layers retained");
            near(h,mana(a),9700,"Dense version remains one group fee");a.p.discard();step(1);h.assertTrue(children.stream().allMatch(Entity::isRemoved),"Root loss cancels all unspent orbs");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_2",timeoutTicks=100)
    public static void invalidRecipientsEmptyManaAndCreative(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            var enemy=a.target(a.center,false);
            a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,new EntityHitResult(enemy),false);a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Both reject hostile executors without charging");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,block(a.center.add(0,3,0)),false);
            a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,block(a.center.add(0,3,0)),false);step(90);h.assertTrue(orbs(a).isEmpty() && beams(a).isEmpty(),"Empty mana starts neither ability");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.ABYSS_ORB_ARRAY,block(a.center.add(0,3,0)),false);
            a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,block(a.center.add(4,3,0)),false);step(90);
            h.assertTrue(!orbs(a).isEmpty() && !beams(a).isEmpty(),"Both sequences work with zero creative mana");near(h,mana(a),0,"Creative output stays free");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_3",timeoutTicks=100)
    public static void nativeTripleTimesPerRayBillingAndFixedDirection(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            var point=a.center.add(0,3,0);a.p.setYRot(0);a.p.setXRot(0);var enemy=a.target(point.add(0,0,6),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,block(point),false);a.p.setYRot(90);
            var created=new LinkedHashMap<UUID,Integer>();var all=new ArrayList<Abyss_Blast_Entity>();
            for(int age=1;age<=225;age++) {
                step(1);for(var beam:beams(a))if(!created.containsKey(beam.getUUID())){created.put(beam.getUUID(),age);all.add(beam);}
                if(age==84)near(h,mana(a),10000,"Native initial preparation is unpaid");
                if(age==85)near(h,mana(a),9920,"First actual output second prepaid");
            }
            h.assertTrue(new ArrayList<>(created.values()).equals(List.of(64,109,154)),"Native beam birth times: "+created.values());
            h.assertTrue(all.size()==3 && all.stream().allMatch(b->b.getDuration()==28 && b.caster==a.p && b.getCasterID()==a.p.getId() && !b.shouldBeSaved()),"Three native short rays, real synchronized owner and transient state");
            for(var beam:all)near(h,beam.getYaw(),Math.PI/2,"Fixed origin ignores later payer view changes");
            near(h,mana(a),9520,"Two paid seconds per native 28-tick output, six total");
            h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(ModEffect.EFFECTABYSSAL_BURN),"Actual native damage and burn");near(h,friend.getHealth(),500,"Every ray protects allies");
            h.assertTrue(all.stream().allMatch(Entity::isRemoved),"All three rays finish their native fade");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_4",timeoutTicks=100)
    public static void allyReaimLockAndExtendedRaysKeepSeparateDirections(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            var host=a.target(a.center.add(0,3,0),true);host.setYRot(0);host.setXRot(0);
            h.assertTrue(triple(a,new EntityHitResult(host),1),"Extended allied triple starts");step(64);var first=beams(a).getFirst();
            h.assertTrue(first.getDuration()==42 && first.getCasterID()==a.p.getId() && ((LeviathanBeam)first).confluxLeviathan$executor()==host.getId(),"Duration, synchronized damage owner and actual emitter stay distinct");
            host.setYRot(90);host.setXRot(-20);host.setPos(host.position().add(1,0,0));step(15);
            near(h,first.position().distanceTo(host.getEyePosition()),0,"Shared eye anchor follows actual ally");near(h,first.getYaw(),Math.PI,"First aim window still tracks");near(h,first.getPitch(),Math.toRadians(20),"Ally pitch retained");
            host.setYRot(-90);host.setXRot(0);step(39);near(h,first.getYaw(),Math.PI,"First extended ray remains locked");
            var second=beams(a).stream().filter(b->b!=first).findFirst().orElseThrow();step(1);near(h,second.getYaw(),0,"Second native reaim window opens at 119");near(h,first.getYaw(),Math.PI,"Reaim does not rotate an overlapping earlier ray");
            host.setYRot(0);step(5);near(h,second.getYaw(),Math.PI/2,"Second window can still turn");host.setYRot(180);step(1);near(h,second.getYaw(),Math.PI/2,"Second ray locks at 125");
            var all=new ArrayList<>(beams(a));host.discard();step(1);h.assertTrue(all.stream().allMatch(Entity::isRemoved),"Ally loss cancels every output");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_5",timeoutTicks=100)
    public static void unpaidSecondCancelsEntireRemainingSequence(GameTestHelper h) {
        try(var a=new NativeAbilityTests.Arena(h)) {
            CapabilityRegistry.getMana(a.p).setMana(80);a.cast(GlyphCatalog.ABYSS_TRIPLE_BLAST,block(a.center.add(0,3,0)),false);
            step(85);near(h,mana(a),0,"Only affordable output second charged");var first=beams(a).getFirst();step(21);
            h.assertTrue(first.isRemoved(),"Unpaid second ends before further damage");CapabilityRegistry.getMana(a.p).setMana(10000);step(120);
            h.assertTrue(beams(a).isEmpty(),"Later rays do not reappear after sequence cancellation");near(h,mana(a),10000,"No delayed fee survives cancellation");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lv_volley_reg_0",timeoutTicks=100)
    public static void existingBeamBilling(GameTestHelper h){LeviathanTests.beamPaymentBurnPoseAndNoUnpaidTail(h);}
    @GameTest(template="empty",batch="lv_volley_reg_1",timeoutTicks=100)
    public static void existingBeamAnchor(GameTestHelper h){LeviathanTests.beamAllySteeringPositionAndCreative(h);}
    private static boolean triple(NativeAbilityTests.Arena a,HitResult hit,double duration) {
        var resolver=new SpellResolver(new SpellContext(a.h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.ABYSS_TRIPLE_BLAST),a.p,LivingCaster.from(a.p),ItemStack.EMPTY));
        return LeviathanVolleys.triple(a.h.getLevel(),CastContext.of(resolver.spellContext,hit,null),0,duration,Resources.quote(resolver.spellContext,Ability.ABYSS_TRIPLE_BLAST,List.of()));
    }
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int ticks){for(int i=0;i<ticks;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static List<Abyss_Orb_Entity> orbs(NativeAbilityTests.Arena a){return a.h.getLevel().getEntitiesOfClass(Abyss_Orb_Entity.class,new AABB(a.center,a.center).inflate(64));}
    private static List<Abyss_Blast_Entity> beams(NativeAbilityTests.Arena a){return a.h.getLevel().getEntitiesOfClass(Abyss_Blast_Entity.class,new AABB(a.center,a.center).inflate(64));}
    private static void near(GameTestHelper h,double actual,double expected,String label){h.assertTrue(Math.abs(actual-expected)<.001,label+": "+actual+" != "+expected);}
}
