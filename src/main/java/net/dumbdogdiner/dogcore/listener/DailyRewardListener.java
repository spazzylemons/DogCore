package net.dumbdogdiner.dogcore.listener;

import java.util.concurrent.CompletableFuture;
import net.dumbdogdiner.dogcore.config.Configurable;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.database.User;
import net.dumbdogdiner.dogcore.event.DailyLoginEvent;
import net.dumbdogdiner.dogcore.messages.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class DailyRewardListener implements Listener, Configurable {
    /** Currently hardcoded, will be loaded from config - the amount to give to players. */
    private int reward;

    public DailyRewardListener() {
        Configuration.register(this);
    }

    @EventHandler
    public void onDailyLogin(final @NotNull DailyLoginEvent event) {
        var player = event.getPlayer();
        User.lookup(player).thenCompose(user -> {
            if (user != null) {
                player.sendMessage(Messages.get("chat.reward"));
                return user.give(reward);
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
    }

    @Override
    public void loadConfig() {
        reward = Configuration.getInt("daily.reward");
    }
}
