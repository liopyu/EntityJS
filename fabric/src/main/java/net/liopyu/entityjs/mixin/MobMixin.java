package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.common.util.overrides.CallbackUtils;
import net.liopyu.entityjs.common.util.implementation.MobAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.liopyu.entityjs.events.EntityModificationEventJS.*;

@Mixin(value = Mob.class, remap = true)
public class MobMixin implements MobAccessor /*implements IModifyEntityJS*/ {
    @Unique
    private Object entityJs$builder;

    @Shadow protected MoveControl moveControl;
    @Shadow protected PathNavigation navigation;

    @Override
    public void entityJs$setNavigation(PathNavigation navigation) {
        this.navigation = navigation;
    }

    @Override
    public void entityJs$setMoveControl(MoveControl moveControl) {
        this.moveControl = moveControl;
    }

    /* @Override
     public ModifyMobBuilder entityJs$getBuilder() {
         return entityJs$builder instanceof ModifyMobBuilder ? (ModifyMobBuilder) entityJs$builder : null;
     }
 */
    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private Mob entityJs$getLivingEntity() {
        return (Mob) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    private Object entityJs$withReturnFallback(String fieldName, CallbackInfoReturnable<?> cir, Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, cir, cir::getReturnValue, callback);
    }

    @Inject(method = "canPickUpLoot", at = @At("RETURN"), cancellable = true)
    private void entityJs$canPickUpLoot(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyMobBuilder builder && builder.canPickUpLoot != null) {
            Object result = entityJs$withReturnFallback("canPickUpLoot", cir,
                    () -> builder.canPickUpLoot.test(entityJs$getLivingEntity()));
            if (result instanceof Boolean value) {
                cir.setReturnValue(value);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canPickUpLoot from entity: "
                        + entityJs$entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isSunBurnTick", at = @At("RETURN"), cancellable = true)
    private void entityJs$isSunBurnTick(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyMobBuilder builder && builder.isSunBurnTick != null) {
            Object result = entityJs$withReturnFallback("isSunBurnTick", cir,
                    () -> builder.isSunBurnTick.test(entityJs$getLivingEntity()));
            if (result instanceof Boolean value) {
                cir.setReturnValue(value);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSunBurnTick from entity: "
                        + entityJs$entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    private void entityJs$getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder instanceof ModifyMobBuilder builder && builder.experienceReward != null) {
            Object result = EntityJSHelperClass.convertObjectToDesired(
                    entityJs$withReturnFallback("experienceReward", cir,
                            () -> builder.experienceReward.apply(entityJs$getLivingEntity())), "integer");
            if (result != null) {
                cir.setReturnValue((int) result);
            }
        }
    }

    @Unique
    private void entityJs$withMixinFallback(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.with(fieldName, ci, () -> null, callback);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onMobInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EntityJSUtils.handlesOwnEntityJsCallbacks(entityJs$getLivingEntity())) {
            entityJs$builder = null;
            return;
        }
        var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
        entityJs$builder = eventJS.getBuilder();
        if (!(entityJs$getLivingEntity() instanceof IAnimatableJS)) {
            if (EventHandlers.addGoalTargets.hasListeners()) {
                EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(entityJs$getLivingEntity(), entityJs$getLivingEntity().targetSelector), entityJs$getTypeId());
            }
            if (EventHandlers.addGoalSelectors.hasListeners()) {
                EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(entityJs$getLivingEntity(), entityJs$getLivingEntity().goalSelector), entityJs$getTypeId());
            }
        }
    }

    @Unique
    public String entityJs$getTypeId() {
        return Objects.requireNonNull(EntityType.getKey(entityJs$getLivingEntity().getType())).toString();
    }

    @Inject(method = "getControllingPassenger", at = @At("RETURN"), remap = true, cancellable = true)
    public void getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.controlledByFirstPassenger != null) {
                if (!builder.controlledByFirstPassenger) return;
                Entity var2 = entityJs$getLivingEntity().getFirstPassenger();
                LivingEntity var10000;
                if (var2 instanceof LivingEntity entity) {
                    var10000 = entity;
                } else {
                    var10000 = null;
                }
                cir.setReturnValue(var10000);
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("RETURN"), remap = true, cancellable = true)
    public void mobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.mobInteract != null) {
                final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
                Object result = entityJs$withReturnFallback("mobInteract", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.mobInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: mobInteract.");
                    return cir.getReturnValue();
                });
                if (result instanceof InteractionResult interactionResult) {
                    cir.setReturnValue(interactionResult);
                }
            }
        }

    }

    @Inject(method = "canTakeItem", at = @At("RETURN"), remap = true, cancellable = true)
    public void canTakeItem(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyMobBuilder builder && builder.canTakeItem != null) {
            var context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), itemStack, entityJs$getLivingEntity().level());
            Object value = entityJs$withReturnFallback("canTakeItem", cir, () -> builder.canTakeItem.test(context));
            if (value instanceof Boolean result) {
                cir.setReturnValue(result);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityJs$entityName() + ". Value: " + value + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"), remap = true, cancellable = true)
    public void doHurtTarget(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (entityJs$builder != null && builder.onHurtTarget != null) {
                final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
                Object result = entityJs$withReturnFallback("onHurtTarget", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
                    return cir.getReturnValue();
                });
                if (result instanceof Boolean value) {
                    cir.setReturnValue(value);
                }
            }
        }
    }

    /*@Inject(method = "aiStep", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void aiStep(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.aiStep != null) {
                EntityJSHelperClass.consumerCallback(builder.aiStep, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: aiStep.");
            }
        }
    }*/

    @Inject(method = "tickLeash", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void tickLeash(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.tickLeash != null) {
                Player $$0 = (Player) entityJs$getLivingEntity().getLeashHolder();
                final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, entityJs$getLivingEntity());
                entityJs$withMixinFallback("tickLeash", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.tickLeash, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tickLeash."));
            }
        }
    }

    @Inject(method = "setTarget", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void setTarget(LivingEntity pTarget, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.onTargetChanged != null) {
                final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(pTarget, entityJs$getLivingEntity());
                entityJs$withMixinFallback("onTargetChanged", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onTargetChanged, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onTargetChanged."));
            }
        }
    }

    @Inject(method = "ate", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void ate(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.ate != null) {
                entityJs$withMixinFallback("ate", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.ate, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: ate."));

            }
        }
    }

    @Inject(method = "createNavigation", at = @At("RETURN"), remap = true, cancellable = true)
    protected void createNavigation(Level pLevel, CallbackInfoReturnable<PathNavigation> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (entityJs$builder == null || builder.createNavigation == null) return;
            final ContextUtils.EntityLevelContext context = new ContextUtils.EntityLevelContext(pLevel, entityJs$getLivingEntity());
            Object obj = entityJs$withReturnFallback("createNavigation", cir, () -> builder.createNavigation.apply(context));
            if (obj instanceof PathNavigation p) {
                cir.setReturnValue(p);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for createNavigation from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be PathNavigation. Defaulting to super method.");

        }
    }

    @Inject(method = "canBeLeashed", at = @At("RETURN"), remap = true, cancellable = true)
    public void canBeLeashed(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canBeLeashed != null) {
                final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, entityJs$getLivingEntity());
            Object obj = entityJs$withReturnFallback("canBeLeashed", cir, () -> builder.canBeLeashed.apply(context));
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeLeashed from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "getMainArm", at = @At("RETURN"), remap = true, cancellable = true)
    public void getMainArm(CallbackInfoReturnable<HumanoidArm> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.mainArm != null) cir.setReturnValue((HumanoidArm) builder.mainArm);
        }
    }


    @Inject(method = "getAmbientSound", at = @At("RETURN"), remap = true, cancellable = true)
    protected void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.setAmbientSound != null) {
                cir.setReturnValue(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.setAmbientSound));
            }
        }
    }

    @Inject(method = "canHoldItem", at = @At("RETURN"), remap = true, cancellable = true)
    public void canHoldItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canHoldItem != null) {
                final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, entityJs$getLivingEntity());
            Object obj = entityJs$withReturnFallback("canHoldItem", cir, () -> builder.canHoldItem.apply(context));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "shouldDespawnInPeaceful", at = @At("RETURN"), remap = true, cancellable = true)
    protected void shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.shouldDespawnInPeaceful == null) return;
            cir.setReturnValue(builder.shouldDespawnInPeaceful);

        }
    }

    @Inject(method = "isPersistenceRequired", at = @At("RETURN"), remap = true, cancellable = true)
    public void isPersistenceRequired(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.isPersistenceRequired == null) return;
            cir.setReturnValue(builder.isPersistenceRequired);

        }
    }

    @Inject(method = "getMeleeAttackRangeSqr", at = @At("RETURN"), remap = true, cancellable = true)
    public void getMeleeAttackRangeSqr(LivingEntity pEntity, CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.meleeAttackRangeSqr != null) {
                Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("meleeAttackRangeSqr", cir, () -> builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity())), "double");
                if (obj != null) {
                    cir.setReturnValue((double) obj);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityJs$entityName() + ". Value: " + builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity()) + ". Must be a double. Defaulting to " + cir.getReturnValue());
                }
            }
        }
    }

    @Inject(method = "getAmbientSoundInterval", at = @At("RETURN"), remap = true, cancellable = true)
    public void getAmbientSoundInterval(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.ambientSoundInterval != null)
                cir.setReturnValue((int) builder.ambientSoundInterval);
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At("RETURN"), remap = true, cancellable = true)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.removeWhenFarAway == null) {
                return;
            }
            final ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, entityJs$getLivingEntity());
            Object obj = entityJs$withReturnFallback("removeWhenFarAway", cir, () -> builder.removeWhenFarAway.test(context));
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }
}
