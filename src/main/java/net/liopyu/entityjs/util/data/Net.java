package net.liopyu.entityjs.util.data;

import io.netty.buffer.ByteBuf;
import net.liopyu.entityjs.EntityJSMod;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.*;

@EventBusSubscriber(modid = EntityJSMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class Net {
    private static final String PROTO = "1";
    private static volatile boolean REGISTERED = false;

    private static final StreamCodec<ByteBuf, Map<String, Tag>> TAG_MAP =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.TAG);
    private static final StreamCodec<ByteBuf, Map<String, Integer>> INT_MAP =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);

    public record SyncAllS2C(UUID entityId, Map<String, Tag> values,
                             Map<String, Integer> types) implements CustomPacketPayload {
        public static final Type<SyncAllS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "sync_all_s2c"));
        public static final StreamCodec<ByteBuf, SyncAllS2C> STREAM_CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, SyncAllS2C::entityId, TAG_MAP, SyncAllS2C::values, INT_MAP, SyncAllS2C::types, SyncAllS2C::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetValueS2C(UUID entityId, String name, Tag value) implements CustomPacketPayload {
        public static final Type<SetValueS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "set_value_s2c"));
        public static final StreamCodec<ByteBuf, SetValueS2C> STREAM_CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, SetValueS2C::entityId, ByteBufCodecs.STRING_UTF8, SetValueS2C::name, ByteBufCodecs.TAG, SetValueS2C::value, SetValueS2C::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetTypeS2C(UUID entityId, String name, int ord) implements CustomPacketPayload {
        public static final Type<SetTypeS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "set_type_s2c"));
        public static final StreamCodec<ByteBuf, SetTypeS2C> STREAM_CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, SetTypeS2C::entityId, ByteBufCodecs.STRING_UTF8, SetTypeS2C::name, ByteBufCodecs.VAR_INT, SetTypeS2C::ord, SetTypeS2C::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DeleteValueS2C(UUID entityId, String name) implements CustomPacketPayload {
        public static final Type<DeleteValueS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "delete_value_s2c"));
        public static final StreamCodec<ByteBuf, DeleteValueS2C> STREAM_CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, DeleteValueS2C::entityId, ByteBufCodecs.STRING_UTF8, DeleteValueS2C::name, DeleteValueS2C::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SetValueC2S(UUID entityId, String name, Tag value) implements CustomPacketPayload {
        public static final Type<SetValueC2S> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "set_value_c2s"));
        public static final StreamCodec<ByteBuf, SetValueC2S> STREAM_CODEC =
                StreamCodec.composite(UUIDUtil.STREAM_CODEC, SetValueC2S::entityId, ByteBufCodecs.STRING_UTF8, SetValueC2S::name, ByteBufCodecs.TAG, SetValueC2S::value, SetValueC2S::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record EnsureValueC2S(UUID entityId, String name, EntitySerializerType serType,
                                 Tag value) implements CustomPacketPayload {
        public static final Type<EnsureValueC2S> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "ensure_value_c2s"));
        public static final StreamCodec<ByteBuf, EnsureValueC2S> STREAM_CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, EnsureValueC2S::entityId,
                        ByteBufCodecs.STRING_UTF8, EnsureValueC2S::name,
                        ByteBufCodecs.VAR_INT, e -> e.serType().ordinal(),
                        ByteBufCodecs.TAG, EnsureValueC2S::value,
                        (id, n, ord, v) -> new EnsureValueC2S(id, n, EntitySerializerType.values()[ord], v)
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent e) {
        if (REGISTERED) return;
        REGISTERED = true;

        PayloadRegistrar r = e.registrar(PROTO);
        r.playToClient(Net.SetTypedValueS2C.TYPE, Net.SetTypedValueS2C.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> {
                    ClientCache.setType(msg.entityId(), msg.name(), msg.ord());
                    ClientCache.set(msg.entityId(), msg.name(), msg.value());
                }));

        r.playToClient(Net.SyncAllS2C.TYPE, Net.SyncAllS2C.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> ClientCache.setAll(msg.entityId(), msg.values(), msg.types())));

        r.playToClient(Net.SetTypeS2C.TYPE, Net.SetTypeS2C.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> ClientCache.setType(msg.entityId(), msg.name(), msg.ord())));

        r.playToClient(Net.SetValueS2C.TYPE, Net.SetValueS2C.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> ClientCache.set(msg.entityId(), msg.name(), msg.value())));

        r.playToServer(Net.SetValueC2S.TYPE, Net.SetValueC2S.STREAM_CODEC, (msg, ctx) -> {
            var p = ctx.player(); if (p == null) return;
            var ent = ((ServerLevel) p.level()).getEntity(msg.entityId());
            if (ent instanceof Entity le) ServerCache.set(le, msg.name(), msg.value());
        });

        r.playToServer(Net.EnsureValueC2S.TYPE, Net.EnsureValueC2S.STREAM_CODEC, (msg, ctx) -> {
            var p = ctx.player(); if (p == null) return;
            var ent = ((ServerLevel) p.level()).getEntity(msg.entityId());
            if (ent instanceof Entity le) ServerCache.ensure(le, msg.name(), msg.value(), msg.serType());
        });
    }

    public static void sendTypedValueTracking(Entity e, UUID id, String name, int ord, Tag value) {
        PacketDistributor.sendToPlayersTrackingEntity(e, new SetTypedValueS2C(id, name, ord, value));
    }

    public static void sendAllTo(ServerPlayer p, UUID id, Map<String, Tag> values, Map<String, Integer> types) {
        PacketDistributor.sendToPlayer(p, new SyncAllS2C(id, values, types));
    }

    public static void sendAllTracking(Entity e, UUID id, Map<String, Tag> values, Map<String, Integer> types) {
        PacketDistributor.sendToPlayersTrackingEntity(e, new SyncAllS2C(id, values, types));
    }

    public static void sendValueTracking(Entity e, UUID id, String name, Tag value) {
        PacketDistributor.sendToPlayersTrackingEntity(e, new SetValueS2C(id, name, value));
    }

    public static void sendTypeTracking(Entity e, UUID id, String name, int ord) {
        PacketDistributor.sendToPlayersTrackingEntity(e, new SetTypeS2C(id, name, ord));
    }

    public static void sendDeleteTracking(Entity e, UUID id, String name) {
        PacketDistributor.sendToPlayersTrackingEntity(e, new DeleteValueS2C(id, name));
    }

    public static void sendSetToServer(UUID id, String name, Tag value) {
        PacketDistributor.sendToServer(new SetValueC2S(id, name, value));
    }

    public static void sendEnsureToServer(UUID id, String name, EntitySerializerType serType, Tag value) {
        PacketDistributor.sendToServer(new EnsureValueC2S(id, name, serType, value));
    }

    public record SetTypedValueS2C(UUID entityId, String name, int ord, Tag value) implements CustomPacketPayload {
        public static final Type<SetTypedValueS2C> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(EntityJSMod.MOD_ID, "set_typed_value_s2c"));
        public static final StreamCodec<ByteBuf, SetTypedValueS2C> STREAM_CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, SetTypedValueS2C::entityId,
                        ByteBufCodecs.STRING_UTF8, SetTypedValueS2C::name,
                        ByteBufCodecs.VAR_INT, SetTypedValueS2C::ord,
                        ByteBufCodecs.TAG, SetTypedValueS2C::value,
                        SetTypedValueS2C::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}