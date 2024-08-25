package net.liopyu.entityjs.client;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.client.nonliving.*;
import net.liopyu.entityjs.util.ModKeybinds;
import net.minecraft.client.KeyMapping;

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
        registerKeyBindings();
    }


    public static void registerEntityRenderers() {
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSEntityRenderer<>(dispatcher, builder));
        }
        for (ArrowEntityBuilder<?> builder : ArrowEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSArrowEntityRenderer<>(dispatcher, builder));
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSProjectileEntityRenderer<>(dispatcher, builder));
        }
        for (EyeOfEnderEntityBuilder<?> builder : EyeOfEnderEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSEnderEyeRenderer<>(dispatcher, builder));
        }
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSNLEntityRenderer<>(dispatcher, builder));
        }
        for (BoatEntityBuilder<?> builder : BoatEntityBuilder.thisList) {
            EntityRendererRegistry.register(UtilsJS.cast(builder.get()), (dispatcher) -> new KubeJSBoatRenderer<>(dispatcher, builder));
        }
    }

    private void registerKeyBindings() {
        KeyMapping mountJumpKey = ModKeybinds.mount_jump;
        KeyBindingHelper.registerKeyBinding(mountJumpKey);
    }
}
