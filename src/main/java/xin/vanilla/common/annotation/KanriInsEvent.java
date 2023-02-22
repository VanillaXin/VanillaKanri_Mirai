package xin.vanilla.common.annotation;

import net.mamoe.mirai.contact.MemberPermission;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.enumeration.PermissionLevel;

import java.lang.annotation.*;

/**
 * 标记一个群管指令方法
 * <p>
 * 参数列表必须为 int[] param1,String param2
 */
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
     * 三级前缀 属性名
     * <p>
     * 对应{@link KanriInstructions}中的属性名
     */
    String prefix() default "";

    /**
     * 正则表达式 方法名
     * <p>
     * 对应{@link RegExpConfig}中的方法名
     */
    String regexp() default "defaultRegExp";
}
