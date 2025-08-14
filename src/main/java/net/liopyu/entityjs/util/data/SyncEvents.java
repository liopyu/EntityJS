package net.liopyu.entityjs.util.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@net.neoforged.fml.common.EventBusSubscriber
public class SyncEvents {
    @net.neoforged.bus.api.SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking e) {
        var ent = e.getTarget();
        if (ent instanceof Entity && e.getEntity() instanceof ServerPlayer sp) {
            var vals = ServerCache.getAll(ent);
            var types = ServerCache.getTypes(ent);
            Net.sendAllTo(sp, ent.getUUID(), vals, types);
        }
    }
}


