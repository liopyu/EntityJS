package net.liopyu.entityjs.client;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.client.living.CustomKubeJSEntityRenderer;
import net.liopyu.entityjs.client.nonliving.*;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.fabric.FabricSyncedDataClient;
import net.liopyu.entityjs.util.EntityRendererTypeHelper;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.EntityType;

public class ClientEventHandlers implements ClientModInitializer {
    /**
     * Initializes client-specific features of the mod.
     * <p>
     * This method registers key bindings immediately and delays the registration of entity renderers by 5 seconds.
     * The delay is necessary due to the random registration order of entity types in KubeJS, where sometimes
     * entity types register after the renderers resulting in a null entity type/failed renderer registry.
     * </p>
     */
    @Override
    public void onInitializeClient() {
        FabricSyncedDataClient.init();
        // EventHandlers.registerClientAttributes();
        registerKeyBindings();
    }


    public static void registerEntityRenderers() {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSEntityRenderer<>(dispatcher, builder));
        }
        for (ArrowEntityBuilder<?> builder : ArrowEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSArrowEntityRenderer<>(dispatcher, builder));
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSProjectileEntityRenderer<>(dispatcher, builder));
        }
        for (EyeOfEnderEntityBuilder<?> builder : EyeOfEnderEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSEnderEyeRenderer<>(dispatcher, builder));
        }
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSNLEntityRenderer<>(dispatcher, builder));
        }
        for (BoatEntityBuilder<?> builder : BoatEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType != null) EntityRendererRegistry.register(UtilsJS.cast(entityType), (dispatcher) -> new KubeJSBoatRenderer<>(dispatcher, builder));
        }
        for (CustomEntityJSBuilder builder : CustomEntityBuilder.thisList) {
            EntityType<?> entityType = EntityJSHelperClass.getRegisteredEntityType(builder, "renderer");
            if (entityType == null) continue;
            EntityRendererRegistry.register(UtilsJS.cast(entityType), dispatcher -> UtilsJS.cast(
                    entityjs$createCustomRenderer(dispatcher, builder)));
        }
    }

    private static EntityRenderer<?> entityjs$createCustomRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context dispatcher,
                                                                    CustomEntityJSBuilder builder) {
                if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesEntityTypeRenderer()) {
                    return EntityRendererTypeHelper.create(dispatcher, customBuilder);
                }
                if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesCustomRenderer()) {
                    return EntityRendererTypeHelper.compatibleRendererOrThrow(customBuilder, customBuilder.createEntityRenderer(dispatcher), "custom renderer");
                }
                if (builder instanceof CustomEntityBuilder customBuilder && customBuilder.usesEntityModelRenderer()) {
                    return new CustomEntityModelRenderer<>(dispatcher, customBuilder);
                }
                if (builder instanceof CustomEntityBuilder customBuilder && !customBuilder.isLivingEntityClass()) {
                    return new CustomKubeJSNonLivingEntityRenderer<>(dispatcher, builder);
                }
                return new CustomKubeJSEntityRenderer<>(dispatcher, builder);
    }

    private void registerKeyBindings() {
        KeyMapping mountJumpKey = ModKeybinds.mount_jump;
        KeyBindingHelper.registerKeyBinding(mountJumpKey);
    }
}
