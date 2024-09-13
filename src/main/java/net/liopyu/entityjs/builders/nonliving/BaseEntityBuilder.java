package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.AnimalEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.keyframe.event.CustomInstructionKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.KeyFrameEvent;
import software.bernie.geckolib.animation.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.data.KeyFrameData;
import software.bernie.geckolib.constant.dataticket.DataTicket;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseEntityBuilder<T extends Entity & IAnimatableJSNL> extends BuilderBase<EntityType<T>> {
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Consumer<ContextUtils.EntityPlayerContext> playerTouch;
    public transient Function<ContextUtils.EntitySqrDistanceContext, Object> shouldRenderAtSqrDistance;
    public transient Consumer<Entity> tick;
    public transient Consumer<ContextUtils.MovementContext> move;
    public transient Boolean isAttackable;
    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient Function<T, Object> modelResource;
    public transient Function<T, Object> textureResource;
    public transient Function<T, Object> animationResource;
    public transient BaseEntityBuilder.RenderType renderType;
    public transient final List<BaseEntityBuilder.AnimationControllerSupplier<T>> animationSuppliers;
    public static final List<BaseEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Consumer<ContextUtils.NLRenderContext<T>> render;
    public transient boolean isPickable;

    public transient boolean isPushable;
    public transient Function<ContextUtils.EPassengerEntityContext, Object> canAddPassenger;
    public transient Function<Entity, Object> setBlockJumpFactor;
    public transient Function<Entity, Object> blockSpeedFactor;
    public transient Object setSwimSound;
    public transient Function<Entity, Object> isFlapping;
    public transient Boolean repositionEntityAfterLoad;
    public transient Function<Entity, Object> nextStep;
    public transient Object setSwimSplashSound;
    public transient Consumer<ContextUtils.EEntityFallDamageContext> onFall;
    public transient Consumer<Entity> onSprint;
    public transient Consumer<Entity> onStopRiding;
    public transient Consumer<Entity> onRemovePassenger;
    public transient Consumer<Entity> rideTick;
    public transient Function<Entity, Object> canFreeze;
    public transient Function<Entity, Object> isCurrentlyGlowing;
    public transient Function<Entity, Object> setMaxFallDistance;
    public transient Consumer<Entity> onClientRemoval;
    public transient Consumer<Entity> onAddedToWorld;
    public transient Consumer<Entity> lavaHurt;
    public transient Consumer<Entity> onFlap;
    public transient Function<Entity, Object> dampensVibrations;
    public transient Function<Entity, Object> showVehicleHealth;
    public transient Consumer<ContextUtils.EThunderHitContext> thunderHit;
    public transient Function<ContextUtils.EDamageContext, Object> isInvulnerableTo;
    public transient Function<ContextUtils.ChangeDimensionsContext, Object> canChangeDimensions;
    public transient Function<ContextUtils.EMayInteractContext, Object> mayInteract;
    public transient Function<ContextUtils.ECanTrampleContext, Object> canTrample;
    public transient Consumer<Entity> onRemovedFromWorld;
    public transient Function<Entity, Object> isFreezing;
    public transient Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith;
    public transient Consumer<ContextUtils.EntityHurtContext> onHurt;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient ResourceLocation[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient float scaleHeight;
    public transient float scaleWidth;
    public transient Consumer<ContextUtils.ScaleModelRenderContextNL<T>> scaleModelForRender;
    public transient Consumer<ContextUtils.PositionRiderContext> positionRider;
    public static Map<EntityType<?>, Item> projectileItems = new HashMap<>();

    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        //immuneTo = ResourceLocation.parse[0];
        fireImmune = false;
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 1;
        mobCategory = MobCategory.MISC;
        isAttackable = true;
        renderType = BaseEntityBuilder.RenderType.CUTOUT;
        animationSuppliers = new ArrayList<>();
        modelResource = t -> newID("geo/entity/", ".geo.json");
        textureResource = t -> newID("textures/entity/", ".png");
        animationResource = t -> newID("animations/entity/", ".animation.json");
        scaleHeight = 1F;
        scaleWidth = 1F;
    }

    @Info(value = """
            Boolean determining if the part entity is pickable.
                                                
            Example usage:
            ```javascript
            entityBuilder.isPickable(true)
            ```
            """)
    public BaseEntityBuilder<T> isPickable(boolean isPickable) {
        this.isPickable = isPickable;
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
    public BaseEntityBuilder<T> canCollideWith(Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith) {
        this.canCollideWith = canCollideWith;
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
    public BaseEntityBuilder<T> isFreezing(Function<Entity, Object> isFreezing) {
        this.isFreezing = isFreezing;
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
    public BaseEntityBuilder<T> setBlockJumpFactor(Function<Entity, Object> blockJumpFactor) {
        setBlockJumpFactor = blockJumpFactor;
        return this;
    }



    /*@Info(value = """
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
    public BaseEntityBuilder<T> modelResource(Function<T, Object> function) {
        modelResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
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
    public BaseEntityBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
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
    public BaseEntityBuilder<T> animationResource(Function<T, Object> function) {
        animationResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid animation resource: " + obj + ". Defaulting to " + entity.getBuilder().newID("animations/entity/", ".animation.json"));
                return entity.getBuilder().newID("animations/entity/", ".animation.json");
            }
        };
        return this;
    }*/


    @Info(value = """
            Sets whether the entity is pushable.
                        
            Example usage:
            ```javascript
            entityBuilder.isPushable(true);
            ```
            """)
    public BaseEntityBuilder<T> isPushable(boolean b) {
        isPushable = b;
        return this;
    }

    @Info(value = """
            @param positionRider A consumer determining the position of rider/riders.
                            
                Example usage:
                ```javascript
                entityBuilder.positionRider(context => {
                    const {entity, passenger, moveFunction} = context
                });
                ```
            """)
    public BaseEntityBuilder<T> positionRider(Consumer<ContextUtils.PositionRiderContext> builderConsumer) {
        this.positionRider = builderConsumer;
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
    public BaseEntityBuilder<T> canAddPassenger(Function<ContextUtils.EPassengerEntityContext, Object> predicate) {
        canAddPassenger = predicate;
        return this;
    }


    @Info(value = """
            Sets the swim sound for the entity using a string representation.
                        
            Example usage:
            ```javascript
            entityBuilder.setSwimSound("minecraft:entity.generic.swim");
            ```
            """)
    public BaseEntityBuilder<T> setSwimSound(Object sound) {
        if (sound instanceof String) setSwimSound = ResourceLocation.parse((String) sound);
        else if (sound instanceof ResourceLocation) setSwimSound = (ResourceLocation) sound;
        else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setSwimSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.swim\"");

            setSwimSound = ResourceLocation.parse("minecraft:entity.generic.swim");
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
    public BaseEntityBuilder<T> setSwimSplashSound(Object sound) {
        if (sound instanceof String) {
            setSwimSplashSound = ResourceLocation.parse((String) sound);
        } else if (sound instanceof ResourceLocation) {
            setSwimSplashSound = (ResourceLocation) sound;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setSwimSplashSound. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.generic.splash\"");

            setSwimSplashSound = ResourceLocation.fromNamespaceAndPath("minecraft", "entity/generic/splash");
        }
        return this;
    }


    @Info(value = """
            Sets a function to determine the block speed factor of the entity.
            The provided Function accepts a {@link Entity} parameter,
            representing the entity whose block speed factor is being determined.
            It returns a Float representing the block speed factor.
                        
            Example usage:
            ```javascript
            entityBuilder.blockSpeedFactor(entity => {
                // Define logic to calculate and return the block speed factor for the entity
                // Use information about the Entity provided by the context.
                return // Some Float value representing the block speed factor;
            });
            ```
            """)
    public BaseEntityBuilder<T> blockSpeedFactor(Function<Entity, Object> callback) {
        blockSpeedFactor = callback;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity is currently flapping.
            The provided Function accepts a {@link Entity} parameter,
            representing the entity whose flapping status is being determined.
            It returns a Boolean indicating whether the entity is flapping.
                        
            Example usage:
            ```javascript
            entityBuilder.isFlapping(entity => {
                // Define logic to determine whether the entity is currently flapping
                // Use information about the Entity provided by the context.
                return // Some Boolean value indicating whether the entity is flapping;
            });
            ```
            """)
    public BaseEntityBuilder<T> isFlapping(Function<Entity, Object> b) {
        this.isFlapping = b;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity is added to the world.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is added to the world.
                        
            Example usage:
            ```javascript
            entityBuilder.onAddedToWorld(entity => {
                // Define custom logic for handling when the entity is added to the world
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onAddedToWorld(Consumer<Entity> onAddedToWorldCallback) {
        this.onAddedToWorld = onAddedToWorldCallback;
        return this;
    }


    @Info(value = """
            Sets whether to reposition the entity after loading.
                        
            Example usage:
            ```javascript
            entityBuilder.repositionEntityAfterLoad(true);
            ```
            """)
    public BaseEntityBuilder<T> repositionEntityAfterLoad(boolean customRepositionEntityAfterLoad) {
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
    public BaseEntityBuilder<T> nextStep(Function<Entity, Object> nextStep) {
        this.nextStep = nextStep;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity starts sprinting.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has started sprinting.
                        
            Example usage:
            ```javascript
            entityBuilder.onSprint(entity => {
                // Define custom logic for handling when the entity starts sprinting
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onSprint(Consumer<Entity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops riding.
                        
            Example usage:
            ```javascript
            entityBuilder.onStopRiding(entity => {
                // Define custom logic for handling when the entity stops riding another entity
            });
            ```
            """)
    public BaseEntityBuilder<T> onStopRiding(Consumer<Entity> callback) {
        onStopRiding = callback;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity's passenger dismounts it.
                        
            Example usage:
            ```javascript
            entityBuilder.onRemovePassenger(entity => {
                // Define custom logic for handling when the entity stops being ridden
            });
            ```
            """)
    public BaseEntityBuilder<T> onRemovePassenger(Consumer<Entity> callback) {
        onRemovePassenger = callback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed during each tick when the entity is being ridden.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being ridden.
                        
            Example usage:
            ```javascript
            entityBuilder.rideTick(entity => {
                // Define custom logic for handling each tick when the entity is being ridden
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> rideTick(Consumer<Entity> callback) {
        rideTick = callback;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is attackable.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for its attackability.
                        
            Example usage:
            ```javascript
            entityBuilder.isAttackable(entity => {
                // Define conditions to check if the entity is attackable
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity is attackable;
            });
            ```
            """)
    public BaseEntityBuilder<T> isAttackable(Boolean predicate) {
        isAttackable = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can undergo freezing.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be subjected to freezing.
                        
            Example usage:
            ```javascript
            entityBuilder.canFreeze(entity => {
                // Define the conditions for the entity to be able to freeze
                // Use information about the Entity provided by the context.
                return true //someBoolean;
            });
            ```
            """)
    public BaseEntityBuilder<T> canFreeze(Function<Entity, Object> predicate) {
        canFreeze = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is currently glowing.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for its glowing state.
                        
            Example usage:
            ```javascript
            entityBuilder.isCurrentlyGlowing(entity => {
                // Define the conditions to check if the entity is currently glowing
                // Use information about the Entity provided by the context.
                const isGlowing = // Some boolean condition to check if the entity is glowing;
                return isGlowing;
            });
            ```
            """)
    public BaseEntityBuilder<T> isCurrentlyGlowing(Function<Entity, Object> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }


    @Info(value = """
            Sets the minimum fall distance for the entity before taking damage.
                        
            Example usage:
            ```javascript
            entityBuilder.setMaxFallDistance(entity => {
                // Define custom logic to determine the maximum fall distance
                // Use information about the Entity provided by the context.
                return 3;
            });
            ```
            """)
    public BaseEntityBuilder<T> setMaxFallDistance(Function<Entity, Object> maxFallDistance) {
        setMaxFallDistance = maxFallDistance;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed on the client side.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being removed on the client side.
                        
            Example usage:
            ```javascript
            entityBuilder.onClientRemoval(entity => {
                // Define custom logic for handling the removal of the entity on the client side
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onClientRemoval(Consumer<Entity> consumer) {
        onClientRemoval = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hurt by lava.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is affected by lava.
                        
            Example usage:
            ```javascript
            entityBuilder.lavaHurt(entity => {
                // Define custom logic for handling the entity being hurt by lava
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> lavaHurt(Consumer<Entity> consumer) {
        lavaHurt = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a flap action.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is flapping.
                        
            Example usage:
            ```javascript
            entityBuilder.onFlap(entity => {
                // Define custom logic for handling the entity's flap action
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onFlap(Consumer<Entity> consumer) {
        onFlap = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the living entity dampens vibrations.
                
            @param predicate The predicate to determine whether the living entity dampens vibrations.
                
            The predicate should take a Entity as a parameter and return a boolean value indicating whether the living entity dampens vibrations.
                
            Example usage:
            ```javascript
            baseEntityBuilder.dampensVibrations(entity => {
                // Determine whether the living entity dampens vibrations
                // Return true if the entity dampens vibrations, false otherwise
            });
            ```
            """)
    public BaseEntityBuilder<T> dampensVibrations(Function<Entity, Object> predicate) {
        this.dampensVibrations = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether to show the vehicle health for the living entity.
                
            @param predicate The predicate to determine whether to show the vehicle health.
                
            The predicate should take a Entity as a parameter and return a boolean value indicating whether to show the vehicle health.
                
            Example usage:
            ```javascript
            baseEntityBuilder.showVehicleHealth(entity => {
                // Determine whether to show the vehicle health for the living entity
                // Return true to show the vehicle health, false otherwise
            });
            ```
            """)
    public BaseEntityBuilder<T> showVehicleHealth(Function<Entity, Object> predicate) {
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
    public BaseEntityBuilder<T> thunderHit(Consumer<ContextUtils.EThunderHitContext> consumer) {
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
    public BaseEntityBuilder<T> isInvulnerableTo(Function<ContextUtils.EDamageContext, Object> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can change dimensions.
            The provided Predicate accepts a {@link ContextUtils.ChangeDimensionsContext} parameter,
            representing the entity that may attempt to change dimensions.
                        
            Example usage:
            ```javascript
            entityBuilder.canChangeDimensions(ctx => {
                // Define the conditions for the entity to be able to change dimensions
                // Use information about the Entity provided by the context.
                return false // Some boolean condition indicating if the entity can change dimensions;
            });
            ```
            """)
    public BaseEntityBuilder<T> canChangeDimensions(Function<ContextUtils.ChangeDimensionsContext, Object> supplier) {
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
    public BaseEntityBuilder<T> mayInteract(Function<ContextUtils.EMayInteractContext, Object> predicate) {
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
    public BaseEntityBuilder<T> canTrample(Function<ContextUtils.ECanTrampleContext, Object> predicate) {
        canTrample = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed from the world.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being removed from the world.
                        
            Example usage:
            ```javascript
            entityBuilder.onRemovedFromWorld(entity => {
                // Define custom logic for handling the removal of the entity from the world
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onRemovedFromWorld(Consumer<Entity> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }

    @Info(value = """
            Sets the scale of the model.
                        
            Example usage:
            ```javascript
            entityBuilder.modelSize(2,2);
            ```
            """)
    public BaseEntityBuilder<T> modelSize(float scaleHeight, float scaleWidth) {
        this.scaleHeight = scaleHeight;
        this.scaleWidth = scaleWidth;
        return this;
    }

    @Info(value = """
            @param scaleModelForRender A Consumer to determing logic for model scaling and rendering
                without affecting core logic such as hitbox sizing.
                        
            Example usage:
            ```javascript
            entityBuilder.scaleModelForRender(context => {
                const { entity, widthScale, heightScale, poseStack, model, isReRender, partialTick, packedLight, packedOverlay } = context
                poseStack.scale(0.5, 0.5, 0.5)
            });
            ```
            """)
    public BaseEntityBuilder<T> scaleModelForRender(Consumer<ContextUtils.ScaleModelRenderContextNL<T>> scaleModelForRender) {
        this.scaleModelForRender = scaleModelForRender;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity falls and takes damage.
            The provided Consumer accepts a {@link ContextUtils.EEntityFallDamageContext} parameter,
            representing the context of the entity falling and taking fall damage.
                        
            Example usage:
            ```javascript
            entityBuilder.onFall(context => {
                // Define custom logic for handling when the entity falls and takes damage
                // Use information about the EEntityFallDamageContext provided by the context.
            });
            ```
            """)
    public BaseEntityBuilder<T> onFall(Consumer<ContextUtils.EEntityFallDamageContext> c) {
        onFall = c;
        return this;
    }

    @Info(value = """
            Sets the list of block names to which the entity is immune.
                        
            Example usage:
            ```javascript
            entityBuilder.immuneTo("minecraft:stone", "minecraft:dirt");
            ```
            """)
    public BaseEntityBuilder<T> immuneTo(String... blockNames) {
        this.immuneTo = Arrays.stream(blockNames)
                .map(ResourceLocation::parse)
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
    public BaseEntityBuilder<T> canSpawnFarFromPlayer(boolean canSpawnFar) {
        this.spawnFarFromPlayer = canSpawnFar;
        return this;
    }

    @Info(value = """
            Defines logic to render the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.render(context => {
                // Define logic to render the entity
                context.poseStack.scale(0.5, 0.5, 0.5);
            });
            ```
            """)
    public BaseEntityBuilder<T> render(Consumer<ContextUtils.NLRenderContext<T>> render) {
        this.render = render;
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
            Sets whether the entity is summonable.
                        
            Example usage:
            ```javascript
            entityBuilder.setSummonable(true);
            ```
            """)
    public BaseEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
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
    public BaseEntityBuilder<T> mobCategory(String category) {
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
    public BaseEntityBuilder<T> modelResource(Function<T, Object> function) {
        modelResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
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
            Determines if the entity should serialize its data. Defaults to true.
                        
            Example usage:
            ```javascript
            entityBuilder.saves(false);
            ```
            """)
    public BaseEntityBuilder<T> saves(boolean shouldSave) {
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
    public BaseEntityBuilder<T> fireImmune(boolean isFireImmune) {
        this.fireImmune = isFireImmune;
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
    public BaseEntityBuilder<T> textureResource(Function<T, Object> function) {
        textureResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
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
    public BaseEntityBuilder<T> animationResource(Function<T, Object> function) {
        animationResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return ResourceLocation.parse((String) obj);
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
            Sets the hit box of the entity type.
                        
            @param width The width of the entity. Defaults to 0.5.
            @param height The height of the entity. Defaults to 0.5.
                        
            Example usage:
            ```javascript
            entityBuilder.sized(1.0f, 1.5f);
            ```
            """)
    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    @Info(value = """
            Sets the client tracking range. Defaults to 5.
                        
            @param trackingRange The client tracking range.
                        
            Example usage:
            ```javascript
            entityBuilder.clientTrackingRange(8);
            ```
            """)
    public BaseEntityBuilder<T> clientTrackingRange(int trackingRange) {
        this.clientTrackingRange = trackingRange;
        return this;
    }


    @Info(value = """
            Sets the update interval in ticks of the entity. 
            Defaults to 1 tick.
                        
            @param updateInterval The update interval in ticks.
                        
            Example usage:
            ```javascript
            entityBuilder.updateInterval(5);
            ```
            """)
    public BaseEntityBuilder<T> updateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }


    @Info(value = """
            Sets a consumer to handle lerping (linear interpolation) of the entity's position.
                        
            @param lerpTo Consumer accepting a {@link ContextUtils.LerpToContext} parameter,
                            providing information and control over the lerping process.
                        
            Example usage:
            ```javascript
            entityBuilder.lerpTo(context => {
                // Custom logic for lerping the entity's position
                // Access information about the lerping process using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity should render at a squared distance.
                        
            @param shouldRenderAtSqrDistance Function accepting a {@link ContextUtils.EntitySqrDistanceContext} parameter,
                             defining the conditions under which the entity should render.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldRenderAtSqrDistance(context => {
                // Custom logic to determine whether the entity should render
                // Access information about the distance using the provided context.
                return true;
            });
            ```
            """)
    public BaseEntityBuilder<T> shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
        shouldRenderAtSqrDistance = func;
        return this;
    }


    @Info(value = """
            Sets whether the entity is attackable or not.
                        
            @param isAttackable Boolean value indicating whether the entity is attackable.
                        
            Example usage:
            ```javascript
            entityBuilder.isAttackable(true);
            ```
            """)
    public BaseEntityBuilder<T> isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a player touches the entity.
            The provided Consumer accepts a {@link ContextUtils.EntityPlayerContext} parameter,
            representing the context of the player's interaction with the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.playerTouch(context => {
                // Custom logic to handle the player's touch interaction with the entity
                // Access information about the interaction using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a movement action.
            The provided Consumer accepts a {@link ContextUtils.MovementContext} parameter,
            representing the context of the entity's movement.
                        
            Example usage:
            ```javascript
            entityBuilder.move(context => {
                // Custom logic to handle the entity's movement action
                // Access information about the movement using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> move(Consumer<ContextUtils.MovementContext> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed on each tick for the entity.
                        
            @param tick A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            entityBuilder.tick(entity => {
                // Custom logic to be executed on each tick of the entity.
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public BaseEntityBuilder<T> tick(Consumer<Entity> consumer) {
        tick = consumer;
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
    public BaseEntityBuilder<T> addAnimationController(String name, int translationTicksLength, BaseEntityBuilder.IAnimationPredicateJS<T> predicate) {
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
    public BaseEntityBuilder<T> addKeyAnimationController(
            String name,
            int translationTicksLength,
            BaseEntityBuilder.IAnimationPredicateJS<T> predicate,
            @Nullable BaseEntityBuilder.ISoundListenerJS<T> soundListener,
            @Nullable BaseEntityBuilder.IParticleListenerJS<T> particleListener,
            @Nullable BaseEntityBuilder.ICustomInstructionListenerJS<T> instructionListener
    ) {
        animationSuppliers.add(new BaseEntityBuilder.AnimationControllerSupplier<>(name, translationTicksLength, predicate, null, null, null, soundListener, particleListener, instructionListener));
        return this;
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
    public BaseEntityBuilder<T> setRenderType(Object type) {
        if (type instanceof BaseEntityBuilder.RenderType) {
            renderType = (BaseEntityBuilder.RenderType) type;
        } else if (type instanceof String) {
            String typeString = (String) type;
            switch (typeString.toLowerCase()) {
                case "solid":
                    renderType = BaseEntityBuilder.RenderType.SOLID;
                    break;
                case "cutout":
                    renderType = BaseEntityBuilder.RenderType.CUTOUT;
                    break;
                case "translucent":
                    renderType = BaseEntityBuilder.RenderType.TRANSLUCENT;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid render type string: " + typeString);
            }
        } else {
            throw new IllegalArgumentException("Invalid render type: " + type);
        }
        return this;
    }


    /**
     * <strong>Do not</strong> override unless you are creating a custom entity type builder<br><br>
     * See: {@link #factory()}
     */

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
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


    /**
     * A 'supplier' for an {@link AnimationController} that does not require a reference to the entity being animated
     *
     * @param name                   The name of the AnimationController that this builds
     * @param translationTicksLength The number of ticks it takes to transition between animations
     * @param predicate              The {@link BaseEntityBuilder.IAnimationPredicateJS script-friendly} animation predicate
     */
    public record AnimationControllerSupplier<E extends Entity & IAnimatableJSNL>(
            String name,
            int translationTicksLength,
            BaseEntityBuilder.IAnimationPredicateJS<E> predicate,
            String triggerableAnimationName,
            String triggerableAnimationID,
            Object loopType,
            @Nullable BaseEntityBuilder.ISoundListenerJS<E> soundListener,
            @Nullable BaseEntityBuilder.IParticleListenerJS<E> particleListener,
            @Nullable BaseEntityBuilder.ICustomInstructionListenerJS<E> instructionListener
    ) {
        public AnimationController<E> get(E entity) {
            final AnimationController<E> controller = new AnimationController<>(entity, name, translationTicksLength, predicate.toGecko());
            if (triggerableAnimationID != null) {
                Object type = EntityJSHelperClass.convertObjectToDesired(loopType, "looptype");
                controller.triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(triggerableAnimationName, (Animation.LoopType) type));
            }
            if (soundListener != null) {
                controller.setSoundKeyframeHandler(event -> soundListener.playSound(new BaseEntityBuilder.SoundKeyFrameEventJS<>(event)));
            }
            if (particleListener != null) {
                controller.setParticleKeyframeHandler(event -> particleListener.summonParticle(new BaseEntityBuilder.ParticleKeyFrameEventJS<>(event)));
            }
            if (instructionListener != null) {
                controller.setCustomInstructionKeyframeHandler(event -> instructionListener.executeInstruction(new BaseEntityBuilder.CustomInstructionKeyframeEventJS<>(event)));
            }
            return controller;
        }
    }

    @Info(value = "Adds a triggerable AnimationController to the entity callable off the entity's methods anywhere.", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "triggerableAnimationID", value = "The unique identifier of the triggerable animation(sets it apart from other triggerable animations)"),
            @Param(name = "triggerableAnimationName", value = "The name of the animation defined in the animations.json"),
            @Param(name = "loopType", value = "The loop type for the triggerable animation, either 'LOOP' or 'PLAY_ONCE' or 'HOLD_ON_LAST_FRAME' or 'DEFAULT'")
    })
    public BaseEntityBuilder<T> addTriggerableAnimationController(
            String name,
            int translationTicksLength,
            String triggerableAnimationName,
            String triggerableAnimationID,
            String loopType
    ) {
        animationSuppliers.add(new BaseEntityBuilder.AnimationControllerSupplier<>(
                name,
                translationTicksLength,
                new BaseEntityBuilder.IAnimationPredicateJS<T>() {
                    @Override
                    public boolean test(BaseEntityBuilder.AnimationEventJS<T> event) {
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
     * A wrapper around {@link software.bernie.geckolib.animation.AnimationController.AnimationStateHandler IAnimationPredicate}
     * that is easier to work with in js
     */
    @FunctionalInterface
    public interface IAnimationPredicateJS<E extends Entity & IAnimatableJSNL> {

        @Info(value = "Determines if an animation should continue for a given AnimationEvent. Return true to continue the current animation", params = {
                @Param(name = "event", value = "The AnimationEvent, provides values that can be used to determine if the animation should continue or not")
        })
        boolean test(BaseEntityBuilder.AnimationEventJS<E> event);

        default AnimationController.AnimationStateHandler<E> toGecko() {
            return event -> {
                if (event != null) {
                    BaseEntityBuilder.AnimationEventJS<E> animationEventJS = new BaseEntityBuilder.AnimationEventJS<>(event);
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
     * A simple wrapper around a {@link AnimationEventJS} that restricts access to certain things
     * and adds {@link @Info} annotations for script writers
     *
     * @param <E> The entity being animated in the event
     */
    public static class AnimationEventJS<E extends Entity & IAnimatableJSNL> {
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

        @Info(value = """
                Sets a triggerable animation with a specified loop type callable anywhere from the entity.
                            
                @param animationName The name of the animation to be triggered, this is the animation named in the json.
                @param triggerableAnimationID The unique identifier for the triggerable animation.
                @param loopTypeEnum The loop type for the triggerable animation. Accepts 'LOOP', 'PLAY_ONCE', 'HOLD_ON_LAST_FRAME', or 'DEFAULT'.
                ```javascript
                 event.addTriggerableAnimation('spawn', 'spawning', 'default')
                 ```
                """)
        public PlayState addTriggerableAnimation(String animationName, String triggerableAnimationID, Object loopTypeEnum) {
            Object type = EntityJSHelperClass.convertObjectToDesired(loopTypeEnum, "looptype");
            parent.getController().triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(animationName, (Animation.LoopType) type));
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
        public BaseEntityBuilder.AnimationEventJS<E> then(String animationName, Animation.LoopType loopType) {
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

    public static class KeyFrameEventJS<E extends Entity & IAnimatableJSNL, B extends KeyFrameData> {
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
    public interface ISoundListenerJS<E extends Entity & IAnimatableJSNL> {
        void playSound(BaseEntityBuilder.SoundKeyFrameEventJS<E> event);
    }

    public static class SoundKeyFrameEventJS<E extends Entity & IAnimatableJSNL> {

        @Info(value = "The name of the sound to play")
        public final String sound;

        public SoundKeyFrameEventJS(SoundKeyframeEvent<E> parent) {
            sound = parent.getKeyframeData().getSound();
        }
    }

    @FunctionalInterface
    public interface IParticleListenerJS<E extends Entity & IAnimatableJSNL> {
        void summonParticle(BaseEntityBuilder.ParticleKeyFrameEventJS<E> event);
    }

    public static class ParticleKeyFrameEventJS<E extends Entity & IAnimatableJSNL> {

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
    public interface ICustomInstructionListenerJS<E extends Entity & IAnimatableJSNL> {
        void executeInstruction(BaseEntityBuilder.CustomInstructionKeyframeEventJS<E> event);
    }

    public static class CustomInstructionKeyframeEventJS<E extends Entity & IAnimatableJSNL> {

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
