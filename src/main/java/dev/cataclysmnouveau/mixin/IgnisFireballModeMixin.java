package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Ignis_Fireball_Entity.class,remap=false)
public abstract class IgnisFireballModeMixin {
    @Unique private static final EntityDataAccessor<Integer> ignis$MODE=SynchedEntityData.defineId(Ignis_Fireball_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void ignis$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(ignis$MODE,0);}
    public int confluxCataclysm$mode(){return ((Ignis_Fireball_Entity)(Object)this).getEntityData().get(ignis$MODE);}
    public void confluxCataclysm$mode(int value){((Ignis_Fireball_Entity)(Object)this).getEntityData().set(ignis$MODE,value);}
}
