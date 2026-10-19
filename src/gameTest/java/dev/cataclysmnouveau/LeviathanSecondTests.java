package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import com.github.L_Ender.cataclysm.entity.util.TidalTentacleUtil;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_leviathan_second") @PrefixGameTestTemplate(false)
public final class LeviathanSecondTests {
    @GameTest(template="empty",batch="lev_second_0",timeoutTicks=100)
    public static void riftGrowthPullDamageAndTerrain(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var p=a.center.add(0,3,0);var enemy=a.target(p.add(2,0,0),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.DIMENSIONAL_RIFT,block(p),true);var rift=entities(a,Dimensional_Rift_Entity.class).getFirst();
            h.assertTrue(rift.getStage()==1 && rift.getLifespan()==600 && !rift.shouldBeSaved(),"Native staged transient rift");
            tick(rift,1);h.assertTrue(enemy.getDeltaMovement().x<0,"Native pull affects enemy");near(h,friend.getDeltaMovement().length(),0,"No allied pull");
            enemy.setPos(p);enemy.invulnerableTime=0;tick(rift,4);near(h,500-enemy.getHealth(),12,"Native ten plus bounded amplify two");near(h,friend.getHealth(),500,"No allied contact damage");
            for(int i=0;i<38;i++)ExecutionSessions.tick();h.assertTrue(rift.getStage()==2,"First native relative growth time");
            for(int i=38;i<102;i++)ExecutionSessions.tick();h.assertTrue(rift.getStage()==4,"Own rift grows to native maximum");
            h.assertTrue(h.getLevel().getBlockState(a.base).isSolid(),"Native terrain-lifting branch disabled");
            near(h,CapabilityRegistry.getMana(a.p).getCurrentMana(),9340,"Rift pays one quote, including amplification");
            h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_1",timeoutTicks=100)
    public static void riftClosingExplosionAndIndependentCasts(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var p=a.center.add(0,3,0);a.cast(GlyphCatalog.DIMENSIONAL_RIFT,block(p),false);
            var first=entities(a,Dimensional_Rift_Entity.class).getFirst();first.setStage(4);
            first.setLifespan(101);first.tickCount=519;var enemy=a.target(p.add(2,0,0),false);var ally=a.target(enemy.position(),true);
            for(int i=0;i<122 && !first.isRemoved();i++)tick(first,1);
            h.assertTrue(first.isRemoved() && enemy.getHealth()<500,"Native shrink reaches closing blast and removal");near(h,ally.getHealth(),500,"Collapse protects allies");
            a.cast(GlyphCatalog.DIMENSIONAL_RIFT,block(p.add(8,0,0)),false);var own=entities(a,Dimensional_Rift_Entity.class).getFirst();
            var unrelated=new Dimensional_Rift_Entity(h.getLevel(),p.x+10,p.y,p.z,a.p);unrelated.setStage(1);h.getLevel().addFreshEntity(unrelated);
            for(int i=0;i<102;i++)ExecutionSessions.tick();h.assertTrue(own.getStage()==4 && unrelated.getStage()==1,"Never upgrade someone else's native rift");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(own.isRemoved(),"Root disappearance cancels rift");unrelated.discard();h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_2",timeoutTicks=100)
    public static void tentacleNativeGateDamageChainsAndRetraction(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.setPos(a.center.add(0,2,-4));var enemy=a.target(a.center.add(0,2,0),false);var friend=a.target(enemy.position().add(1,0,0),true);
            a.cast(GlyphCatalog.TIDAL_TENTACLE,new EntityHitResult(friend),false);near(h,mana(a),10000,"Friendly recipient refunds");
            a.cast(GlyphCatalog.TIDAL_TENTACLE,new EntityHitResult(enemy),true);
            var root=entities(a,Tidal_Tentacle_Entity.class).getFirst();h.assertTrue(root.getCreatorEntityUUID().equals(a.p.getUUID()) && !root.shouldBeSaved(),"Native source and transient link");
            a.cast(GlyphCatalog.TIDAL_TENTACLE,new EntityHitResult(enemy),true);near(h,mana(a),9850,"Existing native chain prevents another paid launch");
            tick(root,6);h.assertTrue(enemy.hasEffect(ModEffect.EFFECTABYSSAL_CURSE),"Native attack and curse execute");near(h,500-enemy.getHealth(),5,"Native three plus amplify two");
            var links=entities(a,Tidal_Tentacle_Entity.class);h.assertTrue(links.size()>1 && links.stream().allMatch(e->((ManagedTidalTentacle)e).confluxLeviathan$chain()==((ManagedTidalTentacle)root).confluxLeviathan$chain()),"Child segments inherit the same chain");
            for(int i=0;i<100;i++)for(var link:entities(a,Tidal_Tentacle_Entity.class))tick(link,1);
            ExecutionSessions.tick();h.assertTrue(entities(a,Tidal_Tentacle_Entity.class).isEmpty() && TidalTentacleUtil.getLastTendon(a.p)==null,"Original backward retraction clears its pointer");
            h.assertTrue(!friend.hasEffect(ModEffect.EFFECTABYSSAL_CURSE) && friend.getHealth()==500,"Chain excludes allied recipients");near(h,mana(a),9850,"Children and retraction never pay again");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_3",timeoutTicks=100)
    public static void tentacleCleanupKeepsForeignPointerAndRejectsRange(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.p.setPos(a.center.add(0,2,-4));var enemy=a.target(a.center.add(0,2,0),false);
            a.cast(GlyphCatalog.TIDAL_TENTACLE,new EntityHitResult(a.target(a.center.add(0,2,20),false)),false);near(h,mana(a),10000,"Beyond native selection range refunds");
            a.cast(GlyphCatalog.TIDAL_TENTACLE,new EntityHitResult(enemy),false);var root=entities(a,Tidal_Tentacle_Entity.class).getFirst();
            var foreign=new Tidal_Tentacle_Entity(com.github.L_Ender.cataclysm.init.ModEntities.TIDAL_TENTACLE.get(),h.getLevel());foreign.setPos(a.center.add(10,2,0));h.getLevel().addFreshEntity(foreign);
            TidalTentacleUtil.setLastTentacle(a.p,foreign);LeviathanTentacles.pointerMetadata(a.p,foreign);
            h.assertTrue(TidalTentacleUtil.getLastTendon(a.p)==foreign,"Foreign pointer fixture has actual native IDs");a.p.discard();ExecutionSessions.tick();
            h.assertTrue(root.isRemoved() && !foreign.isRemoved(),"Only owned segments are cancelled");h.assertTrue(TidalTentacleUtil.getLastTendon(a.p)==foreign,"Cleanup never clears a foreign pointer");foreign.discard();h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_4",timeoutTicks=100)
    public static void minefieldNativeCountDelayAndGroupedFee(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            a.cast(GlyphCatalog.ABYSS_MINEFIELD,block(a.center.add(0,10,0)),true);near(h,mana(a),9540,"Single array quote");
            for(int i=0;i<30;i++)ExecutionSessions.tick();h.assertTrue(entities(a,Abyss_Mine_Entity.class).isEmpty(),"Native preparation retained");
            ExecutionSessions.tick();var mines=entities(a,Abyss_Mine_Entity.class);
            h.assertTrue(!mines.isEmpty() && mines.size()<=35,"Native space-checked mine distribution");
            h.assertTrue(mines.stream().allMatch(m->m.getCaster()==a.p && ((LeviathanArea)m).confluxLeviathan$lifetime()==800 && !m.shouldBeSaved()),"Root owner, original lifetime and managed children");
            var all=new ArrayList<Entity>(mines);a.p.discard();ExecutionSessions.tick();h.assertTrue(all.stream().allMatch(Entity::isRemoved),"Array cancellation clears every owned mine");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_5",timeoutTicks=100)
    public static void bombardmentNativePhasesLiveTargetAndChildCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var target=a.target(a.center,false);a.cast(GlyphCatalog.ABYSS_BOMBARDMENT_ARRAY,new EntityHitResult(target),false);
            near(h,mana(a),9400,"One bombardment quote");for(int i=0;i<55;i++)ExecutionSessions.tick();h.assertTrue(entities(a,Abyss_Blast_Portal_Entity.class).isEmpty(),"Native 56-tick preparation");
            ExecutionSessions.tick();var first=entities(a,Abyss_Blast_Portal_Entity.class);h.assertTrue(!first.isEmpty() && first.size()<=10,"Nine random attempts plus first aimed portal");
            var aimed=first.stream().filter(p->p.position().distanceToSqr(target.position())<.1).findFirst().orElseThrow();tick(aimed,22);
            var beam=((LeviathanPortal)aimed).confluxLeviathan$child();h.assertTrue(beam!=null && beam.caster==a.p,"Native child beam retains real caster");
            target.setPos(a.center.add(4,0,0));for(int i=56;i<76;i++)ExecutionSessions.tick();
            h.assertTrue(entities(a,Abyss_Blast_Portal_Entity.class).stream().anyMatch(p->p.position().distanceToSqr(target.position())<.1),"Second aimed portal follows current target");
            target.setPos(a.center.add(-4,0,0));for(int i=76;i<96;i++)ExecutionSessions.tick();
            var all=entities(a,Abyss_Blast_Portal_Entity.class);h.assertTrue(all.size()<=12 && all.stream().anyMatch(p->p.position().distanceToSqr(target.position())<.1),"Third aimed native phase");
            a.p.discard();ExecutionSessions.tick();h.assertTrue(all.stream().allMatch(Entity::isRemoved) && beam.isRemoved(),"Cancel all parents and existing rays");h.succeed();
        }
    }
    @GameTest(template="empty",batch="lev_second_6",timeoutTicks=100)
    public static void noGroundBombardmentRefundAndCreationEmptyMana(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            // Native search reaches fifty blocks down: move its whole column above the floor.
            a.cast(GlyphCatalog.ABYSS_BOMBARDMENT_ARRAY,block(a.center.add(0,90,0)),false);near(h,mana(a),10000,"No eligible native portal means startup refund");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.DIMENSIONAL_RIFT,block(a.center.add(0,3,0)),false);
            h.assertTrue(entities(a,Dimensional_Rift_Entity.class).isEmpty(),"Empty mana creates no rift");
            a.p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);a.cast(GlyphCatalog.DIMENSIONAL_RIFT,block(a.center.add(0,3,0)),false);
            h.assertTrue(entities(a,Dimensional_Rift_Entity.class).size()==1,"Creative bypasses empty mana");near(h,mana(a),0,"Creative payment is free");h.succeed();
        }
    }
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void tick(Entity e,int count){for(int i=0;i<count && !e.isRemoved();i++){++e.tickCount;e.tick();}}
    private static <T extends Entity> List<T> entities(NativeAbilityTests.Arena a,Class<T> type){return a.h.getLevel().getEntitiesOfClass(type,new AABB(a.center,a.center).inflate(64));}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double actual,double expected,String message){h.assertTrue(Math.abs(actual-expected)<.001,message+": "+actual+" != "+expected);}
}
