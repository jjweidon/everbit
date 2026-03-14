package com.everbit.everbit.user.api;

import com.everbit.everbit.auth.support.CurrentOwnerId;
import com.everbit.everbit.integrations.upbit.UpbitApiException;
import org.springframework.lang.NonNull;
import com.everbit.everbit.integrations.upbit.UpbitExchangeClient;
import com.everbit.everbit.integrations.upbit.UpbitException;
import com.everbit.everbit.user.application.UpbitKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v2/upbit/key")
@RequiredArgsConstructor
public class UpbitKeyController {

	private final UpbitKeyService upbitKeyService;
	private final UpbitExchangeClient upbitExchangeClient;

	@GetMapping("/status")
	public ResponseEntity<UpbitKeyStatusResponse> status(@CurrentOwnerId @NonNull Long ownerId) {
		if (!upbitKeyService.hasKey(ownerId)) {
			return ResponseEntity.ok(new UpbitKeyStatusResponse(
				UpbitKeyStatusResponse.NOT_REGISTERED, null, null));
		}
		return upbitKeyService.getDecryptedCredentials(ownerId)
			.map(creds -> {
				try {
					upbitExchangeClient.getAccounts(creds.accessKey(), creds.secretKey());
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.REGISTERED, java.time.Instant.now(), null));
				} catch (UpbitApiException e) {
					String code = e.is418() ? "UPBIT_BLOCKED_418" : e.is429() ? "RATE_LIMIT_429" : "UPBIT_API_ERROR";
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, code));
				} catch (UpbitException e) {
					log.warn("Upbit key verification failed for owner (no key logged)");
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, "UPBIT_ERROR"));
				} catch (Exception e) {
					log.warn("Upbit key verification failed for owner (no key logged)");
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, "VERIFICATION_FAILED"));
				}
			})
			.orElse(ResponseEntity.ok(new UpbitKeyStatusResponse(
				UpbitKeyStatusResponse.NOT_REGISTERED, null, null)));
	}

	@PostMapping
	public ResponseEntity<UpbitKeyStatusResponse> register(
		@CurrentOwnerId @NonNull Long ownerId,
		@Valid @RequestBody UpbitKeyRegisterRequest request) {
		upbitKeyService.register(ownerId, request.accessKey(), request.secretKey());
		return upbitKeyService.getDecryptedCredentials(ownerId)
			.map(creds -> {
				try {
					upbitExchangeClient.getAccounts(creds.accessKey(), creds.secretKey());
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.REGISTERED, java.time.Instant.now(), null));
				} catch (UpbitApiException e) {
					String code = e.is418() ? "UPBIT_BLOCKED_418" : e.is429() ? "RATE_LIMIT_429" : "UPBIT_API_ERROR";
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, code));
				} catch (UpbitException e) {
					log.warn("Upbit key verification failed after register (no key logged)");
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, "UPBIT_ERROR"));
				} catch (Exception e) {
					log.warn("Upbit key verification failed after register (no key logged)");
					return ResponseEntity.ok(new UpbitKeyStatusResponse(
						UpbitKeyStatusResponse.VERIFICATION_FAILED, null, "VERIFICATION_FAILED"));
				}
			})
			.orElse(ResponseEntity.ok(new UpbitKeyStatusResponse(
				UpbitKeyStatusResponse.REGISTERED, null, null)));
	}

	@DeleteMapping
	public ResponseEntity<Void> delete(@CurrentOwnerId @NonNull Long ownerId) {
		upbitKeyService.delete(ownerId);
		return ResponseEntity.noContent().build();
	}
}
