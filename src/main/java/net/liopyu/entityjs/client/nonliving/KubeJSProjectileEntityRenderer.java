package net.liopyu.entityjs.client.nonliving;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.liopyu.entityjs.builders.nonliving.ProjectileEntityBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KubeJSProjectileEntityRenderer<T extends ThrowableItemProjectile & IAnimatableJSNL> extends EntityRenderer<T> {

    private final ProjectileEntityBuilder<T> builder;
    public static RenderType RENDER_TYPE;


    public KubeJSProjectileEntityRenderer(EntityRendererProvider.Context renderManager, ProjectileEntityBuilder<T> builder) {
        super(renderManager);
        this.builder = builder;
        RENDER_TYPE = RenderType.entityCutoutNoCull(getDynamicTextureLocation());
    }

    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        if (builder.renderScale(builder.pX, builder.pY, builder.pZ).pX != null && builder.renderScale(builder.pX, builder.pY, builder.pZ).pY != null && builder.renderScale(builder.pX, builder.pY, builder.pZ).pZ != null) {
            float pX = builder.renderScale(builder.pX, builder.pY, builder.pZ).pX;
            float pY = builder.renderScale(builder.pX, builder.pY, builder.pZ).pY;
            float pZ = builder.renderScale(builder.pX, builder.pY, builder.pZ).pZ;
            pMatrixStack.scale(pX, pY, pZ);

        } else {
            pMatrixStack.scale(2.0F, 2.0F, 2.0F);
        }
        pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        pMatrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose $$6 = pMatrixStack.last();
        Matrix4f $$7 = $$6.pose();
        Matrix3f $$8 = $$6.normal();
        VertexConsumer $$9 = pBuffer.getBuffer(RENDER_TYPE);
        vertex($$9, $$7, $$8, pPackedLight, 0.0F, 0, 0, 1);
        vertex($$9, $$7, $$8, pPackedLight, 1.0F, 0, 1, 1);
        vertex($$9, $$7, $$8, pPackedLight, 1.0F, 1, 1, 0);
        vertex($$9, $$7, $$8, pPackedLight, 0.0F, 1, 0, 0);
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTick, pMatrixStack, pBuffer, pPackedLight);

    }


    public void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, Matrix3f p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        if (builder.renderOffset(builder.vX, builder.vY, builder.vZ).vX != null && builder.renderOffset(builder.vX, builder.vY, builder.vZ).vY != null && builder.renderOffset(builder.vX, builder.vY, builder.vZ).vZ != null) {
            float vX = builder.renderOffset(builder.vX, builder.vY, builder.vZ).vX;
            float vY = builder.renderOffset(builder.vX, builder.vY, builder.vZ).vY;
            float vZ = builder.renderOffset(builder.vX, builder.vY, builder.vZ).vZ;
            p_114090_.vertex(p_114091_, p_114094_ + vX, p_114095_ + vY, p_114096_ + vZ)
                    .color(255, 255, 255, 255)
                    .uv((float) p_114096_, (float) p_114097_)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(p_114093_)
                    .normal(p_114092_, 0.0F, 1.0F, 0.0F)
                    .endVertex();
        } else p_114090_.vertex(p_114091_, p_114094_, p_114095_, -0.5F) // Position
                .color(255, 255, 255, 255) // Color (white)
                .uv((float) p_114096_, (float) p_114097_) // Texture coordinates
                .overlayCoords(OverlayTexture.NO_OVERLAY) // Overlay coordinates
                .uv2(p_114093_) // UV2 coordinates
                .normal(p_114092_, 0.0F, 1.0F, 0.0F) // Normal vector
                .endVertex(); // Finish defining the vertex
    }


    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureLocation.apply(entity);
    }


    private ResourceLocation getDynamicTextureLocation() {
        return new ResourceLocation(builder.id.getNamespace() + ":textures/entity/projectiles/" + builder.id.getPath() + ".png");
    }

}

