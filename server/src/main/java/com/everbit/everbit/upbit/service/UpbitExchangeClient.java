package com.everbit.everbit.upbit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.global.util.EncryptionUtil;
import com.everbit.everbit.upbit.exception.UpbitException;
import com.everbit.everbit.upbit.dto.exchange.*;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitExchangeClient {
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

    private <T> T parseResponse(ResponseEntity<String> response, Class<T> responseType, String operation) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return createObjectMapper().readValue(response.getBody(), responseType);
            } catch (Exception e) {
                log.error("Failed to parse {} response: {}", operation, response.getBody(), e);
                throw new UpbitException("Failed to parse " + operation + " response: " + e.getMessage());
            }
        }
        throw new UpbitException("Failed to execute " + operation + ": " + response.getStatusCode());
    }

    private <T> T parseResponse(ResponseEntity<String> response, ParameterizedTypeReference<T> responseType, String operation) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return createObjectMapper().readValue(response.getBody(), 
                    createObjectMapper().getTypeFactory().constructType(responseType.getType()));
            } catch (Exception e) {
                log.error("응답 데이터 파싱 실패: {}", response.getBody(), e);
                throw new UpbitException("Failed to parse " + operation + " response: " + e.getMessage());
            }
        }
        throw new UpbitException("Failed to execute " + operation + ": " + response.getStatusCode());
    }

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // 전체 계좌 조회
    public List<AccountResponse> getAccounts(String username) {
        return executeGet(
            username,
            V1_ACCOUNTS,
            Collections.emptyMap(),
            new ParameterizedTypeReference<List<AccountResponse>>() {},
            "accounts"
        );
    }

    // 주문 가능 정보 조회
    public OrderChanceResponse getOrderChance(String username, String market) {
        Map<String, String> params = Collections.singletonMap("market", market);
        return executeGet(
            username,
            V1_ORDERS_CHANCE,
            params,
            OrderChanceResponse.class,
            "order chance"
        );
    }

    // 주문 가능 금액 조회
    public BigDecimal getAvailableBalance(String username, String market) {
        OrderChanceResponse orderChance = getOrderChance(username, market);
        return new BigDecimal(orderChance.askAccount().balance());
    }

    // 개별 주문 조회
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
            "order details"
        );
    }

    // 미체결 주문 조회
    public List<OrderResponse> getOpenOrders(String username, String market, List<String> states) {
        return executeOrderList(username, V1_ORDERS_OPEN, market, states, "open orders");
    }

    // 체결 주문 조회
    public List<OrderResponse> getClosedOrders(String username, String market, List<String> states) {
        return executeOrderList(username, V1_ORDERS_CLOSED, market, states, "closed orders");
    }

    // 주문 생성
    public OrderResponse createOrder(String username, OrderRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("market", request.market());
        params.put("side", request.side());
        if (request.volume() != null) params.put("volume", request.volume());
        if (request.price() != null) params.put("price", request.price());
        params.put("ord_type", request.ordType());
        if (request.identifier() != null) params.put("identifier", request.identifier());
        if (request.timeInForce() != null) params.put("time_in_force", request.timeInForce());
        if (request.smpType() != null) params.put("smp_type", request.smpType());

        return executePost(
            username,
            V1_ORDERS,
            params,
            OrderResponse.class,
            "order creation"
        );
    }

    // 주문 취소 후 재주문
    public ReplaceOrderResponse replaceOrder(String username, ReplaceOrderRequest request) {
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

        return executePost(
            username,
            V1_ORDERS_CANCEL_AND_NEW,
            params,
            ReplaceOrderResponse.class,
            "order replacement"
        );
    }

    // 주문 취소
    public OrderResponse cancelOrder(String username, String uuid, String identifier) {
        if (uuid == null && identifier == null) {
            throw new UpbitException("Either uuid or identifier must be provided");
        }

        Map<String, String> params = new HashMap<>();
        if (uuid != null) params.put("uuid", uuid);
        if (identifier != null) params.put("identifier", identifier);

        return executeDelete(
            username,
            V1_ORDER,
            params,
            OrderResponse.class,
            "order cancellation"
        );
    }

    // Private helper methods
    private <T> T executeGet(String username, String path, Map<String, String> params, 
                           Class<T> responseType, String operation) {
        return executeRequest(username, path, params, HttpMethod.GET, responseType, operation);
    }

    private <T> T executeGet(String username, String path, Map<String, String> params, 
                           ParameterizedTypeReference<T> responseType, String operation) {
        return executeRequest(username, path, params, HttpMethod.GET, responseType, operation);
    }

    private <T> T executePost(String username, String path, Map<String, String> params, 
                            Class<T> responseType, String operation) {
        return executeRequest(username, path, params, HttpMethod.POST, responseType, operation);
    }

    private <T> T executeDelete(String username, String path, Map<String, String> params, 
                              Class<T> responseType, String operation) {
        return executeRequest(username, path, params, HttpMethod.DELETE, responseType, operation);
    }

    private <T> T executeRequest(String username, String path, Map<String, String> params, 
                               HttpMethod method, Class<T> responseType, String operation) {
        try {
            String queryString = buildQueryString(params);
            URI uri = method == HttpMethod.GET ? buildUrl(path, queryString) : buildUrl(path, "");
            HttpHeaders headers = createHeaders(queryString, getUserByUsername(username));
            HttpEntity<?> entity = method == HttpMethod.POST ? 
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            log.info("{} API 요청 실행 - {}: {}", method, operation, 
                method == HttpMethod.GET ? uri : "요청 데이터=" + queryString);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                method,
                entity,
                String.class
            );

            return parseResponse(response, responseType, operation);
        } catch (HttpStatusCodeException e) {
            log.error("{} API 오류 발생: {} - {}", operation, e.getStatusCode(), e.getResponseBodyAsString());
            throw new UpbitException(operation + " API error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("{} 실행 실패", operation, e);
            throw new UpbitException("Failed to execute " + operation + ": " + e.getMessage());
        }
    }

    private <T> T executeRequest(String username, String path, Map<String, String> params, 
                               HttpMethod method, ParameterizedTypeReference<T> responseType, String operation) {
        try {
            String queryString = buildQueryString(params);
            URI uri = method == HttpMethod.GET ? buildUrl(path, queryString) : buildUrl(path, "");
            HttpHeaders headers = createHeaders(queryString, getUserByUsername(username));
            HttpEntity<?> entity = method == HttpMethod.POST ? 
                new HttpEntity<>(params, headers) : new HttpEntity<>(headers);

            log.info("Executing {} request - {}: {}", method, operation, 
                method == HttpMethod.GET ? uri : "params=" + queryString);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                method,
                entity,
                String.class
            );

            return parseResponse(response, responseType, operation);
        } catch (HttpStatusCodeException e) {
            log.error("{} API error: {} - {}", operation, e.getStatusCode(), e.getResponseBodyAsString());
            throw new UpbitException(operation + " API error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Failed to execute {}", operation, e);
            throw new UpbitException("Failed to execute " + operation + ": " + e.getMessage());
        }
    }

    private List<OrderResponse> executeOrderList(String username, String path, String market, List<String> states, String operation) {
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
            new ParameterizedTypeReference<List<OrderResponse>>() {},
            operation
        );
    }

    private String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) return "";
        
        return params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
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
}