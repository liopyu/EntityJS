package net.liopyu.entityjs.util;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;


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

}