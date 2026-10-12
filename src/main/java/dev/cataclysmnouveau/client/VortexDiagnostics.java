package dev.cataclysmnouveau.client;

import com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity;
import com.mojang.logging.LogUtils;
import dev.cataclysmnouveau.CataclysmClientConfig;
import dev.cataclysmnouveau.casting.GuardianChild;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Observations only. Render hooks and entity scans are inert when the option is off. */
@EventBusSubscriber(modid = "cataclysm_nouveau", value = Dist.CLIENT)
public final class VortexDiagnostics {
    private static final Logger LOG = LogUtils.getLogger();
    private static final int LIMIT = 8;
    private static final int CAPTURE_TICKS = 1300;
    private static final Map<UUID, Sample> ACTIVE = new HashMap<>();
    private static final Set<ResourceLocation> TEXTURES = new HashSet<>();
    private static ClientLevel world;
    private static int admitted;
    private static long clock;

    private static final class Sample {
        final Void_Vortex_Entity entity;
        final long start;
        long renders, arcs, lastRender = -1, nextSnapshot;
        ResourceLocation texture;
        Sample(Void_Vortex_Entity entity) { this.entity = entity; start = clock; }
    }

    public static boolean enabled() {
        return CataclysmClientConfig.SPEC.isLoaded() && CataclysmClientConfig.VORTEX_DIAGNOSTICS.get();
    }

    private static void reset(ClientLevel next) {
        ACTIVE.clear();
        TEXTURES.clear();
        admitted = 0;
        clock = 0;
        world = next;
    }

    private static Sample observe(Void_Vortex_Entity entity) {
        if (!enabled() || !entity.level().isClientSide) return null;
        var current = Minecraft.getInstance().level;
        if (current == null || current != entity.level()) return null;
        if (world != current) reset(current);
        var sample = ACTIVE.get(entity.getUUID());
        if (sample != null) return sample;
        if (admitted >= LIMIT) return null;
        sample = new Sample(entity);
        ACTIVE.put(entity.getUUID(), sample);
        admitted++;
        LOG.info("[Vortex diagnostic] tracking id={} uuid={} dimension={} (capture {}/{})",
            entity.getId(), entity.getUUID(), current.dimension().location(), admitted, LIMIT);
        return sample;
    }

    public static void rendered(Void_Vortex_Entity entity) {
        var sample = observe(entity);
        if (sample == null) return;
        sample.renders++;
        sample.lastRender = clock;
    }

    public static void arc(Void_Vortex_Entity entity, ResourceLocation texture) {
        var sample = observe(entity);
        if (sample == null) return;
        sample.arcs++;
        sample.texture = texture;
        if (TEXTURES.add(texture)) {
            var resource = Minecraft.getInstance().getResourceManager().getResource(texture);
            LOG.info("[Vortex diagnostic] native texture={} present={} pack={}", texture,
                resource.isPresent(), resource.map(r -> r.sourcePackId()).orElse("missing"));
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var client = Minecraft.getInstance();
        if (!enabled() || client.level == null) {
            if (world != null) reset(null);
            return;
        }
        if (world != client.level) reset(client.level);
        if (client.isPaused()) return;
        clock++;
        // Read the real ClientLevel collection, before any renderer-specific entity filters.
        if (admitted < LIMIT) {
            for (var entity : world.entitiesForRendering())
                if (entity instanceof Void_Vortex_Entity vortex) observe(vortex);
        }
        var iterator = ACTIVE.values().iterator();
        while (iterator.hasNext()) {
            var sample = iterator.next();
            var entity = sample.entity;
            long elapsed = clock - sample.start;
            boolean removed = entity.isRemoved() || world.getEntity(entity.getId()) != entity;
            if (removed || elapsed >= sample.nextSnapshot || elapsed >= CAPTURE_TICKS) {
                snapshot(sample, removed ? "removed" : elapsed >= CAPTURE_TICKS ? "capture-limit" : "alive");
                sample.nextSnapshot = elapsed < 5 ? elapsed + 1 : elapsed < 20 ? 20
                    : elapsed < 60 ? 60 : elapsed < 120 ? 120 : elapsed < 240 ? 240 : elapsed + 100;
            }
            if (removed || elapsed >= CAPTURE_TICKS) iterator.remove();
        }
    }

    private static void snapshot(Sample sample, String state) {
        var entity = sample.entity;
        var client = Minecraft.getInstance();
        var camera = client.gameRenderer.getMainCamera().getPosition();
        var renderer = client.getEntityRenderDispatcher().getRenderer(entity);
        LOG.info("[Vortex diagnostic] {} id={} elapsed={} age={} life={} managed={} noCulling={} invisible={} registered={} removal={} renderCalls={} arcCalls={} sinceRender={} texture={} renderer={} cameraDistance={} cameraDy={} pos={} physical={} visual={} entityCulling.culled={} entityCulling.outOfCamera={}",
            state, entity.getId(), clock - sample.start, entity.tickCount, entity.getLifespan(),
            ((GuardianChild) entity).confluxGuardian$managed(), entity.noCulling, entity.isInvisible(),
            world.getEntity(entity.getId()) == entity, entity.getRemovalReason(),
            sample.renders, sample.arcs, sample.lastRender < 0 ? -1 : clock - sample.lastRender,
            sample.texture, renderer == null ? "null" : renderer.getClass().getName(),
            camera.distanceTo(entity.position()), camera.y - entity.getY(), entity.position(),
            entity.getBoundingBox(), entity.getBoundingBoxForCulling(),
            optionalFlag(entity, "isCulled"), optionalFlag(entity, "isOutOfCamera"));
    }

    /** Optional optimizer metadata without a class reference or dependency on that optimizer. */
    private static String optionalFlag(Void_Vortex_Entity entity, String method) {
        try { return String.valueOf(entity.getClass().getMethod(method).invoke(entity)); }
        catch (ReflectiveOperationException | SecurityException ignored) { return "unavailable"; }
    }

    private VortexDiagnostics() {}
}
