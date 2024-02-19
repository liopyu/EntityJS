package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.command.KubeJSCommands;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin class to reload error messages on each startup script reload.
 * This ensures that scripters can see error messages again after reloading startup scripts,
 * as startup scripts are the only possible way to trigger a reload for most entity methods.
 */
@Mixin(KubeJSCommands.class)
public abstract class KubeJSCommandsMixin {
    @Inject(method = "reloadStartup", at = @At(value = "RETURN", ordinal = 0), remap = false)
    private static void entityjs$onReloadStartup(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        EntityJSHelperClass.errorMessagesLogged.clear();
    }
}
