package net.liopyu.entityjs.common;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/** Common EntityJS identity and logging surface used by both loader implementations. */
public final class EntityJSMod {
    public static final String MOD_ID = "entityjs";
    public static final Logger LOGGER = LogUtils.getLogger();

    private EntityJSMod() {
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
