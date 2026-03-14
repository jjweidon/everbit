package com.everbit.everbit.global.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

@Slf4j
@Component
public class AesGcmUpbitKeyCrypto {

	private static final String ALG = "AES/GCM/NoPadding";
	private static final int IV_LEN = 12;
	private static final int TAG_LEN_BITS = 128;
	private static final int KEY_LEN_BYTES = 32;

	private final UpbitKeyCryptoProperties properties;

	public AesGcmUpbitKeyCrypto(UpbitKeyCryptoProperties properties) {
		this.properties = properties;
	}

	public byte[] encrypt(String plaintext) {
		if (!properties.isConfigured()) {
			throw new IllegalStateException("Upbit key master key not configured");
		}
		SecretKey key = deriveKey(properties.masterKey());
		byte[] iv = new byte[IV_LEN];
		new SecureRandom().nextBytes(iv);
		try {
			Cipher cipher = Cipher.getInstance(ALG);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
			return ByteBuffer.allocate(IV_LEN + ciphertext.length).put(iv).put(ciphertext).array();
		} catch (Exception e) {
			log.warn("Upbit key encrypt failed (no plaintext logged)");
			throw new RuntimeException("Upbit key encryption failed", e);
		}
	}

	public String decrypt(byte[] ciphertextWithIv) {
		if (!properties.isConfigured()) {
			throw new IllegalStateException("Upbit key master key not configured");
		}
		if (ciphertextWithIv == null || ciphertextWithIv.length <= IV_LEN + 16) {
			throw new IllegalArgumentException("Invalid ciphertext length");
		}
		SecretKey key = deriveKey(properties.masterKey());
		byte[] iv = Arrays.copyOfRange(ciphertextWithIv, 0, IV_LEN);
		byte[] ciphertext = Arrays.copyOfRange(ciphertextWithIv, IV_LEN, ciphertextWithIv.length);
		try {
			Cipher cipher = Cipher.getInstance(ALG);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
			return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
		} catch (Exception e) {
			log.warn("Upbit key decrypt failed (no ciphertext logged)");
			throw new RuntimeException("Upbit key decryption failed", e);
		}
	}

	private static SecretKey deriveKey(String masterKey) {
		byte[] raw = masterKey.getBytes(StandardCharsets.UTF_8);
		if (raw.length >= KEY_LEN_BYTES) {
			return new SecretKeySpec(Arrays.copyOf(raw, KEY_LEN_BYTES), "AES");
		}
		return new SecretKeySpec(Arrays.copyOf(raw, KEY_LEN_BYTES), "AES");
	}
}
