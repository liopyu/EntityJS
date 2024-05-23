package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.liopyu.entityjs.client.ClientEventHandlers;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class EntityJSMod implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "entityjs";

    @Override
    public void onInitialize() {
        LOGGER.info("Loading EntityJS-Liopyu");

        //EventHandlers.init();

        //RegistryUtil.init();

        /*if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            ClientEventHandlers.init();
        }*/
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
