package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import dev.cataclysmnouveau.casting.MaledictusHalberd;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
@Mixin(value=Phantom_Halberd_Entity.class,remap=false)
public abstract class MaledictusHalberdMixin extends Entity implements MaledictusHalberd {
    @Unique private boolean confluxMaledictus$managed;
    protected MaledictusHalberdMixin(EntityType<?> type,Level world){super(type,world);}
    public void confluxMaledictus$managed(boolean value){confluxMaledictus$managed=value;}
    public boolean confluxMaledictus$managed(){return confluxMaledictus$managed;}
    @Override public boolean shouldBeSaved(){return !confluxMaledictus$managed && super.shouldBeSaved();}
}
