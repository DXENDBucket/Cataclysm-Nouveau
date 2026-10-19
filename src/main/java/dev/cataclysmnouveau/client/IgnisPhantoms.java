package dev.cataclysmnouveau.client;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.cataclysmnouveau.CataclysmClientConfig;
import dev.cataclysmnouveau.casting.IgnisAnimations;
import dev.cataclysmnouveau.casting.VisualSampleClock;
import dev.cataclysmnouveau.mixin.client.IgnisVisualAccess;
import dev.cataclysmnouveau.network.IgnisPhantomPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.lang.ref.WeakReference;
import java.util.*;

/** Client-only, unregistered native render proxies. Never calls a Boss tick, AI or attack. */
@EventBusSubscriber(modid="cataclysm_nouveau",value=Dist.CLIENT)
public final class IgnisPhantoms {
    private static final Map<UUID,View> ACTIVE=new LinkedHashMap<>();
    private static WeakReference<ClientLevel> world=new WeakReference<>(null);
    private static final int LEASE=16,FADE=6;
    private static final class View {
        final Ignis_Entity proxy;
        IgnisPhantomPayload data;
        final long born;
        long received,expires;
        float previousYaw;
        final VisualSampleClock particles=new VisualSampleClock();
        View(ClientLevel world){born=world.getGameTime();proxy=new Ignis_Entity(ModEntities.IGNIS.get(),world);proxy.setNoAi(true);proxy.setNoGravity(true);proxy.setSilent(true);proxy.noCulling=true;}
    }
    private static ClientLevel level(){
        var level=Minecraft.getInstance().level;
        if(level!=world.get()){ACTIVE.clear();world=new WeakReference<>(level);}
        return level;
    }
    public static void accept(IgnisPhantomPayload data){
        var level=level();if(level==null || !level.dimension().location().equals(data.dimension()))return;
        if(!CataclysmClientConfig.ignisPhantom(data.animation())){ACTIVE.remove(data.token());return;}
        var animation=IgnisAnimations.animation(data.animation());
        if(!Double.isFinite(data.position().lengthSqr()) || !Float.isFinite(data.yaw()) || data.phase()<0 || data.phase()>2
                || animation==null || data.age()<0 || data.age()>IgnisAnimations.maxProgress(data.animation()))return;
        var view=ACTIVE.get(data.token());
        if(data.closed() && view==null)return;
        if(view==null){view=new View(level);ACTIVE.put(data.token(),view);}
        view.previousYaw=view.data==null?data.yaw():yaw(view,level.getGameTime(),0);
        view.data=data;view.received=level.getGameTime();view.expires=view.received+(data.closed()?FADE:LEASE);
    }
    private static float yaw(View view,long now,float partial){
        return Mth.rotLerp(Mth.clamp((now-view.received+partial)/4,0,1),view.previousYaw,view.data.yaw());
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var level=level();if(level==null)return;
        ACTIVE.values().removeIf(v->level.getGameTime()>=v.expires || !CataclysmClientConfig.ignisPhantom(v.data.animation()));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;
        var level=level();var client=Minecraft.getInstance();
        if(level==null || client.player==null || ACTIVE.isEmpty())return;
        float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var buffers=client.renderBuffers().bufferSource();var touched=new HashSet<RenderType>();
        for(var view:ACTIVE.values()){
            var data=view.data;long now=level.getGameTime();if(now>=view.expires || !CataclysmClientConfig.ignisPhantom(data.animation()))continue;
            LivingEntity actor=null;
            if(!data.positionAnchored()) {
                var entity=level.getEntity(data.actorId());
                if(!(entity instanceof LivingEntity living) || !living.getUUID().equals(data.actorUuid()) || !living.isAlive())continue;
                actor=living;
            }
            var point=actor==null?data.position():actor.getPosition(partial);
            if(client.player.distanceToSqr(point)>96*96 || !event.getFrustum().isVisible(new AABB(point,point).inflate(7)))continue;
            boolean ownFirstPerson=actor==client.player && client.options.getCameraType().isFirstPerson();
            float fade=Math.min(Mth.clamp((view.expires-now-partial)/FADE,0,1),Mth.clamp((now-view.born+partial+1)/4,0,1));
            float opacity=CataclysmClientConfig.phantomOpacity(ownFirstPerson)*fade;
            float effectOpacity=CataclysmClientConfig.phantomEffectOpacity(ownFirstPerson)*fade;
            if(opacity<=0 && effectOpacity<=0)continue;
            var proxy=view.proxy;float yaw=yaw(view,now,partial);
            var animation=IgnisAnimations.animation(data.animation());
            proxy.setAnimation(animation);
            proxy.setAnimationTick(Math.min(animation.getDuration()-1,data.age()+(data.closed()?0:(int)Math.min(4,now-view.received))));
            proxy.tickCount=(int)(now&0x7fffffff);proxy.setPos(point);proxy.xOld=point.x;proxy.yOld=point.y;proxy.zOld=point.z;
            proxy.setYRot(yaw);proxy.yRotO=yaw;proxy.yBodyRot=proxy.yBodyRotO=yaw;proxy.yHeadRot=proxy.yHeadRotO=yaw;
            proxy.setBossPhase(data.phase());proxy.setIsShieldBreak(data.phase()>1);
            proxy.setIsBlocking(data.phase()<2);proxy.setIsSword(data.phase()>1);
            proxy.prevblockingProgress=proxy.blockingProgress=data.phase()<2?10:0;
            proxy.prevswordProgress=proxy.swordProgress=data.phase()>1?10:0;
            var pose=event.getPoseStack();pose.pushPose();
            try{
                var camera=event.getCamera().getPosition();pose.translate(point.x-camera.x,point.y-camera.y,point.z-camera.z);
                float scale=CataclysmClientConfig.phantomScale();pose.scale(scale,scale,scale);
                client.getEntityRenderDispatcher().getRenderer(proxy).render(proxy,yaw,partial,pose,new PhantomBuffers(buffers,opacity,effectOpacity,touched),LightTexture.FULL_BRIGHT);
                // Native render() has now sampled blade2. Reuse its entire visual-only trail scheduler.
                // Seed a new/discontinuous sample before calling it, never interpolate from world origin.
                if(!data.closed()) {
                    var step=view.particles.sample(now,data.animation(),proxy.getAnimationTick());
                    var blade=proxy.socketPosArray[0];
                    if(step!=VisualSampleClock.Step.SKIP) {
                        var visual=(IgnisVisualAccess)(Object)proxy;
                        if(blade!=null && Double.isFinite(blade.lengthSqr())) {
                            blade=point.add(blade.subtract(point).scale(scale));proxy.setSocketPosArray(0,blade);
                            var previous=visual.cataclysm$previousBlade();
                            if(step==VisualSampleClock.Step.RESET || previous==null || !Double.isFinite(previous.lengthSqr()) || previous.distanceToSqr(blade)>16*16)
                                visual.cataclysm$previousBlade(blade);
                            visual.cataclysm$swingParticles();
                        }
                        int t=proxy.getAnimationTick();var nativeAnimation=proxy.getAnimation();
                        if((nativeAnimation==Ignis_Entity.REINFORCED_SMASH_IN_AIR || nativeAnimation==Ignis_Entity.REINFORCED_SMASH_IN_AIR_SOUL) && t>74 && t<81)
                            visual.cataclysm$pattern(1.5F,0,1,nativeAnimation==Ignis_Entity.REINFORCED_SMASH_IN_AIR_SOUL);
                        if((nativeAnimation==Ignis_Entity.REINFORCED_SMASH || nativeAnimation==Ignis_Entity.REINFORCED_SMASH_SOUL) && t>46 && t<56)
                            visual.cataclysm$sphere(.75F,2,6);
                        if(nativeAnimation==Ignis_Entity.COMBO2 && t>108 && t<112)
                            visual.cataclysm$strike(1.25F,5,0);
                    }
                }
            }finally{pose.popPose();}
        }
        touched.forEach(buffers::endBatch);
    }
    private IgnisPhantoms(){}
}
