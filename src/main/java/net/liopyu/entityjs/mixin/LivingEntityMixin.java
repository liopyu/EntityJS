package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.living.modification.*;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
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

import static net.liopyu.entityjs.events.EntityModificationEventJS.eventMap;
import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

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
    public Object entityJs$builder;

    @Override
    public ModifyLivingEntityBuilder entityJs$getBuilder() {
        return null;//(ModifyLivingEntityBuilder) entityJs$builder;
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        Object entity = entityJs$getLivingEntity();
        if (EventHandlers.modifyEntity.hasListeners()) {
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
            } else if (entity instanceof Mob) {
                entityJs$builder = builder;
            } else if (entity instanceof LivingEntity) {
                entityJs$builder = builder;
            } else throw new IllegalStateException("Unknown builder in EntityMixin: " + builder.getClass());
        }
    }

    /*@Inject(method = "getMobType", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getMobType(CallbackInfoReturnable<MobType> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$getBuilder().mobType != null) {
            cir.setReturnValue(entityJs$getBuilder().mobType);
        }
    }*/

    @Inject(method = "tickDeath", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void tickDeath(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.tickDeath != null) {
                EntityJSHelperClass.consumerCallback(builder.tickDeath, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tickDeath.");
            }
        }
    }

    /*@Inject(method = "aiStep", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$aiStep(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().aiStep != null) {
            entityJs$getBuilder().aiStep.accept(entityJs$getLivingEntity());
        }
    }

    //(Base LivingEntity/Entity Overrides)
    @Inject(method = "doHurtTarget", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$isAlliedTo(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$getBuilder().onHurtTarget != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entityJs$getLivingEntity(), entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
        }
    }

    @Inject(method = "travel", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$travel(Vec3 pTravelVector, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().travel != null) {
            final ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().travel, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: travel.");

        }
    }

    @Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$tick(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$getBuilder().tick != null) {
            if (!entityJs$getLivingEntity().level().isClientSide()) {
                EntityJSHelperClass.consumerCallback(entityJs$getBuilder().tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick.");
            }
        }
    }

    @Inject(method = "doAutoAttackOnTouch", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$doAutoAttackOnTouch(LivingEntity pTarget, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(entityJs$getLivingEntity(), pTarget);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().doAutoAttackOnTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: doAutoAttackOnTouch.");
        }
    }

    @Inject(method = "decreaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$decreaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onDecreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onDecreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }

    @Inject(method = "increaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$increaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onIncreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onIncreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onIncreaseAirSupply.");

        }
    }

    @Inject(method = "blockedByShield", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$blockedByShield(LivingEntity pDefender, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onBlockedByShield != null) {
            var context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pDefender);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onBlockedByShield, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEquipItem(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(pSlot, pOldItem, pNewItem, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onEquipItem, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEquipItem.");

        }
    }

    @Inject(method = "onEffectAdded", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEffectAdded(MobEffectInstance pEffectInstance, Entity pEntity, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onEffectAdded, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectAdded.");

        }
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$onEffectRemoved(MobEffectInstance pEffectInstance, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onEffectRemoved, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectRemoved.");
        }
    }

    @Inject(method = "heal", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$heal(float pHealAmount, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(entityJs$getLivingEntity(), pHealAmount);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onLivingHeal, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingHeal.");

        }
    }

    @Inject(method = "die", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$die(DamageSource pDamageSource, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(entityJs$getLivingEntity(), pDamageSource);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onDeath, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDeath.");
        }
    }

    @Inject(method = "dropCustomDeathLoot", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$dropCustomDeathLoot(DamageSource pDamageSource, int pLooting, boolean pHitByPlayer, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(pDamageSource, pLooting, pHitByPlayer, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().dropCustomDeathLoot, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: dropCustomDeathLoot.");

        }
    }


    @Inject(method = "getSoundVolume", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getSoundVolume(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().setSoundVolume != null) {
            cir.setReturnValue(entityJs$getBuilder().setSoundVolume);
        }
    }

    @Inject(method = "getWaterSlowDown", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().setWaterSlowDown != null) {
            cir.setReturnValue(entityJs$getBuilder().setWaterSlowDown);
        }
    }


    @Inject(method = "getStandingEyeHeight", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions, CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        final ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().setStandingEyeHeight.apply(context), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "isPushable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isPushable != null) {
            cir.setReturnValue(entityJs$getBuilder().isPushable);
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().blockSpeedFactor == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$getLivingEntity().getType() + ". Value: " + entityJs$getBuilder().blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "shouldDropLoot", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().shouldDropLoot != null) {
            Object obj = entityJs$getBuilder().shouldDropLoot.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isAffectedByFluids", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAffectedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isAffectedByFluids != null) {
            Object obj = entityJs$getBuilder().isAffectedByFluids.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isAlwaysExperienceDropper", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAlwaysExperienceDropper(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isAlwaysExperienceDropper != null) {
            cir.setReturnValue(entityJs$getBuilder().isAlwaysExperienceDropper);
        }
    }

    @Inject(method = "isImmobile", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isImmobile(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isImmobile != null) {
            Object obj = entityJs$getBuilder().isImmobile.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "calculateFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$calculateFallDamage(float pFallDistance, float pDamageMultiplier, CallbackInfoReturnable<Integer> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().calculateFallDamage == null) return;
        final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(pFallDistance, pDamageMultiplier, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().calculateFallDamage.apply(context), "integer");
        if (obj != null) {
            cir.setReturnValue((int) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + cir.getReturnValue());
    }


    @Inject(method = "getHurtSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getHurtSound(DamageSource pDamageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().setHurtSound == null) return;
        final ContextUtils.HurtContext context = new ContextUtils.HurtContext(entityJs$getLivingEntity(), pDamageSource);
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().setHurtSound.apply(context), "resourcelocation");
        if (obj != null)
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) obj)));
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
    }


    @Inject(method = "canAttackType", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canAttackType(EntityType<?> pEntityType, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(entityJs$getLivingEntity(), pEntityType);
            Object obj = entityJs$getBuilder().canAttackType.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "getScale", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getScale(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().scale == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().scale.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().scale.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "shouldDropExperience", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldDropExperience(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().shouldDropExperience != null) {
            Object obj = entityJs$getBuilder().shouldDropExperience.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "getVisibilityPercent", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getVisibilityPercent(Entity pLookingEntity, CallbackInfoReturnable<Double> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().visibilityPercent != null) {
            final ContextUtils.VisualContext context = new ContextUtils.VisualContext(pLookingEntity, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().visibilityPercent.apply(context), "double");
            if (obj != null) {
                cir.setReturnValue((double) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canAttack(LivingEntity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canAttack != null) {
            final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pTarget);
            Object obj = entityJs$getBuilder().canAttack.apply(context);
            if (obj instanceof Boolean b) {
                boolean bool = b && cir.getReturnValue();
                cir.setReturnValue(bool);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canBeAffected", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canBeAffected(MobEffectInstance pEffectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canBeAffected == null) {
            return;
        }
        final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
        Object result = entityJs$getBuilder().canBeAffected.apply(context);
        if (result instanceof Boolean) {
            cir.setReturnValue((boolean) result);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityJs$entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "isInvertedHealAndHarm", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().invertedHealAndHarm == null) {
            return;
        }
        Object obj = entityJs$getBuilder().invertedHealAndHarm.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "getDeathSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().setDeathSound == null) return;
        cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$getBuilder().setDeathSound)));
    }

    @Inject(method = "getFallSounds", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getFallSounds(CallbackInfoReturnable<LivingEntity.Fallsounds> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().fallSounds != null)
            cir.setReturnValue(new LivingEntity.Fallsounds(
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$getBuilder().smallFallSound)),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$getBuilder().largeFallSound))
            ));
    }

    @Inject(method = "getEatingSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().eatingSound != null)
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$getBuilder().eatingSound)));
    }

    @Inject(method = "onClimbable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onClimbable == null) {
            return;
        }
        Object obj = entityJs$getBuilder().onClimbable.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + cir.getReturnValue());
    }


    @Inject(method = "canBreatheUnderwater", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canBreatheUnderwater(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canBreatheUnderwater != null) {
            cir.setReturnValue(entityJs$getBuilder().canBreatheUnderwater);
        }
    }

    @Inject(method = "causeFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onLivingFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
        }
    }

    @Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onSprint != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint.");
        }
    }

    @Inject(method = "getJumpBoostPower", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getJumpBoostPower(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().jumpBoostPower == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().jumpBoostPower.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) cir.setReturnValue((float) obj);
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().jumpBoostPower.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "canStandOnFluid", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canStandOnFluid(FluidState pFluidState, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(entityJs$getLivingEntity(), pFluidState);
            Object obj = entityJs$getBuilder().canStandOnFluid.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isSensitiveToWater", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isSensitiveToWater != null) {
            Object obj = entityJs$getBuilder().isSensitiveToWater.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$stopRiding(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding.");
        }
    }

    @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$rideTick(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().rideTick != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick.");

        }
    }

    @Inject(method = "onItemPickup", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onItemPickup(ItemEntity pItemEntity, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(entityJs$getLivingEntity(), pItemEntity);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onItemPickup, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onItemPickup.");
        }
    }

    @Inject(method = "hasLineOfSight", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$hasLineOfSight(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().hasLineOfSight != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
            Object obj = entityJs$getBuilder().hasLineOfSight.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "onEnterCombat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onEnterCombat(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onEnterCombat != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onEnterCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEnterCombat.");
        }
    }

    @Inject(method = "onLeaveCombat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$onLeaveCombat(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onLeaveCombat != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onLeaveCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLeaveCombat.");

        }
    }

    @Inject(method = "isAffectedByPotions", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isAffectedByPotions(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isAffectedByPotions != null) {
            Object obj = entityJs$getBuilder().isAffectedByPotions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "attackable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$attackable(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().isAttackable != null) {
            Object obj = entityJs$getBuilder().isAttackable.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canTakeItem", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canTakeItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pStack, entityJs$getLivingEntity().level());
            Object obj = entityJs$getBuilder().canTakeItem.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isSleeping(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$getBuilder().isSleeping != null) {
            Object obj = entityJs$getBuilder().isSleeping.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "startSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$startSleeping(BlockPos pPos, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(entityJs$getLivingEntity(), pPos);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onStartSleeping, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStartSleeping.");
        }
    }

    @Inject(method = "stopSleeping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$stopSleeping(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onStopSleeping != null) {
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onStopSleeping, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopSleeping.");
        }
    }

    @Inject(method = "eat", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$eat(Level pLevel, ItemStack pFood, CallbackInfoReturnable<ItemStack> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pFood, pLevel);
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().eat, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: eat.");
        }
    }

    @Inject(method = "shouldRiderFaceForward", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldRiderFaceForward(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, entityJs$getLivingEntity());
            Object obj = entityJs$getBuilder().shouldRiderFaceForward.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canFreeze", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canFreeze != null) {
            Object obj = entityJs$getBuilder().canFreeze.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$getBuilder().isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
            Object obj = entityJs$getBuilder().isCurrentlyGlowing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canDisableShield", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canDisableShield(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canDisableShield != null) {
            Object obj = entityJs$getBuilder().canDisableShield.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$actuallyHurt(DamageSource pDamageSource, float pDamageAmount, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().onHurt, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurt.");
        }
    }

    @Inject(method = "getExperienceReward", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$getBuilder().experienceReward.apply(entityJs$getLivingEntity()), "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityJs$entityName() + ". Value: " + entityJs$getBuilder().experienceReward.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "canChangeDimensions", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().canChangeDimensions != null) {
            Object obj = entityJs$getBuilder().canChangeDimensions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, boolean pTeleport, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$getBuilder().lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(pX, pY, pZ, pYaw, pPitch, pPosRotationIncrements, pTeleport, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$getBuilder().lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo.");
        }
    }*/
}
