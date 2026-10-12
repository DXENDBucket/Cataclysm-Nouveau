package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Ignis_Entity.class,remap=false)
public interface IgnisAttackAccess {
    @Invoker(value="bladeFireball",remap=false) void confluxIgnis$blade(float radius,float offset,float height,int delay);
}
