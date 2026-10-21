package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.projectile.Axe_Blade_Entity;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.AxeBladeAccess;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_aptrgangr") @PrefixGameTestTemplate(false)
public final class AptrgangrTests {
    @GameTest(template="empty",batch="aptr_0",timeoutTicks=100)
    public static void piercingBladeNativeMotionDamageFadeAndContinuationDedup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,marker,false,true);h.assertTrue(ProjectileDispatch.convert(carrier),"Non-Projectile axe entity converts");var blade=blades(a).getFirst();
            near(h,mana(a),9950,"Single augmented launch quote");near(h,blade.zPower,.1,"Native acceleration power");blade.tick();near(h,blade.getDeltaMovement().z,.085,"Native inertia applied to acceleration");
            h.assertTrue(blade.getOwner()==a.p && !blade.shouldBeSaved() && blade.getTransparency()==1,"Actual owner, transient entity and native fade clock");
            var enemy=a.target(a.center,false);var friend=a.target(a.center,true);hit(blade,new EntityHitResult(friend));near(h,friend.getHealth(),500,"Allied blade hit rejected");
            hit(blade,new EntityHitResult(enemy));near(h,500-enemy.getHealth(),10,"Original eight plus minimum two augmentation");h.assertTrue(marker.hits==1 && !blade.isRemoved() && enemy.getLastDamageSource().getEntity()==a.p,"Damage-success continuation and native piercing");
            hit(blade,new EntityHitResult(enemy));h.assertTrue(marker.hits==1,"Invulnerability failure never continues");enemy.invulnerableTime=0;hit(blade,new EntityHitResult(enemy));h.assertTrue(marker.hits==1 && enemy.getHealth()<490,"Further native damage does not repeat same-victim Ars chain");
            var second=a.target(a.center.add(3,0,0),false);hit(blade,new EntityHitResult(second));h.assertTrue(marker.hits==2,"Another target can continue");
            hit(blade,block(a.center));h.assertTrue(!blade.isRemoved() && marker.hits==2,"Wall passage has no nonexistent block effect continuation");
            for(int i=1;i<80;i++)blade.tick();step(1);h.assertTrue(blade.isRemoved() && carrier.isRemoved(),"Native eighty-tick fade closes carrier");near(h,mana(a),9950,"Piercing contact never adds an ordinary fee");h.succeed();
        }
    }
    @GameTest(template="empty",batch="aptr_1",timeoutTicks=100)
    public static void repeatedBladeKeepsArsTrajectoryVisibilityAndTriggerBilling(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,marker,true,false);h.assertTrue(ProjectileDispatch.convert(carrier),"Orbit axe converts");var blade=blades(a).getFirst();var point=blade.position();var velocity=blade.getDeltaMovement();
            for(int i=0;i<180;i++)blade.tick();near(h,blade.position().distanceTo(point),0,"Native movement suppressed for Ars carrier");near(h,blade.getDeltaMovement().distanceTo(velocity),0,"No native acceleration leaks into Ars trajectory");
            h.assertTrue(!blade.isRemoved() && blade.getTransparency()<=1,"Repeated blade does not fade permanently invisible after four seconds");near(h,mana(a),10000,"Visual lifetime upkeep is free");
            var r=ArsSpellAccess.resolver(carrier);var enemy=a.target(a.center.add(3,0,0),false);r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));near(h,mana(a),9960,"First repeated hit pays forty");near(h,500-enemy.getHealth(),8,"Native repeated damage");
            enemy.invulnerableTime=0;r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));near(h,mana(a),9920,"Second hit pays again");h.assertTrue(marker.hits==2,"Repeated successful triggers continue separately");
            r.onResolveEffect(h.getLevel(),block(a.center));near(h,mana(a),9920,"Native empty block effect is not a paid trigger");CapabilityRegistry.getMana(a.p).setMana(0);float hp=enemy.getHealth();enemy.invulnerableTime=0;r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            near(h,enemy.getHealth(),hp,"Empty mana cannot produce another hit");h.assertTrue(blade.isRemoved() && carrier.isRemoved(),"Empty mana ends repeated shape");h.succeed();
        }
    }
    @GameTest(template="empty",batch="aptr_2",timeoutTicks=100)
    public static void smashNativeTickDamageFanParticlesAndNaturalChildLifetime(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();var particles=new ArrayList<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);if(p instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket f)particles.add(f);})){
            a.p.setYRot(0);a.p.setXRot(0);var enemy=a.target(a.center.add(0,0,3),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.APTRGANGR_SMASH,block(a.center),true);near(h,mana(a),9810,"One augmented smash quote");step(14);h.assertTrue(blades(a).isEmpty(),"No early blades before native tick15");step(1);
            near(h,500-enemy.getHealth(),21.6,"Native eighteen melee plus twenty-percent amplify");near(h,friend.getHealth(),500,"Native smash excludes allies");h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Melee credits actual caster");
            var children=blades(a);h.assertTrue(children.size()==5 && children.stream().allMatch(b->b.getOwner()==a.p && !b.shouldBeSaved() && ((ManagedProjectile)b).confluxCataclysm$sourceAbility()==Ability.APTRGANGR_SMASH),"Exactly five native transient blades with real owner");
            h.assertTrue(children.stream().allMatch(b->Math.abs(b.position().z-(a.center.z+5))<.001 && Math.abs(b.yPower)<.001),"Native fan mouth remains five blocks forward and launches horizontally");
            h.assertTrue(particles.stream().anyMatch(p->p.getParticle() instanceof com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions),"Actual native smash ring packet reaches player");
            var target=a.target(a.center.add(8,0,0),false);hit(children.getFirst(),new EntityHitResult(target));near(h,500-target.getHealth(),10,"Child retains native eight plus one augmentation");near(h,mana(a),9810,"All five children remain included in one fee");
            step(25);h.assertTrue(frames.getLast().closed() && children.stream().filter(b->!b.isRemoved()).count()==5,"Normal recovery closes body without deleting flying blades");
            for(var child:children)for(int i=0;i<80;i++)child.tick();step(1);h.assertTrue(children.stream().allMatch(Entity::isRemoved),"Independent children still expire natively");h.succeed();
        }
    }
    @GameTest(template="empty",batch="aptr_3",timeoutTicks=100)
    public static void alliedSmashUsesExecutorAimAndCrossSeriesInterruption(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);host.setYRot(90);a.p.setYRot(0);var enemy=a.target(a.center.add(-3,0,0),false);
            a.cast(GlyphCatalog.APTRGANGR_SMASH,new EntityHitResult(host),false);step(15);near(h,500-enemy.getHealth(),18,"Ally body aims independently of payer");
            var children=blades(a);h.assertTrue(children.size()==5 && children.stream().allMatch(b->Math.abs(b.getX()-(host.getX()-5))<.01),"Native fan follows actual ally heading");h.assertTrue(AptrgangrCasting.isActing(host),"Action owns executor");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,new EntityHitResult(host),false);h.assertTrue(AptrgangrCasting.isActing(host) && children.stream().noneMatch(Entity::isRemoved),"Unfunded action cannot interrupt or clean children");
            CapabilityRegistry.getMana(a.p).setMana(10000);a.cast(GlyphCatalog.REMNANT_TAIL_SWEEP,new EntityHitResult(host),false);
            h.assertTrue(!AptrgangrCasting.isActing(host) && RemnantActions.isActing(host) && children.stream().allMatch(Entity::isRemoved),"Other Boss action interrupts smash and only its children");
            a.cast(GlyphCatalog.APTRGANGR_SMASH,new EntityHitResult(host),false);h.assertTrue(!RemnantActions.isActing(host) && AptrgangrCasting.isActing(host),"Smash can replace another series");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);h.assertTrue(!AptrgangrCasting.isActing(host),"Authority loss releases actual actor AI");h.succeed();
        }
    }
    @GameTest(template="empty",batch="aptr_4",timeoutTicks=100)
    public static void invalidExecutorCreativeAndOwnerLoss(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.APTRGANGR_SMASH,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Hostile execution has no payer fallback or fee");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.APTRGANGR_SMASH,block(a.center),false);step(15);h.assertTrue(blades(a).isEmpty(),"Unfunded smash never spawns children");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.APTRGANGR_SMASH,block(a.center),false);step(15);var children=blades(a);h.assertTrue(children.size()==5,"Creative empty mana executes native formation");near(h,mana(a),0,"Creative pays nothing");
            a.p.discard();step(1);h.assertTrue(children.stream().allMatch(Entity::isRemoved),"Root loss cleans child sessions");h.succeed();
        }
    }
    @GameTest(template="empty",batch="aptr_5",timeoutTicks=100)
    public static void allNativeBodyStatesRoundTrip(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="aptr_6",timeoutTicks=100)
    public static void sharedProjectileRegression(GameTestHelper h){RevenantTests.nativeBoneGravityHitSourceShatterAndDamageContinuation(h);}
    private static void hit(Axe_Blade_Entity e,HitResult hit){((AxeBladeAccess)e).confluxAptrgangr$hit(hit);}
    private static List<Axe_Blade_Entity> blades(NativeAbilityTests.Arena a){return a.h.getLevel().getEntitiesOfClass(Axe_Blade_Entity.class,new AABB(a.center,a.center).inflate(40));}
    private static EntityProjectileSpell carrier(NativeAbilityTests.Arena a,Marker marker,boolean repeat,boolean amp){
        var parts=new ArrayList<AbstractSpellPart>(List.of(MethodProjectile.INSTANCE,GlyphCatalog.AXE_BLADE));if(amp)parts.add(AugmentAmplify.INSTANCE);parts.add(marker);
        var r=new SpellResolver(new SpellContext(a.h.getLevel(),new Spell(parts),a.p,LivingCaster.from(a.p),ItemStack.EMPTY));r.spellContext.nextPart();var c=repeat?new EntityOrbitProjectile(a.h.getLevel(),r,a.p):new EntityProjectileSpell(a.h.getLevel(),r);c.setPos(a.center.add(0,3,0));c.setDeltaMovement(0,0,.75);return c;
    }
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double x,double y,String m){h.assertTrue(Math.abs(x-y)<.002,m+": "+x+" != "+y);}
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","aptr_marker"),"Marker");}
        public void onResolve(HitResult hit,Level w,LivingEntity c,SpellStats stats,SpellContext context,SpellResolver r){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}public String getBookDescription(){return "Test continuation";}
    }
}
