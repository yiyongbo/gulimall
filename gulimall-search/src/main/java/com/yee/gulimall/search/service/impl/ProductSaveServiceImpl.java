package com.yee.gulimall.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yee.common.to.es.SkuEsModel;
import com.yee.gulimall.search.config.ElasticConfig;
import com.yee.gulimall.search.constant.EsConstant;
import com.yee.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YYB
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到ES
        // 1、给es中建立索引，product，建立好映射关系
        // 2、给es中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(item -> {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(item.getSkuId().toString());
            try {
                String s = objectMapper.writeValueAsString(item);
                indexRequest.source(s, XContentType.JSON);
                bulkRequest.add(indexRequest);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        BulkResponse bulk = client.bulk(bulkRequest, ElasticConfig.COMMON_OPTIONS);
        // TODO 如果批量错误
        boolean errors = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);

        return errors;
    }
}
