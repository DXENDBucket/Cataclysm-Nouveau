package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value={Blazing_Bone_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity.class,Phantom_Arrow_Entity.class,Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Flare_Bomb_Entity.class,Lava_Bomb_Entity.class,Spark_Entity.class,Laser_Beam_Entity.class,Wither_Missile_Entity.class,Wither_Homing_Missile_Entity.class,Wither_Howitzer_Entity.class,Ender_Guardian_Bullet_Entity.class,Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class},remap=false)
public abstract class NativeHitMixin {
    @Inject(method="onHit",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$hit(HitResult hit,CallbackInfo ci) {
        var state=(ManagedProjectile)this;
        if (state.confluxCataclysm$mode()==2 && !state.confluxCataclysm$resolving() && state.confluxCataclysm$impact()!=null) {
            state.confluxCataclysm$impact().accept(hit);ci.cancel();
        }
    }
}
