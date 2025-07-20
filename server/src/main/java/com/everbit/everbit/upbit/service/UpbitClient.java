package com.everbit.everbit.upbit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.global.util.EncryptionUtil;
import com.everbit.everbit.upbit.exception.UpbitException;
import com.everbit.everbit.upbit.dto.AccountResponse;
import com.everbit.everbit.upbit.dto.OrderChanceResponse;
import com.everbit.everbit.upbit.dto.OrderResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitClient {
    private final UpbitConfig upbitConfig;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final RestTemplate restTemplate;

    public List<AccountResponse> getAccounts(String username) {
        try {
            User user = userService.findUserByUsername(username);
            URI uri = buildUrl("/v1/accounts");
            HttpHeaders headers = createHeaders("", user);
            
            log.debug("Making request to Upbit API: {}", uri);
            ResponseEntity<List<AccountResponse>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<List<AccountResponse>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to get accounts. Status: {}", response.getStatusCode());
                throw new UpbitException("Failed to get accounts: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to get accounts", e);
            throw new UpbitException("Failed to get accounts", e);
        }
    }

    public OrderChanceResponse getOrderChance(String username, String market) {
        try {
            User user = userService.findUserByUsername(username);
            String queryString = String.format("market=%s", market);
            URI uri = buildUrl("/v1/orders/chance", queryString);
            HttpHeaders headers = createHeaders(queryString, user);
            
            log.info("Making request to Upbit API - Full URL: {}", uri.toString());
            log.info("Request headers: {}", headers);
            
            ResponseEntity<OrderChanceResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                OrderChanceResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to get order chance. Status: {}", response.getStatusCode());
                throw new UpbitException("Failed to get order chance: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to get order chance. Full URL: {}", buildUrl("/v1/orders/chance").toString(), e);
            throw new UpbitException("Failed to get order chance", e);
        }
    }

    public OrderResponse getOrder(String username, String uuid, String identifier) {
        if (uuid == null && identifier == null) {
            throw new UpbitException("Either uuid or identifier must be provided");
        }

        try {
            User user = userService.findUserByUsername(username);
            StringBuilder queryStringBuilder = new StringBuilder();
            
            if (uuid != null) {
                queryStringBuilder.append("uuid=").append(uuid);
            }
            if (identifier != null) {
                if (queryStringBuilder.length() > 0) {
                    queryStringBuilder.append("&");
                }
                queryStringBuilder.append("identifier=").append(identifier);
            }
            
            String queryString = queryStringBuilder.toString();
            URI uri = buildUrl("/v1/order?" + queryString);
            HttpHeaders headers = createHeaders(queryString, user);
            
            log.debug("Making request to Upbit API for order details: {}", uri);
            ResponseEntity<OrderResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                OrderResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Failed to get order details. Status: {}", response.getStatusCode());
                throw new UpbitException("Failed to get order details: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to get order details", e);
            throw new UpbitException("Failed to get order details", e);
        }
    }

    private URI buildUrl(String path) {
        return buildUrl(path, null);
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