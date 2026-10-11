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
@Mixin(value={Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Player_Ceraunus_Entity.class,Wave_Entity.class,Flare_Bomb_Entity.class,
        Bolt_strike_Entity.class,Void_Rune_Entity.class,Flame_Jet_Entity.class,Elemental_Spear_Entity.class,
        Lightning_Storm_Entity.class,Lightning_Area_Effect_Entity.class,Storm_Serpent_Entity.class,
        Scylla_Entity.class,Netherite_Monstrosity_Entity.class},remap=false)
public abstract class NativeArsDamageMixin {
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
    private boolean nativeArs$entity(Entity target,DamageSource source,float amount,Operation<Boolean> original) {
        return NativeSpellDamage.hurt(NativeDamageContexts.context((Entity)(Object)this),target,source,amount,(s,a)->original.call(target,s,a));
    }
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
    private boolean nativeArs$living(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original) {
        return NativeSpellDamage.hurt(NativeDamageContexts.context((Entity)(Object)this),target,source,amount,(s,a)->original.call(target,s,a));
    }
}
