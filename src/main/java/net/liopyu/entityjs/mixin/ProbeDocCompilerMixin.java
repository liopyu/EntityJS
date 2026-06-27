package net.liopyu.entityjs.mixin;

import com.probejs.docs.DocCompiler;
import com.probejs.features.plugin.DocGenerationEventJS;
import net.liopyu.entityjs.events.EntityJSBuiltinDocs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(value = DocCompiler.class, remap = false)
public class ProbeDocCompilerMixin {
    @Inject(method = "compile", at = @At("HEAD"))
    private static void entityjs$registerBuiltinDocs(Consumer<String> progressReporter, DocGenerationEventJS event, CallbackInfo ci) {
        EntityJSBuiltinDocs.register(event);
    }
}
