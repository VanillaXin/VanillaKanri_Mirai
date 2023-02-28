package xin.vanilla.util;

import java.util.Collection;

public class StringUtils {

    public static final String METHOD_SET_PREFIX = "set";
    public static final String METHOD_GET_PREFIX = "get";
    public static final String COMMON_MARK = ",<.>/?;:'\"[{]}\\|`~!@#$%^&*()-_=+，《。》、？；：‘“【】·~！￥…（）—";

    /**
     * 字符串是否为常用标点符号
     */
    public static boolean isCommonMark(String s) {
        if (s.length() != 1) return false;
        return COMMON_MARK.contains(s);
    }

    /**
     * 根据行号截取字符串
     * <p>(开始堆粪</p>
     *
     * @param suffix 如果结尾还有内容, 是否需要添加的后缀, 例: "后面还有[num]行"
     */
    public static String getByLine(String s, int start, int end, String suffix) {
        if (start > end) return s;
        String code;
        if (s.contains("\r\n")) code = "\r\n";
        else if (s.contains("\r")) code = "\r";
        else if (s.contains("\n")) code = "\n";
        else return s;

        String[] split = s.split(code);
        if (start > split.length) return s;
        if (end >= split.length) {
            StringBuilder back = new StringBuilder();
            for (int i = start - 1; i < split.length; i++) {
                if (i != start - 1) back.append(code);
                back.append(split[i]);
            }
            return back.toString();
        }

        StringBuilder back = new StringBuilder();
        for (int i = start - 1; i < end; i++) {
            if (i != start - 1) back.append(code);
            back.append(split[i]);
        }
        if (!"".equals(suffix))
            back.append(code).append(suffix.replace("[num]", split.length - end + ""));
        return back.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return null == s || s.equals("");
    }

    public static boolean isNullOrEmptyEx(String s) {
        return null == s || s.trim().equals("");
    }

    public static String toString(int[] a) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(",");
        }
    }

    public static String toString(long[] a) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(",");
        }
    }

    public static String toString(Collection<?> a) {
        if (a == null)
            return "null";
        // a = Arrays.stream(a).sorted().toArray();
        int iMax = a.size() - 1;
        if (iMax == -1)
            return "";

        StringBuilder b = new StringBuilder();
        int i = 0;
        for (Object o : a) {
            b.append(o);
            i++;
            if (i <= iMax)
                b.append(",");
        }
        return b.toString();
    }

    /**
     * 转义正则特殊字符  $()*+.[]?\^{},|
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (!StringUtils.isNullOrEmpty(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 获取tap指令限制时间key
     *
     * @param group  群号
     * @param sender 发送者
     * @return tap指令限制时间 key
     */
    public static String getTapTimeKey(long group, long sender) {
        return group + "." + sender + ".tap";
    }

    /**
     * 将字符串转为逻辑真假
     *
     * @param s 0|1|真|假|是|否|true|false|y|n|t|f
     */
    public static boolean stringToBoolean(String s) {
        if (null == s) return false;
        switch (s.toLowerCase().trim()) {
            case "1":
            case "真":
            case "是":
            case "true":
            case "y":
            case "t":
                return true;
            case "0":
            case "假":
            case "否":
            case "false":
            case "n":
            case "f":
            default:
                return false;
        }
    }
}
