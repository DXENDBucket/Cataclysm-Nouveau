package dev.cataclysmnouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.glyph.TriggerPaidGlyph;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import java.util.*;

/** Position effects share payment and context handling; their lifecycle belongs to Cataclysm. */
public final class CataclysmEffectGlyph extends AbstractEffect implements TriggerPaidGlyph {
    public final Ability ability;

    public CataclysmEffectGlyph(Ability ability) {
        super(CarrierRegistry.id("cataclysm_nouveau:glyph_" + ability.id), ability.name);
        this.ability = ability;
        if (ability == Ability.MONSTROUS || ability == Ability.SHOULDER_CHECK || ability == Ability.BONE_FRACTURE || ability == Ability.LAVA_WALKING || ability == Ability.LIGHTNING_FIELD || ability == Ability.THUNDER_CLOUD || ability == Ability.WATER_WALKING) {
            compatibleAugments.add(AugmentExtendTime.INSTANCE);
            compatibleAugments.add(AugmentDurationDown.INSTANCE);
            augmentDescriptions.put(AugmentExtendTime.INSTANCE, Component.translatable("cataclysm_nouveau.augment.duration_up"));
            augmentDescriptions.put(AugmentDurationDown.INSTANCE, Component.translatable("cataclysm_nouveau.augment.duration_down"));
        }
        augmentDescriptions.put(AugmentAmplify.INSTANCE, Component.translatable(ability == Ability.LAVA_ABSORPTION ? "cataclysm_nouveau.augment.healing_up" : (ability == Ability.MONSTROUS || ability == Ability.BONE_FRACTURE) ? "cataclysm_nouveau.augment.effect_up" : "cataclysm_nouveau.augment.increase_damage"));
        augmentDescriptions.put(AugmentDampen.INSTANCE, Component.translatable(ability == Ability.LAVA_ABSORPTION ? "cataclysm_nouveau.augment.healing_down" : (ability == Ability.MONSTROUS || ability == Ability.BONE_FRACTURE) ? "cataclysm_nouveau.augment.effect_down" : "cataclysm_nouveau.augment.decrease_damage"));
        if (ability == Ability.LAVA_WALKING || ability == Ability.WATER_WALKING) {
            compatibleAugments.remove(AugmentAmplify.INSTANCE);
            compatibleAugments.remove(AugmentDampen.INSTANCE);
            augmentDescriptions.remove(AugmentAmplify.INSTANCE);
            augmentDescriptions.remove(AugmentDampen.INSTANCE);
        }
    }

    @Override
    public void onResolve(HitResult hit, Level level, LivingEntity ignored, SpellStats stats,
                          SpellContext context, SpellResolver resolver) {
        if (!(level instanceof ServerLevel world) || !isEnabled()) return;
        var frame = CastContext.of(context, hit, null);
        if (frame.caster() == null || !frame.caster().isAlive()) return;
        firstCharge(context, stats.getAugments()).trigger(() -> MonstrosityCasting.supports(ability)
                ? MonstrosityCasting.effect(ability, world, frame, stats.getAmpMultiplier(), stats.getDurationMultiplier())
                : ScyllaCasting.supports(ability)
                ? ScyllaCasting.effect(ability, world, frame, stats.getAmpMultiplier(), stats.getDurationMultiplier())
                : NativePayloads.effect(ability, world, frame, stats.getAmpMultiplier()));
    }

    public ResourceCharge firstCharge(SpellContext context, List<AbstractAugment> augments) {
        return Resources.quote(context, ability, augments);
    }

    public int getDefaultManaCost() { return 0; }
    public int getCastingCost() { return 0; }
    public SpellTier defaultTier() { return SpellTier.ONE; }
    protected Set<AbstractAugment> getCompatibleAugments() {
        return Set.of(AugmentAmplify.INSTANCE, AugmentDampen.INSTANCE);
    }

    public void addAugmentDescriptions(Map<AbstractAugment, String> descriptions) {
        // Ars invokes this from its superclass constructor, before ability is assigned.
        descriptions.put(AugmentAmplify.INSTANCE, Component.translatable("cataclysm_nouveau.augment.increase_damage").getString());
        descriptions.put(AugmentDampen.INSTANCE, Component.translatable("cataclysm_nouveau.augment.decrease_damage").getString());
    }

    public String getBookDescription() {
        return Component.translatable("cataclysm_nouveau.glyph_desc.glyph_" + ability.id).getString();
    }
}
