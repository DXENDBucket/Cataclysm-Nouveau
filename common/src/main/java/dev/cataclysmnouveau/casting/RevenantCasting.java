package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.Blazing_Bone_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.function.BooleanSupplier;

/** Retain the native four random radial volleys and their own projectile lifetimes. */
public final class RevenantCasting {
    public static boolean storm(ServerLevel world,CastContext cast,double amp){
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),8))return false;
        var geometry=new ActionGeometry(world,cast,anchor);var context=cast.withExecutor(anchor.entity());
        var frame=new SpellRevenant(world,context,geometry.point(),geometry.initialFacing,amp);var goal=frame.stormGoal();
        var driver=new ExecutionDriver<SpellRevenant>() {
            int age;boolean started;Vec3 heading=geometry.initialFacing;
            public boolean start(ExecutionSession<SpellRevenant> s){started=true;frame.setAnimation(Ignited_Revenant_Entity.BONE_STORM_ATTACK);goal.start();return true;}
            public boolean tick(ExecutionSession<SpellRevenant> s){
                if(!geometry.present(cast,8))return true;
                ++age;heading=geometry.steer(heading,age<=20);geometry.pose(frame,heading);frame.setAnimationTick(age);
                if(age<=49)goal.tick();
                for(var child:frame.children)if(!child.isRemoved() && (!world.hasChunkAt(child.blockPosition())
                    || !world.hasChunkAt(BlockPos.containing(child.position().add(child.getDeltaMovement())))))child.discard();
                return age>=49 && frame.children.stream().allMatch(Entity::isRemoved) || age>=200;
            }
            public void close(ExecutionSession<SpellRevenant> s,ExecutionSession.EndReason reason){if(started)goal.stop();frame.clear();}
        };
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,203,
            BodyPhantomSync.wrap(Ability.BONE_STORM,frame,driver),h->{},()->GlyphCatalog.BONE_STORM.isEnabled(),ExecutionScope.DIRECT));
    }
    public static boolean birth(SpellRevenant frame,Entity child,BooleanSupplier original){
        if(!(child instanceof Blazing_Bone_Entity bone))return false;
        if(!(frame.level() instanceof ServerLevel world) || !world.hasChunkAt(child.blockPosition()))return false;
        bone.setOwner(frame.cast.damageOwner()==null?frame.cast.caster():frame.cast.damageOwner());
        var state=(ManagedProjectile)bone;state.confluxCataclysm$mode(2);state.confluxCataclysm$sourceAbility(Ability.BONE_STORM);
        NativeDamageContexts.bind(bone,frame.cast,frame.amplification);
        if(!original.getAsBoolean()){bone.discard();return false;}frame.children.add(bone);return true;
    }
    private RevenantCasting(){}
}
