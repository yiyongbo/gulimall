package com.yee.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author YYB
 */
@Data
public class SpuBoundTO {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}
