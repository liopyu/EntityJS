package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = LivingEntity.class, remap = false)
public abstract class LivingEntityMixin {

    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private LivingEntity entityJs$getLivingEntity() {
        return (LivingEntity) entityJs$entityObject;
    }

    public String entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    public ModifyLivingEntityBuilder builder;

    public LivingEntityMixin() {
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onEntityInit(EntityType<LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        builder = new ModifyLivingEntityBuilder(entityJs$getLivingEntity());
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(builder);
        }
    }
    /*@Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$tick(CallbackInfo ci) {

    }*/

    @Inject(method = "hurt", at = @At(value = "HEAD", ordinal = 0), remap = false)
    private void entityjs$hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if (builder.modifyHurt != null) {
            ContextUtils.EntityHurtContext context = new ContextUtils.EntityHurtContext(entityJs$getLivingEntity(), pSource, pAmount);
            try {
                builder.modifyHurt.accept(context);
            } catch (Exception e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in entityjs$hurt.", e);
            }

        }
    }


    public InteractionResult interact(Player pPlayer, InteractionHand pHand) {
        if (builder.onInteract != null) {
            final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
            builder.onInteract.accept(context);
        }
    }


    public void aiStep() {
        if (builder.aiStep != null) {
            builder.aiStep.accept(entityJs$getLivingEntity());
        }
    }

    //(Base LivingEntity/Entity Overrides)

    public boolean isAlliedTo(Entity pEntity) {
        if (builder.isAlliedTo != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
            Object obj = builder.isAlliedTo.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAlliedTo from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAlliedTo(pEntity));
        }
    }


    public boolean doHurtTarget(Entity pEntity) {
        if (builder != null && builder.onHurtTarget != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurtTarget.");

        }
    }


    public void travel(Vec3 pTravelVector) {
        if (builder.travel != null) {
            final ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.travel, context, "[EntityJS]: Error in " + entityName() + "builder for field: travel.");

        }
    }


    public void tick() {
        if (builder.tick != null) {
            if (!entityJs$getLivingEntity().level().isClientSide()) {
                EntityJSHelperClass.consumerCallback(builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: tick.");

            }
        }
    }


    public void onAddedToWorld() {
        if (builder.onAddedToWorld != null && !entityJs$getLivingEntity().level().isClientSide()) {
            EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onAddedToWorld.");

        }
    }


    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        if (builder.doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(entityJs$getLivingEntity(), target);
            EntityJSHelperClass.consumerCallback(builder.doAutoAttackOnTouch, context, "[EntityJS]: Error in " + entityName() + "builder for field: doAutoAttackOnTouch.");
        }
    }


    protected int decreaseAirSupply(int p_21303_) {
        if (builder.onDecreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(builder.onDecreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }


    protected int increaseAirSupply(int p_21307_) {
        if (builder.onIncreaseAirSupply != null) {
            EntityJSHelperClass.consumerCallback(builder.onIncreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onIncreaseAirSupply.");

        }
    }


    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
        if (builder.onBlockedByShield != null) {
            var context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), p_21246_);
            EntityJSHelperClass.consumerCallback(builder.onBlockedByShield, context, "[EntityJS]: Error in " + entityName() + "builder for field: onDecreaseAirSupply.");
        }
    }


    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {

        if (builder.onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(slot, previous, current, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.onEquipItem, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEquipItem.");

        }
    }


    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.onEffectAdded, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEffectAdded.");

        }
    }


    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {

        if (builder.onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.onEffectRemoved, context, "[EntityJS]: Error in " + entityName() + "builder for field: onEffectRemoved.");
        }
    }


    public void heal(float amount) {
        if (builder.onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(entityJs$getLivingEntity(), amount);
            EntityJSHelperClass.consumerCallback(builder.onLivingHeal, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingHeal.");

        }
    }


    public void die(@NotNull DamageSource damageSource) {
        if (builder.onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(entityJs$getLivingEntity(), damageSource);
            EntityJSHelperClass.consumerCallback(builder.onDeath, context, "[EntityJS]: Error in " + entityName() + "builder for field: onDeath.");
        }
    }


    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(damageSource, lootingMultiplier, allowDrops, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.dropCustomDeathLoot, context, "[EntityJS]: Error in " + entityName() + "builder for field: dropCustomDeathLoot.");

        }
    }


    protected void onFlap() {
        if (builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(builder.onFlap, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onFlap.");

        }
    }

    public LivingEntity getControllingPassenger() {
        Entity var2 = entityJs$getLivingEntity().getFirstPassenger();
        LivingEntity var10000;
        if (var2 instanceof LivingEntity entity) {
            var10000 = entity;
        } else {
            var10000 = null;
        }

        return var10000;
    }


    public boolean canCollideWith(Entity pEntity) {
        if (builder.canCollideWith != null) {
            final ContextUtils.CollidingEntityContext context = new ContextUtils.CollidingEntityContext(entityJs$getLivingEntity(), pEntity);
            Object obj = builder.canCollideWith.apply(context);
            if (obj instanceof Boolean b) return b;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canCollideWith(pEntity));
        }
    }


    protected float getSoundVolume() {
        return Objects.requireNonNullElseGet(builder.setSoundVolume, super::getSoundVolume);
    }


    protected float getWaterSlowDown() {
        return Objects.requireNonNullElseGet(builder.setWaterSlowDown, super::getWaterSlowDown);
    }


    protected float getBlockJumpFactor() {
        if (builder.setBlockJumpFactor == null) return super.getBlockJumpFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityName() + ". Value: " + builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + super.getBlockJumpFactor());
        return super.getBlockJumpFactor();
    }


    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        if (builder == null || builder.setStandingEyeHeight == null)
            return super.getStandingEyeHeight(pPose, pDimensions);
        final ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setStandingEyeHeight.apply(context), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + entityName() + ". Value: " + builder.setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + super.getStandingEyeHeight(pPose, pDimensions));
        return super.getStandingEyeHeight(pPose, pDimensions);
    }


    public boolean isPushable() {
        return builder.isPushable;
    }


    protected float getBlockSpeedFactor() {
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + builder.get() + ". Value: " + builder.blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
            return super.getBlockSpeedFactor();
        }
    }


    protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback) {
        if (builder.positionRider != null) {
            final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(entityJs$getLivingEntity(), pPassenger, pCallback);
            EntityJSHelperClass.consumerCallback(builder.positionRider, context, "[EntityJS]: Error in " + entityName() + "builder for field: positionRider.");
            return;
        }
        super.positionRider(pPassenger, pCallback);
    }


    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder.canAddPassenger == null) {
            return super.canAddPassenger(entity);
        }
        final ContextUtils.PassengerEntityContext context = new ContextUtils.PassengerEntityContext(entity, entityJs$getLivingEntity());
        Object obj = builder.canAddPassenger.apply(context);
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    protected boolean shouldDropLoot() {
        if (builder.shouldDropLoot != null) {
            Object obj = builder.shouldDropLoot.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + super.shouldDropLoot());
        }
        return super.shouldDropLoot();
    }


    protected boolean isAffectedByFluids() {
        if (builder.isAffectedByFluids != null) {
            Object obj = builder.isAffectedByFluids.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByFluids());
        }
        return super.isAffectedByFluids();
    }


    protected boolean isAlwaysExperienceDropper() {
        return builder.isAlwaysExperienceDropper;
    }


    protected boolean isImmobile() {
        if (builder.isImmobile != null) {
            Object obj = builder.isImmobile.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isImmobile());
        }
        return super.isImmobile();
    }


    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
            Object obj = builder.isFlapping.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFlapping());
        }
        return super.isFlapping();
    }


    public int calculateFallDamage(float fallDistance, float pDamageMultiplier) {
        if (builder.calculateFallDamage == null) return super.calculateFallDamage(fallDistance, pDamageMultiplier);
        final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(fallDistance, pDamageMultiplier, entityJs$getLivingEntity());
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.calculateFallDamage.apply(context), "integer");
        if (obj != null) {
            return (int) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityName() + ". Value: " + builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + super.calculateFallDamage(fallDistance, pDamageMultiplier));
        return super.calculateFallDamage(fallDistance, pDamageMultiplier);
    }


    protected boolean repositionEntityAfterLoad() {
        return Objects.requireNonNullElseGet(builder.repositionEntityAfterLoad, super::repositionEntityAfterLoad);
    }


    protected float nextStep() {
        if (builder.nextStep != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.nextStep.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                return (float) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for nextStep from entity: " + entityName() + ". Value: " + builder.nextStep.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + super.nextStep());
            }
        }
        return super.nextStep();
    }


    @Nullable

    protected SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (builder.setHurtSound == null) return super.getHurtSound(p_21239_);
        final ContextUtils.HurtContext context = new ContextUtils.HurtContext(entityJs$getLivingEntity(), p_21239_);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setHurtSound.apply(context), "resourcelocation");
        if (obj != null) return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) obj));
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityName() + ". Value: " + builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
        return super.getHurtSound(p_21239_);
    }


    protected SoundEvent getSwimSplashSound() {
        if (builder.setSwimSplashSound == null) return super.getSwimSplashSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSplashSound));
    }


    protected SoundEvent getSwimSound() {
        if (builder.setSwimSound == null) return super.getSwimSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSound));

    }


    public boolean canAttackType(@NotNull EntityType<?> entityType) {
        if (builder.canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(entityJs$getLivingEntity(), entityType);
            Object obj = builder.canAttackType.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttackType(entityType));
        }
        return super.canAttackType(entityType);
    }


    public float getScale() {
        if (builder.scale == null) return super.getScale();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.scale.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityName() + ". Value: " + builder.scale.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + super.getScale());
            return super.getScale();
        }
    }


    /*
    public boolean rideableUnderWater() {
        return Objects.requireNonNullElseGet(builder.rideableUnderWater, super::rideableUnderWater);
    }*/


    public boolean shouldDropExperience() {
        if (builder.shouldDropExperience != null) {
            Object obj = builder.shouldDropExperience.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldDropExperience());
        }
        return super.shouldDropExperience();
    }


    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        if (builder.visibilityPercent != null) {
            final ContextUtils.VisualContext context = new ContextUtils.VisualContext(p_20969_, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.visibilityPercent.apply(context), "double");
            if (obj != null) {
                return (double) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityName() + ". Value: " + builder.visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + super.getVisibilityPercent(p_20969_));
                return super.getVisibilityPercent(p_20969_);
            }
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }


    public boolean canAttack(@NotNull LivingEntity entity) {
        if (builder.canAttack != null) {
            final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), entity);
            Object obj = builder.canAttack.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj && super.canAttack(entity);
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canAttack(entity));
        }
        return super.canAttack(entity);
    }


    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        if (builder.canBeAffected == null) {
            return super.canBeAffected(effectInstance);
        }
        final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, entityJs$getLivingEntity());
        Object result = builder.canBeAffected.apply(context);
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.canBeAffected(effectInstance));
        return super.canBeAffected(effectInstance);
    }


    public boolean isInvertedHealAndHarm() {
        if (builder.invertedHealAndHarm == null) {
            return super.isInvertedHealAndHarm();
        }
        Object obj = builder.invertedHealAndHarm.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvertedHealAndHarm());
        return super.isInvertedHealAndHarm();
    }


    protected SoundEvent getDeathSound() {
        if (builder.setDeathSound == null) return super.getDeathSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setDeathSound));
    }


    public @NotNull LivingEntity.Fallsounds getFallSounds() {
        if (builder.fallSounds != null)
            return new LivingEntity.Fallsounds(
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.smallFallSound)),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.largeFallSound))
            );
        return super.getFallSounds();
    }


    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        if (builder.eatingSound != null)
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.eatingSound));
        return super.getEatingSound(itemStack);
    }


    public boolean onClimbable() {
        if (builder.onClimbable == null) {
            return super.onClimbable();
        }
        Object obj = builder.onClimbable.apply(entityJs$getLivingEntity());
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + super.onClimbable());
        return super.onClimbable();
    }


    //Deprecated but still works for 1.20.4 :shrug:

    public boolean canBreatheUnderwater() {
        return Objects.requireNonNullElseGet(builder.canBreatheUnderwater, super::canBreatheUnderwater);
    }


    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder.onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(entityJs$getLivingEntity(), damageMultiplier, distance, damageSource);
            EntityJSHelperClass.consumerCallback(builder.onLivingFall, context, "[EntityJS]: Error in " + entityName() + "builder for field: onLivingFall.");

        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }


    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onSprint.");

        }
        super.setSprinting(sprinting);
    }


    public float getJumpBoostPower() {
        if (builder.jumpBoostPower == null) return super.getJumpBoostPower();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.jumpBoostPower.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityName() + ". Value: " + builder.jumpBoostPower.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + super.getJumpBoostPower());
        return super.getJumpBoostPower();
    }


    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(entityJs$getLivingEntity(), fluidState);
            Object obj = builder.canStandOnFluid.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canStandOnFluid(fluidState));
        }
        return super.canStandOnFluid(fluidState);
    }


    public boolean isSensitiveToWater() {
        if (builder.isSensitiveToWater != null) {
            Object obj = builder.isSensitiveToWater.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSensitiveToWater());
        }
        return super.isSensitiveToWater();
    }


    public void stopRiding() {
        super.stopRiding();
        if (builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onStopRiding.");

        }
    }


    public void rideTick() {
        super.rideTick();
        if (builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: rideTick.");

        }
    }


    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        super.onItemPickup(p_21054_);
        if (builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(entityJs$getLivingEntity(), p_21054_);
            EntityJSHelperClass.consumerCallback(builder.onItemPickup, context, "[EntityJS]: Error in " + entityName() + "builder for field: onItemPickup.");

        }
    }


    public boolean hasLineOfSight(@NotNull Entity entity) {
        if (builder.hasLineOfSight != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entity, entityJs$getLivingEntity());
            Object obj = builder.hasLineOfSight.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.hasLineOfSight(entity));
        }
        return super.hasLineOfSight(entity);
    }


    public void onEnterCombat() {
        if (builder.onEnterCombat != null) {
            EntityJSHelperClass.consumerCallback(builder.onEnterCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onEnterCombat.");

        } else {
            super.onEnterCombat();
        }
    }


    public void onLeaveCombat() {
        if (builder.onLeaveCombat != null) {
            EntityJSHelperClass.consumerCallback(builder.onLeaveCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onLeaveCombat.");

        }
        super.onLeaveCombat();
    }


    public boolean isAffectedByPotions() {
        if (builder.isAffectedByPotions != null) {
            Object obj = builder.isAffectedByPotions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isAffectedByPotions());
        }
        return super.isAffectedByPotions();
    }


    public boolean attackable() {
        if (builder.isAttackable != null) {
            Object obj = builder.isAttackable.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.attackable());
        }
        return super.attackable();
    }


    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), itemStack, entityJs$getLivingEntity().level());
            Object obj = builder.canTakeItem.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTakeItem(itemStack));
        }
        return super.canTakeItem(itemStack);
    }


    public boolean isSleeping() {
        if (builder.isSleeping != null) {
            Object obj = builder.isSleeping.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isSleeping());
        }
        return super.isSleeping();
    }


    public void startSleeping(@NotNull BlockPos blockPos) {

        if (builder.onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(entityJs$getLivingEntity(), blockPos);
            EntityJSHelperClass.consumerCallback(builder.onStartSleeping, context, "[EntityJS]: Error in " + entityName() + "builder for field: onStartSleeping.");

        }
        super.startSleeping(blockPos);
    }


    public void stopSleeping() {
        if (builder.onStopSleeping != null) {
            EntityJSHelperClass.consumerCallback(builder.onStopSleeping, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onStopSleeping.");
        }
        super.stopSleeping();
    }


    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), itemStack, level);
            EntityJSHelperClass.consumerCallback(builder.eat, context, "[EntityJS]: Error in " + entityName() + "builder for field: eat.");
            return itemStack;
        }
        return super.eat(level, itemStack);
    }


    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, entityJs$getLivingEntity());
            Object obj = builder.shouldRiderFaceForward.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.shouldRiderFaceForward(player));
        }
        return super.shouldRiderFaceForward(player);
    }


    public boolean canFreeze() {
        if (builder.canFreeze != null) {
            Object obj = builder.canFreeze.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canFreeze());
        }
        return super.canFreeze();
    }


    public boolean isFreezing() {
        if (builder.isFreezing != null) {
            Object obj = builder.isFreezing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isFreezing());
        }
        return super.isFreezing();
    }


    public boolean isCurrentlyGlowing() {
        if (builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
            Object obj = builder.isCurrentlyGlowing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isCurrentlyGlowing());
        }
        return super.isCurrentlyGlowing();
    }


    public boolean canDisableShield() {
        if (builder.canDisableShield != null) {
            Object obj = builder.canDisableShield.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canDisableShield());
        }
        return super.canDisableShield();
    }


    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            EntityJSHelperClass.consumerCallback(builder.onClientRemoval, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onClientRemoval.");

        }
        super.onClientRemoval();
    }


    public void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (builder.onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.onHurt, context, "[EntityJS]: Error in " + entityName() + "builder for field: onHurt.");

        }
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }


    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(builder.lavaHurt, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: lavaHurt.");

        }
        super.lavaHurt();
    }


    public int getExperienceReward() {
        if (builder.experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.experienceReward.apply(entityJs$getLivingEntity()), "integer");
            if (obj != null) {
                return (int) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityName() + ". Value: " + builder.experienceReward.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + super.getExperienceReward());
        }
        return super.getExperienceReward();
    }


    public boolean dampensVibrations() {
        if (builder.dampensVibrations != null) {
            Object obj = builder.dampensVibrations.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.dampensVibrations());
        }
        return super.dampensVibrations();
    }


    public void playerTouch(Player p_20081_) {
        if (builder.playerTouch != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(p_20081_, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityName() + "builder for field: playerTouch.");
        }
    }


    public boolean showVehicleHealth() {
        if (builder.showVehicleHealth != null) {
            Object obj = builder.showVehicleHealth.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.showVehicleHealth());
        }
        return super.showVehicleHealth();
    }


    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            final ContextUtils.ThunderHitContext context = new ContextUtils.ThunderHitContext(p_19927_, p_19928_, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityName() + "builder for field: thunderHit.");

        }
    }


    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder.isInvulnerableTo != null) {
            final ContextUtils.DamageContext context = new ContextUtils.DamageContext(entityJs$getLivingEntity(), p_20122_);
            Object obj = builder.isInvulnerableTo.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.isInvulnerableTo(p_20122_));
        }
        return super.isInvulnerableTo(p_20122_);
    }


    public boolean canChangeDimensions() {
        if (builder.canChangeDimensions != null) {
            Object obj = builder.canChangeDimensions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canChangeDimensions());
        }
        return super.canChangeDimensions();
    }


    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder.mayInteract != null) {
            final ContextUtils.MayInteractContext context = new ContextUtils.MayInteractContext(p_146843_, p_146844_, entityJs$getLivingEntity());
            Object obj = builder.mayInteract.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.mayInteract(p_146843_, p_146844_));
        }

        return super.mayInteract(p_146843_, p_146844_);
    }


    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            final ContextUtils.CanTrampleContext context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, entityJs$getLivingEntity());
            Object obj = builder.canTrample.apply(context);
            if (obj instanceof Boolean) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }


    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityName() + "builder for field: onRemovedFromWorld.");

        }
    }


    public int getMaxFallDistance() {
        if (builder.setMaxFallDistance == null) return super.getMaxFallDistance();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(entityJs$getLivingEntity()), "integer");
        if (obj != null)
            return (int) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityName() + ". Value: " + builder.setMaxFallDistance.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
        return super.getMaxFallDistance();
    }


    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
        if (builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityName() + "builder for field: lerpTo.");
        }
    }
}
