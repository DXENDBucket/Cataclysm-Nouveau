package dev.cataclysmnouveau;
import net.minecraft.core.particles.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public final class CataclysmParticles {
 private static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES,"cataclysm_nouveau");
 public static final RegistryObject<SimpleParticleType> POSITION_STORM=TYPES.register("scylla_position_storm",()->new SimpleParticleType(false));
 public static void register(IEventBus bus){TYPES.register(bus);}
 private CataclysmParticles(){}
}
