package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ender_Guardian_Bullet_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.EventHooks;

import java.util.*;
import java.util.function.Supplier;

/** Native goals/attack bodies, with movement and teleportation performed by the real actor. */
public final class GuardianActions {
    private static final Set<UUID> ACTORS = new HashSet<>();
    public static boolean isActing(Entity actor) { return ACTORS.contains(actor.getUUID()); }
    public static boolean supports(Ability ability) {
        return switch (ability) {
            case GUARDIAN_UPPERCUT, GUARDIAN_BLINK, GUARDIAN_AIR_STRIKE, GUARDIAN_ROCKET_PUNCH -> true;
            default -> false;
        };
    }
    private static boolean enabled(Ability ability) {
        return GlyphCatalog.ALL.stream().anyMatch(g -> g instanceof CataclysmEffectGlyph e && e.ability == ability && e.isEnabled());
    }

    public static boolean effect(Ability ability, ServerLevel world, CastContext cast, double amplify) {
        if (!supports(ability) || cast.target() == null || cast.target().getType() == HitResult.Type.MISS) return false;
        if(cast.target() instanceof BlockHitResult && BodyPhantomAnimations.kind(ability)!=null)
            return PositionBodyActions.start(ability,world,cast,amplify,0);
        final int footprint = ability == Ability.GUARDIAN_UPPERCUT || ability == Ability.GUARDIAN_AIR_STRIKE ? 18 : 12;
        var geometry=ActionGeometry.entity(world,cast,footprint);if(geometry==null)return false;
        LivingEntity actor=geometry.actor;
        var frame=cast.withExecutor(actor);
        Vec3 initialFacing=geometry.desired(geometry.initialFacing);
        LivingEntity target=geometry.projectileTarget(initialFacing);
        boolean needsEnemy = ability == Ability.GUARDIAN_BLINK || ability == Ability.GUARDIAN_AIR_STRIKE;
        if (target == null) {
            if (needsEnemy) return false;
            Vec3 aim = cast.target() instanceof BlockHitResult block ? block.getLocation()
                : actor.position().add(horizontal(ExecutionAnchor.viewDirection(actor)).scale(7));
            target = new ArmorStand(world, aim.x, aim.y, aim.z);
        }
        if (!validTarget(world, actor, target) || !MonstrosityCasting.loadedArea(world, actor.position(), footprint)) return false;
        var boss = new SpellGuardian(world, frame, actor.position(), initialFacing, amplify, ability);
        boss.setTarget(target);
        if (ability == Ability.GUARDIAN_BLINK && groundDestination(world, actor, target.position()) == null) return false;
        if (ability == Ability.GUARDIAN_AIR_STRIKE
                && !clearDestination(world, actor, target.position().add(0, boss.getIsHelmetless() ? 8 : 4, 0))) return false;
        Animation animation = switch (ability) {
            case GUARDIAN_UPPERCUT -> boss.getIsHelmetless() ? Ender_Guardian_Entity.GUARDIAN_RAGE_UPPERCUT : Ender_Guardian_Entity.GUARDIAN_UPPERCUT_AND_BULLET;
            case GUARDIAN_BLINK -> Ender_Guardian_Entity.GUARDIAN_HUG_ME;
            case GUARDIAN_AIR_STRIKE -> Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE1;
            default -> Ender_Guardian_Entity.GUARDIAN_ROCKETPUNCH;
        };
        boss.setAnimation(animation);
        var goal = boss.nativeGoal(switch (ability) {
            case GUARDIAN_UPPERCUT -> boss.getIsHelmetless() ? "RageUppercut" : "UppercutAndBulletGoal";
            case GUARDIAN_BLINK -> "HugmeGoal";
            case GUARDIAN_AIR_STRIKE -> "TeleportStrikeGoal";
            default -> "RocketPunchGoal";
        });
        final LivingEntity executionActor = actor;
        int end = ability == Ability.GUARDIAN_AIR_STRIKE ? 200 : animation.getDuration();
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world, frame, boss, end + 5, dev.cataclysmnouveau.network.BodyPhantomSync.wrap(ability,boss,new ExecutionDriver<SpellGuardian>() {
            private int age, phaseAge;private boolean started,hadNoGravity;
            private Vec3 facing = initialFacing;
            public boolean occupiesCaster() { return executionActor == cast.caster(); }
            public java.util.List<ExecutionClaim> claims(ExecutionSession<SpellGuardian> s){return java.util.List.of(ExecutionClaim.action(executionActor));}
            public boolean start(ExecutionSession<SpellGuardian> session) {
                hadNoGravity=executionActor.isNoGravity();started=true;ACTORS.add(executionActor.getUUID());
                if (executionActor instanceof Mob mob) mob.getNavigation().stop();
                if (ability == Ability.GUARDIAN_AIR_STRIKE) executionActor.setNoGravity(false);
                goal.start();geometry.pose(boss,facing);
                return true;
            }
            public boolean tick(ExecutionSession<SpellGuardian> session) {
                if(!geometry.present(cast,footprint))return true;
                ++age;
                ++phaseAge;
                boolean landing = boss.getAnimation() == Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2;
                var enemy = boss.getTarget();
                if (!landing && !validTarget(world, executionActor, enemy)) return true;
                boolean tracking = ability == Ability.GUARDIAN_UPPERCUT ? phaseAge < 29 || phaseAge > 54 && phaseAge < 84
                    : ability == Ability.GUARDIAN_ROCKET_PUNCH ? phaseAge < 24 : phaseAge < 40;
                facing=geometry.steer(facing,!landing && tracking);
                if(!landing && tracking) {
                    var aimed=geometry.projectileTarget(facing);
                    if(aimed!=null)boss.setTarget(aimed);
                    else if(!needsEnemy) {
                        if(boss.getTarget() instanceof ArmorStand marker)marker.setPos(geometry.aimPoint(facing));
                        else {var p=geometry.aimPoint(facing);boss.setTarget(new ArmorStand(world,p.x,p.y,p.z));}
                    }
                }
                boss.pose(executionActor.position(), facing);
                boss.setBoundingBox(executionActor.getBoundingBox());
                boss.setOnGround(executionActor.onGround());
                boss.setDeltaMovement(executionActor.getDeltaMovement());
                boss.setAnimationTick(phaseAge);
                if (!landing) goal.tick();
                if (boss.teleportFailed) return true;
                if (boss.getAnimation() != animation && !landing) {
                    // The native Air Strike goal changes phases only after actual landing.
                    if (boss.getAnimation() != Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2) return true;
                    phaseAge = 0;
                    return false;
                }
                // Native goals may steer independently; attacks and the phantom use the same shared heading.
                boss.pose(executionActor.position(),facing);boss.setBoundingBox(geometry.bounds());
                boss.aiStep();
                if (!landing && (ability == Ability.GUARDIAN_UPPERCUT || ability == Ability.GUARDIAN_ROCKET_PUNCH)) {
                    var motion = boss.getDeltaMovement();
                    if (!world.hasChunkAt(BlockPos.containing(executionActor.position().add(motion)))
                            || phaseAge > 26 && executionActor.horizontalCollision) return true;
                    executionActor.setDeltaMovement(motion);
                    executionActor.hurtMarked = true;
                }
                if (ability == Ability.GUARDIAN_AIR_STRIKE) {
                    executionActor.fallDistance = 0;
                    if (!landing && age >= 40 && (executionActor instanceof FlyingMob
                            || executionActor instanceof Player player && player.getAbilities().flying)) {
                        executionActor.setDeltaMovement(executionActor.getDeltaMovement().add(0, -.08, 0));
                        executionActor.hurtMarked = true;
                    }
                }
                if (ability == Ability.GUARDIAN_BLINK && phaseAge == 15 && boss.getTeleportPos().isPresent()) {
                    Vec3 spot = Vec3.atBottomCenterOf(boss.getTeleportPos().get());
                    world.sendParticles(ParticleTypes.END_ROD, spot.x, executionActor.getY() + .3, spot.z, 40, 1.5, .15, 1.5, .07);
                }
                boolean strike = landing ? phaseAge == 3 : switch (ability) {
                    case GUARDIAN_UPPERCUT -> phaseAge == 27 || boss.getIsHelmetless() && phaseAge == 84;
                    case GUARDIAN_BLINK -> phaseAge == 38;
                    case GUARDIAN_ROCKET_PUNCH -> phaseAge == 28;
                    default -> false;
                };
                if (strike) {
                    executionActor.swing(InteractionHand.MAIN_HAND);
                    MonstrosityCasting.impactVisual(world, executionActor.position().add(facing.scale(landing ? 0 : 2)), false, true);
                    world.sendParticles(ParticleTypes.REVERSE_PORTAL, executionActor.getX(), executionActor.getY() + .4, executionActor.getZ(), 36, 1, .5, 1, .1);
                }
                return landing ? phaseAge >= Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2.getDuration() : age >= end;
            }
            public void close(ExecutionSession<SpellGuardian> session, ExecutionSession.EndReason reason) {
                if(started){ACTORS.remove(executionActor.getUUID());goal.stop();}
                boss.discard();
                if (started && !executionActor.isRemoved() && ability==Ability.GUARDIAN_AIR_STRIKE) executionActor.setNoGravity(hadNoGravity);
                if (started && executionActor.level() == world && !executionActor.isRemoved()) {
                    executionActor.setDeltaMovement(executionActor.getDeltaMovement().multiply(.2, 1, .2));
                    executionActor.hurtMarked = true;
                }
            }
        }), hit -> {}, () -> enabled(ability) && geometry.present(cast,footprint), ExecutionScope.DIRECT));
    }

    private static Vec3 horizontal(Vec3 direction) {
        Vec3 result = direction.multiply(1, 0, 1).normalize();
        return result.lengthSqr() < .01 ? new Vec3(0, 0, 1) : result;
    }
    private static boolean validTarget(ServerLevel world, LivingEntity actor, LivingEntity target) {
        return target != null && target.isAlive() && !target.isRemoved() && target.level() == world
            && world.hasChunkAt(target.blockPosition())
            && actor.distanceToSqr(target) <= CataclysmConfig.guardianActionRange() * CataclysmConfig.guardianActionRange();
    }
    private static boolean clearDestination(ServerLevel world, LivingEntity actor, Vec3 point) {
        var box = actor.getBoundingBox().move(point.subtract(actor.position()));
        return point.y >= world.getMinBuildHeight() && box.maxY < world.getMaxBuildHeight()
            && MonstrosityCasting.loadedArea(world, point, (int)Math.ceil(actor.getBbWidth()) + 1)
            && world.getWorldBorder().isWithinBounds(box) && world.noCollision(actor, box) && !world.containsAnyLiquid(box);
    }
    private static Vec3 groundDestination(ServerLevel world, LivingEntity actor, Vec3 requested) {
        // Search a small loaded neighborhood, using the actual actor's dimensions.
        for (int radius = 0; radius <= 2; ++radius) {
            for (int dx = -radius; dx <= radius; ++dx) for (int dz = -radius; dz <= radius; ++dz) {
                if (radius > 0 && Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                double x = requested.x + dx, z = requested.z + dz;
                for (int drop = 0; drop <= 8; ++drop) {
                    BlockPos floor = BlockPos.containing(x, requested.y, z).below(drop + 1);
                    if (!world.isInWorldBounds(floor) || !world.hasChunkAt(floor)) break;
                    var shape = world.getBlockState(floor).getCollisionShape(world, floor);
                    if (shape.isEmpty()) continue;
                    var point = new Vec3(x, floor.getY() + shape.max(Direction.Axis.Y), z);
                    if (clearDestination(world, actor, point)) return point;
                }
            }
        }
        return null;
    }
    /** Called only by the detached native frame; native Boss teleportation remains untouched. */
    public static boolean teleport(SpellGuardian boss, Vec3 requested, boolean ground) {
        if (!(boss.level() instanceof ServerLevel world)) return false;
        var actor = boss.cast.executor();
        if(actor==null)return PositionBodyActions.fixed(boss.cast) && world.hasChunkAt(boss.blockPosition());
        if (!actor.isAlive() || actor.level() != world || actor.isPassenger()) return false;
        var event = EventHooks.onEnderTeleport(actor, requested.x, requested.y, requested.z);
        if (event.isCanceled()) return false;
        Vec3 point = new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        point = ground ? groundDestination(world, actor, point) : clearDestination(world, actor, point) ? point : null;
        if (point == null || !MonstrosityCasting.loadedArea(world, point, ground ? 12 : 18)) return false;
        Vec3 old = actor.position();
        actor.teleportTo(point.x, point.y, point.z);
        actor.setDeltaMovement(Vec3.ZERO);
        actor.hurtMarked = true;
        if (!ground) actor.setOnGround(false);
        boss.setPos(actor.position());
        boss.setBoundingBox(actor.getBoundingBox());
        world.playSound(null, actor.blockPosition(), SoundEvents.SHULKER_TELEPORT, SoundSource.PLAYERS, 1, 1);
        world.sendParticles(ParticleTypes.PORTAL, old.x, old.y + .5, old.z, 40, .5, .6, .5, .1);
        world.sendParticles(ParticleTypes.PORTAL, point.x, point.y + .5, point.z, 40, .5, .6, .5, .1);
        return true;
    }
    /** All nine native prepared bullets keep their own flight/hit lifecycle, without extra billing. */
    public static boolean bullet(ServerLevel world, Entity child, SpellGuardian boss, Supplier<Boolean> original) {
        if (!(child instanceof Ender_Guardian_Bullet_Entity bullet)) return original.get();
        if (!world.hasChunkAt(child.blockPosition())) return false;
        var state = (ManagedProjectile) child;
        state.confluxCataclysm$mode(2);
        state.confluxCataclysm$sourceAbility(boss.ability);
        state.confluxCataclysm$amplification(Resources.amplification(boss.amplification));
        bullet.setOwner(boss.cast.damageOwner());
        NativeDamageContexts.bind(bullet, boss.cast, boss.amplification);
        return ExecutionSessions.start(new ExecutionSession<>(world, boss.cast, bullet, 200, new ExecutionDriver<Ender_Guardian_Bullet_Entity>() {
            public boolean start(ExecutionSession<Ender_Guardian_Bullet_Entity> session) { return original.get(); }
            public boolean tick(ExecutionSession<Ender_Guardian_Bullet_Entity> session) {
                return bullet.isRemoved() || !world.hasChunkAt(bullet.blockPosition())
                    || !world.hasChunkAt(BlockPos.containing(bullet.position().add(bullet.getDeltaMovement())));
            }
            public void close(ExecutionSession<Ender_Guardian_Bullet_Entity> session, ExecutionSession.EndReason reason) { bullet.discard(); }
        }, hit -> {}, () -> enabled(boss.ability), ExecutionScope.DIRECT));
    }
    private GuardianActions() {}
}
