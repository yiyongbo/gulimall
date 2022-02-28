package com.yee.gulimall.ware.vo;

import lombok.Data;

/**
 * @author YYB
 */
@Data
public class PurchaseItemDoneVO {

    private Long itemId;
    private Integer status;
    private String reason;
}
