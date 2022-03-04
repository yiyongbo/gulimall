package com.yee.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YYB
 */
public class ProductConstant {

    @Getter
    @AllArgsConstructor
    public enum AttrEnum {
        /**
         * 1, "基本属性"
         */
        ATTR_TYPE_BASE(1, "基本属性"),
        /**
         * 0, "销售属性"
         */
        ATTR_TYPE_SALE(0, "销售属性");

        private int code;
        private String msg;
    }

    @Getter
    @AllArgsConstructor
    public enum StatusEnum {
        /**
         * 0, "新建"
         */
        NEW_SPU(0, "新建"),
        /**
         * 1, "商品上架"
         */
        UP_SPU(1, "商品上架"),
        /**
         * 2, "商品下架"
         */
        DOWN_SPU(2, "商品下架");

        private int code;
        private String msg;
    }
}
