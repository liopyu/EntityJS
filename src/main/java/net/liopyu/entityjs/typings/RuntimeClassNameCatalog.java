package net.liopyu.entityjs.typings;

import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Discovers renderer classes that are available in the running modded JVM.
 * The catalog scans mod file roots and the Java class path as bytecode first, records
 * superclass relationships without eagerly loading every class, then loads only likely
 * {@code EntityRenderer} candidates for compatibility checks.
 *
 * <p>The cached class-name sets are used by EntityJS' generated docs/typings so scripts
 * can reference renderer classes contributed by the current runtime instead of
 * only the classes known at compile time.</p>
 */
public final class RuntimeClassNameCatalog {
    private static final String ENTITY_RENDERER_CLASS = "net.minecraft.client.renderer.entity.EntityRenderer";
    private static volatile RuntimeClassNameCatalog cached;

    private final Set<String> rendererClassNames;

    private RuntimeClassNameCatalog(Set<String> rendererClassNames) {
        this.rendererClassNames = Set.copyOf(rendererClassNames);
    }

    public static RuntimeClassNameCatalog get() {
        RuntimeClassNameCatalog current = cached;
        if (current == null) {
            current = build();
            cached = current;
        }
        return current;
    }

    public Set<String> rendererClassNames() {
        return rendererClassNames;
    }

    private static RuntimeClassNameCatalog build() {
        if (FMLLoader.getDist() != Dist.CLIENT) {
            return new RuntimeClassNameCatalog(Set.of());
        }

        Set<String> renderers = new LinkedHashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = RuntimeClassNameCatalog.class.getClassLoader();
        }

        Class<?> rendererBaseClass = loadClass(classLoader, ENTITY_RENDERER_CLASS);
        if (rendererBaseClass == null) {
            return new RuntimeClassNameCatalog(renderers);
        }

        Map<String, ClassInfo> classInfos = discoverRuntimeClasses();
        for (ClassInfo classInfo : List.copyOf(classInfos.values())) {
            boolean rendererCandidate = rendererBaseClass != null
                    && extendsClass(classLoader, classInfos, classInfo.className(), ENTITY_RENDERER_CLASS);
            if (!rendererCandidate) {
                continue;
            }
            String className = classInfo.className();
            Class<?> type = loadClass(classLoader, className);
            if (rendererCandidate
                    && type != null
                    && rendererBaseClass.isAssignableFrom(type)
                    && EntityReflection.isRendererClassCompatible(type)) {
                renderers.add(className);
            }
        }
        return new RuntimeClassNameCatalog(renderers);
    }

    private static Map<String, ClassInfo> discoverRuntimeClasses() {
        Map<String, ClassInfo> classInfos = new HashMap<>();
        scanModFiles(classInfos);
        scanJavaClassPath(classInfos);
        return classInfos;
    }

    private static void scanModFiles(Map<String, ClassInfo> classInfos) {
        try {
            ModList.get().forEachModFile(modFile -> scanRoot(classInfos, modFile.getSecureJar().getRootPath()));
        } catch (Throwable ignored) {
        }
    }

    private static void scanJavaClassPath(Map<String, ClassInfo> classInfos) {
        String classPath = System.getProperty("java.class.path", "");
        if (classPath.isBlank()) {
            return;
        }
        for (String entry : classPath.split(java.io.File.pathSeparator)) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            Path path = Path.of(entry);
            if (Files.isDirectory(path)) {
                scanRoot(classInfos, path);
            } else if (Files.isRegularFile(path) && entry.endsWith(".jar")) {
                scanJar(classInfos, path);
            }
        }
    }

    private static void scanRoot(Map<String, ClassInfo> classInfos, Path root) {
        if (root == null || !Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> scanClassFile(classInfos, path));
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static void scanJar(Map<String, ClassInfo> classInfos, Path jarPath) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .forEach(entry -> scanJarClass(classInfos, jar, entry));
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static void scanClassFile(Map<String, ClassInfo> classInfos, Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            putClassInfo(classInfos, inputStream);
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static void scanJarClass(Map<String, ClassInfo> classInfos, JarFile jar, JarEntry entry) {
        try (InputStream inputStream = jar.getInputStream(entry)) {
            putClassInfo(classInfos, inputStream);
        } catch (IOException | RuntimeException ignored) {
        }
    }

    private static void putClassInfo(Map<String, ClassInfo> classInfos, InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        String className = classNameFromInternalName(classReader.getClassName());
        if (!isUsefulClassName(className)) {
            return;
        }
        classInfos.putIfAbsent(className, new ClassInfo(
                className,
                classNameFromInternalName(classReader.getSuperName())
        ));
    }

    private static String classNameFromInternalName(String internalName) {
        return internalName == null ? null : internalName.replace('/', '.');
    }

    private static boolean isUsefulClassName(String className) {
        return className != null
                && !className.isBlank()
                && !className.startsWith("META-INF.")
                && !className.contains(".mixin.")
                && !className.endsWith(".package-info")
                && !className.equals("module-info")
                && !className.matches(".*\\$\\d+.*");
    }

    private static boolean extendsClass(ClassLoader classLoader, Map<String, ClassInfo> classInfos, String className, String targetClassName) {
        Set<String> visited = new LinkedHashSet<>();
        String currentClassName = className;
        while (currentClassName != null && visited.add(currentClassName)) {
            ClassInfo classInfo = classInfos.get(currentClassName);
            if (classInfo == null) {
                classInfo = loadClassInfo(classLoader, classInfos, currentClassName);
                if (classInfo == null) {
                    return false;
                }
            }
            String superClassName = classInfo.superClassName();
            if (targetClassName.equals(superClassName)) {
                return true;
            }
            currentClassName = superClassName;
        }
        return false;
    }

    private static ClassInfo loadClassInfo(ClassLoader classLoader, Map<String, ClassInfo> classInfos, String className) {
        if (!isUsefulClassName(className)) {
            return null;
        }
        String resourceName = className.replace('.', '/') + ".class";
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                return null;
            }
            ClassReader classReader = new ClassReader(inputStream);
            String discoveredClassName = classNameFromInternalName(classReader.getClassName());
            if (!isUsefulClassName(discoveredClassName)) {
                return null;
            }
            ClassInfo classInfo = new ClassInfo(
                    discoveredClassName,
                    classNameFromInternalName(classReader.getSuperName())
            );
            classInfos.putIfAbsent(discoveredClassName, classInfo);
            return classInfos.get(className);
        } catch (IOException | RuntimeException ignored) {
            return null;
        }
    }

    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return Class.forName(className, false, classLoader);
        } catch (Throwable throwable) {
            if (throwable instanceof VirtualMachineError error) {
                throw error;
            }
            return null;
        }
    }

    private record ClassInfo(String className, String superClassName) {
    }
}
