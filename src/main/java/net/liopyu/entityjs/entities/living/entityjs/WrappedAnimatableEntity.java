package net.liopyu.entityjs.entities.living.entityjs;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * A dummy class that serves as a wrapper for dynamically created custom entities in the
 * {@link CustomEntityJSBuilder} system. This class is used to integrate GeckoLib's
 * animation system with custom entities while maintaining the original entity's behavior.
 *
 * <p>
 * The {@code WrappedAnimatableEntity} extends {@link LivingEntity} and implements
 * {@link IAnimatableJSCustom}, making it compatible with GeckoLib's animation framework.
 * It acts as a proxy for the original entity, allowing animation and rendering logic
 * to be applied without modifying the base entity's core functionality.
 * </p>
 *
 * <p>
 * This class is primarily used for entities registered via {@link CustomEntityJSBuilder},
 * ensuring they have access to GeckoLib's animation system while retaining their
 * native properties, AI, and interactions. It also allows dynamic modifications
 * without requiring each entity type to be manually subclassed.
 * </p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Retains all behaviors and attributes of the original entity.</li>
 *   <li>Implements {@link IAnimatableJSCustom} for GeckoLib compatibility.</li>
 *   <li>Stores an instance of {@link AnimatableInstanceCache} for animation tracking.</li>
 *   <li>Uses {@link CustomEntityJSBuilder} for defining animation behavior and properties.</li>
 *   <li>Ensures proper item handling through overridden inventory-related methods.</li>
 * </ul>
 *
 * <h2>Usage:</h2>
 * <p>
 * This class is automatically used when creating entities via {@link CustomEntityJSBuilder}.
 * It should not be instantiated manually; instead, use the builder system to define
 * and spawn custom animated entities dynamically.
 * </p>
 */

public class WrappedAnimatableEntity extends LivingEntity implements IAnimatableJSCustom {
    private final LivingEntity originalEntity;
    private final CustomEntityJSBuilder builder;
    private final AnimatableInstanceCache animatableCache;

    public WrappedAnimatableEntity(LivingEntity originalEntity, CustomEntityJSBuilder builder) {
        super((EntityType<? extends LivingEntity>) originalEntity.getType(), originalEntity.level());
        this.originalEntity = originalEntity;
        this.builder = builder;
        this.animatableCache = GeckoLibUtil.createInstanceCache(this);
    }

    @Override
    public int getId() {
        return originalEntity.getId();
    }


    public LivingEntity getOriginalEntity() {
        return originalEntity;
    }

    public int getTickCount() {
        return this.getOriginalEntity().tickCount;
    }

    @Override
    public HumanoidArm getMainArm() {
        return originalEntity.getMainArm();
    }

    @Override
    public CustomEntityJSBuilder getBuilder() {
        return builder != null ? builder : EntityJSUtils.getEntityBuilder(this.getType());
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animatableCache;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return originalEntity.getArmorSlots();
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return originalEntity.getHandSlots();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return originalEntity.getItemBySlot(slot);
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        originalEntity.setItemSlot(slot, stack);
    }
}