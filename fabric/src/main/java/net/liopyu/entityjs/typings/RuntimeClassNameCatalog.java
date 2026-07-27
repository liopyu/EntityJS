package net.liopyu.entityjs.typings;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.entityjs.common.EntityJSMod;
import net.liopyu.entityjs.common.platform.EntityJSPlatform;
import net.liopyu.entityjs.common.typings.RendererClassNameScanner;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Loader-specific roots for the shared renderer class-name scanner. */
public final class RuntimeClassNameCatalog {
    private static final String NAMED_ENTITY_RENDERER_CLASS = "net.minecraft.client.renderer.entity.EntityRenderer";

    private final Set<String> rendererClassNames;

    private RuntimeClassNameCatalog(Set<String> rendererClassNames) {
        this.rendererClassNames = Set.copyOf(rendererClassNames);
    }

    public static RuntimeClassNameCatalog get() {
        return Holder.INSTANCE;
    }

    public Set<String> rendererClassNames() {
        return rendererClassNames;
    }

    private static RuntimeClassNameCatalog build() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return new RuntimeClassNameCatalog(Set.of());
        }
        Set<Path> roots = new LinkedHashSet<>();
        try {
            FabricLoader.getInstance().getAllMods().forEach(mod -> roots.addAll(mod.getRootPaths()));
        } catch (Throwable throwable) {
            if (throwable instanceof VirtualMachineError error) {
                throw error;
            }
            EntityJSMod.LOGGER.debug("[EntityJS]: Unable to enumerate all Fabric renderer scan roots", throwable);
        }
        String rendererRuntimeName = EntityJSPlatform.runtimeClassName(NAMED_ENTITY_RENDERER_CLASS);
        return new RuntimeClassNameCatalog(RendererClassNameScanner.scan(rendererRuntimeName, roots));
    }

    private static final class Holder {
        private static final RuntimeClassNameCatalog INSTANCE = build();
    }
}
