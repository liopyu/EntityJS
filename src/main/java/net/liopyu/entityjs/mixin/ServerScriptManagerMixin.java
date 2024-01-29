package net.liopyu.entityjs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.generator.DataJsonGenerator;
import dev.latvian.mods.kubejs.script.data.VirtualKubeJSDataPack;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedList;

/**
 * A 'simple' mixin into Kube's {@link ServerScriptManager} to ensure our data
 * is added <strong>before</strong> forge runs its biome modifiers, which are
 * read before {@link dev.latvian.mods.kubejs.registry.BuilderBase#generateDataJsons(DataJsonGenerator) BuilderBase#generateDataJsons()}
 * and {@link dev.latvian.mods.kubejs.KubeJSPlugin#generateDataJsons(DataJsonGenerator) KubeJSPlugin#generateDataJsons()}'s data is realized.
 * <p>
 * This is taken from <a href="https://github.com/Notenoughmail/KubeJS-TFC/blob/1.20.1/src/main/java/com/notenoughmail/kubejs_tfc/util/implementation/mixin/ServerScripManagerMixin.java">KubeJS TFC</a>
 * and modified to use Mixin Extra's {@link WrapOperation} annotation
 */
// Currently unused but keeping it around just in case
@Mixin(value = ServerScriptManager.class, remap = false)
public abstract class ServerScriptManagerMixin {

    @Unique
    private MultiPackResourceManager entityjs$WrappedManager;
    @Unique
    private VirtualKubeJSDataPack entityjs$VirtualDataPack;

    @WrapOperation(method = "wrapResourceManager", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/server/ServerScriptManager;reloadScriptManager(Lnet/minecraft/server/packs/resources/ResourceManager;)V"), remap = false)
    private void entityjs$captureMultiManager(ServerScriptManager instance, ResourceManager resourceManager, Operation<Void> original) {
        if (resourceManager instanceof MultiPackResourceManager multiManager) {
            entityjs$WrappedManager = multiManager;
        }
        original.call(instance, resourceManager);
    }

    @WrapOperation(method = "wrapResourceManager", at = @At(value = "INVOKE", target = "Ljava/util/LinkedList;addFirst(Ljava/lang/Object;)V"), remap = false)
    private <E> void entityjs$captureVirtualDataPack(LinkedList<E> instance, E e, Operation<Void> original) {
        if (e instanceof VirtualKubeJSDataPack pack) {
            entityjs$VirtualDataPack = pack;
        }
        original.call(instance, e); // The annotation parser isn't very happy about this despite it being correct (seemingly)
    }

    @Inject(method = "wrapResourceManager", at = @At(target = "Ldev/latvian/mods/kubejs/event/EventHandler;post(Ldev/latvian/mods/kubejs/script/ScriptTypeHolder;Ldev/latvian/mods/kubejs/event/EventJS;)Ldev/latvian/mods/kubejs/event/EventResult;", shift = At.Shift.AFTER, value = "INVOKE", ordinal = 1), remap = false)
    private void entityjs$postEvent(CloseableResourceManager original, CallbackInfoReturnable<MultiPackResourceManager> cir) {
        EventHandlers.postDataEvent(entityjs$VirtualDataPack, entityjs$WrappedManager);
    }
}
