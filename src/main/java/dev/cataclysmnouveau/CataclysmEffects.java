package dev.cataclysmnouveau;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** A temporary, synchronized effect; no permanent entity flags or player abilities. */
@EventBusSubscriber(modid="cataclysm_nouveau")
public final class CataclysmEffects {
    private static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"cataclysm_nouveau");
    public static final DeferredHolder<MobEffect,MobEffect> LAVA_WALKING=EFFECTS.register("lava_walking",
            ()->new MobEffect(MobEffectCategory.BENEFICIAL,0xEF7426) {});
    public static final DeferredHolder<MobEffect,MobEffect> WATER_WALKING=EFFECTS.register("water_walking",
            ()->new MobEffect(MobEffectCategory.BENEFICIAL,0x63C2E0) {});
    public static void register(IEventBus bus) {EFFECTS.register(bus);}
    @SubscribeEvent
    public static void lavaDamage(LivingIncomingDamageEvent event) {
        if(event.getEntity().hasEffect(LAVA_WALKING)
                && (event.getSource().is(DamageTypes.LAVA) || event.getSource().is(DamageTypes.ON_FIRE)))
            event.setCanceled(true);
    }
    private CataclysmEffects() {}
}
