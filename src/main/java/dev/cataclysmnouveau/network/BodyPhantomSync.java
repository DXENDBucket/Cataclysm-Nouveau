package dev.cataclysmnouveau.network;

import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** Samples the real invocation frame after its driver, without changing attacks or claims. */
public final class BodyPhantomSync {
    public static BodyPhantomPayload snapshot(UUID token, Ability ability, LivingEntity actor, LivingEntity frame,
                                              int elapsed, boolean closed) {
        int state=0,age=0,flags=0;
        switch(frame) {
            case SpellAptrgangr boss -> {state=boss.getAttackState();age=boss.attackTicks;}
            case SpellRevenant boss -> {state=1;age=boss.getAnimationTick();}
            case SpellRemnant boss -> {state=boss.getAttackState();age=boss.attackTicks;flags=boss.getIsPower()?1:0;}
            case SpellMaledictus boss -> {state=boss.getAttackState();age=boss.attackTicks;flags=boss.getRageMeter();}
            case SpellScylla boss -> {
                state=boss.getAttackState();age=boss.attackTicks;flags=(boss.getEye()?1:0)|(boss.isFlying()?2:0);
                // The native jump goal is sampled once by the existing action. Its render clock still advances.
                if(ability==Ability.ANCHOR_LEAP && state==15) age=9+elapsed;
            }
            case SpellGuardian boss -> {
                state=BodyPhantomAnimations.id(BodyPhantomAnimations.Kind.GUARDIAN,boss.getAnimation());
                age=boss.getAnimationTick();flags=boss.getIsHelmetless()?1:0;
            }
            case SpellHarbinger boss -> {
                state=BodyPhantomAnimations.id(BodyPhantomAnimations.Kind.HARBINGER,boss.getAnimation());
                age=boss.getAnimationTick();flags=(boss.isPowered()?1:0)|(boss.getIsCharge()?2:0);
            }
            case SpellMonstrosity boss -> { state=boss.getAttackState();age=Math.max(20,boss.attackTicks);flags=boss.getIsBerserk()?1:0; }
            default -> throw new IllegalArgumentException("Unsupported phantom frame "+frame.getClass().getName());
        }
        return new BodyPhantomPayload(token,frame.level().dimension().location(),actor==null?-1:actor.getId(),actor==null?new UUID(0,0):actor.getUUID(),actor==null,
            actor!=null && actor.level()==frame.level() && !actor.isRemoved()?actor.position():frame.position(),
            frame.getYRot(),frame.getXRot(),ability.id,state,age,flags,closed);
    }
    public static <P extends LivingEntity> ExecutionDriver<P> wrap(Ability ability,P frame,ExecutionDriver<P> original) {
        if(BodyPhantomAnimations.kind(ability)==null) throw new IllegalArgumentException("Unsupported phantom ability "+ability);
        return new ExecutionDriver<>() {
            private boolean started;
            private int elapsed;
            private BodyPhantomPayload last;
            private void send(ExecutionSession<P> s,boolean closed) {
                var actor=s.context().executor();
                var data=snapshot(s.id(),ability,actor,frame,elapsed,closed);
                if(!data.valid()) {
                    if(!closed || last==null)return;
                    data=new BodyPhantomPayload(last.token(),last.dimension(),last.actorId(),last.actorUuid(),last.positionAnchored(),
                        last.position(),last.yaw(),last.pitch(),last.ability(),last.animation(),last.age(),last.flags(),true);
                }
                boolean changed=last==null || data.animation()!=last.animation() || data.flags()!=last.flags()
                    || data.age()<last.age();
                if(!closed && elapsed%4!=0 && !changed)return;
                if(closed || actor==null) {
                    // Actor removal/transfer can remove the tracking entry before cancellation.
                    ServerPlayer self=actor instanceof ServerPlayer p && p.level()==s.world()?p:actor==null && s.caster() instanceof ServerPlayer p?p:null;
                    var point=last==null?data.position():last.position();
                    PacketDistributor.sendToPlayersNear(s.world(),self,point.x,point.y,point.z,96,data);
                    if(self!=null)PacketDistributor.sendToPlayer(self,data);
                } else PacketDistributor.sendToPlayersTrackingEntityAndSelf(actor,data);
                last=data;
            }
            public boolean start(ExecutionSession<P> s) {
                started=original.start(s);if(started)send(s,false);return started;
            }
            public boolean tick(ExecutionSession<P> s) {
                ++elapsed;boolean done=original.tick(s);send(s,false);return done;
            }
            public void close(ExecutionSession<P> s,ExecutionSession.EndReason reason) {
                try{if(started)send(s,true);}finally{original.close(s,reason);}
            }
            public boolean occupiesCaster(){return original.occupiesCaster();}
            public List<ExecutionClaim> claims(ExecutionSession<P> s){return original.claims(s);}
            public Vec3 incomingDirection(){return original.incomingDirection();}
        };
    }
    private BodyPhantomSync() {}
}
