package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Ender_Guardian_Bullet_Entity;
import dev.cataclysmnouveau.casting.GuardianBullet;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Ender_Guardian_Bullet_Entity.class,remap=false)
public abstract class GuardianBulletStateMixin implements GuardianBullet {
    @Shadow(remap=false) private boolean fired;
    @Unique private static final EntityDataAccessor<Integer> guardian$MODE=SynchedEntityData.defineId(Ender_Guardian_Bullet_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void guardian$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(guardian$MODE,0);}
    public int confluxCataclysm$mode(){return ((Ender_Guardian_Bullet_Entity)(Object)this).getEntityData().get(guardian$MODE);}
    public void confluxCataclysm$mode(int mode){((Ender_Guardian_Bullet_Entity)(Object)this).getEntityData().set(guardian$MODE,mode);}
    public boolean confluxGuardian$fired(){return fired;}
}
