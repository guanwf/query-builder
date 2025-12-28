package com.obee.query.builder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obee.query.builder.service.QueryNode;
import com.obee.query.builder.service.SqlBuilder;
import com.obee.query.builder.service.SqlSegment;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class QueryBuilderApplicationTests {

	@Autowired
	private ObjectMapper objectMapper; // 需注入 Jackson

/*	{
		"logic": "AND",  // 根节点逻辑
			"children": [
					{
						"logic": "OR", // 嵌套分组 (左边的括号)
						"children": [
							{ "field": "id", "op": "EQ", "value": 1 },
							{ "field": "code", "op": "EQ", "value": "A" }
						]
					},
					{ "field": "age", "op": "GT", "value": 10 } // 右边的条件
			  ]
	}

 ((user_id = #{p.user_id} OR user_code = #{p.user_code}) AND age > #{p.age})
 {user_code=A, user_id=1, age=10}

	*/
	@Test
	void contextLoads() throws JSONException, JsonProcessingException {

		// 1. 定义白名单（建议放在 Config 或 常量类中）
		Map<String, String> FIELD_MAPPING = new HashMap<>();
		FIELD_MAPPING.put("id", "user_id");      // 前端传 id -> 数据库 user_id
		FIELD_MAPPING.put("code", "user_code");  // 前端传 code -> 数据库 user_code
		FIELD_MAPPING.put("age", "age");

		String requestData="{\n  \"logic\": \"AND\",\n  \"children\": [\n    {\n      \"logic\": \"OR\",\n      \"children\": [\n        {\n          \"field\": \"id\",\n          \"op\": \"EQ\",\n          \"value\": 1\n        },\n        {\n          \"field\": \"code\",\n          \"op\": \"EQ\",\n          \"value\": \"A\"\n        }\n      ]\n    },\n    {\n      \"field\": \"age\",\n      \"op\": \"GT\",\n      \"value\": 10\n    }\n  ]\n}";

		QueryNode requestJson = objectMapper.readValue(requestData, QueryNode.class);

		// 2. 解析 JSON 生成 SQL 和 参数
		SqlSegment segment = SqlBuilder.build(requestJson, FIELD_MAPPING);

		System.out.println("生成的SQL: " + segment.getSql());
		// 输出: (user_id = #{p.val_a1} OR user_code = #{p.val_b2}) AND age > #{p.val_c3}
//		生成的SQL: ((user_id = #{p.user_id} OR user_code = #{p.user_code}) AND age > #{p.age})

		System.out.println("生成的参数: " + segment.getParams());
//		生成的参数: {user_code=A, user_id=1, age=10}

	}

}
