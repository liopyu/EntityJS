package net.liopyu.entityjs.entities;

import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;

public interface IAnimatableJS extends IAnimatable {

    void registerControllers(AnimationData data);

}
