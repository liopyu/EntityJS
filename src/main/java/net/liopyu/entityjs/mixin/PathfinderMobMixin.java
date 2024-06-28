package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.living.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.ModifyPathfinderMobBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.IModifyEntityJS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = PathfinderMob.class, remap = false)
public class PathfinderMobMixin implements IModifyEntityJS {
    @Unique
    private ModifyPathfinderMobBuilder entityJs$builder;

    @Override
    public ModifyEntityBuilder entityJs$getBuilder() {
        return entityJs$builder;
    }

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
    private void entityjs$onMobInit(EntityType<LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        EntityModificationEventJS event = EntityModificationEventJS.create(entityJs$getLivingEntity());
        entityJs$builder = (ModifyPathfinderMobBuilder) event.getEvent();
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(event);
        }
    }

    @Inject(method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelReader;)F", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getWalkTargetValue(BlockPos pPos, LevelReader pLevel, CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.walkTargetValue == null) return;
        final ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pPos, pLevel, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.walkTargetValue.apply(context), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.walkTargetValue.apply(context) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "shouldStayCloseToLeashHolder", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void shouldStayCloseToLeashHolder(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.shouldStayCloseToLeashHolder == null) return;
        Object value = entityJs$builder.shouldStayCloseToLeashHolder.apply(entityJs$getLivingEntity());
        if (value instanceof Boolean b) {
            cir.setReturnValue(b);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityJs$entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "followLeashSpeed", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void followLeashSpeed(CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(entityJs$builder.followLeashSpeed == null ? cir.getReturnValue() : entityJs$builder.followLeashSpeed);
    }
}
