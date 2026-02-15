# Nginx + TLS(HTTPS) 세팅 표준 (api.everbit.kr)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

- `api.everbit.kr`에 대해 **HTTPS를 필수**로 강제한다.
- TLS는 **Let's Encrypt**로 발급하고, 갱신은 자동화한다.
- 외부 공개 포트는 80/443만 허용한다(SSH는 별도 정책).

---

## 2. DNS 표준 (everbit.kr / api.everbit.kr)

- `everbit.kr`: Vercel (Next.js)
- `api.everbit.kr`: OCI VM Public IP (A 레코드)

Vercel이 DNS를 관리하는 경우에도, 서브도메인을 외부 서비스로 보낼 수 있다(A/CNAME 레코드 추가).
레퍼런스:
- Vercel: subdomain을 외부 서비스로 pointing: https://vercel.com/kb/guide/pointing-subdomains-to-external-services

운영 규칙:
- `api.everbit.kr`은 **반드시 단일 IPv4(A 레코드)**로 유지
  - 여러 IP가 섞이면 Certbot 인증/갱신에서 실패 가능성이 올라간다(HTTP-01).

---

## 3. Nginx Reverse Proxy 표준

### 3.1 라우팅 규칙(고정)
- `api.everbit.kr`에서:
  - `/.well-known/acme-challenge/` → 인증서 발급용 webroot
  - `/api/` → Spring Boot
  - 그 외 → 404 또는 health 안내(선택)

> 중요한 원칙: “인증서 발급 경로”는 항상 80에서 접근 가능해야 한다.

### 3.2 권장 설정(핵심 헤더)
- `X-Forwarded-Proto`, `X-Forwarded-For`, `Host` 전달
- 요청 바디 크기 제한(예: 1~5MB 수준)
- timeout 설정(Upbit 호출/백테스트 API 등이 길어질 수 있으니, API별로 분리 권장)

---

## 4. Let's Encrypt 발급/갱신 (Certbot)

Certbot은 Nginx 설정을 읽어 자동 구성하거나(`--nginx`), 인증서만 발급(`certonly`)할 수 있다.
레퍼런스:
- Certbot Nginx instructions: https://certbot.eff.org/instructions?os=ubuntufocal&ws=nginx
- Certbot manpage: https://eff-certbot.readthedocs.io/en/stable/man/certbot.html

운영 표준(단일 VM + Docker 구성에 맞춘 방식):
- **인증서 파일은 호스트 또는 도커 볼륨에 보관**
- Nginx는 `/var/www/certbot`을 webroot로 서빙
- Certbot은 `--webroot` 방식으로 인증서만 발급/갱신

### 4.1 사전 조건
- DNS A 레코드가 VM Public IP를 가리킨다.
- OCI/OS 방화벽에서 80/tcp 허용(HTTP-01 챌린지 필요).
- Nginx가 80에서 `/.well-known/acme-challenge/`를 서빙한다.

### 4.2 발급(예시: webroot)
(도커로 certbot 실행하는 예시 — 볼륨 이름은 compose에 맞게 조정)

```bash
docker run --rm -it \
  -v letsencrypt:/etc/letsencrypt \
  -v certbot-www:/var/www/certbot \
  certbot/certbot \
  certonly --webroot \
  -w /var/www/certbot \
  -d api.everbit.kr \
  --email <YOUR_EMAIL> \
  --agree-tos \
  --no-eff-email
```

### 4.3 갱신(크론/시스템 타이머)
갱신은 1일 1회 체크로 충분하다(실제 갱신은 만료 임박시에만 수행).

```bash
docker run --rm \
  -v letsencrypt:/etc/letsencrypt \
  -v certbot-www:/var/www/certbot \
  certbot/certbot \
  renew --webroot -w /var/www/certbot
```

갱신 후 Nginx reload:
```bash
docker exec everbit-nginx nginx -s reload
```

---

## 5. TLS 보안 설정(최소)

운영 최소 기준:
- 80 → 443 리다이렉트 강제
- TLSv1.2/TLSv1.3만 허용
- HSTS(선택): 운영 안정화 후 적용(실수 시 롤백이 번거로움)

---

## 6. Nginx에서 “운영 민감 경로” 차단 규칙

### 6.1 Actuator
- 외부에는 기본적으로 노출하지 않는다.
- Prometheus는 내부 도커 네트워크에서 Spring을 직접 스크랩한다.

정책:
- `/actuator/**`는 전면 차단(또는 allowlist로만 허용)
- `/actuator/prometheus`도 외부 노출 금지

---

## 7. 운영 검증

### 7.1 외부에서
```bash
curl -I http://api.everbit.kr
curl -I https://api.everbit.kr
```

확인 포인트:
- http는 https로 301/308 리다이렉트
- https는 200/401/404 등 정상 응답(서버가 살아있다는 의미)

### 7.2 인증서 만료 확인
```bash
echo | openssl s_client -servername api.everbit.kr -connect api.everbit.kr:443 2>/dev/null | openssl x509 -noout -dates
```

---

## 8. 다음 문서

- `docs/operations/admin-surface-policy.md` (Grafana/Jenkins 노출 정책)
- `docs/operations/deploy.md` (배포 표준)
