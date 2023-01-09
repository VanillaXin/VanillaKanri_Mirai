import org.junit.Test;
import xin.vanilla.util.SqliteUtil;
import xin.vanilla.util.StringUtils;

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
    public void test02() throws SQLException {
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();
        sqliteUtil.containsTable("testTable");
        sqliteUtil.update(
                "CREATE TABLE testTable " +
                        "(ID INT PRIMARY KEY     NOT NULL," +
                        " NAME           TEXT    NOT NULL, " +
                        " AGE            INT     NOT NULL, " +
                        " ADDRESS        CHAR(50), " +
                        " SALARY         REAL)");
        sqliteUtil.containsTable("testTable");
    }
}
