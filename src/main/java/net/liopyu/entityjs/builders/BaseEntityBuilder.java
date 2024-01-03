package net.liopyu.entityjs.builders;

import dev.architectury.utils.value.FloatSupplier;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.liopyu.entityjs.entities.BaseEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.liopyu.entityjs.util.ai.brain.BrainProviderBuilder;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
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
 * The base builder for all entity types that EntityJS can handle, has methods to allow overriding
 * nearly every method available in {@link LivingEntity}. Implementors are free to use as many or few
 * of these as they wish
 *
 * @param <T> The entity class that the built entity type is for, this should be a custom class
 *           that extends {@link LivingEntity} or a subclass and {@link IAnimatableJS}
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
    public transient boolean canBePushed;
    public transient boolean canBeCollidedWith;
    public transient boolean isAttackable;
    public transient Consumer<AttributeSupplier.Builder> attributes;
    public transient final List<AnimationControllerSupplier<T>> animationSuppliers;
    public transient boolean shouldDropLoot;
    public transient boolean setCanAddPassenger;
    public transient boolean canRide;
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
    public transient BrainProviderBuilder brainProviderBuilder;
    public transient HumanoidArm mainArm;
    public transient boolean hasInventory;
    public transient Consumer<BrainBuilder> brainBuilder;

    public transient Consumer<BaseEntityJS> dropExperienceHandler;
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
    public transient Function<Integer, Integer> customGetFireImmuneTicks;
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
    public transient Consumer<T> customKill;
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
    public transient Boolean customRideableUnderWater;
    public transient Consumer<T> customTickDeath;
    public transient Predicate<BaseEntityJS> customShouldDropExperience;

    public transient Function<BaseEntityJS, Integer> customExperienceReward;

    public transient Function<BaseEntityJS, RandomSource> customRandom;

    public transient Function<BaseEntityJS, LivingEntity> customLastHurtByMob;

    public transient IntSupplier customGetLastHurtByMobTimestamp;
    public transient Consumer<Player> customSetLastHurtByPlayer;
    public transient Consumer<LivingEntity> customSetLastHurtByMob;
    public transient Supplier<LivingEntity> customGetLastHurtMob;
    public transient Function<BaseEntityJS, Integer> customGetLastHurtMobTimestamp;
    public transient Consumer<Entity> customSetLastHurtMob;
    public transient IntSupplier customGetNoActionTime;
    public transient IntConsumer customSetNoActionTime;
    public transient BooleanSupplier customShouldDiscardFriction;
    public transient BooleanConsumer customSetDiscardFriction;
    public transient TriConsumer<EquipmentSlot, ItemStack, ItemStack> customOnEquipItem;
    public transient Consumer<ItemStack> customPlayEquipSound;
    public transient Runnable customTickEffects;
    public transient Runnable customUpdateInvisibilityStatus;
    public transient DoubleFunction<Entity> customGetVisibilityPercent;
    public transient Predicate<LivingEntity> customCanAttack;
    public transient BiPredicate<LivingEntity, TargetingConditions> customCanAttackWithConditions;
    public transient BooleanSupplier customCanBeSeenAsEnemy;
    public transient BooleanSupplier customCanBeSeenByAnyone;
    public transient Runnable customRemoveEffectParticles;
    public transient BooleanSupplier customRemoveAllEffects;
    public transient Supplier<Collection<MobEffectInstance>> customGetActiveEffects;
    public transient Supplier<Map<MobEffect, MobEffectInstance>> customGetActiveEffectsMap;
    public transient Predicate<MobEffect> customHasEffect;
    public transient Function<MobEffect, MobEffectInstance> customGetEffect;
    public transient BiPredicate<MobEffectInstance, Entity> customAddEffect;
    public transient Predicate<MobEffectInstance> customCanBeAffected;
    public transient BiConsumer<MobEffectInstance, Entity> customForceAddEffect;
    public transient BooleanSupplier customIsInvertedHealAndHarm;
    public transient Function<MobEffect, MobEffectInstance> customRemoveEffectNoUpdate;
    public transient Predicate<MobEffect> customRemoveEffect;
    public transient BiConsumer<MobEffectInstance, Entity> customOnEffectAdded;
    public transient TriConsumer<MobEffectInstance, Boolean, Entity> customOnEffectUpdated;
    public transient Consumer<MobEffectInstance> customOnEffectRemoved;
    public transient FloatConsumer customHeal;
    public transient FloatSupplier customGetHealth;
    public transient FloatConsumer customSetHealth;
    public transient BooleanSupplier customIsDeadOrDying;
    public transient BiPredicate<DamageSource, Float> customHurt;
    public transient Supplier<DamageSource> customGetLastDamageSource;
    public transient Consumer<DamageSource> customPlayHurtSound;
    public transient Predicate<DamageSource> customIsDamageSourceBlocked;
    public transient Consumer<DamageSource> customDie;
    public transient Consumer<LivingEntity> customCreateWitherRose;
    public transient Consumer<DamageSource> customDropAllDeathLoot;
    public transient Runnable customDropEquipment;
    public transient TriConsumer<DamageSource, Integer, Boolean> customDropCustomDeathLoot;
    public transient Supplier<ResourceLocation> customGetLootTable;
    public transient BiConsumer<DamageSource, Boolean> customDropFromLootTable;
    public transient TriConsumer<Double, Double, Double> customKnockback;
    public transient Runnable customSkipDropExperience;
    public transient BooleanSupplier customWasExperienceConsumed;
    public transient Supplier<LivingEntity.Fallsounds> customGetFallSounds;
    public transient Function<ItemStack, SoundEvent> customGetEatingSound;
    public transient BooleanConsumer customSetOnGround;
    public transient Supplier<Optional<BlockPos>> customGetLastClimbablePos;
    public transient BooleanSupplier customOnClimbable;
    public transient BooleanSupplier customIsAlive;

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
        canBePushed = false;
        canBeCollidedWith = false;
        isAttackable = true;
        attributes = builder -> {
        };
        animationSuppliers = new ArrayList<>();
        shouldDropLoot = true;
        setCanAddPassenger(entity -> false);
        canRide(entity -> false);
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

    public BaseEntityBuilder<T> canBePushed(boolean b) {
        canBePushed = b;
        return this;
    }

    public BaseEntityBuilder<T> canBeCollidedWith(boolean b) {
        canBeCollidedWith = b;
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

    public BaseEntityBuilder<T> setCanAddPassenger(boolean b) {
        setCanAddPassenger = b;
        return this;
    }

    public BaseEntityBuilder<T> canRide(Predicate<Entity> predicate) {
        passengerPredicate = predicate;
        return this;
    }

    public BaseEntityBuilder<T> canRide(boolean b) {
        canRide = b;
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

    public transient Consumer<T> preTick = t -> {
    };
    public transient Consumer<T> postTick = t -> {
    };

    public BaseEntityBuilder<T> preTick(Consumer<T> tickCallback) {
        preTick = tickCallback;
        return this;
    }

    public BaseEntityBuilder<T> postTick(Consumer<T> tickCallback) {
        postTick = tickCallback;
        return this;
    }

    public BaseEntityBuilder<T> getType(EntityType<T> type) {
        getType = type;
        return this;
    }


    // Add this method to set the dropExperienceHandler in BaseEntityBuilder
    public BaseEntityBuilder<T> dropExperienceHandler(Consumer<BaseEntityJS> handler) {
        dropExperienceHandler = handler;
        return this;
    }


    public BaseEntityBuilder<T> shouldRemoveSoulSpeed(String... blockStates) {
        if (blockStates != null) {
            List<BlockState> soulSpeedRemovalList = new ArrayList<>();
            for (String blockState : blockStates) {
                ResourceLocation blockLocation = new ResourceLocation(blockState);
                if (ForgeRegistries.BLOCKS.containsKey(blockLocation)) {
                    soulSpeedRemovalList.add(ForgeRegistries.BLOCKS.getValue(blockLocation).defaultBlockState());
                }
            }
            this.shouldRemoveSoulSpeed = List.of(soulSpeedRemovalList.toArray(new BlockState[0]));
        }
        return this;
    }

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

    public BaseEntityBuilder<T> getFireImmuneTicks(Function<Integer, Integer> customGetFireImmuneTicks) {
        this.customGetFireImmuneTicks = customGetFireImmuneTicks;
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

    public BaseEntityBuilder<T> kill(Consumer<T> customKill) {
        this.customKill = customKill;
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

    public BaseEntityBuilder<T> baseTick(Runnable customBaseTick) {
        this.customBaseTick = customBaseTick;
        return this;
    }

    public BaseEntityBuilder<T> canSpawnSoulSpeedParticle(boolean canSpawn) {
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

    public BaseEntityBuilder<T> isBaby(boolean isBaby) {
        this.isBaby = isBaby;
        return this;
    }

    public BaseEntityBuilder<T> scale(float customScale) {
        this.customScale = customScale;
        return this;
    }

    public BaseEntityBuilder<T> rideableUnderWater(boolean customRideableUnderWater) {
        this.customRideableUnderWater = customRideableUnderWater;
        return this;
    }

    public BaseEntityBuilder<T> tickDeath(Consumer<T> customTickDeath) {
        this.customTickDeath = customTickDeath;
        return this;
    }

    public BaseEntityBuilder<T> shouldDropExperience(Predicate<BaseEntityJS> customShouldDropExperience) {
        this.customShouldDropExperience = customShouldDropExperience;
        return this;
    }

    public BaseEntityBuilder<T> experienceReward(Function<BaseEntityJS, Integer> customExperienceReward) {
        this.customExperienceReward = customExperienceReward;
        return this;
    }

    public BaseEntityBuilder<T> random(Function<BaseEntityJS, RandomSource> customRandom) {
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

    public BaseEntityBuilder<T> setLastHurtByPlayer(Consumer<Player> customSetLastHurtByPlayer) {
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
    }

    public BaseEntityBuilder<T> shouldDiscardFriction(BooleanSupplier customShouldDiscardFriction) {
        this.customShouldDiscardFriction = customShouldDiscardFriction;
        return this;
    }

    public BaseEntityBuilder<T> setDiscardFriction(BooleanConsumer customSetDiscardFriction) {
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

    public BaseEntityBuilder<T> getVisibilityPercent(DoubleFunction<Entity> customGetVisibilityPercent) {
        this.customGetVisibilityPercent = customGetVisibilityPercent;
        return this;
    }

    public BaseEntityBuilder<T> canAttack(Predicate<LivingEntity> customCanAttack) {
        this.customCanAttack = customCanAttack;
        return this;
    }

    public BaseEntityBuilder<T> canAttackWithConditions(BiPredicate<LivingEntity, TargetingConditions> customCanAttackWithConditions) {
        this.customCanAttackWithConditions = customCanAttackWithConditions;
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

    public BaseEntityBuilder<T> removeAllEffects(BooleanSupplier customRemoveAllEffects) {
        this.customRemoveAllEffects = customRemoveAllEffects;
        return this;
    }

    public BaseEntityBuilder<T> getActiveEffects(Supplier<Collection<MobEffectInstance>> customGetActiveEffects) {
        this.customGetActiveEffects = customGetActiveEffects;
        return this;
    }

    public BaseEntityBuilder<T> getActiveEffectsMap(Supplier<Map<MobEffect, MobEffectInstance>> customGetActiveEffectsMap) {
        this.customGetActiveEffectsMap = customGetActiveEffectsMap;
        return this;
    }

    public BaseEntityBuilder<T> hasEffect(Predicate<MobEffect> customHasEffect) {
        this.customHasEffect = customHasEffect;
        return this;
    }

    public BaseEntityBuilder<T> getEffect(Function<MobEffect, MobEffectInstance> customGetEffect) {
        this.customGetEffect = customGetEffect;
        return this;
    }

    public BaseEntityBuilder<T> addEffect(BiPredicate<MobEffectInstance, Entity> customAddEffect) {
        this.customAddEffect = customAddEffect;
        return this;
    }

    public BaseEntityBuilder<T> canBeAffected(Predicate<MobEffectInstance> customCanBeAffected) {
        this.customCanBeAffected = customCanBeAffected;
        return this;
    }

    public BaseEntityBuilder<T> forceAddEffect(BiConsumer<MobEffectInstance, Entity> customForceAddEffect) {
        this.customForceAddEffect = customForceAddEffect;
        return this;
    }

    public BaseEntityBuilder<T> isInvertedHealAndHarm(BooleanSupplier customIsInvertedHealAndHarm) {
        this.customIsInvertedHealAndHarm = customIsInvertedHealAndHarm;
        return this;
    }

    public BaseEntityBuilder<T> removeEffectNoUpdate(Function<MobEffect, MobEffectInstance> customRemoveEffectNoUpdate) {
        this.customRemoveEffectNoUpdate = customRemoveEffectNoUpdate;
        return this;
    }

    public BaseEntityBuilder<T> removeEffect(Predicate<MobEffect> customRemoveEffect) {
        this.customRemoveEffect = customRemoveEffect;
        return this;
    }

    public BaseEntityBuilder<T> onEffectAdded(BiConsumer<MobEffectInstance, Entity> customOnEffectAdded) {
        this.customOnEffectAdded = customOnEffectAdded;
        return this;
    }

    public BaseEntityBuilder<T> onEffectUpdated(TriConsumer<MobEffectInstance, Boolean, Entity> customOnEffectUpdated) {
        this.customOnEffectUpdated = customOnEffectUpdated;
        return this;
    }

    public BaseEntityBuilder<T> onEffectRemoved(Consumer<MobEffectInstance> customOnEffectRemoved) {
        this.customOnEffectRemoved = customOnEffectRemoved;
        return this;
    }

    public BaseEntityBuilder<T> heal(FloatConsumer customHeal) {
        this.customHeal = customHeal;
        return this;
    }

    public BaseEntityBuilder<T> getHealth(FloatSupplier customGetHealth) {
        this.customGetHealth = customGetHealth;
        return this;
    }

    public BaseEntityBuilder<T> setHealth(FloatConsumer customSetHealth) {
        this.customSetHealth = customSetHealth;
        return this;
    }

    public BaseEntityBuilder<T> isDeadOrDying(BooleanSupplier customIsDeadOrDying) {
        this.customIsDeadOrDying = customIsDeadOrDying;
        return this;
    }

    public BaseEntityBuilder<T> hurt(BiPredicate<DamageSource, Float> customHurt) {
        this.customHurt = customHurt;
        return this;
    }

    public BaseEntityBuilder<T> getLastDamageSource(Supplier<DamageSource> customGetLastDamageSource) {
        this.customGetLastDamageSource = customGetLastDamageSource;
        return this;
    }

    public BaseEntityBuilder<T> playHurtSound(Consumer<DamageSource> customPlayHurtSound) {
        this.customPlayHurtSound = customPlayHurtSound;
        return this;
    }

    public BaseEntityBuilder<T> isDamageSourceBlocked(Predicate<DamageSource> customIsDamageSourceBlocked) {
        this.customIsDamageSourceBlocked = customIsDamageSourceBlocked;
        return this;
    }

    public BaseEntityBuilder<T> die(Consumer<DamageSource> customDie) {
        this.customDie = customDie;
        return this;
    }

    public BaseEntityBuilder<T> createWitherRose(Consumer<LivingEntity> customCreateWitherRose) {
        this.customCreateWitherRose = customCreateWitherRose;
        return this;
    }

    public BaseEntityBuilder<T> dropAllDeathLoot(Consumer<DamageSource> customDropAllDeathLoot) {
        this.customDropAllDeathLoot = customDropAllDeathLoot;
        return this;
    }

    public BaseEntityBuilder<T> dropEquipment(Runnable customDropEquipment) {
        this.customDropEquipment = customDropEquipment;
        return this;
    }

    public BaseEntityBuilder<T> dropCustomDeathLoot(TriConsumer<DamageSource, Integer, Boolean> customDropCustomDeathLoot) {
        this.customDropCustomDeathLoot = customDropCustomDeathLoot;
        return this;
    }

    public BaseEntityBuilder<T> getLootTable(Supplier<ResourceLocation> customGetLootTable) {
        this.customGetLootTable = customGetLootTable;
        return this;
    }

    public BaseEntityBuilder<T> dropFromLootTable(BiConsumer<DamageSource, Boolean> customDropFromLootTable) {
        this.customDropFromLootTable = customDropFromLootTable;
        return this;
    }

    public BaseEntityBuilder<T> knockback(TriConsumer<Double, Double, Double> customKnockback) {
        this.customKnockback = customKnockback;
        return this;
    }

    public BaseEntityBuilder<T> skipDropExperience(Runnable customSkipDropExperience) {
        this.customSkipDropExperience = customSkipDropExperience;
        return this;
    }

    public BaseEntityBuilder<T> wasExperienceConsumed(BooleanSupplier customWasExperienceConsumed) {
        this.customWasExperienceConsumed = customWasExperienceConsumed;
        return this;
    }

    public BaseEntityBuilder<T> getFallSounds(Supplier<LivingEntity.Fallsounds> customGetFallSounds) {
        this.customGetFallSounds = customGetFallSounds;
        return this;
    }

    public BaseEntityBuilder<T> getEatingSound(Function<ItemStack, SoundEvent> customGetEatingSound) {
        this.customGetEatingSound = customGetEatingSound;
        return this;
    }

    public BaseEntityBuilder<T> setOnGround(BooleanConsumer customSetOnGround) {
        this.customSetOnGround = customSetOnGround;
        return this;
    }

    public BaseEntityBuilder<T> getLastClimbablePos(Supplier<Optional<BlockPos>> customGetLastClimbablePos) {
        this.customGetLastClimbablePos = customGetLastClimbablePos;
        return this;
    }

    public BaseEntityBuilder<T> onClimbable(BooleanSupplier customOnClimbable) {
        this.customOnClimbable = customOnClimbable;
        return this;
    }

    public BaseEntityBuilder<T> isAlive(BooleanSupplier customIsAlive) {
        this.customIsAlive = customIsAlive;
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
        animationSuppliers.add(new AnimationControllerSupplier<>(name,translationTicksLength, easingType, predicate, soundListener, particleListener, instructionListener));
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
     *
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
     *
     * Unlike most builder types, there is little need to override {@link #createObject()} due to entity types being
     * essentially a supplier for the class.
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
     * @param name The name of the AnimationController that this builds
     * @param translationTicksLength The number of ticks it takes to transition between animations
     * @param predicate The {@link IAnimationPredicateJS script-friendly} animation predicate
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
            return event -> test(new AnimationEventJS<>(event)) ? PlayState.CONTINUE : PlayState.STOP;
        }
    }

    /**
     * A simple wrapper around a {@link AnimationEvent} that restricts access to certain things
     * and adds {@link @Info} annotations for script writers
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

    public static class SoundKeyFrameEventJS<E extends LivingEntity & IAnimatableJS> extends KeyFrameEventJS<E>{

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
