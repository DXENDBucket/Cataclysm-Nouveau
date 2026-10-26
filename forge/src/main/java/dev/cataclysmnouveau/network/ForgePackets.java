package dev.cataclysmnouveau.network;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.Optional;
public final class ForgePackets {
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("cataclysm_nouveau","visuals"),()->"1","1"::equals,"1"::equals);
 public static void register(){
  CHANNEL.registerMessage(0,BodyPhantomPayload.class,BodyPhantomPayload::encode,BodyPhantomPayload::decode,BodyPhantomPayload::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(1,IgnisPhantomPayload.class,IgnisPhantomPayload::encode,IgnisPhantomPayload::decode,IgnisPhantomPayload::handle,Optional.of(NetworkDirection.PLAY_TO_CLIENT));
 }
 public static void sendToPlayersTrackingEntityAndSelf(Entity actor,Object packet){CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->actor),packet);}
 public static void sendToPlayer(ServerPlayer player,Object packet){CHANNEL.send(PacketDistributor.PLAYER.with(()->player),packet);}
 public static void sendToPlayersNear(ServerLevel world,ServerPlayer except,double x,double y,double z,double radius,Object packet){
  for(var p:world.players())if(p!=except&&p.distanceToSqr(x,y,z)<=radius*radius)sendToPlayer(p,packet);
 }
 private ForgePackets(){}
}
