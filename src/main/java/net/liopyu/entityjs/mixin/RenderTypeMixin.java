package net.liopyu.entityjs.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//@Mixin(RenderType.class)
public abstract class RenderTypeMixin {
/*
    private static final ResourceLocation CUSTOM_TEXTURE = new ResourceLocation("entityjs", "textures/entity/custom_texture.png");
private RenderType renderType = (RenderType)(Object) this;
    @Inject(method = "armorCutoutNoCull", at = @At("HEAD"), cancellable = true)
    private static void injectArmorCutoutNoCull(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.armorCutoutNoCull(CUSTOM_TEXTURE));
    }

    @Inject(method = "entitySolid", at = @At("HEAD"), cancellable = true)
    private static void injectEntitySolid(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entitySolid(CUSTOM_TEXTURE));
    }

    @Inject(method = "entityCutout", at = @At("HEAD"), cancellable = true)
    private static void injectEntityCutout(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entityCutout(CUSTOM_TEXTURE));
    }

    @Inject(method = "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;",
            at = @At("HEAD"), cancellable = true)
    private static void injectEntityCutoutNoCull(ResourceLocation pLocation, boolean pOutline, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entityCutoutNoCull(CUSTOM_TEXTURE, pOutline));
    }

    @Inject(method = "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;",
            at = @At("HEAD"), cancellable = true)
    private static void injectEntityCutoutNoCullSimple(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entityCutoutNoCull(CUSTOM_TEXTURE));
    }

    @Inject(method = "entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void injectEntityTranslucent(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entityTranslucent(CUSTOM_TEXTURE,true));
    }
    @Inject(method = "entityTranslucent(Lnet/minecraft/resources/ResourceLocation;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void injectEntityTranslucent(ResourceLocation pLocation, boolean pOutline, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue((RenderType)RenderType.ENTITY_TRANSLUCENT.apply(pLocation, pOutline));
    }

    @Inject(method = "entityShadow", at = @At("HEAD"), cancellable = true)
    private static void injectEntityShadow(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.entityShadow(CUSTOM_TEXTURE));
    }

    @Inject(method = "beaconBeam", at = @At("HEAD"), cancellable = true)
    private static void injectBeaconBeam(ResourceLocation pLocation, boolean pColorFlag, CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(RenderType.beaconBeam(CUSTOM_TEXTURE, pColorFlag));
    }*/
}
