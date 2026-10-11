package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;

/** Native goals choose attacks; Core owns the detached execution's scheduling and cancellation. */
public final class ScyllaTimedCasting {
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS) return false;
        Vec3 point=ability==Ability.LIGHTNING_ROW?NativePayloads.groundPoint(world,cast):cast.origin();
        if(point==null || !MonstrosityCasting.loadedArea(world,point,16)) return false;
        if(ability==Ability.THUNDER_CLOUD && !world.isInWorldBounds(net.minecraft.core.BlockPos.containing(point.add(0,10,0)))) return false;
        Vec3 forward=MonstrosityCasting.direction(cast);
        var boss=new SpellScylla(world,cast,point,Resources.damageBonus(amp));
        LivingEntity target=cast.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && !dev.arsconflux.api.targeting.Allies.areAllies(cast.damageOwner(),living)?living:null;
        if(target==null) {
            // A location target is a detached marker only: no extra mob is spawned or saved.
            var marker=new ArmorStand(world,point.x,point.y,point.z);
            marker.setYRot((float)Math.toDegrees(Math.atan2(-forward.x,forward.z)));
            target=marker;
        }
        boss.setTarget(target);
        var goal=boss.nativeGoal(ability==Ability.THUNDER_CLOUD?"ThunderCloud":"Scylla_Lightning_Explosion");
        int warmup=CataclysmConfig.scyllaCloudWarmup();
        int rainTicks=Math.max(1,Math.min(120,MonstrosityCasting.scaledTicks(31,duration)));
        int lifetime=ability==Ability.THUNDER_CLOUD?warmup+rainTicks+2:57;
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,boss,lifetime,new ExecutionDriver<SpellScylla>() {
            private int age;
            public boolean start(ExecutionSession<SpellScylla> session) {
                goal.start();
                world.playSound(null,point.x,point.y,point.z,ModSounds.SUPER_LIGHTNING.get(),SoundSource.PLAYERS,.4F,1);
                return true;
            }
            public boolean tick(ExecutionSession<SpellScylla> session) {
                var selected=boss.getTarget();
                if(selected==null || !selected.isAlive() || selected.level()!=world
                        || !world.hasChunkAt(selected.blockPosition()) || selected.position().distanceToSqr(point)>48*48) return true;
                int tick=age++;
                if(ability==Ability.THUNDER_CLOUD) {
                    if(!world.isInWorldBounds(net.minecraft.core.BlockPos.containing(point.add(0,10,0)))) return true;
                    boss.attackTicks=tick<warmup?55+tick:115+(tick-warmup)%31;
                    // The original cloud is client-only particles attached to a Boss attack frame.
                    // Send the installed type from the server for this detached, fixed-position cloud.
                    if(tick%2==0) world.sendParticles(ModParticle.RAIN_CLOUD.get(),point.x,point.y+9.5,point.z,5,3,.15,3,.01);
                    if(tick==Math.max(0,warmup-1)) {
                        world.sendParticles(ModParticle.LIGHTNING_EXPLODE.get(),point.x,point.y+9.5,point.z,1,0,0,0,0);
                        world.playSound(null,point.x,point.y+9.5,point.z,ModSounds.SUPER_LIGHTNING.get(),SoundSource.PLAYERS,.4F,1);
                    }
                    if(tick>=warmup && tick<warmup+rainTicks) goal.tick();
                    return age>=warmup+rainTicks;
                }
                boss.attackTicks=tick;
                if(!MonstrosityCasting.loadedArea(world,selected.position(),16)) return true;
                goal.tick(); // Native two rows of nine strikes, at ticks 26 and 54, with prediction.
                return age>=55;
            }
            public void close(ExecutionSession<SpellScylla> session,ExecutionSession.EndReason reason) {goal.stop();}
        },hit->{},()->GlyphCatalog.ALL.stream().filter(g->g instanceof dev.cataclysmnouveau.glyph.CataclysmEffectGlyph e
                && e.ability==ability).allMatch(g->g.isEnabled()),ExecutionScope.DIRECT));
    }
    private ScyllaTimedCasting() {}
}
