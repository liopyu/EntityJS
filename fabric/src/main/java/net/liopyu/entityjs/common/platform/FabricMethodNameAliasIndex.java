package net.liopyu.entityjs.common.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.entityjs.common.EntityJSMod;

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
 * Maps Fabric runtime method identities to stable Mojmap identities and bundled
 * Parchment parameter names used by EntityJS scripts.
 */
final class FabricMethodNameAliasIndex {
    private static final String METHOD_ALIASES = "/entityjs/mappings/method_aliases.tsv";
    private static volatile Index index;

    private FabricMethodNameAliasIndex() {
    }

    static DynamicOverrideMethodMetadata metadata(Method method, String descriptor) {
        return metadata(get().methods(), method, descriptor);
    }

    /**
     * Tiny mappings may record an override group only on its canonical interface or
     * superclass while reflection reports a concrete subclass as the declaring owner.
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

        synchronized (FabricMethodNameAliasIndex.class) {
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
        try (InputStream input = FabricMethodNameAliasIndex.class.getResourceAsStream(METHOD_ALIASES)) {
            if (input == null) {
                EntityJSMod.LOGGER.warn("[EntityJS]: Dynamic override method metadata is missing. Production runtime names and argN parameters may be exposed.");
                return Index.EMPTY;
            }

            Map<MethodKey, DynamicOverrideMethodMetadata> methods = new HashMap<>();
            boolean namedRuntime = FabricLoader.getInstance().getMappingResolver()
                    .getCurrentRuntimeNamespace().equals("named");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\t", -1);
                    if (parts.length == 7) {
                        DynamicOverrideMethodMetadata metadata = new DynamicOverrideMethodMetadata(
                                parts[3], parts[4], parts[5], parameterNames(parts[6])
                        );
                        MethodKey runtimeKey = namedRuntime
                                ? new MethodKey(parts[3], parts[4], parts[5])
                                : new MethodKey(parts[0], parts[1], parts[2]);
                        methods.put(runtimeKey, metadata);
                    }
                }
            }

            long millis = (System.nanoTime() - started) / 1_000_000L;
            EntityJSMod.LOGGER.info("[EntityJS]: Loaded Fabric dynamic override method metadata with {} method entries in {} ms.", methods.size(), millis);
            return methods.isEmpty() ? Index.EMPTY : new Index(Map.copyOf(methods));
        } catch (IOException exception) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Failed to load Fabric dynamic override method metadata. Production runtime names and argN parameters may be exposed.", exception);
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
