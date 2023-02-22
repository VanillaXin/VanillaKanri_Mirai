package xin.vanilla.common.annotation;

import java.lang.annotation.*;

/**
 * 标记一个关键词指令方法
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KeywordInsEvent {
}
