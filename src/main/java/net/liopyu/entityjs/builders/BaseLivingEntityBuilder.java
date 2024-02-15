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
import it.unimi.dsi.fastutil.booleans.BooleanPredicate;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.events.BiomeSpawnsEventJS;
import net.liopyu.entityjs.util.*;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.liopyu.liolib.core.animation.AnimationController;
import net.liopyu.liolib.core.animation.EasingType;
import net.liopyu.liolib.core.animation.RawAnimation;
import net.liopyu.liolib.core.keyframe.event.CustomInstructionKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.KeyFrameEvent;
import net.liopyu.liolib.core.keyframe.event.ParticleKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.SoundKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.data.KeyFrameData;
import net.liopyu.liolib.core.keyframe.event.data.SoundKeyframeData;
import net.liopyu.liolib.core.object.DataTicket;
import net.liopyu.liolib.core.object.PlayState;
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
import net.minecraft.util.random.Weight;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MobSpawnSettings;
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
import net.liopyu.liolib.core.animation.AnimationState;

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
    public transient final List<AnimationControllerSupplier<T>> animationSuppliers;
    public transient Predicate<LivingEntity> shouldDropLoot;
    public transient Predicate<Entity> canAddPassenger;
    public transient Predicate<LivingEntity> isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient Predicate<LivingEntity> isImmobile;
    public transient float getBlockJumpFactor;
    public transient Function<LivingEntity, Integer> blockSpeedFactor;
    public transient float getJumpPower;
    public transient float getSoundVolume;
    public transient float getWaterSlowDown;
    public transient ResourceLocation setSwimSound;
    public transient Function<LivingEntity, Boolean> isFlapping;
    public transient ResourceLocation setDeathSound;
    public transient RenderType renderType;
    public transient EntityType<?> getType;
    public transient HumanoidArm mainArm;

    public transient Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch;

    public transient Function<ContextUtils.EntityPoseDimensionsContext, Float> setStandingEyeHeight;

    public transient Consumer<LivingEntity> onDecreaseAirSupply;
    public transient Consumer<LivingEntity> onBlockedByShield;

    public transient BooleanSupplier repositionEntityAfterLoad;

    public transient Function<Entity, Float> nextStep;

    public transient Consumer<LivingEntity> onIncreaseAirSupply;

    public transient ResourceLocation setHurtSound;

    public transient ResourceLocation setSwimSplashSound;

    public transient Predicate<ContextUtils.EntityTypeEntityContext> canAttackType;

    public transient Function<LivingEntity, Float> scale;
    public transient boolean rideableUnderWater;

    public transient Predicate<LivingEntity> shouldDropExperience;

    public transient Function<LivingEntity, Integer> experienceReward;


    public transient Consumer<ContextUtils.EntityEquipmentContext> onEquipItem;


    public transient Function<Entity, Double> visibilityPercent;

    public transient Predicate<LivingEntity> canAttack;

    public transient Predicate<MobEffectInstance> canBeAffectedPredicate;

    public transient BooleanSupplier invertedHealAndHarm;

    public transient Consumer<ContextUtils.OnEffectContext> onEffectAdded;


    public transient Consumer<ContextUtils.OnEffectContext> onEffectRemoved;

    public transient Consumer<ContextUtils.EntityHealContext> onLivingHeal;


    public transient Consumer<ContextUtils.EntityDamageContext> onHurt;


    public transient Consumer<ContextUtils.DeathContext> onDeath;


    public transient Consumer<ContextUtils.EntityLootContext> dropCustomDeathLoot;


    public transient LivingEntity.Fallsounds fallSounds;
    public transient ResourceLocation smallFallSound;
    public transient ResourceLocation largeFallSound;

    public transient ResourceLocation eatingSound;

    public transient Predicate<LivingEntity> onClimbable;
    public transient boolean canBreatheUnderwater;

    public transient Consumer<ContextUtils.EntityFallDamageContext> onLivingFall;

    public transient Consumer<LivingEntity> onSprint;

    public transient DoubleSupplier jumpBoostPower;
    public transient Predicate<ContextUtils.EntityFluidStateContext> canStandOnFluid;

    public transient Consumer<Float> setSpeed;

    public transient Predicate<LivingEntity> isSensitiveToWater;

    public transient Consumer<LivingEntity> onStopRiding;
    public transient Consumer<LivingEntity> rideTick;


    @FunctionalInterface
    public interface HeptConsumer {
        void accept(double arg1, double arg2, double arg3, float arg4, float arg5, int arg6, boolean arg7);
    }


    public transient Consumer<ContextUtils.EntityItemEntityContext> onItemPickup;
    public transient Predicate<Entity> hasLineOfSight;

    public transient Consumer<ContextUtils.EntityFloatContext> setAbsorptionAmount;
    public transient Consumer<LivingEntity> onEnterCombat;
    public transient Consumer<LivingEntity> onLeaveCombat;

    public transient Predicate<LivingEntity> isAffectedByPotions;

    public transient Predicate<LivingEntity> isAttackable;

    public transient Predicate<ContextUtils.EntityItemLevelContext> canTakeItem;

    public transient Predicate<LivingEntity> isSleeping;
    public transient Consumer<ContextUtils.EntityBlockPosContext> onStartSleeping;
    public transient Consumer<LivingEntity> onStopSleeping;

    public transient Consumer<ContextUtils.EntityItemLevelContext> eat;

    public transient Predicate<ContextUtils.PlayerEntityContext> shouldRiderFaceForward;

    public transient Predicate<LivingEntity> canFreeze;
    public transient Predicate<LivingEntity> isCurrentlyGlowing;
    public transient Predicate<LivingEntity> canDisableShield;
    public transient IntSupplier getMaxFallDistance;
    public transient Function<ContextUtils.MobInteractContext, @Nullable InteractionResult> onInteract;

    public transient Consumer<LivingEntity> onClientRemoval;
    public transient Consumer<LivingEntity> onAddedToWorld;
    public transient Consumer<LivingEntity> lavaHurt;
    public transient Consumer<LivingEntity> onFlap;
    public transient BooleanSupplier dampensVibrations;

    public transient Consumer<ContextUtils.PlayerEntityContext> playerTouch;
    public transient Function<ClipContext, HitResult> pick;
    public transient BooleanSupplier showVehicleHealth;

    public transient Consumer<ContextUtils.ThunderHitContext> thunderHit;
    public transient Predicate<ContextUtils.DamageContext> isInvulnerableTo;
    public transient Predicate<LivingEntity> canChangeDimensions;
    public transient BiFunction<Float, Float, Integer> calculateFallDamage;
    public transient Predicate<ContextUtils.MayInteractContext> mayInteract;
    public transient Predicate<ContextUtils.CanTrampleContext> canTrample;
    public transient Consumer<LivingEntity> onRemovedFromWorld;
    public transient Consumer<LivingEntity> onLivingJump;
    public transient Consumer<LivingEntity> livingAiStep;

    public transient Consumer<AttributeSupplier.Builder> attributes;
    public SpawnPlacements.Type placementType;
    public Heightmap.Types heightMap;
    public SpawnPlacements.SpawnPredicate<? extends Entity> spawnPredicate;
    public static final List<BaseLivingEntityBuilder<?>> spawnList = new ArrayList<>();
    public static final List<EventBasedSpawnModifier.BiomeSpawn> biomeSpawnList = new ArrayList<>();

    //STUFF
    public BaseLivingEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        immuneTo = new ResourceLocation[0];
        fireImmune = false;
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        modelResource = t -> t.getBuilder().newID("geo/", ".geo.json");
        textureResource = t -> t.getBuilder().newID("textures/models/entity/", ".png");
        animationResource = t -> t.getBuilder().newID("animations/", ".animation.json");
        isPushable = true;
        animationSuppliers = new ArrayList<>();
        isAlwaysExperienceDropper = false;
        getBlockJumpFactor = 0.5f;
        blockSpeedFactor = t -> 1;
        getJumpPower = 0.5f;
        getSoundVolume = 1.0f;
        getWaterSlowDown = 0.8f;
        rideableUnderWater = false;
        canBreatheUnderwater = false;
        renderType = RenderType.CUTOUT;
        mainArm = HumanoidArm.RIGHT;

    }

    @Info(value = "Sets the main arm of the entity, defaults to 'right'")
    public BaseLivingEntityBuilder<T> mainArm(HumanoidArm arm) {
        this.mainArm = arm;
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
        this.save = b;
        return this;
    }

    @Info(value = "Determines if the entity is immune to fire, defaults to false")
    public BaseLivingEntityBuilder<T> fireImmune(boolean b) {
        this.fireImmune = b;
        return this;
    }

    @Info(value = "Determines the blocks the entity is 'immune' to")
    public BaseLivingEntityBuilder<T> immuneTo(ResourceLocation... blocks) {
        this.immuneTo = blocks;
        return this;
    }

    @Info(value = "Determines if the entity can spawn far from players")
    public BaseLivingEntityBuilder<T> canSpawnFarFromPlayer(boolean b) {
        this.spawnFarFromPlayer = b;
        return this;
    }

    @Info(value = "Sets the Jump Power of the entity")
    public BaseLivingEntityBuilder<T> getJumpPower(float b) {
        this.getJumpPower = b;
        return this;
    }

    @Info(value = "Sets the Jump Factor of the entity")
    public BaseLivingEntityBuilder<T> getBlockJumpFactor(float b) {
        this.getBlockJumpFactor = b;
        return this;
    }

    @Info(value = "Sets the water slowdown defaults to 0.8")
    public BaseLivingEntityBuilder<T> getWaterSlowDown(float b) {
        this.getWaterSlowDown = b;
        return this;
    }

    @Info(value = "Sets the general volume of the entity")
    public BaseLivingEntityBuilder<T> getSoundVolume(float b) {
        this.getSoundVolume = b;
        return this;
    }

    @Info(value = "Deciding logic for whether an entity drops their loot or not")
    public BaseLivingEntityBuilder<T> shouldDropLoot(Predicate<LivingEntity> b) {
        this.shouldDropLoot = b;
        return this;
    }

    @Info(value = "Sets the aiStep property in the builder.")
    public BaseLivingEntityBuilder<T> livingAiStep(Consumer<LivingEntity> aiStep) {
        this.livingAiStep = aiStep;
        return this;
    }

    public BaseLivingEntityBuilder<T> onLivingJump(Consumer<LivingEntity> onJump) {
        this.onLivingJump = onJump;
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
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> isPushable(boolean b) {
        isPushable = b;
        return this;
    }


    @Info(value = """
            Sets the passenger predicate in the builder.
            "Defaults to allowing any entity to be a passenger.
            """)
    public BaseLivingEntityBuilder<T> canAddPassenger(Predicate<Entity> predicate) {
        canAddPassenger = predicate;
        return this;
    }

    @Info(value = """
            Sets whether the entity is affected by fluids in the builder.
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> isAffectedByFluids(Predicate<LivingEntity> b) {
        isAffectedByFluids = b;
        return this;
    }

    @Info(value = """
            Sets the summonable property in the builder.
            "Defaults to true.
            """)
    public BaseLivingEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
    }

    @Info(value = """
            Sets the immobility property in the builder.
            "Defaults to false.
            """)
    public BaseLivingEntityBuilder<T> isImmobile(Predicate<LivingEntity> b) {
        isImmobile = b;
        return this;
    }

    @Info(value = """
            Sets the always experience dropper property in the builder.
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
    public BaseLivingEntityBuilder<T> setDeathSound(ResourceLocation sound) {
        setDeathSound = sound;
        return this;
    }

    @Info(value = """
            Sets the swim sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> setSwimSound(ResourceLocation sound) {
        setSwimSound = sound;
        return this;
    }

    @Info(value = """
            Sets the swim high-speed splash sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> setSwimSplashSound(ResourceLocation sound) {
        setSwimSplashSound = sound;
        return this;
    }

    @Info(value = """
            Sets the block speed factor for the entity in the builder.
            "Defaults to 0.
            """)
    public BaseLivingEntityBuilder<T> blockSpeedFactor(int i) {
        blockSpeedFactor = t -> i;
        return this;
    }

    @Info(value = """
            Sets the block speed factor function for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> blockSpeedFactor(Function<LivingEntity, Integer> function) {
        blockSpeedFactor = function;
        return this;
    }

    @Info(value = """
            Sets the flapping property for the entity in the builder.
            "Defaults to false.
            """)
    public BaseLivingEntityBuilder<T> isFlapping(Function<LivingEntity, Boolean> b) {
        isFlapping = b;
        return this;
    }

    public transient Consumer<LivingEntity> tick;

    @Info(value = """
            Sets a custom tick callback for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> tick(Consumer<LivingEntity> tickCallback) {
        tick = tickCallback;
        return this;
    }


    @Info(value = "Sets the custom onAddedToWorld behavior")
    public BaseLivingEntityBuilder<T> onAddedToWorld(Consumer<LivingEntity> onAddedToWorldCallback) {
        onAddedToWorld = onAddedToWorldCallback;
        return this;
    }


    @Info(value = """
            Sets a custom behavior for auto-attacking on touch for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> doAutoAttackOnTouch(Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch) {
        this.doAutoAttackOnTouch = doAutoAttackOnTouch;
        return this;
    }


    @Info(value = """
            Sets the function to determine the custom standing eye height for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> setStandingEyeHeight(Function<ContextUtils.EntityPoseDimensionsContext, Float> setStandingEyeHeight) {
        this.setStandingEyeHeight = setStandingEyeHeight;
        return this;
    }


    @Info(value = """
            Sets the function to determine the custom decrease in air supply for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> onDecreaseAirSupply(Consumer<LivingEntity> onDecreaseAirSupply) {
        this.onDecreaseAirSupply = onDecreaseAirSupply;
        return this;
    }

    @Info(value = """
            Sets the custom behavior for when the entity is blocked by a shield in the builder.
            """)
    public BaseLivingEntityBuilder<T> onBlockedByShield(Consumer<LivingEntity> onBlockedByShield) {
        this.onBlockedByShield = onBlockedByShield;
        return this;
    }


    @Info(value = """
            Sets the supplier to determine whether the entity should be repositioned after load in the builder.
            """)
    public BaseLivingEntityBuilder<T> repositionEntityAfterLoad(BooleanSupplier customRepositionEntityAfterLoad) {
        this.repositionEntityAfterLoad = customRepositionEntityAfterLoad;
        return this;
    }

    @Info(value = """
            Sets the function to determine the next step for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> nextStep(Function<Entity, Float> nextStep) {
        this.nextStep = nextStep;
        return this;
    }


    @Info(value = """
            Sets the function to increase the air supply for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> onIncreaseAirSupply(Consumer<LivingEntity> onIncreaseAirSupply) {
        this.onIncreaseAirSupply = onIncreaseAirSupply;
        return this;
    }

    @Info(value = """
            Sets the function to get the hurt sound for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> setHurtSound(ResourceLocation setHurtSound) {
        this.setHurtSound = setHurtSound;
        return this;
    }


    @Info(value = """
            Sets the function to determine if the entity can attack a specific type in the builder.
            """)
    public BaseLivingEntityBuilder<T> canAttackType(Predicate<ContextUtils.EntityTypeEntityContext> canAttackType) {
        this.canAttackType = canAttackType;
        return this;
    }


    @Info(value = """
            Sets the custom scale for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> scale(Function<LivingEntity, Float> customScale) {
        this.scale = customScale;
        return this;
    }

    @Info(value = """
            Sets whether the entity is rideable underwater in the builder.
            """)
    public BaseLivingEntityBuilder<T> rideableUnderWater(boolean rideableUnderWater) {
        this.rideableUnderWater = rideableUnderWater;
        return this;
    }


    @Info(value = """
            Sets whether the entity should drop experience in the builder.
            """)
    public BaseLivingEntityBuilder<T> shouldDropExperience(Predicate<LivingEntity> p) {
        this.shouldDropExperience = p;
        return this;
    }

    @Info(value = """
            Sets the function to get the custom experience reward for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> experienceReward(Function<LivingEntity, Integer> experienceReward) {
        this.experienceReward = experienceReward;
        return this;
    }


    @Info(value = """
            Sets the tri-consumer for the custom onEquipItem behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> onEquipItem(Consumer<ContextUtils.EntityEquipmentContext> onEquipItem) {
        this.onEquipItem = onEquipItem;
        return this;
    }


    @Info(value = """
            Sets the function for getting the visibility percent for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> visibilityPercent(Function<Entity, Double> visibilityPercent) {
        this.visibilityPercent = visibilityPercent;
        return this;
    }

    @Info(value = """
            Sets the predicate for the custom canAttack behavior for the entity in the builder.
            """)
    public BaseLivingEntityBuilder<T> canAttack(Predicate<LivingEntity> customCanAttack) {
        this.canAttack = customCanAttack;
        return this;
    }


    @Info(value = "Sets the custom logic to determine if the entity can be affected by a specific potion effect.")
    public BaseLivingEntityBuilder<T> customCanBeAffected(Predicate<MobEffectInstance> predicate) {
        canBeAffectedPredicate = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if healing and harming effects are inverted for the entity.")
    public BaseLivingEntityBuilder<T> invertedHealAndHarm(BooleanSupplier invertedHealAndHarm) {
        this.invertedHealAndHarm = invertedHealAndHarm;
        return this;
    }


    @Info(value = "Sets the custom logic for when a potion effect is added to the entity.")
    public BaseLivingEntityBuilder<T> onEffectAdded(Consumer<ContextUtils.OnEffectContext> consumer) {
        onEffectAdded = consumer;
        return this;
    }


    @Info(value = "Fires whenever the entity heals.")
    public BaseLivingEntityBuilder<T> onLivingHeal(Consumer<ContextUtils.EntityHealContext> callback) {
        onLivingHeal = callback;
        return this;
    }


    @Info(value = "Sets the custom logic for when a potion effect is removed from the entity.")
    public BaseLivingEntityBuilder<T> onEffectRemoved(Consumer<ContextUtils.OnEffectContext> consumer) {
        onEffectRemoved = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for handling entity damage")
    public BaseLivingEntityBuilder<T> onHurt(Consumer<ContextUtils.EntityDamageContext> predicate) {
        onHurt = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for when the entity dies")
    public BaseLivingEntityBuilder<T> onDeath(Consumer<ContextUtils.DeathContext> consumer) {
        onDeath = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for dropping custom death loot when the entity dies")
    public BaseLivingEntityBuilder<T> dropCustomDeathLoot(Consumer<ContextUtils.EntityLootContext> consumer) {
        dropCustomDeathLoot = consumer;
        return this;
    }


    @Info(value = "Sets the fall sounds for the entity")
    public BaseLivingEntityBuilder<T> fallSounds(ResourceLocation smallFallSound, ResourceLocation largeFallSound) {
        this.smallFallSound = smallFallSound;
        this.largeFallSound = largeFallSound;
        return this;
    }


    @Info(value = "Sets the custom logic for determining the eating sound for the entity")
    public BaseLivingEntityBuilder<T> eatingSound(ResourceLocation function) {
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
    public BaseLivingEntityBuilder<T> onLivingFall(Consumer<ContextUtils.EntityFallDamageContext> c) {
        onLivingFall = c;
        return this;
    }


    @Info(value = "Sets the custom logic to fire when the entity sprints")
    public BaseLivingEntityBuilder<T> onSprint(Consumer<LivingEntity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining the entity's jump boost power")
    public BaseLivingEntityBuilder<T> jumpBoostPower(DoubleSupplier supplier) {
        jumpBoostPower = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity can stand on a specific fluid")
    public BaseLivingEntityBuilder<T> canStandOnFluid(Predicate<ContextUtils.EntityFluidStateContext> predicate) {
        canStandOnFluid = predicate;
        return this;
    }


    @Info(value = "Sets the custom speed for the entity")
    public BaseLivingEntityBuilder<T> setSpeed(Consumer<Float> consumer) {
        setSpeed = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity is sensitive to water")
    public BaseLivingEntityBuilder<T> isSensitiveToWater(Predicate<LivingEntity> predicate) {
        isSensitiveToWater = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for when the entity stops riding")
    public BaseLivingEntityBuilder<T> onStopRiding(Consumer<LivingEntity> callback) {
        onStopRiding = callback;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity is updated while riding")
    public BaseLivingEntityBuilder<T> rideTick(Consumer<LivingEntity> callback) {
        rideTick = callback;
        return this;
    }


    @Info(value = "Sets the custom logic for when the entity picks up an item")
    public BaseLivingEntityBuilder<T> onItemPickup(Consumer<ContextUtils.EntityItemEntityContext> consumer) {
        onItemPickup = consumer;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity has line of sight to another entity")
    public BaseLivingEntityBuilder<T> hasLineOfSight(Predicate<Entity> predicate) {
        hasLineOfSight = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for setting the entity's absorption amount")
    public BaseLivingEntityBuilder<T> setAbsorptionAmount(Consumer<ContextUtils.EntityFloatContext> consumer) {
        setAbsorptionAmount = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity enters combat")
    public BaseLivingEntityBuilder<T> onEnterCombat(Consumer<LivingEntity> runnable) {
        onEnterCombat = runnable;
        return this;
    }

    @Info(value = "Sets the custom logic for when the entity leaves combat")
    public BaseLivingEntityBuilder<T> onLeaveCombat(Consumer<LivingEntity> runnable) {
        onLeaveCombat = runnable;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity is affected by potions")
    public BaseLivingEntityBuilder<T> isAffectedByPotions(Predicate<LivingEntity> predicate) {
        isAffectedByPotions = predicate;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the entity is attackable")
    public BaseLivingEntityBuilder<T> isAttackable(Predicate<LivingEntity> predicate) {
        isAttackable = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity can take a specific item")
    public BaseLivingEntityBuilder<T> canTakeItem(Predicate<ContextUtils.EntityItemLevelContext> predicate) {
        canTakeItem = predicate;
        return this;
    }


    @Info(value = "Sets the custom logic for determining if the entity is sleeping")
    public BaseLivingEntityBuilder<T> isSleeping(Predicate<LivingEntity> supplier) {
        isSleeping = supplier;
        return this;
    }

    @Info(value = "Sets the custom logic for starting sleeping at a specific position")
    public BaseLivingEntityBuilder<T> onStartSleeping(Consumer<ContextUtils.EntityBlockPosContext> consumer) {
        onStartSleeping = consumer;
        return this;
    }

    @Info(value = "Sets the custom logic for stopping sleeping")
    public BaseLivingEntityBuilder<T> onStopSleeping(Consumer<LivingEntity> runnable) {
        onStopSleeping = runnable;
        return this;
    }


    @Info(value = "Sets the custom logic for eating an item in a level")
    public BaseLivingEntityBuilder<T> eat(Consumer<ContextUtils.EntityItemLevelContext> function) {
        eat = function;
        return this;
    }

    @Info(value = "Sets the custom logic for determining if the rider should face forward")
    public BaseLivingEntityBuilder<T> shouldRiderFaceForward(Predicate<ContextUtils.PlayerEntityContext> predicate) {
        shouldRiderFaceForward = predicate;
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
    public BaseLivingEntityBuilder<T> onInteract(Function<ContextUtils.MobInteractContext, @Nullable InteractionResult> f) {
        onInteract = f;
        return this;
    }

    @Info(value = "Sets the custom logic for how far a mob falls before taking damage")
    public BaseLivingEntityBuilder<T> getMaxFallDistance(IntSupplier i) {
        getMaxFallDistance = i;
        return this;
    }


    @Info(value = "Sets the custom onClientRemoval behavior")
    public BaseLivingEntityBuilder<T> onClientRemoval(Consumer<LivingEntity> consumer) {
        onClientRemoval = consumer;
        return this;
    }


    @Info(value = "Sets the custom lavaHurt behavior")
    public BaseLivingEntityBuilder<T> lavaHurt(Consumer<LivingEntity> consumer) {
        lavaHurt = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for entity flapping actions")
    public BaseLivingEntityBuilder<T> onFlap(Consumer<LivingEntity> consumer) {
        onFlap = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the entity dampens vibrations")
    public BaseLivingEntityBuilder<T> dampensVibrations(BooleanSupplier supplier) {
        dampensVibrations = supplier;
        return this;
    }

    @Info(value = "Sets the custom behavior for handling player touch events")
    public BaseLivingEntityBuilder<T> playerTouch(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        playerTouch = consumer;
        return this;
    }

    @Info(value = "Sets the custom behavior for picking entity hit results")
    public BaseLivingEntityBuilder<T> pick(Function<ClipContext, HitResult> function) {
        pick = function;
        return this;
    }

    @Info(value = "Sets the custom behavior for determining if the vehicle health should be shown")
    public BaseLivingEntityBuilder<T> showVehicleHealth(BooleanSupplier supplier) {
        showVehicleHealth = supplier;
        return this;
    }


    @Info(value = "Sets the custom behavior for when the entity is hit by lightning")
    public BaseLivingEntityBuilder<T> thunderHit(Consumer<ContextUtils.ThunderHitContext> consumer) {
        thunderHit = consumer;
        return this;
    }


    @Info(value = "Sets the custom condition for whether the entity is invulnerable to a specific damage source")
    public BaseLivingEntityBuilder<T> isInvulnerableTo(Predicate<ContextUtils.DamageContext> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }


    @Info(value = "Sets whether the entity can change dimensions" +
            "Must return boolean")
    public BaseLivingEntityBuilder<T> canChangeDimensions(Predicate<LivingEntity> supplier) {
        canChangeDimensions = supplier;
        return this;
    }


    @Info(value = "Sets the custom condition for whether the entity may interact with the specified block position" +
            "Must return a boolean")
    public BaseLivingEntityBuilder<T> mayInteract(Predicate<ContextUtils.MayInteractContext> predicate) {
        mayInteract = predicate;
        return this;
    }

    @Info(value = "Sets the custom condition for whether the entity can trample the specified block state at the given position with the given fall distance" +
            "Must return a boolean")
    public BaseLivingEntityBuilder<T> canTrample(Predicate<ContextUtils.CanTrampleContext> predicate) {
        canTrample = predicate;
        return this;
    }

    @Info(value = "Sets the custom behavior for when the entity is removed from the world")
    public BaseLivingEntityBuilder<T> onRemovedFromWorld(Consumer<LivingEntity> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }

    //STUFF
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
        biomeSpawnList.add(new EventBasedSpawnModifier.BiomeSpawn(BiomeSpawnsEventJS.processBiomes(biomes), () -> new MobSpawnSettings.SpawnerData(get(), Weight.of(weight), minCount, maxCount)));
        return this;
    }

    @Info(value = "Adds a new AnimationController to the entity", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "predicate", value = "The predicate for the controller, determines if an animation should continue or not")
    })
    public BaseLivingEntityBuilder<T> addAnimationController(String name, int translationTicksLength, IAnimationPredicateJS<T> predicate) {
        return addAnimationController(name, translationTicksLength, EasingType.LINEAR, predicate, null, null, null);
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
            final AnimationController<E> controller = new AnimationController<>(entity, name, translationTicksLength, predicate.toGecko());
            if (soundListener != null) {
                controller.setSoundKeyframeHandler(event -> soundListener.playSound(new SoundKeyFrameEventJS<>(event)));
            }
            if (particleListener != null) {
                controller.setParticleKeyframeHandler(event -> particleListener.summonParticle(new ParticleKeyFrameEventJS<>(event)));
            }
            if (instructionListener != null) {
                controller.setCustomInstructionKeyframeHandler(event -> instructionListener.executeInstruction(new CustomInstructionKeyframeEventJS<>(event)));
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

        default AnimationController.AnimationStateHandler<E> toGecko() {
            return event -> {
                if (event != null) {
                    AnimationEventJS<E> animationEventJS = new AnimationEventJS<>(event);
                    try {
                        if (animationEventJS == null) return PlayState.STOP;
                    } catch (Exception e) {
                        //throw new RuntimeException(e);
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

        private final AnimationState<E> parent;

        public AnimationEventJS(AnimationState<E> parent) {
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
        @Generics(value = AnimationState.class)
        public void addAnimations(Consumer<AnimationState> builder) {
            final AnimationState animationBuilder = new AnimationState<>(parent.getAnimatable(), parent.getLimbSwing(), parent.getLimbSwingAmount(), parent.getPartialTick(), parent.isMoving());
            builder.accept(animationBuilder);
            parent.getController().setAnimation(animationBuilder.getController().getCurrentRawAnimation());
        }

        @Info(value = "Sets an animation to play")
        public PlayState setAndContinue(String animationName) {
            parent.getController().setAnimation(RawAnimation.begin().thenLoop(animationName));
            return PlayState.CONTINUE;
        }

        @Info(value = """
                Returns any extra data that the event may have
                                
                Usually used by armor animations to know what item is worn
                """)
        public Map<DataTicket<?>, ?> getExtraData() {
            return parent.getExtraData();
        }
    }

    public static class KeyFrameEventJS<E extends LivingEntity & IAnimatableJS, B extends KeyFrameData> {
        @Info(value = "The amount of ticks that have passed in either the current transition or animation, depending on the controller's AnimationState")
        public final double animationTick;
        @Info(value = "The entity being animated")
        public final E entity;
        @Info(value = "The KeyFrame data")
        private final B eventKeyFrame;

        protected KeyFrameEventJS(KeyFrameEvent<E, B> parent) {
            animationTick = parent.getAnimationTick();
            entity = parent.getAnimatable();
            eventKeyFrame = parent.getKeyframeData();
        }
    }


    @FunctionalInterface
    public interface ISoundListenerJS<E extends LivingEntity & IAnimatableJS> {
        void playSound(SoundKeyFrameEventJS<E> event);
    }

    public static class SoundKeyFrameEventJS<E extends LivingEntity & IAnimatableJS> {

        @Info(value = "The name of the sound to play")
        public final String sound;

        public SoundKeyFrameEventJS(SoundKeyframeEvent<E> parent) {
            sound = parent.getKeyframeData().getSound();
        }
    }

    @FunctionalInterface
    public interface IParticleListenerJS<E extends LivingEntity & IAnimatableJS> {
        void summonParticle(ParticleKeyFrameEventJS<E> event);
    }

    public static class ParticleKeyFrameEventJS<E extends LivingEntity & IAnimatableJS> {

        // These aren't documented in geckolib, so I have no idea what they are
        public final String effect;
        public final String locator;
        public final String script;

        public ParticleKeyFrameEventJS(ParticleKeyframeEvent<E> parent) {
            effect = parent.getKeyframeData().getEffect();
            locator = parent.getKeyframeData().getLocator();
            script = parent.getKeyframeData().script();
        }
    }

    @FunctionalInterface
    public interface ICustomInstructionListenerJS<E extends LivingEntity & IAnimatableJS> {
        void executeInstruction(CustomInstructionKeyframeEventJS<E> event);
    }

    public static class CustomInstructionKeyframeEventJS<E extends LivingEntity & IAnimatableJS> {

        @Info(value = "A list of all the custom instructions. In blockbench, each line in the custom instruction box is a separate instruction.")
        public final String instructions;

        public CustomInstructionKeyframeEventJS(CustomInstructionKeyframeEvent<E> parent) {
            instructions = parent.getKeyframeData().getInstructions();
        }
    }

    public enum RenderType {
        SOLID,
        CUTOUT,
        TRANSLUCENT
    }
}
