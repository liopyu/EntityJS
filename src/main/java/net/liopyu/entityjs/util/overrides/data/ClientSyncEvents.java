package net.liopyu.entityjs.util.overrides.data;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientSyncEvents {
    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        ClientCache.clear();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut e) {
        ClientCache.clear();
    }
}
