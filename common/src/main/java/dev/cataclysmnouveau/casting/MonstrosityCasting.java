package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.MonstrosityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

/** Native Boss attacks use detached invocation frames; Conflux owns all delayed executions. */
public final class MonstrosityCasting {
    public static boolean supports(Ability ability) {
        return switch(ability) {case EARTHQUAKE,FISSURE,FLAME_RING,SHOULDER_CHECK,LAVA_ABSORPTION,MONSTROUS,BONE_FRACTURE,LAVA_WALKING->true;default->false;};
    }
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.caster()==null || !cast.caster().isAlive()) return false;
        if(ability==Ability.BONE_FRACTURE || ability==Ability.LAVA_WALKING) {
            if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                    || !target.isAlive()) return false;
            boolean allied=Allies.areAllies(cast.caster(),target);
            if(ability==Ability.BONE_FRACTURE) {
                if(allied) return false;
                return target.addEffect(new MobEffectInstance(ModEffect.EFFECTBONE_FRACTURE,
                        scaledTicks(CataclysmConfig.fractureDuration(),duration),Math.max(0,Math.min(10,(int)Math.floor(amp))),false,true),cast.caster());
            }
            if(!allied) return false;
            return target.addEffect(new MobEffectInstance(CataclysmEffects.LAVA_WALKING,
                    scaledTicks(CataclysmConfig.lavaWalkingDuration(),duration),0,false,true),cast.caster());
        }
        if(ability==Ability.MONSTROUS) {
            if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                    || !Allies.areAllies(cast.caster(),target)) return false;
            return target.addEffect(new MobEffectInstance(ModEffect.EFFECTMONSTROUS,
                    scaledTicks(200,duration),Math.max(0,Math.min(10,(int)Math.floor(amp))),false,true));
        }
        if(ability==Ability.SHOULDER_CHECK) return charge(world,cast,amp,duration);
        Vec3 point=NativePayloads.groundPoint(world,cast);
        if(point==null) return false;
        Vec3 direction=direction(cast);
        var boss=new SpellMonstrosity(world,cast,point,direction,amp);
        var nativeAccess=(MonstrosityAccess)(Object)boss;
        return switch(ability) {
            case EARTHQUAKE -> {
                nativeAccess.confluxCataclysm$quake(CataclysmConfig.quakeRadius());
                impactVisual(world,point,boss.getIsBerserk(),true);yield true;
            }
            case FLAME_RING -> {
                if(!loadedArea(world,point,boss.getIsBerserk()?15:10)) yield false;
                nativeAccess.confluxCataclysm$rings(-.3F,3.4F,boss.getIsBerserk()?14:7,boss.getIsBerserk()?8:4,3);
                nativeAccess.confluxCataclysm$rings(-.3F,-3.4F,boss.getIsBerserk()?14:7,boss.getIsBerserk()?8:4,3);
                impactVisual(world,point,true,true);yield true;
            }
            case FISSURE -> {
                if(!loadedArea(world,point,18)) yield false;
                yield scheduled(world,cast,ability,14,new ExecutionDriver<SpellMonstrosity>() {
                    private int age;
                    public boolean start(ExecutionSession<SpellMonstrosity> s) {impactVisual(world,point,boss.getIsBerserk(),true);return true;}
                    public boolean tick(ExecutionSession<SpellMonstrosity> s) {
                        if(!loadedArea(world,point,18)) return true;
                        int stage=age++;
                        if(stage>=0 && stage<12 && stage%2==0) {
                            int distance=4+stage;
                            for(int offset=0;offset<2;offset++) nativeAccess.confluxCataclysm$stomp(.6F,distance+offset,5,1.05F,-2,0,0,.7F);
                            world.playSound(null,point.x,point.y,point.z,ModSounds.REMNANT_STOMP.get(),SoundSource.PLAYERS,.6F,1);
                        }
                        return age>=12;
                    }
                    public void close(ExecutionSession<SpellMonstrosity> s,ExecutionSession.EndReason reason) {}
                },boss);
            }
            case LAVA_ABSORPTION -> {
                if(!hasLava(world,point,4,1)) yield false;
                yield scheduled(world,cast,ability,8,new ExecutionDriver<SpellMonstrosity>() {
                    private long started;
                    public boolean start(ExecutionSession<SpellMonstrosity> s) {started=world.getGameTime();absorb(0);return true;}
                    private void absorb(int phase) {
                        int radius=4<<phase;boss.absorbed=0;
                        nativeAccess.confluxCataclysm$absorb(radius,1<<phase,radius);
                        if(boss.absorbed>0) cast.caster().heal(Math.max(0,CataclysmConfig.absorptionHeal()+Resources.healingBonus(boss.amplification)));
                        world.playSound(null,point.x,point.y,point.z,SoundEvents.BUCKET_FILL_LAVA,SoundSource.PLAYERS,1.5F,.5F);
                        impactVisual(world,point,true,false);
                    }
                    public boolean tick(ExecutionSession<SpellMonstrosity> s) {
                        int age=(int)(world.getGameTime()-started);
                        if(age==2 || age==4) absorb(age/2);
                        return age>=5;
                    }
                    public void close(ExecutionSession<SpellMonstrosity> s,ExecutionSession.EndReason reason) {}
                },boss);
            }
            default -> false;
        };
    }
    private static boolean charge(ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target() instanceof BlockHitResult && BodyPhantomAnimations.kind(Ability.SHOULDER_CHECK)!=null)
            return PositionBodyActions.start(Ability.SHOULDER_CHECK,world,cast,amp,duration);
        // An allied entity can perform the dash. An enemy/block selects the caster's dash direction.
        LivingEntity actor=cast.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target
                && Allies.areAllies(cast.caster(),target)?target:cast.caster();
        if(actor.isPassenger() || !actor.isAlive() || actor.level()!=world || !world.hasChunkAt(actor.blockPosition())) return false;
        Vec3 heading=actor==cast.caster()?direction(cast):actor.getLookAngle();
        heading=heading.multiply(1,0,1).normalize();
        if(heading.lengthSqr()<.01) return false;
        final Vec3 forward=heading;int ticks=scaledTicks(30,duration);
        var frame=cast.withExecutor(actor);
        var boss=new SpellMonstrosity(world,frame,actor.position(),forward,amp);
        return scheduled(world,frame,Ability.SHOULDER_CHECK,ticks+2,new ExecutionDriver<SpellMonstrosity>() {
            private int age;private boolean started;
            public boolean occupiesCaster() {return true;}
            public java.util.List<ExecutionClaim> claims(ExecutionSession<SpellMonstrosity> s){return java.util.List.of(ExecutionClaim.action(actor));}
            public boolean start(ExecutionSession<SpellMonstrosity> s) {
                started=true;boss.setAttackState(8);impactVisual(world,actor.position(),false,false);return true;
            }
            public boolean tick(ExecutionSession<SpellMonstrosity> s) {
                if(age++>=ticks || actor.horizontalCollision || !world.hasChunkAt(actor.blockPosition())) return true;
                actor.setDeltaMovement(forward.scale(CataclysmConfig.chargeSpeed()).add(0,actor.getDeltaMovement().y,0));
                actor.hurtMarked=true;
                boss.setPos(actor.position());boss.setBoundingBox(actor.getBoundingBox());
                boss.attackTicks=20+(age-1)%29;
                // Run only the native shoulder branch: the mixin suppresses superclass mob ticking.
                boss.aiStep();
                if(age%5==0) impactVisual(world,actor.position(),false,false);
                return false;
            }
            public void close(ExecutionSession<SpellMonstrosity> s,ExecutionSession.EndReason reason) {
                if(started && actor.level()==world && !actor.isRemoved()) {actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;}boss.discard();
            }
        },boss);
    }
    public static Vec3 direction(CastContext cast) {
        Vec3 direction=cast.incomingDirection();
        if(direction==null || direction.lengthSqr()<1e-8) {
            direction=cast.target() instanceof EntityHitResult h && h.getEntity()==cast.caster()
                    ?cast.caster().getLookAngle():cast.origin().subtract(cast.caster().getEyePosition());
        }
        return direction.lengthSqr()<1e-8?cast.caster().getLookAngle():direction.normalize();
    }
    public static int scaledTicks(int ticks,double duration) {return (int)Math.max(2,Math.min(1200,Math.round(ticks*Math.max(.1,1+.5*duration))));}
    private static boolean scheduled(ServerLevel world,CastContext cast,Ability ability,int ticks,ExecutionDriver<SpellMonstrosity> driver,SpellMonstrosity boss) {
        var session=new ExecutionSession<>(world,cast,boss,ticks,ability==Ability.SHOULDER_CHECK?dev.cataclysmnouveau.network.BodyPhantomSync.wrap(ability,boss,driver):driver,hit->{},
                ()->GlyphCatalog.ALL.stream().filter(g->g instanceof dev.cataclysmnouveau.glyph.CataclysmEffectGlyph e && e.ability==ability).allMatch(g->g.isEnabled()),ExecutionScope.DIRECT);
        return ability==Ability.SHOULDER_CHECK?ExecutionSessions.startReplacing(session):ExecutionSessions.start(session);
    }
    public static boolean loadedArea(ServerLevel world,Vec3 point,int radius) {
        var min=BlockPos.containing(point).offset(-radius,0,-radius);var max=BlockPos.containing(point).offset(radius,0,radius);
        for(int x=min.getX()>>4;x<=max.getX()>>4;x++) for(int z=min.getZ()>>4;z<=max.getZ()>>4;z++)
            if(!world.hasChunk(x,z)) return false;
        return true;
    }
    private static boolean hasLava(ServerLevel world,Vec3 point,int radius,int height) {
        var origin=BlockPos.containing(point);
        for(var p:BlockPos.betweenClosed(origin.offset(-radius,0,-radius),origin.offset(radius,height,radius)))
            if(world.hasChunkAt(p) && world.getBlockState(p).is(net.minecraft.world.level.block.Blocks.LAVA)) return true;
        return false;
    }
    /** Reuse installed particle types and sounds; send from the server so clients actually see them. */
    public static void impactVisual(ServerLevel world,Vec3 point,boolean fire,boolean shake) {
        var under=BlockPos.containing(point).below();
        if(world.hasChunkAt(under)) world.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,world.getBlockState(under)),
                point.x,point.y+.1,point.z,30,1.5,.1,1.5,.08);
        world.sendParticles(new RingParticleOptions(0,(float)Math.PI/2,35,fire?204:255,fire?78:255,fire?5:255,1,30,false,0),
                point.x,point.y+.2,point.z,1,0,0,0,0);
        if(fire) world.sendParticles(ParticleTypes.FLAME,point.x,point.y+.2,point.z,20,1,.1,1,.03);
        if(shake) ScreenShake_Entity.ScreenShake(world,point,20,.15F,0,12);
    }
    private MonstrosityCasting() {}
}
