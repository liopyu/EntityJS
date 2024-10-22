package net.liopyu.entityjs.builders.misc;

import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.ai.LookControlJS;
import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;
import java.util.function.Function;

public class LookControlJSBuilder {
    public transient Consumer<Mob> setLookAtVec3;
    public transient Consumer<Mob> setLookAtEntity;
    public transient Consumer<Mob> setLookAtEntityWithRotation;
    public transient Consumer<Mob> setLookAtCoords;
    public transient Consumer<Mob> tick;
    public transient Consumer<Mob> clampHeadRotationToBody;
    public transient Function<Mob, Boolean> resetXRotOnTick;
    public transient Function<Mob, Boolean> isLookingAtTarget;
    public transient Function<Mob, Double> setWantedX;
    public transient Function<Mob, Double> setWantedY;
    public transient Function<Mob, Double> setWantedZ;
    public transient Function<ContextUtils.RotationContext, Object> getXRotD;
    public transient Function<ContextUtils.RotationContext, Object> getYRotD;
    public transient Function<ContextUtils.RotationTowardsContext, Object> rotateTowards;

    public LookControlJSBuilder setLookAtVec3(Consumer<Mob> setLookAtVec3) {
        this.setLookAtVec3 = setLookAtVec3;
        return this;
    }

    public LookControlJSBuilder setIsLookingAtTarget(Function<Mob, Boolean> isLookingAtTarget) {
        this.isLookingAtTarget = isLookingAtTarget;
        return this;
    }

    public LookControlJSBuilder setWantedX(Function<Mob, Double> setWantedX) {
        this.setWantedX = setWantedX;
        return this;
    }

    public LookControlJSBuilder setWantedY(Function<Mob, Double> setWantedY) {
        this.setWantedY = setWantedY;
        return this;
    }

    public LookControlJSBuilder setWantedZ(Function<Mob, Double> setWantedZ) {
        this.setWantedZ = setWantedZ;
        return this;
    }

    public LookControlJSBuilder setXRotD(Function<ContextUtils.RotationContext, Object> getXRotD) {
        this.getXRotD = getXRotD;
        return this;
    }

    public LookControlJSBuilder setYRotD(Function<ContextUtils.RotationContext, Object> getYRotD) {
        this.getYRotD = getYRotD;
        return this;
    }

    public LookControlJSBuilder setRotateTowards(Function<ContextUtils.RotationTowardsContext, Object> rotateTowards) {
        this.rotateTowards = rotateTowards;
        return this;
    }

    public LookControlJSBuilder setLookAtEntity(Consumer<Mob> setLookAtEntity) {
        this.setLookAtEntity = setLookAtEntity;
        return this;
    }

    public LookControlJSBuilder setLookAtEntityWithRotation(Consumer<Mob> setLookAtEntityWithRotation) {
        this.setLookAtEntityWithRotation = setLookAtEntityWithRotation;
        return this;
    }

    public LookControlJSBuilder setLookAtCoords(Consumer<Mob> setLookAtCoords) {
        this.setLookAtCoords = setLookAtCoords;
        return this;
    }

    public LookControlJSBuilder tick(Consumer<Mob> tick) {
        this.tick = tick;
        return this;
    }

    public LookControlJSBuilder setClampHeadRotationToBody(Consumer<Mob> clampHeadRotationToBody) {
        this.clampHeadRotationToBody = clampHeadRotationToBody;
        return this;
    }

    public LookControlJSBuilder setResetXRotOnTick(Function<Mob, Boolean> resetXRotOnTick) {
        this.resetXRotOnTick = resetXRotOnTick;
        return this;
    }
}
