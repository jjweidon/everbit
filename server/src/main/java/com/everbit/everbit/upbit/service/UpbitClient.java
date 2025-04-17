package com.everbit.everbit.upbit.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.everbit.everbit.global.config.UpbitConfig;
import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.member.exception.MemberException;
import com.everbit.everbit.member.repository.MemberRepository;
import com.everbit.everbit.member.service.MemberService;
import com.everbit.everbit.upbit.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Upbit API 클라이언트
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitClient {

    private final UpbitConfig upbitConfig;
    private final MemberService memberService;

    private URI buildUrl(String path) {
        String baseUrl = upbitConfig.getBaseUrl();
        return URI.create(baseUrl + path);
    }

    public String getAccounts(String username) {
        try {
            URI uri = buildUrl("/v1/accounts");
            URL url = uri.toURL();
            Member member = memberService.findMemberByUsername(username);
            String token = createAuthHeaders("", member);

            log.info("Request URL: {}", url);
            log.info("Request Headers: Bearer {}", token);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + token);

            log.info("Starting Upbit API request");
            
            int responseCode = conn.getResponseCode();
            log.info("Response Code: {}", responseCode);

            BufferedReader in;
            if (responseCode >= 200 && responseCode < 300) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String responseBody = response.toString();
            if (responseCode >= 400) {
                log.error("Failed to get accounts: {}", responseBody);
                throw new RuntimeException("Failed to get accounts: " + responseBody);
            }

            return responseBody;
        } catch (IOException e) {
            log.error("Failed to get accounts", e);
            throw new RuntimeException("Failed to get accounts", e);
        }
    }

    /**
     * 인증 헤더 생성
     */
    private String createAuthHeaders(String queryString, Member member) {
        try {
            Account account = memberService.findAccountByMember(member);
            String accessKey = account.getUpbitAccessKey();
            String secretKey = account.getUpbitSecretKey();
            String nonce = UUID.randomUUID().toString();

            log.info("Access Key: {}", accessKey);
            log.info("Nonce: {}", nonce);

            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String jwtToken;

            if (queryString != null && !queryString.isEmpty()) {
                String queryHash = makeQueryHash(queryString);
                jwtToken = JWT.create()
                        .withClaim("access_key", accessKey)
                        .withClaim("nonce", nonce)
                        .withClaim("query_hash", queryHash)
                        .withClaim("query_hash_alg", "SHA512")
                        .sign(algorithm);
            } else {
                jwtToken = JWT.create()
                        .withClaim("access_key", accessKey)
                        .withClaim("nonce", nonce)
                        .sign(algorithm);
            }

            log.info("Generated JWT Token: {}", jwtToken);
            return jwtToken;
        } catch (Exception e) {
            log.error("Failed to create auth headers", e);
            throw new RuntimeException("Failed to create auth headers", e);
        }
    }

    /**
     * 쿼리 해시 생성
     */
    private String makeQueryHash(String queryString) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(queryString.getBytes("utf8"));
        return String.format("%0128x", new BigInteger(1, md.digest()));
    }
} 