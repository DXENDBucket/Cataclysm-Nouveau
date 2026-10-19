package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.NativeDamageContexts;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
/** Binding is opt-in; no native Boss or ordinary projectile gains Ars attributes. */
@Mixin(value={Blazing_Bone_Entity.class,Ashen_Breath_Entity.class,com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity.class,com.github.L_Ender.cataclysm.entity.projectile.EarthQuake_Entity.class,com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity.class,com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity.class,com.github.L_Ender.cataclysm.entity.projectile.Ancient_Desert_Stele_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Dimensional_Rift_Entity.class,com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Mine_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Portal_Abyss_Blast_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity.class,com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity.class,Phantom_Arrow_Entity.class,Phantom_Halberd_Entity.class,Amethyst_Cluster_Projectile_Entity.class,Lionfish_Spike_Entity.class,Player_Ceraunus_Entity.class,Wave_Entity.class,Flare_Bomb_Entity.class,
        Bolt_strike_Entity.class,Void_Rune_Entity.class,Flame_Jet_Entity.class,Elemental_Spear_Entity.class,
        Flame_Strike_Entity.class,Lightning_Storm_Entity.class,Lightning_Area_Effect_Entity.class,Storm_Serpent_Entity.class,
        Scylla_Entity.class,Netherite_Monstrosity_Entity.class,Laser_Beam_Entity.class,Wither_Missile_Entity.class,Wither_Homing_Missile_Entity.class,Wither_Howitzer_Entity.class,Death_Laser_Beam_Entity.class,Wither_Smoke_Effect_Entity.class,Spark_Entity.class,Lava_Bomb_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity.class,Ender_Guardian_Bullet_Entity.class,Void_Vortex_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity.class,Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class,com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity.class},remap=false)
public abstract class DamageAugmentationStateMixin implements dev.cataclysmnouveau.casting.DamageAugmented {
    @org.spongepowered.asm.mixin.Unique private double nativeArs$augmentation;
    public double confluxCataclysm$augmentation(){return nativeArs$augmentation;}
    public void confluxCataclysm$augmentation(double value){nativeArs$augmentation=value;}
}
