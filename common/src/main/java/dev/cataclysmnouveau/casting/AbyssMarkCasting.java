package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.effect.Abyss_Mark_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.EntityHitResult;

/** The native mark owns pursuit and its delayed portal; one session owns the whole chain. */
public final class AbyssMarkCasting {
    public static boolean start(ServerLevel world,CastContext cast,double amp,double duration) {
        if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                || !validTarget(world,cast,target))return false;
        var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
        if(owner==null || owner.level()!=world || !owner.isAlive())return false;
        var point=target.position();
        var mark=new Abyss_Mark_Entity(world,point.x,point.y,point.z,80,
                Math.max(0,(float)CMCommonConfig.Leviathan.AbyssBlastDamage),
                (float)CMCommonConfig.Leviathan.AbyssBlastHpDamage,owner.getUUID(),target);
        var state=(AbyssMark)mark;state.confluxMark$managed(true);
        int output=MonstrosityCasting.scaledTicks(160,duration);
        state.confluxMark$outputTicks(output);NativeDamageContexts.bind(mark,cast,amp);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast.withExecutor(null),mark,output+200,
                new ExecutionDriver<Abyss_Mark_Entity>() {
                    public boolean start(ExecutionSession<Abyss_Mark_Entity> s){return world.addFreshEntity(mark);}
                    public boolean tick(ExecutionSession<Abyss_Mark_Entity> s){
                        var portal=state.confluxMark$portal();
                        if(portal!=null)return portal.isRemoved() || !world.hasChunkAt(portal.blockPosition());
                        return mark.isRemoved() || !world.hasChunkAt(mark.blockPosition());
                    }
                    public void close(ExecutionSession<Abyss_Mark_Entity> s,ExecutionSession.EndReason reason){
                        mark.discard();var portal=state.confluxMark$portal();
                        if(portal!=null)LeviathanFormations.remove(portal);
                    }
                },h->{},()->GlyphCatalog.ABYSS_MARK.isEnabled(),ExecutionScope.DIRECT));
    }
    public static boolean validTarget(ServerLevel world,CastContext cast,Entity target){
        return target!=null && target.isAlive() && !target.isRemoved() && !target.isSpectator()
                && target.level()==world && world.isInWorldBounds(target.blockPosition())
                && world.hasChunkAt(target.blockPosition()) && !HarbingerCasting.allied(cast,target);
    }
    /** Called at the native birth, before the portal can tick or send its spawn packet. */
    public static boolean preparePortal(Abyss_Mark_Entity mark,Abyss_Blast_Portal_Entity portal){
        var cast=NativeDamageContexts.context(mark);
        if(cast==null || !cast.caster().isAlive() || cast.caster().isRemoved()
                || cast.caster().level()!=mark.level() || !GlyphCatalog.ABYSS_MARK.isEnabled()
                || !mark.level().isInWorldBounds(portal.blockPosition()) || !mark.level().hasChunkAt(portal.blockPosition()))return false;
        var state=(AbyssMark)mark;
        ((LeviathanPortal)portal).confluxLeviathan$beamDuration(state.confluxMark$outputTicks());
        ((LeviathanPortal)portal).confluxLeviathan$lifetime(state.confluxMark$outputTicks()+100);
        NativeDamageContexts.bind(portal,cast,NativeDamageContexts.amplification(mark));
        return true;
    }
    private AbyssMarkCasting(){}
}
