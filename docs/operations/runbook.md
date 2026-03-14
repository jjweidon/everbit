# 런북 (E2.1.Micro + Supabase)

Status: **Ready for Operation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

목표:
- 운영 환경에서 “누가 하더라도 동일하게” 대응할 수 있도록 최소 절차를 고정한다.
- **운영 아키텍처**: Backend(OCI E2.1.Micro) + DB/Auth/Storage(Supabase). VM에 DB 컨테이너 없음.

---

## 1. Day-0 요약
- [ ] OCI VM 생성(**E2.1.Micro**) + NSG 규칙(80/443, 22 제한) — 순서: NSG 붙인 뒤 Security List 정리(`docs/operations/oci-setup.md` v3 참고)
- [ ] **Swap 2GB** 설정 + swappiness=10
- [ ] OS 방화벽(iptables) 확인
- [ ] Docker 설치
- [ ] `/etc/everbit/everbit.env` 생성(600) — Supabase 연결 정보 포함
- [ ] docker compose(prod) 구동 — **DB/Redis 서비스 없음**, Backend는 Supabase 연결
- [ ] DNS `api.everbit.kr` → VM Public IP
- [ ] Let's Encrypt 발급 + 자동 갱신
- [ ] Admin 도구 외부 비공개 유지(SSH 터널)

---

## 2. 정상 상태 확인(헬스 체크)

### 2.1 외부
```bash
curl -I https://api.everbit.kr
```

### 2.2 서버(SSH 후)
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml ps
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml logs --tail=200 nginx
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml logs --tail=200 server
```

---

## 3. Admin 도구 접근(기본)
- SSH 터널(표준): `docs/operations/admin-surface-policy.md`

---

## 4. TLS 이슈 대응

증상:
- 인증서 만료/신뢰 오류
- 80/443 접근 불가

절차:
1) DNS(A 레코드) 확인
2) OCI/iptables에서 80/443 허용 확인
3) certbot renew 실행
4) nginx reload

```bash
docker run --rm   -v letsencrypt:/etc/letsencrypt   -v certbot-www:/var/www/certbot   certbot/certbot renew --webroot -w /var/www/certbot

docker compose -f docker/compose.yaml -f docker/compose.prod.yaml exec nginx nginx -s reload
```

---

## 5. 장애 대응(우선순위)

### 5.1 API 5xx 급증
1) 최근 배포가 있으면 즉시 롤백
2) 지속되면:
   - **DB 연결(Supabase)** / 디스크 용량
   - Outbox/Queue backlog(적체) + 워커 처리 지연
   - JVM 메모리/CPU(OOM 시 swap 확인)

### 5.2 주문 파이프라인 이상(429/418/UNKNOWN/중복 의심)
1) 계정 Kill Switch OFF(웹 UI)
2) 429/418/UNKNOWN 메트릭 및 로그 확인
3) 원인 제거 후 재개(Kill Switch ON)

### 5.3 디스크 부족
- VM에는 DB 데이터가 없으므로 디스크 부담은 로그/백테스트 산출물/이미지가 주 원인이다.
- 로그/백테스트 산출물/불필요 이미지 정리.
- **DB 백업**은 Supabase 측(PITR/백업) 정책을 확인한다. (`docs/operations/disaster-recovery.md`)

---

## 6. 정기 작업(주간)
- [ ] OS 보안 업데이트
- [ ] 이미지/컨테이너 업데이트(필요 시)
- [ ] 인증서 만료일 확인(자동 갱신 모니터링)
- [ ] 백업 성공 여부 확인(DR 문서)
- [ ] SSH 접근 로그 점검

---

## 7. 변경 관리
아래는 ADR로 남긴다.
- 포트 공개 정책 변경
- Admin 도구 공개/접근 방식 변경
- 인증/세션 전략 변경
