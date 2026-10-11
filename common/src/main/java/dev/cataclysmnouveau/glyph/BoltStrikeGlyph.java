package dev.cataclysmnouveau.glyph;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.glyph.TriggerPaidGlyph;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import java.util.*;
public final class BoltStrikeGlyph extends AbstractEffect implements TriggerPaidGlyph {
    public BoltStrikeGlyph() { super(CarrierRegistry.id("cataclysm_nouveau:glyph_bolt_strike"),"Bolt Strike"); }
    public void onResolve(HitResult hit,Level level,LivingEntity ignored,SpellStats stats,SpellContext context,SpellResolver resolver) {
        if (!(level instanceof ServerLevel world) || !isEnabled()) return;
        var frame=CastContext.of(context,hit,null);
        if (frame.caster()==null || !frame.caster().isAlive()) return;
        firstCharge(context,stats.getAugments()).trigger(()->NativePayloads.bolt(world,frame,stats.getAmpMultiplier()));
    }
    public ResourceCharge firstCharge(SpellContext context,List<AbstractAugment> augments) { return Resources.quote(context,Ability.BOLT_STRIKE,augments); }
    public int getDefaultManaCost() { return 0; }
    public int getCastingCost() { return 0; }
    public SpellTier defaultTier() { return SpellTier.ONE; }
    protected Set<AbstractAugment> getCompatibleAugments() { return Set.of(AugmentAmplify.INSTANCE,AugmentDampen.INSTANCE); }
    public void addAugmentDescriptions(Map<AbstractAugment,String> descriptions) {
        descriptions.put(AugmentAmplify.INSTANCE,net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.increase_damage").getString());
        descriptions.put(AugmentDampen.INSTANCE,net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.decrease_damage").getString());
    }
    public String getBookDescription() { return net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.glyph_desc.glyph_bolt_strike").getString(); }
}
