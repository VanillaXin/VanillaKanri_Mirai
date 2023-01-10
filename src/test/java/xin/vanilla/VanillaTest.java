package xin.vanilla;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.junit.Test;
import xin.vanilla.entity.TestTable;
import xin.vanilla.util.SqliteUtil;
import xin.vanilla.util.StringUtils;

import java.security.SecureRandom;
import java.sql.SQLException;

public class VanillaTest {
    @Test
    public void test01() {
        String s = "1这是第一行\n"
                + "2这是第二行\n"
                + "3这是第三行\n"
                + "4这是第四行\n"
                + "5这是第五行\n"
                + "6这是第六行\n"
                + "7这是第七行\n";

        System.out.println(StringUtils.getByLine(s, 1, 5, ""));
        System.out.println(StringUtils.getByLine(s, 1, 5, "剩余[num]行"));
    }

    @Test
    public void testCreateTable() throws SQLException {
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();
        if (!sqliteUtil.containsTable("test_table")) {
            sqliteUtil.update(
                    "CREATE TABLE test_table " +
                            "(id        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                            " name      TEXT                              NOT NULL, " +
                            " age       INTEGER                           NOT NULL, " +
                            " address   CHAR(50), " +
                            " salary    REAL)");
        }
        System.out.println(sqliteUtil.containsTable("test_table"));
    }

    @Test
    public void testInsert() throws SQLException {
        testCreateTable();
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();

        String name = NanoIdUtils.randomNanoId(new SecureRandom(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), 5);
        String age = NanoIdUtils.randomNanoId(new SecureRandom(), "0123456789".toCharArray(), 2);
        String address = NanoIdUtils.randomNanoId(new SecureRandom(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), 10);

        System.out.println(sqliteUtil.insert("INSERT INTO test_table (name, age, address, salary) " +
                "VALUES ('" + name + "', " + age + ", '" + address + "', " + (Float.parseFloat(age) * 100 / 3) + " );"));
        System.out.println(sqliteUtil.getEntity("SELECT * FROM test_table", TestTable.class));
        System.out.println(sqliteUtil.getList("SELECT * FROM test_table", TestTable.class));
        System.out.println(sqliteUtil.getList("SELECT * FROM test_table", TestTable.class, 5));
    }

}
