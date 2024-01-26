package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.liopyu.entityjs.util.*;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.liopyu.entityjs.util.MobInteractContext;
import net.minecraft.BlockUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Objects;
import java.util.Optional;

public class MobEntityJS extends Mob implements IAnimatableJS {

    private final MobEntityJSBuilder builder;
    private final AnimationFactory animationFactory;

    public MobEntityJS(MobEntityJSBuilder builder, EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    public BaseLivingEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
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

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level p_21480_) {
        return super.createNavigation(p_21480_);
    }

    //Beginning of Base Overrides
    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public @NotNull Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    // Mirrors the implementation in Mob
    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }

    // Mirrors the implementation in Mob
    @Override
    public void setItemSlot(EquipmentSlot slot, @NotNull ItemStack stack) {
        verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> onEquipItem(slot, handItems.set(slot.getIndex(), stack), stack);
            case ARMOR -> onEquipItem(slot, armorItems.set(slot.getIndex(), stack), stack);
        }
    }


    @Override
    public boolean isPushable() {
        return builder.isPushable;
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return builder.mainArm;
    }


    @Override
    public boolean isAttackable() {
        return builder.isAttackable;
    }

    //Start of the method adding madness - liopyu
    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        return builder.passengerPredicate.test(entity);
    }

    @Override
    protected boolean shouldDropLoot() {
        return builder.shouldDropLoot;
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
    protected boolean isFlapping() {
        return builder.isFlapping;
    }

    @Override
    public int calculateFallDamage(float fallDistance, float fallHeight) {
        return builder.fallDamageFunction == null ? super.calculateFallDamage(fallDistance, fallHeight) : builder.fallDamageFunction.apply(fallDistance, fallHeight);
    }

    @Override
    public void tick() {
        super.tick();
        if (builder.tick != null) {
            builder.tick.accept(this);
        }
    }

    @Override
    public void onAddedToWorld() {
        if (builder.onAddedToWorld != null) {
            builder.onAddedToWorld.accept(this);
            super.onAddedToWorld();
        } else {
            super.onAddedToWorld();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (builder.mobInteract != null) {
            final MobInteractContext context = new MobInteractContext(this, player, hand);
            final InteractionResult result = builder.mobInteract.apply(context);
            return result == null ? super.mobInteract(player, hand) : result;
        }

        return super.mobInteract(player, hand);
    }


    @Override
    protected LootContext.@NotNull Builder createLootContext(boolean p_21105_, @NotNull DamageSource p_21106_) {
        LootContext.Builder originalBuilder = super.createLootContext(p_21105_, p_21106_);

        if (builder.customLootContextBuilder != null) {
            return builder.customLootContextBuilder.apply(originalBuilder);
        }

        return originalBuilder;
    }

    @Override
    protected void doAutoAttackOnTouch(@NotNull LivingEntity target) {
        if (builder.customDoAutoAttack != null) {
            builder.customDoAutoAttack.accept(target);
        }
        builder.applyCustomDoAutoAttackOnTouch(target);
    }


    @Override
    protected int decreaseAirSupply(int p_21303_) {
        if (builder.customDecreaseAirSupply != null) {
            return builder.customDecreaseAirSupply.apply(p_21303_);
        }
        return super.decreaseAirSupply(p_21303_);
    }

    @Override
    protected void blockedByShield(@NotNull LivingEntity p_21246_) {
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
    protected boolean doesEmitEquipEvent(@NotNull EquipmentSlot p_217035_) {
        if (builder.customDoesEmitEquipEvent != null) {
            return builder.customDoesEmitEquipEvent.test(p_217035_);
        } else {
            return super.doesEmitEquipEvent(p_217035_);
        }
    }

    @Override
    protected boolean canEnterPose(@NotNull Pose p_20176_) {
        if (builder.customCanEnterPose != null) {
            return builder.customCanEnterPose.test(p_20176_);
        } else {
            return super.canEnterPose(p_20176_);
        }
    }


    @Override
    protected boolean isHorizontalCollisionMinor(@NotNull Vec3 p_196625_) {
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
    protected float nextStep() {
        if (builder.customNextStep != null) {
            return builder.customNextStep.apply(super.nextStep());
        } else {
            return super.nextStep();
        }
    }

    @Override
    protected @NotNull HoverEvent createHoverEvent() {
        HoverEvent originalHoverEvent = super.createHoverEvent();
        if (builder.customCreateHoverEvent != null) {
            return builder.customCreateHoverEvent.apply(originalHoverEvent);
        } else {
            return originalHoverEvent;
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
    protected @NotNull ListTag newDoubleList(double @NotNull ... p_20064_) {
        if (builder.customNewDoubleList != null) {
            return builder.customNewDoubleList.apply(p_20064_);
        } else {
            return super.newDoubleList(p_20064_);
        }
    }

    @Override
    protected @NotNull ListTag newFloatList(float @NotNull ... p_20066_) {
        if (builder.customNewFloatList != null) {
            return builder.customNewFloatList.apply(p_20066_);
        } else {
            return super.newFloatList(p_20066_);
        }
    }

    @Override
    protected @NotNull Optional<BlockUtil.FoundRectangle> getExitPortal(@NotNull ServerLevel p_185935_, @NotNull BlockPos p_185936_, boolean p_185937_, @NotNull WorldBorder p_185938_) {
        ExitPortalInfo exitPortalInfo = new ExitPortalInfo(p_185935_, p_185936_, p_185937_, p_185938_);
        if (builder.customGetExitPortal != null) {
            return builder.customGetExitPortal.apply(exitPortalInfo);
        } else {
            return super.getExitPortal(p_185935_, p_185936_, p_185937_, p_185938_);
        }
    }

    @Override
    protected @NotNull SoundEvent getDrinkingSound(@NotNull ItemStack p_21174_) {
        if (builder.customGetDrinkingSound != null) {
            return builder.customGetDrinkingSound.apply(p_21174_);
        } else {
            return super.getDrinkingSound(p_21174_);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (builder.customGetHurtSound != null) {
            return builder.customGetHurtSound.apply(p_21239_);
        } else {
            return super.getHurtSound(p_21239_);
        }
    }

    @Nullable
    @Override
    protected PortalInfo findDimensionEntryPoint(@NotNull ServerLevel p_19923_) {
        if (builder.customFindDimensionEntryPoint != null) {
            return builder.customFindDimensionEntryPoint.apply(p_19923_);
        } else {
            return super.findDimensionEntryPoint(p_19923_);
        }
    }

    @Override
    protected @NotNull SoundEvent getSwimSplashSound() {
        if (builder.customGetSwimSplashSound != null) {
            return builder.customGetSwimSplashSound.apply(super.getSwimSplashSound());
        } else {
            return super.getSwimSplashSound();
        }
    }

    @Override
    protected @NotNull Vec3 getRelativePortalPosition(Direction.@NotNull Axis p_21085_, BlockUtil.@NotNull FoundRectangle p_21086_) {
        if (builder.customGetRelativePortalPosition != null) {
            return builder.customGetRelativePortalPosition.apply(p_21085_, p_21086_);
        } else {
            return super.getRelativePortalPosition(p_21085_, p_21086_);
        }
    }

    @Override
    protected @NotNull Vec3 limitPistonMovement(@NotNull Vec3 p_20134_) {
        if (builder.customLimitPistonMovement != null) {
            return builder.customLimitPistonMovement.apply(p_20134_);
        } else {
            return super.limitPistonMovement(p_20134_);
        }
    }

    @Override
    protected @NotNull Vec3 maybeBackOffFromEdge(@NotNull Vec3 p_20019_, @NotNull MoverType p_20020_) {
        if (builder.customMaybeBackOffFromEdge != null) {
            return builder.customMaybeBackOffFromEdge.apply(p_20019_, p_20020_);
        } else {
            return super.maybeBackOffFromEdge(p_20019_, p_20020_);
        }
    }

    @Override
    protected void actuallyHurt(@NotNull DamageSource p_21240_, float p_21241_) {
        if (builder.customActuallyHurt != null) {
            builder.customActuallyHurt.accept(p_21240_, p_21241_);
        } else {
            super.actuallyHurt(p_21240_, p_21241_);
        }
    }

    @Override
    protected void blockUsingShield(@NotNull LivingEntity p_21200_) {
        if (builder.customBlockUsingShield != null) {
            builder.customBlockUsingShield.accept(p_21200_);
        } else {
            super.blockUsingShield(p_21200_);
        }
    }


    @Override
    protected void checkAutoSpinAttack(@NotNull AABB p_21072_, @NotNull AABB p_21073_) {
        if (builder.customCheckAutoSpinAttack != null) {
            builder.customCheckAutoSpinAttack.accept(p_21072_, p_21073_);
        } else {
            super.checkAutoSpinAttack(p_21072_, p_21073_);
        }
    }

    @Override
    protected void checkFallDamage(double p_20990_, boolean p_20991_, @NotNull BlockState p_20992_, @NotNull BlockPos p_20993_) {
        if (builder.customCheckFallDamage != null) {
            builder.customCheckFallDamage.accept(p_20990_, ForgeRegistries.BLOCKS.getKey(p_20992_.getBlock()));
        }
        super.checkFallDamage(p_20990_, p_20991_, p_20992_, p_20993_);
    }

    @Override
    public void kill() {
        if (builder.kill != null) {
            builder.kill.accept(this);
        }
        super.kill();
    }

    @Override
    public boolean canAttackType(@NotNull EntityType<?> entityType) {
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
    public boolean canSpawnSoulSpeedParticle() {
        return Objects.requireNonNullElseGet(builder.canSpawnSoulSpeedParticle, super::canSpawnSoulSpeedParticle);
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
    protected void onChangedBlock(@NotNull BlockPos p_21175_) {
        if (builder.customOnChangedBlock != null) {
            builder.customOnChangedBlock.accept(p_21175_);
        } else {
            super.onChangedBlock(p_21175_);
        }
    }


    @Override
    public float getScale() {
        return Objects.requireNonNullElseGet(builder.customScale, super::getScale);
    }

    @Override
    public boolean rideableUnderWater() {
        return Objects.requireNonNullElseGet(builder.rideableUnderWater, super::rideableUnderWater);
    }


    @Override
    protected void tickDeath() {
        if (builder.tickDeath != null) {
            builder.tickDeath.accept(this);
        } else {
            super.tickDeath();
        }
    }

    @Override
    public boolean shouldDropExperience() {
        return builder.shouldDropExperience;
    }

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
    public void onEquipItem(@NotNull EquipmentSlot slot, @NotNull ItemStack previous, @NotNull ItemStack current) {
        if (builder.customOnEquipItem != null) {
            builder.customOnEquipItem.accept(slot, previous, current);
        } else {
            super.onEquipItem(slot, previous, current);
        }
    }

    @Override
    protected void playEquipSound(@NotNull ItemStack itemStack) {
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
    public boolean canAttack(@NotNull LivingEntity p_21171_) {
        if (builder.customCanAttack != null) {
            return builder.customCanAttack.test(p_21171_);
        } else {
            return super.canAttack(p_21171_);
        }
    }


    @Override
    public boolean canAttack(@NotNull LivingEntity p_21041_, @NotNull TargetingConditions p_21042_) {
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

    @Override
    public boolean addEffect(@NotNull MobEffectInstance p_147208_, @Nullable Entity p_147209_) {
        if (builder.customAddEffect != null) {
            return builder.customAddEffect.test(p_147208_, p_147209_);
        } else {
            return super.addEffect(p_147208_, p_147209_);
        }
    }


    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        if (builder.canBeAffectedPredicate != null) {
            return builder.canBeAffectedPredicate.test(effectInstance);
        }
        return super.canBeAffected(effectInstance);
    }


    @Override
    public void forceAddEffect(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
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


   /* @Nullable
    @Override
    public MobEffectInstance removeEffectNoUpdate(@Nullable MobEffect effect) {
        if (builder.removeEffectNoUpdateFunction != null) {
            return builder.removeEffectNoUpdateFunction.apply(effect);
        } else {
            return super.removeEffectNoUpdate(effect);
        }
    }*/


   /* @Override
    public boolean removeEffect(@NotNull MobEffect effect) {
        if (builder.removeEffect != null) {
            return builder.removeEffect.test(effect, false);
        } else {
            return super.removeEffect(effect);
        }
    }*/


    @Override
    public void onEffectAdded(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        if (builder.onEffectAdded != null) {
            final OnEffectContext context = new OnEffectContext(effectInstance, this);
            builder.onEffectAdded.accept(context);
        } else {
            super.onEffectAdded(effectInstance, entity);
        }
    }


    /*@Override
    protected void onEffectUpdated(@NotNull MobEffectInstance effectInstance, boolean isReapplied, @Nullable Entity entity) {
        if (builder.onEffectUpdated != null) {
            builder.onEffectUpdated.accept(effectInstance, isReapplied, entity);
        } else {
            super.onEffectUpdated(effectInstance, isReapplied, entity);
        }
    }*/


    @Override
    protected void onEffectRemoved(@NotNull MobEffectInstance effectInstance) {

        if (builder.onEffectRemoved != null) {
            final OnEffectContext context = new OnEffectContext(effectInstance, this);
            builder.onEffectRemoved.accept(context);
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


    @Override
    public boolean isDeadOrDying() {
        if (builder.isDeadOrDying != null) {
            return builder.isDeadOrDying.test(this);
        } else {
            return super.isDeadOrDying();
        }
    }


    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
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


    @Override
    public boolean isDamageSourceBlocked(@NotNull DamageSource damageSource) {
        if (builder.isDamageSourceBlocked != null) {
            return builder.isDamageSourceBlocked.test(damageSource);
        } else {
            return super.isDamageSourceBlocked(damageSource);
        }
    }


    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (builder.die != null) {
            builder.die.accept(damageSource);
        } else {
            super.die(damageSource);
        }
    }


    /*@Override
    protected void createWitherRose(@Nullable LivingEntity entity) {
        if (builder.createWitherRose != null) {
            builder.createWitherRose.accept(entity);
        } else {
            super.createWitherRose(entity);
        }
    }*/


    @Override
    protected void dropAllDeathLoot(@NotNull DamageSource damageSource) {
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
    protected void dropCustomDeathLoot(@NotNull DamageSource damageSource, int lootingMultiplier, boolean allowDrops) {
        if (builder.dropCustomDeathLoot != null) {
            builder.dropCustomDeathLoot.accept(damageSource, lootingMultiplier, allowDrops);
        } else {
            super.dropCustomDeathLoot(damageSource, lootingMultiplier, allowDrops);
        }
    }

    @Override
    protected void dropFromLootTable(@NotNull DamageSource source, boolean flag) {
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
    public @NotNull Fallsounds getFallSounds() {
        if (builder.fallSoundsFunction != null) {
            return builder.fallSoundsFunction.apply(super.getFallSounds());
        } else {
            return super.getFallSounds();
        }
    }


    @Override
    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        if (builder.eatingSound != null) {
            return builder.eatingSound.apply(itemStack);
        } else {
            return super.getEatingSound(itemStack);
        }
    }


    @Override
    public boolean onClimbable() {
        if (builder.onClimbable != null) {
            return builder.onClimbable.test(this);
        } else {
            return super.onClimbable();
        }
    }

    //Deprecated but still works for 1.20.4 :shrug:
    @Override
    public boolean canBreatheUnderwater() {
        return builder.canBreatheUnderwater;
    }


    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, @NotNull DamageSource damageSource) {
        if (builder.causeFallDamage != null) {
            return builder.causeFallDamage.test(distance, damageSource);
        } else {
            return super.causeFallDamage(distance, damageMultiplier, damageSource);
        }
    }


    @Override
    protected void playBlockFallSound() {
        if (builder.playBlockFallSound != null) {
            builder.playBlockFallSound.accept(null);
        } else {
            super.playBlockFallSound();
        }
    }


    @Override
    protected void hurtArmor(@NotNull DamageSource source, float amount) {
        if (builder.hurtArmor != null) {
            builder.hurtArmor.accept(source, amount);
        } else {
            super.hurtArmor(source, amount);
        }
    }


    @Override
    protected void hurtHelmet(@NotNull DamageSource source, float amount) {
        if (builder.hurtHelmet != null) {
            builder.hurtHelmet.accept(source, amount);
        } else {
            super.hurtHelmet(source, amount);
        }
    }


    @Override
    protected void hurtCurrentlyUsedShield(float amount) {
        if (builder.hurtCurrentlyUsedShield != null) {
            builder.hurtCurrentlyUsedShield.accept(amount);
        } else {
            super.hurtCurrentlyUsedShield(amount);
        }
    }


    @Override
    public @NotNull CombatTracker getCombatTracker() {
        if (builder.combatTracker != null) {
            return builder.combatTracker.apply(super.getCombatTracker());
        } else {
            return super.getCombatTracker();
        }
    }


    @Nullable
    @Override
    public LivingEntity getKillCredit() {
        if (builder.killCredit != null) {
            return builder.killCredit.apply(super.getKillCredit());
        } else {
            return super.getKillCredit();
        }
    }


    @Override
    public void swing(@NotNull InteractionHand hand) {
        if (builder.swingHand != null) {
            builder.swingHand.accept(hand);
        } else {
            super.swing(hand);
        }
    }


    @Override
    public void swing(@NotNull InteractionHand hand, boolean extended) {
        if (builder.swingHandExtended != null) {
            builder.swingHandExtended.accept(hand, extended);
        } else {
            super.swing(hand, extended);
        }
    }


    @Override
    public void handleEntityEvent(byte event) {
        if (builder.handleEntityEvent != null) {
            builder.handleEntityEvent.accept(event);
        } else {
            super.handleEntityEvent(event);
        }
    }


    @Override
    public void setItemInHand(@NotNull InteractionHand hand, @NotNull ItemStack stack) {
        if (builder.setItemInHand != null) {
            builder.setItemInHand.accept(hand, stack);
        } else {
            super.setItemInHand(hand, stack);
        }
    }


    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.setSprinting != null) {
            builder.setSprinting.accept(sprinting);
        } else {
            super.setSprinting(sprinting);
        }
    }


    @Override
    public void push(@NotNull Entity entity) {
        if (builder.pushEntity != null) {
            builder.pushEntity.accept(entity);
        } else {
            super.push(entity);
        }
    }


    @Override
    public boolean shouldShowName() {
        if (builder.shouldShowName != null) {
            return builder.shouldShowName.test(this);
        } else {
            return super.shouldShowName();
        }
    }


    @Override
    public double getJumpBoostPower() {
        if (builder.jumpBoostPower != null) {
            return builder.jumpBoostPower.getAsDouble();
        } else {
            return super.getJumpBoostPower();
        }
    }

    @Override
    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            return builder.canStandOnFluid.test(fluidState);
        } else {
            return super.canStandOnFluid(fluidState);
        }
    }


    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (builder.travel != null) {
            builder.travel.accept(travelVector);
        } else {
            super.travel(travelVector);
        }
    }


    @Override
    public @NotNull Vec3 handleRelativeFrictionAndCalculateMovement(@NotNull Vec3 movementVector, float friction) {
        if (builder.handleRelativeFrictionAndCalculateMovement != null) {
            return builder.handleRelativeFrictionAndCalculateMovement.apply(movementVector, friction);
        } else {
            return super.handleRelativeFrictionAndCalculateMovement(movementVector, friction);
        }
    }


    @Override
    public void setSpeed(float speed) {
        if (builder.setSpeedConsumer != null) {
            builder.setSpeedConsumer.accept(speed);
        } else {
            super.setSpeed(speed);
        }
    }


    @Override
    public boolean doHurtTarget(@NotNull Entity targetEntity) {
        if (builder.doHurtTarget != null) {
            return builder.doHurtTarget.test(targetEntity, false);
        } else {
            return super.doHurtTarget(targetEntity);
        }
    }


    @Override
    public boolean isSensitiveToWater() {
        if (builder.isSensitiveToWater != null) {
            return builder.isSensitiveToWater.test(false);
        } else {
            return super.isSensitiveToWater();
        }
    }


    @Override
    public boolean isAutoSpinAttack() {
        if (builder.isAutoSpinAttack != null) {
            return builder.isAutoSpinAttack.test(false);
        } else {
            return super.isAutoSpinAttack();
        }
    }


    @Override
    public void stopRiding() {
        if (builder.stopRidingCallback != null) {
            builder.stopRidingCallback.run();
        } else {
            super.stopRiding();
        }
    }


    @Override
    public void rideTick() {
        if (builder.rideTick != null) {
            builder.rideTick.accept(this);
        } else {
            super.rideTick();
        }
    }


    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int interpolationSteps, boolean interpolate) {
        if (builder.lerpTo != null) {
            builder.lerpTo.accept(x, y, z, yRot, xRot, interpolationSteps, interpolate);
        } else {
            super.lerpTo(x, y, z, yRot, xRot, interpolationSteps, interpolate);
        }
    }

    @Override
    public void lerpHeadTo(float lyHeadRot, int lerpHeadSteps) {
        if (builder.lerpHeadTo != null) {
            builder.lerpHeadTo.accept(lyHeadRot, lerpHeadSteps);
        } else {
            super.lerpHeadTo(lyHeadRot, lerpHeadSteps);
        }
    }


    @Override
    public void setJumping(boolean p_21314_) {
        if (builder.setJumping != null) {
            builder.setJumping.accept(p_21314_);
        } else {
            super.setJumping(p_21314_);
        }
    }


    @Override
    public void onItemPickup(@NotNull ItemEntity p_21054_) {
        if (builder.onItemPickup != null) {
            builder.onItemPickup.accept(p_21054_);
        } else {
            super.onItemPickup(p_21054_);
        }
    }

   /* @Override
    public void take(@NotNull Entity p_21030_, int p_21031_) {
        if (builder.take != null) {
            builder.take.accept(p_21030_, p_21031_);
        } else {
            super.take(p_21030_, p_21031_);
        }
    }*/


    @Override
    public boolean hasLineOfSight(@NotNull Entity p_147185_) {
        if (builder.hasLineOfSight != null) {
            return builder.hasLineOfSight.test(p_147185_);
        } else {
            return super.hasLineOfSight(p_147185_);
        }
    }


    @Override
    public boolean isEffectiveAi() {
        if (builder.isEffectiveAi != null) {
            return builder.isEffectiveAi.test(null);
        } else {
            return super.isEffectiveAi();
        }
    }


    @Override
    public boolean isPickable() {
        if (builder.isPickable != null) {
            return builder.isPickable.test(null);
        } else {
            return super.isPickable();
        }
    }


    @Override
    public void setYHeadRot(float value) {
        if (builder.setYHeadRot != null) {
            builder.setYHeadRot.accept(value);
        } else {
            super.setYHeadRot(value);
        }
    }


    @Override
    public void setYBodyRot(float value) {
        if (builder.setYBodyRot != null) {
            builder.setYBodyRot.accept(value);
        } else {
            super.setYBodyRot(value);
        }
    }


    @Override
    public void setAbsorptionAmount(float value) {
        if (builder.setAbsorptionAmount != null) {
            builder.setAbsorptionAmount.accept(value);
        } else {
            super.setAbsorptionAmount(value);
        }
    }


    @Override
    public void onEnterCombat() {
        if (builder.onEnterCombat != null) {
            builder.onEnterCombat.run();
        } else {
            super.onEnterCombat();
        }
    }


    @Override
    public void onLeaveCombat() {
        if (builder.onLeaveCombat != null) {
            builder.onLeaveCombat.run();
        } else {
            super.onLeaveCombat();
        }
    }


    @Override
    public boolean isUsingItem() {
        if (builder.isUsingItem != null) {
            return builder.isUsingItem.test(this);
        } else {
            return super.isUsingItem();
        }
    }


    @Override
    protected void setLivingEntityFlag(int flag, boolean value) {
        if (builder.setLivingEntityFlag != null) {
            builder.setLivingEntityFlag.accept(flag, value);
        } else {
            super.setLivingEntityFlag(flag, value);
        }
    }

    @Override
    public void startUsingItem(@NotNull InteractionHand hand) {
        if (builder.startUsingItem != null) {
            builder.startUsingItem.accept(hand);
        } else {
            super.startUsingItem(hand);
        }
    }


    @Override
    public void lookAt(EntityAnchorArgument.@NotNull Anchor anchor, @NotNull Vec3 target) {
        if (builder.lookAt != null) {
            builder.lookAt.accept(anchor, target);
        } else {
            super.lookAt(anchor, target);
        }
    }


    @Override
    public void releaseUsingItem() {
        if (builder.releaseUsingItem != null) {
            builder.releaseUsingItem.run();
        } else {
            super.releaseUsingItem();
        }
    }


    @Override
    public void stopUsingItem() {
        if (builder.stopUsingItem != null) {
            builder.stopUsingItem.run();
        } else {
            super.stopUsingItem();
        }
    }


    @Override
    public boolean isBlocking() {
        if (builder.isBlocking != null) {
            return builder.isBlocking.test(this);
        } else {
            return super.isBlocking();
        }
    }

    @Override
    public boolean isSuppressingSlidingDownLadder() {
        if (builder.isSuppressingSlidingDownLadder != null) {
            return builder.isSuppressingSlidingDownLadder.test(this);
        } else {
            return super.isSuppressingSlidingDownLadder();
        }
    }


    @Override
    public boolean isFallFlying() {
        if (builder.isFallFlying != null) {
            return builder.isFallFlying.test(this);
        } else {
            return super.isFallFlying();
        }
    }

    @Override
    public boolean isVisuallySwimming() {
        if (builder.isVisuallySwimming != null) {
            return builder.isVisuallySwimming.test(this);
        } else {
            return super.isVisuallySwimming();
        }
    }


    @Override
    public boolean randomTeleport(double p_20985_, double p_20986_, double p_20987_, boolean p_20988_) {
        if (builder.randomTeleportX != null && builder.randomTeleportY != null && builder.randomTeleportZ != null && builder.randomTeleportFlag != null) {
            double newX = builder.randomTeleportX.apply(p_20985_, p_20986_);
            double newY = builder.randomTeleportY.apply(p_20986_, p_20987_);
            double newZ = builder.randomTeleportZ.apply(p_20985_, p_20987_);
            boolean shouldTeleport = builder.randomTeleportFlag.test(p_20988_);

            if (shouldTeleport) {
                this.teleportTo(newX, newY, newZ);
                return true;
            }
        }
        return super.randomTeleport(p_20985_, p_20986_, p_20987_, p_20988_);
    }

    @Override
    public boolean isAffectedByPotions() {
        if (builder.isAffectedByPotions != null) {
            return builder.isAffectedByPotions.test(this);
        } else {
            return super.isAffectedByPotions();
        }
    }

    @Override
    public boolean attackable() {
        if (builder.attackable != null) {
            return builder.attackable.test(super.attackable());
        }
        return super.attackable();
    }

    @Override
    public void setRecordPlayingNearby(@NotNull BlockPos p_21082_, boolean p_21083_) {
        if (builder.setRecordPlayingNearby != null) {
            builder.setRecordPlayingNearby.accept(p_21082_, p_21083_);
        } else {
            super.setRecordPlayingNearby(p_21082_, p_21083_);
        }
    }

    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            return builder.canTakeItem.test(itemStack);
        } else {
            return super.canTakeItem(itemStack);
        }
    }

    @Override
    public void setSleepingPos(@NotNull BlockPos blockPos) {
        if (builder.setSleepingPos != null) {
            builder.setSleepingPos.accept(blockPos);
        } else {
            super.setSleepingPos(blockPos);
        }
    }


    @Override
    public boolean isSleeping() {
        if (builder.isSleeping != null) {
            return builder.isSleeping.get();
        } else {
            return super.isSleeping();
        }
    }

    @Override
    public void startSleeping(@NotNull BlockPos blockPos) {
        if (builder.startSleeping != null) {
            builder.startSleeping.accept(blockPos);
        } else {
            super.startSleeping(blockPos);
        }
    }


    @Override
    public void stopSleeping() {
        if (builder.stopSleeping != null) {
            builder.stopSleeping.run();
        } else {
            super.stopSleeping();
        }
    }

    @Override
    public boolean isInWall() {
        if (builder.isInWall != null) {
            return builder.isInWall.get();
        } else {
            return super.isInWall();
        }
    }


    @Override
    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            return builder.eat.apply(level, itemStack);
        } else {
            return super.eat(level, itemStack);
        }
    }

    @Override
    public void broadcastBreakEvent(@NotNull EquipmentSlot equipmentSlot) {
        if (builder.broadcastBreakEvent != null) {
            builder.broadcastBreakEvent.accept(equipmentSlot);
        } else {
            super.broadcastBreakEvent(equipmentSlot);
        }
    }


    @Override
    public void broadcastBreakEvent(@NotNull InteractionHand interactionHand) {
        if (builder.broadcastBreakEventHand != null) {
            builder.broadcastBreakEventHand.accept(interactionHand);
        } else {
            super.broadcastBreakEvent(interactionHand);
        }
    }

    @Override
    public boolean curePotionEffects(@NotNull ItemStack curativeItem) {
        if (builder.curePotionEffects != null) {
            return builder.curePotionEffects.test(curativeItem, false);
        } else {
            return super.curePotionEffects(curativeItem);
        }
    }


    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            return builder.shouldRiderFaceForward.test(player);
        } else {
            return super.shouldRiderFaceForward(player);
        }
    }

    @Override
    public void invalidateCaps() {
        if (builder.invalidateCaps != null) {
            builder.invalidateCaps.run();
        } else {
            super.invalidateCaps();
        }
    }


    @Override
    public void reviveCaps() {
        if (builder.reviveCaps != null) {
            builder.reviveCaps.run();
        } else {
            super.reviveCaps();
        }
    }

    @Override
    public boolean canFreeze() {
        if (builder.canFreeze != null) {
            return builder.canFreeze.test(this);
        } else {
            return super.canFreeze();
        }
    }


    @Override
    public boolean isCurrentlyGlowing() {
        if (builder.isCurrentlyGlowing != null) {
            return builder.isCurrentlyGlowing.test(this);
        } else {
            return super.isCurrentlyGlowing();
        }
    }

    @Override
    public boolean canDisableShield() {
        if (builder.canDisableShield != null) {
            return builder.canDisableShield.test(this);
        } else {
            return super.canDisableShield();
        }
    }

    @Override
    public boolean isColliding(BlockPos pos, BlockState state) {
        if (builder.isColliding != null) {
            return builder.isColliding.test(pos, state);
        } else {
            return super.isColliding(pos, state);
        }
    }


    @Override
    public boolean addTag(String tag) {
        if (builder.addTag != null) {
            return builder.addTag.test(tag);
        } else {
            return super.addTag(tag);
        }
    }


    @Override
    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            builder.onClientRemoval.accept(this);
        } else {
            super.onClientRemoval();
        }
    }


   /* @Override
    public boolean closerThan(Entity entity, double distance) {
        if (builder.closerThan != null) {
            return builder.closerThan.test(entity, distance);
        } else {
            return super.closerThan(entity, distance);
        }
    }*/


   /* @Override
    public boolean closerThan(@NotNull Entity p_216993_, double p_216994_, double p_216995_) {
        return super.closerThan(p_216993_, p_216994_, p_216995_);
    }

    @Override
    protected void setRot(float p_19916_, float p_19917_) {
        super.setRot(p_19916_, p_19917_);
    }

    @Override
    public void setPos(double p_20210_, double p_20211_, double p_20212_) {
        super.setPos(p_20210_, p_20211_, p_20212_);
    }

    @Override
    public void turn(double p_19885_, double p_19886_) {
        super.turn(p_19885_, p_19886_);
    }*/

    /*@Override
    public void setPortalCooldown() {
        super.setPortalCooldown();
    }*/

    @Override
    public void lavaHurt() {
        if (builder.lavaHurt != null) {
            builder.lavaHurt.accept(this);
        } else {
            super.lavaHurt();
        }
    }


    @Override
    protected void onFlap() {
        if (builder.onFlap != null) {
            builder.onFlap.accept(this);
        } else {
            super.onFlap();
        }
    }


    @Override
    public boolean dampensVibrations() {
        return (builder.dampensVibrations != null) ? builder.dampensVibrations.getAsBoolean() : super.dampensVibrations();
    }


    @Override
    public boolean fireImmune() {
        return builder.fireImmune;
    }


    @Override
    public void playerTouch(Player p_20081_) {
        if (builder.playerTouch != null) {
            final PlayerEntityContext context = new PlayerEntityContext(p_20081_, this);
            builder.playerTouch.accept(context);
        } else {
            super.playerTouch(p_20081_);
        }
    }


    @Override
    public HitResult pick(double p_19908_, float p_19909_, boolean p_19910_) {
        return (builder.pick != null) ? builder.pick.apply(p_19908_, p_19909_, p_19910_) : super.pick(p_19908_, p_19909_, p_19910_);
    }


    @Override
    public boolean showVehicleHealth() {
        return (builder.showVehicleHealth != null) ? builder.showVehicleHealth.getAsBoolean() : super.showVehicleHealth();
    }


    @Override
    public void setInvisible(boolean p_20304_) {
        if (builder.setInvisible != null) {
            builder.setInvisible.accept(p_20304_);
        } else {
            super.setInvisible(p_20304_);
        }
    }


    @Override
    public void setAirSupply(int p_20302_) {
        if (builder.setAirSupply != null) {
            builder.setAirSupply.accept(p_20302_);
        } else {
            super.setAirSupply(p_20302_);
        }
    }


    @Override
    public void setTicksFrozen(int p_146918_) {
        if (builder.setTicksFrozen != null) {
            builder.setTicksFrozen.accept(p_146918_);
        } else {
            super.setTicksFrozen(p_146918_);
        }
    }


    @Override
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        if (builder.thunderHit != null) {
            final ThunderHitContext context = new ThunderHitContext(p_19927_, p_19928_, this);
            builder.thunderHit.accept(context);
        } else {
            super.thunderHit(p_19927_, p_19928_);
        }
    }


    @Override
    public void makeStuckInBlock(@NotNull BlockState p_20006_, @NotNull Vec3 p_20007_) {
        if (builder.makeStuckInBlock != null) {
            final StuckInBlockContext context = new StuckInBlockContext(p_20006_, p_20007_, this);
            builder.makeStuckInBlock.accept(context);
        } else {
            super.makeStuckInBlock(p_20006_, p_20007_);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        if (builder.isInvulnerableTo != null) {
            return builder.isInvulnerableTo.test(p_20122_);
        } else {
            return super.isInvulnerableTo(p_20122_);
        }
    }


    @Override
    public void setInvulnerable(boolean p_20332_) {
        if (builder.setInvulnerable != null) {
            builder.setInvulnerable.accept(p_20332_);
        } else {
            super.setInvulnerable(p_20332_);
        }
    }


    @Override
    public boolean canChangeDimensions() {
        if (builder.canChangeDimensions != null) {
            return builder.canChangeDimensions.get();
        } else {
            return super.canChangeDimensions();
        }
    }


    @Override
    public void setCustomName(@Nullable Component p_20053_) {
        if (builder.setCustomName != null) {
            builder.setCustomName.accept(Optional.ofNullable(p_20053_));
        } else {
            super.setCustomName(p_20053_);
        }
    }


    @Override
    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder.mayInteract != null) {
            return builder.mayInteract.test(p_146843_, p_146844_);
        } else {
            return super.mayInteract(p_146843_, p_146844_);
        }
    }


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            return builder.canTrample.test(state, pos, fallDistance);
        } else {
            return super.canTrample(state, pos, fallDistance);
        }
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
        if (builder.getMaxFallDistance != null) {
            return builder.getMaxFallDistance.getAsInt();
        }
        return super.getMaxFallDistance();
    }
}
