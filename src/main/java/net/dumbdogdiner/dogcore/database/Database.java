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
    private static DSLContext create;

    /**
     * Initialize the database.
     */
    public static void init() {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        var username = Configuration.getString("db.username");
        var password = Configuration.getString("db.password");
        var database = Configuration.getString("db.database");
        var port = Configuration.getInt("db.port");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        Connection conn;
        String url;
        try {
            url = new URI("jdbc:postgresql", null, "localhost", port, "/" + database, null, null).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        create = DSL.using(conn, SQLDialect.POSTGRES);

        try (var resource = DogCorePlugin.getInstance().getResource("database.sql")) {
            if (resource == null) {
                throw new RuntimeException("required database initialization file is missing from the plugin");
            }
            var query = new String(resource.readAllBytes());
            create.execute(query);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        User.init();
    }

    /**
     * Execute a transaction and get its result.
     * @param f The transaction to execute.
     * @return A future that returns a value.
     * @param <T> The type of value to return.
     */
    public static <T> @NotNull CompletionStage<T> execute(final @NotNull TransactionalCallable<T> f) {
        return create.transactionResultAsync(f);
    }

    /**
     * Execute a transaction that does not return any result.
     * @param f The transaction to execute.
     * @return A future that completes when the transaction is finished.
     */
    public static @NotNull CompletionStage<Void> executeUpdate(final @NotNull TransactionalRunnable f) {
        return create.transactionAsync(f);
    }
}
