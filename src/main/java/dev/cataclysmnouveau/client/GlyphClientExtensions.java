package dev.cataclysmnouveau.client;

import com.github.L_Ender.cataclysm.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** A builtin/entity parent needs its native item identity as well as its model. */
@EventBusSubscriber(modid="cataclysm_nouveau",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class GlyphClientExtensions {
    @SubscribeEvent
    public static void register(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(renderer==null) {
                    var client=Minecraft.getInstance();
                    renderer=new BlockEntityWithoutLevelRenderer(client.getBlockEntityRenderDispatcher(),client.getEntityModels()) {
                        @Override public void renderByItem(ItemStack glyph,ItemDisplayContext context,PoseStack pose,
                                                           MultiBufferSource buffers,int light,int overlay) {
                            var nativeStack=ModItems.CERAUNUS.get().getDefaultInstance();
                            IClientItemExtensions.of(nativeStack.getItem()).getCustomRenderer()
                                    .renderByItem(nativeStack,context,pose,buffers,light,overlay);
                        }
                    };
                }
                return renderer;
            }
        },GlyphCatalog.ANCHOR.getGlyph(),GlyphCatalog.WAVE.getGlyph());
    }
    private GlyphClientExtensions() {}
}
