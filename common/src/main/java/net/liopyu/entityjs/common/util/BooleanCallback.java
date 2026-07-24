package net.liopyu.entityjs.common.util;

import java.util.function.Function;

@FunctionalInterface
public interface BooleanCallback<T> extends Function<T, Object> {
    Object test(T value);

    @Override
    default Object apply(T value) {
        return test(value);
    }
}
