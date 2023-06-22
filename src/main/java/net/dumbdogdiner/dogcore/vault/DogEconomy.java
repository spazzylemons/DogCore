package net.dumbdogdiner.dogcore.vault;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dumbdogdiner.dogcore.database.User;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

@SuppressWarnings({"deprecation", "RedundantSuppression"})
public final class DogEconomy implements Economy {
    /** The maximum amount stored for an account. */
    private static final long MAX_AMOUNT = 9007199254740992L;

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "DogCore";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(final double amount) {
        return String.format("%,d", (long) amount);
    }

    @Override
    public String currencyNamePlural() {
        return "beans";
    }

    @Override
    public String currencyNameSingular() {
        return "bean";
    }

    @Override
    public boolean hasAccount(final String playerName) {
        Preconditions.checkNotNull(playerName);
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public synchronized boolean hasAccount(final OfflinePlayer player) {
        Preconditions.checkNotNull(player);
        return User.lookup(player).toCompletableFuture().join() != null;
    }

    @Override
    public boolean hasAccount(final String playerName, final String worldName) {
        Preconditions.checkNotNull(playerName);
        return hasAccount(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public boolean hasAccount(final OfflinePlayer player, final String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(final String playerName) {
        Preconditions.checkNotNull(playerName);
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public synchronized double getBalance(final OfflinePlayer player) {
        Preconditions.checkNotNull(player);
        return (double) User.lookup(player)
            .thenCompose(user -> (user == null) ? CompletableFuture.completedFuture(0L) : user.getBalance())
            .toCompletableFuture()
            .join();
    }

    @Override
    public double getBalance(final String playerName, final String world) {
        Preconditions.checkNotNull(playerName);
        return getBalance(Bukkit.getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(final OfflinePlayer player, final String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(final String playerName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public synchronized boolean has(final OfflinePlayer player, final double amount) {
        Preconditions.checkNotNull(player);
        return User.lookup(player)
            .thenCompose(user -> {
                if (user == null) {
                    return CompletableFuture.completedFuture(false);
                } else {
                    return user.getBalance().thenApply(v -> v >= amount);
                }
            })
            .toCompletableFuture()
            .join();
    }

    @Override
    public boolean has(final String playerName, final String worldName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return has(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(final OfflinePlayer player, final String worldName, final double amount) {
        return has(player, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(final String playerName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public synchronized EconomyResponse withdrawPlayer(final OfflinePlayer player, final double amount) {
        Preconditions.checkNotNull(player);
        return User.lookup(player).thenCompose(user -> {
            CompletableFuture<Boolean> transactionResult;
            if (user == null || amount > MAX_AMOUNT) {
                transactionResult = CompletableFuture.completedFuture(false);
            } else {
                transactionResult = user.give((long) -amount).toCompletableFuture();
            }
            return transactionResult.thenCompose(success -> {
                CompletableFuture<Long> balanceResult;
                if (user != null) {
                    balanceResult = user.getBalance().toCompletableFuture();
                } else {
                    balanceResult = CompletableFuture.completedFuture(0L);
                }
                return balanceResult.thenApply(balance -> {
                    var type = success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE;
                    return new EconomyResponse(amount, balance, type, null);
                });
            });
        }).toCompletableFuture().join();
    }

    @Override
    public EconomyResponse withdrawPlayer(final String playerName, final String worldName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return withdrawPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(final OfflinePlayer player, final String worldName, final double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(final String playerName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public synchronized EconomyResponse depositPlayer(final OfflinePlayer player, final double amount) {
        Preconditions.checkNotNull(player);
        return User.lookup(player).thenCompose(user -> {
            CompletableFuture<Boolean> transactionResult;
            if (user == null || amount > MAX_AMOUNT) {
                transactionResult = CompletableFuture.completedFuture(false);
            } else {
                transactionResult = user.give((long) amount).toCompletableFuture();
            }
            return transactionResult.thenCompose(success -> {
                CompletableFuture<Long> balanceResult;
                if (user != null) {
                    balanceResult = user.getBalance().toCompletableFuture();
                } else {
                    balanceResult = CompletableFuture.completedFuture(0L);
                }
                return balanceResult.thenApply(balance -> {
                    var type = success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE;
                    return new EconomyResponse(amount, balance, type, null);
                });
            });
        }).toCompletableFuture().join();
    }

    @Override
    public EconomyResponse depositPlayer(final String playerName, final String worldName, final double amount) {
        Preconditions.checkNotNull(playerName);
        return depositPlayer(Bukkit.getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(final OfflinePlayer player, final String worldName, final double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(final String name, final String player) {
        return unimplemented();
    }

    @Override
    public EconomyResponse createBank(final String name, final OfflinePlayer player) {
        return unimplemented();
    }

    @Override
    public EconomyResponse deleteBank(final String name) {
        return unimplemented();
    }

    @Override
    public EconomyResponse bankBalance(final String name) {
        return unimplemented();
    }

    @Override
    public EconomyResponse bankHas(final String name, final double amount) {
        return unimplemented();
    }

    @Override
    public EconomyResponse bankWithdraw(final String name, final double amount) {
        return unimplemented();
    }

    @Override
    public EconomyResponse bankDeposit(final String name, final double amount) {
        return unimplemented();
    }

    @Override
    public EconomyResponse isBankOwner(final String name, final String playerName) {
        return unimplemented();
    }

    @Override
    public EconomyResponse isBankOwner(final String name, final OfflinePlayer player) {
        return unimplemented();
    }

    @Override
    public EconomyResponse isBankMember(final String name, final String playerName) {
        return unimplemented();
    }

    @Override
    public EconomyResponse isBankMember(final String name, final OfflinePlayer player) {
        return unimplemented();
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean createPlayerAccount(final OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(final OfflinePlayer player, final String worldName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(final String playerName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(final String playerName, final String worldName) {
        return false;
    }

    private static EconomyResponse unimplemented() {
        return new EconomyResponse(0.0, 0.0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, null);
    }
}
