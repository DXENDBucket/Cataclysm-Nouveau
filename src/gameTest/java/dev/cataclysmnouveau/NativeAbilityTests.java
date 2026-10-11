package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Bolt_strike_Entity;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau") @PrefixGameTestTemplate(false)
public final class NativeAbilityTests {
    private static Vec3 scene(GameTestHelper h) {
        var origin=h.absoluteVec(new Vec3(4,0,4));
        return new Vec3(origin.x,h.getLevel().getMaxBuildHeight()-48,origin.z);
    }
    private static ServerPlayer player(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CatStudent"),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        p.setGameMode(GameType.SURVIVAL);p.setPos(scene(h).add(-10,0,0));h.getLevel().addNewPlayer(p);
        var mana=CapabilityRegistry.getMana(p);mana.setMaxMana(10000);mana.setMana(10000);return p;
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var c=EntityType.COW.create(h.getLevel());c.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);c.setHealth(500);
        c.setNoAi(true);c.setNoGravity(true);c.setPos(point);h.getLevel().addFreshEntity(c);return c;
    }
    private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
    }
    private static BlockHitResult block(Vec3 v) { return new BlockHitResult(v,Direction.UP,BlockPos.containing(v),false); }
    private static void close(GameTestHelper h,double actual,double expected,String message) { h.assertTrue(Math.abs(actual-expected)<.001,message+": "+actual+" != "+expected); }
    private static List<ThrowableProjectile> payloads(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(ThrowableProjectile.class,new AABB(scene(h),scene(h)).inflate(24),e->e instanceof ManagedProjectile s && s.confluxCataclysm$mode()>0);
    }
    private static void clean(GameTestHelper h,ServerPlayer p) {
        for(var entity:h.getLevel().getEntities((Entity)null,new AABB(scene(h),scene(h)).inflate(24),e->e!=p)) entity.discard();
        p.discard();ExecutionSessions.tick();
    }
    @GameTest(template="empty",batch="cat_payloads",timeoutTicks=100)
    public static void nativePayloadDamagePoisonOwnershipAndOrdinaryEntities(GameTestHelper h) {
        var p=player(h);
        try {
            for(var ability:List.of(Ability.AMETHYST_CLUSTER,Ability.LIONFISH_SPIKE)) {
                var target=cow(h,scene(h).add(0,1,3));
                var nativeEntity=NativePayloads.projectile(ability,h.getLevel(),CastContext.detached(p,null,null),1);
                var state=(ManagedProjectile)nativeEntity;state.confluxCataclysm$mode(2);
                h.getLevel().addFreshEntity(nativeEntity);NativePayloads.hit(nativeEntity,new EntityHitResult(target));
                double base=ability==Ability.AMETHYST_CLUSTER?com.github.L_Ender.cataclysm.config.CMCommonConfig.AmethystCrab.AmethystClusterDamage:4;
                close(h,500-target.getHealth(),base+Resources.damageBonus(1),"Native damage plus configured Harm amplification");
                h.assertTrue(nativeEntity.getOwner()==p && nativeEntity.isRemoved(),"Native owner and on-hit removal retained");
                if (ability==Ability.LIONFISH_SPIKE) h.assertTrue(target.hasEffect(MobEffects.POISON) && target.getEffect(MobEffects.POISON).getDuration()==60,"Native three-second poison retained");
                target.discard();
            }
            var target=cow(h,scene(h).add(0,1,3));var ordinary=new Lionfish_Spike_Entity(h.getLevel(),p);
            ((ManagedProjectile)ordinary).confluxCataclysm$bonus(100);
            NativePayloads.hit(ordinary,new EntityHitResult(target));
            close(h,500-target.getHealth(),4,"Unmanaged original projectile stays unchanged");
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_nativeflight",timeoutTicks=100)
    public static void nativeFlightPaymentContinuationAndCreative(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        try {
            for(var glyph:GlyphCatalog.PROJECTILES) {
                mana.setMana(10000);var marker=new Marker();var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(scene(h).add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
                h.assertTrue(ProjectileDispatch.convert(carrier),"Core dispatches the registered native payload");
                var visual=payloads(h).get(0);var state=(ManagedProjectile)visual;
                h.assertTrue(state.confluxCataclysm$mode()==2 && !visual.shouldBeSaved(),"Native flight is transient and opt-in");
                close(h,visual.getDeltaMovement().length(),glyph.ability==Ability.AMETHYST_CLUSTER?.8:.35,"Native initial speed retained");
                int fee=Resources.quote(cast.spellContext,glyph.ability,List.of()).amount();
                close(h,mana.getCurrentMana(),10000-fee,"Launch charges exactly once");
                NativePayloads.hit(visual,block(scene(h).add(0,0,3)));
                h.assertTrue(marker.hits==1 && visual.isRemoved(),"Native hit runs the Ars continuation exactly once");
                close(h,mana.getCurrentMana(),10000-fee,"One-shot impact does not double charge");ExecutionSessions.tick();
            }
            var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.AMETHYST));cast.spellContext.nextPart();mana.setMana(0);
            var denied=new EntityProjectileSpell(h.getLevel(),cast);denied.setPos(scene(h));ProjectileDispatch.convert(denied);
            h.assertTrue(payloads(h).isEmpty(),"Empty mana creates no payload");
            p.setGameMode(GameType.CREATIVE);var creative=new EntityProjectileSpell(h.getLevel(),cast);creative.setPos(scene(h));ProjectileDispatch.convert(creative);
            h.assertTrue(payloads(h).size()==1,"Creative bypasses mana");
            for(var glyph:GlyphCatalog.ALL) h.assertTrue(h.getLevel().getRecipeManager().byKey(glyph.getRegistryName()).isPresent(),"Every glyph has a real Scribe recipe");
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_repeated",timeoutTicks=100)
    public static void arsDrivenRepeatedImpactsPayAndStopOnEmptyMana(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        try {
            for(var glyph:GlyphCatalog.PROJECTILES) {
                mana.setMana(10000);var marker=new Marker();var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var carrier=new EntityOrbitProjectile(h.getLevel(),cast,p);carrier.setPos(scene(h).add(0,2,0));
                h.assertTrue(ProjectileDispatch.convert(carrier),"Core recognizes orbit");
                var visual=payloads(h).get(0);var state=(ManagedProjectile)visual;
                h.assertTrue(state.confluxCataclysm$mode()==1 && state.confluxTrajectory()==carrier,"Native visual exposes the Ars trajectory");
                var before=visual.position();visual.tick();close(h,visual.position().distanceTo(before),0,"No duplicate server movement");
                close(h,mana.getCurrentMana(),10000,"Repeated carrier creation waits for an impact to charge");
                int fee=Resources.quote(cast.spellContext,glyph.ability,List.of()).amount();var hit=block(scene(h).add(0,0,3));
                for(int i=1;i<=2;i++) {
                    ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),hit);
                    h.assertTrue(marker.hits==i && !visual.isRemoved(),"Repeated native impact retains the carrier and resolves once");
                    close(h,mana.getCurrentMana(),10000-i*fee,"Repeated impact pays each time");
                }
                mana.setMana(0);ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),hit);
                h.assertTrue(marker.hits==2 && visual.isRemoved() && carrier.isRemoved(),"No mana stops both payload and carrier");ExecutionSessions.tick();
            }
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_bolt",timeoutTicks=100)
    public static void nativeBoltWarmupDamageColorAndOwnership(GameTestHelper h) {
        var p=player(h);var target=cow(h,scene(h));var mana=CapabilityRegistry.getMana(p);
        var cast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.BOLT));cast.spellContext.nextPart();
        cast.onResolveEffect(h.getLevel(),new EntityHitResult(target));
        var bolts=h.getLevel().getEntitiesOfClass(Bolt_strike_Entity.class,new AABB(scene(h),scene(h)).inflate(3));
        h.assertTrue(bolts.size()==1,"Actual native registered bolt spawned");
        var bolt=bolts.get(0);h.assertTrue(bolt.getCaster()==p && bolt.getB()==255 && bolt.getG()==170,"Native owner and visible color set");
        close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.BOLT_STRIKE),"Effect charges once");
        h.runAfterDelay(CataclysmConfig.boltWarmup()+13,()->{
            try { close(h,500-target.getHealth(),CataclysmConfig.boltDamage(),"Native delayed lightning damage");h.succeed(); }
            finally {clean(h,p);}
        });
    }

    @GameTest(template="empty",batch="cat_ground",timeoutTicks=100)
    public static void nativeGroundEffectsDamageFireAlliesAndCleanup(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        Vec3 runePoint=scene(h).add(0,0,0), jetPoint=scene(h).add(6,0,0);
        h.getLevel().setBlockAndUpdate(BlockPos.containing(runePoint).below(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(BlockPos.containing(jetPoint).below(),Blocks.STONE.defaultBlockState());
        var runeTarget=cow(h,runePoint);var jetTarget=cow(h,jetPoint);
        var ally=cow(h,jetPoint.add(.1,0,0));
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("cat_"+p.getUUID().toString().substring(0,8));
        scoreboard.addPlayerToTeam(p.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        var marker=new Marker();
        var runeCast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.RUNE,AugmentAmplify.INSTANCE,marker));
        runeCast.spellContext.nextPart();runeCast.onResolveEffect(h.getLevel(),new EntityHitResult(runeTarget));
        var jetCast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.JET,AugmentAmplify.INSTANCE));
        jetCast.spellContext.nextPart();jetCast.onResolveEffect(h.getLevel(),new EntityHitResult(jetTarget));
        var area=new AABB(scene(h),scene(h)).inflate(15);
        var runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area);
        var jets=h.getLevel().getEntitiesOfClass(Flame_Jet_Entity.class,area);
        h.assertTrue(runes.size()==1 && jets.size()==1,"Actual registered native ground entities spawned");
        var rune=runes.get(0);var jet=jets.get(0);
        h.assertTrue(rune.getCaster()==p && jet.getCaster()==p,"Native damage attribution follows caster");
        h.assertTrue(marker.hits==1,"Later Ars effects resolve immediately, independent of native warmup");
        close(h,rune.getDamage(),CataclysmConfig.groundDamage(Ability.VOID_RUNE)+Resources.damageBonus(1),"Rune amplification");
        close(h,jet.getDamage(),CataclysmConfig.groundDamage(Ability.FLAME_JET)+Resources.damageBonus(1),"Jet amplification");
        close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.VOID_RUNE)-CataclysmConfig.cost(Ability.FLAME_JET)-2*CataclysmConfig.amplifyCost(),"Each actual spawn pays its own quote once");
        close(h,runeTarget.getHealth(),500,"Rune has no immediate damage");
        close(h,jetTarget.getHealth(),500,"Jet has no immediate damage");
        h.runAfterDelay(CataclysmConfig.groundWarmup(Ability.FLAME_JET)+9,()->{
            close(h,500-jetTarget.getHealth(),jet.getDamage(),"Native jet attacks after its animation delay");
            h.assertTrue(jetTarget.isOnFire(),"Native five-second ignition retained");
            close(h,ally.getHealth(),500,"Native alliance filtering retained");
            h.assertTrue(!ally.isOnFire(),"Allies are not ignited");
        });
        h.runAfterDelay(CataclysmConfig.groundWarmup(Ability.VOID_RUNE)+17,()->{
            close(h,500-runeTarget.getHealth(),rune.getDamage(),"Native rune attack window and amplified damage retained");
        });
        h.runAfterDelay(Math.max(CataclysmConfig.groundWarmup(Ability.VOID_RUNE)+36,CataclysmConfig.groundWarmup(Ability.FLAME_JET)+24),()->{
            try {
                h.assertTrue(rune.isRemoved() && jet.isRemoved(),"Native lifetime removes both effects");
                h.succeed();
            } finally {scoreboard.removePlayerTeam(team);clean(h,p);}
        });
    }

    @GameTest(template="empty",batch="cat_ground_budget",timeoutTicks=100)
    public static void groundFailureBudgetsBlockPlacementAndDamageOwner(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        Vec3 point=scene(h);var area=new AABB(point,point).inflate(12);
        var cast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.RUNE));cast.spellContext.nextPart();
        try {
            cast.onResolveEffect(h.getLevel(),block(point));
            close(h,mana.getCurrentMana(),10000,"Missing local surface does not charge");
            h.assertTrue(h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area).isEmpty(),"No ground means no rune");
            h.getLevel().setBlockAndUpdate(BlockPos.containing(point),Blocks.STONE_SLAB.defaultBlockState());
            Vec3 top=point.add(0,.5,0);
            mana.setMana(0);cast.onResolveEffect(h.getLevel(),block(top));
            h.assertTrue(h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area).isEmpty(),"Unaffordable cast creates no effect");
            p.setGameMode(GameType.CREATIVE);cast.onResolveEffect(h.getLevel(),block(top));
            var runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area);
            h.assertTrue(runes.size()==1,"Creative bypasses empty mana");
            close(h,runes.get(0).getY(),top.y,"Ground placement respects slab surface");
            runes.get(0).discard();p.setGameMode(GameType.SURVIVAL);mana.setMana(10000);
            var damageOwner=cow(h,point.add(-5,0,0));
            h.assertTrue(NativePayloads.effect(Ability.VOID_RUNE,h.getLevel(),CastContext.detached(p,block(top),null).withDamageOwner(damageOwner),0),"Scoped owner effect succeeds");
            var owned=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area).get(0);
            h.assertTrue(owned.getCaster()==damageOwner,"Executor and damage owner remain distinct");owned.discard();
            for(int i=1;i<=2;i++) {
                cast.onResolveEffect(h.getLevel(),block(top));
                close(h,mana.getCurrentMana(),10000-i*CataclysmConfig.cost(Ability.VOID_RUNE),"Repeated triggers each pay once");
            }
            h.succeed();
        } finally {clean(h,p);}
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker() { super(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","test_marker"),"Marker"); }
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver) { hits++; }
        public int getDefaultManaCost() { return 0; }
        public int getCastingCost() { return 0; }
        public SpellTier defaultTier() { return SpellTier.ONE; }
        public SpellTier getConfigTier() { return SpellTier.ONE; }
        public boolean isEnabled() { return true; }
        protected Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }
        public String getBookDescription() { return "Test continuation"; }
    }
}
