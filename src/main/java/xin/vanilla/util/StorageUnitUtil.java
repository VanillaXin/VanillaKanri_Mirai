package xin.vanilla.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 存储单位换算
 **/
public class StorageUnitUtil {

    public static final BigDecimal UNIT = BigDecimal.valueOf(1024);
    public static final BigDecimal BIT = BigDecimal.valueOf(1);
    public static final BigDecimal BYTE = BigDecimal.valueOf(8);
    public static final BigDecimal KB = BYTE.multiply(UNIT);
    public static final BigDecimal MB = KB.multiply(UNIT);
    public static final BigDecimal GB = MB.multiply(UNIT);
    public static final BigDecimal TB = GB.multiply(UNIT);
    public static final Integer SCALE = 2;

    /**
     * 存储单位换算
     *
     * @param length      需要转换的存储大小
     * @param currentUnit 当前存储单位
     * @param targetUnit  转换目标存储单位
     * @param scale       小数点
     **/
    public static BigDecimal convert(BigDecimal length, BigDecimal currentUnit, BigDecimal targetUnit, Integer scale) {
        if (scale == null) {
            scale = SCALE;
        }
        if (currentUnit.compareTo(targetUnit) < 0) {
            BigDecimal b1 = targetUnit.divide(currentUnit, RoundingMode.HALF_UP);
            return length.divide(b1, scale, RoundingMode.HALF_UP);
        } else if (currentUnit.compareTo(targetUnit) > 0) {
            BigDecimal b1 = currentUnit.divide(targetUnit, RoundingMode.HALF_UP);
            return length.multiply(b1).setScale(scale, RoundingMode.HALF_UP);
        } else {
            return length;
        }
    }

    public static void main(String[] args) {
        // 测试 : 1500KB 转为 MB // 结果: 1.465
        BigDecimal convert1 = convert(new BigDecimal("1500"), KB, MB, 3);
        System.out.println(convert1);
        // 测试 : 1500b 转为 byte // 结果: 187.500
        BigDecimal convert2 = convert(new BigDecimal("1500"), BIT, BYTE, 3);
        System.out.println(convert2);
    }
}

