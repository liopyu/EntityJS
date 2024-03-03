package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TameableMobBuilder<T extends TamableAnimal & IAnimatableJS> extends AnimalEntityBuilder<T> {
    public TameableMobBuilder(ResourceLocation i) {
        super(i);
        canJump = true;
        followLeashSpeed = 1.0D;

    }
}