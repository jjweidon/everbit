package com.everbit.everbit.user.application;

import com.everbit.everbit.user.domain.KillSwitch;
import com.everbit.everbit.user.repository.KillSwitchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kill Switch 조회. 대시보드 등에서 accountEnabled, strategyEnabled 조회용.
 * SoT: docs/architecture/data-model.md §2.8, modular-monolith §3.3 (공개 API).
 */
@Service
@RequiredArgsConstructor
public class KillSwitchQueryService {

	private final KillSwitchRepository killSwitchRepository;

	@Transactional(readOnly = true)
	public boolean isAccountEnabled(Long ownerId) {
		return killSwitchRepository.findById(Objects.requireNonNull(ownerId))
			.map(KillSwitch::isAccountEnabled)
			.orElse(true);
	}

	/**
	 * 해당 전략이 허용 목록에 있는지. 없으면 true(기본 허용).
	 */
	@Transactional(readOnly = true)
	public boolean isStrategyEnabled(Long ownerId, String strategyKey) {
		return killSwitchRepository.findById(Objects.requireNonNull(ownerId))
			.map(ks -> isStrategyInEnabled(ks.getEnabledStrategies(), strategyKey))
			.orElse(true);
	}

	private static boolean isStrategyInEnabled(JsonNode enabledStrategies, String strategyKey) {
		if (enabledStrategies == null || !enabledStrategies.isArray()) {
			return true;
		}
		for (JsonNode node : enabledStrategies) {
			if (strategyKey.equals(node.asText())) {
				return true;
			}
		}
		return enabledStrategies.isEmpty();
	}
}
