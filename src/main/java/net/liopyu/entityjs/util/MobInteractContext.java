package net.liopyu.entityjs.util;


import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public record MobInteractContext(MobEntityJS entity, Player player, InteractionHand hand) {
}