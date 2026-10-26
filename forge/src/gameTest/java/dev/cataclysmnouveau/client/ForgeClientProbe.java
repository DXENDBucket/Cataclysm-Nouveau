package dev.cataclysmnouveau.client;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Optional test source: render every glyph and native 3D item, capture, then exit normally. */
@Mod.EventBusSubscriber(modid="cataclysm_nouveau",value=Dist.CLIENT)
public final class ForgeClientProbe {
    private static int ticks;
    private static boolean opened;
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
        if(!Boolean.getBoolean("cataclysm_nouveau.clientProbe") || event.phase!=TickEvent.Phase.END)return;
        var mc=Minecraft.getInstance();
        if(!opened && (mc.screen instanceof TitleScreen || mc.screen!=null && mc.screen.getClass().getSimpleName().contains("Accessibility")) && mc.getOverlay()==null){opened=true;mc.setScreen(new Probe());}
        if(opened && mc.screen instanceof Probe && ++ticks==60){
            Screenshot.grab(mc.gameDirectory,"cataclysm-forge-glyphs.png",mc.getMainRenderTarget(),message->System.out.println("CLIENT_PROBE_SCREENSHOT "+message.getString()));
            System.out.println("CLIENT_PROBE_OK glyphs="+GlyphCatalog.ALL.size());mc.stop();
        }
    }
    private static final class Probe extends Screen {
        Probe(){super(Component.literal("Cataclysm Forge glyph audit"));}
        public void render(GuiGraphics g,int x,int y,float partial){
            g.fill(0,0,width,height,0xff22252b);g.drawString(font,title,8,6,0xffffff);
            int i=0;
            for(var glyph:GlyphCatalog.ALL){g.renderItem(glyph.getGlyph().getDefaultInstance(),8+(i%17)*21,23+(i/17)*22);i++;}
            g.drawString(font,"Native 3D glyphs (FIXED context)",8,165,0xffffff);
            i=0;
            for(var glyph:GlyphCatalog.ALL){
                var stack=glyph.getGlyph().getDefaultInstance();if(GlyphClientExtensions.nativeStack(stack).isEmpty())continue;
                if(i>=12)break;
                var pose=g.pose();pose.pushPose();pose.translate(24+i*30,205,100);pose.scale(23,-23,23);
                var renderer=minecraft.getItemRenderer();var model=renderer.getModel(stack,null,null,0);
                renderer.render(stack,ItemDisplayContext.FIXED,false,pose,g.bufferSource(),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);
                pose.popPose();i++;
            }
            g.flush();
        }
    }
}
