package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.PlayerEntityContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.*;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {


    public transient BiFunction<ServerLevel, AgeableMob, @Nullable AgeableMob> getBreedOffspring;
    public transient Predicate<ItemStack> isFood;
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

    public transient Double meleeAttackRangeSqr;
    public transient Consumer<LivingEntity> aiStep;

    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canBreed = true;
    }

    @Info(value = """
            Sets the offspring resultant from breeding
            
            Defaults to null
            """)
    public AnimalEntityBuilder<T> getBreedOffspring(BiFunction<ServerLevel, AgeableMob, @Nullable AgeableMob> breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }

    @Info(value = """
            Sets the canBreed property in the builder.
            
            Defaults to true.
            """)
    public AnimalEntityBuilder<T> canBreed(boolean canBreed) {
        this.canBreed = canBreed;
        return this;
    }

    @Info(value = """
            Sets the isFood property in the builder.
            
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> isFood(Predicate<ItemStack> isFood) {
        this.isFood = isFood;
        return this;
    }

    @Info(value = """
            Sets the aiStep property in the builder.
            
            Defaults to super-AgeableMob.
            """)
    public AnimalEntityBuilder<T> aiStep(Consumer<LivingEntity> aiStep) {
        this.aiStep = aiStep;
        return this;
    }

    @Info(value = """
            Sets the walkTargetValue property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> walkTargetValue(BiFunction<BlockPos, LevelReader, Float> function) {
        this.walkTargetValue = function;
        return this;
    }

    @Info(value = """
            Sets the myRidingOffset property in the builder.
                        
            Defaults to `0.0`.
            """)
    public AnimalEntityBuilder<T> myRidingOffset(Function<T, Double> myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }

    @Info(value = """
            Sets the ambientSoundInterval property in the builder.
                        
            Defaults to 240.
            """)
    public AnimalEntityBuilder<T> ambientSoundInterval(Function<T, Integer> ambientSoundInterval) {
        this.ambientSoundInterval = ambientSoundInterval;
        return this;
    }

    @Info(value = """
            Sets the removeWhenFarAway property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> removeWhenFarAway(BiPredicate<T, Double> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }

    @Info(value = """
            Sets the canMate property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> canMate(BiPredicate<Animal, Animal> predicate) {
        this.canMate = predicate;
        return this;
    }


    @Info(value = """
            Sets the spawnChildFromBreeding property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> spawnChildFromBreeding(BiConsumer<ServerLevel, Animal> consumer) {
        this.spawnChildFromBreeding = consumer;
        return this;
    }

    @Info(value = """
            Sets the finalizeSpawn property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> finalizeSpawn(QuinaryFunction<ServerLevelAccessor, DifficultyInstance, MobSpawnType, SpawnGroupData, CompoundTag, SpawnGroupData> function) {
        this.finalizeSpawn = function;
        return this;
    }

    @Info(value = """
            Sets the tickLeash property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> tickLeash(Consumer<PlayerEntityContext> consumer) {
        this.tickLeash = consumer;
        return this;
    }

    @Info(value = """
            Sets the shouldStayCloseToLeashHolder property in the builder.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> shouldStayCloseToLeashHolder(Supplier<Boolean> supplier) {
        this.shouldStayCloseToLeashHolder = supplier;
        return this;
    }

    @Info(value = """
            Sets the followLeashSpeed property in the builder.
            
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> followLeashSpeed(Supplier<Double> supplier) {
        this.followLeashSpeed = supplier;
        return this;
    }


    @Info(value = """
            Sets the square of the melee attack range for the entity.
                        
            Defaults to null.
            """)
    public AnimalEntityBuilder<T> meleeAttackRangeSqr(Double meleeAttackRangeSqr) {
        this.meleeAttackRangeSqr = meleeAttackRangeSqr;
        return this;
    }


}
