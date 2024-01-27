package net.liopyu.entityjs.builders;

import com.ibm.icu.impl.Pair;
import dev.latvian.mods.kubejs.typings.Info;
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

    public transient BiPredicate<Animal, Animal> canMate;
    public transient BiConsumer<ServerLevel, Animal> spawnChildFromBreeding;

    @FunctionalInterface
    public interface QuinaryFunction<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    public transient QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> finalizeSpawn;

    public transient Consumer<PlayerEntityContext> tickLeash;
    public transient Supplier<Boolean> shouldStayCloseToLeashHolder;
    public transient Supplier<Double> followLeashSpeed;
    public transient BiConsumer<BlockPathTypes, Float> setPathfindingMalus;
    public transient Function<BlockPathTypes, Boolean> canCutCorner;

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

    @Info(value = """
            Sets the breedOffspring property in the builder.\n\n" +
            "Defaults to <namespace>:breeding/<path>.json
            """)
    public AnimalEntityBuilder<T> getBreedOffspring(Object breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }

    @Info(value = """
            Sets the canBreed property in the builder.\n\n" +
            "Defaults to true.
            """)
    public AnimalEntityBuilder<T> canBreed(boolean canBreed) {
        this.canBreed = canBreed;
        return this;
    }

    @Info(value = """
            Sets the isFood property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> isFood(Object isFood) {
        this.isFood = isFood;
        return this;
    }

    @Info(value = """
            Sets the walkTargetValue property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> walkTargetValue(BiFunction<BlockPos, LevelReader, Float> function) {
        this.walkTargetValue = function;
        return this;
    }

    @Info(value = """
            Sets the myRidingOffset property in the builder.\n\n" +
            "Defaults to 0.0.
            """)
    public AnimalEntityBuilder<T> myRidingOffset(Function<T, Double> myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }

    @Info(value = """
            Sets the ambientSoundInterval property in the builder.\n\n" +
            "Defaults to 240.
            """)
    public AnimalEntityBuilder<T> ambientSoundInterval(Function<T, Integer> ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }

    @Info(value = """
            Sets the removeWhenFarAway property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> removeWhenFarAway(BiPredicate<T, Double> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    @Info(value = """
            Sets the canMate property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canMate(BiPredicate<Animal, Animal> predicate) {
        this.canMate = predicate;
        return this;
    }


    @Info(value = """
            Sets the spawnChildFromBreeding property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> spawnChildFromBreeding(BiConsumer<ServerLevel, Animal> consumer) {
        this.spawnChildFromBreeding = consumer;
        return this;
    }

    @Info(value = """
            Sets the finalizeSpawn property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> finalizeSpawn(QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> function) {
        this.finalizeSpawn = function;
        return this;
    }

    @Info(value = """
            Sets the tickLeash property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> tickLeash(Consumer<PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }

    @Info(value = """
            Sets the shouldStayCloseToLeashHolder property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> shouldStayCloseToLeashHolder(Supplier<Boolean> supplier) {
        this.shouldStayCloseToLeashHolder = supplier;
        return this;
    }

    @Info(value = """
            Sets the followLeashSpeed property in the builder.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> followLeashSpeed(Supplier<Double> supplier) {
        this.followLeashSpeed = supplier;
        return this;
    }


    @Info(value = """
            Sets the pathfinding malus for a specific node type.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> setPathfindingMalus(BiConsumer<BlockPathTypes, Float> setPathfindingMalus) {
        this.setPathfindingMalus = setPathfindingMalus;
        return this;
    }

    @Info(value = """
            Determines if the entity can cut corners for a specific path type.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canCutCorner(Function<BlockPathTypes, Boolean> canCutCorner) {
        this.canCutCorner = canCutCorner;
        return this;
    }


    @Info(value = """
            Sets the target for the entity.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> setTarget(Consumer<LivingEntity> setTarget) {
        this.setTarget = setTarget;
        return this;
    }

    @Info(value = """
            Determines if the entity can fire a projectile weapon.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canFireProjectileWeapon(Predicate<ProjectileWeaponItem> canFireProjectileWeapon) {
        this.canFireProjectileWeapon = canFireProjectileWeapon;
        return this;
    }

    @Info(value = """
            Custom behavior when the entity eats.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> ate(Consumer<LivingEntity> ate) {
        this.ate = ate;
        return this;
    }


    @Info(value = """
            Sets the ambient sound for the entity.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> getAmbientSound(Consumer<Object> getAmbientSound) {
        this.getAmbientSound = getAmbientSound;
        return this;
    }


    @Info(value = """
            Sets the condition for whether the entity can hold specific items.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canHoldItem(List<Object> items) {
        this.canHoldItem = items;
        return this;
    }


    @Info(value = """
            Sets whether the entity should despawn in peaceful mode.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> shouldDespawnInPeaceful(Boolean shouldDespawnInPeaceful) {
        this.shouldDespawnInPeaceful = shouldDespawnInPeaceful;
        return this;
    }


    @Info(value = """
            Sets whether the entity can pick up loot.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> canPickUpLoot(Boolean canPickUpLoot) {
        this.canPickUpLoot = canPickUpLoot;
        return this;
    }

    @Info(value = """
            Sets whether the entity's persistence is required.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> isPersistenceRequired(Boolean isPersistenceRequired) {
        this.isPersistenceRequired = isPersistenceRequired;
        return this;
    }

    @Info(value = """
            Sets the behavior when offspring is spawned from an egg.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> onOffspringSpawnedFromEgg(Consumer<PlayerEntityContext> onOffspringSpawnedFromEgg) {
        this.onOffspringSpawnedFromEgg = onOffspringSpawnedFromEgg;
        return this;
    }


    @Info(value = """
            Sets the square of the melee attack range for the entity.\n\n" +
            "Defaults to null.
            """)
    public AnimalEntityBuilder<T> meleeAttackRangeSqr(Double meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }


}
