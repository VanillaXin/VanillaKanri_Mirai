package xin.vanilla.util;

import lombok.Getter;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegUtils {
    private StringBuilder statement = new StringBuilder(512);

    @Getter
    private Pattern pattern;

    @Getter
    private Matcher matcher;

    public static final String REG_SEPARATOR = "\\s";
    public static final String REG_ATCODE = "(?:(?:" +
            StringUtils.escapeExprSpecialWord(new At(2333333333L).toString()).replace("2333333333", "\\d{6,10}")
            + "|" + StringUtils.escapeExprSpecialWord(AtAll.INSTANCE.toString())
            + "|\\d{6,10})" + REG_SEPARATOR + "?)";

    public static RegUtils start() {
        RegUtils regStmt = new RegUtils();
        regStmt.statement.append("^");
        return regStmt;
    }

    public RegUtils end() {
        statement.append("$");
        return this;
    }

    public String build() {
        return statement.toString();
    }

    /**
     * () 捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param cols 多个由|分隔
     */
    public RegUtils group(Collection<?> cols) {
        statement.append("(");
        processGroup(cols.toArray());
        statement.append(")");
        return this;
    }

    /**
     * () 捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param objects 多个由|分隔
     */
    public RegUtils group(Object... objects) {
        statement.append("(");
        processGroup(objects);
        statement.append(")");
        return this;
    }

    /**
     * (?&lt;name&gt;) 命名捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param cols 多个由|分隔
     */
    public RegUtils groupByName(String name, Collection<?> cols) {
        statement.append("(?<").append(name).append(">");
        processGroup(cols.toArray());
        statement.append(")");
        return this;
    }

    /**
     * (?&lt;name&gt;) 命名捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param objects 多个由|分隔
     */
    public RegUtils groupByName(String name, Object... objects) {
        statement.append("(?<").append(name).append(">");
        processGroup(objects);
        statement.append(")");
        return this;
    }

    /**
     * () 捕获组
     * <p>
     * 不进行特殊字符转义
     */
    public RegUtils groupIg(String str) {
        statement.append("(").append(str).append(")");
        return this;
    }

    /**
     * (?&lt;name&gt;) 命名捕获组
     * <p>
     * 不进行特殊字符转义
     */
    public RegUtils groupIgByName(String name, String... str) {
        statement.append("(?<").append(name).append(">");
        for (int i = 0; i < str.length; i++) {
            if (i > 0) statement.append("|");
            statement.append(str[i]);
        }
        statement.append(")");
        return this;
    }

    /**
     * (?:) 非捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param cols 多个由|分隔
     */
    public RegUtils groupNon(Collection<?> cols) {
        statement.append("(?:");
        processGroup(cols.toArray());
        statement.append(")");
        return this;
    }

    /**
     * (?:) 非捕获组
     * <p>
     * 自动转义正则特殊字符
     *
     * @param objects 多个由|分隔
     */
    public RegUtils groupNon(Object... objects) {
        statement.append("(?:");
        processGroup(objects);
        statement.append(")");
        return this;
    }

    /**
     * (?:) 非捕获组
     * <p>
     * 不进行特殊字符转义
     */
    public RegUtils groupNonIg(String str) {
        statement.append("(?:").append(str).append(")");
        return this;
    }

    /**
     * 字符集合
     */
    public RegUtils characters(Collection<?> cols) {
        statement.append("[");
        for (Object col : cols) {
            statement.append(StringUtils.escapeExprSpecialWord(col.toString()));
        }
        statement.append("]");
        return this;
    }

    /**
     * 字符集合
     */
    public RegUtils characters(Object... objects) {
        statement.append("[");
        for (Object object : objects) {
            statement.append(StringUtils.escapeExprSpecialWord(object.toString()));
        }
        statement.append("]");
        return this;
    }

    /**
     * 否定字符集合
     */
    public RegUtils charactersNon(Collection<?> cols) {
        statement.append("[^");
        for (Object col : cols) {
            statement.append(StringUtils.escapeExprSpecialWord(col.toString()));
        }
        statement.append("]");
        return this;
    }

    /**
     * 否定字符集合
     */
    public RegUtils charactersNon(Object... objects) {
        statement.append("[^");
        for (Object object : objects) {
            statement.append(StringUtils.escapeExprSpecialWord(object.toString()));
        }
        statement.append("]");
        return this;
    }

    public RegUtils append(Object o) {
        statement.append(o);
        return this;
    }

    public RegUtils separator() {
        statement.append(REG_SEPARATOR);
        return this;
    }

    public RegUtils separator(String opr) {
        statement.append(REG_SEPARATOR).append(opr);
        return this;
    }

    public Pattern compile() {
        pattern = Pattern.compile(build(), Pattern.DOTALL);
        return pattern;
    }

    public Pattern compile(int flags) {
        pattern = Pattern.compile(build(), flags);
        return pattern;
    }

    public Matcher matcher(String s) {
        if (pattern == null) {
            compile();
        }
        matcher = pattern.matcher(s);
        return matcher;
    }

    public boolean find() {
        if (pattern == null) return false;
        if (matcher == null) return false;
        return matcher.find();
    }

    protected void processGroup(Object... values) {
        if (values != null && values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                if (i > 0) statement.append("|");
                statement.append(StringUtils.escapeExprSpecialWord(values[i].toString()));
            }
        }
    }

    public static int containsRegSeparator(String s) {
        Matcher matcher = Pattern.compile(RegUtils.REG_SEPARATOR).matcher(s);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }
}
