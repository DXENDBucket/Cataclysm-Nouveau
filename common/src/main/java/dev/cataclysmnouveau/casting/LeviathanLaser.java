package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Entity;
import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import java.util.function.*;

/** One server driver advances the native ray; active seconds are prepaid, fade causes no damage. */
public final class LeviathanLaser implements ExecutionDriver<Abyss_Blast_Entity> {
    private final ExecutionAnchor anchor;
    private final ResourceCharge charge;
    private final Supplier<Vec3> direction;
    private final BooleanSupplier permitted;
    private final Runnable empty;
    private final boolean chargeSound;
    private int age;
    private LeviathanLaser(ExecutionAnchor anchor,ResourceCharge charge,Supplier<Vec3> direction,BooleanSupplier permitted,Runnable empty,boolean chargeSound){
        this.anchor=anchor;this.charge=charge;this.direction=direction;this.permitted=permitted;this.empty=empty;this.chargeSound=chargeSound;
    }
    public static boolean start(ServerLevel world,CastContext cast,double amp,double duration,ResourceCharge charge) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.EYES,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || charge.affordableCount(1)==0)return false;
        var origin=anchor.sample().origin();
        int ticks=MonstrosityCasting.scaledTicks(80,duration);
        var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
        var beam=new Abyss_Blast_Entity(ModEntities.ABYSS_BLAST.get(),world,owner,origin.x,origin.y,origin.z,0,0,ticks,0,
                Math.max(0,(float)CMCommonConfig.Leviathan.AbyssBlastDamage),(float)CMCommonConfig.Leviathan.AbyssBlastHpDamage);
        return ExecutionSessions.start(attach(world,cast,anchor,beam,amp,charge,()->anchor.sample().direction(),
                ()->LeviathanCasting.enabled(Ability.ABYSS_BLAST),()->{},true));
    }
    /** Adopt a native goal-created beam, preserving the same billing, render pose and single driver. */
    static ExecutionSession<Abyss_Blast_Entity> attach(ServerLevel world,CastContext cast,ExecutionAnchor anchor,Abyss_Blast_Entity beam,
            double amp,ResourceCharge charge,Supplier<Vec3> direction,BooleanSupplier permitted,Runnable empty,boolean chargeSound) {
        var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();beam.caster=owner;beam.setCasterID(owner.getId());
        ((LeviathanBeam)beam).confluxLeviathan$managed(true);
        ((LeviathanBeam)beam).confluxLeviathan$executor(anchor.entity()==null?-1:anchor.entity().getId());
        var frame=cast.withExecutor(anchor.entity()).withAccount(charge.account());
        NativeDamageContexts.bind(beam,frame,amp);
        var driver=new LeviathanLaser(anchor,charge,direction,permitted,empty,chargeSound);driver.pose(beam);
        return new ExecutionSession<>(world,frame,beam,beam.getDuration()+28,driver,hit->{},permitted,ExecutionScope.DIRECT);
    }
    private void pose(Abyss_Blast_Entity beam){
        var pose=anchor.sample();var facing=direction.get();
        beam.setPos(pose.origin());beam.setYaw((float)Math.atan2(facing.z,facing.x));
        beam.setPitch((float)Math.atan2(facing.y,facing.horizontalDistance()));
    }
    public boolean start(ExecutionSession<Abyss_Blast_Entity> s){
        if(chargeSound)s.world().playSound(null,BlockPos.containing(anchor.sample().origin()),ModSounds.ABYSS_BLAST_ONLY_CHARGE.get(),SoundSource.PLAYERS,1,1);
        return s.world().addFreshEntity(s.parameters());
    }
    public boolean tick(ExecutionSession<Abyss_Blast_Entity> s){
        var beam=s.parameters();
        if(beam.isRemoved() || !permitted.getAsBoolean() || !anchor.valid(s.world(),s.context()))return true;
        ++age;
        if(age>20 && age<=20+beam.getDuration() && (age-21)%20==0 && !charge.trigger(()->true)){empty.run();return true;}
        if(age==21)s.world().playSound(null,beam.blockPosition(),ModSounds.ABYSS_BLAST_ONLY_SHOOT.get(),SoundSource.PLAYERS,2,1);
        pose(beam);beam.tickCount=age;
        var state=(LeviathanBeam)beam;state.confluxLeviathan$advance(true);
        try{beam.tick();}finally{state.confluxLeviathan$advance(false);}
        return beam.isRemoved();
    }
    public void close(ExecutionSession<Abyss_Blast_Entity> s,ExecutionSession.EndReason reason){s.parameters().on=false;s.parameters().discard();}
}
