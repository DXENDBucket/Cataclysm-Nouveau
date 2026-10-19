package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Predicate;

/** Run native visual/trail ticks while the Ars carrier exclusively owns movement and hits. */
@Mixin(value={com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity.class,Laser_Beam_Entity.class,Wither_Missile_Entity.class,Wither_Homing_Missile_Entity.class,Wither_Howitzer_Entity.class,Ender_Guardian_Bullet_Entity.class},remap=false)
public abstract class HarbingerNativeMixin {
    @WrapMethod(method="tick")
    private void harbinger$visualTick(Operation<Void> original) {
        var e=(Entity)(Object)this;
        if(((ManagedProjectile)e).confluxCataclysm$mode()!=1){original.call();return;}
        if(e.level().isClientSide)e.setPos(e.position().add(e.getDeltaMovement()));
        HarbingerCasting.decorate(e,()->original.call());
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;",remap=true),require=0)
    private HitResult harbinger$collision(Entity e,Predicate<Entity> filter,ClipContext.Block clip,Operation<HitResult> original) {
        return HarbingerCasting.decorating(e)?BlockHitResult.miss(e.position(),Direction.DOWN,e.blockPosition()):original.call(e,filter,clip);
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;",remap=true),require=0)
    private HitResult harbinger$homingCollision(Entity e,Predicate<Entity> filter,Operation<HitResult> original) {
        return HarbingerCasting.decorating(e)?BlockHitResult.miss(e.position(),Direction.DOWN,e.blockPosition()):original.call(e,filter);
    }
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;",remap=true),require=0)
    private Explosion harbinger$explosion(Level world,Entity source,double x,double y,double z,float radius,boolean fire,Level.ExplosionInteraction interaction,Operation<Explosion> original) {
        var payload=(Entity)(Object)this;
        if(payload instanceof com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity && NativeSpellDamage.context(payload)!=null){
            if(HarbingerCasting.decorating(payload) && !((ManagedProjectile)payload).confluxCataclysm$resolving())return null;
            return LeviathanCasting.explosion(payload,world,x,y,z,radius);
        }
        if(NativeSpellDamage.context(source)==null)return original.call(world,source,x,y,z,radius,fire,interaction);
        if(HarbingerCasting.decorating(source) && !((ManagedProjectile)source).confluxCataclysm$resolving())return null;
        return HarbingerCasting.explosion(source,world,x,y,z,radius);
    }
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true),require=0)
    private boolean harbinger$smoke(Level world,Entity child,Operation<Boolean> original) {
        return world instanceof ServerLevel server?HarbingerCasting.smoke(server,child,(Entity)(Object)this,()->original.call(world,child)):original.call(world,child);
    }
    @Inject(method="onHitBlock",at=@At("HEAD"),cancellable=true,require=0)
    private void harbinger$terrain(BlockHitResult hit,CallbackInfo ci) {
        var e=(Entity)(Object)this;
        if(e instanceof Laser_Beam_Entity && NativeSpellDamage.context(e)!=null && !dev.cataclysmnouveau.CataclysmConfig.harbingerTerrainFire())ci.cancel();
    }
}
