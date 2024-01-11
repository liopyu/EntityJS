package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.liopyu.entityjs.util.MobInteractContext;
import net.liopyu.entityjs.util.ai.goal.GoalSelectorBuilder;
import net.liopyu.entityjs.util.ai.goal.GoalTargetBuilder;
import net.minecraft.BlockUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
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
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Optional;

public class MobEntityJS extends Mob implements IAnimatableJS {

    private final MobEntityJSBuilder builder;
    private final AnimationFactory animationFactory;

    public MobEntityJS(MobEntityJSBuilder builder, EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
        if (p_21369_ != null && !p_21369_.isClientSide) {
            this.registerGoals(); // Call again so that the builder isn't null
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

    @Override
    protected void registerGoals() {
        if (builder == null)
            return; // When called in the super method, the builder is null, thus we call it again when we do have a builder
        // Goal selectors
        final GoalSelectorBuilder<MobEntityJS> goalSelectorBuilder = new GoalSelectorBuilder<>();
        builder.goalSelectorBuilder.accept(goalSelectorBuilder);
        goalSelectorBuilder.apply(this.goalSelector, this);
        // Goal targets
        final GoalTargetBuilder<MobEntityJS> goalTargetBuilder = new GoalTargetBuilder<>();
        builder.goalTargetBuilder.accept(goalTargetBuilder);
        goalTargetBuilder.apply(this.targetSelector, this);
    }

    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    @Override
    protected PathNavigation createNavigation(Level p_21480_) {
        return super.createNavigation(p_21480_);
    }

    //Beginning of Base Overrides
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
    public boolean isPushable() {
        return builder.isPushable;
    }

    @Override
    public HumanoidArm getMainArm() {
        return builder.mainArm;
    }


    @Override
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
            if (this != null) {
                builder.tick.accept(MobEntityJS.this);
            }
        }
    }


    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (builder.mobInteract != null) {
            final MobInteractContext context = new MobInteractContext(MobEntityJS.this, player, hand);
            final InteractionResult result = builder.mobInteract.apply(context);
            return result == null ? super.mobInteract(player, hand) : result;
        }
        return super.mobInteract(player, hand);
    }


    @Override
    protected LootContext.Builder createLootContext(boolean p_21105_, DamageSource p_21106_) {
        LootContext.Builder originalBuilder = super.createLootContext(p_21105_, p_21106_);

        if (builder.customLootContextBuilder != null) {
            return builder.customLootContextBuilder.apply(originalBuilder);
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
    protected boolean canEnterPose(Pose p_20176_) {
        if (builder.customCanEnterPose != null) {
            return builder.customCanEnterPose.test(p_20176_);
        } else {
            return super.canEnterPose(p_20176_);
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
        if (builder.kill != null) {
            builder.kill.accept(MobEntityJS.this);
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
    public float getScale() {
        if (builder.customScale != null) {
            return builder.customScale;
        } else {
            return super.getScale();
        }
    }

    @Override
    public boolean rideableUnderWater() {
        if (builder.rideableUnderWater != null) {
            return builder.rideableUnderWater;
        } else {
            return super.rideableUnderWater();
        }
    }


    @Override
    protected void tickDeath() {
        if (builder.tickDeath != null) {
            builder.tickDeath.accept((MobEntityJS) this);
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
            builder.die.accept(damageSource);
        } else {
            super.die(damageSource);
        }
    }


    @Override
    protected void createWitherRose(@Nullable LivingEntity entity) {
        if (builder.createWitherRose != null) {
            builder.createWitherRose.accept(entity);
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
    public boolean onClimbable() {
        if (builder.onClimbable != null) {
            return builder.onClimbable.test(this);
        } else {
            return super.onClimbable();
        }
    }


    @Override
    public boolean canBreatheUnderwater() {
        return builder.canBreatheUnderwater;
    }


    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource damageSource) {
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
    protected void hurtArmor(DamageSource source, float amount) {
        if (builder.hurtArmor != null) {
            builder.hurtArmor.accept(source, amount);
        } else {
            super.hurtArmor(source, amount);
        }
    }


    @Override
    protected void hurtHelmet(DamageSource source, float amount) {
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
    public CombatTracker getCombatTracker() {
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
    public void swing(InteractionHand hand) {
        if (builder.swingHand != null) {
            builder.swingHand.accept(hand);
        } else {
            super.swing(hand);
        }
    }


    @Override
    public void swing(InteractionHand hand, boolean extended) {
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
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
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
    public void push(Entity entity) {
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
    public boolean canStandOnFluid(FluidState fluidState) {
        if (builder.canStandOnFluid != null) {
            return builder.canStandOnFluid.test(fluidState);
        } else {
            return super.canStandOnFluid(fluidState);
        }
    }


    @Override
    public void travel(Vec3 travelVector) {
        if (builder.travel != null) {
            builder.travel.accept(travelVector);
        } else {
            super.travel(travelVector);
        }
    }


    @Override
    public Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 movementVector, float friction) {
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
    public boolean doHurtTarget(Entity targetEntity) {
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
            builder.rideTick.accept(MobEntityJS.this);
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
    public void onItemPickup(ItemEntity p_21054_) {
        if (builder.onItemPickup != null) {
            builder.onItemPickup.accept(p_21054_);
        } else {
            super.onItemPickup(p_21054_);
        }
    }

    @Override
    public void take(Entity p_21030_, int p_21031_) {
        if (builder.take != null) {
            builder.take.accept(p_21030_, p_21031_);
        } else {
            super.take(p_21030_, p_21031_);
        }
    }


    @Override
    public boolean hasLineOfSight(Entity p_147185_) {
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
    public void startUsingItem(InteractionHand hand) {
        if (builder.startUsingItem != null) {
            builder.startUsingItem.accept(hand);
        } else {
            super.startUsingItem(hand);
        }
    }


    @Override
    public void lookAt(EntityAnchorArgument.Anchor anchor, Vec3 target) {
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
            return builder.isAffectedByPotions.test(MobEntityJS.this);
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
    public void setRecordPlayingNearby(BlockPos p_21082_, boolean p_21083_) {
        if (builder.setRecordPlayingNearby != null) {
            builder.setRecordPlayingNearby.accept(p_21082_, p_21083_);
        } else {
            super.setRecordPlayingNearby(p_21082_, p_21083_);
        }
    }

    @Override
    public boolean canTakeItem(ItemStack itemStack) {
        if (builder.canTakeItem != null) {
            return builder.canTakeItem.test(itemStack);
        } else {
            return super.canTakeItem(itemStack);
        }
    }

    @Override
    public void setSleepingPos(BlockPos blockPos) {
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
    public void startSleeping(BlockPos blockPos) {
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
    public ItemStack eat(Level level, ItemStack itemStack) {
        if (builder.eat != null) {
            return builder.eat.apply(level, itemStack);
        } else {
            return super.eat(level, itemStack);
        }
    }

    @Override
    public void broadcastBreakEvent(EquipmentSlot equipmentSlot) {
        if (builder.broadcastBreakEvent != null) {
            builder.broadcastBreakEvent.accept(equipmentSlot);
        } else {
            super.broadcastBreakEvent(equipmentSlot);
        }
    }


    @Override
    public void broadcastBreakEvent(InteractionHand interactionHand) {
        if (builder.broadcastBreakEventHand != null) {
            builder.broadcastBreakEventHand.accept(interactionHand);
        } else {
            super.broadcastBreakEvent(interactionHand);
        }
    }

    @Override
    public boolean curePotionEffects(ItemStack curativeItem) {
        if (builder.curePotionEffects != null) {
            return builder.curePotionEffects.test(curativeItem, false);
        } else {
            return super.curePotionEffects(curativeItem);
        }
    }


    @Override
    public boolean shouldRiderFaceForward(Player player) {
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
            return builder.isCurrentlyGlowing.test(MobEntityJS.this);
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
    public boolean isColliding(BlockPos p_20040_, BlockState p_20041_) {
        return super.isColliding(p_20040_, p_20041_);
    }

    @Override
    public boolean addTag(String p_20050_) {
        return super.addTag(p_20050_);
    }

    @Override
    public boolean removeTag(String p_20138_) {
        return super.removeTag(p_20138_);
    }

    @Override
    public boolean equals(Object p_20245_) {
        return super.equals(p_20245_);
    }

    @Override
    public void remove(RemovalReason p_146834_) {
        super.remove(p_146834_);
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
    }

    @Override
    public void setPose(Pose p_20125_) {
        super.setPose(p_20125_);
    }

    @Override
    public boolean hasPose(Pose p_217004_) {
        return super.hasPose(p_217004_);
    }

    @Override
    public boolean closerThan(Entity p_19951_, double p_19952_) {
        return super.closerThan(p_19951_, p_19952_);
    }

    @Override
    public boolean closerThan(Entity p_216993_, double p_216994_, double p_216995_) {
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
    }

    @Override
    public void setPortalCooldown() {
        super.setPortalCooldown();
    }

    @Override
    public void lavaHurt() {
        super.lavaHurt();
    }

    @Override
    public void setSecondsOnFire(int p_20255_) {
        super.setSecondsOnFire(p_20255_);
    }

    @Override
    public void setRemainingFireTicks(int p_20269_) {
        super.setRemainingFireTicks(p_20269_);
    }

    @Override
    public void clearFire() {
        super.clearFire();
    }

    @Override
    public boolean isFree(double p_20230_, double p_20231_, double p_20232_) {
        return super.isFree(p_20230_, p_20231_, p_20232_);
    }

    @Override
    public void move(MoverType p_19973_, Vec3 p_19974_) {
        super.move(p_19973_, p_19974_);
    }

    @Override
    public void gameEvent(GameEvent p_146853_, @Nullable Entity p_146854_) {
        super.gameEvent(p_146853_, p_146854_);
    }

    @Override
    public void gameEvent(GameEvent p_146851_) {
        super.gameEvent(p_146851_);
    }

    @Override
    protected void onFlap() {
        super.onFlap();
    }

    @Override
    public void playSound(SoundEvent p_19938_, float p_19939_, float p_19940_) {
        super.playSound(p_19938_, p_19939_, p_19940_);
    }

    @Override
    public void playSound(SoundEvent p_216991_) {
        super.playSound(p_216991_);
    }

    @Override
    public boolean isSilent() {
        return super.isSilent();
    }

    @Override
    public void setSilent(boolean p_20226_) {
        super.setSilent(p_20226_);
    }

    @Override
    public boolean isNoGravity() {
        return super.isNoGravity();
    }

    @Override
    public void setNoGravity(boolean p_20243_) {
        super.setNoGravity(p_20243_);
    }

    @Override
    public boolean dampensVibrations() {
        return super.dampensVibrations();
    }

    @Override
    public boolean fireImmune() {
        return super.fireImmune();
    }


    @Override
    public boolean isInWater() {
        return super.isInWater();
    }

    @Override
    public boolean isInWaterOrRain() {
        return super.isInWaterOrRain();
    }

    @Override
    public boolean isInWaterRainOrBubble() {
        return super.isInWaterRainOrBubble();
    }

    @Override
    public boolean isInWaterOrBubble() {
        return super.isInWaterOrBubble();
    }

    @Override
    public boolean isUnderWater() {
        return super.isUnderWater();
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return super.canSpawnSprintParticle();
    }

    @Override
    public boolean isInLava() {
        return super.isInLava();
    }

    @Override
    public void moveRelative(float p_19921_, Vec3 p_19922_) {
        super.moveRelative(p_19921_, p_19922_);
    }

    @Override
    public void absMoveTo(double p_19891_, double p_19892_, double p_19893_, float p_19894_, float p_19895_) {
        super.absMoveTo(p_19891_, p_19892_, p_19893_, p_19894_, p_19895_);
    }

    @Override
    public void absMoveTo(double p_20249_, double p_20250_, double p_20251_) {
        super.absMoveTo(p_20249_, p_20250_, p_20251_);
    }

    @Override
    public void moveTo(Vec3 p_20220_) {
        super.moveTo(p_20220_);
    }

    @Override
    public void moveTo(double p_20105_, double p_20106_, double p_20107_) {
        super.moveTo(p_20105_, p_20106_, p_20107_);
    }

    @Override
    public void moveTo(BlockPos p_20036_, float p_20037_, float p_20038_) {
        super.moveTo(p_20036_, p_20037_, p_20038_);
    }

    @Override
    public void moveTo(double p_20108_, double p_20109_, double p_20110_, float p_20111_, float p_20112_) {
        super.moveTo(p_20108_, p_20109_, p_20110_, p_20111_, p_20112_);
    }

    @Override
    public void playerTouch(Player p_20081_) {
        super.playerTouch(p_20081_);
    }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) {
        super.push(p_20286_, p_20287_, p_20288_);
    }

    @Override
    public HitResult pick(double p_19908_, float p_19909_, boolean p_19910_) {
        return super.pick(p_19908_, p_19909_, p_19910_);
    }

    @Override
    public void awardKillScore(Entity p_19953_, int p_19954_, DamageSource p_19955_) {
        super.awardKillScore(p_19953_, p_19954_, p_19955_);
    }

    @Override
    public boolean shouldRender(double p_20296_, double p_20297_, double p_20298_) {
        return super.shouldRender(p_20296_, p_20297_, p_20298_);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double p_19883_) {
        return super.shouldRenderAtSqrDistance(p_19883_);
    }

    @Override
    public boolean canCollideWith(Entity p_20303_) {
        return super.canCollideWith(p_20303_);
    }

    @Override
    public boolean showVehicleHealth() {
        return super.showVehicleHealth();
    }

    @Override
    public void handleInsidePortal(BlockPos p_20222_) {
        super.handleInsidePortal(p_20222_);
    }

    @Override
    public void lerpMotion(double p_20306_, double p_20307_, double p_20308_) {
        super.lerpMotion(p_20306_, p_20307_, p_20308_);
    }

    @Override
    public void setInvisible(boolean p_20304_) {
        super.setInvisible(p_20304_);
    }

    @Override
    public void setAirSupply(int p_20302_) {
        super.setAirSupply(p_20302_);
    }

    @Override
    public void setTicksFrozen(int p_146918_) {
        super.setTicksFrozen(p_146918_);
    }

    @Override
    public boolean isFullyFrozen() {
        return super.isFullyFrozen();
    }

    @Override
    public void thunderHit(ServerLevel p_19927_, LightningBolt p_19928_) {
        super.thunderHit(p_19927_, p_19928_);
    }

    @Override
    public void makeStuckInBlock(BlockState p_20006_, Vec3 p_20007_) {
        super.makeStuckInBlock(p_20006_, p_20007_);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource p_20122_) {
        return super.isInvulnerableTo(p_20122_);
    }

    @Override
    public boolean isInvulnerable() {
        return super.isInvulnerable();
    }

    @Override
    public void setInvulnerable(boolean p_20332_) {
        super.setInvulnerable(p_20332_);
    }

    @Override
    public boolean canChangeDimensions() {
        return super.canChangeDimensions();
    }

    @Override
    public boolean displayFireAnimation() {
        return super.displayFireAnimation();
    }

    @Override
    public void setCustomName(@Nullable Component p_20053_) {
        super.setCustomName(p_20053_);
    }

    @Override
    public void setCustomNameVisible(boolean p_20341_) {
        super.setCustomNameVisible(p_20341_);
    }

    @Override
    public boolean isCustomNameVisible() {
        return super.isCustomNameVisible();
    }

    @Override
    public void setLevelCallback(EntityInLevelCallback p_146849_) {
        super.setLevelCallback(p_146849_);
    }

    @Override
    public boolean isAlwaysTicking() {
        return super.isAlwaysTicking();
    }

    @Override
    public boolean mayInteract(Level p_146843_, BlockPos p_146844_) {
        return super.mayInteract(p_146843_, p_146844_);
    }

    @Override
    public boolean canUpdate() {
        return super.canUpdate();
    }

    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return super.canTrample(state, pos, fallDistance);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
    }

    @Override
    public Level getLevel() {
        return super.getLevel();
    }
}
