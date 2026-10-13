package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Full native entities keep their registered renderer, events and timed damage window. */
public final class MaledictusCasting {
    public static boolean supports(Ability ability) {return ability==Ability.PHANTOM_HALBERD || ability==Ability.CURSED_RAGE;}
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amplify) {
        if(ability==Ability.CURSED_RAGE) {
            var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
            return anchor!=null && anchor.entity()!=null && anchor.valid(world,cast)
                    && MaledictusRage.gain(anchor.entity(),Math.max(0,CataclysmConfig.rageGain()+(int)Math.floor(amplify)));
        }
        if(ability!=Ability.PHANTOM_HALBERD)return false;
        var point=NativePayloads.groundPoint(world,cast);if(point==null)return false;
        var direction=cast.incomingDirection();
        if(direction==null || direction.lengthSqr()<1e-10)direction=cast.caster().getLookAngle();
        float yaw=(float)Math.atan2(direction.z,direction.x);
        var halberd=new Phantom_Halberd_Entity(world,point.x,point.y,point.z,yaw,
                CataclysmConfig.halberdWarmup(),cast.damageOwner(),
                MaledictusRage.halberdDamage(CMCommonConfig.Maledictus.PhantomHalberdDamage,cast.caster()));
        ((MaledictusHalberd)halberd).confluxMaledictus$managed(true);
        NativeDamageContexts.bind(halberd,cast,amplify);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,halberd,CataclysmConfig.halberdWarmup()+90,
                new ExecutionDriver<Phantom_Halberd_Entity>() {
                    public boolean start(ExecutionSession<Phantom_Halberd_Entity> session){return world.addFreshEntity(halberd);}
                    public boolean tick(ExecutionSession<Phantom_Halberd_Entity> session){return halberd.isRemoved() || !world.hasChunkAt(halberd.blockPosition());}
                    public void close(ExecutionSession<Phantom_Halberd_Entity> session,ExecutionSession.EndReason reason){halberd.discard();}
                },h->{},()->GlyphCatalog.PHANTOM_HALBERD.isEnabled(),ExecutionScope.DIRECT));
    }
    private MaledictusCasting() {}
}
