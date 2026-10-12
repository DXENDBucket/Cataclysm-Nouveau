package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import dev.cataclysmnouveau.CataclysmConfig;
import net.minecraft.world.entity.Entity;

/** Limit only the health-based part of audited native hits, before augmentation. */
public final class HealthDamage {
    public static boolean enabled(Entity source) {
        return !source.level().isClientSide && CataclysmConfig.capHealthDamage()
                && NativeDamageContexts.context(source)!=null;
    }
    public static float limit(float total,double fixed) {
        return (float)Math.min(total,Math.max(0,fixed)*2);
    }
    public static float limit(Entity source,float total) {
        if(!enabled(source))return total;
        if(source instanceof Ignis_Abyss_Fireball_Entity)return limit(total,10);
        if(source instanceof Ignis_Fireball_Entity fire)return limit(total,fire.isSoul()?8:6);
        if(source instanceof Lightning_Storm_Entity storm)return limit(total,storm.getDamage());
        return total;
    }
    /** Keep the native expression intact; only constrain the health input to that expression. */
    public static float healthInput(Entity source,float health,double fixed,float fraction) {
        return enabled(source) && fraction>0?(float)Math.min(health,Math.max(0,fixed)/fraction):health;
    }
    private HealthDamage() {}
}
