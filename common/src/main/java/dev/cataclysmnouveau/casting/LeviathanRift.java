package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Dimensional_Rift_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.HitResult;

public final class LeviathanRift {
    public static boolean start(ServerLevel world,CastContext cast,double amp,double duration){
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS || !world.hasChunkAt(net.minecraft.core.BlockPos.containing(cast.origin())))return false;
        var p=cast.origin();var rift=new Dimensional_Rift_Entity(world,p.x,p.y,p.z,cast.damageOwner());
        int life=MonstrosityCasting.scaledTicks(600,duration);rift.setLifespan(life);rift.setStage(1);
        ((LeviathanArea)rift).confluxLeviathan$lifetime(life);NativeDamageContexts.bind(rift,cast,amp);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast.withExecutor(null),rift,life+200,new ExecutionDriver<Dimensional_Rift_Entity>(){
            int age;
            public boolean start(ExecutionSession<Dimensional_Rift_Entity> s){return world.addFreshEntity(rift);}
            public boolean tick(ExecutionSession<Dimensional_Rift_Entity> s){
                ++age;
                if((age==38 || age==70 || age==102) && rift.getLifespan()>100 && rift.getStage()<4){
                    rift.setStage(rift.getStage()+1);
                    world.playSound(null,rift.blockPosition(),net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK,net.minecraft.sounds.SoundSource.PLAYERS,3,1+(rift.getStage()-1)*.1F);
                    com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity.ScreenShake(world,rift.position(),30,.05F*rift.getStage(),10,10);
                }
                return rift.isRemoved() || !world.hasChunkAt(rift.blockPosition());
            }
            public void close(ExecutionSession<Dimensional_Rift_Entity> s,ExecutionSession.EndReason reason){rift.discard();}
        },hit->{},()->LeviathanCasting.enabled(Ability.DIMENSIONAL_RIFT),ExecutionScope.DIRECT));
    }
    private LeviathanRift(){}
}
