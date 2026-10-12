package dev.cataclysmnouveau;
import dev.arsconflux.api.damage.NativeSpellDamage;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Bolt_strike_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
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
        return player(h,packet->{});
    }
    private static ServerPlayer player(GameTestHelper h,java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> packets) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(),"CatStudent"),false);
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {packets.accept(packet);}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {packets.accept(packet);}
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
            for(var ability:List.of(Ability.AMETHYST_CLUSTER,Ability.LIONFISH_SPIKE,Ability.FLARE_BOMB)) {
                var target=cow(h,scene(h).add(0,1,3));
                var nativeEntity=NativePayloads.projectile(ability,h.getLevel(),CastContext.detached(p,null,null),1);
                var state=(ManagedProjectile)nativeEntity;state.confluxCataclysm$mode(2);
                h.getLevel().addFreshEntity(nativeEntity);NativePayloads.hit(nativeEntity,new EntityHitResult(target));
                double base=ability==Ability.AMETHYST_CLUSTER?com.github.L_Ender.cataclysm.config.CMCommonConfig.AmethystCrab.AmethystClusterDamage:ability==Ability.FLARE_BOMB?7:4;
                close(h,500-target.getHealth(),Resources.augmentedDamage(base,1),"Native damage plus configured Harm amplification");
                h.assertTrue(nativeEntity.getOwner()==p && nativeEntity.isRemoved(),"Native owner and on-hit removal retained");
                if (ability==Ability.LIONFISH_SPIKE) h.assertTrue(target.hasEffect(MobEffects.POISON) && target.getEffect(MobEffects.POISON).getDuration()==60,"Native three-second poison retained");
                target.discard();
            }
            var target=cow(h,scene(h).add(0,1,3));var ordinary=new Lionfish_Spike_Entity(h.getLevel(),p);
            ((ManagedProjectile)ordinary).confluxCataclysm$amplification(100);
            NativePayloads.hit(ordinary,new EntityHitResult(target));
            close(h,500-target.getHealth(),4,"Unmanaged original projectile stays unchanged");
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_nativeflight",timeoutTicks=100)
    public static void nativeFlightPaymentContinuationAndCreative(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        try {
            for(var glyph:List.of(GlyphCatalog.AMETHYST,GlyphCatalog.SPIKE,GlyphCatalog.FLARE)) {
                mana.setMana(10000);var marker=new Marker();var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(scene(h).add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
                h.assertTrue(ProjectileDispatch.convert(carrier),"Core dispatches the registered native payload");
                var visual=payloads(h).get(0);var state=(ManagedProjectile)visual;
                h.assertTrue(state.confluxCataclysm$mode()==2 && !visual.shouldBeSaved(),"Native flight is transient and opt-in");
                close(h,visual.getDeltaMovement().length(),glyph.ability==Ability.AMETHYST_CLUSTER?.8:glyph.ability==Ability.FLARE_BOMB?1:.35,"Native initial speed retained");
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
            for(var glyph:List.of(GlyphCatalog.AMETHYST,GlyphCatalog.SPIKE,GlyphCatalog.FLARE)) {
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
        close(h,rune.getDamage(),CataclysmConfig.groundDamage(Ability.VOID_RUNE),"Rune amplification");
        close(h,jet.getDamage(),CataclysmConfig.groundDamage(Ability.FLAME_JET),"Jet amplification");
        close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.VOID_RUNE)-CataclysmConfig.cost(Ability.FLAME_JET)-2*CataclysmConfig.amplifyCost(),"Each actual spawn pays its own quote once");
        close(h,runeTarget.getHealth(),500,"Rune has no immediate damage");
        close(h,jetTarget.getHealth(),500,"Jet has no immediate damage");
        h.runAfterDelay(CataclysmConfig.groundWarmup(Ability.FLAME_JET)+9,()->{
            close(h,500-jetTarget.getHealth(),Resources.augmentedDamage(jet.getDamage(),1),"Native jet attacks after its animation delay");
            h.assertTrue(jetTarget.isOnFire(),"Native five-second ignition retained");
            close(h,ally.getHealth(),500,"Native alliance filtering retained");
            h.assertTrue(!ally.isOnFire(),"Allies are not ignited");
        });
        h.runAfterDelay(CataclysmConfig.groundWarmup(Ability.VOID_RUNE)+17,()->{
            close(h,500-runeTarget.getHealth(),Resources.augmentedDamage(rune.getDamage(),1),"Native rune attack window and amplified damage retained");
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
    private static Entity newPayload(GameTestHelper h) {
        return h.getLevel().getEntities((Entity)null,new AABB(scene(h),scene(h)).inflate(24),
                e->e instanceof ManagedProjectile state && state.confluxCataclysm$mode()>0).get(0);
    }
    @GameTest(template="empty",batch="cat_anchor",timeoutTicks=120)
    public static void anchorRetainsReturnAndSingleContinuation(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);var marker=new Marker();
        p.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(500);
        var target=cow(h,scene(h).add(0,2,5));
        var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ANCHOR,AugmentAmplify.INSTANCE,marker));cast.spellContext.nextPart();
        var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(scene(h).add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Anchor dispatches through Core");
        var anchor=(Player_Ceraunus_Entity)newPayload(h);var state=(ManagedProjectile)anchor;
        close(h,anchor.getDeltaMovement().length(),2.5,"Native anchor launch speed retained");
        int quote=Resources.quote(cast.spellContext,Ability.CERAUNUS,List.of(AugmentAmplify.INSTANCE)).amount();
        close(h,mana.getCurrentMana(),10000-quote,"Only launch pays");
        ((ProjectileHitAccess)anchor).confluxCataclysm$nativeHit(new EntityHitResult(target));
        close(h,500-target.getHealth(),Resources.augmentedDamage(CataclysmConfig.anchorDamage(),1),"Anchor base ignores caster melee and follows Harm amplification");
        h.assertTrue(anchor.getReturn() && !anchor.isRemoved(),"Native return survives impact");
        h.assertTrue(marker.hits==1 && anchor.getOwner()==p,"Native ownership and exactly one continuation");
        state.confluxCataclysm$impact().accept(new EntityHitResult(target));
        h.assertTrue(marker.hits==1,"Returning anchor cannot repeat Ars phrase");
        ExecutionSessions.tick();h.assertTrue(!anchor.isRemoved(),"Session retains return stage");
        // Freeze the owner and exercise native close-range retrieval directly, avoiding falling fake-player timing.
        anchor.setPos(p.position().add(0,1,0));anchor.setDeltaMovement(Vec3.ZERO);anchor.tick();
        h.assertTrue(anchor.isRemoved(),"Native return removes anchor near its owner");
        clean(h,p);h.succeed();
    }
    @GameTest(template="empty",batch="cat_anchor_wave_orbit",timeoutTicks=100)
    public static void repeatingAnchorAndWavePayWithoutAmbientDamage(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);
        try {
            for(var glyph:List.of(GlyphCatalog.ANCHOR,GlyphCatalog.WAVE)) {
                mana.setMana(10000);var marker=new Marker();var target=cow(h,scene(h).add(0,2,0));
                var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var carrier=new EntityOrbitProjectile(h.getLevel(),cast,p);carrier.setPos(target.position());
                h.assertTrue(ProjectileDispatch.convert(carrier),"Core converts repeated anchor/wave");
                var payload=newPayload(h);var state=(ManagedProjectile)payload;var start=payload.position();
                payload.tick();close(h,payload.position().distanceTo(start),0,"Native tick cannot move an Ars-driven payload");
                close(h,target.getHealth(),500,"Repeating wave has no unpaid ambient contact damage");
                close(h,mana.getCurrentMana(),10000,"Repeated carrier creation is uncharged");
                int fee=Resources.quote(cast.spellContext,glyph.ability,List.of()).amount();
                ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));
                close(h,mana.getCurrentMana(),10000-fee,"Repeated impact pays once");
                h.assertTrue(marker.hits==1 && !payload.isRemoved(),"Paid hit continues Ars and retains carrier");
                if(payload instanceof Player_Ceraunus_Entity anchor) h.assertTrue(!anchor.getReturn(),"Orbit does not switch to native return");
                if(payload instanceof Wave_Entity) h.assertTrue(target.hasEffect(ModEffect.EFFECTWETNESS),"Paid wave retains native Wetness");
                mana.setMana(0);ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));
                h.assertTrue(marker.hits==1 && payload.isRemoved() && carrier.isRemoved(),"Empty mana closes trajectory without a free continuation");
                target.discard();ExecutionSessions.tick();
            }
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_native_wave",timeoutTicks=100)
    public static void nativeWaveContactsAlliesDedupAndLifetime(GameTestHelper h) {
        var p=player(h);var mana=CapabilityRegistry.getMana(p);var marker=new Marker();
        var target=cow(h,scene(h).add(0,0,1));var ally=cow(h,target.position());
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("wave_"+p.getUUID().toString().substring(0,8));
        scoreboard.addPlayerToTeam(p.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.WAVE,marker));cast.spellContext.nextPart();
        var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(target.position());carrier.setDeltaMovement(new Vec3(0,0,.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Native wave conversion");
        var wave=(Wave_Entity)newPayload(h);var control=(WaveControl)wave;
        h.assertTrue(wave.getOwner()==p && wave.getState()==1,"Native owner and spawn animation retained");
        close(h,wave.getDamage(),9,"Scylla damage baseline");
        close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.WAVE),"Whole wave pays once at creation");
        control.confluxCataclysm$attack(1.5,0,-1);
        close(h,500-target.getHealth(),wave.getDamage(),"Native contact damage");
        h.assertTrue(target.hasEffect(ModEffect.EFFECTWETNESS),"Native Wetness retained");
        close(h,ally.getHealth(),500,"Native allied filtering retained");
        target.invulnerableTime=0;control.confluxCataclysm$attack(1.5,0,-1);
        close(h,500-target.getHealth(),2*wave.getDamage(),"Native repeated damage remains possible");
        h.assertTrue(marker.hits==1,"One native wave continues only once per target");
        close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.WAVE),"Native contact does not bill repeatedly");
        h.runAfterDelay(CataclysmConfig.waveLifetime()+3,()->{
            try {h.assertTrue(wave.isRemoved(),"Wave retains bounded native lifespan");h.succeed();}
            finally {scoreboard.removePlayerTeam(team);clean(h,p);}
        });
    }
    @GameTest(template="empty",batch="cat_wave_duration",timeoutTicks=140)
    public static void waveDurationAndRecipeValidation(GameTestHelper h) {
        var p=player(h);
        var extend=com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime.INSTANCE;
        var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.WAVE,extend);
        h.assertTrue(dev.arsconflux.api.glyph.SpellValidators.validate(ArsSpellAccess.parts(spell)).isEmpty(),"Wave accepts duration augmentation");
        h.assertTrue(!dev.arsconflux.api.glyph.SpellValidators.validate(ArsSpellAccess.parts(new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ANCHOR,extend))).isEmpty(),"Anchor does not accept native-wave duration");
        h.assertTrue(!dev.arsconflux.api.glyph.SpellValidators.validate(ArsSpellAccess.parts(new Spell(MethodProjectile.INSTANCE,extend))).isEmpty(),"Unrelated Ars projectile duration stays restricted");
        var cast=resolver(h,p,spell);cast.spellContext.nextPart();
        var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(scene(h));carrier.setDeltaMovement(new Vec3(0,0,.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Extended wave converts");
        var wave=(Wave_Entity)newPayload(h);
        int duration=NativeCarrierCasting.waveDuration(1);
        h.assertTrue(wave.getMaxTicks()==duration && duration==120,"Four-second wave gains two seconds from Extend Time");
        h.runAfterDelay(85,()->h.assertTrue(!wave.isRemoved(),"Extended wave survives beyond baseline lifespan"));
        h.runAfterDelay(duration+3,()->{try {h.assertTrue(wave.isRemoved(),"Extended wave removes after its modified lifespan");h.succeed();}finally{clean(h,p);}});
    }
    @GameTest(template="empty",batch="cat_cooldowns",timeoutTicks=100)
    public static void activeCooldownsAndCreativeBypass(GameTestHelper h) {
        var p=player(h);
        try {
            var provider=new CataclysmActiveCasts();var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.WAVE);
            var request=new ActiveCastRequest(spell,p,ActiveCastSource.CASTER_TOOL);
            h.assertTrue(provider.allowed(request),"Ready active recipe accepted");
            provider.released(request,List.of(spell,spell));
            h.assertTrue(CataclysmActiveCasts.remaining(p,Ability.WAVE)==100 && !provider.allowed(request),"Duplicate branches commit a single shared deadline");
            h.assertTrue(!provider.supports(new Spell(MethodProjectile.INSTANCE,com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm.INSTANCE)),"Pure Ars spell is unaffected");
            p.setGameMode(GameType.CREATIVE);h.assertTrue(provider.allowed(request),"Creative bypasses cooldown");
            h.succeed();
        } finally {clean(h,p);}
    }
    @GameTest(template="empty",batch="cat_descriptions",timeoutTicks=100)
    public static void augmentDescriptionsReuseResolvableAddonKeys(GameTestHelper h) throws Exception {
        for(var locale:List.of("en_us","zh_cn")) {
            try(var stream=NativeAbilityTests.class.getResourceAsStream("/assets/cataclysm_nouveau/lang/"+locale+".json")) {
                h.assertTrue(stream!=null,"Packaged language exists");
                var json=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                var components=new ArrayList<net.minecraft.network.chat.Component>();
                for(var profile:CarrierRegistry.profiles()) if(profile.glyph()!=null)
                    for(var form:GlyphCatalog.PROJECTILES) {
                        var description=profile.glyph().augmentDescriptions.get(form);
                        h.assertTrue(description!=null,"Every carrier has a projectile-form description");components.add(description);
                    }
                for(var glyph:GlyphCatalog.ALL.stream().filter(g->g instanceof dev.cataclysmnouveau.glyph.CataclysmEffectGlyph).toList())
                    components.addAll(glyph.augmentDescriptions.values());
                for(var component:components) {
                    h.assertTrue(component.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents,"Description remains translatable");
                    var key=((net.minecraft.network.chat.contents.TranslatableContents)component.getContents()).getKey();
                    h.assertTrue(json.has(key) && !json.get(key).getAsString().isBlank(),"Tooltip key exists in "+locale+": "+key);
                }
            }
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="cat_flare_pattern",timeoutTicks=110)
    public static void flarePreservesNativePatternAmplificationAlliesAndLifetime(GameTestHelper h) {
        var p=player(h);p.setNoGravity(true);p.setPos(scene(h).add(-15,4,0));
        var mana=CapabilityRegistry.getMana(p);var marker=new Marker();
        var base=BlockPos.containing(scene(h));var impact=scene(h).add(0,1,0);
        var area=new AABB(impact,impact).inflate(14);
        var forced=new ArrayList<net.minecraft.world.level.ChunkPos>();
        // Wide attacks extend beyond the small GameTest template's entity-ticking chunks.
        for(int x=(base.getX()-14)>>4;x<=(base.getX()+14)>>4;x++)
            for(int z=(base.getZ()-14)>>4;z<=(base.getZ()+14)>>4;z++) {
                h.getLevel().setChunkForced(x,z,true);forced.add(new net.minecraft.world.level.ChunkPos(x,z));
            }
        for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++)
            h.getLevel().setBlockAndUpdate(base.offset(x,0,z),Blocks.STONE.defaultBlockState());
        // Native Boss projectiles are opt-out, even if an addon bonus field happens to be set.
        var ordinary=new Flare_Bomb_Entity(com.github.L_Ender.cataclysm.init.ModEntities.FLARE_BOMB.get(),h.getLevel(),p);
        ordinary.setPos(impact);((ManagedProjectile)ordinary).confluxCataclysm$amplification(100);
        NativePayloads.hit(ordinary,block(impact));
        var originalJets=h.getLevel().getEntitiesOfClass(Flame_Jet_Entity.class,area);
        h.assertTrue(originalJets.size()==40,"Native four-arm pattern creates 40 jets on flat ground");
        for(var jet:originalJets) {close(h,jet.getDamage(),7,"Original Boss jet damage is unchanged");jet.discard();}

        var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.FLARE,AugmentAmplify.INSTANCE,marker);
        var cast=resolver(h,p,spell);cast.spellContext.nextPart();
        var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(impact);carrier.setDeltaMovement(new Vec3(0,0,.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Flare dispatches through Core");
        var payload=(Flare_Bomb_Entity)payloads(h).getFirst();
        h.assertTrue(payload.getOwner()==p && !payload.shouldBeSaved(),"Native owner and transient payload retained");
        close(h,payload.getDeltaMovement().length(),1,"Native flare launch speed retained");
        NativePayloads.hit(payload,block(impact));
        var jets=h.getLevel().getEntitiesOfClass(Flame_Jet_Entity.class,area);
        h.assertTrue(jets.size()==40 && payload.isRemoved() && marker.hits==1,"Native impact expands once and continues Ars once");
        for(var jet:jets) {
            h.assertTrue(jet.getCaster()==p,"Generated jets inherit the actual damage owner");
            close(h,jet.getDamage(),CataclysmConfig.flareJetDamage(),"Amplification reaches secondary attacks");
        }
        var outer=jets.stream().max(Comparator.comparingDouble(jet->jet.position().distanceToSqr(impact))).orElseThrow();
        var target=cow(h,outer.position());var ally=cow(h,outer.position());
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("flare_"+p.getUUID().toString().substring(0,8));
        scoreboard.addPlayerToTeam(p.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        int quote=Resources.quote(cast.spellContext,Ability.FLARE_BOMB,List.of(AugmentAmplify.INSTANCE)).amount();
        close(h,mana.getCurrentMana(),10000-quote,"One launch includes all secondary jets without double billing");
        h.runAfterDelay(65,()->{
            try {
                h.assertTrue(target.getHealth()<500 && target.isOnFire(),"Delayed native jets hit and ignite enemies");
                close(h,ally.getHealth(),500,"Native jet alliance filtering retained");
                h.assertTrue(jets.stream().allMatch(Entity::isRemoved),"Native delayed jets clean themselves up");
                h.assertTrue(CataclysmConfig.cooldownTicks(Ability.FLARE_BOMB)==160,"Flare default active cooldown is eight seconds");
                h.succeed();
            } finally {
                scoreboard.removePlayerTeam(team);clean(h,p);
                for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++)
                    h.getLevel().setBlockAndUpdate(base.offset(x,0,z),Blocks.AIR.defaultBlockState());
                for(var chunk:forced) h.getLevel().setChunkForced(chunk.x,chunk.z,false);
            }
        });
    }


    private static final class Arena implements AutoCloseable {
        final GameTestHelper h;final ServerPlayer p;final Vec3 center;final BlockPos base;
        final List<net.minecraft.world.level.ChunkPos> forced=new ArrayList<>();
        final List<Entity> actors=new ArrayList<>();final net.minecraft.world.scores.PlayerTeam team;
        Arena(GameTestHelper h) {
            this.h=h;p=player(h);p.setNoGravity(true);center=scene(h).add(0,1,0);base=BlockPos.containing(scene(h));
            p.setPos(center.add(-22,2,0));
            for(int x=(base.getX()-20)>>4;x<=(base.getX()+20)>>4;x++) for(int z=(base.getZ()-20)>>4;z<=(base.getZ()+20)>>4;z++)
                if(!h.getLevel().getForcedChunks().contains(net.minecraft.world.level.ChunkPos.asLong(x,z))) {
                    h.getLevel().setChunkForced(x,z,true);forced.add(new net.minecraft.world.level.ChunkPos(x,z));
                }
            for(int x=-18;x<=18;x++) for(int z=-18;z<=18;z++) h.getLevel().setBlockAndUpdate(base.offset(x,0,z),Blocks.STONE.defaultBlockState());
            var board=h.getLevel().getScoreboard();team=board.addPlayerTeam("mon_"+p.getUUID().toString().substring(0,8));board.addPlayerToTeam(p.getScoreboardName(),team);
        }
        LivingEntity target(Vec3 point,boolean allied) {
            var entity=cow(h,point);actors.add(entity);
            if(allied) h.getLevel().getScoreboard().addPlayerToTeam(entity.getScoreboardName(),team);
            return entity;
        }
        void cast(dev.cataclysmnouveau.glyph.CataclysmEffectGlyph glyph,HitResult hit,boolean amp) {
            var parts=new ArrayList<AbstractSpellPart>();parts.add(MethodTouch.INSTANCE);parts.add(glyph);
            if(amp) parts.add(AugmentAmplify.INSTANCE);
            var cast=resolver(h,p,new Spell(parts));cast.spellContext.nextPart();cast.onResolveEffect(h.getLevel(),hit);
        }
        @Override public void close() {
            actors.forEach(Entity::discard);h.getLevel().getScoreboard().removePlayerTeam(team);clean(h,p);
            for(int x=-18;x<=18;x++) for(int z=-18;z<=18;z++) for(int y=0;y<=3;y++)
                h.getLevel().setBlockAndUpdate(base.offset(x,y,z),Blocks.AIR.defaultBlockState());
            for(var chunk:forced) h.getLevel().setChunkForced(chunk.x,chunk.z,false);
        }
    }
    @GameTest(template="empty",batch="cat_mon_quake",timeoutTicks=100)
    public static void bossQuakeDamageOwnershipLowHealthAndRefund(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var victim=a.target(a.center.add(0,0,2),false);var ally=a.target(a.center.add(0,0,2),true);
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();
            a.p.setHealth(5);a.cast(GlyphCatalog.QUAKE,block(a.center),true);
            double attack=25*com.github.L_Ender.cataclysm.config.CMCommonConfig.NetheriteMonstrosity.attackMultiplier;
            double expected=Resources.augmentedDamage(attack+Math.min(attack,500*com.github.L_Ender.cataclysm.config.CMCommonConfig.NetheriteMonstrosity.SmashHpdamage),1);
            close(h,500-victim.getHealth(),expected,"Quake uses Boss base and capped target-health damage, plus flat amplification");
            h.assertTrue(victim.getLastDamageSource().getEntity()==a.p && victim.getDeltaMovement().y>0 && victim.isOnFire(),"Owner, native launch and berserk ignition preserved");
            close(h,ally.getHealth(),500,"Allied targets excluded");close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.EARTHQUAKE)+10,"One actual attack pays once");
            before=mana.getCurrentMana();a.cast(GlyphCatalog.QUAKE,BlockHitResult.miss(a.center,Direction.UP,BlockPos.containing(a.center)),false);
            close(h,mana.getCurrentMana(),before,"Invalid location refunds the quote");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_mon_rings",timeoutTicks=110)
    public static void pairedNativeFlameRingsOwnerAmplificationAndCleanup(GameTestHelper h) {
        var a=new Arena(h);a.cast(GlyphCatalog.RINGS,block(a.center),true);
        var jets=h.getLevel().getEntitiesOfClass(Flame_Jet_Entity.class,new AABB(a.center,a.center).inflate(20));
        h.assertTrue(jets.size()==56,"Healthy Boss pattern is two sets of seven spokes with four rings");
        for(var jet:jets) {h.assertTrue(jet.getCaster()==a.p,"Native jet owner is the caster, never a dummy Boss");close(h,jet.getDamage(),CataclysmConfig.groundDamage(Ability.FLAME_JET),"Ring amplification reaches each native jet");}
        a.p.setHealth(5);a.cast(GlyphCatalog.RINGS,block(a.center),false);
        var all=h.getLevel().getEntitiesOfClass(Flame_Jet_Entity.class,new AABB(a.center,a.center).inflate(20));
        h.assertTrue(all.size()==56+224,"Berserk Boss produces two sets of fourteen spokes and eight rings");
        // Forced chunks may need a few ticks to become entity-ticking; allow that activation margin.
        h.runAfterDelay(90,()->{try {h.assertTrue(all.stream().allMatch(Entity::isRemoved),"All native delayed jets expire; remaining ages: "+all.stream().filter(e->!e.isRemoved()).map(e->e.tickCount).toList());h.succeed();}finally{a.close();}});
    }
    @GameTest(template="empty",batch="cat_mon_fissure",timeoutTicks=100)
    public static void fissureAdvancesOverTimeAndStopsOnOwnerLoss(GameTestHelper h) {
        var a=new Arena(h);a.p.setPos(a.center.add(0,2,-22));
        var near=a.target(a.center.add(0,0,2),false);var far=a.target(a.center.add(0,0,12),false);var ally=a.target(a.center.add(0,0,2),true);
        a.cast(GlyphCatalog.FISSURE,block(a.center),true);
        close(h,far.getHealth(),500,"Distant wave has not happened at initial activation");
        h.runAfterDelay(3,()->{h.assertTrue(near.getHealth()<500,"Near ground shock hits first");close(h,far.getHealth(),500,"Far ground shock remains delayed");});
        h.runAfterDelay(15,()->{try {
            h.assertTrue(far.getHealth()<500 && far.getLastDamageSource().getEntity()==a.p,"Wave propagates to distant ground with actual caster attribution");
            close(h,ally.getHealth(),500,"Fissure protects allies");
            double before=CapabilityRegistry.getMana(a.p).getCurrentMana();a.cast(GlyphCatalog.FISSURE,block(a.center),false);
            a.p.discard();ExecutionSessions.tick();close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before-CataclysmConfig.cost(Ability.FISSURE),"Repeated wave pays once, not per ground tile");
            h.succeed();
        }finally{a.close();}});
    }
    @GameTest(template="empty",batch="cat_mon_charge",timeoutTicks=100)
    public static void shoulderUsesNativeCollisionBoneFractureAndObstacleStop(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.setPos(a.center);a.p.setYRot(0);var victim=a.target(a.center.add(0,0,1),false);var ally=a.target(a.center.add(0,0,1),true);
            a.cast(GlyphCatalog.CHARGE,new EntityHitResult(a.p),true);ExecutionSessions.tick();
            h.assertTrue(victim.hasEffect(ModEffect.EFFECTBONE_FRACTURE) && victim.getLastDamageSource().getEntity()==a.p,"Native shoulder impact inflicts fracture with actual attribution");
            close(h,500-victim.getHealth(),Resources.augmentedDamage(25*.4*com.github.L_Ender.cataclysm.config.CMCommonConfig.NetheriteMonstrosity.attackMultiplier,1),"Native shoulder damage plus Harm amplification");
            close(h,ally.getHealth(),500,"Shoulder protects allies");h.assertTrue(a.p.getDeltaMovement().z>0 && dev.arsconflux.api.execution.ExecutionSessions.isCasting(a.p),"Caster receives a forward dash during the session");
            a.p.horizontalCollision=true;ExecutionSessions.tick();h.assertTrue(!ExecutionSessions.isCasting(a.p),"Obstacle stops the dash session");a.p.horizontalCollision=false;
            a.cast(GlyphCatalog.CHARGE,new EntityHitResult(ally),false);ExecutionSessions.tick();h.assertTrue(ally.getDeltaMovement().z>0,"An allied target can execute the dash");
            a.p.discard();ExecutionSessions.tick();h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_mon_absorb",timeoutTicks=100)
    public static void nativeLavaAbsorptionConsumesThreeWavesAndRefundsEmptyGround(GameTestHelper h) {
        var a=new Arena(h);a.p.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);a.p.setHealth(50);
        var positions=List.of(a.base.offset(3,1,0),a.base.offset(6,1,0),a.base.offset(12,1,0));
        positions.forEach(p->h.getLevel().setBlockAndUpdate(p,Blocks.LAVA.defaultBlockState()));
        a.cast(GlyphCatalog.ABSORB,block(a.center),true);
        h.assertTrue(h.getLevel().getBlockState(positions.getFirst()).isAir() && h.getLevel().getBlockState(positions.getLast()).is(Blocks.LAVA),"First native absorption wave is local");
        h.runAfterDelay(7,()->{try {
            h.assertTrue(positions.stream().allMatch(p->h.getLevel().getBlockState(p).isAir()),"All three native lava absorption radii execute");
            close(h,a.p.getHealth(),50+3*(CataclysmConfig.absorptionHeal()+Resources.healingBonus(1)),"Only successful waves heal with amplification");
            double before=CapabilityRegistry.getMana(a.p).getCurrentMana();a.cast(GlyphCatalog.ABSORB,block(a.center),false);
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before,"Empty area produces no healing and refunds mana");h.succeed();
        }finally{a.close();}});
    }
    @GameTest(template="empty",batch="cat_mon_buff",timeoutTicks=100)
    public static void monstrousUsesNativeEffectAndFriendlyTargetRules(GameTestHelper h) {
        var a=new Arena(h);var ally=a.target(a.center,true);ally.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);ally.setHealth(20);
        var enemy=a.target(a.center.add(0,0,3),false);a.cast(GlyphCatalog.MONSTROUS,new EntityHitResult(ally),true);
        var status=ally.getEffect(ModEffect.EFFECTMONSTROUS);
        h.assertTrue(status!=null && status.getAmplifier()==1 && status.getDuration()==200,"Native level and duration retained");
        close(h,ally.getAttributeValue(Attributes.ARMOR),6,"Native amplified armor modifier");close(h,ally.getAttributeValue(Attributes.ARMOR_TOUGHNESS),4,"Native amplified toughness");
        double before=CapabilityRegistry.getMana(a.p).getCurrentMana();a.cast(GlyphCatalog.MONSTROUS,new EntityHitResult(enemy),false);
        h.assertTrue(!enemy.hasEffect(ModEffect.EFFECTMONSTROUS),"Buff does not apply to an enemy");close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before,"Enemy target refunds mana");
        h.runAfterDelay(52,()->{try {h.assertTrue(ally.getHealth()>20,"Native low-health recovery works");h.succeed();}finally{a.close();}});
    }
    @GameTest(template="empty",batch="cat_mon_lava",timeoutTicks=100)
    public static void lavaBombIndependentPoolsNoSpreadAndOwnerCleanup(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var marker=new Marker();var mana=CapabilityRegistry.getMana(a.p);
            var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.LAVA,AugmentAmplify.INSTANCE,marker);var cast=resolver(h,a.p,spell);cast.spellContext.nextPart();
            var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(a.center);carrier.setDeltaMovement(new Vec3(0,0,.75));
            h.assertTrue(ProjectileDispatch.convert(carrier),"Lava bomb uses normal Core dispatch");var visual=(Lava_Bomb_Entity)payloads(h).getFirst();
            var victim=a.target(a.center.add(0,0,1),false);var ally=a.target(a.center.add(0,0,1),true);
            NativePayloads.hit(visual,block(a.center));h.assertTrue(visual.isRemoved() && marker.hits==1,"Parent impact continues Ars exactly once");
            var sources=h.getLevel().getEntitiesOfClass(Lava_Bomb_Entity.class,new AABB(a.center,a.center).inflate(20),Lava_Bomb_Entity::getGround);
            h.assertTrue(sources.size()==1 && sources.getFirst().getOwner()==a.p,"A separate native grounded bomb owns the pool");
            var pos=sources.getFirst().getLavaPos();h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.LAVA) && TemporaryLava.contains(h.getLevel(),pos),"Managed temporary lava is present");
            h.assertTrue(victim.getHealth()<500 && victim.getLastDamageSource().getEntity()==a.p,"Native explosion damages enemies with caster ownership");close(h,ally.getHealth(),500,"Explosion protects allies");
            net.minecraft.world.level.material.Fluids.LAVA.tick(h.getLevel(),pos,h.getLevel().getFluidState(pos));
            h.assertTrue(h.getLevel().getFluidState(pos.relative(Direction.NORTH)).isEmpty(),"Managed lava source cannot spread");
            close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(Ability.LAVA_BOMB)-10,"Pool does not pay again after launch");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(h.getLevel().getBlockState(pos).isAir() && TemporaryLava.count(h.getLevel())==0,"Owner loss clears native pools and tracking");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_mon_lava_expiry",timeoutTicks=240)
    public static void lavaPoolsExpireNaturallyAndDontRemoveReplacedBlocks(GameTestHelper h) {
        var a=new Arena(h);
        var original=NativePayloads.projectile(Ability.LAVA_BOMB,h.getLevel(),CastContext.detached(a.p,null,null),0);
        ((ManagedProjectile)original).confluxCataclysm$mode(2);original.setPos(a.center);NativePayloads.hit(original,block(a.center));
        var pool=h.getLevel().getEntitiesOfClass(Lava_Bomb_Entity.class,new AABB(a.center,a.center).inflate(20)).getFirst();var pos=pool.getLavaPos();
        var second=a.center.add(3,0,0);var other=NativePayloads.projectile(Ability.LAVA_BOMB,h.getLevel(),CastContext.detached(a.p,null,null),0);
        ((ManagedProjectile)other).confluxCataclysm$mode(2);other.setPos(second);NativePayloads.hit(other,block(second));
        var replacement=BlockPos.containing(second);h.getLevel().setBlockAndUpdate(replacement,Blocks.STONE.defaultBlockState());
        h.runAfterDelay(CataclysmConfig.lavaLifetime()+4,()->{try {
            h.assertTrue(pool.isRemoved() && h.getLevel().getBlockState(pos).isAir(),"Native pool and source naturally expire");
            h.assertTrue(h.getLevel().getBlockState(replacement).is(Blocks.STONE),"Cleanup respects a replacement block");
            h.assertTrue(TemporaryLava.count(h.getLevel())==0,"No source records leak");h.succeed();
        }finally{a.close();}});
    }


    @GameTest(template="empty",batch="cat_mon_lava_repeat",timeoutTicks=100)
    public static void repeatedLavaPaysPerImpactAndKeepsIndependentPools(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);var marker=new Marker();
            var cast=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.LAVA,marker));cast.spellContext.nextPart();
            var carrier=new EntityOrbitProjectile(h.getLevel(),cast,a.p);carrier.setPos(a.center.add(0,3,0));
            h.assertTrue(ProjectileDispatch.convert(carrier),"Orbit dispatches the lava payload");
            var payload=(Lava_Bomb_Entity)payloads(h).getFirst();var state=(ManagedProjectile)payload;
            h.assertTrue(state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated(),"Lava visual uses the Ars repeated trajectory");
            state.confluxCataclysm$impact().accept(block(a.center));state.confluxCataclysm$impact().accept(block(a.center.add(3,0,0)));
            h.assertTrue(!payload.isRemoved() && !payload.getGround() && marker.hits==2,"Carrier continues flying while each hit makes an independent pool");
            h.assertTrue(h.getLevel().getEntitiesOfClass(Lava_Bomb_Entity.class,new AABB(a.center,a.center).inflate(20),Lava_Bomb_Entity::getGround).size()==2,"Two native pool owners coexist");
            close(h,mana.getCurrentMana(),10000-2*CataclysmConfig.cost(Ability.LAVA_BOMB),"Exactly one payment per successful repeated impact");
            mana.setMana(0);state.confluxCataclysm$impact().accept(block(a.center.add(6,0,0)));
            h.assertTrue(payload.isRemoved() && marker.hits==2,"Empty mana stops the repeated carrier and does not continue Ars");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(TemporaryLava.count(h.getLevel())==0,"All independent pools clean on owner loss");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_mon_lava_recovery",timeoutTicks=100)
    public static void persistedLavaCleanupSurvivesLostTransientEntities(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var original=NativePayloads.projectile(Ability.LAVA_BOMB,h.getLevel(),CastContext.detached(a.p,null,null),0);
            ((ManagedProjectile)original).confluxCataclysm$mode(2);NativePayloads.hit(original,block(a.center));
            var pool=h.getLevel().getEntitiesOfClass(Lava_Bomb_Entity.class,new AABB(a.center,a.center).inflate(20)).getFirst();var pos=pool.getLavaPos();
            var data=TemporaryLavaData.get(h.getLevel(),false);
            var serialized=data.save(new net.minecraft.nbt.CompoundTag(),h.getLevel().registryAccess());
            h.assertTrue(!serialized.getList("sources",net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty(),"Cleanup locations are in SavedData");
            pool.discard();h.getLevel().setBlockAndUpdate(pos,Blocks.LAVA.defaultBlockState());
            var recovered=TemporaryLavaData.load(serialized,h.getLevel().registryAccess());
            h.getLevel().getDataStorage().set("cataclysm_nouveau_temporary_lava",recovered);
            h.assertTrue(TemporaryLava.contains(h.getLevel(),pos),"Recovered source is prevented from spreading before cleanup");
            TemporaryLavaData.cleanup(h.getLevel());
            h.assertTrue(h.getLevel().getBlockState(pos).isAir() && TemporaryLava.count(h.getLevel())==0,"Reloaded records clean without a saved projectile, with no mutation-iteration failure");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_status",timeoutTicks=100)
    public static void fractureNativeAttributesDurationAndRefund(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var enemy=a.target(a.center.add(3,0,0),false);
            double speed=enemy.getAttributeValue(Attributes.MOVEMENT_SPEED);
            a.cast(GlyphCatalog.FRACTURE,new EntityHitResult(enemy),true);
            var status=enemy.getEffect(ModEffect.EFFECTBONE_FRACTURE);
            h.assertTrue(status!=null && status.getDuration()==100 && status.getAmplifier()==1,"Native fracture level and duration");
            close(h,enemy.getAttributeValue(Attributes.MOVEMENT_SPEED),speed-.04,"Native fracture applies amplified movement penalty");
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-40-CataclysmConfig.amplifyCost(),"Fracture charges once");
            var ally=a.target(a.center.add(-3,0,0),true);
            double mana=CapabilityRegistry.getMana(a.p).getCurrentMana();
            a.cast(GlyphCatalog.FRACTURE,new EntityHitResult(ally),false);
            a.cast(GlyphCatalog.FRACTURE,block(a.center),false);
            h.assertTrue(!ally.hasEffect(ModEffect.EFFECTBONE_FRACTURE),"Fracture excludes allies");
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),mana,"Ally and block failures refund");
            var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.FRACTURE,com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime.INSTANCE);
            var r=resolver(h,a.p,spell);r.spellContext.nextPart();r.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            // A weaker, longer status is kept by vanilla as a hidden effect.
            enemy.removeEffect(ModEffect.EFFECTBONE_FRACTURE);
            var r2=resolver(h,a.p,spell);r2.spellContext.nextPart();r2.onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            h.assertTrue(enemy.getEffect(ModEffect.EFFECTBONE_FRACTURE).getDuration()==150,"Extend Time increases native duration");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_status",timeoutTicks=100)
    public static void lavaWalkingAlliesCostsCollisionAndDamageExpiry(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var ally=a.target(a.center,true);var enemy=a.target(a.center.add(3,0,0),false);
            a.cast(GlyphCatalog.LAVA_WALKING,new EntityHitResult(ally),false);
            h.assertTrue(ally.hasEffect(CataclysmEffects.LAVA_WALKING) && ally.getEffect(CataclysmEffects.LAVA_WALKING).getDuration()==600,"Synced ally status lasts 30 seconds");
            double mana=CapabilityRegistry.getMana(a.p).getCurrentMana();
            a.cast(GlyphCatalog.LAVA_WALKING,new EntityHitResult(enemy),false);
            a.cast(GlyphCatalog.LAVA_WALKING,block(a.center),false);
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),mana,"Enemy and block failures refund");
            h.assertTrue(!enemy.hasEffect(CataclysmEffects.LAVA_WALKING),"Buff excludes enemies");
            h.getLevel().setBlockAndUpdate(a.base,Blocks.LAVA.defaultBlockState());
            var lava=h.getLevel().getFluidState(a.base);
            h.assertTrue(ally.canStandOnFluid(lava) && !ally.canStandOnFluid(Blocks.WATER.defaultBlockState().getFluidState()),"Only lava surface becomes solid");
            ally.setPos(a.base.getX()+.5,a.base.getY()+1.2,a.base.getZ()+.5);
            ally.move(MoverType.SELF,new Vec3(0,-2,0));
            h.assertTrue(ally.getY()>=a.base.getY()+.49,"Real fluid collision supports the actor");
            double health=ally.getHealth();ally.hurt(h.getLevel().damageSources().lava(),4);
            close(h,ally.getHealth(),health,"Lava damage blocked during buff");
            ally.hurt(h.getLevel().damageSources().onFire(),1);
            close(h,ally.getHealth(),health,"Lingering burns blocked during buff");
            ally.setShiftKeyDown(true);h.assertTrue(!ally.canStandOnFluid(lava),"Crouch allows diving");ally.setShiftKeyDown(false);
            ally.removeEffect(CataclysmEffects.LAVA_WALKING);
            h.assertTrue(!ally.canStandOnFluid(lava),"Removing effect restores normal fluid collision");
            ally.invulnerableTime=0;ally.hurt(h.getLevel().damageSources().lava(),4);
            h.assertTrue(ally.getHealth()<health,"Damage protection ends with the effect");
            a.cast(GlyphCatalog.LAVA_WALKING,new EntityHitResult(a.p),false);
            h.assertTrue(a.p.canStandOnFluid(lava),"Player self-cast uses the same status");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_status_buoyancy",timeoutTicks=80)
    public static void lavaWalkingRisesAndExpiresWithoutPermanentFlags(GameTestHelper h) {
        var p=player(h);var pos=BlockPos.containing(scene(h));
        for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) h.getLevel().setBlockAndUpdate(pos.offset(x,0,z),Blocks.LAVA.defaultBlockState());
        var actor=cow(h,Vec3.atBottomCenterOf(pos).add(0,.2,0));((net.minecraft.world.entity.Mob)actor).setNoAi(false);actor.setNoGravity(false);
        actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(CataclysmEffects.LAVA_WALKING,40));
        h.runAfterDelay(25,()->{
            h.assertTrue(actor.getY()>pos.getY()+.8,"Submerged actor rises to the lava surface");
            // This cow's FloatGoal can jump after reaching the surface. Check standing separately,
            // rather than requiring an airborne swimmer to remain grounded at an arbitrary tick.
            ((net.minecraft.world.entity.Mob)actor).setNoAi(true);
            actor.setPos(Vec3.atBottomCenterOf(pos).add(0,.95,0));actor.setDeltaMovement(Vec3.ZERO);actor.setOnGround(false);actor.tick();
            h.assertTrue(actor.onGround(),"Lava surface walking counts as grounded: height="+(actor.getY()-pos.getY())+", velocity="+actor.getDeltaMovement());
            actor.setDeltaMovement(0,.42,0);actor.tick();
            h.assertTrue(actor.getDeltaMovement().y>.12,"Surface grounding preserves a real jump's upward velocity");
            h.assertTrue(!actor.isNoGravity(),"Buoyancy does not change permanent gravity flags");
        });
        h.runAfterDelay(45,()->{try {
            h.assertTrue(!actor.hasEffect(CataclysmEffects.LAVA_WALKING) && !actor.canStandOnFluid(Blocks.LAVA.defaultBlockState().getFluidState()),"Natural expiry restores normal lava behavior");
            h.succeed();
        } finally {
            clean(h,p);
            for(int x=-1;x<=1;x++) for(int z=-1;z<=1;z++) h.getLevel().setBlockAndUpdate(pos.offset(x,0,z),Blocks.AIR.defaultBlockState());
        }});
    }
    @GameTest(template="empty",batch="cat_scylla_spears",timeoutTicks=100)
    public static void scyllaNativeSpearsBounceWetnessAndBlockChildren(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var victim=a.target(a.center.add(0,1,3),false);
            var frame=CastContext.detached(a.p,null,null);
            var water=(Water_Spear_Entity)SpearCasting.create(Ability.WATER_SPEAR,h.getLevel(),frame,new Vec3(1,0,0),1,0);
            ((ManagedProjectile)water).confluxCataclysm$mode(2);
            ((ProjectileHitAccess)water).confluxCataclysm$nativeHit(new EntityHitResult(victim));
            close(h,500-victim.getHealth(),Resources.augmentedDamage(CataclysmConfig.scyllaSpearDamage(),1),"Native spear damage and amplification");
            h.assertTrue(victim.hasEffect(ModEffect.EFFECTWETNESS) && victim.getEffect(ModEffect.EFFECTWETNESS).getDuration()==200,"Native Wetness retained");
            ((ProjectileHitAccess)water).confluxCataclysm$nativeHit(new BlockHitResult(a.center,Direction.WEST,BlockPos.containing(a.center),false));
            h.assertTrue(water.getTotalBounces()==7 && water.getDeltaMovement().x<0,"Native water reflection and bounce budget retained");
            var lightning=(Lightning_Spear_Entity)SpearCasting.create(Ability.LIGHTNING_SPEAR,h.getLevel(),frame,new Vec3(0,0,1),1,0);
            ((ManagedProjectile)lightning).confluxCataclysm$mode(2);
            ((ProjectileHitAccess)lightning).confluxCataclysm$nativeHit(block(a.center));
            var box=new AABB(a.center,a.center).inflate(12);
            var storms=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity.class,box);
            var fields=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity.class,box);
            h.assertTrue(lightning.isRemoved() && storms.size()==1 && fields.size()==1,"Lightning block impact creates both native subeffects");
            h.assertTrue(storms.getFirst().getCaster()==a.p && fields.getFirst().getOwner()==a.p,"Child damage ownership stays with the caster");
            close(h,storms.getFirst().getDamage(),CataclysmConfig.scyllaFieldDamage(),"Native spear landing damage uses its area damage parameter");
            h.assertTrue(!storms.getFirst().shouldBeSaved() && !fields.getFirst().shouldBeSaved(),"Transient child context is not saved");
            var ordinary=new Water_Spear_Entity(a.p,new Vec3(0,0,1),h.getLevel(),3,2);
            h.assertTrue(((ManagedProjectile)ordinary).confluxCataclysm$mode()==0 && ordinary.shouldBeSaved(),"Ordinary Boss spears remain unmanaged");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_scylla_repeated",timeoutTicks=100)
    public static void scyllaSpearCarrierPaymentContinuationsAndEmptyMana(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);
            for(var glyph:List.of(GlyphCatalog.WATER_SPEAR,GlyphCatalog.LIGHTNING_SPEAR)) {
                mana.setMana(10000);var marker=new Marker();
                var cast=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var carrier=new EntityProjectileSpell(h.getLevel(),cast);carrier.setPos(a.center.add(0,2,0));carrier.setDeltaMovement(new Vec3(0,0,1));
                ProjectileDispatch.convert(carrier);
                var box=new AABB(a.center,a.center).inflate(20);
                var spear=h.getLevel().getEntitiesOfClass(Elemental_Spear_Entity.class,box).getFirst();
                close(h,spear.getDeltaMovement().length(),2,"Native spear launch speed retained");
                close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(glyph.ability),"Native launch pays once");
                ((ProjectileHitAccess)spear).confluxCataclysm$nativeHit(block(a.center.add(0,0,2)));
                h.assertTrue(marker.hits==1,"Native impact continues once");
                close(h,mana.getCurrentMana(),10000-CataclysmConfig.cost(glyph.ability),"Native impact does not double charge");
                spear.discard();ExecutionSessions.tick();
                mana.setMana(10000);marker=new Marker();cast=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,glyph,marker));cast.spellContext.nextPart();
                var orbit=new EntityOrbitProjectile(h.getLevel(),cast,a.p);orbit.setPos(a.center.add(0,2,0));
                ProjectileDispatch.convert(orbit);spear=h.getLevel().getEntitiesOfClass(Elemental_Spear_Entity.class,box).getFirst();
                var before=spear.position();spear.tick();close(h,spear.position().distanceTo(before),0,"Ars-driven spear has no duplicate native movement");
                h.assertTrue(spear.getState()==1,"Native spawn animation state retained");
                close(h,mana.getCurrentMana(),10000,"Orbit charges at impact, not creation");
                for(int i=1;i<=2;i++) {
                    ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center));
                    h.assertTrue(marker.hits==i && !spear.isRemoved(),"Recurring native impact continues once and keeps the carrier");
                    close(h,mana.getCurrentMana(),10000-i*CataclysmConfig.cost(glyph.ability),"Each recurring impact pays");
                }
                mana.setMana(0);ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center));
                h.assertTrue(spear.isRemoved() && orbit.isRemoved() && marker.hits==2,"Empty mana stops spear and trajectory");ExecutionSessions.tick();
            }
            a.p.setGameMode(GameType.CREATIVE);mana.setMana(0);
            var creativeCast=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.WATER_SPEAR));creativeCast.spellContext.nextPart();
            var creative=new EntityProjectileSpell(h.getLevel(),creativeCast);creative.setPos(a.center.add(0,2,0));
            ProjectileDispatch.convert(creative);
            h.assertTrue(!h.getLevel().getEntitiesOfClass(Elemental_Spear_Entity.class,new AABB(a.center,a.center).inflate(20)).isEmpty(),"Creative spears bypass mana");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_scylla_storms",timeoutTicks=110)
    public static void scyllaFieldsBurstOwnershipAlliesAndCleanup(GameTestHelper h) {
        var a=new Arena(h);
        var victim=a.target(a.center,false);var ally=a.target(a.center,true);
        a.cast(GlyphCatalog.STORM,new EntityHitResult(victim),true);
        a.cast(GlyphCatalog.FIELD,block(a.center),true);
        double mana=CapabilityRegistry.getMana(a.p).getCurrentMana();
        a.cast(GlyphCatalog.BURST,block(a.center),true);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),mana-CataclysmConfig.cost(Ability.STORM_BURST)-CataclysmConfig.amplifyCost(),"Full burst pays once");
        var box=new AABB(a.center,a.center).inflate(20);
        var storms=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity.class,box);
        var sparks=h.getLevel().getEntitiesOfClass(Spark_Entity.class,box);
        var fields=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity.class,box);
        h.assertTrue(storms.size()==36 && sparks.size()==2,"Native burst produces 35 delayed strikes and two sparks, besides standalone storm");
        h.assertTrue(storms.stream().allMatch(e->e.getCaster()==a.p) && sparks.stream().allMatch(e->e.getOwner()==a.p),"No detached Boss retains damage ownership");
        h.assertTrue(h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity.class,box).isEmpty(),"Invocation frame is never spawned");
        close(h,fields.getFirst().getDamage(),CataclysmConfig.scyllaFieldDamage(),"Field amplification");
        h.runAfterDelay(32,()->{
            h.assertTrue(victim.getHealth()<500 && victim.getLastDamageSource().getEntity()==a.p,"Native storm/field damage reaches the target with caster credit");
            close(h,ally.getHealth(),500,"Storm and field skip allies");
        });
        h.runAfterDelay(65,()->{try {
            h.assertTrue(storms.stream().allMatch(Entity::isRemoved),"Native delayed strikes naturally expire");
            a.p.discard();fields.forEach(Entity::tick);sparks.forEach(Entity::tick);
            h.assertTrue(fields.stream().allMatch(Entity::isRemoved) && sparks.stream().allMatch(Entity::isRemoved),"Owner departure cleans fields and sparks");
            h.succeed();
        } finally {a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_snakes",timeoutTicks=110)
    public static void scyllaSerpentsAimStrikeWetnessAndExpiry(GameTestHelper h) {
        var a=new Arena(h);var victim=a.target(a.center,false);var ally=a.target(a.center,true);
        a.cast(GlyphCatalog.SERPENT,new EntityHitResult(victim),false);
        var snakes=h.getLevel().getEntitiesOfClass(Storm_Serpent_Entity.class,new AABB(a.center,a.center).inflate(20));
        h.assertTrue(snakes.size()==2 && snakes.stream().allMatch(e->e.getCaster()==a.p && !e.shouldBeSaved()),"Two transient native serpents carry actual caster ownership");
        h.runAfterDelay(65,()->{
            h.assertTrue(victim.getHealth()<500 && victim.hasEffect(ModEffect.EFFECTWETNESS) && victim.getEffect(ModEffect.EFFECTWETNESS).getAmplifier()==4,"Native aim and delayed strike reach selection and apply Wetness V");
            close(h,ally.getHealth(),500,"Serpents skip allies");
        });
        h.runAfterDelay(95,()->{try {
            h.assertTrue(snakes.stream().allMatch(Entity::isRemoved),"Native serpents complete their disappearance");h.succeed();
        } finally {a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_spark",timeoutTicks=100)
    public static void electricSparkNativeLandingAndRepeatedPayment(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var victim=a.target(a.center.add(0,1,2),false);
            var spark=NativePayloads.projectile(Ability.SPARK,h.getLevel(),CastContext.detached(a.p,null,null),1);
            spark.setPos(a.center);
            var state=(ManagedProjectile)spark;state.confluxCataclysm$mode(2);
            NativePayloads.hit(spark,new EntityHitResult(victim));close(h,victim.getHealth(),500,"Native spark has no direct entity damage");
            NativePayloads.hit(spark,block(a.center));
            var box=new AABB(a.center,a.center).inflate(20);
            var storms=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity.class,box);
            var fields=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity.class,box);
            h.assertTrue(spark.isRemoved() && storms.size()==1 && fields.size()==1,"Native spark landing generates both complete subeffects");
            close(h,storms.getFirst().getDamage(),CataclysmConfig.scyllaStormDamage(),"Spark strike amplification");
            h.assertTrue(storms.getFirst().getCaster()==a.p && fields.getFirst().getOwner()==a.p,"Spark subeffects retain real ownership");
            var mana=CapabilityRegistry.getMana(a.p);mana.setMana(10000);var marker=new Marker();
            var cast=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SPARK,marker));cast.spellContext.nextPart();
            var orbit=new EntityOrbitProjectile(h.getLevel(),cast,a.p);orbit.setPos(a.center.add(0,2,0));
            ProjectileDispatch.convert(orbit);
            var visual=h.getLevel().getEntitiesOfClass(Spark_Entity.class,box).getFirst();
            var before=visual.position();visual.tick();close(h,visual.position().distanceTo(before),0,"Ars drives spark movement once");
            for(int i=1;i<=2;i++) {
                ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center));
                h.assertTrue(marker.hits==i && !visual.isRemoved(),"Recurring spark resolves once and retains the carrier");
                close(h,mana.getCurrentMana(),10000-i*CataclysmConfig.cost(Ability.SPARK),"Recurring spark pays once per impact, not per child");
            }
            mana.setMana(0);ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center));
            h.assertTrue(visual.isRemoved() && orbit.isRemoved() && marker.hits==2,"Empty mana stops both spark and trajectory");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_scylla_cloud",timeoutTicks=200)
    public static void nativeThundercloudRainsBothSpearsAndOwnerLossStopsIt(GameTestHelper h) {
        var a=new Arena(h);var victim=a.target(a.center,false);var spears=new ArrayList<Elemental_Spear_Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> observer=event->{
            if(event.getEntity() instanceof Elemental_Spear_Entity spear && spear.getOwner()==a.p) spears.add(spear);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        double before=CapabilityRegistry.getMana(a.p).getCurrentMana();
        a.cast(GlyphCatalog.CLOUD,new EntityHitResult(victim),true);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before-CataclysmConfig.cost(Ability.THUNDER_CLOUD)-CataclysmConfig.amplifyCost(),"Whole thundercloud charges once");
        h.runAfterDelay(CataclysmConfig.scyllaCloudWarmup()+36,()->{try {
            long waters=spears.stream().filter(Water_Spear_Entity.class::isInstance).count();
            long lightnings=spears.stream().filter(Lightning_Spear_Entity.class::isInstance).count();
            h.assertTrue(waters==31 && lightnings==31,"Native cloud completes 31 paired water/lightning volleys");
            for(var spear:spears) {
                h.assertTrue(spear.getOwner()==a.p && !spear.shouldBeSaved(),"Cloud spear belongs to caster and is transient");
                double base=spear instanceof Water_Spear_Entity?com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.SpearDamage:com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.SnakeDamage;
                close(h,spear.getDamage(),base,"Cloud preserves its native per-type damage plus amplification");
            }
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before-CataclysmConfig.cost(Ability.THUNDER_CLOUD)-CataclysmConfig.amplifyCost(),"No per-spear hidden extra fee");
            int completed=spears.size();a.cast(GlyphCatalog.CLOUD,new EntityHitResult(victim),false);
            a.p.discard();ExecutionSessions.tick();spears.stream().filter(e->!e.isRemoved()).forEach(Entity::tick);
            h.assertTrue(spears.size()==completed && spears.stream().allMatch(Entity::isRemoved),"Owner loss stops charging cloud and its remaining native spears");
            h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_rows",timeoutTicks=110)
    public static void nativeLightningRowsCompleteBothWavesAndExpire(GameTestHelper h) {
        var a=new Arena(h);var victim=a.target(a.center,false);
        var strikes=new ArrayList<com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> observer=event->{
            if(event.getEntity() instanceof com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity storm && storm.getCaster()==a.p) strikes.add(storm);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        a.cast(GlyphCatalog.ROW,new EntityHitResult(victim),true);
        h.runAfterDelay(30,()->h.assertTrue(strikes.size()==9,"Native first row contains nine strikes"));
        h.runAfterDelay(80,()->{try {
            h.assertTrue(strikes.size()==18 && strikes.stream().allMatch(Entity::isRemoved),"Both nine-strike native waves complete and expire");
            for(var storm:strikes) {
                close(h,storm.getDamage(),CataclysmConfig.scyllaStormDamage(),"Row strike amplification");
                h.assertTrue(storm.getCaster()==a.p && !storm.shouldBeSaved(),"Rows retain true damage ownership without a Boss");
            }
            h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_combo",timeoutTicks=130)
    public static void thunderComboNativeStrikesSpearsAlliesAndPayment(GameTestHelper h) {
        var a=new Arena(h);var actor=a.target(a.center,true);actor.setYRot(0);
        var victim=a.target(a.center.add(0,0,5),false);var ally=a.target(a.center.add(.1,0,5),true);
        var sparks=new ArrayList<Spark_Entity>();var waters=new ArrayList<Water_Spear_Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> observer=event->{
            if(event.getEntity() instanceof Spark_Entity e && e.getOwner()==a.p) sparks.add(e);
            if(event.getEntity() instanceof Water_Spear_Entity e && e.getOwner()==a.p) waters.add(e);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        a.cast(GlyphCatalog.COMBO,new EntityHitResult(actor),true);
        double paid=10000-CataclysmConfig.cost(Ability.THUNDER_COMBO)-CataclysmConfig.amplifyCost();
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),paid,"Combo pays once on start");
        a.cast(GlyphCatalog.COMBO,new EntityHitResult(actor),false);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),paid,"Busy recipient rejects and refunds a second action");
        h.runAfterDelay(25,()->{
            h.assertTrue(victim.getHealth()<500 && victim.getLastDamageSource().getEntity()==a.p,"Native first melee strike credits payer, not recipient or Boss");
            close(h,ally.getHealth(),500,"Native melee excludes nearby allies");
        });
        h.runAfterDelay(110,()->{try {
            h.assertTrue(sparks.size()==32 && waters.size()==5,"Native combo produces two 16-spark strikes and five water spears: "+sparks.size()+" / "+waters.size());
            h.assertTrue(waters.stream().allMatch(e->e.getOwner()==a.p && !e.shouldBeSaved()),"Goal-spawned spears retain root ownership and transient lifecycle");
            close(h,actor.getHealth(),500,"Ally executor receives no friendly damage");
            close(h,ally.getHealth(),500,"Combo children also exclude allies");
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),paid,"Children do not charge additional mana");
            h.assertTrue(!ExecutionSessions.isCasting(a.p),"Ally action does not occupy original caster");h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_spin",timeoutTicks=80)
    public static void anchorWhirlNativeDamageFriendlyPushAndOwnerCleanup(GameTestHelper h) {
        var a=new Arena(h);var actor=a.target(a.center,true);actor.setYRot(0);
        var victim=a.target(a.center.add(0,0,4),false);var ally=a.target(a.center.add(1,0,2),true);
        a.cast(GlyphCatalog.SPIN,new EntityHitResult(actor),true);
        h.runAfterDelay(54,()->{try {
            h.assertTrue(victim.getHealth()<500 && victim.getLastDamageSource().getEntity()==a.p,"Native spin attacks credit the original caster");
            close(h,actor.getHealth(),500,"Spin excludes actor");close(h,ally.getHealth(),500,"Spin excludes ally damage");
            close(h,ally.getDeltaMovement().length(),0,"Native cyclone does not push allies");
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-CataclysmConfig.cost(Ability.ANCHOR_SPIN)-CataclysmConfig.amplifyCost(),"Whole spin pays once");
            a.cast(GlyphCatalog.SPIN,new EntityHitResult(actor),false);a.p.discard();ExecutionSessions.tick();
            h.assertTrue(ScyllaActions.effect(Ability.ANCHOR_SPIN,h.getLevel(),CastContext.detached(actor,new EntityHitResult(actor),null),0,0),"Owner loss releases actor lock for another valid caster");
            actor.discard();ExecutionSessions.tick();h.succeed();
        } finally {a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_water",timeoutTicks=100)
    public static void waterWalkingAlliesDurationSurfaceAndExpiry(GameTestHelper h) {
        var a=new Arena(h);var actor=a.target(a.center,true);var enemy=a.target(a.center.add(3,0,0),false);
        var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.WATER_WALKING,com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime.INSTANCE));
        r.spellContext.nextPart();r.onResolveEffect(h.getLevel(),new EntityHitResult(actor));
        h.assertTrue(actor.getEffect(CataclysmEffects.WATER_WALKING).getDuration()==900,"Water Walking uses standard 50% time extension");
        double paid=CapabilityRegistry.getMana(a.p).getCurrentMana();a.cast(GlyphCatalog.WATER_WALKING,new EntityHitResult(enemy),false);a.cast(GlyphCatalog.WATER_WALKING,block(a.center),false);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),paid,"Invalid water buff targets refund mana");
        h.getLevel().setBlockAndUpdate(a.base.above(),Blocks.WATER.defaultBlockState());
        var water=Blocks.WATER.defaultBlockState().getFluidState();
        h.assertTrue(actor.canStandOnFluid(water) && !actor.canStandOnFluid(Blocks.LAVA.defaultBlockState().getFluidState()),"Water buff solidifies only water");
        actor.setNoGravity(false);actor.setPos(Vec3.atBottomCenterOf(a.base.above()).add(0,.95,0));actor.setDeltaMovement(Vec3.ZERO);actor.setOnGround(false);actor.tick();
        h.assertTrue(actor.onGround(),"Water surface is grounded");actor.setDeltaMovement(0,.42,0);actor.tick();
        h.assertTrue(actor.getDeltaMovement().y>.12,"Water walking preserves jumping");actor.setShiftKeyDown(true);
        h.assertTrue(!actor.canStandOnFluid(water),"Crouching allows water diving");actor.setShiftKeyDown(false);
        actor.removeEffect(CataclysmEffects.WATER_WALKING);actor.addEffect(new net.minecraft.world.effect.MobEffectInstance(CataclysmEffects.WATER_WALKING,30));
        h.runAfterDelay(45,()->{try {
            h.assertTrue(!actor.hasEffect(CataclysmEffects.WATER_WALKING) && !actor.canStandOnFluid(water) && !actor.isNoGravity(),"Natural expiry restores normal water physics and gravity flags");h.succeed();
        } finally {a.close();}});
    }
    @GameTest(template="empty",batch="cat_scylla_leap",timeoutTicks=180)
    public static void anchorPlungeUsesNativeJumpChainAndLandingBurst(GameTestHelper h) {
        var a=new Arena(h);var actor=a.target(a.center,true);actor.setYRot(0);actor.setXRot(65);actor.setOnGround(true);
        ((Mob)actor).setNoAi(false);actor.setNoGravity(false);actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var anchors=new ArrayList<Player_Ceraunus_Entity>();var strikes=new ArrayList<com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> observer=event->{
            if(event.getEntity() instanceof Player_Ceraunus_Entity e && e.getOwner()==actor) anchors.add(e);
            // Spark landing also creates a storm, with native delay -9; count the 35 primary landing strikes separately.
            if(event.getEntity() instanceof com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity e && e.getCaster()==a.p && e.getDelay()!=-9) strikes.add(e);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        a.cast(GlyphCatalog.LEAP,new EntityHitResult(actor),true);
        h.assertTrue(actor.getDeltaMovement().y>1 && !actor.onGround(),"Native goal supplies actor's launch impulse");
        h.runAfterDelay(170,()->{try {
            h.assertTrue(anchors.size()==1 && anchors.getFirst().isRemoved() && !anchors.getFirst().shouldBeSaved(),"One native chain anchor appears then cleans up");
            h.assertTrue(actor.position().distanceToSqr(a.center)>4,"Recipient travels using physical jump and pull, not payer movement");
            h.assertTrue(strikes.size()==35,"Ground landing generates complete native burst: "+strikes.size()+", actor="+actor.position()+", velocity="+actor.getDeltaMovement());
            close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-CataclysmConfig.cost(Ability.ANCHOR_LEAP)-CataclysmConfig.amplifyCost(),"Anchor and landing burst cost one action quote");
            h.assertTrue(strikes.stream().allMatch(e->e.getCaster()==a.p && !e.shouldBeSaved()),"Landing burst damage belongs to root caster");h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);a.close();}});
    }
    @GameTest(template="empty",batch="cat_ars_growth",timeoutTicks=100)
    public static void arsDamageAttributesEventsNativeIsolationAndCancellation(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(6);
            var victim=a.target(a.center.add(0,0,3),false);
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.AMETHYST));
            var frame=CastContext.of(r.spellContext,new EntityHitResult(victim),null);
            int[] calls={0,0};boolean[] cancel={false};float[] quoted={0};
            java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Pre> pre=e->{
                if(e.caster==a.p && e.target==victim) {calls[0]++;quoted[0]=e.damage;if(cancel[0])e.setCanceled(true);else e.damage*=1.5F;}
            };
            java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Post> post=e->{if(e.caster==a.p && e.target==victim)calls[1]++;};
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(pre);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(post);
            try {
                var payload=NativePayloads.projectile(Ability.AMETHYST_CLUSTER,h.getLevel(),frame,1);((ManagedProjectile)payload).confluxCataclysm$mode(2);
                float base=Resources.augmentedDamage(com.github.L_Ender.cataclysm.config.CMCommonConfig.AmethystCrab.AmethystClusterDamage,1);
                NativePayloads.hit(payload,new EntityHitResult(victim));
                close(h,quoted[0],base+6,"Ars adds equipment attribute once, after native amplification");
                h.assertTrue(500-victim.getHealth()>=(base+6)*1.5 && calls[0]==1 && calls[1]==1,"One Pre and one successful Post; native damage also supports external bridge scaling");
                victim.invulnerableTime=0;victim.setHealth(500);cancel[0]=true;
                payload=NativePayloads.projectile(Ability.AMETHYST_CLUSTER,h.getLevel(),frame,0);((ManagedProjectile)payload).confluxCataclysm$mode(2);NativePayloads.hit(payload,new EntityHitResult(victim));
                close(h,victim.getHealth(),500,"Ars Pre cancellation prevents native damage");h.assertTrue(calls[0]==2 && calls[1]==1,"Cancelled damage has no Post");
                cancel[0]=false;victim.invulnerableTime=0;var ordinary=new Lionfish_Spike_Entity(h.getLevel(),a.p);NativePayloads.hit(ordinary,new EntityHitResult(victim));
                close(h,500-victim.getHealth(),4,"Ordinary original projectiles receive no Ars equipment bonus");h.assertTrue(calls[0]==2,"Ordinary native attack emits no adapter Ars event");h.succeed();
            } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(pre);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(post);}
        }
    }
    @GameTest(template="empty",batch="cat_ars_growth_delayed",timeoutTicks=100)
    public static void arsGrowthReachesDelayedFlareChildrenAndRealBridge(GameTestHelper h) {
        var a=new Arena(h);var victim=a.target(a.center.add(.75,0,.75),false);
        a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(6);
        var spell=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.FLARE));
        var frame=CastContext.of(spell.spellContext,block(a.center),null);
        var payload=NativePayloads.projectile(Ability.FLARE_BOMB,h.getLevel(),frame,0);((ManagedProjectile)payload).confluxCataclysm$mode(2);payload.setPos(a.center);NativePayloads.hit(payload,block(a.center));
        int[] hits={0};
        java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Post> post=e->{if(e.caster==a.p && e.target==victim) {hits[0]++;h.assertTrue(e.damage>=CataclysmConfig.flareJetDamage()+6,"Delayed native jets carry Ars attributes and event context");}};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(post);
        h.runAfterDelay(75,()->{try {h.assertTrue(hits[0]>0,"At least one actual delayed jet reaches the event chain");h.succeed();}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(post);a.close();}});
    }
    @GameTest(template="empty",batch="cat_ars_growth_bridge",timeoutTicks=100)
    public static void installedArsSpellsBridgeScalesNativeWaveOnce(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var id=dev.arsconflux.api.projectile.CarrierRegistry.id("irons_spellbooks:spell_power");
            var attribute=net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getHolder(id);
            if(attribute.isEmpty()) {h.succeed();return;}
            a.p.getAttribute(attribute.get()).setBaseValue(2);
            var enemy=a.target(a.center.add(0,0,1),false);
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.WAVE));
            var frame=CastContext.of(r.spellContext,new EntityHitResult(enemy),null);
            var wave=NativeSpellDamage.bind(new Wave_Entity(h.getLevel(),a.p,80,9),frame);((ManagedProjectile)wave).confluxCataclysm$mode(2);wave.setPos(a.center);wave.setState(1);h.getLevel().addFreshEntity(wave);
            wave.tick();h.assertTrue(enemy.getHealth()<500,"Real native wave contact occurs");
            close(h,500-enemy.getHealth(),18,"Installed Ars n Spells maps 2x Iron power onto native contact exactly once");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_air_hook",timeoutTicks=180)
    public static void hookResamplesAirborneAllyAimAndRestoresGravity(GameTestHelper h) {
        var a=new Arena(h);LivingEntity actor;
        if(net.neoforged.fml.ModList.get().isLoaded("irons_spellbooks")) {
            try {
                var type=Class.forName("io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedSwordEntity");
                actor=(LivingEntity)type.getConstructor(Level.class,LivingEntity.class).newInstance(h.getLevel(),a.p);
                actor.setPos(a.center.add(0,5,0));h.getLevel().addFreshEntity(actor);a.actors.add(actor);
            } catch(ReflectiveOperationException error) {a.close();throw new IllegalStateException(error);}
        } else actor=a.target(a.center.add(0,5,0),true);
        actor.setNoGravity(true);actor.setYRot(0);actor.setXRot(20);
        var anchors=new ArrayList<Player_Ceraunus_Entity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> observer=e->{if(e.getEntity() instanceof Player_Ceraunus_Entity hook && hook.getOwner()==actor)anchors.add(hook);};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(observer);
        a.cast(GlyphCatalog.LEAP,new EntityHitResult(actor),false);
        h.assertTrue(ScyllaActions.isLeaping(actor) && !actor.isNoGravity(),"Airborne hovering ally is accepted and temporarily follows action gravity");
        actor.setXRot(35);Vec3 launchDirection=actor.getLookAngle();
        h.runAfterDelay(9,()->{h.assertTrue(anchors.size()==1,"Airborne recipient throws its own native chain");var hook=anchors.get(0);h.assertTrue(hook.isNoGravity() && hook.getDeltaMovement().normalize().dot(launchDirection)>.999,"Launch uses current post-jump look, without old-point downward correction or ballistic drop");});
        h.runAfterDelay(170,()->{try {h.assertTrue(!ScyllaActions.isLeaping(actor) && actor.isNoGravity() && anchors.get(0).isRemoved(),"Action clears the hook/AI lock and restores pre-existing floating state");h.succeed();}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(observer);a.close();}});
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

    private static List<net.minecraft.world.entity.projectile.Projectile> harbingerPayloads(GameTestHelper h) {
        return h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,new AABB(scene(h),scene(h)).inflate(24),e->HarbingerCasting.nativePayload(e) && ((ManagedProjectile)e).confluxCataclysm$mode()>0);
    }
    @GameTest(template="empty",batch="cat_harbinger_flight",timeoutTicks=100)
    public static void harbingerFlightFeesContinuationAndArsMotion(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);
            for(var glyph:List.of(GlyphCatalog.LASER_BOLT,GlyphCatalog.MISSILE,GlyphCatalog.HOMING,GlyphCatalog.HOWITZER)) {
                mana.setMana(10000);var marker=new Marker();var enemy=a.target(a.center.add(0,1,8),false);
                var r=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,glyph,marker));r.spellContext.nextPart();
                var carrier=new EntityProjectileSpell(h.getLevel(),r);carrier.setPos(a.center.add(0,1,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
                h.assertTrue(ProjectileDispatch.convert(carrier),"Harbinger payload dispatch");var nativeEntity=harbingerPayloads(h).getFirst();
                close(h,nativeEntity.getDeltaMovement().length(),glyph==GlyphCatalog.HOWITZER?.6:.1,"Native launch speed");
                h.assertTrue(nativeEntity.getOwner()==a.p && !nativeEntity.shouldBeSaved(),"Root ownership and transient lifecycle");
                int fee=Resources.quote(r.spellContext,glyph.ability,List.of()).amount();close(h,mana.getCurrentMana(),10000-fee,"Single launch payment");
                ((ProjectileHitAccess)nativeEntity).confluxCataclysm$nativeHit(block(a.center.add(0,1,3)));
                h.assertTrue(marker.hits==1 && nativeEntity.isRemoved(),"Single native hit and continuation");
                close(h,mana.getCurrentMana(),10000-fee,"No double impact charge");ExecutionSessions.tick();enemy.discard();
                var orbitResolver=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,glyph,marker));orbitResolver.spellContext.nextPart();
                var orbit=new EntityOrbitProjectile(h.getLevel(),orbitResolver,a.p);orbit.setPos(a.center.add(0,2,0));
                h.assertTrue(ProjectileDispatch.convert(orbit),"Ars orbit dispatch");var orbitVisual=harbingerPayloads(h).getFirst();var pos=orbitVisual.position();var velocity=orbitVisual.getDeltaMovement();
                for(int tick=0;tick<90;tick++) {++orbitVisual.tickCount;orbitVisual.tick();}
                h.assertTrue(!orbitVisual.isRemoved(),"Native homing fuse or missing target cannot kill an Ars-driven carrier");
                close(h,orbitVisual.position().distanceTo(pos),0,"Decorative server tick adds no displacement");close(h,orbitVisual.getDeltaMovement().distanceTo(velocity),0,"Decorative server tick adds no acceleration");
                int before=(int)mana.getCurrentMana();ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center.add(0,1,3)));
                close(h,mana.getCurrentMana(),before-fee,"Repeated impact pays once");h.assertTrue(!orbitVisual.isRemoved(),"Repeated native body retains carrier");
                mana.setMana(0);ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(),block(a.center.add(0,1,3)));
                h.assertTrue(orbitVisual.isRemoved() && orbit.isRemoved(),"Empty mana ends repeated payload");ExecutionSessions.tick();
            }
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_harbinger_native",timeoutTicks=100)
    public static void harbingerNativeDamageKillHealingAndIsolation(GameTestHelper h) {
        try(var a=new Arena(h)) {
            for(var ability:List.of(Ability.LASER_BOLT,Ability.WITHER_MISSILE,Ability.HOMING_MISSILE,Ability.WITHER_HOWITZER)) {
                var enemy=a.target(a.center.add(0,1,2),false);var ally=a.target(a.center.add(0,1,2),true);
                var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.DEATH_LASER));var ctx=CastContext.of(r.spellContext,new EntityHitResult(enemy),null);
                var payload=HarbingerCasting.create(ability,h.getLevel(),ctx,new Vec3(0,0,1),enemy,1);payload.setPos(enemy.position());((ManagedProjectile)payload).confluxCataclysm$mode(2);
                ((ProjectileHitAccess)payload).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
                double base=ability==Ability.LASER_BOLT?com.github.L_Ender.cataclysm.config.CMCommonConfig.Harbinger.Laserdamage:ability==Ability.WITHER_HOWITZER?11:com.github.L_Ender.cataclysm.config.CMCommonConfig.Harbinger.WitherMissiledamage;
                h.assertTrue(500-enemy.getHealth()>=Resources.augmentedDamage(base,1),"Native hit plus Harm amplification");close(h,ally.getHealth(),500,"Explosion spares allies");
                if(ability==Ability.LASER_BOLT)h.assertTrue(enemy.isOnFire(),"Laser ignition retained");
                if(ability==Ability.WITHER_HOWITZER)h.assertTrue(enemy.hasEffect(MobEffects.WITHER),"Shell withering retained");
                enemy.discard();ally.discard();
            }
            var weak=a.target(a.center.add(0,1,6),false);weak.setHealth(1);a.p.setHealth(2);
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.DEATH_LASER));var ctx=CastContext.of(r.spellContext,new EntityHitResult(weak),null);
            var missile=HarbingerCasting.create(Ability.WITHER_MISSILE,h.getLevel(),ctx,new Vec3(0,0,1),weak,0);missile.setPos(weak.position());((ManagedProjectile)missile).confluxCataclysm$mode(2);
            ((ProjectileHitAccess)missile).confluxCataclysm$nativeHit(new EntityHitResult(weak));close(h,a.p.getHealth(),12,"Native missile lethal direct hit heals ordinary owner twice");
            var ordinary=new Laser_Beam_Entity(a.p,new Vec3(0,0,1),h.getLevel(),5);var enemy=a.target(a.center.add(0,1,10),false);
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(100);
            ((ProjectileHitAccess)ordinary).confluxCataclysm$nativeHit(new EntityHitResult(enemy));close(h,500-enemy.getHealth(),5,"Unmanaged native laser ignores Ars bonus");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_harbinger_beam",timeoutTicks=100)
    public static void deathLaserHalfHealthHpCapAndPerSecondPayment(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var enemy=a.target(a.center.add(0,1,5),false);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);enemy.setHealth(1000);
            var ally=a.target(a.center.add(0,1,5),true);var mana=CapabilityRegistry.getMana(a.p);a.p.setYRot(0);a.p.setXRot(0);a.p.setHealth(a.p.getMaxHealth()/2);
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.DEATH_LASER));var ctx=CastContext.of(r.spellContext,block(a.center.add(0,1,0)),null);
            var charge=Resources.quote(r.spellContext,Ability.DEATH_LASER,List.of());h.assertTrue(HarbingerLaser.start(h.getLevel(),ctx,0,0,charge),"Beam starts");
            var beams=h.getLevel().getEntitiesOfClass(Death_Laser_Beam_Entity.class,new AABB(a.center,a.center).inflate(24));h.assertTrue(beams.size()==1,"Native registered beam");var beam=beams.getFirst();
            h.assertTrue(beam.getFire() && beam.caster==a.p && !beam.shouldBeSaved(),"Native half-health branch uses root caster, transient beam");
            close(h,beam.getHpDamage(),com.github.L_Ender.cataclysm.config.CMCommonConfig.Harbinger.DeathLaserHpdamage,"Native hp coefficient retained");beam.setHpDamage(100);
            for(int tick=0;tick<20;tick++)ExecutionSessions.tick();close(h,mana.getCurrentMana(),10000,"Windup does not spend active-second mana");
            ExecutionSessions.tick();close(h,mana.getCurrentMana(),10000-charge.amount(),"First output second pays before damage");
            close(h,1000-enemy.getHealth(),10,"Percentage damage capped at another base hit: 10 total");close(h,ally.getHealth(),500,"Beam excludes allies");h.assertTrue(enemy.isOnFire(),"Powered beam ignites victim");
            float health=enemy.getHealth();mana.setMana(0);for(int tick=0;tick<20;tick++)ExecutionSessions.tick();
            h.assertTrue(beam.isRemoved(),"No mana removes beam before unpaid second");close(h,enemy.getHealth(),health,"No unpaid or fade-tail damage");
            a.p.setGameMode(GameType.CREATIVE);var free=Resources.quote(r.spellContext,Ability.DEATH_LASER,List.of());
            var host=a.target(a.center.add(3,1,0),true);host.setYRot(90);host.setXRot(-20);
            h.assertTrue(HarbingerLaser.start(h.getLevel(),ctx.withTarget(new EntityHitResult(host)),0,0,free),"Creative beams bypass empty mana");
            var follow=h.getLevel().getEntitiesOfClass(Death_Laser_Beam_Entity.class,new AABB(a.center,a.center).inflate(24)).getFirst();
            ExecutionSessions.tick();close(h,follow.position().distanceTo(host.getEyePosition()),0,"Selected executor supplies beam origin");close(h,follow.renderYaw,follow.getYaw(),"Native renderer and server share managed yaw");
            for(int tick=0;tick<90;tick++)ExecutionSessions.tick();h.assertTrue(follow.isRemoved(),"Native duration and fade finish creative beam");close(h,mana.getCurrentMana(),0,"Creative active seconds remain free");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_harbinger_smoke",timeoutTicks=100)
    public static void howitzerSmokeContextDamageAndCasterCleanup(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var enemy=a.target(a.center.add(0,0,2),false);var ally=a.target(enemy.position(),true);
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.HOWITZER));var ctx=CastContext.of(r.spellContext,block(enemy.position()),null);
            var shell=HarbingerCasting.create(Ability.WITHER_HOWITZER,h.getLevel(),ctx,new Vec3(0,0,1),enemy,1);shell.setPos(enemy.position());((ManagedProjectile)shell).confluxCataclysm$mode(2);
            ((ProjectileHitAccess)shell).confluxCataclysm$nativeHit(block(enemy.position()));
            var clouds=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity.class,new AABB(a.center,a.center).inflate(10));
            h.assertTrue(clouds.size()==1,"One native smoke child");var cloud=clouds.getFirst();h.assertTrue(NativeSpellDamage.context(cloud).caster()==a.p && !cloud.shouldBeSaved(),"Child inherits caster context and transient lifecycle");
            h.assertTrue(cloud.noCulling && ((HarbingerSmoke)cloud).confluxHarbinger$managedSmoke(),"Particle-only managed cloud must retain native client ticks");
            var replica=new com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity(h.getLevel(),cloud.getX(),cloud.getY(),cloud.getZ());
            h.assertTrue(!replica.noCulling && !((HarbingerSmoke)replica).confluxHarbinger$managedSmoke(),"Ordinary native cloud retains its original culling behavior");
            var packet=new net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket(cloud.getId(),cloud.getEntityData().packDirty());
            replica.getEntityData().assignValues(packet.packedItems());
            h.assertTrue(replica.noCulling && ((HarbingerSmoke)replica).confluxHarbinger$managedSmoke(),"Spawn metadata installs the particle-tick protection on the receiving entity");
            enemy.setHealth(500);enemy.invulnerableTime=0;for(int tick=0;tick<12;tick++){++cloud.tickCount;cloud.tick();}
            close(h,500-enemy.getHealth(),Resources.augmentedDamage(3,1),"Smoke inherits Harm amplification");close(h,ally.getHealth(),500,"Smoke spares allies");
            h.assertTrue(enemy.hasEffect(MobEffects.WITHER),"Native smoke debuff retained");a.p.setHealth(0);cloud.tick();h.assertTrue(cloud.isRemoved(),"Root death cleans up cloud");h.succeed();
        }
    }

    @GameTest(template="empty",batch="cat_harbinger_smoke_packets",timeoutTicks=100)
    public static void managedSmokeSendsParticlePacketsWithoutClientEntityTicks(GameTestHelper h) {
        var packets=new ArrayList<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket>();
        var p=player(h,packet->{if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket smoke && smoke.getParticle()==net.minecraft.core.particles.ParticleTypes.SMOKE)packets.add(smoke);});
        try {
            for(boolean shellChild:List.of(true,false)) {
                var point=scene(h);packets.clear();
                if(shellChild) {
                    var r=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.HOWITZER));
                    var shell=HarbingerCasting.create(Ability.WITHER_HOWITZER,h.getLevel(),CastContext.of(r.spellContext,block(point),null),new Vec3(0,0,1),null,0);
                    shell.setPos(point);((ManagedProjectile)shell).confluxCataclysm$mode(2);
                    ((ProjectileHitAccess)shell).confluxCataclysm$nativeHit(block(point));
                } else resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.SMOKE)).onResolveEffect(h.getLevel(),block(point));
                var clouds=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity.class,new AABB(point,point).inflate(4));
                h.assertTrue(clouds.size()==1,"Both entry points create one native cloud");var cloud=clouds.getFirst();
                packets.clear();
                for(int tick=1;tick<=6;tick++){cloud.tickCount=tick;cloud.tick();}
                h.assertTrue(packets.size()==1,"Native server tick sends one visual batch after the waiting period");
                var first=packets.getFirst();h.assertTrue(first.getCount()==20 && !first.isOverrideLimiter(),"Bounded batch uses vanilla smoke and respects client particle settings");
                close(h,first.getY(),cloud.getY(),"Particle origin stays at the native cloud height");
                close(h,first.getXDist(),cloud.getRadius()*.45,"Visual spread follows the native radius");
                cloud.tickCount=7;cloud.tick();h.assertTrue(packets.size()==1,"No duplicate packet on intervening ticks");
                cloud.tickCount=8;cloud.tick();h.assertTrue(packets.size()==2 && packets.getLast().getXDist()<first.getXDist(),"Subsequent particle batches shrink with the cloud");
                cloud.discard();cloud.tickCount=10;HarbingerSmokeParticles.send(cloud);h.assertTrue(packets.size()==2,"Removed clouds cannot emit ghost particles");ExecutionSessions.tick();
            }
            packets.clear();
            var ordinary=new com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity(h.getLevel(),scene(h).x,scene(h).y,scene(h).z);
            ordinary.tickCount=24;ordinary.tick();h.assertTrue(packets.isEmpty(),"Ordinary native smoke is not given a second particle source");
            h.succeed();
        } finally {clean(h,p);}
    }

    @GameTest(template="empty",batch="cat_bounded_augments",timeoutTicks=100)
    public static void boundedDamageAugmentsUseNativeBaseAndPrecedeGear(GameTestHelper h) {
        close(h,Resources.augmentedDamage(5,1),7,"Low damage has a two-point floor");
        close(h,Resources.augmentedDamage(20,1),24,"Midrange hit scales by twenty percent");
        close(h,Resources.augmentedDamage(100,1),110,"High damage has a ten-point ceiling");
        close(h,Resources.augmentedDamage(20,3),32,"Multiple augments use the same original base");
        close(h,Resources.augmentedDamage(20,-2),12,"Dampen uses the same bounded step");
        close(h,Resources.augmentedDamage(5,-100),0,"Dampen cannot create negative hurt");
        try(var a=new Arena(h)) {
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(6);
            var victim=a.target(a.center.add(0,0,1),false);var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.WAVE));
            var ctx=CastContext.of(r.spellContext,new EntityHitResult(victim),null);
            var wave=NativeDamageContexts.bind(new Wave_Entity(h.getLevel(),a.p,80,100),ctx,2);((ManagedProjectile)wave).confluxCataclysm$mode(2);
            wave.setPos(a.center);wave.setState(1);h.getLevel().addFreshEntity(wave);wave.tick();
            close(h,500-victim.getHealth(),126,"Two bounded augments then a six-point Ars attribute, exactly once");
            victim.invulnerableTime=0;victim.setHealth(500);wave.discard();
            var weak=NativeDamageContexts.bind(new Wave_Entity(h.getLevel(),a.p,80,100),ctx,-2);((ManagedProjectile)weak).confluxCataclysm$mode(2);
            weak.setPos(a.center);weak.setState(1);h.getLevel().addFreshEntity(weak);weak.tick();
            close(h,500-victim.getHealth(),86,"Dampen reduces before external attributes");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_harbinger_howvol",timeoutTicks=100)
    public static void howitzerVolleysRetainTwoWavesHalfHealthAndChildOwnership(GameTestHelper h) {
        var a=new Arena(h);a.p.setPos(a.center);a.p.setNoGravity(true);
        var target=a.target(a.center.add(0,0,16),false);var spawned=new ArrayList<Wither_Howitzer_Entity>();var times=new ArrayList<Long>();var emitterYs=new ArrayList<Double>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=e->{if(e.getEntity() instanceof Wither_Howitzer_Entity shell && shell.getOwner()==a.p){spawned.add(shell);times.add(h.getLevel().getGameTime());emitterYs.add(shell.getY());}};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);a.cast(GlyphCatalog.HOWITZER_VOLLEY,new EntityHitResult(target),true);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-CataclysmConfig.cost(Ability.HOWITZER_VOLLEY)-CataclysmConfig.amplifyCost(),"One prepaid action, not per-child payload charges");
        h.runAfterDelay(25,()->{try {
            h.assertTrue(spawned.size()==10 && times.get(5)-times.get(0)==6,"Two native waves of five, six ticks apart");
            h.assertTrue(spawned.stream().allMatch(e->!e.shouldBeSaved() && NativeDamageContexts.amplification(e)==1 && NativeSpellDamage.context(e).caster()==a.p),"Native shells retain amplification, root ownership and independent lifetimes");
            close(h,emitterYs.getFirst(),a.p.getEyeY(),"Detached Boss barrel is placed at the caster eye height");
            spawned.forEach(Entity::discard);spawned.clear();times.clear();emitterYs.clear();a.p.setHealth(a.p.getMaxHealth()/2);
            a.cast(GlyphCatalog.HOWITZER_VOLLEY,new EntityHitResult(target),false);
        }catch(RuntimeException|Error error){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);a.close();throw error;}});
        h.runAfterDelay(51,()->{try{h.assertTrue(spawned.size()==14,"Half-health native waves each contain seven shells");h.succeed();}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);a.close();}});
    }
    @GameTest(template="empty",batch="cat_harbinger_missalvo",timeoutTicks=230)
    public static void missileSalvosRetainNativeTimingsAndHalfHealthPairs(GameTestHelper h) {
        var a=new Arena(h);a.p.setPos(a.center);a.p.setNoGravity(true);var target=a.target(a.center.add(0,0,16),false);
        var missiles=new ArrayList<Wither_Homing_Missile_Entity>();var times=new ArrayList<Long>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=e->{if(e.getEntity() instanceof Wither_Homing_Missile_Entity m && m.getOwner()==a.p){missiles.add(m);times.add(h.getLevel().getGameTime());}};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);a.cast(GlyphCatalog.MISSILE_SALVO,new EntityHitResult(target),true);
        close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-CataclysmConfig.cost(Ability.MISSILE_SALVO)-CataclysmConfig.amplifyCost(),"Salvo pays once before its native launch sequence");
        h.runAfterDelay(113,()->{try {
            h.assertTrue(missiles.size()==6 && times.get(1)-times.get(0)==4 && times.get(3)-times.get(0)==18,"Normal native side launch groups");
            h.assertTrue(missiles.stream().allMatch(e->!e.shouldBeSaved() && NativeSpellDamage.context(e).caster()==a.p && NativeDamageContexts.amplification(e)==1),"Salvo retains native payloads and augmented context");
            missiles.forEach(Entity::discard);missiles.clear();times.clear();a.p.setHealth(a.p.getMaxHealth()/2);
            var ally=a.target(a.center,true);ally.setYRot(0);ally.setXRot(0);a.cast(GlyphCatalog.MISSILE_SALVO,new EntityHitResult(ally),false);
        }catch(RuntimeException|Error error){net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);a.close();throw error;}});
        h.runAfterDelay(199,()->{try{h.assertTrue(missiles.size()==6 && times.get(0).equals(times.get(1)) && times.get(2)-times.get(0)==4 && times.get(4)-times.get(0)==8,"Half-health root selects the compact three paired launches from an allied emitter");h.succeed();}finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);a.close();}});
    }
    @GameTest(template="empty",batch="cat_harbinger_charge",timeoutTicks=100)
    public static void harbingerChargeMovesAllyHitsEnemyAndLeavesTerrain(GameTestHelper h) {
        var a=new Arena(h);var actor=a.target(a.center,true);actor.setYRot(0);actor.setXRot(0);actor.setOnGround(true);actor.setNoGravity(false);((Mob)actor).setNoAi(false);actor.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0);
        var victim=a.target(a.center.add(0,0,1.5),false);var original=actor.position();var stone=BlockPos.containing(a.center.add(1,0,0));h.getLevel().setBlockAndUpdate(stone,Blocks.STONE.defaultBlockState());
        int[] hits={0};java.util.function.Consumer<com.hollingsworth.arsnouveau.api.event.SpellDamageEvent.Post> post=e->{if(e.caster==a.p && e.target==victim)hits[0]++;};
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(post);a.cast(GlyphCatalog.HARBINGER_CHARGE,new EntityHitResult(actor),true);
        h.assertTrue(HarbingerActions.isCharging(actor),"Allied actor reserves its movement independently");
        h.runAfterDelay(58,()->{try{
            h.assertTrue(actor.position().distanceToSqr(original)>4,"Actual actor moves with native charge impulse");h.assertTrue(hits[0]>0 && victim.getHealth()<500,"Native collision damage belongs to root caster and reaches Ars events");
            close(h,actor.getHealth(),500,"Native collision ignores executor ally");h.assertTrue(h.getLevel().getBlockState(stone).is(Blocks.STONE),"Charge invocation never runs native terrain griefing");
            h.assertTrue(!HarbingerActions.isCharging(actor),"Native action ending releases actor movement");
            a.p.setHealth(a.p.getMaxHealth()/2);var r=resolver(h,a.p,new Spell(MethodSelf.INSTANCE,GlyphCatalog.HARBINGER_CHARGE));var ctx=CastContext.of(r.spellContext,new EntityHitResult(actor),null).withExecutor(actor);
            var frame=new SpellHarbinger(h.getLevel(),ctx,Ability.HARBINGER_CHARGE,actor.position(),0,0);frame.setTarget(victim);frame.setOverload(1);frame.acceptingRepeat=true;
            long seed=0;while(net.minecraft.util.RandomSource.create(seed).nextInt(2)!=0)seed++;frame.getRandom().setSeed(seed);
            frame.nativeGoal("ChargeGoal").stop();h.assertTrue(frame.isPowered() && frame.repeatRequested,"Native half-health repeat chance uses root health and is captured without broadcasting a detached Boss");h.succeed();
        }finally{net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(post);a.close();}});
    }
    @GameTest(template="empty",batch="cat_guardian_bullet",timeoutTicks=100)
    public static void guardianBulletPreparationDamageContinuationAndOrdinaryIsolation(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var marker=new Marker();var r=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GUARDIAN_BULLET,AugmentAmplify.INSTANCE,marker));r.spellContext.nextPart();
            var carrier=new EntityProjectileSpell(h.getLevel(),r);carrier.setPos(a.center.add(0,2,0));carrier.setDeltaMovement(0,0,.75);
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();ProjectileDispatch.convert(carrier);
            var bullet=(Ender_Guardian_Bullet_Entity)harbingerPayloads(h).getFirst();var state=(ManagedProjectile)bullet;
            h.assertTrue(state.confluxCataclysm$mode()==2 && !bullet.shouldBeSaved(),"Normal Guardian bullet uses transient native flight");
            close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.GUARDIAN_BULLET)+CataclysmConfig.amplifyCost(),"Bullet pays once at launch");
            var enemy=a.target(a.center.add(0,2,15),false);
            ((ProjectileHitAccess)bullet).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            h.assertTrue(!bullet.isRemoved() && marker.hits==0 && enemy.getHealth()==500,"Preparation phase cannot deal damage or fire the continuation");
            for(int tick=0;tick<CataclysmConfig.guardianBulletDelay();tick++)bullet.tick();
            h.assertTrue(((GuardianBullet)bullet).confluxGuardian$fired() && bullet.position().distanceTo(carrier.position())>2,"Native bullet moves to its staging point before firing");
            ((ProjectileHitAccess)bullet).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
            close(h,500-enemy.getHealth(),Resources.augmentedDamage(6,1),"Native six-point hit receives bounded amplification");
            h.assertTrue(enemy.hasEffect(MobEffects.LEVITATION) && enemy.getEffect(MobEffects.LEVITATION).getDuration()==100,"Native five-second levitation retained");
            h.assertTrue(marker.hits==1 && bullet.isRemoved() && enemy.getLastDamageSource().getEntity()==a.p,"Native hit, true owner, and Ars continuation each run once");
            close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.GUARDIAN_BULLET)+CataclysmConfig.amplifyCost(),"Impact does not pay twice");
            var ordinary=new Ender_Guardian_Bullet_Entity(h.getLevel(),a.p,new Vec3(0,0,1));ordinary.setPos(a.center.add(0,2,0));ordinary.setUp(1,0,0,1,ordinary.getX(),ordinary.getY(),ordinary.getZ());ordinary.tick();enemy.invulnerableTime=0;enemy.setHealth(500);
            a.p.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(100);
            ((ProjectileHitAccess)ordinary).confluxCataclysm$nativeHit(new EntityHitResult(enemy));close(h,500-enemy.getHealth(),6,"Unmanaged native bullets do not gain Ars damage");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_orbit",timeoutTicks=100)
    public static void guardianBulletOrbitHasOneMotionDriverAndRepeatedBilling(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var marker=new Marker();var r=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GUARDIAN_BULLET,marker));r.spellContext.nextPart();
            var carrier=new EntityOrbitProjectile(h.getLevel(),r,a.p);carrier.setPos(a.center.add(0,2,0));ProjectileDispatch.convert(carrier);
            var bullet=(Ender_Guardian_Bullet_Entity)harbingerPayloads(h).getFirst();var state=(ManagedProjectile)bullet;var before=bullet.position();
            for(int tick=0;tick<80;tick++)bullet.tick();
            close(h,bullet.position().distanceTo(before),0,"Native preparation and acceleration cannot move the Ars-driven carrier");
            h.assertTrue(!bullet.isRemoved() && ((GuardianBullet)bullet).confluxGuardian$fired(),"Native firing timer cannot expire an orbiting carrier");
            var enemy=a.target(a.center.add(0,2,10),false);var ally=a.target(enemy.position(),true);var mana=CapabilityRegistry.getMana(a.p);double funds=mana.getCurrentMana();
            state.confluxCataclysm$impact().accept(new EntityHitResult(ally));close(h,mana.getCurrentMana(),funds,"Allied repeated contact is free and ignored");
            state.confluxCataclysm$impact().accept(new EntityHitResult(enemy));enemy.invulnerableTime=0;
            state.confluxCataclysm$impact().accept(new EntityHitResult(enemy));
            h.assertTrue(marker.hits==2 && !bullet.isRemoved(),"Repeated impact resumes twice without destroying the carrier");
            close(h,funds-mana.getCurrentMana(),2*CataclysmConfig.cost(Ability.GUARDIAN_BULLET),"Each repeated impact pays once");
            mana.setMana(0);state.confluxCataclysm$impact().accept(new EntityHitResult(enemy));h.assertTrue(bullet.isRemoved() && marker.hits==2,"Empty mana stops later repeated impacts");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_vortex",timeoutTicks=100)
    public static void guardianVortexNativePullCollapseAlliesAndRootCleanup(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.cast(GlyphCatalog.VORTEX,block(a.center),true);
            var clouds=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity.class,new AABB(a.center,a.center).inflate(4));
            h.assertTrue(clouds.size()==1,"One native vortex");var vortex=clouds.getFirst();
            h.assertTrue(!vortex.shouldBeSaved() && ((GuardianChild)vortex).confluxGuardian$managed(),"Vortex is opt-in and transient");
            h.assertTrue(vortex.noCulling,"Managed vortex retains renderer and particle ticks under entity culling");
            close(h,vortex.getBoundingBoxForCulling().getXsize(),6,"Culling extent covers the native six-block plane");
            close(h,vortex.getBoundingBox().getXsize(),2.5,"Visual extent does not expand the physical box");
            var replica=new com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity(com.github.L_Ender.cataclysm.init.ModEntities.VOID_VORTEX.get(),h.getLevel());
            h.assertTrue(!replica.noCulling && !((GuardianChild)replica).confluxGuardian$managed(),"Ordinary native vortex keeps its original culling rules");
            replica.getEntityData().assignValues(vortex.getEntityData().getNonDefaultValues());
            h.assertTrue(replica.noCulling && ((GuardianChild)replica).confluxGuardian$managed(),"Receiving spawn metadata installs culling protection");
            close(h,replica.getLifespan(),vortex.getLifespan(),"Spawn metadata preserves the native lifespan");
            ((GuardianChild)replica).confluxGuardian$managed(false);h.assertTrue(!replica.noCulling,"Clearing the marker restores native culling behavior");
            var enemy=a.target(a.center.add(1,2,0),false);var ally=a.target(enemy.position(),true);
            vortex.tickCount=16;vortex.tick();h.assertTrue(enemy.getDeltaMovement().y< -1.9 && enemy.hurtMarked,"Native downward pull is synchronized from server");close(h,ally.getDeltaMovement().length(),0,"Core allies are not pulled");
            close(h,enemy.getHealth(),500,"Pull is not a fabricated periodic damage field");enemy.setPos(a.center.add(.5,0,0));ally.setPos(enemy.position());vortex.setLifespan(1);vortex.tick();
            h.assertTrue(vortex.isRemoved() && enemy.getHealth()<500 && enemy.getLastDamageSource().getEntity()==a.p,"Native collapse explodes with the real damage owner");close(h,ally.getHealth(),500,"Collapse spares allied targets");h.assertTrue(h.getLevel().getBlockState(a.base).is(Blocks.STONE),"Collapse does not destroy terrain");
            a.cast(GlyphCatalog.VORTEX,block(a.center),false);var next=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity.class,new AABB(a.center,a.center).inflate(4)).getFirst();a.p.setHealth(0);next.tick();h.assertTrue(next.isRemoved(),"Root death discards the vortex without a collapse attack");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_stomp",timeoutTicks=100)
    public static void guardianStompNativePatternHalfHealthAndChildContext(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();a.cast(GlyphCatalog.GUARDIAN_STOMP,block(a.center),true);
            for(int tick=0;tick<32;tick++)ExecutionSessions.tick();
            var area=new AABB(a.center,a.center).inflate(20);var runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area);
            h.assertTrue(Set.of(44,57,70).contains(runes.size()),"Healthy stomp uses 31 ring runes plus 13/26/39 frontal runes");
            for(var rune:runes){h.assertTrue(rune.getCaster()==a.p && !rune.shouldBeSaved(),"Native rune has true owner and transient lifecycle");close(h,rune.getDamage(),com.github.L_Ender.cataclysm.config.CMCommonConfig.EnderGuardian.VoidRuneDamage,"Guardian-specific native rune damage stays unaugmented");close(h,NativeDamageContexts.amplification(rune),1,"Children inherit augmentation once");}
            close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.GUARDIAN_STOMP)+CataclysmConfig.amplifyCost(),"One payment for the entire array");runes.forEach(Entity::discard);ExecutionSessions.tick();
            a.p.setHealth(5);a.cast(GlyphCatalog.GUARDIAN_STOMP,block(a.center),false);for(int tick=0;tick<63;tick++)ExecutionSessions.tick();
            runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,area);h.assertTrue(runes.size()>=132 && runes.size()<=210,"Half-health cast invokes three complete native arrays");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(runes.stream().allMatch(Entity::isRemoved),"Root removal cleans up every rune in all waves");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_punch",timeoutTicks=100)
    public static void gravityPunchAlliedExecutorNativeDamageAndCancellation(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var host=a.target(a.center,true);host.setYRot(0);var enemy=a.target(a.center.add(0,0,3),false);var ally=a.target(enemy.position(),true);
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();a.cast(GlyphCatalog.GRAVITY_PUNCH,new EntityHitResult(host),true);
            ExecutionSessions.tick();h.assertTrue(GuardianCasting.isPunching(host) && enemy.getDeltaMovement().z<0,"Ally performs native preparation, pulling the enemy toward its front");close(h,ally.getDeltaMovement().length(),0,"Friendly entities are excluded from pull");
            for(int tick=1;tick<29;tick++)ExecutionSessions.tick();
            double attack=16*com.github.L_Ender.cataclysm.config.CMCommonConfig.EnderGuardian.attackMultiplier;double raw=attack+Math.min(attack,500*com.github.L_Ender.cataclysm.config.CMCommonConfig.EnderGuardian.GravityPunchHpdamage);
            close(h,500-enemy.getHealth(),Resources.augmentedDamage(raw,1),"Native punch preserves capped health damage before Ars enhancement");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Executor does not replace the root damage owner");close(h,ally.getHealth(),500,"Friend is not punched");close(h,host.getHealth(),500,"Executor is not punched");close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.GRAVITY_PUNCH)+CataclysmConfig.amplifyCost(),"Root pays the entire action once");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(!GuardianCasting.isPunching(host),"Cancellation releases allied movement control");h.succeed();
        }
    }

    @GameTest(template="empty",batch="cat_guardian_uppercut",timeoutTicks=100)
    public static void guardianUppercutNativeComboChildrenHalfHealthAndCleanup(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.setHealth(5);
            var host=a.target(a.center,true);host.setYRot(0);var enemy=a.target(a.center.add(0,0,3),false);var ally=a.target(enemy.position(),true);
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();
            a.cast(GlyphCatalog.GUARDIAN_UPPERCUT,new EntityHitResult(host),true);
            h.assertTrue(GuardianActions.isActing(host),"Allied actor owns movement control");
            double paid=mana.getCurrentMana();a.cast(GlyphCatalog.GUARDIAN_ROCKET_PUNCH,new EntityHitResult(host),false);
            close(h,mana.getCurrentMana(),paid,"A second conflicting action is refunded");
            for(int tick=1;tick<=54;tick++) {
                enemy.invulnerableTime=0;ExecutionSessions.tick();
                if(tick==26)h.assertTrue(host.getDeltaMovement().z>.8,"Native uppercut lunge reaches actual actor");
                if(tick==27)h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(ModEffect.EFFECTSTUN) && enemy.getDeltaMovement().y>=.5,"Uppercut retains damage, stun and launch");
            }
            var box=new AABB(a.center,a.center).inflate(20);
            var bullets=h.getLevel().getEntitiesOfClass(Ender_Guardian_Bullet_Entity.class,box);
            h.assertTrue(bullets.size()==9,"Full native nine-bullet preparation pattern");
            for(var bullet:bullets) {
                h.assertTrue(bullet.getOwner()==a.p && ((ManagedProjectile)bullet).confluxCataclysm$sourceAbility()==Ability.GUARDIAN_UPPERCUT,"Combo children retain root ownership and source ability");
                close(h,NativeDamageContexts.amplification(bullet),1,"Child amplification snapshot applied once");
            }
            double afterFirst=enemy.getHealth();
            for(int tick=55;tick<=84;tick++){enemy.invulnerableTime=0;ExecutionSessions.tick();}
            var runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,box);
            h.assertTrue(runes.size()==70 && enemy.getHealth()<afterFirst,"Half-health combo adds complete 70-rune fan and second native hit");
            close(h,ally.getHealth(),500,"Combo protects nearby allies");close(h,host.getHealth(),500,"Combo protects executor");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Root caster owns native melee");
            close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.GUARDIAN_UPPERCUT)+CataclysmConfig.amplifyCost(),"Entire combo pays once, including children");
            a.p.setHealth(0);ExecutionSessions.tick();
            h.assertTrue(!GuardianActions.isActing(host) && bullets.stream().allMatch(Entity::isRemoved) && runes.stream().allMatch(Entity::isRemoved),"Root loss clears action and delayed children");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_blink",timeoutTicks=100)
    public static void guardianBlinkMovesRealActorAndUsesNativeHit(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var host=a.target(a.center,true);host.setYRot(0);var enemy=a.target(a.center.add(0,0,7),false);
            var before=host.position();a.cast(GlyphCatalog.GUARDIAN_BLINK,new EntityHitResult(host),true);
            for(int tick=1;tick<=20;tick++)ExecutionSessions.tick();
            h.assertTrue(host.position().distanceTo(before)>4 && h.getLevel().noCollision(host),"Native predicted teleport moves real executor to a clear grounded spot");
            enemy.setPos(host.position().add(0,0,2));
            for(int tick=21;tick<=38;tick++)ExecutionSessions.tick();
            h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(ModEffect.EFFECTSTUN) && enemy.getDeltaMovement().y>=.6,"Blink completes native stun/lift strike");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Blink damage source remains root caster");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(!GuardianActions.isActing(host),"Blink cancellation clears control");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_air_strike",timeoutTicks=100)
    public static void guardianAirStrikeWaitsForLandingThenNativeShockwaveAndRunes(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.setHealth(5);var host=a.target(a.center,true);host.setYRot(0);host.setOnGround(true);
            var enemy=a.target(a.center.add(0,0,7),false);var ally=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.GUARDIAN_AIR_STRIKE,new EntityHitResult(host),true);
            h.assertTrue(GuardianActions.isActing(host) && !host.isNoGravity(),"Air strike temporarily enables real fall physics");
            for(int tick=1;tick<=60;tick++)ExecutionSessions.tick();
            close(h,host.getY(),enemy.getY()+8,"Half-health native goal chooses eight-block drop");
            var box=new AABB(a.center,a.center).inflate(20);
            h.assertTrue(h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,box).isEmpty() && enemy.getHealth()==500,"Elapsed time alone cannot trigger the landing attack");
            host.setPos(enemy.position());host.setOnGround(true);ExecutionSessions.tick();
            for(int tick=1;tick<=3;tick++)ExecutionSessions.tick();
            var runes=h.getLevel().getEntitiesOfClass(Void_Rune_Entity.class,box);
            h.assertTrue(Set.of(64,80,96).contains(runes.size()),"Native random radial pattern is complete");
            h.assertTrue(enemy.getHealth()<500 && enemy.getLastDamageSource().getEntity()==a.p,"Landing shockwave retains damage and root ownership");
            close(h,ally.getHealth(),500,"Landing protects allies");close(h,host.getHealth(),500,"Landing protects executor");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(host.isNoGravity() && !GuardianActions.isActing(host),"Root loss restores original floating state");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_rocket",timeoutTicks=100)
    public static void guardianRocketPunchNativeImpulseDamageAndInvalidTargetRefund(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var host=a.target(a.center,true);host.setYRot(0);var enemy=a.target(a.center.add(0,0,6),false);var ally=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.GUARDIAN_ROCKET_PUNCH,new EntityHitResult(host),true);
            for(int tick=1;tick<=24;tick++)ExecutionSessions.tick();
            close(h,host.getDeltaMovement().z,6*.3F,"Rocket punch uses native target-distance horizontal impulse");
            for(int tick=25;tick<=28;tick++)ExecutionSessions.tick();
            h.assertTrue(enemy.getHealth()<500 && enemy.getLastDamageSource().getEntity()==a.p && enemy.getDeltaMovement().z>0,"Native rocket punch hits and knocks back enemies");
            close(h,ally.getHealth(),500,"Rocket punch excludes allied targets");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(!GuardianActions.isActing(host),"Rocket cancellation releases control");
            a.p.setHealth(20);a.p.setPos(a.center.add(0,0,-6));a.p.setYRot(180);var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();
            a.cast(GlyphCatalog.GUARDIAN_BLINK,block(a.center),false);close(h,mana.getCurrentMana(),before,"Blink without enemy entity has no charge");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_guardian_teleport_safety",timeoutTicks=100)
    public static void guardianTeleportCancellationAndBlockedAirDestination(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var host=a.target(a.center,true);host.setYRot(0);var enemy=a.target(a.center.add(0,0,7),false);
            var frame=new SpellGuardian(h.getLevel(),CastContext.detached(a.p,new EntityHitResult(host),null).withExecutor(host),host.position(),host.getLookAngle(),0,Ability.GUARDIAN_BLINK);
            java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity> denial=e->{if(e.getEntity()==host)e.setCanceled(true);};
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(denial);
            try {var original=host.position();h.assertTrue(!GuardianActions.teleport(frame,enemy.position(),true) && original.equals(host.position()),"Real actor teleport event veto is honored");}
            finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(denial);frame.discard();}
            var obstruction=BlockPos.containing(enemy.position().add(0,4,0));h.getLevel().setBlockAndUpdate(obstruction,Blocks.STONE.defaultBlockState());
            try {
                double before=CapabilityRegistry.getMana(a.p).getCurrentMana();a.cast(GlyphCatalog.GUARDIAN_AIR_STRIKE,new EntityHitResult(host),false);
                h.assertTrue(!GuardianActions.isActing(host) && host.isNoGravity(),"Blocked air destination does not start or change gravity");
                close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),before,"Blocked destination refunds trigger fee");
            } finally {h.getLevel().setBlockAndUpdate(obstruction,Blocks.AIR.defaultBlockState());}
            h.succeed();
        }
    }

    @GameTest(template="empty",batch="cat_ignis_fireball",timeoutTicks=100)
    public static void ignisFireballNativeDamageBrandHealingExplosionAndAllies(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var victim=a.target(a.center,false);var ally=a.target(a.center.add(.5,0,0),true);
            var cast=CastContext.detached(a.p,new EntityHitResult(victim),null);
            var fire=(Ignis_Fireball_Entity)IgnisCasting.create(Ability.IGNIS_FIREBALL,h.getLevel(),cast,new Vec3(0,0,1),victim,1);
            close(h,fire.zPower,.2,"Normal fireball begins with native launched power");close(h,fire.getDeltaMovement().z,.25,"Native launch speed is retained");
            ((ManagedProjectile)fire).confluxCataclysm$mode(2);fire.setPos(victim.position());a.p.setHealth(5);
            h.getLevel().addFreshEntity(fire);((ProjectileHitAccess)fire).confluxCataclysm$nativeHit(new EntityHitResult(victim));
            close(h,500-victim.getHealth(),Resources.augmentedDamage(6+500*.07F,1),"Native direct health-based damage is augmented once");
            close(h,a.p.getHealth(),10,"Successful native projectile heals real owner by five");
            h.assertTrue(fire.isRemoved() && victim.getLastDamageSource().getEntity()==a.p,"Native explosion and removal preserve owner");
            h.assertTrue(victim.hasEffect(ModEffect.EFFECTBLAZING_BRAND) && victim.getEffect(ModEffect.EFFECTBLAZING_BRAND).getAmplifier()==1,"Native initial brand is level II");
            close(h,ally.getHealth(),500,"Native explosion protects allies");close(h,ally.getDeltaMovement().length(),0,"Friendly explosion knockback is suppressed");
            h.assertTrue(h.getLevel().getBlockState(a.base).is(Blocks.STONE),"Fireball keeps terrain");
            var ordinary=new Ignis_Fireball_Entity(h.getLevel(),a.p);h.assertTrue(((ManagedProjectile)ordinary).confluxCataclysm$mode()==0,"Ordinary fireball has no bridge mode");
            var soul=(Ignis_Fireball_Entity)IgnisCasting.create(Ability.IGNIS_FIREBALL,h.getLevel(),cast,new Vec3(0,0,1),victim,0);
            h.assertTrue(soul.isSoul() && IgnisCasting.phase(a.p)==1,"Native two-thirds health gate selects soul fire");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_ignis_abyss",timeoutTicks=100)
    public static void ignisAbyssNativeBounceLimitAndDirectDamage(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var cast=CastContext.detached(a.p,null,null);var victim=a.target(a.center.add(4,0,0),false);
            var fire=(Ignis_Abyss_Fireball_Entity)IgnisCasting.create(Ability.IGNIS_ABYSS_FIREBALL,h.getLevel(),cast,new Vec3(0,0,1),victim,1);
            ((ManagedProjectile)fire).confluxCataclysm$mode(2);fire.setPos(a.center);fire.setDeltaMovement(0,0,1);
            var wall=a.base.above();h.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());
            try {
                var hit=new BlockHitResult(a.center,Direction.NORTH,wall,false);
                for(int bounce=0;bounce<6;bounce++)((ProjectileHitAccess)fire).confluxCataclysm$nativeHit(hit);
                h.assertTrue(fire.getTotalBounces()==6 && !fire.isRemoved(),"Native abyss wall bounces retained");
                ((ProjectileHitAccess)fire).confluxCataclysm$nativeHit(hit);h.assertTrue(fire.isRemoved(),"Native bounce exhaustion explodes and removes projectile");
            } finally {h.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());}
            var direct=(Ignis_Abyss_Fireball_Entity)IgnisCasting.create(Ability.IGNIS_ABYSS_FIREBALL,h.getLevel(),cast,new Vec3(0,0,1),victim,1);direct.setPos(victim.position());((ManagedProjectile)direct).confluxCataclysm$mode(2);
            victim.setYRot(90);direct.hurt(h.getLevel().damageSources().mobAttack(victim),1);
            h.assertTrue(direct.getDeltaMovement().x<-.9 && direct.getOwner()==a.p,"Native deflection redirects motion while retaining explicit cast ownership");
            ((ProjectileHitAccess)direct).confluxCataclysm$nativeHit(new EntityHitResult(victim));
            close(h,500-victim.getHealth(),Resources.augmentedDamage(10+500*.2F,1),"Native abyss twenty-percent damage uses bounded augmentation");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_ignis_orbit",timeoutTicks=100)
    public static void ignisOrbitKeepsNativeTrailSingleMotionAndRepeatedBilling(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var marker=new Marker();var r=resolver(h,a.p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.IGNIS_FIREBALL,marker));r.spellContext.nextPart();
            var orbit=new EntityOrbitProjectile(h.getLevel(),r,a.p);orbit.setPos(a.center.add(0,3,0));ProjectileDispatch.convert(orbit);
            var fire=h.getLevel().getEntitiesOfClass(Ignis_Fireball_Entity.class,new AABB(a.center,a.center).inflate(10)).getFirst();
            var original=fire.position();fire.tick();h.assertTrue(fire.hasTrail() && fire.position().equals(original),"Native tick fills trail history without moving Ars payload twice");
            for(int tick=0;tick<170;tick++)fire.tick();h.assertTrue(!fire.isRemoved(),"Native expiry cannot end an active Ars special form");
            var enemy=a.target(a.center.add(0,0,2),false);var mana=CapabilityRegistry.getMana(a.p);mana.setMana(10000);var state=(ManagedProjectile)fire;
            for(int i=1;i<=2;i++){enemy.invulnerableTime=0;state.confluxCataclysm$impact().accept(new EntityHitResult(enemy));h.assertTrue(marker.hits==i && !fire.isRemoved(),"Repeated payload continues after native impact");close(h,mana.getCurrentMana(),10000-i*CataclysmConfig.cost(Ability.IGNIS_FIREBALL),"Each recurring hit pays once");}
            mana.setMana(0);state.confluxCataclysm$impact().accept(new EntityHitResult(enemy));h.assertTrue(fire.isRemoved() && orbit.isRemoved() && marker.hits==2,"Empty Mana ends repeated Ignis payload");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_ignis_blades",timeoutTicks=100)
    public static void ignisPreparedBladeShotsTargetAndRootLifetime(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.setHealth(5);var enemy=a.target(a.center.add(0,0,8),false);
            var mana=CapabilityRegistry.getMana(a.p);double before=mana.getCurrentMana();
            a.cast(GlyphCatalog.IGNIS_BLADE_FIREBALLS,new EntityHitResult(enemy),true);
            for(int i=0;i<25;i++)ExecutionSessions.tick();
            var shots=h.getLevel().getEntitiesOfClass(Ignis_Fireball_Entity.class,new AABB(a.p.position(),a.p.position()).inflate(10));
            h.assertTrue(shots.size()==3,"All three native blade placements are emitted");
            for(var shot:shots){
                h.assertTrue(shot.getOwner()==a.p && shot.isSoul() && !shot.getFired(),"Prepared native soul shots have real owner");
                shot.setUp(1);shot.tick();var direction=enemy.position().add(0,enemy.getBbHeight()*.5,0).subtract(shot.position()).normalize();
                h.assertTrue(shot.getFired() && new Vec3(shot.xPower,shot.yPower,shot.zPower).normalize().dot(direction)>.999,"Prepared shot acquires target even when owner is a player");
                close(h,NativeDamageContexts.amplification(shot),1,"Children inherit augmentation once");
            }
            close(h,before-mana.getCurrentMana(),CataclysmConfig.cost(Ability.IGNIS_BLADE_FIREBALLS)+CataclysmConfig.amplifyCost(),"Native salvo pays once");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(shots.stream().allMatch(Entity::isRemoved),"Root death cancels every prepared shot");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_ignis_jump",timeoutTicks=100)
    public static void ignisLeapingSmashWaitsForRealLandingAndRestoresGravity(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var host=a.target(a.center,true);host.setYRot(0);host.setOnGround(true);var enemy=a.target(a.center.add(0,0,2),false);var ally=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.IGNIS_AIR_SMASH,new EntityHitResult(host),true);
            for(int tick=1;tick<=30;tick++){ExecutionSessions.tick();if(tick==19)close(h,host.getDeltaMovement().y,1.3,"Native jump reaches real executor");}
            close(h,enemy.getHealth(),500,"Midair timer does not create a landing attack");
            host.setOnGround(true);ExecutionSessions.tick();for(int tick=1;tick<=5;tick++)ExecutionSessions.tick();
            h.assertTrue(enemy.getHealth()<500 && enemy.getRemainingFireTicks()==0 && enemy.getLastDamageSource().getEntity()==a.p,"Actual landing starts native non-igniting smash with real damage ownership: health="+enemy.getHealth()+", fire="+enemy.getRemainingFireTicks()+", source="+enemy.getLastDamageSource()+", actor="+host.position()+", motion="+host.getDeltaMovement());
            close(h,ally.getHealth(),500,"Smash protects allies");
            a.p.setHealth(0);ExecutionSessions.tick();h.assertTrue(!IgnisCasting.isActing(host) && host.isNoGravity(),"Cancellation restores previous gravity state");h.succeed();
        }
    }
    @GameTest(template="empty",batch="cat_ignis_combo",timeoutTicks=100)
    public static void ignisBothSwordDancePhasesDamageBrandAndHealingOwnership(GameTestHelper h) {
        try(var a=new Arena(h)) {
            a.p.setHealth(5);var host=a.target(a.center,true);host.setYRot(0);host.setHealth(250);var enemy=a.target(a.center.add(0,0,3),false);var ally=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.IGNIS_SWORD_COMBO,new EntityHitResult(host),true);
            for(int tick=1;tick<=83;tick++){enemy.invulnerableTime=0;ExecutionSessions.tick();}
            h.assertTrue(enemy.hasEffect(ModEffect.EFFECTBLAZING_BRAND) && a.p.getHealth()>5,"Native sword hits brand and heal the root caster");
            double afterFirst=enemy.getHealth();
            for(int tick=84;tick<=215;tick++){enemy.invulnerableTime=0;ExecutionSessions.tick();}
            h.assertTrue(enemy.getHealth()<afterFirst && !IgnisCasting.isActing(host),"Second native phase completes and releases actor");
            close(h,host.getHealth(),250,"Heal does not accidentally target the detached Boss or ally executor");close(h,ally.getHealth(),500,"Both phases protect allies");
            h.assertTrue(h.getLevel().getBlockState(a.base).is(Blocks.STONE),"Ground waves retain terrain");h.succeed();
        }
    }

    @GameTest(template="empty",batch="cat_harbinger_standalone_smoke",timeoutTicks=100)
    public static void standaloneSmokeDurationDamageFeeAndRootCleanup(GameTestHelper h) {
        try(var a=new Arena(h)) {
            var r=resolver(h,a.p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.SMOKE,AugmentAmplify.INSTANCE,com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime.INSTANCE));r.spellContext.nextPart();
            r.onResolveEffect(h.getLevel(),block(a.center));var clouds=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity.class,new AABB(a.center,a.center).inflate(4));
            h.assertTrue(clouds.size()==1,"One native independent smoke cloud");var cloud=clouds.getFirst();
            h.assertTrue(cloud.noCulling && cloud.getDuration()==450 && !cloud.shouldBeSaved(),"Native visual ticks, time augment and transient storage");
            close(h,NativeDamageContexts.amplification(cloud),1,"Smoke has its own augmentation snapshot");close(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),10000-CataclysmConfig.cost(Ability.WITHER_SMOKE)-CataclysmConfig.amplifyCost(),"Standalone smoke charges once");
            var victim=a.target(a.center,false);for(int t=0;t<12;t++){++cloud.tickCount;cloud.tick();}close(h,500-victim.getHealth(),5,"Smoke damage gets the minimum bounded increase");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(cloud.isRemoved(),"Root removal ends independent smoke");h.succeed();
        }
    }
}
