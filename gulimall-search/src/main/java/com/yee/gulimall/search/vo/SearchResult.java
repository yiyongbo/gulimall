package com.yee.gulimall.search.vo;

import com.yee.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yee
 */
@Data
public class SearchResult {

    /**
     * 查询到的所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前查询到的所有分类
     */
    private List<CatalogVO> catalogs;

    /**
     * 当前查询到的所有品牌
     */
    private List<BrandVO> brands;

    /**
     * 当前查询到的所有属性
     */
    private List<AttrVO> attrs;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    /**
     * 导航页码
     */
    private List<Integer> pageNavs;

    /**
     * 面包屑导航数据
     */
    private List<NavVO> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    // ==========以上是返回给页面的所有信息==========

    @Data
    public static class NavVO {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class CatalogVO {
        /**
         * 分类ID
         */
        private Long catalogId;

        /**
         * 分类名字
         */
        private String catalogName;
    }

    @Data
    public static class BrandVO {
        /**
         * 品牌ID
         */
        private Long brandId;

        /**
         * 品牌名字
         */
        private String brandName;

        /**
         * 品牌图片
         */
        private String brandImg;
    }

    @Data
    public static class AttrVO {
        /**
         * 属性ID
         */
        private Long attrId;

        /**
         * 属性名称
         */
        private String attrName;

        /**
         * 属性值
         */
        private List<String> attrValue;
    }
}
