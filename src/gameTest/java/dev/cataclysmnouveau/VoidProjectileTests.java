package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_void") @PrefixGameTestTemplate(false)
public final class VoidProjectileTests {
    @GameTest(template="empty",batch="void_0",timeoutTicks=100)
    public static void howitzerNativeMotionCompleteRingsGrowthOneFeeAndChildrenOutliveParent(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(3);
            var marker=new Marker();var carrier=carrier(a,Ability.VOID_HOWITZER,marker,false,true);
            h.assertTrue(ProjectileDispatch.convert(carrier),"Void grenade converts");var shell=entities(a,Void_Howitzer_Entity.class).getFirst();
            near(h,shell.getDeltaMovement().length(),1,"Native fully-charged shoulder weapon speed");near(h,mana(a),9640,"One parent fee plus amplify");
            shell.tick();h.assertTrue(shell.getDeltaMovement().y<0 && !shell.isNoGravity(),"Native gravity retained");
            var friend=a.target(a.center,true);((ProjectileHitAccess)shell).confluxCataclysm$nativeHit(new EntityHitResult(friend));
            h.assertTrue(!shell.isRemoved() && marker.hits==0,"Friendly contact ignored before explosion");
            ((ProjectileHitAccess)shell).confluxCataclysm$nativeHit(block(a.center));
            var runes=entities(a,Void_Rune_Entity.class);h.assertTrue(runes.size()==76,"All five native rings, not a reduced imitation: "+runes.size());
            h.assertTrue(shell.isRemoved() && marker.hits==1 && friend.getHealth()==500,"One block continuation and friendly explosion protection");
            step(1);h.assertTrue(carrier.isRemoved() && runes.stream().noneMatch(Entity::isRemoved),"Paid rune children outlive parent impact");
            var rune=runes.getFirst();h.assertTrue(runes.stream().allMatch(r->r.getCaster()==a.p && !r.shouldBeSaved()),"Real rune owner and transient children");
            var enemy=a.target(rune.position(),false);friend.setPos(rune.position());
            for(int i=0;i<25;i++)rune.tick();
            near(h,500-enemy.getHealth(),12,"Rune base7 plus bounded2 plus Ars3 once");near(h,friend.getHealth(),500,"Rune Core friend filter");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Rune credits original caster");near(h,mana(a),9640,"No per-rune billing");
            a.p.discard();step(1);h.assertTrue(runes.stream().allMatch(Entity::isRemoved),"Owner loss cleans rune children");h.succeed();
        }
    }
    @GameTest(template="empty",batch="void_1",timeoutTicks=100)
    public static void scatterNativeArrowAndAllSeventeenShardsKeepDamageAndOwnership(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(3);
            var marker=new Marker();var carrier=carrier(a,Ability.VOID_SCATTER_ARROW,marker,false,true);h.assertTrue(ProjectileDispatch.convert(carrier),"Scatter arrow converts");
            var arrow=entities(a,Void_Scatter_Arrow_Entity.class).getFirst();near(h,arrow.getDeltaMovement().length(),3,"Native fully drawn bow speed");
            h.assertTrue(arrow.pickup==AbstractArrow.Pickup.DISALLOWED,"No free ammunition pickup");
            var enemy=a.target(a.center,false);((ProjectileHitAccess)arrow).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            near(h,500-enemy.getHealth(),11,"Vanilla arrow6 plus bounded2 plus Ars3 once");h.assertTrue(marker.hits==1 && arrow.isRemoved(),"Successful direct arrow damage continues once");
            var shards=entities(a,Void_Shard_Entity.class);h.assertTrue(shards.size()==17,"All native shards spawned: "+shards.size());
            h.assertTrue(shards.stream().allMatch(s->s.getOwner()==a.p && !s.shouldBeSaved()),"Native shards own context and cannot persist as unpaid items");
            try{
                var canHit=Void_Shard_Entity.class.getDeclaredMethod("canHitEntity",Entity.class);canHit.setAccessible(true);
                h.assertTrue(!(Boolean)canHit.invoke(shards.getFirst(),enemy),"Shards retain native ignore-primary-victim behavior");
            }catch(ReflectiveOperationException e){throw new AssertionError(e);}
            var friend=a.target(a.center.add(3,0,0),true);friend.invulnerableTime=17;
            ((ProjectileHitAccess)shards.getFirst()).confluxCataclysm$nativeHit(new EntityHitResult(friend));
            near(h,friend.getHealth(),500,"Friendly shard is harmless");h.assertTrue(friend.invulnerableTime==17,"Friendly shard cannot clear hurt immunity");
            var secondary=a.target(a.center.add(4,0,0),false);((ProjectileHitAccess)shards.get(1)).confluxCataclysm$nativeHit(new EntityHitResult(secondary));
            near(h,500-secondary.getHealth(),6.5,"Shard1.5 plus bounded2 plus Ars3 once");
            h.assertTrue(secondary.invulnerableTime==0 && secondary.getLastDamageSource().getEntity()==a.p,"Native shard reset and source preserved");
            h.assertTrue(marker.hits==1,"Children do not multiply Ars continuation");near(h,mana(a),9870,"Only parent launch paid");
            a.p.discard();step(1);h.assertTrue(shards.stream().allMatch(Entity::isRemoved),"Owner loss cleans shard children");h.succeed();
        }
    }
    @GameTest(template="empty",batch="void_2",timeoutTicks=100)
    public static void repeatingFormsPayForEveryNativeImpactAndPreserveArsMotion(GameTestHelper h){
        for(var ability:List.of(Ability.VOID_HOWITZER,Ability.VOID_SCATTER_ARROW))try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,ability,marker,true,false);h.assertTrue(ProjectileDispatch.convert(carrier),"Repeated form converts");
            Entity payload=ability==Ability.VOID_HOWITZER?entities(a,Void_Howitzer_Entity.class).getFirst():entities(a,Void_Scatter_Arrow_Entity.class).getFirst();
            var point=payload.position();var motion=payload.getDeltaMovement();for(int i=0;i<20;i++)payload.tick();
            near(h,payload.position().distanceTo(point),0,"No second native trajectory");near(h,payload.getDeltaMovement().distanceTo(motion),0,"No double gravity");near(h,mana(a),10000,"Visual ticks cost nothing");
            var r=ArsSpellAccess.resolver(carrier);r.onResolveEffect(h.getLevel(),block(a.center));r.onResolveEffect(h.getLevel(),block(a.center));
            near(h,mana(a),10000-2*CataclysmConfig.cost(ability),"Each native payload activation is paid");h.assertTrue(!payload.isRemoved() && marker.hits==2,"Native shatter/discard does not consume repeat carrier");
            int children=ability==Ability.VOID_HOWITZER?entities(a,Void_Rune_Entity.class).size():entities(a,Void_Shard_Entity.class).size();
            h.assertTrue(children==(ability==Ability.VOID_HOWITZER?152:34),"Every activation retains complete native formation");
            CapabilityRegistry.getMana(a.p).setMana(0);r.onResolveEffect(h.getLevel(),block(a.center));h.assertTrue(payload.isRemoved() && carrier.isRemoved() && marker.hits==2,"No unpaid final activation");
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="void_3",timeoutTicks=100)
    public static void cancelledArrowDamageKeepsNativeShardsButDoesNotContinue(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var marker=new Marker();var carrier=carrier(a,Ability.VOID_SCATTER_ARROW,marker,false,false);ProjectileDispatch.convert(carrier);
            var arrow=entities(a,Void_Scatter_Arrow_Entity.class).getFirst();var immune=a.target(a.center,false);immune.setInvulnerable(true);
            ((ProjectileHitAccess)arrow).confluxCataclysm$nativeHit(new EntityHitResult(immune));
            near(h,immune.getHealth(),500,"Cancelled damage does not deal health damage");h.assertTrue(marker.hits==0 && entities(a,Void_Shard_Entity.class).size()==17,"Native secondary creation survives without falsely approving a damage continuation");
            near(h,mana(a),9880,"Successful launch was still paid");h.succeed();
        }
    }
    @GameTest(template="empty",batch="void_4",timeoutTicks=100)
    public static void ordinaryNativeScatterRemainsUnmodified(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(30);
            var arrow=new Void_Scatter_Arrow_Entity(ModEntities.VOID_SCATTER_ARROW.get(),h.getLevel());arrow.setOwner(a.p);arrow.setPos(a.center);arrow.shoot(0,0,1,3,0);
            var enemy=a.target(a.center,false);((ProjectileHitAccess)arrow).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            near(h,500-enemy.getHealth(),6,"Native unbound arrow receives no addon Ars bonus");
            h.assertTrue(((ManagedProjectile)arrow).confluxCataclysm$mode()==0,"Native arrow remains unmanaged");
            var shards=entities(a,Void_Shard_Entity.class);h.assertTrue(shards.size()==17 && shards.stream().allMatch(Entity::shouldBeSaved),"Native children are not enrolled in addon lifecycle");shards.forEach(Entity::discard);h.succeed();
        }
    }
    private static EntityProjectileSpell carrier(NativeAbilityTests.Arena a,Ability ability,Marker marker,boolean repeat,boolean amp){
        var parts=new ArrayList<AbstractSpellPart>();parts.add(MethodProjectile.INSTANCE);parts.add(GlyphCatalog.projectile(ability));if(amp)parts.add(AugmentAmplify.INSTANCE);parts.add(marker);
        var r=new SpellResolver(new SpellContext(a.h.getLevel(),new Spell(parts),a.p,LivingCaster.from(a.p),ItemStack.EMPTY));r.spellContext.nextPart();
        var c=repeat?new EntityOrbitProjectile(a.h.getLevel(),r,a.p):new EntityProjectileSpell(a.h.getLevel(),r);c.setPos(a.center.add(0,3,0));c.setDeltaMovement(0,0,.75);return c;
    }
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> type){return a.h.getLevel().getEntitiesOfClass(type,new AABB(a.center,a.center).inflate(40));}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p).below(),false);}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double a,double b,String message){h.assertTrue(Math.abs(a-b)<.002,message+": "+a+" != "+b);}
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","void_marker"),"Marker");}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}public boolean isEnabled(){return true;}protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}public String getBookDescription(){return "Test continuation";}
    }
}
