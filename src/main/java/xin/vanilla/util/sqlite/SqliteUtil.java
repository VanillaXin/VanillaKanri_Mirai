package xin.vanilla.util.sqlite;


import org.sqlite.JDBC;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.statement.Parenthesize;
import xin.vanilla.util.sqlite.statement.QueryStatement;
import xin.vanilla.util.sqlite.statement.Statement;

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
    /**
     * 提交已创建的事务
     */
    public static final int CLOSE_MODE_COMMIT = 1;
    /**
     * 直接关闭
     */
    public static final int CLOSE_MODE_CLOSE = 0;
    /**
     * 回滚未提交的事务
     */
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
     * @param url      数据库地址
     */
    private SqliteUtil(Integer poolSize, String url, Properties properties) throws SQLException {
        // init pool container
        if (null == poolSize || poolSize <= 0) {
            throw new RuntimeException("连接池大小(poolSize)有误, 应大于0.");
        } else {
            connectionPool = new ArrayDeque<>(poolSize);
        }

        for (int i = 0; i < poolSize; i++) {
            Connection connect = new JDBC().connect(url, properties);
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
        if (!fileName.startsWith(PREFIX)) fileName = PREFIX + fileName;
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
    private PreparedStatement getStatement(Connection con, String sql) {
        try {
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 执行SQL语句
     */
    public int executeSql(String sql) {
        Connection c = this.getConn();
        PreparedStatement ps = this.getStatement(c, sql);
        try {
            if (ps == null) return 0;
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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
     * 执行查询操作
     */
    public ResultSet select(Statement statement) {
        Connection c = this.getConn();
        PreparedStatement ps = this.getStatement(c, statement.toString());
        try {
            if (setStatement(statement, ps)) return null;
            return ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            this.releaseConn(c);
        }
        return null;
    }

    /**
     * 执行插入操作
     */
    public Integer insert(Statement statement) {
        Connection c = this.getConn();
        PreparedStatement ps = this.getStatement(c, statement.toString());
        try {
            if (setStatement(statement, ps)) return -1;
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
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
     * 设置参数
     */
    private boolean setStatement(Statement statement, PreparedStatement ps) throws SQLException {
        if (ps == null) return true;
        List<Object> operands = statement.operands;
        for (int i = 0; i < operands.size(); i++) {
            Object object = operands.get(i);
            if (object instanceof Integer)
                ps.setInt(i + 1, (Integer) object);
            else if (object instanceof Long)
                ps.setLong(i + 1, (Long) object);
            else if (object instanceof Float)
                ps.setFloat(i + 1, (Float) object);
            else if (object instanceof Double)
                ps.setDouble(i + 1, (Double) object);
            else if (object instanceof String)
                ps.setString(i + 1, object.toString());
            else if (object instanceof Byte)
                ps.setByte(i + 1, (Byte) object);
            else if (object instanceof byte[])
                ps.setBytes(i + 1, (byte[]) object);
            else
                ps.setObject(i + 1, object);
        }
        return false;
    }

    /**
     * 执行更新操作
     */
    public Integer update(Statement statement) {
        return insert(statement);
    }

    /**
     * 执行删除操作
     */
    public Integer delete(Statement statement) {
        return insert(statement);
    }

    public <T> T getEntity(final ResultSet resultSet, final Class<T> clazz) {
        try {
            String[] columnNames = getColumnNames(resultSet);
            T entity = clazz.getDeclaredConstructor().newInstance();

            for (String columnName : columnNames) {
                for (Method method : clazz.getMethods()) {
                    if (method.getName().equalsIgnoreCase(StringUtils.METHOD_SET_PREFIX + translateColumnName(columnName))) {
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

    /**
     * 查询对象
     */
    public <T> T getEntity(Statement statement, final Class<T> clazz) {
        try (ResultSet resultSet = select(statement)) {
            if (resultSet == null || !resultSet.next()) return null;
            return getEntity(resultSet, clazz);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 查询对象列表
     */
    public <T> List<T> getList(Statement statement, final Class<T> clazz) {
        try (ResultSet resultSet = select(statement)) {
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(getEntity(resultSet, clazz));
            }
            return list;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    /**
     * 查询对象列表
     */
    public <T> void getList(Statement statement, ArrayList<T> list, final Class<T> clazz) {
        try (ResultSet resultSet = select(statement)) {
            while (resultSet.next()) {
                list.add(getEntity(resultSet, clazz));
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 分页获取对象列表, 并返回分页信息
     * <p/>
     * 首先执行 "SELECT count(*) FROM (sql)" 以获取数据条数
     * <p/>
     * 然后执行"(sql) LIMIT 10 OFFSET 20" 以获取分页数据并塞入集合
     *
     * @param pageNo        当前页数
     * @param pageItemCount 每页条数
     * @param clazz         实体类
     */
    public <T> PaginationList<T> getPaginationList(Statement sql, int pageNo, int pageItemCount, final Class<T> clazz) {
        int totalItemCount = getInt(QueryStatement.rowCount().from(new Parenthesize(sql)));
        if (totalItemCount > 0) {
            PaginationList<T> records = new PaginationList<>();
            getList(sql.copy().limit(pageItemCount, (pageNo - 1) * pageItemCount), records, clazz);
            return records.setPagination(pageNo, pageItemCount, totalItemCount);
        }
        return new PaginationList<>(pageNo, pageItemCount, totalItemCount);
    }

    /**
     * 执行查询并获取第一行第一列中的int值, 查询失败将返回默认值0
     */
    public int getInt(Object sql) {
        return getInt(sql, 0);
    }

    /**
     * 执行查询并获取第一行第一列中的int值, 查询失败将返回给定的默认值 def
     *
     * @param def 默认值
     */
    public int getInt(Object sql, int def) {
        try {
            return getInts(sql)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * 执行查询并返回第一行中的所有列的int值数组
     */
    public int[] getInts(Object sql) {
        Connection c = this.getConn();
        PreparedStatement ps = this.getStatement(c, sql.toString());
        try {
            if (ps == null) return null;
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.first()) {
                int columnCount = resultSet.getMetaData().getColumnCount();
                int[] result = new int[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    result[i] = resultSet.getInt(i);
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                c.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.releaseConn(c);
        }
        return null;
    }

    /**
     * 执行查询并获取第一行第一列中的String值, 查询失败将返回默认值null
     */
    public String getString(Object sql) {
        return getString(sql, null);
    }

    /**
     * 执行查询并获取第一行第一列中的String值, 查询失败将返回给定的默认值 def
     *
     * @param def 默认值
     */
    public String getString(Object sql, String def) {
        try {
            return getStrings(sql)[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return def;
    }

    /**
     * 执行查询并返回第一行中的所有列的String值数组
     */
    public String[] getStrings(Object sql) {
        Connection c = this.getConn();
        PreparedStatement ps = this.getStatement(c, sql.toString());
        try {
            if (ps == null) return null;
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.first()) {
                int columnCount = resultSet.getMetaData().getColumnCount();
                String[] result = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    result[i] = resultSet.getString(i);
                }
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                c.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.releaseConn(c);
        }
        return null;
    }

    /**
     * 立即释放所有连接对象
     * <p>不会自动提交或回滚事务</p>
     * <p/>
     * 使用 <strong>CLOSE_MODE_COMMIT</strong> 在释放前提交所有事务
     * <p>
     * 使用 <strong>CLOSE_MODE_ROLLBACK</strong> 在释放前回滚所有事务
     */
    public static boolean closeAll() {
        return closeAll(CLOSE_MODE_CLOSE);
    }

    /**
     * 立即释放所有连接对象
     * <p/>
     * 使用 <strong>CLOSE_MODE_COMMIT</strong> 在释放前提交所有事务
     * <p>
     * 使用 <strong>CLOSE_MODE_ROLLBACK</strong> 在释放前回滚所有事务
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

    /**
     * 数据库是否存在某表
     */
    public boolean containsTable(String name) {
        Statement statement = QueryStatement.rowCount()
                .from("sqlite_master")
                .where("type").eq("table")
                .and("name").eq(name);
        try (ResultSet resultSet = select(statement)) {
            resultSet.next();
            return resultSet.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 获取指定表的新插入行ID。 如果给定的表没有 INTEGER 主键，则返回 0。
     */
    public int getLastInsertRowId(CharSequence table) {
        Statement statement = QueryStatement.produce("seq").from("sqlite_sequence").where("name").eq(table);
        try (ResultSet resultSet = select(statement)) {
            if (!resultSet.next()) return 0;
            return resultSet.getInt(1);
        } catch (SQLException e) {
            return 0;
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

    /**
     * 转换列名
     */
    private String translateColumnName(String columnName) {
        return columnName.replaceAll("_", "");
    }
}
