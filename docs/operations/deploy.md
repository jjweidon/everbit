# 배포 표준 (단일 VM + Docker Compose)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

- v2 운영 배포를 “반복 가능하고”, “롤백 가능”하게 고정한다.
- 단일 VM이므로 무중단은 목표로 하지 않는다. 대신 **안전한 재시작 + 빠른 롤백**을 목표로 한다.

---

## 2. 배포 전제(고정)

- 운영 VM에는 docker + docker compose가 설치되어 있다.
- 운영 환경변수는 `/etc/everbit/everbit.env`(root-only, 600)에서 주입한다.
- 외부 노출은 Nginx(80/443)만 한다.
- 관측은 Prometheus/Grafana(내부)로 한다.

---

## 3. 이미지/태그 전략(고정)

권장:
- Git SHA 기반 태그: `server:<gitsha>`
- “latest 태그는 운영에서 사용하지 않는다”
- 롤백은 “이전 SHA”로 재배포한다.

(선택) 레지스트리:
- GHCR(GitHub Container Registry) 또는 Docker Hub

---

## 4. 배포 방식

Everbit v2는 배포 방식을 2개 허용한다.

### 4.1 방식 A: 수동 배포(초기/안정화 단계 권장)
1) VM 접속
```bash
ssh -i ~/.ssh/<YOUR_SSH_KEY> ubuntu@api.everbit.kr
```

2) 코드/compose 업데이트
- repo pull 또는 배포용 디렉토리에 compose 파일 반영

3) 이미지 pull
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml pull
```

4) 적용
```bash
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml up -d
```

5) 헬스 체크
- Nginx 443 응답
- Spring health/metrics(내부)
- Kafka/DB 컨테이너 상태

6) 롤백(필요 시)
- compose에서 이미지 태그를 이전 SHA로 변경 후 `up -d` 재실행

---

### 4.2 방식 B: Jenkins 기반 배포(운영 성숙 단계)
Jenkins는 운영 VM에 올릴 수 있으나, **외부 노출은 기본 금지**(Admin Surface 정책 준수).

권장 파이프라인:
1) PR 머지 → 테스트/빌드
2) Docker 이미지 빌드
3) 레지스트리 push
4) VM에 SSH로 접속하여 `docker compose pull && up -d`

필수 보안:
- Jenkins Credential Store에만 시크릿 저장(커밋 금지)
- Jenkins 접근은 SSH 터널(기본) 또는 Nginx allowlist + 인증(예외)

레퍼런스(공식):
- Jenkins reverse proxy 및 접근 제한은 공식 문서에서 언급: https://www.jenkins.io/doc/book/security/access-control/

---

## 5. 운영 점검(배포 후)

### 5.1 외부(인터넷)
```bash
curl -I https://api.everbit.kr
```

### 5.2 내부(서버)
```bash
docker ps
docker compose logs -n 200 --tail=200 server
docker compose logs -n 200 --tail=200 nginx
```

### 5.3 모니터링
- Grafana 대시보드에서:
  - API error rate
  - 주문 파이프라인 지연
  - 429/418 발생률
  - Kafka consumer lag

---

## 6. 실패/사고 대응 가이드(요약)

- 배포 후 5xx 급증:
  - 즉시 롤백(이전 SHA)
  - 롤백 후에도 동일하면 인프라/DB 문제로 분기(runbook 참고)

- 인증서 이슈:
  - `nginx-tls.md`에 따라 certbot renew + nginx reload

상세는 `docs/operations/runbook.md`를 따른다.
