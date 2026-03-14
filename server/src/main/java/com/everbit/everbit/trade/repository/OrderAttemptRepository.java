package com.everbit.everbit.trade.repository;

import com.everbit.everbit.trade.domain.OrderAttempt;
import com.everbit.everbit.trade.domain.OrderAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

/**
 * OrderAttempt 저장소. SoT: docs/architecture/data-model.md §2.11.
 */
public interface OrderAttemptRepository extends JpaRepository<OrderAttempt, Long> {

	long countByOwner_IdAndStatusAndCreatedAtAfter(Long ownerId, OrderAttemptStatus status, Instant since);
}
