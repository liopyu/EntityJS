package net.liopyu.entityjs.builders;

import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.Wrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;

public abstract class AnimalEntityBuilder<T extends Animal & IAnimatableJS> extends MobBuilder<T> {


    public transient Object getBreedOffspring;
    public transient Object isFood;
    public transient boolean canBreed;

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


    public EntityType<?> getBreedOffspringType() {
        return Wrappers.entityType(getBreedOffspring);
    }

}
