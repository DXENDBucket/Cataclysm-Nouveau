package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;

/** Opt-in context for native Scylla payloads; ordinary Boss entities remain untouched. */
public interface ScyllaPayload {
    CastContext confluxScylla$cast();
    void confluxScylla$cast(CastContext cast);
}
