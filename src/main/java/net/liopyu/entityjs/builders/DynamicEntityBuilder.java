package net.liopyu.entityjs.builders;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;

public class DynamicEntityBuilder implements IBaseEntityBuilder {
    public final IBaseEntityBuilder parent;
    private float width;
    private float height;
    private boolean summonable;
    private boolean shouldSave;
    private boolean fireImmune;
    private Block[] immuneTo;
    private boolean canSpawnFarFromPlayer;
    private int clientTrackingRange;
    private int updateInterval;
    private MobCategory mobCategory;
    private BaseEntityBuilder<?> baseEntityBuilder;

    public DynamicEntityBuilder(IBaseEntityBuilder p) {
        parent = p;
        width = parent.getWidth();
        height = parent.getHeight();
        summonable = parent.isSummonable();
        shouldSave = parent.shouldSave();
        fireImmune = parent.isFireImmune();
        immuneTo = parent.getImmuneTo();
        canSpawnFarFromPlayer = parent.canSpawnFarFromPlayer();
        clientTrackingRange = parent.getClientTrackingRange();
        updateInterval = parent.getUpdateInterval();
        mobCategory = parent.getMobCategory();
        baseEntityBuilder = parent.getBaseEntityBuilder();

    }

    @Override
    public float getWidth() {
        return 0;
    }

    @Override
    public float getHeight() {
        return 0;
    }

    @Override
    public boolean isSummonable() {
        return false;
    }

    @Override
    public boolean shouldSave() {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return false;
    }

    @Override
    public Block[] getImmuneTo() {
        return new Block[0];
    }

    @Override
    public boolean canSpawnFarFromPlayer() {
        return false;
    }

    @Override
    public int getClientTrackingRange() {
        return 0;
    }

    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public MobCategory getMobCategory() {
        return null;
    }

    @Override
    public BaseEntityBuilder getBaseEntityBuilder() {
        return null;
    }
}
