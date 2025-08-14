package net.liopyu.entityjs.util.data;


import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.world.entity.Entity;

public final class InitDecl {
    private static final class Session {
        final java.util.Map<String, EntitySerializerType> declared = new java.util.HashMap<>();
        boolean used = false;
    }

    private static final java.util.Map<java.util.UUID, Session> SESSIONS = new java.util.HashMap<>();

    public static void begin(net.minecraft.world.entity.Entity e) {
        SESSIONS.put(e.getUUID(), new Session());
    }

    public static void declare(Entity e, String name, EntitySerializerType type) {
        var s = SESSIONS.get(e.getUUID());
        if (s != null) {
            s.declared.put(name, type); s.used = true;
        }
    }

    public static void finalizeFor(Entity e) {
        var s = SESSIONS.remove(e.getUUID());
        if (!(e.level() instanceof net.minecraft.server.level.ServerLevel sl)) return;

        var id = e.getUUID();
        var vals = net.liopyu.entityjs.util.data.SavedDataJS.get(sl).getAll(id);
        var types = net.liopyu.entityjs.util.data.SavedDataJS.get(sl).getTypes(id);

        if (s != null && s.used) net.liopyu.entityjs.util.data.Net.sendAllTracking(e, id, vals, types);
        if (e instanceof net.minecraft.server.level.ServerPlayer sp) {
            net.liopyu.entityjs.util.data.Net.sendAllTo(sp, id, vals, types);
        }
    }
}
