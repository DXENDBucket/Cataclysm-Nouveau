package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Void_Rune_Entity;
import dev.cataclysmnouveau.casting.GuardianChild;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Void_Rune_Entity.class,remap=false)
public abstract class GuardianRuneStateMixin extends Entity implements GuardianChild {
    @Unique private boolean guardian$managed;
    protected GuardianRuneStateMixin(EntityType<?> type,Level level){super(type,level);}
    public boolean confluxGuardian$managed(){return guardian$managed;}
    public void confluxGuardian$managed(boolean value){guardian$managed=value;}
    @Override public boolean shouldBeSaved(){return !guardian$managed && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void guardian$rootAlive(CallbackInfo ci){var cast=dev.arsconflux.api.damage.NativeSpellDamage.context(this);if(guardian$managed && cast!=null && (cast.caster()==null || !cast.caster().isAlive() || cast.caster().level()!=level())){discard();ci.cancel();}}
}
