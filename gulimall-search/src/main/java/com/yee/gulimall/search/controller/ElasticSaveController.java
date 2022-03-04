package com.yee.gulimall.search.controller;

import com.yee.common.exception.BizCodeEnum;
import com.yee.common.to.es.SkuEsModel;
import com.yee.common.utils.R;
import com.yee.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author YYB
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveService productSaveService;

    @PostMapping("/product")
    private R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean flag = false;
        try {
            flag = productSaveService.productStatusUp(skuEsModels);
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
        }

        return flag ? R.ok() : R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION);
    }
}
