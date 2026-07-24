package net.liopyu.entityjs.common.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.common.util.EntitySerializerType;

@RemapPrefixForJS("entityJs$")
public interface IEntityJS {
    boolean entityJs$isMoving();

    void entityJs$addSyncedData(EntitySerializerType type, String key, Object value);

    Object entityJs$getSyncedData(String identifier);

    void entityJs$setSyncedData(String key, Object value);
}
