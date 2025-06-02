package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.entities.living.entityjs.WrappedAnimatableEntity;
import net.liopyu.entityjs.util.EntitySerializerType;

@RemapPrefixForJS("entityJs$")
public interface ILivingEntityJS {
    WrappedAnimatableEntity entityJs$getAnimatableEntity();

    void entityJs$triggerAnimation(String controllerName, String animName);
    //void entityJs$addSyncedData(EntitySerializerType type, String key, Object value);

   /* <T> T entityJs$getSyncedData(String identifier);

    void entityJs$addSyncedData(String identifier, Object value);

    void entityJs$setSyncedData(String key, Object value);*/
}