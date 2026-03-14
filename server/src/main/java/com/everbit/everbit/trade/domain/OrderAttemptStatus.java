package com.everbit.everbit.trade.domain;

/**
 * OrderAttempt status. SoT: docs/architecture/data-model.md §1.5.
 */
public enum OrderAttemptStatus {
	PREPARED,
	SENT,
	ACKED,
	REJECTED,
	THROTTLED,
	UNKNOWN,
	SUSPENDED
}
