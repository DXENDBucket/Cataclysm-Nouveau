package dev.cataclysmnouveau.mixin.client;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;

/** Pure native visual helper; calling a complete Boss tick would also run gameplay. */
@Mixin(value=Ignis_Entity.class,remap=false)
public interface IgnisVisualAccess {
    @Invoker("SwingParticles") void cataclysm$swingParticles();
    @Invoker("PatternParticle") void cataclysm$pattern(float height,float forward,float size,boolean soul);
    @Invoker("Sphereparticle") void cataclysm$sphere(float height,float forward,float size);
    @Invoker("StrikeParticle") void cataclysm$strike(float spread,int distance,float forward);
    @Accessor("prevBladePos") Vec3 cataclysm$previousBlade();
    @Accessor("prevBladePos") void cataclysm$previousBlade(Vec3 position);
}
