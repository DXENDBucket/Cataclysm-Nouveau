package dev.cataclysmnouveau;

import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;

/** Position-only transport; rendering delegates to the installed Cataclysm particle. */
public final class CataclysmParticles {
    private static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,"cataclysm_nouveau");
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType> POSITION_STORM=
        TYPES.register("scylla_position_storm",()->new SimpleParticleType(false));
    public static void register(IEventBus bus){TYPES.register(bus);}
    private CataclysmParticles() {}
}
