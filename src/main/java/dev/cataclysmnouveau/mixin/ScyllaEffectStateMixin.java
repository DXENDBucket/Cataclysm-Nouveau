package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.*;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.casting.ScyllaPayload;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Lightning_Storm_Entity.class,Lightning_Area_Effect_Entity.class,Storm_Serpent_Entity.class},remap=false)
public abstract class ScyllaEffectStateMixin extends Entity implements ScyllaPayload {
    @Unique private CastContext confluxScylla$cast;
    @Unique private Level confluxScylla$ownerWorld;
    protected ScyllaEffectStateMixin(EntityType<?> type,Level world) {super(type,world);}
    public CastContext confluxScylla$cast() {return confluxScylla$cast;}
    public void confluxScylla$cast(CastContext value) {
        confluxScylla$cast=value;confluxScylla$ownerWorld=value==null?null:value.caster().level();
    }
    @Override public boolean shouldBeSaved() {return confluxScylla$cast==null && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void confluxScylla$cleanup(CallbackInfo ci) {
        if(confluxScylla$cast==null || level().isClientSide) return;
        var owner=confluxScylla$cast.caster();
        if(!owner.isAlive() || owner.isRemoved() || owner.level()!=confluxScylla$ownerWorld || !level().hasChunkAt(blockPosition()) || tickCount>1200) {
            discard();ci.cancel();
        }
    }
}
