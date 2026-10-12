package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.HarbingerSmoke;
import net.minecraft.world.entity.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Wither_Smoke_Effect_Entity.class,remap=false)
public abstract class HarbingerSmokeMixin extends Entity implements HarbingerSmoke {
    @Unique private static final EntityDataAccessor<Boolean> harbinger$MANAGED_SMOKE=SynchedEntityData.defineId(Wither_Smoke_Effect_Entity.class,EntityDataSerializers.BOOLEAN);
    @Unique private float harbinger$bonus;
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void harbinger$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(harbinger$MANAGED_SMOKE,false);}
    public boolean confluxHarbinger$managedSmoke(){return entityData.get(harbinger$MANAGED_SMOKE);}
    public void confluxHarbinger$managedSmoke(boolean value){entityData.set(harbinger$MANAGED_SMOKE,value);}
    @Inject(method="onSyncedDataUpdated",at=@At("TAIL"))
    private void harbinger$particleTicks(EntityDataAccessor<?> key,CallbackInfo ci){
        // Vanilla flag is also respected by Entity Culling; no optional mod class is referenced.
        if(harbinger$MANAGED_SMOKE.equals(key))noCulling=confluxHarbinger$managedSmoke();
    }
    protected HarbingerSmokeMixin(EntityType<?> type,Level level){super(type,level);}
    public float confluxHarbinger$bonus(){return harbinger$bonus;}
    public void confluxHarbinger$bonus(float v){harbinger$bonus=v;}
    @Override public boolean shouldBeSaved(){return !confluxHarbinger$managedSmoke() && NativeSpellDamage.context(this)==null && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void harbinger$alive(CallbackInfo ci){var c=NativeSpellDamage.context(this);if(c!=null && (c.caster()==null || !c.caster().isAlive() || c.caster().level()!=level())){discard();ci.cancel();}}
}
