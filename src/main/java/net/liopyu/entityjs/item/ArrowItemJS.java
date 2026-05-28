package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ArrowItemJS extends ArrowItem implements ProjectileItem {
    private final BuilderBase<?> builder;

    public ArrowItemJS(Properties p_41383_, BuilderBase<?> builder) {
        super(p_41383_);
        this.builder = builder;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        if (builder instanceof ArrowEntityJSBuilder b) {
            var entity = new ArrowEntityJS(b, b.get(), level);
            entity.setPos(new Vec3(position.x(), position.y(), position.z()));
            entity.pickup = AbstractArrow.Pickup.ALLOWED;
            entity.setPickUpItem(itemStack.copyWithCount(1));
            return entity;
        }
        return null;
    }
}
