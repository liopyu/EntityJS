package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;

@RemapPrefixForJS("entityJs$")
public interface ILivingEntityJS {
    WrappedAnimatableEntity entityJs$getAnimatableEntity();
}

