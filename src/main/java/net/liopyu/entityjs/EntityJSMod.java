package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;


@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "entityjs";

    public EntityJSMod() {
        LOGGER.info("Loading EntityJS-Liopyu");
    }


}
