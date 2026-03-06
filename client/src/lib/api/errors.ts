/**
 * API 에러 타입. SoT: docs/api/contracts.md §9, §11
 */

import type { ApiErrorBody } from "@/types/api-contracts";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly body: ApiErrorBody,
    public readonly response?: Response
  ) {
    super(body.message);
    this.name = "ApiError";
  }

  get code(): string {
    return this.body.code;
  }

  get reasonCode(): string | undefined {
    return this.body.reasonCode;
  }

  get details(): Record<string, unknown> | undefined {
    return this.body.details;
  }

  get retryAfter(): number | undefined {
    const d = this.body.details;
    if (d && typeof d.retryAfter === "number") return d.retryAfter;
    return undefined;
  }

  get blockedUntil(): string | undefined {
    const d = this.body.details;
    if (d && typeof d.blockedUntil === "string") return d.blockedUntil;
    return undefined;
  }

  get is401(): boolean {
    return this.status === 401;
  }

  get is403(): boolean {
    return this.status === 403;
  }

  get is429(): boolean {
    return this.status === 429;
  }

  /** 418 또는 503(Upbit 418 대체) */
  get is418Or503(): boolean {
    return this.status === 418 || (this.status === 503 && this.body.reasonCode === "UPBIT_BLOCKED_418");
  }

  get is5xx(): boolean {
    return this.status >= 500 && this.status < 600;
  }
}
