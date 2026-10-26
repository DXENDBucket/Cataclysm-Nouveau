package dev.cataclysmnouveau;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
public final class CataclysmItems {
 private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"cataclysm_nouveau");
 public static final RegistryObject<Item> GUARDIAN_VOID_FRAGMENT=ITEMS.register("guardian_void_fragment",()->new Item(new Item.Properties()));
 public static final RegistryObject<Item> IGNITIUM_FRAGMENT=ITEMS.register("ignitium_fragment",()->new Item(new Item.Properties()));
 public static final RegistryObject<Item> CURSIUM_FRAGMENT=ITEMS.register("cursium_fragment",()->new Item(new Item.Properties()));
 public static final RegistryObject<Item> TIDAL_CLAW_FRAGMENT=ITEMS.register("tidal_claw_fragment",()->new Item(new Item.Properties()));
 public static void register(IEventBus bus){ITEMS.register(bus);}
 private CataclysmItems(){}
}
