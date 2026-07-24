package net.liopyu.entityjs.mixin;

import com.probejs.ProbeCommands;
import com.probejs.features.plugin.DocGenerationEventJS;
import net.liopyu.entityjs.events.EntityJSBuiltinDocs;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Consumer;

@Mixin(value = ProbeCommands.class, remap = false)
public class ProbeCommandsMixin {
    @Inject(
            method = "lambda$triggerDump$4",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/latvian/mods/kubejs/event/EventHandler;post(Ldev/latvian/mods/kubejs/script/ScriptTypeHolder;Ldev/latvian/mods/kubejs/event/EventJS;)Ldev/latvian/mods/kubejs/event/EventResult;"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void entityjs$registerBuiltinDocs(Consumer<String> progressReporter, ServerPlayer player, CallbackInfo ci, DocGenerationEventJS event) {
        EntityJSBuiltinDocs.register(event);
    }
}
