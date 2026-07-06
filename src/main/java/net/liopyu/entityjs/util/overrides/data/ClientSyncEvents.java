package net.liopyu.entityjs.util.overrides.data;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@net.neoforged.fml.common.EventBusSubscriber(value = Dist.CLIENT)
public class ClientSyncEvents {
    @net.neoforged.bus.api.SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        ClientCache.clear();
    }

    @net.neoforged.bus.api.SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientCache.clear();
    }
}
