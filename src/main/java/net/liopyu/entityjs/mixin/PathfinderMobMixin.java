package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.living.modification.*;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.IModifyEntityJS;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.liopyu.entityjs.events.EntityModificationEventJS.eventMap;
import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = PathfinderMob.class, remap = false)
public class PathfinderMobMixin implements IModifyEntityJS {
    @Unique
    public Object entityJs$builder;

    @Override
    public ModifyEntityBuilder entityJs$getBuilder() {
        return null;//(ModifyEntityBuilder) entityJs$builder;
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
    private void entityjs$onMobInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        Object entity = entityJs$getLivingEntity();
        if (EventHandlers.modifyEntity.hasListeners() && eventMap.containsKey(entityJs$getLivingEntity().getType())) {
            EventHandlers.modifyEntity.post(getOrCreate(entityJs$getLivingEntity().getType()));
        }
        if (eventMap.containsKey(entityJs$getLivingEntity().getType())) {
            Object builder = EntityModificationEventJS.getOrCreate(entityJs$getLivingEntity().getType()).getBuilder();
            if (entity instanceof TamableAnimal) {
                entityJs$builder = builder;
            } else if (entity instanceof Animal) {
                entityJs$builder = builder;
            } else if (entity instanceof AgeableMob) {
                entityJs$builder = builder;
            } else if (entity instanceof PathfinderMob) {
                entityJs$builder = builder;
            } else throw new IllegalStateException("Unknown builder in EntityMixin: " + builder.getClass());
        }
    }

    /*@Inject(method = "getWalkTargetValue(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/LevelReader;)F", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getWalkTargetValue(BlockPos pPos, LevelReader pLevel, CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
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
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.shouldStayCloseToLeashHolder == null) return;
        Object value = entityJs$builder.shouldStayCloseToLeashHolder.apply(entityJs$getLivingEntity());
        if (value instanceof Boolean b) {
            cir.setReturnValue(b);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityJs$entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "followLeashSpeed", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void followLeashSpeed(CallbackInfoReturnable<Double> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        cir.setReturnValue(entityJs$builder.followLeashSpeed == null ? cir.getReturnValue() : entityJs$builder.followLeashSpeed);
    }*/
}
