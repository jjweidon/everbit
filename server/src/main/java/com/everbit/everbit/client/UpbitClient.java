package com.everbit.everbit.client;

import com.everbit.everbit.config.UpbitConfig;
import com.everbit.everbit.dto.upbit.CandleDto;
import com.everbit.everbit.dto.upbit.TickerDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Upbit API 클라이언트
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UpbitClient {

    private final WebClient webClient;
    private final UpbitConfig upbitConfig;
    private final ObjectMapper objectMapper;

    /**
     * 인증 헤더 생성
     */
    private Map<String, String> createAuthHeaders(String queryString) {
        String accessKey = upbitConfig.getAccessKey();
        String secretKey = upbitConfig.getSecretKey();
        String authenticationToken = UUID.randomUUID().toString();

        Map<String, String> params = new HashMap<>();
        params.put("access_key", accessKey);
        params.put("nonce", authenticationToken);

        if (queryString != null && !queryString.isEmpty()) {
            try {
                String queryHash = makeQueryHash(queryString);
                params.put("query_hash", queryHash);
                params.put("query_hash_alg", "SHA512");
            } catch (Exception e) {
                log.error("Failed to create query hash", e);
            }
        }

        String jwtToken = createJwtToken(params, secretKey);
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + jwtToken);
        return headers;
    }

    /**
     * JWT 토큰 생성
     */
    private String createJwtToken(Map<String, String> params, String secretKey) {
        try {
            String jsonParams = objectMapper.writeValueAsString(params);
            
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hash = mac.doFinal(jsonParams.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed to create JWT token", e);
            return "";
        }
    }

    /**
     * 쿼리 해시 생성
     */
    private String makeQueryHash(String queryString) throws NoSuchAlgorithmException {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder sb = new StringBuilder();
        for (byte b : md.digest()) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 시세 정보 조회
     */
    public Flux<TickerDto> getTicker(List<String> markets) {
        String marketParam = String.join(",", markets);
        String url = UriComponentsBuilder.fromHttpUrl(upbitConfig.getBaseUrl() + "/v1/ticker")
                .queryParam("markets", marketParam)
                .toUriString();

        Map<String, String> headers = createAuthHeaders("");
        
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                .retrieve()
                .bodyToFlux(TickerDto.class)
                .doOnError(e -> log.error("Failed to get ticker: {}", e.getMessage()));
    }

    /**
     * 분 캔들 조회
     */
    public Flux<CandleDto> getMinuteCandles(String market, String unit, Integer count) {
        String url = UriComponentsBuilder.fromHttpUrl(upbitConfig.getBaseUrl() + "/v1/candles/minutes/" + unit)
                .queryParam("market", market)
                .queryParam("count", count)
                .toUriString();

        Map<String, String> headers = createAuthHeaders("");
        
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                .retrieve()
                .bodyToFlux(CandleDto.class)
                .doOnError(e -> log.error("Failed to get minute candles: {}", e.getMessage()));
    }

    /**
     * 일 캔들 조회
     */
    public Flux<CandleDto> getDayCandles(String market, Integer count) {
        String url = UriComponentsBuilder.fromHttpUrl(upbitConfig.getBaseUrl() + "/v1/candles/days")
                .queryParam("market", market)
                .queryParam("count", count)
                .toUriString();

        Map<String, String> headers = createAuthHeaders("");
        
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                .retrieve()
                .bodyToFlux(CandleDto.class)
                .doOnError(e -> log.error("Failed to get day candles: {}", e.getMessage()));
    }
} 