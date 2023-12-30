package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class MobEntityJS extends Mob implements IAnimatableJS {

    private final MobEntityJSBuilder builder;
    private final AnimationFactory animationFactory;

    public MobEntityJS(MobEntityJSBuilder builder, EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.builder = builder;
        animationFactory = GeckoLibUtil.createFactory(this);
    }

    @Override
    public BaseEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public AnimationFactory getFactory() {
        return animationFactory;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    protected PathNavigation createNavigation(Level p_21480_) {
        return super.createNavigation(p_21480_);
    }
}
