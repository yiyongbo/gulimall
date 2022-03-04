package com.yee.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.yee.common.to.es.SkuEsModel;
import com.yee.gulimall.search.constant.EsConstant;
import com.yee.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author YYB
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    ElasticsearchClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到ES
        // 1、给es中建立索引，product，建立好映射关系
        // 2、给es中保存这些数据
        List<BulkOperation> bulkOperations = skuEsModels.stream().map(item -> BulkOperation.of(
                b -> b.index(
                        c -> c.id(item.getSkuId().toString()).document(item)
                )
        )).collect(Collectors.toList());
        BulkResponse bulk = client.bulk(
                a -> a.index(EsConstant.PRODUCT_INDEX).operations(bulkOperations)
        );
        // TODO 如果批量错误
        boolean errors = bulk.errors();
        List<String> collect = bulk.items().stream().map(BulkResponseItem::id).collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);

        return errors;
    }
}
