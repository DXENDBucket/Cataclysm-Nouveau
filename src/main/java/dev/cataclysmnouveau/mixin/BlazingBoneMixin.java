package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Blazing_Bone_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Blazing_Bone_Entity.class,remap=false)
public abstract class BlazingBoneMixin {
    @Unique private static final EntityDataAccessor<Integer> revenant$MODE=SynchedEntityData.defineId(Blazing_Bone_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void revenant$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(revenant$MODE,0);}
    public int confluxCataclysm$mode(){return ((Blazing_Bone_Entity)(Object)this).getEntityData().get(revenant$MODE);}
    public void confluxCataclysm$mode(int value){((Blazing_Bone_Entity)(Object)this).getEntityData().set(revenant$MODE,value);}
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Blazing_Bone_Entity;discard()V",remap=true))
    private void revenant$repeat(Blazing_Bone_Entity bone,Operation<Void> original){
        var state=(ManagedProjectile)bone;if(state.confluxCataclysm$mode()!=1 || !state.confluxCataclysm$repeated())original.call(bone);
    }
}
