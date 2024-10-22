package net.liopyu.entityjs.util.ai;

import net.liopyu.entityjs.builders.misc.MoveControlJSBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class MoveControlJS extends MoveControl {

    private String entityName() {
        return mob.getType().toString();
    }

    private final MoveControlJSBuilder builder;

    public MoveControlJS(Mob mob, MoveControlJSBuilder builder) {
        super(mob);
        this.builder = builder;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void setStrafeRight(float strafeRight) {
        this.strafeRight = strafeRight;
    }

    public void setStrafeForwards(float strafeForwards) {
        this.strafeForwards = strafeForwards;
    }

    public Operation getOperation() {
        return operation;
    }

    public float getStrafeRight() {
        return strafeRight;
    }

    public float getStrafeForwards() {
        return strafeForwards;
    }

    public void setSpeedModifier(double speedModifier) {
        this.speedModifier = speedModifier;
    }

    @Override
    public double getWantedZ() {
        return wantedZ;
    }

    @Override
    public double getWantedY() {
        return wantedY;
    }

    @Override
    public double getWantedX() {
        return wantedX;
    }

    public Mob getMob() {
        return mob;
    }

    @Override
    public boolean hasWanted() {
        if (builder.hasWanted != null) {
            Object obj = builder.hasWanted.apply(mob);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasWanted from entity:" + entityName() + " Move Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.hasWanted();
    }

    @Override
    public double getSpeedModifier() {
        if (builder.getSpeedModifier != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.getSpeedModifier.apply(mob), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for getSpeedModifier from entity:" + entityName() + " Move Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getSpeedModifier();
    }

    @Override
    public void setWantedPosition(double pX, double pY, double pZ, double pSpeed) {
        if (builder.setWantedPosition != null) {
            ContextUtils.SetWantedPositionContext context = new ContextUtils.SetWantedPositionContext(pX, pY, pZ, pSpeed);
            builder.setWantedPosition.accept(context);
        } else {
            super.setWantedPosition(pX, pY, pZ, pSpeed);
        }
    }

    @Override
    public void strafe(float pForward, float pStrafe) {
        if (builder.strafe != null) {
            ContextUtils.StrafeContext context = new ContextUtils.StrafeContext(pForward, pStrafe);
            EntityJSHelperClass.consumerCallback(builder.strafe, context, "[EntityJS]: Error in " + entityName() + " Move Control builder for field: strafe.");
        } else {
            super.strafe(pForward, pStrafe);
        }
    }

    @Override
    public void tick() {
        if (builder.tick != null) {
            EntityJSHelperClass.consumerCallback(builder.tick, mob, "[EntityJS]: Error in " + entityName() + " Move Control builder for field: tick.");
        } else {
            super.tick();
        }
    }


    @Override
    protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
        if (builder.rotlerp != null) {
            ContextUtils.RotLerpContext context = new ContextUtils.RotLerpContext(pSourceAngle, pTargetAngle, pMaximumChange);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.rotlerp.apply(context), "float");
            if (obj != null) return (float) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for rotlerp from entity:" + entityName() + " Move Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.rotlerp(pSourceAngle, pTargetAngle, pMaximumChange);
    }

    @Override
    protected boolean isWalkable(float pRelativeX, float pRelativeZ) {
        if (builder.isWalkable != null) {
            ContextUtils.IsWalkableContext context = new ContextUtils.IsWalkableContext(pRelativeX, pRelativeZ);
            Object obj = builder.isWalkable.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isWalkable from entity:" + entityName() + " Move Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.isWalkable(pRelativeX, pRelativeZ);
    }
}