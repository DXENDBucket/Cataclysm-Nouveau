package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.syncher.*;

@Mixin(value=Sandstorm_Entity.class,remap=false)
public abstract class RemnantStormMixin implements RemnantStorm {
    @Unique private static final EntityDataAccessor<Integer> remnant$LIFE=SynchedEntityData.defineId(Sandstorm_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void remnant$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(remnant$LIFE,300);}
    public void confluxRemnant$lifetime(int life){((Sandstorm_Entity)(Object)this).getEntityData().set(remnant$LIFE,life);}
    @ModifyConstant(method="tick",constant=@Constant(intValue=295))
    private int remnant$spawnThreshold(int original){return ((Sandstorm_Entity)(Object)this).getEntityData().get(remnant$LIFE)-5;}
    @Unique private SpellRemnant confluxRemnant$frame;
    public SpellRemnant confluxRemnant$frame(){return confluxRemnant$frame;}
    public void confluxRemnant$frame(SpellRemnant frame){confluxRemnant$frame=frame;}
    @WrapOperation(method="updateMotion",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/effect/Sandstorm_Entity;getCaster()Lnet/minecraft/world/entity/LivingEntity;"))
    private LivingEntity remnant$orbit(Sandstorm_Entity storm,Operation<LivingEntity> original){return confluxRemnant$frame==null?original.call(storm):confluxRemnant$frame;}
}
