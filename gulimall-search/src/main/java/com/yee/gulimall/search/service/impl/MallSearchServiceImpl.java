package com.yee.gulimall.search.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yee.common.to.es.SkuEsModel;
import com.yee.common.utils.R;
import com.yee.gulimall.search.config.ElasticConfig;
import com.yee.gulimall.search.constant.EsConstant;
import com.yee.gulimall.search.feign.ProductFeignService;
import com.yee.gulimall.search.service.MallSearchService;
import com.yee.gulimall.search.vo.AttrResponseVO;
import com.yee.gulimall.search.vo.BrandVO;
import com.yee.gulimall.search.vo.SearchParam;
import com.yee.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Yee
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        // 1、动态构建出查询需要的DSL语句
        SearchResult result = null;

        // 2、准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);

        try {
            // 3、执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticConfig.COMMON_OPTIONS);

            // 4、分析响应数据封装成我们需要的格式
            result = buildSearchResult(response, param);
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
        // 构建DSL语句的
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
        // 1.构建 bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 must - 模糊匹配
        if (StringUtils.hasText(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // 1.2 filter - 过滤（按照三级分类id查询）
        if (Objects.nonNull(param.getCatelog3Id())) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatelog3Id()));
        }
        // 1.2 filter - 过滤（按照品牌id查询）
        if (Objects.nonNull(param.getBrandId()) && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // 1.2 filter - 过滤（按照所有指定的属性查询）
        if (Objects.nonNull(param.getAttrs()) && param.getAttrs().size() > 0) {
            param.getAttrs().forEach(item -> {
                // attrs=1_5:8寸&attrs=2_8G&16G
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = item.split("_");
                // 检索的属性Id
                String attrId = s[0];
                // 这个属性的检索用的值
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                // 每一个必须都得生产一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            });

        }
        // 1.2 filter - 过滤（按照是否有库存查询）
        if (Objects.nonNull(param.getHasStock())) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // 1.2 filter - 过滤（按照价格区间查询）
        if (StringUtils.hasText(param.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] str = param.getSkuPrice().split("_");
            // 区间 1_500/_500/500_
            if (param.getSkuPrice().startsWith("_")) {
                rangeQuery.lte(str[0]);
            } else if (param.getSkuPrice().endsWith("_")) {
                rangeQuery.gte(str[0]);
            } else {
                rangeQuery.gte(str[0]).lte(str[1]);
            }
            boolQuery.filter(rangeQuery);
        }
        // 把以前的都有条件都拿来进行封装
        searchSourceBuilder.query(boolQuery);

        // 排序，分页，高亮
        // 2.1 排序
        if (StringUtils.hasText(param.getSort())) {
            String sort = param.getSort();
            // sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(s[0], order);
        }
        // 2.2 分页
        searchSourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);
        // 2.3 高亮
        if (StringUtils.hasText(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 聚合分析
        // 3.1 品牌聚合
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 品牌聚合的子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        searchSourceBuilder.aggregation(brandAgg);
        // 3.2 分类聚合
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        // 分类聚合的子聚合
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        searchSourceBuilder.aggregation(catalogAgg);
        // 3.3 属性聚合
        NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        // 聚合出当前所有的attrId
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        // 聚合分析出当前attr_id对应的名字
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // 聚合分析出当前attr_id对应的所有可能的属性值attrValue
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(attrAgg);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }

    /**
     * 构建结果数据
     *
     * @param response 响应数据
     * @return 结果数据
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) throws JsonProcessingException {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = response.getHits();
        // 1、返回的所有查询到的商品
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        if (Objects.nonNull(hits.getHits()) && hits.getHits().length > 0) {
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = objectMapper.readValue(sourceAsString, SkuEsModel.class);
                HighlightField highlightField = hit.getHighlightFields().get("skuTitle");
                if (Objects.nonNull(highlightField)) {
                    String skuTitle = highlightField.getFragments()[0].string();
                    skuEsModel.setSkuTitle(skuTitle);
                }
                skuEsModelList.add(skuEsModel);
            }
        }
        searchResult.setProducts(skuEsModelList);

        // 2、当前所有商品涉及到的所有属性信息
        List<SearchResult.AttrVO> attrVOList = new ArrayList<>();
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        attrIdAgg.getBuckets().forEach(item -> {
            SearchResult.AttrVO attrVO = new SearchResult.AttrVO();
            // 得到属性的id
            long attrId = item.getKeyAsNumber().longValue();
            attrVO.setAttrId(attrId);
            // 得到属性的名字
            String attrName = ((ParsedStringTerms) item.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            attrVO.setAttrName(attrName);
            // 得到属性的所有值
            List<String> attrValues = ((ParsedStringTerms) item.getAggregations().get("attr_value_agg")).getBuckets()
                    .stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVO.setAttrValue(attrValues);
            attrVOList.add(attrVO);
        });
        searchResult.setAttrs(attrVOList);

        // 3、当前所有商品涉及到的所有品牌信息
        List<SearchResult.BrandVO> brandVOList = new ArrayList<>();
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        brandAgg.getBuckets().forEach(item -> {
            SearchResult.BrandVO brandVO = new SearchResult.BrandVO();
            // 得到品牌的id
            brandVO.setBrandId(item.getKeyAsNumber().longValue());
            // 得到品牌的名称
            String brandName = ((ParsedStringTerms) item.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVO.setBrandName(brandName);
            // 得到品牌的图片
            String brandImg = ((ParsedStringTerms) item.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVO.setBrandImg(brandImg);
            brandVOList.add(brandVO);
        });
        searchResult.setBrands(brandVOList);

        // 4、当前所有商品涉及到的所有分类信息
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVO> catalogVOList = new ArrayList<>();
        catalogAgg.getBuckets().forEach(item -> {
            SearchResult.CatalogVO catalogVO = new SearchResult.CatalogVO();
            // 得到分类id
            catalogVO.setCatalogId(item.getKeyAsNumber().longValue());
            // 得到分类名
            ParsedStringTerms catalogNameAgg = item.getAggregations().get("catalog_name_agg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVO.setCatalogName(catalogName);
            catalogVOList.add(catalogVO);
        });
        searchResult.setCatalogs(catalogVOList);

        // 5、分页信息-页码
        searchResult.setPageNum(param.getPageNum());
        // 6、分页信息-总记录数
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);
        // 7、分页信息-总页码
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGE_SIZE == 0 ? total / EsConstant.PRODUCT_PAGE_SIZE : (total / EsConstant.PRODUCT_PAGE_SIZE) + 1);
        searchResult.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // 8、构建面包屑导航功能
        if (Objects.nonNull(param.getAttrs()) && param.getAttrs().size() > 0) {
            List<SearchResult.NavVO> navVOList = param.getAttrs().stream().map(attr -> {
                // 8.1、分析每个attrs传过来的查询参数值
                SearchResult.NavVO navVO = new SearchResult.NavVO();
                String[] s = attr.split("_");
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                searchResult.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    try {
                        AttrResponseVO data = r.getData("attr", new TypeReference<AttrResponseVO>() {});
                        navVO.setNavName(data.getAttrName());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                }
                navVO.setNavValue(s[1]);

                // 8.2、取消了这个面包屑以后，我们要跳转到那个地方，将请求地址的url里面的当前置空
                String replace = replaceQueryString(param, attr, "attrs");
                navVO.setLink("http://search.gulimall.com/list.html?" + replace);
                return navVO;
            }).collect(Collectors.toList());

            searchResult.setNavs(navVOList);
        }

        // 品牌面包屑导航
        if (Objects.nonNull(param.getBrandId()) && param.getBrandId().size() > 0) {
            List<SearchResult.NavVO> navVOList = searchResult.getNavs();
            SearchResult.NavVO navVO = new SearchResult.NavVO();
            navVO.setNavName("品牌");
            // 远程查询所有品牌
            R r = productFeignService.brandsInfos(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVO> brandVOS = r.getData("brand", new TypeReference<List<BrandVO>>() {});
                StringBuffer buffer = new StringBuffer();
                String replace = "";
                for (BrandVO brandVO : brandVOS) {
                    buffer.append(brandVO.getBrandName()).append(";");
                    replace = replaceQueryString(param, brandVO.getBrandId().toString(), "brandId");

                }
                navVO.setNavValue(buffer.toString());
                navVO.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navVOList.add(navVO);
        }

        // TODO 分类面包屑导航，不需要导航取消



        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String value, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(value, "UTF-8");
            // 浏览器对空格编码和java不一样
            encode = encode.replace("+", "%20");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String replace = param.getQueryString().replace("&" + key + "=" + encode, "");
        return replace;
    }

}
