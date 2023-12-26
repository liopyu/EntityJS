package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseEntityBuilder<T extends Entity & IAnimatableJS> extends BuilderBase<EntityType<T>> implements IBaseEntityBuilder<T> {
    public transient float width;
    public transient float height;
    public transient boolean summonable;
    public transient boolean save;
    public transient boolean fireImmune;
    public transient Block[] immuneTo;
    public transient boolean spawnFarFromPlayer;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;

    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        width = 1;
        height = 1;
        summonable = true;
        save = true;
        fireImmune = false;
        immuneTo = new Block[0];
        spawnFarFromPlayer = false;
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
    }

    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public BaseEntityBuilder<T> setSummonable(boolean b) {
        summonable = b;
        return this;
    }

    public BaseEntityBuilder<T> saves(boolean b) {
        save = b;
        return this;
    }

    public BaseEntityBuilder<T> fireImmune(boolean b) {
        fireImmune = b;
        return this;
    }

    public BaseEntityBuilder<T> immuneTo(ResourceLocation... blocks) {
        List<Block> immuneTo = new ArrayList<>();
        for (ResourceLocation block : blocks) {
            if (ForgeRegistries.BLOCKS.containsKey(block)) {
                immuneTo.add(ForgeRegistries.BLOCKS.getValue(block));
            }
        }
        this.immuneTo = immuneTo.toArray(this.immuneTo);
        return this;
    }

    public BaseEntityBuilder<T> canSpawnFarFromPlayer(boolean b) {
        spawnFarFromPlayer = b;
        return this;
    }

    public BaseEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    public BaseEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    public BaseEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }


    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilderJS<>(this).get();
    }

    abstract public EntityTypeBuilderJS.Factory<T> factory();


    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean isSummonable() {
        return summonable;
    }

    @Override
    public boolean shouldSave() {
        return save;
    }

    @Override
    public boolean isFireImmune() {
        return fireImmune;
    }

    @Override
    public Block[] getImmuneTo() {
        return immuneTo;
    }

    @Override
    public boolean canSpawnFarFromPlayer() {
        return spawnFarFromPlayer;
    }

    @Override
    public int getClientTrackingRange() {
        return clientTrackingRange;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public MobCategory getMobCategory() {
        return mobCategory;
    }

    @Override
    public BaseEntityBuilder<T> getBaseEntityBuilder() {
        return this;
    }
}
