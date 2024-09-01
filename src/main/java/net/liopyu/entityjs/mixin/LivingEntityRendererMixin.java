package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = LivingEntityRenderer.class, priority = 0, remap = true)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {


    @Shadow
    public abstract M getModel();

    private T currentEntity;
    private M currentModel;
    @Unique
    private Object entityJs$builder;

    private LivingEntityRenderer<T, M> getRenderer() {
        return ((LivingEntityRenderer) ((Object) this));
    }


    @Inject(method = "getRenderType", at = @At("HEAD"), remap = true, cancellable = true)
    private void onGetRenderType(T entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        var entityType = entity.getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entity);
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }
        if (entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setTextureLocation != null && builder.setRenderType != null) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: You may not set both setRenderType and setTextureLocation at the same time for entity: " + entity.getType() + ".");
                return;
            }
            if (builder.setTextureLocation != null) {
                var context = new ContextUtils.RendererModelContext(entity, getRenderer(), currentModel);
                try {
                    var resourcelocation = EntityJSHelperClass.convertObjectToDesired(builder.setTextureLocation.apply(context), "resourcelocation");
                    if (resourcelocation != null) {
                        var textureLocation = (ResourceLocation) resourcelocation;
                        if (translucent) {
                            cir.setReturnValue(RenderType.itemEntityTranslucentCull(textureLocation));
                            return;
                        } else if (bodyVisible) {
                            cir.setReturnValue(getModel().renderType(textureLocation));
                            return;
                        } else {
                            var finalValue = glowing ? RenderType.outline(textureLocation) : null;
                            cir.setReturnValue(finalValue);
                            return;
                        }
                    }
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value in setTextureLocation field from entity: " + entity.getType() + ". Value: " + resourcelocation + ". Must be a resource location");
                } catch (Throwable e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in modifyEntity method setTextureLocation from entity: " + entity.getType() + ". ", e);
                }
            }
            if (builder.setRenderType != null) {
                var context = new ContextUtils.RendererModelContext(entity, getRenderer(), currentModel);
                try {
                    var returnValue = EntityJSHelperClass.convertToRenderType(builder.setRenderType.apply(context), cir.getReturnValue());
                    if (returnValue != null) {
                        cir.setReturnValue(returnValue);
                        return;
                    }
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value in setRenderType field from entity: " + entity.getType() + ". Must return either a resource location or a RenderType. Return null for the default texture logic.");
                } catch (Throwable e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in modifyEntity method setRenderType from entity: " + entity.getType() + ". ", e);
                }
            }
        }
    }
}
