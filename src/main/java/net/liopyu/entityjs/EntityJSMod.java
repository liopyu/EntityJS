package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.client.ClientEventHandlers;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;


@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "entityjs";

    public EntityJSMod() {
        LOGGER.info("Loading EntityJS-Liopyu");

        EventHandlers.init();
        RegistryUtil.init(FMLJavaModLoadingContext.get().getModEventBus());

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandlers.init();
        }
    }


    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
