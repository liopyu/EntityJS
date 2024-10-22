package net.liopyu.entityjs.util.ai;

import net.liopyu.entityjs.builders.misc.LookControlJSBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LookControlJS extends LookControl {
    private final LookControlJSBuilder builder;

    private String entityName() {
        return mob.getType().toString();
    }

    public LookControlJS(Mob mob, LookControlJSBuilder builder) {
        super(mob);
        this.builder = builder;
    }

    public void setWantedZ(double wantedZ) {
        this.wantedZ = wantedZ;
    }

    public void setWantedY(double wantedY) {
        this.wantedY = wantedY;
    }

    public void setWantedX(double wantedX) {
        this.wantedX = wantedX;
    }

    public void setLookAtCooldown(int lookAtCooldown) {
        this.lookAtCooldown = lookAtCooldown;
    }

    public void setxMaxRotAngle(float xMaxRotAngle) {
        this.xMaxRotAngle = xMaxRotAngle;
    }

    public void setyMaxRotSpeed(float yMaxRotSpeed) {
        this.yMaxRotSpeed = yMaxRotSpeed;
    }

    public int getLookAtCooldown() {
        return lookAtCooldown;
    }

    public float getxMaxRotAngle() {
        return xMaxRotAngle;
    }

    public float getyMaxRotSpeed() {
        return yMaxRotSpeed;
    }

    public Mob getMob() {
        return mob;
    }

    public LookControlJSBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setLookAt(Vec3 pLookVector) {
        if (builder.setLookAtVec3 != null) {
            EntityJSHelperClass.consumerCallback(builder.setLookAtVec3, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: setLookAtVec3.");
        } else {
            super.setLookAt(pLookVector);
        }
    }

    @Override
    public void setLookAt(Entity pEntity) {
        if (builder.setLookAtEntity != null) {
            EntityJSHelperClass.consumerCallback(builder.setLookAtEntity, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: setLookAtEntity.");

        } else {
            super.setLookAt(pEntity);
        }
    }

    @Override
    public void setLookAt(Entity pEntity, float pDeltaYaw, float pDeltaPitch) {
        if (builder.setLookAtEntityWithRotation != null) {
            EntityJSHelperClass.consumerCallback(builder.setLookAtEntityWithRotation, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: setLookAtEntityWithRotation.");

        } else {
            super.setLookAt(pEntity, pDeltaYaw, pDeltaPitch);
        }
    }

    @Override
    public void setLookAt(double pX, double pY, double pZ) {
        if (builder.setLookAtCoords != null) {
            EntityJSHelperClass.consumerCallback(builder.setLookAtCoords, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: setLookAtCoords.");
        } else {
            super.setLookAt(pX, pY, pZ);
        }
    }

    @Override
    public void tick() {
        if (builder.tick != null) {
            EntityJSHelperClass.consumerCallback(builder.tick, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: tick.");
        } else {
            super.tick();
        }
    }

    @Override
    protected void clampHeadRotationToBody() {
        if (builder.clampHeadRotationToBody != null) {
            EntityJSHelperClass.consumerCallback(builder.clampHeadRotationToBody, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: clampHeadRotationToBody.");
        } else {
            super.clampHeadRotationToBody();
        }
    }

    @Override
    protected boolean resetXRotOnTick() {
        if (builder.resetXRotOnTick != null) {
            Object obj = builder.resetXRotOnTick.apply(mob);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for resetXRotOnTick from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.resetXRotOnTick();
    }

    @Override
    public boolean isLookingAtTarget() {
        if (builder.isLookingAtTarget != null) {
            Object obj = builder.isLookingAtTarget.apply(mob);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isLookingAtTarget from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.isLookingAtTarget();
    }

    @Override
    public double getWantedX() {
        if (builder.setWantedX != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setWantedX.apply(mob), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setWantedX from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getWantedX();
    }

    @Override
    public double getWantedY() {
        if (builder.setWantedY != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setWantedY.apply(mob), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setWantedY from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getWantedY();
    }

    @Override
    public double getWantedZ() {
        if (builder.setWantedZ != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setWantedZ.apply(mob), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setWantedZ from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getWantedZ();
    }

    @Override
    public Optional<Float> getXRotD() {
        if (builder.getXRotD != null) {
            ContextUtils.RotationContext context = new ContextUtils.RotationContext(wantedX, wantedY, wantedZ, mob);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.getXRotD.apply(context), "float");
            if (obj != null) return Optional.of((float) obj);
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for getXRotD from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getXRotD();
    }

    @Override
    public Optional<Float> getYRotD() {
        if (builder.getYRotD != null) {
            ContextUtils.RotationContext context = new ContextUtils.RotationContext(wantedX, wantedY, wantedZ, mob);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.getYRotD.apply(context), "float");
            if (obj != null) return Optional.of((float) obj);
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for getYRotD from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.getYRotD();
    }

    @Override
    public float rotateTowards(float pFrom, float pTo, float pMaxDelta) {
        if (builder.rotateTowards != null) {
            ContextUtils.RotationTowardsContext context = new ContextUtils.RotationTowardsContext(pFrom, pTo, pMaxDelta);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.rotateTowards.apply(context), "float");
            if (obj != null) return (float) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for rotateTowards from entity:" + entityName() + " Look Control builder. Value: " + obj + ". Defaulting to super method.");
        }
        return super.rotateTowards(pFrom, pTo, pMaxDelta);
    }
}
