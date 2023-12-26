package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import net.liopyu.entityjs.kube.EntityJSEvent;
import net.liopyu.entityjs.kube.EntityModificationEventJS;
import net.minecraft.core.Registry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;


@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "entityjs";

    public EntityJSMod() {
        LOGGER.info("Loading EntityJS-Liopyu");
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(EntityJSMod::onEntityRegistry);
    }

    @SubscribeEvent
    public static void onEntityRegistry(RegisterEvent event) {
        if (event.getRegistryKey() == Registry.ENTITY_TYPE_REGISTRY) {
            EntityJSEvent.ENTITYREGISTRY.post(new EntityModificationEventJS());
        }
    }
}
