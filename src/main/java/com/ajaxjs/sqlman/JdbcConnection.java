package com.ajaxjs.sqlman;

import com.ajaxjs.sqlman.model.DatabaseVendor;
import com.ajaxjs.util.ObjectHelper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Objects;

/**
 * JDBC database connection utility.
 * <p>
 * Provides methods for creating, binding, detecting, and closing database connections.
 * </p>
 */
@Slf4j
@UtilityClass
public class JdbcConnection {
    /**
     * Database connection bound to the current thread.
     */
    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    /**
     * Returns the database connection bound to the current thread.
     *
     * @return the database connection bound to the current thread
     * @throws IllegalStateException if no connection is bound to the current thread, or if the bound connection has already been closed
     */
    public static Connection getConnection() {
        Connection conn = CONNECTION.get();

        if (conn == null)
            throw new IllegalStateException("No DB connection is bound to the current thread.");

        try {
            if (conn.isClosed()) {
                CONNECTION.remove();
                throw new IllegalStateException("The DB connection bound to the current thread is closed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check DB connection state.", e);
        }

        return conn;
    }

    /**
     * Binds a database connection to the current thread.
     * <p>
     * An existing connection cannot be silently replaced by another connection.
     * Passing {@code null} removes the current thread binding.
     * </p>
     *
     * @param conn the database connection to bind, or {@code null} to remove the current binding
     * @throws IllegalStateException if another database connection is already bound to the current thread
     */
    public static void setConnection(Connection conn) {
        if (conn == null) {
            CONNECTION.remove();
            return;
        }

        Connection old = CONNECTION.get();

        if (old != null && old != conn) {
            try {
                if (!old.isClosed())
                    throw new IllegalStateException("A DB connection is already bound to the current thread.");

                CONNECTION.remove();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to check DB connection state.", e);
            }
        }

        CONNECTION.set(conn);
    }

    /**
     * Detects the database vendor from the given JDBC connection.
     *
     * @param conn the JDBC database connection
     * @return the detected database vendor
     * @throws NullPointerException          if {@code conn} is {@code null}
     * @throws UnsupportedOperationException if the database vendor is not supported
     * @throws RuntimeException              if the database metadata cannot be obtained
     */
    static DatabaseVendor detectDatabaseVendor(Connection conn) {
        try {
            String databaseProductName = conn.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);

            if (databaseProductName.contains("mariadb"))
                return DatabaseVendor.MARIADB;
            else if (databaseProductName.contains("mysql"))
                return DatabaseVendor.MYSQL;
            else if (databaseProductName.contains("oracle"))
                return DatabaseVendor.ORACLE;
            else if (databaseProductName.contains("postgresql"))
                return DatabaseVendor.POSTGRESQL;
            else if (databaseProductName.contains("sqlite"))
                return DatabaseVendor.SQLLITE;
            else if (databaseProductName.contains("h2"))
                return DatabaseVendor.H2;
            else if (databaseProductName.contains("hsql"))
                return DatabaseVendor.HSQLDB;
            else if (databaseProductName.contains("derby"))
                return DatabaseVendor.DERBY;
            else if (databaseProductName.contains("sqlserver") || databaseProductName.contains("sql server"))
                return DatabaseVendor.SQL_SERVER;
            else if (databaseProductName.contains("db2"))
                return DatabaseVendor.DB2;

            throw new UnsupportedOperationException("Unsupported database: " + databaseProductName);
        } catch (SQLException e) {
            log.error("Obtains database name error.", e);
            throw new RuntimeException("Obtains database name error.", e);
        }
    }

    /**
     * Creates a database connection using the specified JDBC URL, username, and password.
     * <p>
     * The connection is created directly through {@link DriverManager} and does not use
     * a connection pool.
     * </p>
     *
     * @param jdbcUrl  the JDBC connection URL
     * @param userName the database username, may be {@code null}
     * @param password the database password, may be {@code null} or empty
     * @return a newly created database connection
     * @throws RuntimeException if the database connection cannot be created
     */
    public static Connection getConnection(String jdbcUrl, String userName, String password) {
        Connection conn;

        try {
            if (ObjectHelper.hasText(userName) && ObjectHelper.hasText(password))
                conn = DriverManager.getConnection(jdbcUrl, userName, password);
            else
                conn = DriverManager.getConnection(jdbcUrl);

            log.info("数据库连接成功： {}", conn.getMetaData().getURL());
        } catch (SQLException e) {
            log.error("Connect to database failed！", e);
            throw new RuntimeException("Connect to database failed！", e);
        }

        return conn;
    }

    /**
     * Creates a database connection using the specified JDBC URL.
     * <p>
     * The JDBC URL may contain all connection parameters required by the JDBC driver,
     * including authentication information.
     * </p>
     *
     * @param jdbcUrl the JDBC connection URL
     * @return a newly created database connection
     * @throws RuntimeException if the database connection cannot be created
     */
    public static Connection getConnection(String jdbcUrl) {
        return getConnection(jdbcUrl, null, null);
    }

    /**
     * Obtains a database connection from the specified data source.
     *
     * @param dataSource the data source
     * @return a database connection obtained from the data source
     * @throws NullPointerException if {@code dataSource} is {@code null}
     * @throws RuntimeException     if a connection cannot be obtained from the data source
     */
    public static Connection getConnection(DataSource dataSource) {
        Objects.requireNonNull(dataSource, "DataSource must not be null.");

        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.warn("Can't get a connection from a DataSource: " + dataSource, e);
            throw new RuntimeException("Can't get a connection from a DataSource: " + dataSource, e);
        }
    }

    /**
     * Default MySQL JDBC connection URL template.
     * <p>
     * The first placeholder represents the database server address and port,
     * and the second placeholder represents the database name.
     * </p>
     */
    public static final String MYSQL_CONN = "jdbc:mysql://%s/%s?characterEncoding=utf-8&useSSL=false&autoReconnect=true&" +
            "allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai";

    /**
     * Creates a MySQL database connection.
     * <p>
     * The JDBC URL is constructed from the specified server address, database name,
     * username, and password, and the connection is created directly through
     * {@link DriverManager}.
     * </p>
     *
     * @param ipPort   the database server address and port, for example {@code localhost:3306}
     * @param dbName   the database name may be empty
     * @param userName the database username
     * @param password the database password may be empty
     * @return a newly created MySQL database connection
     * @throws RuntimeException if the database connection cannot be created
     */
    public static Connection getMySqlConnection(String ipPort, String dbName, String userName, String password) {
        return getConnection(String.format(MYSQL_CONN, ipPort, dbName), userName, password);
    }

    /**
     * Closes the specified database connection.
     *
     * @param conn the database connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn == null)
            return;

        try {
            if (!conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            log.warn("Failed to close database connection.", e);
        }
    }

    /**
     * Closes and removes the database connection bound to the current thread.
     * <p>
     * Typical usage:
     * </p>
     * <pre>
     * try {
     *     ....
     * } finally {
     *     closeConnection();
     * }
     * </pre>
     */
    public static void closeConnection() {
        Connection conn = CONNECTION.get();

        try {
            closeConnection(conn);
        } finally {
            CONNECTION.remove();
        }
    }
}
