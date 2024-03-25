package net.liopyu.entityjs.entities.living.entityjs;

import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.ContainerTameableJSBuilder;
import net.liopyu.entityjs.util.ModKeybinds;
import net.liopyu.liolib.core.animatable.instance.AnimatableInstanceCache;
import net.liopyu.liolib.util.GeckoLibUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ContainerTameableEntityJS extends TamableAnimal implements ContainerListener, HasCustomInventoryScreen, Saddleable, IAnimatableJS {
    protected final ContainerTameableJSBuilder builder;
    private final AnimatableInstanceCache getAnimatableInstanceCache;

    public ContainerTameableEntityJS(ContainerTameableJSBuilder builder, EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        getAnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
    }

    //Builder and Animatable logic
    @Override
    public BaseLivingEntityBuilder<?> getBuilder() {
        return builder;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return getAnimatableInstanceCache;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return builder.get().create(serverLevel);
    }

    @Override
    public void tick() {
        super.tick();
        if (ModKeybinds.mount_inventory.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player != null && player.isPassenger() && player.getVehicle() == this) {
                openCustomInventoryScreen(player);
            }
        }
    }

    @Override
    public void containerChanged(Container container) {

    }

    @Override
    public void openCustomInventoryScreen(Player player) {

    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Override
    public void equipSaddle(@Nullable SoundSource soundSource) {

    }

    @Override
    public boolean isSaddled() {
        return false;
    }
}
