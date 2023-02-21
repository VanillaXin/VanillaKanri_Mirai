package xin.vanilla.common.annotation;

import net.mamoe.mirai.contact.MemberPermission;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.enumeration.PermissionLevel;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KeywordInsEvent {
    /**
     * 发送者权限
     */
    PermissionLevel sender() default PermissionLevel.PERMISSION_LEVEL_MEMBER;

    /**
     * 机器人群内权限
     */
    MemberPermission[] bot() default MemberPermission.MEMBER;

    /**
     * 三级前缀
     * <p>
     * 对应{@link KeywordInstructions}中的属性名
     */
    String prefix() default "";

    /**
     * 正则表达式
     * <p>
     * 对应{@link RegExpConfig}中的方法名
     */
    String regexp() default "";
}
