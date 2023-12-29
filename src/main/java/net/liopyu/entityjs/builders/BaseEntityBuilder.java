package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.entities.BaseEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class BaseEntityBuilder<T extends LivingEntity & IAnimatableJS> extends BuilderBase<EntityType<T>> {

    public static final List<BaseEntityBuilder<?>> thisList = new ArrayList<>();

    public transient float width;
    public transient float height;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient Block[] immuneTo;
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
    public transient final Consumer<AttributeSupplier.Builder> attributes;
    public transient final List<AnimationControllerSupplier<T>> animationSuppliers;
    public transient boolean shouldDropLoot;
    public transient boolean setCanAddPassenger;
    public transient boolean canRide;
    public transient boolean isAffectedByFluids;
    public transient boolean isAlwaysExperienceDropper;
    public transient boolean isImmobile;
    public transient boolean onSoulSpeedBlock;
    public transient float getBlockJumpFactor;
    public transient float getBlockSpeedFactor;
    public transient float getJumpPower;
    public transient float getSoundVolume;
    public transient float getWaterSlowDown;
    public transient SoundEvent getDeathSound;
    public transient SoundEvent getSwimSound;

    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        fireImmune = false;
        immuneTo = new Block[0];
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        modelResource = t -> newID("geo/", ".geo.json");
        textureResource = t -> newID("textures/model/entity/", ".png");
        animationResource = t -> newID("animations/", ".animation.json");
        canBePushed = false;
        canBeCollidedWith= false;
        isAttackable = true;
        attributes = builder -> {};
        animationSuppliers = new ArrayList<>();
        shouldDropLoot = true;
        setCanAddPassenger(entity -> true);
        canRide(entity -> true);
        isAffectedByFluids = false;
        isAlwaysExperienceDropper = false;
        isImmobile = false;
        onSoulSpeedBlock = false;
        getBlockJumpFactor = 0.5f;
        getBlockSpeedFactor = 0.5f;
        getJumpPower = 0.5f;
        getSoundVolume = 1.0f;
        getWaterSlowDown = 0.0f;
        getDeathSound = SoundEvents.BUCKET_EMPTY;
        getSwimSound = SoundEvents.MOOSHROOM_SHEAR;
    }

    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }



    public BaseEntityBuilder<T> saves(boolean b) {
        save = b;
        return this;
    }

    public BaseEntityBuilder<T> fireImmune(boolean b) {
        fireImmune = b;
        return this;
    }

    // TODO: Defer block getting to builder
    public BaseEntityBuilder<T> immuneTo(ResourceLocation... blocks) {
        List<Block> immuneTo = new ArrayList<>();
        for (ResourceLocation block : blocks) {
            if (ForgeRegistries.BLOCKS.containsKey(block)) {
                immuneTo.add(ForgeRegistries.BLOCKS.getValue(block));
            }
        }
        this.immuneTo = immuneTo.toArray(this.immuneTo);
        return this;
    }

    public BaseEntityBuilder<T> canSpawnFarFromPlayer(boolean b) {
        spawnFarFromPlayer = b;
        return this;
    }

    public BaseEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    public BaseEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    public BaseEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }

    public BaseEntityBuilder<T> modelResourceFunction(Function<T, ResourceLocation> function) {
        modelResource = function;
        return this;
    }

    public BaseEntityBuilder<T> textureResourceFunction(Function<T, ResourceLocation> function) {
        textureResource = function;
        return this;
    }

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

    public BaseEntityBuilder<T> addAttribute(Attribute attribute) {
        attributes.andThen(builder -> builder.add(attribute));
        return this;
    }

    public BaseEntityBuilder<T> addAttribute(Attribute attribute, double amount) {
        attributes.andThen(builder -> builder.add(attribute, amount));
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

    public BaseEntityBuilder<T> calaculateFallDamage(BiFunction<Float, Float, Integer> calculation) {
        fallDamageFunction = calculation;
        return this;
    }

    // TODO: Make this accept sound resource locations
    public BaseEntityBuilder<T> getDeathSound(SoundEvent sound) {
        getDeathSound = sound;
        return this;
    }

    public BaseEntityBuilder<T> getSwimSound(SoundEvent sound) {
        getSwimSound = sound;
        return this;
    }
    @Info(value = "Adds a new AnimationController to the entity", params = {
            @Param(name = "name", value = "The name of the controller"),
            @Param(name = "translationTicksLength", value = "How many ticks it takes to transition between different animations"),
            @Param(name = "predicate", value = "")
    })
    public BaseEntityBuilder<T> addAnimationController(String name, int translationTicksLength, IAnimationPredicateJS<T> predicate) {
        animationSuppliers.add(new AnimationControllerSupplier<>(name, translationTicksLength, predicate));
        return this;
    }

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilderJS<>(this).get();
    }

    @HideFromJS
    abstract public EntityType.EntityFactory<T> factory();

    /**
     * Used to retrieve the entity type's attributes. Implementors are encouraged to first
     * create a {@link AttributeSupplier.Builder} from a static method in the base class
     * (i.e. {@link BaseEntityJS#createLivingAttributes()}) and then apply the {@code attributes}
     * consumer to that before returning it
     * @return The {@link AttributeSupplier.Builder} that will be built during Forge's EntityAttributeCreationEvent
     */
    @HideFromJS
    abstract public AttributeSupplier.Builder getAttributeBuilder();

    public record AnimationControllerSupplier<E extends LivingEntity & IAnimatableJS>(String name, int translationTicksLength, IAnimationPredicateJS<E> predicate) {
        public AnimationController<E> get(E entity) {
            return new AnimationController<>(entity, name, translationTicksLength, predicate.toGecko());
        }
    }

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
}
