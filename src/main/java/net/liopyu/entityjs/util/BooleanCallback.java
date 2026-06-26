package net.liopyu.entityjs.util;

@FunctionalInterface
public interface BooleanCallback<T> {
    Object test(T value);
}
