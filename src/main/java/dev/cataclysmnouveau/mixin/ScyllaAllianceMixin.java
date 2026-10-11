package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.*;
import com.github.L_Ender.cataclysm.entity.projectile.Storm_Serpent_Entity;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.casting.ScyllaPayload;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Lightning_Storm_Entity.class,Lightning_Area_Effect_Entity.class,Storm_Serpent_Entity.class},remap=false)
public abstract class ScyllaAllianceMixin {
    @Inject(method="damage",at=@At("HEAD"),cancellable=true)
    private void confluxScylla$allies(LivingEntity target,CallbackInfo ci) {
        var cast=((ScyllaPayload)this).confluxScylla$cast();
        if(cast!=null && (Allies.areAllies(cast.caster(),target) || Allies.areAllies(cast.damageOwner(),target))) ci.cancel();
    }
}
