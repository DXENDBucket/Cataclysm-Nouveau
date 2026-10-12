package dev.cataclysmnouveau.casting;
import net.minecraft.world.entity.LivingEntity;
/** Prepared native shots keep their target even after assigning the real damage owner. */
public interface IgnisProjectile {
    void confluxIgnis$aim(LivingEntity target,double acceleration);
    boolean confluxIgnis$ready();
}
