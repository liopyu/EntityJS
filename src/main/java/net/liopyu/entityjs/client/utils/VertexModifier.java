package net.liopyu.entityjs.client.utils;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class VertexModifier implements VertexConsumer {
    private final VertexConsumer original;

    public VertexModifier(VertexConsumer original) {
        this.original = original;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return original.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return original.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        float newU = u;
        float newV = v;
        return original.setUv(newU, newV);
    }

    @Override
    public VertexConsumer setUv1(int p_350815_, int p_350629_) {
        return original.setUv1(p_350815_, p_350629_);
    }


    @Override
    public VertexConsumer setOverlay(int p_350697_) {
        return original.setOverlay(p_350697_);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return original.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return original.setNormal(x, y, z);
    }


    @Override
    public VertexConsumer setColor(float red, float green, float blue, float alpha) {
        return original.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setColor(int color) {
        return original.setColor(color);
    }

    @Override
    public VertexConsumer setWhiteAlpha(int alpha) {
        return original.setWhiteAlpha(alpha);
    }

    @Override
    public VertexConsumer setLight(int light) {
        return original.setLight(light);
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay) {
        original.putBulkData(pose, quad, red, green, blue, alpha, light, overlay);
    }

    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] colorMultiplier, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean shaded) {
        original.putBulkData(pose, quad, colorMultiplier, red, green, blue, alpha, lights, overlay, shaded);
    }

    @Override
    public VertexConsumer addVertex(Vector3f vector) {
        return original.addVertex(vector);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, Vector3f vector) {
        return original.addVertex(pose, vector);
    }

    @Override
    public VertexConsumer addVertex(PoseStack.Pose pose, float x, float y, float z) {
        return original.addVertex(pose, x, y, z);
    }

    @Override
    public VertexConsumer addVertex(Matrix4f matrix, float x, float y, float z) {
        return original.addVertex(matrix, x, y, z);
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose pose, float x, float y, float z) {
        return original.setNormal(pose, x, y, z);
    }
}