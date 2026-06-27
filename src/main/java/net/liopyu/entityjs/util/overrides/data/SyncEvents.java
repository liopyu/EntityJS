package net.liopyu.entityjs.util.overrides.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class SyncEvents {
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking e) {
        var ent = e.getTarget();
        if (ent instanceof Entity && e.getEntity() instanceof ServerPlayer sp) {
            var vals = ServerCache.getAll(ent);
            var types = ServerCache.getTypes(ent);
            Net.sendAllTo(sp, ent.getUUID(), vals, types);
        }
    }
}


