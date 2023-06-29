package net.dumbdogdiner.dogcore.listener;

import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.afk.AfkManager;
import net.dumbdogdiner.dogcore.event.AfkChangeEvent;
import net.dumbdogdiner.dogcore.event.DisplayNameChangeEvent;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.jetbrains.annotations.NotNull;

public final class PlayerListNameListener implements Listener {
    /** Every 10 seconds, update the pings in each player list entry. */
    private static final long PERIOD = 200L;

    /** The name of the scoreboard used to display ping. */
    private static final String PING_OBJECTIVE = "dogcore-ping";

    /** The scoreboard objective. */
    private static Objective pingObjective;

    static {
        Bukkit.getScheduler().runTaskTimer(DogCorePlugin.getInstance(), () -> {
            synchronized (PlayerListNameListener.class) {
                for (var player : Bukkit.getOnlinePlayers()) {
                    updateName(player);
                }
            }
        }, 0L, PERIOD);

        // add objective if not already present
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        pingObjective = scoreboard.getObjective(PING_OBJECTIVE);
        if (pingObjective == null) {
            scoreboard.registerNewObjective(PING_OBJECTIVE, Criteria.DUMMY, Component.empty());
            pingObjective = scoreboard.getObjective(PING_OBJECTIVE);
            assert pingObjective != null;
            pingObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            pingObjective.setRenderType(RenderType.INTEGER);
        }
    }

    @EventHandler
    public void onDisplayNameChange(final @NotNull DisplayNameChangeEvent event) {
        updateName(event.getPlayer());
    }

    @EventHandler
    public void onAfkChange(final @NotNull AfkChangeEvent event) {
        updateName(event.getPlayer());
    }

    private static synchronized void updateName(final @NotNull Player player) {
        // set the player's ping "score" to their ping
        pingObjective.getScoreFor(player).setScore(player.getPing());
        // set name, adding AFK indicator if needed
        Component name;
        if (AfkManager.isAfk(player)) {
            name = Component.textOfChildren(
                player.displayName(),
                Component.space(),
                Messages.get("afk.indicator")
            );
        } else {
            name = player.displayName();
        }
        player.playerListName(name);
    }
}
