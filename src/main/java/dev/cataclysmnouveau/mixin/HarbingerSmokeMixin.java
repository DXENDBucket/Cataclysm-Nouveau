package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.HarbingerSmoke;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Wither_Smoke_Effect_Entity.class,remap=false)
public abstract class HarbingerSmokeMixin extends Entity implements HarbingerSmoke {
    @Unique private float harbinger$bonus;
    protected HarbingerSmokeMixin(EntityType<?> type,Level level){super(type,level);}
    public float confluxHarbinger$bonus(){return harbinger$bonus;}
    public void confluxHarbinger$bonus(float v){harbinger$bonus=v;}
    @Override public boolean shouldBeSaved(){return NativeSpellDamage.context(this)==null && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void harbinger$alive(CallbackInfo ci){var c=NativeSpellDamage.context(this);if(c!=null && (c.caster()==null || !c.caster().isAlive() || c.caster().level()!=level())){discard();ci.cancel();}}
}
