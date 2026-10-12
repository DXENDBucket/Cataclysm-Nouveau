package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=The_Harbinger_Entity.class,remap=false)
public interface HarbingerAttackAccess {
    @Invoker("blockbreak") void confluxHarbinger$collision();
}
