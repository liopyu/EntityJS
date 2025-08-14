package net.liopyu.entityjs.util.data;


import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NbtConvert {
    public static Tag toTag(EntitySerializerType type, Object o) {
        return switch (type) {
            case BYTE -> ByteTag.valueOf(((Number) o).byteValue());
            case INT -> IntTag.valueOf(((Number) o).intValue());
            case LONG -> LongTag.valueOf(((Number) o).longValue());
            case FLOAT -> FloatTag.valueOf(((Number) o).floatValue());
            case STRING -> StringTag.valueOf(String.valueOf(o));
            case BOOLEAN -> ByteTag.valueOf((byte) (((Boolean) o) ? 1 : 0));
            case COMPOUND_TAG -> (o instanceof CompoundTag ct) ? ct.copy() : toCompound(o);
            case UUID -> NbtUtils.createUUID((UUID) o);
            case VECTOR3 -> toVec3(vec3x(o), vec3y(o), vec3z(o));
            case QUATERNION -> toQuat((Quaternionf) o);
        };
    }

    public static Object fromTag(EntitySerializerType type, Tag tag) {
        return switch (type) {
            case BYTE -> ((ByteTag) tag).getAsByte();
            case INT -> ((IntTag) tag).getAsInt();
            case LONG -> ((LongTag) tag).getAsLong();
            case FLOAT -> ((FloatTag) tag).getAsFloat();
            case STRING -> ((StringTag) tag).getAsString();
            case BOOLEAN -> ((ByteTag) tag).getAsByte() != 0;
            case COMPOUND_TAG -> ((CompoundTag) tag).copy();
            case UUID -> NbtUtils.loadUUID(tag);
            case VECTOR3 -> {
                CompoundTag c = (CompoundTag) tag;
                yield new Vector3f(c.getFloat("x"), c.getFloat("y"), c.getFloat("z"));
            }
            case QUATERNION -> {
                CompoundTag c = (CompoundTag) tag;
                yield new Quaternionf(c.getFloat("x"), c.getFloat("y"), c.getFloat("z"), c.getFloat("w"));
            }
        };
    }

    private static CompoundTag toCompound(Object o) {
        CompoundTag ct = new CompoundTag();
        if (o instanceof Map<?, ?> m)
            for (var e : m.entrySet()) ct.put(String.valueOf(e.getKey()), wrapAny(e.getValue()));
        return ct;
    }

    private static Tag wrapAny(Object o) {
        if (o instanceof Tag t) return t.copy();
        if (o instanceof String s) return StringTag.valueOf(s);
        if (o instanceof Integer i) return IntTag.valueOf(i);
        if (o instanceof Long l) return LongTag.valueOf(l);
        if (o instanceof Float f) return FloatTag.valueOf(f);
        if (o instanceof Double d) return FloatTag.valueOf(d.floatValue());
        if (o instanceof Short s) return ShortTag.valueOf(s);
        if (o instanceof Byte b) return ByteTag.valueOf(b);
        if (o instanceof Boolean b) return ByteTag.valueOf((byte) (b ? 1 : 0));
        if (o instanceof UUID u) return NbtUtils.createUUID(u);
        if (o instanceof Vector3f v3) return toVec3(v3.x(), v3.y(), v3.z());
        if (o instanceof Vector3d v3d) return toVec3((float) v3d.x(), (float) v3d.y(), (float) v3d.z());
        if (o instanceof Vec3 v) return toVec3((float) v.x, (float) v.y, (float) v.z);
        if (o instanceof Vec3i vi) return toVec3(vi.getX(), vi.getY(), vi.getZ());
        if (o instanceof List<?> list) {
            ListTag lt = new ListTag(); for (Object e : list) lt.add(wrapAny(e)); return lt;
        }
        if (o instanceof Map<?, ?> map) return toCompound(map);
        return StringTag.valueOf(String.valueOf(o));
    }

    private static CompoundTag toVec3(float x, float y, float z) {
        CompoundTag c = new CompoundTag();
        c.putFloat("x", x);
        c.putFloat("y", y);
        c.putFloat("z", z);
        return c;
    }

    private static CompoundTag toQuat(Quaternionf q) {
        CompoundTag c = new CompoundTag();
        c.putFloat("x", q.x());
        c.putFloat("y", q.y());
        c.putFloat("z", q.z());
        c.putFloat("w", q.w());
        return c;
    }

    private static float vec3x(Object o) {
        if (o instanceof Vector3f v) return v.x();
        if (o instanceof Vector3d v) return (float) v.x();
        if (o instanceof Vec3 v) return (float) v.x;
        if (o instanceof Vec3i v) return v.getX();
        if (o instanceof float[] a && a.length >= 3) return a[0];
        if (o instanceof double[] a2 && a2.length >= 3) return (float) a2[0];
        if (o instanceof int[] a3 && a3.length >= 3) return a3[0];
        if (o instanceof List<?> l && l.size() >= 3) return num(l.get(0));
        return 0f;
    }

    private static float vec3y(Object o) {
        if (o instanceof Vector3f v) return v.y();
        if (o instanceof Vector3d v) return (float) v.y();
        if (o instanceof Vec3 v) return (float) v.y;
        if (o instanceof Vec3i v) return v.getY();
        if (o instanceof float[] a && a.length >= 3) return a[1];
        if (o instanceof double[] a2 && a2.length >= 3) return (float) a2[1];
        if (o instanceof int[] a3 && a3.length >= 3) return a3[1];
        if (o instanceof List<?> l && l.size() >= 3) return num(l.get(1));
        return 0f;
    }

    private static float vec3z(Object o) {
        if (o instanceof Vector3f v) return v.z();
        if (o instanceof Vector3d v) return (float) v.z();
        if (o instanceof Vec3 v) return (float) v.z;
        if (o instanceof Vec3i v) return v.getZ();
        if (o instanceof float[] a && a.length >= 3) return a[2];
        if (o instanceof double[] a2 && a2.length >= 3) return (float) a2[2];
        if (o instanceof int[] a3 && a3.length >= 3) return a3[2];
        if (o instanceof List<?> l && l.size() >= 3) return num(l.get(2));
        return 0f;
    }

    private static float num(Object o) {
        if (o instanceof Number n) return n.floatValue();
        if (o instanceof String s) try {
            return Float.parseFloat(s);
        } catch (Exception ignored) {
        }
        return 0f;
    }
}