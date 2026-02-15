# 런북 (단일 VM 올인원)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목적

운영 환경에서 “누가 하더라도 동일하게” 대응할 수 있도록, Everbit v2의 최소 운영 절차를 고정한다.

관련 문서:
- OCI 세팅: `docs/operations/oci-setup.md`
- Nginx/TLS: `docs/operations/nginx-tls.md`
- Admin Surface 정책: `docs/operations/admin-surface-policy.md`
- 배포 표준: `docs/operations/deploy.md`
- 재해 복구: `docs/operations/disaster-recovery.md`

---

## 2. Day-0 (초기 세팅) 요약

- [ ] OCI VM 생성 + NSG 규칙(80/443, 22 제한)
- [ ] OS iptables에서 80/443/22 허용(OCI Ubuntu 케이스)
- [ ] Docker 설치
- [ ] `/etc/everbit/everbit.env` 생성(600)
- [ ] docker compose(prod) 구동
- [ ] DNS `api.everbit.kr` → VM Public IP
- [ ] Let's Encrypt 발급 + 자동 갱신
- [ ] Admin 도구(Grafana/Jenkins) 외부 비공개 유지(기본)

---

## 3. 정상 상태 확인(헬스 체크)

### 3.1 외부(인터넷)
```bash
curl -I https://api.everbit.kr
```

### 3.2 서버(SSH 접속 후)
```bash
docker ps
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml logs -n 200 --tail=200 nginx
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml logs -n 200 --tail=200 server
```

### 3.3 모니터링
- Grafana에서:
  - API 5xx/4xx 비율
  - 주문 파이프라인 지연/적체
  - 429/418 발생률
  - Kafka lag(소비 지연)

---

## 4. 배포

- 배포는 `docs/operations/deploy.md`를 따른다.
- 운영에서는 **항상 Git SHA 태그**를 사용하고, 롤백 가능한 상태로 배포한다.

---

## 5. TLS(인증서) 이슈 대응

증상:
- 브라우저에서 인증서 만료/신뢰 오류
- 80/443 접근 불가

절차:
1) DNS가 맞는지 확인(A 레코드)
2) 80이 열려 있는지 확인(OCI + OS)
3) certbot 갱신 실행
4) nginx reload

예시:
```bash
docker run --rm \
  -v letsencrypt:/etc/letsencrypt \
  -v certbot-www:/var/www/certbot \
  certbot/certbot renew --webroot -w /var/www/certbot

docker exec everbit-nginx nginx -s reload
```

---

## 6. Admin 도구 접근

기본:
- SSH 터널로 접근(외부 비공개)

예시:
```bash
ssh -i ~/.ssh/<YOUR_SSH_KEY> -L 3000:127.0.0.1:3000 ubuntu@api.everbit.kr
```

예외 공개는 `docs/operations/admin-surface-policy.md`의 Break-glass 절차를 따른다.

---

## 7. 장애 대응(우선순위)

### 7.1 API 5xx 급증
1) 최근 배포가 있으면 즉시 롤백(이전 SHA)
2) 롤백 후에도 지속되면:
   - DB 연결/디스크 용량 확인
   - Kafka down 여부 확인
   - 서버 메모리/CPU 확인

### 7.2 주문 파이프라인 이상(중복/지연/429/418)
1) 계정 Kill Switch OFF(웹 UI)
2) Upbit 429/418 메트릭 확인
3) 원인:
   - 레이트리밋 스로틀링 설정 미흡
   - 네트워크 불안정으로 주문 확인 루프 과다
4) 재가동:
   - 원인 제거 후 Kill Switch ON

### 7.3 디스크 부족(가장 흔함)
- 증상: Postgres write 실패, Kafka log segment 오류, 서버 OOM
- 즉시 조치:
  - 로그/백테스트 산출물 정리(커밋 금지 경로 확인)
  - DB vacuum/retention 정책 확인
  - 필요 시 블록 볼륨 확장(Always Free 한도 내)

---

## 8. 정기 작업(주간)

- [ ] OS 보안 업데이트
- [ ] Docker 이미지/컨테이너 업데이트(필요 시)
- [ ] 인증서 만료일 확인(자동 갱신 모니터링)
- [ ] 백업 성공 여부 확인(재해 복구 문서)
- [ ] SSH 접근 로그 점검(침입 징후 확인)

---

## 9. 변경 관리

운영 스펙 변경은 ADR로 남긴다.
- 포트 공개 정책 변경
- Admin 도구 공개/접근 방식 변경
- TLS 방식 변경(예: Cloudflare, mTLS 등)
