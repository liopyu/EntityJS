package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
//import net.liopyu.entityjs.builders.BaseEntityBuilder;
//import net.liopyu.entityjs.builders.EntityTypeBuilderJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.slf4j.Logger;

// Named this way to not interfere with Kube's EntityJS class
@Mod(EntityJSMod.MOD_ID)
public class EntityJSMod {

    public static final String MOD_NAME = "EntityJS";
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

    public static void register() {
        // Register your entities here
        // Call the appropriate function from your mod to register the entities
        EntityJSEvent.GROUP.register();
    }
}
