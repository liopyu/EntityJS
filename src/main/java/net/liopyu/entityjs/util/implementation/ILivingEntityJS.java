package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.nbt.Tag;

@RemapPrefixForJS("entityJs$")
public interface ILivingEntityJS {
    WrappedAnimatableEntity entityJs$getAnimatableEntity();

    void entityJs$triggerAnimation(String controllerName, String animName);

}