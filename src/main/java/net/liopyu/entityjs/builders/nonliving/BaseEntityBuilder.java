package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.PartBuilder;
import net.liopyu.entityjs.entities.living.entityjs.AnimalEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.liolib.core.animation.Animation;
import net.liopyu.liolib.core.animation.AnimationController;
import net.liopyu.liolib.core.animation.AnimationState;
import net.liopyu.liolib.core.animation.RawAnimation;
import net.liopyu.liolib.core.keyframe.event.CustomInstructionKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.KeyFrameEvent;
import net.liopyu.liolib.core.keyframe.event.ParticleKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.SoundKeyframeEvent;
import net.liopyu.liolib.core.keyframe.event.data.KeyFrameData;
import net.liopyu.liolib.core.object.DataTicket;
import net.liopyu.liolib.core.object.PlayState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    public BaseEntityBuilder(ResourceLocation i) {
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
        renderType = BaseEntityBuilder.RenderType.CUTOUT;
        animationSuppliers = new ArrayList<>();
        modelResource = t -> newID("geo/entity/", ".geo.json");
        textureResource = t -> newID("textures/entity/", ".png");
        animationResource = t -> newID("animations/entity/", ".animation.json");
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
    public BaseEntityBuilder<T> animationResource(Function<T, Object> function) {
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
                        
            @param consumer A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
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

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
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
     * A wrapper around {@link net.liopyu.liolib.core.animation.AnimationController.AnimationStateHandler IAnimationPredicate}
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
        private final net.liopyu.liolib.core.animation.AnimationState<E> parent;

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
