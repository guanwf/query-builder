package com.obee.query.builder.service;

import com.obee.query.builder.model.Op;

import java.util.*;

import static com.obee.query.builder.model.Op.*;

/**
 * @description:
 * @author: Guanwf
 * @date: 2025/12/28 21:35
 */
public class SqlBuilder {

    /**
     * 生成不带减号的 UUID
     * JDK 原生实现，替代 IdUtil.simpleUUID()
     */
    private static String generateUniqueKey() {
        return UUID.randomUUID().toString().replace("-", "");
    }


    /**
     * @param node    前端传的 JSON 树根节点
     * @param mapping 字段白名单映射 <前端字段名, 数据库列名>
     */
    public static SqlSegment build(QueryNode node, Map<String, String> mapping) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        // 递归构建
        recursiveBuild(node, sql, params, mapping);

        return new SqlSegment(sql.toString(), params);
    }

    private static void recursiveBuild(QueryNode node, StringBuilder sb,
                                       Map<String, Object> params,
                                       Map<String, String> mapping) {
        if (node == null) return;

        // === 场景1：处理分组（递归处理括号）===
        if (node.isGroup()) {
            List<QueryNode> children = node.getChildren();
            if (children.isEmpty()) return;

            sb.append("("); // 括号开始
            for (int i = 0; i < children.size(); i++) {
                // 递归调用
                recursiveBuild(children.get(i), sb, params, mapping);

                // 拼接逻辑符 (AND/OR)，最后一个元素后面不拼
                if (i < children.size() - 1) {
                    sb.append(" ").append(node.getLogic().name()).append(" ");
                }
            }
            sb.append(")"); // 括号结束
            return;
        }

        // === 场景2：处理具体的查询条件 ===
        String dbColumn = mapping.get(node.getField());
        if (dbColumn == null) {
            // 安全守卫：如果字段不在白名单，跳过，防止前端探测数据库字段
            // 也可以抛出异常 throw new IllegalArgumentException("非法字段: " + node.getField());
            sb.append("1=1");
            return;
        }

        // 生成唯一的参数 Key，避免冲突
        String paramKey = "val_" + generateUniqueKey();
        paramKey = dbColumn;

        // 拼接 SQL 片段
        buildConditionSql(sb, dbColumn, node.getOp(), paramKey, node.getValue(), params);
    }

    private static void buildConditionSql(StringBuilder sb, String column, Op op,
                                          String key, Object val, Map<String, Object> params) {
        // 注意：这里生成的 #{p.key} 是为了配合 MyBatis 的 Map 参数读取方式
        String placeHolder = "#{p." + key + "}";

        switch (op) {
            case EQ:
                sb.append(column).append(" = ").append(placeHolder);
                params.put(key, val);
                break;
            case LIKE:
                sb.append(column).append(" LIKE ").append(placeHolder);
                params.put(key, "%" + val + "%"); // 后端控制模糊匹配方式
                break;
            case IN:
                // IN 需要特殊处理：IN (#{p.key_0}, #{p.key_1})
                handleInList(sb, column, key, val, params);
                break;
            case GT:
                sb.append(column).append(" > ").append(placeHolder);
                params.put(key, val);
                break;
            case LT:
                sb.append(column).append(" < ").append(placeHolder);
                params.put(key, val);
                break;
            case GE:
                sb.append(column).append(" >= ").append(placeHolder);
                params.put(key, val);
                break;
            case LE:
                sb.append(column).append(" <= ").append(placeHolder);
                params.put(key, val);
                break;
            case NE:
                sb.append(column).append(" != ").append(placeHolder);
                params.put(key, val);
                break;
            // ... 其他操作符 (LT, GE, LE, IS_NULL)
            default:
                break;
        }
    }

    // 处理 IN 查询的参数展开
    private static void handleInList(StringBuilder sb, String column, String baseKey,
                                     Object val, Map<String, Object> params) {
        if (val instanceof Collection) {
            Collection<?> list = (Collection<?>) val;
            if (list.isEmpty()) {
                sb.append("1=0"); // 空集合查不到
                return;
            }
            sb.append(column).append(" IN (");
            int idx = 0;
            for (Object item : list) {
                String itemKey = baseKey + "_" + idx;
                if (idx > 0) sb.append(", ");
                sb.append("#{p.").append(itemKey).append("}");
                params.put(itemKey, item);
                idx++;
            }
            sb.append(")");
        }
    }

}
