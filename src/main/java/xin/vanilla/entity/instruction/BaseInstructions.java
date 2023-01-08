package xin.vanilla.entity.instruction;

import java.util.HashSet;
import java.util.Set;

/**
 * 基础通用指令
 */
public class BaseInstructions {
    // 添加+(如: 加入黑名单)
    public Set<String> add = new HashSet<String>() {{
        add("add");
    }};
    // 删除+(如: 删除群管理)
    public Set<String> delete = new HashSet<String>() {{
        add("del");
    }};
    // 全局+(如: 全局词库)
    public Set<String> global = new HashSet<String>() {{
        add("all");
    }};
}
