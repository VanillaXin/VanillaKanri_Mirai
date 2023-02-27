package xin.vanilla.mapper;

public class Base {
    protected static String dbVersion = "1.0.1";

    protected String getTableName(String table) {
        // Date date = new Date(time * 1000);
        // return DateUtils.getYearOfDate(date) + "." + DateUtils.getMonthOfDateWithZero(date);
        return table + "_" + dbVersion;
    }
}
