package dev.cataclysmnouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
@Mod("cataclysm_nouveau")
public final class CataclysmNouveau {
    public CataclysmNouveau(IEventBus bus,ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER,CataclysmConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT,CataclysmClientConfig.SPEC);
        CataclysmEffects.register(bus);
        CataclysmParticles.register(bus);
        CataclysmItems.register(bus);
        GlyphCatalog.register();
        bus.addListener(dev.cataclysmnouveau.network.IgnisPhantomPayload::register);
        bus.addListener(dev.cataclysmnouveau.network.BodyPhantomPayload::register);
        bus.addListener((FMLCommonSetupEvent event)->event.enqueueWork(GlyphCatalog::configureCarriers));
        var tabs=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"cataclysm_nouveau");
        tabs.register("glyphs",()->CreativeModeTab.builder().title(Component.translatable("itemGroup.cataclysm_nouveau"))
            .icon(()->GlyphCatalog.AMETHYST.getGlyph().getDefaultInstance())
            .displayItems((params,output)->{
                output.accept(CataclysmItems.GUARDIAN_VOID_FRAGMENT.get());
                output.accept(CataclysmItems.IGNITIUM_FRAGMENT.get());
                output.accept(CataclysmItems.CURSIUM_FRAGMENT.get());
                output.accept(CataclysmItems.TIDAL_CLAW_FRAGMENT.get());
                GlyphCatalog.ALL.forEach(g->output.accept(g.getGlyph()));
            }).build());
        tabs.register(bus);
    }
}
