package net.liopyu.entityjs.common.util.overrides;

import net.liopyu.entityjs.common.EntityJSMod;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class CallbackProfiler {
    private static final boolean ENABLED = Boolean.getBoolean("entityjs.profileCallbacks");
    private static final String FOCUS_METHOD = System.getProperty("entityjs.profileCallbacks.focusMethod", "");
    private static final long REPORT_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(Long.getLong("entityjs.profileCallbacks.intervalSeconds", 5L));
    private static final long MIN_REPORT_NANOS = minReportNanos();
    private static final boolean SLOW_STACKS = Boolean.getBoolean("entityjs.profileCallbacks.slowStacks");
    private static final long SLOW_STACK_NANOS = TimeUnit.MILLISECONDS.toNanos(Long.getLong("entityjs.profileCallbacks.slowStackMillis", 25L));
    private static final int SLOW_STACK_LIMIT = Integer.getInteger("entityjs.profileCallbacks.slowStackLimit", 8);
    private static final ConcurrentHashMap<String, Stats> STATS = new ConcurrentHashMap<>();
    private static final AtomicLong NEXT_REPORT = new AtomicLong(System.nanoTime() + REPORT_INTERVAL_NANOS);
    private static final AtomicInteger SLOW_STACK_REPORTS = new AtomicInteger();
    private static final ThreadLocal<String> CURRENT_METHOD = new ThreadLocal<>();
    private static volatile ScheduledExecutorService slowStackExecutor;

    private CallbackProfiler() {
    }

    public static boolean enabled() {
        return ENABLED;
    }

    public static boolean focused(String method) {
        return FOCUS_METHOD.isBlank() || FOCUS_METHOD.equals(method);
    }

    public static String currentMethod() {
        return CURRENT_METHOD.get();
    }

    public static void profile(String method, Runnable callback) {
        if (!shouldProfile(method)) {
            callback.run();
            return;
        }

        long started = System.nanoTime();
        String previous = CURRENT_METHOD.get();
        CURRENT_METHOD.set(method);
        try {
            callback.run();
        } finally {
            restoreCurrentMethod(previous);
            record(method, System.nanoTime() - started);
        }
    }

    public static <T> T profile(String method, Supplier<T> callback) {
        if (!shouldProfile(method)) {
            return callback.get();
        }

        long started = System.nanoTime();
        String previous = CURRENT_METHOD.get();
        CURRENT_METHOD.set(method);
        try {
            return callback.get();
        } finally {
            restoreCurrentMethod(previous);
            record(method, System.nanoTime() - started);
        }
    }

    public static void profileLayer(String method, String layer, Runnable callback) {
        if (!shouldProfile(method)) {
            callback.run();
            return;
        }

        long started = System.nanoTime();
        try {
            callback.run();
        } finally {
            record(layerKey(method, layer), System.nanoTime() - started);
        }
    }

    public static <T> T profileLayer(String method, String layer, Supplier<T> callback) {
        if (!shouldProfile(method)) {
            return callback.get();
        }

        long started = System.nanoTime();
        try {
            return callback.get();
        } finally {
            record(layerKey(method, layer), System.nanoTime() - started);
        }
    }

    public static boolean profileLayer(String method, String layer, BooleanSupplier callback) {
        if (!shouldProfile(method)) {
            return callback.getAsBoolean();
        }

        long started = System.nanoTime();
        try {
            return callback.getAsBoolean();
        } finally {
            record(layerKey(method, layer), System.nanoTime() - started);
        }
    }

    public static void recordLayer(String method, String layer, long elapsedNanos) {
        if (shouldProfile(method)) {
            record(layerKey(method, layer), elapsedNanos);
        }
    }

    public static StackProbe beginStackProbe(String method, String layer, String detail) {
        if (!shouldProfile(method) || !SLOW_STACKS || SLOW_STACK_NANOS <= 0L || SLOW_STACK_REPORTS.get() >= SLOW_STACK_LIMIT) {
            return StackProbe.NOOP;
        }

        StackProbe probe = new StackProbe(Thread.currentThread(), method, layer, detail, System.nanoTime());
        executor().schedule(probe::reportIfRunning, SLOW_STACK_NANOS, TimeUnit.NANOSECONDS);
        return probe;
    }

    private static boolean shouldProfile(String method) {
        return ENABLED && method != null && focused(method);
    }

    private static long minReportNanos() {
        String nanos = System.getProperty("entityjs.profileCallbacks.minNanos");
        if (nanos != null) {
            return Long.parseLong(nanos);
        }

        String micros = System.getProperty("entityjs.profileCallbacks.minMicros");
        if (micros != null) {
            return TimeUnit.MICROSECONDS.toNanos(Long.parseLong(micros));
        }

        return TimeUnit.MILLISECONDS.toNanos(Long.getLong("entityjs.profileCallbacks.minMillis", 10L));
    }

    private static void restoreCurrentMethod(String previous) {
        if (previous == null) {
            CURRENT_METHOD.remove();
        } else {
            CURRENT_METHOD.set(previous);
        }
    }

    private static String layerKey(String method, String layer) {
        return method + "/" + layer;
    }

    private static ScheduledExecutorService executor() {
        ScheduledExecutorService executor = slowStackExecutor;
        if (executor == null) {
            synchronized (CallbackProfiler.class) {
                executor = slowStackExecutor;
                if (executor == null) {
                    executor = Executors.newSingleThreadScheduledExecutor(task -> {
                        Thread thread = new Thread(task, "EntityJS Callback Slow Stack Probe");
                        thread.setDaemon(true);
                        return thread;
                    });
                    slowStackExecutor = executor;
                }
            }
        }
        return executor;
    }

    private static void record(String key, long elapsedNanos) {
        Stats stats = STATS.computeIfAbsent(key, ignored -> new Stats());
        stats.calls.increment();
        stats.nanos.add(elapsedNanos);
        stats.maxNanos.accumulateAndGet(elapsedNanos, Math::max);

        long now = System.nanoTime();
        long next = NEXT_REPORT.get();
        if (now >= next && NEXT_REPORT.compareAndSet(next, now + REPORT_INTERVAL_NANOS)) {
            report();
        }
    }

    private static void report() {
        var rows = STATS.entrySet().stream()
                .map(entry -> new Row(entry.getKey(), entry.getValue().calls.sumThenReset(), entry.getValue().nanos.sumThenReset(), entry.getValue().maxNanos.getAndSet(0L)))
                .filter(row -> row.calls > 0 && (row.nanos >= MIN_REPORT_NANOS || row.maxNanos >= MIN_REPORT_NANOS))
                .sorted(Comparator.comparingLong(Row::nanos).reversed())
                .limit(16)
                .toList();

        if (rows.isEmpty()) {
            return;
        }

        StringBuilder message = new StringBuilder("[EntityJS]: Hot callback profile:");
        for (Row row : rows) {
            double totalMs = row.nanos / 1_000_000.0D;
            double averageUs = row.nanos / 1_000.0D / row.calls;
            double maxUs = row.maxNanos / 1_000.0D;
            message.append(System.lineSeparator())
                    .append(" - ")
                    .append(row.key)
                    .append(": ")
                    .append(row.calls)
                    .append(" calls, ")
                    .append(String.format("%.2f ms total, %.2f us avg, %.2f us max", totalMs, averageUs, maxUs));
        }
        EntityJSMod.LOGGER.warn(message.toString());
    }

    private static final class Stats {
        private final LongAdder calls = new LongAdder();
        private final LongAdder nanos = new LongAdder();
        private final AtomicLong maxNanos = new AtomicLong();
    }

    private record Row(String key, long calls, long nanos, long maxNanos) {
    }

    public static final class StackProbe implements AutoCloseable {
        private static final StackProbe NOOP = new StackProbe(null, null, null, null, 0L);

        private final Thread thread;
        private final String method;
        private final String layer;
        private final String detail;
        private final long started;
        private volatile boolean running;

        private StackProbe(Thread thread, String method, String layer, String detail, long started) {
            this.thread = thread;
            this.method = method;
            this.layer = layer;
            this.detail = detail;
            this.started = started;
            this.running = thread != null;
        }

        private void reportIfRunning() {
            if (!running || SLOW_STACK_REPORTS.getAndIncrement() >= SLOW_STACK_LIMIT) {
                return;
            }

            long elapsed = System.nanoTime() - started;
            StringBuilder message = new StringBuilder("[EntityJS]: Slow callback stack probe: ")
                    .append(method)
                    .append("/")
                    .append(layer)
                    .append(" still running after ")
                    .append(String.format("%.2f ms", elapsed / 1_000_000.0D));
            ThreadInfo threadInfo = ManagementFactory.getThreadMXBean().getThreadInfo(thread.getId(), 64);
            if (threadInfo != null) {
                message.append(System.lineSeparator())
                        .append(" thread: ")
                        .append(threadInfo.getThreadName())
                        .append(" state=")
                        .append(threadInfo.getThreadState());
                if (threadInfo.getLockName() != null) {
                    message.append(" lock=").append(threadInfo.getLockName());
                }
                if (threadInfo.getLockOwnerName() != null) {
                    message.append(" lockOwner=").append(threadInfo.getLockOwnerName());
                }
            }
            if (detail != null && !detail.isBlank()) {
                message.append(System.lineSeparator()).append(" detail: ").append(detail);
            }
            StackTraceElement[] stackTrace = threadInfo == null ? thread.getStackTrace() : threadInfo.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                message.append(System.lineSeparator()).append("   at ").append(element);
            }
            EntityJSMod.LOGGER.warn(message.toString());
        }

        @Override
        public void close() {
            running = false;
        }
    }
}
