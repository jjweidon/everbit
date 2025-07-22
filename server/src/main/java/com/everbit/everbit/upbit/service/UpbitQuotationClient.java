package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.core.ParameterizedTypeReference;

import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitQuotationClient {
    private static final String V1_TICKER_ALL = "/v1/ticker/all";
    private static final String V1_TICKER = "/v1/ticker";

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
}
