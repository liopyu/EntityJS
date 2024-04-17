package net.liopyu.entityjs.util;

import dev.architectury.platform.Platform;
import net.liopyu.entityjs.EntityJSMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = EntityJSMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SubEvents {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (Platform.isModLoaded("cgm") && Platform.isModLoaded("framework")) {
            event.enqueueWork(() -> registerCGMEntities());
        }
    }

    private static void registerCGMEntities() {
        for (net.liopyu.entityjs.item.CGMProjectileItemBuilder itemBuilder : net.liopyu.entityjs.item.CGMProjectileItemBuilder.thisList) {
            // Only register entities if the "cgm" mod is loaded
            com.mrcrayfish.guns.common.ProjectileManager.getInstance().registerFactory(
                    itemBuilder.get(),
                    (worldIn, entity, weapon, item1, modifiedGun) -> (com.mrcrayfish.guns.entity.ProjectileEntity) newCGMProjectileEntity(itemBuilder, worldIn, entity, weapon, item1, modifiedGun)
            );
        }
    }

    private static Object newCGMProjectileEntity(net.liopyu.entityjs.item.CGMProjectileItemBuilder itemBuilder, Level worldIn, LivingEntity entity, ItemStack weapon, com.mrcrayfish.guns.item.GunItem item1, com.mrcrayfish.guns.common.Gun modifiedGun) {
        // Construct and return CGMProjectileEntityJS instance
        return new net.liopyu.entityjs.entities.nonliving.modded.CGMProjectileEntityJS(
                itemBuilder.parent,
                itemBuilder.parent.get(),
                worldIn,
                entity,
                weapon,
                item1,
                modifiedGun
        );
    }
}
