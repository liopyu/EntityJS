package net.liopyu.entityjs.client.living.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.liolib.cache.object.BakedGeoModel;
import net.liopyu.liolib.cache.object.GeoBone;
import net.liopyu.liolib.core.animatable.GeoAnimatable;
import net.liopyu.liolib.model.GeoModel;
import net.liopyu.liolib.renderer.GeoRenderer;
import net.liopyu.liolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomGeoRenderLayer<T extends LivingEntity & IAnimatableJS> extends GeoRenderLayer<T> {

    /*
        public final BaseLivingEntityBuilder<T> parent;
    */
    public transient Function<T, ResourceLocation> textureResource;
    public final KubeJSEntityRenderer<T> renderer;
/*
    public final CustomGeoRenderLayer.Builder<T> renderBuilder;
*/

    public CustomGeoRenderLayer(KubeJSEntityRenderer<T> renderer) {
        super(renderer);
        /*this.parent = parent;
        this.renderBuilder = renderBuilder;*/
        this.renderer = renderer;
        textureResource = t -> new ResourceLocation("kubejs:textures/entity/sasuke.png");
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.renderer.getGeoModel();
    }

    /*@Override
    protected ResourceLocation getTextureResource(T animatable) {
        return renderBuilder.textureResource.apply(animatable);
    }*/


    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static class Builder<T extends LivingEntity & IAnimatableJS> {
        private final BaseLivingEntityBuilder<T> parent;
        public transient Function<T, ResourceLocation> textureResource;
        public final KubeJSEntityRenderer<T> renderer;

        public Builder(KubeJSEntityRenderer<T> renderer, BaseLivingEntityBuilder<T> parent) {
            this.parent = parent;
            this.renderer = renderer;
        }

        public Builder<T> textureResource(Function<T, ResourceLocation> textureResource) {
            this.textureResource = textureResource;
            return this;
        }

       /* public CustomGeoRenderLayer<T> build() {
            return new CustomGeoRenderLayer<>(renderer, parent, this);
        }*/
    }
}

