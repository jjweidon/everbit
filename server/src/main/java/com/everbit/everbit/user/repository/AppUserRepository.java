package com.everbit.everbit.user.repository;

import com.everbit.everbit.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * AppUser 저장소. SoT: docs/architecture/data-model.md.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	java.util.Optional<AppUser> findByKakaoId(String kakaoId);

	boolean existsByKakaoId(String kakaoId);

	/**
	 * OWNER 락 검사: 이미 등록된 계정이 존재하는지.
	 */
	long count();
}
