package com.iscms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe connection factory.
 *
 * FIX-BUG3: The original singleton shared a single Connection across all callers.
 * In a Swing app with background threads (e.g. scheduled jobs) this causes
 * silent data corruption — two threads interleave statements on the same Connection.
 *
 * Correct fix: open a new Connection per call (short-lived, always closed by the DAO's
 * try-with-resources). This is safe for a desktop app with low concurrency.
 * For a server-side app, swap in a HikariCP pool instead.
 *
 * Credentials are loaded from the classpath:
 *   1. db.properties.local  (local override, gitignored — real credentials go here)
 *   2. db.properties        (template committed to repo — contains placeholder values)
 *
 * Never hard-code credentials in source code.
 */
public class DBConnection {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        // Try local override first; fall back to committed template
        String[] candidates = { "/db.properties.local", "/db.properties" };
        boolean loaded = false;
        for (String candidate : candidates) {
            try (InputStream in = DBConnection.class.getResourceAsStream(candidate)) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                    break;
                }
            } catch (IOException ignored) {}
        }
        if (!loaded) throw new RuntimeException(
                "Neither db.properties.local nor db.properties found on classpath.");
        URL      = props.getProperty("db.url");
        USER     = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    // FIX-BUG3: Singleton with shared Connection removed.
    // getInstance() now returns a factory; getConnection() opens a fresh Connection each time.
    // Each DAO method wraps its Connection in try-with-resources, so it is closed automatically.
    private static final DBConnection INSTANCE = new DBConnection();

    private DBConnection() {}

    public static DBConnection getInstance() {
        return INSTANCE;
    }

    /** Opens and returns a new JDBC connection. Caller must close it (use try-with-resources). */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
