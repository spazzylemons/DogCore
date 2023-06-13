package net.dumbdogdiner.dogcore.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import net.dumbdogdiner.dogcore.DogCorePlugin;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TransactionalCallable;
import org.jooq.TransactionalRunnable;
import org.jooq.impl.DSL;

public final class Database {
    private Database() {}

    private static DSLContext create;

    public static void init(@NotNull DogCorePlugin plugin) {
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
        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/" + database, username, password);
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

    public static void executeUpdate(TransactionalRunnable f) {
        create.transaction(f);
    }

    public static <T> T execute(TransactionalCallable<T> f) {
        return create.transactionResult(f);
    }
}
