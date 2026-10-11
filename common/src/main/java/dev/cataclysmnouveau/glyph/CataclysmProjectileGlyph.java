package dev.cataclysmnouveau.glyph;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.projectile.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.casting.*;
import java.util.List;
public final class CataclysmProjectileGlyph extends AbstractAugment implements ProjectileForm {
    public final Ability ability;
    public CataclysmProjectileGlyph(Ability ability) { super(CarrierRegistry.id("cataclysm_nouveau:glyph_"+ability.id),ability.name);this.ability=ability; }
    public boolean replace(EntityProjectileSpell carrier,CarrierProfile profile) { return ProjectileCasting.replace(ability,carrier,profile); }
    public ResourceCharge initialCharge(SpellContext context,List<AbstractAugment> augments) { return Resources.quote(context,ability,augments); }
    public int getDefaultManaCost() { return 0; }
    public int getCastingCost() { return 0; }
    public SpellTier defaultTier() { return SpellTier.ONE; }
    public String getBookDescription() { return net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.glyph_desc.glyph_"+ability.id).getString(); }
}
