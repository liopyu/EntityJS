package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.overrides.CallbackUtils;
import net.liopyu.entityjs.util.implementation.MobAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = Mob.class, remap = true)
public class MobMixin implements MobAccessor {
    @Unique
    private Object entityJs$builder;

    /* @Override
     public ModifyMobBuilder entityJs$getBuilder() {
         return entityJs$builder instanceof ModifyMobBuilder ? (ModifyMobBuilder) entityJs$builder : null;
     }
 */
    @Shadow
    protected MoveControl moveControl;
    @Shadow
    protected PathNavigation navigation;

    @Override
    public void entityJs$setNavigation(PathNavigation nav) {
        this.navigation = nav;
    }

    @Override
    public void entityJs$setMoveControl(MoveControl control) {
        this.moveControl = control;
    }

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
    private void entityJs$withMixinFallback(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.with(fieldName, ci, () -> null, callback);
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

    @Inject(method = "canTakeItem", at = @At("RETURN"), remap = true, cancellable = true)
    public void canTakeItem(ItemStack pItemstack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canTakeItem != null) {
                try {
                    var context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pItemstack, entityJs$getLivingEntity().level());
                    Object obj = entityJs$withReturnFallback("canTakeItem", cir, () -> builder.canTakeItem.test(context));
                    if (obj instanceof Boolean b) {
                        cir.setReturnValue(b);
                    } else {
                        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
                    }
                } catch (Exception e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityJs$entityName() + "builder for field: canTakeItem. ", e);
                }
            }
        }
    }

    @Inject(method = "canPickUpLoot", at = @At("RETURN"), remap = true, cancellable = true)
    public void canPickUpLoot(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canPickUpLoot != null) {
                try {
                    Object obj = entityJs$withReturnFallback("canPickUpLoot", cir, () -> builder.canPickUpLoot.test(entityJs$getLivingEntity()));
                    if (obj instanceof Boolean b) {
                        cir.setReturnValue(b);
                    } else {
                        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canPickUpLoot from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
                    }
                } catch (Exception e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityJs$entityName() + "builder for field: canPickUpLoot. ", e);
                }
            }
        }
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

    @Inject(method = "isSunBurnTick", at = @At("RETURN"), remap = true, cancellable = true)
    protected void isSunBurnTick(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            try {
                if (builder.isSunBurnTick != null) {
                    Object obj = entityJs$withReturnFallback("isSunBurnTick", cir, () -> builder.isSunBurnTick.test(entityJs$getLivingEntity()));
                    if (obj instanceof Boolean b) {
                        cir.setReturnValue(b);
                    } else
                        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSunBurnTick from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.");
                }
            } catch (Exception e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityJs$entityName() + "builder for field: isSunBurnTick. ", e);
            }
        }
    }

    @Inject(method = "getExperienceReward", at = @At("RETURN"), remap = true, cancellable = true)
    private void entityjs$getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.experienceReward != null) {
                Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("experienceReward", cir, () -> builder.experienceReward.apply(entityJs$getLivingEntity())), "integer");
                if (obj != null) {
                    cir.setReturnValue((int) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be an integer. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "mobInteract", at = @At("RETURN"), remap = true, cancellable = true)
    public void mobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.mobInteract != null) {
                final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
                Object obj = entityJs$withReturnFallback("mobInteract", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.mobInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: mobInteract.");
                    return cir.getReturnValue();
                });
                if (obj instanceof InteractionResult result) {
                    cir.setReturnValue(result);
                }
            }
        }

    }

    @Inject(method = "doHurtTarget", at = @At("RETURN"), remap = true, cancellable = true)
    public void doHurtTarget(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (entityJs$builder != null && builder.onHurtTarget != null) {
                final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
                Object obj = entityJs$withReturnFallback("onHurtTarget", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
                    return cir.getReturnValue();
                });
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
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
                Object obj = entityJs$withReturnFallback("canBeLeashed", cir, () -> builder.canBeLeashed.test(context));
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
                cir.setReturnValue(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setAmbientSound));
            }
        }
    }

    @Inject(method = "canHoldItem", at = @At("RETURN"), remap = true, cancellable = true)
    public void canHoldItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canHoldItem != null) {
                final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, entityJs$getLivingEntity());
                Object obj = entityJs$withReturnFallback("canHoldItem", cir, () -> builder.canHoldItem.test(context));
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
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a double. Defaulting to " + cir.getReturnValue());
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
