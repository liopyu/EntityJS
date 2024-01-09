package net.liopyu.entityjs.util;


import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class MobInteractContext {
    public final MobEntityJS entity;
    public final Player player;
    public final InteractionHand hand;
    public final InteractionResult interactionResult;

    public MobInteractContext(MobEntityJS entity, Player player, InteractionHand hand, InteractionResult interactionResult) {
        this.entity = entity;
        this.player = player;
        this.hand = hand;
        this.interactionResult = interactionResult;
    }
}