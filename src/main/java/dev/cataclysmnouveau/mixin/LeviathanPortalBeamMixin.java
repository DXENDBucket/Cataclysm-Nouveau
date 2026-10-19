package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Portal_Abyss_Blast_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Portal_Abyss_Blast_Entity.class,remap=false)
public abstract class LeviathanPortalBeamMixin extends Entity implements LeviathanBeam {
    @Unique private static final EntityDataAccessor<Boolean> leviathan$MANAGED=SynchedEntityData.defineId(Portal_Abyss_Blast_Entity.class,EntityDataSerializers.BOOLEAN);
    protected LeviathanPortalBeamMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$MANAGED,false);}
    public boolean confluxLeviathan$managed(){return entityData.get(leviathan$MANAGED);}
    public void confluxLeviathan$managed(boolean value){entityData.set(leviathan$MANAGED,value);}
    @Override public boolean shouldBeSaved(){return !confluxLeviathan$managed() && super.shouldBeSaved();}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",remap=true))
    private boolean leviathan$terrain(Level world,BlockPos pos,boolean drops,Operation<Boolean> original){return !confluxLeviathan$managed() && original.call(world,pos,drops);}
}
