package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

// pain.
@Mixin(PathfinderMob.class)
public abstract class EntityPartMixin extends LivingEntity {
    @Unique
    private Object entityJs$builder;
    @Unique
    private Object entityJs$entityObject = this;
    @Unique
    public PartEntityJS<?>[] partEntities;

    @Unique
    private Entity entityJs$getLivingEntity() {
        return (Entity) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    protected EntityPartMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        var entityType = entityJs$getLivingEntity().getType();
        var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
        eventJS.postModifyEventIfNeeded();
        entityJs$builder = eventJS.getBuilder();

        List<PartEntityJS<?>> tempPartEntities = new ArrayList<>();
        for (ContextUtils.PartEntityParams<?> params : ((ModifyLivingEntityBuilder) entityJs$builder).partEntityParamsList) {
            PartEntityJS<?> partEntity = new PartEntityJS<>(entityJs$getLivingEntity(), params.name, params.width, params.height, params.builder);
            tempPartEntities.add(partEntity);
        }
        ConsoleJS.STARTUP.log(partEntities.length);
        partEntities = tempPartEntities.toArray(new PartEntityJS<?>[0]);

    }

    // Part Entity Logical Overrides --------------------------------
    @Override
    public void setId(int entityId) {
        super.setId(entityId);
        for (int i = 0; i < partEntities.length; i++) {
            PartEntityJS<?> partEntity = partEntities[i];
            if (partEntity != null) {
                partEntity.setId(entityId + i + 1);
            }
        }
    }

    public void tickPart(String partName, double offsetX, double offsetY, double offsetZ) {
        var x = this.getX();
        var y = this.getY();
        var z = this.getZ();
        for (PartEntityJS<?> partEntity : partEntities) {
            if (partEntity.name.equals(partName)) {
                partEntity.movePart(x + offsetX, y + offsetY, z + offsetZ, partEntity.getYRot(), partEntity.getXRot());
                return;
            }
        }
        EntityJSHelperClass.logWarningMessageOnce("Part with name " + partName + " not found for entity: " + entityJs$entityName());
    }

    @Override
    public boolean isMultipartEntity() {
        return partEntities != null;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return partEntities;
    }
}
