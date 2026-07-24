package net.liopyu.entityjs.client.nonliving;

import com.mojang.blaze3d.vertex.PoseStack;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.client.nonliving.model.CustomNonLivingGeoModelJS;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.liopyu.entityjs.entities.nonliving.entityjs.WrappedNonLivingAnimatableEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import org.jetbrains.annotations.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public class CustomKubeJSNonLivingEntityRenderer<T extends Entity & IAnimatableJSCustom> extends GeoEntityRenderer<T> {
    private final CustomEntityJSBuilder builder;
    private final Map<Entity, WrappedNonLivingAnimatableEntity> wrapperCache = new WeakHashMap<>();

    public CustomKubeJSNonLivingEntityRenderer(EntityRendererProvider.Context renderManager, CustomEntityJSBuilder builder) {
        super(renderManager, new CustomNonLivingGeoModelJS<>(builder));
        this.builder = builder;
        this.scaleHeight = builder.scaleHeight;
        this.scaleWidth = builder.scaleWidth;
    }

    public String entityName() {
        return this.animatable.getType().toString();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return (ResourceLocation) builder.textureResource.apply(unwrap(entity));
    }

    @Override
    public RenderType getRenderType(T animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        try {
            if (builder.renderTypeFunction != null) {
                return EntityJSHelperClass.convertToRenderType(builder.renderTypeFunction.apply(unwrap(animatable)), defaultRenderType(texture));
            }
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error in " + entityName() + "builder for field: renderTypeFunction.", e);
        }
        return defaultRenderType(texture);
    }

    private RenderType defaultRenderType(ResourceLocation texture) {
        return switch (builder.renderType) {
            case SOLID -> RenderType.entitySolid(texture);
            case CUTOUT -> RenderType.entityCutout(texture);
            case TRANSLUCENT -> RenderType.entityTranslucent(texture);
        };
    }

    @Override
    public void render(T animatable, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        T renderEntity = ensureIAnimatableJS(animatable);
        Entity originalEntity = unwrap(renderEntity);
        if (builder.render != null) {
            final ContextUtils.NLRenderContext<Entity> context = new ContextUtils.NLRenderContext<>(originalEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            EntityJSHelperClass.consumerCallback(builder.render, context, "[EntityJS]: Error in " + originalEntity.getType() + "builder for field: render.");
        }
        super.render(renderEntity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private Entity unwrap(Entity entity) {
        if (entity instanceof WrappedNonLivingAnimatableEntity wrappedEntity) {
            return wrappedEntity.getOriginalEntity();
        }
        return entity;
    }

    private T ensureIAnimatableJS(Entity entity) {
        if (entity instanceof IAnimatableJSCustom animatableJS) {
            return (T) animatableJS;
        }
        return (T) wrapperCache.computeIfAbsent(entity, key -> new WrappedNonLivingAnimatableEntity(key, builder)).syncFromOriginal();
    }
}
