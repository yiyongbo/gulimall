package com.yee.gulimall.product.vo;

import lombok.Data;

import java.util.List;

/**
 * @author YYB
 */
@Data
public class SpuSaveVO {

    private String spuName;
    private String spuDescription;
    private Integer catalogId;
    private Integer brandId;
    private Double weight;
    private Integer publishStatus;
    private List<String> decript;
    private List<String> images;
    private Bounds bounds;
    private List<BaseAttrs> baseAttrs;
    private List<Skus> skus;
}
