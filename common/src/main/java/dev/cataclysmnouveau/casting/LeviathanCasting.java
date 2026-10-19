package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.*;

/** Native stationary areas retain their own tick, events, attacks and visual lifecycle. */
public final class LeviathanCasting {
    public static boolean supports(Ability a) { return a==Ability.ABYSS_MINE || a==Ability.ABYSS_BOMBARDMENT; }
    public static boolean enabled(Ability a) {
        return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==a && e.isEnabled());
    }
    public static boolean rejectDamage(Entity source,Entity target) {
        if(!(source instanceof Abyss_Blast_Entity || source instanceof Portal_Abyss_Blast_Entity))return false;
        var cast=NativeDamageContexts.context(source);
        if(cast==null)return false;
        int duration=source instanceof Abyss_Blast_Entity beam?beam.getDuration():((Portal_Abyss_Blast_Entity)source).getDuration();
        return HarbingerCasting.allied(cast,target) || source.tickCount>20+duration;
    }
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        // Mines can float; bombardment portals retain the native local ground search.
        Vec3 point=ability==Ability.ABYSS_MINE?cast.origin():NativePayloads.groundPoint(world,cast);
        if(point==null || !world.isInWorldBounds(BlockPos.containing(point)) || !world.hasChunkAt(BlockPos.containing(point)))return false;
        var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
        Entity entity;
        int life;
        if(ability==Ability.ABYSS_MINE) {
            life=MonstrosityCasting.scaledTicks(800,duration);
            entity=new Abyss_Mine_Entity(world,point.x,point.y,point.z,0,0,owner);
            if(!world.noCollision(entity))return false;
        } else if(ability==Ability.ABYSS_BOMBARDMENT) {
            int output=MonstrosityCasting.scaledTicks(160,duration);
            life=output+100;
            entity=new Abyss_Blast_Portal_Entity(world,point.x,point.y,point.z,0,0,
                    Math.max(0,(float)CMCommonConfig.Leviathan.AbyssBlastDamage),(float)CMCommonConfig.Leviathan.AbyssBlastHpDamage,owner);
            ((LeviathanPortal)entity).confluxLeviathan$beamDuration(output);
        } else return false;
        ((LeviathanArea)entity).confluxLeviathan$lifetime(life);
        NativeDamageContexts.bind(entity,cast,amp);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,entity,life+10,new ExecutionDriver<Entity>() {
            public boolean start(ExecutionSession<Entity> s){return world.addFreshEntity(entity);}
            public boolean tick(ExecutionSession<Entity> s){return entity.isRemoved() || !world.hasChunkAt(entity.blockPosition());}
            public void close(ExecutionSession<Entity> s,ExecutionSession.EndReason reason){
                if(entity instanceof LeviathanPortal portal && portal.confluxLeviathan$child()!=null)portal.confluxLeviathan$child().discard();
                entity.discard();
            }
        },hit->{},()->enabled(ability),ExecutionScope.DIRECT));
    }
    public static void bindChild(Abyss_Blast_Portal_Entity parent,Portal_Abyss_Blast_Entity child) {
        var cast=NativeDamageContexts.context(parent);
        ((LeviathanBeam)child).confluxLeviathan$managed(true);
        NativeDamageContexts.bind(child,cast,NativeDamageContexts.amplification(parent));
        ((LeviathanPortal)parent).confluxLeviathan$child(child);
    }
    public static Explosion explosion(Entity payload,Level world,double x,double y,double z,float radius) {
        // Explicit source credit is required for stationary entities as well as projectiles.
        return GuardianCasting.explosion(payload,world,x,y,z,radius);
    }
    private LeviathanCasting() {}
}
