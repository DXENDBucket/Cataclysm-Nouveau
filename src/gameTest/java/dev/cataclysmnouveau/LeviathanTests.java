package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_leviathan") @PrefixGameTestTemplate(false)
public final class LeviathanTests {
    @GameTest(template="empty",batch="leviathan_0",timeoutTicks=100)
    public static void orbNativeTrackingFearAndAllies(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var victim=a.target(a.center.add(0,2,8),false);var friend=a.target(victim.position(),true);
            var mana=CapabilityRegistry.getMana(a.p);var marker=new Marker();
            var r=resolver(a,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ABYSS_ORB,marker));r.spellContext.nextPart();
            var carrier=new EntityProjectileSpell(h.getLevel(),r);carrier.setPos(a.center.add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
            h.assertTrue(ProjectileDispatch.convert(carrier),"Orb carrier converts");
            var orb=entities(a,Abyss_Orb_Entity.class).getFirst();
            near(h,mana.getCurrentMana(),10000-60,"One launch fee");
            h.assertTrue(orb.getOwner()==a.p && !orb.shouldBeSaved() && !orb.getTracking(),"Native ownership and tracking windup");
            ((ProjectileHitAccess)orb).confluxCataclysm$nativeHit(block(orb.position()));
            h.assertTrue(!orb.isRemoved() && marker.hits==0,"Young block contact is not an activation");
            for(int i=0;i<40;i++)orb.tick();
            h.assertTrue(orb.getTracking(),"Native forty-tick tracking delay");
            ((ProjectileHitAccess)orb).confluxCataclysm$nativeHit(new EntityHitResult(friend));
            near(h,friend.getHealth(),500,"Friendly contact ignored");h.assertTrue(!orb.isRemoved(),"Friend does not consume shot");
            ((ProjectileHitAccess)orb).confluxCataclysm$nativeHit(new EntityHitResult(victim));
            h.assertTrue(orb.isRemoved() && marker.hits==1 && victim.getHealth()<500,"Native hit, explosion and one continuation");
            h.assertTrue(victim.hasEffect(ModEffect.EFFECTABYSSAL_FEAR),"Successful direct hit applies native fear");
            float health=victim.getHealth();victim.heal(20);near(h,victim.getHealth(),health,"Native fear cancels healing");
            near(h,friend.getHealth(),500,"Explosion also protects allies");near(h,mana.getCurrentMana(),9940,"Impact has no second launch fee");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_1",timeoutTicks=100)
    public static void repeatedOrbMotionPaymentAndCancellation(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);var marker=new Marker();
            var r=resolver(a,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ABYSS_ORB,marker));r.spellContext.nextPart();
            var carrier=new EntityOrbitProjectile(h.getLevel(),r,a.p);carrier.setPos(a.center.add(0,3,0));
            h.assertTrue(ProjectileDispatch.convert(carrier),"Repeating form converts");
            var orb=entities(a,Abyss_Orb_Entity.class).getFirst();var at=orb.position();var velocity=orb.getDeltaMovement();
            orb.tick();near(h,orb.position().distanceTo(at),0,"Decorative tick cannot move Ars trajectory");
            near(h,orb.getDeltaMovement().distanceTo(velocity),0,"Decorative tick cannot add native acceleration");
            near(h,mana.getCurrentMana(),10000,"Repeating carrier has no prepaid contact");
            var impact=ArsSpellAccessResolver(carrier);
            for(int i=0;i<2;i++)impact.onResolveEffect(h.getLevel(),new EntityHitResult(a.target(a.center.add(4+i,2,0),false)));
            h.assertTrue(marker.hits==2 && !orb.isRemoved(),"Two accepted contacts preserve orbit");near(h,mana.getCurrentMana(),9880,"Each contact pays");
            mana.setMana(0);impact.onResolveEffect(h.getLevel(),new EntityHitResult(a.target(a.center.add(7,2,0),false)));
            h.assertTrue(orb.isRemoved() && carrier.isRemoved() && marker.hits==2,"Empty mana cancels before attack and continuation");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_2",timeoutTicks=100)
    public static void mineFloatingLifetimeFearAndCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);var spot=a.center.add(0,3,0);
            a.cast(GlyphCatalog.ABYSS_MINE,block(spot),false);
            var mine=entities(a,Abyss_Mine_Entity.class).getFirst();
            h.assertTrue(!mine.shouldBeSaved() && ((LeviathanArea)mine).confluxLeviathan$lifetime()==800,"Transient native forty-second floating mine");
            var friend=a.target(spot,true);tick(mine,25);h.assertTrue(!mine.isRemoved(),"Ally cannot trigger mine");
            var enemy=a.target(spot,false);tick(mine,1);
            h.assertTrue(mine.isRemoved() && enemy.hasEffect(ModEffect.EFFECTABYSSAL_FEAR),"Enemy detonates original mine and receives fear");
            near(h,friend.getHealth(),500,"Mine explosion excludes allies");near(h,mana.getCurrentMana(),9920,"Single area payment");
            a.cast(GlyphCatalog.ABYSS_MINE,block(spot.add(8,0,0)),false);var expiry=entities(a,Abyss_Mine_Entity.class).getFirst();
            tick(expiry,805);h.assertTrue(expiry.isRemoved(),"Untriggered mine naturally expires");
            a.cast(GlyphCatalog.ABYSS_MINE,block(spot.add(-8,0,0)),false);var orphan=entities(a,Abyss_Mine_Entity.class).getFirst();
            a.p.discard();ExecutionSessions.tick();h.assertTrue(orphan.isRemoved(),"Root disappearance cleans transient mines");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_3",timeoutTicks=100)
    public static void beamPaymentBurnPoseAndNoUnpaidTail(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var spot=a.center.add(0,2,0);a.p.setYRot(0);a.p.setXRot(0);
            var enemy=a.target(spot.add(0,0,5),false);var friend=a.target(enemy.position(),true);
            var mana=CapabilityRegistry.getMana(a.p);var r=resolver(a,new Spell(MethodTouch.INSTANCE,GlyphCatalog.ABYSS_BLAST));
            var cast=CastContext.of(r.spellContext,block(spot),null);var charge=Resources.quote(r.spellContext,Ability.ABYSS_BLAST,List.of());
            h.assertTrue(LeviathanLaser.start(h.getLevel(),cast,0,0,charge),"Fixed native beam starts");
            var beam=entities(a,Abyss_Blast_Entity.class).getFirst();beam.setHpDamage(100);
            for(int i=0;i<20;i++)ExecutionSessions.tick();near(h,mana.getCurrentMana(),10000,"No preparation fee");
            ExecutionSessions.tick();near(h,mana.getCurrentMana(),9920,"Prepay first active second");
            near(h,500-enemy.getHealth(),20,"Native capped health term");near(h,friend.getHealth(),500,"Beam excludes allies");
            h.assertTrue(enemy.hasEffect(ModEffect.EFFECTABYSSAL_BURN),"Native burn is applied");near(h,beam.renderYaw,beam.getYaw(),"Render and damage share yaw");
            float health=enemy.getHealth();mana.setMana(0);for(int i=0;i<20;i++)ExecutionSessions.tick();
            h.assertTrue(beam.isRemoved(),"No unpaid second remains");near(h,enemy.getHealth(),health,"No damage after unpaid boundary");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_4",timeoutTicks=100)
    public static void beamAllySteeringPositionAndCreative(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var host=a.target(a.center.add(0,3,0),true);var enemy=a.target(a.center.add(5,3,0),false);
            var r=resolver(a,new Spell(MethodTouch.INSTANCE,GlyphCatalog.ABYSS_BLAST));var mana=CapabilityRegistry.getMana(a.p);
            var cast=CastContext.of(r.spellContext,new EntityHitResult(enemy),null);var charge=Resources.quote(r.spellContext,Ability.ABYSS_BLAST,List.of());
            h.assertTrue(!LeviathanLaser.start(h.getLevel(),cast,0,0,charge),"Hostile recipient cannot emit a beneficial beam");
            mana.setMana(0);h.assertTrue(!LeviathanLaser.start(h.getLevel(),cast.withTarget(new EntityHitResult(host)),0,0,charge),"Empty mana prevents spawning");
            a.p.setGameMode(GameType.CREATIVE);
            h.assertTrue(LeviathanLaser.start(h.getLevel(),cast.withTarget(new EntityHitResult(host)),0,1,Resources.quote(r.spellContext,Ability.ABYSS_BLAST,List.of())),"Creative starts without mana");
            var beam=entities(a,Abyss_Blast_Entity.class).getFirst();h.assertTrue(beam.getDuration()==120 && !beam.shouldBeSaved() && ((LeviathanBeam)beam).confluxLeviathan$executor()==host.getId(),"Duration, transient marker and real emitter ID");
            host.setYRot(90);host.setXRot(-20);ExecutionSessions.tick();near(h,beam.position().distanceTo(host.getEyePosition()),0,"Ally eyes are physical origin");
            near(h,beam.getPitch(),Math.toRadians(20),"Ally current pitch");near(h,beam.renderYaw,beam.getYaw(),"Client-facing synced yaw");
            host.setYRot(-90);host.setXRot(10);ExecutionSessions.tick();near(h,beam.getPitch(),Math.toRadians(-10),"Steering continues during native ray");
            for(int i=0;i<150;i++)ExecutionSessions.tick();h.assertTrue(beam.isRemoved(),"Full duration and fade finish");near(h,mana.getCurrentMana(),0,"Creative output stays free");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_5",timeoutTicks=100)
    public static void portalChildNativeDirectionDamageAndCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            a.cast(GlyphCatalog.ABYSS_BOMBARDMENT,block(a.center),true);
            var portal=entities(a,Abyss_Blast_Portal_Entity.class).getFirst();tick(portal,22);
            var child=((LeviathanPortal)portal).confluxLeviathan$child();h.assertTrue(child!=null,"Native portal spawns its native child on tick twenty-two");
            h.assertTrue(((LeviathanBeam)child).confluxLeviathan$managed() && !child.shouldBeSaved(),"Child marker and lifecycle");
            h.assertTrue(NativeSpellDamage.context(child).caster()==a.p && child.caster==a.p,"Child retains actual caster");
            near(h,child.getPitch(),Math.PI/2,"Native portal fires vertically upwards");h.assertTrue(child.getDuration()==160,"Native eight-second duration");
            var enemy=a.target(a.center.add(0,4,0),false);var friend=a.target(enemy.position(),true);tick(child,21);
            h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(ModEffect.EFFECTABYSSAL_BURN),"Native child ray really damages and burns");near(h,friend.getHealth(),500,"Child ray respects allies");
            near(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),9690,"One area fee including amplify, no child fee");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(portal.isRemoved() && child.isRemoved(),"Root cancellation removes both native entities");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_6",timeoutTicks=100)
    public static void portalGroundFailureDurationAndNaturalEnd(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);a.cast(GlyphCatalog.ABYSS_BOMBARDMENT,block(a.center.add(0,12,0)),false);
            near(h,mana.getCurrentMana(),10000,"Missing ground refunds area payment");h.assertTrue(entities(a,Abyss_Blast_Portal_Entity.class).isEmpty(),"No phantom portal");
            var r=resolver(a,new Spell(MethodTouch.INSTANCE,GlyphCatalog.ABYSS_BOMBARDMENT));
            h.assertTrue(LeviathanCasting.effect(Ability.ABYSS_BOMBARDMENT,h.getLevel(),CastContext.of(r.spellContext,block(a.center),null),0,1),"Extended portal starts");
            var portal=entities(a,Abyss_Blast_Portal_Entity.class).getFirst();h.assertTrue(((LeviathanArea)portal).confluxLeviathan$lifetime()==340,"Portal outlives extended ray and fade");
            tick(portal,22);var beam=((LeviathanPortal)portal).confluxLeviathan$child();h.assertTrue(beam.getDuration()==240,"Extend changes actual native ray duration");
            tick(beam,270);h.assertTrue(beam.isRemoved(),"Native ray naturally fades and ends");tick(portal,325);ExecutionSessions.tick();h.assertTrue(portal.isRemoved(),"Native portal naturally closes");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="leviathan_7",timeoutTicks=100)
    public static void arsScalingCancellationAndNativeIsolation(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)) {
            var enemy=a.target(a.center.add(0,2,3),false);
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(6);
            var r=resolver(a,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ABYSS_ORB));var c=CastContext.of(r.spellContext,new EntityHitResult(enemy),null);
            var orb=(Abyss_Orb_Entity)HarbingerCasting.create(Ability.ABYSS_ORB,h.getLevel(),c,new Vec3(0,0,1),enemy,1);orb.setPos(enemy.position());
            var amounts=new ArrayList<Float>();boolean[] cancel={false};
            java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Pre> listener=e->{if(e.caster==a.p && e.target==enemy){amounts.add(e.damage);if(cancel[0])e.setCanceled(true);}};
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
            try {
                ((ProjectileHitAccess)orb).confluxCataclysm$nativeHit(new EntityHitResult(enemy));near(h,amounts.getFirst(),12,"Native four plus bounded augment two plus Ars six, once");
                h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Damage credits real owner");
                enemy.removeAllEffects();enemy.invulnerableTime=0;enemy.setHealth(500);cancel[0]=true;
                var denied=(Abyss_Orb_Entity)HarbingerCasting.create(Ability.ABYSS_ORB,h.getLevel(),c,new Vec3(0,0,1),enemy,0);denied.setPos(enemy.position());
                ((ProjectileHitAccess)denied).confluxCataclysm$nativeHit(new EntityHitResult(enemy));near(h,enemy.getHealth(),500,"Cancelled direct and blast hits cause no damage");h.assertTrue(!enemy.hasEffect(ModEffect.EFFECTABYSSAL_FEAR),"Cancelled direct hit does not apply fear");
                cancel[0]=false;int count=amounts.size();var ordinary=new Abyss_Orb_Entity(a.p,0,0,1,h.getLevel(),4,enemy);ordinary.setPos(enemy.position());
                ((ProjectileHitAccess)ordinary).confluxCataclysm$nativeHit(new EntityHitResult(enemy));h.assertTrue(amounts.size()==count,"Unmanaged native orb never gains Ars event or attribute");
            } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}
            h.succeed();
        }
    }
    private static SpellResolver resolver(NativeAbilityTests.Arena a,Spell spell){return new SpellResolver(new SpellContext(a.h.getLevel(),spell,a.p,LivingCaster.from(a.p),ItemStack.EMPTY));}
    private static SpellResolver ArsSpellAccessResolver(EntityProjectileSpell carrier){return dev.arsconflux.api.glyph.ArsSpellAccess.resolver(carrier);}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void tick(Entity e,int ticks){for(int i=0;i<ticks && !e.isRemoved();i++){++e.tickCount;e.tick();}}
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> type){return a.h.getLevel().getEntitiesOfClass(type,new AABB(a.center,a.center).inflate(24));}
    private static void near(GameTestHelper h,double actual,double expected,String message){h.assertTrue(Math.abs(actual-expected)<.001,message+": "+actual+" != "+expected);}
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker(){super(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","leviathan_test_marker"),"Marker");}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public String getBookDescription(){return "Test continuation";}
    }
}
