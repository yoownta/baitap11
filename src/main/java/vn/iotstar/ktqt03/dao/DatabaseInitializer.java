package vn.iotstar.ktqt03.dao;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import vn.iotstar.ktqt03.util.Jpa;

/** Schema/seed are versioned SQL, not Hibernate auto-update. Existing rows are never reset. */
public final class DatabaseInitializer {
    private DatabaseInitializer() {}
    public static void initialize() {
        if ("false".equalsIgnoreCase(System.getenv("KTQT_DB_INIT"))) return;
        SQLServerDataSource source = new SQLServerDataSource();
        String url = Jpa.requiredEnv("KTQT_DB_URL");
        source.setURL(url);
        source.setUser(Jpa.requiredEnv("KTQT_DB_USER"));
        source.setPassword(Jpa.requiredEnv("KTQT_DB_PASSWORD"));
        var match = java.util.regex.Pattern.compile("(?i)(?:^|;)databaseName=([A-Za-z][A-Za-z0-9_]{0,100})(?:;|$)").matcher(url);
        if (!match.find())
            throw new IllegalStateException("KTQT_DB_URL cần databaseName hợp lệ.");
        String database = match.group(1);
        try {
            source.setDatabaseName("master");
            try (Connection c = source.getConnection();
                 PreparedStatement q = c.prepareStatement("SELECT DB_ID(?)")) {
                q.setString(1, database);
                try (ResultSet r = q.executeQuery()) {
                    r.next();
                    if (r.getObject(1) == null) {
                        try (Statement s = c.createStatement()) {
                            s.executeUpdate("CREATE DATABASE [" + database + "]");
                        }
                    }
                }
            }
            source.setDatabaseName(database);
            try (Connection c = source.getConnection()) {
                c.setAutoCommit(false);
                try {
                    execute(c, """
                        DECLARE @result INT;
                        EXEC @result=sys.sp_getapplock @Resource=N'KTQT03.initialize',
                          @LockMode='Exclusive', @LockOwner='Session', @LockTimeout=30000;
                        IF @result<0 THROW 50001, 'Cannot acquire initialization lock', 1;
                        """);
                    execute(c, resource("/db/schema.sql"));
                    execute(c, resource("/db/seed.sql"));
                    execute(c, resource("/db/commerce.sql"));
                    execute(c, resource("/db/order-trigger.sql"));
                    c.commit();
                } catch (Exception ex) {
                    c.rollback();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể khởi tạo database. Kiểm tra kết nối/quyền SQL và log server.", ex);
        }
    }
    private static void execute(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) { s.execute(sql); }
    }
    private static String resource(String path) throws IOException {
        try (var in = DatabaseInitializer.class.getResourceAsStream(path)) {
            if (in == null) throw new IOException("Missing resource " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
