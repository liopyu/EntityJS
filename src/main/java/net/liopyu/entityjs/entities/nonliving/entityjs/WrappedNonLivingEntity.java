package net.liopyu.entityjs.entities.nonliving.entityjs;


import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;

public class WrappedNonLivingEntity extends Entity {
    private final Entity originalEntity;
    private final CustomEntityJSBuilder builder;

    public WrappedNonLivingEntity(Entity originalEntity, CustomEntityJSBuilder builder) {
        super((EntityType<? extends Entity>) originalEntity.getType(), originalEntity.level());
        this.originalEntity = originalEntity;
        this.builder = builder;
    }

    @Override
    public int getId() {
        return originalEntity.getId();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }


    public Entity getOriginalEntity() {
        return originalEntity;
    }

    public WrappedNonLivingEntity syncFromOriginal() {
        this.tickCount = originalEntity.tickCount;
        this.setPos(originalEntity.getX(), originalEntity.getY(), originalEntity.getZ());
        this.xo = originalEntity.xo;
        this.yo = originalEntity.yo;
        this.zo = originalEntity.zo;
        this.setXRot(originalEntity.getXRot());
        this.setYRot(originalEntity.getYRot());
        this.xRotO = originalEntity.xRotO;
        this.yRotO = originalEntity.yRotO;
        return this;
    }

    public int getTickCount() {
        return this.getOriginalEntity().tickCount;
    }

    public CustomEntityJSBuilder getBuilder() {
        return builder != null ? builder : EntityJSUtils.getEntityBuilder(this.getType());
    }
}
