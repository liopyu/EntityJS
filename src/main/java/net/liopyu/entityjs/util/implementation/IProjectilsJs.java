package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.util.EntitySerializerType;

@RemapPrefixForJS("entityJs$")
public interface IProjectilsJs {
    void entityJs$addSyncedData(EntitySerializerType type, String key, Object value);

    <T> T entityJs$getSyncedData(String identifier);

    void entityJs$addSyncedData(String identifier, Object value);

    void entityJs$setSyncedData(String key, Object value);
}
