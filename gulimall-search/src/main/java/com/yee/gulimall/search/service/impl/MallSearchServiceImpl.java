package com.yee.gulimall.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.yee.gulimall.search.service.MallSearchService;
import com.yee.gulimall.search.vo.SearchParam;
import com.yee.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Yee
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private ElasticsearchClient client;

    @Override
    public SearchResult search(SearchParam param) {
        // 1、动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 2、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 3、执行检索请求
            SearchResponse<SearchResult> response = client.search(searchRequest, SearchResult.class);

            // 4、分析响应数据封装成我们需要的格式
            result = buildSearchResult(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 准备检索请求
     * 模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存），排序，分页，高亮，聚合分析
     *
     * @param param 查询参数
     * @return 检索请求
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        return null;
    }

    /**
     * 构建结果数据
     *
     * @param response 响应数据
     * @return 结果数据
     */
    private SearchResult buildSearchResult(SearchResponse<SearchResult> response) {
        return null;
    }

}
