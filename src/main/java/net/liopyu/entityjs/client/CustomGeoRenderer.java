package net.liopyu.entityjs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;
import software.bernie.geckolib3.util.RenderUtils;

public class CustomGeoRenderer<T> implements IGeoRenderer<T> {

    @Override
    public void createVerticesOfQuad(GeoQuad quad, Matrix4f poseState, Vector3f normal, VertexConsumer buffer,
                                     int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (quad != null && buffer != null) {
            for (GeoVertex vertex : quad.vertices) {
                if (vertex != null && vertex.position != null) {
                    Vector4f vector4f = new Vector4f(vertex.position.x(), vertex.position.y(), vertex.position.z(), 1);

                    vector4f.transform(poseState);
                    buffer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertex.textureU,
                            vertex.textureV, packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
                }
            }
        }
    }


    @Override
    public MultiBufferSource getCurrentRTB() {
        return null;
    }

    @Override
    public GeoModelProvider getGeoModelProvider() {
        return null;
    }

    @Override
    public ResourceLocation getTextureLocation(T animatable) {
        return null;
    }

    @Override
    public void renderRecursively(GeoBone bone, PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                  int packedOverlay, float red, float green, float blue, float alpha) {
        if (bone.isHidden()) return;

        for (GeoCube cube : bone.childCubes) {
            if (!bone.cubesAreHidden()) {
                poseStack.pushPose();
                RenderUtils.prepMatrixForBone(poseStack, bone);
                renderCube(cube, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
                poseStack.popPose();
            }
        }

        for (GeoBone childBone : bone.childBones) {
            renderRecursively(childBone, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }

}
