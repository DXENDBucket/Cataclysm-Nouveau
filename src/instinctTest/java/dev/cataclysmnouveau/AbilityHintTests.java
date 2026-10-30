package dev.cataclysmnouveau;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.glyph.*;
import dev.arsconflux.api.targeting.Allies;
import dev.arsinstinct.api.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("cataclysm_nouveau_instinct")
@PrefixGameTestTemplate(false)
public final class AbilityHintTests {
    @GameTest(template="empty", timeoutTicks=30)
    public static void allAbilitiesRegisteredAndReplacementTakesPriority(GameTestHelper h) {
        h.assertTrue(GlyphCatalog.ALL.size()==102,"Expected the maintained 102-glyph catalog");
        for(var glyph:GlyphCatalog.ALL) {
            var tags=GlyphAbilities.get(glyph);
            h.assertTrue(!tags.isEmpty(),"Missing runtime hint: "+glyph.getRegistryName());
            h.assertTrue(!tags.contains(AbilityTags.PLAYER_ONLY),"No current adapter requires a player");
        }
        for(var glyph:GlyphCatalog.PROJECTILES) {
            var intent=ProgramIntent.of(new Spell(MethodProjectile.INSTANCE,glyph,GlyphCatalog.MONSTROUS));
            h.assertTrue(intent.primary()==glyph&&intent.has(AbilityTags.ATTACK)&&intent.has(AbilityTags.ENEMY)
                    &&!intent.friendly(),"Replacement remains an enemy attack before a following buff");
        }
        h.assertTrue(!GlyphAbilities.get(GlyphCatalog.ABSORB).contains(AbilityTags.ALLY),
                "Lava Absorption heals its caster, not a selected ally");
        h.succeed();
    }

    @GameTest(template="empty", timeoutTicks=30)
    public static void nativeReceiverSemanticsDriveInstinctSelection(GameTestHelper h) throws Exception {
        var actor=h.spawn(EntityType.ZOMBIE,2,3,2);actor.setNoAi(true);
        var ally=h.spawn(EntityType.COW,6,3,2);ally.setNoAi(true);
        var enemy=h.spawn(EntityType.PIG,11,3,2);enemy.setNoAi(true);
        try(var ignored=Allies.register(ResourceLocation.parse("cataclysm_nouveau:hint_test_"+actor.getUUID()),
                (first,second)->first==actor&&second==ally)) {
            for(var effect:new com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart[]{
                    GlyphCatalog.SPIN,GlyphCatalog.HALBERD_CLEAVE,GlyphCatalog.ASHEN_BREATH}) {
                h.assertTrue(CombatTargets.recipient(actor,enemy,new Spell(MethodProjectile.INSTANCE,effect),48,e->true)==ally,
                        "An allied receiver performs the native action, regardless of its species or boss source");
                h.assertTrue(CombatTargets.recipient(actor,enemy,new Spell(MethodSelf.INSTANCE,effect),12,e->true)==actor,
                        "The same action can be performed by the caster");
            }
            var pointSpell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.FIELD);
            h.assertTrue(CombatTargets.recipient(actor,enemy,pointSpell,48,e->true)==enemy,
                    "A field is centered on the enemy, not redirected to an allied executor");
            var touch=new Spell(MethodTouch.INSTANCE,GlyphCatalog.FRACTURE);
            double range=new NativeSpellBackend().describe(actor,ItemStack.EMPTY,touch).range();
            h.assertTrue(range==3&&CombatTargets.recipient(actor,enemy,touch,range,e->true)==null,
                    "Touch does not become a ranged attack");
            enemy.setPos(actor.getX()+2,actor.getY(),actor.getZ());
            h.assertTrue(CombatTargets.recipient(actor,enemy,touch,range,e->true)==enemy,
                    "Touch can select an enemy once within range");
        }
        h.succeed();
    }

    @GameTest(template="empty", timeoutTicks=30)
    public static void appliedBuffsAndCappedRageAreNotRepeated(GameTestHelper h) throws Exception {
        var actor=h.spawn(EntityType.ZOMBIE,2,3,2);actor.setNoAi(true);
        var receiver=h.spawn(EntityType.COW,4,3,2);receiver.setNoAi(true);
        try(var ignored=Allies.register(ResourceLocation.parse("cataclysm_nouveau:buff_test_"+actor.getUUID()),
                (first,second)->first==actor&&second==receiver)) {
            var cast=CastContext.detached(actor,new EntityHitResult(receiver),null);
            for(var glyph:new dev.cataclysmnouveau.glyph.CataclysmEffectGlyph[]{
                    GlyphCatalog.MONSTROUS,GlyphCatalog.LAVA_WALKING,GlyphCatalog.WATER_WALKING}) {
                h.assertTrue(GlyphConditions.useful(glyph,actor,receiver),"Fresh buff is useful");
                boolean applied=glyph.ability==Ability.WATER_WALKING
                        ? ScyllaActions.effect(glyph.ability,h.getLevel(),cast,0,1)
                        : MonstrosityCasting.effect(glyph.ability,h.getLevel(),cast,0,1);
                h.assertTrue(applied,"Native allied buff applies");
                h.assertTrue(!GlyphConditions.useful(glyph,actor,receiver),"Already active buff is skipped by AI");
            }
            h.assertTrue(GlyphConditions.useful(GlyphCatalog.RAGE,actor,receiver),"Rage can start below cap");
            MaledictusRage.gain(receiver,MaledictusRage.MAX_STACKS);
            h.assertTrue(!GlyphConditions.useful(GlyphCatalog.RAGE,actor,receiver),"Full rage is not repeatedly refreshed by AI");
            MaledictusRage.consume(receiver,1);
            h.assertTrue(GlyphConditions.useful(GlyphCatalog.RAGE,actor,receiver),"Consumed rage becomes useful again");
        }
        h.succeed();
    }
}
