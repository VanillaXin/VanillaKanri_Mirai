package xin.vanilla.util;


import org.sqlite.JDBC;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Properties;

/**
 * sqlite util
 */
public class SqliteUtil {
    public static final String PREFIX = "jdbc:sqlite:";

    private static final HashMap<String, SqliteUtil> INSTANCE = new HashMap<>();

    /**
     * connectionPool store connections
     */
    private Deque<Connection> connectionPool = null;

    /**
     * init connectionPool
     */
    private SqliteUtil(Integer poolSize, String dbName, Properties properties) throws SQLException {
        // init pool container
        if (null == poolSize || poolSize <= 0) {
            throw new RuntimeException("'poolSize' 有误, 应大于0.");
        } else {
            connectionPool = new ArrayDeque<>(poolSize);
        }

        for (int i = 0; i < poolSize; i++) {
            try (Connection connect = new JDBC().connect("jdbc:sqlite:" + dbName, properties)) {
                connect.setAutoCommit(false);
                connectionPool.add(connect);
            }
        }
    }

    /**
     * construction with poolSize
     */
    public static SqliteUtil getInstance(int poolSize, String dbName, Properties properties) throws SQLException {
        dbName = dbName != null && !dbName.equals("") ? dbName.substring(PREFIX.length()) : "test.db";
        properties = properties != null ? properties : new Properties();

        if (!INSTANCE.containsKey(dbName) || null == INSTANCE.get(dbName)) {
            INSTANCE.put(dbName, new SqliteUtil(poolSize, dbName, properties));
        }
        return INSTANCE.get(dbName);
    }

    /**
     * construction with poolSize
     */
    public static SqliteUtil getInstance(String dbName, Properties properties) throws SQLException {
        return getInstance(5, dbName, properties);
    }

    /**
     * construction with poolSize
     */
    public static SqliteUtil getInstance(Integer poolSize) throws SQLException {
        return getInstance(poolSize, null, null);
    }

    /**
     * construction without poolSize
     * default size is 5
     */
    public static SqliteUtil getInstance(String dbName) throws SQLException {
        return getInstance(5, dbName, null);
    }

    /**
     * construction without poolSize
     * default size is 5
     */
    public static SqliteUtil getInstance() throws SQLException {
        return getInstance(5);
    }

    /**
     * get connection
     */
    private synchronized Connection getConn() {
        return connectionPool.removeLast();
    }

    /**
     * release connection
     */
    private void releaseConn(Connection c) {
        connectionPool.addLast(c);
    }

    /**
     * get statement
     */
    private Statement getStatement(Connection con) {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * query
     *
     * @param sql SQL
     */
    public ResultSet select(String sql) {
        Connection c = this.getConn();
        Statement stm = this.getStatement(c);
        try {
            return stm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.releaseConn(c);
        }
        return null;
    }

    /**
     * insert
     *
     * @param sql SQL
     */
    public Integer insert(String sql) {
        Connection c = this.getConn();
        Statement ps = this.getStatement(c);
        try {
            return ps.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                c.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.releaseConn(c);
        }
    }

    /**
     * update
     *
     * @param sql SQL
     */
    public Integer update(String sql) {
        return insert(sql);
    }

    /**
     * delete
     *
     * @param sql SQL
     */
    public Integer delete(String sql) {
        return insert(sql);
    }

    public boolean containsTable(String name) {
        try (ResultSet resultSet = select("SELECT COUNT(*)  FROM sqlite_master WHERE type='table' AND name = '" + name + "';")) {
            resultSet.next();
            return resultSet.getInt(0) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

}
