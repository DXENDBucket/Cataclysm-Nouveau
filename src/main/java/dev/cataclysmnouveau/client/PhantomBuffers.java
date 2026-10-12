package dev.cataclysmnouveau.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cataclysmnouveau.mixin.client.*;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** All textured native layers use alpha blending without writing phantom depth. */
final class PhantomBuffers implements MultiBufferSource {
    private final MultiBufferSource.BufferSource buffers;
    private final float opacity;
    private final Set<RenderType> touched;
    PhantomBuffers(MultiBufferSource.BufferSource buffers,float opacity,Set<RenderType> touched){this.buffers=buffers;this.opacity=opacity;this.touched=touched;}
    private static Optional<ResourceLocation> texture(RenderType type){
        if(!(type instanceof PhantomRenderTypeAccess access))return Optional.empty();
        var state=(PhantomCompositeStateAccess)(Object)access.confluxCataclysm$state();
        return ((PhantomTextureStateAccess)(Object)state.confluxCataclysm$texture()).confluxCataclysm$location();
    }
    @Override public VertexConsumer getBuffer(RenderType original){
        var type=texture(original).map(t->RenderType.entityTranslucentEmissive(t,false)).orElse(original);
        touched.add(type);return new Alpha(buffers.getBuffer(type),opacity);
    }
    private record Alpha(VertexConsumer delegate,float opacity) implements VertexConsumer {
        public VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
        public VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor(r,g,b,Math.round(a*opacity));return this;}
        public VertexConsumer setUv(float u,float v){delegate.setUv(u,v);return this;}
        public VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
        public VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
        public VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
    }
}
