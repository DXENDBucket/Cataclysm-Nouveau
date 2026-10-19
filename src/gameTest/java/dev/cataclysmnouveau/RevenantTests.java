package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_revenant") @PrefixGameTestTemplate(false)
public final class RevenantTests {
    @GameTest(template="empty",batch="revenant_0",timeoutTicks=100)
    public static void nativeBoneGravityHitSourceShatterAndDamageContinuation(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,marker,false,true);h.assertTrue(ProjectileDispatch.convert(carrier),"Bone form converts");
            var bone=bones(a).getFirst();near(h,bone.getDeltaMovement().length(),.5,"Native eight-bone launch velocity");near(h,mana(a),9965,"Single augmented launch fee");
            bone.tick();h.assertTrue(bone.getDeltaMovement().y<0 && !bone.isNoGravity(),"Native ballistic gravity retained");
            var friend=a.target(a.center,true);((ProjectileHitAccess)bone).confluxCataclysm$nativeHit(new EntityHitResult(friend));
            h.assertTrue(!bone.isRemoved() && marker.hits==0,"Friendly contact neither shatters nor continues");
            var enemy=a.target(a.center.add(3,0,0),false);((ProjectileHitAccess)bone).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            near(h,500-enemy.getHealth(),7,"Native five plus bounded amplify two");h.assertTrue(bone.isRemoved() && marker.hits==1 && enemy.getLastDamageSource().getEntity()==a.p,"Native shatter and successful-damage continuation credit caster");
            step(1);h.assertTrue(carrier.isRemoved(),"Native removal closes carrier");near(h,mana(a),9965,"No ordinary impact fee");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_1",timeoutTicks=100)
    public static void repeatingBoneKeepsArsMotionAndBillsEachActualHit(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,marker,true,false);h.assertTrue(ProjectileDispatch.convert(carrier),"Orbit bone converts");var bone=bones(a).getFirst();
            var point=bone.position();var velocity=bone.getDeltaMovement();for(int i=0;i<80;i++)bone.tick();
            near(h,bone.position().distanceTo(point),0,"No native movement under Ars trajectory");near(h,bone.getDeltaMovement().distanceTo(velocity),0,"No native gravity under Ars trajectory");near(h,mana(a),10000,"Visual ticks are free");
            var enemy=a.target(a.center.add(4,0,0),false);var r=ArsSpellAccess.resolver(carrier);r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            near(h,mana(a),9975,"First repeated hit pays");near(h,500-enemy.getHealth(),5,"Native direct damage");h.assertTrue(!bone.isRemoved() && marker.hits==1,"Repeated carrier survives native shatter event");
            enemy.invulnerableTime=0;r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));near(h,mana(a),9950,"Second contact pays again");h.assertTrue(marker.hits==2,"Second damage continues");
            CapabilityRegistry.getMana(a.p).setMana(0);float hp=enemy.getHealth();enemy.invulnerableTime=0;r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            near(h,enemy.getHealth(),hp,"No unpaid last hit");h.assertTrue(bone.isRemoved() && carrier.isRemoved() && marker.hits==2,"Empty mana ends repeated carrier");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_2",timeoutTicks=100)
    public static void breathUsesActualEyesFacingGrowthBlindnessAndSingleDriver(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);host.setYRot(0);host.setXRot(0);a.p.setYRot(180);a.p.yHeadRot=180;
            var enemy=a.target(a.center.add(0,0,2),false);var far=a.target(a.center.add(0,0,6),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.ASHEN_BREATH,new EntityHitResult(host),true);var breath=breaths(a).getFirst();
            near(h,breath.position().distanceTo(host.getEyePosition()),0,"Actual host eyes emit breath");near(h,mana(a),9965,"First active second prepays with augment");
            h.assertTrue(breath.getCaster()==a.p && !breath.shouldBeSaved() && ((AshenBreath)breath).confluxRevenant$lifetime()==25,"Real owner and synced native lifetime");
            step(2);near(h,enemy.getHealth(),500,"Native warmup before cone attacks");step(4);
            near(h,500-enemy.getHealth(),6,"Native four plus bounded amplify two in ally heading");near(h,far.getHealth(),500,"Native cone grows gradually");near(h,friend.getHealth(),500,"No allied damage or blindness");
            h.assertTrue(enemy.hasEffect(MobEffects.BLINDNESS) && enemy.getEffect(MobEffects.BLINDNESS).getDuration()==60 && enemy.getLastDamageSource().getEntity()==a.p,"Native blindness and real damage credit");
            enemy.invulnerableTime=0;float hp=enemy.getHealth();breath.tick();near(h,enemy.getHealth(),hp,"World tick cannot add another paid-output tick");
            host.setPos(a.center.add(1,0,0));step(1);near(h,breath.position().distanceTo(host.getEyePosition()),0,"Moving ally carries the mouth");host.setPos(a.center);
            step(5);h.assertTrue(far.getHealth()<500,"Native reach eventually hits at six blocks");step(8);near(h,mana(a),9965,"No premature second fee");step(1);near(h,mana(a),9930,"Next active second prepays once");
            step(5);h.assertTrue(breath.isRemoved(),"Native twenty-sixth tick ends breath");near(h,mana(a),9930,"No hidden double tick fees");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_3",timeoutTicks=100)
    public static void breathEmptyManaStopsBeforeNewDamage(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.setYRot(0);a.p.setXRot(0);CapabilityRegistry.getMana(a.p).setMana(25);
            var enemy=a.target(a.center.add(0,0,2),false);a.cast(GlyphCatalog.ASHEN_BREATH,block(a.center),false);var breath=breaths(a).getFirst();
            step(20);enemy.setHealth(500);enemy.invulnerableTime=0;step(1);
            near(h,enemy.getHealth(),500,"Cannot hit on native tick21 without paying next second");h.assertTrue(breath.isRemoved(),"Empty mana ends cone before damage");near(h,mana(a),0,"First second consumed all supplied mana");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_4",timeoutTicks=100)
    public static void breathDurationChangesSyncedLifeAndLeavesNativeUnmanagedAlone(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.setYRot(0);a.p.setXRot(0);
            var resolver=resolver(a,new Spell(MethodTouch.INSTANCE,GlyphCatalog.ASHEN_BREATH,AugmentExtendTime.INSTANCE));resolver.spellContext.nextPart();resolver.onResolveEffect(h.getLevel(),block(a.center));
            var breath=breaths(a).getFirst();h.assertTrue(((AshenBreath)breath).confluxRevenant$lifetime()==38,"Extend updates client and server lifetime");step(27);h.assertTrue(!breath.isRemoved(),"Extended cone survives ordinary expiration");step(12);h.assertTrue(breath.isRemoved(),"Extended cone still naturally ends");
            var ordinary=new Ashen_Breath_Entity(com.github.L_Ender.cataclysm.init.ModEntities.ASHEN_BREATH.get(),h.getLevel(),4,a.p);ordinary.setPos(a.center);
            h.assertTrue(((AshenBreath)ordinary).confluxRevenant$lifetime()==0 && ordinary.shouldBeSaved(),"Ordinary native entity not opted into addon policy");
            for(int i=1;i<=26;i++){ordinary.tickCount=i;ordinary.tick();}h.assertTrue(ordinary.isRemoved(),"Unmanaged native tick still expires normally");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_5",timeoutTicks=100)
    public static void nativeStormFourRandomVolleysChildDamageOneFeeAndCleanup(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);})){
            a.p.setYRot(0);a.p.setXRot(0);a.cast(GlyphCatalog.BONE_STORM,block(a.center),true);near(h,mana(a),9750,"One augmented formation quote");
            step(4);h.assertTrue(bones(a).isEmpty(),"Native first round waits until tick5");int count=0;
            for(int round=0;round<4;round++){
                step(round==0?1:5);var all=bones(a);int added=all.size()-count;h.assertTrue(added==6 || added==8 || added==10,"Original random radial count: "+added);count=all.size();
            }
            var all=bones(a);h.assertTrue(count>=24 && count<=40 && all.stream().allMatch(b->b.getOwner()==a.p && !b.shouldBeSaved() && ((ManagedProjectile)b).confluxCataclysm$sourceAbility()==Ability.BONE_STORM),"All native children have actual owner and transient state");
            h.assertTrue(frames.stream().anyMatch(f->f.valid() && f.positionAnchored() && f.animation()==1 && f.ability().equals("bone_storm")),"New native Revenant formation phantom protocol");
            var enemy=a.target(a.center.add(3,0,0),false);((ProjectileHitAccess)all.getFirst()).confluxCataclysm$nativeHit(new EntityHitResult(enemy));near(h,500-enemy.getHealth(),7,"Native group child applies its own base and augment once");
            near(h,mana(a),9750,"Child contacts have no extra fee");a.p.discard();step(1);h.assertTrue(all.stream().allMatch(Entity::isRemoved) && frames.getLast().closed(),"Root loss cleans every remaining child and closes phantom");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_6",timeoutTicks=100)
    public static void hostileRejectionCreativeParallelEffectsAndAuthorityLoss(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.ASHEN_BREATH,new EntityHitResult(enemy),false);a.cast(GlyphCatalog.BONE_STORM,new EntityHitResult(enemy),false);near(h,mana(a),10000,"No hostile execution fallback or fee");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.ASHEN_BREATH,block(a.center),false);a.cast(GlyphCatalog.BONE_STORM,block(a.center),false);h.assertTrue(breaths(a).isEmpty(),"Unfunded effects do not spawn");
            a.p.setGameMode(GameType.CREATIVE);var host=a.target(a.center,true);host.setYRot(0);a.cast(GlyphCatalog.ASHEN_BREATH,new EntityHitResult(host),false);a.cast(GlyphCatalog.BONE_STORM,new EntityHitResult(host),false);step(5);
            var cone=breaths(a).getFirst();var children=bones(a);h.assertTrue(!children.isEmpty() && !cone.isRemoved() && !ExecutionSessions.isOccupied(host,ExecutionClaim.ENTITY_ACTION),"Both non-body effects run together at creative empty mana");near(h,mana(a),0,"Creative mana unchanged");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);h.assertTrue(cone.isRemoved() && children.stream().allMatch(Entity::isRemoved),"Revoked executor authority cancels both independent effects");h.succeed();
        }
    }
    @GameTest(template="empty",batch="revenant_7",timeoutTicks=100)
    public static void completeBodyPhantomProtocol(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="revenant_8",timeoutTicks=100)
    public static void sharedProjectileRegression(GameTestHelper h){RemnantSecondTests.cursedStormNativeDelayTwoHitsCurseFadeAndOneLaunchFee(h);}
    private static EntityProjectileSpell carrier(NativeAbilityTests.Arena a,Marker marker,boolean repeat,boolean amp){
        var parts=new ArrayList<AbstractSpellPart>();parts.add(MethodProjectile.INSTANCE);parts.add(GlyphCatalog.BLAZING_BONE);if(amp)parts.add(AugmentAmplify.INSTANCE);parts.add(marker);
        var r=resolver(a,new Spell(parts));r.spellContext.nextPart();var c=repeat?new EntityOrbitProjectile(a.h.getLevel(),r,a.p):new EntityProjectileSpell(a.h.getLevel(),r);c.setPos(a.center.add(0,3,0));c.setDeltaMovement(0,0,.75);return c;
    }
    private static SpellResolver resolver(NativeAbilityTests.Arena a,Spell spell){return new SpellResolver(new SpellContext(a.h.getLevel(),spell,a.p,LivingCaster.from(a.p),ItemStack.EMPTY));}
    private static List<Blazing_Bone_Entity> bones(NativeAbilityTests.Arena a){return entities(a,Blazing_Bone_Entity.class);}
    private static List<Ashen_Breath_Entity> breaths(NativeAbilityTests.Arena a){return entities(a,Ashen_Breath_Entity.class);}
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> c){return a.h.getLevel().getEntitiesOfClass(c,new AABB(a.center,a.center).inflate(40));}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double a,double b,String m){h.assertTrue(Math.abs(a-b)<.002,m+": "+a+" != "+b);}
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","revenant_marker"),"Marker");}
        public void onResolve(HitResult hit,Level w,LivingEntity c,SpellStats stats,SpellContext context,SpellResolver r){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}public String getBookDescription(){return "Test continuation";}
    }
}
