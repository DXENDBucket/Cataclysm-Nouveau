package dev.cataclysmnouveau.casting;
import com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions;
import com.github.L_Ender.cataclysm.init.ModParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/** Sends client-only native strike particle types from the shared actual execution geometry. */
final class MaledictusCombatVisuals {
    static void tick(ServerLevel world,SpellMaledictus frame,LivingEntity actor,int state,int age) {
        boolean strike=switch(state){case 9->age==4;case 25->age==4 || age==37;
            case 19,21->age==12 || age==24;case 22,23->age==23 || age==33;default->false;};
        if(strike && actor!=null)actor.swing(InteractionHand.MAIN_HAND);
        boolean impact=state==9 && age==4 || state==25 && (age==4 || age==37)
                || (state==19 || state==21) && age==24 || (state==22 || state==23) && age==33;
        var p=frame.position();var front=p.add(frame.getLookAngle().multiply(1,0,1).normalize().scale(state==25?2.25:state==22 || state==23?3.5:2.5));
        if(impact) {
            float size=state==9?50:state==22 || state==23?20:30;
            world.sendParticles(new RingParticleOptions(0,(float)Math.PI/2,40,86,236,204,1,size,false,2),front.x,front.y+.2,front.z,1,0,0,0,0);
            var block=world.getBlockState(BlockPos.containing(front).below());
            if(!block.isAir())world.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,block),front.x,front.y+.2,front.z,
                    state==9?70:20,1,.2,1,.1);
        }
        boolean rushing=(state==11 || state==12) && age>=24 && age<=33
                || (state==15 || state==16) && age>=16 && age<=25 || state==17 && age>=16 && age<=24;
        if(rushing) {
            float yaw=(float)Math.toRadians(-frame.getYRot()),pitch=(float)Math.toRadians(-frame.getXRot());
            for(int side=0;side<2;side++)world.sendParticles(new RingParticleOptions(yaw+side*(float)Math.PI,pitch,40,86,236,204,1,50,false,2),
                    p.x,p.y+frame.getBbHeight()/2,p.z,1,0,0,0,0);
        }
        if((rushing || state==3 || state==24) && age%2==0)
            world.sendParticles(ModParticle.PHANTOM_WING_FLAME.get(),p.x,p.y+.5,p.z,3,.2,.4,.2,.01);
    }
    private MaledictusCombatVisuals(){}
}
