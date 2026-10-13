package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.client.particle.Options.*;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/** Server transport for native particle types that otherwise exist only inside Boss client AI. */
final class MaledictusVisuals {
    static void halberd(ServerLevel world,Phantom_Halberd_Entity halberd,Ability ability) {
        int count=ability==Ability.PHANTOM_SPIRAL?1:ability==Ability.PHANTOM_RINGS?6:2;
        world.sendParticles(ModParticle.PHANTOM_WING_FLAME.get(),halberd.getX(),halberd.getY()+.3,halberd.getZ(),
                count,count==1?0:.35,0,count==1?0:.35,.007);
    }
    static void tick(ServerLevel world,SpellMaledictus frame,LivingEntity actor,boolean cleave,int age) {
        var origin=frame.position();var heading=frame.getLookAngle().multiply(1,0,1).normalize();
        if(cleave) {
            if((age==15 || age==44) && actor!=null)actor.swing(InteractionHand.MAIN_HAND);
            return;
        }
        if(age==10 || age==15 || age==20) {
            double radius=age==10?5:age==15?7:9;
            // Bounded batches keep the original flame type and expanding preparation footprint.
            world.sendParticles(ModParticle.PHANTOM_WING_FLAME.get(),origin.x,origin.y+.3,origin.z,70,radius/3,0,radius/3,.02);
        }
        var front=origin.add(heading.scale(1.5));
        if(age==30 || age==33 || age==36)
            world.sendParticles(new RoarParticleOptions(20,26,158,130,.4F,.4F,.5F,2.5F),
                    front.x,front.y+1.8,front.z,1,0,0,0,0);
        if(age==34) {
            var ring=origin.add(heading);
            world.sendParticles(new RingParticleOptions(0,(float)Math.PI/2,30,86,236,204,1,85,false,0),
                    ring.x,ring.y+.02,ring.z,1,0,0,0,0);
        }
        if(age>34 && age<44 && age%2==0) {
            var center=origin.add(heading);
            world.sendParticles(ModParticle.CURSED_FLAME.get(),center.x,center.y+.3,center.z,80,0,0,0,.25);
        }
    }
    private MaledictusVisuals() {}
}
