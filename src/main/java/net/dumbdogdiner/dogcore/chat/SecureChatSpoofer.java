package net.dumbdogdiner.dogcore.chat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Registers an event handler with ProtocolLib that hides the secure chat warning by altering the server data packet.
 */
public final class SecureChatSpoofer extends PacketAdapter {
    private SecureChatSpoofer() {
        super(DogCorePlugin.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.SERVER_DATA);
    }

    @Override
    public void onPacketSending(final @NotNull PacketEvent event) {
        // set secure chat field to true
        event.getPacket().getBooleans().write(0, true);
    }

    public static void register() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new SecureChatSpoofer());
    }
}
