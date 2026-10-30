package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.mixin.HarbingerAttackAccess;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;

/** Fixed native attack origin. It is neither a surrogate world entity nor the caster's body. */
final class PositionBodyActions {
    static boolean fixed(CastContext cast) {
        return cast.executor()==null && cast.target() instanceof BlockHitResult hit && hit.getType()==HitResult.Type.BLOCK;
    }
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || anchor.entity()!=null || BodyPhantomAnimations.kind(ability)==null)return false;
        var frame=cast.withExecutor(null);
        Vec3 point=anchor.sample().origin();
        Vec3 heading=ActionGeometry.horizontal(anchor.sample().direction(),ExecutionAnchor.viewDirection(cast.caster()));
        int area=switch(BodyPhantomAnimations.kind(ability)){case SCYLLA -> 14;case GUARDIAN -> 18;default -> 12;};
        if(!anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,point,area))return false;
        LivingEntity boss=switch(BodyPhantomAnimations.kind(ability)) {
            case SCYLLA -> new SpellScylla(world,frame,point,Resources.amplification(amp));
            case GUARDIAN -> new SpellGuardian(world,frame,point,heading,amp,ability);
            case HARBINGER -> new SpellHarbinger(world,frame,ability,point,0,amp);
            case MONSTROSITY -> new SpellMonstrosity(world,frame,point,heading,amp);
            case PRIEST -> throw new IllegalArgumentException("Priest uses its shared area driver");
            case BERSERKER -> throw new IllegalArgumentException("Berserker uses its shared native driver");
            case APTRGANGR -> throw new IllegalArgumentException("Aptrgangr uses its shared smash driver");
            case REVENANT -> throw new IllegalArgumentException("Revenant uses its native formation driver");
            case REMNANT -> throw new IllegalArgumentException("Remnant uses its shared formation driver");
            case MALEDICTUS -> throw new IllegalArgumentException("Maledictus uses its shared anchor driver");
        };
        // A private, unregistered aim marker freezes direction even if the caster turns or walks away.
        var aim=new ArmorStand(world,point.x+heading.x*7,point.y,point.z+heading.z*7);
        Goal goal=null;
        int ticks;
        switch(boss) {
            case SpellScylla b -> {
                b.setTarget(aim);
                if(ability==Ability.THUNDER_COMBO){goal=b.nativeGoal("WhipAndSpearGoal");ticks=105;}
                else if(ability==Ability.ANCHOR_LEAP){goal=b.nativeGoal("Scylla_Flying");ticks=76;}
                else {b.setAttackState(7);ticks=52;}
            }
            case SpellGuardian b -> {
                b.setTarget(aim);
                var animation=switch(ability) {
                    case GRAVITY_PUNCH -> Ender_Guardian_Entity.GUARDIAN_RIGHT_STRONG_ATTACK;
                    case GUARDIAN_UPPERCUT -> b.getIsHelmetless()?Ender_Guardian_Entity.GUARDIAN_RAGE_UPPERCUT:Ender_Guardian_Entity.GUARDIAN_UPPERCUT_AND_BULLET;
                    case GUARDIAN_BLINK -> Ender_Guardian_Entity.GUARDIAN_HUG_ME;
                    case GUARDIAN_AIR_STRIKE -> Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE1;
                    default -> Ender_Guardian_Entity.GUARDIAN_ROCKETPUNCH;
                };
                b.setAnimation(animation);
                if(ability!=Ability.GRAVITY_PUNCH)goal=b.nativeGoal(switch(ability) {
                    case GUARDIAN_UPPERCUT -> b.getIsHelmetless()?"RageUppercut":"UppercutAndBulletGoal";
                    case GUARDIAN_BLINK -> "HugmeGoal";
                    case GUARDIAN_AIR_STRIKE -> "TeleportStrikeGoal";
                    default -> "RocketPunchGoal";
                });
                ticks=ability==Ability.GUARDIAN_AIR_STRIKE?100:animation.getDuration();
            }
            case SpellHarbinger b -> {b.setTarget(aim);b.setAnimation(The_Harbinger_Entity.CHARGE_ANIMATION);goal=b.nativeGoal("ChargeGoal");ticks=138;}
            case SpellMonstrosity b -> {b.setAttackState(8);ticks=MonstrosityCasting.scaledTicks(30,duration);}
            default -> throw new IllegalArgumentException("Unsupported position action");
        }
        final Goal nativeGoal=goal;final int lifetime=ticks;
        var driver=new ExecutionDriver<LivingEntity>() {
            private int age,phaseAge;
            private boolean started,stopped,landing;
            private void pose() {
                boss.setPos(point);boss.setYRot((float)Math.toDegrees(Math.atan2(-heading.x,heading.z)));
                boss.yRotO=boss.yBodyRot=boss.yHeadRot=boss.getYRot();
                boss.setBoundingBox(new AABB(point.x-.3,point.y,point.z-.3,point.x+.3,point.y+1.8,point.z+.3));
                boss.setOnGround(true);boss.setDeltaMovement(Vec3.ZERO);
            }
            public boolean start(ExecutionSession<LivingEntity> s) {
                started=true;pose();if(nativeGoal!=null)nativeGoal.start();
                if(boss instanceof SpellScylla b && ability==Ability.ANCHOR_LEAP)b.attackTicks=9;
                if(boss instanceof SpellMonstrosity b)b.attackTicks=20;
                pose();return true;
            }
            public boolean tick(ExecutionSession<LivingEntity> s) {
                if(!anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,point,area))return true;
                ++age;++phaseAge;pose();
                switch(boss) {
                    case SpellScylla b -> {
                        b.attackTicks=age;
                        if(ability==Ability.THUNDER_COMBO) {
                            nativeGoal.tick();pose();
                            if(age==21 || age==104)b.aiStep();
                            ScyllaActionVisuals.tick(world,b,null,false,age);
                        } else if(ability==Ability.ANCHOR_SPIN) {
                            if(age>=12 && age<=52)b.aiStep();
                            ScyllaActionVisuals.tick(world,b,null,true,age);
                        } else {
                            // A fixed origin has no body to hook/pull. Finish the native preparation, then strike here.
                            b.attackTicks=9+age;
                            if(age>=70 && !landing) {
                                landing=true;b.setAttackState(17);b.attackTicks=1;b.aiStep();
                                ScyllaActionVisuals.landing(world,point);
                            } else if(landing)b.attackTicks=age-69;
                        }
                    }
                    case SpellGuardian b -> {
                        b.setAnimationTick(phaseAge);
                        if(!landing && nativeGoal!=null)nativeGoal.tick();
                        pose();
                        if(ability==Ability.GUARDIAN_AIR_STRIKE && !landing
                                && b.getAnimation()==Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2) {
                            landing=true;phaseAge=0;b.setAnimationTick(0);return false;
                        }
                        b.aiStep();
                        boolean strike=landing?phaseAge==3:switch(ability) {
                            case GRAVITY_PUNCH -> phaseAge==29;
                            case GUARDIAN_UPPERCUT -> phaseAge==27 || b.getIsHelmetless() && phaseAge==84;
                            case GUARDIAN_BLINK -> phaseAge==38;
                            case GUARDIAN_ROCKET_PUNCH -> phaseAge==28;
                            default -> false;
                        };
                        if(strike)MonstrosityCasting.impactVisual(world,point.add(heading.scale(landing?0:2)),false,true);
                        if(landing)return phaseAge>=Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2.getDuration();
                    }
                    case SpellHarbinger b -> {
                        if(phaseAge>=45) {
                            b.repeatRequested=false;b.acceptingRepeat=true;
                            try{nativeGoal.stop();}finally{b.acceptingRepeat=false;}stopped=true;
                            if(!b.repeatRequested)return true;
                            phaseAge=0;b.setAnimationTick(0);b.setIsCharge(false);nativeGoal.start();stopped=false;pose();return age>=lifetime;
                        }
                        b.setAnimationTick(phaseAge);b.tickCount=phaseAge;nativeGoal.tick();pose();
                        if(b.getIsCharge())((HarbingerAttackAccess)(Object)b).confluxHarbinger$collision();
                    }
                    case SpellMonstrosity b -> {b.attackTicks=20+(age-1)%29;b.aiStep();}
                    default -> throw new IllegalArgumentException("Unsupported position action");
                }
                pose();return age>=lifetime;
            }
            public void close(ExecutionSession<LivingEntity> s,ExecutionSession.EndReason reason) {
                try{if(started && nativeGoal!=null && !stopped)nativeGoal.stop();}
                finally{aim.discard();boss.discard();}
            }
        };
        // Fixed actions have no body claim: another action at another point must remain independent.
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,ticks+5,
            BodyPhantomSync.wrap(ability,boss,driver),hit->{},()->enabled(ability) && anchor.valid(world,cast)
                && MonstrosityCasting.loadedArea(world,point,area),ExecutionScope.DIRECT));
    }
    private static boolean enabled(Ability ability) {
        return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==ability && e.isEnabled());
    }
    private PositionBodyActions() {}
}
