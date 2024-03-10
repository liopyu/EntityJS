package net.liopyu.entityjs.builders.partbuilders;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.EventBasedSpawnModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class PartBuilder {
    public transient boolean isPickable;
    public transient Function<Entity, Boolean> canBeHurt;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Consumer<ContextUtils.EntityPlayerContext> playerTouch;
    public transient Function<ContextUtils.EntitySqrDistanceContext, Object> shouldRenderAtSqrDistance;
    public transient Consumer<Entity> tick;
    public transient Consumer<ContextUtils.MovementContext> move;
    public transient Function<Entity, Object> isAttackable;

    //new overrides
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
    public transient boolean isPushable;
    public transient Function<Entity, Object> shouldDropLoot;
    public transient Function<ContextUtils.EPassengerEntityContext, Object> canAddPassenger;
    public transient Function<Entity, Object> isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient Function<Entity, Object> isImmobile;
    public transient Function<Entity, Object> setBlockJumpFactor;
    public transient Function<Entity, Object> blockSpeedFactor;
    public transient Float setSoundVolume;
    public transient Float setWaterSlowDown;
    public transient Object setSwimSound;
    public transient Function<Entity, Object> isFlapping;
    public transient Object setDeathSound;
    public transient BaseEntityBuilder.RenderType renderType;
    public transient EntityType<?> getType;
    public transient Object mainArm;

    public transient Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch;

    public transient Function<ContextUtils.EntityPoseDimensionsContext, Object> setStandingEyeHeight;

    public transient Consumer<Entity> onDecreaseAirSupply;
    public transient Consumer<Entity> onBlockedByShield;

    public transient Boolean repositionEntityAfterLoad;

    public transient Function<Entity, Object> nextStep;

    public transient Consumer<Entity> onIncreaseAirSupply;

    public transient Function<ContextUtils.HurtContext, Object> setHurtSound;

    public transient Object setSwimSplashSound;


    public transient Function<ContextUtils.EntityTypeEntityContext, Object> canAttackType;

    public transient Function<Entity, Object> scale;

    public transient Function<Entity, Object> shouldDropExperience;

    public transient Function<Entity, Object> experienceReward;


    public transient Consumer<ContextUtils.EntityEquipmentContext> onEquipItem;


    public transient Function<ContextUtils.VisualContext, Object> visibilityPercent;

    public transient Function<ContextUtils.EntityTargetContext, Object> canAttack;

    public transient Function<ContextUtils.OnEffectContext, Object> canBeAffected;

    public transient Function<Entity, Object> invertedHealAndHarm;

    public transient Consumer<ContextUtils.OnEffectContext> onEffectAdded;


    public transient Consumer<ContextUtils.OnEffectContext> onEffectRemoved;

    public transient Consumer<ContextUtils.EntityHealContext> onLivingHeal;


    public transient Consumer<ContextUtils.EntityDamageContext> onHurt;


    public transient Consumer<ContextUtils.DeathContext> onDeath;


    public transient Consumer<ContextUtils.EntityLootContext> dropCustomDeathLoot;


    public transient Object eatingSound;

    public transient Function<Entity, Object> onClimbable;
    public transient Boolean canBreatheUnderwater;

    public transient Consumer<ContextUtils.EEntityFallDamageContext> onLivingFall;

    public transient Consumer<Entity> onSprint;

    public transient Function<Entity, Object> jumpBoostPower;
    public transient Function<ContextUtils.EntityFluidStateContext, Object> canStandOnFluid;


    public transient Function<Entity, Object> isSensitiveToWater;

    public transient Consumer<Entity> onStopRiding;
    public transient Consumer<Entity> rideTick;


    public transient Consumer<ContextUtils.EntityItemEntityContext> onItemPickup;
    public transient Function<ContextUtils.LineOfSightContext, Object> hasLineOfSight;

    public transient Consumer<Entity> onEnterCombat;
    public transient Consumer<Entity> onLeaveCombat;

    public transient Function<Entity, Object> isAffectedByPotions;


    public transient Function<ContextUtils.EntityItemLevelContext, Object> canTakeItem;

    public transient Function<Entity, Object> isSleeping;
    public transient Consumer<ContextUtils.EntityBlockPosContext> onStartSleeping;
    public transient Consumer<Entity> onStopSleeping;

    public transient Consumer<ContextUtils.EntityItemLevelContext> eat;

    public transient Function<ContextUtils.PlayerEntityContext, Object> shouldRiderFaceForward;

    public transient Function<Entity, Object> canFreeze;
    public transient Function<Entity, Object> isCurrentlyGlowing;
    public transient Function<Entity, Object> canDisableShield;
    public transient Function<Entity, Object> setMaxFallDistance;
    public transient Consumer<ContextUtils.MobInteractContext> onInteract;

    public transient Consumer<Entity> onClientRemoval;
    public transient Consumer<Entity> onAddedToWorld;
    public transient Consumer<Entity> lavaHurt;
    public transient Consumer<Entity> onFlap;
    public transient Function<Entity, Object> dampensVibrations;

    public transient Function<Entity, Object> showVehicleHealth;

    public transient Consumer<ContextUtils.EThunderHitContext> thunderHit;
    public transient Function<ContextUtils.EDamageContext, Object> isInvulnerableTo;
    public transient Function<Entity, Object> canChangeDimensions;
    public transient Function<ContextUtils.CalculateFallDamageContext, Object> calculateFallDamage;
    public transient Function<ContextUtils.EMayInteractContext, Object> mayInteract;
    public transient Function<ContextUtils.ECanTrampleContext, Object> canTrample;
    public transient Consumer<Entity> onRemovedFromWorld;
    public transient Consumer<Entity> onLivingJump;
    public transient Consumer<Entity> aiStep;

    public transient MobType mobType;
    public transient Function<Entity, Object> isFreezing;

    public transient Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith;

    public PartBuilder() {
        isPickable = true;
    }

    public PartBuilder canBeHurt(Function<Entity, Boolean> canBeHurt) {
        this.canBeHurt = canBeHurt;
        return this;
    }

    public PartBuilder isPickable(boolean isPickable) {
        this.isPickable = isPickable;
        return this;
    }

    @Info(value = """
            Sets a consumer to handle lerping (linear interpolation) of the entity's position.
                        
            @param consumer Consumer accepting a {@link ContextUtils.LerpToContext} parameter,
                            providing information and control over the lerping process.
                        
            Example usage:
            ```javascript
            entityBuilder.lerpTo(context => {
                // Custom logic for lerping the entity's position
                // Access information about the lerping process using the provided context.
            });
            ```
            """)
    public PartBuilder lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity should render at a squared distance.
                        
            @param function Function accepting a {@link ContextUtils.EntitySqrDistanceContext} parameter,
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
    public PartBuilder shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
        shouldRenderAtSqrDistance = func;
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
    public PartBuilder playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
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
    public PartBuilder move(Consumer<ContextUtils.MovementContext> consumer) {
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
    public PartBuilder tick(Consumer<Entity> consumer) {
        tick = consumer;
        return this;
    }

    @Info(value = """
            Consumer overriding the tickDeath responsible to counting down
            the ticks it takes to remove the entity when it dies.
                        
            Example usage:
            ```javascript
            entityBuilder.deathTick(entity => {
                // Override the tickDeath method in the entity
            });
            ```
            """)
    public PartBuilder tickDeath(Consumer<Entity> tickDeath) {
        this.tickDeath = tickDeath;
        return this;
    }

    @Info(value = """
            Boolean determining whether the entity can jump while mounted by a player.
            (Currently experimental jumping logic subject to change in the future)
            Defaults to false.
            Example usage:
            ```javascript
            entityBuilder.mountJumpingEnabled(true);
            ```
            """)
    public PartBuilder mountJumpingEnabled(boolean mountJumpingEnabled) {
        this.mountJumpingEnabled = mountJumpingEnabled;
        return this;
    }


    @Info(value = """
            Boolean determining whether the passenger is able to steer the entity while riding.
            Defaults to true.
            Example usage:
            ```javascript
            entityBuilder.canSteer(false);
            ```
            """)
    public PartBuilder canSteer(boolean canSteer) {
        this.canSteer = canSteer;
        return this;
    }


    @Info(value = """
            Boolean determining if the entity will turn sideways on death.
            Defaults to true.
            Example usage:
            ```javascript
            entityBuilder.defaultDeathPose(false);
            ```
            """)
    public PartBuilder defaultDeathPose(boolean defaultDeathPose) {
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
    public PartBuilder canCollideWith(Function<ContextUtils.ECollidingEntityContext, Object> canCollideWith) {
        this.canCollideWith = canCollideWith;
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
    public PartBuilder mobType(Object mt) {
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
    public PartBuilder isFreezing(Function<Entity, Object> isFreezing) {
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
    public PartBuilder render(Consumer<ContextUtils.RenderContext> render) {
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
    public PartBuilder mainArm(Object arm) {
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
    public PartBuilder sized(float width, float height) {
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
    public PartBuilder saves(boolean shouldSave) {
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
    public PartBuilder fireImmune(boolean isFireImmune) {
        this.fireImmune = isFireImmune;
        return this;
    }

    @Info(value = """
            Sets the list of block names to which the entity is immune.
                        
            Example usage:
            ```javascript
            entityBuilder.immuneTo("minecraft:stone", "minecraft:dirt");
            ```
            """)
    public PartBuilder immuneTo(String... blockNames) {
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
    public PartBuilder canSpawnFarFromPlayer(boolean canSpawnFar) {
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
    public PartBuilder setBlockJumpFactor(Function<Entity, Object> blockJumpFactor) {
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
    public PartBuilder setWaterSlowDown(float slowdownFactor) {
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
    public PartBuilder setSoundVolume(float volume) {
        this.setSoundVolume = volume;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity should drop loot upon death.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity whose loot dropping behavior is being determined.
            It returns a Boolean indicating whether the entity should drop loot.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldDropLoot(entity => {
                // Define logic to determine whether the entity should drop loot
                // Use information about the Entity provided by the context.
                return // Some Boolean value indicating whether the entity should drop loot;
            });
            ```
            """)
    public PartBuilder shouldDropLoot(Function<Entity, Object> b) {
        this.shouldDropLoot = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed during the living entity's AI step.
            The provided Consumer accepts a {@link Entity} parameter,
            allowing customization of the AI behavior.
                        
            Example usage:
            ```javascript
            entityBuilder.aiStep(entity => {
                // Custom logic to be executed during the living entity's AI step
                // Access and modify information about the entity using the provided context.
            });
            ```
            """)
    public PartBuilder aiStep(Consumer<Entity> aiStep) {
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
    public PartBuilder onLivingJump(Consumer<Entity> onJump) {
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
    public PartBuilder clientTrackingRange(int i) {
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
    public PartBuilder updateInterval(int i) {
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
    public PartBuilder mobCategory(String category) {
        mobCategory = stringToMobCategory(category);
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
    public PartBuilder modelResource(Function<T, Object> function) {
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
    public PartBuilder textureResource(Function<T, Object> function) {
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
    public PartBuilder animationResource(Function<T, Object> function) {
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
    public PartBuilder isPushable(boolean b) {
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
    public PartBuilder canAddPassenger(Function<ContextUtils.EPassengerEntityContext, Object> predicate) {
        canAddPassenger = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity is affected by fluids.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity whose interaction with fluids is being determined.
            It returns a Boolean indicating whether the entity is affected by fluids.
                        
            Example usage:
            ```javascript
            entityBuilder.isAffectedByFluids(entity => {
                // Define logic to determine whether the entity is affected by fluids
                // Use information about the Entity provided by the context.
                return // Some Boolean value indicating whether the entity is affected by fluids;
            });
            ```
            """)
    public PartBuilder isAffectedByFluids(Function<Entity, Object> b) {
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
    public PartBuilder setSummonable(boolean b) {
        summonable = b;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine whether the entity is immobile.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity whose immobility is being determined.
            It returns a Boolean indicating whether the entity is immobile.
                        
            Example usage:
            ```javascript
            entityBuilder.isImmobile(entity => {
                // Define logic to determine whether the entity is immobile
                // Use information about the Entity provided by the context.
                return // Some Boolean value indicating whether the entity is immobile;
            });
            ```
            """)
    public PartBuilder isImmobile(Function<Entity, Object> b) {
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
    public PartBuilder isAlwaysExperienceDropper(boolean b) {
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
    public PartBuilder calculateFallDamage(Function<ContextUtils.CalculateFallDamageContext, Object> calculation) {
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
    public PartBuilder setDeathSound(Object sound) {
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
    public PartBuilder setSwimSound(Object sound) {
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
    public PartBuilder setSwimSplashSound(Object sound) {
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
    public PartBuilder blockSpeedFactor(Function<Entity, Object> callback) {
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
    public PartBuilder isFlapping(Function<Entity, Object> b) {
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
    public PartBuilder onAddedToWorld(Consumer<Entity> onAddedToWorldCallback) {
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
    public PartBuilder doAutoAttackOnTouch(Consumer<ContextUtils.AutoAttackContext> doAutoAttackOnTouch) {
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
    public PartBuilder setStandingEyeHeight(Function<ContextUtils.EntityPoseDimensionsContext, Object> setStandingEyeHeight) {
        this.setStandingEyeHeight = setStandingEyeHeight;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's air supply decreases.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity whose air supply is being decreased.
                        
            Example usage:
            ```javascript
            entityBuilder.onDecreaseAirSupply(entity => {
                // Define custom logic for handling when the entity's air supply decreases
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onDecreaseAirSupply(Consumer<Entity> onDecreaseAirSupply) {
        this.onDecreaseAirSupply = onDecreaseAirSupply;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity is blocked by a shield.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that is blocked by a shield.
                        
            Example usage:
            ```javascript
            entityBuilder.onBlockedByShield(entity => {
                // Define custom logic for handling when the entity is blocked by a shield
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onBlockedByShield(Consumer<Entity> onBlockedByShield) {
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
    public PartBuilder repositionEntityAfterLoad(boolean customRepositionEntityAfterLoad) {
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
    public PartBuilder nextStep(Function<Entity, Object> nextStep) {
        this.nextStep = nextStep;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity's air supply increases.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity whose air supply is being increased.
                        
            Example usage:
            ```javascript
            entityBuilder.onIncreaseAirSupply(entity => {
                // Define custom logic for handling when the entity's air supply increases
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onIncreaseAirSupply(Consumer<Entity> onIncreaseAirSupply) {
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
    public PartBuilder setHurtSound(Function<ContextUtils.HurtContext, Object> sound) {
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
    public PartBuilder canAttackType(Function<ContextUtils.EntityTypeEntityContext, Object> canAttackType) {
        this.canAttackType = canAttackType;
        return this;
    }


    @Info(value = """
            Sets a function to determine the custom hitbox scale of the entity.
            The provided Function accepts a {@link Entity} parameter,
            representing the entity whose scale is being determined.
            It returns a Float representing the custom scale.
                        
            Example usage:
            ```javascript
            entityBuilder.scale(entity => {
                // Define logic to calculate and return the custom scale for the entity
                // Use information about the Entity provided by the context.
                return // Some Float value;
            });
            ```
            """)
    public PartBuilder scale(Function<Entity, Object> customScale) {
        this.scale = customScale;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity should drop experience upon death.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity whose experience drop is being determined.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldDropExperience(entity => {
                // Define conditions to check if the entity should drop experience upon death
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity should drop experience;
            });
            ```
            """)
    public PartBuilder shouldDropExperience(Function<Entity, Object> p) {
        this.shouldDropExperience = p;
        return this;
    }


    @Info(value = """
            Sets a function to determine the experience reward for killing the entity.
            The provided Function accepts a {@link Entity} parameter,
            representing the entity whose experience reward is being determined.
            It returns an Integer representing the experience reward.
                        
            Example usage:
            ```javascript
            entityBuilder.experienceReward(killedEntity => {
                // Define logic to calculate and return the experience reward for the killedEntity
                // Use information about the Entity provided by the context.
                return // Some Integer value representing the experience reward;
            });
            ```
            """)
    public PartBuilder experienceReward(Function<Entity, Object> experienceReward) {
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
    public PartBuilder onEquipItem(Consumer<ContextUtils.EntityEquipmentContext> onEquipItem) {
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
    public PartBuilder visibilityPercent(Function<ContextUtils.VisualContext, Object> visibilityPercent) {
        this.visibilityPercent = visibilityPercent;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity can attack another entity.
            The provided Predicate accepts a {@link ContextUtils.EntityContext} parameter,
            representing the entity that may be attacked.
                        
            Example usage:
            ```javascript
            entityBuilder.canAttack(context => {
                // Define conditions to check if the entity can attack the targetEntity
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity can attack the targetEntity;
            });
            ```
            """)
    public PartBuilder canAttack(Function<ContextUtils.EntityTargetContext, Object> customCanAttack) {
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
    public PartBuilder canBeAffected(Function<ContextUtils.OnEffectContext, Object> predicate) {
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
    public PartBuilder invertedHealAndHarm(Function<Entity, Object> invertedHealAndHarm) {
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
    public PartBuilder onEffectAdded(Consumer<ContextUtils.OnEffectContext> consumer) {
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
    public PartBuilder onLivingHeal(Consumer<ContextUtils.EntityHealContext> callback) {
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
    public PartBuilder onEffectRemoved(Consumer<ContextUtils.OnEffectContext> consumer) {
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
    public PartBuilder onHurt(Consumer<ContextUtils.EntityDamageContext> predicate) {
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
    public PartBuilder onDeath(Consumer<ContextUtils.DeathContext> consumer) {
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
    public PartBuilder dropCustomDeathLoot(Consumer<ContextUtils.EntityLootContext> consumer) {
        dropCustomDeathLoot = consumer;
        return this;
    }


    @Info(value = """
            Sets the sound resource location for the entity's eating sound using either a string representation or a ResourceLocation object.
                        
            Example usage:
            ```javascript
            entityBuilder.eatingSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public PartBuilder eatingSound(Object sound) {
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
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for being on a climbable surface.
                        
            Example usage:
            ```javascript
            entityBuilder.onClimbable(entity => {
                // Define conditions to check if the entity is on a climbable surface
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity is on a climbable surface;
            });
            ```
            """)
    public PartBuilder onClimbable(Function<Entity, Object> predicate) {
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
    public PartBuilder canBreatheUnderwater(boolean canBreatheUnderwater) {
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
    public PartBuilder onLivingFall(Consumer<ContextUtils.EEntityFallDamageContext> c) {
        onLivingFall = c;
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
    public PartBuilder onSprint(Consumer<Entity> consumer) {
        onSprint = consumer;
        return this;
    }


    @Info(value = """
            Sets the jump boost power for the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.jumpBoostPower(entity => {
                return //some float value
            });
            ```
            """)
    public PartBuilder jumpBoostPower(Function<Entity, Object> jumpBoostPower) {
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
    public PartBuilder canStandOnFluid(Function<ContextUtils.EntityFluidStateContext, Object> predicate) {
        canStandOnFluid = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is sensitive to water.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for sensitivity to water.
                        
            Example usage:
            ```javascript
            entityBuilder.isSensitiveToWater(entity => {
                // Define conditions to check if the entity is sensitive to water
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity is sensitive to water;
            });
            ```
            """)
    public PartBuilder isSensitiveToWater(Function<Entity, Object> predicate) {
        isSensitiveToWater = predicate;
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
    public PartBuilder onStopRiding(Consumer<Entity> callback) {
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
    public PartBuilder rideTick(Consumer<Entity> callback) {
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
    public PartBuilder onItemPickup(Consumer<ContextUtils.EntityItemEntityContext> consumer) {
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
    public PartBuilder hasLineOfSight(Function<ContextUtils.LineOfSightContext, Object> f) {
        hasLineOfSight = f;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity enters combat.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has entered combat.
                        
            Example usage:
            ```javascript
            entityBuilder.onEnterCombat(entity => {
                // Define custom logic for handling the entity entering combat
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onEnterCombat(Consumer<Entity> c) {
        onEnterCombat = c;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity leaves combat.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has left combat.
                        
            Example usage:
            ```javascript
            entityBuilder.onLeaveCombat(entity => {
                // Define custom logic for handling the entity leaving combat
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onLeaveCombat(Consumer<Entity> runnable) {
        onLeaveCombat = runnable;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is affected by potions.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for its susceptibility to potions.
                        
            Example usage:
            ```javascript
            entityBuilder.isAffectedByPotions(entity => {
                // Define conditions to check if the entity is affected by potions
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity is affected by potions;
            });
            ```
            """)
    public PartBuilder isAffectedByPotions(Function<Entity, Object> predicate) {
        isAffectedByPotions = predicate;
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
    public PartBuilder isAttackable(Function<Entity, Object> predicate) {
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
    public PartBuilder canTakeItem(Function<ContextUtils.EntityItemLevelContext, Object> predicate) {
        canTakeItem = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate function to determine whether the entity is currently sleeping.
            The provided Predicate accepts a {@link Entity} parameter,
            representing the entity that may be checked for its sleeping state.
                        
            Example usage:
            ```javascript
            entityBuilder.isSleeping(entity => {
                // Define conditions to check if the entity is currently sleeping
                // Use information about the Entity provided by the context.
                return // Some boolean condition indicating if the entity is sleeping;
            });
            ```
            """)
    public PartBuilder isSleeping(Function<Entity, Object> supplier) {
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
    public PartBuilder onStartSleeping(Consumer<ContextUtils.EntityBlockPosContext> consumer) {
        onStartSleeping = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity stops sleeping.
            The provided Consumer accepts a {@link Entity} parameter,
            representing the entity that has stopped sleeping.
                        
            Example usage:
            ```javascript
            entityBuilder.onStopSleeping(entity => {
                // Define custom logic for handling the entity stopping sleeping
                // Use information about the Entity provided by the context.
            });
            ```
            """)
    public PartBuilder onStopSleeping(Consumer<Entity> runnable) {
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
    public PartBuilder eat(Consumer<ContextUtils.EntityItemLevelContext> function) {
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
    public PartBuilder shouldRiderFaceForward(Function<ContextUtils.PlayerEntityContext, Object> predicate) {
        shouldRiderFaceForward = predicate;
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
    public PartBuilder canFreeze(Function<Entity, Object> predicate) {
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
    public PartBuilder isCurrentlyGlowing(Function<Entity, Object> predicate) {
        isCurrentlyGlowing = predicate;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity can disable its target's shield.
            The provided Predicate accepts a {@link Entity} parameter.
                        
            Example usage:
            ```javascript
            entityBuilder.canDisableShield(entity => {
                // Define the conditions to check if the entity can disable its shield
                // Use information about the Entity provided by the context.
                return true;
            });
            ```
            """)
    public PartBuilder canDisableShield(Function<Entity, Object> predicate) {
        canDisableShield = predicate;
        return this;
    }

    @Info(value = """
            Sets a consumer to handle the interaction with the entity.
            The provided Consumer accepts a {@link ContextUtils.MobInteractContext} parameter,
            representing the context of the interaction
                        
            Example usage:
            ```javascript
            entityBuilder.onInteract(context => {
                // Define custom logic for the interaction with the entity
                // Use information about the MobInteractContext provided by the context.
                if (context.player.isShiftKeyDown()) return
                context.player.startRiding(context.entity);
            });
            ```
            """)
    public PartBuilder onInteract(Consumer<ContextUtils.MobInteractContext> c) {
        onInteract = c;
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
    public PartBuilder setMaxFallDistance(Function<Entity, Object> maxFallDistance) {
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
    public PartBuilder onClientRemoval(Consumer<Entity> consumer) {
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
    public PartBuilder lavaHurt(Consumer<Entity> consumer) {
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
    public PartBuilder onFlap(Consumer<Entity> consumer) {
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
    public PartBuilder dampensVibrations(Function<Entity, Object> predicate) {
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
    public PartBuilder showVehicleHealth(Function<Entity, Object> predicate) {
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
    public PartBuilder thunderHit(Consumer<ContextUtils.EThunderHitContext> consumer) {
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
    public PartBuilder isInvulnerableTo(Function<ContextUtils.EDamageContext, Object> predicate) {
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
    public PartBuilder canChangeDimensions(Function<Entity, Object> supplier) {
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
    public PartBuilder mayInteract(Function<ContextUtils.EMayInteractContext, Object> predicate) {
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
    public PartBuilder canTrample(Function<ContextUtils.ECanTrampleContext, Object> predicate) {
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
    public PartBuilder onRemovedFromWorld(Consumer<Entity> consumer) {
        onRemovedFromWorld = consumer;
        return this;
    }
}
