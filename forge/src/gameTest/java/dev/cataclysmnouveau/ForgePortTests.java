package dev.cataclysmnouveau;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_forge") @PrefixGameTestTemplate(false)
public class ForgePortTests {
    @GameTest(template="empty",timeoutTicks=100)
    public static void nativeEntitiesAndGlyphsLoad(GameTestHelper h){
        int count=0;
        for(var entry:ForgeRegistries.ENTITY_TYPES.getEntries())if(entry.getKey().location().getNamespace().equals("cataclysm")){
            try{var e=entry.getValue().create(h.getLevel());if(e!=null){e.discard();count++;}}
            catch(Throwable failure){throw new AssertionError("Native constructor: "+entry.getKey().location(),failure);}
        }
        h.assertTrue(count>80,"Native entities exercise their required mixins");
        h.assertTrue(GlyphCatalog.ALL.size()==102,"All shared glyphs registered");
        for(var glyph:GlyphCatalog.ALL)h.assertTrue(ForgeRegistries.ITEMS.containsValue(glyph.getGlyph()),"Glyph item "+glyph.getRegistryName());
        h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void allProjectileReplacementsAndCleanup(GameTestHelper h){
        var p=player(h);var mana=CapabilityRegistry.getMana(p).orElseThrow(()->new AssertionError("Missing Ars mana"));
        try{
            for(var glyph:GlyphCatalog.PROJECTILES)for(boolean orbit:List.of(false,true)){
                mana.setMaxMana(100000);mana.setMana(100000);
                var spell=new Spell(MethodProjectile.INSTANCE,glyph);
                var r=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
                r.spellContext.nextPart();
                var carrier=orbit?new com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile(h.getLevel(),r,p):new EntityProjectileSpell(h.getLevel(),r);carrier.setPos(p.position().add(0,2,4));carrier.setDeltaMovement(new Vec3(0,0,.75));
                // Guided missiles require an actual forward target.
                var cow=EntityType.COW.create(h.getLevel());cow.setPos(carrier.position().add(0,0,6));cow.setNoAi(true);cow.setNoGravity(true);h.getLevel().addFreshEntity(cow);
                h.assertTrue(ProjectileDispatch.convert(carrier),"Carrier accepted "+glyph.ability);
                ExecutionSessions.tick();
                for(var entity:h.getLevel().getEntities((Entity)null,carrier.getBoundingBox().inflate(20),e->e instanceof ManagedProjectile))entity.tick();
                p.discard();ExecutionSessions.tick();cow.discard();
                p=player(h);mana=CapabilityRegistry.getMana(p).orElseThrow(()->new AssertionError("Missing Ars mana"));
            }
            h.succeed();
        }finally{p.discard();ExecutionSessions.tick();}
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void rageTentaclesAndExplosionHooks(GameTestHelper h){
        var p=player(h);
        try{
            h.assertTrue(ForgeNative.tentacles(p)!=null,"Native tentacle capability attached");
            MaledictusRage.gain(p,3);h.assertTrue(MaledictusRage.stacks(p)==3,"Three rage levels");
            var effect=p.getEffect(CataclysmEffects.CURSED_RAGE.get());
            for(int i=0;i<CataclysmConfig.rageDecay();i++)effect.tick(p,()->{});
            h.assertTrue(MaledictusRage.stacks(p)==2,"Hidden-effect decay bridge");
            var enemy=EntityType.COW.create(h.getLevel());enemy.setPos(p.position().add(3,0,0));enemy.setNoGravity(true);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);enemy.setHealth(500);h.getLevel().addFreshEntity(enemy);
            var ally=EntityType.COW.create(h.getLevel());ally.setPos(p.position().add(0,0,3));ally.setNoGravity(true);h.getLevel().addFreshEntity(ally);
            var calculator=new ForgeExplosionCalculator(){
                public boolean shouldDamageEntity(net.minecraft.world.level.Explosion e,Entity target){return target==enemy;}
                public float getEntityDamageAmount(net.minecraft.world.level.Explosion e,Entity target){return 7;}
            };
            h.getLevel().explode(p,null,calculator,p.getX(),p.getY(),p.getZ(),4,false,net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            h.assertTrue(Math.abs(enemy.getHealth()-493)<.01 && ally.getHealth()==ally.getMaxHealth() && ally.getDeltaMovement().lengthSqr()==0,"Opt-in native explosion damage and ally protection");
            enemy.discard();ally.discard();h.succeed();
        }finally{p.discard();ExecutionSessions.tick();}
    }
    @GameTest(template="empty",timeoutTicks=100)
    public static void nativeCursedStormCollisionContinuesAndCreditsCaster(GameTestHelper h){
        var p=player(h);
        try{
            for(boolean orbit:List.of(false,true)){
                var marker=new Marker();
                var r=new SpellResolver(new SpellContext(h.getLevel(),new Spell(MethodProjectile.INSTANCE,GlyphCatalog.CURSED_SANDSTORM,marker),p,LivingCaster.from(p),ItemStack.EMPTY));r.spellContext.nextPart();
                var carrier=orbit?new com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile(h.getLevel(),r,p):new EntityProjectileSpell(h.getLevel(),r);
                carrier.setPos(p.position().add(0,2,4));carrier.setDeltaMovement(new Vec3(0,0,.75));
                var enemy=EntityType.COW.create(h.getLevel());enemy.setPos(carrier.position().add(0,0,6));enemy.setNoAi(true);enemy.setNoGravity(true);enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);enemy.setHealth(500);h.getLevel().addFreshEntity(enemy);
                h.assertTrue(ProjectileDispatch.convert(carrier),"Sandstorm converted");
                var storm=h.getLevel().getEntitiesOfClass(com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity.class,carrier.getBoundingBox().inflate(20)).stream().filter(e->((ManagedProjectile)e).confluxTrajectory()==carrier).findFirst().orElseThrow();
                storm.setState(0);
                if(orbit)((ManagedProjectile)storm).confluxCataclysm$impact().accept(new EntityHitResult(enemy));
                else ((dev.cataclysmnouveau.mixin.ProjectileHitAccess)storm).confluxCataclysm$nativeHit(new EntityHitResult(enemy));
                h.assertTrue(enemy.getHealth()<500 && marker.hits==1 && enemy.getLastDamageSource().getEntity()==p,"Real native collision continues with original damage owner; orbit="+orbit);
                if(orbit){enemy.invulnerableTime=0;((ManagedProjectile)storm).confluxCataclysm$impact().accept(new EntityHitResult(enemy));h.assertTrue(marker.hits==2 && !storm.isRemoved() && storm.getState()==0,"Repeating contact retains the native visual state");}
                storm.discard();carrier.discard();enemy.discard();ExecutionSessions.tick();
            }
            h.succeed();
        }finally{p.discard();ExecutionSessions.tick();}
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker(){super(new net.minecraft.resources.ResourceLocation("cataclysm_nouveau","forge_test_marker"),"Marker");}
        public void onResolve(HitResult hit,net.minecraft.world.level.Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
        public int getDefaultManaCost(){return 0;}
        public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;}
        public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;}
        protected Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public String getBookDescription(){return "Test continuation";}
    }
    private static ServerPlayer player(GameTestHelper h){
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"ForgeStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,p){
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
        };
        p.setGameMode(GameType.SURVIVAL);p.setNoGravity(true);p.setPos(h.absoluteVec(new Vec3(4,100,4)));h.getLevel().addNewPlayer(p);
        var mana=CapabilityRegistry.getMana(p).orElseThrow(()->new AssertionError("Missing Ars mana"));mana.setMaxMana(100000);mana.setMana(100000);return p;
    }
}
