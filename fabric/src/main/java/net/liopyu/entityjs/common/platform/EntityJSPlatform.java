package net.liopyu.entityjs.common.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import dev.latvian.mods.rhino.mod.util.RemappingHelper;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.network.GeckoLibNetwork;
import software.bernie.geckolib.network.packet.AnimTriggerPacket;
import software.bernie.geckolib.network.packet.EntityAnimTriggerPacket;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class EntityJSPlatform {
    private EntityJSPlatform() {
    }

    public static boolean isClientEnvironment() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Nullable
    public static DynamicOverrideMethodMetadata dynamicOverrideMethodMetadata(Method method, String descriptor) {
        return FabricMethodNameAliasIndex.metadata(method, descriptor);
    }

    public static String runtimeClassName(String namedClassName) {
        String rhinoRuntimeName = RemappingHelper.getMinecraftRemapper().getUnmappedClass(namedClassName);
        if (!rhinoRuntimeName.isBlank()) {
            return rhinoRuntimeName;
        }
        return FabricLoader.getInstance().getMappingResolver().mapClassName("named", namedClassName);
    }

    public static String scriptClassName(Class<?> runtimeClass) {
        String mappedName = RemappingHelper.getMinecraftRemapper().getMappedClass(runtimeClass);
        if (mappedName != null && !mappedName.isBlank()) {
            return mappedName;
        }
        String canonicalName = runtimeClass.getCanonicalName();
        return canonicalName == null ? runtimeClass.getName() : canonicalName;
    }

    public static boolean isAnimalTameCancelled(TamableAnimal animal, Player player) {
        return false;
    }

    public static void onLivingJump(LivingEntity entity) {
    }

    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
        return stack.getItem().getFoodProperties();
    }

    public static boolean hasNativeEntityLifecycle() {
        return false;
    }

    public static InteractionHand getBowHoldingHand(LivingEntity entity) {
        return ProjectileUtil.getWeaponHoldingHand(entity, Items.BOW);
    }

    public static AbstractArrow customizeArrow(BowItem bow, AbstractArrow arrow) {
        return arrow;
    }

    public static AttributeSupplier.Builder applyAttributeBuilder(BaseLivingEntityBuilder<?> entityBuilder,
                                                                  AttributeSupplier.Builder attributes) {
        if (entityBuilder.attributes != null) {
            entityBuilder.attributes.accept(attributes);
        }
        return attributes;
    }

    public static boolean isCustomAnimatableClass(Class<? extends Entity> entityClass) {
        return IAnimatableJSCustom.class.isAssignableFrom(entityClass);
    }

    public static boolean shouldRiderSit(Entity vehicle) {
        return true;
    }

    public static void sendEntityAnimation(Entity entity, @Nullable String controllerName, String animationName) {
        GeckoLibNetwork.sendToTrackingEntityAndSelf(
                new EntityAnimTriggerPacket(entity.getId(), controllerName, animationName), entity);
    }

    public static void sendAnimation(Entity relatedEntity, String animatableClass, long instanceId,
                                     @Nullable String controllerName, String animationName) {
        GeckoLibNetwork.sendToTrackingEntityAndSelf(
                new AnimTriggerPacket(animatableClass, instanceId, controllerName, animationName), relatedEntity);
    }
}
