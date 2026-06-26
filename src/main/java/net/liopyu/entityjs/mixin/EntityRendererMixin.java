package net.liopyu.entityjs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.client.utils.VertexModifier;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.util.overrides.CallbackUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.IdentityHashMap;
import java.util.Map;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRendererMixin {
    @Unique
    private Map<EntityType<?>, Object> entityJs$builders;

    private EntityModel model;

    @Unique
    private EntityRenderDispatcher entityRenderDispatcher = (EntityRenderDispatcher) (Object) this;

    @Unique
    private Object entityJs$withRendererFallback(String fieldName, CallbackInfo ci, Object fallback, java.util.function.Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, ci, () -> fallback, callback);
    }

    @Inject(method = "render", at = @At("HEAD"), remap = true, cancellable = true)
    public <E extends Entity> void render(E entity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (entity instanceof LivingEntity) return;
        if (EntityJSUtils.handlesOwnEntityJsCallbacks(entity)) return;

        var entityType = entity.getType();
        Object entityJs$builder = entityJs$builder(entityType, entity);
        if (entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setTextureLocation == null && builder.setRenderType == null) {
                return;
            }
        }

        /*if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(eventJS);
        }*/
        if (entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setTextureLocation != null && builder.setRenderType != null) {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: You may not set both setRenderType and setTextureLocation at the same time for entity: " + entity.getType() + ".");
                return;
            }
            if (builder.setTextureLocation != null) {
                var context = new ContextUtils.RendererModelContext(entity, entityRenderDispatcher.getRenderer(entity), model);
                try {
                    var obj = entityJs$withRendererFallback("setTextureLocation", ci, null, () -> builder.setTextureLocation.apply(context));
                    var resourcelocation = EntityJSHelperClass.convertObjectToDesired(obj, "resourcelocation");
                    if (resourcelocation != null) {
                        var textureLocation = (ResourceLocation) resourcelocation;
                        entityjs$render(entityRenderDispatcher, entity, pX, pY, pZ, pRotationYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight, ci, textureLocation);
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
                    var obj = entityJs$withRendererFallback("setRenderType", ci, null, () -> builder.setRenderType.apply(context));
                    var returnValue = EntityJSHelperClass.convertToRenderType(obj, null);
                    if (returnValue != null) {
                        entityjs$render(entityRenderDispatcher, entity, pX, pY, pZ, pRotationYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight, ci, returnValue);
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
    private Object entityJs$builder(EntityType<?> entityType, Entity entity) {
        if (entityJs$builders == null) {
            entityJs$builders = new IdentityHashMap<>();
        }
        return entityJs$builders.computeIfAbsent(entityType, key -> getOrCreate(key, entity).getBuilder());
    }

    public <E extends Entity> void entityjs$render(EntityRenderDispatcher entityRenderDispatcher, E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci, Object locationOrRenderType) {
        EntityRenderer<? super E> entityrenderer = entityRenderDispatcher.getRenderer(pEntity);

        try {
            Vec3 vec3 = entityrenderer.getRenderOffset(pEntity, pPartialTicks);
            double d2 = pX + vec3.x();
            double d3 = pY + vec3.y();
            double d0 = pZ + vec3.z();

            pPoseStack.pushPose();
            pPoseStack.translate(d2, d3, d0);
            MultiBufferSource interceptedBuffer = pBuffer;
            if (locationOrRenderType instanceof RenderType type) {
                interceptedBuffer = renderType -> new VertexModifier(pBuffer.getBuffer(type));
            } else if (locationOrRenderType instanceof ResourceLocation location) {
                interceptedBuffer = renderType -> new VertexModifier(pBuffer.getBuffer(RenderType.entityCutout(location)));
            }

            entityrenderer.render(pEntity, pRotationYaw, pPartialTicks, pPoseStack, interceptedBuffer, pPackedLight);

            if (pEntity.displayFireAnimation()) {
                entityRenderDispatcher.renderFlame(pPoseStack, pBuffer, pEntity, Mth.rotationAroundAxis(Mth.Y_AXIS, entityRenderDispatcher.cameraOrientation, new Quaternionf()));
            }

            pPoseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            if ((Boolean) entityRenderDispatcher.options.entityShadows().get() &&
                    entityRenderDispatcher.shouldRenderShadow &&
                    entityrenderer.shadowRadius > 0.0F &&
                    !pEntity.isInvisible()) {

                double d1 = entityRenderDispatcher.distanceToSqr(pEntity.getX(), pEntity.getY(), pEntity.getZ());
                float f = (float) (((double) 1.0F - d1 / 256.0F) * entityrenderer.shadowStrength);

                if (f > 0.0F) {
                    EntityRenderDispatcher.renderShadow(pPoseStack, pBuffer, pEntity, f, pPartialTicks, entityRenderDispatcher.level, Math.min(entityrenderer.shadowRadius, 32.0F));
                }
            }

            if (entityRenderDispatcher.renderHitBoxes && !pEntity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                EntityRenderDispatcher.renderHitbox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), pEntity, pPartialTicks, 1.0F, 1.0F, 1.0F);
            }

            pPoseStack.popPose();

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            pEntity.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
            crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(entityRenderDispatcher.level, pX, pY, pZ));
            crashreportcategory1.setDetail("Rotation", pRotationYaw);
            crashreportcategory1.setDetail("Delta", pPartialTicks);
            throw new ReportedException(crashreport);
        }
        ci.cancel();
    }
}

