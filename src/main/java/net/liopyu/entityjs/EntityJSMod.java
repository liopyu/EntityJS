package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import net.liopyu.entityjs.client.ClientEventHandlers;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;


@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "entityjs";

    public EntityJSMod() {
        LOGGER.info("Loading EntityJS-Liopyu");

        EventHandlers.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandlers.init();
        }
    }
}
