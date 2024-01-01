package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.entities.BaseEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.liopyu.entityjs.util.ai.brain.BrainProviderBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    public transient boolean onSoulSpeedBlock;
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
    public transient BrainProviderBuilder brainProviderBuilder;
    public transient HumanoidArm mainArm;
    public transient boolean hasInventory;
    public transient Consumer<BrainBuilder> brainBuilder;

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
        onSoulSpeedBlock = false;
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

    public BaseEntityBuilder<T> onSoulSpeedBlock(boolean b) {
        onSoulSpeedBlock = b;
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
