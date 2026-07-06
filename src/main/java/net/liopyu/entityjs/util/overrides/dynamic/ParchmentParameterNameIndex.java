package net.liopyu.entityjs.util.overrides.dynamic;

import com.google.gson.stream.JsonReader;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.EntityJSMod;
import net.minecraft.SharedConstants;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Lazy parameter-name lookup used by dynamic override callback contexts.
 * The index loads Parchment data from an explicit path, the bundled resource, or the local
 * Gradle cache, then maps method local-variable slots to readable parameter names. When no
 * mapping is available it deliberately falls back to reflection names or {@code argN}
 * placeholders so dynamic overrides remain usable.
 */
final class ParchmentParameterNameIndex {
    private static final String PATH_PROPERTY = "entityjs.parchment.path";
    private static final String PATH_ENV = "ENTITYJS_PARCHMENT_PATH";
    private static final String MINECRAFT_VERSION_PROPERTY = "entityjs.parchment.minecraftVersion";
    private static final String PARCHMENT_JSON = "parchment.json";
    private static final String EMBEDDED_PARCHMENT_JSON = "/entityjs/parchment/parchment.json";
    private static final String PARCHMENT_GROUP_PATH = ".gradle/caches/modules-2/files-2.1/org.parchmentmc.data";
    private static volatile Index index;

    private ParchmentParameterNameIndex() {
    }

    @HideFromJS
    static String[] parameterNames(Method method, String[] fallbackNames) {
        ParameterNames names = get().parameters(method);
        if (names == null) {
            return fallbackNames;
        }

        String[] resolved = fallbackNames.clone();
        int slot = Modifier.isStatic(method.getModifiers()) ? 0 : 1;
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            String name = names.byLocalSlot().get(slot);
            if (isUsableName(name)) {
                resolved[i] = name;
            }
            slot += localSize(parameterTypes[i]);
        }
        return resolved;
    }

    private static Index get() {
        Index loaded = index;
        if (loaded != null) {
            return loaded;
        }

        synchronized (ParchmentParameterNameIndex.class) {
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

        Optional<Path> configured = configuredPath();
        if (configured.isPresent()) {
            return loadExternal(configured.get(), started);
        }

        Optional<Index> embedded = loadEmbedded(started);
        if (embedded.isPresent()) {
            return embedded.get();
        }

        Optional<Path> path = findParchmentZip();
        return path.map(value -> loadExternal(value, started)).orElse(Index.EMPTY);
    }

    private static Index loadExternal(Path path, long started) {
        try {
            Index loaded = read(path);
            long millis = (System.nanoTime() - started) / 1_000_000L;
            EntityJSMod.LOGGER.info("[EntityJS]: Loaded Parchment parameter name index from {} with {} method entries in {} ms.", path, loaded.size(), millis);
            return loaded;
        } catch (Exception exception) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Failed to load Parchment parameter name index from {}. Dynamic override contexts will use reflection/argN names.", path, exception);
            return Index.EMPTY;
        }
    }

    private static Optional<Index> loadEmbedded(long started) {
        try (InputStream input = ParchmentParameterNameIndex.class.getResourceAsStream(EMBEDDED_PARCHMENT_JSON)) {
            if (input == null) {
                return Optional.empty();
            }

            Index loaded = read(input);
            long millis = (System.nanoTime() - started) / 1_000_000L;
            EntityJSMod.LOGGER.info("[EntityJS]: Loaded bundled Parchment parameter name index with {} method entries in {} ms.", loaded.size(), millis);
            return Optional.of(loaded);
        } catch (Exception exception) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Failed to load bundled Parchment parameter name index. Dynamic override contexts will use external Parchment or reflection/argN names.", exception);
            return Optional.empty();
        }
    }

    private static Optional<Path> findParchmentZip() {
        String minecraftVersion = minecraftVersion();
        Path groupDir = Path.of(System.getProperty("user.home", ""), PARCHMENT_GROUP_PATH);
        if (!Files.isDirectory(groupDir)) {
            return Optional.empty();
        }

        if (!minecraftVersion.isBlank()) {
            Optional<Path> versioned = newestZip(groupDir.resolve("parchment-" + minecraftVersion));
            if (versioned.isPresent()) {
                return versioned;
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> configuredPath() {
        String configured = System.getProperty(PATH_PROPERTY);
        if (configured == null || configured.isBlank()) {
            configured = System.getenv(PATH_ENV);
        }
        if (configured == null || configured.isBlank()) {
            return Optional.empty();
        }

        Path path = Path.of(configured).toAbsolutePath().normalize();
        if (!Files.isRegularFile(path)) {
            EntityJSMod.LOGGER.warn("[EntityJS]: Ignoring configured Parchment path because it is not a file: {}", path);
            return Optional.empty();
        }
        return Optional.of(path);
    }

    private static Optional<Path> newestZip(Path root) {
        if (!Files.isDirectory(root)) {
            return Optional.empty();
        }

        try (var stream = Files.walk(root, 4)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .max((left, right) -> Long.compare(lastModified(left), lastModified(right)));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return 0L;
        }
    }

    private static String minecraftVersion() {
        String configured = System.getProperty(MINECRAFT_VERSION_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return configured;
        }

        try {
            return SharedConstants.getCurrentVersion().getName();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static Index read(Path path) throws IOException {
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            ZipEntry entry = zipFile.getEntry(PARCHMENT_JSON);
            if (entry == null) {
                throw new IOException("Missing " + PARCHMENT_JSON);
            }

            try (JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(zipFile.getInputStream(entry)), StandardCharsets.UTF_8))) {
                return readRoot(reader);
            }
        }
    }

    private static Index read(InputStream input) throws IOException {
        try (JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(input), StandardCharsets.UTF_8))) {
            return readRoot(reader);
        }
    }

    private static Index readRoot(JsonReader reader) throws IOException {
        Map<MethodKey, ParameterNames> parameters = new HashMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("classes")) {
                readClasses(reader, parameters);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return parameters.isEmpty() ? Index.EMPTY : new Index(Map.copyOf(parameters));
    }

    private static void readClasses(JsonReader reader, Map<MethodKey, ParameterNames> parameters) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            readClass(reader, parameters);
        }
        reader.endArray();
    }

    private static void readClass(JsonReader reader, Map<MethodKey, ParameterNames> parameters) throws IOException {
        String owner = null;
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                owner = reader.nextString();
            } else if (name.equals("methods")) {
                readMethods(reader, owner, parameters);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    }

    private static void readMethods(JsonReader reader, String owner, Map<MethodKey, ParameterNames> parameters) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            readMethod(reader, owner, parameters);
        }
        reader.endArray();
    }

    private static void readMethod(JsonReader reader, String owner, Map<MethodKey, ParameterNames> parameters) throws IOException {
        String name = null;
        String descriptor = null;
        Map<Integer, String> names = Map.of();
        reader.beginObject();
        while (reader.hasNext()) {
            String property = reader.nextName();
            if (property.equals("name")) {
                name = reader.nextString();
            } else if (property.equals("descriptor")) {
                descriptor = reader.nextString();
            } else if (property.equals("parameters")) {
                names = readParameters(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        if (owner != null && name != null && descriptor != null && !names.isEmpty()) {
            parameters.put(new MethodKey(owner, name, descriptor), new ParameterNames(Map.copyOf(names)));
        }
    }

    private static Map<Integer, String> readParameters(JsonReader reader) throws IOException {
        Map<Integer, String> names = new HashMap<>();
        reader.beginArray();
        while (reader.hasNext()) {
            Integer index = null;
            String name = null;
            reader.beginObject();
            while (reader.hasNext()) {
                String property = reader.nextName();
                if (property.equals("index")) {
                    index = reader.nextInt();
                } else if (property.equals("name")) {
                    name = reader.nextString();
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();

            if (index != null && isUsableName(name)) {
                names.put(index, name);
            }
        }
        reader.endArray();
        return names;
    }

    private static boolean isUsableName(String name) {
        return name != null && !name.isBlank();
    }

    private static int localSize(Class<?> type) {
        return type == long.class || type == double.class ? 2 : 1;
    }

    private record MethodKey(String owner, String name, String descriptor) {
        static MethodKey from(Method method) {
            return new MethodKey(method.getDeclaringClass().getName().replace('.', '/'), method.getName(), DynamicOverrideMethodCatalog.descriptor(method));
        }
    }

    private record ParameterNames(Map<Integer, String> byLocalSlot) {
    }

    private record Index(Map<MethodKey, ParameterNames> parameters) {
        private static final Index EMPTY = new Index(Map.of());

        ParameterNames parameters(Method method) {
            return parameters.get(MethodKey.from(method));
        }

        int size() {
            return parameters.size();
        }
    }
}
