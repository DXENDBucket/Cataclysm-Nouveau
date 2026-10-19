package dev.cataclysmnouveau.client;

import dev.cataclysmnouveau.CataclysmClientConfig;
import dev.cataclysmnouveau.casting.BodyPhantomAnimations;
import dev.cataclysmnouveau.casting.VisualSampleClock;
import net.minecraft.world.entity.Mob;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.lang.ref.WeakReference;
import java.util.*;

/** Client-only, unregistered native render proxies. Never calls a Boss tick, AI or attack. */
@EventBusSubscriber(modid="cataclysm_nouveau",value=Dist.CLIENT)
public final class BodyPhantoms {
    private static final Map<UUID,View> ACTIVE=new LinkedHashMap<>();
    private static WeakReference<ClientLevel> world=new WeakReference<>(null);
    private static final int LEASE=16,FADE=6;
    private record Trail(BodyPhantomPayload pose,Vec3 point,float yaw,int age,long born) {}
    private static final class View {
        final Mob proxy;
        final BodyPhantomAnimations.Kind kind;
        BodyPhantomPayload data;
        final long born;
        long received,expires;
        float previousYaw;
        final VisualSampleClock trailClock=new VisualSampleClock();
        final Deque<Trail> trails=new ArrayDeque<>();
        Vec3 previousPoint;
        View(ClientLevel world,BodyPhantomAnimations.Kind kind){born=world.getGameTime();this.kind=kind;proxy=BodyPhantomModels.create(kind,world);}
    }
    private static ClientLevel level(){
        var level=Minecraft.getInstance().level;
        if(level!=world.get()){ACTIVE.clear();world=new WeakReference<>(level);}
        return level;
    }
    public static void accept(BodyPhantomPayload data){
        var level=level();if(level==null || !level.dimension().location().equals(data.dimension()))return;
        if(!data.valid())return;
        var kind=BodyPhantomAnimations.kind(BodyPhantomAnimations.ability(data.ability()));
        if(!CataclysmClientConfig.bodyPhantom(kind)){ACTIVE.remove(data.token());return;}
        var view=ACTIVE.get(data.token());
        if(data.closed() && view==null)return;
        if(view!=null && view.kind!=kind)return;
        if(view==null){view=new View(level,kind);ACTIVE.put(data.token(),view);}
        view.previousYaw=view.data==null?data.yaw():yaw(view,level.getGameTime(),0);
        view.data=data;view.received=level.getGameTime();view.expires=view.received+(data.closed()?FADE:LEASE);
    }
    private static float yaw(View view,long now,float partial){
        return Mth.rotLerp(Mth.clamp((now-view.received+partial)/4,0,1),view.previousYaw,view.data.yaw());
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var level=level();if(level==null)return;
        ACTIVE.values().removeIf(v->level.getGameTime()>=v.expires || !CataclysmClientConfig.bodyPhantom(v.kind));
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;
        var level=level();var client=Minecraft.getInstance();
        if(level==null || client.player==null || ACTIVE.isEmpty())return;
        float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var buffers=client.renderBuffers().bufferSource();var touched=new HashSet<RenderType>();
        for(var view:ACTIVE.values()){
            var data=view.data;long now=level.getGameTime();if(now>=view.expires || !CataclysmClientConfig.bodyPhantom(view.kind))continue;
            LivingEntity actor=null;
            if(!data.closed() && !data.positionAnchored()) {
                var entity=level.getEntity(data.actorId());
                if(!(entity instanceof LivingEntity living) || !living.getUUID().equals(data.actorUuid()) || !living.isAlive())continue;
                actor=living;
            }
            var point=actor==null?data.position():actor.getPosition(partial);
            if(client.player.distanceToSqr(point)>96*96 || !event.getFrustum().isVisible(new AABB(point,point).inflate(12)))continue;
            boolean ownFirstPerson=data.actorUuid().equals(client.player.getUUID()) && client.options.getCameraType().isFirstPerson();
            float fade=Math.min(Mth.clamp((view.expires-now-partial)/FADE,0,1),Mth.clamp((now-view.born+partial+1)/4,0,1));
            float opacity=CataclysmClientConfig.phantomOpacity(ownFirstPerson)*fade;
            float effectOpacity=CataclysmClientConfig.phantomEffectOpacity(ownFirstPerson)*fade;
            if(opacity<=0 && effectOpacity<=0)continue;
            float yaw=yaw(view,now,partial);
            int age=data.age()+(data.closed()?0:(int)Math.min(4,now-view.received));
            view.trails.removeIf(trail->now-trail.born()>=5);
            for(var trail:view.trails) {
                // Fixed historical geometry; use the same native model/layers and local opacity policy.
                float trailOpacity=CataclysmClientConfig.phantomOpacity(ownFirstPerson)*.5F/(1+now-trail.born()+partial);
                draw(event,view,trail.pose(),trail.point(),trail.yaw(),trail.age(),partial,trailOpacity,trailOpacity,buffers,touched);
            }
            draw(event,view,data,point,yaw,age,partial,opacity,effectOpacity,buffers,touched);
            if(!data.closed() && view.kind==BodyPhantomAnimations.Kind.MALEDICTUS
                    && view.trailClock.sample(now,data.animation(),age)!=VisualSampleClock.Step.SKIP) {
                if(BodyPhantomAnimations.maledictusAfterimage(data.animation(),age)) {
                    Vec3 previous=view.previousPoint==null?point:view.previousPoint;
                    // A skipped view/teleport must not leave a trail at a stale, unrelated location.
                    if(previous.distanceToSqr(point)>16*16)previous=point;
                    var behind=Vec3.directionFromRotation(0,yaw).scale(-.4);
                    view.trails.addLast(new Trail(data,previous.add(behind),yaw,age,now));
                }
                view.previousPoint=point;
            }
        }
        touched.forEach(buffers::endBatch);
    }
    private static void draw(RenderLevelStageEvent event,View view,BodyPhantomPayload data,Vec3 point,float yaw,int age,
            float partial,float opacity,float effectOpacity,MultiBufferSource.BufferSource buffers,Set<RenderType> touched) {
            var client=Minecraft.getInstance();var proxy=view.proxy;
            BodyPhantomModels.pose(proxy,data,age);
            proxy.setPos(point);proxy.xOld=point.x;proxy.yOld=point.y;proxy.zOld=point.z;
            proxy.setYRot(yaw);proxy.yRotO=yaw;proxy.yBodyRot=proxy.yBodyRotO=yaw;proxy.yHeadRot=proxy.yHeadRotO=yaw;
            proxy.setXRot(data.pitch());proxy.xRotO=data.pitch();
            var pose=event.getPoseStack();pose.pushPose();
            try{
                var camera=event.getCamera().getPosition();pose.translate(point.x-camera.x,point.y-camera.y,point.z-camera.z);
                float scale=CataclysmClientConfig.phantomScale();pose.scale(scale,scale,scale);
                client.getEntityRenderDispatcher().getRenderer(proxy).render(proxy,yaw,partial,pose,new PhantomBuffers(buffers,opacity,effectOpacity,touched),LightTexture.FULL_BRIGHT);
            }finally{pose.popPose();}
    }
    private BodyPhantoms(){}
}
