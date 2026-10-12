package dev.cataclysmnouveau.network;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.casting.SpellIgnis;
import dev.cataclysmnouveau.casting.IgnisAnimations;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.UUID;

/** Actual attack pose is authoritative; the visual proxy never controls the executor. */
public final class IgnisPhantomSync {
    public static IgnisPhantomPayload snapshot(UUID token,LivingEntity actor,SpellIgnis frame,boolean closed){
        return new IgnisPhantomPayload(token,frame.level().dimension().location(),actor==null?-1:actor.getId(),actor==null?new UUID(0,0):actor.getUUID(),
            actor==null,closed || actor==null?frame.position():actor.position(),frame.getYRot(),frame.getBossPhase(),IgnisAnimations.id(frame.getAnimation()),
            frame.getAnimationTick(),closed);
    }
    public static void send(UUID token,LivingEntity actor,SpellIgnis frame,boolean closed){
        if(IgnisAnimations.id(frame.getAnimation())==0)return;
        var data=snapshot(token,actor,frame,closed);
        if(!closed && actor!=null){PacketDistributor.sendToPlayersTrackingEntityAndSelf(actor,data);return;}
        // Removed/transferred actors may have already left the tracking map. Use the last attack world/pose.
        ServerPlayer self=actor instanceof ServerPlayer player?player:actor==null && frame.cast.caster() instanceof ServerPlayer player?player:null;
        PacketDistributor.sendToPlayersNear((ServerLevel)frame.level(),self,frame.getX(),frame.getY(),frame.getZ(),96,data);
        if(self!=null)PacketDistributor.sendToPlayer(self,data);
    }
    private IgnisPhantomSync(){}
}
