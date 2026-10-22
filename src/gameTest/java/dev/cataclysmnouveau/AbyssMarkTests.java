package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.effect.Abyss_Mark_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_mark") @PrefixGameTestTemplate(false)
public final class AbyssMarkTests {
    @GameTest(template="empty",batch="mark_0",timeoutTicks=100)
    public static void pursuitNativeDelayChildSourceAndGrowth(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(3);
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.ABYSS_MARK,new EntityHitResult(enemy),true);
            var mark=marks(a).getFirst();var state=(AbyssMark)mark;
            near(h,mana(a),9670,"One mark fee including amplification");
            h.assertTrue(state.confluxMark$managed() && !mark.shouldBeSaved() && mark.getCreatorEntity()==a.p,"Native renderer entity is transient and owned by real caster");
            var origin=mark.position();enemy.setPos(a.center.add(6,0,0));tick(mark,79);
            h.assertTrue(mark.getX()>origin.x+4 && !mark.isRemoved() && state.confluxMark$portal()==null,"Native pursuit and eighty-tick delay retained");
            tick(mark,1);ExecutionSessions.tick();var portal=state.confluxMark$portal();
            h.assertTrue(mark.isRemoved() && portal!=null && !portal.isRemoved(),"Normal mark expiry leaves its delayed portal alive");
            h.assertTrue(!portal.shouldBeSaved() && NativeSpellDamage.context(portal).caster()==a.p,"Portal inherits transient ownership");
            tick(portal,22);var beam=((LeviathanPortal)portal).confluxLeviathan$child();
            h.assertTrue(beam!=null && beam.caster==a.p && !beam.shouldBeSaved(),"Full native portal emits real child ray");
            enemy.setPos(portal.position().add(0,4,0));enemy.invulnerableTime=0;var friend=a.target(enemy.position(),true);
            tick(beam,21);near(h,500-enemy.getHealth(),15.6,"Native10 plus half a point health term, bounded2.1, Ars3 once");
            enemy.setHealth(500);enemy.invulnerableTime=0;beam.setHpDamage(100);tick(beam,1);
            near(h,500-enemy.getHealth(),27,"Native health term remains capped by base damage");
            near(h,friend.getHealth(),500,"Child ray protects Core allies");
            h.assertTrue(enemy.hasEffect(ModEffect.EFFECTABYSSAL_BURN) && enemy.getLastDamageSource().getEntity()==a.p,"Burn and real caster attribution");
            near(h,mana(a),9670,"Portal and beam are included in initial fee");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(portal.isRemoved() && beam.isRemoved(),"Root loss removes entire chain");h.succeed();
        }
    }
    @GameTest(template="empty",batch="mark_1",timeoutTicks=100)
    public static void invalidTargetsEmptyManaAndCreative(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.cast(GlyphCatalog.ABYSS_MARK,new EntityHitResult(a.target(a.center,true)),false);
            a.cast(GlyphCatalog.ABYSS_MARK,new BlockHitResult(a.center,Direction.UP,BlockPos.containing(a.center),false),false);
            near(h,mana(a),10000,"Allies and blocks fail free");h.assertTrue(marks(a).isEmpty(),"No invalid mark");
            var enemy=a.target(a.center,false);CapabilityRegistry.getMana(a.p).setMana(0);
            a.cast(GlyphCatalog.ABYSS_MARK,new EntityHitResult(enemy),false);h.assertTrue(marks(a).isEmpty(),"No unfunded mark");
            a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.ABYSS_MARK,new EntityHitResult(enemy),false);
            h.assertTrue(marks(a).size()==1,"Creative still works without mana");near(h,mana(a),0,"Creative stays free");h.succeed();
        }
    }
    @GameTest(template="empty",batch="mark_2",timeoutTicks=100)
    public static void targetGoneKeepsLastPointAndNaturalEnd(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var enemy=a.target(a.center,false);a.cast(GlyphCatalog.ABYSS_MARK,new EntityHitResult(enemy),false);
            var mark=marks(a).getFirst();tick(mark,5);var point=mark.position();enemy.discard();tick(mark,75);
            var portal=((AbyssMark)mark).confluxMark$portal();h.assertTrue(portal!=null,"Target death keeps native delayed last-location output");
            near(h,portal.position().distanceTo(point),0,"No teleport to caster or removed victim");
            tick(portal,22);var beam=((LeviathanPortal)portal).confluxLeviathan$child();
            tick(beam,190);tick(portal,270);ExecutionSessions.tick();
            h.assertTrue(portal.isRemoved() && beam.isRemoved(),"Native beam fade and portal natural expiry");h.succeed();
        }
    }
    @GameTest(template="empty",batch="mark_3",timeoutTicks=100)
    public static void existingPortalRegression(GameTestHelper h){LeviathanTests.portalChildNativeDirectionDamageAndCleanup(h);}
    private static List<Abyss_Mark_Entity> marks(NativeAbilityTests.Arena a){return a.h.getLevel().getEntitiesOfClass(Abyss_Mark_Entity.class,new AABB(a.center,a.center).inflate(35),e->e.getCreatorEntityUUID()!=null && e.getCreatorEntityUUID().equals(a.p.getUUID()));}
    private static void tick(Entity e,int n){for(int i=0;i<n && !e.isRemoved();i++){++e.tickCount;e.tick();}}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double x,double y,String m){h.assertTrue(Math.abs(x-y)<.005,m+": "+x+" != "+y);}
}
