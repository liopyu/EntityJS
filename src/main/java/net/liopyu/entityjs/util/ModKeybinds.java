package net.liopyu.entityjs.util;

import net.minecraft.client.KeyMapping;

public class ModKeybinds {
    //public static final KeyMapping mount_jump;
    public static final KeyMapping mount_jump = new KeyMapping("key.mount_jump", 296, "key.categories.misc");
    /*public static void init() {
        // Minecraft instance is null during data gen
        if (Minecraft.getInstance() == null)
            return;

        Minecraft.getInstance().options.keyMappings = ArrayUtils.add(Minecraft.getInstance().options.keyMappings, mount_jump);
    }*///Call where needed with ModKeybinds.mount_jump.isDown()
}