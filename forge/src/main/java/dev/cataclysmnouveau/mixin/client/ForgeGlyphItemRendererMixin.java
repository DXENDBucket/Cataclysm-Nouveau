package dev.cataclysmnouveau.mixin.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.client.GlyphClientExtensions;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.*;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve ItemRenderer and Conflux GUI framing; substitute identity only at the native renderer call. */
@Mixin(ItemRenderer.class)
public abstract class ForgeGlyphItemRendererMixin {
 @WrapOperation(method="render",at=@At(value="INVOKE",target="Lnet/minecraft/client/renderer/BlockEntityWithoutLevelRenderer;renderByItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
 private void confluxCat$nativeStack(BlockEntityWithoutLevelRenderer renderer,ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffers,int light,int overlay,Operation<Void> original){
  var nativeStack=GlyphClientExtensions.nativeStack(stack);
  if(nativeStack.isEmpty())original.call(renderer,stack,context,pose,buffers,light,overlay);
  else IClientItemExtensions.of(nativeStack).getCustomRenderer().renderByItem(nativeStack,context,pose,buffers,light,overlay);
 }
}
