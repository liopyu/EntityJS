package net.liopyu.entityjs.client;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.client.nonliving.*;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandlers {
    @Mod.EventBusSubscriber(modid = EntityJSMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(ModKeybinds.mount_jump);
            //event.register(ModKeybinds.mount_inventory);
        }
    }

    public static void init() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ClientEventHandlers::registerEntityRenders);
    }

    private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSEntityRenderer<>(renderManager, builder));
        }
        for (ArrowEntityBuilder<?> builder : ArrowEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSArrowEntityRenderer<>(renderManager, builder));
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSProjectileEntityRenderer<>(renderManager, builder));
        }
        for (EyeOfEnderEntityBuilder<?> builder : EyeOfEnderEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSEnderEyeRenderer<>(renderManager, builder));
        }
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSNLEntityRenderer<>(renderManager, builder));
        }
        for (BoatEntityBuilder<?> builder : BoatEntityBuilder.thisList) {
            event.registerEntityRenderer(UtilsJS.cast(builder.get()), renderManager -> new KubeJSBoatRenderer<>(renderManager, builder));
        }
    }
}
