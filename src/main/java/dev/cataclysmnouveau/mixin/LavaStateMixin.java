package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Lava_Bomb_Entity;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Lava_Bomb_Entity.class,remap=false)
public abstract class LavaStateMixin {
    @Unique private static final EntityDataAccessor<Integer> confluxCataclysm$MODE=SynchedEntityData.defineId(Lava_Bomb_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxCataclysm$define(SynchedEntityData.Builder builder,CallbackInfo ci) {builder.define(confluxCataclysm$MODE,0);}
    public int confluxCataclysm$mode() {return ((Lava_Bomb_Entity)(Object)this).getEntityData().get(confluxCataclysm$MODE);}
    public void confluxCataclysm$mode(int mode) {((Lava_Bomb_Entity)(Object)this).getEntityData().set(confluxCataclysm$MODE,mode);}
}
