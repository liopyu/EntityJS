package net.liopyu.entityjs.common.util.overrides.catalog;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.common.platform.DynamicOverrideMethodMetadata;
import net.liopyu.entityjs.common.platform.EntityJSPlatform;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Resolves readable parameter names from the loader-specific metadata bundled in
 * the EntityJS jar. The metadata is generated from Parchment at build time, so
 * production installations never depend on a local Gradle cache.
 */
final class ParchmentParameterNameIndex {
    private ParchmentParameterNameIndex() {
    }

    @HideFromJS
    static String[] parameterNames(Method method, String[] fallbackNames) {
        DynamicOverrideMethodMetadata metadata = EntityJSPlatform.dynamicOverrideMethodMetadata(
                method,
                DynamicOverrideMethodCatalog.descriptor(method)
        );
        if (metadata == null || metadata.parameterNamesBySlot().isEmpty()) {
            return fallbackNames;
        }

        String[] resolved = fallbackNames.clone();
        int slot = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            String name = metadata.parameterNamesBySlot().get(slot);
            if (name != null && !name.isBlank()) {
                resolved[i] = name;
            }
            slot += localSize(parameterTypes[i]);
        }
        return resolved;
    }

    private static int localSize(Class<?> type) {
        return type == long.class || type == double.class ? 2 : 1;
    }
}
