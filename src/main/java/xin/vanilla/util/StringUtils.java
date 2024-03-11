package xin.vanilla.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.KeywordInstructions;

import java.security.SecureRandom;
import java.util.Collection;

@SuppressWarnings("unused")
public class StringUtils {

    public static final String METHOD_SET_PREFIX = "set";
    public static final String METHOD_GET_PREFIX = "get";
    public static final String COMMON_MARK = ",<.>/?;:'\"[{]}\\|`~!@#$%^&*()-_=+，《。》、？；：‘“【】·~！￥…（）—";
    /**
     * NanoId默认随机字符串生成器
     */
    public static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

    /**
     * NanoId默认随机字符串序列
     */
    public static final char[] DEFAULT_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * NanoId默认随机字符串序列
     */
    public static final char[] NUMBER_ALPHABET = "0123456789".toCharArray();

    /**
     * NanoId默认随机字符串长度
     */
    public static final int DEFAULT_SIZE = 21;

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

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    @NotNull
    public static String toString(int[] a) {
        return toString(a, ',');
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    @NotNull
    public static String toString(int[] a, char separator) {
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
            b.append(separator);
        }
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    @NotNull
    public static String toString(long[] a) {
        return toString(a, ',');
    }

    /**
     * 将数值数组 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    @NotNull
    public static String toString(long[] a, char separator) {
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
            b.append(separator);
        }
    }

    /**
     * 将数值集合 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     */
    public static String toString(Collection<?> a) {
        return toString(a, ',');
    }

    /**
     * 将数值集合 <code>[123456789, 234567890]</code>
     * <p>转换为形如 <code>123456789,234567890</code> 的字符串</p>
     *
     * @param separator 分隔符
     */
    public static String toString(Collection<?> a, char separator) {
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
                b.append(separator);
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

    /**
     * 通过关键词类型获取关键词类型名称
     */
    public static String getKeywordTypeName(String type) {
        KeywordInstructions keyword = VanillaKanri.INSTANCE.getGlobalConfig().getInstructions().getKeyword();
        if (keyword.getExactly().contains(type)) {
            return "完全匹配";
        } else if (keyword.getContain().contains(type)) {
            return "包含匹配";
        } else if (keyword.getPinyin().contains(type)) {
            return "拼音包含匹配";
        } else if (keyword.getRegex().contains(type)) {
            return "正则匹配";
        } else {
            return "未知类型";
        }
    }

    /**
     * 通过关键词类型名称获取关键词类型
     */
    public static String getKeywordType(String typeName) {
        KeywordInstructions keyword = VanillaKanri.INSTANCE.getGlobalConfig().getInstructions().getKeyword();
        if ("完全匹配".equals(typeName)) {
            return keyword.getExactly().iterator().next();
        } else if ("包含匹配".equals(typeName)) {
            return keyword.getContain().iterator().next();
        } else if ("拼音包含匹配".equals(typeName)) {
            return keyword.getPinyin().iterator().next();
        } else if ("正则匹配".equals(typeName)) {
            return keyword.getRegex().iterator().next();
        } else {
            return "";
        }
    }

    /**
     * 将String[][] 格式化为 String
     */
    public static String convertToString(String[][] stringArray, String x, String y) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stringArray.length; i++) {
            for (int j = 0; j < stringArray[i].length; j++) {
                sb.append(stringArray[i][j]);
                if (j < stringArray[i].length - 1) {
                    sb.append(x);
                }
            }
            if (i < stringArray.length - 1) {
                sb.append(y);
            }
        }
        return sb.toString();
    }

    public static boolean isNullOrEmpty(String s) {
        return null == s || s.isEmpty();
    }

    public static boolean isNullOrEmptyEx(String s) {
        return null == s || s.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isNotNull(Object s) {
        return s != null;
    }

    /**
     * @param s 字符串
     * @return 空字符串or本身
     */
    @NotNull
    public static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @NotNull
    public static String substring(String s, int start, int end) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (end < start) {
            return s;
        }
        if (length >= start && length >= end) {
            return s.substring(start, end);
        }
        return s;
    }

    @NotNull
    public static String substring(String s, int start) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (start > length) {
            return s;
        }
        return s.substring(start);
    }

    @NotNull
    public static String substringEnd(String s, int len) {
        if (isNullOrEmpty(s)) {
            return "";
        }
        int length = s.length();
        if (len > length) {
            return s;
        }
        return s.substring(0, length - len);
    }

    @NotNull
    public static String toString(String s, @NotNull String emptyDefault) {
        return StringUtils.isNullOrEmpty(s) ? emptyDefault : s;
    }

    @NotNull
    public static String randString() {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, DEFAULT_SIZE);
    }

    @NotNull
    public static String randString(String alphabet) {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, alphabet.toCharArray(), DEFAULT_SIZE);
    }

    @NotNull
    public static String randString(int size) {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, size);
    }

    @NotNull
    public static String randString(String alphabet, int size) {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, alphabet.toCharArray(), size);
    }

    @NotNull
    public static String randNumberString() {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, NUMBER_ALPHABET, DEFAULT_SIZE);
    }

    @NotNull
    public static String randNumberString(int size) {
        return NanoIdUtils.randomNanoId(DEFAULT_NUMBER_GENERATOR, NUMBER_ALPHABET, size);
    }

    public static String getAvatarUrl(long qq, int size) {
        return "http://q.qlogo.cn/g?b=qq&nk=" + qq + "&s=" + size;
    }
}
