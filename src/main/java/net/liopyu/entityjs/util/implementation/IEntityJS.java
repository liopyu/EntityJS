package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;

@RemapPrefixForJS("entityJs$")
public interface IEntityJS {
    boolean entityJs$isMoving();

    void entityJs$addSyncedData(EntitySerializerType type, String key, Object value);

    Object entityJs$getSyncedData(String identifier);

    void entityJs$setSyncedData(String key, Object value);
}