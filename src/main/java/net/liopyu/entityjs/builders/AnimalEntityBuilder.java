package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {
    public transient Object getBreedOffspring;
    public transient Ingredient isFood;
    public transient Function<ContextUtils.EntityItemStackContext, Object> isFoodPredicate;
    public transient Function<LivingEntity, Object> canBreed;


    public transient Function<ContextUtils.EntityAnimalContext, Object> canMate;
    public transient Consumer<ContextUtils.LevelAnimalContext> onSpawnChildFromBreeding;

    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        followLeashSpeed = 1.0D;

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
    public AnimalEntityBuilder<T> getBreedOffspring(Object breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the animal entity can breed.
                        
            @param canBreed A {@link Function} that defines the conditions for breeding.
                        
            Example usage:
            ```javascript
            animalBuilder.canBreed(entity => {
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
            animalBuilder.isFoodPredicate(context => {
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
            Sets a predicate to determine if the entity can mate.
                        
            @param predicate A Function accepting a ContextUtils.EntityAnimalContext parameter,
                             defining the condition for the entity to be able to mate.
                        
            Example usage:
            ```javascript
            animalBuilder.canMate(context => {
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
            animalBuilder.onSpawnChildFromBreeding(context => {
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
