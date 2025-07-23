package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.upbit.dto.quotation.MinuteCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.DayCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.WeekCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.MonthCandleResponse;
import com.everbit.everbit.upbit.dto.quotation.SecondCandleResponse;
import com.everbit.everbit.upbit.exception.UpbitException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.time.OffsetDateTime;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitQuotationClient {
    private static final String V1_TICKER_ALL = "/v1/ticker/all";
    private static final String V1_TICKER = "/v1/ticker";
    private static final String V1_CANDLES_MINUTES = "/v1/candles/minutes/{unit}";
    private static final String V1_CANDLES_DAYS = "/v1/candles/days";
    private static final String V1_CANDLES_WEEKS = "/v1/candles/weeks";
    private static final String V1_CANDLES_MONTHS = "/v1/candles/months";
    private static final String V1_CANDLES_SECONDS = "/v1/candles/seconds";

    private final UpbitConfig upbitConfig;
    private final RestTemplate restTemplate;

    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private <T> T parseResponse(ResponseEntity<String> response, ParameterizedTypeReference<T> responseType, String operation) {
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                return createObjectMapper().readValue(response.getBody(), 
                    createObjectMapper().getTypeFactory().constructType(responseType.getType()));
            } catch (Exception e) {
                log.error("{} 응답 파싱 실패: {}", operation, response.getBody(), e);
                throw new UpbitException(operation + " 응답 파싱 실패: " + e.getMessage());
            }
        }
        throw new UpbitException(operation + " 실행 실패: " + response.getStatusCode());
    }

    // 종목 단위 현재가 정보 조회
    public List<TickerResponse> getTickers(List<String> markets) {
        if (markets == null || markets.isEmpty()) {
            throw new UpbitException("마켓 코드는 필수 입력값입니다");
        }

        try {
            String marketsParam = markets.stream().collect(Collectors.joining(","));
            
            URI uri = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_TICKER)
                .queryParam("markets", marketsParam)
                .build()
                .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("특정 마켓 ticker 조회 요청 실행: {}", marketsParam);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<TickerResponse>>() {}, "특정 마켓 ticker 조회");
        } catch (Exception e) {
            log.error("ticker 조회 실패", e);
            throw new UpbitException("ticker 조회 실패: " + e.getMessage());
        }
    }

    // 마켓 단위 현재가 정보 조회
    public List<TickerResponse> getAllTickers(List<String> quoteCurrencies) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_TICKER_ALL);

            if (quoteCurrencies != null && !quoteCurrencies.isEmpty()) {
                String quoteCurrenciesParam = quoteCurrencies.stream().collect(Collectors.joining(","));
                builder.queryParam("quote_currencies", quoteCurrenciesParam);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("전체 ticker 조회 요청 실행: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<TickerResponse>>() {}, "전체 ticker 조회");
        } catch (Exception e) {
            log.error("전체 ticker 조회 실패", e);
            throw new UpbitException("전체 ticker 조회 실패: " + e.getMessage());
        }
    }

    public List<SecondCandleResponse> getSecondCandles(String market, OffsetDateTime to, Integer count) {
        if (market == null || market.isEmpty()) {
            throw new UpbitException("Market parameter is required");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("Count must be between 1 and 200");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_CANDLES_SECONDS)
                .queryParam("market", market);

            if (to != null) {
                builder.queryParam("to", to.toString());
            }

            if (count != null) {
                builder.queryParam("count", count);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("초단위 캔들 조회 요청 실행: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<SecondCandleResponse>>() {}, "get second candles");
        } catch (Exception e) {
            log.error("초단위 캔들 조회 실패", e);
            throw new UpbitException("Failed to get second candles: " + e.getMessage());
        }
    }

    public List<MinuteCandleResponse> getMinuteCandles(int unit, String market, OffsetDateTime to, Integer count) {
        if (!List.of(1, 3, 5, 10, 15, 30, 60, 240).contains(unit)) {
            throw new UpbitException("잘못된 단위값입니다. 허용된 값: 1, 3, 5, 10, 15, 30, 60, 240");
        }

        if (market == null || market.isEmpty()) {
            throw new UpbitException("마켓 코드는 필수 입력값입니다");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("조회 개수는 1-200 사이여야 합니다");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_CANDLES_MINUTES)
                .queryParam("market", market);

            if (to != null) {
                builder.queryParam("to", to.toString());
            }

            if (count != null) {
                builder.queryParam("count", count);
            }

            URI uri = builder.buildAndExpand(unit).toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("{}분봉 캔들 조회 요청 실행: {}", unit, uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<MinuteCandleResponse>>() {}, unit + "분봉 캔들 조회");
        } catch (Exception e) {
            log.error("{}분봉 캔들 조회 실패", unit, e);
            throw new UpbitException(unit + "분봉 캔들 조회 실패: " + e.getMessage());
        }
    }

    public List<DayCandleResponse> getDayCandles(String market, OffsetDateTime to, Integer count, String convertingPriceUnit) {
        if (market == null || market.isEmpty()) {
            throw new UpbitException("Market parameter is required");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("Count must be between 1 and 200");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_CANDLES_DAYS)
                .queryParam("market", market);

            if (to != null) {
                builder.queryParam("to", to.toString());
            }

            if (count != null) {
                builder.queryParam("count", count);
            }

            if (convertingPriceUnit != null && !convertingPriceUnit.isEmpty()) {
                builder.queryParam("converting_price_unit", convertingPriceUnit);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("일봉 캔들 조회 요청 실행: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<DayCandleResponse>>() {}, "get day candles");
        } catch (Exception e) {
            log.error("일봉 캔들 조회 실패", e);
            throw new UpbitException("Failed to get day candles: " + e.getMessage());
        }
    }

    public List<WeekCandleResponse> getWeekCandles(String market, OffsetDateTime to, Integer count) {
        if (market == null || market.isEmpty()) {
            throw new UpbitException("Market parameter is required");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("Count must be between 1 and 200");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_CANDLES_WEEKS)
                .queryParam("market", market);

            if (to != null) {
                builder.queryParam("to", to.toString());
            }

            if (count != null) {
                builder.queryParam("count", count);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("주봉 캔들 조회 요청 실행: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<WeekCandleResponse>>() {}, "get week candles");
        } catch (Exception e) {
            log.error("주봉 캔들 조회 실패", e);
            throw new UpbitException("Failed to get week candles: " + e.getMessage());
        }
    }

    public List<MonthCandleResponse> getMonthCandles(String market, OffsetDateTime to, Integer count) {
        if (market == null || market.isEmpty()) {
            throw new UpbitException("Market parameter is required");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("Count must be between 1 and 200");
        }

        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(upbitConfig.getBaseUrl())
                .path(V1_CANDLES_MONTHS)
                .queryParam("market", market);

            if (to != null) {
                builder.queryParam("to", to.toString());
            }

            if (count != null) {
                builder.queryParam("count", count);
            }

            URI uri = builder.build().toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("accept", "application/json");

            HttpEntity<?> entity = new HttpEntity<>(headers);

            log.info("월봉 캔들 조회 요청 실행: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<MonthCandleResponse>>() {}, "get month candles");
        } catch (Exception e) {
            log.error("월봉 캔들 조회 실패", e);
            throw new UpbitException("Failed to get month candles: " + e.getMessage());
        }
    }
}
