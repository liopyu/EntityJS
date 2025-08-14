package net.liopyu.entityjs.util.data;


import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class SavedDataJS extends SavedData {
    public static final String ID = EntityJSMod.MOD_ID + "_synced";
    private final Map<UUID, Map<String, Tag>> values = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> types = new HashMap<>();

    public static SavedDataJS get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SavedDataJS::new, SavedDataJS::load, DataFixTypes.LEVEL),
                ID
        );
    }

    public static SavedDataJS load(CompoundTag tag, HolderLookup.Provider regs) {
        SavedDataJS d = new SavedDataJS();
        ListTag ents = tag.getList("entries", Tag.TAG_COMPOUND);
        for (Tag t : ents) {
            CompoundTag c = (CompoundTag) t;
            UUID id = c.getUUID("id");
            Map<String, Tag> vmap = new HashMap<>();
            Map<String, Integer> tmap = new HashMap<>();
            ListTag vals = c.getList("values", Tag.TAG_COMPOUND);
            for (Tag vt : vals) {
                CompoundTag vc = (CompoundTag) vt;
                String name = vc.getString("name");
                Tag v = vc.get("v");
                int type = vc.contains("t", Tag.TAG_INT) ? vc.getInt("t") : -1;
                if (v != null) vmap.put(name, v.copy());
                if (type >= 0) tmap.put(name, type);
            }
            d.values.put(id, vmap);
            d.types.put(id, tmap);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag out, HolderLookup.Provider regs) {
        ListTag ents = new ListTag();
        for (var e : values.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putUUID("id", e.getKey());
            ListTag vals = new ListTag();
            Map<String, Integer> tmap = types.getOrDefault(e.getKey(), Map.of());
            for (var n : e.getValue().entrySet()) {
                CompoundTag vc = new CompoundTag();
                vc.putString("name", n.getKey());
                vc.put("v", n.getValue().copy());
                if (tmap.containsKey(n.getKey())) vc.putInt("t", tmap.get(n.getKey()));
                vals.add(vc);
            }
            c.put("values", vals);
            ents.add(c);
        }
        out.put("entries", ents);
        return out;
    }

    public Map<String, Tag> getAll(UUID id) {
        Map<String, Tag> m = values.get(id);
        if (m == null) return Map.of();
        Map<String, Tag> copy = new HashMap<>();
        m.forEach((k, v) -> copy.put(k, v.copy()));
        return copy;
    }

    public Map<String, Integer> getTypes(UUID id) {
        Map<String, Integer> m = types.get(id);
        if (m == null) return Map.of();
        return new HashMap<>(m);
    }

    public Tag get(UUID id, String name) {
        Map<String, Tag> m = values.get(id);
        if (m == null) return null;
        Tag t = m.get(name);
        return t == null ? null : t.copy();
    }

    public boolean has(UUID id, String name) {
        Map<String, Tag> m = values.get(id);
        return m != null && m.containsKey(name);
    }

    public void put(UUID id, String name, Tag v) {
        values.computeIfAbsent(id, k -> new HashMap<>()).put(name, v.copy());
        setDirty();
    }

    public void putWithType(UUID id, String name, Tag v, EntitySerializerType type) {
        values.computeIfAbsent(id, k -> new HashMap<>()).put(name, v.copy());
        types.computeIfAbsent(id, k -> new HashMap<>()).put(name, type.ordinal());
        setDirty();
    }

    public Optional<EntitySerializerType> getType(UUID id, String name) {
        Integer i = types.getOrDefault(id, Map.of()).get(name);
        return i == null ? Optional.empty() : Optional.of(EntitySerializerType.values()[i]);
    }

    public void remove(UUID id, String name) {
        var m = values.get(id);
        if (m != null) m.remove(name);
        var tm = types.get(id);
        if (tm != null) tm.remove(name);
        setDirty();
    }
}