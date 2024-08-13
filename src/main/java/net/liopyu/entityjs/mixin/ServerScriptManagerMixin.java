package net.liopyu.entityjs.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.kubejs.script.data.GeneratedDataStage;
import dev.latvian.mods.kubejs.script.data.VirtualDataPack;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

/**
 * A mixin into KubeJS's {@link ServerScriptManager} to ensure custom data
 * is added before Forge runs its biome modifiers.
 */
@Mixin(value = ServerScriptManager.class, remap = false)
public abstract class ServerScriptManagerMixin {

    @Unique
    private static final ThreadLocal<List<PackResources>> entityjs$CapturedPacks = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<VirtualDataPack> entityjs$CapturedVirtualDataPack = new ThreadLocal<>();

    @WrapOperation(method = "createPackResources", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>(Ljava/util/Collection;)V"), remap = false)
    private static void entityjs$capturePacks(List<PackResources> originalList, Operation<ArrayList<PackResources>> original, ServerScriptManager instance) {
        entityjs$CapturedPacks.set(originalList);
        original.call(originalList);
    }

    @WrapOperation(method = "createPackResources", at = @At(value = "INVOKE", target = "Ldev/latvian/mods/kubejs/server/ServerScriptManager;reload()V"), remap = false)
    private static void entityjs$captureVirtualDataPack(ServerScriptManager instance, Operation<Void> original) {
        entityjs$CapturedVirtualDataPack.set(instance.virtualPacks.get(GeneratedDataStage.LAST));
        original.call(instance);
    }

    //Unused
    /*@Inject(method = "createPackResources", at = @At("RETURN"), remap = false)
    private void entityjs$postEvent(CallbackInfoReturnable<List<PackResources>> cir) {
        EventHandlers.postDataEvent(entityjs$CapturedVirtualDataPack.get(), entityjs$CapturedPacks.get());
        // Clean up ThreadLocal variables to avoid memory leaks
        entityjs$CapturedPacks.remove();
        entityjs$CapturedVirtualDataPack.remove();
    }*/
}