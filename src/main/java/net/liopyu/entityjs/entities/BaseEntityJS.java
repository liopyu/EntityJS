package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

/**
 * The 'basic' implementation of a custom entity, implements most methods through the builder with some
 * conditionally delegating to the {@code super} implementation if the function is null. Other implementations
 * are <strong>not</strong> required to override every method in a class.<br><br>
 *
 * Further, the only real requirements for a custom entity class is that the class signature respects the contract
 * <pre>{@code public class YourEntityClass extends <? extends LivingEntity> implements <? extends IAnimatableJS>}</pre>
 * A basic implementation for a custom {@link net.minecraft.world.entity.animal.Animal Animal} entity could be as simple as
 * <pre>{@code public class AnimalEntityJS extends Animal implements IAnimatableJS {
 *
 *     private final AnimalBuilder builder;
 *     private final AnimationFactory animationFactory;
 *
 *     public AnimalEntityJS(AnimalBuilder builder, EntityType<? extends Animal> type, Level level) {
 *         super(type, level);
 *         this.builder = builder;
 *         animationFactory = GeckoLibUtil.createFactory(this);
 *     }
 *
 *     @Override
 *     public BaseEntityBuilder<?> getBuilder() {
 *         return builder;
 *     }
 *
 *     @Override
 *     public AnimationFactory getFactory() {
 *         return animationFactory;
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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BaseEntityJS extends LivingEntity implements IAnimatableJS {

    private final AnimationFactory animationFactory;

    protected final BaseEntityJSBuilder builder;

    public BaseEntityJS(BaseEntityJSBuilder builder, EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(0, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot p_21127_) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot p_21036_, ItemStack p_21037_) {

    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public BaseEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }


    public boolean isPushable() {
        return getBuilder().canBePushed;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    public boolean canBeCollidedWith() {
        return getBuilder().canBeCollidedWith;
    }

    public boolean isAttackable() {
        return getBuilder().isAttackable;
    }

    //Start of the method adding madness - liopyu
    @Override
    protected boolean canAddPassenger(Entity entity) {
        return builder.passengerPredicate.test(entity);
    }

    @Override
    protected boolean shouldDropLoot() {
        return builder.shouldDropLoot;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return builder.passengerPredicate.test(entity);
    }

    @Override
    protected boolean isAffectedByFluids() {
        return builder.isAffectedByFluids;
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return builder.isAlwaysExperienceDropper;
    }

    @Override
    protected boolean isImmobile() {
        return builder.isImmobile;
    }


    @Override
    protected float getBlockJumpFactor() {
        return builder.getBlockJumpFactor;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return builder.blockSpeedFactor == null ? super.getBlockSpeedFactor() : builder.blockSpeedFactor.apply(this);
    }


    @Override
    protected float getJumpPower() {
        return builder.getJumpPower;
    }

    @Override
    protected float getSoundVolume() {
        return builder.getSoundVolume;
    }

    @Override
    protected float getWaterSlowDown() {
        return builder.getWaterSlowDown;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return builder.setDeathSound;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return builder.setSwimSound;
    }

    @Override
    protected boolean isFlapping() {
        return builder.isFlapping;
    }

    @Override
    public int calculateFallDamage(float fallDistance, float fallHeight) {
        return builder.fallDamageFunction == null ? super.calculateFallDamage(fallDistance, fallHeight) : builder.fallDamageFunction.apply(fallDistance, fallHeight);
    }

    @Override
    public void tick() {
        builder.preTick.accept(this);
        super.tick();
        builder.postTick.accept(this);
    }

    @Override
    protected SoundEvent getSwimHighSpeedSplashSound() {
        return builder.getSwimHighSpeedSplashSound;
    }

    @Override
    public EntityType<?> getType() {
        return builder.getType;
    }

    private BlockState[] shouldRemoveSoulSpeed;

    @Override
    protected boolean onSoulSpeedBlock() {
        if (shouldRemoveSoulSpeed != null && shouldRemoveSoulSpeed.length > 0) {
            BlockPos entityPos = this.blockPosition();
            BlockState entityBlockState = this.level.getBlockState(entityPos);

            for (BlockState blockState : shouldRemoveSoulSpeed) {
                if (entityBlockState.equals(blockState)) {
                    return false;  // Do not apply soul speed effect
                }
            }
        }

        // Default behavior if no custom logic is specified
        return builder.onSoulSpeedBlock;
    }

    @Override
    protected void dropExperience() {
        if (builder.dropExperienceHandler != null) {
            builder.dropExperienceHandler.accept(this);
        }
    }

    @Override
    protected boolean shouldRemoveSoulSpeed(BlockState p_21140_) {
        if (shouldRemoveSoulSpeed != null && shouldRemoveSoulSpeed.length > 0) {
            BlockPos entityPos = this.blockPosition();
            BlockState entityBlockState = this.level.getBlockState(entityPos);

            for (BlockState blockState : shouldRemoveSoulSpeed) {
                if (entityBlockState.equals(blockState)) {
                    return false;  // Do not apply soul speed effect
                }
            }
        }

        // Default behavior if no custom logic is specified
        return this.shouldRemoveSoulSpeed(p_21140_);
    }

    @Override
    protected AABB makeBoundingBox() {
        if (builder.customBoundingBox != null) {
            return builder.customBoundingBox;
        }
        return this.makeBoundingBox();
    }

    @Override
    protected LootContext.Builder createLootContext(boolean p_21105_, DamageSource p_21106_) {
        LootContext.Builder originalBuilder = super.createLootContext(p_21105_, p_21106_);

        if (this.builder.customLootContextBuilder != null) {
            return this.builder.customLootContextBuilder.apply(originalBuilder);
        }

        return originalBuilder;
    }

    @Override
    protected void doAutoAttackOnTouch(LivingEntity target) {
        if (builder.customDoAutoAttack != null) {
            builder.customDoAutoAttack.accept(target);
        }
        builder.applyCustomDoAutoAttackOnTouch(target);
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
        if (builder.customGetStandingEyeHeight != null) {
            return builder.customGetStandingEyeHeight.apply(poseIn);
        }
        return super.getStandingEyeHeight(poseIn, sizeIn);
    }

    @Override
    protected BlockPos getBlockPosBelowThatAffectsMyMovement() {
        if (builder.customGetBlockPosBelow != null) {
            return builder.customGetBlockPosBelow.apply(this.blockPosition());
        }
        return super.getBlockPosBelowThatAffectsMyMovement();
    }

    @Override
    protected int decreaseAirSupply(int p_21303_) {
        if (builder.customDecreaseAirSupply != null) {
            return builder.customDecreaseAirSupply.apply(p_21303_);
        }
        return super.decreaseAirSupply(p_21303_);
    }

    @Override
    protected void blockedByShield(LivingEntity p_21246_) {
        if (builder.customBlockedByShield != null) {
            builder.customBlockedByShield.accept(p_21246_);
        } else {
            super.blockedByShield(p_21246_);
        }
    }

    @Override
    protected float tickHeadTurn(float p_21260_, float p_21261_) {
        if (builder.customTickHeadTurn != null) {
            return builder.customTickHeadTurn.apply(p_21260_, p_21261_);
        } else {
            return super.tickHeadTurn(p_21260_, p_21261_);
        }
    }

    @Override
    protected boolean doesEmitEquipEvent(EquipmentSlot p_217035_) {
        if (builder.customDoesEmitEquipEvent != null) {
            return builder.customDoesEmitEquipEvent.test(p_217035_);
        } else {
            return super.doesEmitEquipEvent(p_217035_);
        }
    }

    @Override
    protected AABB getBoundingBoxForPose(Pose p_20218_) {
        if (builder.customGetBoundingBoxForPose != null) {
            return builder.customGetBoundingBoxForPose.apply(p_20218_);
        } else {
            return super.getBoundingBoxForPose(p_20218_);
        }
    }

    @Override
    protected boolean canEnterPose(Pose p_20176_) {
        if (builder.customCanEnterPose != null) {
            return builder.customCanEnterPose.test(p_20176_);
        } else {
            return super.canEnterPose(p_20176_);
        }
    }

    @Override
    protected boolean getSharedFlag(int p_20292_) {
        if (builder.customGetSharedFlag != null) {
            return builder.customGetSharedFlag.apply(p_20292_);
        } else {
            return super.getSharedFlag(p_20292_);
        }
    }

    @Override
    protected boolean isHorizontalCollisionMinor(Vec3 p_196625_) {
        if (builder.customIsHorizontalCollisionMinor != null) {
            return builder.customIsHorizontalCollisionMinor.test(p_196625_);
        } else {
            return super.isHorizontalCollisionMinor(p_196625_);
        }
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        if (builder.customRepositionEntityAfterLoad != null) {
            return builder.customRepositionEntityAfterLoad.getAsBoolean();
        } else {
            return super.repositionEntityAfterLoad();
        }
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        if (builder.customUpdateInWaterStateAndDoFluidPushing != null) {
            return builder.customUpdateInWaterStateAndDoFluidPushing.getAsBoolean();
        } else {
            return super.updateInWaterStateAndDoFluidPushing();
        }
    }

    @Override
    protected Component getTypeName() {
        if (builder.customGetTypeName != null) {
            return builder.customGetTypeName.apply(this);
        } else {
            return super.getTypeName();
        }
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource p_21162_, float p_21163_) {
        if (builder.customGetDamageAfterArmorAbsorb != null) {
            return builder.customGetDamageAfterArmorAbsorb.apply(p_21162_, p_21163_);
        } else {
            return super.getDamageAfterArmorAbsorb(p_21162_, p_21163_);
        }
    }

    @Override
    protected float getDamageAfterMagicAbsorb(DamageSource p_21193_, float p_21194_) {
        if (builder.customGetDamageAfterMagicAbsorb != null) {
            return builder.customGetDamageAfterMagicAbsorb.apply(p_21193_, p_21194_);
        } else {
            return super.getDamageAfterMagicAbsorb(p_21193_, p_21194_);
        }
    }

    @Override
    protected float nextStep() {
        if (builder.customNextStep != null) {
            return builder.customNextStep.apply(super.nextStep());
        } else {
            return super.nextStep();
        }
    }

    @Override
    protected HoverEvent createHoverEvent() {
        HoverEvent originalHoverEvent = super.createHoverEvent();
        if (builder.customCreateHoverEvent != null) {
            return builder.customCreateHoverEvent.apply(originalHoverEvent);
        } else {
            return originalHoverEvent;
        }
    }

    @Override
    protected int getFireImmuneTicks() {
        int originalFireImmuneTicks = super.getFireImmuneTicks();
        if (builder.customGetFireImmuneTicks != null) {
            return builder.customGetFireImmuneTicks.apply(originalFireImmuneTicks);
        } else {
            return originalFireImmuneTicks;
        }
    }

    @Override
    protected int getPermissionLevel() {
        int originalPermissionLevel = super.getPermissionLevel();
        if (builder.customGetPermissionLevel != null) {
            return builder.customGetPermissionLevel.apply(originalPermissionLevel);
        } else {
            return originalPermissionLevel;
        }
    }

    @Override
    protected int increaseAirSupply(int p_21307_) {
        int originalAirSupply = super.increaseAirSupply(p_21307_);
        if (builder.customIncreaseAirSupply != null) {
            return builder.customIncreaseAirSupply.apply(originalAirSupply);
        } else {
            return originalAirSupply;
        }
    }

    @Override
    protected ListTag newDoubleList(double... p_20064_) {
        if (builder.customNewDoubleList != null) {
            return builder.customNewDoubleList.apply(p_20064_);
        } else {
            return super.newDoubleList(p_20064_);
        }
    }

    @Override
    protected ListTag newFloatList(float... p_20066_) {
        if (builder.customNewFloatList != null) {
            return builder.customNewFloatList.apply(p_20066_);
        } else {
            return super.newFloatList(p_20066_);
        }
    }

    @Override
    protected MovementEmission getMovementEmission() {
        if (builder.customGetMovementEmission != null) {
            return builder.customGetMovementEmission.apply(super.getMovementEmission());
        } else {
            return super.getMovementEmission();
        }
    }

    @Override
    protected Optional<BlockUtil.FoundRectangle> getExitPortal(ServerLevel p_185935_, BlockPos p_185936_, boolean p_185937_, WorldBorder p_185938_) {
        ExitPortalInfo exitPortalInfo = new ExitPortalInfo(p_185935_, p_185936_, p_185937_, p_185938_);
        if (builder.customGetExitPortal != null) {
            return builder.customGetExitPortal.apply(exitPortalInfo);
        } else {
            return super.getExitPortal(p_185935_, p_185936_, p_185937_, p_185938_);
        }
    }

    @Override
    protected SoundEvent getDrinkingSound(ItemStack p_21174_) {
        if (builder.customGetDrinkingSound != null) {
            return builder.customGetDrinkingSound.apply(p_21174_);
        } else {
            return super.getDrinkingSound(p_21174_);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource p_21239_) {
        if (builder.customGetHurtSound != null) {
            return builder.customGetHurtSound.apply(p_21239_);
        } else {
            return super.getHurtSound(p_21239_);
        }
    }

    @Nullable
    @Override
    protected PortalInfo findDimensionEntryPoint(ServerLevel p_19923_) {
        if (builder.customFindDimensionEntryPoint != null) {
            return builder.customFindDimensionEntryPoint.apply(p_19923_);
        } else {
            return super.findDimensionEntryPoint(p_19923_);
        }
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        if (builder.customGetSwimSplashSound != null) {
            return builder.customGetSwimSplashSound.apply(super.getSwimSplashSound());
        } else {
            return super.getSwimSplashSound();
        }
    }

    @Override
    protected Vec3 getRelativePortalPosition(Direction.Axis p_21085_, BlockUtil.FoundRectangle p_21086_) {
        if (builder.customGetRelativePortalPosition != null) {
            return builder.customGetRelativePortalPosition.apply(p_21085_, p_21086_);
        } else {
            return super.getRelativePortalPosition(p_21085_, p_21086_);
        }
    }

    @Override
    protected Vec3 limitPistonMovement(Vec3 p_20134_) {
        if (builder.customLimitPistonMovement != null) {
            return builder.customLimitPistonMovement.apply(p_20134_);
        } else {
            return super.limitPistonMovement(p_20134_);
        }
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 p_20019_, MoverType p_20020_) {
        if (builder.customMaybeBackOffFromEdge != null) {
            return builder.customMaybeBackOffFromEdge.apply(p_20019_, p_20020_);
        } else {
            return super.maybeBackOffFromEdge(p_20019_, p_20020_);
        }
    }

    @Override
    protected void actuallyHurt(DamageSource p_21240_, float p_21241_) {
        if (builder.customActuallyHurt != null) {
            builder.customActuallyHurt.accept(p_21240_, p_21241_);
        } else {
            super.actuallyHurt(p_21240_, p_21241_);
        }
    }

    @Override
    protected void blockUsingShield(LivingEntity p_21200_) {
        if (builder.customBlockUsingShield != null) {
            builder.customBlockUsingShield.accept(p_21200_);
        } else {
            super.blockUsingShield(p_21200_);
        }
    }


    @Override
    protected void checkAutoSpinAttack(AABB p_21072_, AABB p_21073_) {
        if (builder.customCheckAutoSpinAttack != null) {
            builder.customCheckAutoSpinAttack.accept(p_21072_, p_21073_);
        } else {
            super.checkAutoSpinAttack(p_21072_, p_21073_);
        }
    }

    @FunctionalInterface
    interface CustomFallDamageCheck {
        boolean apply(double distance, boolean onGround, BlockState blockState, BlockPos blockPos);
    }


    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, BlockState p_20992_, BlockPos p_20993_) {
        if (builder.customCheckFallDamage != null) {
            builder.customCheckFallDamage.accept(p_20990_, ForgeRegistries.BLOCKS.getKey(p_20992_.getBlock()));
        }
        super.checkFallDamage(p_20990_, p_20991_, p_20992_, p_20993_);
    }

    @Override
    public void kill() {
        if (builder.customKill != null) {
            builder.customKill.accept(this);
        }
        super.kill();
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return (builder.customCanAttackType != null && builder.customCanAttackType.apply(entityType)) || super.canAttackType(entityType);
    }

    @Override
    public float getSwimAmount(float p_20999_) {
        if (builder.customGetSwimAmount != null) {
            return builder.customGetSwimAmount.apply(p_20999_);
        }
        return super.getSwimAmount(p_20999_);
    }

    @Override
    public void baseTick() {
        if (builder.customBaseTick != null) {
            builder.customBaseTick.run();
        }
        super.baseTick();
    }

    @Override
    public boolean canSpawnSoulSpeedParticle() {
        if (builder.canSpawnSoulSpeedParticle != null) {
            return builder.canSpawnSoulSpeedParticle;
        }
        return super.canSpawnSoulSpeedParticle();
    }

    @Override
    protected void spawnSoulSpeedParticle() {
        if (builder.customSpawnSoulSpeedParticle != null) {
            builder.customSpawnSoulSpeedParticle.run();
        } else {
            super.spawnSoulSpeedParticle();
        }
    }

    @Override
    protected void removeSoulSpeed() {
        if (builder.customRemoveSoulSpeed != null) {
            builder.customRemoveSoulSpeed.run();
        } else {
            super.removeSoulSpeed();
        }
    }

    @Override
    protected void tryAddSoulSpeed() {
        if (builder.customTryAddSoulSpeed != null) {
            builder.customTryAddSoulSpeed.run();
        } else {
            super.tryAddSoulSpeed();
        }
    }

    @Override
    protected void removeFrost() {
        if (builder.customRemoveFrost != null) {
            builder.customRemoveFrost.run();
        } else {
            super.removeFrost();
        }
    }

    @Override
    protected void tryAddFrost() {
        if (builder.customTryAddFrost != null) {
            builder.customTryAddFrost.run();
        } else {
            super.tryAddFrost();
        }
    }

    @Override
    protected void onChangedBlock(BlockPos p_21175_) {
        if (builder.customOnChangedBlock != null) {
            builder.customOnChangedBlock.accept(p_21175_);
        } else {
            super.onChangedBlock(p_21175_);
        }
    }

    @Override
    public boolean isBaby() {
        if (builder.isBaby != null) {
            return builder.isBaby;
        } else {
            return super.isBaby();
        }
    }

    @Override
    public float getScale() {
        if (builder.customScale != null) {
            return builder.customScale;
        } else {
            return super.getScale();
        }
    }

    @Override
    public boolean rideableUnderWater() {
        if (builder.customRideableUnderWater != null) {
            return builder.customRideableUnderWater;
        } else {
            return super.rideableUnderWater();
        }
    }

    @Override
    protected void tickDeath() {
        if (builder.customTickDeath != null) {
            builder.customTickDeath.accept(this);
        } else {
            super.tickDeath();
        }
    }

    @Override
    public boolean shouldDropExperience() {
        if (builder.customShouldDropExperience != null) {
            return builder.customShouldDropExperience.test(this);
        } else {
            return super.shouldDropExperience();
        }
    }

    @Override
    public int getExperienceReward() {
        if (builder.customExperienceReward != null) {
            return builder.customExperienceReward.apply(this);
        } else {
            return super.getExperienceReward();
        }
    }

    @Override
    public RandomSource getRandom() {
        if (builder.customRandom != null) {
            return builder.customRandom.apply(this);
        } else {
            return super.getRandom();
        }
    }

    @Nullable
    @Override
    public LivingEntity getLastHurtByMob() {
        if (builder.customLastHurtByMob != null) {
            return builder.customLastHurtByMob.apply(this);
        } else {
            return super.getLastHurtByMob();
        }
    }

    @Override
    public int getLastHurtByMobTimestamp() {
        if (builder.customGetLastHurtMobTimestamp != null) {
            return builder.customGetLastHurtMobTimestamp.apply(this);
        } else {
            return super.getLastHurtByMobTimestamp();
        }
    }

    @Override
    public void setLastHurtByPlayer(@Nullable Player p_21248_) {
        super.setLastHurtByPlayer(p_21248_);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity p_21039_) {
        super.setLastHurtByMob(p_21039_);
    }

    @Nullable
    @Override
    public LivingEntity getLastHurtMob() {
        return super.getLastHurtMob();
    }

    @Override
    public int getLastHurtMobTimestamp() {
        return super.getLastHurtMobTimestamp();
    }

    @Override
    public void setLastHurtMob(Entity p_21336_) {
        super.setLastHurtMob(p_21336_);
    }

    @Override
    public int getNoActionTime() {
        return super.getNoActionTime();
    }

    @Override
    public void setNoActionTime(int p_21311_) {
        super.setNoActionTime(p_21311_);
    }

    @Override
    public boolean shouldDiscardFriction() {
        return super.shouldDiscardFriction();
    }

    @Override
    public void setDiscardFriction(boolean p_147245_) {
        super.setDiscardFriction(p_147245_);
    }

    @Override
    public void onEquipItem(EquipmentSlot p_238393_, ItemStack p_238394_, ItemStack p_238395_) {
        super.onEquipItem(p_238393_, p_238394_, p_238395_);
    }

    @Override
    protected void playEquipSound(ItemStack p_217042_) {
        super.playEquipSound(p_217042_);
    }

    @Override
    protected void tickEffects() {
        super.tickEffects();
    }

    @Override
    protected void updateInvisibilityStatus() {
        super.updateInvisibilityStatus();
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        return super.getVisibilityPercent(p_20969_);
    }

    @Override
    public boolean canAttack(LivingEntity p_21171_) {
        return super.canAttack(p_21171_);
    }

    @Override
    public boolean canAttack(LivingEntity p_21041_, TargetingConditions p_21042_) {
        return super.canAttack(p_21041_, p_21042_);
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return super.canBeSeenAsEnemy();
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return super.canBeSeenByAnyone();
    }

    @Override
    protected void removeEffectParticles() {
        super.removeEffectParticles();
    }

    @Override
    public boolean removeAllEffects() {
        return super.removeAllEffects();
    }

    @Override
    public Collection<MobEffectInstance> getActiveEffects() {
        return super.getActiveEffects();
    }

    @Override
    public Map<MobEffect, MobEffectInstance> getActiveEffectsMap() {
        return super.getActiveEffectsMap();
    }

    @Override
    public boolean hasEffect(MobEffect p_21024_) {
        return super.hasEffect(p_21024_);
    }

    @Nullable
    @Override
    public MobEffectInstance getEffect(MobEffect p_21125_) {
        return super.getEffect(p_21125_);
    }

    @Override
    public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
        return super.addEffect(p_147208_, p_147209_);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance p_21197_) {
        return super.canBeAffected(p_21197_);
    }

    @Override
    public void forceAddEffect(MobEffectInstance p_147216_, @Nullable Entity p_147217_) {
        super.forceAddEffect(p_147216_, p_147217_);
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return super.isInvertedHealAndHarm();
    }

    @Nullable
    @Override
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect p_21164_) {
        return super.removeEffectNoUpdate(p_21164_);
    }

    @Override
    public boolean removeEffect(MobEffect p_21196_) {
        return super.removeEffect(p_21196_);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance p_147190_, @Nullable Entity p_147191_) {
        super.onEffectAdded(p_147190_, p_147191_);
    }

    @Override
    protected void onEffectUpdated(MobEffectInstance p_147192_, boolean p_147193_, @Nullable Entity p_147194_) {
        super.onEffectUpdated(p_147192_, p_147193_, p_147194_);
    }

    @Override
    protected void onEffectRemoved(MobEffectInstance p_21126_) {
        super.onEffectRemoved(p_21126_);
    }

    @Override
    public void heal(float p_21116_) {
        super.heal(p_21116_);
    }

    @Override
    public float getHealth() {
        return super.getHealth();
    }

    @Override
    public void setHealth(float p_21154_) {
        super.setHealth(p_21154_);
    }

    @Override
    public boolean isDeadOrDying() {
        return super.isDeadOrDying();
    }

    @Override
    public boolean hurt(DamageSource p_21016_, float p_21017_) {
        return super.hurt(p_21016_, p_21017_);
    }

    @Nullable
    @Override
    public DamageSource getLastDamageSource() {
        return super.getLastDamageSource();
    }

    @Override
    protected void playHurtSound(DamageSource p_21160_) {
        super.playHurtSound(p_21160_);
    }

    @Override
    public boolean isDamageSourceBlocked(DamageSource p_21276_) {
        return super.isDamageSourceBlocked(p_21276_);
    }

    @Override
    public void die(DamageSource p_21014_) {
        super.die(p_21014_);
    }

    @Override
    protected void createWitherRose(@Nullable LivingEntity p_21269_) {
        super.createWitherRose(p_21269_);
    }

    @Override
    protected void dropAllDeathLoot(DamageSource p_21192_) {
        super.dropAllDeathLoot(p_21192_);
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource p_21018_, int p_21019_, boolean p_21020_) {
        super.dropCustomDeathLoot(p_21018_, p_21019_, p_21020_);
    }

    @Override
    public ResourceLocation getLootTable() {
        return super.getLootTable();
    }

    @Override
    protected void dropFromLootTable(DamageSource p_21021_, boolean p_21022_) {
        super.dropFromLootTable(p_21021_, p_21022_);
    }

    @Override
    public void knockback(double p_147241_, double p_147242_, double p_147243_) {
        super.knockback(p_147241_, p_147242_, p_147243_);
    }

    @Override
    public void skipDropExperience() {
        super.skipDropExperience();
    }

    @Override
    public boolean wasExperienceConsumed() {
        return super.wasExperienceConsumed();
    }

    @Override
    public Fallsounds getFallSounds() {
        return super.getFallSounds();
    }

    @Override
    public SoundEvent getEatingSound(ItemStack p_21202_) {
        return super.getEatingSound(p_21202_);
    }

    @Override
    public void setOnGround(boolean p_21182_) {
        super.setOnGround(p_21182_);
    }

    @Override
    public Optional<BlockPos> getLastClimbablePos() {
        return super.getLastClimbablePos();
    }

    @Override
    public boolean onClimbable() {
        return super.onClimbable();
    }

    @Override
    public boolean isAlive() {
        return super.isAlive();
    }
}
