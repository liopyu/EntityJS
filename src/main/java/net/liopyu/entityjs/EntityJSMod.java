package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import net.liopyu.entityjs.client.ClientEventHandlers;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;


@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "entityjs";

    public EntityJSMod(IEventBus modBus) {
        LOGGER.info("Loading EntityJS-Liopyu");

        EventHandlers.init();
        RegistryUtil.init(modBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandlers.init(modBus);
        }
    }


    public static ResourceLocation identifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
