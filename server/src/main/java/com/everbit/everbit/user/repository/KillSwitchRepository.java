package com.everbit.everbit.user.repository;

import com.everbit.everbit.user.domain.KillSwitch;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Kill Switch 저장소. SoT: docs/architecture/data-model.md §2.8.
 */
public interface KillSwitchRepository extends JpaRepository<KillSwitch, Long> {
}
