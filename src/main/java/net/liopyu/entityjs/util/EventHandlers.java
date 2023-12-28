package net.liopyu.entityjs.util;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class EventHandlers {

    public static void init() {

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(EventHandlers::attributeCreation);
    }

    private static void attributeCreation(EntityAttributeCreationEvent event) {
        // BaseEntityBuilder.attributeSuppliers.forEach((type, attribute) -> {
        //     event.put(type.get(), attribute.build()); // Doesn't work, needs to be a living entity builder
        // });
    }
}
