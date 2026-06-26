package net.liopyu.entityjs.client;

import dev.latvian.mods.kubejs.util.Cast;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderEntityBuilder;
import net.liopyu.entityjs.client.living.CustomKubeJSEntityRenderer;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.client.nonliving.*;
import net.liopyu.entityjs.util.EntityRendererTypeHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientEventHandlers {


    /*@EventBusSubscriber(modid = EntityJSMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(ModKeybinds.mount_jump.get());
        }
    }*/

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientEventHandlers::registerEntityRenders);
    }

    private static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSEntityRenderer<>(renderManager, builder));
        }
        for (ArrowEntityBuilder<?> builder : ArrowEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSArrowEntityRenderer<>(renderManager, builder));
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSProjectileEntityRenderer<>(renderManager, builder));
        }
        for (EyeOfEnderEntityBuilder<?> builder : EyeOfEnderEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSEnderEyeRenderer<>(renderManager, builder));
        }
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSNLEntityRenderer<>(renderManager, builder));
        }
        for (BoatEntityBuilder<?> builder : BoatEntityBuilder.thisList) {
            event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new KubeJSBoatRenderer<>(renderManager, builder));
        }
        for (CustomEntityJSBuilder builder : CustomEntityBuilder.thisList) {
            if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesEntityTypeRenderer()) {
                event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> EntityRendererTypeHelper.create(renderManager, customBuilder));
            } else if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesCustomRenderer()) {
                event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> EntityRendererTypeHelper.compatibleRendererOrThrow(customBuilder, customBuilder.createEntityRenderer(renderManager), "custom renderer"));
            } else if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesEntityModelRenderer()) {
                event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new CustomEntityModelRenderer<>(renderManager, customBuilder));
            } else if (builder instanceof CustomEntityBuilder customBuilder && !customBuilder.isLivingEntityClass()) {
                event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new CustomKubeJSNonLivingEntityRenderer<>(renderManager, builder));
            } else {
                event.registerEntityRenderer(Cast.to(builder.get()), renderManager -> new CustomKubeJSEntityRenderer<>(renderManager, builder));
            }
        }
    }

}
