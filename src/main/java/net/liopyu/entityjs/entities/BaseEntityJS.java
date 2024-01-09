package net.liopyu.entityjs.entities;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.util.ExitPortalInfo;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.minecraft.BlockUtil;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.liopyu.entityjs.util.ai.brain.BrainBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * The 'basic' implementation of a custom entity, implements most methods through the builder with some
 * conditionally delegating to the {@code super} implementation if the function is null. Other implementations
 * are <strong>not</strong> required to override every method in a class.<br><br>
 * <p>
 * Further, the only real requirements for a custom entity class is that the class signature respects the contract
 * <pre>{@code public class YourEntityClass extends <? extends LivingEntity> implements <? extends IAnimatableJS>}</pre>
 * A basic implementation for a custom {@link net.minecraft.world.entity.animal.Animal Animal} entity could be as simple as
 * <pre>{@code public class AnimalEntityJS extends Animal implements IAnimatableJS {
 *
 *     private final AnimalBuilder builder;
 *     private final AnimationFactory animationFactory;
 *
 *     public AnimalEntityJS(AnimalBuilder builder, EntityType<? extends Animal> type, Level level) {
 *         super(type, level);
 *         this.builder = builder;
 *         animationFactory = GeckoLibUtil.createFactory(this);
 *     }
 *
 *     @Override
 *     public BaseEntityBuilder<?> getBuilder() {
 *         return builder;
 *     }
 *
 *     @Override
 *     public AnimationFactory getFactory() {
 *         return animationFactory;
 *     }
 *
 *     @Override
 *     @Nullable
 *     public AnimalEntityJS getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
 *         return null;
 *     }
 * }}</pre>
 * Of course this does not implement any possible networking/synced entity data stuff. figure that out yourself, it scares me
 */
public class BaseEntityJS extends LivingEntity implements IAnimatableJS {

    private final AnimationFactory animationFactory;

    protected final BaseEntityJSBuilder builder;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public BaseEntityJS(BaseEntityJSBuilder builder, EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {

        return builder.brainProviderBuilder == null ? super.brainProvider() : builder.brainProviderBuilder.build();
    }

    @Override
    protected Brain<BaseEntityJS> makeBrain(Dynamic<?> p_21069_) {
        final Brain<BaseEntityJS> brain = UtilsJS.cast(super.makeBrain(p_21069_)); // This has become a crutch
        if (builder.brainBuilder != null) {
            final BrainBuilder brainBuilder = new BrainBuilder(builder.id);
            builder.brainBuilder.accept(brainBuilder);
            return brainBuilder.build(brain);
        }
        return brain;
    }

    // Synced entity data is basically impossible, it is class dependent and mostly static
    // @Override
    // protected void defineSynchedData() {
    //     super.defineSynchedData();
    // }
    //
    // Do we actually want to let users touch this, kube's persistent data works well enough
    // @Override
    // public void readAdditionalSaveData(CompoundTag pCompound) {
    //     super.readAdditionalSaveData(pCompound);
    // }
    //
    // @Override
    // public void addAdditionalSaveData(CompoundTag pCompound) {
    //     super.addAdditionalSaveData(pCompound);
    // }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return armorItems;
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return handItems;
    }

    // Mirrors the implementation in Mob
    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot.getType()) {
            case HAND -> handItems.get(slot.getIndex());
            case ARMOR -> armorItems.get(slot.getIndex());
        };
    }

    // Mirrors the implementation in Mob
    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        verifyEquippedItem(stack);
        switch (slot.getType()) {
            case HAND -> onEquipItem(slot, handItems.set(slot.getIndex(), stack), stack);
            case ARMOR -> onEquipItem(slot, armorItems.set(slot.getIndex(), stack), stack);
        }
    }

    @Override
    public BaseEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }

    @Override
    public boolean isPushable() {
        return builder.isPushable;
    }

    @Override
    public HumanoidArm getMainArm() {
        return builder.mainArm;
    }


    @Override
    public boolean isAttackable() {
        return builder.isAttackable;
    }
//Removed for now - liopyu
}
