package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.common.util.overrides.CallbackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = PathfinderMob.class, remap = true)
public abstract class PathfinderMobMixin/*implements IModifyEntityJS*/ {

    @Unique
    public Object entityJs$builder;

    /* @Override
     public ModifyPathfinderMobBuilder entityJs$getBuilder() {
         return entityJs$builder instanceof ModifyPathfinderMobBuilder ? (ModifyPathfinderMobBuilder) entityJs$builder : null;
     }*/
    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private PathfinderMob entityJs$getLivingEntity() {
        return (PathfinderMob) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    private Object entityJs$withReturnFallback(String fieldName, CallbackInfoReturnable<?> cir, Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, cir, cir::getReturnValue, callback);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onMobInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EntityJSUtils.handlesOwnEntityJsCallbacks(entityJs$getLivingEntity())) {
            entityJs$builder = null;
            return;
        }

        var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
        entityJs$builder = eventJS.getBuilder();
    }

    @Inject(method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelReader;)F", at = @At("RETURN"), remap = true, cancellable = true)
    public void getWalkTargetValue(BlockPos pPos, LevelReader pLevel, CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.walkTargetValue == null) return;
            final ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pPos, pLevel, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("walkTargetValue", cir, () -> builder.walkTargetValue.apply(context)), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a float. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "shouldStayCloseToLeashHolder", at = @At("RETURN"), remap = true, cancellable = true)
    protected void shouldStayCloseToLeashHolder(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.shouldStayCloseToLeashHolder == null) return;
            Object value = entityJs$withReturnFallback("shouldStayCloseToLeashHolder", cir, () -> builder.shouldStayCloseToLeashHolder.test(entityJs$getLivingEntity()));
            if (value instanceof Boolean b) {
                cir.setReturnValue(b);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityJs$entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "followLeashSpeed", at = @At("RETURN"), remap = true, cancellable = true)
    protected void followLeashSpeed(CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.followLeashSpeed == null) return;
            cir.setReturnValue(builder.followLeashSpeed);
        }
    }
}
