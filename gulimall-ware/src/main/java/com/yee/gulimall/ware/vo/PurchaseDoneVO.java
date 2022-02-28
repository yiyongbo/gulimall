package com.yee.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author YYB
 */
@Data
public class PurchaseDoneVO {

    @NotNull
    private Long id;

    private List<PurchaseItemDoneVO> items;
}
