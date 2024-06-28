package net.liopyu.entityjs.builders.living.modification;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ModifyEntityBuilder {
    public final Entity entity;

    public ModifyEntityBuilder(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public Level getLevel() {
        return entity.level();
    }
}
