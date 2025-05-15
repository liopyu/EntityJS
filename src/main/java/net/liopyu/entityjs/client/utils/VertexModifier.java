package net.liopyu.entityjs.client.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class VertexModifier implements VertexConsumer {
    private final VertexConsumer original;

    public VertexModifier(VertexConsumer original) {
        this.original = original;
    }

    @Override
    public VertexConsumer vertex(double v, double v1, double v2) {
        return original.vertex(v, v1, v2);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return original.color(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        float newU = u;
        float newV = v;
        return original.uv(newU, newV);
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        return original.overlayCoords(u, v);
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return original.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return original.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        original.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        original.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        original.unsetDefaultColor();
    }
}