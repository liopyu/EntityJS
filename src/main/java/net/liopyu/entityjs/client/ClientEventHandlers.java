package net.liopyu.entityjs.client;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandlers {

    public static void init() {

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientEventHandlers::registerEntityRenders);
    }

    private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSEntityRenderer<>(renderManager, builder));
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSArrowEntityRenderer<>(renderManager, builder));
        }
    }
}
