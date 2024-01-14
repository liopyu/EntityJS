package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.entities.BaseEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.*;
import net.minecraft.BlockUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.liopyu.entityjs.util.ai.brain.BrainProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.util.TriPredicate;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.easing.EasingType;
import software.bernie.geckolib3.core.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib3.core.event.KeyframeEvent;
import software.bernie.geckolib3.core.event.ParticleKeyFrameEvent;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.*;
import java.util.function.*;

/**
 * The base builder for all entity types that EntityJS can handle, has methods to allow overriding
 * nearly every method available in {@link LivingEntity}. Implementors are free to use as many or few
 * of these as they wish
 *
 * @param <T> The entity class that the built entity type is for, this should be a custom class
 *            that extends {@link LivingEntity} or a subclass and {@link IAnimatableJS}
 */
@SuppressWarnings("unused")
public abstract class BaseEntityBuilder<T extends LivingEntity & IAnimatableJS> extends BuilderBase<EntityType<T>> {

    public static final List<BaseEntityBuilder<?>> thisList = new ArrayList<>();

    public transient float width;
    public transient float height;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient ResourceLocation[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient Function<T, ResourceLocation> modelResource;
    public transient Function<T, ResourceLocation> textureResource;
    public transient Function<T, ResourceLocation> animationResource;
    public transient boolean isPushable;

    public transient boolean isAttackable;
    public transient Consumer<AttributeSupplier.Builder> attributes;
    public transient final List<AnimationControllerSupplier<T>> animationSuppliers;
    public transient boolean shouldDropLoot;
    public transient Predicate<Entity> setCanAddPassenger;
    public transient boolean isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient boolean isImmobile;
    public transient float getBlockJumpFactor;
    public transient Function<T, Integer> blockSpeedFactor;
    public transient float getJumpPower;
    public transient float getSoundVolume;
    public transient float getWaterSlowDown;
    public transient SoundEvent setDeathSound;
    public transient SoundEvent setSwimSound;
    public transient boolean isFlapping;
    public transient SoundEvent getDeathSound;
    public transient SoundEvent getSwimSound;
    public transient RenderType renderType;
    public transient SoundEvent getSwimHighSpeedSplashSound;
    public transient EntityType<?> getType;
    public transient AABB customBoundingBox;

    public transient BrainProviderBuilder brainProviderBuilder;
    public transient HumanoidArm mainArm;
    public transient boolean hasInventory;
    public transient Consumer<BrainBuilder> brainBuilder;

    /*public transient Consumer<BaseEntityJS> dropExperienceHandler;*/
    public transient boolean onSoulSpeedBlock;

    public transient Function<LootContext.Builder, LootContext.Builder> customLootContextBuilder;
    public transient Consumer<LivingEntity> customDoAutoAttack;

    public transient Function<Pose, Float> customGetStandingEyeHeight;
    public transient Function<BlockPos, BlockPos> customGetBlockPosBelow;
    public transient Function<Integer, Integer> customDecreaseAirSupply;
    public transient Consumer<LivingEntity> customBlockedByShield;
    public transient BiFunction<Float, Float, Float> customTickHeadTurn;
    public transient Predicate<EquipmentSlot> customDoesEmitEquipEvent;
    public transient Function<Pose, AABB> customGetBoundingBoxForPose;
    public transient Predicate<Pose> customCanEnterPose;
    public transient IntFunction<Boolean> customGetSharedFlag;
    public transient Predicate<Vec3> customIsHorizontalCollisionMinor;
    public transient BooleanSupplier customRepositionEntityAfterLoad;
    public transient BooleanSupplier customUpdateInWaterStateAndDoFluidPushing;
    public transient Function<LivingEntity, Component> customGetTypeName;
    public transient BiFunction<DamageSource, Float, Float> customGetDamageAfterArmorAbsorb;
    public transient BiFunction<DamageSource, Float, Float> customGetDamageAfterMagicAbsorb;
    public transient Function<Float, Float> customNextStep;
    public transient Function<HoverEvent, HoverEvent> customCreateHoverEvent;
    public transient Function<Integer, Integer> customGetPermissionLevel;
    public transient Function<Integer, Integer> customIncreaseAirSupply;
    public transient Function<double[], ListTag> customNewDoubleList;
    public transient Function<float[], ListTag> customNewFloatList;
    public transient Function<Entity.MovementEmission, Entity.MovementEmission> customGetMovementEmission;
    public transient Function<ExitPortalInfo, Optional<BlockUtil.FoundRectangle>> customGetExitPortal;
    public transient Function<ItemStack, SoundEvent> customGetDrinkingSound;
    public transient Function<DamageSource, SoundEvent> customGetHurtSound;
    public transient Function<ServerLevel, PortalInfo> customFindDimensionEntryPoint;
    public transient Function<SoundEvent, SoundEvent> customGetSwimSplashSound;
    public transient BiFunction<Direction.Axis, BlockUtil.FoundRectangle, Vec3> customGetRelativePortalPosition;
    public transient Function<Vec3, Vec3> customLimitPistonMovement;
    public transient BiFunction<Vec3, MoverType, Vec3> customMaybeBackOffFromEdge;
    public transient BiConsumer<DamageSource, Float> customActuallyHurt;
    public transient Consumer<LivingEntity> customBlockUsingShield;
    public transient BiConsumer<AABB, AABB> customCheckAutoSpinAttack;
    public transient BiConsumer<Double, ResourceLocation> customCheckFallDamage;
    public transient Consumer<T> kill;
    public transient Function<EntityType<?>, Boolean> customCanAttackType;

    public transient Function<Float, Float> customGetSwimAmount;

    public transient Runnable customBaseTick;
    public transient Boolean canSpawnSoulSpeedParticle;
    public transient Runnable customSpawnSoulSpeedParticle;
    public transient Runnable customRemoveSoulSpeed;
    public transient Runnable customTryAddSoulSpeed;
    public transient Runnable customRemoveFrost;
    public transient Runnable customTryAddFrost;
    public transient Consumer<BlockPos> customOnChangedBlock;
    public transient Boolean isBaby;
    public transient Float customScale;
    public transient Boolean rideableUnderWater;
    public transient Consumer<T> tickDeath;
    public transient boolean shouldDropExperience;

    public transient Function<BaseEntityJS, Integer> customExperienceReward;
    public transient Supplier<Boolean> customShouldDiscardFriction;
    public transient Consumer<Boolean> customSetDiscardFriction;

    public transient TriConsumer<EquipmentSlot, ItemStack, ItemStack> customOnEquipItem;
    public transient Consumer<ItemStack> customPlayEquipSound;
    public transient Runnable customTickEffects;
    public transient Runnable customUpdateInvisibilityStatus;

    public transient Function<Entity, Double> customGetVisibilityPercent;

    public transient Predicate<LivingEntity> customCanAttack;

    public transient BiPredicate<LivingEntity, TargetingConditions> customCanAttackWithConditions;

    public transient BooleanSupplier customCanBeSeenAsEnemy;

    public transient BooleanSupplier customCanBeSeenByAnyone;

    public transient Runnable customRemoveEffectParticles;

    public transient Predicate<Boolean> customRemoveAllEffects;
    public transient BiPredicate<MobEffectInstance, Entity> customAddEffect;
    public transient Predicate<MobEffectInstance> canBeAffectedPredicate;


    public transient BiConsumer<MobEffectInstance, @Nullable Entity> forceAddEffectConsumer;

    public transient boolean invertedHealAndHarm;


    /* public transient Function<MobEffect, MobEffectInstance> removeEffectNoUpdateFunction;*/

    /*public transient BiPredicate<MobEffect, Boolean> removeEffect;*/


    public transient Consumer<OnEffectContext> onEffectAdded;

    /* public transient TriConsumer<MobEffectInstance, Boolean, Entity> onEffectUpdated;*/

    public transient Consumer<OnEffectContext> onEffectRemoved;

    public transient BiConsumer<Float, T> healAmount;


    public transient Predicate<LivingEntity> isDeadOrDying;

    public transient BiPredicate<DamageSource, Float> hurtPredicate;

    public transient Supplier<DamageSource> lastDamageSourceSupplier;

    public transient Predicate<DamageSource> isDamageSourceBlocked;

    public transient Consumer<DamageSource> die;

    /*public transient Consumer<LivingEntity> createWitherRose;*/

    public transient Consumer<DamageSource> dropAllDeathLoot;

    public transient Consumer<Void> dropEquipment;

    public transient TriConsumer<DamageSource, Integer, Boolean> dropCustomDeathLoot;

    public transient Supplier<ResourceLocation> lootTable;


    public transient BiConsumer<DamageSource, Boolean> dropFromLootTable;

    public transient TriConsumer<Double, Double, Double> knockback;

    public transient Runnable skipDropExperience;

    public transient Supplier<Boolean> wasExperienceConsumed;

    public transient Function<LivingEntity.Fallsounds, LivingEntity.Fallsounds> fallSoundsFunction;

    public transient Function<ItemStack, SoundEvent> eatingSound;

    public transient Predicate<LivingEntity> onClimbable;
    public transient boolean canBreatheUnderwater;

    public transient BiPredicate<Float, DamageSource> causeFallDamage;

    public transient Consumer<Void> playBlockFallSound;
    public transient BiConsumer<DamageSource, Float> hurtArmor;

    public transient BiConsumer<DamageSource, Float> hurtHelmet;
    public transient Consumer<Float> hurtCurrentlyUsedShield;
    public transient Function<CombatTracker, CombatTracker> combatTracker;
    public transient Function<LivingEntity, LivingEntity> killCredit;
    public transient Consumer<InteractionHand> swingHand;
    public transient BiConsumer<InteractionHand, Boolean> swingHandExtended;
    public transient Consumer<Byte> handleEntityEvent;
    public transient BiConsumer<InteractionHand, ItemStack> setItemInHand;
    public transient Consumer<Boolean> setSprinting;
    public transient Consumer<Entity> pushEntity;
    public transient Predicate<LivingEntity> shouldShowName;
    public transient DoubleSupplier jumpBoostPower;
    public transient Predicate<FluidState> canStandOnFluid;
    public transient Consumer<Vec3> travel;
    public transient BiFunction<Vec3, Float, Vec3> handleRelativeFrictionAndCalculateMovement;
    public transient Consumer<Float> setSpeedConsumer;
    public transient BiPredicate<Entity, Boolean> doHurtTarget;
    public transient Predicate<Boolean> isSensitiveToWater;
    public transient Predicate<Boolean> isAutoSpinAttack;
    public transient Runnable stopRidingCallback;
    public transient Consumer<T> rideTick;

    @FunctionalInterface
    public interface HeptConsumer {
        void accept(double arg1, double arg2, double arg3, float arg4, float arg5, int arg6, boolean arg7);
    }

    public transient HeptConsumer lerpTo;
    public transient BiConsumer<Float, Integer> lerpHeadTo;
    public transient Consumer<Boolean> setJumping;
    public transient Consumer<ItemEntity> onItemPickup;
    /*public transient BiConsumer<Entity, Integer> take;*/
    public transient Predicate<Entity> hasLineOfSight;
    public transient Predicate<Void> isEffectiveAi;
    public transient Predicate<Void> isPickable;
    public transient Consumer<Float> setYHeadRot;
    public transient Consumer<Float> setYBodyRot;
    public transient Consumer<Float> setAbsorptionAmount;
    public transient Runnable onEnterCombat;
    public transient Runnable onLeaveCombat;
    public transient Predicate<LivingEntity> isUsingItem;

    public transient BiConsumer<Integer, Boolean> setLivingEntityFlag;
    public transient Consumer<InteractionHand> startUsingItem;
    public transient BiConsumer<EntityAnchorArgument.Anchor, Vec3> lookAt;
    public transient Runnable releaseUsingItem;
    public transient Runnable stopUsingItem;
    public transient Predicate<LivingEntity> isBlocking;
    public transient Predicate<LivingEntity> isSuppressingSlidingDownLadder;
    public transient Predicate<LivingEntity> isFallFlying;
    public transient Predicate<LivingEntity> isVisuallySwimming;
    public transient BiFunction<Double, Double, Double> randomTeleportX;
    public transient BiFunction<Double, Double, Double> randomTeleportY;
    public transient BiFunction<Double, Double, Double> randomTeleportZ;
    public transient Predicate<Boolean> randomTeleportFlag;
    public transient Predicate<LivingEntity> isAffectedByPotions;

    public transient Predicate<Boolean> attackable;
    public transient BiConsumer<BlockPos, Boolean> setRecordPlayingNearby;
    public transient Predicate<ItemStack> canTakeItem;
    public transient Consumer<BlockPos> setSleepingPos;
    public transient Supplier<Boolean> isSleeping;
    public transient Consumer<BlockPos> startSleeping;
    public transient Runnable stopSleeping;
    public transient Supplier<Boolean> isInWall;
    public transient BiFunction<Level, ItemStack, ItemStack> eat;
    public transient Consumer<EquipmentSlot> broadcastBreakEvent;
    public transient Consumer<InteractionHand> broadcastBreakEventHand;
    public transient BiPredicate<ItemStack, Boolean> curePotionEffects;
    public transient Predicate<Player> shouldRiderFaceForward;
    public transient Runnable invalidateCaps;

    public transient Runnable reviveCaps;
    public transient Predicate<LivingEntity> canFreeze;
    public transient Predicate<LivingEntity> isCurrentlyGlowing;
    public transient Predicate<LivingEntity> canDisableShield;
    public transient IntSupplier getMaxFallDistance;
    /*Right now Mob Interaction result is only functioning for MobEntityJS so we will probably have to add
    More public transients as we add more extensions or figure out some other logic*/
    public transient Function<MobInteractContext, @Nullable InteractionResult> mobInteract;
    public transient BiPredicate<BlockPos, BlockState> isColliding;

    public transient Predicate<String> addTag;
    public transient Consumer<T> onClientRemoval;
    public transient Consumer<T> onAddedToWorld;
    /*public transient BiPredicate<Entity, Double> closerThan;*/
    public transient Consumer<T> lavaHurt;
    public transient Consumer<T> onFlap;
    public transient BooleanSupplier dampensVibrations;

    public transient Consumer<PlayerEntityContext> playerTouch;
    public transient TriFunction<Double, Float, Boolean, HitResult> pick;
    public transient BooleanSupplier showVehicleHealth;
    public transient Consumer<Boolean> setInvisible;
    public transient IntConsumer setAirSupply;
    public transient IntConsumer setTicksFrozen;
    public transient Consumer<ThunderHitContext> thunderHit;
    public transient Consumer<StuckInBlockContext> makeStuckInBlock;
    public transient Predicate<DamageSource> isInvulnerableTo;
    public transient Consumer<Boolean> setInvulnerable;
    public transient Supplier<Boolean> canChangeDimensions;
    public transient Consumer<Optional<Component>> setCustomName;

    public transient BiPredicate<Level, BlockPos> mayInteract;
    public transient TriPredicate<BlockState, BlockPos, Float> canTrample;
    public transient Consumer<T> onRemovedFromWorld;

    //STUFF
    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        fireImmune = false;
        immuneTo = new ResourceLocation[0];
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        modelResource = t -> t.getBuilder().newID("geo/", ".geo.json");
        textureResource = t -> t.getBuilder().newID("textures/model/entity/", ".png");
        animationResource = t -> t.getBuilder().newID("animations/", ".animation.json");
        isPushable = true;
        isAttackable = true;
        attributes = builder -> {
        };
        animationSuppliers = new ArrayList<>();
        shouldDropLoot = true;
        isAffectedByFluids = false;
        isAlwaysExperienceDropper = false;
        isImmobile = false;
        getBlockJumpFactor = 0.5f;
        blockSpeedFactor = t -> 1;
        getJumpPower = 0.5f;
        getSoundVolume = 1.0f;
        getWaterSlowDown = 0.0f;
        setDeathSound = SoundEvents.BUCKET_EMPTY;
        setSwimSound = SoundEvents.MOOSHROOM_SHEAR;
        fallDamageFunction = null;
        isFlapping = false;
        getDeathSound = SoundEvents.BUCKET_EMPTY;
        getSwimSound = SoundEvents.MOOSHROOM_SHEAR;
        renderType = RenderType.CUTOUT;
        getSwimHighSpeedSplashSound = SoundEvents.MOOSHROOM_SHEAR;
        mainArm = HumanoidArm.RIGHT;
        canBreatheUnderwater = false;
        passengerPredicate = entity -> true;

    }

    @Info(value = "Sets the main arm of the entity, defaults to 'right'")
    public BaseEntityBuilder<T> mainArm(HumanoidArm arm) {
        mainArm = arm;
        return this;
    }

    @Info(value = "Sets the hit box of the entity type", params = {
            @Param(name = "width", value = "The width of the entity, defaults to 1"),
            @Param(name = "height", value = "The height if the entity, defaults to 1")
    })
    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Info(value = "Determines if the entity should serialize its data, defaults to true")
    public BaseEntityBuilder<T> saves(boolean b) {
        save = b;
        return this;
    }

    @Info(value = "Determines if the entity is immune to fire, defaults to false")
    public BaseEntityBuilder<T> fireImmune(boolean b) {
        fireImmune = b;
        return this;
    }

    @Info(value = "Determines the blocks the entity is 'immune' to")
    public BaseEntityBuilder<T> immuneTo(ResourceLocation... blocks) {
        this.immuneTo = blocks;
        return this;
    }

    @Info(value = "Determines if the entity can spawn far from players")
    public BaseEntityBuilder<T> canSpawnFarFromPlayer(boolean b) {
        spawnFarFromPlayer = b;
        return this;
    }

    @Info(value = "Sets the client tracking range, defaults to 5")
    public BaseEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public BaseEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public BaseEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }

    @Info(value = """
            Sets how the model of the entity is determined, has access to the entity
            to allow changing the model based on info about the entity
                        
            Defaults to returning <namespace>:geo/<path>.geo.json
            """)
    public BaseEntityBuilder<T> modelResourceFunction(Function<T, ResourceLocation> function) {
        modelResource = function;
        return this;
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/model/entity/<path>.png
            """)
    public BaseEntityBuilder<T> textureResourceFunction(Function<T, ResourceLocation> function) {
        textureResource = function;
        return this;
    }

    @Info(value = """
            Sets how the animations of the entity is determined, has access to the entity
            to allow changing the animations based on info about the entity
                        
            Defaults to returning <namespace>:animations/<path>.animation.json
            """)
    public BaseEntityBuilder<T> animationResourceFunction(Function<T, ResourceLocation> function) {
        animationResource = function;
        return this;
    }

    public BaseEntityBuilder<T> isPushable(boolean b) {
        isPushable = b;
        return this;
    }


    public BaseEntityBuilder<T> isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }

    @Info(value = "Adds the provided attribute to the entity type")
    public BaseEntityBuilder<T> addAttribute(Attribute attribute) {
        attributes = attributes.andThen(builder -> builder.add(attribute));
        return this;
    }

    @Info(value = "Adds the provided attribute to the entity type with the provided base value", params = {
            @Param(name = "attribute", value = "The attribute"),
            @Param(name = "value", value = "The default value of the attribute")
    })
    public BaseEntityBuilder<T> addAttribute(Attribute attribute, double value) {
        attributes = attributes.andThen(builder -> builder.add(attribute, value));
        return this;
    }

    public Predicate<Entity> passengerPredicate;
    public Predicate<LivingEntity> livingpassengerPredicate;

    public BaseEntityBuilder<T> setCanAddPassenger(Predicate<Entity> predicate) {
        passengerPredicate = predicate;
        return this;
    }


    public BaseEntityBuilder<T> isAffectedByFluids(boolean b) {
        isAffectedByFluids = b;
        return this;
    }


    public BaseEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
    }

    public BaseEntityBuilder<T> isImmobile(boolean b) {
        isImmobile = b;
        return this;
    }

    public BaseEntityBuilder<T> isAlwaysExperienceDropper(boolean b) {
        isAlwaysExperienceDropper = b;
        return this;
    }

    public BiFunction<Float, Float, Integer> fallDamageFunction;

    public BaseEntityBuilder<T> calculateFallDamage(BiFunction<Float, Float, Integer> calculation) {
        fallDamageFunction = calculation;
        return this;
    }

    public BaseEntityBuilder<T> getDeathSound(SoundEvent sound) {
        setDeathSound = sound;
        return this;
    }

    public BaseEntityBuilder<T> getSwimSound(SoundEvent sound) {
        setSwimSound = sound;
        return this;
    }

    public BaseEntityBuilder<T> getSwimHighSpeedSplashSound(SoundEvent sound) {
        getSwimHighSpeedSplashSound = sound;
        return this;
    }


    public BaseEntityBuilder<T> blockSpeedFactor(int i) {
        blockSpeedFactor = t -> i;
        return this;
    }

    public BaseEntityBuilder<T> blockSpeedFactor(Function<T, Integer> function) {
        blockSpeedFactor = function;
        return this;
    }

    public BaseEntityBuilder<T> isFlapping(boolean b) {
        isFlapping = b;
        return this;
    }

    public transient Consumer<T> tick = t -> {
    };


    public BaseEntityBuilder<T> tick(Consumer<T> tickCallback) {
        tick = tickCallback;
        return this;
    }

    @Info(value = "Sets the custom onAddedToWorld behavior")
    public BaseEntityBuilder<T> onAddedToWorld(Consumer<T> onAddedToWorldCallback) {
        onAddedToWorld = onAddedToWorldCallback;
        return this;
    }

    public BaseEntityBuilder<T> getType(EntityType<T> type) {
        getType = type;
        return this;
    }


    // Add this method to set the dropExperienceHandler in BaseEntityBuilder
    /*public BaseEntityBuilder<T> dropExperienceHandler(Consumer<BaseEntityJS> handler) {
        dropExperienceHandler = handler;
        return this;
    }*/


    public BaseEntityBuilder<T> boundingBox(AABB boundingBox) {
        this.customBoundingBox = boundingBox;
        return this;
    }

    public BaseEntityBuilder<T> lootContextBuilder(Function<LootContext.Builder, LootContext.Builder> customLootContextBuilder) {
        this.customLootContextBuilder = customLootContextBuilder;
        return this;
    }


    public BaseEntityBuilder<T> doAutoAttackOnTouch(Consumer<LivingEntity> customDoAutoAttack) {
        this.customDoAutoAttack = customDoAutoAttack;
        return this;
    }

    public void applyCustomDoAutoAttackOnTouch(LivingEntity target) {
        if (customDoAutoAttack != null) {
            customDoAutoAttack.accept(target);
        }
    }

    public BaseEntityBuilder<T> getStandingEyeHeight(Function<Pose, Float> customGetStandingEyeHeight) {
        this.customGetStandingEyeHeight = customGetStandingEyeHeight;
        return this;
    }

    public BaseEntityBuilder<T> getBlockPosBelowThatAffectsMyMovement(Function<BlockPos, BlockPos> customGetBlockPosBelow) {
        this.customGetBlockPosBelow = customGetBlockPosBelow;
        return this;
    }

    public BaseEntityBuilder<T> decreaseAirSupply(Function<Integer, Integer> customDecreaseAirSupply) {
        this.customDecreaseAirSupply = customDecreaseAirSupply;
        return this;
    }

    public BaseEntityBuilder<T> blockedByShield(Consumer<LivingEntity> customBlockedByShield) {
        this.customBlockedByShield = customBlockedByShield;
        return this;
    }

    public BaseEntityBuilder<T> tickHeadTurn(BiFunction<Float, Float, Float> customTickHeadTurn) {
        this.customTickHeadTurn = customTickHeadTurn;
        return this;
    }

    public BaseEntityBuilder<T> doesEmitEquipEvent(Predicate<EquipmentSlot> customDoesEmitEquipEvent) {
        this.customDoesEmitEquipEvent = customDoesEmitEquipEvent;
        return this;
    }

    public BaseEntityBuilder<T> getBoundingBoxForPose(Function<Pose, AABB> customGetBoundingBoxForPose) {
        this.customGetBoundingBoxForPose = customGetBoundingBoxForPose;
        return this;
    }

    public BaseEntityBuilder<T> canEnterPose(Predicate<Pose> customCanEnterPose) {
        this.customCanEnterPose = customCanEnterPose;
        return this;
    }

    public BaseEntityBuilder<T> getSharedFlag(IntFunction<Boolean> customGetSharedFlag) {
        this.customGetSharedFlag = customGetSharedFlag;
        return this;
    }

    public BaseEntityBuilder<T> isHorizontalCollisionMinor(Predicate<Vec3> customIsHorizontalCollisionMinor) {
        this.customIsHorizontalCollisionMinor = customIsHorizontalCollisionMinor;
        return this;
    }

    public BaseEntityBuilder<T> repositionEntityAfterLoad(BooleanSupplier customRepositionEntityAfterLoad) {
        this.customRepositionEntityAfterLoad = customRepositionEntityAfterLoad;
        return this;
    }

    public BaseEntityBuilder<T> updateInWaterStateAndDoFluidPushing(BooleanSupplier customUpdateInWaterStateAndDoFluidPushing) {
        this.customUpdateInWaterStateAndDoFluidPushing = customUpdateInWaterStateAndDoFluidPushing;
        return this;
    }

    public BaseEntityBuilder<T> getTypeName(Function<LivingEntity, Component> customGetTypeName) {
        this.customGetTypeName = customGetTypeName;
        return this;
    }

    public BaseEntityBuilder<T> getDamageAfterArmorAbsorb(BiFunction<DamageSource, Float, Float> customGetDamageAfterArmorAbsorb) {
        this.customGetDamageAfterArmorAbsorb = customGetDamageAfterArmorAbsorb;
        return this;
    }

    public BaseEntityBuilder<T> getDamageAfterMagicAbsorb(BiFunction<DamageSource, Float, Float> customGetDamageAfterMagicAbsorb) {
        this.customGetDamageAfterMagicAbsorb = customGetDamageAfterMagicAbsorb;
        return this;
    }

    public BaseEntityBuilder<T> nextStep(Function<Float, Float> customNextStep) {
        this.customNextStep = customNextStep;
        return this;
    }

    public BaseEntityBuilder<T> createHoverEvent(Function<HoverEvent, HoverEvent> customCreateHoverEvent) {
        this.customCreateHoverEvent = customCreateHoverEvent;
        return this;
    }

    public BaseEntityBuilder<T> getPermissionLevel(Function<Integer, Integer> customGetPermissionLevel) {
        this.customGetPermissionLevel = customGetPermissionLevel;
        return this;
    }

    public BaseEntityBuilder<T> increaseAirSupply(Function<Integer, Integer> customIncreaseAirSupply) {
        this.customIncreaseAirSupply = customIncreaseAirSupply;
        return this;
    }

    public BaseEntityBuilder<T> newDoubleList(Function<double[], ListTag> customNewDoubleList) {
        this.customNewDoubleList = customNewDoubleList;
        return this;
    }

    public BaseEntityBuilder<T> newFloatList(Function<float[], ListTag> customNewFloatList) {
        this.customNewFloatList = customNewFloatList;
        return this;
    }

    public BaseEntityBuilder<T> getMovementEmission(Function<Entity.MovementEmission, Entity.MovementEmission> customGetMovementEmission) {
        this.customGetMovementEmission = customGetMovementEmission;
        return this;
    }

    public BaseEntityBuilder<T> getExitPortal(Function<ExitPortalInfo, Optional<BlockUtil.FoundRectangle>> customGetExitPortal) {
        this.customGetExitPortal = customGetExitPortal;
        return this;
    }

    public BaseEntityBuilder<T> getDrinkingSound(Function<ItemStack, SoundEvent> customGetDrinkingSound) {
        this.customGetDrinkingSound = customGetDrinkingSound;
        return this;
    }

    public BaseEntityBuilder<T> getHurtSound(Function<DamageSource, SoundEvent> customGetHurtSound) {
        this.customGetHurtSound = customGetHurtSound;
        return this;
    }

    public BaseEntityBuilder<T> findDimensionEntryPoint(Function<ServerLevel, PortalInfo> customFindDimensionEntryPoint) {
        this.customFindDimensionEntryPoint = customFindDimensionEntryPoint;
        return this;
    }

    public BaseEntityBuilder<T> getSwimSplashSound(Function<SoundEvent, SoundEvent> customGetSwimSplashSound) {
        this.customGetSwimSplashSound = customGetSwimSplashSound;
        return this;
    }

    public BaseEntityBuilder<T> getRelativePortalPosition(BiFunction<Direction.Axis, BlockUtil.FoundRectangle, Vec3> customGetRelativePortalPosition) {
        this.customGetRelativePortalPosition = customGetRelativePortalPosition;
        return this;
    }

    public BaseEntityBuilder<T> limitPistonMovement(Function<Vec3, Vec3> customLimitPistonMovement) {
        this.customLimitPistonMovement = customLimitPistonMovement;
        return this;
    }

    public BaseEntityBuilder<T> maybeBackOffFromEdge(BiFunction<Vec3, MoverType, Vec3> customMaybeBackOffFromEdge) {
        this.customMaybeBackOffFromEdge = customMaybeBackOffFromEdge;
        return this;
    }

    public BaseEntityBuilder<T> actuallyHurt(BiConsumer<DamageSource, Float> customActuallyHurt) {
        this.customActuallyHurt = customActuallyHurt;
        return this;
    }

    public BaseEntityBuilder<T> blockUsingShield(Consumer<LivingEntity> customBlockUsingShield) {
        this.customBlockUsingShield = customBlockUsingShield;
        return this;
    }

    public BaseEntityBuilder<T> checkAutoSpinAttack(BiConsumer<AABB, AABB> customCheckAutoSpinAttack) {
        this.customCheckAutoSpinAttack = customCheckAutoSpinAttack;
        return this;
    }

    public BaseEntityBuilder<T> customCheckFallDamage(BiConsumer<Double, ResourceLocation> customCheckFallDamage) {
        this.customCheckFallDamage = customCheckFallDamage;
        return this;
    }

    public void applyCustomCheckFallDamage(double fallDistance, ResourceLocation blockLocation) {
        if (customCheckFallDamage != null) {
            customCheckFallDamage.accept(fallDistance, blockLocation);
        }
    }

    public BaseEntityBuilder<T> kill(Consumer<T> kill) {
        this.kill = kill;
        return this;
    }

    public BaseEntityBuilder<T> canAttackType(Function<EntityType<?>, Boolean> customCanAttackType) {
        this.customCanAttackType = customCanAttackType;
        return this;
    }

    public BaseEntityBuilder<T> swimAmount(Function<Float, Float> customGetSwimAmount) {
        this.customGetSwimAmount = customGetSwimAmount;
        return this;
    }

   /* public BaseEntityBuilder<T> baseTick(Runnable customBaseTick) {
        this.customBaseTick = customBaseTick;
        return this;
    }*/

    public BaseEntityBuilder<T> canSpawnSoulSpeedParticle(Boolean canSpawn) {
        this.canSpawnSoulSpeedParticle = canSpawn;
        return this;
    }

    public BaseEntityBuilder<T> spawnSoulSpeedParticle(Runnable customSpawnSoulSpeedParticle) {
        this.customSpawnSoulSpeedParticle = customSpawnSoulSpeedParticle;
        return this;
    }

    public BaseEntityBuilder<T> removeSoulSpeed(Runnable customRemoveSoulSpeed) {
        this.customRemoveSoulSpeed = customRemoveSoulSpeed;
        return this;
    }

    public BaseEntityBuilder<T> tryAddSoulSpeed(Runnable customTryAddSoulSpeed) {
        this.customTryAddSoulSpeed = customTryAddSoulSpeed;
        return this;
    }

    public BaseEntityBuilder<T> removeFrost(Runnable customRemoveFrost) {
        this.customRemoveFrost = customRemoveFrost;
        return this;
    }

    public BaseEntityBuilder<T> tryAddFrost(Runnable customTryAddFrost) {
        this.customTryAddFrost = customTryAddFrost;
        return this;
    }

    public BaseEntityBuilder<T> onChangedBlock(Consumer<BlockPos> customOnChangedBlock) {
        this.customOnChangedBlock = customOnChangedBlock;
        return this;
    }


    public BaseEntityBuilder<T> scale(float customScale) {
        this.customScale = customScale;
        return this;
    }

    public BaseEntityBuilder<T> rideableUnderWater(Boolean rideableUnderWater) {
        this.rideableUnderWater = rideableUnderWater;
        return this;
    }

    public BaseEntityBuilder<T> tickDeath(Consumer<T> tickDeath) {
        this.tickDeath = tickDeath;
        return this;
    }

    public BaseEntityBuilder<T> shouldDropExperience(boolean b) {
        this.shouldDropExperience = b;
        return this;
    }

    public BaseEntityBuilder<T> experienceReward(Function<BaseEntityJS, Integer> customExperienceReward) {
        this.customExperienceReward = customExperienceReward;
        return this;
    }

    /*public BaseEntityBuilder<T> random(Function<BaseEntityJS, RandomSource> customRandom) {
        this.customRandom = customRandom;
        return this;
    }

    public BaseEntityBuilder<T> lastHurtByMob(Function<BaseEntityJS, LivingEntity> customLastHurtByMob) {
        this.customLastHurtByMob = customLastHurtByMob;
        return this;
    }

    public BaseEntityBuilder<T> getLastHurtByMobTimestamp(IntSupplier customGetLastHurtByMobTimestamp) {
        this.customGetLastHurtByMobTimestamp = customGetLastHurtByMobTimestamp;
        return this;
    }

    public BaseEntityBuilder<T> setLastHurtByPlayer(Consumer<@Nullable Player> customSetLastHurtByPlayer) {
        this.customSetLastHurtByPlayer = customSetLastHurtByPlayer;
        return this;
    }


    public BaseEntityBuilder<T> setLastHurtByMob(Consumer<LivingEntity> customSetLastHurtByMob) {
        this.customSetLastHurtByMob = customSetLastHurtByMob;
        return this;
    }

    public BaseEntityBuilder<T> getLastHurtMob(Supplier<LivingEntity> customGetLastHurtMob) {
        this.customGetLastHurtMob = customGetLastHurtMob;
        return this;
    }

    public BaseEntityBuilder<T> lastHurtByMobTimestamp(Function<BaseEntityJS, Integer> customLastHurtByMobTimestamp) {
        this.customGetLastHurtMobTimestamp = customGetLastHurtMobTimestamp;
        return this;
    }

    public BaseEntityBuilder<T> setLastHurtMob(Consumer<Entity> customSetLastHurtMob) {
        this.customSetLastHurtMob = customSetLastHurtMob;
        return this;
    }

    public BaseEntityBuilder<T> getNoActionTime(IntSupplier customGetNoActionTime) {
        this.customGetNoActionTime = customGetNoActionTime;
        return this;
    }

    public BaseEntityBuilder<T> setNoActionTime(IntConsumer customSetNoActionTime) {
        this.customSetNoActionTime = customSetNoActionTime;
        return this;
    }*/

    public BaseEntityBuilder<T> shouldDiscardFriction(Supplier<Boolean> customShouldDiscardFriction) {
        this.customShouldDiscardFriction = customShouldDiscardFriction;
        return this;
    }


    public BaseEntityBuilder<T> setDiscardFriction(Consumer<Boolean> customSetDiscardFriction) {
        this.customSetDiscardFriction = customSetDiscardFriction;
        return this;
    }


    public BaseEntityBuilder<T> onEquipItem(TriConsumer<EquipmentSlot, ItemStack, ItemStack> customOnEquipItem) {
        this.customOnEquipItem = customOnEquipItem;
        return this;
    }

    public BaseEntityBuilder<T> playEquipSound(Consumer<ItemStack> customPlayEquipSound) {
        this.customPlayEquipSound = customPlayEquipSound;
        return this;
    }

    public BaseEntityBuilder<T> tickEffects(Runnable customTickEffects) {
        this.customTickEffects = customTickEffects;
        return this;
    }


    public BaseEntityBuilder<T> updateInvisibilityStatus(Runnable customUpdateInvisibilityStatus) {
        this.customUpdateInvisibilityStatus = customUpdateInvisibilityStatus;
        return this;
    }


    public BaseEntityBuilder<T> getVisibilityPercent(Function<Entity, Double> customGetVisibilityPercent) {
        this.customGetVisibilityPercent = customGetVisibilityPercent;
        return this;
    }


    public BaseEntityBuilder<T> canAttack(Predicate<LivingEntity> customCanAttack) {
        this.customCanAttack = customCanAttack;
        return this;
    }

    public BaseEntityBuilder<T> canAttackWithConditions(BiPredicate<LivingEntity, TargetingConditions> customCanAttack) {
        this.customCanAttackWithConditions = customCanAttack;
        return this;
    }


    public BaseEntityBuilder<T> canBeSeenAsEnemy(BooleanSupplier customCanBeSeenAsEnemy) {
        this.customCanBeSeenAsEnemy = customCanBeSeenAsEnemy;
        return this;
    }


    public BaseEntityBuilder<T> canBeSeenByAnyone(BooleanSupplier customCanBeSeenByAnyone) {
        this.customCanBeSeenByAnyone = customCanBeSeenByAnyone;
        return this;
    }


    public BaseEntityBuilder<T> removeEffectParticles(Runnable customRemoveEffectParticles) {
        this.customRemoveEffectParticles = customRemoveEffectParticles;
        return this;
    }


    public BaseEntityBuilder<T> removeAllEffects(Predicate<Boolean> customRemoveAllEffects) {
        this.customRemoveAllEffects = customRemoveAllEffects;
        return this;
    }


    public BaseEntityBuilder<T> addEffect(BiPredicate<MobEffectInstance, Entity> customAddEffect) {
        this.customAddEffect = customAddEffect;
        return this;
    }


    @Info(value = "Sets the custom logic to determine if the entity can be affected by a specific potion effect.")
    public BaseEntityBuilder<T> customCanBeAffected(Predicate<MobEffectInstance> predicate) {
        canBeAffectedPredicate = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for forcefully adding a potion effect to the entity.")
    public BaseEntityBuilder<T> customForceAddEffect(BiConsumer<MobEffectInstance, @Nullable Entity> consumer) {
        forceAddEffectConsumer = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if healing and harming effects are inverted for the entity.")
    public BaseEntityBuilder<T> customInvertedHealAndHarm(boolean invertedHealAndHarm) {
        this.invertedHealAndHarm = invertedHealAndHarm;
        return this;
    }


    /*@Info(value = "Sets the custom logic for removing a potion effect without updating the entity state.")
    public BaseEntityBuilder<T> customRemoveEffectNoUpdate(Function<MobEffect, MobEffectInstance> function) {
        removeEffectNoUpdateFunction = function;
        return this;
    }*/


    /*@Info(value = "Sets the custom logic for removing a potion effect from the entity.")
    public BaseEntityBuilder<T> customRemoveEffect(BiPredicate<MobEffect, Boolean> predicate) {
        removeEffect = predicate;
        return this;
    }*/


    @Info(value = "Sets the custom logic for when a potion effect is added to the entity.")
    public BaseEntityBuilder<T> onEffectAdded(Consumer<OnEffectContext> consumer) {
        onEffectAdded = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for healing the entity")
    public BaseEntityBuilder<T> customHeal(BiConsumer<Float, T> callback) {
        healAmount = callback;
        return this;
    }


    @Info(value = "Sets the custom logic for when a potion effect is removed from the entity.")
    public BaseEntityBuilder<T> onEffectRemoved(Consumer<OnEffectContext> consumer) {
        onEffectRemoved = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for healing the entity")
    public BaseEntityBuilder<T> healAmount(BiConsumer<Float, T> callback) {
        healAmount = callback;
        return this;
    }


    /*public BaseEntityBuilder<T> getHealth(FloatSupplier customGetHealth) {
        this.customGetHealth = customGetHealth;
        return this;
    }*/

    /*@Info(value = "Sets the custom logic for setting the entity's health")
    public BaseEntityBuilder<T> setHealth(float f) {
        setHealth = f;
        return this;
    }*/


    @Info(value = "Sets the custom logic for determining if the entity is dead or dying")
    public BaseEntityBuilder<T> isDeadOrDying(Predicate<LivingEntity> predicate) {
        isDeadOrDying = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for handling entity damage")
    public BaseEntityBuilder<T> customHurt(BiPredicate<DamageSource, Float> predicate) {
        hurtPredicate = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for providing the last damage source")
    public BaseEntityBuilder<T> lastDamageSourceSupplier(Supplier<DamageSource> supplier) {
        lastDamageSourceSupplier = supplier;
        return this;
    }


    /*@Info(value = "Sets the custom logic for playing the hurt sound")
    public BaseEntityBuilder<T> playHurtSound(Consumer<DamageSource> consumer) {
        playHurtSound = consumer;
        return this;
    }*/


    @Info(value = "Sets the custom logic for determining if the damage source is blocked")
    public BaseEntityBuilder<T> isDamageSourceBlocked(Predicate<DamageSource> predicate) {
        isDamageSourceBlocked = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for when the entity dies")
    public BaseEntityBuilder<T> die(Consumer<DamageSource> consumer) {
        die = consumer;
        return this;
    }


    /*@Info(value = "Sets the custom logic for creating a wither rose when the entity dies")
    public BaseEntityBuilder<T> createWitherRose(Consumer<LivingEntity> consumer) {
        createWitherRose = consumer;
        return this;
    }*/


    @Info(value = "Sets the custom logic for dropping all death loot when the entity dies")
    public BaseEntityBuilder<T> dropAllDeathLoot(Consumer<DamageSource> consumer) {
        dropAllDeathLoot = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping equipment when the entity dies")
    public BaseEntityBuilder<T> dropEquipment(Consumer<Void> consumer) {
        dropEquipment = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping custom death loot when the entity dies")
    public BaseEntityBuilder<T> dropCustomDeathLoot(TriConsumer<DamageSource, Integer, Boolean> consumer) {
        dropCustomDeathLoot = consumer;
        return this;
    }


    @Info(value = "Sets the custom loot table for the entity")
    public BaseEntityBuilder<T> lootTable(Supplier<ResourceLocation> supplier) {
        lootTable = supplier;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping items from the entity's loot table upon death")
    public BaseEntityBuilder<T> dropFromLootTable(BiConsumer<DamageSource, Boolean> consumer) {
        dropFromLootTable = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for knockback effect on the entity")
    public BaseEntityBuilder<T> knockback(TriConsumer<Double, Double, Double> consumer) {
        knockback = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic to skip dropping experience points upon entity death")
    public BaseEntityBuilder<T> skipDropExperience(Runnable runnable) {
        skipDropExperience = runnable;
        return this;
    }


    @Info(value = "Sets the custom logic to determine if experience points were consumed upon entity death")
    public BaseEntityBuilder<T> wasExperienceConsumed(Supplier<Boolean> supplier) {
        wasExperienceConsumed = supplier;
        return this;
    }


    @Info(value = "Sets the custom logic for determining fall sounds for the entity")
    public BaseEntityBuilder<T> fallSounds(Function<LivingEntity.Fallsounds, LivingEntity.Fallsounds> function) {
        fallSoundsFunction = function;
        return this;
    }


    @Info(value = "Sets the custom logic for determining the eating sound for the entity")
    public BaseEntityBuilder<T> eatingSound(Function<ItemStack, SoundEvent> function) {
        eatingSound = function;
        return this;
    }


    /*public BaseEntityBuilder<T> setOnGround(BooleanConsumer customSetOnGround) {
        this.customSetOnGround = customSetOnGround;
        return this;
    }*/

   /* public BaseEntityBuilder<T> getLastClimbablePos(Supplier<Optional<BlockPos>> customGetLastClimbablePos) {
        this.customGetLastClimbablePos = customGetLastClimbablePos;
        return this;
    }*/

    @Info(value = "Sets the custom logic for determining if the entity is on a climbable surface")
    public BaseEntityBuilder<T> onClimbable(Predicate<LivingEntity> predicate) {
        onClimbable = predicate;
        return this;
    }


    /*public BaseEntityBuilder<T> isAlive(BooleanSupplier customIsAlive) {
        this.customIsAlive = customIsAlive;
        return this;
    }*/
    @Info(value = "Sets the custom logic for determining if the entity can breathe underwater")
    public BaseEntityBuilder<T> canBreatheUnderwater(boolean b) {
        canBreatheUnderwater = b;
        return this;
    }

    @Info(value = "Sets the custom logic for causing fall damage")
    public BaseEntityBuilder<T> causeFallDamage(BiPredicate<Float, DamageSource> predicate) {
        causeFallDamage = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for playing block fall sound")
    public BaseEntityBuilder<T> playBlockFallSound(Consumer<Void> consumer) {
        playBlockFallSound = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting armor")
    public BaseEntityBuilder<T> hurtArmor(BiConsumer<DamageSource, Float> consumer) {
        hurtArmor = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting the helmet")
    public BaseEntityBuilder<T> hurtHelmet(BiConsumer<DamageSource, Float> consumer) {
        hurtHelmet = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting the currently used shield")
    public BaseEntityBuilder<T> hurtCurrentlyUsedShield(Consumer<Float> consumer) {
        hurtCurrentlyUsedShield = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for getting the combat tracker")
    public BaseEntityBuilder<T> combatTracker(Function<CombatTracker, CombatTracker> function) {
        combatTracker = function;
        return this;
    }

    @Info(value = "Sets the custom logic for getting the kill credit entity")
    public BaseEntityBuilder<T> killCredit(Function<LivingEntity, LivingEntity> function) {
        killCredit = function;
        return this;
    }

    @Info(value = "Sets the custom logic for swinging the entity's hand")
    public BaseEntityBuilder<T> swingHand(Consumer<InteractionHand> consumer) {
        swingHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for swinging the entity's hand with extended parameters")
    public BaseEntityBuilder<T> swingHandExtended(BiConsumer<InteractionHand, Boolean> consumer) {
        swingHandExtended = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for handling entity events")
    public BaseEntityBuilder<T> handleEntityEvent(Consumer<Byte> consumer) {
        handleEntityEvent = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the item in the entity's hand")
    public BaseEntityBuilder<T> setItemInHand(BiConsumer<InteractionHand, ItemStack> consumer) {
        setItemInHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's sprinting state")
    public BaseEntityBuilder<T> setSprinting(Consumer<Boolean> consumer) {
        setSprinting = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for pushing another entity")
    public BaseEntityBuilder<T> pushEntity(Consumer<Entity> consumer) {
        pushEntity = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity's name should be shown")
    public BaseEntityBuilder<T> shouldShowName(Predicate<LivingEntity> predicate) {
        shouldShowName = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining the entity's jump boost power")
    public BaseEntityBuilder<T> jumpBoostPower(DoubleSupplier supplier) {
        jumpBoostPower = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can stand on a specific fluid")
    public BaseEntityBuilder<T> canStandOnFluid(Predicate<FluidState> predicate) {
        canStandOnFluid = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for entity movement")
    public BaseEntityBuilder<T> travel(Consumer<Vec3> consumer) {
        travel = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for handling relative friction and calculating movement")
    public BaseEntityBuilder<T> handleRelativeFrictionAndCalculateMovement(BiFunction<Vec3, Float, Vec3> function) {
        handleRelativeFrictionAndCalculateMovement = function;
        return this;
    }

    @Info(value = "Sets the custom speed for the entity")
    public BaseEntityBuilder<T> setSpeed(Consumer<Float> consumer) {
        setSpeedConsumer = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting a target entity")
    public BaseEntityBuilder<T> doHurtTarget(BiPredicate<Entity, Boolean> predicate) {
        doHurtTarget = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is sensitive to water")
    public BaseEntityBuilder<T> isSensitiveToWater(Predicate<Boolean> predicate) {
        isSensitiveToWater = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity performs auto spin attack")
    public BaseEntityBuilder<T> isAutoSpinAttack(Predicate<Boolean> predicate) {
        isAutoSpinAttack = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity stops riding")
    public BaseEntityBuilder<T> stopRidingCallback(Runnable callback) {
        stopRidingCallback = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity is updated while riding")
    public BaseEntityBuilder<T> rideTick(Consumer<T> callback) {
        rideTick = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for lerping the entity's position and rotation.")
    public BaseEntityBuilder<T> lerpTo(HeptConsumer consumer) {
        lerpTo = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for lerping the head position")
    public BaseEntityBuilder<T> lerpHeadTo(BiConsumer<Float, Integer> consumer) {
        lerpHeadTo = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the jumping state")
    public BaseEntityBuilder<T> setJumping(Consumer<Boolean> consumer) {
        setJumping = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity picks up an item")
    public BaseEntityBuilder<T> onItemPickup(Consumer<ItemEntity> consumer) {
        onItemPickup = consumer;
        return this;
    }

    /*@Info(value = "Sets the custom logic for when the entity takes an action with another entity")
    public BaseEntityBuilder<T> take(BiConsumer<Entity, Integer> consumer) {
        take = consumer;
        return this;
    }*/

    @Info(value = "Sets the custom logic for determining if the entity has line of sight to another entity")
    public BaseEntityBuilder<T> hasLineOfSight(Predicate<Entity> predicate) {
        hasLineOfSight = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity has effective AI")
    public BaseEntityBuilder<T> isEffectiveAi(Predicate<Void> predicate) {
        isEffectiveAi = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is pickable")
    public BaseEntityBuilder<T> isPickable(Predicate<Void> predicate) {
        isPickable = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's head rotation on the Y-axis")
    public BaseEntityBuilder<T> setYHeadRot(Consumer<Float> consumer) {
        setYHeadRot = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's body rotation on the Y-axis")
    public BaseEntityBuilder<T> setYBodyRot(Consumer<Float> consumer) {
        setYBodyRot = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's absorption amount")
    public BaseEntityBuilder<T> setAbsorptionAmount(Consumer<Float> consumer) {
        setAbsorptionAmount = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity enters combat")
    public BaseEntityBuilder<T> onEnterCombat(Runnable runnable) {
        onEnterCombat = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity leaves combat")
    public BaseEntityBuilder<T> onLeaveCombat(Runnable runnable) {
        onLeaveCombat = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is using an item")
    public BaseEntityBuilder<T> isUsingItem(Predicate<LivingEntity> predicate) {
        isUsingItem = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for living entity flags")
    public BaseEntityBuilder<T> setLivingEntityFlag(BiConsumer<Integer, Boolean> consumer) {
        setLivingEntityFlag = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for starting to use an item")
    public BaseEntityBuilder<T> startUsingItem(Consumer<InteractionHand> consumer) {
        startUsingItem = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for making the entity look at a specific position using an anchor point")
    public BaseEntityBuilder<T> lookAt(BiConsumer<EntityAnchorArgument.Anchor, Vec3> consumer) {
        lookAt = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for releasing the item in use by the entity")
    public BaseEntityBuilder<T> releaseUsingItem(Runnable runnable) {
        releaseUsingItem = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for stopping the use of an item by the entity")
    public BaseEntityBuilder<T> stopUsingItem(Runnable runnable) {
        stopUsingItem = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is blocking")
    public BaseEntityBuilder<T> isBlocking(Predicate<LivingEntity> predicate) {
        isBlocking = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is suppressing sliding down a ladder")
    public BaseEntityBuilder<T> isSuppressingSlidingDownLadder(Predicate<LivingEntity> predicate) {
        isSuppressingSlidingDownLadder = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is fall flying")
    public BaseEntityBuilder<T> isFallFlying(Predicate<LivingEntity> predicate) {
        isFallFlying = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is visually swimming")
    public BaseEntityBuilder<T> isVisuallySwimming(Predicate<LivingEntity> predicate) {
        isVisuallySwimming = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for random teleportation of the entity")
    public BaseEntityBuilder<T> randomTeleport(
            BiFunction<Double, Double, Double> teleportX,
            BiFunction<Double, Double, Double> teleportY,
            BiFunction<Double, Double, Double> teleportZ,
            Predicate<Boolean> teleportFlag) {
        randomTeleportX = teleportX;
        randomTeleportY = teleportY;
        randomTeleportZ = teleportZ;
        randomTeleportFlag = teleportFlag;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is affected by potions")
    public BaseEntityBuilder<T> isAffectedByPotions(Predicate<LivingEntity> predicate) {
        isAffectedByPotions = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is attackable")
    public BaseEntityBuilder<T> attackable(Predicate<Boolean> predicate) {
        attackable = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for playing records nearby")
    public BaseEntityBuilder<T> setRecordPlayingNearby(BiConsumer<BlockPos, Boolean> consumer) {
        setRecordPlayingNearby = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can take a specific item")
    public BaseEntityBuilder<T> canTakeItem(Predicate<ItemStack> predicate) {
        canTakeItem = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the sleeping position")
    public BaseEntityBuilder<T> setSleepingPos(Consumer<BlockPos> consumer) {
        setSleepingPos = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is sleeping")
    public BaseEntityBuilder<T> isSleeping(Supplier<Boolean> supplier) {
        isSleeping = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for starting sleeping at a specific position")
    public BaseEntityBuilder<T> startSleeping(Consumer<BlockPos> consumer) {
        startSleeping = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for stopping sleeping")
    public BaseEntityBuilder<T> stopSleeping(Runnable runnable) {
        stopSleeping = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is in a wall")
    public BaseEntityBuilder<T> isInWall(Supplier<Boolean> supplier) {
        isInWall = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for eating an item in a level")
    public BaseEntityBuilder<T> eat(BiFunction<Level, ItemStack, ItemStack> function) {
        eat = function;
        return this;
    }

    @Info(value = "Sets the custom logic for broadcasting a break event for an equipment slot")
    public BaseEntityBuilder<T> broadcastBreakEvent(Consumer<EquipmentSlot> consumer) {
        broadcastBreakEvent = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for broadcasting a break event for an interaction hand")
    public BaseEntityBuilder<T> broadcastBreakEventHand(Consumer<InteractionHand> consumer) {
        broadcastBreakEventHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for curing potion effects with a curative item")
    public BaseEntityBuilder<T> curePotionEffects(BiPredicate<ItemStack, Boolean> predicate) {
        curePotionEffects = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the rider should face forward")
    public BaseEntityBuilder<T> shouldRiderFaceForward(Predicate<Player> predicate) {
        shouldRiderFaceForward = predicate;
        return this;
    }

    @Info(value = "Sets the custom callback for invalidating capabilities")
    public BaseEntityBuilder<T> invalidateCapsCallback(Runnable callback) {
        invalidateCaps = callback;
        return this;
    }

    @Info(value = "Sets the custom callback for reviving capabilities")
    public BaseEntityBuilder<T> reviveCapsCallback(Runnable callback) {
        reviveCaps = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can freeze")
    public BaseEntityBuilder<T> canFreezePredicate(Predicate<LivingEntity> predicate) {
        canFreeze = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is currently glowing")
    public BaseEntityBuilder<T> isCurrentlyGlowing(Predicate<LivingEntity> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can disable a shield")
    public BaseEntityBuilder<T> canDisableShield(Predicate<LivingEntity> predicate) {
        canDisableShield = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for mob interaction")
    public BaseEntityBuilder<T> mobInteract(Function<MobInteractContext, @Nullable InteractionResult> f) {
        mobInteract = f;
        return this;
    }

    @Info(value = "Sets the custom logic for how far a mob falls before taking damage")
    public BaseEntityBuilder<T> getMaxFallDistance(IntSupplier i) {
        getMaxFallDistance = i;
        return this;
    }

    @Info(value = "Sets the custom isColliding behavior")
    public BaseEntityBuilder<T> isColliding(BiPredicate<BlockPos, BlockState> predicate) {
        isColliding = predicate;
        return this;
    }

    @Info(value = "Sets the custom addTag behavior")
    public BaseEntityBuilder<T> addTag(Predicate<String> predicate) {
        addTag = predicate;
        return this;
    }

    @Info(value = "Sets the custom onClientRemoval behavior")
    public BaseEntityBuilder<T> onClientRemoval(Consumer<T> consumer) {
        onClientRemoval = consumer;
        return this;
    }

    /*@Info(value = "Sets the custom closerThan behavior")
    public BaseEntityBuilder<T> closerThan(BiPredicate<Entity, Double> predicate) {
        closerThan = predicate;
        return this;
    }*/
    @Info(value = "Sets the custom lavaHurt behavior")
    public BaseEntityBuilder<T> lavaHurt(Consumer<T> consumer) {
        lavaHurt = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for entity flapping actions")
    public BaseEntityBuilder<T> onFlap(Consumer<T> consumer) {
        onFlap = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the entity dampens vibrations")
    public BaseEntityBuilder<T> dampensVibrations(BooleanSupplier supplier) {
        dampensVibrations = supplier;
        return this;
    }

    @Info(value = "Sets the custom behavior for handling player touch events")
    public BaseEntityBuilder<T> playerTouch(Consumer<PlayerEntityContext> consumer) {
        playerTouch = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for picking entity hit results")
    public BaseEntityBuilder<T> pick(TriFunction<Double, Float, Boolean, HitResult> function) {
        pick = function;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the vehicle health should be shown")
    public BaseEntityBuilder<T> showVehicleHealth(BooleanSupplier supplier) {
        showVehicleHealth = supplier;
        return this;
    }

    @Info(value = "Sets the custom behavior for making the entity invisible or visible")
    public BaseEntityBuilder<T> setInvisible(Consumer<Boolean> consumer) {
        setInvisible = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for setting the entity's air supply")
    public BaseEntityBuilder<T> setAirSupply(IntConsumer consumer) {
        setAirSupply = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for setting the number of ticks the entity is frozen")
    public BaseEntityBuilder<T> setTicksFrozen(IntConsumer consumer) {
        setTicksFrozen = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for when the entity is hit by lightning")
    public BaseEntityBuilder<T> thunderHit(Consumer<ThunderHitContext> consumer) {
        thunderHit = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior when the entity gets stuck in a block")
    public BaseEntityBuilder<T> makeStuckInBlock(Consumer<StuckInBlockContext> consumer) {
        makeStuckInBlock = consumer;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity is invulnerable to a specific damage source")
    public BaseEntityBuilder<T> isInvulnerableTo(Predicate<DamageSource> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }

    @Info(value = "Sets whether the entity is invulnerable or not")
    public BaseEntityBuilder<T> setInvulnerable(Consumer<Boolean> consumer) {
        setInvulnerable = consumer;
        return this;
    }

    @Info(value = "Sets whether the entity can change dimensions")
    public BaseEntityBuilder<T> canChangeDimensions(Supplier<Boolean> supplier) {
        canChangeDimensions = supplier;
        return this;
    }

    @Info(value = "Sets the custom name of the entity")
    public BaseEntityBuilder<T> setCustomName(Consumer<Optional<Component>> consumer) {
        setCustomName = consumer;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity may interact with the specified block position")
    public BaseEntityBuilder<T> mayInteract(BiPredicate<Level, BlockPos> predicate) {
        mayInteract = predicate;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity can trample the specified block state at the given position with the given fall distance")
    public BaseEntityBuilder<T> canTrample(TriPredicate<BlockState, BlockPos, Float> predicate) {
        canTrample = predicate;
        return this;
    }

    @Info(value = "Sets the custom behavior for when the entity is removed from the world")
    public BaseEntityBuilder<T> onRemovedFromWorld(Consumer<T> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }

    //STUFF
    public BaseEntityBuilder<T> brainProvider(Consumer<BrainProviderBuilder> brainProvider) {
        brainProviderBuilder = new BrainProviderBuilder(id);
        brainProvider.accept(brainProviderBuilder);
        // ConsoleJS.STARTUP.error(brainProviderBuilder);
        return this;
    }

    public BaseEntityBuilder<T> brainBuilder(Consumer<BrainBuilder> brainBuilder) {
        this.brainBuilder = brainBuilder;
        return this;
    }

    @Info(value = "Adds a new AnimationController to the entity", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "predicate", value = "The predicate for the controller, determines if an animation should continue or not")
    })
    public BaseEntityBuilder<T> addAnimationController(String name, int translationTicksLength, IAnimationPredicateJS<T> predicate) {
        return addAnimationController(name, translationTicksLength, EasingType.CUSTOM, predicate, null, null, null);
    }

    @Info(value = "Adds a new AnimationController to the entity, with the ability to specify the easing type and add event listeners", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "easingType", value = "The easing type used by the animation controller"),
            @Param(name = "predicate", value = "The predicate for the controller, determines if an animation should continue or not"),
            @Param(name = "soundListener", value = "A sound listener, used to execute actions when the json requests a sound to play. May be null"),
            @Param(name = "particleListener", value = "A particle listener, used to execute actions when the json requests a particle. May be null"),
            @Param(name = "instructionListener", value = "A custom instruction listener, used to execute actions based on arbitrary instructions provided by the json. May be null")
    })
    public BaseEntityBuilder<T> addAnimationController(
            String name,
            int translationTicksLength,
            EasingType easingType,
            IAnimationPredicateJS<T> predicate,
            @Nullable ISoundListenerJS<T> soundListener,
            @Nullable IParticleListenerJS<T> particleListener,
            @Nullable ICustomInstructionListenerJS<T> instructionListener
    ) {
        animationSuppliers.add(new AnimationControllerSupplier<>(name, translationTicksLength, easingType, predicate, soundListener, particleListener, instructionListener));
        return this;
    }

    public BaseEntityBuilder<T> setRenderType(RenderType type) {
        renderType = type;
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

    /**
     * <strong>Do not</strong> override unless you are creating a custom entity type builder<br><br>
     * <p>
     * See: {@link #factory()}
     */
    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilderJS<>(this).get();
    }

    /**
     * This is the method which should be overrriden to create new type, a typical implementation looks like
     * {@code (type, level) -> new <CustomEntityClass>(this, type, level)}. See {@link BaseEntityJSBuilder#factory()}
     * and {@link BaseEntityJS} for examples.<br><br>
     * <p>
     * Unlike most builder types, there is little need to override {@link #createObject()} due to entity types being
     * essentially a supplier for the class.
     *
     * @return The {@link EntityType.EntityFactory} that is used by the {@link EntityType} this builder creates
     */
    @HideFromJS
    abstract public EntityType.EntityFactory<T> factory();

    /**
     * Used to retrieve the entity type's attributes. Implementors are encouraged to first
     * create a {@link AttributeSupplier.Builder} from a static method in the base class
     * (i.e. {@link BaseEntityJS#createLivingAttributes()}) and then apply the {@code attributes}
     * consumer to that before returning it
     *
     * @return The {@link AttributeSupplier.Builder} that will be built during Forge's EntityAttributeCreationEvent
     */
    @HideFromJS
    abstract public AttributeSupplier.Builder getAttributeBuilder();

    /**
     * A 'supplier' for an {@link AnimationController} that does not require a reference to the entity being animated
     *
     * @param name                   The name of the AnimationController that this builds
     * @param translationTicksLength The number of ticks it takes to transition between animations
     * @param predicate              The {@link IAnimationPredicateJS script-friendly} animation predicate
     */
    public record AnimationControllerSupplier<E extends LivingEntity & IAnimatableJS>(
            String name,
            int translationTicksLength,
            EasingType easingType,
            IAnimationPredicateJS<E> predicate,
            @Nullable ISoundListenerJS<E> soundListener,

            @Nullable IParticleListenerJS<E> particleListener,
            @Nullable ICustomInstructionListenerJS<E> instructionListener
    ) {
        public AnimationController<E> get(E entity) {
            final AnimationController<E> controller = new AnimationController<>(entity, name, translationTicksLength, easingType, predicate.toGecko());
            if (soundListener != null) {
                controller.registerSoundListener(event -> soundListener.playSound(new SoundKeyFrameEventJS<>(event)));
            }
            if (particleListener != null) {
                controller.registerParticleListener(event -> particleListener.summonParticle(new ParticleKeyFrameEventJS<>(event)));
            }
            if (instructionListener != null) {
                controller.registerCustomInstructionListener(event -> instructionListener.executeInstruction(new CustomInstructionKeyframeEventJS<>(event)));
            }
            return controller;
        }
    }

    // Wrappers around geckolib things that allow script writers to know what they're doing

    /**
     * A wrapper around {@link software.bernie.geckolib3.core.controller.AnimationController.IAnimationPredicate IAnimationPredicate}
     * that is easier to work with in js
     */
    @FunctionalInterface
    public interface IAnimationPredicateJS<E extends LivingEntity & IAnimatableJS> {

        @Info(value = "Determines if an animation should continue for a given AnimationEvent. Return true to continue the current animation", params = {
                @Param(name = "event", value = "The AnimationEvent, provides values that can be used to determine if the animation should continue or not")
        })
        boolean test(AnimationEventJS<E> event);

        default AnimationController.IAnimationPredicate<E> toGecko() {

            return event -> {
                if (event != null) {
                    AnimationEventJS<E> animationEventJS = new AnimationEventJS<>(event);
                    try {
                        if (animationEventJS == null) return PlayState.STOP;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return test(animationEventJS) ? PlayState.CONTINUE : PlayState.STOP;

                } else {
                    ConsoleJS.STARTUP.error("AnimationEventJS was null in IAnimationPredicateJS.toGecko()");
                    return PlayState.STOP;
                }
            };
        }
    }


    /**
     * A simple wrapper around a {@link AnimationEvent} that restricts access to certain things
     * and adds {@link @Info} annotations for script writers
     *
     * @param <E> The entity being animated in the event
     */
    public static class AnimationEventJS<E extends LivingEntity & IAnimatableJS> {

        private final AnimationEvent<E> parent;

        public AnimationEventJS(AnimationEvent<E> parent) {
            this.parent = parent;
        }

        @Info(value = "Returns the number of ticks the entity has been animating for")
        public double getAnimationTick() {
            return parent.getAnimationTick();
        }

        @Info(value = "Returns the entity that is being animated")
        public E getEntity() {
            return parent.getAnimatable();
        }

        // ?
        public float getLimbSwing() {
            return parent.getLimbSwing();
        }

        // ?
        public float getLimbSwingAmount() {
            return parent.getLimbSwingAmount();
        }

        @Info(value = "Returns a number, in the range [0, 1], how far through the tick it currently is")
        public float getPartialTick() {
            return parent.getPartialTick();
        }

        @Info(value = "If the entity is moving")
        public boolean isMoving() {
            return parent.isMoving();
        }

        @Info(value = "Returns the animation controller this event is part of")
        public AnimationController<E> getController() {
            return parent.getController();
        }

        @Info(value = "Adds a new Animation Builder to the AnimationController")
        @Generics(value = AnimationBuilder.class)
        public void addAnimations(Consumer<AnimationBuilder> builder) {
            final AnimationBuilder animationBuilder = new AnimationBuilder();
            builder.accept(animationBuilder);
            parent.getController().setAnimation(animationBuilder);
        }

        @Info(value = """
                Returns any extra data that the event may have
                                
                Usually used by armor animations to know what item is worn
                """)
        public List<Object> getExtraData() {
            return parent.getExtraData();
        }

        @Info(value = "Returns the extra data that is of the provided class")
        public <D> List<D> getExtraDataOfType(Class<D> type) {
            return parent.getExtraDataOfType(type);
        }
    }

    public static class KeyFrameEventJS<E extends LivingEntity & IAnimatableJS> {

        @Info(value = "The amount of ticks that have passed in either the current transition or animation, depending on the controller's AnimationState")
        public final double animationTick;
        @Info(value = "The entity being animated")
        public final E entity;

        protected KeyFrameEventJS(KeyframeEvent<E> parent) {
            animationTick = parent.getAnimationTick();
            entity = parent.getEntity();
        }
    }

    @FunctionalInterface
    public interface ISoundListenerJS<E extends LivingEntity & IAnimatableJS> {
        void playSound(SoundKeyFrameEventJS<E> event);
    }

    public static class SoundKeyFrameEventJS<E extends LivingEntity & IAnimatableJS> extends KeyFrameEventJS<E> {

        @Info(value = "The name of the sound to play")
        public final String sound;

        public SoundKeyFrameEventJS(SoundKeyframeEvent<E> parent) {
            super(parent);
            sound = parent.sound;
        }
    }

    @FunctionalInterface
    public interface IParticleListenerJS<E extends LivingEntity & IAnimatableJS> {
        void summonParticle(ParticleKeyFrameEventJS<E> event);
    }

    public static class ParticleKeyFrameEventJS<E extends LivingEntity & IAnimatableJS> extends KeyFrameEventJS<E> {

        // These aren't documented in geckolib, so I have no idea what they are
        public final String effect;
        public final String locator;
        public final String script;

        public ParticleKeyFrameEventJS(ParticleKeyFrameEvent<E> parent) {
            super(parent);
            effect = parent.effect;
            locator = parent.locator;
            script = parent.script;
        }
    }

    @FunctionalInterface
    public interface ICustomInstructionListenerJS<E extends LivingEntity & IAnimatableJS> {
        void executeInstruction(CustomInstructionKeyframeEventJS<E> event);
    }

    public static class CustomInstructionKeyframeEventJS<E extends LivingEntity & IAnimatableJS> extends KeyFrameEventJS<E> {

        @Info(value = "A list of all the custom instructions. In blockbench, each line in the custom instruction box is a separate instruction.")
        public final String instructions;

        public CustomInstructionKeyframeEventJS(CustomInstructionKeyframeEvent<E> parent) {
            super(parent);
            instructions = parent.instructions;
        }
    }

    public enum RenderType {
        SOLID,
        CUTOUT,
        TRANSLUCENT
    }
}
