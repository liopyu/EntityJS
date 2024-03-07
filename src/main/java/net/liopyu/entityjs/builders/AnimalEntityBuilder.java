package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.AnimalEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.entities.PartEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {
    public transient Function<ContextUtils.BreedableEntityContext, Object> setBreedOffspring;
    public transient Ingredient isFood;
    public transient Function<ContextUtils.EntityItemStackContext, Object> isFoodPredicate;
    public transient Function<LivingEntity, Object> canBreed;


    public transient Function<ContextUtils.EntityAnimalContext, Object> canMate;
    public transient Consumer<ContextUtils.LevelAnimalContext> onSpawnChildFromBreeding;
    public final List<PartEntityParams> partEntityParamsList = new ArrayList<>();

    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        followLeashSpeed = 1.0D;
    }

    public AnimalEntityBuilder<T> addPartEntity(String name, float width, float height) {
        partEntityParamsList.add(new PartEntityParams(name, width, height));
        return this;
    }

    public static class PartEntityParams {
        public final String name;
        public final float width;
        public final float height;

        public PartEntityParams(String name, float width, float height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }
    }

    @Info(value = """
            Sets the offspring for the Animal Entity.
                        
            @param breedOffspring Function returning a resource location for the breed offspring.
                        
            Example usage:
            ```javascript
            animalBuilder.setBreedOffspring(context => {
                const { entity, mate, level } = context
                // Use the context to return a ResourceLocation of an entity to spawn when the entity mates
                return 'minecraft:cow' //Some Resource location representing the entity to spawn.
            })
            ```
            """)
    public AnimalEntityBuilder<T> setBreedOffspring(Function<ContextUtils.BreedableEntityContext, Object> breedOffspring) {
        this.setBreedOffspring = breedOffspring;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if the animal entity can breed.
                        
            @param canBreed A Function that defines the conditions for breeding.
                        
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
                return true // Some Boolean value;
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
