package com.everbit.everbit.upbit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.global.util.EncryptionUtil;
import com.everbit.everbit.upbit.exception.UpbitException;
import com.everbit.everbit.upbit.dto.*;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitClient {
    private static final String V1_ACCOUNTS = "/v1/accounts";
    private static final String V1_ORDERS_CHANCE = "/v1/orders/chance";
    private static final String V1_ORDER = "/v1/order";
    private static final String V1_ORDERS = "/v1/orders";
    private static final String V1_ORDERS_OPEN = "/v1/orders/open";
    private static final String V1_ORDERS_CLOSED = "/v1/orders/closed";
    private static final String V1_ORDERS_CANCEL_AND_NEW = "/v1/orders/cancel_and_new";

    private final UpbitConfig upbitConfig;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final RestTemplate restTemplate;

    // Account API

    /**
     * 사용자의 계좌 정보를 조회합니다.
     *
     * @param username 사용자 이름
     * @return 계좌 정보 목록
     * @throws UpbitException API 호출 실패 시
     */
    public List<AccountResponse> getAccounts(String username) {
        return executeGet(
            username,
            V1_ACCOUNTS,
            Collections.emptyMap(),
            new ParameterizedTypeReference<List<AccountResponse>>() {},
            "Failed to get accounts"
        );
    }

    // Order API

    /**
     * 마켓별 주문 가능 정보를 조회합니다.
     *
     * @param username 사용자 이름
     * @param market 마켓 ID (예: KRW-BTC)
     * @return 주문 가능 정보
     * @throws UpbitException API 호출 실패 시
     */
    public OrderChanceResponse getOrderChance(String username, String market) {
        Map<String, String> params = Collections.singletonMap("market", market);
        return executeGet(
            username,
            V1_ORDERS_CHANCE,
            params,
            OrderChanceResponse.class,
            "Failed to get order chance"
        );
    }

    /**
     * 개별 주문을 조회합니다.
     * uuid 또는 identifier 중 하나는 반드시 제공되어야 합니다.
     *
     * @param username 사용자 이름
     * @param uuid 주문 UUID
     * @param identifier 조회용 사용자 지정 값
     * @return 주문 정보
     * @throws UpbitException API 호출 실패 시 또는 필수 파라미터 누락 시
     */
    public OrderResponse getOrder(String username, String uuid, String identifier) {
        if (uuid == null && identifier == null) {
            throw new UpbitException("Either uuid or identifier must be provided");
        }

        Map<String, String> params = new HashMap<>();
        if (uuid != null) params.put("uuid", uuid);
        if (identifier != null) params.put("identifier", identifier);

        return executeGet(
            username,
            V1_ORDER,
            params,
            OrderResponse.class,
            "Failed to get order details"
        );
    }

    /**
     * 미체결 주문 목록을 조회합니다.
     *
     * @param username 사용자 이름
     * @param market 마켓 ID (선택적)
     * @param states 주문 상태 목록 (선택적, 예: wait, watch)
     * @return 미체결 주문 목록
     * @throws UpbitException API 호출 실패 시
     */
    public List<OrderItemResponse> getOpenOrders(String username, String market, List<String> states) {
        return executeOrderList(username, V1_ORDERS_OPEN, market, states, "Failed to get open orders");
    }

    /**
     * 체결된 주문 목록을 조회합니다.
     *
     * @param username 사용자 이름
     * @param market 마켓 ID (선택적)
     * @param states 주문 상태 목록 (선택적, 예: done, cancel)
     * @return 체결된 주문 목록
     * @throws UpbitException API 호출 실패 시
     */
    public List<OrderItemResponse> getClosedOrders(String username, String market, List<String> states) {
        return executeOrderList(username, V1_ORDERS_CLOSED, market, states, "Failed to get closed orders");
    }

    /**
     * 새로운 주문을 생성합니다.
     *
     * @param username 사용자 이름
     * @param request 주문 요청 정보
     * @return 생성된 주문 정보
     * @throws UpbitException API 호출 실패 시
     */
    public OrderItemResponse createOrder(String username, OrderRequest request) {
        try {
            User user = getUserByUsername(username);
            
            // Convert request to query string for JWT token
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
            URI uri = buildUrl(V1_ORDERS, "");
            HttpHeaders headers = createHeaders(queryString, user);

            log.info("Creating order - Request: {}, Query String: {}", request, queryString);
            
            HttpEntity<OrderRequest> entity = new HttpEntity<>(request, headers);
            try {
                ResponseEntity<OrderItemResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    OrderItemResponse.class
                );

                OrderItemResponse result = handleResponse(response, "Failed to create order");
                log.info("Order created successfully - UUID: {}", result.uuid());
                return result;
            } catch (Exception e) {
                log.error("Failed to create order - Error: {}, Request: {}, Response: {}", 
                    e.getMessage(), request, e instanceof HttpStatusCodeException ? 
                    ((HttpStatusCodeException) e).getResponseBodyAsString() : "No response body");
                throw e;
            }
        } catch (Exception e) {
            log.error("Failed to create order", e);
            throw new UpbitException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * 기존 주문을 취소하고 새로운 주문을 생성합니다.
     *
     * @param username 사용자 이름
     * @param request 취소 및 신규 주문 요청 정보
     * @return 취소된 주문 정보와 새로운 주문 UUID
     * @throws UpbitException API 호출 실패 시
     */
    public ReplaceOrderResponse replaceOrder(String username, ReplaceOrderRequest request) {
        try {
            User user = getUserByUsername(username);
            
            // Convert request to query string for JWT token
            Map<String, String> params = new HashMap<>();
            if (request.prevOrderUuid() != null) {
                params.put("prev_order_uuid", request.prevOrderUuid());
            }
            if (request.prevOrderIdentifier() != null) {
                params.put("prev_order_identifier", request.prevOrderIdentifier());
            }
            params.put("new_ord_type", request.newOrdType());
            if (request.newVolume() != null) {
                params.put("new_volume", request.newVolume());
            }
            if (request.newPrice() != null) {
                params.put("new_price", request.newPrice());
            }
            if (request.newSmpType() != null) {
                params.put("new_smp_type", request.newSmpType());
            }
            if (request.newIdentifier() != null) {
                params.put("new_identifier", request.newIdentifier());
            }
            if (request.newTimeInForce() != null) {
                params.put("new_time_in_force", request.newTimeInForce());
            }
            
            String queryString = buildQueryString(params);
            URI uri = buildUrl(V1_ORDERS_CANCEL_AND_NEW, "");
            HttpHeaders headers = createHeaders(queryString, user);

            HttpEntity<ReplaceOrderRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<ReplaceOrderResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                entity,
                ReplaceOrderResponse.class
            );

            return handleResponse(response, "Failed to replace order");
        } catch (Exception e) {
            throw new UpbitException("Failed to replace order", e);
        }
    }

    // Private helper methods
    private <T> T executeGet(String username, String path, Map<String, String> params, Class<T> responseType, String errorMessage) {
        try {
            String queryString = buildQueryString(params);
            URI uri = buildUrl(path, queryString);
            HttpHeaders headers = createHeaders(queryString, getUserByUsername(username));

            ResponseEntity<T> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                responseType
            );

            return handleResponse(response, errorMessage);
        } catch (Exception e) {
            throw new UpbitException(errorMessage, e);
        }
    }

    private <T> T executeGet(String username, String path, Map<String, String> params, 
                           ParameterizedTypeReference<T> responseType, String errorMessage) {
        try {
            String queryString = buildQueryString(params);
            URI uri = buildUrl(path, queryString);
            HttpHeaders headers = createHeaders(queryString, getUserByUsername(username));

            ResponseEntity<T> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                responseType
            );

            return handleResponse(response, errorMessage);
        } catch (Exception e) {
            throw new UpbitException(errorMessage, e);
        }
    }

    private List<OrderItemResponse> executeOrderList(String username, String path, String market, List<String> states, String errorMessage) {
        Map<String, String> params = new HashMap<>();
        if (market != null && !market.isEmpty()) {
            params.put("market", market);
        }
        if (states != null && !states.isEmpty()) {
            states.forEach(state -> params.put("states[]", state));
        }

        return executeGet(
            username,
            path,
            params,
            new ParameterizedTypeReference<List<OrderItemResponse>>() {},
            errorMessage
        );
    }

    private String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) return "";
        
        return params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining("&"));
    }

    private URI buildUrl(String path, String queryString) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(upbitConfig.getBaseUrl())
            .path(path);
            
        if (queryString != null && !queryString.isEmpty()) {
            builder.query(queryString);
        }
        
        return builder.build().toUri();
    }

    private User getUserByUsername(String username) {
        return userService.findUserByUsername(username);
    }

    private HttpHeaders createHeaders(String queryString, User user) {
        try {
            String accessKey = encryptionUtil.decrypt(user.getUpbitAccessKey());
            String secretKey = encryptionUtil.decrypt(user.getUpbitSecretKey());
            String token = createAuthToken(accessKey, secretKey, queryString);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            return headers;
        } catch (Exception e) {
            throw new UpbitException("Failed to create auth headers", e);
        }
    }

    private String createAuthToken(String accessKey, String secretKey, String queryString) {
        try {
            String nonce = UUID.randomUUID().toString();
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            if (queryString != null && !queryString.isEmpty()) {
                String queryHash = makeQueryHash(queryString);
                return JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", nonce)
                    .withClaim("query_hash", queryHash)
                    .withClaim("query_hash_alg", "SHA512")
                    .sign(algorithm);
            } else {
                return JWT.create()
                    .withClaim("access_key", accessKey)
                    .withClaim("nonce", nonce)
                    .sign(algorithm);
            }
        } catch (Exception e) {
            throw new UpbitException("Failed to create auth token", e);
        }
    }

    private String makeQueryHash(String queryString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("utf8"));
        return String.format("%0128x", new BigInteger(1, md.digest()));
    }

    private <T> T handleResponse(ResponseEntity<T> response, String errorMessage) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new UpbitException(errorMessage + ": " + response.getStatusCode());
    }
} 