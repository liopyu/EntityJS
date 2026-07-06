package net.liopyu.entityjs.util;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;


public enum EntitySerializerType {
    BYTE,
    INT,
    LONG,
    FLOAT,
    STRING,
    BOOLEAN,
    COMPOUND_TAG,
    UUID,
    VECTOR3,
    QUATERNION;

    public static Optional<EntitySerializerType> byOrdinal(int ordinal) {
        EntitySerializerType[] types = values();
        if (ordinal < 0 || ordinal >= types.length) {
            return Optional.empty();
        }
        return Optional.of(types[ordinal]);
    }

}
