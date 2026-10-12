package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Wither_Howitzer_Entity;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Wither_Howitzer_Entity.class,remap=false)
public abstract class HowitzerStateMixin {
    @Unique private static final EntityDataAccessor<Integer> confluxCataclysm$MODE=SynchedEntityData.defineId(Wither_Howitzer_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxCataclysm$define(SynchedEntityData.Builder builder,CallbackInfo ci) { builder.define(confluxCataclysm$MODE,0); }
    public int confluxCataclysm$mode() { return ((Wither_Howitzer_Entity)(Object)this).getEntityData().get(confluxCataclysm$MODE); }
    public void confluxCataclysm$mode(int mode) { ((Wither_Howitzer_Entity)(Object)this).getEntityData().set(confluxCataclysm$MODE,mode); }
}
