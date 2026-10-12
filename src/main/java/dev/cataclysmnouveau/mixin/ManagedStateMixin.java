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
@Mixin(value={Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Player_Ceraunus_Entity.class,Wave_Entity.class,Flare_Bomb_Entity.class,Lava_Bomb_Entity.class,Elemental_Spear_Entity.class,Spark_Entity.class,Laser_Beam_Entity.class,Wither_Missile_Entity.class,Wither_Homing_Missile_Entity.class,Wither_Howitzer_Entity.class,Ender_Guardian_Bullet_Entity.class,Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class},remap=false)
public abstract class ManagedStateMixin extends Entity implements ManagedProjectile {
    @Unique private EntityProjectileSpell confluxCataclysm$carrier;
    @Unique private dev.cataclysmnouveau.Ability confluxCataclysm$sourceAbility;
    public dev.cataclysmnouveau.Ability confluxCataclysm$sourceAbility(){return confluxCataclysm$sourceAbility;}
    public void confluxCataclysm$sourceAbility(dev.cataclysmnouveau.Ability value){confluxCataclysm$sourceAbility=value;}
    @Unique private float confluxCataclysm$amplification;
    @Unique private boolean confluxCataclysm$resolving, confluxCataclysm$repeated;
    @Unique private Consumer<HitResult> confluxCataclysm$impact;
    protected ManagedStateMixin(EntityType<?> type,Level level) { super(type,level); }
    @Override public boolean shouldBeSaved() { return confluxCataclysm$mode()==0 && (!((Object)this instanceof dev.cataclysmnouveau.casting.ScyllaPayload payload) || payload.confluxScylla$cast()==null) && super.shouldBeSaved(); }
    public EntityProjectileSpell confluxTrajectory() { return confluxCataclysm$carrier; }
    public void confluxCataclysm$carrier(EntityProjectileSpell value) { confluxCataclysm$carrier=value; }
    public float confluxCataclysm$amplification() { return confluxCataclysm$amplification; }
    public void confluxCataclysm$amplification(float value) { confluxCataclysm$amplification=value; }
    public boolean confluxCataclysm$resolving() { return confluxCataclysm$resolving; }
    public void confluxCataclysm$resolving(boolean value) { confluxCataclysm$resolving=value; }
    public boolean confluxCataclysm$repeated() { return confluxCataclysm$repeated; }
    public void confluxCataclysm$repeated(boolean value) { confluxCataclysm$repeated=value; }
    public Consumer<HitResult> confluxCataclysm$impact() { return confluxCataclysm$impact; }
    public void confluxCataclysm$impact(Consumer<HitResult> value) { confluxCataclysm$impact=value; }
}
