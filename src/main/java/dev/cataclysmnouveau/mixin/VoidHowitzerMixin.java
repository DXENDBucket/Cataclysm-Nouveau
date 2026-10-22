package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Void_Howitzer_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.VoidPayloads;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Void_Howitzer_Entity.class,remap=false)
public abstract class VoidHowitzerMixin {
    @Unique private static final EntityDataAccessor<Integer> voidPayload$MODE=SynchedEntityData.defineId(Void_Howitzer_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void voidPayload$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(voidPayload$MODE,0);}
    public int confluxCataclysm$mode(){return ((Entity)(Object)this).getEntityData().get(voidPayload$MODE);}
    public void confluxCataclysm$mode(int value){((Entity)(Object)this).getEntityData().set(voidPayload$MODE,value);}
    @WrapOperation(method="spawnFangs",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean voidPayload$rune(Level level,Entity child,Operation<Boolean> original){return VoidPayloads.child((Entity)(Object)this,child,()->original.call(level,child));}
    @Inject(method="spawnFangs",at=@At("HEAD"),cancellable=true)
    private void voidPayload$loaded(double x,double z,double minY,double maxY,float rotation,int delay,CallbackInfo ci){
        var self=(Entity)(Object)this;
        if(NativeSpellDamage.context(self)!=null && !self.level().hasChunkAt(net.minecraft.core.BlockPos.containing(x,maxY,z)))ci.cancel();
    }
}
