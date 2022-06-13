package com.yee.gulimall.product.vo;

import com.yee.gulimall.product.entity.SkuImagesEntity;
import com.yee.gulimall.product.entity.SkuInfoEntity;
import com.yee.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Yee
 */
@Data
public class SkuItemVO {

    // 1、sku基本信息获取  pms_sku_info
    SkuInfoEntity skuInfoEntity;

    // 2、sku的图片信息 pms_sku_images
    List<SkuImagesEntity> images;

    // 3、获取spu的销售属性组合
    List<SkuItemSaleAttrVO> saleAttr;

    // 4、获取spu的介绍
    SpuInfoDescEntity spuInfoDescEntity;

    // 5、获取spu的规格参数信息
    List<SpuItemAttrGroupVO> groupAttrs;


    @Data
    public static class SkuItemSaleAttrVO {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVO {
        private String groupName;
        private List<SpuBaseAttrVO> attrs;
    }

    @Data
    public static class SpuBaseAttrVO {
        private String attrName;
        private String attrValue;
    }

}
