package net.dumbdogdiner.dogcore.database;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletionStage;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import net.dumbdogdiner.dogcore.config.Configuration;
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
    private static final DSLContext CONTEXT;

    private static @NotNull String getDatabaseUri() {
        var database = Configuration.getString("db.database");
        var port = Configuration.getInt("db.port");
        try {
            return new URI("jdbc:postgresql", null, "localhost", port, "/" + database, null, null).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull Connection connect() {
        var username = Configuration.getString("db.username");
        var password = Configuration.getString("db.password");
        var uri = getDatabaseUri();

        try {
            return DriverManager.getConnection(uri, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull DSLContext loadContext() {
        var connection = connect();
        DSLContext result = null;

        try {
            // don't autocommit - we are using transactions
            connection.setAutoCommit(false);
            // build DSL context
            var context = DSL.using(connection, SQLDialect.POSTGRES);
            // load the DDL
            try (var resource = DogCorePlugin.getInstance().getResource("database.sql")) {
                if (resource == null) {
                    throw new RuntimeException("required database initialization file is missing from the plugin");
                }
                var query = new String(resource.readAllBytes());
                context.execute(query);
            }
            result = context;
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (result == null) {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return result;
    }

    static {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        CONTEXT = loadContext();
    }

    /**
     * Execute a transaction and get its result.
     * @param f The transaction to execute.
     * @return A future that returns a value.
     * @param <T> The type of value to return.
     */
    public static <T> @NotNull CompletionStage<T> execute(final @NotNull TransactionalCallable<T> f) {
        return CONTEXT.transactionResultAsync(f);
    }

    /**
     * Execute a transaction that does not return any result.
     * @param f The transaction to execute.
     * @return A future that completes when the transaction is finished.
     */
    public static @NotNull CompletionStage<Void> executeUpdate(final @NotNull TransactionalRunnable f) {
        return CONTEXT.transactionAsync(f);
    }
}
