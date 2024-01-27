package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.bindings.ItemWrapper;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {
    @FunctionalInterface
    public interface FoodChecker {
        boolean test(ItemStack stack);
    }

    public transient Object getBreedOffspring;
    public transient FoodChecker isFood;

    public AnimalEntityBuilder(ResourceLocation i) {
        super(i);
    }

    public AnimalEntityBuilder<T> getBreedOffspring(Object breedOffspring) {
        this.getBreedOffspring = breedOffspring;
        return this;
    }

    public AnimalEntityBuilder<T> isFood(Object isFood) {
        if (isFood instanceof Predicate<?>) {
            this.isFood = (FoodChecker) isFood;
        } else if (isFood instanceof String) {
            this.isFood = stack -> {
                ItemStack itemStack = Wrappers.getItemStackFromObject(isFood);
                return ItemWrapper.isItem(itemStack) && this.isFood.test(itemStack);
            };
        } else if (isFood instanceof ResourceLocation foodItemLocation) {
            this.isFood = stack -> {
                ItemStack itemStack = Wrappers.getItemStackFromObject(foodItemLocation.toString());
                return ItemWrapper.isItem(itemStack) && this.isFood.test(itemStack);
            };
        }
        return this;
    }


    public EntityType<?> getBreedOffspringType() {
        return Wrappers.entityType(getBreedOffspring);
    }

}
