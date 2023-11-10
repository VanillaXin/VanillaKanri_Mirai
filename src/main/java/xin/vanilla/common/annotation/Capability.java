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
}
