package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.entity.player.Player;

public record PlayerEntityContext(Player player, MobEntityJS entity) {
}
