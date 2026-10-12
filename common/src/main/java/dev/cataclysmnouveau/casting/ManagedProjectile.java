package dev.cataclysmnouveau.casting;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.projectile.CarrierProxy;
import net.minecraft.world.phys.HitResult;
import java.util.function.Consumer;
/** Transient state; mode 0 leaves original Cataclysm projectiles untouched. */
public interface ManagedProjectile extends CarrierProxy {
    dev.cataclysmnouveau.Ability confluxCataclysm$sourceAbility();
    void confluxCataclysm$sourceAbility(dev.cataclysmnouveau.Ability value);
    int confluxCataclysm$mode();
    void confluxCataclysm$mode(int mode);
    void confluxCataclysm$carrier(EntityProjectileSpell carrier);
    float confluxCataclysm$amplification();
    void confluxCataclysm$amplification(float bonus);
    boolean confluxCataclysm$resolving();
    void confluxCataclysm$resolving(boolean value);
    boolean confluxCataclysm$repeated();
    void confluxCataclysm$repeated(boolean value);
    Consumer<HitResult> confluxCataclysm$impact();
    void confluxCataclysm$impact(Consumer<HitResult> callback);
}
