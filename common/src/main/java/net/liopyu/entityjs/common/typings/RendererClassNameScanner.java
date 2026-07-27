package net.liopyu.entityjs.common.typings;

import net.liopyu.entityjs.builders.misc.EntityReflection;
import net.liopyu.entityjs.common.EntityJSMod;
import net.liopyu.entityjs.common.platform.EntityJSPlatform;
import org.objectweb.asm.ClassReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/** Bytecode-first renderer discovery shared by both loader implementations. */
public final class RendererClassNameScanner {
    private RendererClassNameScanner() {
    }

    public static Set<String> scan(String rendererRuntimeName, Collection<Path> loaderRoots) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = RendererClassNameScanner.class.getClassLoader();
        }

        ScanFailures failures = new ScanFailures();
        Class<?> rendererBaseClass = loadClass(classLoader, rendererRuntimeName, failures);
        if (rendererBaseClass == null) {
            failures.logSummary();
            return Set.of();
        }

        Map<String, ClassInfo> classInfos = discoverRuntimeClasses(loaderRoots, failures);
        Set<String> renderers = new LinkedHashSet<>();
        for (ClassInfo classInfo : List.copyOf(classInfos.values())) {
            if (!extendsClass(classLoader, classInfos, classInfo.className(), rendererRuntimeName, failures)) {
                continue;
            }
            Class<?> type = loadClass(classLoader, classInfo.className(), failures);
            if (type != null
                    && rendererBaseClass.isAssignableFrom(type)
                    && EntityReflection.isRendererClassCompatible(type)) {
                renderers.add(EntityJSPlatform.scriptClassName(type));
            }
        }
        failures.logSummary();
        return Set.copyOf(renderers);
    }

    private static Map<String, ClassInfo> discoverRuntimeClasses(
            Collection<Path> loaderRoots,
            ScanFailures failures
    ) {
        Map<String, ScanRoot> rootsByIdentity = new LinkedHashMap<>();
        loaderRoots.forEach(root -> addRoot(rootsByIdentity, root, failures));
        addJavaClassPath(rootsByIdentity, failures);

        Map<String, ClassInfo> classInfos = new HashMap<>();
        rootsByIdentity.values().forEach(root -> scanRoot(classInfos, root.path(), failures));
        return classInfos;
    }

    private static void addJavaClassPath(Map<String, ScanRoot> rootsByIdentity, ScanFailures failures) {
        String classPath = System.getProperty("java.class.path", "");
        if (classPath.isBlank()) {
            return;
        }
        for (String entry : classPath.split(java.io.File.pathSeparator)) {
            if (entry != null && !entry.isBlank()) {
                try {
                    addRoot(rootsByIdentity, Path.of(entry), failures);
                } catch (RuntimeException exception) {
                    failures.record("classpath " + entry, exception);
                }
            }
        }
    }

    private static void addRoot(Map<String, ScanRoot> rootsByIdentity, Path root, ScanFailures failures) {
        if (root == null) {
            return;
        }
        try {
            Path normalized = normalize(root);
            if (!Files.isDirectory(normalized)
                    && !(Files.isRegularFile(normalized) && normalized.toString().endsWith(".jar"))) {
                return;
            }
            rootsByIdentity.putIfAbsent(rootIdentity(normalized), new ScanRoot(normalized));
        } catch (RuntimeException exception) {
            failures.record("root " + root, exception);
        }
    }

    private static Path normalize(Path path) {
        try {
            return path.toRealPath();
        } catch (IOException ignored) {
            return path.toAbsolutePath().normalize();
        }
    }

    private static String rootIdentity(Path path) {
        String identity = path.toUri().normalize().toString();
        int archiveSeparator = identity.indexOf("!/");
        if (archiveSeparator >= 0) {
            identity = identity.substring(0, archiveSeparator);
        }
        return identity.startsWith("jar:") ? identity.substring(4) : identity;
    }

    private static void scanRoot(Map<String, ClassInfo> classInfos, Path root, ScanFailures failures) {
        if (Files.isDirectory(root)) {
            scanDirectory(classInfos, root, failures);
        } else {
            scanJar(classInfos, root, failures);
        }
    }

    private static void scanDirectory(Map<String, ClassInfo> classInfos, Path root, ScanFailures failures) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> scanClassFile(classInfos, path, failures));
        } catch (IOException | RuntimeException exception) {
            failures.record("directory " + root, exception);
        }
    }

    private static void scanJar(Map<String, ClassInfo> classInfos, Path jarPath, ScanFailures failures) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            jar.stream()
                    .filter(entry -> !entry.isDirectory() && entry.getName().endsWith(".class"))
                    .forEach(entry -> scanJarClass(classInfos, jar, entry, failures));
        } catch (IOException | RuntimeException exception) {
            failures.record("jar " + jarPath, exception);
        }
    }

    private static void scanClassFile(Map<String, ClassInfo> classInfos, Path path, ScanFailures failures) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            putClassInfo(classInfos, inputStream);
        } catch (IOException | RuntimeException exception) {
            failures.record("class " + path, exception);
        }
    }

    private static void scanJarClass(
            Map<String, ClassInfo> classInfos,
            JarFile jar,
            JarEntry entry,
            ScanFailures failures
    ) {
        try (InputStream inputStream = jar.getInputStream(entry)) {
            putClassInfo(classInfos, inputStream);
        } catch (IOException | RuntimeException exception) {
            failures.record("class " + entry.getName() + " in " + jar.getName(), exception);
        }
    }

    private static void putClassInfo(Map<String, ClassInfo> classInfos, InputStream inputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        String className = classNameFromInternalName(classReader.getClassName());
        if (isUsefulClassName(className)) {
            classInfos.putIfAbsent(className, new ClassInfo(
                    className,
                    classNameFromInternalName(classReader.getSuperName())
            ));
        }
    }

    private static boolean extendsClass(
            ClassLoader classLoader,
            Map<String, ClassInfo> classInfos,
            String className,
            String targetClassName,
            ScanFailures failures
    ) {
        Set<String> visited = new LinkedHashSet<>();
        String currentClassName = className;
        while (currentClassName != null && visited.add(currentClassName)) {
            ClassInfo classInfo = classInfos.get(currentClassName);
            if (classInfo == null) {
                classInfo = loadClassInfo(classLoader, classInfos, currentClassName, failures);
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

    private static ClassInfo loadClassInfo(
            ClassLoader classLoader,
            Map<String, ClassInfo> classInfos,
            String className,
            ScanFailures failures
    ) {
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
            classInfos.putIfAbsent(discoveredClassName, new ClassInfo(
                    discoveredClassName,
                    classNameFromInternalName(classReader.getSuperName())
            ));
            return classInfos.get(className);
        } catch (IOException | RuntimeException exception) {
            failures.record("resource " + resourceName, exception);
            return null;
        }
    }

    private static Class<?> loadClass(ClassLoader classLoader, String className, ScanFailures failures) {
        if (className == null || className.isBlank()) {
            return null;
        }
        try {
            return Class.forName(className, false, classLoader);
        } catch (Throwable throwable) {
            if (throwable instanceof VirtualMachineError error) {
                throw error;
            }
            failures.record("load " + className, throwable);
            return null;
        }
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

    private record ScanRoot(Path path) {
    }

    private record ClassInfo(String className, String superClassName) {
    }

    private static final class ScanFailures {
        private static final int MAX_SAMPLES = 5;
        private final List<String> samples = new ArrayList<>();
        private int count;

        private void record(String subject, Throwable throwable) {
            if (throwable instanceof VirtualMachineError error) {
                throw error;
            }
            count++;
            if (samples.size() < MAX_SAMPLES) {
                samples.add(subject + ": " + throwable.getClass().getSimpleName());
            }
        }

        private void logSummary() {
            if (count > 0) {
                EntityJSMod.LOGGER.debug("[EntityJS]: Renderer discovery skipped {} roots/classes; samples: {}",
                        count, samples);
            }
        }
    }
}
