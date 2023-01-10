package xin.vanilla.util;


import org.sqlite.JDBC;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

/**
 * sqlite util
 * <p/>
 * 部分源码引用自<a href="https://github.com/vince-styling/aSQLitePlus-android">vince-styling/aSQLitePlus-android</a>
 */
@SuppressWarnings("unused")
public class SqliteUtil {
    public static final int CLOSE_MODE_COMMIT = 1;
    public static final int CLOSE_MODE_CLOSE = 0;
    public static final int CLOSE_MODE_ROLLBACK = -1;

    public static final String PREFIX = "jdbc:sqlite:";

    private static final HashMap<String, SqliteUtil> INSTANCE = new HashMap<>();

    /**
     * connectionPool store connections
     */
    private final Deque<Connection> connectionPool;

    /**
     * init connectionPool
     *
     * @param poolSize 连接池大小
     * @param fileName 数据库文件名
     */
    private SqliteUtil(Integer poolSize, String fileName, Properties properties) throws SQLException {
        // init pool container
        if (null == poolSize || poolSize <= 0) {
            throw new RuntimeException("'poolSize' 有误, 应大于0.");
        } else {
            connectionPool = new ArrayDeque<>(poolSize);
        }

        for (int i = 0; i < poolSize; i++) {
            Connection connect = new JDBC().connect("jdbc:sqlite:" + fileName, properties);
            connect.setAutoCommit(false);
            connectionPool.add(connect);
        }
    }

    /**
     * 构造数据库连接
     *
     * @param poolSize 连接池大小
     * @param fileName 数据库文件名
     */
    public static SqliteUtil getInstance(int poolSize, String fileName, Properties properties) throws SQLException {
        fileName = fileName != null && !fileName.equals("") ? fileName : "test.db";
        properties = properties != null ? properties : new Properties();

        if (!INSTANCE.containsKey(fileName) || null == INSTANCE.get(fileName)) {
            INSTANCE.put(fileName, new SqliteUtil(poolSize, fileName, properties));
        }
        return INSTANCE.get(fileName);
    }

    /**
     * 通过数据库文件名构造数据库连接
     * <p>连接池大小默认为 5</p>
     *
     * @param fileName 数据库文件名
     */
    public static SqliteUtil getInstance(String fileName, Properties properties) throws SQLException {
        return getInstance(5, fileName, properties);
    }

    /**
     * 通过连接池大小构造数据库连接
     * <p>数据库文件名默认为 test.db</p>
     *
     * @param poolSize 连接池大小
     */
    public static SqliteUtil getInstance(Integer poolSize) throws SQLException {
        return getInstance(poolSize, null, null);
    }

    /**
     * 通过数据库文件名构造数据库连接
     * <p>连接池大小默认为 5</p>
     *
     * @param fileName 数据库文件名
     */
    public static SqliteUtil getInstance(String fileName) throws SQLException {
        return getInstance(5, fileName, null);
    }

    /**
     * 构造数据库连接
     * <p>数据库文件名默认为 test.db</p>
     * <p>连接池大小默认为 5</p>
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

    public boolean execute(String sql) {
        Connection c = this.getConn();
        Statement ps = this.getStatement(c);
        try {
            if (ps == null) return false;
            return ps.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
     * query
     *
     * @param sql SQL
     */
    public ResultSet select(String sql) {
        Connection c = this.getConn();
        Statement stm = this.getStatement(c);
        try {
            if (stm == null) return null;
            return stm.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.releaseConn(c);
        }
        return null;
    }

    public static final String METHOD_PREFIX = "set";

    public <T> T getEntity(final ResultSet resultSet, final Class<T> clazz) {
        try {
            String[] columnNames = getColumnNames(resultSet);
            T entity = clazz.getDeclaredConstructor().newInstance();

            for (String columnName : columnNames) {
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equalsIgnoreCase(METHOD_PREFIX + translateColumnName(columnName))) {
                        Class<?> paramType = method.getParameterTypes()[0];
                        // invoke the proper getter method of Cursor to putting the value in.
                        if (paramType == String.class)
                            method.invoke(entity, resultSet.getString(columnName));
                        else if (paramType == int.class)
                            method.invoke(entity, resultSet.getInt(columnName));
                        else if (paramType == short.class)
                            method.invoke(entity, resultSet.getShort(columnName));
                        else if (paramType == long.class)
                            method.invoke(entity, resultSet.getLong(columnName));
                        else if (paramType == float.class)
                            method.invoke(entity, resultSet.getFloat(columnName));
                        else if (paramType == double.class)
                            method.invoke(entity, resultSet.getDouble(columnName));
                        else if (paramType == byte[].class)
                            method.invoke(entity, resultSet.getBlob(columnName));
                        break;
                    }
                }
            }
            return entity;
        } catch (Exception ignored) {
            return null;
        }
    }

    public <T> T getEntity(Object sql, final Class<T> clazz) {
        try (ResultSet resultSet = select(sql.toString())) {
            if (!resultSet.next()) return null;
            return getEntity(resultSet, clazz);
        } catch (SQLException e) {
            return null;
        }
    }

    public <T> List<T> getList(Object sql, final Class<T> clazz, int limit) {
        try (ResultSet resultSet = select(sql.toString())) {
            List<T> list = new ArrayList<>();
            while (resultSet.next() && (limit < 0 || list.size() < limit)) {
                list.add(getEntity(resultSet, clazz));
            }
            return list;
        } catch (Exception ignored) {
            return null;
        }
    }

    public <T> List<T> getList(Object sql, final Class<T> clazz) {
        return getList(sql, clazz, -1);
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
            if (ps == null) return null;
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

    /**
     * 立即释放所有连接对象
     * <p>不会自动提交或回滚事务</p>
     */
    public static boolean closeAll() {
        return closeAll(CLOSE_MODE_CLOSE);
    }

    /**
     * 立即释放所有连接对象
     * <p>不会自动提交或回滚事务</p>
     */
    public static boolean closeAll(int mode) {
        for (String s : SqliteUtil.INSTANCE.keySet()) {
            Deque<Connection> pool = SqliteUtil.INSTANCE.get(s).connectionPool;
            try {
                Connection connection = pool.removeLast();
                if (mode == CLOSE_MODE_COMMIT) {
                    connection.commit();
                } else if (mode == CLOSE_MODE_ROLLBACK) {
                    connection.rollback();
                }
                connection.close();
            } catch (SQLException e) {
                return false;
            }
        }
        return true;
    }

    public boolean containsTable(String name) {
        try (ResultSet resultSet = select("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name = '" + name + "';")) {
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 获取列名数组
     */
    private String[] getColumnNames(ResultSet resultSet) {
        ResultSetMetaData metaData;
        try {
            metaData = resultSet.getMetaData();
            Set<String> columnNames = new HashSet<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            return columnNames.toArray(new String[]{});
        } catch (SQLException e) {
            return new String[]{};
        }
    }


    private String translateColumnName(String columnName) {
        return columnName.replaceAll("_", "");
    }
}
