package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.server.message.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CitadelNetworking {
    private static final int PROPERTIES = 0;
    private static final int ANIMATION = 1;
    private static final int SYNC_CLIENT_TICK_RATE = 2;
    private static final int DANCE_JUKEBOX = 3;
    private static final int SYNC_PATH = 4;
    private static final int SYNC_PATH_REACHED = 5;

    public static void register() {
        registerServerReceiver();
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            registerClientReceiver();
        }
    }

    private static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(Citadel.PACKET_CHANNEL, (server, player, handler, buf, responseSender) -> {
            int id = buf.readVarInt();
            server.execute(() -> {
                switch (id) {
                    case PROPERTIES -> {
                        PropertiesMessage msg = PropertiesMessage.read(buf);
                        PropertiesMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(player, PacketContext.PacketDirection.PLAY_TO_SERVER)));
                    }
                    case ANIMATION -> {
                        AnimationMessage msg = AnimationMessage.read(buf);
                        AnimationMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(player, PacketContext.PacketDirection.PLAY_TO_SERVER)));
                    }
                    case DANCE_JUKEBOX -> {
                        DanceJukeboxMessage msg = DanceJukeboxMessage.read(buf);
                        DanceJukeboxMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(player, PacketContext.PacketDirection.PLAY_TO_SERVER)));
                    }
                    default -> {}
                }
            });
        });
    }

    private static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(Citadel.PACKET_CHANNEL, (client, handler, buf, responseSender) -> {
            int id = buf.readVarInt();
            client.execute(() -> {
                switch (id) {
                    case PROPERTIES -> {
                        PropertiesMessage msg = PropertiesMessage.read(buf);
                        PropertiesMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    case ANIMATION -> {
                        AnimationMessage msg = AnimationMessage.read(buf);
                        AnimationMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    case SYNC_CLIENT_TICK_RATE -> {
                        SyncClientTickRateMessage msg = SyncClientTickRateMessage.read(buf);
                        SyncClientTickRateMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    case DANCE_JUKEBOX -> {
                        DanceJukeboxMessage msg = DanceJukeboxMessage.read(buf);
                        DanceJukeboxMessage.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    case SYNC_PATH -> {
                        MessageSyncPath msg = MessageSyncPath.read(buf);
                        MessageSyncPath.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    case SYNC_PATH_REACHED -> {
                        MessageSyncPathReached msg = MessageSyncPathReached.read(buf);
                        MessageSyncPathReached.Handler.handle(msg, PacketContext.wrap(new FabricPacketContext(null, PacketContext.PacketDirection.PLAY_TO_CLIENT)));
                    }
                    default -> {}
                }
            });
        });
    }

    public static void sendToServer(Object message) {
        if (FabricLoader.getInstance().getEnvironmentType() != net.fabricmc.api.EnvType.CLIENT) return;
        ClientPlayNetworking.send(Citadel.PACKET_CHANNEL, writeMessageToBuf(message));
    }

    public static void sendToClient(Object message, ServerPlayer player) {
        ServerPlayNetworking.send(player, Citadel.PACKET_CHANNEL, writeMessageToBuf(message));
    }

    private static FriendlyByteBuf writeMessageToBuf(Object message) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        if (message instanceof PropertiesMessage m) {
            buf.writeVarInt(PROPERTIES);
            PropertiesMessage.write(m, buf);
        } else if (message instanceof AnimationMessage m) {
            buf.writeVarInt(ANIMATION);
            AnimationMessage.write(m, buf);
        } else if (message instanceof SyncClientTickRateMessage m) {
            buf.writeVarInt(SYNC_CLIENT_TICK_RATE);
            SyncClientTickRateMessage.write(m, buf);
        } else if (message instanceof DanceJukeboxMessage m) {
            buf.writeVarInt(DANCE_JUKEBOX);
            DanceJukeboxMessage.write(m, buf);
        } else if (message instanceof MessageSyncPath m) {
            buf.writeVarInt(SYNC_PATH);
            m.write(buf);
        } else if (message instanceof MessageSyncPathReached m) {
            buf.writeVarInt(SYNC_PATH_REACHED);
            m.write(buf);
        }
        return buf;
    }
}
