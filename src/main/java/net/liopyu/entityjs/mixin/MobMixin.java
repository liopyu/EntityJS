package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = Mob.class, remap = true)
public class MobMixin /*implements IModifyEntityJS*/ {
    @Unique
    private Object entityJs$builder;

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

    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onMobInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }
    }


    @Inject(method = "mobInteract", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void mobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.mobInteract != null) {
                final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
                EntityJSHelperClass.consumerCallback(builder.mobInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: mobInteract.");
            }
        }

    }

    @Inject(method = "doHurtTarget", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void doHurtTarget(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (entityJs$builder != null && builder.onHurtTarget != null) {
                final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
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
                EntityJSHelperClass.consumerCallback(builder.tickLeash, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tickLeash.");
            }
        }
    }

    @Inject(method = "setTarget", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void setTarget(LivingEntity pTarget, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.onTargetChanged != null) {
                final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(pTarget, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onTargetChanged, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onTargetChanged.");
            }
        }
    }

    @Inject(method = "ate", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void ate(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.ate != null) {
                EntityJSHelperClass.consumerCallback(builder.ate, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: ate.");

            }
        }
    }

    @Inject(method = "createNavigation", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void createNavigation(Level pLevel, CallbackInfoReturnable<PathNavigation> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (entityJs$builder == null || builder.createNavigation == null) return;
            final ContextUtils.EntityLevelContext context = new ContextUtils.EntityLevelContext(pLevel, entityJs$getLivingEntity());
            Object obj = builder.createNavigation.apply(context);
            if (obj instanceof PathNavigation p) {
                cir.setReturnValue(p);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for createNavigation from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be PathNavigation. Defaulting to super method.");

        }
    }

    @Inject(method = "canBeLeashed", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void canBeLeashed(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canBeLeashed != null) {
                final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, entityJs$getLivingEntity());
                Object obj = builder.canBeLeashed.apply(context);
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeLeashed from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "getMainArm", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void getMainArm(CallbackInfoReturnable<HumanoidArm> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.mainArm != null) cir.setReturnValue((HumanoidArm) builder.mainArm);
        }
    }


    @Inject(method = "getAmbientSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.setAmbientSound != null) {
                cir.setReturnValue(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setAmbientSound));
            }
        }
    }

    @Inject(method = "canHoldItem", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void canHoldItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.canHoldItem != null) {
                final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, entityJs$getLivingEntity());
                Object obj = builder.canHoldItem.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "shouldDespawnInPeaceful", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.shouldDespawnInPeaceful == null) return;
            cir.setReturnValue(builder.shouldDespawnInPeaceful);

        }
    }

    @Inject(method = "isPersistenceRequired", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isPersistenceRequired(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.isPersistenceRequired == null) return;
            cir.setReturnValue(builder.isPersistenceRequired);

        }
    }

    @Inject(method = "getMeleeAttackRangeSqr", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void getMeleeAttackRangeSqr(LivingEntity pEntity, CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.meleeAttackRangeSqr != null) {
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity()), "double");
                if (obj != null) {
                    cir.setReturnValue((double) obj);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityJs$entityName() + ". Value: " + builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity()) + ". Must be a double. Defaulting to " + cir.getReturnValue());
                }
            }
        }
    }

    @Inject(method = "getAmbientSoundInterval", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void getAmbientSoundInterval(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.ambientSoundInterval != null)
                cir.setReturnValue((int) builder.ambientSoundInterval);
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyMobBuilder builder) {
            if (builder.removeWhenFarAway == null) {
                return;
            }
            final ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, entityJs$getLivingEntity());
            Object obj = builder.removeWhenFarAway.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }
}