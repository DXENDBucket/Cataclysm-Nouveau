package dev.cataclysmnouveau.client;

import com.github.L_Ender.cataclysm.client.particle.StormParticle;
import dev.cataclysmnouveau.CataclysmParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Original trail, texture, orbit, fade and expiry, with a fixed client-only anchor.
 * The anchor is never added to the world, tracked, rendered, ticked or used for gameplay. */
@EventBusSubscriber(modid="cataclysm_nouveau",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class PositionStormParticle extends StormParticle {
    private final Vec3 origin;
    private final ArmorStand visualAnchor;
    private PositionStormParticle(ClientLevel level,double x,double y,double z) {
        super(level,x,y,z,.388F,.76F,.88F,5.5F,.7F,-1);
        origin=new Vec3(x,y,z);
        visualAnchor=new ArmorStand(level,x,y,z);
    }
    @Override public Vec3 getEntityPosition(){return origin==null?super.getEntityPosition():origin;}
    @Override public Entity getFromEntity(){return visualAnchor;}
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpecial(CataclysmParticles.POSITION_STORM.get(),
            (options,world,x,y,z,vx,vy,vz)->new PositionStormParticle(world,x,y,z));
    }
}
