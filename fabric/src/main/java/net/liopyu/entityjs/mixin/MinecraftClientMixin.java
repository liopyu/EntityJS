package net.liopyu.entityjs.mixin;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.liopyu.entityjs.client.ClientEventHandlers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
@Environment(EnvType.CLIENT)
public class MinecraftClientMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onClientInit(CallbackInfo info) {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ClientEventHandlers.registerEntityRenderers();
        });
    }
}