package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityMixin implements IModifyEntityJS {

    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private LivingEntity entityJs$getLivingEntity() {
        return (LivingEntity) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    public ModifyLivingEntityBuilder entityJs$builder;

    @Override
    public ModifyLivingEntityBuilder entityJs$getBuilder() {
        return entityJs$builder;
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onEntityInit(EntityType<LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        entityJs$builder = new ModifyLivingEntityBuilder(entityJs$getLivingEntity());
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(entityJs$builder);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$aiStep(CallbackInfo ci) {
        if (entityJs$builder.aiStep != null) {
            entityJs$builder.aiStep.accept(entityJs$getLivingEntity());
        }
    }

    //(Base LivingEntity/Entity Overrides)
    @Inject(method = "doHurtTarget", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$isAlliedTo(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder.onHurtTarget != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entityJs$getLivingEntity(), entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
        }
    }

    @Inject(method = "travel", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$travel(Vec3 pTravelVector, CallbackInfo ci) {
        if (entityJs$builder.travel != null) {
            final ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.travel, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: travel.");

        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$tick(CallbackInfo ci) {
        if (entityJs$builder.tick != null) {
            if (!entityJs$getLivingEntity().level().isClientSide()) {
                EntityJSHelperClass.consumerCallback(entityJs$builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick.");
            }
        }
    }

    @Inject(method = "doAutoAttackOnTouch", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$doAutoAttackOnTouch(LivingEntity pTarget, CallbackInfo ci) {
        if (entityJs$builder.doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(entityJs$getLivingEntity(), pTarget);
            EntityJSHelperClass.consumerCallback(entityJs$builder.doAutoAttackOnTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: doAutoAttackOnTouch.");
        }
    }

    @Inject(method = "decreaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$decreaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder.onDecreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onDecreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }

    @Inject(method = "increaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$increaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder.onIncreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onIncreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onIncreaseAirSupply.");

        }
    }

    @Inject(method = "blockedByShield", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$blockedByShield(LivingEntity pDefender, CallbackInfo ci) {
        if (entityJs$builder.onBlockedByShield != null) {
            var context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pDefender);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onBlockedByShield, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEquipItem(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        if (entityJs$builder.onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(pSlot, pOldItem, pNewItem, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onEquipItem, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEquipItem.");

        }
    }

    @Inject(method = "onEffectAdded", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEffectAdded(MobEffectInstance pEffectInstance, Entity pEntity, CallbackInfo ci) {
        if (entityJs$builder.onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onEffectAdded, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectAdded.");

        }
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEffectRemoved(MobEffectInstance pEffectInstance, CallbackInfo ci) {
        if (entityJs$builder.onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onEffectRemoved, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectRemoved.");
        }
    }

    @Inject(method = "heal", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$heal(float pHealAmount, CallbackInfo ci) {
        if (entityJs$builder.onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(entityJs$getLivingEntity(), pHealAmount);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onLivingHeal, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingHeal.");

        }
    }

    @Inject(method = "die", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$die(DamageSource pDamageSource, CallbackInfo ci) {
        if (entityJs$builder.onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(entityJs$getLivingEntity(), pDamageSource);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onDeath, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDeath.");
        }
    }

    @Inject(method = "dropCustomDeathLoot", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$dropCustomDeathLoot(DamageSource pDamageSource, int pLooting, boolean pHitByPlayer, CallbackInfo ci) {
        if (entityJs$builder.dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(pDamageSource, pLooting, pHitByPlayer, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.dropCustomDeathLoot, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: dropCustomDeathLoot.");

        }
    }


    @Inject(method = "getSoundVolume", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getSoundVolume(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.setSoundVolume != null) {
            cir.setReturnValue(entityJs$builder.setSoundVolume);
        }
    }

    @Inject(method = "getWaterSlowDown", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.setWaterSlowDown != null) {
            cir.setReturnValue(entityJs$builder.setWaterSlowDown);
        }
    }


    @Inject(method = "getStandingEyeHeight", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions, CallbackInfoReturnable<Float> cir) {
        final ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.setStandingEyeHeight.apply(context), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "isPushable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isPushable != null) {
            cir.setReturnValue(entityJs$builder.isPushable);
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.blockSpeedFactor == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$getLivingEntity().getType() + ". Value: " + entityJs$builder.blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "shouldDropLoot", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.shouldDropLoot != null) {
            Object obj = entityJs$builder.shouldDropLoot.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isAffectedByFluids", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAffectedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isAffectedByFluids != null) {
            Object obj = entityJs$builder.isAffectedByFluids.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isAlwaysExperienceDropper", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAlwaysExperienceDropper(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isAlwaysExperienceDropper != null) {
            cir.setReturnValue(entityJs$builder.isAlwaysExperienceDropper);
        }
    }

    @Inject(method = "isImmobile", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isImmobile(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isImmobile != null) {
            Object obj = entityJs$builder.isImmobile.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "calculateFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$calculateFallDamage(float pFallDistance, float pDamageMultiplier, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder.calculateFallDamage == null) return;
        final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(pFallDistance, pDamageMultiplier, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.calculateFallDamage.apply(context), "integer");
        if (obj != null) {
            cir.setReturnValue((int) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + cir.getReturnValue());
    }


    @Inject(method = "getHurtSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getHurtSound(DamageSource pDamageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder.setHurtSound == null) return;
        final ContextUtils.HurtContext context = new ContextUtils.HurtContext(entityJs$getLivingEntity(), pDamageSource);
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.setHurtSound.apply(context), "resourcelocation");
        if (obj != null)
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) obj)));
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
    }


    @Inject(method = "canAttackType", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canAttackType(EntityType<?> pEntityType, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(entityJs$getLivingEntity(), pEntityType);
            Object obj = entityJs$builder.canAttackType.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "getScale", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getScale(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.scale == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.scale.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.scale.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "shouldDropExperience", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldDropExperience(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.shouldDropExperience != null) {
            Object obj = entityJs$builder.shouldDropExperience.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "getVisibilityPercent", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getVisibilityPercent(Entity pLookingEntity, CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder.visibilityPercent != null) {
            final ContextUtils.VisualContext context = new ContextUtils.VisualContext(pLookingEntity, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.visibilityPercent.apply(context), "double");
            if (obj != null) {
                cir.setReturnValue((double) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canAttack(LivingEntity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canAttack != null) {
            final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pTarget);
            Object obj = entityJs$builder.canAttack.apply(context);
            if (obj instanceof Boolean b) {
                boolean bool = b && cir.getReturnValue();
                cir.setReturnValue(bool);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canBeAffected", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canBeAffected(MobEffectInstance pEffectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canBeAffected == null) {
            return;
        }
        final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
        Object result = entityJs$builder.canBeAffected.apply(context);
        if (result instanceof Boolean) {
            cir.setReturnValue((boolean) result);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityJs$entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "isInvertedHealAndHarm", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.invertedHealAndHarm == null) {
            return;
        }
        Object obj = entityJs$builder.invertedHealAndHarm.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "getDeathSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder.setDeathSound == null) return;
        cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.setDeathSound)));
    }

    @Inject(method = "getFallSounds", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getFallSounds(CallbackInfoReturnable<LivingEntity.Fallsounds> cir) {
        if (entityJs$builder.fallSounds != null)
            cir.setReturnValue(new LivingEntity.Fallsounds(
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.smallFallSound)),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.largeFallSound))
            ));
    }

    @Inject(method = "getEatingSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder.eatingSound != null)
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.eatingSound)));
    }

    @Inject(method = "onClimbable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.onClimbable == null) {
            return;
        }
        Object obj = entityJs$builder.onClimbable.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + cir.getReturnValue());
    }


    @Inject(method = "canBreatheUnderwater", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canBreatheUnderwater(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canBreatheUnderwater != null) {
            cir.setReturnValue(entityJs$builder.canBreatheUnderwater);
        }
    }

    @Inject(method = "causeFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onLivingFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
        }
    }

    @Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (entityJs$builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint.");
        }
    }

    @Inject(method = "getJumpBoostPower", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getJumpBoostPower(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder.jumpBoostPower == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.jumpBoostPower.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) cir.setReturnValue((float) obj);
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.jumpBoostPower.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "canStandOnFluid", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canStandOnFluid(FluidState pFluidState, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(entityJs$getLivingEntity(), pFluidState);
            Object obj = entityJs$builder.canStandOnFluid.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isSensitiveToWater", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isSensitiveToWater != null) {
            Object obj = entityJs$builder.isSensitiveToWater.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$stopRiding(CallbackInfo ci) {
        if (entityJs$builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding.");
        }
    }

    @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$rideTick(CallbackInfo ci) {
        if (entityJs$builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick.");

        }
    }

    @Inject(method = "onItemPickup", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onItemPickup(ItemEntity pItemEntity, CallbackInfo ci) {
        if (entityJs$builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(entityJs$getLivingEntity(), pItemEntity);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onItemPickup, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onItemPickup.");
        }
    }

    @Inject(method = "hasLineOfSight", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$hasLineOfSight(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.hasLineOfSight != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
            Object obj = entityJs$builder.hasLineOfSight.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "onEnterCombat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onEnterCombat(CallbackInfo ci) {
        if (entityJs$builder.onEnterCombat != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onEnterCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEnterCombat.");
        }
    }

    @Inject(method = "onLeaveCombat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onLeaveCombat(CallbackInfo ci) {
        if (entityJs$builder.onLeaveCombat != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onLeaveCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLeaveCombat.");

        }
    }

    @Inject(method = "isAffectedByPotions", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAffectedByPotions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isAffectedByPotions != null) {
            Object obj = entityJs$builder.isAffectedByPotions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "attackable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$attackable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isAttackable != null) {
            Object obj = entityJs$builder.isAttackable.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canTakeItem", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canTakeItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pStack, entityJs$getLivingEntity().level());
            Object obj = entityJs$builder.canTakeItem.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isSleeping(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isSleeping != null) {
            Object obj = entityJs$builder.isSleeping.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "startSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$startSleeping(BlockPos pPos, CallbackInfo ci) {
        if (entityJs$builder.onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(entityJs$getLivingEntity(), pPos);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onStartSleeping, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStartSleeping.");
        }
    }

    @Inject(method = "stopSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$stopSleeping(CallbackInfo ci) {
        if (entityJs$builder.onStopSleeping != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onStopSleeping, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopSleeping.");
        }
    }

    @Inject(method = "eat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$eat(Level pLevel, ItemStack pFood, CallbackInfoReturnable<ItemStack> cir) {
        if (entityJs$builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pFood, pLevel);
            EntityJSHelperClass.consumerCallback(entityJs$builder.eat, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: eat.");
        }
    }

    @Inject(method = "shouldRiderFaceForward", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldRiderFaceForward(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, entityJs$getLivingEntity());
            Object obj = entityJs$builder.shouldRiderFaceForward.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canFreeze", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canFreeze != null) {
            Object obj = entityJs$builder.canFreeze.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
            Object obj = entityJs$builder.isCurrentlyGlowing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canDisableShield", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canDisableShield(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canDisableShield != null) {
            Object obj = entityJs$builder.canDisableShield.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$actuallyHurt(DamageSource pDamageSource, float pDamageAmount, CallbackInfo ci) {
        if (entityJs$builder.onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onHurt, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurt.");
        }
    }

    @Inject(method = "getExperienceReward", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder.experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.experienceReward.apply(entityJs$getLivingEntity()), "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.experienceReward.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "canChangeDimensions", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canChangeDimensions != null) {
            Object obj = entityJs$builder.canChangeDimensions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport, CallbackInfo ci) {
        if (entityJs$builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(pX, pY, pZ, pYaw, pPitch, pPosRotationIncrements, pTeleport, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo.");
        }
    }
}
