package net.liopyu.entityjs.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.liolib.core.animation.Animation;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntityJSHelperClass {
    public static final Set<String> errorMessagesLogged = new HashSet<>();
    public static final Set<String> warningMessagesLogged = new HashSet<>();

    public static void logErrorMessageOnce(String errorMessage) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.warn(errorMessage);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logErrorMessageOnceCatchable(String errorMessage, Throwable e) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.warn(errorMessage, e);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessageOnce(String errorMessage) {
        if (!warningMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.warn(errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static <T> boolean consumerCallback(Consumer<T> consumer, T value, String errorMessage) {
        try {
            consumer.accept(value);
        } catch (Throwable e) {
            logErrorMessageOnceCatchable(errorMessage, e);
            return false;
        }
        return true;
    }


    public static Object convertObjectToDesired(Object input, String outputType) {
        return switch (outputType.toLowerCase()) {
            case "integer" -> convertToInteger(input);
            case "double" -> convertToDouble(input);
            case "float" -> convertToFloat(input);
            case "boolean" -> convertToBoolean(input);
            case "interactionresult" -> convertToInteractionResult(input);
            case "resourcelocation" -> convertToResourceLocation(input);
            case "looptype" -> convertToLoopType(input);
            default -> input;
        };
    }

    public static RenderType convertToRenderType(Object input, Object defaultValue) {
        if (input == null) {
            return (RenderType) defaultValue;
        } else if (input instanceof RenderType) {
            return (RenderType) input;
        } else if (input instanceof String string) {
            return RenderType.entityCutout(new ResourceLocation(string));
        }
        return null;
    }

    private static Animation.LoopType convertToLoopType(Object input) {
        if (input instanceof Animation.LoopType) {
            return (Animation.LoopType) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toUpperCase();
            return switch (stringValue) {
                case "LOOP" -> Animation.LoopType.LOOP;
                case "PLAY_ONCE" -> Animation.LoopType.PLAY_ONCE;
                case "HOLD_ON_LAST_FRAME" -> Animation.LoopType.HOLD_ON_LAST_FRAME;
                default -> Animation.LoopType.DEFAULT;
            };
        }
        return Animation.LoopType.DEFAULT;
    }

    private static ResourceLocation convertToResourceLocation(Object input) {
        if (input instanceof ResourceLocation) {
            return (ResourceLocation) input;
        } else if (input instanceof String) {
            return new ResourceLocation((String) input);
        }
        return null;
    }

    private static InteractionResult convertToInteractionResult(Object input) {
        if (input instanceof InteractionResult) {
            return (InteractionResult) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toLowerCase();
            switch (stringValue) {
                case "success":
                    return InteractionResult.SUCCESS;
                case "consume":
                    return InteractionResult.CONSUME;
                case "pass":
                    return InteractionResult.PASS;
                case "fail":
                    return InteractionResult.FAIL;
                case "consume_partial":
                    return InteractionResult.CONSUME_PARTIAL;
            }
        }
        return null;
    }

    private static Boolean convertToBoolean(Object input) {
        if (input instanceof Boolean) {
            return (Boolean) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toLowerCase();
            if ("true".equals(stringValue)) {
                return true;
            } else if ("false".equals(stringValue)) {
                return false;
            }
        }
        return null;
    }


    private static Integer convertToInteger(Object input) {
        if (input instanceof Integer) {
            return (Integer) input;
        } else if (input instanceof Double || input instanceof Float) {
            return ((Number) input).intValue();
        } else {
            return null;
        }
    }

    private static Double convertToDouble(Object input) {
        if (input instanceof Double) {
            return (Double) input;
        } else if (input instanceof Integer || input instanceof Float) {
            return ((Number) input).doubleValue();
        } else {
            return null;
        }
    }

    private static Float convertToFloat(Object input) {
        if (input instanceof Float) {
            return (Float) input;
        } else if (input instanceof Integer || input instanceof Double) {
            return ((Number) input).floatValue();
        } else {
            return null;
        }
    }

    public static class EntityMovementTracker {
        private double prevX;
        private double prevY;
        private double prevZ;

        public EntityMovementTracker() {
            prevX = 0;
            prevY = 0;
            prevZ = 0;
        }

        public boolean isMoving(Entity entity) {
            double currentX = entity.getX();
            double currentY = entity.getY();
            double currentZ = entity.getZ();

            boolean moving = currentX != prevX || currentY != prevY || currentZ != prevZ;

            // Update previous position
            prevX = currentX;
            prevY = currentY;
            prevZ = currentZ;

            return moving;
        }
    }

    public static void removeAllGoals(Predicate<Goal> p_262575_, GoalSelector goalSelector) {
        goalSelector.getAvailableGoals().removeIf((p_262564_) -> p_262575_.test(p_262564_.getGoal()));
    }

    public static <E extends Entity> void render(EntityRenderDispatcher entityRenderDispatcher, E pEntity, double pX, double pY, double pZ, float pRotationYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci, Object locationOrRenderType) {
        EntityRenderer<? super E> entityrenderer = entityRenderDispatcher.getRenderer(pEntity);

        try {
            Vec3 vec3 = entityrenderer.getRenderOffset(pEntity, pPartialTicks);
            double d2 = pX + vec3.x();
            double d3 = pY + vec3.y();
            double d0 = pZ + vec3.z();

            pPoseStack.pushPose();
            pPoseStack.translate(d2, d3, d0);
            MultiBufferSource interceptedBuffer = pBuffer;
            if (locationOrRenderType instanceof RenderType type) {
                interceptedBuffer = renderType -> new VertexModifier(pBuffer.getBuffer(type));
            } else if (locationOrRenderType instanceof ResourceLocation location) {
                interceptedBuffer = renderType -> new VertexModifier(pBuffer.getBuffer(RenderType.entityCutout(location)));
            }

            entityrenderer.render(pEntity, pRotationYaw, pPartialTicks, pPoseStack, interceptedBuffer, pPackedLight);

            if (pEntity.displayFireAnimation()) {
                entityRenderDispatcher.renderFlame(pPoseStack, pBuffer, pEntity);
            }

            pPoseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            if ((Boolean) entityRenderDispatcher.options.entityShadows().get() &&
                    entityRenderDispatcher.shouldRenderShadow &&
                    entityrenderer.shadowRadius > 0.0F &&
                    !pEntity.isInvisible()) {

                double d1 = entityRenderDispatcher.distanceToSqr(pEntity.getX(), pEntity.getY(), pEntity.getZ());
                float f = (float) (((double) 1.0F - d1 / 256.0F) * entityrenderer.shadowStrength);

                if (f > 0.0F) {
                    EntityRenderDispatcher.renderShadow(pPoseStack, pBuffer, pEntity, f, pPartialTicks, entityRenderDispatcher.level, Math.min(entityrenderer.shadowRadius, 32.0F));
                }
            }

            if (entityRenderDispatcher.renderHitBoxes && !pEntity.isInvisible() && !Minecraft.getInstance().showOnlyReducedInfo()) {
                EntityRenderDispatcher.renderHitbox(pPoseStack, pBuffer.getBuffer(RenderType.lines()), pEntity, pPartialTicks);
            }

            pPoseStack.popPose();

        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            pEntity.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", entityrenderer);
            crashreportcategory1.setDetail("Location", CrashReportCategory.formatLocation(entityRenderDispatcher.level, pX, pY, pZ));
            crashreportcategory1.setDetail("Rotation", pRotationYaw);
            crashreportcategory1.setDetail("Delta", pPartialTicks);
            throw new ReportedException(crashreport);
        }
        ci.cancel();
    }


    private static class VertexModifier implements VertexConsumer {
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
}
