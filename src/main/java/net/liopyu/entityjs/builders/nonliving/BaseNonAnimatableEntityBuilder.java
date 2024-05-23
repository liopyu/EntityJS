package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.nonliving.entityjs.PartBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseNonAnimatableEntityBuilder<T extends Entity> extends BuilderBase<EntityType<T>> {
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
    public static final List<BaseNonAnimatableEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Consumer<ContextUtils.NLRenderContext<T>> render;
    //New Base Overrides
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
    public transient Function<Entity, Object> canChangeDimensions;
    public transient Function<ContextUtils.EMayInteractContext, Object> mayInteract;
   // public transient Function<ContextUtils.ECanTrampleContext, Object> canTrample;
    public transient Consumer<Entity> onRemovedFromWorld;
    public transient Function<Entity, Object> isFreezing;
    public transient Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith;
    public transient Consumer<ContextUtils.EntityHurtContext> onHurt;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient ResourceLocation[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient Consumer<ContextUtils.PositionRiderContext> positionRider;

    public BaseNonAnimatableEntityBuilder(ResourceLocation i) {
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
        isAttackable = true;
        isPushable = false;
    }

    @Info(value = """
            Boolean determining if the part entity is pickable.
                                                
            Example usage:
            ```javascript
            entityBuilder.isPickable(true)
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> isPickable(boolean isPickable) {
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
    public BaseNonAnimatableEntityBuilder<T> canCollideWith(Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith) {
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
    public BaseNonAnimatableEntityBuilder<T> isFreezing(Function<Entity, Object> isFreezing) {
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
    public BaseNonAnimatableEntityBuilder<T> setBlockJumpFactor(Function<Entity, Object> blockJumpFactor) {
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
    public BaseNonAnimatableEntityBuilder<T> modelResource(Function<T, Object> function) {
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
    public BaseNonAnimatableEntityBuilder<T> textureResource(Function<T, Object> function) {
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
    public BaseNonAnimatableEntityBuilder<T> animationResource(Function<T, Object> function) {
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
    }*/


    @Info(value = """
            Sets whether the entity is pushable.
                        
            Example usage:
            ```javascript
            entityBuilder.isPushable(true);
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> isPushable(boolean b) {
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
    public BaseNonAnimatableEntityBuilder<T> positionRider(Consumer<ContextUtils.PositionRiderContext> builderConsumer) {
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
    public BaseNonAnimatableEntityBuilder<T> canAddPassenger(Function<ContextUtils.EPassengerEntityContext, Object> predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> setSwimSound(Object sound) {
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
    public BaseNonAnimatableEntityBuilder<T> setSwimSplashSound(Object sound) {
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
    public BaseNonAnimatableEntityBuilder<T> blockSpeedFactor(Function<Entity, Object> callback) {
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
    public BaseNonAnimatableEntityBuilder<T> isFlapping(Function<Entity, Object> b) {
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
    public BaseNonAnimatableEntityBuilder<T> onAddedToWorld(Consumer<Entity> onAddedToWorldCallback) {
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
    public BaseNonAnimatableEntityBuilder<T> repositionEntityAfterLoad(boolean customRepositionEntityAfterLoad) {
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
    public BaseNonAnimatableEntityBuilder<T> nextStep(Function<Entity, Object> nextStep) {
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
    public BaseNonAnimatableEntityBuilder<T> onSprint(Consumer<Entity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops riding.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has stopped being ridden.
                        
            Example usage:
            ```javascript
            entityBuilder.onStopRiding(entity => {
                // Define custom logic for handling when the entity stops being ridden
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> onStopRiding(Consumer<Entity> callback) {
        onStopRiding = callback;
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
    public BaseNonAnimatableEntityBuilder<T> rideTick(Consumer<Entity> callback) {
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
    public BaseNonAnimatableEntityBuilder<T> isAttackable(Boolean predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> canFreeze(Function<Entity, Object> predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> isCurrentlyGlowing(Function<Entity, Object> predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> setMaxFallDistance(Function<Entity, Object> maxFallDistance) {
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
    public BaseNonAnimatableEntityBuilder<T> onClientRemoval(Consumer<Entity> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> lavaHurt(Consumer<Entity> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> onFlap(Consumer<Entity> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> dampensVibrations(Function<Entity, Object> predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> showVehicleHealth(Function<Entity, Object> predicate) {
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
    public BaseNonAnimatableEntityBuilder<T> thunderHit(Consumer<ContextUtils.EThunderHitContext> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> isInvulnerableTo(Function<ContextUtils.EDamageContext, Object> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can change dimensions.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may attempt to change dimensions.
                        
            Example usage:
            ```javascript
            entityBuilder.canChangeDimensions(entity => {
                // Define the conditions for the entity to be able to change dimensions
                // Use information about the Entity provided by the context.
                return false // Some boolean condition indicating if the entity can change dimensions;
            });
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> canChangeDimensions(Function<Entity, Object> supplier) {
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
    public BaseNonAnimatableEntityBuilder<T> mayInteract(Function<ContextUtils.EMayInteractContext, Object> predicate) {
        mayInteract = predicate;
        return this;
    }


   /* @Info(value = """
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
    public BaseNonAnimatableEntityBuilder<T> canTrample(Function<ContextUtils.ECanTrampleContext, Object> predicate) {
        canTrample = predicate;
        return this;
    }*/


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
    public BaseNonAnimatableEntityBuilder<T> onRemovedFromWorld(Consumer<Entity> consumer) {
        onRemovedFromWorld = consumer;
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
    public BaseNonAnimatableEntityBuilder<T> onFall(Consumer<ContextUtils.EEntityFallDamageContext> c) {
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
    public BaseNonAnimatableEntityBuilder<T> immuneTo(String... blockNames) {
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
    public BaseNonAnimatableEntityBuilder<T> canSpawnFarFromPlayer(boolean canSpawnFar) {
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
    public BaseNonAnimatableEntityBuilder<T> render(Consumer<ContextUtils.NLRenderContext<T>> render) {
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
    public BaseNonAnimatableEntityBuilder<T> setSummonable(boolean b) {
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
    public BaseNonAnimatableEntityBuilder<T> mobCategory(String category) {
        mobCategory = stringToMobCategory(category);
        return this;
    }

    @Info(value = """
            Determines if the entity should serialize its data. Defaults to true.
                        
            Example usage:
            ```javascript
            entityBuilder.saves(false);
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> saves(boolean shouldSave) {
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
    public BaseNonAnimatableEntityBuilder<T> fireImmune(boolean isFireImmune) {
        this.fireImmune = isFireImmune;
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
    public BaseNonAnimatableEntityBuilder<T> sized(float width, float height) {
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
    public BaseNonAnimatableEntityBuilder<T> clientTrackingRange(int trackingRange) {
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
    public BaseNonAnimatableEntityBuilder<T> updateInterval(int updateInterval) {
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
    public BaseNonAnimatableEntityBuilder<T> lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
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
    public BaseNonAnimatableEntityBuilder<T> isAttackable(boolean b) {
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
    public BaseNonAnimatableEntityBuilder<T> playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
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
    public BaseNonAnimatableEntityBuilder<T> move(Consumer<ContextUtils.MovementContext> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed on each tick for the entity.
                        
            @param consumer A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            entityBuilder.tick(entity => {
                // Custom logic to be executed on each tick of the entity.
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public BaseNonAnimatableEntityBuilder<T> tick(Consumer<Entity> consumer) {
        tick = consumer;
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

    @HideFromJS
    abstract public EntityType.EntityFactory<T> factory();

    @Override
    public EntityType<T> createObject() {
        return new NonAnimatableEntityTypeBuilder<>(this).get();
    }
}
