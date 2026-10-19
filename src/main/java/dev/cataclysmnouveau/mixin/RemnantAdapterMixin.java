package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=Ancient_Remnant_Entity.class,remap=false)
public abstract class RemnantAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/IABossMonsters/IABoss_monster;aiStep()V"))
    private void remnant$noMobTick(Ancient_Remnant_Entity boss,Operation<Void> original){if(!((Object)this instanceof SpellRemnant))original.call(boss);}
    @WrapOperation(method={"aiStep","spawnBlocks","EarthQuakeSummon"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean remnant$birth(Level world,Entity child,Operation<Boolean> original){return RemnantCasting.spawn(child,()->original.call(world,child));}
    @ModifyArg(method={"spawnBlocks","AreaAttack"},at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity remnant$owner(LivingEntity original){return (Object)this instanceof SpellRemnant frame?frame.cast.damageOwner():original;}
    @ModifyExpressionValue(method={"StompParticle","Roarparticle"},at=@At(value="FIELD",target="Lnet/minecraft/world/level/Level;isClientSide:Z",remap=true))
    private boolean remnant$visualBranch(boolean original){return original || (Object)this instanceof SpellRemnant;}
    @WrapOperation(method={"StompParticle","Roarparticle"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",remap=true))
    private void remnant$particles(Level world,ParticleOptions particle,double x,double y,double z,double vx,double vy,double vz,Operation<Void> original){
        if((Object)this instanceof SpellRemnant && world instanceof ServerLevel server)server.sendParticles(particle,x,y,z,0,vx,vy,vz,1);
        else original.call(world,particle,x,y,z,vx,vy,vz);
    }
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",remap=true))
    private void remnant$positionalSound(Level world,net.minecraft.world.entity.player.Player except,Entity emitter,SoundEvent sound,SoundSource source,float volume,float pitch,Operation<Void> original){
        if((Object)this instanceof SpellRemnant)world.playSound(except,emitter.getX(),emitter.getY(),emitter.getZ(),sound,source,volume,pitch);
        else original.call(world,except,emitter,sound,source,volume,pitch);
    }
    @WrapOperation(method="Stompsound",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V",remap=true))
    private void remnant$sound(Level world,double x,double y,double z,SoundEvent sound,SoundSource source,float volume,float pitch,boolean delayed,Operation<Void> original){
        if((Object)this instanceof SpellRemnant)world.playSound(null,x,y,z,sound,source,volume,pitch);
        else original.call(world,x,y,z,sound,source,volume,pitch,delayed);
    }
}
