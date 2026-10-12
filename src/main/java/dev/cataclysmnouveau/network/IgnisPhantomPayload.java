package dev.cataclysmnouveau.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.UUID;

/** Small visual snapshot; no client gameplay or registered Boss entity is involved. */
public record IgnisPhantomPayload(UUID token,ResourceLocation dimension,int actorId,UUID actorUuid,
        Vec3 position,float yaw,int phase,int animation,int age,boolean closed) implements CustomPacketPayload {
    public static final Type<IgnisPhantomPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath("cataclysm_nouveau","ignis_phantom"));
    public static final StreamCodec<RegistryFriendlyByteBuf,IgnisPhantomPayload> CODEC=StreamCodec.of((b,p)->{
        b.writeUUID(p.token);b.writeResourceLocation(p.dimension);b.writeVarInt(p.actorId);b.writeUUID(p.actorUuid);
        b.writeDouble(p.position.x);b.writeDouble(p.position.y);b.writeDouble(p.position.z);b.writeFloat(p.yaw);
        b.writeVarInt(p.phase);b.writeVarInt(p.animation);b.writeVarInt(p.age);b.writeBoolean(p.closed);
    },b->new IgnisPhantomPayload(b.readUUID(),b.readResourceLocation(),b.readVarInt(),b.readUUID(),
        new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),b.readFloat(),b.readVarInt(),b.readVarInt(),b.readVarInt(),b.readBoolean()));
    @Override public Type<IgnisPhantomPayload> type(){return TYPE;}
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToClient(TYPE,CODEC,(p,c)->{
            if(FMLEnvironment.dist==Dist.CLIENT)c.enqueueWork(()->dev.cataclysmnouveau.client.IgnisPhantoms.accept(p));
        });
    }
}
