package dev.cataclysmnouveau.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(targets="com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity$RemnantMonolithAttackGoal",remap=false)
public interface RemnantMonolithAccess {
    @Invoker("spawnSpikeLine") void confluxRemnant$stele(double x,double z,double y,float rotation,int delay);
}
