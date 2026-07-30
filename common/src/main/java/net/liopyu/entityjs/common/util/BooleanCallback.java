package net.liopyu.entityjs.common.util;

@FunctionalInterface
public interface BooleanCallback<T> {
    Object test(T value);
}
