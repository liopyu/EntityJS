package net.liopyu.entityjs.client.living.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.client.living.KubeJSEntityRenderer;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.liolib.cache.object.BakedGeoModel;
import net.liopyu.liolib.core.animatable.GeoAnimatable;
import net.liopyu.liolib.renderer.GeoRenderer;
import net.liopyu.liolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public class GeoLayerJS<T extends LivingEntity & IAnimatableJS> extends GeoRenderLayer<T> {
    public T entity;
    public final GeoLayerJSBuilder<T> geoBuilder;
    public final KubeJSEntityRenderer<T> renderer;
    public final BaseLivingEntityBuilder<T> builder;

    public GeoLayerJS(KubeJSEntityRenderer<T> entityRendererIn, GeoLayerJSBuilder<T> geoBuilder, BaseLivingEntityBuilder<T> builder) {
        super(entityRendererIn);
        this.geoBuilder = geoBuilder;
        this.renderer = entityRendererIn;
        this.builder = builder;
        //this.entity = entityRendererIn.getAnimatable();
    }

    public ResourceLocation getLocation(T object) {
        if (geoBuilder.texture != null) {
            return geoBuilder.texture;
        }
        return new ResourceLocation(KubeJS.MOD_ID, "textures/entity/wyrm.png");
    }

    //new ResourceLocation(KubeJS.MOD_ID, "textures/entity/wyrm.png");
    //new ResourceLocation(KubeJS.MOD_ID, "textures/entity/sasuke.png");
    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTicks,
                       int packedLightIn, int packedOverlay) {
        RenderType renderLayer = RenderType.entityCutoutNoCull(getLocation(animatable));
        getRenderer().reRender(getDefaultBakedModel(animatable), poseStack, bufferSource, animatable, renderLayer, bufferSource.getBuffer(renderLayer), partialTicks, packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
