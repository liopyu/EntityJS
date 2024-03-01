package net.liopyu.entityjs.client;

import net.liopyu.entityjs.EntityJSMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = EntityJSMod.MOD_ID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class ClientModHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean isJumpKeyPressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            long windowHandle = Minecraft.getInstance().getWindow().getWindow();
            if (GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
                isJumpKeyPressed = true;
            } else {
                isJumpKeyPressed = false;
            }
            //LOGGER.debug("Jump key pressed: " + isJumpKeyPressed);
        }
    }

    public static boolean isJumpKeyPressed() {
        return isJumpKeyPressed;
    }
}

