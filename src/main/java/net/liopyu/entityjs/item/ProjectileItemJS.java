package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileAnimatableJSBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityJSBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ProjectileItemJS extends Item implements ProjectileItem {
    private final BuilderBase<?> builder;

    public ProjectileItemJS(Properties p_41383_, BuilderBase<?> builder) {
        super(p_41383_);
        this.builder = builder;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        if (builder instanceof ProjectileAnimatableJSBuilder b) {
            var entity = new ProjectileAnimatableJS(b, b.get(), level);
            entity.setPos(new Vec3(position.x(), position.y(), position.z()));
            return entity;
        } else if (builder instanceof ProjectileEntityBuilder<?> b) {
            var bu = (ProjectileEntityJSBuilder) b;
            var entity = new ProjectileEntityJS(bu, bu.get(), level);
            entity.setPos(new Vec3(position.x(), position.y(), position.z()));
            return entity;
        }
        return null;
    }
}
