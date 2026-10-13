package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.AI.InternalAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import dev.cataclysmnouveau.casting.NativeInternalGoal;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=InternalAttackGoal.class,remap=false)
public interface NativeAttackGoalAccess extends NativeInternalGoal {
    @Accessor("attackstate") int confluxMaledictus$state();
    @Accessor("attackMaxtick") int confluxMaledictus$duration();
}
