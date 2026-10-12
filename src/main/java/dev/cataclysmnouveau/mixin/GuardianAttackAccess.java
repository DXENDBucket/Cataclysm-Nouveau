package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Ender_Guardian_Entity.class,remap=false)
public interface GuardianAttackAccess {
    @Invoker(value="StompAttack",remap=false) void confluxGuardian$stomp();
}
