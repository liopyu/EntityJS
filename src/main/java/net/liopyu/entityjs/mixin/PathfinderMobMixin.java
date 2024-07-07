package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = PathfinderMob.class, remap = false)
public class PathfinderMobMixin /*implements IModifyEntityJS*/ {
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

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onMobInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(getOrCreate(entityJs$getLivingEntity().getType(), entityJs$getLivingEntity()));
        }
        Object builder = EntityModificationEventJS.getOrCreate(entityJs$getLivingEntity().getType(), entityJs$getLivingEntity()).getBuilder();
        entityJs$builder = builder;
    }

    @Inject(method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelReader;)F", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getWalkTargetValue(BlockPos pPos, LevelReader pLevel, CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.walkTargetValue == null) return;
            final ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pPos, pLevel, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.walkTargetValue.apply(context), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + entityJs$entityName() + ". Value: " + builder.walkTargetValue.apply(context) + ". Must be a float. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "shouldStayCloseToLeashHolder", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void shouldStayCloseToLeashHolder(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.shouldStayCloseToLeashHolder == null) return;
            Object value = builder.shouldStayCloseToLeashHolder.apply(entityJs$getLivingEntity());
            if (value instanceof Boolean b) {
                cir.setReturnValue(b);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityJs$entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "followLeashSpeed", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void followLeashSpeed(CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyPathfinderMobBuilder builder) {
            if (builder.followLeashSpeed == null) return;
            cir.setReturnValue(builder.followLeashSpeed);
        }
    }
}
