package net.liopyu.entityjs.common.util.overrides;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

public final class CallbackUtils {
    @HideFromJS
    public static final CallbackInfoAccess CALLBACK_INFO = new CallbackInfoAccess();

    private static final ScopedVariable<CallbackFrame> CURRENT = new ScopedVariable<>();

    private CallbackUtils() {
    }

    @HideFromJS
    public static void with(String method, CallbackInfo callbackInfo, Supplier<Object> fallback, Runnable callback) {
        if (OverrideUtils.isCallingFallback()) {
            fallback.get();
            return;
        }

        CallbackFrame frame = new CallbackFrame(method, callbackInfo);
        if (!CallbackProfiler.enabled()) {
            CallbackFrame previous = CURRENT.pushValue(frame);
            try {
                OverrideUtils.with(fallback, callback);
            } finally {
                CURRENT.restore(previous);
            }
            return;
        }

        long started = System.nanoTime();
        CallbackFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "callbackFramePush", System.nanoTime() - started);
        try {
            CallbackProfiler.profile(method, () -> OverrideUtils.with(fallback, callback));
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "callbackFramePop", System.nanoTime() - started);
        }
    }

    @HideFromJS
    public static <T> T with(String method, CallbackInfoReturnable<?> callbackInfo, Supplier<Object> fallback, Supplier<T> callback) {
        if (OverrideUtils.isCallingFallback()) {
            return (T) fallback.get();
        }

        CallbackFrame frame = new CallbackFrame(method, callbackInfo);
        if (!CallbackProfiler.enabled()) {
            CallbackFrame previous = CURRENT.pushValue(frame);
            try {
                T value = OverrideUtils.with(fallback, callback);
                return frame.hasReturnValue() ? (T) frame.returnValue() : value;
            } finally {
                CURRENT.restore(previous);
            }
        }

        long started = System.nanoTime();
        CallbackFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "callbackFramePush", System.nanoTime() - started);
        try {
            T value = CallbackProfiler.profile(method, () -> OverrideUtils.with(fallback, callback));
            return frame.hasReturnValue() ? (T) frame.returnValue() : value;
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "callbackFramePop", System.nanoTime() - started);
        }
    }

    @HideFromJS
    public static <T> T with(String method, CallbackInfo callbackInfo, Supplier<Object> fallback, Supplier<T> callback) {
        if (OverrideUtils.isCallingFallback()) {
            return (T) fallback.get();
        }

        CallbackFrame frame = new CallbackFrame(method, callbackInfo);
        if (!CallbackProfiler.enabled()) {
            CallbackFrame previous = CURRENT.pushValue(frame);
            try {
                return OverrideUtils.with(fallback, callback);
            } finally {
                CURRENT.restore(previous);
            }
        }

        long started = System.nanoTime();
        CallbackFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "callbackFramePush", System.nanoTime() - started);
        try {
            return CallbackProfiler.profile(method, () -> OverrideUtils.with(fallback, callback));
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "callbackFramePop", System.nanoTime() - started);
        }
    }

    @HideFromJS
    public static void withAlreadyCalled(String method, CallbackInfo callbackInfo, Object value, Runnable callback) {
        if (OverrideUtils.isCallingFallback()) {
            return;
        }

        CallbackFrame frame = new CallbackFrame(method, callbackInfo);
        if (!CallbackProfiler.enabled()) {
            CallbackFrame previous = CURRENT.pushValue(frame);
            try {
                OverrideUtils.withAlreadyCalled(value, callback);
            } finally {
                CURRENT.restore(previous);
            }
            return;
        }

        long started = System.nanoTime();
        CallbackFrame previous = CURRENT.pushValue(frame);
        CallbackProfiler.recordLayer(method, "callbackFramePush", System.nanoTime() - started);
        try {
            CallbackProfiler.profile(method, () -> OverrideUtils.withAlreadyCalled(value, callback));
        } finally {
            started = System.nanoTime();
            CURRENT.restore(previous);
            CallbackProfiler.recordLayer(method, "callbackFramePop", System.nanoTime() - started);
        }
    }

    @HideFromJS
    public static void cancel() {
        if (!CURRENT.isBound()) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.getCallbackInfo().cancel() was called outside an EntityJS callback.");
            return;
        }
        CURRENT.get().cancel();
    }

    @HideFromJS
    public static void setReturnValue(Object value) {
        if (!CURRENT.isBound()) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.getCallbackInfo().setReturnValue() was called outside an EntityJS callback.");
            return;
        }
        CURRENT.get().setReturnValue(value);
    }

    @HideFromJS
    public static boolean isCancelled() {
        return CURRENT.isBound() && CURRENT.get().isCancelled();
    }

    @HideFromJS
    public static boolean hasReturnValue() {
        return CURRENT.isBound() && CURRENT.get().hasReturnValue();
    }

    @HideFromJS
    public static Object returnValue() {
        return CURRENT.isBound() ? CURRENT.get().returnValue() : null;
    }

    public static final class CallbackInfoAccess {
        private CallbackInfoAccess() {
        }

        @Info("Cancels the currently executing cancellable EntityJS modify callback.")
        public void cancel() {
            CallbackUtils.cancel();
        }

        @Info("Sets the return value for the currently executing returnable EntityJS modify callback.")
        public void setReturnValue(Object value) {
            CallbackUtils.setReturnValue(value);
        }

        @Info("Returns true when the currently executing EntityJS modify callback has been cancelled.")
        public boolean isCancelled() {
            return CallbackUtils.isCancelled();
        }

        @Info("Returns true when the currently executing EntityJS modify callback has set a return value.")
        public boolean hasReturnValue() {
            return CallbackUtils.hasReturnValue();
        }

        @Info("Returns the value set through EntityJSUtils.getCallbackInfo().setReturnValue() in the current EntityJS modify callback.")
        public Object returnValue() {
            return CallbackUtils.returnValue();
        }
    }

    private static final class CallbackFrame {
        private final String method;
        private final CallbackInfo callbackInfo;
        private boolean hasReturnValue;
        private Object returnValue;

        private CallbackFrame(String method, CallbackInfo callbackInfo) {
            this.method = method;
            this.callbackInfo = callbackInfo;
        }

        private void cancel() {
            if (callbackInfo == null) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.getCallbackInfo().cancel() was called in " + method + ", but this EntityJS callback has no cancellable mixin callback.");
                return;
            }
            if (!callbackInfo.isCancellable()) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.getCallbackInfo().cancel() was called in " + method + ", but the mixin callback is not cancellable.");
                return;
            }
            callbackInfo.cancel();
        }

        private void setReturnValue(Object value) {
            if (!(callbackInfo instanceof CallbackInfoReturnable<?>)) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: EntityJSUtils.getCallbackInfo().setReturnValue() was called in " + method + ", but this EntityJS callback has no return value.");
                return;
            }
            hasReturnValue = true;
            returnValue = value;
        }

        private boolean isCancelled() {
            return callbackInfo != null && callbackInfo.isCancelled();
        }

        private boolean hasReturnValue() {
            return hasReturnValue;
        }

        private Object returnValue() {
            return returnValue;
        }
    }
}
