package com.everbit.everbit.integrations.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Upbit GET /v1/accounts 응답 DTO.
 * SoT: docs/integrations/upbit.md
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AccountResponse(
	String currency,
	String balance,
	String locked,
	String avgBuyPrice,
	Boolean avgBuyPriceModified,
	String unitCurrency
) {}
