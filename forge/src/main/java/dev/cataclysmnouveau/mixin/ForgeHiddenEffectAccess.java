package dev.cataclysmnouveau.mixin;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(MobEffectInstance.class)
public interface ForgeHiddenEffectAccess {
    @Accessor("hiddenEffect") void confluxCataclysm$hidden(MobEffectInstance value);
}
