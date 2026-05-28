package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import net.liopyu.entityjs.builders.nonliving.vanilla.TridentJSBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.TridentEntityJS;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TridentItemJS extends TridentItem implements ProjectileItem {
    private final BuilderBase<?> builder;

    public TridentItemJS(Properties p_43381_, BuilderBase<?> builderBase) {
        super(p_43381_);
        this.builder = builderBase;
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        var b = (TridentJSBuilder) builder;
        var entity = new TridentEntityJS(b, b.get(), level);
        entity.setPos(new Vec3(position.x(), position.y(), position.z()));
        entity.pickup = AbstractArrow.Pickup.ALLOWED;
        entity.setPickupItemStack(itemStack.copy());
        return entity;
    }
}
