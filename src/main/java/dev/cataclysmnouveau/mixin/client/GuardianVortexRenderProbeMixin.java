package dev.cataclysmnouveau.mixin.client;

import com.github.L_Ender.cataclysm.client.render.entity.Void_Vortex_Renderer;
import com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.cataclysmnouveau.client.VortexDiagnostics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client-list mixin: record native calls, never cancel or replace the renderer. */
@Mixin(value = Void_Vortex_Renderer.class, remap = false)
public abstract class GuardianVortexRenderProbeMixin {
    @Unique private Void_Vortex_Entity guardianDiagnostic$current;

    @Inject(method = "render(Lcom/github/L_Ender/cataclysm/entity/effect/Void_Vortex_Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void guardianDiagnostic$render(Void_Vortex_Entity entity, float yaw, float partial,
                                          PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        guardianDiagnostic$current = VortexDiagnostics.enabled() ? entity : null;
        if (guardianDiagnostic$current != null) VortexDiagnostics.rendered(entity);
    }

    @Inject(method = "render(Lcom/github/L_Ender/cataclysm/entity/effect/Void_Vortex_Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void guardianDiagnostic$finished(Void_Vortex_Entity entity, float yaw, float partial,
                                            PoseStack pose, MultiBufferSource buffers, int light, CallbackInfo ci) {
        guardianDiagnostic$current = null;
    }

    @Inject(method = "renderArc", at = @At("HEAD"))
    private void guardianDiagnostic$texture(PoseStack pose, MultiBufferSource buffers,
                                           ResourceLocation texture, CallbackInfo ci) {
        if (guardianDiagnostic$current != null) VortexDiagnostics.arc(guardianDiagnostic$current, texture);
    }
}
