package net.dumbdogdiner.dogcore.database;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.dumbdogdiner.dogcore.chat.MiscFormatter;
import net.dumbdogdiner.dogcore.chat.NameFormatter;
import net.dumbdogdiner.dogcore.config.Configuration;
import net.dumbdogdiner.dogcore.event.DailyLoginEvent;
import net.dumbdogdiner.dogcore.messages.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

//CHECKSTYLE:OFF
import static net.dumbdogdiner.dogcore.database.schema.Tables.*;
//CHECKSTYLE:ON

public final class User {
    /** The maximum length of a nickname. */
    public static final int MAX_NICKNAME_LENGTH = 16;

    /** The size of the pages for the top balance. */
    private static final int PAGE_SIZE = 5;

    /** The error code returned by PostgreSQL when a value is out of range. */
    private static final String OUT_OF_RANGE = "22003";

    /** The error code returned by PostgreSQL when a check fails. */
    private static final String CHECK_VIOLATION = "23514";

    /** Cached users for online players. */
    private static final Map<Player, User> CACHE = new WeakHashMap<>();

    /** The initial balance for users. */
    private static long initialBalance;

    public record BaltopEntry(int index, @NotNull Component name, long amount) { }

    /** The UUID of this user. */
    private final @NotNull UUID uuid;

    /**
     * Create a user object.
     * @param userId The UUID of the user.
     */
    private User(@NotNull final UUID userId) {
        uuid = userId;
    }

    /**
     * Determine if this user is muted.
     * @return A future which returns whether the user is muted.
     */
    public @NotNull CompletionStage<Boolean> isMuted() {
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
     * @param duration The duration to mute for, or null if indefinite.
     * @return A future which completes when this user is muted.
     */
    public @NotNull CompletionStage<Void> mute(
        @Nullable final Duration duration
    ) {
        Long expires;
        if (duration == null) {
            expires = null;
        } else {
            expires = duration.toMillis() + System.currentTimeMillis();
        }
        return Database.executeUpdate(ctx -> ctx.dsl().insertInto(MUTES)
            .columns(MUTES.PLAYER_ID, MUTES.EXPIRES)
            .values(uuid, expires)
            .onConflict(MUTES.PLAYER_ID)
            .doUpdate()
            .set(MUTES.EXPIRES, expires)
            .execute());
    }

    /**
     * Unmute this user.
     * @return A future which completes when this user is unmuted.
     */
    public @NotNull CompletionStage<Void> unmute() {
        return Database.executeUpdate(ctx -> ctx.dsl().deleteFrom(MUTES)
            .where(MUTES.PLAYER_ID.eq(uuid))
            .execute());
    }

    /**
     * Get the balance of this user.
     * @return A future which returns the balance of this user.
     */
    public @NotNull CompletionStage<@NotNull Long> getBalance() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.BALANCE)
            .from(USERS)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .fetch()
            .get(0)
            .value1());
    }

    /**
     * Set the balance of this user.
     * @param balance The value to set.
     * @return A future which completes when the value is set.
     */
    public @NotNull CompletionStage<@NotNull Boolean> setBalance(final long balance) {
        return Database.execute(ctx -> {
            try {
                ctx.dsl().update(USERS)
                    .set(USERS.BALANCE, balance)
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
            } catch (DataAccessException e) {
                var s = e.sqlState();
                if (OUT_OF_RANGE.equals(s) || CHECK_VIOLATION.equals(s)) {
                    return false;
                }
                throw e;
            }
            return true;
        });
    }

    private void sendPaymentNotification(final long amount) {
        var player = Bukkit.getPlayer(uuid);
        if (player != null) {
            player.sendMessage(Messages.get("eco.received", MiscFormatter.formatCurrency(amount)));
        }
    }

    /**
     * Give or take money from this player.
     * Fails if balance would overflow or be negative.
     * @param amount the amount to give or take from the user.
     * @return A future which returns a boolean indicating the success of the
     *         transaction.
     */
    public @NotNull CompletionStage<@NotNull Boolean> give(final long amount) {
        return Database.execute(ctx -> {
            try {
                ctx.dsl().update(USERS)
                    .set(USERS.BALANCE, USERS.BALANCE.add(amount))
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
            } catch (DataAccessException e) {
                var s = e.sqlState();
                if (OUT_OF_RANGE.equals(s) || CHECK_VIOLATION.equals(s)) {
                    return false;
                }
                throw e;
            }
            // tell the player about the money that they received
            sendPaymentNotification(amount);
            return true;
        });
    }

    /**
     * Pay another player. Fails if either balance would be out of range.
     * @param other The other user to pay.
     * @param amount the amount to pay to the user.
     * @return A future which returns a boolean indicating the success of the
     *         transaction.
     */
    public @NotNull CompletionStage<@NotNull Boolean> pay(
        @NotNull final User other,
        final long amount
    ) {
        // don't allow paying ourselves, and don't allow paying negatives
        if (other.uuid.equals(uuid) || amount < 0L) {
            return CompletableFuture.completedFuture(false);
        }
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
                var s = e.sqlState();
                if (OUT_OF_RANGE.equals(s) || CHECK_VIOLATION.equals(s)) {
                    dsl.rollback().execute();
                    return false;
                }
                throw e;
            }
            // tell the player about the money that they received
            sendPaymentNotification(amount);
            return true;
        });
    }

    /**
     * Set the nickname of this user.
     * @param value The value to set.
     * @return A future which returns a boolean indicating if the new username
     *         is valid, and was set.
     */
    public @NotNull CompletionStage<@NotNull Boolean> setNickname(
        @Nullable final String value
    ) {
        if (value == null || value.length() <= MAX_NICKNAME_LENGTH) {
            return Database.execute(ctx -> ctx.dsl().update(USERS)
                .set(USERS.NICKNAME, value)
                .where(USERS.UNIQUE_ID.eq(uuid))
                .execute())
                .thenCompose(v -> {
                    var player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        return NameFormatter.refreshPlayerName(player)
                            .thenApply(w -> true);
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                });
        }
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Get the social spy status of this user.
     * @return A future which returns the social spy status.
     */
    public @NotNull CompletionStage<@NotNull Boolean> getSocialSpy() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.SOCIAL_SPY)
            .from(USERS)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .fetch()
            .get(0)
            .value1());
    }

    /**
     * Set the social spy status of this user.
     * @param value The value to set.
     * @return A future which completes when the value is set.
     */
    public @NotNull CompletionStage<Void> setSocialSpy(final boolean value) {
        return Database.executeUpdate(ctx -> ctx.dsl().update(USERS)
            .set(USERS.SOCIAL_SPY, value)
            .where(USERS.UNIQUE_ID.eq(uuid))
            .execute());
    }

    /**
     * Get the formatted name of this user.
     * @return A future which returns the formatted name of the user.
     */
    public @NotNull CompletableFuture<@NotNull Component> formattedName() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.USERNAME, USERS.NICKNAME)
                .from(USERS)
                .where(USERS.UNIQUE_ID.eq(uuid))
                .fetch()
                .get(0))
            .thenCompose(row -> NameFormatter.formatUsername(uuid, row.value1(), row.value2()))
            .toCompletableFuture();
    }

    public @NotNull CompletionStage<Void> checkDailyLogin() {
        return Database.executeUpdate(ctx -> {
            var dsl = ctx.dsl();
            var lastRewardDate = dsl.select(USERS.LAST_LOGIN_DATE)
                .from(USERS)
                .where(USERS.UNIQUE_ID.eq(uuid))
                .forUpdate()
                .fetch()
                .get(0)
                .value1();
            var today = LocalDate.now();
            if (lastRewardDate == null || today.isAfter(lastRewardDate)) {
                dsl.update(USERS)
                    .set(USERS.LAST_LOGIN_DATE, today)
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
                // fire login event
                var player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    Bukkit.getPluginManager().callEvent(new DailyLoginEvent(player));
                }
            }
        });
    }

    /**
     * Look up a user.
     * @param uuid The UUID of the player to look up.
     * @return A future which returns the user if found, or null.
     */
    public static @NotNull CompletionStage<@Nullable User> lookup(
        @NotNull final UUID uuid
    ) {
        var player = Bukkit.getPlayer(uuid);
        if (player != null) {
            var fromCache = CACHE.get(player);
            if (fromCache != null) {
                return CompletableFuture.completedFuture(fromCache);
            }
        }

        return Database.execute(ctx -> ctx.dsl()
                .fetchCount(USERS, USERS.UNIQUE_ID.eq(uuid)) != 0)
            .thenApply(exists -> {
                if (exists) {
                    var result = new User(uuid);
                    if (player != null) {
                        CACHE.put(player, result);
                    }
                    return result;
                } else {
                    return null;
                }
            });
    }

    /**
     * Look up a user.
     * @param player The player to look up.
     * @return A future which returns the user if found, or null.
     */
    public static @NotNull CompletionStage<@Nullable User> lookup(
        @NotNull final OfflinePlayer player
    ) {
        return lookup(player.getUniqueId());
    }

    /**
     * Look up a user, sending an error on failure.
     * @param player The player to look up.
     * @param sender The sender to send an error message to, if the user does
     *               not exist.
     * @param onSuccess A callback the user is passed to if they exist.
     */
    public static void lookupCommand(
        @NotNull final OfflinePlayer player,
        @NotNull final CommandSender sender,
        @NotNull final Consumer<@NotNull User> onSuccess
    ) {
        lookupCommand(player, sender, onSuccess, null);
    }

    /**
     * Look up a user, sending an error on failure.
     * @param player The player to look up.
     * @param sender The sender to send an error message to, if the user does
     *               not exist.
     * @param onSuccess A callback the user is passed to if they exist.
     * @param onFailure A callback ran if the user does not exist. Can be null.
     */
    public static void lookupCommand(
        @NotNull final OfflinePlayer player,
        @NotNull final CommandSender sender,
        @NotNull final Consumer<@NotNull User> onSuccess,
        @Nullable final Runnable onFailure
    ) {
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

    /**
     * Register this user.
     * @param player The player to register.
     * @return A future which returns whether the user was registered.
     */
    public static @NotNull CompletionStage<@NotNull Boolean> register(
        @NotNull final Player player
    ) {
        var uuid = player.getUniqueId();
        var name = player.getName();
        return Database.execute(ctx -> {
            var dsl = ctx.dsl();
            var exists = dsl.fetchCount(USERS, USERS.UNIQUE_ID.eq(uuid)) != 0;
            if (exists) {
                dsl.update(USERS)
                    .set(USERS.USERNAME, name)
                    .where(USERS.UNIQUE_ID.eq(uuid))
                    .execute();
            } else {
                dsl.insertInto(USERS)
                    .columns(USERS.UNIQUE_ID, USERS.USERNAME, USERS.BALANCE)
                    .values(uuid, name, initialBalance)
                    .execute();
            }
            return !exists;
        });
    }

    /**
     * Query the users with the highest balance.
     * @param page The page to display. Index starts at one.
     * @return A future which returns requested list of users.
     */
    public static @NotNull CompletionStage<@NotNull List<@NotNull BaltopEntry>> top(
        final int page
    ) {
        // TODO: improve efficiency of query
        return Database.execute(ctx -> ctx.dsl()
            .select(DSL.rowNumber().over(), USERS.UNIQUE_ID, USERS.BALANCE)
            .from(USERS)
            .orderBy(USERS.BALANCE.desc())
            .limit((long) (page - 1) * PAGE_SIZE, PAGE_SIZE)
            .fetch(row -> {
                var index = row.value1();
                var username = row.value2();
                var balance = row.value3();
                return new User(username)
                    .formattedName()
                    .thenApply(name -> new BaltopEntry(index, name, balance));
            })).thenCompose(futures -> {
                // https://stackoverflow.com/questions/59108125/
                var array = futures.toArray(new CompletableFuture[0]);
                return CompletableFuture.allOf(array)
                    .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()));
            });
    }

    /**
     * Query the users with social spy enabled.
     * @return A future which returns a list of social spy users;
     */
    public static @NotNull CompletionStage<@NotNull List<@NotNull Player>> spies() {
        return Database.execute(ctx -> ctx.dsl().select(USERS.UNIQUE_ID)
            .from(USERS)
            .where(USERS.SOCIAL_SPY.isTrue())
            .fetch(row -> Bukkit.getPlayer(row.value1()))
            .stream().filter(Objects::nonNull)
            .collect(Collectors.toList()));
    }

    public static @NotNull CompletionStage<@Nullable Component> nameIfNotMuted(final @NotNull CommandSender sender) {
        CompletionStage<Boolean> isMutedFuture;
        if (sender instanceof Player p) {
            var future = new CompletableFuture<Boolean>();
            isMutedFuture = future;
            User.lookupCommand(
                p,
                sender,
                user -> user.isMuted().thenAccept(future::complete),
                () -> future.complete(false)
            );
        } else {
            isMutedFuture = CompletableFuture.completedFuture(false);
        }
        return isMutedFuture.thenApply(isMuted -> {
            if (isMuted) {
                sender.sendMessage(Messages.get("error.muted"));
                return null;
            }

            if (sender instanceof Player p) {
                return p.displayName();
            } else {
                return sender.name();
            }
        });
    }

    public static void init() {
        Configuration.register(() -> {
            initialBalance = Configuration.getInt("economy.initial");
        });
    }
}
