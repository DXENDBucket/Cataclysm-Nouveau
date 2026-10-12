package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.util.CustomExplosion.IgnisExplosion;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.IgnisCasting;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Keep Cataclysm's explosion body and native particle packets; opt in owner/ally/scaling rules. */
@Mixin(value={Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class},remap=false)
public abstract class IgnisExplosionMixin {
    @WrapOperation(method={"onHitEntity","onHitBlock"},at=@At(value="NEW",target="com/github/L_Ender/cataclysm/util/CustomExplosion/IgnisExplosion"))
    private IgnisExplosion ignis$explosion(Level world,Entity source,DamageSource damage,ExplosionDamageCalculator calculator,
            double x,double y,double z,float radius,boolean fire,Explosion.BlockInteraction interaction,Operation<IgnisExplosion> original) {
        if(world.isClientSide || NativeSpellDamage.context(source)==null)return original.call(world,source,damage,calculator,x,y,z,radius,fire,interaction);
        return IgnisCasting.explosion(world,source,x,y,z,radius);
    }
    @WrapOperation(method={"onHitEntity","onHitBlock"},at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/util/CustomExplosion/IgnisExplosion;explode()V"))
    private void ignis$damageScope(IgnisExplosion explosion,Operation<Void> original) {
        var source=(Entity)(Object)this;
        if(source.level().isClientSide){original.call(explosion);return;}
        NativeSpellDamage.during(NativeSpellDamage.context(source),()->{original.call(explosion);return null;});
    }
}
