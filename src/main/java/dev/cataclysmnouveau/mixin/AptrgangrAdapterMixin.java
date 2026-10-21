package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Aptrgangr_Entity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=Aptrgangr_Entity.class,remap=false)
public abstract class AptrgangrAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Internal_Animation_Monster;aiStep()V"))
    private void aptr$frameOnly(Aptrgangr_Entity e,Operation<Void> original){if(!((Object)this instanceof SpellAptrgangr))original.call(e);}
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean aptr$child(Level w,Entity child,Operation<Boolean> original){return (Object)this instanceof SpellAptrgangr frame?AptrgangrCasting.child(frame,child,()->original.call(w,child)):original.call(w,child);}
    @ModifyArg(method="AreaAttack",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity aptr$owner(LivingEntity owner){return (Object)this instanceof SpellAptrgangr frame?(frame.cast.damageOwner()==null?frame.cast.caster():frame.cast.damageOwner()):owner;}
    @ModifyExpressionValue(method="Makeparticle",at=@At(value="FIELD",target="Lnet/minecraft/world/level/Level;isClientSide:Z",remap=true))
    private boolean aptr$particles(boolean client){return client || (Object)this instanceof SpellAptrgangr;}
    @WrapOperation(method="Makeparticle",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",remap=true))
    private void aptr$send(Level w,ParticleOptions p,double x,double y,double z,double vx,double vy,double vz,Operation<Void> original){
        if((Object)this instanceof SpellAptrgangr && w instanceof ServerLevel server)server.sendParticles(p,x,y,z,0,vx,vy,vz,1);else original.call(w,p,x,y,z,vx,vy,vz);
    }
}
