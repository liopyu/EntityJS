package net.liopyu.entityjs.common.platform;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJSCustom;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.network.GeckoLibNetwork;
import software.bernie.geckolib.network.packet.AnimTriggerPacket;
import software.bernie.geckolib.network.packet.EntityAnimTriggerPacket;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class EntityJSPlatform {
    private EntityJSPlatform() {
    }

    public static boolean isClientEnvironment() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Nullable
    public static DynamicOverrideMethodMetadata dynamicOverrideMethodMetadata(Method method, String descriptor) {
        return MethodNameAliasIndex.metadata(method, descriptor);
    }

    public static String runtimeClassName(String namedClassName) {
        return namedClassName;
    }

    public static String scriptClassName(Class<?> runtimeClass) {
        String canonicalName = runtimeClass.getCanonicalName();
        return canonicalName == null ? runtimeClass.getName() : canonicalName;
    }

    public static boolean isAnimalTameCancelled(TamableAnimal animal, Player player) {
        return ForgeEventFactory.onAnimalTame(animal, player);
    }

    public static void onLivingJump(LivingEntity entity) {
        ForgeHooks.onLivingJump(entity);
    }

    @Nullable
    public static FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
        return stack.getFoodProperties(entity);
    }

    public static boolean hasNativeEntityLifecycle() {
        return true;
    }

    public static InteractionHand getBowHoldingHand(LivingEntity entity) {
        return ProjectileUtil.getWeaponHoldingHand(entity, item -> item instanceof BowItem);
    }

    public static AbstractArrow customizeArrow(BowItem bow, AbstractArrow arrow) {
        return bow.customArrow(arrow);
    }

    public static AttributeSupplier.Builder applyAttributeBuilder(BaseLivingEntityBuilder<?> entityBuilder,
                                                                  AttributeSupplier.Builder attributes) {
        return attributes;
    }

    public static boolean isCustomAnimatableClass(Class<? extends Entity> entityClass) {
        return IAnimatableJSCustom.class.isAssignableFrom(entityClass);
    }

    public static boolean shouldRiderSit(Entity vehicle) {
        return vehicle.shouldRiderSit();
    }

    public static void sendEntityAnimation(Entity entity, @Nullable String controllerName, String animationName) {
        GeckoLibNetwork.send(new EntityAnimTriggerPacket<>(entity.getId(), controllerName, animationName),
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
    }

    public static void sendAnimation(Entity relatedEntity, String animatableClass, long instanceId,
                                     @Nullable String controllerName, String animationName) {
        GeckoLibNetwork.send(new AnimTriggerPacket<>(animatableClass, instanceId, controllerName, animationName),
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> relatedEntity));
    }
}
