package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.client.particle.Options.RoarParticleOptions;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Native formation/goal code supplies the payloads; shared anchors and ray drivers own execution. */
public final class LeviathanVolleys {
    private interface Birth {boolean spawn(Level world,Entity child,BooleanSupplier original);}
    private static final ThreadLocal<Birth> BIRTH=new ThreadLocal<>();
    private static void invoke(Birth birth,Runnable nativeCall) {
        var previous=BIRTH.get();BIRTH.set(birth);
        try{nativeCall.run();}finally{if(previous==null)BIRTH.remove();else BIRTH.set(previous);}
    }
    public static boolean spawn(Level world,Entity child,BooleanSupplier original) {
        var birth=BIRTH.get();return birth==null?original.getAsBoolean():birth.spawn(world,child,original);
    }
    /** Entity-attached sounds cannot resolve an unregistered invocation frame on a client. */
    public static boolean sound(Level world,Player except,Entity emitter,SoundEvent sound,SoundSource source,float volume,float pitch) {
        if(!(emitter instanceof SpellLeviathan))return false;
        world.playSound(except,emitter.getX(),emitter.getY(),emitter.getZ(),sound,source,volume,pitch);return true;
    }
    private static boolean loaded(ServerLevel world,Vec3 point){return world.isInWorldBounds(BlockPos.containing(point)) && world.hasChunkAt(BlockPos.containing(point));}
    private static void pose(SpellLeviathan frame,Vec3 point,Vec3 facing) {
        frame.setPos(point);frame.setYRot((float)Math.toDegrees(Math.atan2(-facing.x,facing.z)));
        frame.yRotO=frame.yBodyRot=frame.yHeadRot=frame.yHeadRotO=frame.getYRot();
        frame.setXRot((float)-Math.toDegrees(Math.atan2(facing.y,facing.horizontalDistance())));frame.xRotO=frame.getXRot();
    }
    private static void prepare(ServerLevel world,Vec3 point) {
        world.sendParticles(ParticleTypes.PORTAL,point.x,point.y,point.z,10,.35,.35,.35,.15);
    }
    private static void roar(ServerLevel world,Vec3 point,float size) {
        world.sendParticles(new RoarParticleOptions(60,102,26,204,.9F,1,1.2F,size),point.x,point.y,point.z,1,0,0,0,0);
    }
    public static boolean orbs(ServerLevel world,CastContext cast,double amp) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.EYES,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !loaded(world,anchor.sample().origin()))return false;
        var context=cast.withExecutor(anchor.entity());var frame=new SpellLeviathan(world,context,Ability.ABYSS_ORB_ARRAY,anchor.sample().origin(),amp,0);
        boolean dense=cast.caster().getHealth()<=cast.caster().getMaxHealth()*.5F;
        frame.setMeltDown(!dense);frame.setAnimation(dense?The_Leviathan_Entity.LEVIATHAN_PHASE2:The_Leviathan_Entity.LEVIATHAN_STUN);
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,540,new ExecutionDriver<SpellLeviathan>() {
            int age;
            public boolean start(ExecutionSession<SpellLeviathan> s){prepare(world,anchor.sample().origin());return true;}
            private boolean child(Level unused,Entity entity,BooleanSupplier original) {
                if(!(entity instanceof Abyss_Orb_Entity orb) || frame.childrenCancelled)return false;
                var point=anchor.sample().origin();if(!loaded(world,point)){orb.discard();return false;}
                orb.setOwner(context.damageOwner()==null?context.caster():context.damageOwner());orb.setPos(point);
                var state=(ManagedProjectile)orb;state.confluxCataclysm$mode(2);state.confluxCataclysm$sourceAbility(Ability.ABYSS_ORB_ARRAY);
                NativeDamageContexts.bind(orb,context,amp);
                boolean accepted=original.getAsBoolean();if(accepted)frame.children.add(orb);else orb.discard();return accepted;
            }
            public boolean tick(ExecutionSession<SpellLeviathan> s) {
                if(!anchor.valid(world,context))return true;
                ++age;var sample=anchor.sample();pose(frame,sample.origin(),sample.direction());
                if(age<20 && age%4==0)prepare(world,sample.origin());
                if(age==20) {
                    // These are complete native twelve-/twenty-four-orb bursts, not copied direction tables.
                    var target=HarbingerCasting.target(world,sample.origin(),sample.direction(),context);
                    frame.setTarget(target);frame.setAnimationTick(dense?90:52);
                    invoke(this::child,frame::aiStep);roar(world,sample.origin(),dense?13:8);
                    if(frame.children.isEmpty())return true;
                }
                if(age==23 || age==26)roar(world,sample.origin(),dense?(age==23?14:15):(age==23?9:10));
                for(var child:frame.children)if(!child.isRemoved() && (!loaded(world,child.position()) || !loaded(world,child.position().add(child.getDeltaMovement()))))child.discard();
                return age>=535 || age>26 && frame.children.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<SpellLeviathan> s,ExecutionSession.EndReason reason){frame.clearChildren();frame.discard();}
        },hit->{},()->LeviathanCasting.enabled(Ability.ABYSS_ORB_ARRAY),ExecutionScope.DIRECT));
    }
    public static boolean triple(ServerLevel world,CastContext cast,double amp,double duration,ResourceCharge charge) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.EYES,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !loaded(world,anchor.sample().origin()) || charge.affordableCount(1)==0)return false;
        var context=cast.withExecutor(anchor.entity()).withAccount(charge.account());
        var frame=new SpellLeviathan(world,context,Ability.ABYSS_TRIPLE_BLAST,anchor.sample().origin(),amp,duration);
        frame.setAnimation(The_Leviathan_Entity.LEVIATHAN_ABYSS_BLAST_FIRE);
        var goal=frame.nativeGoal();var marker=new ArmorStand(world,0,0,0);frame.setTarget(marker);
        int output=MonstrosityCasting.scaledTicks(28,duration);
        class Shot {final int freeze;Vec3 direction;Shot(int freeze,Vec3 direction){this.freeze=freeze;this.direction=direction;}}
        var shots=new ArrayList<Shot>();
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,154+output+64,new ExecutionDriver<SpellLeviathan>() {
            int age;Vec3 facing=anchor.sample().direction();boolean started;
            private void update() {
                var point=anchor.sample().origin();pose(frame,point,facing);marker.setPos(point.add(facing.scale(24)));
            }
            public boolean start(ExecutionSession<SpellLeviathan> s){started=true;update();goal.start();return true;}
            private boolean child(Level unused,Entity entity,BooleanSupplier original) {
                if(!(entity instanceof Abyss_Blast_Entity beam) || frame.childrenCancelled)return false;
                beam.setDuration(output);var shot=new Shot(age+16,facing);shots.add(shot);
                var session=LeviathanLaser.attach(world,context,anchor,beam,amp,charge,()->shot.direction,
                    ()->!frame.childrenCancelled && LeviathanCasting.enabled(Ability.ABYSS_TRIPLE_BLAST),()->frame.childrenCancelled=true,false);
                boolean accepted=ExecutionSessions.start(session);
                if(accepted)frame.children.add(beam);else{frame.childrenCancelled=true;beam.discard();}
                return accepted;
            }
            public boolean tick(ExecutionSession<SpellLeviathan> s) {
                if(frame.childrenCancelled || !anchor.valid(world,context))return true;
                ++age;
                // Native goal aim windows: let each beam settle, then retain its own locked heading.
                boolean aiming=age<80 || age>118 && age<125 || age>163 && age<170;
                if(aiming) {
                    facing=anchor.sample().direction();for(var shot:shots)if(age<shot.freeze)shot.direction=facing;
                }
                update();frame.setAnimationTick(age);invoke(this::child,goal::tick);
                if(aiming && age%4==0)prepare(world,anchor.sample().origin());
                return age>154 && frame.children.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<SpellLeviathan> s,ExecutionSession.EndReason reason){
                frame.clearChildren();if(started)goal.stop();marker.discard();frame.discard();
            }
        },hit->{},()->LeviathanCasting.enabled(Ability.ABYSS_TRIPLE_BLAST),ExecutionScope.DIRECT));
    }
    private LeviathanVolleys(){}
}
