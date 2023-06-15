package net.dumbdogdiner.dogcore.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TransactionalCallable;
import org.jooq.TransactionalRunnable;
import org.jooq.impl.DSL;

public final class Database {
    private Database() { }

    /**
     * The database context.
     */
    private static DSLContext create;

    /**
     * Initialize the database.
     * @param plugin The plugin to pull configuration from.
     */
    public static void init(@NotNull final DogCorePlugin plugin) {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        var username = plugin.getConfigString("db.username");
        var password = plugin.getConfigString("db.password");
        var database = plugin.getConfigString("db.database");
        var port = plugin.getConfigString("db.port");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Connection conn;
        var url = "jdbc:postgresql://localhost:" + port + "/" + database;
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        create = DSL.using(conn, SQLDialect.POSTGRES);

        try (var resource = plugin.getResource("database.sql")) {
            if (resource == null) {
                throw new RuntimeException("required database initialization file is missing from the plugin");
            }
            var query = new String(resource.readAllBytes());
            create.execute(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute a transaction and get its result.
     * @param f The transaction to execute.
     * @return A future that returns a value.
     * @param <T> The type of value to return.
     */
    public static <T> @NotNull CompletionStage<T> execute(
        @NotNull final TransactionalCallable<T> f
    ) {
        return create.transactionResultAsync(f);
    }

    /**
     * Execute a transaction that does not return any result.
     * @param f The transaction to execute.
     * @return A future that completes when the transaction is finished.
     */
    public static @NotNull CompletionStage<Void> executeUpdate(
        @NotNull final TransactionalRunnable f
    ) {
        return create.transactionAsync(f);
    }
}
