package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;
import java.util.function.Function;

public class MoveControlJSBuilder {

    public transient Function<Mob, Object> hasWanted;
    public transient Function<Mob, Object> getSpeedModifier;
    public transient Consumer<ContextUtils.SetWantedPositionContext> setWantedPosition;
    public transient Consumer<ContextUtils.StrafeContext> strafe;
    public transient Consumer<Mob> tick;
    public transient Function<ContextUtils.RotLerpContext, Object> rotlerp;
    public transient Function<ContextUtils.IsWalkableContext, Object> isWalkable;

    public MoveControlJSBuilder setHasWanted(Function<Mob, Object> hasWanted) {
        this.hasWanted = hasWanted;
        return this;
    }

    public MoveControlJSBuilder setSpeedModifier(Function<Mob, Object> getSpeedModifier) {
        this.getSpeedModifier = getSpeedModifier;
        return this;
    }

    public MoveControlJSBuilder setWantedPosition(Consumer<ContextUtils.SetWantedPositionContext> setWantedPosition) {
        this.setWantedPosition = setWantedPosition;
        return this;
    }

    public MoveControlJSBuilder setStrafe(Consumer<ContextUtils.StrafeContext> strafe) {
        this.strafe = strafe;
        return this;
    }

    public MoveControlJSBuilder tick(Consumer<Mob> tick) {
        this.tick = tick;
        return this;
    }

    public MoveControlJSBuilder setRotlerp(Function<ContextUtils.RotLerpContext, Object> rotlerp) {
        this.rotlerp = rotlerp;
        return this;
    }

    public MoveControlJSBuilder setIsWalkable(Function<ContextUtils.IsWalkableContext, Object> isWalkable) {
        this.isWalkable = isWalkable;
        return this;
    }
}