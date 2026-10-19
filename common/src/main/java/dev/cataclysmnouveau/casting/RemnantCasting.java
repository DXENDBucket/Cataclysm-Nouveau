package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ancient_Desert_Stele_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.mixin.RemnantMonolithAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.function.*;

/** Native branches create storms, steles and rising ground blocks; no copied attack pattern. */
public final class RemnantCasting {
    private static final ThreadLocal<Function<Entity,Boolean>> BIRTH=new ThreadLocal<>();
    public static void invoke(Function<Entity,Boolean> birth,Runnable call){
        var previous=BIRTH.get();BIRTH.set(birth);
        try{call.run();}finally{if(previous==null)BIRTH.remove();else BIRTH.set(previous);}
    }
    public static boolean spawn(Entity child,BooleanSupplier original){
        var birth=BIRTH.get();return birth==null?original.getAsBoolean():birth.apply(child);
    }
    public static boolean supports(Ability a){return a==Ability.DESERT_STELE_ARRAY || a==Ability.REMNANT_TRIPLE_QUAKE || a==Ability.SANDSTORM_RING || a==Ability.DESERT_STELE || a==Ability.REMNANT_STOMP;}
    public static boolean enabled(Ability a){return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==a && e.isEnabled());}
    public static boolean rejectDamage(Entity source,Entity target){
        if(source.level().isClientSide || !(source instanceof Sandstorm_Entity || source instanceof Ancient_Desert_Stele_Entity))return false;
        var cast=NativeDamageContexts.context(source);return cast!=null && HarbingerCasting.allied(cast,target);
    }
    private static boolean loaded(ServerLevel w,Vec3 p){return w.isInWorldBounds(BlockPos.containing(p)) && w.hasChunkAt(BlockPos.containing(p));}
    public static boolean effect(Ability a,ServerLevel world,CastContext cast,double amp,double duration) {
        if(a==Ability.DESERT_STELE_ARRAY || a==Ability.REMNANT_TRIPLE_QUAKE)return RemnantFormations.start(a,world,cast,amp);
        if(a==Ability.SANDSTORM_RING)return storms(world,cast,amp,duration);
        if(a==Ability.DESERT_STELE)return stele(world,cast,amp);
        if(a==Ability.REMNANT_STOMP)return stomp(world,cast,amp);
        return false;
    }
    static boolean add(ServerLevel world,SpellRemnant frame,Entity child,double amp) {
        if(!loaded(world,child.position())){child.discard();return false;}
        var owner=frame.cast.damageOwner()==null?frame.cast.caster():frame.cast.damageOwner();
        if(child instanceof com.github.L_Ender.cataclysm.entity.projectile.EarthQuake_Entity quake){
            quake.setOwner(owner);var state=(ManagedProjectile)quake;state.confluxCataclysm$mode(2);state.confluxCataclysm$sourceAbility(frame.ability);
        }
        if(child instanceof Sandstorm_Entity storm){storm.setCaster(owner);((RemnantStorm)storm).confluxRemnant$frame(frame);}
        if(child instanceof Ancient_Desert_Stele_Entity stele)stele.setCaster(owner);
        NativeDamageContexts.bind(child,frame.cast,amp);
        if(!world.addFreshEntity(child)){child.discard();return false;}frame.children.add(child);return true;
    }
    private static boolean storms(ServerLevel world,CastContext cast,double amp,double duration) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),10))return false;
        var context=cast.withExecutor(anchor.entity());var pose=anchor.sample();
        var frame=new SpellRemnant(world,context,Ability.SANDSTORM_RING,pose.origin(),pose.direction(),amp);
        int life=MonstrosityCasting.scaledTicks(CataclysmConfig.remnantStormLifetime(),duration);
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,life+2,new ExecutionDriver<SpellRemnant>() {
            int age;
            public boolean start(ExecutionSession<SpellRemnant> s){
                frame.setAttackState(6);frame.attackTicks=55;
                invoke(e->{if(!(e instanceof Sandstorm_Entity storm))return false;storm.setLifespan(life);((RemnantStorm)storm).confluxRemnant$lifetime(life);return add(world,frame,e,amp);},frame::aiStep);
                return frame.children.size()==3;
            }
            public boolean tick(ExecutionSession<SpellRemnant> s){
                if(!anchor.valid(world,context) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),10))return true;
                var sample=anchor.sample();frame.pose(sample.origin(),sample.direction());
                return ++age>=life || frame.children.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<SpellRemnant> s,ExecutionSession.EndReason reason){frame.clear();}
        },hit->{},()->enabled(Ability.SANDSTORM_RING),ExecutionScope.DIRECT));
    }
    private static boolean stele(ServerLevel world,CastContext cast,double amp) {
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        Vec3 point=cast.target() instanceof EntityHitResult hit?hit.getEntity().position():cast.origin();
        if(!loaded(world,point) || !loaded(world,point.add(0,20,0)))return false;
        var frame=new SpellRemnant(world,cast,Ability.DESERT_STELE,point,MonstrosityCasting.direction(cast),amp);
        var access=(RemnantMonolithAccess)frame.monolithGoal();
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,frame,100,new ExecutionDriver<SpellRemnant>() {
            public boolean start(ExecutionSession<SpellRemnant> s){
                invoke(e->e instanceof Ancient_Desert_Stele_Entity && add(world,frame,e,amp),
                    ()->access.confluxRemnant$stele(point.x,point.z,point.y,frame.getYRot()*(float)Math.PI/180,10));
                return !frame.children.isEmpty();
            }
            public boolean tick(ExecutionSession<SpellRemnant> s){
                for(var e:frame.children)if(!e.isRemoved() && !loaded(world,e.position()))e.discard();
                return frame.children.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<SpellRemnant> s,ExecutionSession.EndReason reason){frame.clear();}
        },hit->{},()->enabled(Ability.DESERT_STELE),ExecutionScope.DIRECT));
    }
    private static boolean stomp(ServerLevel world,CastContext cast,double amp) {
        Vec3 point=NativePayloads.groundPoint(world,cast);
        if(point==null || !MonstrosityCasting.loadedArea(world,point,22))return false;
        var friendly=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        var anchor=friendly==null?ExecutionAnchor.position(point,MonstrosityCasting.direction(cast)):friendly;
        var context=friendly==null?cast:cast.withExecutor(anchor.entity());
        var frame=new SpellRemnant(world,context,Ability.REMNANT_STOMP,point,anchor.sample().direction(),amp);
        frame.setAttackState(8);
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,65,new ExecutionDriver<SpellRemnant>() {
            int age;
            public boolean start(ExecutionSession<SpellRemnant> s){return true;}
            public boolean tick(ExecutionSession<SpellRemnant> s){
                if(!anchor.valid(world,context) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),22))return true;
                var sample=anchor.sample();frame.pose(sample.origin(),sample.direction());frame.attackTicks=++age;
                if(age<=46)invoke(e->add(world,frame,e,amp),frame::aiStep);
                return age>=62;
            }
            public void close(ExecutionSession<SpellRemnant> s,ExecutionSession.EndReason reason){frame.clear();}
        },hit->{},()->enabled(Ability.REMNANT_STOMP),ExecutionScope.DIRECT));
    }
    private RemnantCasting(){}
}
