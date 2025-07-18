package com.everbit.everbit.upbit.dto;

import java.math.BigDecimal;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AccountResponse(
    String currency,
    BigDecimal balance,
    BigDecimal locked,
    BigDecimal avgBuyPrice,
    boolean avgBuyPriceModified,
    String unitCurrency
) {} 