"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, EyeOff } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useApiOpts } from "@/hooks/useApiOpts";
import {
  deleteUpbitKey,
  getUpbitKeyStatus,
  registerUpbitKey,
} from "@/lib/api/endpoints";
import { getLogoutUrl } from "@/lib/api/config";
import { ApiError } from "@/lib/api/errors";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { ConfirmModal } from "@/components/ui/Modal";
import { useToast } from "@/components/ui/Toast";
import type { UpbitKeyStatusResponse } from "@/types/api-contracts";

const UPBIT_OPEN_API_URL =
  "https://upbit.com/upbit_user/private/signin?pathname=%2Fmypage%2Fopen_api_management";

function formatVerifiedAt(value?: string) {
  if (!value) return null;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  return d.toLocaleString("ko-KR");
}

function mapVerificationError(code?: string) {
  if (!code) return null;
  if (code === "UPBIT_BLOCKED_418")
    return "업비트 API가 일시적으로 차단(418)되었습니다. 잠시 후 다시 시도해 주세요.";
  if (code === "RATE_LIMIT_429")
    return "요청 제한(429)에 걸렸습니다. 잠시 후 다시 시도해 주세요.";
  if (code === "UPBIT_API_ERROR")
    return "업비트 API 응답이 비정상입니다. 키 권한/IP 제한을 확인해 주세요.";
  if (code === "UPBIT_ERROR") return "업비트 연동 중 오류가 발생했습니다.";
  if (code === "VERIFICATION_FAILED") return "키 검증에 실패했습니다.";
  return `키 검증에 실패했습니다. (${code})`;
}

export default function SettingsUpbitKeyPage() {
  const router = useRouter();
  const { isAuthenticated, accessToken, setAccessToken, authReady } = useAuth();
  const opts = useApiOpts();
  const toast = useToast();

  const [statusLoading, setStatusLoading] = useState(true);
  const [status, setStatus] = useState<UpbitKeyStatusResponse | null>(null);
  const [statusError, setStatusError] = useState("");

  const [accessKey, setAccessKey] = useState("");
  const [secretKey, setSecretKey] = useState("");
  const [showAccessKey, setShowAccessKey] = useState(false);
  const [showSecretKey, setShowSecretKey] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<{
    accessKey?: string;
    secretKey?: string;
  }>({});
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!authReady) return;
    if (!isAuthenticated) {
      router.replace("/login");
      return;
    }
    if (!accessToken) return;

    let cancelled = false;
    setStatusLoading(true);
    setStatusError("");
    getUpbitKeyStatus(opts)
      .then((res) => {
        if (cancelled) return;
        setStatus(res);
      })
      .catch((e) => {
        if (cancelled) return;
        setStatusError(
          e instanceof ApiError ? e.body.message : "키 상태 조회에 실패했습니다."
        );
      })
      .finally(() => {
        if (!cancelled) setStatusLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [authReady, isAuthenticated, accessToken, opts, router]);

  const verifiedAtText = useMemo(
    () => formatVerifiedAt(status?.lastVerifiedAt),
    [status?.lastVerifiedAt]
  );
  const verificationMessage = useMemo(
    () => mapVerificationError(status?.verificationErrorCode),
    [status?.verificationErrorCode]
  );

  const validate = (): boolean => {
    const next: { accessKey?: string; secretKey?: string } = {};
    if (!accessKey.trim()) next.accessKey = "Access Key를 입력하세요.";
    if (!secretKey.trim()) next.secretKey = "Secret Key를 입력하세요.";
    setFieldErrors(next);
    setSubmitError("");
    return Object.keys(next).length === 0;
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate() || submitting) return;
    setSubmitting(true);
    setSubmitError("");
    try {
      const res = await registerUpbitKey(
        { accessKey: accessKey.trim(), secretKey: secretKey.trim() },
        opts
      );
      setStatus(res);
      if (res.status === "REGISTERED") {
        toast.add("업비트 키가 등록되었습니다.", "success");
        setAccessKey("");
        setSecretKey("");
        return;
      }
      if (res.status === "VERIFICATION_FAILED") {
        setSubmitError(
          mapVerificationError(res.verificationErrorCode) ??
            "키 검증에 실패했습니다."
        );
      }
    } catch (err) {
      setSubmitError(err instanceof ApiError ? err.body.message : "등록에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (deleting) return;
    setDeleting(true);
    try {
      await deleteUpbitKey(opts);
      toast.add("업비트 키를 삭제했습니다. 로그아웃합니다.", "info");
      setAccessToken(null);
      if (typeof window !== "undefined") {
        window.location.href = getLogoutUrl();
      }
    } catch (err) {
      toast.add(
        err instanceof ApiError ? err.body.message : "삭제에 실패했습니다.",
        "error"
      );
    } finally {
      setDeleting(false);
    }
  };

  const isRegistered = status?.status === "REGISTERED";

  if (!authReady) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-text-3">
        <div className="h-7 w-7 animate-spin rounded-full border-2 border-borderSubtle border-t-text-3" />
      </div>
    );
  }

  if (authReady && !isAuthenticated) {
    return null;
  }

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div className="space-y-1">
        <h1 className="text-xl font-semibold text-text-heading">설정 · 업비트 API 키</h1>
        <p className="text-sm text-text-2">
          업비트에서 발급받은 API 키를 등록/삭제할 수 있습니다. 키는 서버에 암호화되어 저장됩니다.
        </p>
      </div>

      <div className="rounded-lg border border-borderSubtle bg-bg2 p-4 sm:p-6">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <div className="space-y-1">
            <div className="text-sm font-medium text-text-1">현재 상태</div>
            {statusLoading ? (
              <div className="text-sm text-text-3">조회 중…</div>
            ) : statusError ? (
              <div className="text-sm text-red">{statusError}</div>
            ) : (
              <div className="text-sm text-text-2">
                <span className="font-medium text-text-1">
                  {status?.status === "REGISTERED"
                    ? "등록됨"
                    : status?.status === "NOT_REGISTERED"
                      ? "미등록"
                      : "검증 실패"}
                </span>
                {verifiedAtText && (
                  <span className="ml-2 text-text-3">(마지막 검증: {verifiedAtText})</span>
                )}
              </div>
            )}
            {!statusLoading && !statusError && verificationMessage && (
              <div className="text-sm text-red">{verificationMessage}</div>
            )}
          </div>
          <a
            href={UPBIT_OPEN_API_URL}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex shrink-0 items-center justify-center gap-2 rounded-token-md border border-borderSubtle bg-bg1 px-4 py-2.5 text-sm font-medium text-text-1 transition-colors hover:bg-bg2 focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1"
          >
            업비트 API 키 발급받기 <span className="text-cyan" aria-hidden>↗</span>
          </a>
        </div>
      </div>

      {!isRegistered && (
        <form
          onSubmit={handleRegister}
          className="rounded-lg border border-borderSubtle bg-bg2 p-4 sm:p-6"
        >
          <h2 className="text-base font-semibold text-text-1">키 등록</h2>
          <p className="mt-1 text-sm text-text-2">
            Access Key / Secret Key를 입력하고 저장하세요.
          </p>

          <div className="mt-4 space-y-4">
            <div>
              <label
                htmlFor="settings-upbit-access-key"
                className="mb-1 block text-sm font-medium text-text-2"
              >
                Access Key
              </label>
              <div className="relative">
                <Input
                  id="settings-upbit-access-key"
                  type={showAccessKey ? "text" : "password"}
                  value={accessKey}
                  onChange={(e) => setAccessKey(e.target.value)}
                  placeholder="Access Key를 입력하세요"
                  error={!!fieldErrors.accessKey}
                  autoComplete="off"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowAccessKey((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-text-3 hover:text-text-2 focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1"
                  aria-label={showAccessKey ? "Access Key 숨기기" : "Access Key 표시"}
                  tabIndex={0}
                >
                  {showAccessKey ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {fieldErrors.accessKey && (
                <p className="mt-1 text-xs text-red">{fieldErrors.accessKey}</p>
              )}
            </div>

            <div>
              <label
                htmlFor="settings-upbit-secret-key"
                className="mb-1 block text-sm font-medium text-text-2"
              >
                Secret Key
              </label>
              <div className="relative">
                <Input
                  id="settings-upbit-secret-key"
                  type={showSecretKey ? "text" : "password"}
                  value={secretKey}
                  onChange={(e) => setSecretKey(e.target.value)}
                  placeholder="Secret Key를 입력하세요"
                  error={!!fieldErrors.secretKey}
                  autoComplete="off"
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowSecretKey((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-text-3 hover:text-text-2 focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1"
                  aria-label={showSecretKey ? "Secret Key 숨기기" : "Secret Key 표시"}
                  tabIndex={0}
                >
                  {showSecretKey ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
              {fieldErrors.secretKey && (
                <p className="mt-1 text-xs text-red">{fieldErrors.secretKey}</p>
              )}
            </div>

            {submitError && (
              <div
                className="rounded-token-md border border-red/50 bg-red/10 px-3 py-2 text-sm text-red"
                role="alert"
              >
                {submitError}
              </div>
            )}
          </div>

          <div className="mt-6 flex items-center gap-2">
            <Button type="submit" variant="primary" disabled={submitting}>
              {submitting ? "저장 중…" : "저장"}
            </Button>
          </div>
        </form>
      )}

      {isRegistered && (
        <div className="rounded-lg border border-red/40 bg-red/5 p-4 sm:p-6">
          <h2 className="text-base font-semibold text-text-1">키 삭제</h2>
          <p className="mt-1 text-sm text-text-2">
            키를 삭제하면 <span className="font-medium text-text-1">즉시 로그아웃</span> 처리됩니다.
            서비스를 계속 이용하려면 다시 업비트 키를 등록해야 합니다.
          </p>
          <div className="mt-4">
            <Button
              variant="destructive"
              onClick={() => setDeleteOpen(true)}
              disabled={deleting}
            >
              {deleting ? "삭제 중…" : "업비트 키 삭제"}
            </Button>
          </div>
        </div>
      )}

      <ConfirmModal
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        title="업비트 키를 삭제할까요?"
        confirmLabel="삭제"
        cancelLabel="취소"
        destructive
        onConfirm={handleDelete}
      >
        <div className="space-y-2">
          <p>삭제 후에는 서비스를 이용하려면 업비트 키를 다시 등록해야 합니다.</p>
          <p className="text-text-3">삭제가 완료되면 자동으로 로그아웃됩니다.</p>
        </div>
      </ConfirmModal>
    </div>
  );
}

