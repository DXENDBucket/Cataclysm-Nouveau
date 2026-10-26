package dev.cataclysmnouveau;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
@Mod("cataclysm_nouveau")
public final class CataclysmNouveau {
 public CataclysmNouveau(){
  var bus=FMLJavaModLoadingContext.get().getModEventBus();
  ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,CataclysmConfig.SPEC);
  ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,CataclysmClientConfig.SPEC);
  CataclysmEffects.register(bus);CataclysmItems.register(bus);CataclysmParticles.register(bus);
  GlyphCatalog.register();dev.cataclysmnouveau.network.ForgePackets.register();
  bus.addListener((FMLCommonSetupEvent e)->e.enqueueWork(GlyphCatalog::configureCarriers));
  var tabs=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"cataclysm_nouveau");
  tabs.register("glyphs",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.cataclysm_nouveau"))
   .icon(()->GlyphCatalog.AMETHYST.getGlyph().getDefaultInstance()).displayItems((params,out)->{
    out.accept(CataclysmItems.GUARDIAN_VOID_FRAGMENT.get());out.accept(CataclysmItems.IGNITIUM_FRAGMENT.get());
    out.accept(CataclysmItems.CURSIUM_FRAGMENT.get());out.accept(CataclysmItems.TIDAL_CLAW_FRAGMENT.get());
    GlyphCatalog.ALL.forEach(g->out.accept(g.getGlyph()));}).build());
  tabs.register(bus);
 }
}
