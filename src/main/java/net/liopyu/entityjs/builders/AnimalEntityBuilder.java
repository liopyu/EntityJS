package net.liopyu.entityjs.builders;

import com.ibm.icu.impl.Pair;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.PlayerEntityContext;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import java.util.List;
import java.util.function.*;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {


    public transient Object getBreedOffspring;
    public transient Object isFood;
    public transient boolean canBreed;
    public transient BiFunction<BlockPos, LevelReader, Float> walkTargetValue;

    public transient Function<T, Double> myRidingOffset;
    public transient Function<T, Integer> ambientSoundInterval;
    public transient BiPredicate<T, Double> removeWhenFarAway;
    // Transients for AnimalEntityBuilder
    public transient BiPredicate<Animal, Animal> canMate;
    public transient BiConsumer<ServerLevel, Animal> spawnChildFromBreeding;

    @FunctionalInterface
    public interface QuinaryFunction<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    public transient QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> finalizeSpawn;
    // Transients for AnimalEntityBuilder
    public transient Consumer<PlayerEntityContext> tickLeash;
    public transient Supplier<Boolean> shouldStayCloseToLeashHolder;
    public transient Supplier<Double> followLeashSpeed;
    public transient BiConsumer<BlockPathTypes, Float> setPathfindingMalus;
    public transient Function<BlockPathTypes, Boolean> canCutCorner;
    public transient Supplier<BodyRotationControl> createBodyControl;

    public transient Consumer<LivingEntity> setTarget;
    public transient Predicate<ProjectileWeaponItem> canFireProjectileWeapon;
    public transient Consumer<LivingEntity> ate;
    public transient Consumer<Object> getAmbientSound;
    public transient List<Object> canHoldItem;
    public transient Boolean shouldDespawnInPeaceful;
    public transient Boolean canPickUpLoot;
    public transient Boolean isPersistenceRequired;
    public transient Consumer<PlayerEntityContext> onOffspringSpawnedFromEgg;

    public transient Double meleeAttackRangeSqr;


    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canBreed = true;
    }

    public AnimalEntityBuilder<T> getBreedOffspring(Object breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }

    public AnimalEntityBuilder<T> canBreed(boolean canBreed) {
        this.canBreed = canBreed;
        return this;
    }

    public AnimalEntityBuilder<T> isFood(Object isFood) {
        this.isFood = isFood;
        return this;
    }

    public AnimalEntityBuilder<T> walkTargetValue(BiFunction<BlockPos, LevelReader, Float> function) {
        this.walkTargetValue = function;
        return this;
    }

    public AnimalEntityBuilder<T> myRidingOffset(Function<T, Double> myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }

    public AnimalEntityBuilder<T> ambientSoundInterval(Function<T, Integer> ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }

    public AnimalEntityBuilder<T> removeWhenFarAway(BiPredicate<T, Double> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    public AnimalEntityBuilder<T> canMate(BiPredicate<Animal, Animal> predicate) {
        this.canMate = predicate;
        return this;
    }

    public AnimalEntityBuilder<T> spawnChildFromBreeding(BiConsumer<ServerLevel, Animal> consumer) {
        this.spawnChildFromBreeding = consumer;
        return this;
    }

    public AnimalEntityBuilder<T> finalizeSpawn(QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> function) {
        this.finalizeSpawn = function;
        return this;
    }

    // AnimalEntityBuilder methods
    public AnimalEntityBuilder<T> tickLeash(Consumer<PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }

    public AnimalEntityBuilder<T> shouldStayCloseToLeashHolder(Supplier<Boolean> supplier) {
        this.shouldStayCloseToLeashHolder = supplier;
        return this;
    }

    public AnimalEntityBuilder<T> followLeashSpeed(Supplier<Double> supplier) {
        this.followLeashSpeed = supplier;
        return this;
    }


    /**
     * Sets the pathfinding malus for a specific node type.
     *
     * @param setPathfindingMalus BiConsumer accepting pNodeType and pMalus.
     */
    public AnimalEntityBuilder<T> setPathfindingMalus(BiConsumer<BlockPathTypes, Float> setPathfindingMalus) {
        this.setPathfindingMalus = setPathfindingMalus;
        return this;
    }

    /**
     * Determines if the entity can cut corners for a specific path type.
     *
     * @param canCutCorner Function accepting pPathType and returning a boolean.
     */
    public AnimalEntityBuilder<T> canCutCorner(Function<BlockPathTypes, Boolean> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }

    /**
     * Creates a custom BodyRotationControl.
     *
     * @param createBodyControl Supplier returning a custom BodyRotationControl.
     */
    public AnimalEntityBuilder<T> createBodyControl(Supplier<BodyRotationControl> createBodyControl) {
        this.createBodyControl = createBodyControl;
        return this;
    }
// AnimalEntityBuilder methods

    /**
     * Sets the target for the entity.
     *
     * @param setTarget Consumer accepting a LivingEntity as the target.
     */
    public AnimalEntityBuilder<T> setTarget(Consumer<LivingEntity> setTarget) {
        this.setTarget = setTarget;
        return this;
    }

    /**
     * Determines if the entity can fire a projectile weapon.
     *
     * @param canFireProjectileWeapon Predicate accepting a ProjectileWeaponItem and returning a boolean.
     */
    public AnimalEntityBuilder<T> canFireProjectileWeapon(Predicate<ProjectileWeaponItem> canFireProjectileWeapon) {
        this.canFireProjectileWeapon = canFireProjectileWeapon;
        return this;
    }

    /**
     * Custom behavior when the entity eats.
     *
     * @param ate Runnable representing the custom eating behavior.
     */
    public AnimalEntityBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }


    /**
     * Sets the ambient sound for the entity.
     *
     * @param getAmbientSound Supplier providing the ambient sound.
     */
    public AnimalEntityBuilder<T> getAmbientSound(Consumer<Object> getAmbientSound) {
        this.getAmbientSound = getAmbientSound;
        return this;
    }


    /**
     * Sets the condition for whether the entity can hold specific items.
     *
     * @param items List of ItemStacks or ResourceLocations representing the items the entity can hold.
     */
    public AnimalEntityBuilder<T> canHoldItem(List<Object> items) {
        this.canHoldItem = items;
        return this;
    }


    /**
     * Sets whether the entity should despawn in peaceful mode.
     *
     * @param shouldDespawnInPeaceful Boolean indicating whether the entity should despawn in peaceful mode.
     */
    public AnimalEntityBuilder<T> shouldDespawnInPeaceful(Boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }
// AnimalEntityBuilder methods

    /**
     * Sets whether the entity can pick up loot.
     *
     * @param canPickUpLoot Boolean indicating whether the entity can pick up loot.
     */
    public AnimalEntityBuilder<T> canPickUpLoot(Boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    /**
     * Sets whether the entity's persistence is required.
     *
     * @param isPersistenceRequired Boolean indicating whether the entity's persistence is required.
     */
    public AnimalEntityBuilder<T> isPersistenceRequired(Boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

    /**
     * Sets the behavior when offspring is spawned from an egg.
     *
     * @param onOffspringSpawnedFromEgg Consumer accepting a Pair of Player and Mob when offspring is spawned.
     */
    public AnimalEntityBuilder<T> onOffspringSpawnedFromEgg(Consumer<PlayerEntityContext> onOffspringSpawnedFromEgg) {
        this.onOffspringSpawnedFromEgg = onOffspringSpawnedFromEgg;
        return this;
    }
// AnimalEntityBuilder methods

    /**
     * Sets the square of the melee attack range for the entity.
     *
     * @param meleeAttackRangeSqr Double representing the square of the melee attack range.
     */
    public AnimalEntityBuilder<T> meleeAttackRangeSqr(Double meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }


}
