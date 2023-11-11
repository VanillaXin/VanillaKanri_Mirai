package xin.vanilla.common.annotation;

import java.lang.annotation.*;

/**
 * 标记一个可选功能方法
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Capability {
    /**
     * 可选功能名称
     */
    String value() default "";

    /**
     * 排序值(0为关闭, 数字越大执行顺序越优先)
     */
    int sort() default 0;
}
