package dev.cataclysmnouveau.mixin.client;

import com.github.L_Ender.cataclysm.client.render.entity.Abyss_Blast_Renderer;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.cataclysmnouveau.casting.LeviathanBeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native first-person clipping belongs to the physical emitter, not its damage owner. */
@Mixin(value=Abyss_Blast_Renderer.class,remap=false)
public abstract class LeviathanBeamViewMixin {
    @Shadow private boolean clearerView;
    @Inject(method="render(Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/Abyss_Blast_Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at=@At(value="FIELD",target="Lcom/github/L_Ender/cataclysm/client/render/entity/Abyss_Blast_Renderer;clearerView:Z",opcode=181,shift=At.Shift.AFTER))
    private void leviathan$emitter(Abyss_Blast_Entity beam,float yaw,float delta,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci){
        var state=(LeviathanBeam)beam;
        if(!state.confluxLeviathan$managed())return;
        var client=Minecraft.getInstance();
        clearerView=client.player!=null && state.confluxLeviathan$executor()==client.player.getId() && client.options.getCameraType().isFirstPerson();
    }
}
