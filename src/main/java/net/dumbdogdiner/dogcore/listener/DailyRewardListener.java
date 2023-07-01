package net.dumbdogdiner.dogcore.listener;

import java.util.concurrent.CompletableFuture;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.event.DailyLoginEvent;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class DailyRewardListener implements Listener {
    /** Currently hardcoded, will be loaded from config - the amount to give to players. */
    private static int reward;

    static {
        Configuration.register(() -> {
            synchronized (DailyRewardListener.class) {
                reward = Configuration.getInt("daily.reward");
            }
        });
    }

    @EventHandler
    public void onDailyLogin(final @NotNull DailyLoginEvent event) {
        var player = event.getPlayer();
        User.lookup(player).thenCompose(user -> {
            if (user != null) {
                player.sendMessage(Messages.get("chat.reward"));
                int r;
                synchronized (DailyRewardListener.class) {
                    r = reward;
                }
                return user.give(r);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }
}
