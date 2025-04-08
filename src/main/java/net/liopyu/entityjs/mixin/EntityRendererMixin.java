package net.liopyu.entityjs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {
    @Unique
    private Object entityJs$builder;

    private EntityModel model;

    @Unique
    private EntityRenderDispatcher entityRenderDispatcher = (EntityRenderDispatcher) (Object) this;

    @Inject(method = "render", at = @At("HEAD"), remap = true, cancellable = true)
    public <E extends Entity> void render(E entity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity) return;
        var entityType = entity.getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entity.getClass());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }
        if (entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setTextureLocation != null && builder.setRenderType != null) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: You may not set both setRenderType and setTextureLocation at the same time for entity: " + entity.getType() + ".");
                return;
            }
            if (builder.setTextureLocation != null) {
                var context = new ContextUtils.RendererModelContext(entity, entityRenderDispatcher.getRenderer(entity), model);
                try {
                    var obj = builder.setTextureLocation.apply(context);
                    var resourcelocation = EntityJSHelperClass.convertObjectToDesired(obj, "resourcelocation");
                    if (resourcelocation != null) {
                        var textureLocation = (ResourceLocation) resourcelocation;
                        EntityJSHelperClass.render(entityRenderDispatcher, entity, pX, pY, pZ, pRotationYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight, ci, textureLocation);
                        return;
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
                var context = new ContextUtils.RendererModelContext(entity, entityRenderDispatcher.getRenderer(entity), model);
                try {
                    var obj = builder.setRenderType.apply(context);
                    var returnValue = EntityJSHelperClass.convertToRenderType(obj, null);
                    if (returnValue != null) {
                        EntityJSHelperClass.render(entityRenderDispatcher, entity, pX, pY, pZ, pRotationYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight, ci, returnValue);
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

}

