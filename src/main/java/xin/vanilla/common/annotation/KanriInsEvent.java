package xin.vanilla.common.annotation;

import net.mamoe.mirai.contact.MemberPermission;
import xin.vanilla.enumeration.PermissionLevel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KanriInsEvent {
    /**
     * 发送者权限
     */
    PermissionLevel sender() default PermissionLevel.PERMISSION_LEVEL_MEMBER;

    /**
     * 机器人群内权限
     */
    MemberPermission[] bot() default MemberPermission.MEMBER;

    /**
     * 前缀属性名
     */
    String prefix() default "";

    /**
     * 正则表达式
     */
    String regexp() default "";
}
