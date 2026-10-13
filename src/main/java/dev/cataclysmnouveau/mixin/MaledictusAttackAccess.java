package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=Maledictus_Entity.class,remap=false)
public interface MaledictusAttackAccess {
    @Accessor("combo") boolean confluxMaledictus$combo();
    @Invoker("StrikeWindmillHalberd") void confluxMaledictus$spiral(int branches,int count,double radius,double increment,double curve,int delay);
    @Invoker("StrikeHalberd") void confluxMaledictus$ring(int count,float angularCount,float offset,double radius,int delay);
}
