package net.liopyu.entityjs.util.data;


import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public final class ServerCache {
    public static Map<String, Tag> getAll(Entity e) {
        if (!(e.level() instanceof ServerLevel sl)) return Map.of();
        return SavedDataJS.get(sl).getAll(e.getUUID());
    }

    public static Map<String, Integer> getTypes(Entity e) {
        if (!(e.level() instanceof ServerLevel sl)) return Map.of();
        return SavedDataJS.get(sl).getTypes(e.getUUID());
    }

    public static Tag get(Entity e, String name) {
        if (!(e.level() instanceof ServerLevel sl)) return null;
        return SavedDataJS.get(sl).get(e.getUUID(), name);
    }

    public static boolean has(Entity e, String name) {
        if (!(e.level() instanceof ServerLevel sl)) return false;
        return SavedDataJS.get(sl).has(e.getUUID(), name);
    }

    public static void set(Entity e, String name, Tag v) {
        if (!(e.level() instanceof ServerLevel sl)) return;
        SavedDataJS.get(sl).put(e.getUUID(), name, v);
        Net.sendValueTracking(e, e.getUUID(), name, v);
    }

    public static void ensure(net.minecraft.world.entity.Entity e, String name, net.minecraft.nbt.Tag v, EntitySerializerType type) {
        if (!(e.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        var data = net.liopyu.entityjs.util.data.SavedDataJS.get(sl);
        var id = e.getUUID();
        var prev = data.getType(id, name);
        if (prev.isEmpty()) {
            data.putWithType(id, name, v, type);
            Net.sendTypedValueTracking(e, id, name, type.ordinal(), v);
        } else if (prev.get() != type) {
            data.putWithType(id, name, v, type);
            Net.sendTypedValueTracking(e, id, name, type.ordinal(), v);
        }
        net.liopyu.entityjs.util.data.InitDecl.declare(e, name, type);
    }


    public static void migrateIfAbsent(net.minecraft.world.entity.Entity e, String name, net.minecraft.nbt.Tag v, EntitySerializerType type) {
        if (!(e.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;
        var data = net.liopyu.entityjs.util.data.SavedDataJS.get(sl);
        var id = e.getUUID();
        if (data.getType(id, name).isEmpty()) {
            data.putWithType(id, name, v, type);
        }
    }

}