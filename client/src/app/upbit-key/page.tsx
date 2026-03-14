"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { Eye, EyeOff } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { useApiOpts } from "@/hooks/useApiOpts";
import { getUpbitKeyStatus, registerUpbitKey } from "@/lib/api/endpoints";
import { ApiError } from "@/lib/api/errors";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";

const UPBIT_OPEN_API_URL = "https://upbit.com/upbit_user/private/signin?pathname=%2Fmypage%2Fopen_api_management";

const GUIDE_STEPS = [
  { step: 1, title: "업비트 로그인", description: "업비트 웹사이트에 로그인합니다.", imagePath: "/images/upbit-key/step1-login.png" },
  { step: 2, title: "QR 코드 인증", description: "보안을 위해 QR 코드로 2차 인증을 진행하고 로그인을 완료합니다.", imagePath: "/images/upbit-key/step2-qr.png" },
  { step: 3, title: "API 키 발급 요청", description: "Open API 관리 페이지에서 모든 항목 체크 후, IP 주소를 등록하고 Open API Key 발급을 요청합니다.", imagePath: "/images/upbit-key/step3-request-key.png" },
  { step: 4, title: "API 키 확인", description: "발급된 Access Key와 Secret Key를 확인합니다. 이 키들은 중요한 정보이므로 복사해서 안전하게 보관해주세요. Secret Key는 이 페이지를 벗어나면 다시 확인할 수 없습니다.", imagePath: "/images/upbit-key/step4-take-key.png" },
] as const;

export default function UpbitKeyPage() {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const opts = useApiOpts();
  const [accessKey, setAccessKey] = useState("");
  const [secretKey, setSecretKey] = useState("");
  const [showAccessKey, setShowAccessKey] = useState(false);
  const [showSecretKey, setShowSecretKey] = useState(false);
  const [submitError, setSubmitError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<{ accessKey?: string; secretKey?: string }>({});
  const [loading, setLoading] = useState(false);
  const [statusCheckDone, setStatusCheckDone] = useState(false);

  useEffect(() => {
    if (!isAuthenticated || !opts.accessToken) return;
    let cancelled = false;
    getUpbitKeyStatus(opts).then((res) => {
      if (cancelled) return;
      setStatusCheckDone(true);
      if (res.status === "REGISTERED") router.replace("/dashboard");
    }).catch(() => { if (!cancelled) setStatusCheckDone(true); });
    return () => { cancelled = true; };
  }, [isAuthenticated, opts, router]);

  const validate = (): boolean => {
    const next: { accessKey?: string; secretKey?: string } = {};
    if (!accessKey.trim()) next.accessKey = "Access Key를 입력하세요.";
    if (!secretKey.trim()) next.secretKey = "Secret Key를 입력하세요.";
    setFieldErrors(next);
    setSubmitError("");
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate() || loading) return;
    setLoading(true);
    setSubmitError("");
    try {
      const res = await registerUpbitKey({ accessKey: accessKey.trim(), secretKey: secretKey.trim() }, opts);
      if (res.status === "REGISTERED") { router.replace("/dashboard"); return; }
      if (res.status === "VERIFICATION_FAILED") {
        setSubmitError(res.verificationErrorCode === "UPBIT_BLOCKED_418" ? "업비트 API가 일시적으로 차단(418)되었습니다. 잠시 후 다시 시도해 주세요." : res.verificationErrorCode === "RATE_LIMIT_429" ? "요청 제한(429)에 걸렸습니다. 잠시 후 다시 시도해 주세요." : "키 검증에 실패했습니다. 키와 IP 제한을 확인해 주세요.");
      }
    } catch (err) {
      setSubmitError(err instanceof ApiError ? err.body.message : "등록에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  if (!statusCheckDone && isAuthenticated) {
    return (<div className="flex min-h-[40vh] items-center justify-center text-text-3"><div className="h-7 w-7 animate-spin rounded-full border-2 border-borderSubtle border-t-text-3" /></div>);
  }

  return (
    <div className="mx-auto max-w-4xl space-y-10">
      <h1 className="text-xl font-semibold text-text-heading">업비트 API 키 등록</h1>
      <div className="flex flex-col gap-4 rounded-lg border border-borderSubtle bg-bg2 p-4 sm:flex-row sm:items-center sm:justify-between sm:p-6">
        <p className="text-sm text-text-2 sm:text-base">업비트에서 발급받은 API 키를 등록해야 서비스를 이용할 수 있습니다. API 키는 암호화되어 보관되며, 읽기·주문 권한만 사용합니다.</p>
        <a href={UPBIT_OPEN_API_URL} target="_blank" rel="noopener noreferrer" className="inline-flex shrink-0 items-center justify-center gap-2 rounded-token-md border border-borderSubtle bg-bg1 px-4 py-2.5 text-sm font-medium text-text-1 transition-colors hover:bg-bg2 focus:ring-2 focus:ring-cyan focus:ring-offset-2 focus:ring-offset-bg1">업비트 API 키 발급받기 <span className="text-cyan" aria-hidden>↗</span></a>
      </div>
      <form onSubmit={handleSubmit} className="rounded-lg border border-borderSubtle bg-bg2 p-4 sm:p-6">
        <div className="space-y-4">
          <div>
            <label htmlFor="upbit-access-key" className="mb-1 block text-sm font-medium text-text-2">Access Key</label>
            <div className="relative">
              <Input
                id="upbit-access-key"
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
            {fieldErrors.accessKey && <p className="mt-1 text-xs text-red">{fieldErrors.accessKey}</p>}
          </div>
          <div>
            <label htmlFor="upbit-secret-key" className="mb-1 block text-sm font-medium text-text-2">Secret Key</label>
            <div className="relative">
              <Input
                id="upbit-secret-key"
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
            {fieldErrors.secretKey && <p className="mt-1 text-xs text-red">{fieldErrors.secretKey}</p>}
          </div>
          {submitError && <div className="rounded-token-md border border-red/50 bg-red/10 px-3 py-2 text-sm text-red" role="alert">{submitError}</div>}
        </div>
        <div className="mt-6"><Button type="submit" variant="primary" disabled={loading} className="w-full sm:w-auto">{loading ? "등록 중…" : "저장"}</Button></div>
      </form>
      <section aria-label="업비트 API 키 발급 가이드" className="space-y-6">
        <h2 className="text-lg font-semibold text-text-heading">업비트 API 키 발급 가이드</h2>
        <div className="space-y-8">{GUIDE_STEPS.map((step) => (
          <div key={step.step} className="overflow-hidden rounded-lg border border-borderSubtle bg-bg2"><div className="p-4 sm:p-6"><div className="mb-3 flex items-center gap-3"><span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-token-md bg-cyan/20 text-sm font-bold text-cyan" aria-hidden>{step.step}</span><h3 className="text-base font-medium text-text-1">{step.title}</h3></div><p className="mb-4 whitespace-pre-line text-sm text-text-2">{step.description}</p><div className="relative overflow-hidden rounded-token-md border border-borderSubtle bg-bg1"><Image src={step.imagePath} alt={step.title} width={800} height={450} className="h-auto w-full" unoptimized onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }} /></div></div></div>
        ))}</div>
      </section>
    </div>
  );
}
