package com.everbit.everbit.trade.repository;

import com.everbit.everbit.trade.domain.PnlSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * PnL 스냅샷 저장소. SoT: docs/architecture/data-model.md §2.15.
 */
public interface PnlSnapshotRepository extends JpaRepository<PnlSnapshot, Long> {

	Optional<PnlSnapshot> findTop1ByOwner_IdOrderByCapturedAtDesc(Long ownerId);

	List<PnlSnapshot> findByOwner_IdAndCapturedAt(Long ownerId, Instant capturedAt);
}
