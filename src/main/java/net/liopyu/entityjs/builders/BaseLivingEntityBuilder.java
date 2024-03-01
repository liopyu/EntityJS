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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.events.BiomeSpawnsEventJS;
import net.liopyu.entityjs.events.RegisterMobCategoryEventJS;
import net.liopyu.entityjs.util.*;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.liopyu.liolib.core.animation.*;
import net.liopyu.liolib.core.animation.AnimationState;
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
import net.minecraft.util.ToFloatFunction;
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
import net.minecraft.world.level.block.Blocks;
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
    public transient boolean canSpawnFarFromPlayer;
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
    public transient Function<LivingEntity, Object> shouldDropLoot;
    public transient Function<ContextUtils.PassengerEntityContext, Object> canAddPassenger;
    public transient Function<LivingEntity, Object> isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient Function<LivingEntity, Object> isImmobile;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Function<LivingEntity, Object> setBlockJumpFactor;
    public transient Function<LivingEntity, Object> blockSpeedFactor;
    public transient Float setSoundVolume;
    public transient Float setWaterSlowDown;
    public transient Object setSwimSound;
    public transient Function<LivingEntity, Object> isFlapping;
    public transient Object setDeathSound;
    public transient RenderType renderType;
    public transient EntityType<?> getType;
    public transient Object mainArm;

    public transient Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch;

    public transient Function<ContextUtils.EntityPoseDimensionsContext, Object> setStandingEyeHeight;

    public transient Consumer<LivingEntity> onDecreaseAirSupply;
    public transient Consumer<LivingEntity> onBlockedByShield;

    public transient Boolean repositionEntityAfterLoad;

    public transient Function<Entity, Object> nextStep;

    public transient Consumer<LivingEntity> onIncreaseAirSupply;

    public transient Function<ContextUtils.HurtContext, Object> setHurtSound;

    public transient Object setSwimSplashSound;


    public transient Function<ContextUtils.EntityTypeEntityContext, Object> canAttackType;

    public transient Function<LivingEntity, Object> scale;
    public transient Boolean rideableUnderWater;

    public transient Function<LivingEntity, Object> shouldDropExperience;

    public transient Function<LivingEntity, Object> experienceReward;


    public transient Consumer<ContextUtils.EntityEquipmentContext> onEquipItem;


    public transient Function<ContextUtils.VisualContext, Object> visibilityPercent;

    public transient Function<ContextUtils.LivingEntityContext, Object> canAttack;

    public transient Function<ContextUtils.OnEffectContext, Object> canBeAffected;

    public transient Function<LivingEntity, Object> invertedHealAndHarm;

    public transient Consumer<ContextUtils.OnEffectContext> onEffectAdded;


    public transient Consumer<ContextUtils.OnEffectContext> onEffectRemoved;

    public transient Consumer<ContextUtils.EntityHealContext> onLivingHeal;


    public transient Consumer<ContextUtils.EntityDamageContext> onHurt;


    public transient Consumer<ContextUtils.DeathContext> onDeath;


    public transient Consumer<ContextUtils.EntityLootContext> dropCustomDeathLoot;


    public transient LivingEntity.Fallsounds fallSounds;
    public transient Object smallFallSound;
    public transient Object largeFallSound;

    public transient Object eatingSound;

    public transient Function<LivingEntity, Object> onClimbable;
    public transient Boolean canBreatheUnderwater;

    public transient Consumer<ContextUtils.EntityFallDamageContext> onLivingFall;

    public transient Consumer<LivingEntity> onSprint;

    public transient Function<LivingEntity, Object> jumpBoostPower;
    public transient Function<ContextUtils.EntityFluidStateContext, Object> canStandOnFluid;


    public transient Function<LivingEntity, Object> isSensitiveToWater;

    public transient Consumer<LivingEntity> onStopRiding;
    public transient Consumer<LivingEntity> rideTick;


    public transient Consumer<ContextUtils.EntityItemEntityContext> onItemPickup;
    public transient Function<ContextUtils.LineOfSightContext, Object> hasLineOfSight;

    public transient Consumer<LivingEntity> onEnterCombat;
    public transient Consumer<LivingEntity> onLeaveCombat;

    public transient Function<LivingEntity, Object> isAffectedByPotions;

    public transient Function<LivingEntity, Object> isAttackable;

    public transient Function<ContextUtils.EntityItemLevelContext, Object> canTakeItem;

    public transient Function<LivingEntity, Object> isSleeping;
    public transient Consumer<ContextUtils.EntityBlockPosContext> onStartSleeping;
    public transient Consumer<LivingEntity> onStopSleeping;

    public transient Consumer<ContextUtils.EntityItemLevelContext> eat;

    public transient Function<ContextUtils.PlayerEntityContext, Object> shouldRiderFaceForward;

    public transient Function<LivingEntity, Object> canFreeze;
    public transient Function<LivingEntity, Object> isCurrentlyGlowing;
    public transient Function<LivingEntity, Object> canDisableShield;
    public transient Function<LivingEntity, Object> setMaxFallDistance;
    public transient Function<ContextUtils.MobInteractContext, Object> onInteract;

    public transient Consumer<LivingEntity> onClientRemoval;
    public transient Consumer<LivingEntity> onAddedToWorld;
    public transient Consumer<LivingEntity> lavaHurt;
    public transient Consumer<LivingEntity> onFlap;
    public transient Function<LivingEntity, Object> dampensVibrations;

    public transient Consumer<ContextUtils.PlayerEntityContext> playerTouch;
    public transient Function<LivingEntity, Object> showVehicleHealth;

    public transient Consumer<ContextUtils.ThunderHitContext> thunderHit;
    public transient Function<ContextUtils.DamageContext, Object> isInvulnerableTo;
    public transient Function<LivingEntity, Object> canChangeDimensions;
    public transient Function<ContextUtils.CalculateFallDamageContext, Object> calculateFallDamage;
    public transient Function<ContextUtils.MayInteractContext, Object> mayInteract;
    public transient Function<ContextUtils.CanTrampleContext, Object> canTrample;
    public transient Consumer<LivingEntity> onRemovedFromWorld;
    public transient Consumer<LivingEntity> onLivingJump;
    public transient Consumer<LivingEntity> aiStep;

    public transient Consumer<AttributeSupplier.Builder> attributes;
    public SpawnPlacements.Type placementType;
    public Heightmap.Types heightMap;
    public SpawnPlacements.SpawnPredicate<? extends Entity> spawnPredicate;
    public static final List<BaseLivingEntityBuilder<?>> spawnList = new ArrayList<>();
    public static final List<EventBasedSpawnModifier.BiomeSpawn> biomeSpawnList = new ArrayList<>();

    public transient Consumer<ContextUtils.RenderContext> render;
    public transient MobType mobType;
    public transient Function<LivingEntity, Object> isFreezing;
    public transient Function<ContextUtils.CollidingEntityContext, Object> canCollideWith;
    public transient Boolean defaultDeathPose;

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
        updateInterval = 1;
        mobCategory = MobCategory.MISC;
        modelResource = t -> t.getBuilder().newID("geo/entity/", ".geo.json");
        textureResource = t -> t.getBuilder().newID("textures/entity/", ".png");
        animationResource = t -> t.getBuilder().newID("animations/entity/", ".animation.json");
        isPushable = true;
        animationSuppliers = new ArrayList<>();
        isAlwaysExperienceDropper = false;
        setSoundVolume = 1.0f;
        setWaterSlowDown = 0.8f;
        repositionEntityAfterLoad = true;
        rideableUnderWater = false;
        canBreatheUnderwater = false;
        renderType = RenderType.CUTOUT;
        mainArm = HumanoidArm.RIGHT;
        mobType = MobType.UNDEFINED;
        defaultDeathPose = true;
    }

    @Info(value = """
            Boolean determining if the entity will turn sideways on death.
            Defaults to true.
            Example usage:
            ```javascript
            entityBuilder.defaultDeathPose(false);
            ```
            """)
    public BaseLivingEntityBuilder<T> defaultDeathPose(boolean defaultDeathPose) {
        this.defaultDeathPose = defaultDeathPose;
        return this;
    }

    @Info(value = """
            Function determining if the entity may collide with another entity
            using the ContextUtils.CollidingEntityContext which has this entity and the
            one colliding with this entity.
                        
            Example usage:
            ```javascript
            entityBuilder.canCollideWith(context => {
                return true //Some Boolean value determining whether the entity may collide with another
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canCollideWith(Function<ContextUtils.CollidingEntityContext, Object> canCollideWith) {
        this.canCollideWith = canCollideWith;
        return this;
    }

    @Info(value = """
            Sets whether the entity is rideable underwater.
                        
            Example usage:
            ```javascript
            entityBuilder.rideableUnderWater(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> rideableUnderWater(boolean rideableUnderWater) {
        this.rideableUnderWater = rideableUnderWater;
        return this;
    }

    @Info(value = """
            Defines the Mob's Type
            Examples: 'undead', 'water', 'arthropod', 'undefined', 'illager'
                        
            Example usage:
            ```javascript
            entityBuilder.mobType('undead');
            ```
            """)
    public BaseLivingEntityBuilder<T> mobType(Object mt) {
        if (mt instanceof String string) {
            switch (string.toLowerCase()) {
                case "undead":
                    this.mobType = MobType.UNDEAD;
                    break;
                case "arthropod":
                    this.mobType = MobType.ARTHROPOD;
                    break;
                case "undefined":
                    this.mobType = MobType.UNDEFINED;
                    break;
                case "illager":
                    this.mobType = MobType.ILLAGER;
                    break;
                case "water":
                    this.mobType = MobType.WATER;
                    break;
                default:
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for mobType: " + mt + ". Example: \"undead\"");
                    break;
            }
        } else if (mt instanceof MobType type) {
            this.mobType = type;
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for mobType: " + mt + ". Example: \"undead\"");

        return this;
    }

    @Info(value = """
            Defines in what condition the entity will start freezing.
                        
            Example usage:
            ```javascript
            entityBuilder.isFreezing(entity => {
                return true;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isFreezing(Function<LivingEntity, Object> isFreezing) {
        this.isFreezing = isFreezing;
        return this;
    }

    @Info(value = """
            Defines logic to render the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.render(context => {
                // Define logic to render the entity
                if (context.entity.isBaby()) {
                    context.poseStack.scale(0.5, 0.5, 0.5);
                }
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> render(Consumer<ContextUtils.RenderContext> render) {
        this.render = render;
        return this;
    }


    @Info(value = """
            Sets the main arm of the entity. Defaults to 'right'.
                        
            @param arm The main arm of the entity. Accepts values "left" or "right".
                        
            Example usage:
            ```javascript
            entityBuilder.mainArm("left");
            ```
            """)
    public BaseLivingEntityBuilder<T> mainArm(Object arm) {
        if (arm instanceof HumanoidArm) {
            this.mainArm = (HumanoidArm) arm;
            return this;
        } else if (arm instanceof String string) {
            switch (string.toLowerCase()) {
                case "left":
                    this.mainArm = HumanoidArm.LEFT;
                    break;
                case "right":
                    this.mainArm = HumanoidArm.RIGHT;
                    break;
                default:
                    break;
            }
        } else {
            this.mainArm = HumanoidArm.RIGHT;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for mainArm: " + arm + ". Example: \"left\"");
        }
        return this;
    }


    @Info(value = """
            Sets the hit box of the entity type.
                        
            @param width The width of the entity, defaults to 1.
            @param height The height of the entity, defaults to 1.
                        
            Example usage:
            ```javascript
            entityBuilder.sized(2, 3);
            ```
            """)
    public BaseLivingEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    @Info(value = """
            Determines if the entity should serialize its data. Defaults to true.
                        
            Example usage:
            ```javascript
            entityBuilder.saves(false);
            ```
            """)
    public BaseLivingEntityBuilder<T> saves(boolean shouldSave) {
        this.save = shouldSave;
        return this;
    }


    @Info(value = """
            Sets whether the entity is immune to fire damage.
                        
            Example usage:
            ```javascript
            entityBuilder.fireImmune(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> fireImmune(boolean isFireImmune) {
        this.fireImmune = isFireImmune;
        return this;
    }

    @Info(value = """
            Sets a consumer to handle custom lerping logic for the living entity.
                
            @param lerpTo The consumer to handle the custom lerping logic.
                
            The consumer should take a LerpToContext as a parameter, providing information about the lerping operation, including the target position, yaw, pitch, increment count, teleport flag, and the entity itself.
                
            Example usage:
            ```javascript
            baseLivingEntityBuilder.lerpTo(context => {
                // Custom lerping logic for the living entity
                const { x, y, z, yaw, pitch, posRotationIncrements, teleport, entity } = context;
                // Perform custom lerping operations using the provided context
                // For example, you can smoothly move the entity from its current position to the target position
                entity.setPositionAndRotation(x, y, z, yaw, pitch);
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> lerpTo(Consumer<ContextUtils.LerpToContext> lerpTo) {
        this.lerpTo = lerpTo;
        return this;
    }


    @Info(value = """
            Sets the list of block names to which the entity is immune.
                        
            Example usage:
            ```javascript
            entityBuilder.immuneTo("minecraft:stone", "minecraft:dirt");
            ```
            """)
    public BaseLivingEntityBuilder<T> immuneTo(String... blockNames) {
        this.immuneTo = Arrays.stream(blockNames)
                .map(ResourceLocation::new)
                .toArray(ResourceLocation[]::new);
        return this;
    }


    @Info(value = """
            Sets whether the entity can spawn far from the player.
                        
            Example usage:
            ```javascript
            entityBuilder.canSpawnFarFromPlayer(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> canSpawnFarFromPlayer(boolean canSpawnFar) {
        this.spawnFarFromPlayer = canSpawnFar;
        return this;
    }


    @Info(value = """
            Sets the block jump factor for the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.setBlockJumpFactor(entity => {
                //Set the jump factor for the entity through context
                return 1 //some float value;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> setBlockJumpFactor(Function<LivingEntity, Object> blockJumpFactor) {
        setBlockJumpFactor = blockJumpFactor;
        return this;
    }


    @Info(value = """
            Sets the water slowdown factor for the entity. Defaults to 0.8.
                        
            Example usage:
            ```javascript
            entityBuilder.setWaterSlowDown(0.6);
            ```
            """)
    public BaseLivingEntityBuilder<T> setWaterSlowDown(float slowdownFactor) {
        this.setWaterSlowDown = slowdownFactor;
        return this;
    }


    @Info(value = """
            Sets the overall sound volume for the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.setSoundVolume(0.5);
            ```
            """)
    public BaseLivingEntityBuilder<T> setSoundVolume(float volume) {
        this.setSoundVolume = volume;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity should drop loot upon death.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity whose loot dropping behavior is being determined.
            It returns a Boolean indicating whether the entity should drop loot.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldDropLoot(entity => {
                // Define logic to determine whether the entity should drop loot
                // Use information about the LivingEntity provided by the context.
                return // Some Boolean value indicating whether the entity should drop loot;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> shouldDropLoot(Function<LivingEntity, Object> b) {
        this.shouldDropLoot = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed during the living entity's AI step.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            allowing customization of the AI behavior.
                        
            Example usage:
            ```javascript
            entityBuilder.aiStep(entity => {
                // Custom logic to be executed during the living entity's AI step
                // Access and modify information about the entity using the provided context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> aiStep(Consumer<LivingEntity> aiStep) {
        this.aiStep = aiStep;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity jumps.
                        
            Example usage:
            ```javascript
            entityBuilder.onLivingJump(entity => {
                // Custom logic to handle the entity's jump action
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onLivingJump(Consumer<LivingEntity> onJump) {
        this.onLivingJump = onJump;
        return this;
    }


    @Info(value = """
            Sets the client tracking range for the entity.
            Defaults to 5.
            Example usage:
            ```javascript
            entityBuilder.clientTrackingRange(64); // Set the client tracking range to 64 blocks
            ```
            """)
    public BaseLivingEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }


    @Info(value = """
            Sets the update interval for the entity.
            Defaults to 1 tick.
            Example usage:
            ```javascript
            entityBuilder.updateInterval(20); // Set the update interval to 20 ticks
            ```
            """)
    public BaseLivingEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @HideFromJS
    public static MobCategory stringToMobCategory(String category) {
        return switch (category) {
            case "monster" -> MobCategory.MONSTER;
            case "creature" -> MobCategory.CREATURE;
            case "ambient" -> MobCategory.AMBIENT;
            case "water_creature" -> MobCategory.WATER_CREATURE;
            case "misc" -> MobCategory.MISC;
            default -> MobCategory.MISC;
        };
    }

    @Info(value = """
            Sets the mob category for the entity.
            Available options: 'monster', 'creature', 'ambient', 'water_creature', 'misc'.
            Defaults to 'misc'.
                        
            Example usage:
            ```javascript
            entityBuilder.mobCategory('monster');
            ```
            """)
    public BaseLivingEntityBuilder<T> mobCategory(String category) {
        mobCategory = stringToMobCategory(category);
        return this;
    }


    @Info(value = """
            Sets a function to determine the model resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the model based on information about the entity.
            The default behavior returns <namespace>:geo/entity/<path>.geo.json.
                        
            Example usage:
            ```javascript
            entityBuilder.modelResource(entity => {
                // Define logic to determine the model resource for the entity
                // Use information about the entity provided by the context.
                return "kubejs:geo/entity/wyrm.geo.json" // Some ResourceLocation representing the model resource;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> modelResource(Function<T, Object> function) {
        modelResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid model resource: " + obj + "Defaulting to " + entity.getBuilder().newID("geo/entity/", ".geo.json"));
                return entity.getBuilder().newID("geo/entity/", ".geo.json");
            }
        };
        return this;
    }


    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/entity/<path>.png.
                        
            Example usage:
            ```javascript
            entityBuilder.textureResource(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return "kubejs:textures/entity/wyrm.png" // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid texture resource: " + obj + "Defaulting to " + entity.getBuilder().newID("textures/entity/", ".png"));
                return entity.getBuilder().newID("textures/entity/", ".png");
            }
        };
        return this;
    }


    @Info(value = """
            Sets a function to determine the animation resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the animations based on information about the entity.
            The default behavior returns <namespace>:animations/<path>.animation.json.
                        
            Example usage:
            ```javascript
            entityBuilder.animationResource(entity => {
                // Define logic to determine the animation resource for the entity
                // Use information about the entity provided by the context.
                //return some ResourceLocation representing the animation resource;
                return "kubejs:animations/entity/wyrm.animation.json" // Some ResourceLocation representing the animation resource;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> animationResource(Function<T, Object> function) {
        animationResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid animation resource: " + obj + ". Defaulting to " + entity.getBuilder().newID("animations/entity/", ".animation.json"));
                return entity.getBuilder().newID("animations/entity/", ".animation.json");
            }
        };
        return this;
    }


    @Info(value = """
            Sets whether the entity is pushable.
                        
            Example usage:
            ```javascript
            entityBuilder.isPushable(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> isPushable(boolean b) {
        isPushable = b;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if a passenger can be added to the entity.
                        
            @param predicate The predicate to check if a passenger can be added.
                        
            Example usage:
            ```javascript
            entityBuilder.canAddPassenger(context => {
                // Custom logic to determine if a passenger can be added to the entity
                return true; 
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canAddPassenger(Function<ContextUtils.PassengerEntityContext, Object> predicate) {
        canAddPassenger = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity is affected by fluids.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity whose interaction with fluids is being determined.
            It returns a Boolean indicating whether the entity is affected by fluids.
                        
            Example usage:
            ```javascript
            entityBuilder.isAffectedByFluids(entity => {
                // Define logic to determine whether the entity is affected by fluids
                // Use information about the LivingEntity provided by the context.
                return // Some Boolean value indicating whether the entity is affected by fluids;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isAffectedByFluids(Function<LivingEntity, Object> b) {
        isAffectedByFluids = b;
        return this;
    }


    @Info(value = """
            Sets whether the entity is summonable.
                        
            Example usage:
            ```javascript
            entityBuilder.setSummonable(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity is immobile.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity whose immobility is being determined.
            It returns a Boolean indicating whether the entity is immobile.
                        
            Example usage:
            ```javascript
            entityBuilder.isImmobile(entity => {
                // Define logic to determine whether the entity is immobile
                // Use information about the LivingEntity provided by the context.
                return // Some Boolean value indicating whether the entity is immobile;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isImmobile(Function<LivingEntity, Object> b) {
        isImmobile = b;
        return this;
    }


    @Info(value = """
            Sets whether the entity is always considered as an experience dropper.
                        
            Example usage:
            ```javascript
            entityBuilder.isAlwaysExperienceDropper(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> isAlwaysExperienceDropper(boolean b) {
        isAlwaysExperienceDropper = b;
        return this;
    }


    @Info(value = """
            Sets a function to calculate fall damage for the entity.
            The provided Function accepts a {@link ContextUtils.CalculateFallDamageContext} parameter,
            representing the context of the fall damage calculation.
            It returns an Integer representing the calculated fall damage.
                        
            Example usage:
            ```javascript
            entityBuilder.calculateFallDamage(context => {
                // Define logic to calculate and return the fall damage for the entity
                // Use information about the CalculateFallDamageContext provided by the context.
                return // Some Integer value representing the calculated fall damage;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> calculateFallDamage(Function<ContextUtils.CalculateFallDamageContext, Object> calculation) {
        calculateFallDamage = calculation;
        return this;
    }


    @Info(value = """
            Sets the death sound for the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.setDeathSound("minecraft:entity.generic.death");
            ```
            """)
    public BaseLivingEntityBuilder<T> setDeathSound(Object sound) {
        if (sound instanceof String) setDeathSound = new ResourceLocation((String) sound);
        else if (sound instanceof ResourceLocation) setDeathSound = (ResourceLocation) sound;
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setDeathSound. Value: " + sound + ". Must be a ResourceLocation. Example: \"minecraft:entity.generic.death\"");
        return this;
    }


    @Info(value = """
            Sets the swim sound for the entity using a string representation.
                        
            Example usage:
            ```javascript
            entityBuilder.setSwimSound("minecraft:entity.generic.swim");
            ```
            """)
    public BaseLivingEntityBuilder<T> setSwimSound(Object sound) {
        if (sound instanceof String) setSwimSound = new ResourceLocation((String) sound);
        else if (sound instanceof ResourceLocation) setSwimSound = (ResourceLocation) sound;
        else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setSwimSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.swim\"");

            setSwimSound = new ResourceLocation("minecraft:entity.generic.swim");
        }
        return this;
    }


    @Info(value = """
            Sets the swim splash sound for the entity using either a string representation or a ResourceLocation object.
                        
            Example usage:
            ```javascript
            entityBuilder.setSwimSplashSound("minecraft:entity.generic.splash");
            ```
            """)
    public BaseLivingEntityBuilder<T> setSwimSplashSound(Object sound) {
        if (sound instanceof String) {
            setSwimSplashSound = new ResourceLocation((String) sound);
        } else if (sound instanceof ResourceLocation) {
            setSwimSplashSound = (ResourceLocation) sound;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setSwimSplashSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.splash\"");

            setSwimSplashSound = new ResourceLocation("minecraft", "entity/generic/splash");
        }
        return this;
    }


    @Info(value = """
            Sets a function to determine the block speed factor of the entity.
            The provided Function accepts a {@link LivingEntity} parameter,
            representing the entity whose block speed factor is being determined.
            It returns a Float representing the block speed factor.
                        
            Example usage:
            ```javascript
            entityBuilder.blockSpeedFactor(entity => {
                // Define logic to calculate and return the block speed factor for the entity
                // Use information about the LivingEntity provided by the context.
                return // Some Float value representing the block speed factor;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> blockSpeedFactor(Function<LivingEntity, Object> callback) {
        blockSpeedFactor = callback;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity is currently flapping.
            The provided Function accepts a {@link LivingEntity} parameter,
            representing the entity whose flapping status is being determined.
            It returns a Boolean indicating whether the entity is flapping.
                        
            Example usage:
            ```javascript
            entityBuilder.isFlapping(entity => {
                // Define logic to determine whether the entity is currently flapping
                // Use information about the LivingEntity provided by the context.
                return // Some Boolean value indicating whether the entity is flapping;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isFlapping(Function<LivingEntity, Object> b) {
        this.isFlapping = b;
        return this;
    }


    public transient Consumer<LivingEntity> tick;

    @Info(value = """
            Sets a callback function to be executed during each tick of the entity.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is being ticked.
                        
            Example usage:
            ```javascript
            entityBuilder.tick(entity => {
                // Define custom logic for handling during each tick of the entity
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> tick(Consumer<LivingEntity> tickCallback) {
        this.tick = tickCallback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is added to the world.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is added to the world.
                        
            Example usage:
            ```javascript
            entityBuilder.onAddedToWorld(entity => {
                // Define custom logic for handling when the entity is added to the world
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onAddedToWorld(Consumer<LivingEntity> onAddedToWorldCallback) {
        this.onAddedToWorld = onAddedToWorldCallback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity automatically attacks on touch.
            The provided Consumer accepts a {@link ContextUtils.AutoAttackContext} parameter,
            representing the context of the auto-attack when the entity touches another entity.
                        
            Example usage:
            ```javascript
            entityBuilder.doAutoAttackOnTouch(context => {
                // Define custom logic for handling when the entity automatically attacks on touch
                // Use information about the AutoAttackContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> doAutoAttackOnTouch(Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch) {
        this.doAutoAttackOnTouch = doAutoAttackOnTouch;
        return this;
    }


    @Info(value = """
            Sets a function to determine the standing eye height of the entity.
            The provided Function accepts a {@link ContextUtils.EntityPoseDimensionsContext} parameter,
            representing the context of the entity's pose and dimensions when standing.
            It returns a Float representing the standing eye height.
                        
            Example usage:
            ```javascript
            entityBuilder.setStandingEyeHeight(context => {
                // Define logic to calculate and return the standing eye height for the entity
                // Use information about the EntityPoseDimensionsContext provided by the context.
                return // Some Float value representing the standing eye height;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> setStandingEyeHeight(Function<ContextUtils.EntityPoseDimensionsContext, Object> setStandingEyeHeight) {
        this.setStandingEyeHeight = setStandingEyeHeight;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's air supply decreases.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity whose air supply is being decreased.
                        
            Example usage:
            ```javascript
            entityBuilder.onDecreaseAirSupply(entity => {
                // Define custom logic for handling when the entity's air supply decreases
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onDecreaseAirSupply(Consumer<LivingEntity> onDecreaseAirSupply) {
        this.onDecreaseAirSupply = onDecreaseAirSupply;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is blocked by a shield.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is blocked by a shield.
                        
            Example usage:
            ```javascript
            entityBuilder.onBlockedByShield(entity => {
                // Define custom logic for handling when the entity is blocked by a shield
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onBlockedByShield(Consumer<LivingEntity> onBlockedByShield) {
        this.onBlockedByShield = onBlockedByShield;
        return this;
    }


    @Info(value = """
            Sets whether to reposition the entity after loading.
                        
            Example usage:
            ```javascript
            entityBuilder.repositionEntityAfterLoad(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> repositionEntityAfterLoad(boolean customRepositionEntityAfterLoad) {
        this.repositionEntityAfterLoad = customRepositionEntityAfterLoad;
        return this;
    }


    @Info(value = """
            Sets a function to determine the next step distance for the entity.
            The provided Function accepts a {@link Entity} parameter,
            representing the entity whose next step distance is being determined.
            It returns a Float representing the next step distance.
                        
            Example usage:
            ```javascript
            entityBuilder.nextStep(entity => {
                // Define logic to calculate and return the next step distance for the entity
                // Use information about the Entity provided by the context.
                return // Some Float value representing the next step distance;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> nextStep(Function<Entity, Object> nextStep) {
        this.nextStep = nextStep;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's air supply increases.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity whose air supply is being increased.
                        
            Example usage:
            ```javascript
            entityBuilder.onIncreaseAirSupply(entity => {
                // Define custom logic for handling when the entity's air supply increases
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onIncreaseAirSupply(Consumer<LivingEntity> onIncreaseAirSupply) {
        this.onIncreaseAirSupply = onIncreaseAirSupply;
        return this;
    }


    @Info(value = """
            Sets a function to determine the custom hurt sound of the entity.
            The provided Function accepts a {@link ContextUtils.HurtContext} parameter,
            ```javascript
            entityBuilder.setHurtSound(context => {
                // Custom logic to determine the hurt sound for the entity
                // You can use information from the HurtContext to customize the sound based on the context
                const { entity, damageSource } = context;
                // Determine the hurt sound based on the type of damage source
                switch (damageSource.getType()) {
                    case "fire":
                        return "minecraft:entity.generic.burn";
                    case "fall":
                        return "minecraft:entity.generic.hurt";
                    case "drown":
                        return "minecraft:entity.generic.hurt";
                    case "explosion":
                        return "minecraft:entity.generic.explode";
                    default:
                        return "minecraft:entity.generic.explode";
                }
            })
            ```
            """)
    public BaseLivingEntityBuilder<T> setHurtSound(Function<ContextUtils.HurtContext, Object> sound) {
        this.setHurtSound = sound;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can attack a specific entity type.
            The provided Predicate accepts a {@link ContextUtils.EntityTypeEntityContext} parameter,
            representing the context of the entity attacking a specific entity type.
                        
            Example usage:
            ```javascript
            entityBuilder.canAttackType(context => {
                // Define conditions to check if the entity can attack the specified entity type
                // Use information about the EntityTypeEntityContext provided by the context.
                return // Some boolean condition indicating if the entity can attack the specified entity type;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canAttackType(Function<ContextUtils.EntityTypeEntityContext, Object> canAttackType) {
        this.canAttackType = canAttackType;
        return this;
    }


    @Info(value = """
            Sets a function to determine the custom hitbox scale of the entity.
            The provided Function accepts a {@link LivingEntity} parameter,
            representing the entity whose scale is being determined.
            It returns a Float representing the custom scale.
                        
            Example usage:
            ```javascript
            entityBuilder.scale(entity => {
                // Define logic to calculate and return the custom scale for the entity
                // Use information about the LivingEntity provided by the context.
                return // Some Float value;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> scale(Function<LivingEntity, Object> customScale) {
        this.scale = customScale;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity should drop experience upon death.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity whose experience drop is being determined.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldDropExperience(entity => {
                // Define conditions to check if the entity should drop experience upon death
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity should drop experience;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> shouldDropExperience(Function<LivingEntity, Object> p) {
        this.shouldDropExperience = p;
        return this;
    }


    @Info(value = """
            Sets a function to determine the experience reward for killing the entity.
            The provided Function accepts a {@link LivingEntity} parameter,
            representing the entity whose experience reward is being determined.
            It returns an Integer representing the experience reward.
                        
            Example usage:
            ```javascript
            entityBuilder.experienceReward(killedEntity => {
                // Define logic to calculate and return the experience reward for the killedEntity
                // Use information about the LivingEntity provided by the context.
                return // Some Integer value representing the experience reward;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> experienceReward(Function<LivingEntity, Object> experienceReward) {
        this.experienceReward = experienceReward;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity equips an item.
            The provided Consumer accepts a {@link ContextUtils.EntityEquipmentContext} parameter,
            representing the context of the entity equipping an item.
                        
            Example usage:
            ```javascript
            entityBuilder.onEquipItem(context => {
                // Define custom logic for handling when the entity equips an item
                // Use information about the EntityEquipmentContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onEquipItem(Consumer<ContextUtils.EntityEquipmentContext> onEquipItem) {
        this.onEquipItem = onEquipItem;
        return this;
    }


    @Info(value = """
            Sets a function to determine the visibility percentage of the entity.
            The provided Function accepts a {@link ContextUtils.VisualContext} parameter,
            representing both the entity whose visibility percentage is being determined
            and the the builder entity who is being looked at.
            It returns a Double representing the visibility percentage.
                        
            Example usage:
            ```javascript
            entityBuilder.visibilityPercent(context => {
                // Define logic to calculate and return the visibility percentage for the targetEntity
                // Use information about the Entity provided by the context.
                return // Some Double value representing the visibility percentage;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> visibilityPercent(Function<ContextUtils.VisualContext, Object> visibilityPercent) {
        this.visibilityPercent = visibilityPercent;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can attack another entity.
            The provided Predicate accepts a {@link ContextUtils.LivingEntityContext} parameter,
            representing the entity that may be attacked.
                        
            Example usage:
            ```javascript
            entityBuilder.canAttack(context => {
                // Define conditions to check if the entity can attack the targetEntity
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity can attack the targetEntity;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canAttack(Function<ContextUtils.LivingEntityContext, Object> customCanAttack) {
        this.canAttack = customCanAttack;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can be affected by an effect.
            The provided Predicate accepts a {@link ContextUtils.OnEffectContext} parameter,
            representing the context of the effect that may affect the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.canBeAffected(context => {
                // Define conditions to check if the entity can be affected by the effect
                // Use information about the OnEffectContext provided by the context.
                return // Some boolean condition indicating if the entity can be affected by an effect;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canBeAffected(Function<ContextUtils.OnEffectContext, Object> predicate) {
        canBeAffected = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the entity has inverted heal and harm behavior.
                        
            @param invertedHealAndHarm The predicate to check for inverted heal and harm behavior.
                        
            Example usage:
            ```javascript
            entityBuilder.invertedHealAndHarm(entity => {
                // Custom logic to determine if the entity has inverted heal and harm behavior
                return true; // Replace with your custom boolean condition
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> invertedHealAndHarm(Function<LivingEntity, Object> invertedHealAndHarm) {
        this.invertedHealAndHarm = invertedHealAndHarm;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when an effect is added to the entity.
            The provided Consumer accepts a {@link ContextUtils.OnEffectContext} parameter,
            representing the context of the effect being added to the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.onEffectAdded(context => {
                // Define custom logic for handling when an effect is added to the entity
                // Use information about the OnEffectContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onEffectAdded(Consumer<ContextUtils.OnEffectContext> consumer) {
        onEffectAdded = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity receives healing.
            The provided Consumer accepts a {@link ContextUtils.EntityHealContext} parameter,
            representing the context of the entity receiving healing.
            Very similar to {@link ForgeEventFactory.onLivingHeal}
                        
            Example usage:
            ```javascript
            entityBuilder.onLivingHeal(context => {
                // Define custom logic for handling when the entity receives healing
                // Use information about the EntityHealContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onLivingHeal(Consumer<ContextUtils.EntityHealContext> callback) {
        onLivingHeal = callback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when an effect is removed from the entity.
            The provided Consumer accepts a {@link ContextUtils.OnEffectContext} parameter,
            representing the context of the effect being removed from the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.onEffectRemoved(context => {
                // Define custom logic for handling when an effect is removed from the entity
                // Use information about the OnEffectContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onEffectRemoved(Consumer<ContextUtils.OnEffectContext> consumer) {
        onEffectRemoved = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hurt.
            The provided Consumer accepts a {@link ContextUtils.EntityDamageContext} parameter,
            representing the context of the entity being hurt.
                        
            Example usage:
            ```javascript
            entityBuilder.onHurt(context => {
                // Define custom logic for handling when the entity is hurt
                // Use information about the EntityDamageContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onHurt(Consumer<ContextUtils.EntityDamageContext> predicate) {
        onHurt = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity dies.
            The provided Consumer accepts a {@link ContextUtils.DeathContext} parameter,
            representing the context of the entity's death.
                        
            Example usage:
            ```javascript
            entityBuilder.onDeath(context => {
                // Define custom logic for handling the entity's death
                // Use information about the DeathContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onDeath(Consumer<ContextUtils.DeathContext> consumer) {
        onDeath = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity drops custom loot upon death.
            The provided Consumer accepts a {@link ContextUtils.EntityLootContext} parameter,
            representing the context of the entity's death and loot dropping.
                        
            Example usage:
            ```javascript
            entityBuilder.dropCustomDeathLoot(context => {
                // Define custom logic for handling the entity dropping custom loot upon death
                // Use information about the EntityLootContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> dropCustomDeathLoot(Consumer<ContextUtils.EntityLootContext> consumer) {
        dropCustomDeathLoot = consumer;
        return this;
    }


    @Info(value = """
            Sets the sound resource locations for small and large falls of the entity using either string representations or ResourceLocation objects.
                        
            Example usage:
            ```javascript
            entityBuilder.fallSounds("minecraft:entity.generic.small_fall",
                "minecraft:entity.generic.large_fall");
            ```
            """)
    public BaseLivingEntityBuilder<T> fallSounds(Object smallFallSound, Object largeFallSound) {
        if (smallFallSound instanceof String) {
            this.smallFallSound = new ResourceLocation((String) smallFallSound);
        } else if (smallFallSound instanceof ResourceLocation) {
            this.smallFallSound = (ResourceLocation) smallFallSound;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for smallFallSound. Value: " + smallFallSound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.small_fall\"");
            this.smallFallSound = new ResourceLocation("minecraft", "entity/generic/small_fall");
        }

        if (largeFallSound instanceof String) {
            this.largeFallSound = new ResourceLocation((String) largeFallSound);
        } else if (largeFallSound instanceof ResourceLocation) {
            this.largeFallSound = (ResourceLocation) largeFallSound;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for largeFallSound. Value: " + largeFallSound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.large_fall\"");
            this.largeFallSound = new ResourceLocation("minecraft", "entity/generic/large_fall");
        }

        return this;
    }


    @Info(value = """
            Sets the sound resource location for the entity's eating sound using either a string representation or a ResourceLocation object.
                        
            Example usage:
            ```javascript
            entityBuilder.eatingSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public BaseLivingEntityBuilder<T> eatingSound(Object sound) {
        if (sound instanceof String) {
            this.eatingSound = new ResourceLocation((String) sound);
        } else if (sound instanceof ResourceLocation) {
            this.eatingSound = (ResourceLocation) sound;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for eatingSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.zombie.ambient\"");
            this.eatingSound = new ResourceLocation("minecraft", "entity/zombie/ambient");
        }
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is on a climbable surface.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for being on a climbable surface.
                        
            Example usage:
            ```javascript
            entityBuilder.onClimbable(entity => {
                // Define conditions to check if the entity is on a climbable surface
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity is on a climbable surface;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onClimbable(Function<LivingEntity, Object> predicate) {
        onClimbable = predicate;
        return this;
    }


    @Info(value = """
            Sets whether the entity can breathe underwater.
                        
            Example usage:
            ```javascript
            entityBuilder.canBreatheUnderwater(true);
            ```
            """)
    public BaseLivingEntityBuilder<T> canBreatheUnderwater(boolean canBreatheUnderwater) {
        this.canBreatheUnderwater = canBreatheUnderwater;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the living entity falls and takes damage.
            The provided Consumer accepts a {@link ContextUtils.EntityFallDamageContext} parameter,
            representing the context of the entity falling and taking fall damage.
                        
            Example usage:
            ```javascript
            entityBuilder.onLivingFall(context => {
                // Define custom logic for handling when the living entity falls and takes damage
                // Use information about the EntityFallDamageContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onLivingFall(Consumer<ContextUtils.EntityFallDamageContext> c) {
        onLivingFall = c;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity starts sprinting.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that has started sprinting.
                        
            Example usage:
            ```javascript
            entityBuilder.onSprint(entity => {
                // Define custom logic for handling when the entity starts sprinting
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onSprint(Consumer<LivingEntity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = """
            Sets the jump boost power for the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.jumpBoostPower(entity => {
                return //some double value
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> jumpBoostPower(Function<LivingEntity, Object> jumpBoostPower) {
        this.jumpBoostPower = jumpBoostPower;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can stand on a fluid.
            The provided Predicate accepts a {@link ContextUtils.EntityFluidStateContext} parameter,
            representing the context of the entity potentially standing on a fluid.
                        
            Example usage:
            ```javascript
            entityBuilder.canStandOnFluid(context => {
                // Define conditions for the entity to be able to stand on a fluid
                // Use information about the EntityFluidStateContext provided by the context.
                return // Some boolean condition indicating if the entity can stand on the fluid;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canStandOnFluid(Function<ContextUtils.EntityFluidStateContext, Object> predicate) {
        canStandOnFluid = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is sensitive to water.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for sensitivity to water.
                        
            Example usage:
            ```javascript
            entityBuilder.isSensitiveToWater(entity => {
                // Define conditions to check if the entity is sensitive to water
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity is sensitive to water;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isSensitiveToWater(Function<LivingEntity, Object> predicate) {
        isSensitiveToWater = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops riding.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that has stopped being ridden.
                        
            Example usage:
            ```javascript
            entityBuilder.onStopRiding(entity => {
                // Define custom logic for handling when the entity stops being ridden
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onStopRiding(Consumer<LivingEntity> callback) {
        onStopRiding = callback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed during each tick when the entity is being ridden.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is being ridden.
                        
            Example usage:
            ```javascript
            entityBuilder.rideTick(entity => {
                // Define custom logic for handling each tick when the entity is being ridden
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> rideTick(Consumer<LivingEntity> callback) {
        rideTick = callback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity picks up an item.
            The provided Consumer accepts a {@link ContextUtils.EntityItemEntityContext} parameter,
            representing the context of the entity picking up an item with another entity.
                        
            Example usage:
            ```javascript
            entityBuilder.onItemPickup(context => {
                // Define custom logic for handling the entity picking up an item
                // Use information about the EntityItemEntityContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onItemPickup(Consumer<ContextUtils.EntityItemEntityContext> consumer) {
        onItemPickup = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity has line of sight to another entity.
            The provided Function accepts a {@link LineOfSightContext} parameter,
            representing the entity to check for line of sight.
                        
            Example usage:
            ```javascript
            entityBuilder.hasLineOfSight(context => {
                // Define conditions to check if the entity has line of sight to the target entity
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if there is line of sight;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> hasLineOfSight(Function<ContextUtils.LineOfSightContext, Object> f) {
        hasLineOfSight = f;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity enters combat.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that has entered combat.
                        
            Example usage:
            ```javascript
            entityBuilder.onEnterCombat(entity => {
                // Define custom logic for handling the entity entering combat
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onEnterCombat(Consumer<LivingEntity> c) {
        onEnterCombat = c;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity leaves combat.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that has left combat.
                        
            Example usage:
            ```javascript
            entityBuilder.onLeaveCombat(entity => {
                // Define custom logic for handling the entity leaving combat
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onLeaveCombat(Consumer<LivingEntity> runnable) {
        onLeaveCombat = runnable;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is affected by potions.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for its susceptibility to potions.
                        
            Example usage:
            ```javascript
            entityBuilder.isAffectedByPotions(entity => {
                // Define conditions to check if the entity is affected by potions
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity is affected by potions;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isAffectedByPotions(Function<LivingEntity, Object> predicate) {
        isAffectedByPotions = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is attackable.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for its attackability.
                        
            Example usage:
            ```javascript
            entityBuilder.isAttackable(entity => {
                // Define conditions to check if the entity is attackable
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity is attackable;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isAttackable(Function<LivingEntity, Object> predicate) {
        isAttackable = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can take an item.
            The provided Predicate accepts a {@link ContextUtils.EntityItemLevelContext} parameter,
            representing the context of the entity potentially taking an item.
                        
            Example usage:
            ```javascript
            entityBuilder.canTakeItem(context => {
                // Define conditions for the entity to be able to take an item
                // Use information about the EntityItemLevelContext provided by the context.
                return // Some boolean condition indicating if the entity can take the item;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canTakeItem(Function<ContextUtils.EntityItemLevelContext, Object> predicate) {
        canTakeItem = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is currently sleeping.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for its sleeping state.
                        
            Example usage:
            ```javascript
            entityBuilder.isSleeping(entity => {
                // Define conditions to check if the entity is currently sleeping
                // Use information about the LivingEntity provided by the context.
                return // Some boolean condition indicating if the entity is sleeping;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isSleeping(Function<LivingEntity, Object> supplier) {
        isSleeping = supplier;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity starts sleeping.
            The provided Consumer accepts a {@link ContextUtils.EntityBlockPosContext} parameter,
            representing the context of the entity starting to sleep at a specific block position.
                        
            Example usage:
            ```javascript
            entityBuilder.onStartSleeping(context => {
                // Define custom logic for handling the entity starting to sleep
                // Use information about the EntityBlockPosContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onStartSleeping(Consumer<ContextUtils.EntityBlockPosContext> consumer) {
        onStartSleeping = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops sleeping.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that has stopped sleeping.
                        
            Example usage:
            ```javascript
            entityBuilder.onStopSleeping(entity => {
                // Define custom logic for handling the entity stopping sleeping
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onStopSleeping(Consumer<LivingEntity> runnable) {
        onStopSleeping = runnable;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs an eating action.
            The provided Consumer accepts a {@link ContextUtils.EntityItemLevelContext} parameter,
            representing the context of the entity's interaction with a specific item during eating.
                        
            Example usage:
            ```javascript
            entityBuilder.eat(context => {
                // Custom logic to handle the entity's eating action
                // Access information about the item being consumed using the provided context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> eat(Consumer<ContextUtils.EntityItemLevelContext> function) {
        eat = function;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the rider of the entity should face forward.
            The provided Predicate accepts a {@link ContextUtils.PlayerEntityContext} parameter,
            representing the context of the player entity riding the main entity.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldRiderFaceForward(context => {
                // Define the conditions for the rider to face forward
                // Use information about the player entity provided by the context.
                return true //someBoolean;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> shouldRiderFaceForward(Function<ContextUtils.PlayerEntityContext, Object> predicate) {
        shouldRiderFaceForward = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can undergo freezing.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be subjected to freezing.
                        
            Example usage:
            ```javascript
            entityBuilder.canFreeze(entity => {
                // Define the conditions for the entity to be able to freeze
                // Use information about the LivingEntity provided by the context.
                return true //someBoolean;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canFreeze(Function<LivingEntity, Object> predicate) {
        canFreeze = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is currently glowing.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may be checked for its glowing state.
                        
            Example usage:
            ```javascript
            entityBuilder.isCurrentlyGlowing(entity => {
                // Define the conditions to check if the entity is currently glowing
                // Use information about the LivingEntity provided by the context.
                const isGlowing = // Some boolean condition to check if the entity is glowing;
                return isGlowing;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isCurrentlyGlowing(Function<LivingEntity, Object> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity can disable its target's shield.
            The provided Predicate accepts a {@link LivingEntity} parameter.
                        
            Example usage:
            ```javascript
            entityBuilder.canDisableShield(entity => {
                // Define the conditions to check if the entity can disable its shield
                // Use information about the LivingEntity provided by the context.
                return true;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canDisableShield(Function<LivingEntity, Object> predicate) {
        canDisableShield = predicate;
        return this;
    }

    @Info(value = """
            Sets a function to handle the interaction with the entity.
            The provided Function accepts a {@link ContextUtils.MobInteractContext} parameter,
            representing the context of the interaction, and returns a nullable {@link InteractionResult}.
                        
            Example usage:
            ```javascript
            entityBuilder.onInteract(context => {
                // Define custom logic for the interaction with the entity
                // Use information about the MobInteractContext provided by the context.
                // InteractionResult is a bound value able to be used without loading the class with Java.loadClass()
                if (context.player.isShiftKeyDown()) return InteractionResult.FAIL
                context.player.startRiding(context.entity);
                return InteractionResult.sidedSuccess(context.entity.level.isClientSide());
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onInteract(Function<ContextUtils.MobInteractContext, Object> f) {
        onInteract = f;
        return this;
    }


    @Info(value = """
            Sets the minimum fall distance for the entity before taking damage.
                        
            Example usage:
            ```javascript
            entityBuilder.setMaxFallDistance(entity => {
                // Define custom logic to determine the maximum fall distance
                // Use information about the LivingEntity provided by the context.
                return 3;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> setMaxFallDistance(Function<LivingEntity, Object> maxFallDistance) {
        setMaxFallDistance = maxFallDistance;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed on the client side.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is being removed on the client side.
                        
            Example usage:
            ```javascript
            entityBuilder.onClientRemoval(entity => {
                // Define custom logic for handling the removal of the entity on the client side
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onClientRemoval(Consumer<LivingEntity> consumer) {
        onClientRemoval = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hurt by lava.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is affected by lava.
                        
            Example usage:
            ```javascript
            entityBuilder.lavaHurt(entity => {
                // Define custom logic for handling the entity being hurt by lava
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> lavaHurt(Consumer<LivingEntity> consumer) {
        lavaHurt = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a flap action.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is flapping.
                        
            Example usage:
            ```javascript
            entityBuilder.onFlap(entity => {
                // Define custom logic for handling the entity's flap action
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onFlap(Consumer<LivingEntity> consumer) {
        onFlap = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the living entity dampens vibrations.
                
            @param predicate The predicate to determine whether the living entity dampens vibrations.
                
            The predicate should take a LivingEntity as a parameter and return a boolean value indicating whether the living entity dampens vibrations.
                
            Example usage:
            ```javascript
            baseLivingEntityBuilder.dampensVibrations(entity => {
                // Determine whether the living entity dampens vibrations
                // Return true if the entity dampens vibrations, false otherwise
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> dampensVibrations(Function<LivingEntity, Object> predicate) {
        this.dampensVibrations = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a player interacts with the entity.
            The provided Consumer accepts a {@link ContextUtils.PlayerEntityContext} parameter,
            representing the context of the player's interaction with the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.playerTouch(context => {
                // Define custom logic for handling player interaction with the entity
                // Use information about the PlayerEntityContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> playerTouch(Consumer<ContextUtils.PlayerEntityContext> consumer) {
        playerTouch = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether to show the vehicle health for the living entity.
                
            @param predicate The predicate to determine whether to show the vehicle health.
                
            The predicate should take a LivingEntity as a parameter and return a boolean value indicating whether to show the vehicle health.
                
            Example usage:
            ```javascript
            baseLivingEntityBuilder.showVehicleHealth(entity => {
                // Determine whether to show the vehicle health for the living entity
                // Return true to show the vehicle health, false otherwise
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> showVehicleHealth(Function<LivingEntity, Object> predicate) {
        this.showVehicleHealth = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hit by thunder.
            The provided Consumer accepts a {@link ContextUtils.ThunderHitContext} parameter,
            representing the context of the entity being hit by thunder.
                        
            Example usage:
            ```javascript
            entityBuilder.thunderHit(context => {
                // Define custom logic for handling the entity being hit by thunder
                // Use information about the ThunderHitContext provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> thunderHit(Consumer<ContextUtils.ThunderHitContext> consumer) {
        thunderHit = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is invulnerable to a specific type of damage.
            The provided Predicate accepts a {@link ContextUtils.DamageContext} parameter,
            representing the context of the damage, and returns a boolean indicating invulnerability.
                        
            Example usage:
            ```javascript
            entityBuilder.isInvulnerableTo(context => {
                // Define conditions for the entity to be invulnerable to the specific type of damage
                // Use information about the DamageContext provided by the context.
                return true // Some boolean condition indicating if the entity has invulnerability to the damage type;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> isInvulnerableTo(Function<ContextUtils.DamageContext, Object> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can change dimensions.
            The provided Predicate accepts a {@link LivingEntity} parameter,
            representing the entity that may attempt to change dimensions.
                        
            Example usage:
            ```javascript
            entityBuilder.canChangeDimensions(entity => {
                // Define the conditions for the entity to be able to change dimensions
                // Use information about the LivingEntity provided by the context.
                return false // Some boolean condition indicating if the entity can change dimensions;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canChangeDimensions(Function<LivingEntity, Object> supplier) {
        canChangeDimensions = supplier;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity may interact with something.
            The provided Predicate accepts a {@link ContextUtils.MayInteractContext} parameter,
            representing the context of the potential interaction, and returns a boolean.
                        
            Example usage:
            ```javascript
            entityBuilder.mayInteract(context => {
                // Define conditions for the entity to be allowed to interact
                // Use information about the MayInteractContext provided by the context.
                return false // Some boolean condition indicating if the entity may interact;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> mayInteract(Function<ContextUtils.MayInteractContext, Object> predicate) {
        mayInteract = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can trample or step on something.
            The provided Predicate accepts a {@link ContextUtils.CanTrampleContext} parameter,
            representing the context of the potential trampling action, and returns a boolean.
                        
            Example usage:
            ```javascript
            entityBuilder.canTrample(context => {
                // Define conditions for the entity to be allowed to trample
                // Use information about the CanTrampleContext provided by the context.
                return false // Some boolean condition indicating if the entity can trample;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> canTrample(Function<ContextUtils.CanTrampleContext, Object> predicate) {
        canTrample = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed from the world.
            The provided Consumer accepts a {@link LivingEntity} parameter,
            representing the entity that is being removed from the world.
                        
            Example usage:
            ```javascript
            entityBuilder.onRemovedFromWorld(entity => {
                // Define custom logic for handling the removal of the entity from the world
                // Use information about the LivingEntity provided by the context.
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> onRemovedFromWorld(Consumer<LivingEntity> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }


    //STUFF
    @Info(value = """
            Sets the spawn placement of the entity type
            entityBuilder.spawnPlacement('on_ground', 'world_surface', (entitypredicate, levelaccessor, spawntype, blockpos, randomsource) => {
                if (levelaccessor.getLevel().getBiome(blockpos) == 'minecraft:plains') return true;
                return false
            })  
            """, params = {
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

    @Info(value = """
            Adds an animation controller to the entity with the specified parameters.
                        
            @param name The name of the animation controller.
            @param translationTicksLength The length of translation ticks for the animation.
            @param predicate The animation predicate defining the conditions for the animation to be played.
                        
            Example usage:
            ```javascript
            entityBuilder.addAnimationController('exampleController', 5, event => {
                // Define conditions for the animation to be played based on the entity.
                if (event.entity.hurtTime > 0) {
                    event.thenLoop('spawn');
                } else {
                    event.thenPlayAndHold('idle');
                }
                return true; // Some boolean condition indicating if the animation should be played;
            });
            ```
            """)
    public BaseLivingEntityBuilder<T> addAnimationController(String name, int translationTicksLength, IAnimationPredicateJS<T> predicate) {
        return addKeyAnimationController(name, translationTicksLength, predicate, null, null, null);
    }


    @Info(value = "Adds a new AnimationController to the entity, with the ability to add event listeners", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "predicate", value = "The predicate for the controller, determines if an animation should continue or not"),
            @Param(name = "soundListener", value = "A sound listener, used to execute actions when the json requests a sound to play. May be null"),
            @Param(name = "particleListener", value = "A particle listener, used to execute actions when the json requests a particle. May be null"),
            @Param(name = "instructionListener", value = "A custom instruction listener, used to execute actions based on arbitrary instructions provided by the json. May be null")
    })
    public BaseLivingEntityBuilder<T> addKeyAnimationController(
            String name,
            int translationTicksLength,
            IAnimationPredicateJS<T> predicate,
            @Nullable ISoundListenerJS<T> soundListener,
            @Nullable IParticleListenerJS<T> particleListener,
            @Nullable ICustomInstructionListenerJS<T> instructionListener
    ) {
        animationSuppliers.add(new AnimationControllerSupplier<>(name, translationTicksLength, predicate, null, null, null, soundListener, particleListener, instructionListener));
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

    @HideFromJS
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
            IAnimationPredicateJS<E> predicate,
            String triggerableAnimationName,
            String triggerableAnimationID,
            String loopType,
            @Nullable ISoundListenerJS<E> soundListener,
            @Nullable IParticleListenerJS<E> particleListener,
            @Nullable ICustomInstructionListenerJS<E> instructionListener
    ) {
        public AnimationController<E> get(E entity) {
            final AnimationController<E> controller = new AnimationController<>(entity, name, translationTicksLength, predicate.toGecko());
            if (triggerableAnimationID != null) {
                Animation.LoopType loopTypeEnum = Animation.LoopType.fromString(loopType.toUpperCase());
                controller.triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(triggerableAnimationName, loopTypeEnum));
            }
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

    @Info(value = "Adds a triggerable AnimationController to the entity callable off the entity's methods anywhere.", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "triggerableAnimationName", value = "The unique identifier of the triggerable animation(sets it apart from other triggerable animations)"),
            @Param(name = "triggerableAnimationID", value = "The name of the animation defined in the animations.json"),
            @Param(name = "loopType", value = "The loop type for the triggerable animation, either 'LOOP' or 'PLAY_ONCE' or 'HOLD_ON_LAST_FRAME' or 'DEFAULT'")
    })
    public BaseLivingEntityBuilder<T> addTriggerableAnimationController(
            String name,
            int translationTicksLength,
            String triggerableAnimationName,
            String triggerableAnimationID,
            String loopType
    ) {
        animationSuppliers.add(new AnimationControllerSupplier<>(
                name,
                translationTicksLength,
                new IAnimationPredicateJS<T>() {
                    @Override
                    public boolean test(AnimationEventJS<T> event) {
                        return true;
                    }
                },
                triggerableAnimationName,
                triggerableAnimationID,
                loopType,
                null,
                null,
                null
        ));
        return this;
    }


    // Wrappers around geckolib things that allow script writers to know what they're doing

    /**
     * A wrapper around {@link net.liopyu.liolib.core.controller.AnimationController.IAnimationPredicate IAnimationPredicate}
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
                        ConsoleJS.STARTUP.error("Exception in IAnimationPredicateJS.toGecko()", e);
                        return PlayState.STOP;
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
        private final List<RawAnimation.Stage> animationList = new ObjectArrayList();
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

        @Info(value = "Returns the entity's limb swing")
        public float getLimbSwing() {
            return parent.getLimbSwing();
        }

        @Info(value = "Returns the entity's limb swing amount")
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

        @Info(value = """
                Sets a triggerable animation with a specified loop type callable anywhere from the entity.
                            
                @param animationName The name of the animation to be triggered, this is the animation named in the json.
                @param triggerableAnimationID The unique identifier for the triggerable animation.
                @param loopTypeEnum The loop type for the triggerable animation. Accepts 'LOOP', 'PLAY_ONCE', 'HOLD_ON_LAST_FRAME', or 'DEFAULT'.
                ```javascript
                 event.addTriggerableAnimation('spawn', 'spawning', 'default')
                 ```
                """)
        public PlayState addTriggerableAnimation(String animationName, String triggerableAnimationID, String loopTypeEnum) {
            Animation.LoopType loopType = Animation.LoopType.fromString(loopTypeEnum.toUpperCase());
            parent.getController().triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(animationName, loopType));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play defaulting to the animations.json file loop type")
        public PlayState thenPlay(String animationName) {
            parent.getController().setAnimation(RawAnimation.begin().then(animationName, Animation.LoopType.DEFAULT));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play in a loop")
        public PlayState thenLoop(String animationName) {
            parent.getController().setAnimation(RawAnimation.begin().thenLoop(animationName));
            return PlayState.CONTINUE;
        }

        @Info(value = "Wait a certain amount of ticks before starting the next animation")
        public PlayState thenWait(int ticks) {
            parent.getController().setAnimation(RawAnimation.begin().thenWait(ticks));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play and hold on the last frame")
        public PlayState thenPlayAndHold(String animationName) {
            parent.getController().setAnimation(RawAnimation.begin().then(animationName, Animation.LoopType.HOLD_ON_LAST_FRAME));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play an x amount of times")
        public PlayState thenPlayXTimes(String animationName, int times) {
            for (int i = 0; i < times; ++i) {
                parent.getController().setAnimation(RawAnimation.begin().then(animationName, i == times - 1 ? Animation.LoopType.DEFAULT : Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }

        @Info(value = "Adds an animation to the current animation list")
        public AnimationEventJS<E> then(String animationName, Animation.LoopType loopType) {
            this.animationList.add(new RawAnimation.Stage(animationName, loopType));
            return this;
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

    @Info(value = """
            Sets the render type for the entity.
                        
            @param type The render type to be set. Acceptable values are:
                         - "solid
                         - "cutout"
                         - "translucent"
                         - RenderType.SOLID
                         - RenderType.CUTOUT
                         - RenderType.TRANSLUCENT
                        
            Example usage:
            ```javascript
            entityBuilder.setRenderType("translucent");
            ```
            """)
    public BaseLivingEntityBuilder<T> setRenderType(Object type) {
        if (type instanceof RenderType) {
            renderType = (RenderType) type;
        } else if (type instanceof String) {
            String typeString = (String) type;
            switch (typeString.toLowerCase()) {
                case "solid":
                    renderType = RenderType.SOLID;
                    break;
                case "cutout":
                    renderType = RenderType.CUTOUT;
                    break;
                case "translucent":
                    renderType = RenderType.TRANSLUCENT;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid render type string: " + typeString);
            }
        } else {
            throw new IllegalArgumentException("Invalid render type: " + type);
        }
        return this;
    }

    public enum RenderType {
        SOLID,
        CUTOUT,
        TRANSLUCENT
    }
}
