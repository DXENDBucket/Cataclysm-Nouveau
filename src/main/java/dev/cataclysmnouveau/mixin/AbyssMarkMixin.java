package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.Abyss_Mark_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.GlyphCatalog;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Abyss_Mark_Entity.class,remap=false)
public abstract class AbyssMarkMixin extends Entity implements AbyssMark {
    @Unique private static final EntityDataAccessor<Boolean> mark$MANAGED=SynchedEntityData.defineId(Abyss_Mark_Entity.class,EntityDataSerializers.BOOLEAN);
    @Shadow private Entity finalTarget;
    @Unique private int mark$output=160;
    @Unique private Abyss_Blast_Portal_Entity mark$portal;
    protected AbyssMarkMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void mark$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(mark$MANAGED,false);}
    public boolean confluxMark$managed(){return entityData.get(mark$MANAGED);}
    public void confluxMark$managed(boolean value){entityData.set(mark$MANAGED,value);noCulling=value;}
    public int confluxMark$outputTicks(){return mark$output;}
    public void confluxMark$outputTicks(int value){mark$output=value;}
    public Abyss_Blast_Portal_Entity confluxMark$portal(){return mark$portal;}
    public void confluxMark$portal(Abyss_Blast_Portal_Entity value){mark$portal=value;}
    @Override public boolean shouldBeSaved(){return !confluxMark$managed() && super.shouldBeSaved();}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){
        super.onSyncedDataUpdated(key);if(mark$MANAGED.equals(key) && confluxMark$managed())noCulling=true;
    }
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void mark$validate(CallbackInfo ci){
        if(!confluxMark$managed() || !(level() instanceof ServerLevel world))return;
        var cast=NativeDamageContexts.context(this);
        if(cast==null || cast.caster()==null || !cast.caster().isAlive() || cast.caster().isRemoved()
                || cast.caster().level()!=world || !world.isInWorldBounds(blockPosition())
                || !world.hasChunkAt(blockPosition()) || !GlyphCatalog.ABYSS_MARK.isEnabled()){
            discard();ci.cancel();return;
        }
        // Keep the last location when pursuit is no longer valid, as the native mark does on death.
        if(finalTarget!=null && !AbyssMarkCasting.validTarget(world,cast,finalTarget))finalTarget=null;
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean mark$child(Level world,Entity child,Operation<Boolean> original){
        if(!confluxMark$managed() || world.isClientSide)return original.call(world,child);
        if(!(child instanceof Abyss_Blast_Portal_Entity portal)
                || !AbyssMarkCasting.preparePortal((Abyss_Mark_Entity)(Object)this,portal)){child.discard();return false;}
        boolean added=original.call(world,child);
        if(added)mark$portal=portal;else child.discard();return added;
    }
}
