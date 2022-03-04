package com.yee.gulimall.search.service;

import com.yee.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author YYB
 */
public interface ProductSaveService {

    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
