package net.liopyu.entityjs.util.overrides.dynamic;

import cpw.mods.modlauncher.api.INameMappingService;
import net.liopyu.entityjs.EntityJSMod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Forge production-name helper for dynamic override method keys.
 * The bundled alias table maps SRG/runtime method names back to the readable mapped names
 * shown in development docs, allowing scripts to keep using stable override keys when the
 * production environment exposes different method names.
 */
final class MethodNameAliasIndex {
    private static final String METHOD_ALIASES = "/entityjs/mappings/method_aliases.tsv";
    private static volatile Index index;

    private MethodNameAliasIndex() {
    }

    static String alias(Method method) {
        return get().aliases().get(MethodKey.from(method));
    }

    private static Index get() {
        Index loaded = index;
        if (loaded != null) {
            return loaded;
        }

        synchronized (MethodNameAliasIndex.class) {
            loaded = index;
            if (loaded == null) {
                loaded = loadIndex();
                index = loaded;
            }
        }
        return loaded;
    }

    private static Index loadIndex() {
        long started = System.nanoTime();
        try (InputStream input = MethodNameAliasIndex.class.getResourceAsStream(METHOD_ALIASES)) {
            if (input == null) {
                return Index.EMPTY;
            }

            Map<MethodKey, String> aliases = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t", -1);
                    if (parts.length != 4) {
                        continue;
                    }
                    String owner = parts[0];
                    String srgName = parts[1];
                    String mappedName = parts[2];
                    String descriptor = parts[3];
                    String runtimeName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, srgName);
                    if (!runtimeName.equals(mappedName)) {
                        aliases.put(new MethodKey(owner, runtimeName, descriptor), mappedName);
                    }
                }
            }

            long millis = (System.nanoTime() - started) / 1_000_000L;
            EntityJSMod.LOGGER.info("[EntityJS]: Loaded dynamic override method alias index with {} method entries in {} ms.", aliases.size(), millis);
            return aliases.isEmpty() ? Index.EMPTY : new Index(Map.copyOf(aliases));
        } catch (IOException exception) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Failed to load dynamic override method alias index. Production runtime names may be required for dynamic overrides.", exception);
            return Index.EMPTY;
        }
    }

    private record MethodKey(String owner, String name, String descriptor) {
        static MethodKey from(Method method) {
            return new MethodKey(method.getDeclaringClass().getName().replace('.', '/'), method.getName(), DynamicOverrideMethodCatalog.descriptor(method));
        }
    }

    private record Index(Map<MethodKey, String> aliases) {
        private static final Index EMPTY = new Index(Map.of());
    }
}
