package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.Death_Laser_Beam_Entity;
import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;

/** The installed beam executes once per server session tick; charge before each active second. */
public final class HarbingerLaser implements ExecutionDriver<Death_Laser_Beam_Entity> {
    private final LivingEntity anchor;private final Vec3 fixed,direction;private final ResourceCharge charge;private int age;
    private HarbingerLaser(LivingEntity anchor,Vec3 fixed,Vec3 direction,ResourceCharge charge){this.anchor=anchor;this.fixed=fixed;this.direction=direction;this.charge=charge;}
    public static boolean start(ServerLevel world,CastContext frame,double amp,double duration,ResourceCharge charge) {
        var source=ExecutionAnchor.beneficial(frame,ExecutionAnchor.Basis.EYES,Vec3.ZERO);
        if(source==null || !source.valid(world,frame))return false;
        LivingEntity host=source.entity();
        var facing=source.sample().direction();
        var driver=new HarbingerLaser(host,source.sample().origin(),facing,charge);
        var origin=driver.origin();if(!world.hasChunkAt(BlockPos.containing(origin)) || charge.affordableCount(1)==0)return false;
        int ticks=(int)Math.max(20,Math.min(1200,Math.round(60*Math.max(.1,1+.5*duration))));
        var beam=new Death_Laser_Beam_Entity(ModEntities.DEATH_LASER_BEAM.get(),world,frame.damageOwner()==null?frame.caster():frame.damageOwner(),origin.x,origin.y,origin.z,0,0,ticks,Math.max(0,(float)CMCommonConfig.Harbinger.DeathLaserdamage),(float)CMCommonConfig.Harbinger.DeathLaserHpdamage);
        beam.setFire(frame.caster().getHealth()<=frame.caster().getMaxHealth()/2);((ManagedDeathLaser)beam).confluxHarbinger$managed(true);
        var execution=frame.withExecutor(host).withAccount(charge.account());NativeDamageContexts.bind(beam,execution,amp);
        driver.pose(beam);
        return ExecutionSessions.start(new ExecutionSession<>(world,execution,beam,ticks+27,driver,h->{},()->GlyphCatalog.DEATH_LASER.isEnabled(),ExecutionScope.DIRECT));
    }
    private Vec3 origin(){return anchor==null?fixed:anchor.getEyePosition();}
    private Vec3 facing(){return anchor==null?direction:anchor.getLookAngle();}
    private void pose(Death_Laser_Beam_Entity beam){var point=origin();var vec=facing().normalize();beam.setPos(point);beam.setYaw((float)Math.atan2(vec.z,vec.x));beam.setPitch((float)Math.atan2(vec.y,vec.horizontalDistance()));}
    public boolean start(ExecutionSession<Death_Laser_Beam_Entity> s){s.world().playSound(null,BlockPos.containing(origin()),ModSounds.HARBINGER_DEATHLASER_PREPARE.get(),SoundSource.PLAYERS,3,1.2F);return s.world().addFreshEntity(s.parameters());}
    public boolean tick(ExecutionSession<Death_Laser_Beam_Entity> s) {
        var beam=s.parameters();if(beam.isRemoved() || anchor!=null && (!anchor.isAlive() || anchor.level()!=s.world()) || !s.world().hasChunkAt(BlockPos.containing(origin())))return true;
        var far=origin().add(facing().normalize().scale(30));for(int i=0;i<=30;i++)if(!s.world().hasChunkAt(BlockPos.containing(origin().lerp(far,i/30.0))))return true;
        ++age;
        if(age>20 && age<=20+beam.getDuration() && (age-21)%20==0 && !charge.trigger(()->true))return true;
        if(age==21)s.world().playSound(null,BlockPos.containing(origin()),ModSounds.DEATH_LASER.get(),SoundSource.PLAYERS,4,.75F);
        pose(beam);beam.tickCount=age;var state=(ManagedDeathLaser)beam;state.confluxHarbinger$advance(true);
        try{beam.tick();}finally{state.confluxHarbinger$advance(false);}
        return beam.isRemoved();
    }
    public void close(ExecutionSession<Death_Laser_Beam_Entity> s,ExecutionSession.EndReason reason){s.parameters().on=false;s.parameters().discard();}
}
