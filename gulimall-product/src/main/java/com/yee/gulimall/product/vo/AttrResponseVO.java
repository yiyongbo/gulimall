package com.yee.gulimall.product.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author YYB
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AttrResponseVO extends AttrVO {

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
