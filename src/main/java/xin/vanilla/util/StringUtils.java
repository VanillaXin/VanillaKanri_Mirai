package xin.vanilla.util;

public class StringUtils {
    /**
     * 字符串是否为常用标点符号
     */
    public static boolean isCommonMark(String s) {
        if (s.length() != 1) return false;
        String marks = ",<.>/?;:'\"[{]}\\|`~!@#$%^&*()-_=+，《。》、？；：‘“【】·~！￥…（）—";
        return marks.contains(s);
    }
}
