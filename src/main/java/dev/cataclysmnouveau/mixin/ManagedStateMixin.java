package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import java.util.function.Consumer;
@Mixin(value={Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Player_Ceraunus_Entity.class,Wave_Entity.class,Flare_Bomb_Entity.class,Lava_Bomb_Entity.class,Elemental_Spear_Entity.class},remap=false)
public abstract class ManagedStateMixin extends Entity implements ManagedProjectile {
    @Unique private EntityProjectileSpell confluxCataclysm$carrier;
    @Unique private float confluxCataclysm$bonus;
    @Unique private boolean confluxCataclysm$resolving, confluxCataclysm$repeated;
    @Unique private Consumer<HitResult> confluxCataclysm$impact;
    protected ManagedStateMixin(EntityType<?> type,Level level) { super(type,level); }
    @Override public boolean shouldBeSaved() { return confluxCataclysm$mode()==0 && super.shouldBeSaved(); }
    public EntityProjectileSpell confluxTrajectory() { return confluxCataclysm$carrier; }
    public void confluxCataclysm$carrier(EntityProjectileSpell value) { confluxCataclysm$carrier=value; }
    public float confluxCataclysm$bonus() { return confluxCataclysm$bonus; }
    public void confluxCataclysm$bonus(float value) { confluxCataclysm$bonus=value; }
    public boolean confluxCataclysm$resolving() { return confluxCataclysm$resolving; }
    public void confluxCataclysm$resolving(boolean value) { confluxCataclysm$resolving=value; }
    public boolean confluxCataclysm$repeated() { return confluxCataclysm$repeated; }
    public void confluxCataclysm$repeated(boolean value) { confluxCataclysm$repeated=value; }
    public Consumer<HitResult> confluxCataclysm$impact() { return confluxCataclysm$impact; }
    public void confluxCataclysm$impact(Consumer<HitResult> value) { confluxCataclysm$impact=value; }
}
