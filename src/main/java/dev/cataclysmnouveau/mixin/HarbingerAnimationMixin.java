package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.lionfishapi.server.animation.*;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import dev.cataclysmnouveau.casting.SpellHarbinger;
import dev.cataclysmnouveau.casting.SpellGuardian;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Detached native goals may request another charge; never broadcast an unspawned Boss ID. */
@Mixin(value=AnimationHandler.class,remap=false)
public abstract class HarbingerAnimationMixin {
    @Inject(method="sendAnimationMessage",at=@At("HEAD"),cancellable=true)
    private void harbinger$detached(Entity entity,Animation animation,CallbackInfo ci) {
        if(entity instanceof dev.cataclysmnouveau.casting.SpellIgnis frame){frame.setAnimation(animation);ci.cancel();return;}
        if(entity instanceof SpellGuardian frame){frame.setAnimation(animation);ci.cancel();return;}
        if(entity instanceof SpellHarbinger frame){
            if(frame.acceptingRepeat && animation==The_Harbinger_Entity.CHARGE_ANIMATION)frame.repeatRequested=true;
            frame.setAnimation(animation);ci.cancel();
        }
    }
}
