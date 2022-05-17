package com.yee.gulimall.search.service;

import com.yee.gulimall.search.vo.SearchParam;
import com.yee.gulimall.search.vo.SearchResult;

/**
 * @author Yee
 */
public interface MallSearchService {

    /**
     * 检索
     * @param param 检索的所有参数
     * @return 返回检索的结果
     */
    SearchResult search(SearchParam param);
}
