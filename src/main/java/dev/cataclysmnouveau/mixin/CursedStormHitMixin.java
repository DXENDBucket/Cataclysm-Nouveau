package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Projectile.class)
public abstract class CursedStormHitMixin {
    @Inject(method="onHit",at=@At("HEAD"),cancellable=true)
    private void remnant$hit(HitResult hit,CallbackInfo ci){
        if(!((Object)this instanceof Cursed_Sandstorm_Entity))return;
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()==2 && !state.confluxCataclysm$resolving() && state.confluxCataclysm$impact()!=null){state.confluxCataclysm$impact().accept(hit);ci.cancel();}
    }
}
