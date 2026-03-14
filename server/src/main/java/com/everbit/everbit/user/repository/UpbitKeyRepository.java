package com.everbit.everbit.user.repository;

import com.everbit.everbit.user.domain.UpbitKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UpbitKeyRepository extends JpaRepository<UpbitKey, Long> {

	@Override
	Optional<UpbitKey> findById(Long ownerId);
}
