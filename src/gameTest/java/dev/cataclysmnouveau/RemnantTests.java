package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.RemnantQuakeAccess;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_remnant") @PrefixGameTestTemplate(false)
public final class RemnantTests {
    @GameTest(template="empty",batch="remnant_0",timeoutTicks=100)
    public static void nativeThreeStormsRadiusDamageCreditAndNaturalEnd(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.cast(GlyphCatalog.SANDSTORM_RING,block(a.center),true);var storms=entities(a,Sandstorm_Entity.class);
            h.assertTrue(storms.size()==3,"Native three storm birth branch");near(h,mana(a),9710,"Single group payment plus amplify");
            h.assertTrue(storms.stream().allMatch(s->s.getCaster()==a.p && s.getLifespan()==300 && !s.shouldBeSaved()),"Actual owner, native lifespan, no saving");
            var storm=storms.getFirst();storm.tickCount=3;storm.tick();near(h,storm.position().distanceTo(a.center),8,"Boss radius rather than six-block player bottle radius");
            var victim=a.target(storm.position(),false);var friend=a.target(storm.position(),true);storm.tickCount=3;storm.tick();
            near(h,500-victim.getHealth(),9,"Native seven plus bounded amplify two");near(h,friend.getHealth(),500,"No allied storm damage");
            h.assertTrue(victim.hasEffect(ModEffect.EFFECTCURSE_OF_DESERT) && victim.getLastDamageSource().getEntity()==a.p,"Native curse and real damage source");
            near(h,mana(a),9710,"Contact never pays again");for(var s:storms){s.setLifespan(1);s.tick();}ExecutionSessions.tick();
            h.assertTrue(storms.stream().allMatch(Entity::isRemoved),"Native lifetime ends every storm");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_1",timeoutTicks=100)
    public static void alliedStormAnchorDurationAndAuthorityCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);var cast=CastContext.detached(a.p,new EntityHitResult(host),null);
            h.assertTrue(RemnantCasting.effect(Ability.SANDSTORM_RING,h.getLevel(),cast,0,1),"Extended ally storm starts");
            var storms=entities(a,Sandstorm_Entity.class);h.assertTrue(storms.stream().allMatch(s->s.getLifespan()==450),"Extend changes actual native lifespan");
            host.setPos(a.center.add(2,1,0));ExecutionSessions.tick();for(var s:storms){s.tick();near(h,s.position().distanceTo(host.position()),8,"Every orbit follows actual ally");}
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());ExecutionSessions.tick();h.assertTrue(storms.stream().allMatch(Entity::isRemoved),"Revoked execution authority cancels owned storms");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_2",timeoutTicks=100)
    public static void rejectionPaymentCreativeAndRootCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.SANDSTORM_RING,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Enemy cannot become storm executor");
            a.cast(GlyphCatalog.REMNANT_STOMP,block(a.center.add(0,15,0)),false);near(h,mana(a),10000,"No ground refunds stomp");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.SANDSTORM_RING,block(a.center),false);a.cast(GlyphCatalog.DESERT_STELE,block(a.center),false);
            h.assertTrue(entities(a,Sandstorm_Entity.class).isEmpty() && entities(a,Ancient_Desert_Stele_Entity.class).isEmpty(),"Unfunded casts create nothing");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.SANDSTORM_RING,block(a.center),false);a.cast(GlyphCatalog.DESERT_STELE,block(a.center),false);
            var storms=entities(a,Sandstorm_Entity.class);var steles=entities(a,Ancient_Desert_Stele_Entity.class);h.assertTrue(storms.size()==3 && steles.size()==1,"Creative bypasses empty mana");near(h,mana(a),0,"Creative keeps zero mana");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(storms.stream().allMatch(Entity::isRemoved) && steles.stream().allMatch(Entity::isRemoved),"Root removal cancels both independent effects");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_3",timeoutTicks=100)
    public static void steleNativeWarningFallPierceImpactAndAllies(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.cast(GlyphCatalog.DESERT_STELE,block(a.center),true);var stele=entities(a,Ancient_Desert_Stele_Entity.class).getFirst();
            h.assertTrue(stele.getCaster()==a.p && !stele.shouldBeSaved() && stele.getWarmUp()==10,"Native owner, transient and warmup");near(h,mana(a),9945,"One stele quote");
            var top=stele.position();tick(stele,9);near(h,stele.position().distanceTo(top),0,"Stele remains still during warning");h.assertTrue(!stele.isActivate(),"Not active before warning ends");tick(stele,1);h.assertTrue(stele.isActivate(),"Native synchronized fall activation");
            var victim=a.target(a.center,false);var friend=a.target(a.center,true);var splash=a.target(a.center.add(2,0,0),false);
            tick(stele,90);h.assertTrue(stele.isRemoved() && victim.hasEffect(ModEffect.EFFECTCURSE_OF_DESERT),"Native fall strikes and ends");
            near(h,500-victim.getHealth(),13.2,"Native eleven with twenty percent amplify");near(h,500-splash.getHealth(),8.6,"Native sixty-percent impact plus minimum amplify");near(h,friend.getHealth(),500,"Pierce and landing protect friend");
            h.assertTrue(victim.getLastDamageSource().getEntity()==a.p,"Stele source credits caster");near(h,mana(a),9945,"Landing never pays again");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_4",timeoutTicks=100)
    public static void stompNativeTimingDamageCapSourceAndGroundBlocks(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            force(a,32);var victim=a.target(a.center.add(1.4,0,3),false);var friend=a.target(victim.position(),true);
            var nativeConfig=com.github.L_Ender.cataclysm.config.CMCommonConfig.AncientRemnant.StompHpDamage;
            try{
                com.github.L_Ender.cataclysm.config.CMCommonConfig.AncientRemnant.StompHpDamage=1;
                a.p.setYRot(0);a.p.setXRot(0);a.cast(GlyphCatalog.REMNANT_STOMP,block(a.center),true);near(h,mana(a),9750,"One ground-wave fee");
                step(27);near(h,victim.getHealth(),500,"Original 28-tick first strike retained");step(1);
                h.assertTrue(victim.getHealth()<500 && !entities(a,com.github.L_Ender.cataclysm.entity.effect.Cm_Falling_Block_Entity.class).isEmpty(),"Native damage and actual rising ground-block visuals");
                h.assertTrue(500-victim.getHealth()<=54.001,"Health-based addition capped at fixed hit before twenty-percent amplify");
                h.assertTrue(victim.getLastDamageSource().getEntity()==a.p,"Ground attack credits actual caster");near(h,friend.getHealth(),500,"No allied ground damage");
                var blocks=new ArrayList<Entity>(entities(a,com.github.L_Ender.cataclysm.entity.effect.Cm_Falling_Block_Entity.class));a.p.discard();step(1);h.assertTrue(blocks.stream().allMatch(Entity::isRemoved),"Root cancellation removes owned ground fragments");
            }finally{com.github.L_Ender.cataclysm.config.CMCommonConfig.AncientRemnant.StompHpDamage=nativeConfig;}
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_5",timeoutTicks=100)
    public static void nativeQuakeGroundedContactsSuccessfulDamageAndContinuationDedup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var r=resolver(a,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GROUND_QUAKE,marker));r.spellContext.nextPart();
            var carrier=new EntityProjectileSpell(h.getLevel(),r);carrier.setPos(a.center);carrier.setDeltaMovement(0,0,.75);
            h.assertTrue(ProjectileDispatch.convert(carrier),"Ground wave carrier converts");var wave=entities(a,EarthQuake_Entity.class).getFirst();
            near(h,mana(a),9950,"One launch fee");h.assertTrue(wave.getOwner()==a.p && !wave.shouldBeSaved(),"Actual owner and transient projectile");
            var victim=a.target(a.center,false);victim.setOnGround(true);var airborne=a.target(a.center,false);airborne.setOnGround(false);var friend=a.target(a.center,true);friend.setOnGround(true);
            wave.tickCount=5;((RemnantQuakeAccess)wave).confluxRemnant$contact();near(h,500-victim.getHealth(),11,"Native quake damage");near(h,airborne.getHealth(),500,"Native quake only damages grounded targets");near(h,friend.getHealth(),500,"Ally filtered");h.assertTrue(marker.hits==1,"One damage-success continuation");
            ((RemnantQuakeAccess)wave).confluxRemnant$contact();h.assertTrue(marker.hits==1,"Invulnerability failure does not continue");victim.invulnerableTime=0;((RemnantQuakeAccess)wave).confluxRemnant$contact();h.assertTrue(marker.hits==1 && victim.getHealth()<489,"More native contact damage does not repeat Ars chain for same victim");
            tick(wave,65);step(1);h.assertTrue(wave.isRemoved() && carrier.isRemoved(),"Original wave lifetime ends carrier");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_6",timeoutTicks=100)
    public static void repeatingQuakeOneMotionDriverPaidContactAndEmptyStop(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var r=resolver(a,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GROUND_QUAKE,marker));r.spellContext.nextPart();
            var carrier=new EntityOrbitProjectile(h.getLevel(),r,a.p);carrier.setPos(a.center);
            h.assertTrue(ProjectileDispatch.convert(carrier),"Repeating wave converts");var wave=entities(a,EarthQuake_Entity.class).getFirst();
            near(h,mana(a),10000,"No prepaid repeating fee");var pos=wave.position();var motion=wave.getDeltaMovement();wave.tick();near(h,wave.position().distanceTo(pos),0,"Native tick does not move Ars carrier");near(h,wave.getDeltaMovement().distanceTo(motion),0,"Native tick does not change Ars velocity");
            var impact=ArsSpellAccess.resolver(carrier);var air=a.target(a.center.add(4,0,0),false);air.setOnGround(false);impact.onResolveEffect(h.getLevel(),new EntityHitResult(air));near(h,mana(a),10000,"Airborne target is not a paid ground contact");
            var victim=a.target(a.center.add(5,0,0),false);victim.setOnGround(true);impact.onResolveEffect(h.getLevel(),new EntityHitResult(victim));
            near(h,mana(a),9950,"Accepted repeating contact pays once");near(h,500-victim.getHealth(),11,"Repeated carrier still invokes native attack");h.assertTrue(marker.hits==1,"One continuation for paid hit");
            CapabilityRegistry.getMana(a.p).setMana(0);victim.invulnerableTime=0;impact.onResolveEffect(h.getLevel(),new EntityHitResult(victim));
            h.assertTrue(wave.isRemoved() && carrier.isRemoved() && marker.hits==1,"Empty mana stops before new damage and continuation");h.succeed();
        }
    }
    @GameTest(template="empty",batch="remnant_7",timeoutTicks=100)
    public static void coreAlliancesArsEquipmentEventsAndUnmanagedIsolation(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(6);
            a.cast(GlyphCatalog.SANDSTORM_RING,block(a.center),false);var storm=entities(a,Sandstorm_Entity.class).getFirst();storm.tickCount=1;storm.tick();
            var enemy=a.target(storm.position(),false);var friend=a.target(storm.position(),false);
            var seen=new ArrayList<Float>();boolean[] cancel={false};
            java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Pre> listener=e->{if(e.caster==a.p && e.target==enemy){seen.add(e.damage);if(cancel[0])e.setCanceled(true);}};
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
            try(var relation=dev.arsconflux.api.targeting.Allies.register(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","remnant_test_allies"),(x,y)->x==a.p && y==friend)){
                storm.tickCount=3;storm.tick();near(h,500-enemy.getHealth(),13,"Ars flat equipment damage added once");near(h,friend.getHealth(),500,"Core provider protects non-team ally");h.assertTrue(seen.size()==1,"One Ars event per native hit");
                enemy.setHealth(500);enemy.invulnerableTime=0;enemy.removeAllEffects();cancel[0]=true;storm.tickCount=6;storm.tick();near(h,enemy.getHealth(),500,"Cancelled Ars pre-event prevents native damage");h.assertTrue(!enemy.hasEffect(ModEffect.EFFECTCURSE_OF_DESERT),"Cancelled damage does not curse");
                int count=seen.size();var ordinary=new Sandstorm_Entity(h.getLevel(),enemy.getX(),enemy.getY(),enemy.getZ(),300,0,null);ordinary.tickCount=3;ordinary.tick();near(h,500-enemy.getHealth(),7,"Ordinary native storm keeps its own damage");h.assertTrue(seen.size()==count,"Unmanaged storm never acquires Ars events");ordinary.discard();
            }catch(Exception e){throw new AssertionError(e);}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}
            h.succeed();
        }
    }
    private static void force(NativeAbilityTests.Arena a,int r){for(int x=(a.base.getX()-r)>>4;x<=(a.base.getX()+r)>>4;x++)for(int z=(a.base.getZ()-r)>>4;z<=(a.base.getZ()+r)>>4;z++)if(!a.h.getLevel().getForcedChunks().contains(ChunkPos.asLong(x,z))){a.h.getLevel().setChunkForced(x,z,true);a.forced.add(new ChunkPos(x,z));}}
    private static SpellResolver resolver(NativeAbilityTests.Arena a,Spell spell){return new SpellResolver(new SpellContext(a.h.getLevel(),spell,a.p,LivingCaster.from(a.p),ItemStack.EMPTY));}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void tick(Entity e,int n){for(int i=0;i<n && !e.isRemoved();i++){++e.tickCount;e.tick();}}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> c){return a.h.getLevel().getEntitiesOfClass(c,new AABB(a.center,a.center).inflate(32));}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double a,double b,String m){h.assertTrue(Math.abs(a-b)<.002,m+": "+a+" != "+b);}
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","remnant_test_marker"),"Marker");}
        public void onResolve(HitResult hit,Level w,LivingEntity c,SpellStats stats,SpellContext context,SpellResolver r){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}public String getBookDescription(){return "Test continuation";}
    }
}
