package com.obee.query.builder.service;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @description:
 * @author: Guanwf
 * @date: 2025/12/28 21:35
 * 解析结果封装
 */
@Data
@AllArgsConstructor
public class SqlSegment {
    private String sql; // 例如: (user_id = #{p.val_1} OR status = #{p.val_2})
    private Map<String, Object> params; // { "val_1": 1, "val_2": "A" }
}
