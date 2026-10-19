package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.*;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_remnant_second") @PrefixGameTestTemplate(false)
public final class RemnantSecondTests {
    @GameTest(template="empty",batch="remnant_second_0",timeoutTicks=100)
    public static void completeNativeSteleArrayTimingGeometryAndOneFee(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            force(a);a.p.setYRot(0);a.p.setXRot(0);a.cast(GlyphCatalog.DESERT_STELE_ARRAY,block(a.center),true);
            near(h,mana(a),9390,"Whole formation pays one quote plus augment");step(19);h.assertTrue(steles(a).isEmpty(),"Native twenty-tick birth retained");a.p.setYRot(90);step(1);
            var all=steles(a);h.assertTrue(all.size()==144,"Eight times sixteen plus sixteen native line steles: "+all.size());
            h.assertTrue(all.stream().allMatch(e->e.getCaster()==a.p && !e.shouldBeSaved()),"Every child has real owner and transient state");
            h.assertTrue(all.stream().filter(e->Math.abs(e.getX()-a.center.x)<.001 && e.getZ()>a.center.z).count()>=16,"Fixed-position line keeps initial forward heading despite payer turning");
            h.assertTrue(all.stream().mapToInt(Ancient_Desert_Stele_Entity::getWarmUp).min().orElse(0)==1 && all.stream().mapToInt(Ancient_Desert_Stele_Entity::getWarmUp).max().orElse(0)==27,"Native staggered warning delays retained");
            h.assertTrue(!frames.isEmpty() && frames.stream().allMatch(f->f.valid() && f.positionAnchored() && f.animation()==17),"Valid position-anchored native monolith visual protocol");
            a.p.discard();step(1);h.assertTrue(all.stream().allMatch(Entity::isRemoved) && frames.getLast().closed(),"Cancellation clears all steles and explicitly closes visual");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_1",timeoutTicks=100)
    public static void nativeThreeSlamsCountsDamageOwnershipAndChildAmplification(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            force(a);a.p.setYRot(0);a.p.setXRot(0);var enemy=a.target(a.center.add(0,0,6),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.REMNANT_TRIPLE_QUAKE,block(a.center),true);near(h,mana(a),9570,"One triple group quote");step(25);h.assertTrue(quakes(a).isEmpty(),"No early radial wave");step(1);
            int first=quakes(a).size();h.assertTrue(first>=22 && first<=29,"Native first wave count: "+first);near(h,500-enemy.getHealth(),60,"Native twenty-five plus capped five percent, then augment ten");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Actual caster credited for direct slam");near(h,friend.getHealth(),500,"Direct slam protects ally");
            step(29);int second=quakes(a).size();h.assertTrue(second-first>=22 && second-first<=29,"Native second wave at tick55");step(30);var all=quakes(a);h.assertTrue(all.size()-second>=22 && all.size()-second<=29,"Native third wave at tick85");
            h.assertTrue(all.stream().allMatch(q->q.getOwner()==a.p && !q.shouldBeSaved() && ((ManagedProjectile)q).confluxCataclysm$sourceAbility()==Ability.REMNANT_TRIPLE_QUAKE),"All children share parent source and transient lifecycle");
            var q=all.getFirst();enemy.setPos(q.position());enemy.setOnGround(true);enemy.invulnerableTime=0;enemy.setHealth(500);q.tickCount=5;((RemnantQuakeAccess)q).confluxRemnant$contact();near(h,500-enemy.getHealth(),13.2,"Child quake receives native eleven plus one twenty-percent augment");
            near(h,mana(a),9570,"No fees for radial child damage");a.p.discard();step(1);h.assertTrue(all.stream().allMatch(Entity::isRemoved),"Root cancellation removes every ring");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_2",timeoutTicks=100)
    public static void alliesMoveAndSteerBeforeNativeArrayBirth(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            force(a);var host=a.target(a.center,true);host.setYRot(90);host.setXRot(0);a.cast(GlyphCatalog.DESERT_STELE_ARRAY,new EntityHitResult(host),false);
            step(10);host.setPos(a.center.add(3,0,0));host.setYRot(180);step(10);var all=steles(a);h.assertTrue(all.size()==144,"Allied array fully builds");
            h.assertTrue(all.stream().filter(e->Math.abs(e.getX()-host.getX())<.001 && e.getZ()<host.getZ()).count()>=16,"Shared aiming uses ally view and moved origin");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);h.assertTrue(all.stream().allMatch(Entity::isRemoved),"Revoked ally authority clears group");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_3",timeoutTicks=100)
    public static void failureRefundCreativeAndParallelGroups(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            force(a);var enemy=a.target(a.center,false);
            a.cast(GlyphCatalog.DESERT_STELE_ARRAY,new EntityHitResult(enemy),false);a.cast(GlyphCatalog.REMNANT_TRIPLE_QUAKE,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Hostile executor rejected without fee");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.DESERT_STELE_ARRAY,block(a.center),false);a.cast(GlyphCatalog.REMNANT_TRIPLE_QUAKE,block(a.center),false);step(30);h.assertTrue(steles(a).isEmpty() && quakes(a).isEmpty(),"No unfunded group starts");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.DESERT_STELE_ARRAY,block(a.center),false);a.cast(GlyphCatalog.REMNANT_TRIPLE_QUAKE,block(a.center),false);step(26);
            h.assertTrue(steles(a).size()==144 && quakes(a).size()>=22,"Both non-body groups can coexist with creative empty mana");near(h,mana(a),0,"Creative output free");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_4",timeoutTicks=100)
    public static void cursedStormNativeDelayTwoHitsCurseFadeAndOneLaunchFee(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var origin=a.center.add(0,3,0);a.target(origin.add(0,0,24),false);var marker=new Marker();
            var carrier=carrier(a,marker,false,true,origin);h.assertTrue(ProjectileDispatch.convert(carrier),"Cursed storm converts");var storm=storms(a).getFirst();
            h.assertTrue(storm.getOwner()==a.p && !storm.shouldBeSaved() && storm.getHit()==2 && !storm.getTracking(),"Actual owner and native two-hit delayed homing");near(h,mana(a),9940,"One launch payment including amplify");
            tick(storm,14);h.assertTrue(!storm.getTracking(),"No early tracking");tick(storm,1);h.assertTrue(storm.getTracking(),"Native fifteen-tick homing delay");
            var friend=a.target(storm.position(),true);((ProjectileHitAccess)storm).confluxCataclysm$nativeHit(new EntityHitResult(friend));h.assertTrue(storm.getHit()==2 && marker.hits==0,"Friendly target does not consume hit budget or continue");
            var one=a.target(origin.add(4,0,0),false);storm.setPos(one.position());((ProjectileHitAccess)storm).confluxCataclysm$nativeHit(new EntityHitResult(one));near(h,500-one.getHealth(),9.5,"Native seven-and-half plus minimum amplify two");h.assertTrue(storm.getHit()==1 && storm.getState()==0 && marker.hits==1,"First successful hit continues while projectile remains");
            h.assertTrue(one.getEffect(ModEffect.EFFECTCURSE_OF_DESERT).getAmplifier()==1 && one.getLastDamageSource().getEntity()==a.p,"Native curse II and real source");
            var two=a.target(origin.add(6,0,0),false);storm.setPos(two.position());((ProjectileHitAccess)storm).confluxCataclysm$nativeHit(new EntityHitResult(two));h.assertTrue(storm.getState()==2 && marker.hits==2,"Second hit starts native fade and one more continuation");
            ((ProjectileHitAccess)storm).confluxCataclysm$nativeHit(new EntityHitResult(one));h.assertTrue(marker.hits==2,"Fading storm cannot continue again");tick(storm,12);step(1);h.assertTrue(storm.isRemoved() && carrier.isRemoved(),"Native fade finishes parent carrier");near(h,mana(a),9940,"No extra ordinary impact fee");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_5",timeoutTicks=100)
    public static void repeatingStormRetainsMotionLifetimeBillingAndEmptyManaStop(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,marker,true,false,a.center.add(0,3,0));h.assertTrue(ProjectileDispatch.convert(carrier),"Orbit storm converts");var storm=storms(a).getFirst();
            var pos=storm.position();var velocity=storm.getDeltaMovement();tick(storm,310);near(h,storm.position().distanceTo(pos),0,"No native movement added to Ars trajectory");near(h,storm.getDeltaMovement().distanceTo(velocity),0,"No native acceleration leaks into Ars motion");
            h.assertTrue(!storm.isRemoved() && storm.getState()==0,"Native 290-tick expiry cannot silently disable repeating carrier");near(h,mana(a),10000,"Visual ticking never charges");
            var victim=a.target(a.center.add(4,3,0),false);var impact=ArsSpellAccess.resolver(carrier);
            impact.onResolveEffect(h.getLevel(),new EntityHitResult(victim));near(h,500-victim.getHealth(),7.5,"Native contact damage");near(h,mana(a),9950,"First repeat quote");
            victim.invulnerableTime=0;impact.onResolveEffect(h.getLevel(),new EntityHitResult(victim));near(h,mana(a),9900,"Second repeat quote");h.assertTrue(marker.hits==2 && storm.getState()==0 && storm.getHit()==2,"Repeated contacts do not exhaust the carrier's native hit state");
            CapabilityRegistry.getMana(a.p).setMana(0);victim.invulnerableTime=0;float hp=victim.getHealth();impact.onResolveEffect(h.getLevel(),new EntityHitResult(victim));near(h,victim.getHealth(),hp,"No unpaid damage tail");h.assertTrue(storm.isRemoved() && carrier.isRemoved() && marker.hits==2,"Empty mana stops repeat before attack and continuation");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_second_6",timeoutTicks=100)
    public static void allNativeVisualStatesRoundTripOnServer(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="remnant_second_7",timeoutTicks=100)
    public static void previousSteleRegression(GameTestHelper h){RemnantTests.steleNativeWarningFallPierceImpactAndAllies(h);}
    @GameTest(template="empty",batch="remnant_second_8",timeoutTicks=100)
    public static void sharedProjectileDriverRegression(GameTestHelper h){LeviathanTests.orbNativeTrackingFearAndAllies(h);}
    private static EntityProjectileSpell carrier(NativeAbilityTests.Arena a,Marker marker,boolean repeat,boolean amp,Vec3 origin){
        var parts=new ArrayList<AbstractSpellPart>(List.of(MethodProjectile.INSTANCE,GlyphCatalog.CURSED_SANDSTORM));if(amp)parts.add(com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify.INSTANCE);parts.add(marker);
        var r=new SpellResolver(new SpellContext(a.h.getLevel(),new Spell(parts),a.p,LivingCaster.from(a.p),ItemStack.EMPTY));r.spellContext.nextPart();
        var c=repeat?new EntityOrbitProjectile(a.h.getLevel(),r,a.p):new EntityProjectileSpell(a.h.getLevel(),r);c.setPos(origin);c.setDeltaMovement(0,0,.75);return c;
    }
    private static void force(NativeAbilityTests.Arena a){int r=32;for(int x=(a.base.getX()-r)>>4;x<=(a.base.getX()+r)>>4;x++)for(int z=(a.base.getZ()-r)>>4;z<=(a.base.getZ()+r)>>4;z++)if(!a.h.getLevel().getForcedChunks().contains(ChunkPos.asLong(x,z))){a.h.getLevel().setChunkForced(x,z,true);a.forced.add(new ChunkPos(x,z));}}
    private static List<Ancient_Desert_Stele_Entity> steles(NativeAbilityTests.Arena a){return entities(a,Ancient_Desert_Stele_Entity.class);}
    private static List<EarthQuake_Entity> quakes(NativeAbilityTests.Arena a){return entities(a,EarthQuake_Entity.class);}
    private static List<Cursed_Sandstorm_Entity> storms(NativeAbilityTests.Arena a){return entities(a,Cursed_Sandstorm_Entity.class);}
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> c){return a.h.getLevel().getEntitiesOfClass(c,new AABB(a.center,a.center).inflate(40));}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void tick(Entity e,int n){for(int i=0;i<n && !e.isRemoved();i++){++e.tickCount;e.tick();}}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double actual,double expected,String m){h.assertTrue(Math.abs(actual-expected)<.002,m+": "+actual+" != "+expected);}
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","remnant_second_marker"),"Marker");}
        public void onResolve(HitResult hit,Level w,LivingEntity c,SpellStats stats,SpellContext context,SpellResolver r){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}public String getBookDescription(){return "Test continuation";}
    }
}
