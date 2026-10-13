package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.mixin.MaledictusAttackAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.*;

/** Native geometry is prepared without world insertion; only eligible phases become live. */
final class MaledictusFormations {
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        var point=NativePayloads.groundPoint(world,cast);
        int radius=ability==Ability.PHANTOM_SPIRAL?12:8;
        if(point==null || !MonstrosityCasting.loadedArea(world,point,radius))return false;
        var context=cast.withExecutor(null);
        var boss=new SpellMaledictus(world,context,ability,point,MonstrosityCasting.direction(cast),amp);
        var access=(MaledictusAttackAccess)(Object)boss;
        boss.collecting=true;
        try {
            if(ability==Ability.PHANTOM_SPIRAL) {
                access.confluxMaledictus$spiral(6,10,1,.75,.2,1);
                access.confluxMaledictus$spiral(4,10,1,1.2,.15,1);
            } else {
                access.confluxMaledictus$ring(14,14,7+boss.getRandom().nextInt(3),4,1);
                boss.collectingStage=1;access.confluxMaledictus$ring(16,16,10+boss.getRandom().nextInt(5),5.5,1);
                boss.collectingStage=3;access.confluxMaledictus$ring(18,18,13+boss.getRandom().nextInt(8),6.5,1);
                boss.collectingStage=4;access.confluxMaledictus$ring(20,20,16+boss.getRandom().nextInt(10),7.5,1);
            }
        } finally {boss.collecting=false;}
        boolean usable=boss.stages.entrySet().stream().anyMatch(e->eligible(boss,e.getKey()) && !e.getValue().isEmpty());
        if(!usable){discardPending(boss);boss.discard();return false;}
        return ExecutionSessions.start(new ExecutionSession<>(world,context,boss,8,new ExecutionDriver<SpellMaledictus>() {
            int age;
            public boolean start(ExecutionSession<SpellMaledictus> s) {
                boss.playSound(com.github.L_Ender.cataclysm.init.ModSounds.PHANTOM_SPEAR.get(),1,1);
                stage(world,boss,0);return true;
            }
            public boolean tick(ExecutionSession<SpellMaledictus> s) {
                if(!MonstrosityCasting.loadedArea(world,point,radius)){s.finish(ExecutionSession.EndReason.WORLD_UNLOADED);return false;}
                stage(world,boss,++age);return age>=(ability==Ability.PHANTOM_SPIRAL?1:4);
            }
            public void close(ExecutionSession<SpellMaledictus> s,ExecutionSession.EndReason reason) {
                if(reason!=ExecutionSession.EndReason.COMPLETED)boss.cancelChildren();
                discardPending(boss);boss.discard();
            }
        },h->{},()->MaledictusCasting.enabled(ability),ExecutionScope.DIRECT));
    }
    private static boolean eligible(SpellMaledictus boss,int phase){return phase<3 || (phase==3?boss.isHalfHealth():boss.isQuarterHealth());}
    private static void stage(ServerLevel world,SpellMaledictus boss,int phase) {
        var list=boss.stages.remove(phase);if(list==null)return;
        for(var halberd:list) {
            if(!eligible(boss,phase) || !MaledictusCasting.child(world,halberd,boss))halberd.discard();
        }
    }
    private static void discardPending(SpellMaledictus boss){boss.stages.values().forEach(list->list.forEach(Entity::discard));boss.stages.clear();}
    private MaledictusFormations() {}
}
