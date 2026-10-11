package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import net.minecraft.world.entity.Entity;

/** Cataclysm-specific invocation frames; general attachments and damage processing live in Core. */
public final class NativeDamageContexts {
    public static CastContext context(Entity entity) {
        if (entity instanceof SpellScylla frame) return frame.cast;
        if (entity instanceof SpellMonstrosity frame) return frame.cast;
        if (entity instanceof ScyllaPayload frame && frame.confluxScylla$cast() != null)
            return frame.confluxScylla$cast();
        return NativeSpellDamage.context(entity);
    }
    private NativeDamageContexts() {}
}
