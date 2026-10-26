package dev.cataclysmnouveau;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.registries.*;
@Mod.EventBusSubscriber(modid="cataclysm_nouveau")
public final class CataclysmEffects {
 private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,"cataclysm_nouveau");
 private static MobEffect effect(int color){return new MobEffect(MobEffectCategory.BENEFICIAL,color){
  @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer){
   consumer.accept(dev.cataclysmnouveau.client.GlyphClientExtensions.status());
  }
 };}
 public static final RegistryObject<MobEffect> LAVA_WALKING=EFFECTS.register("lava_walking",()->effect(0xEF7426));
 public static final RegistryObject<MobEffect> WATER_WALKING=EFFECTS.register("water_walking",()->effect(0x63C2E0));
 public static final RegistryObject<MobEffect> CURSED_RAGE=EFFECTS.register("cursed_rage",()->effect(0xA240CD));
 public static void register(IEventBus bus){EFFECTS.register(bus);}
 @SubscribeEvent public static void lavaDamage(LivingAttackEvent e){
  if(e.getEntity().hasEffect(LAVA_WALKING.get())&&(e.getSource().is(DamageTypes.LAVA)||e.getSource().is(DamageTypes.ON_FIRE)))e.setCanceled(true);
 }
 private CataclysmEffects(){}
}
