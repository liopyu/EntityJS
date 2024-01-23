package net.liopyu.entityjs.entities;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.AnimalEntityJSBuilder;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.EntityTypeRegistry;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.ParametersAreNonnullByDefault;

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
 *     public BaseLivingEntityBuilder<?> getBuilder() {
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
@MethodsReturnNonnullByDefault // Just remove the countless number of warnings present
@ParametersAreNonnullByDefault
public class AnimalEntityJS extends Animal implements IAnimatableJS {

    private final AnimationFactory animationFactory;

    protected final AnimalEntityJSBuilder builder;
    private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);

    public AnimalEntityJS(AnimalEntityJSBuilder builder, EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        if (EventHandlers.buildBrainProvider.hasListeners(getTypeId())) {
            final BuildBrainProviderEventJS event = new BuildBrainProviderEventJS();
            EventHandlers.buildBrainProvider.post(event, getTypeId());
            return event.provide();
        } else {
            return super.brainProvider();
        }
    }

    @Override
    protected Brain<AnimalEntityJS> makeBrain(Dynamic<?> p_21069_) {
        if (EventHandlers.buildBrain.hasListeners(getTypeId())) {
            final Brain<AnimalEntityJS> brain = UtilsJS.cast(brainProvider().makeBrain(p_21069_));
            EventHandlers.buildBrain.post(new BuildBrainEventJS<>(brain), getTypeId());
            return brain;
        } else {
            return UtilsJS.cast(super.makeBrain(p_21069_));
        }
    }


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
    public BaseLivingEntityBuilder<?> getBuilder() {
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

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (builder.breedOffspringLocation != null) {
            EntityType<? extends AgeableMob> breedOffspringType = EntityTypeRegistry.getEntityType(builder.breedOffspringLocation);

            if (breedOffspringType != null) {
                return breedOffspringType.create(serverLevel);
            }
        }
        return null;
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return super.isFood(pStack);
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public boolean canBreed() {
        return true;
    }
}
