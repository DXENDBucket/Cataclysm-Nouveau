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
        },dev.cataclysmnouveau.CataclysmEffects.LAVA_WALKING,dev.cataclysmnouveau.CataclysmEffects.WATER_WALKING,dev.cataclysmnouveau.CataclysmEffects.CURSED_RAGE);
        event.registerItem(nativeItem(() -> ModItems.CURSED_BOW.get()),GlyphCatalog.PHANTOM_ARROW.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.SOUL_RENDER.get()),GlyphCatalog.PHANTOM_HALBERD.getGlyph(),GlyphCatalog.PHANTOM_SPIRAL.getGlyph(),GlyphCatalog.PHANTOM_RINGS.getGlyph(),GlyphCatalog.HALBERD_CLEAVE.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.CERAUNUS.get()),GlyphCatalog.ANCHOR.getGlyph(),GlyphCatalog.WAVE.getGlyph(),GlyphCatalog.SPIN.getGlyph(),GlyphCatalog.LEAP.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.LASER_GATLING.get()),GlyphCatalog.LASER_BOLT.getGlyph(),GlyphCatalog.DEATH_LASER.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.WITHER_ASSULT_SHOULDER_WEAPON.get()),GlyphCatalog.MISSILE.getGlyph(),GlyphCatalog.HOMING.getGlyph(),GlyphCatalog.HOWITZER.getGlyph(),GlyphCatalog.HOWITZER_VOLLEY.getGlyph(),GlyphCatalog.MISSILE_SALVO.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.THE_INCINERATOR.get()),GlyphCatalog.IGNIS_BLADE_FIREBALLS.getGlyph(),GlyphCatalog.IGNIS_SWORD_COMBO.getGlyph(),GlyphCatalog.IGNIS_ULTIMATE.getGlyph(),GlyphCatalog.IGNIS_FLAME_STRIKE.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.BULWARK_OF_THE_FLAME.get()),GlyphCatalog.IGNIS_AIR_SMASH.getGlyph(),GlyphCatalog.IGNIS_EARTH_SHUDDERS.getGlyph(),GlyphCatalog.IGNIS_REINFORCED_SMASH.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.INFERNAL_FORGE.get()),GlyphCatalog.QUAKE.getGlyph());
        event.registerItem(nativeItem(() -> ModItems.GAUNTLET_OF_GUARD.get()),GlyphCatalog.GRAVITY_PUNCH.getGlyph(),
            GlyphCatalog.GUARDIAN_UPPERCUT.getGlyph(),GlyphCatalog.GUARDIAN_ROCKET_PUNCH.getGlyph());
    }
    private static net.minecraft.client.renderer.texture.TextureAtlasSprite statusIcon(net.minecraft.world.effect.MobEffectInstance instance) {
        if(instance.getEffect().value()==dev.cataclysmnouveau.CataclysmEffects.CURSED_RAGE.value())
            return Minecraft.getInstance().getMobEffectTextures().get(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);
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
