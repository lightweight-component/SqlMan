package com.ajaxjs.sqlman;

import com.ajaxjs.util.DebugTools;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * JDBC Connection
 */
@Slf4j
@Data
public class JdbcConnection {
    /**
     * Database connection
     */
    private Connection conn;

    /**
     * 当前数据库厂商，默认 MySQL
     */
    private JdbcConstants.DatabaseVendor databaseVendor = JdbcConstants.DatabaseVendor.MYSQL;

    /**
     * 获取数据库连接的回调函数
     */
    public static Supplier<Connection> GET_CONN_SUPPLIER;

    /**
     * 当前进程的数据库连接
     */
    private static final ThreadLocal<Connection> CONNECTION = new ThreadLocal<>();

    /**
     * 获取一个当前进程的数据库连接
     *
     * @return 当前进程的数据库连接对象
     */
    public static Connection getConnection() {
        return CONNECTION.get();
    }

    /**
     * 保存一个数据库连接对象到当前进程
     *
     * @param conn 当前进程的数据库连接对象
     */
    public static void setConnection(Connection conn) {
        CONNECTION.set(conn);
    }

    private long startTime;

    /**
     * Create a JDBC action with global connection
     */
    public JdbcConnection() {
        this.startTime = System.currentTimeMillis();
        Connection conn = getConnection();

        if (conn == null) {
            if (GET_CONN_SUPPLIER == null)
                throw new IllegalArgumentException("Database Connection NOT SET by config.");
            else {
                conn = GET_CONN_SUPPLIER.get();

                if (conn == null)
                    throw new IllegalArgumentException("Database Connection is NULL");
            }
        }

        this.conn = conn;
    }

    /**
     * Create a JDBC action with specified connection
     */
    public JdbcConnection(Connection conn) {
        this.startTime = System.currentTimeMillis();
        this.conn = conn;
        initDatabaseVendor();
    }

    /**
     * Create a JDBC action with specified data source
     */
    public JdbcConnection(DataSource dataSource) {
        this(getConnection(dataSource));
    }

    protected void initDatabaseVendor() {
        try {
            String databaseProductName = conn.getMetaData().getDatabaseProductName().toLowerCase();

            if (databaseProductName.contains("mysql"))
                databaseVendor = JdbcConstants.DatabaseVendor.MYSQL;
            else if (databaseProductName.contains("oracle"))
                databaseVendor = JdbcConstants.DatabaseVendor.ORACLE;
            else if (databaseProductName.contains("h2"))
                databaseVendor = JdbcConstants.DatabaseVendor.H2;
        } catch (SQLException e) {
            log.error("Getting database name error.", e);
            throw new RuntimeException("Getting database name error.", e);
        }
    }

    /**
     * 连接数据库。这种方式最简单，但是没有经过数据库连接池。
     * 有时不能把 user 和 password 写在第一个 jdbc 连接字符串上 那样会连不通
     * 分开 user 和 password 就可以
     *
     * @param jdbcUrl  数据库连接字符串，不包含用户名和密码
     * @param userName 用户
     * @param password 密码
     * @return 数据库连接对象
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
     * 连接数据库。这种方式最简单，但是没有经过数据库连接池。
     *
     * @param jdbcUrl 数据库连接字符串，已包含用户名和密码
     * @return 数据库连接对象
     */
    public static Connection getConnection(String jdbcUrl) {
        return getConnection(jdbcUrl, null, null);
    }

    /**
     * 从指定的数据源获取数据库连接
     *
     * @param dataSource 数据源对象，用于提供数据库连接
     * @return Connection 数据库连接对象
     * @throws RuntimeException 如果无法从数据源获取连接，则抛出运行时异常
     */
    public static Connection getConnection(DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.warn("Can't get a connection from a DataSource: " + dataSource, e);
            throw new RuntimeException("Can't get a connection from a DataSource: " + dataSource, e);
        }
    }

    /**
     * 一般情况用的数据库连接字符串
     */
    public static final String MYSQL_CONN = "jdbc:mysql://%s/%s?characterEncoding=utf-8&useSSL=false&autoReconnect=true&" +
            "allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull&rewriteBatchedStatements=true&serverTimezone=Asia/Shanghai";

    /**
     * 连接 MySQL 数据库
     *
     * @param ipPort   数据库地址和端口
     * @param dbName   数据库名，可为空字符串
     * @param userName 用户
     * @param password 密码
     * @return 数据库连接对象
     */
    public static Connection getMySqlConnection(String ipPort, String dbName, String userName, String password) {
        return getConnection(String.format(MYSQL_CONN, ipPort, dbName), userName, password);
    }

    /**
     * 关闭数据库连接
     *
     * @param conn 数据库连接对象
     */
    public static void closeDb(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();

                if (DebugTools.isDebug)
                    log.info("Database Connection Closed.");
            }
        } catch (SQLException e) {
            log.warn("Database Connection Closes failed.", e);
            throw new RuntimeException("Database Connection Closes failed.", e);
        }
    }

    /**
     * 关闭当前进程的数据库连接
     * 使用方式：
     * <pre>
     * try {
     *      ....
     * } finally {
     *      closeDb();
     * }
     * </pre>
     */
    public static void closeDb() {
        closeDb(getConnection());
        CONNECTION.remove();
    }
}
