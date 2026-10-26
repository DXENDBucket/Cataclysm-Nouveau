package dev.cataclysmnouveau.client;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cataclysmnouveau.mixin.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
final class PhantomBuffers implements MultiBufferSource {
 private final MultiBufferSource.BufferSource buffers;private final float opacity,effectOpacity;private final Set<RenderType> touched;
 PhantomBuffers(MultiBufferSource.BufferSource b,float a,Set<RenderType> t){this(b,a,a,t);}
 PhantomBuffers(MultiBufferSource.BufferSource b,float a,float e,Set<RenderType> t){buffers=b;opacity=a;effectOpacity=e;touched=t;}
 private static Optional<ResourceLocation> texture(RenderType type){
  if(!(type instanceof PhantomRenderTypeAccess access))return Optional.empty();
  var state=(PhantomCompositeStateAccess)(Object)access.confluxCataclysm$state();
  return ((PhantomTextureStateAccess)(Object)state.confluxCataclysm$texture()).confluxCataclysm$location();
 }
 public VertexConsumer getBuffer(RenderType original){
  var texture=texture(original);var type=texture.map(t->RenderType.entityTranslucentEmissive(t,false)).orElse(original);
  boolean effect=texture.map(t->t.getPath().startsWith("textures/particle/")).orElse(true);
  touched.add(type);return new Alpha(buffers.getBuffer(type),effect?effectOpacity:opacity);
 }
 private record Alpha(VertexConsumer delegate,float opacity) implements VertexConsumer{
  public VertexConsumer vertex(double x,double y,double z){delegate.vertex(x,y,z);return this;}
  public VertexConsumer color(int r,int g,int b,int a){delegate.color(r,g,b,Math.round(a*opacity));return this;}
  public VertexConsumer uv(float u,float v){delegate.uv(u,v);return this;}
  public VertexConsumer overlayCoords(int u,int v){delegate.overlayCoords(u,v);return this;}
  public VertexConsumer uv2(int u,int v){delegate.uv2(u,v);return this;}
  public VertexConsumer normal(float x,float y,float z){delegate.normal(x,y,z);return this;}
  public void endVertex(){delegate.endVertex();}
  public void defaultColor(int r,int g,int b,int a){delegate.defaultColor(r,g,b,Math.round(a*opacity));}
  public void unsetDefaultColor(){delegate.unsetDefaultColor();}
 }
}
