package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.liolib.core.animatable.GeoAnimatable;
import net.liopyu.liolib.core.animation.AnimatableManager;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;

/**
 * This provides a default implementation of {@link GeoAnimatable#registerControllers(AnimatableManager.ControllerRegistrar)}}
 * which delegates to the entity builder. Unfortunately, {@link #getAnimatableInstanceCache()} cannot have a default
 * implementation due to the {@link AnimatableInstanceCache} returned needing to be cached in the entity.<br><br>
 * <p>
 * See the comments on the methods here and in {@link AnimalEntityJS} for further reading.
 */
public interface IAnimatableJS extends GeoAnimatable {

    /**
     * Used to retrieve the builder this entity(type) was built with. Used to retrieve information
     * about animations and anything the builder defines
     */
    BaseLivingEntityBuilder<?> getBuilder();


    /**
     * Note for implementors: by default this casts {@code this} to {@code <E extends LivingEntity & IAnimatableJS>},
     * if your implementation is not a subclass of {@link net.minecraft.world.entity.LivingEntity} this will cause
     * problems and this will necessitate overriding this method with custom implementations
     */
    default void registerControllers(AnimatableManager.ControllerRegistrar data) {
        for (BaseLivingEntityBuilder.AnimationControllerSupplier<?> supplier : getBuilder().animationSuppliers) {
            data.add(supplier.get(UtilsJS.cast(this)));
        }
    }

    /**
     * This cannot be implemented here, the returned value should be a cached value that is initialized in the entity's constructor. See {@link BaseEntityJS} for an example.<br><br>
     * <p>
     * If the value is not cached, some of the values available through query in the animation json file will not 'progress'
     *
     * @return The entity's {@link AnimatableInstanceCache}
     */
    AnimatableInstanceCache getAnimatableInstanceCache();

    default double getTick(Object entity) {
        return (double) ((Entity) entity).tickCount;
    }

    /**
     * Gets the id of the entity's entity type
     */
    default String getTypeId() {
        return Registry.ENTITY_TYPE.getKey(getType()).toString();
    }

    EntityType<?> getType();
}
