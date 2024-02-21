package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import net.liopyu.entityjs.EntityJSMod;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Consumer;

//TODO: Eventually add custom Mob Categories
@Mod.EventBusSubscriber(modid = EntityJSMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegisterMobCategoryEventJS extends EventJS {
    private RegisterMobCategoryEvent event;

    public RegisterMobCategoryEventJS(RegisterMobCategoryEvent event) {
        this.event = event;
    }

    @SubscribeEvent
    public void registerMobCategories(RegisterMobCategoryEvent event) {
        this.event = event;
    }

    public static class RegisterMobCategoryEvent extends Event {
        private final Consumer<MobCategoryRegistrationHelper> registrationHelperConsumer;

        public RegisterMobCategoryEvent(Consumer<MobCategoryRegistrationHelper> registrationHelperConsumer) {
            this.registrationHelperConsumer = registrationHelperConsumer;
        }

        public void registerCategories(String name, String displayName, int max, boolean isFriendly, boolean isPersistent, int despawnDistance) {
            registrationHelperConsumer.accept(new MobCategoryRegistrationHelper(name, displayName, max, isFriendly, isPersistent, despawnDistance));
        }
    }

    public static class MobCategoryRegistrationHelper {
        private final String name;
        private final String displayName;
        private final int max;
        private final boolean isFriendly;
        private final boolean isPersistent;
        private final int despawnDistance;

        public MobCategoryRegistrationHelper(String name, String displayName, int max, boolean isFriendly, boolean isPersistent, int despawnDistance) {
            this.name = name;
            this.displayName = displayName;
            this.max = max;
            this.isFriendly = isFriendly;
            this.isPersistent = isPersistent;
            this.despawnDistance = despawnDistance;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getMax() {
            return max;
        }

        public boolean isFriendly() {
            return isFriendly;
        }

        public boolean isPersistent() {
            return isPersistent;
        }

        public int getDespawnDistance() {
            return despawnDistance;
        }
    }
}
