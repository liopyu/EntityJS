package net.liopyu.entityjs.common.util.overrides;

import net.liopyu.entityjs.common.util.EntityJSHelperClass;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class OverrideUtils {
    private static final ScopedVariable<OverrideFrame> CURRENT = new ScopedVariable<>();
    private static final ThreadLocal<Integer> FALLBACK_DEPTH = new ThreadLocal<>();

    private OverrideUtils() {
    }

    public static Object call() {
        if (!CURRENT.isBound()) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.superCall() was called outside an EntityJS override.");
            return null;
        }
        return CURRENT.get().call();
    }

    public static boolean isCallingFallback() {
        Integer depth = FALLBACK_DEPTH.get();
        return depth != null && depth > 0;
    }

    public static <T> T with(Supplier<Object> fallback, Supplier<T> callback) {
        return with(new OverrideFrame(fallback), callback);
    }

    public static boolean with(Supplier<Object> fallback, Runnable callback) {
        OverrideFrame frame = new OverrideFrame(fallback);
        with(frame, callback);
        return frame.wasCalled();
    }

    public static void beforeFallback(Runnable fallback, Runnable callback) {
        boolean called = with(() -> {
            fallback.run();
            return null;
        }, callback);
        if (!called) {
            fallback.run();
        }
    }

    public static <T> T resultBeforeFallback(Supplier<T> fallback, Runnable callback) {
        AtomicReference<T> value = new AtomicReference<>();
        boolean called = with(() -> {
            T result = fallback.get();
            value.set(result);
            return result;
        }, callback);
        return called ? value.get() : fallback.get();
    }

    public static void afterFallback(Runnable fallback, Runnable callback) {
        fallback.run();
        withAlreadyCalled(callback);
    }

    public static void replaceFallback(Runnable fallback, Runnable callback) {
        with(() -> {
            fallback.run();
            return null;
        }, callback);
    }

    public static <T> T resultReplaceFallback(Supplier<T> fallback, Runnable callback, T defaultValue) {
        AtomicReference<T> value = new AtomicReference<>(defaultValue);
        with(() -> {
            T result = fallback.get();
            value.set(result);
            return result;
        }, callback);
        return value.get();
    }

    public static void withAlreadyCalled(Object value, Runnable callback) {
        with(OverrideFrame.alreadyCalled(value), callback);
    }

    public static void withAlreadyCalled(Runnable callback) {
        withAlreadyCalled(null, callback);
    }

    private static <T> T with(OverrideFrame frame, Supplier<T> callback) {
        String method = CallbackProfiler.currentMethod();
        if (!CallbackProfiler.enabled() || method == null) {
            OverrideFrame previous = CURRENT.pushValue(frame);
            try {
                return callback.get();
            } finally {
                CURRENT.restore(previous);
            }
        }

        long started = System.nanoTime();
        OverrideFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "overrideFramePush", System.nanoTime() - started);
        try {
            try (var ignored = CallbackProfiler.beginStackProbe(method, "jsInvoke", "callback=" + callback.getClass().getName())) {
                return CallbackProfiler.profileLayer(method, "jsInvoke", callback);
            }
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "overrideFramePop", System.nanoTime() - started);
        }
    }

    private static void with(OverrideFrame frame, Runnable callback) {
        String method = CallbackProfiler.currentMethod();
        if (!CallbackProfiler.enabled() || method == null) {
            OverrideFrame previous = CURRENT.pushValue(frame);
            try {
                callback.run();
            } finally {
                CURRENT.restore(previous);
            }
            return;
        }

        long started = System.nanoTime();
        OverrideFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "overrideFramePush", System.nanoTime() - started);
        try {
            try (var ignored = CallbackProfiler.beginStackProbe(method, "jsInvoke", "callback=" + callback.getClass().getName())) {
                CallbackProfiler.profileLayer(method, "jsInvoke", callback);
            }
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "overrideFramePop", System.nanoTime() - started);
        }
    }

    private static final class OverrideFrame {
        private final Supplier<Object> fallback;
        private boolean called;
        private Object value;

        private OverrideFrame(Supplier<Object> fallback) {
            this.fallback = fallback;
        }

        private static OverrideFrame alreadyCalled(Object value) {
            OverrideFrame frame = new OverrideFrame(() -> value);
            frame.called = true;
            frame.value = value;
            return frame;
        }

        private Object call() {
            if (!called) {
                called = true;
                String method = CallbackProfiler.currentMethod();
                value = callFallback(method);
            }
            return value;
        }

        private Object callFallback(String method) {
            Integer depth = FALLBACK_DEPTH.get();
            FALLBACK_DEPTH.set(depth == null ? 1 : depth + 1);
            try {
                if (CallbackProfiler.enabled() && method != null) {
                    return CallbackProfiler.profileLayer(method, "superCallFallback", fallback);
                }
                return fallback.get();
            } finally {
                if (depth == null) {
                    FALLBACK_DEPTH.remove();
                } else {
                    FALLBACK_DEPTH.set(depth);
                }
            }
        }

        private boolean wasCalled() {
            return called;
        }
    }
}
