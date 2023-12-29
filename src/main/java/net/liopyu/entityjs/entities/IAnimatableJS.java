package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public interface IAnimatableJS extends IAnimatable {

    BaseEntityBuilder<?> getBuilder();

    default void registerControllers(AnimationData data) {
        for (BaseEntityBuilder.AnimationControllerSupplier<?> supplier : getBuilder().animationSuppliers) {
            data.addAnimationController(supplier.get(UtilsJS.cast(this)));
        }
    }

    /**
     * This cannot be implemented here, the returned value should be a cached value that is initialized in the entity's constructor. See {@link BaseEntityJS} for an example.<br><br>
     *
     * If the value is not cached, some of the values available through query in the animation json file will not 'progress'
     * @return The entity's {@link AnimationFactory}
     */
    AnimationFactory getFactory();
}
