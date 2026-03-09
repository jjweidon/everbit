package com.everbit.everbit.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshSessionJpaRepository extends JpaRepository<RefreshSession, String>, RefreshSessionRepositoryCustom {
}
