package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Ignited_Berserker_Entity;
import com.github.L_Ender.cataclysm.init.*;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.casting.*;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("cataclysm_nouveau_berserker") @PrefixGameTestTemplate(false)
public final class BerserkerTests {
    @GameTest(template="empty",batch="bers_0",timeoutTicks=100)
    public static void nativeCrossTimingBrandHealingDamageAndArsBonus(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=arena(h,frames)){
            a.p.setYRot(0);a.p.setHealth(5);a.p.getAttribute(PerkAttributes.SPELL_DAMAGE_BONUS).setBaseValue(3);
            var enemy=a.target(a.center.add(0,0,2),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.BERSERKER_CROSS_SLASH,block(a.center),true);near(h,mana(a),9900,"One augmented ninety-mana quote");step(16);near(h,enemy.getHealth(),500,"No early strike");step(1);
            near(h,500-enemy.getHealth(),14.375,"Native 7.5 times 1.25, bounded plus2, Ars equipment plus3 exactly once");
            near(h,a.p.getHealth(),7,"Detached recovery reaches original caster");near(h,friend.getHealth(),500,"Core friend excluded");
            var brand=enemy.getEffect(ModEffect.EFFECTBLAZING_BRAND);h.assertTrue(brand!=null && brand.getDuration()==100 && brand.getAmplifier()==0,"Native first brand and duration");
            h.assertTrue(enemy.getLastDamageSource().getEntity()==a.p,"Actual caster credited");enemy.invulnerableTime=0;step(18);
            near(h,500-enemy.getHealth(),28.75,"Second native slash timing");near(h,a.p.getHealth(),11,"Second stacked brand heals four");h.assertTrue(enemy.getEffect(ModEffect.EFFECTBLAZING_BRAND).getAmplifier()==1,"Brand caps at native level two");
            step(15);h.assertTrue(frames.getLast().closed(),"Cross recovery ends at native fifty ticks");near(h,mana(a),9900,"Multiple hits are included in one payment");h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_1",timeoutTicks=100)
    public static void swordDanceNativeFourHitsImpulsesAndAllyAim(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=arena(h,frames)){
            var host=a.target(a.center,true);host.setYRot(90);host.setHealth(100);a.p.setYRot(0);a.p.setHealth(5);
            var enemy=a.target(a.center.add(-2,0,0),false);a.cast(GlyphCatalog.BERSERKER_SWORD_DANCE,new EntityHitResult(host),false);
            var strikes=new ArrayList<Integer>();float previous=500;
            for(int t=1;t<=55;t++){enemy.invulnerableTime=0;step(1);if(enemy.getHealth()<previous){strikes.add(t);near(h,previous-enemy.getHealth(),7.5,"Native strike damage");previous=enemy.getHealth();}}
            boolean left=strikes.equals(List.of(13,20,25,30));h.assertTrue(left || strikes.equals(List.of(17,25,28,35)),"Complete native left/right cadence: "+strikes);
            near(h,host.getHealth(),114,"Native brand recovery belongs to allied executor");near(h,a.p.getHealth(),5,"Payer receives no allied healing");
            near(h,host.getDeltaMovement().x,left?-1.8:-1.35,"Original left/right impulse schedule retained");near(h,host.getDeltaMovement().z,0,"Impulse follows ally view, not payer");
            near(h,mana(a),9850,"One group fee");h.assertTrue(!BerserkerCasting.isActing(host),"Native dance completes and releases AI");h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_2",timeoutTicks=100)
    public static void spinNativeThreePhasesRepeatedContactAndNoHealing(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=arena(h,frames)){
            a.p.setYRot(0);a.p.setHealth(5);var enemy=a.target(a.center.add(.5,0,0),false);var friend=a.target(enemy.position(),true);
            a.cast(GlyphCatalog.BERSERKER_SPIN,block(a.center),false);step(30);near(h,enemy.getHealth(),500,"Thirty ticks native windup");
            h.assertTrue(frames.getLast().animation()==3,"Windup enters looping spin");int hits=0;
            for(int t=31;t<=120;t++){enemy.invulnerableTime=0;step(1);if(t%4==0)hits++;}
            near(h,500-enemy.getHealth(),hits*7.5,"Native four-tick attempts with reset fixture immunity");near(h,a.p.getHealth(),5,"Spin has no native recovery");near(h,friend.getHealth(),500,"Spin protects friends");h.assertTrue(!enemy.hasEffect(ModEffect.EFFECTBLAZING_BRAND),"Spin does not invent slash branding");
            h.assertTrue(enemy.getDeltaMovement().x>0 && frames.getLast().animation()==4,"Native push and finish phase");float hp=enemy.getHealth();step(40);near(h,enemy.getHealth(),hp,"Finish phase has no free damage");
            near(h,mana(a),9820,"Whole three-phase spin pays once");h.assertTrue(frames.getLast().closed(),"Full native 30+90+40 lifecycle");h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_3",timeoutTicks=100)
    public static void detachedDanceMovesInsteadOfSnappingBack(GameTestHelper h){
        var frames=new ArrayList<BodyPhantomPayload>();
        try(var a=arena(h,frames)){
            a.p.setYRot(0);var root=a.p.position();a.cast(GlyphCatalog.BERSERKER_SWORD_DANCE,block(a.center),false);step(40);
            var last=frames.getLast();h.assertTrue(last.positionAnchored() && last.position().z>a.center.z+.3,"Native impulse moves the detached collision frame");
            near(h,a.p.position().distanceTo(root),0,"Position execution never drags payer");h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_4",timeoutTicks=100)
    public static void replacementAuthorityCreativeAndOwnerCleanup(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var host=a.target(a.center,true);var enemy=a.target(a.center.add(2,0,0),false);
            a.cast(GlyphCatalog.BERSERKER_SPIN,new EntityHitResult(enemy),false);near(h,mana(a),10000,"Hostile executor rejected without fallback or fee");
            a.cast(GlyphCatalog.BERSERKER_SPIN,new EntityHitResult(host),false);h.assertTrue(BerserkerCasting.isActing(host),"Executor action claimed");
            CapabilityRegistry.getMana(a.p).setMana(0);a.cast(GlyphCatalog.APTRGANGR_SMASH,new EntityHitResult(host),false);h.assertTrue(BerserkerCasting.isActing(host),"Unfunded replacement leaves existing action");
            CapabilityRegistry.getMana(a.p).setMana(10000);a.cast(GlyphCatalog.APTRGANGR_SMASH,new EntityHitResult(host),false);h.assertTrue(!BerserkerCasting.isActing(host) && AptrgangrCasting.isActing(host),"Cross-series replacement releases only old action");
            a.cast(GlyphCatalog.BERSERKER_CROSS_SLASH,new EntityHitResult(host),false);h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());step(1);h.assertTrue(!BerserkerCasting.isActing(host),"Revoked alliance stops immediately");
            CapabilityRegistry.getMana(a.p).setMana(0);a.p.setGameMode(GameType.CREATIVE);a.cast(GlyphCatalog.BERSERKER_SWORD_DANCE,new EntityHitResult(a.p),false);h.assertTrue(BerserkerCasting.isActing(a.p),"Creative empty mana can execute");near(h,mana(a),0,"Creative fee bypass");
            a.p.discard();step(1);h.assertTrue(!BerserkerCasting.isActing(a.p),"Removed owner releases actor control");h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_5",timeoutTicks=100)
    public static void nativeUnboundBerserkerRemainsNative(GameTestHelper h){
        try(var a=new NativeAbilityTests.Arena(h)){
            var nativeMob=new Ignited_Berserker_Entity(ModEntities.IGNITED_BERSERKER.get(),h.getLevel());nativeMob.setPos(a.center);nativeMob.setYRot(0);nativeMob.yBodyRot=0;nativeMob.setHealth(10);nativeMob.setAttackState(1);nativeMob.attackTicks=17;
            var enemy=a.target(a.center.add(0,0,2),false);nativeMob.aiStep();near(h,500-enemy.getHealth(),9.375,"Unbound native damage unchanged");near(h,nativeMob.getHealth(),12,"Unbound recovery remains on native creature");h.assertTrue(enemy.getLastDamageSource().getEntity()==nativeMob,"Native damage owner unchanged");nativeMob.discard();h.succeed();
        }
    }
    @GameTest(template="empty",batch="bers_6",timeoutTicks=100)
    public static void allBodyPhantomStatesRoundTrip(GameTestHelper h){NativeAbilityTests.bodyPhantomProtocolTracksNativeStatesAndActualExecutor(h);}
    @GameTest(template="empty",batch="bers_7",timeoutTicks=100)
    public static void smashRegression(GameTestHelper h){AptrgangrTests.smashNativeTickDamageFanParticlesAndNaturalChildLifetime(h);}
    private static NativeAbilityTests.Arena arena(GameTestHelper h,List<BodyPhantomPayload> frames){return new NativeAbilityTests.Arena(h,p->{if(p instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket c && c.payload() instanceof BodyPhantomPayload f)frames.add(f);});}
    private static BlockHitResult block(Vec3 p){return new BlockHitResult(p,Direction.UP,BlockPos.containing(p),false);}
    private static void step(int n){for(int i=0;i<n;i++)ExecutionSessions.tick();}
    private static double mana(NativeAbilityTests.Arena a){return CapabilityRegistry.getMana(a.p).getCurrentMana();}
    private static void near(GameTestHelper h,double x,double y,String m){h.assertTrue(Math.abs(x-y)<.003,m+": "+x+" != "+y);}
}
