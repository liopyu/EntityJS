package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.common.util.BooleanCallback;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;
import java.util.function.Function;

public class MoveControlJSBuilder {

    public transient BooleanCallback<Mob> hasWanted;
    public transient Function<Mob, Object> getSpeedModifier;
    public transient Consumer<ContextUtils.SetWantedPositionContext> setWantedPosition;
    public transient Consumer<ContextUtils.StrafeContext> strafe;
    public transient Consumer<Mob> tick;
    public transient Function<ContextUtils.RotLerpContext, Object> rotlerp;
    public transient BooleanCallback<ContextUtils.IsWalkableContext> isWalkable;

    public MoveControlJSBuilder setHasWanted(BooleanCallback<Mob> hasWanted) {
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

    public MoveControlJSBuilder setIsWalkable(BooleanCallback<ContextUtils.IsWalkableContext> isWalkable) {
        this.isWalkable = isWalkable;
        return this;
    }
}
