package net.liopyu.entityjs.util.data;

import net.liopyu.entityjs.EntityJSMod;
import net.minecraft.world.entity.Entity;

@net.neoforged.fml.common.EventBusSubscriber
public class SyncEvents {
    @net.neoforged.bus.api.SubscribeEvent
    public static void onStartTracking(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking e) {
        var t = e.getTarget();
        if (t instanceof Entity le && e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            var vals = net.liopyu.entityjs.util.data.ServerCache.getAll(le);
            var types = net.liopyu.entityjs.util.data.ServerCache.getTypes(le);
            net.liopyu.entityjs.util.data.Net.sendAllTo(sp, le.getUUID(), vals, types);
        }
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onEntityJoin(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e) {
        if (!e.getLevel().isClientSide() && e.getEntity() instanceof Entity le) {
            var vals = net.liopyu.entityjs.util.data.ServerCache.getAll(le);
            var types = net.liopyu.entityjs.util.data.ServerCache.getTypes(le);
            net.liopyu.entityjs.util.data.Net.sendAllTracking(le, le.getUUID(), vals, types);
        }
    }
}

