package com.everbit.everbit.integrations.upbit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Upbit REST API 인증용 JWT 생성. SoT: docs/integrations/upbit.md §3, Upbit 인증 문서.
 * Access/Secret은 호출 측에서 복호화 후 전달하며, 이 클래스는 로그에 남기지 않는다.
 */
public final class UpbitJwtProvider {

	private static final String QUERY_HASH_ALG = "SHA512";

	private UpbitJwtProvider() {}

	/**
	 * @param accessKey Upbit Access Key (평문, 로그에 남기지 말 것)
	 * @param secretKey Upbit Secret Key (평문, 로그에 남기지 말 것)
	 * @param queryString GET이면 query string, POST/DELETE면 body를 query string 형태로. 없으면 "".
	 */
	public static String createToken(String accessKey, String secretKey, String queryString) {
		Algorithm algorithm = Algorithm.HMAC256(secretKey);
		String nonce = UUID.randomUUID().toString();
		if (queryString != null && !queryString.isEmpty()) {
			String queryHash = makeQueryHash(queryString);
			return JWT.create()
				.withClaim("access_key", accessKey)
				.withClaim("nonce", nonce)
				.withClaim("query_hash", queryHash)
				.withClaim("query_hash_alg", QUERY_HASH_ALG)
				.sign(algorithm);
		}
		return JWT.create()
			.withClaim("access_key", accessKey)
			.withClaim("nonce", nonce)
			.sign(algorithm);
	}

	static String makeQueryHash(String queryString) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.update(queryString.getBytes(StandardCharsets.UTF_8));
			byte[] digest = md.digest();
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new UpbitException("SHA-512 not available", e);
		}
	}
}
