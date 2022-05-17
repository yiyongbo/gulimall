package com.yee.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的查询条件
 * @author Yee
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类
     */
    private Long catelog3Id;

    /**
     * 排序条件
     * saleCount_asc/desc
     * skuPrice_asc/desc
     * hotScore_asc/desc
     */
    private String sort;

    /**
     * 是否有货，0：无，1：有
     */
    private Integer hasStock;

    /**
     * 价格区间，1_500/_500/500_
     */
    private Integer skuPrice;

    /**
     * 品牌ID集合
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     * attrs=1_安卓:其他&attrs=2_5寸:6寸
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum;
}
