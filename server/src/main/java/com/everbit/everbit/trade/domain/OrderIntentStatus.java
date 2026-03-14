package com.everbit.everbit.trade.domain;

/**
 * OrderIntent status. SoT: docs/architecture/data-model.md §1.5.
 */
public enum OrderIntentStatus {
	CREATED,
	CANCELED,
	COMPLETED
}
