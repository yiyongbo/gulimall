package com.yee.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author YYB
 */
@Data
public class MergeVO {

    private Long purchaseId;
    private List<Long> items;
}
