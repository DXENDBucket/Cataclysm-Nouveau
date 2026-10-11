package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Flare_Bomb_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;
import dev.cataclysmnouveau.CataclysmConfig;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Preserve native pattern generation, animation and owner; modify only our paid payloads. */
@Mixin(value=Flare_Bomb_Entity.class,remap=false)
public abstract class FlareDamageMixin {
    @ModifyArg(method="onHitEntity",at=@At(value="INVOKE",
            target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float confluxCataclysm$direct(float amount) {
        var state=(ManagedProjectile)this;
        return state.confluxCataclysm$mode()==0?amount:Math.max(0,CataclysmConfig.flareDirectDamage()+state.confluxCataclysm$bonus());
    }
    @ModifyArg(method="spawnJet",at=@At(value="INVOKE",
            target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),index=0,require=2)
    private Entity confluxCataclysm$jet(Entity entity) {
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()!=0 && entity instanceof Flame_Jet_Entity jet)
            jet.setDamage(Math.max(0,CataclysmConfig.flareJetDamage()+state.confluxCataclysm$bonus()));
        dev.arsconflux.api.damage.NativeSpellDamage.bind(entity,dev.cataclysmnouveau.casting.NativeDamageContexts.context((Entity)(Object)this));
        return entity;
    }
}
