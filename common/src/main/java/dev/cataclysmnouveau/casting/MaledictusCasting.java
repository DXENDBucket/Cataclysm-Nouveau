package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;

/** Full native entities keep their registered renderer, events and timed damage window. */
public final class MaledictusCasting {
    private static final Set<UUID> ACTORS=new HashSet<>();
    public static boolean isActing(Entity actor){return ACTORS.contains(actor.getUUID());}
    static void lock(LivingEntity actor){if(actor!=null)ACTORS.add(actor.getUUID());}
    static void unlock(LivingEntity actor){if(actor!=null)ACTORS.remove(actor.getUUID());}
    public static boolean supports(Ability ability) {return switch(ability){case PHANTOM_HALBERD,CURSED_RAGE,PHANTOM_SPIRAL,PHANTOM_RINGS,HALBERD_CLEAVE,SOUL_BURST->true;default->false;};}
    static boolean enabled(Ability ability) {
        return switch(ability){
            case PHANTOM_HALBERD -> GlyphCatalog.PHANTOM_HALBERD.isEnabled();
            case PHANTOM_SPIRAL -> GlyphCatalog.PHANTOM_SPIRAL.isEnabled();
            case PHANTOM_RINGS -> GlyphCatalog.PHANTOM_RINGS.isEnabled();
            case HALBERD_CLEAVE -> GlyphCatalog.HALBERD_CLEAVE.isEnabled();
            case SOUL_BURST -> GlyphCatalog.SOUL_BURST.isEnabled();
            default -> false;
        };
    }
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amplify) {
        if(ability==Ability.CURSED_RAGE) {
            var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
            return anchor!=null && anchor.entity()!=null && anchor.valid(world,cast)
                    && MaledictusRage.gain(anchor.entity(),Math.max(0,CataclysmConfig.rageGain()+(int)Math.floor(amplify)));
        }
        if(ability==Ability.PHANTOM_SPIRAL || ability==Ability.PHANTOM_RINGS)
            return MaledictusFormations.start(ability,world,cast,amplify);
        if(ability==Ability.HALBERD_CLEAVE || ability==Ability.SOUL_BURST)
            return MaledictusActions.start(ability,world,cast,amplify);
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
    /** Native children inherit their actual source, so parent cancellation and configuration stay scoped. */
    public static boolean child(ServerLevel world,Entity entity,SpellMaledictus boss) {
        if(!(entity instanceof Phantom_Halberd_Entity halberd) || boss.childrenCancelled)return false;
        if(boss.collecting) {
            boss.stages.computeIfAbsent(boss.collectingStage,k->new ArrayList<>()).add(halberd);
            return true;
        }
        if(!world.hasChunkAt(halberd.blockPosition()))return false;
        halberd.setCaster(boss.cast.damageOwner());
        halberd.setDamage(MaledictusRage.halberdDamage(CMCommonConfig.Maledictus.PhantomHalberdDamage,boss.cast.caster()));
        ((MaledictusHalberd)halberd).confluxMaledictus$managed(true);
        NativeDamageContexts.bind(halberd,boss.cast,boss.amplification);
        boolean started=ExecutionSessions.start(new ExecutionSession<>(world,boss.cast,halberd,120,
                new ExecutionDriver<Phantom_Halberd_Entity>() {
                    public boolean start(ExecutionSession<Phantom_Halberd_Entity> s){return world.addFreshEntity(halberd);}
                    public boolean tick(ExecutionSession<Phantom_Halberd_Entity> s){return halberd.isRemoved() || boss.childrenCancelled
                            || !world.hasChunkAt(halberd.blockPosition()) || !enabled(boss.ability)
                            || boss.cast.executor()!=null && !ExecutionAnchor.mayExecute(boss.cast,boss.cast.executor());}
                    public void close(ExecutionSession<Phantom_Halberd_Entity> s,ExecutionSession.EndReason reason){boss.children.remove(halberd);halberd.discard();}
                },h->{},()->enabled(boss.ability),ExecutionScope.DIRECT));
        if(started){boss.spawnedHalberds++;boss.children.add(halberd);MaledictusVisuals.halberd(world,halberd,boss.ability);}
        return started;
    }
    private MaledictusCasting() {}
}
