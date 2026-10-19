package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Ashen_Breath_Entity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.cataclysmnouveau.casting.AshenBreath;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Ashen_Breath_Entity.class,remap=false)
public abstract class AshenBreathMixin extends Entity implements AshenBreath {
    @Unique private static final EntityDataAccessor<Integer> revenant$LIFE=SynchedEntityData.defineId(Ashen_Breath_Entity.class,EntityDataSerializers.INT);
    @Unique private boolean revenant$advancing;
    protected AshenBreathMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void revenant$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(revenant$LIFE,0);}
    public int confluxRevenant$lifetime(){return getEntityData().get(revenant$LIFE);}
    public void confluxRevenant$lifetime(int value){getEntityData().set(revenant$LIFE,value);}
    public void confluxRevenant$advance(boolean value){revenant$advancing=value;}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void revenant$singleDriver(CallbackInfo ci){if(!level().isClientSide && confluxRevenant$lifetime()>0 && !revenant$advancing)ci.cancel();}
    @ModifyConstant(method="tick",constant=@Constant(intValue=25))
    private int revenant$lifetime(int original){return confluxRevenant$lifetime()>0?confluxRevenant$lifetime():original;}
    @ModifyExpressionValue(method="tick",at=@At(value="FIELD",target="Lnet/minecraft/world/entity/LivingEntity;yHeadRot:F",remap=true))
    private float revenant$executorYaw(float original){return confluxRevenant$lifetime()>0?getYRot():original;}
    @Override public boolean shouldBeSaved(){return confluxRevenant$lifetime()==0 && super.shouldBeSaved();}
}
