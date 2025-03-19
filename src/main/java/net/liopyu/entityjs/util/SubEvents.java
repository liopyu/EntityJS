package net.liopyu.entityjs.util;

import dev.architectury.platform.Platform;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.BaseNonAnimatableEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.*;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = EntityJSMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SubEvents {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        if (Platform.isModLoaded("cgm") && Platform.isModLoaded("framework")) {
            event.enqueueWork(() -> registerCGMEntities());
        }
        event.enqueueWork(() -> {
            for (BaseNonAnimatableEntityBuilder<?> b : BaseNonAnimatableEntityBuilder.thisList) {
                if (b instanceof ArrowEntityJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = ForgeRegistries.ITEMS.getValue(builder.item.id);
                        DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
                            @Override
                            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                                var entity = new ArrowEntityJS(builder, builder.get(), level);
                                entity.setPos(new Vec3(position.x(), position.y(), position.z()));
                                entity.pickup = AbstractArrow.Pickup.ALLOWED;
                                entity.setPickUpItem(itemStack.copyWithCount(1));
                                return entity;
                            }
                        });
                    }
                } else if (b instanceof ProjectileEntityJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = ForgeRegistries.ITEMS.getValue(builder.item.id);
                        DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
                            @Override
                            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                                var entity = new ProjectileEntityJS(builder, builder.get(), level);
                                entity.setPos(new Vec3(position.x(), position.y(), position.z()));
                                return entity;
                            }
                        });
                    }
                }
            }
            for (BaseEntityBuilder<?> b : BaseEntityBuilder.thisList) {
                if (b instanceof TridentJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = ForgeRegistries.ITEMS.getValue(builder.item.id);
                        DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
                            @Override
                            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                                var entity = new TridentEntityJS(builder, builder.get(), level);
                                entity.setPos(new Vec3(position.x(), position.y(), position.z()));
                                entity.pickup = AbstractArrow.Pickup.ALLOWED;
                                entity.setTridentItem(itemStack.copy());
                                return entity;
                            }
                        });
                    }
                } else if (b instanceof ProjectileAnimatableJSBuilder builder) {
                    if (!builder.noItem && builder.canShootFromDispenser) {
                        var item = ForgeRegistries.ITEMS.getValue(builder.item.id);
                        DispenserBlock.registerBehavior(item, new AbstractProjectileDispenseBehavior() {
                            @Override
                            protected Projectile getProjectile(Level level, Position position, ItemStack itemStack) {
                                var entity = new ProjectileAnimatableJS(builder, builder.get(), level);
                                entity.setPos(new Vec3(position.x(), position.y(), position.z()));
                                return entity;
                            }
                        });
                    }
                }
            }
        });
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
