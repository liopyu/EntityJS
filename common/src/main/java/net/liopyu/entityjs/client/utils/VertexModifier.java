package net.liopyu.entityjs.client.utils;

public class VertexModifier implements com.mojang.blaze3d.vertex.VertexConsumer {
    private final com.mojang.blaze3d.vertex.VertexConsumer original;

    public VertexModifier(com.mojang.blaze3d.vertex.VertexConsumer original) {
        this.original = original;
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer vertex(double x, double y, double z) {
        return original.vertex(x, y, z);
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer color(int red, int green, int blue, int alpha) {
        return original.color(red, green, blue, alpha);
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer uv(float u, float v) {
        return original.uv(u, v);
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer overlayCoords(int u, int v) {
        return original.overlayCoords(u, v);
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer uv2(int u, int v) {
        return original.uv2(u, v);
    }

    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer normal(float x, float y, float z) {
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
