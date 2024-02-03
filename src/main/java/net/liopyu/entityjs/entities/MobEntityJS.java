package net.liopyu.entityjs.entities;

import com.mojang.logging.LogUtils;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.util.*;
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
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Objects;
import java.util.Optional;

public class MobEntityJS extends PathfinderMob implements IAnimatableJS {

    private final MobEntityJSBuilder builder;
    private final AnimationFactory animationFactory;

    public MobEntityJS(MobEntityJSBuilder builder, EntityType<? extends PathfinderMob> p_21368_, Level p_21369_) {
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

    public boolean canJump() {
        return builder.canJump;
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

    public void onJump() {
        if (builder.onJump != null) {
            builder.onJump.accept(this);
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
        // Check if the entity can stand on the forward block
        BlockPos forwardPos = this.blockPosition().relative(this.getDirection());
        return this.level.loadedAndEntityCanStandOn(forwardPos, this) && this.getStepHeight() < this.level.getBlockState(forwardPos).getShape(this.level, forwardPos).max(Direction.Axis.Y);
    }

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level p_21480_) {
        return super.createNavigation(p_21480_);
    }

    //Mob Overrides


    @Override
    public void setPathfindingMalus(BlockPathTypes nodeType, float malus) {
        if (builder == null) {
            super.setPathfindingMalus(nodeType, malus);
            return;
        }
        if (builder.setPathfindingMalus != null) {
            builder.setPathfindingMalus.put(nodeType, malus);
        } else {
            super.setPathfindingMalus(nodeType, malus);
        }
    }

    @Override
    public float getPathfindingMalus(BlockPathTypes pNodeType) {
        return super.getPathfindingMalus(pNodeType);
    }

    @Override
    public boolean canCutCorner(BlockPathTypes pathType) {

        if (builder.canCutCorner != null) {
            final ContextUtils.EntityBlockPathTypeContext context = new ContextUtils.EntityBlockPathTypeContext(pathType, this);
            return builder.canCutCorner.apply(context);
        }
        return super.canCutCorner(pathType);
    }


    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (builder.onTargetChanged != null) {
            assert target != null;
            final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(target, this);
            builder.onTargetChanged.accept(context);
        }
    }

    public boolean canFireProjectileWeaponPredicate(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeaponPredicate != null) {
            final ContextUtils.EntityProjectileWeaponContext context = new ContextUtils.EntityProjectileWeaponContext(projectileWeapon, this);
            return builder.canFireProjectileWeaponPredicate.test(context);
        }
        return false;
    }

    public boolean canFireProjectileWeaponBoolean(ProjectileWeaponItem projectileWeapon) {
        if (builder.canFireProjectileWeapon != null) {
            final Item[] items = Wrappers.getItemFromObject(builder.canFireProjectileWeapon);
            for (Item item : items) {
                if (projectileWeapon.equals(item) && item instanceof ProjectileWeaponItem) {
                    return true;
                }
            }
        }
        return super.canFireProjectileWeapon(projectileWeapon);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        if (canFireProjectileWeaponBoolean(projectileWeapon) || canFireProjectileWeaponPredicate(projectileWeapon)) {
            final Item[] items = Wrappers.getItemFromObject(builder.canFireProjectileWeapon);
            for (Item item : items) {
                if (projectileWeapon.equals(item) && item instanceof ProjectileWeaponItem) {
                    return true;
                }
            }
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
            return builder.getAmbientSound.get();
        } else {
            return super.getAmbientSound();
        }
    }


    @Override
    public boolean canHoldItem(ItemStack stack) {
        if (builder.canHoldItem != null) {
            return builder.canHoldItem.test(stack);
        } else {
            return super.canHoldItem(stack);
        }
    }


    @Override
    protected boolean shouldDespawnInPeaceful() {
        return (builder.shouldDespawnInPeaceful != null) ? builder.shouldDespawnInPeaceful : super.shouldDespawnInPeaceful();
    }

    /*@Override
    public boolean canPickUpLoot() {
        return (builder.canPickUpLoot != null) ? builder.canPickUpLoot : super.canPickUpLoot();
    }*/

    @Override
    public boolean isPersistenceRequired() {
        return (builder.isPersistenceRequired != null) ? builder.isPersistenceRequired : super.isPersistenceRequired();
    }


    @Override
    public double getMeleeAttackRangeSqr(LivingEntity entity) {
        return (builder.meleeAttackRangeSqr != null) ? builder.meleeAttackRangeSqr.apply(this) : super.getMeleeAttackRangeSqr(entity);
    }

    //Beginning of Base Overrides
    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    protected float getBlockSpeedFactor() {
        if (builder.blockSpeedFactor != null) {
            return builder.blockSpeedFactor.apply(this);
        }
        return super.getBlockSpeedFactor();
    }

    @Override
    public @NotNull Iterable<ItemStack> getHandSlots() {
        return handItems;
    }


    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }


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


    //Start of the method adding madness - liopyu
    @Override
    protected boolean canAddPassenger(@NotNull Entity entity) {
        if (builder.canAddPassenger != null) {
            return builder.canAddPassenger.test(entity);
        } else return super.canAddPassenger(entity);
    }

    @Override
    protected boolean shouldDropLoot() {
        if (builder.shouldDropLoot != null) {
            return builder.shouldDropLoot.test(this);
        } else {
            return super.shouldDropLoot();
        }
    }


    @Override
    protected boolean isAffectedByFluids() {
        if (builder.isAffectedByFluids != null) {
            return builder.isAffectedByFluids.test(this);
        } else {
            return super.isAffectedByFluids();
        }
    }

    @Override
    protected boolean isAlwaysExperienceDropper() {
        return builder.isAlwaysExperienceDropper;
    }

    @Override
    protected boolean isImmobile() {
        if (builder.isImmobile != null) {
            return builder.isImmobile.test(this);
        } else return super.isImmobile();
    }


    @Override
    protected boolean isFlapping() {
        if (builder.isFlapping != null) {
            return builder.isFlapping.apply(this);
        } else {
            return super.isFlapping();
        }
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (builder.onInteract != null) {
            final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(this, pPlayer, pHand);
            final InteractionResult result = builder.onInteract.apply(context);
            return result == null ? super.interact(pPlayer, pHand) : result;
        }

        return super.mobInteract(pPlayer, pHand);
    }

    @Override
    public int calculateFallDamage(float fallDistance, float fallHeight) {
        return builder.calculateFallDamage == null ? super.calculateFallDamage(fallDistance, fallHeight) : builder.calculateFallDamage.apply(fallDistance, fallHeight);
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
        if (builder.onAddedToWorld != null) {
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
        if (builder.repositionEntityAfterLoad != null) {
            return builder.repositionEntityAfterLoad.getAsBoolean();
        } else {
            return super.repositionEntityAfterLoad();
        }
    }


    @Override
    protected float nextStep() {
        if (builder.nextStep != null) {
            return builder.nextStep.apply(this);
        } else {
            return super.nextStep();
        }
    }


    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource p_21239_) {
        if (builder.setHurtSound != null) {
            return Objects.requireNonNull(Wrappers.soundEvent(builder.setHurtSound));
        } else {
            return super.getHurtSound(p_21239_);
        }
    }


    @Override
    protected SoundEvent getSwimSplashSound() {
        if (builder.setSwimSplashSound != null) {
            return Objects.requireNonNull(Wrappers.soundEvent(builder.setSwimSplashSound));
        } else {
            return super.getSwimSplashSound();
        }
    }


    @Override
    public boolean canAttackType(@NotNull EntityType<?> entityType) {
        if (builder.canAttackType != null) {
            final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(this, entityType);
            return builder.canAttackType.test(context);
        }
        return super.canAttackType(entityType);
    }


    @Override
    public float getScale() {
        if (builder.scale != null) {
            return builder.scale.apply(this);
        } else {
            return super.getScale();
        }
    }

    @Override
    public boolean rideableUnderWater() {
        return builder.rideableUnderWater;
    }


    @Override
    public boolean shouldDropExperience() {
        if (builder.shouldDropExperience == null) {
            return super.shouldDropExperience();
        }
        return builder.shouldDropExperience.test(this);
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
            return builder.visibilityPercent.apply(p_20969_);
        } else {
            return super.getVisibilityPercent(p_20969_);
        }
    }


    @Override
    public boolean canAttack(@NotNull LivingEntity entity) {
        if (builder.canAttack != null) {
            return builder.canAttack.test(entity) && super.canAttack(entity);
        } else {
            return super.canAttack(entity);
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
    public boolean isInvertedHealAndHarm() {
        if (builder.invertedHealAndHarm != null) {
            return builder.invertedHealAndHarm.getAsBoolean();
        } else {
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
        if (builder.fallSounds != null) {
            final SoundEvent smallFall = Wrappers.soundEvent(builder.fallSounds.small());
            final SoundEvent bigFall = Wrappers.soundEvent(builder.fallSounds.big());
            assert smallFall != null;
            assert bigFall != null;
            return new Fallsounds(smallFall, bigFall);
        } else {
            return super.getFallSounds();
        }
    }


    @Override
    public @NotNull SoundEvent getEatingSound(@NotNull ItemStack itemStack) {
        if (builder.eatingSound != null) {
            return Objects.requireNonNull(Wrappers.soundEvent(builder.eatingSound));
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
        if (builder.onLivingFall != null) {
            final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(this, damageMultiplier, distance, damageSource);
            builder.onLivingFall.accept(context);
            return super.causeFallDamage(distance, damageMultiplier, damageSource);
        } else {
            return super.causeFallDamage(distance, damageMultiplier, damageSource);
        }
    }


    @Override
    public void setSprinting(boolean sprinting) {
        if (builder.onSprint != null) {
            builder.onSprint.accept(this);
            super.setSprinting(sprinting);
        } else {
            super.setSprinting(sprinting);
        }
    }


    @Override
    public double getJumpBoostPower() {
        if (builder.jumpBoostPower != null) {
            return builder.jumpBoostPower.getAsDouble() + super.getJumpBoostPower();
        } else {
            return super.getJumpBoostPower();
        }
    }

    @Override
    public boolean canStandOnFluid(@NotNull FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(this, fluidState);
            return builder.canStandOnFluid.test(context);
        } else {
            return super.canStandOnFluid(fluidState);
        }
    }


    @Override
    public void setSpeed(float speed) {
        if (builder.setSpeed != null) {
            builder.setSpeed.accept(speed);
        } else {
            super.setSpeed(speed);
        }
    }


    @Override
    public boolean isSensitiveToWater() {
        if (builder.isSensitiveToWater != null) {
            return builder.isSensitiveToWater.test(this);
        } else {
            return super.isSensitiveToWater();
        }
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
        if (builder.onItemPickup != null) {
            final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(this, p_21054_);
            builder.onItemPickup.accept(context);
        } else {
            super.onItemPickup(p_21054_);
        }
    }


    @Override
    public boolean hasLineOfSight(@NotNull Entity p_147185_) {
        if (builder.hasLineOfSight != null) {
            return builder.hasLineOfSight.test(p_147185_);
        } else {
            return super.hasLineOfSight(p_147185_);
        }
    }


    @Override
    public void setAbsorptionAmount(float value) {
        if (builder.setAbsorptionAmount != null) {
            final ContextUtils.EntityFloatContext context = new ContextUtils.EntityFloatContext(this, value);
            builder.setAbsorptionAmount.accept(context);
        } else {
            super.setAbsorptionAmount(value);
        }
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
        } else {
            super.onLeaveCombat();
        }
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
        if (builder.isAttackable != null) {
            return builder.isAttackable.test(this);
        }
        return super.attackable();
    }


    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, this.level);
            return builder.canTakeItem.test(context);
        } else {
            return super.canTakeItem(itemStack);
        }
    }


    @Override
    public boolean isSleeping() {
        if (builder.isSleeping != null) {
            return builder.isSleeping.test(this);
        } else {
            return super.isSleeping();
        }
    }


    @Override
    public void startSleeping(@NotNull BlockPos blockPos) {

        if (builder.onStartSleeping != null) {
            final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(this, blockPos);
            builder.onStartSleeping.accept(context);
        } else {
            super.startSleeping(blockPos);
        }
    }


    @Override
    public void stopSleeping() {
        if (builder.onStopSleeping != null) {
            builder.onStopSleeping.accept(this);
        } else {
            super.stopSleeping();
        }
    }


    @Override
    public @NotNull ItemStack eat(@NotNull Level level, @NotNull ItemStack itemStack) {
        if (builder.eat != null) {
            final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(this, itemStack, level);
            builder.eat.accept(context);
            return itemStack;
        } else {
            return super.eat(level, itemStack);
        }
    }


    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, this);
            return builder.shouldRiderFaceForward.test(context);
        } else {
            return super.shouldRiderFaceForward(player);
        }
    }

    @Override
    public boolean canFreeze() {
        if (builder.canFreeze != null) {
            return builder.canFreeze.test(this) && !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
        } else {
            return super.canFreeze();
        }
    }


    @Override
    public boolean isCurrentlyGlowing() {
        if (builder.isCurrentlyGlowing != null && !this.level.isClientSide()) {
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
    public void onClientRemoval() {
        if (builder.onClientRemoval != null) {
            builder.onClientRemoval.accept(this);
        } else {
            super.onClientRemoval();
        }
    }

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
    public void playerTouch(Player p_20081_) {
        if (builder.playerTouch != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(p_20081_, this);
            builder.playerTouch.accept(context);
        } else {
            super.playerTouch(p_20081_);
        }
    }

    @Override
    public HitResult pick(double p_19908_, float p_19909_, boolean p_19910_) {
        if (builder.pick != null) {
            final ClipContext context = new ClipContext(this.position(), this.position(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this);
            return builder.pick.apply(context);
        } else {
            return super.pick(p_19908_, p_19909_, p_19910_);
        }
    }


    @Override
    public boolean showVehicleHealth() {
        return (builder.showVehicleHealth != null) ? builder.showVehicleHealth.getAsBoolean() : super.showVehicleHealth();
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
            super.isInvulnerableTo(p_20122_);
            final ContextUtils.DamageContext context = new ContextUtils.DamageContext(this, p_20122_);
            return builder.isInvulnerableTo.test(context);
        }
        return super.isInvulnerableTo(p_20122_);
    }


    public static final Logger LOGGER = LogUtils.getLogger();


    @Override
    public boolean canChangeDimensions() {
        if (builder.canChangeDimensions != null) {
            return builder.canChangeDimensions.test(this);
        } else {
            return super.canChangeDimensions();
        }
    }


    @Override
    public boolean mayInteract(@NotNull Level p_146843_, @NotNull BlockPos p_146844_) {
        if (builder.mayInteract != null) {
            final ContextUtils.MayInteractContext context = new ContextUtils.MayInteractContext(p_146843_, p_146844_, this);
            return builder.mayInteract.test(context);
        } else {
            return super.mayInteract(p_146843_, p_146844_);
        }
    }


    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            final ContextUtils.CanTrampleContext context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, this);
            return builder.canTrample.test(context);
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
