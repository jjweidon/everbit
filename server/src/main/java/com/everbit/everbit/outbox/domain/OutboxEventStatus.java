package com.everbit.everbit.outbox.domain;

/**
 * OutboxEvent status. SoT: docs/architecture/event-bus.md §4.2.
 */
public enum OutboxEventStatus {
	PENDING,
	PROCESSING,
	DONE,
	DEAD
}
