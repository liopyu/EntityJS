package net.liopyu.entityjs.util.overrides;

import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public final class NonLivingEntityOverrides {
    private NonLivingEntityOverrides() {
    }

    public static <T extends Entity> void tick(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        tick(entity, builder == null ? null : builder.tick, fallback);
    }

    public static <T extends Entity> void tick(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        tick(entity, builder == null ? null : builder.tick, fallback);
    }

    private static <T extends Entity> void tick(T entity, Consumer<Entity> callback, Runnable fallback) {
        fallback.run();
        if (callback == null) {
            return;
        }
        OverrideUtils.withAlreadyCalled(() ->
                EntityJSHelperClass.consumerCallback(callback, entity, "[EntityJS]: Error in " + entityName(entity) + "builder for field: tick."));
    }

    public static <T extends Entity> void onAddedToWorld(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.onAddedToWorld, "onAddedToWorld", fallback);
    }

    public static <T extends Entity> void onAddedToWorld(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.onAddedToWorld, "onAddedToWorld", fallback);
    }

    public static <T extends Entity> void onRemovedFromWorld(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onRemovedFromWorld, "onRemovedFromWorld", fallback);
    }

    public static <T extends Entity> void onRemovedFromWorld(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onRemovedFromWorld, "onRemovedFromWorld", fallback);
    }

    public static <T extends Entity> void setSprinting(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onSprint, "onSprint", fallback);
    }

    public static <T extends Entity> void setSprinting(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onSprint, "onSprint", fallback);
    }

    public static <T extends Entity> void rideTick(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.rideTick, "rideTick", fallback);
    }

    public static <T extends Entity> void rideTick(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        afterSuper(entity, builder == null ? null : builder.rideTick, "rideTick", fallback);
    }

    public static <T extends Entity> void onClientRemoval(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onClientRemoval, "onClientRemoval", fallback);
    }

    public static <T extends Entity> void onClientRemoval(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.onClientRemoval, "onClientRemoval", fallback);
    }

    public static <T extends Entity> void lavaHurt(T entity, BaseNonAnimatableEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.lavaHurt, "lavaHurt", fallback);
    }

    public static <T extends Entity> void lavaHurt(T entity, BaseEntityBuilder<?> builder, Runnable fallback) {
        beforeSuper(entity, builder == null ? null : builder.lavaHurt, "lavaHurt", fallback);
    }

    public static <T extends Entity> void playerTouch(T entity, BaseNonAnimatableEntityBuilder<?> builder, Player player, Runnable fallback) {
        playerTouch(entity, builder == null ? null : builder.playerTouch, player, fallback);
    }

    public static <T extends Entity> void playerTouch(T entity, BaseEntityBuilder<?> builder, Player player, Runnable fallback) {
        playerTouch(entity, builder == null ? null : builder.playerTouch, player, fallback);
    }

    private static <T extends Entity> void playerTouch(T entity, Consumer<ContextUtils.EntityPlayerContext> callback, Player player, Runnable fallback) {
        if (callback == null) {
            fallback.run();
            return;
        }

        var context = new ContextUtils.EntityPlayerContext(player, entity);
        OverrideUtils.replaceFallback(fallback, () ->
                EntityJSHelperClass.consumerCallback(callback, context, "[EntityJS]: Error in " + entityName(entity) + "builder for field: playerTouch."));
    }

    public static <T extends Entity> boolean canCollideWith(T entity, BaseNonAnimatableEntityBuilder<?> builder, Entity other, BooleanSupplier fallback) {
        return canCollideWith(entity, builder == null ? null : builder.canCollideWith, other, fallback);
    }

    public static <T extends Entity> boolean canCollideWith(T entity, BaseEntityBuilder<?> builder, Entity other, BooleanSupplier fallback) {
        return canCollideWith(entity, builder == null ? null : builder.canCollideWith, other, fallback);
    }

    private static <T extends Entity> boolean canCollideWith(T entity, Function<ContextUtils.ECollidingEntityContext, Object> callback, Entity other, BooleanSupplier fallback) {
        if (callback == null) {
            return fallback.getAsBoolean();
        }

        var context = new ContextUtils.ECollidingEntityContext(entity, other);
        return booleanOverride(entity, callback, context, "canCollideWith", fallback);
    }

    public static <T extends Entity> boolean canFreeze(T entity, BaseNonAnimatableEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.canFreeze, entity, "canFreeze", fallback);
    }

    public static <T extends Entity> boolean canFreeze(T entity, BaseEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.canFreeze, entity, "canFreeze", fallback);
    }

    public static <T extends Entity> boolean isFreezing(T entity, BaseNonAnimatableEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isFreezing, entity, "isFreezing", fallback);
    }

    public static <T extends Entity> boolean isFreezing(T entity, BaseEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isFreezing, entity, "isFreezing", fallback);
    }

    public static <T extends Entity> boolean isCurrentlyGlowing(T entity, BaseNonAnimatableEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isCurrentlyGlowing, entity, "isCurrentlyGlowing", fallback);
    }

    public static <T extends Entity> boolean isCurrentlyGlowing(T entity, BaseEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.isCurrentlyGlowing, entity, "isCurrentlyGlowing", fallback);
    }

    public static <T extends Entity> boolean dampensVibrations(T entity, BaseNonAnimatableEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.dampensVibrations, entity, "dampensVibrations", fallback);
    }

    public static <T extends Entity> boolean dampensVibrations(T entity, BaseEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.dampensVibrations, entity, "dampensVibrations", fallback);
    }

    public static <T extends Entity> boolean showVehicleHealth(T entity, BaseNonAnimatableEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.showVehicleHealth, entity, "showVehicleHealth", fallback);
    }

    public static <T extends Entity> boolean showVehicleHealth(T entity, BaseEntityBuilder<?> builder, BooleanSupplier fallback) {
        return booleanOverride(entity, builder == null ? null : builder.showVehicleHealth, entity, "showVehicleHealth", fallback);
    }

    public static boolean isPushable(BaseNonAnimatableEntityBuilder<?> builder) {
        return builder != null && builder.isPushable;
    }

    public static boolean isPushable(BaseEntityBuilder<?> builder) {
        return builder != null && builder.isPushable;
    }

    private static <T extends Entity> void afterSuper(T entity, Consumer<Entity> callback, String fieldName, Runnable fallback) {
        fallback.run();
        if (callback == null) {
            return;
        }
        OverrideUtils.withAlreadyCalled(() ->
                EntityJSHelperClass.consumerCallback(callback, entity, "[EntityJS]: Error in " + entityName(entity) + "builder for field: " + fieldName + "."));
    }

    private static <T extends Entity> void beforeSuper(T entity, Consumer<Entity> callback, String fieldName, Runnable fallback) {
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

    private static <T extends Entity, C> boolean booleanOverride(T entity, Function<C, Object> callback, C context, String fieldName, BooleanSupplier fallback) {
        if (callback == null) {
            return fallback.getAsBoolean();
        }

        try {
            Object result = OverrideUtils.with(fallback::getAsBoolean, () -> callback.apply(context));
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
