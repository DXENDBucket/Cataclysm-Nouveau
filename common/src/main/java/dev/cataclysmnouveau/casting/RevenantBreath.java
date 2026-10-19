package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.Ashen_Breath_Entity;
import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Native cone, growing reach, blindness and client smoke/flames; prepay each active second. */
public final class RevenantBreath {
    public static boolean start(ServerLevel world,CastContext cast,double amp,double duration,ResourceCharge charge){
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.EYES,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),8) || charge.affordableCount(1)==0)return false;
        int life=MonstrosityCasting.scaledTicks(25,duration);
        var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
        var breath=new Ashen_Breath_Entity(ModEntities.ASHEN_BREATH.get(),world,Math.max(0,(float)CMCommonConfig.IgnitedRevenant.AshenbreathDamage),owner);
        ((AshenBreath)breath).confluxRevenant$lifetime(life);
        var context=cast.withExecutor(anchor.entity()).withAccount(charge.account());NativeDamageContexts.bind(breath,context,amp);
        var driver=new ExecutionDriver<Ashen_Breath_Entity>() {
            int age;
            private void pose(){var p=anchor.sample();var d=p.direction();breath.setPos(p.origin());
                breath.setYRot((float)Math.toDegrees(Math.atan2(-d.x,d.z)));breath.setXRot((float)-Math.toDegrees(Math.atan2(d.y,d.horizontalDistance())));}
            public boolean start(ExecutionSession<Ashen_Breath_Entity> s){
                pose();return charge.trigger(()->{boolean added=world.addFreshEntity(breath);
                    if(added)world.playSound(null,breath.getX(),breath.getY(),breath.getZ(),ModSounds.REVENANT_BREATH.get(),SoundSource.PLAYERS,1,1);return added;});
            }
            public boolean tick(ExecutionSession<Ashen_Breath_Entity> s){
                if(breath.isRemoved() || !anchor.valid(world,context) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),8))return true;
                ++age;if(age>1 && (age-1)%20==0 && !charge.trigger(()->true))return true;
                pose();breath.tickCount=age;var state=(AshenBreath)breath;state.confluxRevenant$advance(true);
                try{breath.tick();}finally{state.confluxRevenant$advance(false);}return breath.isRemoved();
            }
            public void close(ExecutionSession<Ashen_Breath_Entity> s,ExecutionSession.EndReason reason){breath.discard();}
        };
        return ExecutionSessions.start(new ExecutionSession<>(world,context,breath,life+3,driver,h->{},()->GlyphCatalog.ASHEN_BREATH.isEnabled(),ExecutionScope.DIRECT));
    }
    private RevenantBreath(){}
}
