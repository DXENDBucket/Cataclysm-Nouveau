package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.client.particle.Options.*;
import com.github.L_Ender.cataclysm.init.ModParticle;
import dev.cataclysmnouveau.CataclysmParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Same native visual timing and geometry for a moving actor or a fixed invocation frame. */
final class ScyllaActionVisuals {
    static void tick(ServerLevel world,SpellScylla frame,LivingEntity actor,boolean spin,int age) {
        if(spin) {
            if(age==29 || age==40 || age==51)strike(world,frame,actor,true);
            else if(age>=12 && age<=51 && age%4==0) {
                Vec3 origin=actor==null?frame.position():actor.position();
                ParticleOptions trail=actor==null?CataclysmParticles.POSITION_STORM.get():
                    new StormParticleOptions(.388F,.76F,.88F,5.5F,.7F,actor.getId());
                world.sendParticles(trail,origin.x,origin.y,origin.z,1,0,0,0,0);
            }
        } else if(age==21 || age==104)strike(world,frame,actor,false);
    }
    private static void strike(ServerLevel world,SpellScylla frame,LivingEntity actor,boolean spin) {
        if(actor!=null)actor.swing(InteractionHand.MAIN_HAND);
        Vec3 origin=actor==null?frame.position():actor.position();
        double height=actor==null?frame.getBbHeight():actor.getBbHeight();
        Vec3 point=spin?origin:origin.add(frame.getLookAngle().multiply(1,0,1).normalize().scale(6));
        world.sendParticles(new ScyllaSwingParticleOptions(spin?2:1.2F,(float)Math.toRadians(-frame.yBodyRot),spin?-1.3F:0),
                point.x,point.y+height*.5,point.z,1,0,0,0,0);
        if(!spin) {
            landing(world,point);
            world.sendParticles(new RingParticleOptions(0,(float)Math.PI/2,30,143,241,215,1,30,false,0),point.x,point.y+.02,point.z,1,0,0,0,0);
            var below=BlockPos.containing(point).below();
            if(world.hasChunkAt(below))world.sendParticles(new BlockParticleOption(ParticleTypes.DUST_PILLAR,world.getBlockState(below)),
                    point.x,point.y+.2,point.z,40,1.5,.2,1.5,.07);
        }
    }
    static void landing(ServerLevel world,Vec3 point) {
        world.sendParticles(ModParticle.LIGHTNING_EXPLODE.get(),point.x,point.y+.03,point.z,1,0,0,0,0);
    }
    private ScyllaActionVisuals() {}
}
