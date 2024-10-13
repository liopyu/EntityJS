package net.liopyu.entityjs.client.nonliving;


import com.mojang.blaze3d.vertex.PoseStack;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.entities.nonliving.entityjs.IArrowEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;


public class KubeJSArrowEntityRenderer<T extends AbstractArrow & IArrowEntityJS> extends ArrowRenderer<T> {

    private final ArrowEntityBuilder<T> builder;

    public KubeJSArrowEntityRenderer(EntityRendererProvider.Context renderManager, ArrowEntityBuilder<T> builder) {
        super(renderManager);
        this.builder = builder;
    }


    @Override
    public void render(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (builder.render != null) {
            var context = new ContextUtils.NLRenderContext<>(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + pEntity.getType() + "builder for field: render.");
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureLocation.apply(entity);
    }
}

