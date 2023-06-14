package net.dumbdogdiner.dogcore.database;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import static net.dumbdogdiner.dogcore.database.schema.Tables.*;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SortOrder;
import org.jooq.exception.DataAccessException;

public final class User {
    public static final int MAX_NICKNAME_LENGTH = 16;

    private static final int PAGE_SIZE = 5;

    private static final String OUT_OF_RANGE = "22003";

    private static final String CHECK_VOILATION = "23514";

    public record BaltopEntry(Component name, long amount) {}

    private final @NotNull UUID uuid;

    private User(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    public CompletionStage<Boolean> isMuted() {
        return Database.execute(ctx -> {
            var dsl = ctx.dsl();
            // does an entry exist in the mutes table?
            var result = dsl.select(MUTES.EXPIRES)
                .from(MUTES)
                .where(MUTES.PLAYER_ID.eq(uuid))
                .fetch();
            if (result.isEmpty()) {
                // there is no mute, so the player is not muted
                return false;
            }
            // does the mute have an expiration time?
            var expirationTime = result.getValue(0, MUTES.EXPIRES);
            if (expirationTime == null) {
                // if the expiration time is null, mute is indefinite
                return true;
            }
            // has that time passed?
            var now = System.currentTimeMillis();
            if (now >= expirationTime) {
                // the mute has expired, so delete the row
                dsl.deleteFrom(MUTES)
                    .where(MUTES.PLAYER_ID.eq(uuid))
                    .execute();
                // no longer muted
                return false;
            }
            return true;
        });
    }

    /**
     * Mute for the given duration, or indefinitely if duration is null.
     * Replaces any existing mute duration if one exists.
     */
    public @NotNull CompletionStage<Void> mute(@Nullable Duration duration) {
        var expires = (duration == null) ? null : duration.toMillis() + System.currentTimeMillis();
        return Database.executeUpdate(ctx -> ctx.dsl().insertInto(MUTES)
            .columns(MUTES.PLAYER_ID, MUTES.EXPIRES)
            .values(uuid, expires)
            .onConflict(MUTES.PLAYER_ID)
            .doUpdate()
            .set(MUTES.EXPIRES, expires)
            .execute());
    }

    /**
     * Unmute this player.
     */
    public @NotNull CompletionStage<Void> unmute() {
        return Database.executeUpdate(ctx -> ctx.dsl().deleteFrom(MUTES)
            .where(MUTES.PLAYER_ID.eq(uuid))
            .execute());
    }

    public @NotNull CompletionStage<@NotNull Long> getBalance() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.BALANCE)
            .from(USERS)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .fetch()
            .get(0)
            .value1());
    }

    public @NotNull CompletionStage<Void> setBalance(long balance) {
        return Database.executeUpdate(ctx -> ctx.dsl().update(USERS)
            .set(USERS.BALANCE, balance)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .execute());
    }

    /**
     * Give or take money from this player.
     * Fails if balance would overflow or be negative.
     */
    public @NotNull CompletionStage<@NotNull Boolean> give(long amount) {
        return Database.execute(ctx -> {
            try {
                ctx.dsl().update(USERS)
                    .set(USERS.BALANCE, USERS.BALANCE.add(amount))
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
            } catch (DataAccessException e) {
                var state = e.sqlState();
                if (OUT_OF_RANGE.equals(state) || CHECK_VOILATION.equals(state)) {
                    return false;
                }
                throw e;
            }
            return true;
        });
    }

    /**
     * Pay another player. Fails if either balance would be out of range.
     */
    public @NotNull CompletionStage<@NotNull Boolean> pay(@NotNull User other, long amount) {
        // don't allow paying ourselves, or we'd generate infinite money
        if (other.uuid.equals(uuid)) return CompletableFuture.completedFuture(false);
        // don't allow paying negatives
        if (amount < 0L) return CompletableFuture.completedFuture(false);
        // otherwise, perform transaction
        return Database.execute(ctx -> {
            var dsl = ctx.dsl();
            try {
                // remove the amount from our account
                dsl.update(USERS)
                    .set(USERS.BALANCE, USERS.BALANCE.sub(amount))
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
                // add the amount to the other account
                dsl.update(USERS)
                    .set(USERS.BALANCE, USERS.BALANCE.add(amount))
                    .where(USERS.UNIQUE_ID.eq(other.uuid))
                    .execute();
            } catch (DataAccessException e) {
                var state = e.sqlState();
                if (OUT_OF_RANGE.equals(state) || CHECK_VOILATION.equals(state)) {
                    dsl.rollback().execute();
                    return false;
                }
                throw e;
            }
            return true;
        });
    }

    public @NotNull CompletionStage<@NotNull Boolean> setNickname(@Nullable String value) {
        if (value == null || value.length() <= MAX_NICKNAME_LENGTH) {
            return Database.execute(ctx -> ctx.dsl().update(USERS)
                .set(USERS.NICKNAME, value)
                .where(USERS.UNIQUE_ID.eq(uuid))
                .execute())
                .thenCompose(v -> {
                    var player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        return NameFormatter.refreshPlayerName(player).thenApply(w -> true);
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                });
        }
        return CompletableFuture.completedFuture(false);
    }

    public @NotNull CompletionStage<@NotNull Boolean> getSocialSpy() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.SOCIAL_SPY)
            .from(USERS)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .fetch()
            .get(0)
            .value1());
    }

    public @NotNull CompletionStage<Void> setSocialSpy(boolean value) {
        return Database.executeUpdate(ctx -> ctx.dsl().update(USERS)
            .set(USERS.SOCIAL_SPY, value)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .execute());
    }

    public @NotNull CompletableFuture<@NotNull Component> formattedName() {
        return Database.execute(ctx -> {
            var row = ctx.dsl().select(USERS.NICKNAME, USERS.USERNAME)
                .from(USERS)
                .where(USERS.UNIQUE_ID.eq(uuid))
                .fetch()
                .get(0);
            var nickname = row.value1();
            if (nickname != null) {
                return nickname;
            }
            return row.value2();
        }).thenCompose(name -> NameFormatter.formatUsername(uuid, name)).toCompletableFuture();
    }

    public static @NotNull CompletionStage<@Nullable User> lookup(@NotNull UUID uuid) {
        return Database.execute(ctx -> ctx.dsl().fetchCount(USERS, USERS.UNIQUE_ID.eq(uuid)) != 0)
            .thenApply(exists -> exists ? new User(uuid) : null);
    }

    public static @NotNull CompletionStage<@Nullable User> lookup(@NotNull OfflinePlayer player) {
        return lookup(player.getUniqueId());
    }

    public static void lookupCommand(@NotNull OfflinePlayer player, @NotNull CommandSender sender, @NotNull Consumer<@NotNull User> onSuccess) {
        lookupCommand(player, sender, onSuccess, null);
    }

    public static void lookupCommand(@NotNull OfflinePlayer player, @NotNull CommandSender sender, @NotNull Consumer<@NotNull User> onSuccess, @Nullable Runnable onFailure) {
        lookup(player).thenAccept(user -> {
            if (user == null) {
                sender.sendMessage(Messages.get("error.playerNotFound"));
                if (onFailure != null) {
                    onFailure.run();
                }
            } else {
                onSuccess.accept(user);
            }
        });
    }

    public static @NotNull CompletionStage<@NotNull Boolean> register(@NotNull Player player) {
        var uuid = player.getUniqueId();
        var name = player.getName();
        return Database.execute(ctx -> {
            var exists = ctx.dsl().fetchCount(USERS, USERS.UNIQUE_ID.eq(uuid)) != 0;
            if (exists) {
                ctx.dsl().update(USERS)
                    .set(USERS.USERNAME, name)
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
            } else {
                ctx.dsl().insertInto(USERS)
                    .columns(USERS.UNIQUE_ID, USERS.USERNAME)
                    .values(uuid, name)
                    .execute();
            }
            return !exists;
        });
    }

    /** Page index starts at 1 */
    public static @NotNull CompletionStage<@NotNull List<@NotNull BaltopEntry>> top(int page) {
        // TODO: https://blog.jooq.org/calculating-pagination-metadata-without-extra-roundtrips-in-sql/
        return Database.execute(ctx -> ctx.dsl().select(USERS.UNIQUE_ID, USERS.BALANCE)
            .from(USERS)
            .orderBy(USERS.BALANCE.sort(SortOrder.DESC))
            .limit((long) (page - 1) * PAGE_SIZE, PAGE_SIZE)
            .fetch(row -> {
                var balance = row.value2();
                return new User(row.value1()).formattedName().thenApply(name -> new BaltopEntry(name, balance));
            })).thenCompose(futures -> {
                // https://stackoverflow.com/questions/59108125/return-a-completablefuture-containing-a-list-of-completablefutures
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
            });
    }

    public static @NotNull CompletionStage<@NotNull List<@NotNull Player>> spies() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.UNIQUE_ID)
            .from(USERS)
            .where(USERS.SOCIAL_SPY.isTrue())
            .fetch(row -> Bukkit.getPlayer(row.value1()))
            .stream().filter(Objects::nonNull)
            .collect(Collectors.toList()));
    }
}
