package net.liopyu.entityjs;

import com.mojang.logging.LogUtils;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import org.slf4j.Logger;

public class EntityJSMod implements ModInitializer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "entityjs";

    @Override
    public void onInitialize() {
        LOGGER.info("Loading EntityJS-Liopyu");
        EventHandlers.init();
    }

    public static ResourceLocation identifier(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
