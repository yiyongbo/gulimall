package com.yee.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author YYB
 */
public class WareConstant {

    @Getter
    @AllArgsConstructor
    public enum PurchaseStatusEnum {
        /**
         * 0, "新建"
         */
        CREATED(0, "新建"),
        /**
         * 1, "已分配"
         */
        ASSIGNED(1, "已分配"),
        /**
         * 2, "已领取"
         */
        RECEIVE(2, "已领取"),
        /**
         * 3, "已完成"
         */
        FINISH(3, "已完成"),
        /**
         * 4, "有异常"
         */
        HASERROR(4, "有异常");

        private int code;
        private String msg;
    }

    @Getter
    @AllArgsConstructor
    public enum PurchaseDetailStatusEnum {
        /**
         * 0, "新建"
         */
        CREATED(0, "新建"),
        /**
         * 1, "已分配"
         */
        ASSIGNED(1, "已分配"),
        /**
         * 2, "正在采购"
         */
        BUYING(2, "正在采购"),
        /**
         * 3, "已完成"
         */
        FINISH(3, "已完成"),
        /**
         * 4, "采购失败"
         */
        HASERROR(4, "采购失败");

        private int code;
        private String msg;
    }
}
