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

    public EntityDataSerializer<?> getSerializer() {
        return switch (this) {
            case BYTE -> EntityDataSerializers.BYTE;
            case INT -> EntityDataSerializers.INT;
            case LONG -> EntityDataSerializers.LONG;
            case FLOAT -> EntityDataSerializers.FLOAT;
            case STRING -> EntityDataSerializers.STRING;
            case BOOLEAN -> EntityDataSerializers.BOOLEAN;
            case COMPOUND_TAG -> EntityDataSerializers.COMPOUND_TAG;
            case VECTOR3 -> EntityDataSerializers.VECTOR3;
            case QUATERNION -> EntityDataSerializers.QUATERNION;
            case UUID -> EntityDataSerializers.OPTIONAL_UUID;
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> T castValue(Object value, @Nullable String castHint) {
        if (value instanceof Number num) {
            if (castHint != null) {
                switch (castHint.toLowerCase()) {
                    case "byte":
                        return (T) Byte.valueOf(num.byteValue());
                    case "int":
                        return (T) Integer.valueOf(num.intValue());
                    case "float":
                        return (T) Float.valueOf(num.floatValue());
                    case "long":
                        return (T) Long.valueOf(num.longValue());
                }
            }

            double d = num.doubleValue();
            if (num instanceof Float) return (T) Float.valueOf(num.floatValue());
            if (num instanceof Long || d % 1 == 0 && d > Integer.MAX_VALUE && d <= Long.MAX_VALUE)
                return (T) Long.valueOf(num.longValue());
            if (num instanceof Integer || d % 1 == 0 && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE)
                return (T) Integer.valueOf(num.intValue());
            return (T) Float.valueOf(num.floatValue());
        }
        if (value instanceof java.util.UUID) return (T) value;
        if (value instanceof String) return (T) value;
        if (value instanceof Boolean) return (T) value;
        if (value instanceof net.minecraft.nbt.CompoundTag) return (T) value;
        if (value instanceof org.joml.Vector3f) return (T) value;
        if (value instanceof org.joml.Vector3d vec3)
            return (T) new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
        if (value instanceof org.joml.Vector3i vec3)
            return (T) new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
        if (value instanceof Vec3 vec3) return (T) new Vector3f((float) vec3.x, (float) vec3.y, (float) vec3.z);
        if (value instanceof org.joml.Quaternionf) return (T) value;

        throw new IllegalArgumentException("Unsupported value type for casting: " + value.getClass());
    }


    public static EntitySerializerType fromObject(Object value) {
        if (value instanceof Byte) return BYTE;
        if (value instanceof Number num) {
            double d = num.doubleValue();
            if (num instanceof Float) return FLOAT;
            if (num instanceof Long || d % 1 == 0 && (d >= Long.MIN_VALUE && d <= Long.MAX_VALUE && d > Integer.MAX_VALUE))
                return LONG;
            if (num instanceof Integer || d % 1 == 0 && (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE)) return INT;
            return FLOAT;
        }
        if (value instanceof java.util.UUID) return UUID;
        if (value instanceof String) return STRING;
        if (value instanceof Boolean) return BOOLEAN;
        if (value instanceof net.minecraft.nbt.CompoundTag) return COMPOUND_TAG;
        if (value instanceof org.joml.Vector3f) return VECTOR3;
        if (value instanceof org.joml.Vector3d) return VECTOR3;
        if (value instanceof org.joml.Vector3i) return VECTOR3;
        if (value instanceof Vec3) return VECTOR3;
        if (value instanceof org.joml.Quaternionf) return QUATERNION;
        throw new IllegalArgumentException("Unsupported value type for serializer: " + value.getClass());
    }

    public static EntitySerializerType fromSerializer(EntityDataSerializer<?> serializer) {
        for (EntitySerializerType type : values()) {
            if (type.getSerializer() == serializer) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown EntityDataSerializer: " + serializer);
    }

}