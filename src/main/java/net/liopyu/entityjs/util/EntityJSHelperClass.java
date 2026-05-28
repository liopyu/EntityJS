package net.liopyu.entityjs.util;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import dev.latvian.mods.kubejs.script.ConsoleJS;

import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import com.geckolib.animation.object.LoopType;

import java.util.*;
import java.util.function.Consumer;

public class EntityJSHelperClass {
    public static final Set<String> errorMessagesLogged = new HashSet<>();
    public static final Set<String> warningMessagesLogged = new HashSet<>();

    public static void logErrorMessageOnce(String errorMessage) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.error(errorMessage);
            errorMessagesLogged.add(errorMessage);
        }
    }

    public static void logWarningMessageOnce(String errorMessage) {
        if (!warningMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.warn(errorMessage);
            warningMessagesLogged.add(errorMessage);
        }
    }

    public static void logErrorMessageOnceCatchable(String errorMessage, Throwable e) {
        if (!errorMessagesLogged.contains(errorMessage)) {
            ConsoleJS.STARTUP.error(errorMessage, e);
            errorMessagesLogged.add(errorMessage);
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
            case "resourcelocation" -> convertToIdentifier(input);
            case "looptype" -> convertToLoopType(input);
            case "aabb" -> convertToBoundingBox(input);
            default -> input;
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static net.minecraft.client.renderer.rendertype.RenderType convertToRenderType(Object input, Object defaultValue) {
        return switch (input) {
            case null -> (net.minecraft.client.renderer.rendertype.RenderType) defaultValue;
            case net.minecraft.client.renderer.rendertype.RenderType renderType -> renderType;
            case String string -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(Identifier.parse(string));
            default -> null;
        };
    }

    private static AABB convertToBoundingBox(Object input) {
        if (input instanceof AABB) {
            return ((AABB) input);
        } else return null;
    }

    private static LoopType convertToLoopType(Object input) {
        if (input instanceof LoopType) {
            return (LoopType) input;
        } else if (input instanceof String) {
            String stringValue = ((String) input).toUpperCase();
            return switch (stringValue) {
                case "LOOP" -> LoopType.LOOP;
                case "PLAY_ONCE" -> LoopType.PLAY_ONCE;
                case "HOLD_ON_LAST_FRAME" -> LoopType.HOLD_ON_LAST_FRAME;
                default -> LoopType.DEFAULT;
            };
        }
        return LoopType.DEFAULT;
    }

    private static Identifier convertToIdentifier(Object input) {
        if (input instanceof Identifier) {
            return (Identifier) input;
        } else if (input instanceof String) {
            return Identifier.parse((String) input);
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

    public enum SpawnPlacementTypeEnum {
        NO_RESTRICTIONS,
        IN_WATER,
        IN_LAVA,
        ON_GROUND
    }

    public static SpawnPlacementType getSpawnPlacementType(SpawnPlacementTypeEnum typeEnum) {
        switch (typeEnum) {
            case IN_LAVA -> {
                return SpawnPlacementTypes.IN_LAVA;
            }
            case ON_GROUND -> {
                return SpawnPlacementTypes.ON_GROUND;
            }
            case IN_WATER -> {
                return SpawnPlacementTypes.IN_WATER;
            }
            case NO_RESTRICTIONS -> {
                return SpawnPlacementTypes.NO_RESTRICTIONS;
            }
            default -> {
                return SpawnPlacementTypes.NO_RESTRICTIONS;
            }
        }
    }

    public static boolean isLegacyKubeJS() {
        String v = ModList.get().getModContainerById("kubejs")
                .map(c -> c.getModInfo().getVersion().toString())
                .orElse("0.0.0");
        return v.startsWith("2101.7.1");
    }


}
