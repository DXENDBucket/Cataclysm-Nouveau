package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import net.minecraft.world.entity.Entity;

/** Cataclysm-specific invocation frames; general attachments and damage processing live in Core. */
public final class NativeDamageContexts {
    public static CastContext context(Entity entity) {
        if (entity instanceof SpellBerserker frame) return frame.cast;
        if (entity instanceof SpellAptrgangr frame) return frame.cast;
        if (entity instanceof SpellRemnant frame) return frame.cast;
        if (entity instanceof SpellHarbinger frame) return frame.cast;
        if (entity instanceof SpellGuardian frame) return frame.cast;
        if (entity instanceof SpellMaledictus frame) return frame.cast;
        if (entity instanceof SpellIgnis frame) return frame.cast;
        if (entity instanceof SpellScylla frame) return frame.cast;
        if (entity instanceof SpellMonstrosity frame) return frame.cast;
        if (entity instanceof ScyllaPayload frame && frame.confluxScylla$cast() != null)
            return frame.confluxScylla$cast();
        return NativeSpellDamage.context(entity);
    }
    public static <T extends Entity> T bind(T entity,CastContext cast,double amplify) {
        if(cast==null)return entity;
        NativeSpellDamage.bind(entity,cast);
        if(entity instanceof DamageAugmented state)state.confluxCataclysm$augmentation(amplify);
        if(entity instanceof ManagedProjectile state)state.confluxCataclysm$amplification(Resources.amplification(amplify));
        return entity;
    }
    public static double amplification(Entity source){return source instanceof DamageAugmented state?state.confluxCataclysm$augmentation():0;}
    private NativeDamageContexts() {}
}
