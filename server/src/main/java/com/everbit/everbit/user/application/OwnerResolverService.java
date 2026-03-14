package com.everbit.everbit.user.application;

import java.util.Objects;

import com.everbit.everbit.user.domain.AppUser;
import com.everbit.everbit.user.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OWNER 조회/생성. 1인 전용 락 적용.
 * SoT: docs/requirements/functional.md FR-AUTH-002.
 */
@Service
@RequiredArgsConstructor
public class OwnerResolverService implements OwnerResolver {

	private final AppUserRepository appUserRepository;

	@Override
	@Transactional
	public AppUser findOrCreateOwner(String kakaoId, String email) {
		return appUserRepository.findByKakaoId(kakaoId)
			.orElseGet(() -> createOwnerIfFirst(kakaoId, email));
	}

	private AppUser createOwnerIfFirst(String kakaoId, String email) {
		if (appUserRepository.count() > 0) {
			throw new NotOwnerException();
		}
		AppUser user = Objects.requireNonNull(AppUser.create(kakaoId, email));
		return appUserRepository.save(user);
	}
}
