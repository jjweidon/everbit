package com.everbit.everbit.integrations.upbit;

import com.everbit.everbit.integrations.upbit.dto.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Upbit Exchange REST API 클라이언트. SoT: docs/integrations/upbit.md.
 * 호출 측에서 키를 복호화해 전달하며, 브라우저에서는 Upbit 직접 호출 금지.
 * 429/418 시 UpbitApiException으로 상태 코드 전달.
 */
@Slf4j
@Component
public class UpbitExchangeClient {

	private static final String V1_ACCOUNTS = "/v1/accounts";
	private static final String V1_ORDERS_CHANCE = "/v1/orders/chance";
	private static final String V1_ORDER = "/v1/order";
	private static final String V1_ORDERS = "/v1/orders";
	private static final String V1_ORDERS_OPEN = "/v1/orders/open";
	private static final String V1_ORDERS_CLOSED = "/v1/orders/closed";
	private static final String V1_ORDERS_CANCEL_AND_NEW = "/v1/orders/cancel_and_new";

	private final UpbitProperties properties;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	public UpbitExchangeClient(UpbitProperties properties,
		@Qualifier("upbitRestTemplate") RestTemplate upbitRestTemplate) {
		this.properties = properties;
		this.restTemplate = upbitRestTemplate;
		this.objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule())
			.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public List<AccountResponse> getAccounts(String accessKey, String secretKey) {
		return executeGet(
			accessKey, secretKey,
			V1_ACCOUNTS,
			Collections.emptyMap(),
			new ParameterizedTypeReference<>() {},
			"Failed to get accounts"
		);
	}

	public OrderChanceResponse getOrderChance(String accessKey, String secretKey, String market) {
		Map<String, String> params = Collections.singletonMap("market", market);
		return executeGet(
			accessKey, secretKey,
			V1_ORDERS_CHANCE,
			params,
			OrderChanceResponse.class,
			"Failed to get order chance"
		);
	}

	public OrderResponse getOrder(String accessKey, String secretKey, String uuid, String identifier) {
		if (uuid == null && identifier == null) {
			throw new UpbitException("Either uuid or identifier must be provided");
		}
		Map<String, String> params = new HashMap<>();
		if (uuid != null) params.put("uuid", uuid);
		if (identifier != null) params.put("identifier", identifier);
		return executeGet(
			accessKey, secretKey,
			V1_ORDER,
			params,
			OrderResponse.class,
			"Failed to get order"
		);
	}

	public List<OrderResponse> getOpenOrders(String accessKey, String secretKey, String market, List<String> states) {
		return executeOrderList(accessKey, secretKey, V1_ORDERS_OPEN, market, states, "Failed to get open orders");
	}

	public List<OrderResponse> getClosedOrders(String accessKey, String secretKey, String market, List<String> states) {
		return executeOrderList(accessKey, secretKey, V1_ORDERS_CLOSED, market, states, "Failed to get closed orders");
	}

	public OrderResponse createOrder(String accessKey, String secretKey, OrderRequest request) {
		Map<String, String> params = new HashMap<>();
		params.put("market", request.market());
		params.put("side", request.side());
		if (request.volume() != null) params.put("volume", request.volume());
		if (request.price() != null) params.put("price", request.price());
		params.put("ord_type", request.ordType());
		if (request.identifier() != null) params.put("identifier", request.identifier());
		if (request.timeInForce() != null) params.put("time_in_force", request.timeInForce());
		if (request.smpType() != null) params.put("smp_type", request.smpType());

		String queryString = buildQueryString(params);
		URI uri = buildUri(V1_ORDERS, "");
		HttpHeaders headers = createHeaders(accessKey, secretKey, queryString);
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, Objects.requireNonNull(headers));

		try {
			ResponseEntity<String> response = restTemplate.exchange(Objects.requireNonNull(uri), Objects.requireNonNull(HttpMethod.POST), entity, String.class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return objectMapper.readValue(response.getBody(), OrderResponse.class);
			}
			throw new UpbitException("Create order failed: " + response.getStatusCode());
		} catch (HttpStatusCodeException e) {
			throw toUpbitApiException(e);
		} catch (Exception e) {
			if (e instanceof UpbitException u) throw u;
			log.warn("Create order error (no body logged)", e);
			throw new UpbitException("Failed to create order: " + e.getMessage(), e);
		}
	}

	public OrderResponse cancelOrder(String accessKey, String secretKey, String uuid, String identifier) {
		if (uuid == null && identifier == null) {
			throw new UpbitException("Either uuid or identifier must be provided");
		}
		Map<String, String> params = new HashMap<>();
		if (uuid != null) params.put("uuid", uuid);
		if (identifier != null) params.put("identifier", identifier);
		String queryString = buildQueryString(params);
		URI uri = buildUri(V1_ORDER, queryString);
		HttpHeaders headers = createHeaders(accessKey, secretKey, queryString);

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				Objects.requireNonNull(uri), Objects.requireNonNull(HttpMethod.DELETE), new HttpEntity<>(Objects.requireNonNull(headers)), String.class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return objectMapper.readValue(response.getBody(), OrderResponse.class);
			}
			throw new UpbitException("Cancel order failed: " + response.getStatusCode());
		} catch (HttpStatusCodeException e) {
			throw toUpbitApiException(e);
		} catch (Exception e) {
			if (e instanceof UpbitException u) throw u;
			log.warn("Cancel order error (no body logged)", e);
			throw new UpbitException("Failed to cancel order: " + e.getMessage(), e);
		}
	}

	public ReplaceOrderResponse replaceOrder(String accessKey, String secretKey, ReplaceOrderRequest request) {
		Map<String, String> params = new HashMap<>();
		if (request.prevOrderUuid() != null) params.put("prev_order_uuid", request.prevOrderUuid());
		if (request.prevOrderIdentifier() != null) params.put("prev_order_identifier", request.prevOrderIdentifier());
		params.put("new_ord_type", request.newOrdType());
		if (request.newVolume() != null) params.put("new_volume", request.newVolume());
		if (request.newPrice() != null) params.put("new_price", request.newPrice());
		if (request.newSmpType() != null) params.put("new_smp_type", request.newSmpType());
		if (request.newIdentifier() != null) params.put("new_identifier", request.newIdentifier());
		if (request.newTimeInForce() != null) params.put("new_time_in_force", request.newTimeInForce());

		String queryString = buildQueryString(params);
		URI uri = buildUri(V1_ORDERS_CANCEL_AND_NEW, "");
		HttpHeaders headers = createHeaders(accessKey, secretKey, queryString);
		HttpEntity<Map<String, String>> entity = new HttpEntity<>(params, Objects.requireNonNull(headers));

		try {
			ResponseEntity<String> response = restTemplate.exchange(Objects.requireNonNull(uri), Objects.requireNonNull(HttpMethod.POST), entity, String.class);
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return objectMapper.readValue(response.getBody(), ReplaceOrderResponse.class);
			}
			throw new UpbitException("Replace order failed: " + response.getStatusCode());
		} catch (HttpStatusCodeException e) {
			throw toUpbitApiException(e);
		} catch (Exception e) {
			if (e instanceof UpbitException u) throw u;
			log.warn("Replace order error (no body logged)", e);
			throw new UpbitException("Failed to replace order: " + e.getMessage(), e);
		}
	}

	// --- private ---

	private <T> T executeGet(String accessKey, String secretKey, String path, Map<String, String> params,
		Class<T> responseType, String errorMessage) {
		try {
			String queryString = buildQueryString(params);
			URI uri = buildUri(path, queryString);
			HttpHeaders headers = createHeaders(accessKey, secretKey, queryString);
			ResponseEntity<T> response = restTemplate.exchange(
				Objects.requireNonNull(uri), Objects.requireNonNull(HttpMethod.GET), new HttpEntity<>(Objects.requireNonNull(headers)), Objects.requireNonNull(responseType));
			return handleResponse(response, errorMessage);
		} catch (HttpStatusCodeException e) {
			throw toUpbitApiException(e);
		} catch (Exception e) {
			if (e instanceof UpbitException u) throw u;
			throw new UpbitException(errorMessage, e);
		}
	}

	private <T> T executeGet(String accessKey, String secretKey, String path, Map<String, String> params,
		ParameterizedTypeReference<T> responseType, String errorMessage) {
		try {
			String queryString = buildQueryString(params);
			URI uri = buildUri(path, queryString);
			HttpHeaders headers = createHeaders(accessKey, secretKey, queryString);
			ResponseEntity<T> response = restTemplate.exchange(
				Objects.requireNonNull(uri), Objects.requireNonNull(HttpMethod.GET), new HttpEntity<>(Objects.requireNonNull(headers)), Objects.requireNonNull(responseType));
			return handleResponse(response, errorMessage);
		} catch (HttpStatusCodeException e) {
			throw toUpbitApiException(e);
		} catch (Exception e) {
			if (e instanceof UpbitException u) throw u;
			throw new UpbitException(errorMessage, e);
		}
	}

	private List<OrderResponse> executeOrderList(String accessKey, String secretKey, String path,
		String market, List<String> states, String errorMessage) {
		Map<String, String> params = new HashMap<>();
		if (market != null && !market.isEmpty()) params.put("market", market);
		if (states != null && !states.isEmpty()) {
			// Upbit expects states[]=wait&states[]=done etc.
			for (String state : states) {
				params.put("states[]", state);
			}
		}
		return executeGet(
			accessKey, secretKey,
			path, params,
			new ParameterizedTypeReference<>() {},
			errorMessage
		);
	}

	private static String buildQueryString(Map<String, String> params) {
		if (params.isEmpty()) return "";
		return params.entrySet().stream()
			.map(e -> e.getKey() + "=" + e.getValue())
			.collect(Collectors.joining("&"));
	}

	private URI buildUri(String path, String queryString) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Objects.requireNonNull(properties.baseUrl())).path(Objects.requireNonNull(path));
		if (queryString != null && !queryString.isEmpty()) {
			builder.query(Objects.requireNonNull(queryString));
		}
		return builder.build().toUri();
	}

	private HttpHeaders createHeaders(String accessKey, String secretKey, String queryString) {
		String token = UpbitJwtProvider.createToken(accessKey, secretKey, queryString);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(Objects.requireNonNull(token));
		return headers;
	}

	private static <T> T handleResponse(ResponseEntity<T> response, String errorMessage) {
		if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
			return response.getBody();
		}
		throw new UpbitException(errorMessage + ": " + response.getStatusCode());
	}

	private static UpbitApiException toUpbitApiException(HttpStatusCodeException e) {
		String body = null;
		try {
			body = e.getResponseBodyAsString();
		} catch (Exception ignored) {}
		return new UpbitApiException(e.getStatusCode(), body);
	}
}
