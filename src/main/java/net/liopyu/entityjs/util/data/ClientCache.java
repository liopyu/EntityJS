package net.liopyu.entityjs.util.data;


import net.liopyu.entityjs.util.EntitySerializerType;

public final class ClientCache {
    private static final java.util.Map<java.util.UUID, java.util.Map<String, net.minecraft.nbt.Tag>> DATA = new java.util.HashMap<>();
    private static final java.util.Map<java.util.UUID, java.util.Map<String, Integer>> TYPES = new java.util.HashMap<>();

    public static void setAll(java.util.UUID id, java.util.Map<String, net.minecraft.nbt.Tag> values, java.util.Map<String, Integer> types) {
        java.util.Map<String, net.minecraft.nbt.Tag> vcopy = new java.util.HashMap<>();
        values.forEach((k, t) -> vcopy.put(k, t.copy()));
        DATA.put(id, vcopy);
        TYPES.put(id, new java.util.HashMap<>(types));
    }

    public static void set(java.util.UUID id, String name, net.minecraft.nbt.Tag v) {
        DATA.computeIfAbsent(id, k -> new java.util.HashMap<>()).put(name, v.copy());
    }

    public static void setType(java.util.UUID id, String name, int ord) {
        Integer old = TYPES.computeIfAbsent(id, k -> new java.util.HashMap<>()).put(name, ord);
        if (old == null || old != ord) {
            var m = DATA.get(id);
            if (m != null) m.remove(name);
        }
    }

    public static net.minecraft.nbt.Tag get(java.util.UUID id, String name) {
        var m = DATA.get(id);
        if (m == null) return null;
        var t = m.get(name);
        return t == null ? null : t.copy();
    }

    public static java.util.Optional<EntitySerializerType> getType(java.util.UUID id, String name) {
        Integer i = TYPES.getOrDefault(id, java.util.Map.of()).get(name);
        return i == null ? java.util.Optional.empty() : java.util.Optional.of(EntitySerializerType.values()[i]);
    }

    public static void remove(java.util.UUID id, String name) {
        var m = DATA.get(id);
        if (m != null) m.remove(name);
        var tm = TYPES.get(id);
        if (tm != null) tm.remove(name);
    }
}
