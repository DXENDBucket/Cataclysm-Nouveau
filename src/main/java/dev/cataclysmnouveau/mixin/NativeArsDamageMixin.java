package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.NativeDamageContexts;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Binding is opt-in; no native Boss or ordinary projectile gains Ars attributes. */
@Mixin(value={com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity.class,Phantom_Arrow_Entity.class,Phantom_Halberd_Entity.class,Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Player_Ceraunus_Entity.class,Wave_Entity.class,Flare_Bomb_Entity.class,
        Bolt_strike_Entity.class,Void_Rune_Entity.class,Flame_Jet_Entity.class,Elemental_Spear_Entity.class,Water_Spear_Entity.class,Lightning_Spear_Entity.class,
        Flame_Strike_Entity.class,Lightning_Storm_Entity.class,Lightning_Area_Effect_Entity.class,Storm_Serpent_Entity.class,
        Scylla_Entity.class,Netherite_Monstrosity_Entity.class,Laser_Beam_Entity.class,Wither_Missile_Entity.class,Wither_Homing_Missile_Entity.class,Wither_Howitzer_Entity.class,Death_Laser_Beam_Entity.class,Wither_Smoke_Effect_Entity.class,Spark_Entity.class,Lava_Bomb_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity.class,Ender_Guardian_Bullet_Entity.class,Void_Vortex_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity.class,Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity.class},remap=false)
public abstract class NativeArsDamageMixin {
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
    private boolean nativeArs$entity(Entity target,DamageSource source,float amount,Operation<Boolean> original) {
        if(((Entity)(Object)this).level().isClientSide)return original.call(target,source,amount);
        if(dev.cataclysmnouveau.casting.GuardianCasting.rejectDamage((Entity)(Object)this,target))return false;
        if(dev.cataclysmnouveau.casting.HarbingerCasting.rejectDamage((Entity)(Object)this,target)) return false;
        amount=dev.cataclysmnouveau.casting.HealthDamage.limit((Entity)(Object)this,amount);
        if(NativeDamageContexts.context((Entity)(Object)this)!=null)
            amount=dev.cataclysmnouveau.casting.Resources.augmentedDamage(amount,NativeDamageContexts.amplification((Entity)(Object)this));
        return NativeSpellDamage.hurt(NativeDamageContexts.context((Entity)(Object)this),target,source,amount,(s,a)->original.call(target,s,a));
    }
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
    private boolean nativeArs$living(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original) {
        if(((Entity)(Object)this).level().isClientSide)return original.call(target,source,amount);
        if(dev.cataclysmnouveau.casting.GuardianCasting.rejectDamage((Entity)(Object)this,target))return false;
        if(dev.cataclysmnouveau.casting.HarbingerCasting.rejectDamage((Entity)(Object)this,target)) return false;
        amount=dev.cataclysmnouveau.casting.HealthDamage.limit((Entity)(Object)this,amount);
        if(NativeDamageContexts.context((Entity)(Object)this)!=null)
            amount=dev.cataclysmnouveau.casting.Resources.augmentedDamage(amount,NativeDamageContexts.amplification((Entity)(Object)this));
        return NativeSpellDamage.hurt(NativeDamageContexts.context((Entity)(Object)this),target,source,amount,(s,a)->original.call(target,s,a));
    }
}
