package net.liopyu.entityjs.fabric;

import net.fabricmc.api.ModInitializer;
import net.liopyu.entityjs.common.EntityJSMod;
import net.liopyu.entityjs.util.EventHandlers;

public final class EntityJSFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EntityJSMod.LOGGER.info("Loading EntityJS-Liopyu");
        EventHandlers.init();
        FabricSyncedData.init();
    }
}
