package dev.cataclysmnouveau;
import com.github.L_Ender.cataclysm.capabilities.TidalTentacleCapability;
import com.github.L_Ender.cataclysm.init.ModCapabilities;
import com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity;
import net.minecraft.world.entity.LivingEntity;
public final class ForgeNative {
 public static TidalTentacleCapability.ITentacleCapability tentacles(LivingEntity owner){return ModCapabilities.getCapability(owner,ModCapabilities.TENTACLE_CAPABILITY);}
 public static void accelerateStorm(Cursed_Sandstorm_Entity p,double scale){p.xPower*=scale;p.yPower*=scale;p.zPower*=scale;}
 private ForgeNative(){}
    public static net.minecraft.world.effect.MobEffectInstance rageLayer(int duration,int amplifier,net.minecraft.world.effect.MobEffectInstance hidden) {
        var effect=new net.minecraft.world.effect.MobEffectInstance(CataclysmEffects.CURSED_RAGE.get(),duration,amplifier,false,true,true);
        ((dev.cataclysmnouveau.mixin.ForgeHiddenEffectAccess)(Object)effect).confluxCataclysm$hidden(hidden);
        return effect;
    }
}
