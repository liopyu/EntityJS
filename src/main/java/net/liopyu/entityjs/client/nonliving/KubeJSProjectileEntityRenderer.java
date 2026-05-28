package net.liopyu.entityjs.client.nonliving;

import net.minecraft.client.renderer.rendertype.RenderTypes;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.entities.nonliving.entityjs.IProjectileEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

@OnlyIn(Dist.CLIENT)
public class KubeJSProjectileEntityRenderer<T extends Entity & IProjectileEntityJS> extends EntityRenderer<T> {

    private final ProjectileEntityBuilder<T> builder;


    public KubeJSProjectileEntityRenderer(EntityRendererProvider.Context renderManager, ProjectileEntityBuilder<T> builder) {
        super(renderManager);
        this.builder = builder;
    }

    public RenderType getRenderType(T entity) {
        if (builder.renderTypeFunction != null) {
            try {
                return builder.renderTypeFunction.apply(entity);
            } catch (RuntimeException e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in renderTypeFunction. Defaulting to RenderTypes.entityCutout()", e);
            }
        }
        return RenderTypes.entityCutout(this.getTextureLocation(entity));
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (builder.render != null) {
            var context = new ContextUtils.NLRenderContext<>(pEntity, pEntityYaw, pPartialTick, pMatrixStack, pBuffer, pPackedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + pEntity.getType() + "builder for field: render.");
        }
        pMatrixStack.pushPose();
        if (builder.pX != null && builder.pY != null && builder.pZ != null) {
            float pX = builder.pX;
            float pY = builder.pY;
            float pZ = builder.pZ;
            pMatrixStack.scale(pX, pY, pZ);
        } else {
            pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        }
        pMatrixStack.pushPose();

        pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        PoseStack.Pose posestack$pose = pMatrixStack.last();
        VertexConsumer vertexconsumer = pBuffer.getBuffer(this.getRenderType(pEntity));
        vertex(vertexconsumer, posestack$pose, pPackedLight, 0.0F, 0, 0, 1);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 1.0F, 0, 1, 1);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 1.0F, 1, 1, 0);
        vertex(vertexconsumer, posestack$pose, pPackedLight, 0.0F, 1, 0, 0);

        pMatrixStack.popPose();
        pMatrixStack.popPose();

        super.render(pEntity, pEntityYaw, pPartialTick, pMatrixStack, pBuffer, pPackedLight);

    }


    public void vertex(VertexConsumer p_254095_, PoseStack.Pose p_324420_, int p_253829_, float p_253995_, int p_254031_, int p_253641_, int p_254243_) {
        try {
            if (builder.vX != null && builder.vY != null && builder.vZ != null) {
                float vX = builder.vX;
                float vY = builder.vY;
                float vZ = builder.vZ;
                p_254095_.addVertex(p_324420_, p_253995_ + vX, (float) p_254031_ + vY, 0.0F + vZ)
                        .setColor(-1)
                        .setUv((float) p_253641_, (float) p_254243_)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(p_253829_)
                        .setNormal(p_324420_, 0.0F, 1.0F, 0.0F);
            } else {
                p_254095_.addVertex(p_324420_, p_253995_ - 0.5F, (float) p_254031_ - 0.25F, 0.0F)
                        .setColor(255, 255, 255, 255)
                        .setUv((float) p_253641_, (float) p_254243_)
                        .setOverlay(OverlayTexture.NO_OVERLAY)
                        .setLight(p_253829_)
                        .setNormal(p_324420_, 0.0F, 1.0F, 0.0F);
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("", e);
        }
    }


    @Override
    public Identifier getTextureLocation(T entity) {
        if (builder != null && builder.textureLocation != null) {
            try {
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.textureLocation.apply(entity), "resourcelocation");
                if (obj != null) return (Identifier) obj;
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for textureLocation: " + obj + ". Must be a Identifier. Defaulting to super method: " + getDynamicTextureLocation());
            } catch (Exception e) {
                EntityJSHelperClass.logErrorMessageOnceCatchable("", e);
            }
        }
        return getDynamicTextureLocation();
    }


    private Identifier getDynamicTextureLocation() {
        return Identifier.parse(builder.id.getNamespace() + ":textures/entity/projectiles/" + builder.id.getPath() + ".png");
    }

}

