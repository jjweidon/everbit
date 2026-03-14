package com.everbit.everbit.integrations.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Upbit POST /v1/orders 요청 DTO.
 * SoT: docs/integrations/upbit.md §6.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderRequest(
	String market,
	String side,
	String volume,
	String price,
	String ordType,
	String identifier,
	String timeInForce,
	String smpType
) {
	public static final String SIDE_BID = "bid";
	public static final String SIDE_ASK = "ask";
	public static final String ORD_TYPE_LIMIT = "limit";
	public static final String ORD_TYPE_PRICE = "price";
	public static final String ORD_TYPE_MARKET = "market";
	public static final String ORD_TYPE_BEST = "best";

	public static OrderRequest createLimit(String market, String side, String volume, String price, String identifier) {
		return new OrderRequest(market, side, volume, price, ORD_TYPE_LIMIT, identifier, null, null);
	}
}
