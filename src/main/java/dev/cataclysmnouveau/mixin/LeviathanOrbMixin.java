package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity;
import dev.cataclysmnouveau.casting.LeviathanOrb;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Abyss_Orb_Entity.class,remap=false)
public abstract class LeviathanOrbMixin implements LeviathanOrb {
    @Unique private static final EntityDataAccessor<Integer> leviathan$MODE=SynchedEntityData.defineId(Abyss_Orb_Entity.class,EntityDataSerializers.INT);
    @Shadow private int lifetick;
    @Unique private double leviathan$acceleration=1;
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$MODE,0);}
    public int confluxCataclysm$mode(){return ((Abyss_Orb_Entity)(Object)this).getEntityData().get(leviathan$MODE);}
    public void confluxCataclysm$mode(int value){((Abyss_Orb_Entity)(Object)this).getEntityData().set(leviathan$MODE,value);}
    public boolean confluxLeviathan$blockReady(){return lifetick>=(((Abyss_Orb_Entity)(Object)this).getTracking()?200:400);}
    public void confluxLeviathan$acceleration(double value){leviathan$acceleration=value;}
    @ModifyConstant(method="tick",constant=@Constant(doubleValue=0.05))
    private double leviathan$trackingSpeed(double original){return original*leviathan$acceleration;}
    @ModifyConstant(method="tick",constant=@Constant(doubleValue=-0.05))
    private double leviathan$trackingFallSpeed(double original){return original*leviathan$acceleration;}
}
