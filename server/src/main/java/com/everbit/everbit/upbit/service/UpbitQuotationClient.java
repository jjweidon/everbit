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
                log.error("Failed to parse {} response: {}", operation, response.getBody(), e);
                throw new UpbitException("Failed to parse " + operation + " response: " + e.getMessage());
            }
        }
        throw new UpbitException("Failed to execute " + operation + ": " + response.getStatusCode());
    }

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

            log.info("Executing GET request - get all tickers: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<TickerResponse>>() {}, "get all tickers");
        } catch (Exception e) {
            log.error("Failed to get all tickers", e);
            throw new UpbitException("Failed to get all tickers: " + e.getMessage());
        }
    }

    public List<TickerResponse> getTickers(List<String> markets) {
        if (markets == null || markets.isEmpty()) {
            throw new UpbitException("Markets parameter is required");
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

            log.info("Executing GET request - get tickers for markets: {}", marketsParam);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<TickerResponse>>() {}, "get tickers");
        } catch (Exception e) {
            log.error("Failed to get tickers", e);
            throw new UpbitException("Failed to get tickers: " + e.getMessage());
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

            log.info("Executing GET request - get second candles: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<SecondCandleResponse>>() {}, "get second candles");
        } catch (Exception e) {
            log.error("Failed to get second candles", e);
            throw new UpbitException("Failed to get second candles: " + e.getMessage());
        }
    }

    public List<MinuteCandleResponse> getMinuteCandles(int unit, String market, OffsetDateTime to, Integer count) {
        if (!List.of(1, 3, 5, 10, 15, 30, 60, 240).contains(unit)) {
            throw new UpbitException("Invalid unit value. Allowed values are: 1, 3, 5, 10, 15, 30, 60, 240");
        }

        if (market == null || market.isEmpty()) {
            throw new UpbitException("Market parameter is required");
        }

        if (count != null && (count < 1 || count > 200)) {
            throw new UpbitException("Count must be between 1 and 200");
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

            log.info("Executing GET request - get minute candles: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<MinuteCandleResponse>>() {}, "get minute candles");
        } catch (Exception e) {
            log.error("Failed to get minute candles", e);
            throw new UpbitException("Failed to get minute candles: " + e.getMessage());
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

            log.info("Executing GET request - get day candles: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<DayCandleResponse>>() {}, "get day candles");
        } catch (Exception e) {
            log.error("Failed to get day candles", e);
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

            log.info("Executing GET request - get week candles: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<WeekCandleResponse>>() {}, "get week candles");
        } catch (Exception e) {
            log.error("Failed to get week candles", e);
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

            log.info("Executing GET request - get month candles: {}", uri);

            ResponseEntity<String> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                entity,
                String.class
            );

            return parseResponse(response, new ParameterizedTypeReference<List<MonthCandleResponse>>() {}, "get month candles");
        } catch (Exception e) {
            log.error("Failed to get month candles", e);
            throw new UpbitException("Failed to get month candles: " + e.getMessage());
        }
    }
}
