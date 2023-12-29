package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
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
        canBeCollidedWith = false;
        isAttackable = true;
        attributes = builder -> {
        };
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

    public Predicate<Entity> passengerPredicate;

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

    public BaseEntityBuilder<T> getDeathSound(SoundEvent sound) {
        getDeathSound = sound;
        return this;
    }

    public BaseEntityBuilder<T> getSwimSound(SoundEvent sound) {
        getSwimSound = sound;
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

    public BaseEntityBuilder<T> getWaterSlowDown(int i) {
        getWaterSlowDown = i;
        return this;
    }

    public BaseEntityBuilder<T> getSoundVolume(int i) {
        getSoundVolume = i;
        return this;
    }

    public BaseEntityBuilder<T> getBlockSpeedFactor(int i) {
        getBlockSpeedFactor = i;
        return this;
    }

    public BaseEntityBuilder<T> getJumpPower(int i) {
        getJumpPower = i;
        return this;
    }

    public BaseEntityBuilder<T> getBlockJumpFactor(int i) {
        getBlockJumpFactor = i;
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

    public BaseEntityBuilder<T> shouldDropLoot(boolean b) {
        shouldDropLoot = b;
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

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilderJS<>(this).get();
    }

    abstract public EntityType.EntityFactory<T> factory();

    /**
     * @return
     */
    abstract public AttributeSupplier.Builder getAttributeBuilder();


}
