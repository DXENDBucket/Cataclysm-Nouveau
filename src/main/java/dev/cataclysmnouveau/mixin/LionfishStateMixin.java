package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Lionfish_Spike_Entity;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
@Mixin(value=Lionfish_Spike_Entity.class,remap=false)
public abstract class LionfishStateMixin extends ThrowableItemProjectile {
    @Unique private static final EntityDataAccessor<Integer> confluxCataclysm$MODE=SynchedEntityData.defineId(Lionfish_Spike_Entity.class,EntityDataSerializers.INT);
    protected LionfishStateMixin(EntityType<? extends ThrowableItemProjectile> type,Level level) { super(type,level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(confluxCataclysm$MODE,0); }
    public int confluxCataclysm$mode() { return entityData.get(confluxCataclysm$MODE); }
    public void confluxCataclysm$mode(int mode) { entityData.set(confluxCataclysm$MODE,mode); }
}
