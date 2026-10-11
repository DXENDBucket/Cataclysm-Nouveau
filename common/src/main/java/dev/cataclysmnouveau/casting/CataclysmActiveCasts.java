package dev.cataclysmnouveau.casting;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import dev.arsconflux.api.casting.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.*;

/** Native capabilities supply their own timing policy; Conflux supplies active-use transactions. */
public final class CataclysmActiveCasts implements ActiveCastProvider {
    private static final String DEADLINES="cataclysm_nouveau_cooldowns";
    public static void register() {ActiveCasts.register(CarrierRegistry.id("cataclysm_nouveau:cooldowns"),new CataclysmActiveCasts());}
    private static EnumSet<Ability> collect(List<Spell> spells) {
        var found=EnumSet.noneOf(Ability.class);
        for(var spell:spells) for(var part:ArsSpellAccess.parts(spell)) {
            if(part instanceof CataclysmProjectileGlyph glyph) found.add(glyph.ability);
            if(part instanceof CataclysmEffectGlyph glyph) found.add(glyph.ability);
        }
        return found;
    }
    private static boolean applies(LivingEntity caster) {
        return caster.level() instanceof ServerLevel && CataclysmConfig.cooldownsEnabled()
                && (!(caster instanceof Player player) || !player.isCreative() || CataclysmConfig.creativeCooldown());
    }
    public static long remaining(LivingEntity caster,Ability ability) {
        if(!applies(caster) || CataclysmConfig.cooldownTicks(ability)==0) return 0;
        long time=caster.level().getServer().overworld().getGameTime();
        return Math.max(0,caster.getPersistentData().getCompound(DEADLINES).getLong(ability.id)-time);
    }
    @Override public boolean supports(Spell spell) {return !collect(List.of(spell)).isEmpty();}
    @Override public boolean allowed(ActiveCastRequest request) {
        for(var ability:collect(List.of(request.spell()))) {
            long ticks=remaining(request.caster(),ability);
            if(ticks==0) continue;
            if(request.caster() instanceof ServerPlayer player) player.displayClientMessage(Component.translatable(
                    "cataclysm_nouveau.cast.cooldown",Component.translatable("cataclysm_nouveau.glyph_name.glyph_"+ability.id),String.format(Locale.ROOT,"%.1f",ticks/20.0)),true);
            return false;
        }
        return true;
    }
    @Override public void released(ActiveCastRequest request,List<Spell> recipes) {
        if(!applies(request.caster())) return;
        var persistent=request.caster().getPersistentData();var deadlines=persistent.getCompound(DEADLINES);
        long now=request.caster().level().getServer().overworld().getGameTime();
        for(var ability:collect(recipes)) {
            int duration=CataclysmConfig.cooldownTicks(ability);
            if(duration>0) deadlines.putLong(ability.id,Math.max(deadlines.getLong(ability.id),now+duration));
        }
        persistent.put(DEADLINES,deadlines);
    }
}
