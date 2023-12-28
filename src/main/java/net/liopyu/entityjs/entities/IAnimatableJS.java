package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public interface IAnimatableJS extends IAnimatable {

    BaseEntityBuilder<?> getBuilder();

    default void registerControllers(AnimationData data) {

    }

    @Override
    default AnimationFactory getFactory() {
        return GeckoLibUtil.createFactory(this);
    }
}
