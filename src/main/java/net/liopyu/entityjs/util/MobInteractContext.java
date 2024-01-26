package net.liopyu.entityjs.util;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MobInteractContext {
    public final LivingEntity entity;
    public final Player player;
    public final InteractionHand hand;

    public MobInteractContext(LivingEntity entity, Player player, InteractionHand hand) {
        this.entity = entity;
        this.player = player;
        this.hand = hand;
    }
}