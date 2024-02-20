package net.liopyu.entityjs.entities;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.builders.AnimalEntityJSBuilder;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.liopyu.liolib.util.GeckoLibUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The 'basic' implementation of a custom entity, implements most methods through the builder with some
 * conditionally delegating to the {@code super} implementation if the function is null. Other implementations
 * are <strong>not</strong> required to override every method in a class.<br><br>
 * <p>
 * Further, the only real requirements for a custom entity class is that the class signature respects the contract
 * <pre>{@code public class YourEntityClass extends <? extends LivingEntity> implements <? extends IAnimatableJS>}</pre>
 * A basic implementation for a custom {@link net.minecraft.world.entity.animal.Animal Animal} entity could be as simple as
 * <pre>{@code public class AnimalEntityJS extends Animal implements IAnimatableJS {
 *
 *     private final AnimalBuilder builder;
 *     private final AnimatableInstanceCache animatableInstanceCache;
 *
 *     public AnimalEntityJS(AnimalBuilder builder, EntityType<? extends Animal> type, Level level) {
 *         super(type, level);
 *         this.builder = builder;
 *         AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
 *     }
 *
 *     @Override
 *     public BaseLivingEntityBuilder<?> getBuilder() {
 *         return builder;
 *     }
 *
 *     @Override
 *     public AnimatableInstanceCache getAnimatableInstanceCache() {
 *         return animatableInstanceCache;
 *     }
 *
 *     @Override
 *     @Nullable
 *     public AnimalEntityJS getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
 *         return null;
 *     }
 * }}</pre>
 * Of course this does not implement any possible networking/synced entity data stuff. figure that out yourself, it scares me
 */
@MethodsReturnNonnullByDefault // Just remove the countless number of warnings present
@ParametersAreNonnullByDefault
public class AnimalEntityJS extends Animal implements IAnimatableJS {

    private final AnimatableInstanceCache getAnimatableInstanceCache;

    protected final AnimalEntityJSBuilder builder;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public String entityName() {
        return this.getType().toString();
    }

    public AnimalEntityJS(AnimalEntityJSBuilder builder, EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public BaseLivingEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimatableInstanceCache;
    }

    //Some logic overrides up here because there are different implementations in the other builders.


    @Override
    protected Brain.Provider<?> brainProvider() {
        if (EventHandlers.buildBrainProvider.hasListeners()) {
            final BuildBrainProviderEventJS event = new BuildBrainProviderEventJS();
            EventHandlers.buildBrainProvider.post(event, getTypeId());
            return event.provide();
        } else {
            return super.brainProvider();
        }
    }

    @Override
    protected Brain<AnimalEntityJS> makeBrain(Dynamic<?> p_21069_) {
        if (EventHandlers.buildBrain.hasListeners()) {
            final Brain<AnimalEntityJS> brain = UtilsJS.cast(brainProvider().makeBrain(p_21069_));
            EventHandlers.buildBrain.post(new BuildBrainEventJS<>(brain), getTypeId());
            return brain;
        } else {
            return UtilsJS.cast(super.makeBrain(p_21069_));
        }
    }

    @Override
    protected void registerGoals() {
        if (EventHandlers.addGoalTargets.hasListeners()) {
            EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(this, targetSelector), getTypeId());
        }
        if (EventHandlers.addGoalSelectors.hasListeners()) {
            EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(this, goalSelector), getTypeId());
        }
    }


    //Ageable Mob Overrides
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (builder.getBreedOffspring != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.getBreedOffspring, "resourcelocation");
            if (obj != null) {
                EntityType<?> breedOffspringType = ForgeRegistries.ENTITY_TYPES.getValue((ResourceLocation) obj);
                if (breedOffspringType != null) {
                    Entity breedOffspringEntity = breedOffspringType.create(serverLevel);
                    if (breedOffspringEntity instanceof AgeableMob) {
                        return (AgeableMob) breedOffspringEntity;
                    }
                }
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid resource location or Entity Type for breedOffspring: " + builder.getBreedOffspring + ". Must be an AgeableMob subclass. Defaulting to super method: " + builder.get());
            }
        }
        return null;
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        if (builder.isFood != null) {
            return builder.isFood.test(pStack);
        }
        return super.isFood(pStack);
    }

    public boolean isFoodPredicate(ItemStack pStack) {
        if (builder.isFoodPredicate == null) {
            return super.isFood(pStack);
        }
        final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isFoodPredicate.apply(context), "boolean");
        if (obj != null) {
            return (boolean) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFoodPredicate from entity: " + entityName() + ". Value: " + builder.isFoodPredicate.apply(context) + ". Must be a boolean. Defaulting to false.");
            return false;
        }
    }


    @Override
    public boolean canBreed() {
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canBreed.apply(this), "boolean");
        if (obj != null) {
            return (boolean) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBreed from entity: " + entityName() + ". Value: " + builder.canBreed.apply(this) + ". Must be a boolean. Defaulting to super method: " + super.canBreed());
            return super.canBreed();
        }
    }


    @Override
    public double getMyRidingOffset() {
        if (builder.myRidingOffset == null) return super.getMyRidingOffset();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.myRidingOffset.apply(this), "double");
        if (obj != null) return (double) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + entityName() + ". Value: " + builder.myRidingOffset.apply(this) + ". Must be a double. Defaulting to " + super.getMyRidingOffset());
        return super.getMyRidingOffset();
    }


    @Override
    public int getAmbientSoundInterval() {
        if (builder.ambientSoundInterval != null) return (int) builder.ambientSoundInterval;
        return super.getAmbientSoundInterval();
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        if (builder.removeWhenFarAway == null) return super.removeWhenFarAway(pDistanceToClosestPlayer);
        final ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.removeWhenFarAway.apply(context), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + entityName() + ". Value: " + builder.removeWhenFarAway.apply(context) + ". Must be a boolean. Defaulting to " + super.removeWhenFarAway(pDistanceToClosestPlayer));
        return super.removeWhenFarAway(pDistanceToClosestPlayer);
    }


    @Override
    public boolean canMate(Animal pOtherAnimal) {
        if (builder.canMate == null) return super.canMate(pOtherAnimal);
        final ContextUtils.EntityAnimalContext context = new ContextUtils.EntityAnimalContext(this, pOtherAnimal);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canMate.apply(context), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canMate from entity: " + entityName() + ". Value: " + builder.canMate.apply(context) + ". Must be a boolean. Defaulting to " + super.canMate(pOtherAnimal));
        return super.canMate(pOtherAnimal);
    }


    @Override
    public void spawnChildFromBreeding(ServerLevel pLevel, Animal pMate) {
        if (builder.onSpawnChildFromBreeding != null) {
            final ContextUtils.LevelAnimalContext context = new ContextUtils.LevelAnimalContext(pMate, this, pLevel);
            builder.onSpawnChildFromBreeding.accept(context);
            super.spawnChildFromBreeding(pLevel, pMate);
        } else {
            super.spawnChildFromBreeding(pLevel, pMate);
        }
    }


    @Override
    protected double followLeashSpeed() {
        return Objects.requireNonNullElseGet(builder.followLeashSpeed, super::followLeashSpeed);
    }

    //Mob Interact here because it has special implimentations due to breeding in AgeableMob classes.
    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {

        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (this.isFood(itemstack) || this.isFoodPredicate(itemstack)) {
            int i = this.getAge();
            if (!this.level.isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.setInLove(pPlayer);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby()) {
                this.usePlayerItem(pPlayer, pHand, itemstack);
                this.ageUp(getSpeedUpSecondsWhenFeeding(-i), true);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.level.isClientSide) {
                return InteractionResult.CONSUME;
            }
        }
        if (builder.onInteract != null) {
            final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.onInteract.apply(context), "interactionresult");
            if (obj != null) {
                return (InteractionResult) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onInteract from entity: " + entityName() + ". Value: " + obj + ". Must be an InteractionResult. Defaulting to " + super.mobInteract(pPlayer, pHand));
        }
        return super.mobInteract(pPlayer, pHand);

    }

    //Mob Overrides
    public boolean canJump() {
        return Objects.requireNonNullElse(builder.canJump, true);
    }

    public void onJump() {
        if (builder.onLivingJump != null) {
            builder.onLivingJump.accept(this);
        }
    }

    public void jump() {
        double jumpPower = this.getJumpPower() + this.getJumpBoostPower();
        Vec3 currentVelocity = this.getDeltaMovement();

        // Adjust the Y component of the velocity to the calculated jump power
        this.setDeltaMovement(currentVelocity.x, jumpPower, currentVelocity.z);

        if (this.isSprinting()) {
            // If sprinting, add a horizontal impulse for forward boost
            float yawRadians = this.getYRot() * 0.017453292F;
            this.setDeltaMovement(
                    this.getDeltaMovement().add(
                            -Math.sin(yawRadians) * 0.2,
                            0.0,
                            Math.cos(yawRadians) * 0.2
                    )
            );
        }

        this.hasImpulse = true;
        onJump();
        ForgeHooks.onLivingJump(this);
    }

    public boolean shouldJump() {
        BlockPos forwardPos = this.blockPosition().relative(this.getDirection());
        return this.level.loadedAndEntityCanStandOn(forwardPos, this) && this.getStepHeight() < this.level.getBlockState(forwardPos).getShape(this.level, forwardPos).max(Direction.Axis.Y);
    }

    @Override
    public HumanoidArm getMainArm() {
        if (builder.mainArm != null) return (HumanoidArm) builder.mainArm;
        return super.getMainArm();
    }


    @Override
    public void aiStep() {
        super.aiStep();
        if (builder.aiStep != null) {
            builder.aiStep.accept(this);
        }
        if (canJump() && this.onGround && this.getNavigation().isInProgress() && shouldJump()) {
            jump();
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader levelReader) {
        if (builder.walkTargetValue == null) return super.getWalkTargetValue(pos, levelReader);
        final ContextUtils.EntityBlockPosLevelContext context = new ContextUtils.EntityBlockPosLevelContext(pos, levelReader, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.walkTargetValue.apply(context), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for walkTargetValue from entity: " + entityName() + ". Value: " + builder.walkTargetValue.apply(context) + ". Must be a float. Defaulting to " + super.getWalkTargetValue(pos, levelReader));
        return super.getWalkTargetValue(pos, levelReader);
    }

    @Override
    protected void tickLeash() {
        super.tickLeash();
        if (builder.tickLeash != null) {
            Player $$0 = (Player) this.getLeashHolder();
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, this);
            builder.tickLeash.accept(context);
        }
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        if (builder.shouldStayCloseToLeashHolder == null) return super.shouldStayCloseToLeashHolder();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.shouldStayCloseToLeashHolder.apply(this), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldStayCloseToLeashHolder from entity: " + entityName() + ". Value: " + builder.shouldStayCloseToLeashHolder.apply(this) + ". Must be a boolean. Defaulting to " + super.shouldStayCloseToLeashHolder());
        return super.shouldStayCloseToLeashHolder();
    }


    @Override
    public boolean canCutCorner(BlockPathTypes pathType) {
        if (builder.canCutCorner != null) {
            final ContextUtils.EntityBlockPathTypeContext context = new ContextUtils.EntityBlockPathTypeContext(pathType, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canCutCorner.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCutCorner from entity: " + entityName() + ". Value: " + builder.canCutCorner.apply(context) + ". Must be a boolean. Defaulting to " + super.canCutCorner(pathType));
                return super.canCutCorner(pathType);
            }
        }
        return super.canCutCorner(pathType);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        LivingChangeTargetEvent changeTargetEvent = ForgeHooks.onLivingChangeTarget(this, target, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);
        if (builder.onTargetChanged != null) {
            if (!changeTargetEvent.isCanceled()) {
                assert target != null;
                @Nullable LivingEntity target1 = changeTargetEvent.getNewTarget();
                //noinspection removal
                ForgeHooks.onLivingSetAttackTarget(this, target1);
                final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(target, this);
                builder.onTargetChanged.accept(context);
            }
        } else super.setTarget(target);
    }

    public boolean canFireProjectileWeaponPredicate(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeaponPredicate != null) {
            final ContextUtils.EntityProjectileWeaponContext context = new ContextUtils.EntityProjectileWeaponContext(projectileWeapon, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canFireProjectileWeaponPredicate.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFireProjectileWeaponPredicate from entity: " + entityName() + ". Value: " + builder.canFireProjectileWeaponPredicate.apply(context) + ". Must be a boolean. Defaulting to false.");
                return false;
            }
        }
        return false;
    }


    public boolean canFireProjectileWeapons(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeapon != null) {
            return builder.canFireProjectileWeapon.test(projectileWeapon.getDefaultInstance()) && projectileWeapon instanceof ProjectileWeaponItem;
        }
        return super.canFireProjectileWeapon(projectileWeapon);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        if (canFireProjectileWeapons(projectileWeapon) || canFireProjectileWeaponPredicate(projectileWeapon)) {
            return canFireProjectileWeapons(projectileWeapon) && canFireProjectileWeaponPredicate(projectileWeapon);
        }
        return super.canFireProjectileWeapon(projectileWeapon);
    }

    @Override
    public void ate() {
        super.ate();
        if (builder.ate != null) {
            builder.ate.accept(this);
        }
    }


    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (builder.getAmbientSound != null) {
            return ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.getAmbientSound);
        } else {
            return super.getAmbientSound();
        }
    }


    @Override
    public boolean canHoldItem(ItemStack stack) {
        if (builder.canHoldItem != null) {
            final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(stack, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canHoldItem.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + entityName() + ". Value: " + builder.canHoldItem.apply(context) + ". Must be a boolean. Defaulting to " + super.canHoldItem(stack));
                return super.canHoldItem(stack);
            }
        } else {
            return super.canHoldItem(stack);
        }
    }


    @Override
    protected boolean shouldDespawnInPeaceful() {
        return Objects.requireNonNullElseGet(builder.shouldDespawnInPeaceful, super::shouldDespawnInPeaceful);
    }

    @Override
    public boolean isPersistenceRequired() {
        return Objects.requireNonNullElseGet(builder.isPersistenceRequired, super::isPersistenceRequired);
    }


    @Override
    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        if (builder.meleeAttackRangeSqr != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.meleeAttackRangeSqr.apply(this), "double");
            if (obj != null) {
                return (double) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityName() + ". Value: " + builder.meleeAttackRangeSqr.apply(this) + ". Must be a double. Defaulting to " + super.getMeleeAttackRangeSqr(entity));
                return super.getMeleeAttackRangeSqr(entity);
            }
        } else {
            return super.getMeleeAttackRangeSqr(entity);
        }
    }


    //(Base LivingEntity/Entity Overrides)
    @Override
    protected float getSoundVolume() {
        return Objects.requireNonNullElseGet(builder.setSoundVolume, super::getSoundVolume);
    }

    @Override
    protected float getWaterSlowDown() {
        return Objects.requireNonNullElseGet(builder.setWaterSlowDown, super::getWaterSlowDown);
    }

    @Override
    protected float getJumpPower() {
        if (builder.setJumpPower != null) {
            return builder.setJumpPower * this.getBlockJumpFactor();
        } else return super.getJumpPower();
    }


    @Override
    protected float getBlockJumpFactor() {
        if (builder.setBlockJumpFactor == null) return super.getBlockJumpFactor();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBlockJumpFactor.apply(this), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityName() + ". Value: " + builder.setBlockJumpFactor.apply(this) + ". Must be a float. Defaulting to " + super.getBlockJumpFactor());
        return super.getBlockJumpFactor();
    }

    @Override
    protected float getStandingEyeHeight(Pose pPose, EntityDimensions pDimensions) {
        if (builder == null || builder.setStandingEyeHeight == null)
            return super.getStandingEyeHeight(pPose, pDimensions);
        final ContextUtils.EntityPoseDimensionsContext context = new ContextUtils.EntityPoseDimensionsContext(pPose, pDimensions, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setStandingEyeHeight.apply(context), "float");
        if (obj != null) return (float) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setStandingEyeHeight from entity: " + entityName() + ". Value: " + builder.setStandingEyeHeight.apply(context) + ". Must be a float. Defaulting to " + super.getStandingEyeHeight(pPose, pDimensions));
        return super.getStandingEyeHeight(pPose, pDimensions);
    }


    @Override
    public boolean isPushable() {
        return builder.isPushable;
    }

    @Override
    protected float getBlockSpeedFactor() {
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(this), "float");
        if (builder.blockSpeedFactor == null) return super.getBlockSpeedFactor();
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + builder.get() + ". Value: " + builder.blockSpeedFactor.apply(this) + ". Must be a float, defaulting to " + super.getBlockSpeedFactor());
            return super.getBlockSpeedFactor();
        }
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder.canAddPassenger == null) return super.canAddPassenger(entity);
        final ContextUtils.PassengerEntityContext context = new ContextUtils.PassengerEntityContext(entity, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canAddPassenger.apply(context), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityName() + ". Value: " + builder.canAddPassenger.apply(context) + ". Must be a boolean, defaulting to " + super.canAddPassenger(entity));
        return super.canAddPassenger(entity);
    }


    @Override
    protected boolean shouldDropLoot() {
        if (builder.shouldDropLoot != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.shouldDropLoot.apply(this), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityName() + ". Value: " + builder.shouldDropLoot.apply(this) + ". Must be a boolean, defaulting to " + super.shouldDropLoot());
        }

        return super.shouldDropLoot();
    }


    @Override
    protected boolean isAffectedByFluids() {
        if (builder.isAffectedByFluids != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isAffectedByFluids.apply(this), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityName() + ". Value: " + builder.isAffectedByFluids.apply(this) + ". Must be a boolean. Defaulting to " + super.isAffectedByFluids());
        }

        return super.isAffectedByFluids();
    }


    @Override
    protected boolean isAlwaysExperienceDropper() {
        return builder.isAlwaysExperienceDropper;
    }


    @Override
    protected boolean isImmobile() {
        if (builder.isImmobile != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isImmobile.apply(this), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityName() + ". Value: " + builder.isImmobile.apply(this) + ". Must be a boolean. Defaulting to " + super.isImmobile());
        }

        return super.isImmobile();
    }


    @Override
    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isFlapping.apply(this), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityName() + ". Value: " + builder.isFlapping.apply(this) + ". Must be a boolean. Defaulting to " + super.isFlapping());
        }

        return super.isFlapping();
    }


    @Override
    public int calculateFallDamage(float fallDistance, float pDamageMultiplier) {
        if (builder.calculateFallDamage == null) return super.calculateFallDamage(fallDistance, pDamageMultiplier);
        final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(fallDistance, pDamageMultiplier, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.calculateFallDamage.apply(context), "integer");
        if (obj != null) {
            return (int) obj;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityName() + ". Value: " + builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + super.calculateFallDamage(fallDistance, pDamageMultiplier));
        return super.calculateFallDamage(fallDistance, pDamageMultiplier);
    }

    @Override
    public void tick() {
        super.tick();
        if (builder.tick != null) {
            if (!this.level.isClientSide()) {
                builder.tick.accept(this);
            }
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (builder.onAddedToWorld != null && !this.level.isClientSide()) {
            builder.onAddedToWorld.accept(this);
        }
    }


    @Override
    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        super.doAutoAttackOnTouch(target);
        if (builder.doAutoAttackOnTouch != null) {
            final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(this, target);
            builder.doAutoAttackOnTouch.accept(context);
        }
    }


    @Override
    protected int decreaseAirSupply(int p_21303_) {
        if (builder.onDecreaseAirSupply != null) {
            builder.onDecreaseAirSupply.accept(this);
        }
        return super.decreaseAirSupply(p_21303_);
    }

    @Override
    protected int increaseAirSupply(int p_21307_) {
        if (builder.onIncreaseAirSupply != null) {
            builder.onIncreaseAirSupply.accept(this);
        }
        return super.increaseAirSupply(p_21307_);
    }

    @Override
    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
        super.blockedByShield(p_21246_);
        if (builder.onBlockedByShield != null) {
            builder.onBlockedByShield.accept(p_21246_);
        }
    }


    @Override
    protected boolean repositionEntityAfterLoad() {
        return Objects.requireNonNullElseGet(builder.repositionEntityAfterLoad, super::repositionEntityAfterLoad);
    }

    @Override
    protected float nextStep() {
        if (builder.nextStep != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.nextStep.apply(this), "float");
            if (obj != null) {
                return (float) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for nextStep from entity: " + entityName() + ". Value: " + builder.nextStep.apply(this) + ". Must be a float, defaulting to " + super.nextStep());
            }
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Missing nextStep value for entity: " + entityName() + ". Defaulting to " + super.nextStep());
        }
        return super.nextStep();
    }


    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (builder.setHurtSound == null) return super.getHurtSound(p_21239_);
        final ContextUtils.HurtContext context = new ContextUtils.HurtContext(this, p_21239_);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setHurtSound.apply(context), "resourcelocation");
        if (obj != null) return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) obj));
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityName() + ". Value: " + builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");
        return super.getHurtSound(p_21239_);
    }


    @Override
    protected SoundEvent getSwimSplashSound() {
        if (builder.setSwimSplashSound == null) return super.getSwimSplashSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSplashSound));
    }


    @Override
    protected SoundEvent getSwimSound() {
        if (builder.setSwimSound == null) return super.getSwimSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSound));

    }


    @Override
    public boolean canAttackType(@NotNull EntityType<?> entityType) {
        if (builder.canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(this, entityType);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canAttackType.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityName() + ". Value: " + builder.canAttackType.apply(context) + ". Must be a boolean. Defaulting to " + super.canAttackType(entityType));
                return super.canAttackType(entityType);
            }
        }
        return super.canAttackType(entityType);
    }


    @Override
    public float getScale() {
        if (builder.scale == null) return super.getScale();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.scale.apply(this), "float");
        if (obj != null) {
            return (float) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityName() + ". Value: " + builder.scale.apply(this) + ". Must be a float. Defaulting to " + super.getScale());
            return super.getScale();
        }
    }


    @Override
    public boolean rideableUnderWater() {
        return Objects.requireNonNullElseGet(builder.rideableUnderWater, super::rideableUnderWater);
    }


    @Override
    public boolean shouldDropExperience() {
        if (builder.shouldDropExperience != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.shouldDropExperience.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityName() + ". Value: " + builder.shouldDropExperience.apply(this) + ". Must be a boolean. Defaulting to " + super.shouldDropExperience());
                return super.shouldDropExperience();
            }
        }
        return super.shouldDropExperience();
    }


    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {
        super.onEquipItem(slot, previous, current);
        if (builder.onEquipItem != null) {
            final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(slot, previous, current, this);
            builder.onEquipItem.accept(context);
        }
    }


    @Override
    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        if (builder.visibilityPercent != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.visibilityPercent.apply(p_20969_), "double");
            if (obj != null) {
                return (double) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityName() + ". Value: " + builder.visibilityPercent.apply(p_20969_) + ". Must be a double. Defaulting to " + super.getVisibilityPercent(p_20969_));
                return super.getVisibilityPercent(p_20969_);
            }
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }


    @Override
    public boolean canAttack(@NotNull LivingEntity entity) {
        if (builder.canAttack != null) {
            final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(this, entity);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canAttack.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj && super.canAttack(entity);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityName() + ". Value: " + builder.canAttack.apply(context) + ". Must be a boolean. Defaulting to " + super.canAttack(entity));
                return super.canAttack(entity);
            }
        } else {
            return super.canAttack(entity);
        }
    }


    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canBeAffected.apply(context), "boolean");
        if (obj != null) {
            return (boolean) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityName() + ". Value: " + builder.canBeAffected.apply(context) + ". Must be a boolean. Defaulting to " + super.canBeAffected(effectInstance));
            return super.canBeAffected(effectInstance);
        }
    }


    @Override
    public boolean isInvertedHealAndHarm() {
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.invertedHealAndHarm.apply(this), "boolean");
        if (obj != null) {
            return (boolean) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityName() + ". Value: " + builder.invertedHealAndHarm.apply(this) + ". Must be a boolean. Defaulting to " + super.isInvertedHealAndHarm());
            return super.isInvertedHealAndHarm();
        }
    }


    @Override
    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            builder.onEffectAdded.accept(context);
        } else {
            super.onEffectAdded(effectInstance, entity);
        }
    }


    @Override
    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {

        if (builder.onEffectRemoved != null) {
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(effectInstance, this);
            builder.onEffectRemoved.accept(context);
        } else {
            super.onEffectRemoved(effectInstance);
        }
    }


    @Override
    public void heal(float amount) {
        super.heal(amount);
        if (builder.onLivingHeal != null) {
            final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(this, amount);
            builder.onLivingHeal.accept(context);
        }
    }


    @Override
    public void die(@NotNull DamageSource damageSource) {
        super.die(damageSource);
        if (builder.onDeath != null) {
            final ContextUtils.DeathContext context = new ContextUtils.DeathContext(this, damageSource);
            builder.onDeath.accept(context);
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (builder.setDeathSound == null) return super.getDeathSound();
        return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setDeathSound));
    }


    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(damageSource, lootingMultiplier, allowDrops, this);
            builder.dropCustomDeathLoot.accept(context);
        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }
    }


    @Override
    public @NotNull Fallsounds getFallSounds() {
        if (builder.fallSounds != null)
            return new Fallsounds(
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.smallFallSound)),
                    Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.largeFallSound))
            );
        return super.getFallSounds();
    }

    @Override
    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        if (builder.eatingSound != null)
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.eatingSound));
        return super.getEatingSound(itemStack);
    }

    @Override
    public boolean onClimbable() {
        if (builder.onClimbable == null) return super.onClimbable();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.onClimbable.apply(this), "boolean");
        if (obj != null) {
            return (boolean) obj;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityName() + ". Value: " + builder.onClimbable.apply(this) + ". Must be a boolean. Defaulting to super.onClimbable(): " + super.onClimbable());
            return super.onClimbable();
        }
    }


    //Deprecated but still works for 1.20.4 :shrug:
    @Override
    public boolean canBreatheUnderwater() {
        return Objects.requireNonNullElseGet(builder.canBreatheUnderwater, super::canBreatheUnderwater);
    }


    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder.onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            builder.onLivingFall.accept(context);
        }
        return super.causeFallDamage(distance, damageMultiplier, damageSource);
    }


    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            builder.onSprint.accept(this);
        }
        super.setSprinting(sprinting);
    }


    @Override
    public double getJumpBoostPower() {
        return Objects.requireNonNullElseGet(builder.jumpBoostPower, super::getJumpBoostPower);
    }


    @Override
    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(this, fluidState);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canStandOnFluid.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityName() + ". Value: " + builder.canStandOnFluid.apply(context) + ". Must be a boolean. Defaulting to " + super.canStandOnFluid(fluidState));
        }
        return super.canStandOnFluid(fluidState);
    }


    @Override
    public boolean isSensitiveToWater() {
        if (builder.isSensitiveToWater != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isSensitiveToWater.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityName() + ". Value: " + builder.isSensitiveToWater.apply(this) + ". Must be a boolean. Defaulting to " + super.isSensitiveToWater());
        }
        return super.isSensitiveToWater();
    }


    @Override
    public void stopRiding() {
        super.stopRiding();
        if (builder.onStopRiding != null) {
            builder.onStopRiding.accept(this);
        }
    }


    @Override
    public void rideTick() {
        super.rideTick();
        if (builder.rideTick != null) {
            builder.rideTick.accept(this);
        }
    }


    @Override
    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        super.onItemPickup(p_21054_);
        if (builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(this, p_21054_);
            builder.onItemPickup.accept(context);
        }
    }


    @Override
    public boolean hasLineOfSight(@NotNull Entity entity) {
        if (builder.hasLineOfSight != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.hasLineOfSight.apply(entity), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityName() + ". Value: " + builder.hasLineOfSight.apply(entity) + ". Must be a boolean. Defaulting to " + super.hasLineOfSight(entity));
        }
        return super.hasLineOfSight(entity);
    }


    @Override
    public void onEnterCombat() {
        if (builder.onEnterCombat != null) {
            builder.onEnterCombat.accept(this);
        } else {
            super.onEnterCombat();
        }
    }


    @Override
    public void onLeaveCombat() {
        if (builder.onLeaveCombat != null) {
            builder.onLeaveCombat.accept(this);
        }
        super.onLeaveCombat();
    }

    @Override
    public boolean isAffectedByPotions() {
        if (builder.isAffectedByPotions != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isAffectedByPotions.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityName() + ". Value: " + builder.isAffectedByPotions.apply(this) + ". Must be a boolean. Defaulting to " + super.isAffectedByPotions());
        }
        return super.isAffectedByPotions();
    }

    @Override
    public boolean attackable() {
        if (builder.isAttackable != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isAttackable.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityName() + ". Value: " + builder.isAttackable.apply(this) + ". Must be a boolean. Defaulting to " + super.attackable());
        }
        return super.attackable();
    }


    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, this.level);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canTakeItem.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityName() + ". Value: " + builder.canTakeItem.apply(context) + ". Must be a boolean. Defaulting to " + super.canTakeItem(itemStack));
        }
        return super.canTakeItem(itemStack);
    }


    @Override
    public boolean isSleeping() {
        if (builder.isSleeping != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isSleeping.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityName() + ". Value: " + builder.isSleeping.apply(this) + ". Must be a boolean. Defaulting to " + super.isSleeping());
        }
        return super.isSleeping();
    }


    @Override
    public void startSleeping(@NotNull BlockPos blockPos) {

        if (builder.onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(this, blockPos);
            builder.onStartSleeping.accept(context);
        }
        super.startSleeping(blockPos);
    }


    @Override
    public void stopSleeping() {
        if (builder.onStopSleeping != null) {
            builder.onStopSleeping.accept(this);
        }
        super.stopSleeping();
    }


    @Override
    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, level);
            builder.eat.accept(context);
            return itemStack;
        }
        return super.eat(level, itemStack);
    }


    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.shouldRiderFaceForward.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityName() + ". Value: " + builder.shouldRiderFaceForward.apply(context) + ". Must be a boolean. Defaulting to " + super.shouldRiderFaceForward(player));
        }
        return super.shouldRiderFaceForward(player);
    }


    @Override
    public boolean canFreeze() {
        if (builder.canFreeze != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canFreeze.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj && !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityName() + ". Value: " + builder.canFreeze.apply(this) + ". Must be a boolean. Defaulting to " + super.canFreeze());
        }
        return super.canFreeze();
    }


    @Override
    public boolean isCurrentlyGlowing() {
        if (builder.isCurrentlyGlowing != null && !this.level.isClientSide()) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isCurrentlyGlowing.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityName() + ". Value: " + builder.isCurrentlyGlowing.apply(this) + ". Must be a boolean. Defaulting to " + super.isCurrentlyGlowing());
        }
        return super.isCurrentlyGlowing();
    }


    @Override
    public boolean canDisableShield() {
        if (builder.canDisableShield != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canDisableShield.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityName() + ". Value: " + builder.canDisableShield.apply(this) + ". Must be a boolean. Defaulting to " + super.canDisableShield());
        }
        return super.canDisableShield();
    }


    @Override
    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            builder.onClientRemoval.accept(this);
        }
        super.onClientRemoval();
    }

    @Override
    protected void actuallyHurt(DamageSource pDamageSource, float pDamageAmount) {
        if (builder.onHurt != null) {
            final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, this);
            builder.onHurt.accept(context);
        }
        super.actuallyHurt(pDamageSource, pDamageAmount);
    }

    @Override
    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            builder.lavaHurt.accept(this);
        }
        super.lavaHurt();
    }


    @Override
    protected void onFlap() {
        if (builder.onFlap != null) {
            builder.onFlap.accept(this);
        }
        super.onFlap();
    }

    @Override
    public int getExperienceReward() {
        if (builder.experienceReward != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.experienceReward.apply(this), "integer");
            if (obj != null) {
                return (int) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityName() + ". Value: " + builder.experienceReward.apply(this) + ". Must be an integer. Defaulting to " + super.getExperienceReward());
        }
        return super.getExperienceReward();
    }


    @Override
    public boolean dampensVibrations() {
        if (builder.dampensVibrations != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.dampensVibrations.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityName() + ". Value: " + builder.dampensVibrations.apply(this) + ". Must be a boolean. Defaulting to " + super.dampensVibrations());
        }
        return super.dampensVibrations();
    }


    @Override
    public void playerTouch(Player p_20081_) {
        if (builder.playerTouch != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(p_20081_, this);
            builder.playerTouch.accept(context);
        }
    }


    @Override
    public boolean showVehicleHealth() {
        if (builder.showVehicleHealth != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.showVehicleHealth.apply(this), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityName() + ". Value: " + builder.showVehicleHealth.apply(this) + ". Must be a boolean. Defaulting to " + super.showVehicleHealth());
        }
        return super.showVehicleHealth();
    }


    @Override
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder.thunderHit != null) {
            super.thunderHit(p_19927_, p_19928_);
            final ContextUtils.ThunderHitContext context = new ContextUtils.ThunderHitContext(p_19927_, p_19928_, this);
            builder.thunderHit.accept(context);
        }
    }


    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder.isInvulnerableTo != null) {
            final ContextUtils.DamageContext context = new ContextUtils.DamageContext(this, p_20122_);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isInvulnerableTo.apply(context), "boolean");
            if (obj != null) {
                return (boolean) obj;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityName() + ". Value: " + builder.isInvulnerableTo.apply(context) + ". Must be a boolean. Defaulting to " + super.isInvulnerableTo(p_20122_));
        }
        return super.isInvulnerableTo(p_20122_);
    }

    @Override
    public boolean canChangeDimensions() {
        if (builder.canChangeDimensions != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canChangeDimensions.apply(this), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityName() + ". Value: " + builder.canChangeDimensions.apply(this) + ". Must be a boolean. Defaulting to " + super.canChangeDimensions());
        }

        return super.canChangeDimensions();
    }


    @Override
    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder.mayInteract != null) {
            final ContextUtils.MayInteractContext context = new ContextUtils.MayInteractContext(p_146843_, p_146844_, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.mayInteract.apply(context), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityName() + ". Value: " + builder.mayInteract.apply(context) + ". Must be a boolean. Defaulting to " + super.mayInteract(p_146843_, p_146844_));
        }

        return super.mayInteract(p_146843_, p_146844_);
    }


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            final ContextUtils.CanTrampleContext context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, this);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canTrample.apply(context), "boolean");
            if (obj != null)
                return (boolean) obj;

            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + builder.canTrample.apply(context) + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }

        return super.canTrample(state, pos, fallDistance);
    }


    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (builder.onRemovedFromWorld != null) {
            builder.onRemovedFromWorld.accept(this);
        }
    }


    @Override
    public int getMaxFallDistance() {
        if (builder.setMaxFallDistance == null) return super.getMaxFallDistance();
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(this), "integer");
        if (obj != null)
            return (int) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityName() + ". Value: " + builder.setMaxFallDistance.apply(this) + ". Must be an integer. Defaulting to " + super.getMaxFallDistance());
        return super.getMaxFallDistance();
    }


    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        if (builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            builder.lerpTo.accept(context);
        } else super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
    }


    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> onEquipItem(slot, handItems.set(slot.getIndex(), stack), stack);
            case ARMOR -> onEquipItem(slot, armorItems.set(slot.getIndex(), stack), stack);
        }
    }
}
