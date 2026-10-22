package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.GlyphCatalog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import java.util.function.Supplier;

/** Native rune rings and shards keep their own lifecycle after the paid parent impact. */
public final class VoidPayloads {
    public static boolean child(Entity parent,Entity child,Supplier<Boolean> spawn) {
        var cast=NativeSpellDamage.context(parent);
        if(cast==null || !(parent.level() instanceof ServerLevel world))return spawn.get();
        if(!(child instanceof Void_Rune_Entity) && !(child instanceof Void_Shard_Entity))return spawn.get();
        if(!world.hasChunkAt(child.blockPosition()))return false;
        var source=((ManagedProjectile)parent).confluxCataclysm$sourceAbility();
        NativeDamageContexts.bind(child,cast,NativeDamageContexts.amplification(parent));
        if(child instanceof GuardianChild state)state.confluxGuardian$managed(true);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,child,200,new ExecutionDriver<Entity>() {
            public boolean start(ExecutionSession<Entity> session){return spawn.get();}
            public boolean tick(ExecutionSession<Entity> session){return child.isRemoved() || !world.hasChunkAt(child.blockPosition());}
            public void close(ExecutionSession<Entity> session,ExecutionSession.EndReason reason){child.discard();}
        },hit->{},()->source!=null && GlyphCatalog.projectile(source).isEnabled(),ExecutionScope.DIRECT));
    }
    private VoidPayloads() {}
}
