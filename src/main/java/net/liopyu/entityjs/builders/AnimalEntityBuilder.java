package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {


    public transient Object getBreedOffspring;
    public transient Ingredient isFood;
    public transient Function<ContextUtils.EntityItemStackContext, Object> isFoodPredicate;
    public transient Function<LivingEntity, Object> canBreed;

    public transient Object myRidingOffset;
    public transient Object ambientSoundInterval;
    public transient Function<ContextUtils.EntityDistanceToPlayerContext, Object> removeWhenFarAway;

    public transient Function<ContextUtils.EntityAnimalContext, Object> canMate;
    public transient Consumer<ContextUtils.LevelAnimalContext> onSpawnChildFromBreeding;


    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        followLeashSpeed = 1.0D;
        ambientSoundInterval = 120;
        myRidingOffset = 0.14;
    }

    @Info(value = """
            Sets the resource location for the offspring spawned when breeding.
                        
            @param breedOffspring The resource location for the breed offspring.
            Can also be an instance of AgeableMob.
                        
            Example usage:
            ```javascript
            animalBuilder.getBreedOffspring("minecraft:cow");
            ```
            """)
    public AnimalEntityBuilder<T> getBreedOffspring(ResourceLocation breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the animal entity can breed.
                        
            @param canBreed A {@link Predicate} that defines the conditions for breeding.
                        
            Example usage:
            ```javascript
            animalBuilder.canBreed(entity -> {
                // Custom logic to determine if the entity can breed
                // Return true if the entity can breed, false otherwise.
            });
            ```
            """)
    public AnimalEntityBuilder<T> canBreed(Function<LivingEntity, Object> canBreed) {
        this.canBreed = canBreed;
        return this;
    }


    @Info(value = """
            Sets the ingredient representing the list of items that the animal entity can eat.
                        
            @param isFood An {@link Ingredient} specifying the items that the entity can eat.
                        
            Example usage:
            ```javascript
            animalBuilder.isFood([
            "#minecraft:apple",
            "minecraft:golden_apple",
            "minecraft:diamond"
            ]);
            ```
            """)
    public AnimalEntityBuilder<T> isFood(Ingredient isFood) {
        this.isFood = isFood;
        return this;
    }


    @Info(value = """
            Sets the predicate to determine if an entity item stack is considered as food for the animal entity.
                        
            @param isFoodPredicate A predicate accepting a {@link ContextUtils.EntityItemStackContext} parameter,
                                   defining the conditions for an entity item stack to be considered as food.
                        
            Example usage:
            ```javascript
            animalBuilder.isFoodPredicate(context -> {
                // Custom logic to determine if the entity item stack is considered as food.
                // Access information about the item stack using the provided context.
                return someCondition;
            });
            ```
            """)
    public AnimalEntityBuilder<T> isFoodPredicate(Function<ContextUtils.EntityItemStackContext, Object> isFoodPredicate) {
        this.isFoodPredicate = isFoodPredicate;
        return this;
    }


    @Info(value = """
            Sets the offset for riding on the animal entity.
                        
            @param myRidingOffset The offset value for riding on the animal.
            Defaults to 0.0.
                        
            Example usage:
            ```javascript
            animalBuilder.myRidingOffset(1.5);
            ```
            """)
    public AnimalEntityBuilder<T> myRidingOffset(double myRidingOffset) {
        this.myRidingOffset = myRidingOffset;
        return this;
    }


    @Info(value = """
            Sets the interval in ticks between ambient sounds for the animal entity.
                        
            @param ambientSoundInterval The interval in ticks between ambient sounds.
            Defaults to 240.
                        
            Example usage:
            ```javascript
            animalBuilder.ambientSoundInterval(100);
            ```
            """)
    public AnimalEntityBuilder<T> ambientSoundInterval(int ambientSoundInterval) {
        Object obj = ambientSoundInterval;
        if (obj instanceof Integer) {
            this.ambientSoundInterval = (int) obj;
        } else {
            ConsoleJS.STARTUP.error("Invalid value for ambientSoundInterval: " + obj + "must be an Integer");
            this.ambientSoundInterval = 240;
        }
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the entity should be removed when far away from the player.
                        
            @param removeWhenFarAway A Predicate accepting a ContextUtils.EntityDistanceToPlayerContext parameter,
                                     defining the condition for the entity to be removed when far away.
                        
            Example usage:
            ```javascript
            animalBuilder.removeWhenFarAway(context -> {
                // Custom logic to determine if the entity should be removed when far away
                // Return true if the entity should be removed based on the provided context.
            });
            ```
            """)
    public AnimalEntityBuilder<T> removeWhenFarAway(Function<ContextUtils.EntityDistanceToPlayerContext, Object> removeWhenFarAway) {
        this.removeWhenFarAway = removeWhenFarAway;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the entity can mate.
                        
            @param predicate A Predicate accepting a ContextUtils.EntityAnimalContext parameter,
                             defining the condition for the entity to be able to mate.
                        
            Example usage:
            ```javascript
            animalBuilder.canMate(context -> {
                // Custom logic to determine if the entity can mate
                // Return true if mating is allowed based on the provided context.
            });
            ```
            """)
    public AnimalEntityBuilder<T> canMate(Function<ContextUtils.EntityAnimalContext, Object> predicate) {
        this.canMate = predicate;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a child is spawned from breeding.
                        
            @param consumer A Consumer accepting a ContextUtils.LevelAnimalContext parameter,
                             defining the behavior to be executed when a child is spawned from breeding.
                        
            Example usage:
            ```javascript
            animalBuilder.onSpawnChildFromBreeding(context -> {
                // Custom logic to handle the spawning of a child from breeding
                // Access information about the breeding event using the provided context.
            });
            ```
            """)
    public AnimalEntityBuilder<T> onSpawnChildFromBreeding(Consumer<ContextUtils.LevelAnimalContext> consumer) {
        this.onSpawnChildFromBreeding = consumer;
        return this;
    }


}
