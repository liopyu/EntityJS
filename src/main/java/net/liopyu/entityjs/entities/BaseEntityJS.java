package net.liopyu.entityjs.entities;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
public class BaseEntityJS extends LivingEntity implements IAnimatableJS {

    private final AnimationFactory animationFactory;

    protected final BaseEntityJSBuilder builder;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public BaseEntityJS(BaseEntityJSBuilder builder, EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return builder.brainProviderBuilder == null ? super.brainProvider() : builder.brainProviderBuilder.build();
    }

    @Override
    protected Brain<BaseEntityJS> makeBrain(Dynamic<?> p_21069_) {
        final Brain<BaseEntityJS> brain = UtilsJS.cast(super.makeBrain(p_21069_)); // This has become a crutch
        if (builder.brainBuilder != null) {
            final BrainBuilder brainBuilder = new BrainBuilder(builder.id);
            builder.brainBuilder.accept(brainBuilder);
            return brainBuilder.build(brain);
        }
        return brain;
    }

    // Synced entity data is basically impossible, it is class dependent and mostly static
    // @Override
    // protected void defineSynchedData() {
    //     super.defineSynchedData();
    // }
    //
    // Do we actually want to let users touch this, kube's persistent data works well enough
    // @Override
    // public void readAdditionalSaveData(CompoundTag pCompound) {
    //     super.readAdditionalSaveData(pCompound);
    // }
    //
    // @Override
    // public void addAdditionalSaveData(CompoundTag pCompound) {
    //     super.addAdditionalSaveData(pCompound);
    // }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    // Mirrors the implementation in Mob
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }

    // Mirrors the implementation in Mob
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> onEquipItem(slot, handItems.set(slot.getIndex(), stack), stack);
            case ARMOR -> onEquipItem(slot, armorItems.set(slot.getIndex(), stack), stack);
        }
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
        return builder.canBePushed;
    }

    @Override
    public HumanoidArm getMainArm() {
        return builder.mainArm;
    }

    public boolean canBeCollidedWith() {
        return builder.canBeCollidedWith;
    }

    public boolean isAttackable() {
        return builder.isAttackable;
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

    /*@Override
    protected AABB makeBoundingBox() {
        if (builder.customBoundingBox != null) {
            return builder.customBoundingBox;
        }
        return this.makeBoundingBox();
    }*/

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

    /*@Override
    public void baseTick() {
        if (builder.customBaseTick != null) {
            builder.customBaseTick.run();
        }
        super.baseTick();
    }*/

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

    /*@Override
    public boolean isBaby() {
        if (builder.isBaby != null) {
            return builder.isBaby;
        } else {
            return super.isBaby();
        }
    }*/

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

    /*@Override
    public RandomSource getRandom() {
        if (builder.customRandom != null) {
            return builder.customRandom.apply(this);
        } else {
            return super.getRandom();
        }
    }*/

    /*@Nullable
    @Override
    public LivingEntity getLastHurtByMob() {
        if (builder.customLastHurtByMob != null) {
            return builder.customLastHurtByMob.apply(this);
        } else {
            return super.getLastHurtByMob();
        }
    }*/

    /*@Override
    public int getLastHurtByMobTimestamp() {
        if (builder.customGetLastHurtMobTimestamp != null) {
            return builder.customGetLastHurtMobTimestamp.apply(this);
        } else {
            return super.getLastHurtByMobTimestamp();
        }
    }*/

   /* @Override
    public void setLastHurtByPlayer(@Nullable Player p_21248_) {
        if (builder.customSetLastHurtByPlayer != null) {
            builder.customSetLastHurtByPlayer.accept(p_21248_);
        } else {
            super.setLastHurtByPlayer(p_21248_);
        }
    }*/


    /*@Override
    public void setLastHurtByMob(@Nullable LivingEntity p_21039_) {
        super.setLastHurtByMob(p_21039_);
    }*/

    /*@Nullable
    @Override
    public LivingEntity getLastHurtMob() {
        return super.getLastHurtMob();
    }*/

    /*@Override
    public int getLastHurtMobTimestamp() {
        return super.getLastHurtMobTimestamp();
    }*/

   /* @Override
    public void setLastHurtMob(Entity p_21336_) {
        super.setLastHurtMob(p_21336_);
    }*/

    /*@Override
    public int getNoActionTime() {
        return super.getNoActionTime();
    }*/

    /*@Override
    public void setNoActionTime(int p_21311_) {
        super.setNoActionTime(p_21311_);
    }*/

    @Override
    public boolean shouldDiscardFriction() {
        if (builder.customShouldDiscardFriction != null) {
            return builder.customShouldDiscardFriction.get();
        } else {
            return super.shouldDiscardFriction();
        }
    }


    @Override
    public void setDiscardFriction(boolean p_147245_) {
        if (builder.customSetDiscardFriction != null) {
            builder.customSetDiscardFriction.accept(p_147245_);
        } else {
            super.setDiscardFriction(p_147245_);
        }
    }

    //EquipmentSlot, Old Itemstack, New Itemstack
    @Override
    public void onEquipItem(EquipmentSlot slot, ItemStack previous, ItemStack current) {
        if (builder.customOnEquipItem != null) {
            builder.customOnEquipItem.accept(slot, previous, current);
        } else {
            super.onEquipItem(slot, previous, current);
        }
    }

    @Override
    protected void playEquipSound(ItemStack itemStack) {
        if (builder.customPlayEquipSound != null) {
            builder.customPlayEquipSound.accept(itemStack);
        } else {
            super.playEquipSound(itemStack);
        }
    }


    @Override
    protected void tickEffects() {
        if (builder.customTickEffects != null) {
            builder.customTickEffects.run();
        } else {
            super.tickEffects();
        }
    }


    @Override
    protected void updateInvisibilityStatus() {
        if (builder.customUpdateInvisibilityStatus != null) {
            builder.customUpdateInvisibilityStatus.run();
        } else {
            super.updateInvisibilityStatus();
        }
    }


    @Override
    public double getVisibilityPercent(@Nullable Entity p_20969_) {
        if (builder.customGetVisibilityPercent != null) {
            return builder.customGetVisibilityPercent.apply(p_20969_);
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }


    @Override
    public boolean canAttack(LivingEntity p_21171_) {
        if (builder.customCanAttack != null) {
            return builder.customCanAttack.test(p_21171_);
        } else {
            return super.canAttack(p_21171_);
        }
    }


    @Override
    public boolean canAttack(LivingEntity p_21041_, TargetingConditions p_21042_) {
        if (builder.customCanAttackWithConditions != null) {
            return builder.customCanAttackWithConditions.test(p_21041_, p_21042_);
        } else {
            return super.canAttack(p_21041_, p_21042_);
        }
    }


    @Override
    public boolean canBeSeenAsEnemy() {
        if (builder.customCanBeSeenAsEnemy != null) {
            return builder.customCanBeSeenAsEnemy.getAsBoolean();
        } else {
            return super.canBeSeenAsEnemy();
        }
    }


    @Override
    public boolean canBeSeenByAnyone() {
        if (builder.customCanBeSeenByAnyone != null) {
            return builder.customCanBeSeenByAnyone.getAsBoolean();
        } else {
            return super.canBeSeenByAnyone();
        }
    }


    @Override
    protected void removeEffectParticles() {
        if (builder.customRemoveEffectParticles != null) {
            builder.customRemoveEffectParticles.run();
        } else {
            super.removeEffectParticles();
        }
    }


    @Override
    public boolean removeAllEffects() {
        if (builder.customRemoveAllEffects != null) {
            return builder.customRemoveAllEffects.test(super.removeAllEffects());
        } else {
            return super.removeAllEffects();
        }
    }


    /*@Override
    public Collection<MobEffectInstance> getActiveEffects() {
        if (builder.customGetActiveEffects != null) {
            return builder.customGetActiveEffects.apply(super.getActiveEffects());
        } else {
            return super.getActiveEffects();
        }
    }*/


    /*@Override
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
    }*/

    @Override
    public boolean addEffect(MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
        if (builder.customAddEffect != null) {
            return builder.customAddEffect.test(p_147208_, p_147209_);
        } else {
            return super.addEffect(p_147208_, p_147209_);
        }
    }


    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (builder.canBeAffectedPredicate != null) {
            return builder.canBeAffectedPredicate.test(effectInstance);
        }
        return super.canBeAffected(effectInstance);
    }


    @Override
    public void forceAddEffect(MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.forceAddEffectConsumer != null) {
            builder.forceAddEffectConsumer.accept(effectInstance, entity);
        } else {
            super.forceAddEffect(effectInstance, entity);
        }
    }


    @Override
    public boolean isInvertedHealAndHarm() {
        return builder.invertedHealAndHarm || super.isInvertedHealAndHarm();
    }


    @Nullable
    @Override
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect effect) {
        if (builder.removeEffectNoUpdateFunction != null) {
            return builder.removeEffectNoUpdateFunction.apply(effect);
        } else {
            return super.removeEffectNoUpdate(effect);
        }
    }


    @Override
    public boolean removeEffect(MobEffect effect) {
        if (builder.removeEffect != null) {
            return builder.removeEffect.test(effect, false);
        } else {
            return super.removeEffect(effect);
        }
    }


    @Override
    protected void onEffectAdded(MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            builder.onEffectAdded.accept(effectInstance, entity);
        } else {
            super.onEffectAdded(effectInstance, entity);
        }
    }


    @Override
    protected void onEffectUpdated(MobEffectInstance effectInstance, boolean isReapplied, @Nullable Entity entity) {
        if (builder.onEffectUpdated != null) {
            builder.onEffectUpdated.accept(effectInstance, isReapplied, entity);
        } else {
            super.onEffectUpdated(effectInstance, isReapplied, entity);
        }
    }


    @Override
    protected void onEffectRemoved(MobEffectInstance effectInstance) {
        if (builder.onEffectRemoved != null) {
            builder.onEffectRemoved.accept(effectInstance);
        } else {
            super.onEffectRemoved(effectInstance);
        }
    }


    @Override
    public void heal(float amount) {
        if (builder.healAmount != null) {
            builder.healAmount.accept(amount, this);
        } else {
            super.heal(amount);
        }
    }


    /*@Override
    public float getHealth() {
        return super.getHealth();
    }*/

    @Override
    public void setHealth(float health) {
        if (builder.setHealthAmount != null) {
            builder.setHealthAmount.accept(health, this);
        } else {
            super.setHealth(health);
        }
    }


    @Override
    public boolean isDeadOrDying() {
        if (builder.isDeadOrDying != null) {
            return builder.isDeadOrDying.test(this);
        } else {
            return super.isDeadOrDying();
        }
    }


    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (builder.hurtPredicate != null) {
            return builder.hurtPredicate.test(damageSource, amount);
        } else {
            return super.hurt(damageSource, amount);
        }
    }


    @Nullable
    @Override
    public DamageSource getLastDamageSource() {
        if (builder.lastDamageSourceSupplier != null) {
            return builder.lastDamageSourceSupplier.get();
        } else {
            return super.getLastDamageSource();
        }
    }

//Not needed since we have getHurtSound
   /* @Override
    protected void playHurtSound(DamageSource damageSource) {
        if (builder.playHurtSound != null) {
            builder.playHurtSound.accept(damageSource);
        } else {
            super.playHurtSound(damageSource);
        }
    }*/


    @Override
    public boolean isDamageSourceBlocked(DamageSource damageSource) {
        if (builder.isDamageSourceBlocked != null) {
            return builder.isDamageSourceBlocked.test(damageSource);
        } else {
            return super.isDamageSourceBlocked(damageSource);
        }
    }


    @Override
    public void die(DamageSource damageSource) {
        if (builder.die != null) {
            builder.die.accept(damageSource, this);
        } else {
            super.die(damageSource);
        }
    }


    @Override
    protected void createWitherRose(@Nullable LivingEntity entity) {
        if (builder.createWitherRose != null) {
            builder.createWitherRose.accept(entity, this);
        } else {
            super.createWitherRose(entity);
        }
    }


    @Override
    protected void dropAllDeathLoot(DamageSource damageSource) {
        if (builder.dropAllDeathLoot != null) {
            builder.dropAllDeathLoot.accept(damageSource);
        } else {
            super.dropAllDeathLoot(damageSource);
        }
    }


    @Override
    protected void dropEquipment() {
        if (builder.dropEquipment != null) {
            builder.dropEquipment.accept(null);
        } else {
            super.dropEquipment();
        }
    }


    @Override
    protected void dropCustomDeathLoot(DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            builder.dropCustomDeathLoot.accept(damageSource, lootingMultiplier, allowDrops);
        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }
    }


    @Override
    public ResourceLocation getLootTable() {
        return (builder.lootTable != null) ? builder.lootTable.get() : super.getLootTable();
    }


    @Override
    protected void dropFromLootTable(DamageSource source, boolean flag) {
        if (builder.dropFromLootTable != null) {
            builder.dropFromLootTable.accept(source, flag);
        } else {
            super.dropFromLootTable(source, flag);
        }
    }


    @Override
    public void knockback(double x, double y, double z) {
        if (builder.knockback != null) {
            builder.knockback.accept(x, y, z);
        } else {
            super.knockback(x, y, z);
        }
    }


    @Override
    public void skipDropExperience() {
        if (builder.skipDropExperience != null) {
            builder.skipDropExperience.run();
        } else {
            super.skipDropExperience();
        }
    }


    @Override
    public boolean wasExperienceConsumed() {
        if (builder.wasExperienceConsumed != null) {
            return builder.wasExperienceConsumed.get();
        } else {
            return super.wasExperienceConsumed();
        }
    }


    @Override
    public Fallsounds getFallSounds() {
        if (builder.fallSoundsFunction != null) {
            return builder.fallSoundsFunction.apply(super.getFallSounds());
        } else {
            return super.getFallSounds();
        }
    }


    @Override
    public SoundEvent getEatingSound(ItemStack itemStack) {
        if (builder.eatingSound != null) {
            return builder.eatingSound.apply(itemStack);
        } else {
            return super.getEatingSound(itemStack);
        }
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
