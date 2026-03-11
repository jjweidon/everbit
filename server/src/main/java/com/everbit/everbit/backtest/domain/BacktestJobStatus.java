package com.everbit.everbit.backtest.domain;

/**
 * BacktestJob status. SoT: docs/architecture/data-model.md §1.5.
 */
public enum BacktestJobStatus {
	QUEUED,
	RUNNING,
	DONE,
	FAILED
}
