package net.liopyu.entityjs.inventory;

import net.liopyu.entityjs.builders.living.entityjs.ContainerTameableJSBuilder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ContainerJS extends AbstractContainerMenu {
    private final Container mobInventory;
    private final ContainerTameableJSBuilder mob;

    protected ContainerJS(Container mobInventory,
                          ContainerTameableJSBuilder mob,
                          @Nullable MenuType<?> pMenuType,
                          int pContainerId) {
        super(pMenuType, pContainerId);
        this.mobInventory = mobInventory;
        this.mob = mob;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
