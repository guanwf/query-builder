package com.obee.query.builder.service;

import com.obee.query.builder.model.Logic;
import com.obee.query.builder.model.Op;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: Guanwf
 * @date: 2025/12/28 21:34
 *
 * 查询节点 (既可以是组，也可以是条件)
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryNode {
    // === 分组节点属性 ===
    private Logic logic;
    private List<QueryNode> children;

    // === 叶子节点属性 ===
    private String field;
    private Op op;
    private Object value;

    public boolean isGroup() {
        return children != null && !children.isEmpty();
    }
}
