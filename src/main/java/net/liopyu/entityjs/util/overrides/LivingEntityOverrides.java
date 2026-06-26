package net.liopyu.entityjs.util.overrides;

import net.liopyu.entityjs.util.BooleanCallback;

import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.overrides.OverrideUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class LivingEntityOverrides {
    private LivingEntityOverrides() {
    }

    public static <T extends LivingEntity & IAnimatableJS> void tick(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        fallback.run();
        if (builder == null || builder.tick == null) {
            return;
        }
        OverrideUtils.withAlreadyCalled(() ->
                EntityJSHelperClass.consumerCallback(builder.tick, entity, "[EntityJS]: Error in " + entityName(entity) + "builder for field: tick."));
    }

    public static <T extends LivingEntity & IAnimatableJS> void onAddedToLevel(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.onAddedToWorld, "onAddedToWorld", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void onRemovedFromLevel(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onRemovedFromWorld, "onRemovedFromWorld", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void setSprinting(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onSprint, "onSprint", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void rideTick(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.rideTick, "rideTick", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void onClientRemoval(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onClientRemoval, "onClientRemoval", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void lavaHurt(T entity, BaseLivingEntityBuilder<T> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.lavaHurt, "lavaHurt", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> void playerTouch(T entity, BaseLivingEntityBuilder<T> builder, Player player, Runnable fallback) {
        if (builder == null || builder.playerTouch == null) {
            fallback.run();
            return;
        }

        var context = new ContextUtils.PlayerEntityContext(player, entity);
        OverrideUtils.with(() -> {
            fallback.run();
            return null;
        }, () -> EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityName(entity) + "builder for field: playerTouch."));
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean canCollideWith(T entity, BaseLivingEntityBuilder<T> builder, Entity other, BooleanSupplier fallback) {
        if (builder == null || builder.canCollideWith == null) {
            return fallback.getAsBoolean();
        }

        var context = new ContextUtils.CollidingEntityContext(entity, other);
        try {
            Object result = OverrideUtils.with(fallback::getAsBoolean, () -> builder.canCollideWith.test(context));
            Object converted = EntityJSHelperClass.convertObjectToDesired(result, "boolean");
            return converted instanceof Boolean bool ? bool : fallback.getAsBoolean();
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Exception in " + entityName(entity) + " builder for field: canCollideWith. Defaulting to " + fallback.getAsBoolean(), e);
            return fallback.getAsBoolean();
        }
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean canFreeze(T entity, BaseLivingEntityBuilder<T> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.canFreeze, "canFreeze", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean isFreezing(T entity, BaseLivingEntityBuilder<T> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isFreezing, "isFreezing", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean isCurrentlyGlowing(T entity, BaseLivingEntityBuilder<T> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isCurrentlyGlowing, "isCurrentlyGlowing", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean dampensVibrations(T entity, BaseLivingEntityBuilder<T> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.dampensVibrations, "dampensVibrations", fallback);
    }

    public static <T extends LivingEntity & IAnimatableJS> boolean showVehicleHealth(T entity, BaseLivingEntityBuilder<T> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.showVehicleHealth, "showVehicleHealth", fallback);
    }

    public static boolean isPushable(BaseLivingEntityBuilder<?> builder) {
        return builder != null && builder.isPushable;
    }

    private static <T extends LivingEntity & IAnimatableJS> void afterSuper(T entity, Consumer<LivingEntity> callback, String fieldName, Runnable fallback) {
        fallback.run();
        if (callback == null) {
            return;
        }
        OverrideUtils.withAlreadyCalled(() ->
                EntityJSHelperClass.consumerCallback(callback, entity, "[EntityJS]: Error in " + entityName(entity) + "builder for field: " + fieldName + "."));
    }

    private static <T extends LivingEntity & IAnimatableJS> void beforeSuper(T entity, Consumer<LivingEntity> callback, String fieldName, Runnable fallback) {
        if (callback == null) {
            fallback.run();
            return;
        }

        boolean called = OverrideUtils.with(() -> {
            fallback.run();
            return null;
        }, () -> EntityJSHelperClass.consumerCallback(callback, entity, "[EntityJS]: Error in " + entityName(entity) + "builder for field: " + fieldName + "."));
        if (!called) {
            fallback.run();
        }
    }

    private static <T extends LivingEntity & IAnimatableJS> boolean booleanOverride(T entity, BooleanCallback<LivingEntity> callback, String fieldName, BooleanSupplier fallback) {
        if (callback == null) {
            return fallback.getAsBoolean();
        }

        try {
            Object result = OverrideUtils.with(fallback::getAsBoolean, () -> callback.test(entity));
            Object converted = EntityJSHelperClass.convertObjectToDesired(result, "boolean");
            return converted instanceof Boolean bool ? bool : fallback.getAsBoolean();
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Exception in " + entityName(entity) + " builder for field: " + fieldName + ". Defaulting to " + fallback.getAsBoolean(), e);
            return fallback.getAsBoolean();
        }
    }

    private static String entityName(Entity entity) {
        return entity.getType().toString();
    }
}
