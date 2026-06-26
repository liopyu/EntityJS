package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.overrides.CallbackUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = LivingEntityRenderer.class, priority = 0, remap = true)
public abstract class LivingEntityRendererMixin<T, M extends EntityModel<?>> {


    @Shadow
    protected M model;

    @Unique
    private Map<EntityType<?>, Object> entityJs$builders;

    private LivingEntityRenderer<?, M> getRenderer() {
        return ((LivingEntityRenderer) ((Object) this));
    }

    @Unique
    private Object entityJs$withRendererFallback(String fieldName, CallbackInfoReturnable<?> cir, Object fallback, java.util.function.Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, cir, () -> fallback, callback);
    }

    @Inject(method = "getRenderType", at = @At("RETURN"), remap = true, cancellable = true)
    private void onGetRenderType(LivingEntity entity, boolean bodyVisible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
        if (EntityJSUtils.handlesOwnEntityJsCallbacks(entity)) return;

        var entityType = entity.getType();
        Object entityJs$builder = entityJs$builder(entityType, entity);
        if (entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setTextureLocation == null && builder.setRenderType == null) {
                return;
            }
        }
        if (entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setTextureLocation != null && builder.setRenderType != null) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: You may not set both setRenderType and setTextureLocation at the same time for entity: " + entity.getType() + ".");
                return;
            }

            if (builder.setTextureLocation != null) {
                var context = new ContextUtils.RendererModelContext(entity, getRenderer(), model);
                try {
                    var obj = entityJs$withRendererFallback("setTextureLocation", cir, cir.getReturnValue(), () -> builder.setTextureLocation.apply(context));
                    var resourcelocation = EntityJSHelperClass.convertObjectToDesired(obj, "resourcelocation");
                    if (resourcelocation != null) {
                        var textureLocation = (ResourceLocation) resourcelocation;
                        if (translucent) {
                            cir.setReturnValue(RenderType.itemEntityTranslucentCull(textureLocation));
                            return;
                        } else if (bodyVisible) {
                            cir.setReturnValue(model.renderType(textureLocation));
                            return;
                        } else {
                            var finalValue = glowing ? RenderType.outline(textureLocation) : null;
                            cir.setReturnValue(finalValue);
                            return;
                        }
                    }
                    if (obj != null) {
                        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value in setTextureLocation field from entity: " + entity.getType() + ". Value: " + resourcelocation + ". Must be a resource location");
                    }
                    return;
                } catch (Throwable e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in modifyEntity method setTextureLocation from entity: " + entity.getType() + ". ", e);
                }
            }
            if (builder.setRenderType != null) {
                var context = new ContextUtils.RendererModelContext(entity, getRenderer(), model);
                try {
                    var obj = entityJs$withRendererFallback("setRenderType", cir, cir.getReturnValue(), () -> builder.setRenderType.apply(context));
                    var returnValue = EntityJSHelperClass.convertToRenderType(obj, cir.getReturnValue());
                    if (returnValue != null) {
                        cir.setReturnValue(returnValue);
                        return;
                    }
                    if (obj != null) {
                        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value in setRenderType field from entity: " + entity.getType() + ". Must return either a resource location or a RenderType. Return null for the default texture logic.");
                    }
                } catch (Throwable e) {
                    EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in modifyEntity method setRenderType from entity: " + entity.getType() + ". ", e);
                }
            }
        }
    }

    @Unique
    private Object entityJs$builder(EntityType<?> entityType, LivingEntity entity) {
        if (entityJs$builders == null) {
            entityJs$builders = new IdentityHashMap<>();
        }
        return entityJs$builders.computeIfAbsent(entityType, key -> getOrCreate(key, entity).getBuilder());
    }
}
