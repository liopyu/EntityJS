package net.liopyu.entityjs.client;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.client.model.EntityModelJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class KubeJSEntityRenderer<T extends Entity & IAnimatableJS> extends EntityRenderer<T> implements IGeoRenderer<T> {

    private final AnimatedGeoModel<T> modelProvider;

    public static <E extends Entity & IAnimatableJS> KubeJSEntityRenderer<?> create(EntityRendererProvider.Context renderManager, BaseEntityBuilder<?> builder) {
        return new KubeJSEntityRenderer<>(renderManager, new EntityModelJS<>(builder));
    }

    public KubeJSEntityRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<T> modelProvider) {
        super(renderManager);
        this.modelProvider = modelProvider;
    }

    @Override
    public MultiBufferSource getCurrentRTB() {
        return null;
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return modelProvider;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return entity.getTextureLocation();
    }
}
