package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.MobEntityJSBuilder;
import net.liopyu.entityjs.util.ai.goal.GoalSelectorBuilder;
import net.liopyu.entityjs.util.ai.goal.GoalTargetBuilder;
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
        if (p_21369_ != null && !p_21369_.isClientSide) {
            this.registerGoals(); // Call again so that the builder isn't null
        }
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
        if (builder == null) return; // When called in the super constructor, the builder is null, thus we call it again when we do have a builder
        // Goal selectors
        final GoalSelectorBuilder<MobEntityJS> goalSelectorBuilder = new GoalSelectorBuilder<>(this);
        builder.goalSelectorBuilder.accept(goalSelectorBuilder);
        goalSelectorBuilder.apply(this.goalSelector);
        // Goal targets
        final GoalTargetBuilder<MobEntityJS> goalTargetBuilder = new GoalTargetBuilder<>(this);
        builder.goalTargetBuilder.accept(goalTargetBuilder);
        goalTargetBuilder.apply(this.targetSelector);
    }

    @Override
    protected PathNavigation createNavigation(Level p_21480_) {
        return super.createNavigation(p_21480_);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return builder.canBreatheUnderwater;
    }
}
