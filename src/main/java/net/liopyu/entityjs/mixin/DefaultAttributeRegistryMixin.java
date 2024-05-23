package net.liopyu.entityjs.mixin;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.liopyu.entityjs.events.ModifyAttributeEventJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@Mixin(FabricDefaultAttributeRegistry.class)
public class DefaultAttributeRegistryMixin {
    //@Inject(method = "register(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;)V", at = @At("RETURN"), cancellable = true)
    private static void onGet(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder, CallbackInfo ci) {
        ModifyAttributeEventJS event = new ModifyAttributeEventJS();
        ModifyAttributeEventJS.HANDLERS.forEach(handler -> handler.accept(event));
    }
}
