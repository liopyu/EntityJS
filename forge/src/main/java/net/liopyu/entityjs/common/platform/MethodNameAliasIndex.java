package net.liopyu.entityjs.common.platform;

import cpw.mods.modlauncher.api.INameMappingService;
import net.liopyu.entityjs.common.EntityJSMod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps Forge runtime method identities to stable Mojmap identities and bundled
 * Parchment parameter names used by EntityJS scripts.
 */
final class MethodNameAliasIndex {
    private static final String METHOD_ALIASES = "/entityjs/mappings/method_aliases.tsv";
    private static volatile Index index;

    private MethodNameAliasIndex() {
    }

    static DynamicOverrideMethodMetadata metadata(Method method, String descriptor) {
        return metadata(get().methods(), method, descriptor);
    }

    /**
     * Forge's SRG mapping file records an override group under its canonical owner.
     * Reflection can report the same runtime method as declared by a concrete subclass,
     * so fall back through that class's supertype tree when the exact owner is absent.
     */
    private static DynamicOverrideMethodMetadata metadata(Map<MethodKey, DynamicOverrideMethodMetadata> methods,
                                                          Method method, String descriptor) {
        DynamicOverrideMethodMetadata exact = methods.get(MethodKey.from(method, descriptor));
        if (exact != null) {
            return exact;
        }

        ArrayDeque<Class<?>> pending = new ArrayDeque<>();
        addSupertypes(method.getDeclaringClass(), pending);
        Set<Class<?>> visited = new HashSet<>();
        DynamicOverrideMethodMetadata resolved = null;
        while (!pending.isEmpty()) {
            Class<?> owner = pending.removeFirst();
            if (!visited.add(owner)) {
                continue;
            }
            DynamicOverrideMethodMetadata candidate = methods.get(MethodKey.from(owner, method.getName(), descriptor));
            if (candidate != null) {
                if (resolved != null && !sameScriptMethod(resolved, candidate)) {
                    return null;
                }
                resolved = candidate;
            }
            addSupertypes(owner, pending);
        }
        return resolved;
    }

    private static boolean sameScriptMethod(DynamicOverrideMethodMetadata left, DynamicOverrideMethodMetadata right) {
        return left.scriptName().equals(right.scriptName())
                && left.scriptDescriptor().equals(right.scriptDescriptor());
    }

    private static void addSupertypes(Class<?> type, ArrayDeque<Class<?>> pending) {
        Class<?> superclass = type.getSuperclass();
        if (superclass != null) {
            pending.addLast(superclass);
        }
        for (Class<?> interfaceType : type.getInterfaces()) {
            pending.addLast(interfaceType);
        }
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
                EntityJSMod.LOGGER.warn("[EntityJS]: Dynamic override method metadata is missing. Production runtime names and argN parameters may be exposed.");
                return Index.EMPTY;
            }

            Map<MethodKey, DynamicOverrideMethodMetadata> methods = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t", -1);
                    if (parts.length != 7) {
                        continue;
                    }
                    String owner = parts[0];
                    String srgName = parts[1];
                    String runtimeDescriptor = parts[2];
                    String runtimeName = ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, srgName);
                    methods.put(
                            new MethodKey(owner, runtimeName, runtimeDescriptor),
                            new DynamicOverrideMethodMetadata(parts[3], parts[4], parts[5], parameterNames(parts[6]))
                    );
                }
            }

            long millis = (System.nanoTime() - started) / 1_000_000L;
            EntityJSMod.LOGGER.info("[EntityJS]: Loaded Forge dynamic override method metadata with {} method entries in {} ms.", methods.size(), millis);
            return methods.isEmpty() ? Index.EMPTY : new Index(Map.copyOf(methods));
        } catch (IOException exception) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Failed to load Forge dynamic override method metadata. Production runtime names and argN parameters may be exposed.", exception);
            return Index.EMPTY;
        }
    }

    private static Map<Integer, String> parameterNames(String encoded) {
        if (encoded.isBlank()) {
            return Map.of();
        }
        Map<Integer, String> names = new HashMap<>();
        for (String entry : encoded.split(";")) {
            int separator = entry.indexOf(':');
            if (separator > 0 && separator + 1 < entry.length()) {
                names.put(Integer.parseInt(entry.substring(0, separator)), entry.substring(separator + 1));
            }
        }
        return Map.copyOf(names);
    }

    private record MethodKey(String owner, String name, String descriptor) {
        static MethodKey from(Method method, String descriptor) {
            return from(method.getDeclaringClass(), method.getName(), descriptor);
        }

        static MethodKey from(Class<?> owner, String name, String descriptor) {
            return new MethodKey(owner.getName().replace('.', '/'), name, descriptor);
        }
    }

    private record Index(Map<MethodKey, DynamicOverrideMethodMetadata> methods) {
        private static final Index EMPTY = new Index(Map.of());
    }
}
