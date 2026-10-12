package dev.cataclysmnouveau.network;

import dev.cataclysmnouveau.casting.BodyPhantomAnimations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.UUID;

/** Render-only pose. The native visual proxy is never added to the world. */
public record BodyPhantomPayload(UUID token, ResourceLocation dimension, int actorId, UUID actorUuid,
        Vec3 position, float yaw, float pitch, String ability, int animation, int age, int flags, boolean closed)
        implements CustomPacketPayload {
    public static final Type<BodyPhantomPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","body_phantom"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BodyPhantomPayload> CODEC=StreamCodec.of((b,p)->{
        b.writeUUID(p.token);b.writeResourceLocation(p.dimension);b.writeVarInt(p.actorId);b.writeUUID(p.actorUuid);
        b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);b.writeFloat(p.yaw);b.writeFloat(p.pitch);
        b.writeUtf(p.ability,64);b.writeVarInt(p.animation);b.writeVarInt(p.age);b.writeVarInt(p.flags);b.writeBoolean(p.closed);
    },b->new BodyPhantomPayload(b.readUUID(),b.readResourceLocation(),b.readVarInt(),b.readUUID(),
        new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat(),b.readFloat(),b.readUtf(64),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
    public boolean valid() {
        return Double.isFinite(position.lengthSqr()) && Float.isFinite(yaw) && Float.isFinite(pitch)
            && BodyPhantomAnimations.valid(BodyPhantomAnimations.ability(ability),animation,age,flags);
    }
    @Override public Type<BodyPhantomPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToClient(TYPE,CODEC,(p,c)->{
            if(FMLEnvironment.dist==Dist.CLIENT)c.enqueueWork(()->dev.cataclysmnouveau.client.BodyPhantoms.accept(p));
        });
    }
}
