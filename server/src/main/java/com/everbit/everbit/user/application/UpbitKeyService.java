package com.everbit.everbit.user.application;

import com.everbit.everbit.global.crypto.AesGcmUpbitKeyCrypto;
import com.everbit.everbit.user.domain.AppUser;
import com.everbit.everbit.user.domain.UpbitKey;
import com.everbit.everbit.user.repository.AppUserRepository;
import com.everbit.everbit.user.repository.UpbitKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpbitKeyService {

	private static final int KEY_VERSION = 1;

	private final UpbitKeyRepository upbitKeyRepository;
	private final AppUserRepository appUserRepository;
	private final AesGcmUpbitKeyCrypto upbitKeyCrypto;

	public Optional<DecryptedUpbitCredentials> getDecryptedCredentials(@NonNull Long ownerId) {
		return upbitKeyRepository.findById(ownerId)
			.map(key -> new DecryptedUpbitCredentials(
				upbitKeyCrypto.decrypt(key.getAccessKeyEnc()),
				upbitKeyCrypto.decrypt(key.getSecretKeyEnc())
			));
	}

	public boolean hasKey(@NonNull Long ownerId) {
		return upbitKeyRepository.findById(ownerId).isPresent();
	}

	@Transactional
	public void register(@NonNull Long ownerId, String accessKeyPlain, String secretKeyPlain) {
		AppUser owner = appUserRepository.findById(ownerId)
			.orElseThrow(() -> new OwnerNotFoundException(ownerId));
		byte[] accessEnc = upbitKeyCrypto.encrypt(accessKeyPlain);
		byte[] secretEnc = upbitKeyCrypto.encrypt(secretKeyPlain);
		upbitKeyRepository.findById(ownerId)
			.ifPresentOrElse(
				k -> k.rotate(accessEnc, secretEnc, KEY_VERSION),
				() -> upbitKeyRepository.save(Objects.requireNonNull(UpbitKey.create(owner, accessEnc, secretEnc, KEY_VERSION)))
			);
	}

	@Transactional
	public void delete(@NonNull Long ownerId) {
		upbitKeyRepository.deleteById(ownerId);
	}
}
