package com.everbit.everbit.upbit.dto.exchange;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AccountResponse(
    String currency,
    String balance,
    String locked,
    String avgBuyPrice,
    Boolean avgBuyPriceModified,
    String unitCurrency
) {} 