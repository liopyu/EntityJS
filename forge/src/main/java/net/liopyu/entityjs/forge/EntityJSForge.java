package net.liopyu.entityjs.forge;

import net.liopyu.entityjs.common.EntityJSMod;
import net.liopyu.entityjs.client.ClientEventHandlers;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.RegistryUtil;
import net.liopyu.entityjs.util.overrides.data.Net;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(EntityJSMod.MOD_ID)
public final class EntityJSForge {
    public EntityJSForge() {
        EntityJSMod.LOGGER.info("Loading EntityJS-Liopyu");
        EventHandlers.init();
        Net.register();
        RegistryUtil.init(FMLJavaModLoadingContext.get().getModEventBus());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandlers.init();
        }
    }
}
