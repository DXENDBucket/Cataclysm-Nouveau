package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

/** Visual transport only: native cloud ticks still own damage, radius and expiry. */
public final class HarbingerSmokeParticles {
    public static void send(Wither_Smoke_Effect_Entity cloud) {
        if (!(cloud.level() instanceof ServerLevel world)
                || !((HarbingerSmoke) cloud).confluxHarbinger$managedSmoke()
                || cloud.isRemoved() || cloud.isWaiting() || (cloud.tickCount & 1) != 0) return;
        // One batch per two ticks at the native cloud origin. No forced particle setting.
        double spread = cloud.getRadius() * .45;
        world.sendParticles(ParticleTypes.SMOKE, cloud.getX(), cloud.getY(), cloud.getZ(),
                20, spread, 0, spread, .025);
    }

    private HarbingerSmokeParticles() {}
}
