package net.liopyu.entityjs.common.util.overrides;

public final class ScopedVariable<T> {
    private final ThreadLocal<T> value = new ThreadLocal<>();

    public Scope push(T newValue) {
        return new Scope(pushValue(newValue));
    }

    public T pushValue(T newValue) {
        T previous = value.get();
        value.set(newValue);
        return previous;
    }

    public void restore(T previous) {
        if (previous == null) {
            value.remove();
        } else {
            value.set(previous);
        }
    }

    public boolean isBound() {
        return value.get() != null;
    }

    public T get() {
        return value.get();
    }

    public final class Scope implements AutoCloseable {
        private final T previous;
        private boolean closed;

        private Scope(T previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (!closed) {
                restore(previous);
                closed = true;
            }
        }
    }
}
