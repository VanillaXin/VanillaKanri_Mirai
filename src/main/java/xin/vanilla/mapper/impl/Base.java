package xin.vanilla.mapper.impl;

public class Base {
    protected static String dbVersion = "1.1.0";

    protected String getTableName(String table) {
        // Date date = new Date(time * 1000);
        // return DateUtils.getYearOfDate(date) + "." + DateUtils.getMonthOfDateWithZero(date);
        return table + "_" + dbVersion;
    }
}
