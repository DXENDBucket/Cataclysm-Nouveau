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
        // Use a vanilla status sprite at runtime; neither upstream pixels nor a new atlas are bundled.
        event.registerMobEffect(new net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions() {
            @Override public boolean renderInventoryIcon(net.minecraft.world.effect.MobEffectInstance instance,
                    net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen<?> screen,
                    net.minecraft.client.gui.GuiGraphics graphics,int x,int y,int z) {
                graphics.blit(x,y+7,z,18,18,statusIcon(instance));
                return true;
            }
            @Override public boolean renderGuiIcon(net.minecraft.world.effect.MobEffectInstance instance,
                    net.minecraft.client.gui.Gui gui,net.minecraft.client.gui.GuiGraphics graphics,
                    int x,int y,float z,float alpha) {
                graphics.blit(x+3,y+3,(int)z,18,18,statusIcon(instance),1,1,1,alpha);
                return true;
            }
        },dev.cataclysmnouveau.CataclysmEffects.LAVA_WALKING,dev.cataclysmnouveau.CataclysmEffects.WATER_WALKING);
        event.registerItem(nativeItem(() -> ModItems.CERAUNUS.get()),GlyphCatalog.ANCHOR.getGlyph(),GlyphCatalog.WAVE.getGlyph(),GlyphCatalog.SPIN.getGlyph(),GlyphCatalog.LEAP.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.LASER_GATLING.get()),GlyphCatalog.LASER_BOLT.getGlyph(),GlyphCatalog.DEATH_LASER.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.WITHER_ASSULT_SHOULDER_WEAPON.get()),GlyphCatalog.MISSILE.getGlyph(),GlyphCatalog.HOMING.getGlyph(),GlyphCatalog.HOWITZER.getGlyph(),GlyphCatalog.HOWITZER_VOLLEY.getGlyph(),GlyphCatalog.MISSILE_SALVO.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.INFERNAL_FORGE.get()),GlyphCatalog.QUAKE.getGlyph());
    }
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite statusIcon(net.minecraft.world.effect.MobEffectInstance instance) {
        return Minecraft.getInstance().getMobEffectTextures().get(instance.getEffect().value()==dev.cataclysmnouveau.CataclysmEffects.WATER_WALKING.value()
                ?net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE:net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE);
    }
    private static IClientItemExtensions nativeItem(java.util.function.Supplier<net.minecraft.world.item.Item> source) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;
            @Override public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if(renderer==null) {
                    var client=Minecraft.getInstance();
                    renderer=new BlockEntityWithoutLevelRenderer(client.getBlockEntityRenderDispatcher(),client.getEntityModels()) {
                        @Override public void renderByItem(ItemStack glyph,ItemDisplayContext context,PoseStack pose,
                                                           MultiBufferSource buffers,int light,int overlay) {
                            var nativeStack=source.get().getDefaultInstance();
                            IClientItemExtensions.of(nativeStack.getItem()).getCustomRenderer()
                                    .renderByItem(nativeStack,context,pose,buffers,light,overlay);
                        }
                    };
                }
                return renderer;
            }
        };
    }
    private GlyphClientExtensions() {}
}
