package net.liopyu.entityjs.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class PlayerEntityContext {
    public final LivingEntity entity;
    public final Player player;

    public PlayerEntityContext(Player player, LivingEntity entity) {
        this.entity = entity;
        this.player = player;
    }
}
