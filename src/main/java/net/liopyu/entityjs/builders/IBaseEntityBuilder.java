package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;

public interface IBaseEntityBuilder<T extends Entity & IAnimatableJS> {


    float getWidth();

    float getHeight();

    boolean isSummonable();

    boolean shouldSave();

    boolean isFireImmune();

    Block[] getImmuneTo();

    boolean canSpawnFarFromPlayer();

    int getClientTrackingRange();

    int getUpdateInterval();

    MobCategory getMobCategory();

    BaseEntityBuilder<T> getBaseEntityBuilder();
}