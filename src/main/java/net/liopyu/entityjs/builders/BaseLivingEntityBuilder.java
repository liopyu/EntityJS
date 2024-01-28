package net.liopyu.entityjs.builders;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.JsonIO;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.*;
import net.minecraft.BlockUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.TriPredicate;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;
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
 * The base builder for all Living Entity types that EntityJS can handle, has methods to allow overriding
 * nearly every method available in {@link LivingEntity}. Implementors are free to use as many or few
 * of these as they wish
 *
 * @param <T> The entity class that the built entity type is for, this should be a custom class
 *            that extends {@link LivingEntity} or a subclass and {@link IAnimatableJS}
 */
@SuppressWarnings("unused")
public abstract class BaseLivingEntityBuilder<T extends LivingEntity & IAnimatableJS> extends BuilderBase<EntityType<T>> {

    public static final List<BaseLivingEntityBuilder<?>> thisList = new ArrayList<>();

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
    public transient List<BlockState> shouldRemoveSoulSpeed;
    public transient HumanoidArm mainArm;
    public transient boolean hasInventory;
    public transient Consumer<T> dropExperienceHandler;
    public transient boolean onSoulSpeedBlock;

    public transient Function<LootContext.Builder, LootContext.Builder> customLootContextBuilder;
    public transient Consumer<LivingEntity> doAutoAttackOnTouch;

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

    public transient Function<AnimalEntityJS, Integer> customExperienceReward;
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
    public SpawnPlacements.Type placementType;
    public Heightmap.Types heightMap;
    public SpawnPlacements.SpawnPredicate<? extends Entity> spawnPredicate;

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
    public transient Function<MobInteractContext, @Nullable InteractionResult> interact;
    public transient BiPredicate<BlockPos, BlockState> isColliding;

    public transient Predicate<String> addTag;
    public transient Consumer<T> onClientRemoval;
    public transient Consumer<T> onAddedToWorld;
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
    private transient int biomeSpawnsCount;
    public static final Map<ResourceLocation, String> spawnsBiomeModifiers = new HashMap<>();
    public static final List<BaseLivingEntityBuilder<?>> spawnList = new ArrayList<>();

    //STUFF
    public BaseLivingEntityBuilder(ResourceLocation i) {
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
        textureResource = t -> t.getBuilder().newID("textures/models/entity/", ".png");
        animationResource = t -> t.getBuilder().newID("animations/", ".animation.json");
        isPushable = true;
        isAttackable = true;
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
        isFlapping = false;
        getDeathSound = SoundEvents.BUCKET_EMPTY;
        getSwimSound = SoundEvents.MOOSHROOM_SHEAR;
        renderType = RenderType.CUTOUT;
        getSwimHighSpeedSplashSound = SoundEvents.MOOSHROOM_SHEAR;
        mainArm = HumanoidArm.RIGHT;
        canBreatheUnderwater = false;
        passengerPredicate = entity -> true;
        biomeSpawnsCount = 0;
    }

    @Info(value = "Sets the main arm of the entity, defaults to 'right'")
    public BaseLivingEntityBuilder<T> mainArm(HumanoidArm arm) {
        mainArm = arm;
        return this;
    }

    @Info(value = "Sets the hit box of the entity type", params = {
            @Param(name = "width", value = "The width of the entity, defaults to 1"),
            @Param(name = "height", value = "The height if the entity, defaults to 1")
    })
    public BaseLivingEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Info(value = "Determines if the entity should serialize its data, defaults to true")
    public BaseLivingEntityBuilder<T> saves(boolean b) {
        save = b;
        return this;
    }

    @Info(value = "Determines if the entity is immune to fire, defaults to false")
    public BaseLivingEntityBuilder<T> fireImmune(boolean b) {
        fireImmune = b;
        return this;
    }

    @Info(value = "Determines the blocks the entity is 'immune' to")
    public BaseLivingEntityBuilder<T> immuneTo(ResourceLocation... blocks) {
        this.immuneTo = blocks;
        return this;
    }

    @Info(value = "Determines if the entity can spawn far from players")
    public BaseLivingEntityBuilder<T> canSpawnFarFromPlayer(boolean b) {
        spawnFarFromPlayer = b;
        return this;
    }

    @Info(value = "Sets the client tracking range, defaults to 5")
    public BaseLivingEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public BaseLivingEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public BaseLivingEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }

    @Info(value = """
            Sets how the model of the entity is determined, has access to the entity
            to allow changing the model based on info about the entity
                      
            Defaults to returning <namespace>:geo/<path>.geo.json
            """)
    public BaseLivingEntityBuilder<T> modelResourceFunction(Function<T, ResourceLocation> function) {
        modelResource = function;
        return this;
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                      
            Defaults to returning <namespace>:textures/model/entity/<path>.png
            """)
    public BaseLivingEntityBuilder<T> textureResourceFunction(Function<T, ResourceLocation> function) {
        textureResource = function;
        return this;
    }

    @Info(value = """
            Sets how the animations of the entity is determined, has access to the entity
            to allow changing the animations based on info about the entity
                      
            Defaults to returning <namespace>:animations/<path>.animation.json
            """)
    public BaseLivingEntityBuilder<T> animationResourceFunction(Function<T, ResourceLocation> function) {
        animationResource = function;
        return this;
    }

    @Info(value = """
            Sets the isPushable property in the builder.
                        
            " +
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> isPushable(boolean b) {
        isPushable = b;
        return this;
    }

    @Info(value = """
            Sets the isAttackable property in the builder.
                        
            " +
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }



    public BiFunction<Float, Float, Integer> calculateFallDamage;
    public Predicate<Entity> passengerPredicate;
    public Predicate<LivingEntity> livingpassengerPredicate;

    @Info(value = """
            Sets the passenger predicate in the builder.
                        
            " +
            "Defaults to allowing any entity to be a passenger.
            """)
    public BaseLivingEntityBuilder<T> setCanAddPassenger(Predicate<Entity> predicate) {
        passengerPredicate = predicate;
        return this;
    }

    @Info(value = """
            Sets whether the entity is affected by fluids in the builder.
                        
            " +
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> isAffectedByFluids(boolean b) {
        isAffectedByFluids = b;
        return this;
    }

    @Info(value = """
            Sets the summonable property in the builder.
                        
            " +
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
    }

    @Info(value = """
            Sets the immobility property in the builder.
                        
            " +
            "Defaults to false.
            """)
    public BaseLivingEntityBuilder<T> isImmobile(boolean b) {
        isImmobile = b;
        return this;
    }

    @Info(value = """
            Sets the always experience dropper property in the builder.
                        
            " +
            "Defaults to false.
            """)
    public BaseLivingEntityBuilder<T> isAlwaysExperienceDropper(boolean b) {
        isAlwaysExperienceDropper = b;
        return this;
    }

    @Info(value = """
            Sets the fall damage calculation function in the builder.
            """)
    public BaseLivingEntityBuilder<T> calculateFallDamage(BiFunction<Float, Float, Integer> calculation) {
        calculateFallDamage = calculation;
        return this;
    }

    @Info(value = """
            Sets the death sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getDeathSound(SoundEvent sound) {
        setDeathSound = sound;
        return this;
    }

    @Info(value = """
            Sets the swim sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getSwimSound(SoundEvent sound) {
        setSwimSound = sound;
        return this;
    }

    @Info(value = """
            Sets the swim high-speed splash sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getSwimHighSpeedSplashSound(SoundEvent sound) {
        getSwimHighSpeedSplashSound = sound;
        return this;
    }

    @Info(value = """
            Sets the block speed factor for the entity in the builder.
                        
            " +
            "Defaults to 0.
            """)
    public BaseLivingEntityBuilder<T> blockSpeedFactor(int i) {
        blockSpeedFactor = t -> i;
        return this;
    }

    @Info(value = """
            Sets the block speed factor function for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> blockSpeedFactor(Function<T, Integer> function) {
        blockSpeedFactor = function;
        return this;
    }

    @Info(value = """
            Sets the flapping property for the entity in the builder.
                        
            " +
            "Defaults to false.
            """)
    public BaseLivingEntityBuilder<T> isFlapping(boolean b) {
        isFlapping = b;
        return this;
    }

    @Info(value = """
            Sets a custom tick callback for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tick(Consumer<T> tickCallback) {
        tick = tickCallback;
        return this;
    }


    public transient Consumer<T> tick = t -> {
    };

    @Info(value = "Sets the custom onAddedToWorld behavior")
    public BaseLivingEntityBuilder<T> onAddedToWorld(Consumer<T> onAddedToWorldCallback) {
        onAddedToWorld = onAddedToWorldCallback;
        return this;
    }

    public BaseLivingEntityBuilder<T> getType(EntityType<T> type) {
        getType = type;
        return this;
    }


    @Info(value = """
            Sets the custom bounding box for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> boundingBox(AABB boundingBox) {
        this.customBoundingBox = boundingBox;
        return this;
    }

    @Info(value = """
            Sets the custom loot context builder for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> lootContextBuilder(Function<LootContext.Builder, LootContext.Builder> customLootContextBuilder) {
        this.customLootContextBuilder = customLootContextBuilder;
        return this;
    }

    @Info(value = """
            Sets a custom behavior for auto-attacking on touch for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> doAutoAttackOnTouch(Consumer<LivingEntity> doAutoAttackOnTouch) {
        this.doAutoAttackOnTouch = doAutoAttackOnTouch;
        return this;
    }


    @Info(value = """
            Sets the function to determine the custom standing eye height for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getStandingEyeHeight(Function<Pose, Float> customGetStandingEyeHeight) {
        this.customGetStandingEyeHeight = customGetStandingEyeHeight;
        return this;
    }

    @Info(value = """
            Sets the function to determine the custom BlockPos below that affects the entity's movement in the builder.
            """)
    public BaseLivingEntityBuilder<T> getBlockPosBelowThatAffectsMyMovement(Function<BlockPos, BlockPos> customGetBlockPosBelow) {
        this.customGetBlockPosBelow = customGetBlockPosBelow;
        return this;
    }

    @Info(value = """
            Sets the function to determine the custom decrease in air supply for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> decreaseAirSupply(Function<Integer, Integer> customDecreaseAirSupply) {
        this.customDecreaseAirSupply = customDecreaseAirSupply;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for when the entity is blocked by a shield in the builder.
            """)
    public BaseLivingEntityBuilder<T> blockedByShield(Consumer<LivingEntity> customBlockedByShield) {
        this.customBlockedByShield = customBlockedByShield;
        return this;
    }

    @Info(value = """
            Sets the function to custom tick head turn for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tickHeadTurn(BiFunction<Float, Float, Float> customTickHeadTurn) {
        this.customTickHeadTurn = customTickHeadTurn;
        return this;
    }

    @Info(value = """
            Sets the predicate to determine whether the entity emits an equip event for each equipment slot in the builder.
            """)
    public BaseLivingEntityBuilder<T> doesEmitEquipEvent(Predicate<EquipmentSlot> customDoesEmitEquipEvent) {
        this.customDoesEmitEquipEvent = customDoesEmitEquipEvent;
        return this;
    }

    @Info(value = """
            Sets the function to determine the custom bounding box for a pose in the builder.
            """)
    public BaseLivingEntityBuilder<T> getBoundingBoxForPose(Function<Pose, AABB> customGetBoundingBoxForPose) {
        this.customGetBoundingBoxForPose = customGetBoundingBoxForPose;
        return this;
    }

    @Info(value = """
            Sets the predicate to determine whether the entity can enter a specific pose in the builder.
            """)
    public BaseLivingEntityBuilder<T> canEnterPose(Predicate<Pose> customCanEnterPose) {
        this.customCanEnterPose = customCanEnterPose;
        return this;
    }

    @Info(value = """
            Sets the function to get the shared flag for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getSharedFlag(IntFunction<Boolean> customGetSharedFlag) {
        this.customGetSharedFlag = customGetSharedFlag;
        return this;
    }

    @Info(value = """
            Sets the predicate to determine whether there is a minor horizontal collision in the builder.
            """)
    public BaseLivingEntityBuilder<T> isHorizontalCollisionMinor(Predicate<Vec3> customIsHorizontalCollisionMinor) {
        this.customIsHorizontalCollisionMinor = customIsHorizontalCollisionMinor;
        return this;
    }

    @Info(value = """
            Sets the supplier to determine whether the entity should be repositioned after load in the builder.
            """)
    public BaseLivingEntityBuilder<T> repositionEntityAfterLoad(BooleanSupplier customRepositionEntityAfterLoad) {
        this.customRepositionEntityAfterLoad = customRepositionEntityAfterLoad;
        return this;
    }

    @Info(value = """
            Sets the supplier to determine whether the entity should update its in-water state and perform fluid pushing in the builder.
            """)
    public BaseLivingEntityBuilder<T> updateInWaterStateAndDoFluidPushing(BooleanSupplier customUpdateInWaterStateAndDoFluidPushing) {
        this.customUpdateInWaterStateAndDoFluidPushing = customUpdateInWaterStateAndDoFluidPushing;
        return this;
    }

    @Info(value = """
            Sets the function to get the custom type name for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getTypeName(Function<LivingEntity, Component> customGetTypeName) {
        this.customGetTypeName = customGetTypeName;
        return this;
    }

    @Info(value = """
            Sets the function to get the damage after armor absorb for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getDamageAfterArmorAbsorb(BiFunction<DamageSource, Float, Float> customGetDamageAfterArmorAbsorb) {
        this.customGetDamageAfterArmorAbsorb = customGetDamageAfterArmorAbsorb;
        return this;
    }


    @Info(value = """
            Sets the function to get the damage after magic absorb for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getDamageAfterMagicAbsorb(BiFunction<DamageSource, Float, Float> customGetDamageAfterMagicAbsorb) {
        this.customGetDamageAfterMagicAbsorb = customGetDamageAfterMagicAbsorb;
        return this;
    }

    @Info(value = """
            Sets the function to determine the next step for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> nextStep(Function<Float, Float> customNextStep) {
        this.customNextStep = customNextStep;
        return this;
    }

    @Info(value = """
            Sets the function to create a custom hover event for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> createHoverEvent(Function<HoverEvent, HoverEvent> customCreateHoverEvent) {
        this.customCreateHoverEvent = customCreateHoverEvent;
        return this;
    }

    @Info(value = """
            Sets the function to get the custom permission level for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getPermissionLevel(Function<Integer, Integer> customGetPermissionLevel) {
        this.customGetPermissionLevel = customGetPermissionLevel;
        return this;
    }

    @Info(value = """
            Sets the function to increase the air supply for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> increaseAirSupply(Function<Integer, Integer> customIncreaseAirSupply) {
        this.customIncreaseAirSupply = customIncreaseAirSupply;
        return this;
    }

    @Info(value = """
            Sets the function to create a new double list for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> newDoubleList(Function<double[], ListTag> customNewDoubleList) {
        this.customNewDoubleList = customNewDoubleList;
        return this;
    }

    @Info(value = """
            Sets the function to create a new float list for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> newFloatList(Function<float[], ListTag> customNewFloatList) {
        this.customNewFloatList = customNewFloatList;
        return this;
    }

    @Info(value = """
            Sets the function to get the movement emission for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getMovementEmission(Function<Entity.MovementEmission, Entity.MovementEmission> customGetMovementEmission) {
        this.customGetMovementEmission = customGetMovementEmission;
        return this;
    }

    @Info(value = """
            Sets the function to get the exit portal information for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getExitPortal(Function<ExitPortalInfo, Optional<BlockUtil.FoundRectangle>> customGetExitPortal) {
        this.customGetExitPortal = customGetExitPortal;
        return this;
    }

    @Info(value = """
            Sets the function to get the drinking sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getDrinkingSound(Function<ItemStack, SoundEvent> customGetDrinkingSound) {
        this.customGetDrinkingSound = customGetDrinkingSound;
        return this;
    }

    @Info(value = """
            Sets the function to get the hurt sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getHurtSound(Function<DamageSource, SoundEvent> customGetHurtSound) {
        this.customGetHurtSound = customGetHurtSound;
        return this;
    }

    @Info(value = """
            Sets the function to find the dimension entry point for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> findDimensionEntryPoint(Function<ServerLevel, PortalInfo> customFindDimensionEntryPoint) {
        this.customFindDimensionEntryPoint = customFindDimensionEntryPoint;
        return this;
    }

    @Info(value = """
            Sets the function to get the swim splash sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getSwimSplashSound(Function<SoundEvent, SoundEvent> customGetSwimSplashSound) {
        this.customGetSwimSplashSound = customGetSwimSplashSound;
        return this;
    }

    @Info(value = """
            Sets the function to get the relative portal position for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getRelativePortalPosition(BiFunction<Direction.Axis, BlockUtil.FoundRectangle, Vec3> customGetRelativePortalPosition) {
        this.customGetRelativePortalPosition = customGetRelativePortalPosition;
        return this;
    }

    @Info(value = """
            Sets the function to limit piston movement for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> limitPistonMovement(Function<Vec3, Vec3> customLimitPistonMovement) {
        this.customLimitPistonMovement = customLimitPistonMovement;
        return this;
    }

    @Info(value = """
            Sets the function to maybe back off from the edge for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> maybeBackOffFromEdge(BiFunction<Vec3, MoverType, Vec3> customMaybeBackOffFromEdge) {
        this.customMaybeBackOffFromEdge = customMaybeBackOffFromEdge;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom actually hurt behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> actuallyHurt(BiConsumer<DamageSource, Float> customActuallyHurt) {
        this.customActuallyHurt = customActuallyHurt;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom block using shield behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> blockUsingShield(Consumer<LivingEntity> customBlockUsingShield) {
        this.customBlockUsingShield = customBlockUsingShield;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom check auto-spin attack behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> checkAutoSpinAttack(BiConsumer<AABB, AABB> customCheckAutoSpinAttack) {
        this.customCheckAutoSpinAttack = customCheckAutoSpinAttack;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom check fall damage behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> customCheckFallDamage(BiConsumer<Double, ResourceLocation> customCheckFallDamage) {
        this.customCheckFallDamage = customCheckFallDamage;
        return this;
    }

    @Info(value = """
            Applies the custom check fall damage behavior for the entity with the provided fall distance and block location.
            """)
    public void applyCustomCheckFallDamage(double fallDistance, ResourceLocation blockLocation) {
        if (customCheckFallDamage != null) {
            customCheckFallDamage.accept(fallDistance, blockLocation);
        }
    }


    @Info(value = """
            Sets the consumer for the custom kill behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> kill(Consumer<T> kill) {
        this.kill = kill;
        return this;
    }

    @Info(value = """
            Sets the function to determine if the entity can attack a specific type in the builder.
            """)
    public BaseLivingEntityBuilder<T> canAttackType(Function<EntityType<?>, Boolean> customCanAttackType) {
        this.customCanAttackType = customCanAttackType;
        return this;
    }

    @Info(value = """
            Sets the function to get the swim amount for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> swimAmount(Function<Float, Float> customGetSwimAmount) {
        this.customGetSwimAmount = customGetSwimAmount;
        return this;
    }

    @Info(value = """
            Sets whether the entity can spawn Soul Speed particles in the builder.
            """)
    public BaseLivingEntityBuilder<T> canSpawnSoulSpeedParticle(Boolean canSpawn) {
        this.canSpawnSoulSpeedParticle = canSpawn;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for spawning Soul Speed particles for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> spawnSoulSpeedParticle(Runnable customSpawnSoulSpeedParticle) {
        this.customSpawnSoulSpeedParticle = customSpawnSoulSpeedParticle;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for removing Soul Speed for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> removeSoulSpeed(Runnable customRemoveSoulSpeed) {
        this.customRemoveSoulSpeed = customRemoveSoulSpeed;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for trying to add Soul Speed for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tryAddSoulSpeed(Runnable customTryAddSoulSpeed) {
        this.customTryAddSoulSpeed = customTryAddSoulSpeed;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for removing Frost for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> removeFrost(Runnable customRemoveFrost) {
        this.customRemoveFrost = customRemoveFrost;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for trying to add Frost for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tryAddFrost(Runnable customTryAddFrost) {
        this.customTryAddFrost = customTryAddFrost;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom onChangedBlock behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> onChangedBlock(Consumer<BlockPos> customOnChangedBlock) {
        this.customOnChangedBlock = customOnChangedBlock;
        return this;
    }

    @Info(value = """
            Sets the custom scale for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> scale(float customScale) {
        this.customScale = customScale;
        return this;
    }

    @Info(value = """
            Sets whether the entity is rideable underwater in the builder.
            """)
    public BaseLivingEntityBuilder<T> rideableUnderWater(Boolean rideableUnderWater) {
        this.rideableUnderWater = rideableUnderWater;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom tickDeath behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tickDeath(Consumer<T> tickDeath) {
        this.tickDeath = tickDeath;
        return this;
    }

    @Info(value = """
            Sets whether the entity should drop experience in the builder.
            """)
    public BaseLivingEntityBuilder<T> shouldDropExperience(boolean b) {
        this.shouldDropExperience = b;
        return this;
    }

    @Info(value = """
            Sets the function to get the custom experience reward for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> experienceReward(Function<AnimalEntityJS, Integer> customExperienceReward) {
        this.customExperienceReward = customExperienceReward;
        return this;
    }

    @Info(value = """
            Sets the supplier for determining whether friction should be discarded for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> shouldDiscardFriction(Supplier<Boolean> customShouldDiscardFriction) {
        this.customShouldDiscardFriction = customShouldDiscardFriction;
        return this;
    }


    @Info(value = """
            Sets the consumer for the custom setDiscardFriction behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> setDiscardFriction(Consumer<Boolean> customSetDiscardFriction) {
        this.customSetDiscardFriction = customSetDiscardFriction;
        return this;
    }

    @Info(value = """
            Sets the tri-consumer for the custom onEquipItem behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> onEquipItem(TriConsumer<EquipmentSlot, ItemStack, ItemStack> customOnEquipItem) {
        this.customOnEquipItem = customOnEquipItem;
        return this;
    }

    @Info(value = """
            Sets the consumer for the custom playEquipSound behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> playEquipSound(Consumer<ItemStack> customPlayEquipSound) {
        this.customPlayEquipSound = customPlayEquipSound;
        return this;
    }

    @Info(value = """
            Sets the runnable for the custom tickEffects behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tickEffects(Runnable customTickEffects) {
        this.customTickEffects = customTickEffects;
        return this;
    }

    @Info(value = """
            Sets the runnable for the custom updateInvisibilityStatus behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> updateInvisibilityStatus(Runnable customUpdateInvisibilityStatus) {
        this.customUpdateInvisibilityStatus = customUpdateInvisibilityStatus;
        return this;
    }

    @Info(value = """
            Sets the function for getting the visibility percent for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> getVisibilityPercent(Function<Entity, Double> customGetVisibilityPercent) {
        this.customGetVisibilityPercent = customGetVisibilityPercent;
        return this;
    }

    @Info(value = """
            Sets the predicate for the custom canAttack behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> canAttack(Predicate<LivingEntity> customCanAttack) {
        this.customCanAttack = customCanAttack;
        return this;
    }

    @Info(value = """
            Sets the bi-predicate for the custom canAttackWithConditions behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> canAttackWithConditions(BiPredicate<LivingEntity, TargetingConditions> customCanAttack) {
        this.customCanAttackWithConditions = customCanAttack;
        return this;
    }

    @Info(value = """
            Sets the boolean supplier for the custom canBeSeenAsEnemy behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> canBeSeenAsEnemy(BooleanSupplier customCanBeSeenAsEnemy) {
        this.customCanBeSeenAsEnemy = customCanBeSeenAsEnemy;
        return this;
    }

    @Info(value = """
            Sets the boolean supplier for the custom canBeSeenByAnyone behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> canBeSeenByAnyone(BooleanSupplier customCanBeSeenByAnyone) {
        this.customCanBeSeenByAnyone = customCanBeSeenByAnyone;
        return this;
    }

    @Info(value = """
            Sets the runnable for the custom removeEffectParticles behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> removeEffectParticles(Runnable customRemoveEffectParticles) {
        this.customRemoveEffectParticles = customRemoveEffectParticles;
        return this;
    }

    @Info(value = """
            Sets the predicate for the custom removeAllEffects behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> removeAllEffects(Predicate<Boolean> customRemoveAllEffects) {
        this.customRemoveAllEffects = customRemoveAllEffects;
        return this;
    }

    @Info(value = """
            Sets the bi-predicate for the custom addEffect behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> addEffect(BiPredicate<MobEffectInstance, Entity> customAddEffect) {
        this.customAddEffect = customAddEffect;
        return this;
    }


    @Info(value = "Sets the custom logic to determine if the entity can be affected by a specific potion effect.")
    public BaseLivingEntityBuilder<T> customCanBeAffected(Predicate<MobEffectInstance> predicate) {
        canBeAffectedPredicate = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for forcefully adding a potion effect to the entity.")
    public BaseLivingEntityBuilder<T> customForceAddEffect(BiConsumer<MobEffectInstance, @Nullable Entity> consumer) {
        forceAddEffectConsumer = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if healing and harming effects are inverted for the entity.")
    public BaseLivingEntityBuilder<T> customInvertedHealAndHarm(boolean invertedHealAndHarm) {
        this.invertedHealAndHarm = invertedHealAndHarm;
        return this;
    }


    @Info(value = "Sets the custom logic for when a potion effect is added to the entity.")
    public BaseLivingEntityBuilder<T> onEffectAdded(Consumer<OnEffectContext> consumer) {
        onEffectAdded = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for healing the entity")
    public BaseLivingEntityBuilder<T> customHeal(BiConsumer<Float, T> callback) {
        healAmount = callback;
        return this;
    }


    @Info(value = "Sets the custom logic for when a potion effect is removed from the entity.")
    public BaseLivingEntityBuilder<T> onEffectRemoved(Consumer<OnEffectContext> consumer) {
        onEffectRemoved = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for healing the entity")
    public BaseLivingEntityBuilder<T> healAmount(BiConsumer<Float, T> callback) {
        healAmount = callback;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity is dead or dying")
    public BaseLivingEntityBuilder<T> isDeadOrDying(Predicate<LivingEntity> predicate) {
        isDeadOrDying = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for handling entity damage")
    public BaseLivingEntityBuilder<T> customHurt(BiPredicate<DamageSource, Float> predicate) {
        hurtPredicate = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for providing the last damage source")
    public BaseLivingEntityBuilder<T> lastDamageSourceSupplier(Supplier<DamageSource> supplier) {
        lastDamageSourceSupplier = supplier;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the damage source is blocked")
    public BaseLivingEntityBuilder<T> isDamageSourceBlocked(Predicate<DamageSource> predicate) {
        isDamageSourceBlocked = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for when the entity dies")
    public BaseLivingEntityBuilder<T> die(Consumer<DamageSource> consumer) {
        die = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping all death loot when the entity dies")
    public BaseLivingEntityBuilder<T> dropAllDeathLoot(Consumer<DamageSource> consumer) {
        dropAllDeathLoot = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping equipment when the entity dies")
    public BaseLivingEntityBuilder<T> dropEquipment(Consumer<Void> consumer) {
        dropEquipment = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping custom death loot when the entity dies")
    public BaseLivingEntityBuilder<T> dropCustomDeathLoot(TriConsumer<DamageSource, Integer, Boolean> consumer) {
        dropCustomDeathLoot = consumer;
        return this;
    }


    @Info(value = "Sets the custom loot table for the entity")
    public BaseLivingEntityBuilder<T> lootTable(Supplier<ResourceLocation> supplier) {
        lootTable = supplier;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping items from the entity's loot table upon death")
    public BaseLivingEntityBuilder<T> dropFromLootTable(BiConsumer<DamageSource, Boolean> consumer) {
        dropFromLootTable = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for knockback effect on the entity")
    public BaseLivingEntityBuilder<T> knockback(TriConsumer<Double, Double, Double> consumer) {
        knockback = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic to skip dropping experience points upon entity death")
    public BaseLivingEntityBuilder<T> skipDropExperience(Runnable runnable) {
        skipDropExperience = runnable;
        return this;
    }


    @Info(value = "Sets the custom logic to determine if experience points were consumed upon entity death")
    public BaseLivingEntityBuilder<T> wasExperienceConsumed(Supplier<Boolean> supplier) {
        wasExperienceConsumed = supplier;
        return this;
    }


    @Info(value = "Sets the custom logic for determining fall sounds for the entity")
    public BaseLivingEntityBuilder<T> fallSounds(Function<LivingEntity.Fallsounds, LivingEntity.Fallsounds> function) {
        fallSoundsFunction = function;
        return this;
    }


    @Info(value = "Sets the custom logic for determining the eating sound for the entity")
    public BaseLivingEntityBuilder<T> eatingSound(Function<ItemStack, SoundEvent> function) {
        eatingSound = function;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity is on a climbable surface")
    public BaseLivingEntityBuilder<T> onClimbable(Predicate<LivingEntity> predicate) {
        onClimbable = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity can breathe underwater")
    public BaseLivingEntityBuilder<T> canBreatheUnderwater(boolean b) {
        canBreatheUnderwater = b;
        return this;
    }

    @Info(value = "Sets the custom logic for causing fall damage")
    public BaseLivingEntityBuilder<T> causeFallDamage(BiPredicate<Float, DamageSource> predicate) {
        causeFallDamage = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for playing block fall sound")
    public BaseLivingEntityBuilder<T> playBlockFallSound(Consumer<Void> consumer) {
        playBlockFallSound = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting armor")
    public BaseLivingEntityBuilder<T> hurtArmor(BiConsumer<DamageSource, Float> consumer) {
        hurtArmor = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting the helmet")
    public BaseLivingEntityBuilder<T> hurtHelmet(BiConsumer<DamageSource, Float> consumer) {
        hurtHelmet = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting the currently used shield")
    public BaseLivingEntityBuilder<T> hurtCurrentlyUsedShield(Consumer<Float> consumer) {
        hurtCurrentlyUsedShield = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for getting the combat tracker")
    public BaseLivingEntityBuilder<T> combatTracker(Function<CombatTracker, CombatTracker> function) {
        combatTracker = function;
        return this;
    }

    @Info(value = "Sets the custom logic for getting the kill credit entity")
    public BaseLivingEntityBuilder<T> killCredit(Function<LivingEntity, LivingEntity> function) {
        killCredit = function;
        return this;
    }

    @Info(value = "Sets the custom logic for swinging the entity's hand")
    public BaseLivingEntityBuilder<T> swingHand(Consumer<InteractionHand> consumer) {
        swingHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for swinging the entity's hand with extended parameters")
    public BaseLivingEntityBuilder<T> swingHandExtended(BiConsumer<InteractionHand, Boolean> consumer) {
        swingHandExtended = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for handling entity events")
    public BaseLivingEntityBuilder<T> handleEntityEvent(Consumer<Byte> consumer) {
        handleEntityEvent = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the item in the entity's hand")
    public BaseLivingEntityBuilder<T> setItemInHand(BiConsumer<InteractionHand, ItemStack> consumer) {
        setItemInHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's sprinting state")
    public BaseLivingEntityBuilder<T> setSprinting(Consumer<Boolean> consumer) {
        setSprinting = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for pushing another entity")
    public BaseLivingEntityBuilder<T> pushEntity(Consumer<Entity> consumer) {
        pushEntity = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity's name should be shown")
    public BaseLivingEntityBuilder<T> shouldShowName(Predicate<LivingEntity> predicate) {
        shouldShowName = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining the entity's jump boost power")
    public BaseLivingEntityBuilder<T> jumpBoostPower(DoubleSupplier supplier) {
        jumpBoostPower = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can stand on a specific fluid")
    public BaseLivingEntityBuilder<T> canStandOnFluid(Predicate<FluidState> predicate) {
        canStandOnFluid = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for entity movement")
    public BaseLivingEntityBuilder<T> travel(Consumer<Vec3> consumer) {
        travel = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for handling relative friction and calculating movement")
    public BaseLivingEntityBuilder<T> handleRelativeFrictionAndCalculateMovement(BiFunction<Vec3, Float, Vec3> function) {
        handleRelativeFrictionAndCalculateMovement = function;
        return this;
    }

    @Info(value = "Sets the custom speed for the entity")
    public BaseLivingEntityBuilder<T> setSpeed(Consumer<Float> consumer) {
        setSpeedConsumer = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for hurting a target entity")
    public BaseLivingEntityBuilder<T> doHurtTarget(BiPredicate<Entity, Boolean> predicate) {
        doHurtTarget = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is sensitive to water")
    public BaseLivingEntityBuilder<T> isSensitiveToWater(Predicate<Boolean> predicate) {
        isSensitiveToWater = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity performs auto spin attack")
    public BaseLivingEntityBuilder<T> isAutoSpinAttack(Predicate<Boolean> predicate) {
        isAutoSpinAttack = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity stops riding")
    public BaseLivingEntityBuilder<T> stopRidingCallback(Runnable callback) {
        stopRidingCallback = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity is updated while riding")
    public BaseLivingEntityBuilder<T> rideTick(Consumer<T> callback) {
        rideTick = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for lerping the entity's position and rotation.")
    public BaseLivingEntityBuilder<T> lerpTo(HeptConsumer consumer) {
        lerpTo = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for lerping the head position")
    public BaseLivingEntityBuilder<T> lerpHeadTo(BiConsumer<Float, Integer> consumer) {
        lerpHeadTo = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the jumping state")
    public BaseLivingEntityBuilder<T> setJumping(Consumer<Boolean> consumer) {
        setJumping = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity picks up an item")
    public BaseLivingEntityBuilder<T> onItemPickup(Consumer<ItemEntity> consumer) {
        onItemPickup = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity has line of sight to another entity")
    public BaseLivingEntityBuilder<T> hasLineOfSight(Predicate<Entity> predicate) {
        hasLineOfSight = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity has effective AI")
    public BaseLivingEntityBuilder<T> isEffectiveAi(Predicate<Void> predicate) {
        isEffectiveAi = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is pickable")
    public BaseLivingEntityBuilder<T> isPickable(Predicate<Void> predicate) {
        isPickable = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's head rotation on the Y-axis")
    public BaseLivingEntityBuilder<T> setYHeadRot(Consumer<Float> consumer) {
        setYHeadRot = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's body rotation on the Y-axis")
    public BaseLivingEntityBuilder<T> setYBodyRot(Consumer<Float> consumer) {
        setYBodyRot = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the entity's absorption amount")
    public BaseLivingEntityBuilder<T> setAbsorptionAmount(Consumer<Float> consumer) {
        setAbsorptionAmount = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity enters combat")
    public BaseLivingEntityBuilder<T> onEnterCombat(Runnable runnable) {
        onEnterCombat = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity leaves combat")
    public BaseLivingEntityBuilder<T> onLeaveCombat(Runnable runnable) {
        onLeaveCombat = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is using an item")
    public BaseLivingEntityBuilder<T> isUsingItem(Predicate<LivingEntity> predicate) {
        isUsingItem = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for living entity flags")
    public BaseLivingEntityBuilder<T> setLivingEntityFlag(BiConsumer<Integer, Boolean> consumer) {
        setLivingEntityFlag = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for starting to use an item")
    public BaseLivingEntityBuilder<T> startUsingItem(Consumer<InteractionHand> consumer) {
        startUsingItem = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for making the entity look at a specific position using an anchor point")
    public BaseLivingEntityBuilder<T> lookAt(BiConsumer<EntityAnchorArgument.Anchor, Vec3> consumer) {
        lookAt = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for releasing the item in use by the entity")
    public BaseLivingEntityBuilder<T> releaseUsingItem(Runnable runnable) {
        releaseUsingItem = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for stopping the use of an item by the entity")
    public BaseLivingEntityBuilder<T> stopUsingItem(Runnable runnable) {
        stopUsingItem = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is blocking")
    public BaseLivingEntityBuilder<T> isBlocking(Predicate<LivingEntity> predicate) {
        isBlocking = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is suppressing sliding down a ladder")
    public BaseLivingEntityBuilder<T> isSuppressingSlidingDownLadder(Predicate<LivingEntity> predicate) {
        isSuppressingSlidingDownLadder = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is fall flying")
    public BaseLivingEntityBuilder<T> isFallFlying(Predicate<LivingEntity> predicate) {
        isFallFlying = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is visually swimming")
    public BaseLivingEntityBuilder<T> isVisuallySwimming(Predicate<LivingEntity> predicate) {
        isVisuallySwimming = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for random teleportation of the entity")
    public BaseLivingEntityBuilder<T> randomTeleport(
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
    public BaseLivingEntityBuilder<T> isAffectedByPotions(Predicate<LivingEntity> predicate) {
        isAffectedByPotions = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is attackable")
    public BaseLivingEntityBuilder<T> attackable(Predicate<Boolean> predicate) {
        attackable = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for playing records nearby")
    public BaseLivingEntityBuilder<T> setRecordPlayingNearby(BiConsumer<BlockPos, Boolean> consumer) {
        setRecordPlayingNearby = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can take a specific item")
    public BaseLivingEntityBuilder<T> canTakeItem(Predicate<ItemStack> predicate) {
        canTakeItem = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for setting the sleeping position")
    public BaseLivingEntityBuilder<T> setSleepingPos(Consumer<BlockPos> consumer) {
        setSleepingPos = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is sleeping")
    public BaseLivingEntityBuilder<T> isSleeping(Supplier<Boolean> supplier) {
        isSleeping = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for starting sleeping at a specific position")
    public BaseLivingEntityBuilder<T> startSleeping(Consumer<BlockPos> consumer) {
        startSleeping = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for stopping sleeping")
    public BaseLivingEntityBuilder<T> stopSleeping(Runnable runnable) {
        stopSleeping = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is in a wall")
    public BaseLivingEntityBuilder<T> isInWall(Supplier<Boolean> supplier) {
        isInWall = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for eating an item in a level")
    public BaseLivingEntityBuilder<T> eat(BiFunction<Level, ItemStack, ItemStack> function) {
        eat = function;
        return this;
    }

    @Info(value = "Sets the custom logic for broadcasting a break event for an equipment slot")
    public BaseLivingEntityBuilder<T> broadcastBreakEvent(Consumer<EquipmentSlot> consumer) {
        broadcastBreakEvent = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for broadcasting a break event for an interaction hand")
    public BaseLivingEntityBuilder<T> broadcastBreakEventHand(Consumer<InteractionHand> consumer) {
        broadcastBreakEventHand = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for curing potion effects with a curative item")
    public BaseLivingEntityBuilder<T> curePotionEffects(BiPredicate<ItemStack, Boolean> predicate) {
        curePotionEffects = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the rider should face forward")
    public BaseLivingEntityBuilder<T> shouldRiderFaceForward(Predicate<Player> predicate) {
        shouldRiderFaceForward = predicate;
        return this;
    }

    @Info(value = "Sets the custom callback for invalidating capabilities")
    public BaseLivingEntityBuilder<T> invalidateCapsCallback(Runnable callback) {
        invalidateCaps = callback;
        return this;
    }

    @Info(value = "Sets the custom callback for reviving capabilities")
    public BaseLivingEntityBuilder<T> reviveCapsCallback(Runnable callback) {
        reviveCaps = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can freeze")
    public BaseLivingEntityBuilder<T> canFreezePredicate(Predicate<LivingEntity> predicate) {
        canFreeze = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is currently glowing")
    public BaseLivingEntityBuilder<T> isCurrentlyGlowing(Predicate<LivingEntity> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can disable a shield")
    public BaseLivingEntityBuilder<T> canDisableShield(Predicate<LivingEntity> predicate) {
        canDisableShield = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for living entity interaction")
    public BaseLivingEntityBuilder<T> interact(Function<MobInteractContext, @Nullable InteractionResult> f) {
        interact = f;
        return this;
    }

    @Info(value = "Sets the custom logic for how far a mob falls before taking damage")
    public BaseLivingEntityBuilder<T> getMaxFallDistance(IntSupplier i) {
        getMaxFallDistance = i;
        return this;
    }

    @Info(value = "Sets the custom isColliding behavior")
    public BaseLivingEntityBuilder<T> isColliding(BiPredicate<BlockPos, BlockState> predicate) {
        isColliding = predicate;
        return this;
    }

    @Info(value = "Sets the custom addTag behavior")
    public BaseLivingEntityBuilder<T> addTag(Predicate<String> predicate) {
        addTag = predicate;
        return this;
    }

    @Info(value = "Sets the custom onClientRemoval behavior")
    public BaseLivingEntityBuilder<T> onClientRemoval(Consumer<T> consumer) {
        onClientRemoval = consumer;
        return this;
    }


    @Info(value = "Sets the custom lavaHurt behavior")
    public BaseLivingEntityBuilder<T> lavaHurt(Consumer<T> consumer) {
        lavaHurt = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for entity flapping actions")
    public BaseLivingEntityBuilder<T> onFlap(Consumer<T> consumer) {
        onFlap = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the entity dampens vibrations")
    public BaseLivingEntityBuilder<T> dampensVibrations(BooleanSupplier supplier) {
        dampensVibrations = supplier;
        return this;
    }

    @Info(value = "Sets the custom behavior for handling player touch events")
    public BaseLivingEntityBuilder<T> playerTouch(Consumer<PlayerEntityContext> consumer) {
        playerTouch = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for picking entity hit results")
    public BaseLivingEntityBuilder<T> pick(TriFunction<Double, Float, Boolean, HitResult> function) {
        pick = function;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the vehicle health should be shown")
    public BaseLivingEntityBuilder<T> showVehicleHealth(BooleanSupplier supplier) {
        showVehicleHealth = supplier;
        return this;
    }

    @Info(value = "Sets the custom behavior for making the entity invisible or visible")
    public BaseLivingEntityBuilder<T> setInvisible(Consumer<Boolean> consumer) {
        setInvisible = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for setting the entity's air supply")
    public BaseLivingEntityBuilder<T> setAirSupply(IntConsumer consumer) {
        setAirSupply = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for setting the number of ticks the entity is frozen")
    public BaseLivingEntityBuilder<T> setTicksFrozen(IntConsumer consumer) {
        setTicksFrozen = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for when the entity is hit by lightning")
    public BaseLivingEntityBuilder<T> thunderHit(Consumer<ThunderHitContext> consumer) {
        thunderHit = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior when the entity gets stuck in a block")
    public BaseLivingEntityBuilder<T> makeStuckInBlock(Consumer<StuckInBlockContext> consumer) {
        makeStuckInBlock = consumer;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity is invulnerable to a specific damage source")
    public BaseLivingEntityBuilder<T> isInvulnerableTo(Predicate<DamageSource> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }

    @Info(value = "Sets whether the entity is invulnerable or not")
    public BaseLivingEntityBuilder<T> setInvulnerable(Consumer<Boolean> consumer) {
        setInvulnerable = consumer;
        return this;
    }

    @Info(value = "Sets whether the entity can change dimensions")
    public BaseLivingEntityBuilder<T> canChangeDimensions(Supplier<Boolean> supplier) {
        canChangeDimensions = supplier;
        return this;
    }

    @Info(value = "Sets the custom name of the entity")
    public BaseLivingEntityBuilder<T> setCustomName(Consumer<Optional<Component>> consumer) {
        setCustomName = consumer;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity may interact with the specified block position")
    public BaseLivingEntityBuilder<T> mayInteract(BiPredicate<Level, BlockPos> predicate) {
        mayInteract = predicate;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity can trample the specified block state at the given position with the given fall distance")
    public BaseLivingEntityBuilder<T> canTrample(TriPredicate<BlockState, BlockPos, Float> predicate) {
        canTrample = predicate;
        return this;
    }

    @Info(value = "Sets the custom behavior for when the entity is removed from the world")
    public BaseLivingEntityBuilder<T> onRemovedFromWorld(Consumer<T> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }

    //STUFF

    @Info(value = "Adds a new AnimationController to the entity", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "predicate", value = "The predicate for the controller, determines if an animation should continue or not")
    })
    public BaseLivingEntityBuilder<T> addAnimationController(String name, int translationTicksLength, IAnimationPredicateJS<T> predicate) {
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
    public BaseLivingEntityBuilder<T> addAnimationController(
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

    public BaseLivingEntityBuilder<T> setRenderType(RenderType type) {
        renderType = type;
        return this;
    }

    @Info(value = "Sets the spawn placement of the entity type", params = {
            @Param(name = "placementType", value = "The placement type of the spawn, accepts 'on_ground', 'in_water', 'no_restrictions', 'in_lava'"),
            @Param(name = "heightMap", value = "The height map used for the spawner"),
            @Param(name = "spawnPredicate", value = "The predicate that determines if the entity will spawn")
    })
    public BaseLivingEntityBuilder<T> spawnPlacement(SpawnPlacements.Type placementType, Heightmap.Types heightMap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
        spawnList.add(this);
        this.spawnPredicate = spawnPredicate;
        this.placementType = placementType;
        this.heightMap = heightMap;
        return this;
    }

    @Info(value = "Adds a spawner for this entity to the provided biome(s)", params = {
            @Param(name = "biomes", value = "A list of biomes that the entity should spawn in. If using a tag, only one value may be provided"),
            @Param(name = "weight", value = "The spawn weight the entity should have"),
            @Param(name = "minCount", value = "The minimum number of entities that can spawn at a time"),
            @Param(name = "maxCount", value = "The maximum number of entities that can spawn at a time")
    })
    public BaseLivingEntityBuilder<T> biomeSpawn(List<String> biomes, int weight, int minCount, int maxCount) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", "forge:add_spawns");
        if (biomes.size() == 1) {
            json.addProperty("biomes", biomes.get(0));
        } else {
            final JsonArray array = new JsonArray(biomes.size());
            biomes.forEach(array::add);
            json.add("biomes", array);
        }

        final JsonObject spawner = new JsonObject();
        spawner.addProperty("type", id.toString());
        spawner.addProperty("weight", weight);
        spawner.addProperty("minCount", minCount);
        spawner.addProperty("maxCount", maxCount);
        final JsonArray spawners = new JsonArray(1);
        spawners.add(spawner);
        json.add("spawners", spawners);
        spawnsBiomeModifiers.put(
                EntityJSMod.identifier("forge/biome_modifiers/" + id.getNamespace() + "/" + id.getPath() + "_" + biomeSpawnsCount++),
                JsonIO.toString(json)
        );
        return this;
    }

    /**
     * <strong>Do not</strong> override unless you are creating a custom entity type builder<br><br>
     * See: {@link #factory()}
     */
    @Override
    public EntityType<T> createObject() {
        return new LivingEntityTypeBuilderJS<>(this).get();
    }

    /**
     * This is the method which should be overrriden to create new type, a typical implementation looks like
     * {@code (type, level) -> new <CustomEntityClass>(this, type, level)}. See {@link AnimalEntityJSBuilder#factory()}
     * and {@link AnimalEntityJS} for examples.<br><br>
     * <p>
     * Unlike most builder types, there is little need to override {@link #createObject()} due to entity types being
     * essentially a supplier for the class.
     *
     * @return The {@link EntityType.EntityFactory} that is used by the {@link EntityType} this builder creates
     */
    @HideFromJS
    abstract public EntityType.EntityFactory<T> factory();

    /**
     * Used to retrieve the entity type's attributes. Implementors are encouraged to return
     * the {@link AttributeSupplier.Builder} from a static method in the base class
     * (i.e. {@link AnimalEntityJS#createLivingAttributes()})
     *
     * @return The {@link AttributeSupplier.Builder} that will be built during Forge's EntityAttributeCreationEvent
     */
    @HideFromJS
    abstract public AttributeSupplier.Builder getAttributeBuilder();

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

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
