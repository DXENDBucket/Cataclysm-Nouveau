package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.AI.InternalStateGoal;
import org.spongepowered.asm.mixin.Mixin;
import dev.cataclysmnouveau.casting.NativeInternalGoal;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=InternalStateGoal.class,remap=false)
public interface NativeStateGoalAccess extends NativeInternalGoal {
    @Accessor("attackstate") int confluxMaledictus$state();
    @Accessor("attackfinaltick") int confluxMaledictus$duration();
}
