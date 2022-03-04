package com.yee.gulimall.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.nodes.Ingest;
import com.yee.common.to.MemberPrice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    ElasticsearchClient client;

    @Test
    void searchData() throws IOException {
        SearchResponse<MemberPrice> user = client.search(
                s -> s.index("memberprice")
                        .query(q -> q.match(m -> m.field("name").query(FieldValue.of("aab"))))
                , MemberPrice.class);
        user.hits().hits().forEach(item -> System.out.println(item.source()));
    }

    /**
     * 测试存储数据到ES
     * 更新也可以
     */
    @Test
    void indexData() throws IOException {
        MemberPrice memberPrice = new MemberPrice();
        memberPrice.setName("yee aab");
        // memberPrice.setPrice(BigDecimal.ZERO);
        // User user = new User();
        // user.setName("yee");
        // user.setAge(23);

        IndexResponse indexResponse = client.index(x -> x.index("memberprice").id("1").document(memberPrice));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Integer age;
    }

    @Test
    void contextLoads() {
        System.out.println(client);
    }

}
