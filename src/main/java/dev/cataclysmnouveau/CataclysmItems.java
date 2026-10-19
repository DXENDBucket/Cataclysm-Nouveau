package dev.cataclysmnouveau;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Divisible Boss materials keep learning a whole series within a few kills. */
public final class CataclysmItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("cataclysm_nouveau");
    public static final DeferredItem<Item> GUARDIAN_VOID_FRAGMENT =
        ITEMS.registerSimpleItem("guardian_void_fragment", new Item.Properties());
    public static final DeferredItem<Item> IGNITIUM_FRAGMENT =
        ITEMS.registerSimpleItem("ignitium_fragment", new Item.Properties());
    public static final DeferredItem<Item> CURSIUM_FRAGMENT =
        ITEMS.registerSimpleItem("cursium_fragment", new Item.Properties());
    public static final DeferredItem<Item> TIDAL_CLAW_FRAGMENT =
        ITEMS.registerSimpleItem("tidal_claw_fragment", new Item.Properties());
    public static void register(IEventBus bus) { ITEMS.register(bus); }
    private CataclysmItems() {}
}
