package net.liopyu.entityjs.builders.modification;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModifyEntityBuilder extends EventJS {
    public final EntityType<?> entityType;
    public transient Boolean repositionEntityAfterLoad;
    public transient Object mainArm;
    public transient Function<ContextUtils.EPassengerEntityContext, Object> canAddPassenger;
    public transient Function<Entity, Object> setBlockJumpFactor;
    public transient Object setSwimSound;
    public transient Function<Entity, Object> isFlapping;
    public transient Object setSwimSplashSound;
    public transient Function<ContextUtils.LineOfSightContext, Object> isAlliedTo;
    public transient Consumer<ContextUtils.PositionRiderContext> positionRider;
    public transient Function<Entity, Object> isFreezing;
    public transient Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith;
    public transient Function<ContextUtils.EMayInteractContext, Object> mayInteract;
    public transient Function<ContextUtils.ECanTrampleContext, Object> canTrample;
    public transient Consumer<Entity> onRemovedFromWorld;
    public transient Consumer<Entity> onLivingJump;
    public transient Consumer<ContextUtils.EThunderHitContext> thunderHit;
    public transient Function<ContextUtils.EDamageContext, Object> isInvulnerableTo;
    public transient Function<Entity, Object> dampensVibrations;
    public transient Consumer<ContextUtils.EntityPlayerContext> playerTouch;
    public transient Function<Entity, Object> showVehicleHealth;
    public transient Consumer<Entity> lavaHurt;
    public transient Consumer<Entity> onFlap;
    public transient Consumer<Entity> onAddedToWorld;
    public transient Consumer<Entity> onClientRemoval;
    public transient Consumer<ContextUtils.MobInteractContext> onInteract;
    public transient Function<Entity, Object> setMaxFallDistance;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Function<ContextUtils.EntitySqrDistanceContext, Object> shouldRenderAtSqrDistance;
    public transient Consumer<Entity> tick;
    public transient Consumer<ContextUtils.MovementContext> move;
    public transient Boolean isAttackable;
    public transient Function<Entity, Object> canChangeDimensions;
    public transient Function<Entity, Object> blockSpeedFactor;
    public transient boolean isPickable;
    public transient Consumer<ContextUtils.EEntityFallDamageContext> onFall;
    public transient Consumer<Entity> onSprint;
    public transient Consumer<Entity> onStopRiding;
    public transient Consumer<Entity> rideTick;
    public transient Function<Entity, Object> canFreeze;
    public transient Function<Entity, Object> isCurrentlyGlowing;
    public transient Boolean isPushable;
    public transient Function<Entity, Object> myRidingOffset;
    public transient Boolean controlledByFirstPassenger;
    public static Map<EntityType<?>, ModifyEntityBuilder> builderMap = new HashMap<>();

    public ModifyEntityBuilder(EntityType<?> entityType) {
        this.entityType = entityType;

    }

    @Info(value = """
            Returns the current entity type's builder in string format.\s
            \s
            Example usage:\s
            ```javascript
            EntityJSEvents.modifyEntity(event => {
                event.modify("minecraft:zombie", builder => {
                    console.log(builder.builderType()) // Will output "ModifyPathfinderMobBuilder"
                })
            })
            ```
            """)
    public String builderType() {
        return "[EntityJS]: Builder for " + this.getEntityType().toString() + ": " + this.getClass().getSimpleName();
    }

    @HideFromJS
    public static ModifyEntityBuilder getOrCreate(EntityType<?> type) {
        if (!builderMap.containsKey(type)) {
            var builder = new ModifyEntityBuilder(type);
            builderMap.put(type, builder);
        }
        return builderMap.get(type);
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
    }


    @Info(value = """
            Boolean determining if the entity is controlled by the first passenger
                                                
            Example usage:
            ```javascript
            modifyBuilder.controlledByFirstPassenger(true)
            ```
            """)
    public ModifyEntityBuilder controlledByFirstPassenger(boolean controlledByFirstPassenger) {
        this.controlledByFirstPassenger = controlledByFirstPassenger;
        return this;
    }

    @Info(value = """
            Function which sets the offset for riding on the entity.
                        
            @param myRidingOffset The offset value for riding on the mob.
            Defaults to 0.0.
                        
            Example usage:
            ```javascript
            modifyBuilder.myRidingOffset(entity => {
                //Use the provided context about the entity to determine the riding offset of the passengers
                return 5 //Some double value;
            })
            ```
            """)
    public ModifyEntityBuilder myRidingOffset(Function<Entity, Object> myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }

    @Info(value = """
            Boolean determining if the part entity is pickable.
                                                
            Example usage:
            ```javascript
            modifyBuilder.isPickable(true)
            ```
            """)
    public ModifyEntityBuilder isPickable(boolean isPickable) {
        this.isPickable = isPickable;
        return this;
    }

    @Info(value = """
            Function determining if the entity may collide with another entity
            using the ContextUtils.CollidingEntityContext which has this entity and the
            one colliding with this entity.
                        
            Example usage:
            ```javascript
            modifyBuilder.canCollideWith(context => {
                return true //Some Boolean value determining whether the entity may collide with another
            });
            ```
            """)
    public ModifyEntityBuilder canCollideWith(Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith) {
        this.canCollideWith = canCollideWith;
        return this;
    }


    @Info(value = """
            Defines in what condition the entity will start freezing.
                        
            Example usage:
            ```javascript
            modifyBuilder.isFreezing(entity => {
                return true;
            });
            ```
            """)
    public ModifyEntityBuilder isFreezing(Function<Entity, Object> isFreezing) {
        this.isFreezing = isFreezing;
        return this;
    }


    @Info(value = """
            Sets the block jump factor for the entity.
                        
            Example usage:
            ```javascript
            modifyBuilder.setBlockJumpFactor(entity => {
                //Set the jump factor for the entity through context
                return 1 //some float value;
            });
            ```
            """)
    public ModifyEntityBuilder setBlockJumpFactor(Function<Entity, Object> blockJumpFactor) {
        setBlockJumpFactor = blockJumpFactor;
        return this;
    }

    @Info(value = """
            Sets whether the entity is pushable.
                        
            Example usage:
            ```javascript
            modifyBuilder.isPushable(true);
            ```
            """)
    public ModifyEntityBuilder isPushable(boolean b) {
        isPushable = b;
        return this;
    }

    @Info(value = """
            @param positionRider A consumer determining the position of rider/riders.
                            
                Example usage:
                ```javascript
                modifyBuilder.positionRider(context => {
                    const {entity, passenger, moveFunction} = context
                });
                ```
            """)
    public ModifyEntityBuilder positionRider(Consumer<ContextUtils.PositionRiderContext> builderConsumer) {
        this.positionRider = builderConsumer;
        return this;
    }

    @Info(value = """
            Sets a predicate to determine if a passenger can be added to the entity.
                        
            @param predicate The predicate to check if a passenger can be added.
                        
            Example usage:
            ```javascript
            modifyBuilder.canAddPassenger(context => {
                // Custom logic to determine if a passenger can be added to the entity
                return true;
            });
            ```
            """)
    public ModifyEntityBuilder canAddPassenger(Function<ContextUtils.EPassengerEntityContext, Object> predicate) {
        canAddPassenger = predicate;
        return this;
    }


    @Info(value = """
            Sets the swim sound for the entity using a string representation.
                        
            Example usage:
            ```javascript
            modifyBuilder.setSwimSound("minecraft:entity.generic.swim");
            ```
            """)
    public ModifyEntityBuilder setSwimSound(Object sound) {
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
            modifyBuilder.setSwimSplashSound("minecraft:entity.generic.splash");
            ```
            """)
    public ModifyEntityBuilder setSwimSplashSound(Object sound) {
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
            modifyBuilder.blockSpeedFactor(entity => {
                // Define logic to calculate and return the block speed factor for the entity
                // Use information about the Entity provided by the context.
                return // Some Float value representing the block speed factor;
            });
            ```
            """)
    public ModifyEntityBuilder blockSpeedFactor(Function<Entity, Object> callback) {
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
            modifyBuilder.isFlapping(entity => {
                // Define logic to determine whether the entity is currently flapping
                // Use information about the Entity provided by the context.
                return // Some Boolean value indicating whether the entity is flapping;
            });
            ```
            """)
    public ModifyEntityBuilder isFlapping(Function<Entity, Object> b) {
        this.isFlapping = b;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity is added to the world.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is added to the world.
                        
            Example usage:
            ```javascript
            modifyBuilder.onAddedToWorld(entity => {
                // Define custom logic for handling when the entity is added to the world
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onAddedToWorld(Consumer<Entity> onAddedToWorldCallback) {
        this.onAddedToWorld = onAddedToWorldCallback;
        return this;
    }


    @Info(value = """
            Sets whether to reposition the entity after loading.
                        
            Example usage:
            ```javascript
            modifyBuilder.repositionEntityAfterLoad(true);
            ```
            """)
    public ModifyEntityBuilder repositionEntityAfterLoad(boolean customRepositionEntityAfterLoad) {
        this.repositionEntityAfterLoad = customRepositionEntityAfterLoad;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity starts sprinting.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has started sprinting.
                        
            Example usage:
            ```javascript
            modifyBuilder.onSprint(entity => {
                // Define custom logic for handling when the entity starts sprinting
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onSprint(Consumer<Entity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops riding.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has stopped being ridden.
                        
            Example usage:
            ```javascript
            modifyBuilder.onStopRiding(entity => {
                // Define custom logic for handling when the entity stops being ridden
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onStopRiding(Consumer<Entity> callback) {
        onStopRiding = callback;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed during each tick when the entity is being ridden.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being ridden.
                        
            Example usage:
            ```javascript
            modifyBuilder.rideTick(entity => {
                // Define custom logic for handling each tick when the entity is being ridden
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder rideTick(Consumer<Entity> callback) {
        rideTick = callback;
        return this;
    }

    @Info(value = """
            Sets a predicate function to determine whether the entity can undergo freezing.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be subjected to freezing.
                        
            Example usage:
            ```javascript
            modifyBuilder.canFreeze(entity => {
                // Define the conditions for the entity to be able to freeze
                // Use information about the Entity provided by the context.
                return true //someBoolean;
            });
            ```
            """)
    public ModifyEntityBuilder canFreeze(Function<Entity, Object> predicate) {
        canFreeze = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is currently glowing.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for its glowing state.
                        
            Example usage:
            ```javascript
            modifyBuilder.isCurrentlyGlowing(entity => {
                // Define the conditions to check if the entity is currently glowing
                // Use information about the Entity provided by the context.
                const isGlowing = // Some boolean condition to check if the entity is glowing;
                return isGlowing;
            });
            ```
            """)
    public ModifyEntityBuilder isCurrentlyGlowing(Function<Entity, Object> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }


    @Info(value = """
            Sets the minimum fall distance for the entity before taking damage.
                        
            Example usage:
            ```javascript
            modifyBuilder.setMaxFallDistance(entity => {
                // Define custom logic to determine the maximum fall distance
                // Use information about the Entity provided by the context.
                return 3;
            });
            ```
            """)
    public ModifyEntityBuilder setMaxFallDistance(Function<Entity, Object> maxFallDistance) {
        setMaxFallDistance = maxFallDistance;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed on the client side.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being removed on the client side.
                        
            Example usage:
            ```javascript
            modifyBuilder.onClientRemoval(entity => {
                // Define custom logic for handling the removal of the entity on the client side
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onClientRemoval(Consumer<Entity> consumer) {
        onClientRemoval = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hurt by lava.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is affected by lava.
                        
            Example usage:
            ```javascript
            modifyBuilder.lavaHurt(entity => {
                // Define custom logic for handling the entity being hurt by lava
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder lavaHurt(Consumer<Entity> consumer) {
        lavaHurt = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a flap action.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is flapping.
                        
            Example usage:
            ```javascript
            modifyBuilder.onFlap(entity => {
                // Define custom logic for handling the entity's flap action
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onFlap(Consumer<Entity> consumer) {
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
    public ModifyEntityBuilder dampensVibrations(Function<Entity, Object> predicate) {
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
    public ModifyEntityBuilder showVehicleHealth(Function<Entity, Object> predicate) {
        this.showVehicleHealth = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is hit by thunder.
            The provided Consumer accepts a {@link ContextUtils.ThunderHitContext} parameter,
            representing the context of the entity being hit by thunder.
                        
            Example usage:
            ```javascript
            modifyBuilder.thunderHit(context => {
                // Define custom logic for handling the entity being hit by thunder
                // Use information about the ThunderHitContext provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder thunderHit(Consumer<ContextUtils.EThunderHitContext> consumer) {
        thunderHit = consumer;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is invulnerable to a specific type of damage.
            The provided Predicate accepts a {@link ContextUtils.DamageContext} parameter,
            representing the context of the damage, and returns a boolean indicating invulnerability.
                        
            Example usage:
            ```javascript
            modifyBuilder.isInvulnerableTo(context => {
                // Define conditions for the entity to be invulnerable to the specific type of damage
                // Use information about the DamageContext provided by the context.
                return true // Some boolean condition indicating if the entity has invulnerability to the damage type;
            });
            ```
            """)
    public ModifyEntityBuilder isInvulnerableTo(Function<ContextUtils.EDamageContext, Object> predicate) {
        isInvulnerableTo = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can change dimensions.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may attempt to change dimensions.
                        
            Example usage:
            ```javascript
            modifyBuilder.canChangeDimensions(entity => {
                // Define the conditions for the entity to be able to change dimensions
                // Use information about the Entity provided by the context.
                return false // Some boolean condition indicating if the entity can change dimensions;
            });
            ```
            """)
    public ModifyEntityBuilder canChangeDimensions(Function<Entity, Object> supplier) {
        canChangeDimensions = supplier;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity may interact with something.
            The provided Predicate accepts a {@link ContextUtils.MayInteractContext} parameter,
            representing the context of the potential interaction, and returns a boolean.
                        
            Example usage:
            ```javascript
            modifyBuilder.mayInteract(context => {
                // Define conditions for the entity to be allowed to interact
                // Use information about the MayInteractContext provided by the context.
                return false // Some boolean condition indicating if the entity may interact;
            });
            ```
            """)
    public ModifyEntityBuilder mayInteract(Function<ContextUtils.EMayInteractContext, Object> predicate) {
        mayInteract = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can trample or step on something.
            The provided Predicate accepts a {@link ContextUtils.CanTrampleContext} parameter,
            representing the context of the potential trampling action, and returns a boolean.
                        
            Example usage:
            ```javascript
            modifyBuilder.canTrample(context => {
                // Define conditions for the entity to be allowed to trample
                // Use information about the CanTrampleContext provided by the context.
                return false // Some boolean condition indicating if the entity can trample;
            });
            ```
            """)
    public ModifyEntityBuilder canTrample(Function<ContextUtils.ECanTrampleContext, Object> predicate) {
        canTrample = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is removed from the world.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is being removed from the world.
                        
            Example usage:
            ```javascript
            modifyBuilder.onRemovedFromWorld(entity => {
                // Define custom logic for handling the removal of the entity from the world
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onRemovedFromWorld(Consumer<Entity> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }

    @Info(value = """
            Sets a callback function to be executed when the entity falls and takes damage.
            The provided Consumer accepts a {@link ContextUtils.EEntityFallDamageContext} parameter,
            representing the context of the entity falling and taking fall damage.
                        
            Example usage:
            ```javascript
            modifyBuilder.onFall(context => {
                // Define custom logic for handling when the entity falls and takes damage
                // Use information about the EEntityFallDamageContext provided by the context.
            });
            ```
            """)
    public ModifyEntityBuilder onFall(Consumer<ContextUtils.EEntityFallDamageContext> c) {
        onFall = c;
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
            Sets a consumer to handle lerping (linear interpolation) of the entity's position.
                        
            @param lerpTo Consumer accepting a {@link ContextUtils.LerpToContext} parameter,
                            providing information and control over the lerping process.
                        
            Example usage:
            ```javascript
            modifyBuilder.lerpTo(context => {
                // Custom logic for lerping the entity's position
                // Access information about the lerping process using the provided context.
            });
            ```
            """)
    public ModifyEntityBuilder lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity should render at a squared distance.
                        
            @param shouldRenderAtSqrDistance Function accepting a {@link ContextUtils.EntitySqrDistanceContext} parameter,
                             defining the conditions under which the entity should render.
                        
            Example usage:
            ```javascript
            modifyBuilder.shouldRenderAtSqrDistance(context => {
                // Custom logic to determine whether the entity should render
                // Access information about the distance using the provided context.
                return true;
            });
            ```
            """)
    public ModifyEntityBuilder shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
        shouldRenderAtSqrDistance = func;
        return this;
    }


    @Info(value = """
            Sets whether the entity is attackable or not.
                        
            @param isAttackable Boolean value indicating whether the entity is attackable.
                        
            Example usage:
            ```javascript
            modifyBuilder.isAttackable(true);
            ```
            """)
    public ModifyEntityBuilder isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a player touches the entity.
            The provided Consumer accepts a {@link ContextUtils.EntityPlayerContext} parameter,
            representing the context of the player's interaction with the entity.
                        
            Example usage:
            ```javascript
            modifyBuilder.playerTouch(context => {
                // Custom logic to handle the player's touch interaction with the entity
                // Access information about the interaction using the provided context.
            });
            ```
            """)
    public ModifyEntityBuilder playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a movement action.
            The provided Consumer accepts a {@link ContextUtils.MovementContext} parameter,
            representing the context of the entity's movement.
                        
            Example usage:
            ```javascript
            modifyBuilder.move(context => {
                // Custom logic to handle the entity's movement action
                // Access information about the movement using the provided context.
            });
            ```
            """)
    public ModifyEntityBuilder move(Consumer<ContextUtils.MovementContext> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed on each tick for the entity.
                        
            @param tick A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            modifyBuilder.tick(entity => {
                // Custom logic to be executed on each tick of the entity.
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public ModifyEntityBuilder tick(Consumer<Entity> consumer) {
        tick = consumer;
        return this;
    }
}
