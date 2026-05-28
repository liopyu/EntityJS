package net.liopyu.entityjs.builders.misc;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.liopyu.entityjs.client.living.model.CustomGeoLayerJSBuilder;
import net.liopyu.entityjs.client.living.model.GeoLayerJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.AnimalEntityJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.util.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLLoader;
import com.geckolib.animation.*;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.KeyFrameEvent;
import com.geckolib.cache.animation.keyframeevent.CustomInstructionKeyframeData;
import com.geckolib.cache.animation.keyframeevent.KeyFrameData;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.animation.object.PlayState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


public abstract class CustomEntityJSBuilder extends BuilderBase<EntityType<?>> {
    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient Function<LivingEntity, Object> modelResource;
    public transient Function<LivingEntity, Object> textureResource;
    public transient Function<LivingEntity, Object> animationResource;
    public transient CustomEntityJSBuilder.RenderType renderType;
    public transient final List<CustomEntityJSBuilder.AnimationControllerSupplier<?>> animationSuppliers;
    public static final List<CustomEntityJSBuilder> thisList = new ArrayList<>();
    public transient Consumer<ContextUtils.RenderContextCustom<?>> render;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient Identifier[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient float scaleHeight;
    public transient float scaleWidth;
    public transient Consumer<ContextUtils.ScaleModelRenderContext> scaleModelForRender;
    public final List<CustomGeoLayerJSBuilder<? extends LivingEntity>> layerList = new ArrayList<>();
    public final List<CustomGeoLayerJSBuilder<? extends LivingEntity>> glowingLayerList = new ArrayList<>();
    public transient Boolean defaultDeathPose;

    public CustomEntityJSBuilder(Identifier i) {
        super(i);
        thisList.add(this);
        translationKey("entity." + i.getNamespace() + "." + i.getPath());
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        immuneTo = new Identifier[0];
        fireImmune = false;
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 1;
        mobCategory = MobCategory.MISC;
        renderType = CustomEntityJSBuilder.RenderType.CUTOUT;
        animationSuppliers = new ArrayList<>();
        modelResource = t -> newID("geo/entity/", ".geo.json");
        textureResource = t -> newID("textures/entity/", ".png");
        animationResource = t -> newID("animations/entity/", ".animation.json");
        scaleHeight = 1F;
        scaleWidth = 1F;
        defaultDeathPose = true;
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
                return "kubejs:geo/entity/wyrm.geo.json" // Some Identifier representing the model resource;
            });
            ```
            """)
    public CustomEntityJSBuilder modelResource(Function<LivingEntity, Object> function) {
        modelResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return Identifier.parse((String) obj);
            } else if (obj instanceof Identifier) {
                return (Identifier) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid model resource: " + obj + ". Defaulting to " + this.newID("geo/entity/", ".geo.json"));
                return this.newID("geo/entity/", ".geo.json");
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
                return "kubejs:textures/entity/wyrm.png" // Some Identifier representing the texture resource;
            });
            ```
            """)
    public CustomEntityJSBuilder textureResource(Function<LivingEntity, Object> function) {
        textureResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return Identifier.parse((String) obj);
            } else if (obj instanceof Identifier) {
                return (Identifier) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid texture resource: " + obj + ". Defaulting to " + this.newID("textures/entity/", ".png"));
                return this.newID("textures/entity/", ".png");
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
                //return some Identifier representing the animation resource;
                return "kubejs:animations/entity/wyrm.animation.json" // Some Identifier representing the animation resource;
            });
            ```
            """)
    public CustomEntityJSBuilder animationResource(Function<LivingEntity, Object> function) {
        animationResource = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return Identifier.parse((String) obj);
            } else if (obj instanceof Identifier) {
                return (Identifier) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid animation resource: " + obj + ". Defaulting to " + this.newID("animations/entity/", ".animation.json"));
                return this.newID("animations/entity/", ".animation.json");
            }
        };
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
    public CustomEntityJSBuilder defaultDeathPose(boolean defaultDeathPose) {
        this.defaultDeathPose = defaultDeathPose;
        return this;
    }

    @Info(value = """
            Adds an extra render layer to the mob.
            @param newGeoLayer The builder Consumer for the new render layer.
            
                Example usage:
                ```javascript
                entityBuilder.newGeoLayer(builder => {
                    builder.textureResource(entity => {
                        return "kubejs:textures/entity/sasuke.png"
                    })
                });
                ```
            """)
    public CustomEntityJSBuilder newGeoLayer(Consumer<CustomGeoLayerJSBuilder<? extends LivingEntity>> builderConsumer) {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            CustomGeoLayerJSBuilder<? extends LivingEntity> layerBuild = new CustomGeoLayerJSBuilder<>(this);
            builderConsumer.accept(layerBuild);
            layerList.add(layerBuild);
        }
        return this;
    }

    @Info(value = """
            Adds an extra glowing render layer to the mob.
            @param newGlowingGeoLayer The builder Consumer for the new render layer.
            
                Example usage:
                ```javascript
                entityBuilder.newGlowingGeoLayer(builder => {
                    builder.textureResource(entity => {
                        return "kubejs:textures/entity/sasuke.png"
                    })
                });
                ```
            """)
    public CustomEntityJSBuilder newGlowingGeoLayer(Consumer<CustomGeoLayerJSBuilder<? extends LivingEntity>> builderConsumer) {
        if (FMLLoader.getDist() == Dist.CLIENT) {
            CustomGeoLayerJSBuilder<? extends LivingEntity> layerBuild = new CustomGeoLayerJSBuilder<>(this);
            builderConsumer.accept(layerBuild);
            glowingLayerList.add(layerBuild);
        }
        return this;
    }

    @Info(value = """
            Sets the scale of the model.
            
            Example usage:
            ```javascript
            entityBuilder.modelSize(2,2);
            ```
            """)
    public CustomEntityJSBuilder modelSize(float scaleHeight, float scaleWidth) {
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
    public CustomEntityJSBuilder scaleModelForRender(Consumer<ContextUtils.ScaleModelRenderContext> scaleModelForRender) {
        this.scaleModelForRender = scaleModelForRender;
        return this;
    }

    @Info(value = """
            Sets the list of block names to which the entity is immune.
            
            Example usage:
            ```javascript
            entityBuilder.immuneTo("minecraft:stone", "minecraft:dirt");
            ```
            """)
    public CustomEntityJSBuilder immuneTo(String... blockNames) {
        this.immuneTo = Arrays.stream(blockNames)
                .map(Identifier::parse)
                .toArray(Identifier[]::new);
        return this;
    }


    @Info(value = """
            Sets whether the entity can spawn far from the player.
            
            Example usage:
            ```javascript
            entityBuilder.canSpawnFarFromPlayer(true);
            ```
            """)
    public CustomEntityJSBuilder canSpawnFarFromPlayer(boolean canSpawnFar) {
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
    public CustomEntityJSBuilder render(Consumer<ContextUtils.RenderContextCustom<?>> render) {
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
    public CustomEntityJSBuilder setSummonable(boolean b) {
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
    public CustomEntityJSBuilder mobCategory(String category) {
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
    public CustomEntityJSBuilder saves(boolean shouldSave) {
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
    public CustomEntityJSBuilder fireImmune(boolean isFireImmune) {
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
    public CustomEntityJSBuilder sized(float width, float height) {
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
    public CustomEntityJSBuilder clientTrackingRange(int trackingRange) {
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
    public CustomEntityJSBuilder updateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
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
    public CustomEntityJSBuilder addAnimationController(String name, int translationTicksLength, CustomEntityJSBuilder.IAnimationPredicateJS<?> predicate) {
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
    public CustomEntityJSBuilder addKeyAnimationController(
            String name,
            int translationTicksLength,
            CustomEntityJSBuilder.IAnimationPredicateJS predicate,
            @Nullable CustomEntityJSBuilder.ISoundListenerJS soundListener,
            @Nullable CustomEntityJSBuilder.IParticleListenerJS particleListener,
            @Nullable CustomEntityJSBuilder.ICustomInstructionListenerJS instructionListener
    ) {
        animationSuppliers.add(new CustomEntityJSBuilder.AnimationControllerSupplier<>(name, translationTicksLength, predicate, null, null, null, soundListener, particleListener, instructionListener));
        return this;
    }

    public transient Function<LivingEntity, net.minecraft.client.renderer.rendertype.RenderType> renderTypeFunction;

    @Info(value = """
            Sets the render type for the entity via a function.
            
            Example usage:
            ```javascript
            entityBuilder.renderType(entity => RenderTypes.entityCutout("kubejs:path/to/texture", outlineEntityBoolean));
            ```
            """)
    public CustomEntityJSBuilder renderType(Function<LivingEntity, net.minecraft.client.renderer.rendertype.RenderType> type) {
        renderTypeFunction = type;
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
    public CustomEntityJSBuilder setRenderType(Object type) {
        if (type instanceof CustomEntityJSBuilder.RenderType) {
            renderType = (CustomEntityJSBuilder.RenderType) type;
        } else if (type instanceof String) {
            String typeString = (String) type;
            switch (typeString.toLowerCase()) {
                case "solid":
                    renderType = CustomEntityJSBuilder.RenderType.SOLID;
                    break;
                case "cutout":
                    renderType = CustomEntityJSBuilder.RenderType.CUTOUT;
                    break;
                case "translucent":
                    renderType = CustomEntityJSBuilder.RenderType.TRANSLUCENT;
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
    public EntityType<?> createObject() {
        return new CustomLivingEntityTypeBuilderJS<>(this).get();
    }

    abstract public EntityType.EntityFactory<?> factory();


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
     * @param predicate              The {@link IAnimationPredicateJS script-friendly} animation predicate
     */
    public record AnimationControllerSupplier<E extends LivingEntity & IAnimatableJSCustom>(
            String name,
            int translationTicksLength,
            IAnimationPredicateJS<E> predicate,
            String triggerableAnimationName,
            String triggerableAnimationID,
            Object loopType,
            @Nullable ISoundListenerJS<E> soundListener,
            @Nullable IParticleListenerJS<E> particleListener,
            @Nullable ICustomInstructionListenerJS<E> instructionListener
    ) {
        public AnimationController<E> get(E entity) {
            final AnimationController<E> controller = new AnimationController<>(entity, name, translationTicksLength, predicate.toGecko());
            if (triggerableAnimationID != null) {
                Object type = EntityJSHelperClass.convertObjectToDesired(loopType, "looptype");
                controller.triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(triggerableAnimationName, (LoopType) type));
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
            @Param(name = "triggerableAnimationID", value = "The unique identifier of the triggerable animation(sets it apart from other triggerable animations)"),
            @Param(name = "triggerableAnimationName", value = "The name of the animation defined in the animations.json"),
            @Param(name = "loopType", value = "The loop type for the triggerable animation, either 'LOOP' or 'PLAY_ONCE' or 'HOLD_ON_LAST_FRAME' or 'DEFAULT'")
    })
    public CustomEntityJSBuilder addTriggerableAnimationController(
            String name,
            int translationTicksLength,
            String triggerableAnimationName,
            String triggerableAnimationID,
            String loopType
    ) {
        animationSuppliers.add(new AnimationControllerSupplier<>(
                name,
                translationTicksLength,
                new IAnimationPredicateJS() {
                    @Override
                    public boolean test(AnimationEventJS event) {
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
     * A wrapper around {@link com.geckolib.animation.AnimationController.AnimationStateHandler IAnimationPredicate}
     * that is easier to work with in js
     */
    @FunctionalInterface
    public interface IAnimationPredicateJS<E extends LivingEntity & IAnimatableJSCustom> {

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
     * A simple wrapper around a {@link AnimationEventJS} that restricts access to certain things
     * and adds {@link @Info} annotations for script writers
     *
     * @param <E> The entity being animated in the event
     */
    public static class AnimationEventJS<E extends LivingEntity & IAnimatableJSCustom> {
        private final List<RawAnimation.Stage> animationList = new ObjectArrayList();
        private final AnimationTest<E> parent;

        public AnimationEventJS(AnimationTest<E> parent) {
            this.parent = parent;
        }

        @Info(value = "Returns the number of ticks the entity has been animating for")
        public double getAnimationTick() {
            return parent.getAnimationTick();
        }

        @Info(value = "Returns the entity that is being animated")
        public E getEntity() {
            return parent.animatable();
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
            return parent.controller();
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
            parent.controller().triggerableAnim(triggerableAnimationID, RawAnimation.begin().then(animationName, (LoopType) type));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play defaulting to the animations.json file loop type")
        public PlayState thenPlay(String animationName) {
            parent.controller().setAnimation(RawAnimation.begin().then(animationName, LoopType.DEFAULT));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play in a loop")
        public PlayState thenLoop(String animationName) {
            parent.controller().setAnimation(RawAnimation.begin().thenLoop(animationName));
            return PlayState.CONTINUE;
        }

        @Info(value = "Wait a certain amount of ticks before starting the next animation")
        public PlayState thenWait(int ticks) {
            parent.controller().setAnimation(RawAnimation.begin().thenWait(ticks));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play and hold on the last frame")
        public PlayState thenPlayAndHold(String animationName) {
            parent.controller().setAnimation(RawAnimation.begin().then(animationName, LoopType.HOLD_ON_LAST_FRAME));
            return PlayState.CONTINUE;
        }

        @Info(value = "Sets an animation to play an x amount of times")
        public PlayState thenPlayXTimes(String animationName, int times) {
            for (int i = 0; i < times; ++i) {
                parent.controller().setAnimation(RawAnimation.begin().then(animationName, i == times - 1 ? LoopType.DEFAULT : LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }

        @Info(value = "Adds an animation to the current animation list")
        public AnimationEventJS<E> then(String animationName, LoopType loopType) {
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

    public static class KeyFrameEventJS<E extends LivingEntity & IAnimatableJSCustom, B extends KeyFrameData> {
        @Info(value = "The entity this animation is being applied to.")
        public final E entity;
        @Info(value = "The current tick of the animation.")
        public final double animationTick;
        @Info(value = "The controller handling this animation.")
        public final AnimationController<E> controller;
        @Info(value = "The keyframe data containing extra information about the instruction.")
        public final B keyframeData;

        protected KeyFrameEventJS(KeyFrameEvent<E, B> parent) {
            animationTick = parent.getAnimationTick();
            entity = parent.animatable();
            controller = parent.controller();
            keyframeData = parent.keyframeData();
        }
    }


    @FunctionalInterface
    public interface ISoundListenerJS<E extends LivingEntity & IAnimatableJSCustom> {
        void playSound(SoundKeyFrameEventJS<E> event);
    }

    public static class SoundKeyFrameEventJS<E extends LivingEntity & IAnimatableJSCustom> extends KeyFrameEventJS<E, SoundKeyframeData> {
        @Info(value = "Gets the sound id given by the Keyframe instruction from the animation. json")
        public final String sound;

        public SoundKeyFrameEventJS(KeyFrameEvent<E, SoundKeyframeData> parent) {
            super(parent);
            sound = parent.keyframeData().getSound();
        }
    }

    @FunctionalInterface
    public interface IParticleListenerJS<E extends LivingEntity & IAnimatableJSCustom> {
        void summonParticle(ParticleKeyFrameEventJS<E> event);
    }

    public static class ParticleKeyFrameEventJS<E extends LivingEntity & IAnimatableJSCustom> extends KeyFrameEventJS<E, ParticleKeyframeData> {
        @Info(value = "Gets the effect id given by the Keyframe instruction from the animation.json")
        public final String effect;
        @Info(value = "Gets the locator string given by the Keyframe instruction from the animation.json")
        public final String locator;
        @Info(value = "Gets the script string given by the Keyframe instruction from the animation.json")
        public final String script;

        public ParticleKeyFrameEventJS(KeyFrameEvent<E, ParticleKeyframeData> parent) {
            super(parent);
            effect = parent.keyframeData().getEffect();
            locator = parent.keyframeData().getLocator();
            script = parent.keyframeData().script();
        }
    }

    @FunctionalInterface
    public interface ICustomInstructionListenerJS<E extends LivingEntity & IAnimatableJSCustom> {
        void executeInstruction(CustomInstructionKeyframeEventJS<E> event);
    }

    public static class CustomInstructionKeyframeEventJS<E extends LivingEntity & IAnimatableJSCustom> extends KeyFrameEventJS<E, CustomInstructionKeyframeData> {
        @Info(value = "A list of all the custom instructions. In Blockbench, each line in the custom instruction box is a separate instruction.")
        public final String instructions;

        public CustomInstructionKeyframeEventJS(KeyFrameEvent<E, CustomInstructionKeyframeData> parent) {
            super(parent);
            this.instructions = parent.keyframeData().getInstructions();
        }
    }


    public enum RenderType {
        SOLID,
        CUTOUT,
        TRANSLUCENT
    }

}