package com.everbit.everbit.trade.domain;

/**
 * OrderIntent type. SoT: docs/architecture/data-model.md §1.5.
 */
public enum OrderIntentType {
	ENTRY,
	EXIT_STOPLOSS,
	EXIT_TP,
	EXIT_TRAIL,
	EXIT_TIME
}
